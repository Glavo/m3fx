// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Snackbar;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// Internal renderer and lifecycle owner for snackbar messages in one overlay pane.
///
/// The presenter owns exactly one reusable snackbar node tree. Public [M3Snackbar] instances are observable message
/// models and never enter the JavaFX scene graph. Queueing, automatic timeout, motion, focus routing, and
/// accessibility therefore remain stable while the current model or its properties change.
@NotNullByDefault
public final class M3SnackbarPresenter extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-snackbar-presenter";

    /// The initial vertical offset used by entrance and exit motion.
    private static final double TRANSITION_OFFSET_Y = 16.0;

    /// Horizontal movement required before a pointer gesture claims the snackbar, in logical pixels.
    private static final double SWIPE_ACTIVATION_DISTANCE = 8.0;

    /// Fraction of container width that commits a swipe dismissal.
    private static final double SWIPE_DISMISS_THRESHOLD = 0.5;

    /// Fraction of container width where drag opacity starts decreasing.
    private static final double SWIPE_FADE_START = 0.1;

    /// Fraction of container width where drag opacity reaches zero.
    private static final double SWIPE_FADE_END = 0.6;

    /// The default snackbar container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 4.0;

    /// The default snackbar content padding.
    private static final double DEFAULT_CONTENT_PADDING = 16.0;

    /// The default minimum snackbar container width.
    private static final double DEFAULT_CONTAINER_MIN_WIDTH = 344.0;

    /// The default maximum snackbar container width.
    private static final double DEFAULT_CONTAINER_MAX_WIDTH = 672.0;

    /// The default single-line snackbar container height.
    private static final double DEFAULT_SINGLE_LINE_CONTAINER_HEIGHT = 48.0;

    /// The default two-line snackbar container height.
    private static final double DEFAULT_TWO_LINE_CONTAINER_HEIGHT = 68.0;

    /// The default snackbar action button container height.
    private static final double DEFAULT_ACTION_CONTAINER_HEIGHT = 32.0;

    /// Pending messages in FIFO display order.
    private final ObservableList<M3Snackbar> queue = M3ObservableLists.nonNullElementList("snackbar");

    /// Unmodifiable live view of the pending queue.
    private final @UnmodifiableView ObservableList<M3Snackbar> queueView =
            FXCollections.unmodifiableObservableList(queue);

    /// Optional explicit automatic-dismissal duration owned by the overlay pane.
    private final ObjectProperty<@Nullable Duration> displayDuration;

    /// Whether the owning overlay permits horizontal swipe dismissal.
    private final BooleanProperty swipeToDismissEnabled;

    /// Automatic-dismissal timer reused for every passive message.
    private final PauseTransition displayTimer = new PauseTransition();

    /// Reusable entrance transition for the stable presentation node.
    private final SnackbarTransition showAnimation = new SnackbarTransition(true);

    /// Reusable exit transition for the stable presentation node.
    private final SnackbarTransition hideAnimation = new SnackbarTransition(false);

    /// Message associated with the current automatic-dismissal callback.
    private @Nullable M3Snackbar displayTimerTarget;

    /// Message whose observable content is currently connected to this presenter.
    private @Nullable M3Snackbar observedSnackbar;

    /// Applies supporting-text changes from the current observable message.
    private final InvalidationListener currentTextInvalidation =
            observable -> handleCurrentTextChanged();

    /// Weak wrapper installed on the current message's supporting-text property.
    private final WeakInvalidationListener weakCurrentTextInvalidation =
            new WeakInvalidationListener(currentTextInvalidation);

    /// Applies action-label and close-affordance changes from the current observable message.
    private final InvalidationListener currentAffordanceInvalidation =
            observable -> handleCurrentAffordancesChanged();

    /// Weak wrapper installed on the current message's affordance properties.
    private final WeakInvalidationListener weakCurrentAffordanceInvalidation =
            new WeakInvalidationListener(currentAffordanceInvalidation);

    /// Whether focus should remain on an action when the queue advances after exit.
    private boolean transferFocusAfterHide;

    /// Whether a modal overlay currently blocks snackbar interaction and timeout progress.
    private boolean modalBlocked;

    /// Whether a primary pointer press is eligible to become a snackbar swipe.
    private boolean swipePointerDown;

    /// Whether the current pointer sequence has committed to horizontal snackbar movement.
    private boolean swipeActive;

    /// Whether vertical intent rejected the current pointer sequence.
    private boolean swipeRejected;

    /// Scene-space horizontal coordinate where the current pointer sequence began.
    private double swipeStartSceneX;

    /// Scene-space vertical coordinate where the current pointer sequence began.
    private double swipeStartSceneY;

    /// Observes effective motion settings while snackbar work is active.
    private final M3MotionSettingsObserver motionSettingsObserver;

    /// Reports changes to the rendered snackbar focus target.
    private final M3AccessibleFocusNotifier focusNotifier;

    /// Creates an empty presenter backed by state properties owned by its overlay pane.
    ///
    /// @param snackbar              current-message state to update
    /// @param showing               visible-display state to update
    /// @param displayDuration       optional explicit automatic-dismissal duration to observe
    /// @param swipeToDismissEnabled whether the owning overlay permits horizontal swipe dismissal
    /// @throws NullPointerException if any property is `null`
    public M3SnackbarPresenter(
            ReadOnlyObjectWrapper<@Nullable M3Snackbar> snackbar,
            ReadOnlyBooleanWrapper showing,
            ObjectProperty<@Nullable Duration> displayDuration,
            BooleanProperty swipeToDismissEnabled
    ) {
        this.snackbar = Objects.requireNonNull(snackbar, "snackbar");
        this.showing = Objects.requireNonNull(showing, "showing");
        this.displayDuration = Objects.requireNonNull(displayDuration, "displayDuration");
        this.swipeToDismissEnabled = Objects.requireNonNull(swipeToDismissEnabled, "swipeToDismissEnabled");
        motionSettingsObserver = new M3MotionSettingsObserver(this, this::refreshMotionSettings, false);
        focusNotifier = new M3AccessibleFocusNotifier(this, this::currentFocusNode);

        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        setPickOnBounds(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusCurrentAccessibleNode, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);

        showing.addListener((observable, oldValue, newValue) -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
        });
        displayTimer.setOnFinished(event -> handleDisplayTimerFinished());
        displayDuration.addListener(observable -> refreshDisplayTimer());
        swipeToDismissEnabled.addListener(observable -> handleSwipeAvailabilityChanged());

        // The stable node tree must exist before the first message is animated, including while detached.
        setSkin(createDefaultSkin());
        this.snackbar.addListener((observable, oldSnackbar, newSnackbar) ->
                observeSnackbar(oldSnackbar, newSnackbar));
        focusNotifier.start();
    }

    /// Backing property for the current message exposed by the overlay pane.
    ///
    /// The presenter updates the wrapper but does not own it; the overlay pane supplies and observes the same
    /// wrapper for the presenter's lifetime.
    private final ReadOnlyObjectWrapper<@Nullable M3Snackbar> snackbar;

    /// Returns the currently presented message.
    ///
    /// @return the current message, or `null` while idle
    public @Nullable M3Snackbar getSnackbar() {
        return snackbar.get();
    }

    /// Returns the read-only current-message property.
    ///
    /// @return the current-message property
    public ReadOnlyObjectProperty<@Nullable M3Snackbar> snackbarProperty() {
        return snackbar.getReadOnlyProperty();
    }

    /// Backing property for the visible display phase exposed by the overlay pane.
    ///
    /// The presenter updates the wrapper but does not own it; the overlay pane supplies and observes the same
    /// wrapper for the presenter's lifetime.
    private final ReadOnlyBooleanWrapper showing;

    /// Returns whether the current message is in its visible display phase.
    ///
    /// @return `true` during entrance and the stable visible phase
    public boolean isShowing() {
        return showing.get();
    }

    /// Returns pending messages in FIFO order.
    ///
    /// The returned list is an unmodifiable, live, observable view and excludes the current message.
    ///
    /// @return the pending-message queue
    public @UnmodifiableView ObservableList<M3Snackbar> getQueue() {
        return queueView;
    }

    /// Updates whether a modal overlay blocks snackbar interaction.
    ///
    /// The current message remains rendered beneath the modal layer. Its timeout is paused and restarted when the
    /// modal layer clears.
    ///
    /// @param modalBlocked whether a modal overlay blocks the presenter
    public void setModalBlocked(boolean modalBlocked) {
        if (this.modalBlocked == modalBlocked) {
            return;
        }
        if (modalBlocked) {
            cancelSwipeGesture(false);
        }
        this.modalBlocked = modalBlocked;
        refreshDisplayTimer();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        focusNotifier.refresh();
    }

    /// Appends a message to the FIFO queue or shows it immediately while idle.
    ///
    /// The same observable message instance may occur more than once in the queue. Property changes made while a
    /// message is pending are reflected if and when that instance becomes current.
    ///
    /// @param snackbar the message to enqueue
    /// @throws NullPointerException if `snackbar` is `null`
    public void enqueue(M3Snackbar snackbar) {
        M3Snackbar checkedSnackbar = Objects.requireNonNull(snackbar, "snackbar");
        if (getSnackbar() == null && !showing.get()) {
            show(checkedSnackbar);
        } else {
            queue.add(checkedSnackbar);
        }
    }

    /// Removes all pending messages without changing the current message.
    public void clearQueue() {
        queue.clear();
    }

    /// Immediately presents a message, replacing any current message without changing the queue.
    ///
    /// Calling this method with the current instance restarts its visible phase. Use [#enqueue(M3Snackbar)] to
    /// preserve an existing message before the supplied one.
    ///
    /// @param snackbar the message to present
    /// @throws NullPointerException if `snackbar` is `null`
    public void show(M3Snackbar snackbar) {
        M3Snackbar checkedSnackbar = Objects.requireNonNull(snackbar, "snackbar");
        boolean transferFocus = nodeOwnsFocus(currentFocusNode());
        motionSettingsObserver.start();
        stopDisplayTimer();
        stopTransition(showAnimation);
        stopTransition(hideAnimation);
        clearSwipeTracking();
        transferFocusAfterHide = false;

        Node node = presentationNode();
        boolean wasIdle = getSnackbar() == null;
        this.snackbar.set(checkedSnackbar);
        if (wasIdle) {
            node.setOpacity(0.0);
            node.setTranslateX(0.0);
            node.setTranslateY(TRANSITION_OFFSET_Y);
        }
        showing.set(true);
        notifyMessageChanged();
        playShowAnimation(checkedSnackbar, node);
        if (transferFocus) {
            focusCurrentAccessibleNode();
        }
    }

    /// Dismisses the current message and advances to the first queued message after exit motion.
    ///
    /// The method is a no-op while idle or while the current message is already leaving.
    public void dismiss() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar == null || !showing.get()) {
            return;
        }
        stopDisplayTimer();
        stopTransition(showAnimation);
        clearSwipeTracking();
        transferFocusAfterHide = nodeOwnsFocus(currentFocusNode());
        showing.set(false);
        playHideAnimation(currentSnackbar, presentationNode());
    }

    /// Clears the pending queue and dismisses the current message.
    public void dismissAll() {
        clearQueue();
        dismiss();
    }

    /// Executes the current message action and then dismisses that same message.
    ///
    /// If the action installs another current message, the newly installed message is not dismissed. Dismissal is
    /// attempted in a `finally` block so an application exception cannot leave the acted-on message stuck onscreen.
    public void fireCurrentAction() {
        @Nullable M3Snackbar target = getSnackbar();
        if (target == null || !target.hasAction() || !showing.get() || modalBlocked || isDisabled()) {
            return;
        }
        @Nullable Runnable action = target.getAction();
        try {
            if (action != null) {
                action.run();
            }
        } finally {
            if (getSnackbar() == target) {
                dismiss();
            }
        }
    }

    /// The snackbar container corner radius, in logical pixels.
    ///
    /// The default value is `4.0`. Values must be finite and non-negative. This property is exposed to CSS as
    /// `-m3-container-shape`.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the snackbar container shape radius.
    ///
    /// @return the shape radius in logical pixels
    public double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Returns the styleable snackbar container shape property.
    ///
    /// @return the shape property
    public StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = styleableProperty(
                    DEFAULT_CONTAINER_SHAPE, "containerShape", StyleableProperties.CONTAINER_SHAPE
            );
        }
        return containerShape;
    }

    /// The padding between the snackbar container and its content, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative. This property is exposed to CSS as
    /// `-m3-content-padding`.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty contentPadding;

    /// Returns the snackbar content padding.
    ///
    /// @return the padding in logical pixels
    public double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Returns the styleable snackbar content padding property.
    ///
    /// @return the content padding property
    public StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = styleableProperty(
                    DEFAULT_CONTENT_PADDING, "contentPadding", StyleableProperties.CONTENT_PADDING
            );
        }
        return contentPadding;
    }

    /// The minimum snackbar container width, in logical pixels.
    ///
    /// The default value is `344.0`. Values must be finite and non-negative. This property is exposed to CSS as
    /// `-m3-container-min-width`.
    ///
    /// @defaultValue `344.0`
    private @Nullable StyleableDoubleProperty containerMinWidth;

    /// Returns the minimum snackbar container width.
    ///
    /// @return the minimum width in logical pixels
    public double getContainerMinWidth() {
        return containerMinWidth == null ? DEFAULT_CONTAINER_MIN_WIDTH : containerMinWidth.get();
    }

    /// Returns the styleable minimum snackbar width property.
    ///
    /// @return the minimum width property
    public StyleableDoubleProperty containerMinWidthProperty() {
        if (containerMinWidth == null) {
            containerMinWidth = styleableProperty(
                    DEFAULT_CONTAINER_MIN_WIDTH, "containerMinWidth", StyleableProperties.CONTAINER_MIN_WIDTH
            );
        }
        return containerMinWidth;
    }

    /// The maximum snackbar container width, in logical pixels.
    ///
    /// The default value is `672.0`. Values must be finite and non-negative. This property is exposed to CSS as
    /// `-m3-container-max-width`.
    ///
    /// @defaultValue `672.0`
    private @Nullable StyleableDoubleProperty containerMaxWidth;

    /// Returns the maximum snackbar container width.
    ///
    /// @return the maximum width in logical pixels
    public double getContainerMaxWidth() {
        return containerMaxWidth == null ? DEFAULT_CONTAINER_MAX_WIDTH : containerMaxWidth.get();
    }

    /// Returns the styleable maximum snackbar width property.
    ///
    /// @return the maximum width property
    public StyleableDoubleProperty containerMaxWidthProperty() {
        if (containerMaxWidth == null) {
            containerMaxWidth = styleableProperty(
                    DEFAULT_CONTAINER_MAX_WIDTH, "containerMaxWidth", StyleableProperties.CONTAINER_MAX_WIDTH
            );
        }
        return containerMaxWidth;
    }

    /// The snackbar container height used for a single line of supporting text, in logical pixels.
    ///
    /// The default value is `48.0`. Values must be finite and non-negative. This property is exposed to CSS as
    /// `-m3-single-line-container-height`.
    ///
    /// @defaultValue `48.0`
    private @Nullable StyleableDoubleProperty singleLineContainerHeight;

    /// Returns the single-line snackbar container height.
    ///
    /// @return the single-line height in logical pixels
    public double getSingleLineContainerHeight() {
        return singleLineContainerHeight == null
                ? DEFAULT_SINGLE_LINE_CONTAINER_HEIGHT
                : singleLineContainerHeight.get();
    }

    /// Returns the styleable single-line container height property.
    ///
    /// @return the single-line height property
    public StyleableDoubleProperty singleLineContainerHeightProperty() {
        if (singleLineContainerHeight == null) {
            singleLineContainerHeight = styleableProperty(
                    DEFAULT_SINGLE_LINE_CONTAINER_HEIGHT,
                    "singleLineContainerHeight",
                    StyleableProperties.SINGLE_LINE_CONTAINER_HEIGHT
            );
        }
        return singleLineContainerHeight;
    }

    /// The snackbar container height used for two lines of supporting text, in logical pixels.
    ///
    /// The default value is `68.0`. Values must be finite and non-negative. This property is exposed to CSS as
    /// `-m3-two-line-container-height`.
    ///
    /// @defaultValue `68.0`
    private @Nullable StyleableDoubleProperty twoLineContainerHeight;

    /// Returns the two-line snackbar container height.
    ///
    /// @return the two-line height in logical pixels
    public double getTwoLineContainerHeight() {
        return twoLineContainerHeight == null ? DEFAULT_TWO_LINE_CONTAINER_HEIGHT : twoLineContainerHeight.get();
    }

    /// Returns the styleable two-line container height property.
    ///
    /// @return the two-line height property
    public StyleableDoubleProperty twoLineContainerHeightProperty() {
        if (twoLineContainerHeight == null) {
            twoLineContainerHeight = styleableProperty(
                    DEFAULT_TWO_LINE_CONTAINER_HEIGHT,
                    "twoLineContainerHeight",
                    StyleableProperties.TWO_LINE_CONTAINER_HEIGHT
            );
        }
        return twoLineContainerHeight;
    }

    /// The snackbar action button container height, in logical pixels.
    ///
    /// The default value is `32.0`. Values must be finite and non-negative. This property is exposed to CSS as
    /// `-m3-action-container-height`.
    ///
    /// @defaultValue `32.0`
    private @Nullable StyleableDoubleProperty actionContainerHeight;

    /// Returns the snackbar action button container height.
    ///
    /// @return the action height in logical pixels
    public double getActionContainerHeight() {
        return actionContainerHeight == null ? DEFAULT_ACTION_CONTAINER_HEIGHT : actionContainerHeight.get();
    }

    /// Returns the styleable action button container height property.
    ///
    /// @return the action height property
    public StyleableDoubleProperty actionContainerHeightProperty() {
        if (actionContainerHeight == null) {
            actionContainerHeight = styleableProperty(
                    DEFAULT_ACTION_CONTAINER_HEIGHT,
                    "actionContainerHeight",
                    StyleableProperties.ACTION_CONTAINER_HEIGHT
            );
        }
        return actionContainerHeight;
    }

    /// Creates one non-negative styleable geometry property.
    ///
    /// @param defaultValue default token value
    /// @param name         JavaFX property name
    /// @param metadata     CSS metadata
    /// @return the new property
    private StyleableDoubleProperty styleableProperty(
            double defaultValue,
            String name,
            CssMetaData<M3SnackbarPresenter, Number> metadata
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(
                defaultValue, this, name, metadata, this::requestLayout
        );
    }

    /// Returns CSS metadata for this presenter class.
    ///
    /// @return the immutable CSS metadata list
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns CSS metadata for this presenter instance.
    ///
    /// @return the immutable CSS metadata list
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the snackbar user-agent stylesheet.
    ///
    /// @return the snackbar stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("snackbar.css");
    }

    /// Creates the reusable snackbar presenter skin.
    ///
    /// @return the default presenter skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SnackbarPresenterSkin(this);
    }

    /// Returns accessibility attributes for the currently rendered message.
    ///
    /// Pending messages are data, not hidden accessibility nodes. They become accessible only when presented.
    ///
    /// @param attribute  requested accessibility attribute
    /// @param parameters optional attribute parameters
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
            case CONTENTS -> !isShowing() || getSnackbar() == null ? null : presentationNode();
            case EXPANDED -> isShowing();
            case FOCUS_NODE -> currentFocusNode();
            case ITEM_COUNT -> isShowing() ? presenterSkin().getInteractiveItemCount() : 0;
            case ITEM_AT_INDEX -> isShowing() ? interactiveItemAt(parameters) : null;
            case TEXT -> isShowing() ? currentSnackbarText() : "";
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by the rendered snackbar.
    ///
    /// @param action     requested accessibility action
    /// @param parameters optional action parameters
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
            case REQUEST_FOCUS -> focusCurrentAccessibleNode();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Handles Escape while focus is inside the rendered snackbar.
    ///
    /// @param event the key event to handle
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && isShowing()) {
            dismiss();
            event.consume();
        }
    }

    /// Plays entrance motion on the stable presentation node.
    private void playShowAnimation(M3Snackbar message, Node node) {
        showAnimation.configure(M3Animation.fastSpatial(this), message, node, 1.0, 0.0, 0.0);
        playConfiguredTransition(showAnimation);
    }

    /// Plays exit motion on the stable presentation node.
    private void playHideAnimation(M3Snackbar message, Node node) {
        hideAnimation.configure(
                M3Animation.fastSpatial(this),
                message,
                node,
                0.0,
                node.getTranslateX(),
                TRANSITION_OFFSET_Y
        );
        playConfiguredTransition(hideAnimation);
    }

    /// Plays horizontal dismissal motion after a committed swipe.
    private void playSwipeHideAnimation(M3Snackbar message, Node node, double targetTranslateX) {
        hideAnimation.configure(
                M3Animation.fastSpatial(this),
                message,
                node,
                0.0,
                targetTranslateX,
                node.getTranslateY()
        );
        playConfiguredTransition(hideAnimation);
    }

    /// Starts a configured transition or synchronously settles a completed entrance.
    private void playConfiguredTransition(SnackbarTransition transition) {
        if (transition.isEntrance() && !transition.hasVisualChange()) {
            M3Animation.finish(transition);
        } else {
            M3Animation.playFromStart(this, transition);
        }
    }

    /// Stops a reusable transition and releases its retained targets.
    private static void stopTransition(SnackbarTransition transition) {
        transition.stop();
        transition.clearTargets();
    }

    /// Applies changed runtime motion settings and window lifecycle.
    private void refreshMotionSettings() {
        if (isWindowUnavailable()) {
            M3Animation.finishIfRunning(showAnimation);
            M3Animation.finishIfRunning(hideAnimation);
        }
        refreshDisplayTimer();
    }

    /// Returns whether the presenter lacks a showing window.
    private boolean isWindowUnavailable() {
        @Nullable Scene scene = getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        return window == null || !window.isShowing();
    }

    /// Resolves the effective automatic-dismissal duration.
    private Duration effectiveDisplayDuration() {
        @Nullable Duration duration = displayDuration.get();
        return duration == null ? M3Animation.motionBehavior(this).snackbarDisplayDuration() : duration;
    }

    /// Reschedules or cancels automatic dismissal for the current message.
    private void refreshDisplayTimer() {
        @Nullable M3Snackbar target = getSnackbar();
        Duration duration = effectiveDisplayDuration();
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

    /// Stops automatic dismissal and releases the retained message.
    private void stopDisplayTimer() {
        displayTimer.stop();
        displayTimerTarget = null;
    }

    /// Dismisses the message retained by the stable timer callback.
    private void handleDisplayTimerFinished() {
        @Nullable M3Snackbar target = displayTimerTarget;
        displayTimerTarget = null;
        if (target != null && getSnackbar() == target && showing.get()) {
            if (!target.hasAction() && !target.isCloseButtonVisible()) {
                dismiss();
            }
        }
    }

    /// Handles completion of one reusable transition.
    private void handleTransitionFinished(SnackbarTransition transition) {
        @Nullable M3Snackbar target = transition.takeMessage();
        if (target == null) {
            return;
        }
        if (transition.isEntrance()) {
            if (getSnackbar() == target && showing.get()) {
                refreshDisplayTimer();
            }
        } else {
            removeSnackbar(target);
        }
    }

    /// Removes one exited message and advances the FIFO queue.
    private void removeSnackbar(M3Snackbar target) {
        if (getSnackbar() != target) {
            return;
        }
        boolean transferFocus = transferFocusAfterHide;
        transferFocusAfterHide = false;
        Node node = presentationNode();
        this.snackbar.set(null);
        setAccessibleText(null);
        node.setOpacity(1.0);
        node.setTranslateX(0.0);
        node.setTranslateY(0.0);
        notifyMessageChanged();
        showNextQueuedSnackbar(transferFocus);
    }

    /// Presents the first queued message when one exists.
    private void showNextQueuedSnackbar(boolean transferFocus) {
        if (getSnackbar() != null) {
            return;
        }
        if (queue.isEmpty()) {
            motionSettingsObserver.stop();
            return;
        }
        M3Snackbar nextSnackbar = queue.remove(0);
        show(nextSnackbar);
        if (transferFocus) {
            focusCurrentAccessibleNode();
        }
    }

    /// Notifies accessibility and layout clients after current message replacement.
    private void notifyMessageChanged() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_AT_INDEX);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        focusNotifier.refresh();
    }

    /// Moves observable-content listeners from the previous current message to the replacement.
    private void observeSnackbar(
            @Nullable M3Snackbar oldSnackbar,
            @Nullable M3Snackbar newSnackbar
    ) {
        if (oldSnackbar != null) {
            oldSnackbar.textProperty().removeListener(weakCurrentTextInvalidation);
            oldSnackbar.actionTextProperty().removeListener(weakCurrentAffordanceInvalidation);
            oldSnackbar.closeButtonVisibleProperty().removeListener(weakCurrentAffordanceInvalidation);
        }
        observedSnackbar = newSnackbar;
        if (newSnackbar != null) {
            newSnackbar.textProperty().addListener(weakCurrentTextInvalidation);
            newSnackbar.actionTextProperty().addListener(weakCurrentAffordanceInvalidation);
            newSnackbar.closeButtonVisibleProperty().addListener(weakCurrentAffordanceInvalidation);
            setAccessibleText(newSnackbar.getText());
        } else {
            setAccessibleText(null);
        }
    }

    /// Refreshes layout and accessibility after the current supporting text changes.
    private void handleCurrentTextChanged() {
        @Nullable M3Snackbar current = observedSnackbar;
        if (current == null || current != getSnackbar()) {
            return;
        }
        setAccessibleText(current.getText());
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Refreshes timeout, layout, focus, and accessibility after visible affordances change.
    private void handleCurrentAffordancesChanged() {
        @Nullable M3Snackbar current = observedSnackbar;
        if (current == null || current != getSnackbar()) {
            return;
        }
        presenterSkin().updateAffordanceVisibility();
        refreshDisplayTimer();
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_AT_INDEX);
        transferFocusFromHiddenAffordance();
        notifyFocusNodeChanged();
    }

    /// Moves focus from a newly hidden snackbar action to the remaining reachable affordance.
    private void transferFocusFromHiddenAffordance() {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        Node presentation = presentationNode();
        if (focusOwner != null
                && M3Accessible.containsNode(presentation, focusOwner)
                && !M3Accessible.canReach(focusOwner)) {
            focusCurrentAccessibleNode();
        }
    }

    /// Returns the stable presentation node from the eagerly installed skin.
    private Node presentationNode() {
        return presenterSkin().getPresentationNode();
    }

    /// Begins tracking a possible horizontal swipe from the snackbar surface.
    void handleSwipeMousePressed(MouseEvent event) {
        clearSwipeTracking();
        if (event.getButton() != MouseButton.PRIMARY
                || !event.isPrimaryButtonDown()
                || !canStartSwipe(event)) {
            return;
        }
        swipePointerDown = true;
        swipeStartSceneX = event.getSceneX();
        swipeStartSceneY = event.getSceneY();
    }

    /// Updates a claimed horizontal swipe without changing layout geometry.
    void handleSwipeMouseDragged(MouseEvent event) {
        if (!swipePointerDown || swipeRejected || !event.isPrimaryButtonDown()) {
            return;
        }

        double deltaX = event.getSceneX() - swipeStartSceneX;
        double deltaY = event.getSceneY() - swipeStartSceneY;
        double absoluteX = Math.abs(deltaX);
        double absoluteY = Math.abs(deltaY);
        if (!swipeActive) {
            if (absoluteY >= SWIPE_ACTIVATION_DISTANCE && absoluteY >= absoluteX) {
                swipeRejected = true;
                return;
            }
            if (absoluteX < SWIPE_ACTIVATION_DISTANCE || absoluteX <= absoluteY) {
                return;
            }
            swipeActive = true;
            stopDisplayTimer();
            stopTransition(showAnimation);
        }

        Node node = presentationNode();
        node.setTranslateX(snapPositionX(deltaX));
        node.setOpacity(swipeOpacity(absoluteX, node.getLayoutBounds().getWidth()));
        event.consume();
    }

    /// Commits or cancels the current horizontal swipe when the pointer is released.
    void handleSwipeMouseReleased(MouseEvent event) {
        if (!swipePointerDown) {
            return;
        }

        boolean wasActive = swipeActive;
        clearSwipeTracking();
        if (!wasActive) {
            return;
        }

        event.consume();
        @Nullable M3Snackbar current = getSnackbar();
        Node node = presentationNode();
        double width = Math.max(1.0, node.getLayoutBounds().getWidth());
        if (current != null
                && showing.get()
                && Math.abs(node.getTranslateX()) >= width * SWIPE_DISMISS_THRESHOLD) {
            dismissBySwipe(current, node, Math.copySign(1.0, node.getTranslateX()));
        } else if (current != null && showing.get()) {
            playShowAnimation(current, node);
        } else {
            resetSwipeVisuals(node);
        }
    }

    /// Returns whether the current pointer press can initiate a snackbar swipe.
    private boolean canStartSwipe(MouseEvent event) {
        return swipeToDismissEnabled.get()
                && showing.get()
                && getSnackbar() != null
                && !modalBlocked
                && !isDisabled()
                && hideAnimation.getStatus() != Animation.Status.RUNNING
                && showAnimation.getStatus() != Animation.Status.RUNNING
                && !presenterSkin().isSwipeExcludedTarget(event.getTarget());
    }

    /// Dismisses the current message horizontally and preserves normal queue advancement.
    private void dismissBySwipe(M3Snackbar current, Node node, double direction) {
        stopDisplayTimer();
        stopTransition(showAnimation);
        stopTransition(hideAnimation);
        transferFocusAfterHide = nodeOwnsFocus(currentFocusNode());
        showing.set(false);
        playSwipeHideAnimation(current, node, swipeExitTranslateX(node, direction));
    }

    /// Returns the horizontal translation that places the snackbar fully outside the presenter.
    private double swipeExitTranslateX(Node node, double direction) {
        double layoutMinX = node.getLayoutX() + node.getLayoutBounds().getMinX();
        if (direction < 0.0) {
            return -(layoutMinX + node.getLayoutBounds().getWidth());
        }
        return getWidth() - layoutMinX;
    }

    /// Returns drag opacity using Android's snackbar fade interval.
    private static double swipeOpacity(double absoluteOffset, double width) {
        double fraction = absoluteOffset / Math.max(1.0, width);
        double fadeFraction = (fraction - SWIPE_FADE_START) / (SWIPE_FADE_END - SWIPE_FADE_START);
        return 1.0 - Math.max(0.0, Math.min(1.0, fadeFraction));
    }

    /// Cancels an active swipe after host interaction becomes unavailable.
    private void handleSwipeAvailabilityChanged() {
        if (!swipeToDismissEnabled.get()) {
            cancelSwipeGesture(true);
        }
    }

    /// Cancels pointer tracking and optionally animates the surface back to rest.
    private void cancelSwipeGesture(boolean animate) {
        boolean wasActive = swipeActive;
        clearSwipeTracking();
        if (!wasActive) {
            return;
        }
        Node node = presentationNode();
        @Nullable M3Snackbar current = getSnackbar();
        if (animate && current != null && showing.get()) {
            playShowAnimation(current, node);
        } else {
            resetSwipeVisuals(node);
        }
    }

    /// Clears transient pointer-sequence state without changing node visuals.
    private void clearSwipeTracking() {
        swipePointerDown = false;
        swipeActive = false;
        swipeRejected = false;
        swipeStartSceneX = 0.0;
        swipeStartSceneY = 0.0;
    }

    /// Restores visual channels owned by horizontal swipe interaction.
    private static void resetSwipeVisuals(Node node) {
        node.setOpacity(1.0);
        node.setTranslateX(0.0);
    }

    /// Returns the installed reusable presenter skin.
    private M3SnackbarPresenterSkin presenterSkin() {
        Skin<?> skin = getSkin();
        if (skin instanceof M3SnackbarPresenterSkin presenterSkin) {
            return presenterSkin;
        }
        M3SnackbarPresenterSkin replacement = new M3SnackbarPresenterSkin(this);
        setSkin(replacement);
        return replacement;
    }

    /// Returns the currently rendered accessibility focus target.
    private @Nullable Node currentFocusNode() {
        if (!showing.get() || modalBlocked) {
            return null;
        }
        @Nullable Node node = presenterSkin().getAccessibleFocusNode();
        if (node == null) {
            return null;
        }
        @Nullable Node externalTarget = M3Accessible.activeExternalFocusTarget(this, node);
        return externalTarget == null ? node : externalTarget;
    }

    /// Focuses the current action or close affordance.
    private boolean focusCurrentAccessibleNode() {
        if (M3Accessible.showItem(this, currentFocusNode())) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Focuses an interactive affordance referenced by accessibility parameters.
    private boolean showAccessibleItem(Object... parameters) {
        @Nullable Node item = parameters.length == 0 ? currentFocusNode() : interactiveItemAt(parameters);
        if (item != null && M3Accessible.showItem(this, item)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns a rendered interactive item for an accessibility index.
    private @Nullable Node interactiveItemAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        return presenterSkin().getInteractiveItem(index);
    }

    /// Notifies accessibility clients that the preferred focus node changed.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns whether the supplied node or one of its descendants owns scene focus.
    private static boolean nodeOwnsFocus(@Nullable Node node) {
        if (node == null) {
            return false;
        }
        if (node.isFocused()) {
            return true;
        }
        @Nullable Scene scene = node.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(node, focusOwner);
    }

    /// Returns the current text exposed through accessibility.
    private String currentSnackbarText() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        return currentSnackbar == null ? "" : currentSnackbar.getText();
    }

    /// Reuses one primitive transition for entrance or exit without allocating key frames.
    @NotNullByDefault
    private final class SnackbarTransition extends M3FiniteTransition {
        /// Whether this transition represents entrance rather than exit.
        private final boolean entrance;

        /// Message associated with the current run.
        private @Nullable M3Snackbar message;

        /// Stable node receiving interpolated values.
        private @Nullable Node targetNode;

        /// Starting opacity.
        private double startOpacity;

        /// Target opacity.
        private double targetOpacity;

        /// Starting horizontal translation.
        private double startTranslateX;

        /// Target horizontal translation.
        private double targetTranslateX;

        /// Starting vertical translation.
        private double startTranslateY;

        /// Target vertical translation.
        private double targetTranslateY;

        /// Whether the current start and target states differ visibly.
        private boolean visualChange;

        /// Creates a reusable entrance or exit transition.
        private SnackbarTransition(boolean entrance) {
            this.entrance = entrance;
            setOnFinished(event -> handleTransitionFinished(this));
        }

        /// Captures current node values and configures the next target state.
        private void configure(
                M3MotionSpec spec,
                M3Snackbar message,
                Node targetNode,
                double targetOpacity,
                double targetTranslateX,
                double targetTranslateY
        ) {
            stop();
            clearTargets();
            this.message = message;
            this.targetNode = targetNode;
            startOpacity = targetNode.getOpacity();
            startTranslateX = targetNode.getTranslateX();
            startTranslateY = targetNode.getTranslateY();
            this.targetOpacity = targetOpacity;
            this.targetTranslateX = targetTranslateX;
            this.targetTranslateY = targetTranslateY;
            visualChange = Double.compare(startOpacity, targetOpacity) != 0
                    || Double.compare(startTranslateX, targetTranslateX) != 0
                    || Double.compare(startTranslateY, targetTranslateY) != 0;
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
        }

        /// Returns whether this transition represents entrance.
        private boolean isEntrance() {
            return entrance;
        }

        /// Returns whether interpolation changes a visible channel.
        private boolean hasVisualChange() {
            return visualChange;
        }

        /// Clears targets and returns the associated message.
        private @Nullable M3Snackbar takeMessage() {
            @Nullable M3Snackbar currentMessage = message;
            clearTargets();
            return currentMessage;
        }

        /// Releases the associated message and stable node.
        private void clearTargets() {
            message = null;
            targetNode = null;
            visualChange = false;
        }

        /// Applies one eased frame to primitive node channels.
        @Override
        protected void interpolate(double fraction) {
            @Nullable Node node = targetNode;
            if (node == null) {
                return;
            }
            node.setOpacity(interpolate(startOpacity, targetOpacity, fraction));
            node.setTranslateX(M3SnackbarPresenter.this.snapPositionX(
                    interpolate(startTranslateX, targetTranslateX, fraction)
            ));
            node.setTranslateY(M3SnackbarPresenter.this.snapPositionY(
                    interpolate(startTranslateY, targetTranslateY, fraction)
            ));
        }

        /// Interpolates one primitive channel.
        private static double interpolate(double start, double end, double fraction) {
            return start + (end - start) * fraction;
        }
    }

    /// CSS metadata for internal snackbar component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3SnackbarPresenter, Number> CONTAINER_SHAPE =
                new GeometryMetadata("-m3-container-shape", DEFAULT_CONTAINER_SHAPE, Geometry.CONTAINER_SHAPE);

        /// CSS metadata for the content padding token.
        private static final CssMetaData<M3SnackbarPresenter, Number> CONTENT_PADDING =
                new GeometryMetadata("-m3-content-padding", DEFAULT_CONTENT_PADDING, Geometry.CONTENT_PADDING);

        /// CSS metadata for the minimum container width token.
        private static final CssMetaData<M3SnackbarPresenter, Number> CONTAINER_MIN_WIDTH =
                new GeometryMetadata("-m3-container-min-width", DEFAULT_CONTAINER_MIN_WIDTH, Geometry.MIN_WIDTH);

        /// CSS metadata for the maximum container width token.
        private static final CssMetaData<M3SnackbarPresenter, Number> CONTAINER_MAX_WIDTH =
                new GeometryMetadata("-m3-container-max-width", DEFAULT_CONTAINER_MAX_WIDTH, Geometry.MAX_WIDTH);

        /// CSS metadata for the single-line height token.
        private static final CssMetaData<M3SnackbarPresenter, Number> SINGLE_LINE_CONTAINER_HEIGHT =
                new GeometryMetadata(
                        "-m3-single-line-container-height",
                        DEFAULT_SINGLE_LINE_CONTAINER_HEIGHT,
                        Geometry.SINGLE_LINE_HEIGHT
                );

        /// CSS metadata for the two-line height token.
        private static final CssMetaData<M3SnackbarPresenter, Number> TWO_LINE_CONTAINER_HEIGHT =
                new GeometryMetadata(
                        "-m3-two-line-container-height",
                        DEFAULT_TWO_LINE_CONTAINER_HEIGHT,
                        Geometry.TWO_LINE_HEIGHT
                );

        /// CSS metadata for the action height token.
        private static final CssMetaData<M3SnackbarPresenter, Number> ACTION_CONTAINER_HEIGHT =
                new GeometryMetadata(
                        "-m3-action-container-height",
                        DEFAULT_ACTION_CONTAINER_HEIGHT,
                        Geometry.ACTION_HEIGHT
                );

        /// Complete immutable metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_SHAPE);
            styleables.add(CONTENT_PADDING);
            styleables.add(CONTAINER_MIN_WIDTH);
            styleables.add(CONTAINER_MAX_WIDTH);
            styleables.add(SINGLE_LINE_CONTAINER_HEIGHT);
            styleables.add(TWO_LINE_CONTAINER_HEIGHT);
            styleables.add(ACTION_CONTAINER_HEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents instantiation of the metadata holder.
        private StyleableProperties() {
        }
    }

    /// Selects one presenter geometry property for static CSS metadata.
    private enum Geometry {
        /// Container shape property.
        CONTAINER_SHAPE,
        /// Content padding property.
        CONTENT_PADDING,
        /// Minimum container width property.
        MIN_WIDTH,
        /// Maximum container width property.
        MAX_WIDTH,
        /// Single-line container height property.
        SINGLE_LINE_HEIGHT,
        /// Two-line container height property.
        TWO_LINE_HEIGHT,
        /// Action container height property.
        ACTION_HEIGHT;

        /// Returns the selected property from a presenter.
        private StyleableDoubleProperty get(M3SnackbarPresenter presenter) {
            return switch (this) {
                case CONTAINER_SHAPE -> presenter.containerShapeProperty();
                case CONTENT_PADDING -> presenter.contentPaddingProperty();
                case MIN_WIDTH -> presenter.containerMinWidthProperty();
                case MAX_WIDTH -> presenter.containerMaxWidthProperty();
                case SINGLE_LINE_HEIGHT -> presenter.singleLineContainerHeightProperty();
                case TWO_LINE_HEIGHT -> presenter.twoLineContainerHeightProperty();
                case ACTION_HEIGHT -> presenter.actionContainerHeightProperty();
            };
        }
    }

    /// CSS metadata implementation shared by non-negative presenter geometry tokens.
    @NotNullByDefault
    private static final class GeometryMetadata extends CssMetaData<M3SnackbarPresenter, Number> {
        /// Presenter property selected by this metadata entry.
        private final Geometry geometry;

        /// Creates one presenter geometry metadata entry.
        private GeometryMetadata(String property, double initialValue, Geometry geometry) {
            super(property, SizeConverter.getInstance(), initialValue);
            this.geometry = geometry;
        }

        /// Returns whether CSS may update the selected property.
        @Override
        public boolean isSettable(M3SnackbarPresenter control) {
            return M3Css.isSettable(geometry.get(control));
        }

        /// Returns the selected styleable property.
        @Override
        public StyleableProperty<Number> getStyleableProperty(M3SnackbarPresenter control) {
            return geometry.get(control);
        }
    }
}
