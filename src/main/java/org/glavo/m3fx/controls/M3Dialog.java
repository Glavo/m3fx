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
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3DialogPresenter;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Controls a Material Design 3 modal dialog rendered inside an owner scene.
///
/// `M3Dialog` does not create a native window and does not use JavaFX [javafx.scene.control.Dialog] modality. Its
/// pane and scrim are installed in the [M3OverlayPane] containing the owner, so the owner window retains normal
/// window-manager behavior while application content remains blocked by the scrim and focus trap. Presentation
/// never replaces the scene root. Closing the dialog removes only its overlay layer and restores prior focus when
/// no newer overlay has superseded it.
///
/// [#show()] is non-blocking. An action button first bubbles its ordinary action event; consuming that event prevents
/// dialog closing. Otherwise the dialog emits a cancellable [M3DialogEvent#CLOSE_REQUEST] and runs its exit
/// transition. Lifecycle events expose the initiating [ButtonType], while application results remain in caller-owned
/// state. Reduced-motion requests settle presentation immediately.
/// Activating the surrounding scrim requests the same cancellable close by default and produces no button type;
/// [#dismissOnScrimClickProperty()] disables only that pointer-dismissal behavior while retaining modality.
///
/// A dialog must have an attached [owner][#setOwner(Node)] inside an [M3OverlayPane] before it is shown. The owner
/// also supplies scene stylesheets, node orientation, motion settings, and its effective Material theme. A theme
/// installed on the owner scene is authoritative; without one, the nearest locally themed owner ancestor is used.
/// Hiding the owner window forcibly removes the overlay and completes the hiding lifecycle without emitting a
/// cancellable close request.
///
/// ```java
/// M3Dialog dialog = new M3Dialog();
/// dialog.setOwner(ownerNode);
/// dialog.getDialogPane().setHeaderText("Delete item?");
/// dialog.getDialogPane().setContentText("This action cannot be undone.");
/// dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
/// dialog.setOnHidden(event -> {
///     if (event.getButtonType() == ButtonType.OK) {
///         deleteItem();
///     }
/// });
/// dialog.show();
/// ```
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
@NotNullByDefault
public class M3Dialog {
    /// The retained Material pane rendered by this dialog.
    private final M3DialogPane dialogPane;

    /// Installs the pane and scrim into the configured owner scene.
    private final M3DialogPresenter presenter;

    /// Animates pane opacity during dialog entrance and exit.
    private final M3NodeTransition presentationAnimation;

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
    /// Throwing from this handler cancels the pending transition and keeps the dialog visible during an ordinary
    /// close. If the owner window has already hidden, presentation cleanup cannot be cancelled; the exception is
    /// rethrown after the overlay is removed.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onHiding =
            new SimpleObjectProperty<>(this, "onHiding");

    /// The handler invoked after this dialog's layer has been removed.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onHidden =
            new SimpleObjectProperty<>(this, "onHidden");

    /// The handler invoked whenever code or an action button requests that the dialog close.
    ///
    /// Calling [M3DialogEvent#consume()] from this handler rejects the request before lifecycle mutation, scrim
    /// motion, or pane exit motion begins.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onCloseRequest =
            new SimpleObjectProperty<>(this, "onCloseRequest");

    /// The node whose scene and local context own this dialog.
    private @Nullable Node owner;

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

    /// The action associated with the accepted close transition.
    private @Nullable ButtonType pendingButtonType;

    /// Whether the pane has completed the active exit transition.
    private boolean paneExitFinished;

    /// Whether the scrim has completed the active exit transition.
    private boolean scrimExitFinished;

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

    /// Returns the read-only property reporting whether this dialog owns an installed overlay layer.
    ///
    /// @return the read-only showing property
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
            pendingButtonType = null;
            paneExitFinished = false;
            scrimExitFinished = false;
            closing = false;
            presentationAnimation.stop();
            restingOpacity = dialogPane.getOpacity();
            if (canAnimatePresentation(activeOwner)) {
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
                stopOwnerWindowObservation();
                restorePaneOpacity();
                showing.set(false);
            }
            presenting = false;
        }
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

        closing = true;
        pendingButtonType = buttonType;
        try {
            fireLifecycle(M3DialogEvent.HIDING, getOnHiding(), buttonType);
        } catch (RuntimeException | Error exception) {
            closing = false;
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

    /// Removes the overlay and completes the accepted close lifecycle.
    private void completeHide(@Nullable ButtonType buttonType) {
        presentationAnimation.stop();
        dialogPane.setModalActive(false);
        presenter.dispose();
        restorePaneOpacity();
        stopOwnerWindowObservation();
        pendingButtonType = null;
        paneExitFinished = false;
        scrimExitFinished = false;
        closing = false;
        showing.set(false);
        fireLifecycle(M3DialogEvent.HIDDEN, getOnHidden(), buttonType);
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

    /// Returns whether entrance opacity can animate using the owner context available before attachment.
    private boolean canAnimatePresentation(Node activeOwner) {
        return !dialogPane.opacityProperty().isBound() && M3Animation.areAnimationsEnabled(activeOwner);
    }

    /// Restores the pane opacity captured before its latest show transition.
    private void restorePaneOpacity() {
        if (!dialogPane.opacityProperty().isBound()) {
            dialogPane.setOpacity(restingOpacity);
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

    /// Returns the property holding the handler invoked immediately before presentation.
    ///
    /// @return the showing-handler property
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

    /// Returns the property holding the handler invoked after the overlay layer is installed.
    ///
    /// @return the shown-handler property
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

    /// Returns the property holding the handler invoked before an accepted close transition.
    ///
    /// @return the hiding-handler property
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

    /// Returns the property holding the handler invoked after the overlay layer is removed.
    ///
    /// @return the hidden-handler property
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

    /// Returns the property holding the handler invoked for cancellable close requests.
    ///
    /// @return the close-request-handler property
    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onCloseRequestProperty() {
        return onCloseRequest;
    }

}
