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
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3DialogScrimPresenter;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.internal.theme.M3ThemeCssCompiler;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A JavaFX dialog with a Material Design 3 dialog pane.
///
/// This class retains the [Dialog] result, event, ownership, and modality contracts while using an
/// [M3DialogPane]. [show()][Dialog#show()] displays the dialog without blocking; [showAndWait()][Dialog#showAndWait()]
/// enters a nested event loop until the dialog is hidden. Button actions use the standard JavaFX dialog closing
/// rules and the configured result converter.
///
/// An owned dialog dims its owner's scene with a Material scrim. The scrim and dialog container fade in when the
/// dialog is shown and fade out before a successful close. When reduced motion is requested for the dialog, these
/// transitions settle immediately. A dialog without an attached, visible owner scene has no surface to scrim.
/// With motion enabled, [close()][Dialog#close()] and [hide()][Dialog#hide()] begin the exit transition and the
/// dialog remains showing until it completes; a close-request handler may still consume the final close request.
///
/// A dialog may be owned by a [Window] through the inherited API or by a [Node] through [initOwner(Node)]. The node
/// overload is useful for controls inside a locally themed subtree: while the dialog is showing, it inherits that
/// subtree's theme, stylesheets, and effective node orientation. An explicit [theme][#themeProperty()] overrides
/// inherited theme values. The default modality and all restrictions on initializing owner and modality are those
/// defined by [Dialog].
///
/// ```java
/// private void showDeleteDialog(Node owner) {
///     M3Dialog<String> dialog = new M3Dialog<>();
///     dialog.initOwner(owner);
///     dialog.getM3DialogPane().setHeaderText("Delete item?");
///     dialog.getM3DialogPane().setContentText("This action cannot be undone.");
///     dialog.getM3DialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
///     dialog.setResultConverter(type -> type == ButtonType.OK ? "delete" : null);
///     dialog.showAndWait();
/// }
/// ```
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

    /// The explicit theme applied to this dialog, or `null` to inherit an owner theme.
    ///
    /// The default value is `null`. Changing this property while the dialog is showing updates its Material theme.
    /// When no explicit value is present, the dialog resolves the theme from its owner node or owner window.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3Theme> theme = new SimpleObjectProperty<>(this, "theme") {
        /// Applies theme declarations to the Material dialog pane.
        @Override
        protected void invalidated() {
            applyEffectiveTheme();
        }
    };

    /// Presents and synchronizes the modal scrim over the owner scene.
    private final M3DialogScrimPresenter scrimPresenter = new M3DialogScrimPresenter();

    /// Animates the dialog container opacity during show and close transitions.
    private final M3NodeTransition presentationAnimation;

    /// The dialog pane opacity restored after presentation transitions.
    private double restingOpacity = 1.0;

    /// Whether the presentation animation is currently executing an exit transition.
    private boolean exitAnimationRunning;

    /// Whether a close request was issued internally after the exit transition.
    private boolean closeAfterExitAnimation;

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

    /// Creates an empty Material Design 3 dialog with a new [M3DialogPane].
    ///
    /// No title, content, buttons, owner, or explicit theme is configured by this constructor.
    public M3Dialog() {
        this(new M3DialogPane());
    }

    /// Creates a Material dialog with a specialized package-owned pane.
    ///
    /// @param pane the Material dialog pane installed before lifecycle handlers are registered
    M3Dialog(M3DialogPane pane) {
        M3DialogPane materialPane = Objects.requireNonNull(pane, "pane");
        initStyle(StageStyle.TRANSPARENT);
        installStylesheet(materialPane);
        setDialogPane(materialPane);
        presentationAnimation = new M3NodeTransition(materialPane);
        presentationAnimation.setOnFinished(event -> handlePresentationAnimationFinished());
        addEventFilter(DialogEvent.DIALOG_SHOWING, this::handleDialogShowing);
        addEventFilter(DialogEvent.DIALOG_SHOWN, this::handleDialogShown);
        addEventFilter(DialogEvent.DIALOG_HIDING, this::handleDialogHiding);
        addEventFilter(DialogEvent.DIALOG_CLOSE_REQUEST, this::handleDialogCloseRequest);
        addEventFilter(DialogEvent.DIALOG_HIDDEN, this::handleDialogHidden);
    }

    /// Prepares inherited context, scrim presentation, and the dialog's initial visual state.
    private void handleDialogShowing(DialogEvent event) {
        presentationAnimation.stop();
        exitAnimationRunning = false;
        closeAfterExitAnimation = false;

        M3DialogPane pane = getM3DialogPane();
        @Nullable Scene scene = pane.getScene();
        if (scene != null) {
            scene.setFill(Color.TRANSPARENT);
        }

        refreshOwnerWindowFromNode();
        startInheritedThemeContextObservation();
        syncOwnerNodeOrientation();
        applyEffectiveTheme();
        pane.applyCss();
        restingOpacity = pane.getOpacity();
        showScrim();
        if (canAnimatePresentation()) {
            pane.setOpacity(0.0);
        }
    }

    /// Starts the dialog container entrance transition after the dialog has been presented.
    private void handleDialogShown(DialogEvent event) {
        if (canAnimatePresentation()) {
            playEntranceAnimation();
        } else {
            restorePaneOpacity();
        }
    }

    /// Suppresses the provisional hiding event used to start an animated close.
    private void handleDialogHiding(DialogEvent event) {
        if (shouldDelayClose()) {
            event.consume();
        }
    }

    /// Converts the first close request into an exit transition and permits the final request.
    private void handleDialogCloseRequest(DialogEvent event) {
        if (closeAfterExitAnimation) {
            return;
        }
        if (exitAnimationRunning) {
            event.consume();
            return;
        }
        if (!shouldDelayClose()) {
            return;
        }

        event.consume();
        playExitAnimation();
    }

    /// Releases presentation resources and restores caller-configured pane state after hiding.
    private void handleDialogHidden(DialogEvent event) {
        presentationAnimation.stop();
        exitAnimationRunning = false;
        closeAfterExitAnimation = false;
        restorePaneOpacity();
        scrimPresenter.dispose();
        stopInheritedThemeContextObservation();
    }

    /// Shows a scrim over the attached owner scene when one is available.
    private void showScrim() {
        @Nullable Scene scene = ownerThemeScene();
        if (scene == null) {
            return;
        }

        Parent sceneRoot = scene.getRoot();
        @Nullable Node node = ownerNode;
        Node popupOwner = node != null && node.getScene() == scene ? node : sceneRoot;
        M3DialogPane pane = getM3DialogPane();
        scrimPresenter.show(popupOwner, pane, scene, pane);
    }

    /// Starts or reverses the dialog container entrance fade.
    private void playEntranceAnimation() {
        M3DialogPane pane = getM3DialogPane();
        exitAnimationRunning = false;
        M3MotionSpec spec = M3Animation.defaultEffects(pane);
        presentationAnimation.configure(
                spec,
                restingOpacity,
                pane.getScaleX(),
                pane.getScaleY(),
                pane.getTranslateX(),
                pane.getTranslateY()
        );
        M3Animation.playFromStart(pane, presentationAnimation);
    }

    /// Starts the dialog container and scrim exit fades.
    private void playExitAnimation() {
        M3DialogPane pane = getM3DialogPane();
        exitAnimationRunning = true;
        scrimPresenter.hide();
        M3MotionSpec spec = M3Animation.fastEffects(pane);
        presentationAnimation.configure(
                spec,
                0.0,
                pane.getScaleX(),
                pane.getScaleY(),
                pane.getTranslateX(),
                pane.getTranslateY()
        );
        M3Animation.playFromStart(pane, presentationAnimation);
    }

    /// Completes an animated close or leaves a finished entrance transition in place.
    private void handlePresentationAnimationFinished() {
        if (!exitAnimationRunning) {
            return;
        }

        exitAnimationRunning = false;
        closeAfterExitAnimation = true;
        try {
            close();
        } finally {
            if (isShowing()) {
                closeAfterExitAnimation = false;
                scrimPresenter.restore();
                playEntranceAnimation();
            }
        }
    }

    /// Returns whether the current close request should wait for an exit transition.
    private boolean shouldDelayClose() {
        return isShowing() && !closeAfterExitAnimation && canAnimatePresentation();
    }

    /// Returns whether the dialog pane can participate in presentation motion.
    private boolean canAnimatePresentation() {
        M3DialogPane pane = getM3DialogPane();
        return !pane.opacityProperty().isBound() && M3Animation.areAnimationsEnabled(pane);
    }

    /// Restores the pane opacity captured before the dialog was shown.
    private void restorePaneOpacity() {
        M3DialogPane pane = getM3DialogPane();
        if (!pane.opacityProperty().isBound()) {
            pane.setOpacity(restingOpacity);
        }
    }

    /// Creates an otherwise empty Material Design 3 dialog with the specified window title.
    ///
    /// @param title the dialog window title
    /// @throws NullPointerException if `title` is `null`
    public M3Dialog(String title) {
        this();
        setTitle(Objects.requireNonNull(title, "title"));
    }

    /// Creates a Material Design 3 dialog with text content and the specified button types.
    ///
    /// Button types are added in array order. The supplied array and every element must be non-null. A result
    /// converter may still be required when the result type is not [ButtonType].
    ///
    /// @param title       the dialog window title
    /// @param headerText  the dialog pane header text
    /// @param contentText the dialog pane content text
    /// @param buttonTypes the button types installed in the dialog pane
    /// @throws NullPointerException if `title`, `headerText`, `contentText`, `buttonTypes`, or an element of
    ///         `buttonTypes` is `null`
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

    /// Returns the Material Design 3 dialog pane currently installed on this dialog.
    ///
    /// @return the Material Design 3 dialog pane
    /// @throws IllegalStateException if the inherited [dialogPane][Dialog#dialogPaneProperty()] has been replaced
    ///         with a pane that is not an [M3DialogPane]
    public final M3DialogPane getM3DialogPane() {
        DialogPane pane = getDialogPane();
        if (pane instanceof M3DialogPane materialPane) {
            return materialPane;
        }
        throw new IllegalStateException("dialog pane is not an M3DialogPane");
    }

    /// Returns the explicit theme applied directly to this dialog.
    ///
    /// @return the explicit theme applied to this dialog, or `null` to inherit from the owner scene
    public final @Nullable M3Theme getTheme() {
        return theme.get();
    }

    /// Sets the explicit theme applied directly to this dialog.
    ///
    /// Passing `null` clears the explicit override and immediately restores owner-theme inheritance when the
    /// dialog is showing.
    ///
    /// @param theme the explicit theme to apply, or `null` to inherit from the owner scene
    public final void setTheme(@Nullable M3Theme theme) {
        this.theme.set(theme);
    }

    /// Returns the property that stores the dialog's explicit Material theme.
    ///
    /// @return the explicit theme property; a `null` value requests owner-theme inheritance
    public final ObjectProperty<@Nullable M3Theme> themeProperty() {
        return theme;
    }

    /// Records a node as this dialog's ownership and inherited-theme context.
    ///
    /// If the node is attached to a window and the JavaFX window owner has not already been initialized, that
    /// window becomes the dialog owner. Otherwise this method leaves the existing window owner unchanged. The node
    /// remains the source for inherited theme and orientation while the dialog is showing. Calling this method
    /// again replaces the previously recorded node context.
    ///
    /// @param owner the node that owns this dialog
    /// @throws NullPointerException if `owner` is `null`
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
        scrimPresenter.sync();
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
            stylesheets.setAll(M3ThemeRuntime.stylesheetUrl());
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
            stylesheets.remove(M3ThemeRuntime.themeStylesheetUrl(ownerSceneTheme));
        }
    }

    /// Adds the shared M3FX stylesheet to the dialog pane.
    private static void installStylesheet(M3DialogPane pane) {
        M3PopupStyles.addFallbackRootStyleClass(pane);
        String stylesheet = M3ThemeRuntime.stylesheetUrl();
        moveOrAdd(pane.getStylesheets(), stylesheet, 0);
    }

    /// Applies or clears theme declarations on the dialog pane.
    private static void applyTheme(M3DialogPane pane, @Nullable M3Theme theme) {
        if (theme == null) {
            uninstallThemeStylesheet(pane);
            M3ThemeRuntime.clearThemeStyleClasses(pane);
            M3ThemeMetadata.clearTheme(pane);
            Object baseStyleValue = pane.getProperties().remove(BASE_STYLE_PROPERTY_KEY);
            pane.setStyle(baseStyleValue instanceof String baseStyle ? baseStyle : "");
            return;
        }

        installStylesheet(pane);
        M3ThemeMetadata.setTheme(pane, theme);
        M3ThemeRuntime.applyThemeStyleClasses(pane, theme);
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
        String stylesheet = M3ThemeRuntime.themeStylesheetUrl(theme);
        Object previousStylesheet = pane.getProperties().put(THEME_STYLESHEET_PROPERTY_KEY, stylesheet);
        if (previousStylesheet instanceof String previous && !previous.equals(stylesheet)) {
            pane.getStylesheets().remove(previous);
        }
        ObservableList<String> stylesheets = pane.getStylesheets();
        int baseStylesheetIndex = stylesheets.indexOf(M3ThemeRuntime.stylesheetUrl());
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
