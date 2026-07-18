// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.skins.M3SnackbarHostSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;

/// Internal presentation host for transient Material Design 3 snackbar messages.
///
/// `M3SnackbarHostImpl` owns the visible snackbar slot, pending FIFO queue, automatic timeout, accessibility route,
/// and reusable entrance and exit transitions for one [org.glavo.m3fx.controls.M3OverlayPane]. Applications use the
/// overlay pane's snackbar API and never install this implementation node directly.
@NotNullByDefault
public final class M3SnackbarHostImpl extends Control {
    /// The base style class for M3FX snackbar hosts.
    public static final String STYLE_CLASS = "m3-snackbar-host";

    /// The initial vertical offset used by snackbar entrance and exit motion.
    private static final double TRANSITION_OFFSET_Y = 16.0;

    /// Private node-property key recording the host that currently owns a snackbar instance.
    private static final Object OWNER_PROPERTY_KEY = new Object();

    /// Backing property for the current snackbar API owned by the overlay pane.
    private final ReadOnlyObjectWrapper<@Nullable M3Snackbar> snackbar;

    /// Pending snackbars waiting to be shown.
    private final ObservableList<M3Snackbar> queue = M3ObservableLists.nonNullElementList("snackbar");

    /// Read-only view of pending snackbars.
    private final @UnmodifiableView ObservableList<M3Snackbar> queueView =
            FXCollections.unmodifiableObservableList(queue);

    /// The display duration used for automatic dismissal.
    ///
    /// `null` selects the duration from the effective motion behavior. Zero, unknown, and indefinite durations
    /// disable automatic dismissal. The timer applies only while a non-actionable snackbar without a close
    /// affordance is visible in a showing window. Changing this property reschedules or cancels the current timer.
    private final ObjectProperty<@Nullable Duration> displayDuration;

    /// Backing property for the showing state owned by the overlay pane.
    private final ReadOnlyBooleanWrapper showing;

    /// The automatic dismissal timer.
    private final PauseTransition displayTimer = new PauseTransition();

    /// The reusable entrance animation.
    private final SnackbarTransition showAnimation = new SnackbarTransition(true);

    /// The reusable exit animation.
    private final SnackbarTransition hideAnimation = new SnackbarTransition(false);

    /// The snackbar associated with the current display timer.
    private @Nullable M3Snackbar displayTimerTarget;

    /// Whether a modal overlay currently blocks snackbar interaction and timeout progress.
    private boolean modalBlocked;

    /// Observes runtime motion settings while a snackbar is active.
    private final M3MotionSettingsObserver motionSettingsObserver;

    /// Reports hosted snackbar focus changes to accessibility clients.
    private final M3AccessibleFocusNotifier focusNotifier;

    /// Refreshes automatic dismissal when the current snackbar gains or loses an interactive affordance.
    private final InvalidationListener snackbarInteractivityInvalidation = observable -> refreshDisplayTimer();

    /// Dismisses the current snackbar when its close affordance requests dismissal.
    private final EventHandler<Event> dismissRequestHandler = event -> {
        if (event.getSource() == getSnackbar()) {
            event.consume();
            dismiss();
        }
    };

    /// Creates an empty snackbar host backed by state properties owned by its overlay pane.
    ///
    /// @param snackbar        the current-snackbar state to update
    /// @param showing         the visible-display state to update
    /// @param displayDuration the optional explicit automatic-dismissal duration to observe
    public M3SnackbarHostImpl(
            ReadOnlyObjectWrapper<@Nullable M3Snackbar> snackbar,
            ReadOnlyBooleanWrapper showing,
            ObjectProperty<@Nullable Duration> displayDuration
    ) {
        this.snackbar = Objects.requireNonNull(snackbar, "snackbar");
        this.showing = Objects.requireNonNull(showing, "showing");
        this.displayDuration = Objects.requireNonNull(displayDuration, "displayDuration");
        motionSettingsObserver = new M3MotionSettingsObserver(this, this::refreshMotionSettings, false);
        focusNotifier = new M3AccessibleFocusNotifier(this, this::currentFocusNode);
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusCurrentAccessibleNode, this::showAccessibleSnackbar);
        setPickOnBounds(false);
        showing.addListener((observable, oldValue, newValue) -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
        });
        queue.addListener((ListChangeListener<M3Snackbar>) change ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT));
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        displayTimer.setOnFinished(event -> handleDisplayTimerFinished());
        displayDuration.addListener(observable -> refreshDisplayTimer());
        focusNotifier.start();
    }

    /// Returns the currently hosted snackbar.
    ///
    /// @return the currently hosted snackbar, or `null` when the host is idle
    public @Nullable M3Snackbar getSnackbar() {
        return snackbar.get();
    }

    /// Returns the read-only property containing the currently hosted snackbar.
    ///
    /// @return the current-snackbar property
    public ReadOnlyObjectProperty<@Nullable M3Snackbar> snackbarProperty() {
        return snackbar.getReadOnlyProperty();
    }

    /// Returns whether the current snackbar is in its visible display phase.
    ///
    /// @return `true` if the current snackbar is in its visible display phase
    public boolean isShowing() {
        return showing.get();
    }

    /// Returns the pending snackbars waiting to be shown.
    ///
    /// The returned list is an unmodifiable, live, observable view in FIFO order. It excludes the current snackbar.
    /// Use [#enqueue(M3Snackbar)] to append an item and [#clearQueue()] to remove pending items.
    ///
    /// @return the unmodifiable live queue of pending snackbars
    public @UnmodifiableView ObservableList<M3Snackbar> getQueue() {
        return queueView;
    }

    /// Returns the display duration before automatic dismissal.
    ///
    /// The duration applies only to snackbars without an action or close affordance. Actionable snackbars remain
    /// visible until an action, dismissal request, replacement, or explicit host dismissal occurs. A null value
    /// resolves from the active [org.glavo.m3fx.animation.M3MotionBehavior]. A zero, unknown, or indefinite duration
    /// disables automatic dismissal.
    ///
    /// @return the explicitly configured duration, or the duration supplied by the effective motion behavior when
    ///     the property contains `null`
    private Duration getDisplayDuration() {
        @Nullable Duration duration = displayDuration.get();
        return duration == null ? M3Animation.motionBehavior(this).snackbarDisplayDuration() : duration;
    }

    /// Updates whether a modal overlay blocks this host.
    ///
    /// Blocking suppresses accessibility focus and reveal actions and pauses automatic dismissal. The visible
    /// snackbar remains rendered below the modal scrim and resumes a fresh timeout when the modal stack clears.
    ///
    /// @param modalBlocked whether a modal overlay blocks snackbar interaction
    public void setModalBlocked(boolean modalBlocked) {
        if (this.modalBlocked == modalBlocked) {
            return;
        }

        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        boolean expandedResultChanges = isShowing();
        int itemCount = (currentSnackbar == null ? 0 : 1) + queue.size();
        String currentText = currentSnackbarText();
        @Nullable Node previousFocusNode = currentFocusNode();

        this.modalBlocked = modalBlocked;
        refreshDisplayTimer();
        if (currentSnackbar != null) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        }
        if (expandedResultChanges) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        }
        @Nullable Node currentFocusNode = currentFocusNode();
        if (previousFocusNode != currentFocusNode) {
            M3Accessible.notifyFocusNodeChanged(this);
        }
        focusNotifier.refresh();
        if (itemCount > 0) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_AT_INDEX);
        }
        if (!currentText.isEmpty()) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }
    }

    /// Adds the supplied snackbar to the end of the display queue.
    ///
    /// If the host is idle, the snackbar becomes current immediately instead of being retained in [#getQueue()].
    /// Otherwise it is appended after all pending snackbars. The same snackbar instance may be queued more than
    /// once; each occurrence is processed in insertion order.
    ///
    /// @param snackbar the snackbar to enqueue
    /// @throws NullPointerException     if `snackbar` is `null`
    /// @throws IllegalArgumentException if `snackbar` belongs to another overlay pane or an unrelated parent
    public void enqueue(M3Snackbar snackbar) {
        Objects.requireNonNull(snackbar, "snackbar");
        if (getSnackbar() == null && !showing.get()) {
            show(snackbar);
        } else {
            claimSnackbar(snackbar);
            try {
                queue.add(snackbar);
            } catch (RuntimeException | Error exception) {
                releaseSnackbarIfUnused(snackbar);
                throw exception;
            }
        }
    }

    /// Clears pending snackbars without dismissing or otherwise modifying the currently hosted snackbar.
    public void clearQueue() {
        while (!queue.isEmpty()) {
            M3Snackbar removedSnackbar = queue.remove(queue.size() - 1);
            releaseSnackbarIfUnused(removedSnackbar);
        }
    }

    /// Shows the supplied snackbar.
    ///
    /// This operation replaces the current snackbar immediately and does not modify the pending queue. Calling it
    /// with the current snackbar restarts its visible phase. Use [#enqueue(M3Snackbar)] when existing messages must
    /// retain FIFO ordering.
    ///
    /// @param snackbar the snackbar to show
    /// @throws NullPointerException     if `snackbar` is `null`
    /// @throws IllegalArgumentException if `snackbar` belongs to another overlay pane or an unrelated parent
    public void show(M3Snackbar snackbar) {
        Objects.requireNonNull(snackbar, "snackbar");
        validateSnackbarClaim(snackbar);
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        show(snackbar, currentSnackbar != null && snackbarFocusNodeOwnsFocus(currentSnackbar));
    }

    /// Shows the supplied snackbar and optionally transfers current snackbar action focus to it.
    private void show(M3Snackbar snackbar, boolean transferActionFocus) {
        claimSnackbar(snackbar);
        motionSettingsObserver.start();

        stopDisplayTimer();
        stopTransition(showAnimation);
        stopTransition(hideAnimation);

        @Nullable M3Snackbar previousSnackbar = getSnackbar();
        boolean enteringNewSnackbar = previousSnackbar != snackbar;
        if (enteringNewSnackbar) {
            if (previousSnackbar != null) {
                previousSnackbar.actionTextProperty().removeListener(snackbarInteractivityInvalidation);
                previousSnackbar.closeButtonVisibleProperty().removeListener(snackbarInteractivityInvalidation);
                previousSnackbar.removeEventHandler(M3Snackbar.DISMISS_REQUEST, dismissRequestHandler);
                resetSnackbar(previousSnackbar);
            }
            this.snackbar.set(snackbar);
            if (previousSnackbar != null) {
                releaseSnackbarIfUnused(previousSnackbar);
            }
            snackbar.actionTextProperty().addListener(snackbarInteractivityInvalidation);
            snackbar.closeButtonVisibleProperty().addListener(snackbarInteractivityInvalidation);
            snackbar.addEventHandler(M3Snackbar.DISMISS_REQUEST, dismissRequestHandler);
            snackbar.setOpacity(0.0);
            snackbar.setTranslateY(TRANSITION_OFFSET_Y);
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
            notifyFocusNodeChanged();
        }

        snackbar.setManaged(true);
        snackbar.setVisible(true);
        showing.set(true);
        refreshAccessibleFocusNode();
        playShowAnimation(snackbar);
        if (transferActionFocus) {
            focusCurrentAccessibleNode();
        }
    }

    /// Dismisses the currently hosted snackbar.
    ///
    /// The operation is a no-op while the host is idle or the current snackbar is already leaving. After dismissal
    /// completes, the first pending snackbar becomes current.
    public void dismiss() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar == null || !showing.get()) {
            return;
        }

        stopDisplayTimer();
        stopTransition(showAnimation);
        showing.set(false);
        playHideAnimation(currentSnackbar);
    }

    /// Clears pending snackbars and dismisses the currently hosted snackbar when one is visible.
    ///
    /// This method is idempotent when the host and queue are already empty.
    public void dismissAll() {
        clearQueue();
        dismiss();
    }

    /// Returns the user-agent stylesheet for M3FX snackbar hosts.
    ///
    /// @return the snackbar user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("snackbar.css");
    }

    /// Creates the default Material Design 3 snackbar host skin.
    ///
    /// @return the default Material Design 3 snackbar host skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SnackbarHostSkin(this);
    }

    /// Returns accessibility attributes for the current snackbar and queue state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (modalBlocked) {
            return switch (attribute) {
                case CONTENTS, FOCUS_NODE, ITEM_AT_INDEX -> null;
                case EXPANDED -> false;
                case ITEM_COUNT -> 0;
                case TEXT -> "";
                default -> super.queryAccessibleAttribute(attribute, parameters);
            };
        }
        return switch (attribute) {
            case CONTENTS -> getSnackbar();
            case EXPANDED -> isShowing();
            case FOCUS_NODE -> currentFocusNode();
            case ITEM_COUNT -> (getSnackbar() == null ? 0 : 1) + queue.size();
            case ITEM_AT_INDEX -> snackbarAt(parameters);
            case TEXT -> currentSnackbarText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by the snackbar host.
    ///
    /// @param action     the requested accessibility action
    /// @param parameters the optional action parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled() || modalBlocked) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case COLLAPSE -> dismiss();
            case REQUEST_FOCUS -> {
                if (M3Accessible.canReach(this)) {
                    focusCurrentAccessibleNode();
                }
            }
            case SHOW_ITEM -> {
                if (M3Accessible.canReach(this) || isDetachedReachableHost()) {
                    showAccessibleSnackbar(parameters);
                }
            }
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Handles keyboard dismissal while focus is inside the snackbar host.
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && isShowing()) {
            dismiss();
            event.consume();
        }
    }

    /// Plays the snackbar entrance animation from its currently rendered state.
    private void playShowAnimation(M3Snackbar target) {
        showAnimation.configure(M3Animation.fastSpatial(this), target, 1.0, 0.0);
        playConfiguredTransition(showAnimation);
    }

    /// Plays the snackbar exit animation from its currently rendered state.
    private void playHideAnimation(M3Snackbar target) {
        hideAnimation.configure(
                M3Animation.fastSpatial(this),
                target,
                0.0,
                TRANSITION_OFFSET_Y
        );
        playConfiguredTransition(hideAnimation);
    }

    /// Starts a configured transition or settles an already completed visual state.
    private void playConfiguredTransition(SnackbarTransition transition) {
        if (transition.isEntrance() && !transition.hasVisualChange()) {
            M3Animation.finish(transition);
        } else {
            M3Animation.playFromStart(this, transition);
        }
    }

    /// Stops one reusable transition and releases its snackbar target.
    private static void stopTransition(SnackbarTransition transition) {
        transition.stop();
        transition.clearTarget();
    }

    /// Applies changed runtime motion settings and window lifecycle to active snackbar work.
    private void refreshMotionSettings() {
        if (isWindowUnavailable()) {
            M3Animation.finishIfRunning(showAnimation);
            M3Animation.finishIfRunning(hideAnimation);
        }
        refreshDisplayTimer();
    }

    /// Returns whether this host lacks a currently showing window.
    private boolean isWindowUnavailable() {
        @Nullable Scene scene = getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        return window == null || !window.isShowing();
    }

    /// Schedules automatic dismissal for the target snackbar.
    private void scheduleAutoDismiss(M3Snackbar target) {
        if (getSnackbar() == target && showing.get()) {
            refreshDisplayTimer();
        }
    }

    /// Applies the current display duration to the automatic dismissal timer.
    private void refreshDisplayTimer() {
        @Nullable M3Snackbar target = getSnackbar();
        Duration duration = getDisplayDuration();
        if (target == null
                || !showing.get()
                || modalBlocked
                || isWindowUnavailable()
                || target.hasAction()
                || target.isCloseButtonVisible()
                || duration.isUnknown()
                || duration.isIndefinite()
                || duration.lessThanOrEqualTo(Duration.ZERO)) {
            stopDisplayTimer();
            return;
        }

        displayTimerTarget = target;
        if (showAnimation.getStatus() == Animation.Status.RUNNING) {
            displayTimer.stop();
            displayTimer.setDuration(duration);
            return;
        }
        M3Animation.updatePauseDuration(displayTimer, duration, true);
        if (displayTimer.getStatus() != Animation.Status.RUNNING) {
            displayTimer.playFromStart();
        }
    }

    /// Stops automatic dismissal and releases the snackbar retained for its callback.
    private void stopDisplayTimer() {
        displayTimer.stop();
        displayTimerTarget = null;
    }

    /// Dismisses the snackbar retained by the stable display timer callback.
    private void handleDisplayTimerFinished() {
        @Nullable M3Snackbar target = displayTimerTarget;
        displayTimerTarget = null;
        if (target != null && getSnackbar() == target && showing.get()) {
            dismiss();
        }
    }

    /// Handles completion of a reusable entrance or exit transition.
    private void handleTransitionFinished(SnackbarTransition transition) {
        @Nullable M3Snackbar target = transition.takeTarget();
        if (target == null) {
            return;
        }
        if (transition.isEntrance()) {
            scheduleAutoDismiss(target);
        } else {
            removeSnackbar(target);
        }
    }

    /// Reuses one primitive transition for a snackbar entrance or exit.
    @NotNullByDefault
    private final class SnackbarTransition extends M3FiniteTransition {
        /// Whether this transition represents entrance rather than exit.
        private final boolean entrance;

        /// The snackbar receiving the current transition, or null while idle.
        private @Nullable M3Snackbar target;

        /// The starting opacity.
        private double startOpacity;

        /// The target opacity.
        private double targetOpacity;

        /// The starting vertical translation.
        private double startTranslateY;

        /// The target vertical translation.
        private double targetTranslateY;

        /// Whether the current start and target states differ visibly.
        private boolean visualChange;

        /// Creates a reusable snackbar transition.
        private SnackbarTransition(boolean entrance) {
            this.entrance = entrance;
            setOnFinished(event -> handleTransitionFinished(this));
        }

        /// Captures current rendered values and configures the next target state.
        private void configure(
                M3MotionSpec spec,
                M3Snackbar target,
                double targetOpacity,
                double targetTranslateY
        ) {
            stop();
            clearTarget();
            this.target = target;
            startOpacity = target.getOpacity();
            startTranslateY = target.getTranslateY();
            this.targetOpacity = targetOpacity;
            this.targetTranslateY = targetTranslateY;
            visualChange = Double.compare(startOpacity, targetOpacity) != 0
                    || Double.compare(startTranslateY, targetTranslateY) != 0;
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
        }

        /// Returns whether this transition represents snackbar entrance.
        private boolean isEntrance() {
            return entrance;
        }

        /// Returns whether the configured state requires visible interpolation.
        private boolean hasVisualChange() {
            return visualChange;
        }

        /// Clears and returns the current target.
        private @Nullable M3Snackbar takeTarget() {
            @Nullable M3Snackbar currentTarget = target;
            clearTarget();
            return currentTarget;
        }

        /// Releases the current snackbar target.
        private void clearTarget() {
            target = null;
            visualChange = false;
        }

        /// Applies one eased frame without allocating key frames or writable values.
        @Override
        protected void interpolate(double fraction) {
            @Nullable M3Snackbar currentTarget = target;
            if (currentTarget == null) {
                return;
            }
            currentTarget.setOpacity(interpolate(startOpacity, targetOpacity, fraction));
            currentTarget.setTranslateY(interpolate(startTranslateY, targetTranslateY, fraction));
        }

        /// Interpolates one primitive channel.
        private static double interpolate(double start, double end, double fraction) {
            return start + (end - start) * fraction;
        }
    }

    /// Removes the snackbar after its exit transition finishes.
    private void removeSnackbar(M3Snackbar target) {
        if (getSnackbar() != target) {
            return;
        }

        boolean transferActionFocus = snackbarFocusNodeOwnsFocus(target);
        target.actionTextProperty().removeListener(snackbarInteractivityInvalidation);
        target.closeButtonVisibleProperty().removeListener(snackbarInteractivityInvalidation);
        target.removeEventHandler(M3Snackbar.DISMISS_REQUEST, dismissRequestHandler);
        resetSnackbar(target);
        this.snackbar.set(null);
        releaseSnackbarIfUnused(target);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyFocusNodeChanged();
        showNextQueuedSnackbar(transferActionFocus);
    }

    /// Resets a snackbar after it leaves the host.
    private static void resetSnackbar(M3Snackbar target) {
        target.setVisible(false);
        target.setManaged(false);
        target.setOpacity(1.0);
        target.setTranslateY(0.0);
    }

    /// Claims a snackbar for this host after validating its existing ownership and parent.
    private void claimSnackbar(M3Snackbar target) {
        validateSnackbarClaim(target);
        if (!target.hasProperties() || target.getProperties().get(OWNER_PROPERTY_KEY) == null) {
            target.getProperties().put(OWNER_PROPERTY_KEY, this);
        }
    }

    /// Validates that a snackbar can be used by this host without changing either host's state.
    private void validateSnackbarClaim(M3Snackbar target) {
        Objects.requireNonNull(target, "target");
        @Nullable Object owner = target.hasProperties() ? target.getProperties().get(OWNER_PROPERTY_KEY) : null;
        if (owner != null && owner != this) {
            throw new IllegalArgumentException("snackbar is already claimed by another M3OverlayPane");
        }

        @Nullable Node parent = target.getParent();
        if (parent != null && parent != this) {
            throw new IllegalArgumentException("snackbar already belongs to an unrelated parent");
        }
    }

    /// Releases this host's claim when the snackbar is neither current nor queued by identity.
    private void releaseSnackbarIfUnused(M3Snackbar target) {
        if (getSnackbar() == target || queueContainsIdentity(target) || !target.hasProperties()) {
            return;
        }
        target.getProperties().remove(OWNER_PROPERTY_KEY, this);
    }

    /// Returns whether the pending queue contains the supplied snackbar instance.
    private boolean queueContainsIdentity(M3Snackbar target) {
        for (M3Snackbar queuedSnackbar : queue) {
            if (queuedSnackbar == target) {
                return true;
            }
        }
        return false;
    }

    /// Removes the first queued occurrence of the supplied snackbar instance.
    private boolean removeFirstQueuedSnackbar(M3Snackbar target) {
        for (int index = 0; index < queue.size(); index++) {
            if (queue.get(index) == target) {
                queue.remove(index);
                return true;
            }
        }
        return false;
    }

    /// Shows the next queued snackbar and optionally transfers focused action ownership to it.
    private void showNextQueuedSnackbar(boolean transferActionFocus) {
        if (getSnackbar() != null) {
            return;
        }
        if (queue.isEmpty()) {
            motionSettingsObserver.stop();
            return;
        }

        M3Snackbar nextSnackbar = queue.get(0);
        validateSnackbarClaim(nextSnackbar);
        queue.remove(0);
        try {
            show(nextSnackbar, transferActionFocus);
        } catch (RuntimeException | Error exception) {
            releaseSnackbarIfUnused(nextSnackbar);
            throw exception;
        }
    }

    /// Returns the current or queued snackbar at the supplied accessibility index.
    private @Nullable Node snackbarAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar != null) {
            if (index == 0) {
                return currentSnackbar;
            }
            index--;
        }
        return index < queue.size() ? queue.get(index) : null;
    }

    /// Returns the preferred focus node for the current snackbar.
    private @Nullable Node currentFocusNode() {
        if (modalBlocked || !M3Accessible.canReach(this)) {
            return null;
        }

        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar == null || !isShowing()) {
            return null;
        }

        @Nullable Object focusNode = currentSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof Node node ? node : null;
    }

    /// Refreshes the current snackbar skin before reporting its focus node to accessibility clients.
    private void refreshAccessibleFocusNode() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar != null) {
            currentSnackbar.applyCss();
        }
        notifyFocusNodeChanged();
    }

    /// Focuses the current snackbar action focus node when it is reachable.
    ///
    /// @return `true` when the current snackbar focus node accepted focus
    boolean focusCurrentAccessibleNode() {
        if (M3Accessible.showItem(this, currentFocusNode())) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns whether the supplied snackbar's exposed action target owns keyboard focus.
    private boolean snackbarFocusNodeOwnsFocus(M3Snackbar target) {
        Objects.requireNonNull(target, "target");
        if (!M3Accessible.canReach(this)) {
            return false;
        }

        target.applyCss();
        @Nullable Object focusNode = target.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof Node node && nodeOwnsFocus(node);
    }

    /// Returns whether the supplied node or one of its descendants owns keyboard focus.
    private static boolean nodeOwnsFocus(Node node) {
        Objects.requireNonNull(node, "node");
        if (node.isFocused()) {
            return true;
        }

        @Nullable Scene scene = node.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(node, focusOwner);
    }

    /// Returns whether this host can run structural accessibility actions before scene attachment.
    private boolean isDetachedReachableHost() {
        return getScene() == null && M3Accessible.isEffectivelyReachable(this);
    }

    /// Shows or focuses the snackbar referenced by accessibility action parameters.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when the target snackbar was focused, shown, or accepted as the current target
    boolean showAccessibleSnackbar(Object... parameters) {
        if (modalBlocked || (!M3Accessible.canReach(this) && !isDetachedReachableHost())) {
            return false;
        }
        @Nullable M3Snackbar target = accessibleSnackbar(parameters);
        if (target == null) {
            return false;
        }

        if (target == getSnackbar()) {
            refreshAccessibleFocusNode();
            boolean shown = showAccessibleSnackbarTarget(target, parameters);
            if (shown) {
                refreshAccessibleFocusNode();
            }
            return shown;
        }
        if (queueContainsIdentity(target)) {
            validateSnackbarClaim(target);
            if (!removeFirstQueuedSnackbar(target)) {
                return false;
            }
            show(target);
            boolean shown = showAccessibleSnackbarTarget(target, parameters);
            if (shown) {
                refreshAccessibleFocusNode();
            }
            return shown;
        }
        return false;
    }

    /// Focuses the hosted snackbar or delegates to one of its nested action-owned targets.
    private boolean showAccessibleSnackbarTarget(M3Snackbar target, Object... parameters) {
        Objects.requireNonNull(target, "target");
        Object[] targetParameters = snackbarTargetParameters(parameters);
        if (targetParameters.length > 0 && !parametersReferenceSnackbar(target, targetParameters)) {
            if (M3Accessible.containsUnrevealableActionNodeTarget(target, targetParameters)) {
                return false;
            }
            target.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetParameters);
            if (currentFocusNode() != null) {
                return true;
            }
        }
        return M3Accessible.showItem(this, currentFocusNode());
    }

    /// Returns parameters that should be applied after an indexed snackbar has already been resolved.
    private static Object[] snackbarTargetParameters(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length <= 1 || !(parameters[0] instanceof Number)) {
            return parameters;
        }

        Object[] targetParameters = new Object[parameters.length - 1];
        System.arraycopy(parameters, 1, targetParameters, 0, targetParameters.length);
        return targetParameters;
    }

    /// Returns whether any supplied parameter directly references the snackbar item.
    private static boolean parametersReferenceSnackbar(M3Snackbar target, Object... parameters) {
        for (Object parameter : parameters) {
            if (parameterReferencesSnackbar(target, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one accessibility parameter directly references the snackbar item.
    private static boolean parameterReferencesSnackbar(M3Snackbar target, @Nullable Object parameter) {
        if (parameter == target) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (parameterReferencesSnackbar(target, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (parameterReferencesSnackbar(target, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns the snackbar referenced by accessibility action parameters.
    private @Nullable M3Snackbar accessibleSnackbar(Object... parameters) {
        if (parameters.length == 0) {
            @Nullable M3Snackbar currentSnackbar = getSnackbar();
            if (canRevealSnackbar(currentSnackbar)) {
                return currentSnackbar;
            }
            return queue.isEmpty() ? null : revealableSnackbar(queue.get(0));
        }

        int index = M3Accessible.indexParameter(parameters);
        if (index >= 0) {
            return indexedAccessibleSnackbar(parameters);
        }

        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar != null
                && canRevealSnackbarTarget(currentSnackbar, parameters)
                && (M3Accessible.containsNodeTarget(currentSnackbar, parameters)
                || M3Accessible.containsAccessibleActionTarget(currentSnackbar, parameters))) {
            return currentSnackbar;
        }
        for (M3Snackbar queuedSnackbar : queue) {
            if (canRevealSnackbarTarget(queuedSnackbar, parameters)
                    && (M3Accessible.containsNodeTarget(queuedSnackbar, parameters)
                    || M3Accessible.containsAccessibleActionTarget(queuedSnackbar, parameters))) {
                return queuedSnackbar;
            }
        }
        return null;
    }

    /// Returns the indexed snackbar when the snackbar and any nested target can be revealed.
    private @Nullable M3Snackbar indexedAccessibleSnackbar(Object... parameters) {
        @Nullable M3Snackbar snackbar = revealableSnackbar(snackbarAt(parameters));
        if (snackbar == null || parameters.length <= 1) {
            return snackbar;
        }

        Object[] targetParameters = snackbarTargetParameters(parameters);
        return canRevealSnackbarTarget(snackbar, targetParameters) ? snackbar : null;
    }

    /// Returns the supplied snackbar when it can be revealed through this host.
    private static @Nullable M3Snackbar revealableSnackbar(@Nullable Node item) {
        return item instanceof M3Snackbar snackbar && canRevealSnackbar(snackbar) ? snackbar : null;
    }

    /// Returns whether one snackbar and any explicitly requested nested target can participate in reveal.
    private static boolean canRevealSnackbarTarget(M3Snackbar snackbar, Object... parameters) {
        Objects.requireNonNull(snackbar, "snackbar");
        return canRevealSnackbar(snackbar)
                && !M3Accessible.containsUnrevealableActionNodeTarget(snackbar, parameters);
    }

    /// Returns whether one snackbar can participate in explicit accessibility reveal.
    private static boolean canRevealSnackbar(@Nullable M3Snackbar snackbar) {
        return M3Accessible.isEffectivelyReachable(snackbar);
    }

    /// Returns text for the currently hosted snackbar.
    private String currentSnackbarText() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar == null) {
            return "";
        }
        @Nullable String text = currentSnackbar.getAccessibleText();
        return text == null ? "" : text;
    }

}
