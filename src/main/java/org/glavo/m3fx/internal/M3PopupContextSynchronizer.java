// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/// Synchronizes copied stylesheet and theme context for popup-hosted roots while they remain visible.
///
/// JavaFX popup content lives in a separate scene after it is shown, so ordinary scene inheritance is not enough
/// for Material token lookups. This helper mirrors the owner scene stylesheet list and the nearest M3FX theme root
/// into a detached popup root, then keeps that copy fresh when the owner scene, stylesheet list, or installed theme
/// metadata changes during the popup lifetime.
@NotNullByDefault
public final class M3PopupContextSynchronizer {
    /// The control or item that owns the popup content.
    private final Node owner;

    /// The detached popup content root that receives copied context.
    private final Parent popupRoot;

    /// Supplies the stylesheet list that should be mirrored into the popup root.
    private final Supplier<@Nullable ObservableList<String>> stylesheetSourceSupplier;

    /// Supplies the currently active theme root for popup token declarations.
    private final Supplier<@Nullable Parent> themeRootSupplier;

    /// Additional popup-control stylesheet URLs appended after owner stylesheets.
    private final String @Unmodifiable [] controlStylesheets;

    /// Handles owner scene changes.
    private final ChangeListener<@Nullable Scene> ownerSceneListener = this::handleOwnerSceneChanged;

    /// Handles owner parent-chain changes.
    private final ChangeListener<@Nullable Parent> ownerParentListener = this::handleOwnerParentChanged;

    /// Handles root changes on the current owner scene.
    private final ChangeListener<Parent> sceneRootListener = this::handleSceneRootChanged;

    /// Handles stylesheet list mutations from the current stylesheet source.
    private final ListChangeListener<String> stylesheetSourceListener = change -> sync();

    /// Handles theme metadata changes on the owner scene root.
    private final MapChangeListener<Object, Object> sceneRootPropertiesListener = this::handleThemeRootPropertiesChanged;

    /// Handles theme metadata changes on the currently resolved active theme root.
    private final MapChangeListener<Object, Object> activeThemeRootPropertiesListener =
            this::handleThemeRootPropertiesChanged;

    /// Handles theme metadata changes on owner ancestors that may become the active local theme root.
    private final MapChangeListener<Object, Object> ancestorThemeRootPropertiesListener =
            this::handleThemeRootPropertiesChanged;

    /// Handles parent-chain changes on owner ancestors while a popup remains visible.
    private final ChangeListener<@Nullable Parent> ancestorParentListener = this::handleOwnerParentChanged;

    /// The stylesheet list currently observed for mutations.
    private @Nullable ObservableList<String> observedStylesheetSource;

    /// The scene currently observed for root changes.
    private @Nullable Scene observedScene;

    /// The scene root currently observed for theme metadata changes.
    private @Nullable Parent observedSceneRoot;

    /// The active theme root currently observed for direct theme metadata changes.
    private @Nullable Parent observedThemeRoot;

    /// Owner ancestors currently observed for future local theme installation.
    private final List<Parent> observedAncestorThemeRoots = new ArrayList<>();

    /// Whether listeners are currently registered.
    private boolean running;

    /// Creates a synchronizer that mirrors the owner scene stylesheets and nearest theme root.
    ///
    /// @param owner the node that owns the popup content
    /// @param popupRoot the detached popup root to synchronize
    /// @param controlStylesheets popup-specific control stylesheet URLs
    public M3PopupContextSynchronizer(Node owner, Parent popupRoot, String... controlStylesheets) {
        this(
                owner,
                popupRoot,
                () -> {
                    @Nullable Scene scene = owner.getScene();
                    return scene == null ? null : scene.getStylesheets();
                },
                () -> M3ThemeResolver.findThemeRoot(owner),
                controlStylesheets
        );
    }

    /// Creates a synchronizer with custom stylesheet and theme-root suppliers.
    ///
    /// @param owner the node that owns the popup content
    /// @param popupRoot the detached popup root to synchronize
    /// @param stylesheetSourceSupplier supplies the stylesheet list mirrored into the popup root
    /// @param themeRootSupplier supplies the current theme root copied into the popup root
    /// @param controlStylesheets popup-specific control stylesheet URLs
    public M3PopupContextSynchronizer(
            Node owner,
            Parent popupRoot,
            Supplier<@Nullable ObservableList<String>> stylesheetSourceSupplier,
            Supplier<@Nullable Parent> themeRootSupplier,
            String... controlStylesheets
    ) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.popupRoot = Objects.requireNonNull(popupRoot, "popupRoot");
        this.stylesheetSourceSupplier = Objects.requireNonNull(stylesheetSourceSupplier, "stylesheetSourceSupplier");
        this.themeRootSupplier = Objects.requireNonNull(themeRootSupplier, "themeRootSupplier");
        this.controlStylesheets = Objects.requireNonNull(controlStylesheets, "controlStylesheets").clone();
        for (String stylesheet : this.controlStylesheets) {
            Objects.requireNonNull(stylesheet, "stylesheet");
        }
    }

    /// Starts observing owner context and immediately synchronizes the popup root.
    public void start() {
        if (running) {
            sync();
            return;
        }

        running = true;
        owner.sceneProperty().addListener(ownerSceneListener);
        owner.parentProperty().addListener(ownerParentListener);
        refreshObservedThemeRoots();
        sync();
    }

    /// Stops observing owner context while leaving the popup root styled with its last synchronized values.
    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        owner.sceneProperty().removeListener(ownerSceneListener);
        owner.parentProperty().removeListener(ownerParentListener);
        updateObservedStylesheetSource(null);
        updateObservedScene(null);
        updateObservedSceneRoot(null);
        updateObservedThemeRoot(null);
        clearObservedAncestorThemeRoots();
    }

    /// Copies the latest owner stylesheet and theme context into the popup root.
    public void sync() {
        @Nullable ObservableList<String> stylesheetSource = stylesheetSourceSupplier.get();
        @Nullable Parent themeRoot = themeRootSupplier.get();
        if (running) {
            updateObservedStylesheetSource(stylesheetSource);
            refreshObservedThemeRoots(themeRoot);
        }

        M3PopupStyles.preparePopupRoot(
                popupRoot,
                stylesheetSource == null ? List.of() : stylesheetSource,
                themeRoot,
                controlStylesheets
        );
        popupRoot.applyCss();
        popupRoot.layout();
    }

    /// Handles owner scene changes by refreshing observed roots and copying the new context.
    private void handleOwnerSceneChanged(
            javafx.beans.value.ObservableValue<? extends @Nullable Scene> observable,
            @Nullable Scene oldScene,
            @Nullable Scene newScene
    ) {
        sync();
    }

    /// Handles owner parent-chain changes by refreshing observed theme roots.
    private void handleOwnerParentChanged(
            javafx.beans.value.ObservableValue<? extends @Nullable Parent> observable,
            @Nullable Parent oldParent,
            @Nullable Parent newParent
    ) {
        sync();
    }

    /// Handles scene root changes by copying context from the new root.
    private void handleSceneRootChanged(
            javafx.beans.value.ObservableValue<? extends Parent> observable,
            Parent oldRoot,
            Parent newRoot
    ) {
        sync();
    }

    /// Handles installed-theme metadata changes on observed roots.
    private void handleThemeRootPropertiesChanged(MapChangeListener.Change<?, ?> change) {
        if (Objects.equals(M3ThemeManager.THEME_PROPERTY_KEY, change.getKey())) {
            sync();
        }
    }

    /// Updates the observed stylesheet list.
    private void updateObservedStylesheetSource(@Nullable ObservableList<String> stylesheetSource) {
        if (observedStylesheetSource == stylesheetSource) {
            return;
        }
        if (observedStylesheetSource != null) {
            observedStylesheetSource.removeListener(stylesheetSourceListener);
        }
        observedStylesheetSource = stylesheetSource;
        if (observedStylesheetSource != null) {
            observedStylesheetSource.addListener(stylesheetSourceListener);
        }
    }

    /// Refreshes every theme-related root observer from the current owner state.
    private void refreshObservedThemeRoots() {
        refreshObservedThemeRoots(themeRootSupplier.get());
    }

    /// Refreshes every theme-related root observer using a known active theme root.
    private void refreshObservedThemeRoots(@Nullable Parent themeRoot) {
        updateObservedScene(owner.getScene());
        updateObservedSceneRoot(observedScene == null ? null : observedScene.getRoot());
        updateObservedThemeRoot(themeRoot);
        updateObservedAncestorThemeRoots();
    }

    /// Updates the observed scene.
    private void updateObservedScene(@Nullable Scene scene) {
        if (observedScene == scene) {
            return;
        }
        if (observedScene != null) {
            observedScene.rootProperty().removeListener(sceneRootListener);
        }
        observedScene = scene;
        if (observedScene != null) {
            observedScene.rootProperty().addListener(sceneRootListener);
        }
    }

    /// Updates the observed scene root.
    private void updateObservedSceneRoot(@Nullable Parent sceneRoot) {
        if (observedSceneRoot == sceneRoot) {
            return;
        }
        if (observedSceneRoot != null) {
            observedSceneRoot.getProperties().removeListener(sceneRootPropertiesListener);
        }
        observedSceneRoot = sceneRoot;
        if (observedSceneRoot != null) {
            observedSceneRoot.getProperties().addListener(sceneRootPropertiesListener);
        }
    }

    /// Updates the observed active theme root.
    private void updateObservedThemeRoot(@Nullable Parent themeRoot) {
        @Nullable Parent nextThemeRoot = themeRoot == observedSceneRoot ? null : themeRoot;
        if (observedThemeRoot == nextThemeRoot) {
            return;
        }
        if (observedThemeRoot != null) {
            observedThemeRoot.getProperties().removeListener(activeThemeRootPropertiesListener);
        }
        observedThemeRoot = nextThemeRoot;
        if (observedThemeRoot != null) {
            observedThemeRoot.getProperties().addListener(activeThemeRootPropertiesListener);
        }
    }

    /// Updates the observed owner ancestors that could receive a local theme after the popup is already open.
    private void updateObservedAncestorThemeRoots() {
        clearObservedAncestorThemeRoots();
        @Nullable Node current = owner;
        while (current != null) {
            if (current instanceof Parent parent
                    && parent != observedSceneRoot
                    && parent != observedThemeRoot) {
                parent.getProperties().addListener(ancestorThemeRootPropertiesListener);
                parent.parentProperty().addListener(ancestorParentListener);
                observedAncestorThemeRoots.add(parent);
            }
            current = current.getParent();
        }
    }

    /// Removes local-theme listeners from all previously observed owner ancestors.
    private void clearObservedAncestorThemeRoots() {
        for (Parent parent : observedAncestorThemeRoots) {
            parent.getProperties().removeListener(ancestorThemeRootPropertiesListener);
            parent.parentProperty().removeListener(ancestorParentListener);
        }
        observedAncestorThemeRoots.clear();
    }
}
