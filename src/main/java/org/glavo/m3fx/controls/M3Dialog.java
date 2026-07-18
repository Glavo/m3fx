// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3DialogPresenter;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.internal.theme.M3ThemeCssCompiler;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/// Controls a Material Design 3 modal dialog rendered inside an owner scene.
///
/// `M3Dialog` does not create a native window and does not use JavaFX [javafx.scene.control.Dialog] modality. Its
/// pane and scrim are installed in the [M3OverlayPane] containing the owner, so the owner window retains normal
/// window-manager behavior while application content remains blocked by the scrim and focus trap. Presentation
/// never replaces the scene root. Closing the dialog removes only its overlay layer and restores prior focus when
/// no newer overlay has superseded it.
///
/// [#show()] is non-blocking. [#showAndWait()] enters a JavaFX nested event loop and returns the nullable result after
/// the dialog has closed. An action button first bubbles its ordinary action event; consuming that event prevents
/// dialog closing. Otherwise the dialog emits a cancellable [M3DialogEvent#CLOSE_REQUEST], converts the button type
/// to a result, and runs its exit transition. Reduced-motion requests settle presentation immediately.
/// Activating the surrounding scrim requests the same cancellable close by default and produces no button type;
/// [#dismissOnScrimClickProperty()] disables only that pointer-dismissal behavior while retaining modality.
///
/// A dialog must have an attached [owner][#setOwner(Node)] inside an [M3OverlayPane] before it is shown. The owner
/// also supplies inherited stylesheets, node orientation, motion settings, and the nearest local Material theme.
/// An explicit [theme][#themeProperty()] overrides that inherited theme while preserving the owner's remaining
/// context. Hiding the owner window forcibly removes the overlay and completes the hiding lifecycle without
/// emitting a cancellable close request.
///
/// ```java
/// M3Dialog<String> dialog = new M3Dialog<>();
/// dialog.setOwner(ownerNode);
/// dialog.getDialogPane().setHeaderText("Delete item?");
/// dialog.getDialogPane().setContentText("This action cannot be undone.");
/// dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
/// dialog.setResultConverter(type -> type == ButtonType.OK ? "delete" : null);
/// dialog.show();
/// ```
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
///
/// @param <R> the dialog result type
@NotNullByDefault
public class M3Dialog<R> {
    /// The property key that stores the presentation context style before explicit theme declarations are added.
    private static final String BASE_STYLE_PROPERTY_KEY = M3Dialog.class.getName() + ".baseStyle";

    /// The property key that stores the generated explicit-theme stylesheet installed on the presentation context.
    private static final String THEME_STYLESHEET_PROPERTY_KEY = M3Dialog.class.getName() + ".themeStylesheet";

    /// The retained Material pane rendered by this dialog.
    private final M3DialogPane dialogPane;

    /// Installs the pane and scrim into the configured owner scene.
    private final M3DialogPresenter presenter;

    /// Animates pane opacity during dialog entrance and exit.
    private final M3NodeTransition presentationAnimation;

    /// The explicit Material theme for this dialog.
    ///
    /// A `null` value inherits the nearest theme controlling the owner. Changes made while the dialog is visible
    /// are applied to the existing overlay without recreating its pane or action nodes.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3Theme> theme = new SimpleObjectProperty<>(this, "theme") {
        /// Applies an updated explicit or inherited theme context.
        @Override
        protected void invalidated() {
            applyEffectiveTheme();
        }
    };

    /// The latest completed dialog result.
    ///
    /// [#show()] resets this property to `null`. An accepted action commits its converted result only after the exit
    /// transition has completed; a consumed close request leaves the property unchanged.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable R> result = new SimpleObjectProperty<>(this, "result");

    /// The callback used to convert an accepted [ButtonType] into the dialog result type.
    ///
    /// When this property is `null`, the initiating button type is used as the result. Callers whose result type
    /// does not accept [ButtonType] should install a converter before presenting the dialog.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Callback<ButtonType, @Nullable R>> resultConverter =
            new SimpleObjectProperty<>(this, "resultConverter");

    /// Whether a primary click on the surrounding scrim requests that this dialog close.
    ///
    /// A scrim dismissal follows the ordinary cancellable close lifecycle and produces no button type. Setting this
    /// property to `false` keeps the scrim modal but prevents pointer dismissal. Escape and action buttons are not
    /// affected.
    ///
    /// @defaultValue `true`
    private final BooleanProperty dismissOnScrimClickValue =
            new SimpleBooleanProperty(this, "dismissOnScrimClick", true);

    /// Reports whether this dialog currently owns an overlay layer in its owner's scene.
    ///
    /// The property becomes `true` after the layer is installed and becomes `false` after accepted exit motion and
    /// cleanup have completed. It is read-only to callers.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// The handler invoked immediately before this dialog begins presentation.
    ///
    /// Throwing from this handler aborts presentation before the owner scene is modified.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onShowing =
            new SimpleObjectProperty<>(this, "onShowing");

    /// The handler invoked after this dialog's overlay layer has been installed.
    ///
    /// Throwing from this handler removes the partially presented layer from its stable overlay pane.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onShown =
            new SimpleObjectProperty<>(this, "onShown");

    /// The handler invoked after a close request is accepted but before exit motion begins.
    ///
    /// Throwing from this handler cancels the pending transition, keeps the dialog visible, and leaves its completed
    /// result unchanged during an ordinary close. If the owner window has already hidden, presentation cleanup cannot
    /// be cancelled; the exception is rethrown after the overlay is removed.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onHiding =
            new SimpleObjectProperty<>(this, "onHiding");

    /// The handler invoked after this dialog's layer has been removed and its result has been committed.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onHidden =
            new SimpleObjectProperty<>(this, "onHidden");

    /// The handler invoked whenever code or an action button requests that the dialog close.
    ///
    /// Calling [M3DialogEvent#consume()] from this handler rejects the request before result conversion, lifecycle
    /// mutation, scrim motion, or pane exit motion begins.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onCloseRequest =
            new SimpleObjectProperty<>(this, "onCloseRequest");

    /// The node whose scene and local context own this dialog.
    private @Nullable Node owner;

    /// Synchronizes inherited stylesheets, orientation, theme metadata, and motion context while showing.
    private @Nullable M3PopupContextSynchronizer contextSynchronizer;

    /// Handles owner-window disappearance while this dialog owns an in-scene overlay.
    private final EventHandler<WindowEvent> ownerWindowHiddenHandler = event -> handleOwnerWindowHidden();

    /// The owner window currently observed for forced presentation cleanup.
    private @Nullable Window observedOwnerWindow;

    /// Whether a show operation is currently dispatching lifecycle callbacks or installing its overlay.
    private boolean presenting;

    /// The pane opacity restored after presentation transitions.
    private double restingOpacity = 1.0;

    /// Whether an accepted close is currently running its exit transition.
    private boolean closing;

    /// The result committed after the current exit transition completes.
    private @Nullable R pendingResult;

    /// The action associated with the accepted close transition.
    private @Nullable ButtonType pendingButtonType;

    /// Whether the pane has completed the active exit transition.
    private boolean paneExitFinished;

    /// Whether the scrim has completed the active exit transition.
    private boolean scrimExitFinished;

    /// Whether [#showAndWait()] currently owns a nested event loop for this dialog.
    private boolean nestedEventLoopRunning;

    /// Creates an empty Material dialog with a new [M3DialogPane].
    public M3Dialog() {
        this(new M3DialogPane());
    }

    /// Creates a Material dialog around a specialized package-owned pane.
    ///
    /// @param dialogPane the pane retained for the lifetime of this dialog
    /// @throws NullPointerException if `dialogPane` is `null`
    M3Dialog(M3DialogPane dialogPane) {
        this.dialogPane = Objects.requireNonNull(dialogPane, "dialogPane");
        presenter = new M3DialogPresenter(dialogPane, this::handleScrimAction);
        presenter.setDismissOnScrimClick(isDismissOnScrimClick());
        dismissOnScrimClickValue.addListener((observable, oldValue, enabled) ->
                presenter.setDismissOnScrimClick(enabled));
        presentationAnimation = new M3NodeTransition(dialogPane);
        presentationAnimation.setOnFinished(event -> handlePresentationAnimationFinished());
        dialogPane.setButtonAction(this::handleButtonAction);
        installStylesheet(presenter.contextRoot());
    }

    /// Returns this dialog's retained Material pane.
    ///
    /// @return the non-null dialog pane
    public final M3DialogPane getDialogPane() {
        return dialogPane;
    }

    /// Returns the node that owns this dialog's scene presentation and inherited context.
    ///
    /// @return the owner node, or `null` before one is configured
    public final @Nullable Node getOwner() {
        return owner;
    }

    /// Sets the owner node used for subsequent presentations.
    ///
    /// The owner may be replaced while the dialog is fully hidden. It must be attached to a showing window and be
    /// the [M3OverlayPane] used as that scene's root or one of its descendants when [#show()] is called.
    ///
    /// @param owner the owner node
    /// @throws IllegalStateException if this dialog is showing or beginning presentation
    /// @throws NullPointerException  if `owner` is `null`
    public final void setOwner(Node owner) {
        if (isShowing() || presenting) {
            throw new IllegalStateException("owner cannot change while the dialog is showing or presenting");
        }
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    /// Returns the explicit theme applied to this dialog.
    ///
    /// @return the explicit theme, or `null` to inherit from the owner hierarchy
    public final @Nullable M3Theme getTheme() {
        return theme.get();
    }

    /// Sets the explicit theme applied to this dialog.
    ///
    /// @param theme the explicit theme, or `null` to restore owner-theme inheritance
    public final void setTheme(@Nullable M3Theme theme) {
        this.theme.set(theme);
    }

    public final ObjectProperty<@Nullable M3Theme> themeProperty() {
        return theme;
    }

    /// Returns the latest completed dialog result.
    ///
    /// @return the result, or `null` when no result has been produced
    public final @Nullable R getResult() {
        return result.get();
    }

    /// Sets the stored result without changing dialog visibility.
    ///
    /// @param result the result value, or `null`
    public final void setResult(@Nullable R result) {
        this.result.set(result);
    }

    public final ObjectProperty<@Nullable R> resultProperty() {
        return result;
    }

    /// Returns the callback that converts an accepted action button to a result.
    ///
    /// @return the result converter, or `null` to use the button type itself
    public final @Nullable Callback<ButtonType, @Nullable R> getResultConverter() {
        return resultConverter.get();
    }

    /// Sets the callback that converts an accepted action button to a result.
    ///
    /// @param converter the converter, or `null` to use the button type itself
    public final void setResultConverter(@Nullable Callback<ButtonType, @Nullable R> converter) {
        resultConverter.set(converter);
    }

    public final ObjectProperty<@Nullable Callback<ButtonType, @Nullable R>> resultConverterProperty() {
        return resultConverter;
    }

    /// Returns whether a primary click on the surrounding scrim requests that this dialog close.
    ///
    /// @return `true` when pointer activation of the scrim requests dismissal
    public final boolean isDismissOnScrimClick() {
        return dismissOnScrimClickValue.get();
    }

    /// Sets whether a primary click on the surrounding scrim requests that this dialog close.
    ///
    /// Disabling this property does not make the scrim mouse-transparent; owner content remains blocked while the
    /// dialog is showing.
    ///
    /// @param dismissOnScrimClick whether pointer activation of the scrim requests dismissal
    public final void setDismissOnScrimClick(boolean dismissOnScrimClick) {
        dismissOnScrimClickValue.set(dismissOnScrimClick);
    }

    /// Returns the property controlling pointer dismissal through the surrounding scrim.
    ///
    /// @return the scrim pointer-dismissal property
    public final BooleanProperty dismissOnScrimClickProperty() {
        return dismissOnScrimClickValue;
    }

    /// Returns whether this dialog currently owns an installed overlay layer.
    ///
    /// @return `true` between completed show and hide transitions
    public final boolean isShowing() {
        return showing.get();
    }

    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Displays this dialog without blocking the calling JavaFX event handler.
    ///
    /// Repeated calls while showing or beginning presentation have no effect. The method must run on the JavaFX
    /// application thread.
    ///
    /// @throws IllegalStateException if called off the JavaFX application thread, if no owner is configured, if the
    ///         owner is detached or outside an [M3OverlayPane], or if the dialog pane already belongs to another
    ///         scene-graph parent
    public final void show() {
        checkFxThread();
        if (isShowing() || presenting) {
            return;
        }

        Node activeOwner = requireShowingOwner();
        if (dialogPane.getParent() != null) {
            throw new IllegalStateException("dialog pane already belongs to a scene-graph parent");
        }

        presenting = true;
        boolean presented = false;
        try {
            fireLifecycle(M3DialogEvent.SHOWING, getOnShowing(), null);
            result.set(null);
            pendingResult = null;
            pendingButtonType = null;
            paneExitFinished = false;
            scrimExitFinished = false;
            closing = false;
            presentationAnimation.stop();
            startContextSynchronization(activeOwner);
            applyEffectiveTheme();
            dialogPane.applyCss();
            restingOpacity = dialogPane.getOpacity();
            if (canAnimatePresentation()) {
                dialogPane.setOpacity(0.0);
            }
            dialogPane.setModalActive(true);
            startOwnerWindowObservation(activeOwner);
            presenter.show(activeOwner);
            showing.set(true);
            fireLifecycle(M3DialogEvent.SHOWN, getOnShown(), null);
            presented = true;
            Platform.runLater(() -> {
                if (isShowing() && !closing) {
                    dialogPane.requestInitialFocus();
                }
            });
            if (isShowing() && !closing && canAnimatePresentation()) {
                playEntranceAnimation();
            } else if (isShowing() && !closing) {
                restorePaneOpacity();
            }
        } finally {
            if (!presented) {
                dialogPane.setModalActive(false);
                presenter.dispose();
                stopContextSynchronization();
                stopOwnerWindowObservation();
                restorePaneOpacity();
                showing.set(false);
            }
            presenting = false;
        }
    }

    /// Displays this dialog and enters a nested JavaFX event loop until it closes.
    ///
    /// @return the completed result, or `null`
    /// @throws IllegalStateException if called off the JavaFX application thread, while already showing or beginning
    ///         presentation, while a nested wait for this dialog is already active, if no showing owner inside an
    ///         [M3OverlayPane] is configured, or if the dialog pane already belongs to another scene-graph parent
    public final @Nullable R showAndWait() {
        checkFxThread();
        if (isShowing() || presenting || nestedEventLoopRunning) {
            throw new IllegalStateException("dialog is already showing, presenting, or waiting");
        }

        show();
        if (!isShowing()) {
            return getResult();
        }
        nestedEventLoopRunning = true;
        try {
            Platform.enterNestedEventLoop(this);
        } finally {
            nestedEventLoopRunning = false;
        }
        return getResult();
    }

    /// Requests that this dialog close without selecting an action button.
    ///
    /// The request has no effect while the dialog is hidden or already closing. A configured
    /// [#onCloseRequestProperty()] handler may consume the request and keep the dialog visible. This method must run
    /// on the JavaFX application thread.
    ///
    /// @throws IllegalStateException if called off the JavaFX application thread
    public final void close() {
        checkFxThread();
        requestClose(null);
    }

    /// Requests that this dialog hide without selecting an action button.
    ///
    /// This method is equivalent to [#close()] and therefore participates in the same cancellable close lifecycle.
    ///
    /// @throws IllegalStateException if called off the JavaFX application thread
    public final void hide() {
        close();
    }

    /// Handles an unconsumed action from one of the pane's action buttons.
    private void handleButtonAction(ButtonType buttonType) {
        requestClose(Objects.requireNonNull(buttonType, "buttonType"));
    }

    /// Handles activation of the surrounding scrim through the ordinary cancellable close lifecycle.
    private void handleScrimAction() {
        requestClose(null);
    }

    /// Emits a cancellable close request and starts an accepted exit transition.
    private void requestClose(@Nullable ButtonType buttonType) {
        if (!isShowing() || closing) {
            return;
        }

        M3DialogEvent closeEvent = fireLifecycle(
                M3DialogEvent.CLOSE_REQUEST,
                getOnCloseRequest(),
                buttonType
        );
        if (closeEvent.isConsumed()) {
            return;
        }

        @Nullable R nextResult = buttonType == null ? getResult() : convertResult(buttonType);
        closing = true;
        pendingResult = nextResult;
        pendingButtonType = buttonType;
        try {
            fireLifecycle(M3DialogEvent.HIDING, getOnHiding(), buttonType);
        } catch (RuntimeException | Error exception) {
            closing = false;
            pendingResult = null;
            pendingButtonType = null;
            throw exception;
        }

        if (canAnimatePresentation()) {
            paneExitFinished = false;
            scrimExitFinished = false;
            presenter.hideScrim(this::handleScrimExitFinished);
            playExitAnimation();
        } else {
            completeHide(buttonType);
        }
    }

    /// Converts one action button through the configured callback or the default button-type result.
    @SuppressWarnings("unchecked")
    private @Nullable R convertResult(ButtonType buttonType) {
        @Nullable Callback<ButtonType, @Nullable R> converter = getResultConverter();
        return converter == null ? (R) buttonType : converter.call(buttonType);
    }

    /// Starts the pane entrance fade from its current opacity.
    private void playEntranceAnimation() {
        M3MotionSpec spec = M3Animation.defaultEffects(dialogPane);
        presentationAnimation.configure(
                spec,
                restingOpacity,
                dialogPane.getScaleX(),
                dialogPane.getScaleY(),
                dialogPane.getTranslateX(),
                dialogPane.getTranslateY()
        );
        M3Animation.playFromStart(dialogPane, presentationAnimation);
    }

    /// Starts the pane exit fade while retaining its overlay layer.
    private void playExitAnimation() {
        M3MotionSpec spec = M3Animation.fastEffects(dialogPane);
        presentationAnimation.configure(
                spec,
                0.0,
                dialogPane.getScaleX(),
                dialogPane.getScaleY(),
                dialogPane.getTranslateX(),
                dialogPane.getTranslateY()
        );
        M3Animation.playFromStart(dialogPane, presentationAnimation);
    }

    /// Completes an exit animation when one is active.
    private void handlePresentationAnimationFinished() {
        if (closing) {
            paneExitFinished = true;
            completeHideWhenExitFinished();
        }
    }

    /// Records completion of the scrim exit transition.
    private void handleScrimExitFinished() {
        if (closing) {
            scrimExitFinished = true;
            completeHideWhenExitFinished();
        }
    }

    /// Removes the overlay after both independently animated dialog layers have settled.
    private void completeHideWhenExitFinished() {
        if (closing && paneExitFinished && scrimExitFinished) {
            completeHide(pendingButtonType);
        }
    }

    /// Removes the overlay and commits the result after an accepted close.
    private void completeHide(@Nullable ButtonType buttonType) {
        presentationAnimation.stop();
        dialogPane.setModalActive(false);
        presenter.dispose();
        restorePaneOpacity();
        stopContextSynchronization();
        stopOwnerWindowObservation();
        result.set(pendingResult);
        pendingResult = null;
        pendingButtonType = null;
        paneExitFinished = false;
        scrimExitFinished = false;
        closing = false;
        showing.set(false);
        try {
            fireLifecycle(M3DialogEvent.HIDDEN, getOnHidden(), buttonType);
        } finally {
            if (nestedEventLoopRunning) {
                Platform.exitNestedEventLoop(this, null);
            }
        }
    }

    /// Forces presentation cleanup after the owner window has disappeared.
    private void handleOwnerWindowHidden() {
        if (!isShowing()) {
            return;
        }

        presentationAnimation.stop();
        @Nullable ButtonType buttonType = pendingButtonType;
        boolean fireHiding = !closing;
        if (fireHiding) {
            closing = true;
            pendingResult = getResult();
            pendingButtonType = null;
        }

        @Nullable Throwable failure = null;
        if (fireHiding) {
            try {
                fireLifecycle(M3DialogEvent.HIDING, getOnHiding(), null);
            } catch (RuntimeException | Error exception) {
                failure = exception;
            }
        }
        try {
            completeHide(buttonType);
        } catch (RuntimeException | Error exception) {
            if (failure == null) {
                failure = exception;
            } else if (failure != exception) {
                failure.addSuppressed(exception);
            }
        }

        if (failure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (failure instanceof Error error) {
            throw error;
        }
    }

    /// Returns whether pane opacity can participate in presentation motion.
    private boolean canAnimatePresentation() {
        return !dialogPane.opacityProperty().isBound() && M3Animation.areAnimationsEnabled(dialogPane);
    }

    /// Restores the pane opacity captured before its latest show transition.
    private void restorePaneOpacity() {
        if (!dialogPane.opacityProperty().isBound()) {
            dialogPane.setOpacity(restingOpacity);
        }
    }

    /// Starts inherited owner-context synchronization for a presentation.
    private void startContextSynchronization(Node activeOwner) {
        stopContextSynchronization();
        contextSynchronizer = new M3PopupContextSynchronizer(
                activeOwner,
                presenter.contextRoot(),
                () -> {
                    @Nullable Scene scene = activeOwner.getScene();
                    return scene == null ? null : scene.getStylesheets();
                },
                () -> getTheme() == null ? M3ThemeResolver.findThemeRoot(activeOwner) : presenter.contextRoot()
        );
        contextSynchronizer.start();
    }

    /// Stops inherited context listeners after presentation ends or fails.
    private void stopContextSynchronization() {
        @Nullable M3PopupContextSynchronizer synchronizer = contextSynchronizer;
        contextSynchronizer = null;
        if (synchronizer != null) {
            synchronizer.stop();
        }
    }

    /// Observes the window that owns the current in-scene presentation.
    private void startOwnerWindowObservation(Node activeOwner) {
        stopOwnerWindowObservation();
        Scene scene = Objects.requireNonNull(activeOwner.getScene(), "owner scene");
        Window window = Objects.requireNonNull(scene.getWindow(), "owner window");
        observedOwnerWindow = window;
        window.addEventHandler(WindowEvent.WINDOW_HIDDEN, ownerWindowHiddenHandler);
    }

    /// Releases the current owner-window observation.
    private void stopOwnerWindowObservation() {
        @Nullable Window window = observedOwnerWindow;
        observedOwnerWindow = null;
        if (window != null) {
            window.removeEventHandler(WindowEvent.WINDOW_HIDDEN, ownerWindowHiddenHandler);
        }
    }

    /// Applies the explicit theme or refreshes inherited owner theme context.
    private void applyEffectiveTheme() {
        Parent contextRoot = presenter.contextRoot();
        @Nullable M3Theme explicitTheme = getTheme();
        if (explicitTheme == null) {
            clearExplicitTheme(contextRoot);
        } else {
            applyTheme(contextRoot, explicitTheme);
        }

        @Nullable M3PopupContextSynchronizer synchronizer = contextSynchronizer;
        if (synchronizer != null) {
            synchronizer.sync();
        }
        if (presenter.isShowing()) {
            presenter.sync();
        }
    }

    /// Returns the attached owner or throws a presentation-state exception.
    private Node requireShowingOwner() {
        @Nullable Node activeOwner = owner;
        @Nullable Scene scene = activeOwner == null ? null : activeOwner.getScene();
        if (activeOwner == null || scene == null || scene.getWindow() == null || !scene.getWindow().isShowing()) {
            throw new IllegalStateException("dialog owner must be attached to a showing window");
        }
        return activeOwner;
    }

    /// Rejects lifecycle calls made outside the JavaFX application thread.
    private static void checkFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("dialog lifecycle methods must run on the JavaFX application thread");
        }
    }

    /// Creates and dispatches one lifecycle event to its configured property handler.
    private M3DialogEvent fireLifecycle(
            javafx.event.EventType<M3DialogEvent> eventType,
            @Nullable EventHandler<M3DialogEvent> handler,
            @Nullable ButtonType buttonType
    ) {
        M3DialogEvent event = new M3DialogEvent(this, dialogPane, eventType, buttonType);
        if (handler != null) {
            handler.handle(event);
        }
        return event;
    }

    /// Returns the handler invoked immediately before this dialog begins presentation.
    ///
    /// @return the showing handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnShowing() {
        return onShowing.get();
    }

    /// Sets the handler invoked immediately before this dialog begins presentation.
    ///
    /// @param handler the showing handler, or `null` to remove it
    public final void setOnShowing(@Nullable EventHandler<M3DialogEvent> handler) {
        onShowing.set(handler);
    }

    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onShowingProperty() {
        return onShowing;
    }

    /// Returns the handler invoked after this dialog's overlay layer has been installed.
    ///
    /// @return the shown handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnShown() {
        return onShown.get();
    }

    /// Sets the handler invoked after this dialog's overlay layer has been installed.
    ///
    /// @param handler the shown handler, or `null` to remove it
    public final void setOnShown(@Nullable EventHandler<M3DialogEvent> handler) {
        onShown.set(handler);
    }

    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onShownProperty() {
        return onShown;
    }

    /// Returns the handler invoked before an accepted close transition.
    ///
    /// @return the hiding handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnHiding() {
        return onHiding.get();
    }

    /// Sets the handler invoked before an accepted close transition.
    ///
    /// @param handler the hiding handler, or `null` to remove it
    public final void setOnHiding(@Nullable EventHandler<M3DialogEvent> handler) {
        onHiding.set(handler);
    }

    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onHidingProperty() {
        return onHiding;
    }

    /// Returns the handler invoked after this dialog's layer has been removed.
    ///
    /// @return the hidden handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnHidden() {
        return onHidden.get();
    }

    /// Sets the handler invoked after this dialog's layer has been removed.
    ///
    /// @param handler the hidden handler, or `null` to remove it
    public final void setOnHidden(@Nullable EventHandler<M3DialogEvent> handler) {
        onHidden.set(handler);
    }

    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onHiddenProperty() {
        return onHidden;
    }

    /// Returns the handler invoked for cancellable close requests.
    ///
    /// @return the close-request handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnCloseRequest() {
        return onCloseRequest.get();
    }

    /// Sets the handler invoked for cancellable close requests.
    ///
    /// @param handler the close-request handler, or `null` to remove it
    public final void setOnCloseRequest(@Nullable EventHandler<M3DialogEvent> handler) {
        onCloseRequest.set(handler);
    }

    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onCloseRequestProperty() {
        return onCloseRequest;
    }

    /// Adds the shared M3FX stylesheet and fallback token class to a presentation context root.
    private static void installStylesheet(Parent root) {
        M3PopupStyles.addFallbackRootStyleClass(root);
        String stylesheet = M3ThemeRuntime.stylesheetUrl();
        moveOrAdd(root.getStylesheets(), stylesheet, 0);
    }

    /// Applies an explicit theme and its generated stylesheet to a presentation context root.
    private static void applyTheme(Parent root, M3Theme theme) {
        installStylesheet(root);
        M3ThemeMetadata.setTheme(root, theme);
        M3ThemeRuntime.applyThemeStyleClasses(root, theme);
        installThemeStylesheet(root, theme);
        if (!root.getProperties().containsKey(BASE_STYLE_PROPERTY_KEY)) {
            root.getProperties().put(BASE_STYLE_PROPERTY_KEY, root.getStyle());
        }

        Object baseStyleValue = root.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        String baseStyle = baseStyleValue instanceof String style ? style : "";
        root.setStyle(mergeStyles(baseStyle, M3ThemeCssCompiler.rootStyleDeclarations(theme)));
    }

    /// Clears explicit theme state so inherited owner context can be copied onto a presentation context root.
    private static void clearExplicitTheme(Parent root) {
        uninstallThemeStylesheet(root);
        M3ThemeRuntime.clearThemeStyleClasses(root);
        M3ThemeMetadata.clearTheme(root);
        Object baseStyleValue = root.getProperties().remove(BASE_STYLE_PROPERTY_KEY);
        if (baseStyleValue instanceof String baseStyle) {
            root.setStyle(baseStyle);
        }
    }

    /// Adds the generated stylesheet for an explicit theme to a presentation context root.
    private static void installThemeStylesheet(Parent root, M3Theme theme) {
        String stylesheet = M3ThemeRuntime.themeStylesheetUrl(theme);
        Object previousStylesheet = root.getProperties().put(THEME_STYLESHEET_PROPERTY_KEY, stylesheet);
        if (previousStylesheet instanceof String previous && !previous.equals(stylesheet)) {
            root.getStylesheets().remove(previous);
        }
        ObservableList<String> stylesheets = root.getStylesheets();
        int baseStylesheetIndex = stylesheets.indexOf(M3ThemeRuntime.stylesheetUrl());
        moveOrAdd(stylesheets, stylesheet, baseStylesheetIndex >= 0 ? baseStylesheetIndex + 1 : 0);
    }

    /// Removes the generated explicit-theme stylesheet from a presentation context root.
    private static void uninstallThemeStylesheet(Parent root) {
        Object previousStylesheet = root.getProperties().remove(THEME_STYLESHEET_PROPERTY_KEY);
        if (previousStylesheet instanceof String previous) {
            root.getStylesheets().remove(previous);
        }
    }

    /// Moves an existing stylesheet or inserts it at a requested bounded index.
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
