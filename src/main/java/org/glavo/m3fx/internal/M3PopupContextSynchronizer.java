// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/// Synchronizes detached popup context for popup-hosted roots while they remain visible.
///
/// JavaFX popup content lives in a separate scene after it is shown, so ordinary scene inheritance is not enough
/// for Material token lookups. This helper mirrors the owner scene stylesheet list, nearest M3FX theme root,
/// effective node orientation, and resolved motion settings into a detached popup root, then keeps that copy fresh
/// when owner scene, stylesheet, theme, orientation, or motion settings change during the popup lifetime.
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

    /// The popup-control stylesheet appended after owner stylesheets, or `null` when none is needed.
    private final @Nullable String controlStylesheet;

    /// Runtime listeners and ancestor caches, created only after this synchronizer is first started.
    private @Nullable Observation observation;

    /// Whether listeners are currently registered.
    private boolean running;

    /// Whether a synchronization pass is already in progress.
    private boolean syncing;

    /// Whether the current observation run has copied context from an attached owner scene.
    private boolean hasSyncedAttachedOwnerContext;

    /// Whether [syncedAnimationsEnabled], [syncedMotionScheme], and [syncedMotionBehavior] contain a snapshot.
    private boolean hasSyncedMotionContext;

    /// Last owner-resolved animation switch copied to the popup root.
    private boolean syncedAnimationsEnabled;

    /// Last owner-resolved motion scheme copied to the popup root.
    private @Nullable M3MotionScheme syncedMotionScheme;

    /// Last owner-resolved motion behavior copied to the popup root.
    private @Nullable M3MotionBehavior syncedMotionBehavior;

    /// Creates a synchronizer that mirrors the owner scene stylesheets and nearest theme root.
    ///
    /// @param owner the node that owns the popup content
    /// @param popupRoot the detached popup root to synchronize
    public M3PopupContextSynchronizer(Node owner, Parent popupRoot) {
        this(
                owner,
                popupRoot,
                () -> {
                    @Nullable Scene scene = owner.getScene();
                    return scene == null ? null : scene.getStylesheets();
                },
                () -> M3ThemeResolver.findThemeRoot(owner)
        );
    }

    /// Creates a synchronizer that mirrors owner context and appends one popup-control stylesheet.
    ///
    /// @param owner the node that owns the popup content
    /// @param popupRoot the detached popup root to synchronize
    /// @param controlStylesheet the popup-specific control stylesheet URL
    public M3PopupContextSynchronizer(Node owner, Parent popupRoot, String controlStylesheet) {
        this(
                owner,
                popupRoot,
                () -> {
                    @Nullable Scene scene = owner.getScene();
                    return scene == null ? null : scene.getStylesheets();
                },
                () -> M3ThemeResolver.findThemeRoot(owner),
                Objects.requireNonNull(controlStylesheet, "controlStylesheet")
        );
    }

    /// Creates a synchronizer with custom stylesheet and theme-root suppliers.
    ///
    /// @param owner the node that owns the popup content
    /// @param popupRoot the detached popup root to synchronize
    /// @param stylesheetSourceSupplier supplies the stylesheet list mirrored into the popup root
    /// @param themeRootSupplier supplies the current theme root copied into the popup root
    public M3PopupContextSynchronizer(
            Node owner,
            Parent popupRoot,
            Supplier<@Nullable ObservableList<String>> stylesheetSourceSupplier,
            Supplier<@Nullable Parent> themeRootSupplier
    ) {
        this(owner, popupRoot, stylesheetSourceSupplier, themeRootSupplier, null);
    }

    /// Creates a synchronizer with custom context suppliers and an optional popup-control stylesheet.
    ///
    /// @param owner the node that owns the popup content
    /// @param popupRoot the detached popup root to synchronize
    /// @param stylesheetSourceSupplier supplies the stylesheet list mirrored into the popup root
    /// @param themeRootSupplier supplies the current theme root copied into the popup root
    /// @param controlStylesheet the popup-specific control stylesheet URL, or `null` when none is needed
    public M3PopupContextSynchronizer(
            Node owner,
            Parent popupRoot,
            Supplier<@Nullable ObservableList<String>> stylesheetSourceSupplier,
            Supplier<@Nullable Parent> themeRootSupplier,
            @Nullable String controlStylesheet
    ) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.popupRoot = Objects.requireNonNull(popupRoot, "popupRoot");
        this.stylesheetSourceSupplier = Objects.requireNonNull(stylesheetSourceSupplier, "stylesheetSourceSupplier");
        this.themeRootSupplier = Objects.requireNonNull(themeRootSupplier, "themeRootSupplier");
        this.controlStylesheet = controlStylesheet;
    }

    /// Starts observing owner context and immediately synchronizes the popup root.
    public void start() {
        if (running) {
            sync();
            return;
        }

        running = true;
        hasSyncedAttachedOwnerContext = false;
        Observation currentObservation = observation;
        if (currentObservation == null) {
            currentObservation = new Observation();
            observation = currentObservation;
        }
        try {
            currentObservation.start();
            sync();
        } catch (RuntimeException | Error exception) {
            running = false;
            currentObservation.stop();
            clearSyncedMotionContext();
            throw exception;
        }
    }

    /// Stops observing owner context while leaving the popup root styled with its last synchronized values.
    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        Observation currentObservation = observation;
        if (currentObservation != null) {
            currentObservation.stop();
        }
        hasSyncedAttachedOwnerContext = false;
        clearSyncedMotionContext();
    }

    /// Copies the latest owner stylesheet, theme, orientation, and motion context into the popup root.
    public void sync() {
        if (syncing) {
            return;
        }
        if (running && owner.getScene() == null && hasSyncedAttachedOwnerContext) {
            return;
        }

        syncing = true;
        try {
            @Nullable ObservableList<String> stylesheetSource = stylesheetSourceSupplier.get();
            @Nullable Parent themeRoot = themeRootSupplier.get();
            if (running) {
                Observation currentObservation = observation;
                if (currentObservation != null) {
                    currentObservation.refreshThemeRoots(stylesheetSource, themeRoot);
                }
            }

            syncNodeOrientation();
            syncMotionContext();
            M3PopupStyles.preparePopupRoot(
                    popupRoot,
                    stylesheetSource == null ? List.of() : stylesheetSource,
                    themeRoot,
                    controlStylesheet
            );
            popupRoot.applyCss();
            popupRoot.layout();
            if (owner.getScene() != null) {
                hasSyncedAttachedOwnerContext = true;
            }
        } finally {
            syncing = false;
        }
    }

    /// Mirrors the owner's effective orientation when the popup root is not already bound by its owner control.
    private void syncNodeOrientation() {
        if (popupRoot.nodeOrientationProperty().isBound()) {
            return;
        }

        NodeOrientation ownerOrientation = owner.getEffectiveNodeOrientation();
        if (popupRoot.getNodeOrientation() != ownerOrientation) {
            popupRoot.setNodeOrientation(ownerOrientation);
        }
    }

    /// Updates orientation-dependent popup CSS and layout without rebuilding unrelated theme context.
    private void syncNodeOrientationAndLayout() {
        if (syncing) {
            return;
        }

        syncing = true;
        try {
            syncNodeOrientation();
            popupRoot.applyCss();
            popupRoot.layout();
        } finally {
            syncing = false;
        }
    }

    /// Copies the owner's resolved motion settings into the popup root and records the copied context.
    private void syncMotionContext() {
        boolean animationsEnabled = M3MotionSettings.areAnimationsEnabled(owner);
        M3MotionScheme motionScheme = M3Animation.motionScheme(owner);
        M3MotionBehavior motionBehavior = M3Animation.motionBehavior(owner);

        syncMotionContext(animationsEnabled, motionScheme, motionBehavior);
    }

    /// Copies already resolved motion settings into the popup root.
    private void syncMotionContext(
            boolean animationsEnabled,
            M3MotionScheme motionScheme,
            M3MotionBehavior motionBehavior
    ) {
        cacheSyncedMotionContext(animationsEnabled, motionScheme, motionBehavior);
        M3MotionSettings.setAnimationsEnabled(popupRoot, animationsEnabled);
        M3MotionSettings.setMotionScheme(popupRoot, motionScheme);
        M3MotionSettings.setMotionBehavior(popupRoot, motionBehavior);
    }

    /// Synchronizes only when a settings notification changes this owner's resolved motion context.
    private void syncIfMotionContextChanged() {
        if (syncing) {
            return;
        }

        boolean animationsEnabled = M3MotionSettings.areAnimationsEnabled(owner);
        M3MotionScheme motionScheme = M3Animation.motionScheme(owner);
        M3MotionBehavior motionBehavior = M3Animation.motionBehavior(owner);
        if (hasSyncedMotionContext
                && syncedAnimationsEnabled == animationsEnabled
                && Objects.equals(syncedMotionScheme, motionScheme)
                && Objects.equals(syncedMotionBehavior, motionBehavior)) {
            return;
        }

        syncing = true;
        try {
            syncMotionContext(animationsEnabled, motionScheme, motionBehavior);
        } finally {
            syncing = false;
        }
    }

    /// Records the owner-resolved motion settings copied during the latest synchronization pass.
    private void cacheSyncedMotionContext(
            boolean animationsEnabled,
            M3MotionScheme motionScheme,
            M3MotionBehavior motionBehavior
    ) {
        hasSyncedMotionContext = true;
        syncedAnimationsEnabled = animationsEnabled;
        syncedMotionScheme = motionScheme;
        syncedMotionBehavior = motionBehavior;
    }

    /// Clears the cached owner-resolved motion context after listener teardown.
    private void clearSyncedMotionContext() {
        hasSyncedMotionContext = false;
        syncedMotionScheme = null;
        syncedMotionBehavior = null;
    }

    /// Owns listeners and mutable lookup caches used only while popup context is observed.
    @NotNullByDefault
    private final class Observation {
        /// Handles owner scene changes.
        private final ChangeListener<@Nullable Scene> ownerSceneListener =
                (observable, oldScene, newScene) -> sync();

        /// Handles owner parent-chain changes.
        private final ChangeListener<@Nullable Parent> ownerParentListener =
                (observable, oldParent, newParent) -> sync();

        /// Handles owner effective orientation changes.
        private final ChangeListener<NodeOrientation> ownerOrientationListener =
                (observable, oldValue, newValue) -> syncNodeOrientationAndLayout();

        /// Handles global and node-local motion setting changes.
        private final InvalidationListener motionSettingsListener = observable -> syncIfMotionContextChanged();

        /// Handles root changes on the current owner scene.
        private final ChangeListener<Parent> sceneRootListener =
                (observable, oldRoot, newRoot) -> sync();

        /// Handles stylesheet list mutations from the current stylesheet source.
        private final ListChangeListener<String> stylesheetSourceListener = change -> sync();

        /// Handles theme metadata changes on the owner scene root.
        private final MapChangeListener<Object, Object> sceneRootPropertiesListener =
                this::handleThemeRootPropertiesChanged;

        /// Handles theme metadata changes on the currently resolved active theme root.
        private final MapChangeListener<Object, Object> activeThemeRootPropertiesListener =
                this::handleThemeRootPropertiesChanged;

        /// Handles theme metadata changes on owner ancestors that may become the active local theme root.
        private final MapChangeListener<Object, Object> ancestorThemeRootPropertiesListener =
                this::handleThemeRootPropertiesChanged;

        /// Handles parent-chain changes on owner ancestors while a popup remains visible.
        private final ChangeListener<@Nullable Parent> ancestorParentListener =
                (observable, oldParent, newParent) -> sync();

        /// The stylesheet list currently observed for mutations.
        private @Nullable ObservableList<String> observedStylesheetSource;

        /// The scene currently observed for root changes.
        private @Nullable Scene observedScene;

        /// The scene root currently observed for theme metadata changes.
        private @Nullable Parent observedSceneRoot;

        /// The active theme root currently observed for direct theme metadata changes.
        private @Nullable Parent observedThemeRoot;

        /// Owner ancestors currently observed for future local theme installation.
        private ArrayList<Parent> observedAncestorThemeRoots = new ArrayList<>();

        /// Reusable storage for collecting the current owner ancestor chain.
        private ArrayList<Parent> ancestorThemeRootsScratch = new ArrayList<>();

        /// Installs owner and global listeners before the first synchronization pass.
        private void start() {
            owner.sceneProperty().addListener(ownerSceneListener);
            owner.parentProperty().addListener(ownerParentListener);
            owner.effectiveNodeOrientationProperty().addListener(ownerOrientationListener);
            M3MotionSettings.addSettingsChangeListener(motionSettingsListener);
        }

        /// Removes every external listener and clears observed object references.
        private void stop() {
            owner.sceneProperty().removeListener(ownerSceneListener);
            owner.parentProperty().removeListener(ownerParentListener);
            owner.effectiveNodeOrientationProperty().removeListener(ownerOrientationListener);
            M3MotionSettings.removeSettingsChangeListener(motionSettingsListener);
            updateObservedStylesheetSource(null);
            updateObservedScene(null);
            updateObservedSceneRoot(null);
            updateObservedThemeRoot(null);
            clearObservedAncestorThemeRoots();
        }

        /// Handles installed-theme metadata changes on observed roots.
        private void handleThemeRootPropertiesChanged(MapChangeListener.Change<?, ?> change) {
            if (M3ThemeMetadata.isThemePropertyKey(change.getKey())) {
                sync();
            }
        }

        /// Refreshes stylesheet and theme-related observers from one resolved context snapshot.
        private void refreshThemeRoots(
                @Nullable ObservableList<String> stylesheetSource,
                @Nullable Parent themeRoot
        ) {
            updateObservedStylesheetSource(stylesheetSource);
            updateObservedScene(owner.getScene());
            updateObservedSceneRoot(observedScene == null ? null : observedScene.getRoot());
            updateObservedThemeRoot(themeRoot);
            updateObservedAncestorThemeRoots();
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

        /// Updates owner ancestors that could receive a local theme while the popup remains open.
        private void updateObservedAncestorThemeRoots() {
            ancestorThemeRootsScratch.clear();
            @Nullable Node current = owner;
            while (current != null) {
                if (current instanceof Parent parent
                        && parent != observedSceneRoot
                        && parent != observedThemeRoot) {
                    ancestorThemeRootsScratch.add(parent);
                }
                current = current.getParent();
            }

            boolean unchanged = observedAncestorThemeRoots.size() == ancestorThemeRootsScratch.size();
            for (int index = 0; unchanged && index < observedAncestorThemeRoots.size(); index++) {
                unchanged = observedAncestorThemeRoots.get(index) == ancestorThemeRootsScratch.get(index);
            }
            if (unchanged) {
                ancestorThemeRootsScratch.clear();
                return;
            }

            for (Parent parent : observedAncestorThemeRoots) {
                parent.getProperties().removeListener(ancestorThemeRootPropertiesListener);
                parent.parentProperty().removeListener(ancestorParentListener);
            }
            for (Parent parent : ancestorThemeRootsScratch) {
                parent.getProperties().addListener(ancestorThemeRootPropertiesListener);
                parent.parentProperty().addListener(ancestorParentListener);
            }

            ArrayList<Parent> previousRoots = observedAncestorThemeRoots;
            observedAncestorThemeRoots = ancestorThemeRootsScratch;
            ancestorThemeRootsScratch = previousRoots;
            ancestorThemeRootsScratch.clear();
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
}
