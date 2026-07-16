// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.internal.theme.M3ThemeCssCompiler;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A JavaFX dialog that uses an [M3DialogPane] by default.
///
/// `M3Dialog` keeps the standard JavaFX [Dialog] lifecycle, result conversion, modality, ownership, and button
/// handling while installing a Material Design 3 dialog pane. It can inherit the theme from an owner window or
/// accept an explicit [org.glavo.m3fx.theme.M3Theme] so dialogs opened from popups or secondary windows retain
/// the same color and typography context.
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
///
/// @param <R> the dialog result type
@NotNullByDefault
public class M3Dialog<R> extends Dialog<R> {
    /// The property key that stores the dialog pane style before theme declarations were added.
    private static final String BASE_STYLE_PROPERTY_KEY = M3Dialog.class.getName() + ".baseStyle";

    /// The property key that stores the generated theme stylesheet installed on the dialog pane.
    private static final String THEME_STYLESHEET_PROPERTY_KEY =
            M3Dialog.class.getName() + ".themeStylesheet";

    // The explicit theme applied directly to the dialog pane.
    private final ObjectProperty<@Nullable M3Theme> theme = new SimpleObjectProperty<>(this, "theme") {
        /// Applies theme declarations to the Material dialog pane.
        @Override
        protected void invalidated() {
            applyEffectiveTheme();
        }
    };

    /// The node whose local hierarchy supplies inherited theme context.
    private @Nullable Node ownerNode;

    /// Handles owner-node scene changes while this dialog is observing inherited theme context.
    private final ChangeListener<@Nullable Scene> ownerNodeSceneListener =
            (observable, oldScene, newScene) -> refreshInheritedThemeContextAndApplyTheme();

    /// Handles owner-node direct parent changes while this dialog is observing inherited theme context.
    private final ChangeListener<@Nullable Parent> ownerNodeParentListener =
            (observable, oldParent, newParent) -> refreshInheritedThemeContextAndApplyTheme();

    /// Handles owner-node effective orientation changes while this dialog is showing.
    private final ChangeListener<NodeOrientation> ownerNodeOrientationListener =
            (observable, oldValue, newValue) -> syncOwnerNodeOrientation();

    /// Handles owner-window scene changes while this dialog is observing inherited theme context.
    private final ChangeListener<@Nullable Scene> ownerWindowSceneListener =
            (observable, oldScene, newScene) -> refreshInheritedThemeContextAndApplyTheme();

    /// Handles root changes on the currently observed owner scene.
    private final ChangeListener<Parent> ownerSceneRootListener =
            (observable, oldRoot, newRoot) -> refreshInheritedThemeContextAndApplyTheme();

    /// Handles owner scene root effective orientation changes while this dialog is showing.
    private final ChangeListener<NodeOrientation> ownerSceneRootOrientationListener =
            (observable, oldValue, newValue) -> syncOwnerNodeOrientation();

    /// Handles owner scene stylesheet mutations while the dialog is showing.
    private final ListChangeListener<String> ownerSceneStylesheetsListener =
            change -> applyEffectiveTheme();

    /// Handles theme metadata changes on the owner scene root.
    private final MapChangeListener<Object, Object> sceneRootPropertiesListener =
            this::handleThemeRootPropertiesChanged;

    /// Handles parent-chain changes on observed owner ancestors.
    private final ChangeListener<@Nullable Parent> ancestorParentListener =
            (observable, oldParent, newParent) -> refreshInheritedThemeContextAndApplyTheme();

    /// Handles theme metadata changes on owner ancestors.
    private final MapChangeListener<Object, Object> ancestorThemeRootPropertiesListener =
            this::handleThemeRootPropertiesChanged;

    /// The owner window currently observed for scene changes.
    private @Nullable Window observedOwnerWindow;

    /// The owner scene currently observed for root changes.
    private @Nullable Scene observedOwnerScene;

    /// The owner scene root currently observed for theme metadata changes.
    private @Nullable Parent observedSceneRoot;

    /// The owner scene stylesheet list currently observed for dialog stylesheet mirroring.
    private @Nullable ObservableList<String> observedOwnerStylesheets;

    /// Owner ancestors currently observed for local theme metadata and parent-chain changes.
    private ArrayList<Parent> observedAncestorThemeRoots = new ArrayList<>();

    /// Reusable storage for collecting the current owner ancestor theme roots.
    private ArrayList<Parent> ancestorThemeRootsScratch = new ArrayList<>();

    /// Whether inherited theme context listeners are currently registered.
    private boolean observingInheritedThemeContext;

    /// The dialog pane orientation value before owner orientation mirroring began.
    private NodeOrientation baseNodeOrientationBeforeInheritance = NodeOrientation.INHERIT;

    /// Whether the dialog pane currently contains a mirrored owner orientation value.
    private boolean inheritedNodeOrientationApplied;

    /// Creates a Material Design 3 dialog.
    public M3Dialog() {
        this(new M3DialogPane());
    }

    /// Creates a Material dialog with a specialized package-owned pane.
    ///
    /// @param pane the Material dialog pane installed before lifecycle handlers are registered
    M3Dialog(M3DialogPane pane) {
        M3DialogPane materialPane = Objects.requireNonNull(pane, "pane");
        installStylesheet(materialPane);
        setDialogPane(materialPane);
        addEventFilter(DialogEvent.DIALOG_SHOWING, event -> {
            refreshOwnerWindowFromNode();
            startInheritedThemeContextObservation();
            syncOwnerNodeOrientation();
            applyEffectiveTheme();
        });
        addEventFilter(DialogEvent.DIALOG_HIDDEN, event -> stopInheritedThemeContextObservation());
    }

    /// Creates a Material Design 3 dialog with a title.
    ///
    /// @param title the dialog window title
    public M3Dialog(String title) {
        this();
        setTitle(Objects.requireNonNull(title, "title"));
    }

    /// Creates a Material Design 3 dialog with title, header text, content text, and button types.
    ///
    /// @param title the dialog window title
    /// @param headerText the dialog pane header text
    /// @param contentText the dialog pane content text
    /// @param buttonTypes the button types installed in the dialog pane
    public M3Dialog(
            String title,
            String headerText,
            String contentText,
            ButtonType... buttonTypes
    ) {
        this(title);
        Objects.requireNonNull(buttonTypes, "buttonTypes");
        for (ButtonType buttonType : buttonTypes) {
            Objects.requireNonNull(buttonType, "buttonType");
        }

        M3DialogPane pane = getM3DialogPane();
        pane.setHeaderText(Objects.requireNonNull(headerText, "headerText"));
        pane.setContentText(Objects.requireNonNull(contentText, "contentText"));
        pane.getButtonTypes().addAll(buttonTypes);
    }

    /// Returns the Material Design 3 dialog pane.
    ///
    /// @return the Material Design 3 dialog pane
    public final M3DialogPane getM3DialogPane() {
        DialogPane pane = getDialogPane();
        if (pane instanceof M3DialogPane materialPane) {
            return materialPane;
        }
        throw new IllegalStateException("dialog pane is not an M3DialogPane");
    }

    /// Returns the explicit theme applied directly to this dialog.
    ///
    /// When this value is null, the dialog inherits the owner scene theme when it is shown.
    ///
    /// @return the explicit theme applied to this dialog, or `null` to inherit from the owner scene
    public final @Nullable M3Theme getTheme() {
        return theme.get();
    }

    /// Sets the explicit theme applied directly to this dialog.
    ///
    /// Passing null clears the explicit override and allows owner scene theme inheritance.
    ///
    /// @param theme the explicit theme to apply, or `null` to inherit from the owner scene
    public final void setTheme(@Nullable M3Theme theme) {
        this.theme.set(theme);
    }

    /// Returns the explicit theme property.
    ///
    /// @return the explicit theme property
    public final ObjectProperty<@Nullable M3Theme> themeProperty() {
        return theme;
    }

    /// Initializes this dialog with a node owner and inherits local theme context from that node.
    ///
    /// This overload is useful when a dialog is launched from a subtree with a locally installed M3FX theme. If
    /// the node is already attached to a window, the JavaFX window owner is initialized from the node scene.
    ///
    /// @param owner the node that owns this dialog
    public final void initOwner(Node owner) {
        @Nullable Node previousOwnerNode = ownerNode;
        if (observingInheritedThemeContext && previousOwnerNode != null) {
            previousOwnerNode.sceneProperty().removeListener(ownerNodeSceneListener);
            previousOwnerNode.parentProperty().removeListener(ownerNodeParentListener);
            previousOwnerNode.effectiveNodeOrientationProperty().removeListener(ownerNodeOrientationListener);
        }

        ownerNode = Objects.requireNonNull(owner, "owner");
        refreshOwnerWindowFromNode();
        if (observingInheritedThemeContext) {
            owner.sceneProperty().addListener(ownerNodeSceneListener);
            owner.parentProperty().addListener(ownerNodeParentListener);
            owner.effectiveNodeOrientationProperty().addListener(ownerNodeOrientationListener);
            refreshInheritedThemeContextAndApplyTheme();
        }
    }

    /// Initializes the JavaFX window owner from the recorded owner node when possible.
    private void refreshOwnerWindowFromNode() {
        if (getOwner() != null || isShowing()) {
            return;
        }

        @Nullable Node node = ownerNode;
        if (node == null) {
            return;
        }

        @Nullable Scene scene = node.getScene();
        if (scene != null && scene.getWindow() != null) {
            initOwner(scene.getWindow());
        }
    }

    /// Applies the explicit theme or the current owner scene theme to the dialog pane.
    private void applyEffectiveTheme() {
        M3Theme effectiveTheme = getTheme();
        if (effectiveTheme == null) {
            effectiveTheme = getOwnerTheme();
        }
        M3DialogPane pane = getM3DialogPane();
        syncOwnerStylesheets(pane, effectiveTheme);
        applyTheme(pane, effectiveTheme);
    }

    /// Returns the theme installed on the owner scene when one is available.
    private @Nullable M3Theme getOwnerTheme() {
        @Nullable Node node = ownerNode;
        if (node != null) {
            @Nullable M3Theme nodeTheme = M3ThemeResolver.findTheme(node);
            if (nodeTheme != null) {
                return nodeTheme;
            }
        }

        Window owner = getOwner();
        if (owner == null) {
            return null;
        }

        @Nullable Scene ownerScene = owner.getScene();
        return ownerScene == null ? null : M3ThemeResolver.findTheme(ownerScene);
    }

    /// Starts observing inherited owner theme sources for runtime changes.
    private void startInheritedThemeContextObservation() {
        if (observingInheritedThemeContext) {
            refreshInheritedThemeContextSources();
            return;
        }

        observingInheritedThemeContext = true;
        @Nullable Node node = ownerNode;
        if (node != null) {
            node.sceneProperty().addListener(ownerNodeSceneListener);
            node.parentProperty().addListener(ownerNodeParentListener);
            node.effectiveNodeOrientationProperty().addListener(ownerNodeOrientationListener);
        }
        refreshInheritedThemeContextSources();
    }

    /// Stops observing inherited owner theme sources.
    private void stopInheritedThemeContextObservation() {
        if (!observingInheritedThemeContext) {
            return;
        }

        observingInheritedThemeContext = false;
        @Nullable Node node = ownerNode;
        if (node != null) {
            node.sceneProperty().removeListener(ownerNodeSceneListener);
            node.parentProperty().removeListener(ownerNodeParentListener);
            node.effectiveNodeOrientationProperty().removeListener(ownerNodeOrientationListener);
        }
        restoreBaseNodeOrientation();
        updateObservedOwnerWindow(null);
        updateObservedOwnerScene(null);
        updateObservedSceneRoot(null);
        updateObservedOwnerStylesheets(null);
        clearObservedAncestorThemeRoots();
    }

    /// Refreshes all owner roots that may provide inherited theme context.
    private void refreshInheritedThemeContextSources() {
        updateObservedOwnerWindow(getOwner());
        @Nullable Scene ownerScene = ownerThemeScene();
        updateObservedOwnerScene(ownerScene);
        updateObservedSceneRoot(ownerScene == null ? null : ownerScene.getRoot());
        updateObservedOwnerStylesheets(ownerScene == null ? null : ownerScene.getStylesheets());
        updateObservedAncestorThemeRoots();
    }

    /// Returns the scene whose root can provide inherited scene-level theme context.
    private @Nullable Scene ownerThemeScene() {
        @Nullable Node node = ownerNode;
        if (node != null && node.getScene() != null) {
            return node.getScene();
        }

        @Nullable Window window = observedOwnerWindow;
        return window == null ? null : window.getScene();
    }

    /// Handles installed-theme metadata changes on observed owner roots.
    private void handleThemeRootPropertiesChanged(MapChangeListener.Change<?, ?> change) {
        if (M3ThemeMetadata.isThemePropertyKey(change.getKey())) {
            refreshInheritedThemeContextAndApplyTheme();
        }
    }

    /// Refreshes inherited owner context sources, orientation, and effective dialog theme.
    private void refreshInheritedThemeContextAndApplyTheme() {
        refreshInheritedThemeContextSources();
        syncOwnerNodeOrientation();
        applyEffectiveTheme();
    }

    /// Updates the observed owner window.
    private void updateObservedOwnerWindow(@Nullable Window window) {
        if (observedOwnerWindow == window) {
            return;
        }
        if (observedOwnerWindow != null) {
            observedOwnerWindow.sceneProperty().removeListener(ownerWindowSceneListener);
        }
        observedOwnerWindow = window;
        if (observedOwnerWindow != null) {
            observedOwnerWindow.sceneProperty().addListener(ownerWindowSceneListener);
        }
    }

    /// Updates the observed owner scene.
    private void updateObservedOwnerScene(@Nullable Scene scene) {
        if (observedOwnerScene == scene) {
            return;
        }
        if (observedOwnerScene != null) {
            observedOwnerScene.rootProperty().removeListener(ownerSceneRootListener);
        }
        observedOwnerScene = scene;
        if (observedOwnerScene != null) {
            observedOwnerScene.rootProperty().addListener(ownerSceneRootListener);
        }
    }

    /// Updates the observed owner scene root.
    private void updateObservedSceneRoot(@Nullable Parent sceneRoot) {
        if (observedSceneRoot == sceneRoot) {
            return;
        }
        if (observedSceneRoot != null) {
            observedSceneRoot.getProperties().removeListener(sceneRootPropertiesListener);
            observedSceneRoot.effectiveNodeOrientationProperty().removeListener(ownerSceneRootOrientationListener);
        }
        observedSceneRoot = sceneRoot;
        if (observedSceneRoot != null) {
            observedSceneRoot.getProperties().addListener(sceneRootPropertiesListener);
            observedSceneRoot.effectiveNodeOrientationProperty().addListener(ownerSceneRootOrientationListener);
        }
    }

    /// Updates the observed owner stylesheet list.
    private void updateObservedOwnerStylesheets(@Nullable ObservableList<String> stylesheets) {
        if (observedOwnerStylesheets == stylesheets) {
            return;
        }
        if (observedOwnerStylesheets != null) {
            observedOwnerStylesheets.removeListener(ownerSceneStylesheetsListener);
        }
        observedOwnerStylesheets = stylesheets;
        if (observedOwnerStylesheets != null) {
            observedOwnerStylesheets.addListener(ownerSceneStylesheetsListener);
        }
    }

    /// Updates observed owner ancestors that can receive or lose local themes.
    private void updateObservedAncestorThemeRoots() {
        ancestorThemeRootsScratch.clear();
        @Nullable Node current = ownerNode;
        while (current != null) {
            if (current instanceof Parent parent && parent != observedSceneRoot) {
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

    /// Removes local-theme listeners from all observed owner ancestors.
    private void clearObservedAncestorThemeRoots() {
        for (Parent parent : observedAncestorThemeRoots) {
            parent.getProperties().removeListener(ancestorThemeRootPropertiesListener);
            parent.parentProperty().removeListener(ancestorParentListener);
        }
        observedAncestorThemeRoots.clear();
    }

    /// Mirrors owner node or owner scene root orientation into the detached dialog pane.
    private void syncOwnerNodeOrientation() {
        @Nullable Node node = ownerNode;
        @Nullable Node source = node != null && node.getScene() != null ? node : observedSceneRoot;
        if (source == null) {
            restoreBaseNodeOrientation();
            return;
        }

        M3DialogPane pane = getM3DialogPane();
        if (!inheritedNodeOrientationApplied) {
            baseNodeOrientationBeforeInheritance = pane.getNodeOrientation();
            inheritedNodeOrientationApplied = true;
        }
        pane.setNodeOrientation(source.getEffectiveNodeOrientation());
    }

    /// Restores the dialog pane orientation value that existed before owner orientation mirroring began.
    private void restoreBaseNodeOrientation() {
        if (!inheritedNodeOrientationApplied) {
            return;
        }
        getM3DialogPane().setNodeOrientation(baseNodeOrientationBeforeInheritance);
        baseNodeOrientationBeforeInheritance = NodeOrientation.INHERIT;
        inheritedNodeOrientationApplied = false;
    }

    /// Mirrors owner scene stylesheets into the dialog pane while keeping M3FX base styles available.
    private void syncOwnerStylesheets(M3DialogPane pane, @Nullable M3Theme effectiveTheme) {
        @Nullable ObservableList<String> ownerStylesheets = observedOwnerStylesheets;
        ObservableList<String> stylesheets = pane.getStylesheets();
        if (ownerStylesheets == null) {
            stylesheets.setAll(M3ThemeManager.stylesheetUrl());
            return;
        }

        stylesheets.setAll(ownerStylesheets);
        removeSupersededOwnerThemeStylesheet(stylesheets, effectiveTheme);
        installStylesheet(pane);
    }

    /// Removes an owner-scene theme stylesheet when an explicit dialog theme should have priority.
    private void removeSupersededOwnerThemeStylesheet(List<String> stylesheets, @Nullable M3Theme effectiveTheme) {
        @Nullable Scene ownerScene = ownerThemeScene();
        @Nullable M3Theme ownerSceneTheme = ownerScene == null ? null : M3ThemeResolver.findTheme(ownerScene);
        if (ownerSceneTheme != null && ownerSceneTheme != effectiveTheme) {
            stylesheets.remove(M3ThemeManager.themeStylesheetUrl(ownerSceneTheme));
        }
    }

    /// Adds the shared M3FX stylesheet to the dialog pane.
    private static void installStylesheet(M3DialogPane pane) {
        M3PopupStyles.addFallbackRootStyleClass(pane);
        String stylesheet = M3ThemeManager.stylesheetUrl();
        moveOrAdd(pane.getStylesheets(), stylesheet, 0);
    }

    /// Applies or clears theme declarations on the dialog pane.
    private static void applyTheme(M3DialogPane pane, @Nullable M3Theme theme) {
        if (theme == null) {
            uninstallThemeStylesheet(pane);
            M3ThemeManager.clearThemeStyleClasses(pane);
            Object baseStyleValue = pane.getProperties().remove(BASE_STYLE_PROPERTY_KEY);
            pane.setStyle(baseStyleValue instanceof String baseStyle ? baseStyle : "");
            return;
        }

        installStylesheet(pane);
        M3ThemeManager.applyThemeStyleClasses(pane, theme);
        installThemeStylesheet(pane, theme);

        if (!pane.getProperties().containsKey(BASE_STYLE_PROPERTY_KEY)) {
            pane.getProperties().put(BASE_STYLE_PROPERTY_KEY, pane.getStyle());
        }

        Object baseStyleValue = pane.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        String baseStyle = baseStyleValue instanceof String ? (String) baseStyleValue : "";
        pane.setStyle(mergeStyles(baseStyle, M3ThemeCssCompiler.rootStyleDeclarations(theme)));
    }

    /// Adds the generated theme stylesheet for the supplied theme.
    private static void installThemeStylesheet(M3DialogPane pane, M3Theme theme) {
        String stylesheet = M3ThemeManager.themeStylesheetUrl(theme);
        Object previousStylesheet = pane.getProperties().put(THEME_STYLESHEET_PROPERTY_KEY, stylesheet);
        if (previousStylesheet instanceof String previous && !previous.equals(stylesheet)) {
            pane.getStylesheets().remove(previous);
        }
        ObservableList<String> stylesheets = pane.getStylesheets();
        int baseStylesheetIndex = stylesheets.indexOf(M3ThemeManager.stylesheetUrl());
        moveOrAdd(stylesheets, stylesheet, baseStylesheetIndex >= 0 ? baseStylesheetIndex + 1 : 0);
    }

    /// Removes the generated theme stylesheet from the dialog pane.
    private static void uninstallThemeStylesheet(M3DialogPane pane) {
        Object previousStylesheet = pane.getProperties().remove(THEME_STYLESHEET_PROPERTY_KEY);
        if (previousStylesheet instanceof String previous) {
            pane.getStylesheets().remove(previous);
        }
    }

    /// Moves an existing stylesheet or adds a new stylesheet at the requested index.
    private static void moveOrAdd(List<String> stylesheets, String stylesheet, int index) {
        int targetIndex = Math.min(Math.max(0, index), stylesheets.size());
        int currentIndex = stylesheets.indexOf(stylesheet);
        if (currentIndex == targetIndex) {
            return;
        }
        if (currentIndex >= 0) {
            stylesheets.remove(currentIndex);
            if (currentIndex < targetIndex) {
                targetIndex--;
            }
        }
        stylesheets.add(Math.min(targetIndex, stylesheets.size()), stylesheet);
    }

    /// Merges existing pane style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }
}
