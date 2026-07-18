// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3DialogPresentation;
import org.glavo.m3fx.internal.M3EventHandlerManager;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3OverlayDialogPresentation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Describes a Material Design 3 dialog independently of its presentation host.
///
/// `M3Dialog` does not create or own a native window. [M3OverlayPane#showDialog(M3Dialog)] installs its pane and a
/// Material scrim in an existing scene, while [M3DialogWindow#showDialog(M3Dialog)] presents the same pane in a
/// dedicated native Stage without a cross-window scrim. Neither mode replaces an application scene root.
///
/// Both presentation methods are non-blocking and return an [M3DialogHandle] for that specific presentation. An
/// action button first bubbles its ordinary action event; consuming that event prevents dialog
/// closing. Otherwise the dialog emits a cancellable [M3DialogEvent#CLOSE_REQUEST] and runs its exit transition.
/// Lifecycle events expose the initiating [ButtonType], while application results remain in caller-owned state. They
/// are dispatched through this dialog's JavaFX [EventTarget] chain, so callers may register multiple filters and
/// handlers in addition to the singleton `onXxx` properties. Reduced-motion requests settle presentation
/// immediately.
/// In overlay mode, activating the surrounding scrim requests the same cancellable close by default and produces no
/// button type; [#dismissOnScrimClickProperty()] disables only that pointer-dismissal behavior while retaining
/// modality. The property has no effect in native-window mode because that mode has no Material scrim.
///
/// The selected host supplies stylesheets, node orientation, motion settings, and the effective Material theme.
/// Hiding or detaching its window forcibly removes the presentation and completes the hiding lifecycle without
/// emitting a cancellable close request. One dialog instance cannot be presented concurrently; after it is fully
/// hidden, it may be presented again by either host type.
///
/// ```java
/// M3Dialog dialog = new M3Dialog();
/// dialog.getDialogPane().setHeaderText("Delete item?");
/// dialog.getDialogPane().setContentText("This action cannot be undone.");
/// dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
/// dialog.setOnHidden(event -> {
///     if (event.getButtonType() == ButtonType.OK) {
///         deleteItem();
///     }
/// });
/// M3DialogHandle handle = overlayPane.showDialog(dialog);
/// // A later programmatic dismissal acts only on this presentation.
/// handle.requestClose();
/// ```
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
@NotNullByDefault
public class M3Dialog implements EventTarget {
    /// The retained Material pane rendered by this dialog.
    private final M3DialogPane dialogPane;

    /// Animates pane opacity during dialog entrance and exit.
    private final M3NodeTransition presentationAnimation;

    /// Owns this non-Node target's JavaFX event registrations and dispatch phases.
    private final M3EventHandlerManager eventHandlerManager = new M3EventHandlerManager(this);

    /// Whether a primary click on the surrounding scrim requests that this dialog close.
    ///
    /// A scrim dismissal follows the ordinary cancellable close lifecycle and produces no button type. Setting this
    /// property to `false` keeps the scrim modal but prevents pointer dismissal. Escape and action buttons are not
    /// affected. Native-window presentations do not have a scrim and ignore this value.
    ///
    /// @defaultValue `true`
    private final BooleanProperty dismissOnScrimClickValue =
            new SimpleBooleanProperty(this, "dismissOnScrimClick", true);

    /// The handle for the current presentation, or `null` while this dialog is detached.
    private @Nullable M3DialogHandle activeHandle;

    /// The backend for the current presentation, or `null` while this dialog is detached.
    private @Nullable M3DialogPresentation activePresentation;

    /// Handles host-window disappearance while this dialog owns a presentation surface.
    private final EventHandler<WindowEvent> hostWindowHiddenHandler = event -> handleHostUnavailable();

    /// Handles removal of the active host from its presentation scene.
    private final ChangeListener<@Nullable Scene> hostSceneListener =
            (observable, oldScene, newScene) -> handleHostUnavailable();

    /// Handles transfer of the active scene away from its presentation window.
    private final ChangeListener<@Nullable Window> hostWindowListener =
            (observable, oldWindow, newWindow) -> handleHostUnavailable();

    /// The presentation context root currently observed for forced cleanup.
    private @Nullable Parent observedContextRoot;

    /// The scene currently observed for forced presentation cleanup.
    private @Nullable Scene observedHostScene;

    /// The host window currently observed for forced presentation cleanup.
    private @Nullable Window observedHostWindow;

    /// Whether a show operation is currently dispatching lifecycle callbacks or installing its overlay.
    private boolean presenting;

    /// The pane opacity restored after presentation transitions.
    private double restingOpacity = 1.0;

    /// Whether an accepted close is currently running its exit transition.
    private boolean closing;

    /// Whether a cancellable close request is currently traversing this dialog's event chain.
    private boolean closeRequestPending;

    /// The action associated with the accepted close transition.
    private @Nullable ButtonType pendingButtonType;

    /// Whether the pane has completed the active exit transition.
    private boolean paneExitFinished;

    /// Whether the presentation background has completed the active exit transition.
    private boolean backgroundExitFinished;

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
        dismissOnScrimClickValue.addListener((observable, oldValue, enabled) -> {
            @Nullable M3DialogPresentation presentation = activePresentation;
            if (presentation != null) {
                presentation.setDismissOnScrimClick(enabled);
            }
        });
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

    /// Returns whether a primary click on the surrounding scrim requests that this dialog close.
    ///
    /// The value is retained independently of the current host and has no effect in [M3DialogWindow] mode.
    ///
    /// @return `true` when pointer activation of the scrim requests dismissal
    public final boolean isDismissOnScrimClick() {
        return dismissOnScrimClickValue.get();
    }

    /// Sets whether a primary click on the surrounding scrim requests that this dialog close.
    ///
    /// Disabling this property does not make the scrim mouse-transparent; lower content remains blocked while the
    /// dialog is showing in an overlay. Native-window presentations have no scrim and ignore this value.
    ///
    /// @param dismissOnScrimClick whether pointer activation of the scrim requests dismissal
    public final void setDismissOnScrimClick(boolean dismissOnScrimClick) {
        dismissOnScrimClickValue.set(dismissOnScrimClick);
    }

    /// Returns the property controlling pointer dismissal through the surrounding scrim.
    ///
    /// The property may be changed while an overlay presentation is showing. Native-window presentations retain but
    /// do not act on its value.
    ///
    /// @return the scrim pointer-dismissal property
    public final BooleanProperty dismissOnScrimClickProperty() {
        return dismissOnScrimClickValue;
    }

    /// Creates and presents an in-scene overlay presentation.
    ///
    /// @param host the overlay pane that owns the presentation
    /// @return the exact handle allocated for this presentation
    /// @throws IllegalStateException if presentation preconditions are not satisfied
    /// @throws NullPointerException  if `host` is `null`
    final M3DialogHandle present(M3OverlayPane host) {
        checkFxThread();
        M3OverlayPane nonNullHost = Objects.requireNonNull(host, "host");
        return present(new M3OverlayDialogPresentation(nonNullHost, dialogPane, this::handleScrimAction));
    }

    /// Creates and presents a dedicated native-window presentation.
    ///
    /// @param host the native window host that owns the presentation
    /// @return the exact handle allocated for this presentation
    /// @throws IllegalStateException if presentation preconditions are not satisfied
    /// @throws NullPointerException  if `host` is `null`
    final M3DialogHandle present(M3DialogWindow host) {
        checkFxThread();
        M3DialogWindow nonNullHost = Objects.requireNonNull(host, "host");
        return present(nonNullHost.createPresentation(dialogPane, this::handleWindowCloseRequest));
    }

    /// Runs the host-neutral showing lifecycle for one detached presentation backend.
    ///
    /// @param presentation the detached backend to install
    /// @return the exact handle allocated for this presentation
    /// @throws IllegalStateException if this dialog is already presented, its pane already has a parent, or the
    ///                               backend cannot currently be installed
    /// @throws NullPointerException  if `presentation` is `null`
    private M3DialogHandle present(M3DialogPresentation presentation) {
        M3DialogPresentation nonNullPresentation = Objects.requireNonNull(presentation, "presentation");
        M3DialogHandle handle = new M3DialogHandle(this, nonNullPresentation);
        boolean ownsPresentingState = false;
        boolean presented = false;
        try {
            if (activeHandle != null || activePresentation != null || presenting) {
                throw new IllegalStateException("dialog is already presented");
            }
            if (dialogPane.getParent() != null) {
                throw new IllegalStateException("dialog pane already belongs to a scene-graph parent");
            }

            nonNullPresentation.prepare();
            presenting = true;
            ownsPresentingState = true;
            activeHandle = handle;
            activePresentation = nonNullPresentation;
            nonNullPresentation.setDismissOnScrimClick(isDismissOnScrimClick());
            fireLifecycle(M3DialogEvent.SHOWING, null);
            pendingButtonType = null;
            paneExitFinished = false;
            backgroundExitFinished = false;
            closing = false;
            presentationAnimation.stop();
            restingOpacity = dialogPane.getOpacity();
            if (canAnimatePresentation(nonNullPresentation.getContextRoot())) {
                dialogPane.setOpacity(0.0);
            }
            dialogPane.setModalActive(true);
            nonNullPresentation.install();
            startHostWindowObservation(nonNullPresentation.getContextRoot());
            handle.markShowing();
            fireLifecycle(M3DialogEvent.SHOWN, null);
            presented = true;
            Platform.runLater(() -> {
                if (activeHandle == handle && handle.isShowing() && !closing) {
                    dialogPane.requestInitialFocus();
                }
            });
            if (isPresented() && !closing && canAnimatePresentation()) {
                playEntranceAnimation();
            } else if (isPresented() && !closing) {
                restorePaneOpacity();
            }
            return handle;
        } finally {
            if (!presented) {
                boolean ownsDialogState = activeHandle == handle || activePresentation == nonNullPresentation;
                if (ownsDialogState) {
                    presentationAnimation.stop();
                    dialogPane.setModalActive(false);
                    stopHostWindowObservation();
                    restorePaneOpacity();
                    pendingButtonType = null;
                    paneExitFinished = false;
                    backgroundExitFinished = false;
                    closing = false;
                    closeRequestPending = false;
                }
                nonNullPresentation.dispose();
                if (activeHandle == handle) {
                    activeHandle = null;
                }
                if (activePresentation == nonNullPresentation) {
                    activePresentation = null;
                }
                handle.detach();
            }
            if (ownsPresentingState) {
                presenting = false;
            }
        }
    }

    /// Requests closure through the handle for the current presentation.
    ///
    /// @param handle the presentation issuing the request
    /// @return `true` if this request started an accepted close transition; `false` if the handle is stale, the
    ///         request was consumed, or this presentation is already closing
    /// @throws IllegalStateException if called off the JavaFX Application Thread
    /// @throws NullPointerException  if `handle` is `null`
    final boolean requestClose(M3DialogHandle handle) {
        checkFxThread();
        M3DialogHandle nonNullHandle = Objects.requireNonNull(handle, "handle");
        @Nullable M3DialogPresentation presentation = activePresentation;
        if (activeHandle != nonNullHandle
                || presentation == null
                || !nonNullHandle.belongsTo(presentation)) {
            return false;
        }
        return requestCloseInternal(null);
    }

    /// Returns whether this dialog currently has an installed presentation.
    ///
    /// @return `true` while the active handle occupies its host surface
    private boolean isPresented() {
        @Nullable M3DialogHandle handle = activeHandle;
        return handle != null && activePresentation != null && handle.isShowing();
    }

    /// Handles an unconsumed action from one of the pane's action buttons.
    private void handleButtonAction(ButtonType buttonType) {
        requestCloseInternal(Objects.requireNonNull(buttonType, "buttonType"));
    }

    /// Handles activation of the surrounding scrim through the ordinary cancellable close lifecycle.
    private void handleScrimAction() {
        requestCloseInternal(null);
    }

    /// Handles a native window-manager close request through the ordinary cancellable lifecycle.
    private void handleWindowCloseRequest() {
        requestCloseInternal(null);
    }

    /// Emits a cancellable close request and starts an accepted exit transition.
    ///
    /// @return `true` if an accepted close transition started; `false` otherwise
    private boolean requestCloseInternal(@Nullable ButtonType buttonType) {
        if (!isPresented() || closing || closeRequestPending) {
            return false;
        }

        closeRequestPending = true;
        try {
            M3DialogEvent closeEvent = fireLifecycle(M3DialogEvent.CLOSE_REQUEST, buttonType);
            if (closeEvent.isConsumed()) {
                return false;
            }

            closing = true;
            pendingButtonType = buttonType;
            try {
                fireLifecycle(M3DialogEvent.HIDING, buttonType);
            } catch (RuntimeException | Error exception) {
                closing = false;
                pendingButtonType = null;
                throw exception;
            }

            if (canAnimatePresentation()) {
                paneExitFinished = false;
                backgroundExitFinished = false;
                M3DialogPresentation presentation =
                        Objects.requireNonNull(activePresentation, "active dialog presentation");
                presentation.startBackgroundExit(this::handleBackgroundExitFinished);
                playExitAnimation();
            } else {
                completeHide(buttonType);
            }
            return true;
        } finally {
            closeRequestPending = false;
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

    /// Starts the pane exit fade while retaining its host surface.
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

    /// Records completion of the presentation background exit transition.
    private void handleBackgroundExitFinished() {
        if (closing) {
            backgroundExitFinished = true;
            completeHideWhenExitFinished();
        }
    }

    /// Removes the presentation after the pane and background transitions have settled.
    private void completeHideWhenExitFinished() {
        if (closing && paneExitFinished && backgroundExitFinished) {
            completeHide(pendingButtonType);
        }
    }

    /// Removes the presentation surface and completes the accepted close lifecycle.
    private void completeHide(@Nullable ButtonType buttonType) {
        M3DialogHandle handle = Objects.requireNonNull(activeHandle, "active dialog handle");
        M3DialogPresentation presentation =
                Objects.requireNonNull(activePresentation, "active dialog presentation");
        activeHandle = null;
        activePresentation = null;
        presentationAnimation.stop();
        dialogPane.setModalActive(false);
        presentation.dispose();
        restorePaneOpacity();
        stopHostWindowObservation();
        pendingButtonType = null;
        paneExitFinished = false;
        backgroundExitFinished = false;
        closing = false;
        closeRequestPending = false;
        handle.detach();
        fireLifecycle(M3DialogEvent.HIDDEN, buttonType, handle);
    }

    /// Forces presentation cleanup after the host window has disappeared.
    private void handleHostUnavailable() {
        if (!isPresented()) {
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
                fireLifecycle(M3DialogEvent.HIDING, null);
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

    /// Returns whether entrance opacity can animate using the host context available before attachment.
    private boolean canAnimatePresentation(Parent contextRoot) {
        return !dialogPane.opacityProperty().isBound() && M3Animation.areAnimationsEnabled(contextRoot);
    }

    /// Restores the pane opacity captured before its latest show transition.
    private void restorePaneOpacity() {
        if (!dialogPane.opacityProperty().isBound()) {
            dialogPane.setOpacity(restingOpacity);
        }
    }

    /// Observes the context root and window that own the current presentation.
    private void startHostWindowObservation(Parent contextRoot) {
        stopHostWindowObservation();
        Scene scene = Objects.requireNonNull(contextRoot.getScene(), "host scene");
        Window window = Objects.requireNonNull(scene.getWindow(), "host window");
        observedContextRoot = contextRoot;
        observedHostScene = scene;
        observedHostWindow = window;
        contextRoot.sceneProperty().addListener(hostSceneListener);
        scene.windowProperty().addListener(hostWindowListener);
        window.addEventHandler(WindowEvent.WINDOW_HIDDEN, hostWindowHiddenHandler);
    }

    /// Releases the current host-window observation.
    private void stopHostWindowObservation() {
        @Nullable Parent contextRoot = observedContextRoot;
        observedContextRoot = null;
        if (contextRoot != null) {
            contextRoot.sceneProperty().removeListener(hostSceneListener);
        }

        @Nullable Scene scene = observedHostScene;
        observedHostScene = null;
        if (scene != null) {
            scene.windowProperty().removeListener(hostWindowListener);
        }

        @Nullable Window window = observedHostWindow;
        observedHostWindow = null;
        if (window != null) {
            window.removeEventHandler(WindowEvent.WINDOW_HIDDEN, hostWindowHiddenHandler);
        }
    }

    /// Rejects lifecycle calls made outside the JavaFX application thread.
    private static void checkFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("dialog presentation methods must run on the JavaFX application thread");
        }
    }

    /// Creates and dispatches one lifecycle event through this dialog's JavaFX event chain.
    private M3DialogEvent fireLifecycle(
            EventType<M3DialogEvent> eventType,
            @Nullable ButtonType buttonType
    ) {
        M3DialogHandle handle = Objects.requireNonNull(activeHandle, "active dialog handle");
        return fireLifecycle(eventType, buttonType, handle);
    }

    /// Creates and dispatches one lifecycle event for an explicitly retained presentation handle.
    private M3DialogEvent fireLifecycle(
            EventType<M3DialogEvent> eventType,
            @Nullable ButtonType buttonType,
            M3DialogHandle handle
    ) {
        M3DialogEvent event = new M3DialogEvent(this, handle, eventType, buttonType);
        Event.fireEvent(this, event);
        return event;
    }

    /// Adds this dialog's dispatcher to an event dispatch chain.
    ///
    /// Applications normally dispatch events with [Event#fireEvent(EventTarget, Event)] rather than invoking this
    /// method directly.
    ///
    /// @param tail the chain to which this dialog's dispatcher is prepended
    /// @return the supplied chain containing this dialog's dispatcher
    /// @throws NullPointerException if `tail` is `null`
    @Override
    public final EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return Objects.requireNonNull(tail, "tail").prepend(eventHandlerManager);
    }

    /// Registers an event handler that receives matching events during the bubbling phase.
    ///
    /// The same handler instance is registered at most once for a given event type. Event types are hierarchical, so
    /// a handler registered for [M3DialogEvent#ANY] also receives each concrete dialog lifecycle event.
    ///
    /// @param eventType    the event type accepted by the handler
    /// @param eventHandler the handler to register
    /// @param <E>          the event class accepted by the handler
    /// @throws NullPointerException if `eventType` or `eventHandler` is `null`
    @Override
    public final <E extends Event> void addEventHandler(
            EventType<E> eventType,
            EventHandler<? super E> eventHandler
    ) {
        eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    /// Removes a previously registered bubbling-phase event handler.
    ///
    /// The method has no effect when the same handler instance is not registered for the supplied event type.
    ///
    /// @param eventType    the event type from which to remove the handler
    /// @param eventHandler the handler to remove
    /// @param <E>          the event class accepted by the handler
    /// @throws NullPointerException if `eventType` or `eventHandler` is `null`
    @Override
    public final <E extends Event> void removeEventHandler(
            EventType<E> eventType,
            EventHandler<? super E> eventHandler
    ) {
        eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

    /// Registers an event filter that receives matching events during the capturing phase.
    ///
    /// Filters run before bubbling handlers and may consume an event to prevent the remaining dispatch chain from
    /// receiving it. The same filter instance is registered at most once for a given event type.
    ///
    /// @param eventType   the event type accepted by the filter
    /// @param eventFilter the filter to register
    /// @param <E>         the event class accepted by the filter
    /// @throws NullPointerException if `eventType` or `eventFilter` is `null`
    @Override
    public final <E extends Event> void addEventFilter(
            EventType<E> eventType,
            EventHandler<? super E> eventFilter
    ) {
        eventHandlerManager.addEventFilter(eventType, eventFilter);
    }

    /// Removes a previously registered capturing-phase event filter.
    ///
    /// The method has no effect when the same filter instance is not registered for the supplied event type.
    ///
    /// @param eventType   the event type from which to remove the filter
    /// @param eventFilter the filter to remove
    /// @param <E>         the event class accepted by the filter
    /// @throws NullPointerException if `eventType` or `eventFilter` is `null`
    @Override
    public final <E extends Event> void removeEventFilter(
            EventType<E> eventType,
            EventHandler<? super E> eventFilter
    ) {
        eventHandlerManager.removeEventFilter(eventType, eventFilter);
    }

    /// Returns the singleton handler invoked immediately before this dialog begins presentation.
    ///
    /// Additional handlers registered with [#addEventHandler(EventType, EventHandler)] are independent of this
    /// property and run before its handler.
    ///
    /// @return the showing handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnShowing() {
        return eventHandlerManager.getEventHandler(M3DialogEvent.SHOWING);
    }

    /// Sets the singleton handler invoked immediately before this dialog begins presentation.
    ///
    /// @param handler the showing handler, or `null` to remove it
    /// @throws RuntimeException if [#onShowingProperty()] is bound
    public final void setOnShowing(@Nullable EventHandler<M3DialogEvent> handler) {
        eventHandlerManager.setEventHandler(M3DialogEvent.SHOWING, handler);
    }

    /// Returns the property holding the singleton handler invoked immediately before presentation.
    ///
    /// @return the showing-handler property
    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onShowingProperty() {
        return eventHandlerManager.eventHandlerProperty(M3DialogEvent.SHOWING, "onShowing");
    }

    /// Returns the singleton handler invoked after this dialog's presentation surface has been installed.
    ///
    /// @return the shown handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnShown() {
        return eventHandlerManager.getEventHandler(M3DialogEvent.SHOWN);
    }

    /// Sets the singleton handler invoked after this dialog's presentation surface has been installed.
    ///
    /// @param handler the shown handler, or `null` to remove it
    /// @throws RuntimeException if [#onShownProperty()] is bound
    public final void setOnShown(@Nullable EventHandler<M3DialogEvent> handler) {
        eventHandlerManager.setEventHandler(M3DialogEvent.SHOWN, handler);
    }

    /// Returns the property holding the singleton handler invoked after the presentation surface is installed.
    ///
    /// @return the shown-handler property
    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onShownProperty() {
        return eventHandlerManager.eventHandlerProperty(M3DialogEvent.SHOWN, "onShown");
    }

    /// Returns the singleton handler invoked before an accepted close transition.
    ///
    /// @return the hiding handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnHiding() {
        return eventHandlerManager.getEventHandler(M3DialogEvent.HIDING);
    }

    /// Sets the singleton handler invoked before an accepted close transition.
    ///
    /// @param handler the hiding handler, or `null` to remove it
    /// @throws RuntimeException if [#onHidingProperty()] is bound
    public final void setOnHiding(@Nullable EventHandler<M3DialogEvent> handler) {
        eventHandlerManager.setEventHandler(M3DialogEvent.HIDING, handler);
    }

    /// Returns the property holding the singleton handler invoked before an accepted close transition.
    ///
    /// @return the hiding-handler property
    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onHidingProperty() {
        return eventHandlerManager.eventHandlerProperty(M3DialogEvent.HIDING, "onHiding");
    }

    /// Returns the singleton handler invoked after this dialog's layer has been removed.
    ///
    /// @return the hidden handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnHidden() {
        return eventHandlerManager.getEventHandler(M3DialogEvent.HIDDEN);
    }

    /// Sets the singleton handler invoked after this dialog's layer has been removed.
    ///
    /// @param handler the hidden handler, or `null` to remove it
    /// @throws RuntimeException if [#onHiddenProperty()] is bound
    public final void setOnHidden(@Nullable EventHandler<M3DialogEvent> handler) {
        eventHandlerManager.setEventHandler(M3DialogEvent.HIDDEN, handler);
    }

    /// Returns the property holding the singleton handler invoked after the presentation surface is removed.
    ///
    /// @return the hidden-handler property
    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onHiddenProperty() {
        return eventHandlerManager.eventHandlerProperty(M3DialogEvent.HIDDEN, "onHidden");
    }

    /// Returns the singleton handler invoked for cancellable close requests.
    ///
    /// @return the close-request handler, or `null` when none is installed
    public final @Nullable EventHandler<M3DialogEvent> getOnCloseRequest() {
        return eventHandlerManager.getEventHandler(M3DialogEvent.CLOSE_REQUEST);
    }

    /// Sets the singleton handler invoked for cancellable close requests.
    ///
    /// @param handler the close-request handler, or `null` to remove it
    /// @throws RuntimeException if [#onCloseRequestProperty()] is bound
    public final void setOnCloseRequest(@Nullable EventHandler<M3DialogEvent> handler) {
        eventHandlerManager.setEventHandler(M3DialogEvent.CLOSE_REQUEST, handler);
    }

    /// Returns the property holding the singleton handler invoked for cancellable close requests.
    ///
    /// @return the close-request-handler property
    public final ObjectProperty<@Nullable EventHandler<M3DialogEvent>> onCloseRequestProperty() {
        return eventHandlerManager.eventHandlerProperty(M3DialogEvent.CLOSE_REQUEST, "onCloseRequest");
    }

}
