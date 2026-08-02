// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.IdentityKey;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3PresentationActivity;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/// A JavaFX scroll pane with Material styling, smooth wheel motion, and continuous-input stretch overscroll enabled
/// by default.
///
/// This control retains the complete [ScrollPane] content, viewport, layout, and scrolling API. Construction applies
/// the M3FX scroll stylesheet and installs decorated scroll handling. Fit policies, scrollbar policies, pannability,
/// focus traversal, and content sizing retain their inherited JavaFX defaults. Logical horizontal and vertical values
/// remain bounded; overscroll is rendered independently by [#getOverscrollEffect()].
///
/// The static methods apply Material presentation and smooth wheel behavior to existing JavaFX [ScrollPane]
/// instances and style standalone [ScrollBar] instances. Overscroll effects are specific to this subtype. Styling
/// and smooth scrolling remain independently controllable:
/// [#style(ScrollPane)] installs Material visual treatment, while [#enableSmoothScrolling(ScrollPane)] changes wheel
/// input handling. Neither operation affects unrelated scroll controls.
///
/// Bounded scrolling consumes input only when the target pane can move in the requested direction; an installed
/// overscroll effect may additionally consume the remainder at an edge. Scroll owners nested inside the pane keep
/// their own input. Unconsumed direct movement is offered as post-scroll delta to enclosing enabled `M3ScrollPane`
/// owners without crossing another kind of scroll owner. Installation is idempotent and remains attached until
/// [#disableSmoothScrolling(ScrollPane)] is called or the scroll pane becomes unreachable.
/// An accepted movement is applied synchronously while the pane has no scene or its associated window is hidden,
/// because no rendered pulse is available to advance the transition. Values written by scroll handling remain
/// within each axis's configured minimum and maximum, including while a spatial easing curve overshoots its target.
/// The default [M3OverscrollInputMode#CONTINUOUS] mode excludes isolated indirect wheel events while retaining direct
/// manipulation and lifecycle-delimited indirect gestures. Set [#setOverscrollEffect(M3OverscrollEffect)] to `null`
/// to disable the edge effect without changing styling or bounded scrolling.
///
/// See [Material Design scrolling behavior](https://m3.material.io/).
@NotNullByDefault
public final class M3ScrollPane extends ScrollPane {
    /// The node property key used to store the installed smooth scroll state.
    private static final IdentityKey SMOOTH_SCROLL_STATE_KEY =
            new IdentityKey(M3ScrollPane.class.getName() + ".smoothScrollState");

    /// The scene property key used to share direct-scroll routing.
    private static final IdentityKey DIRECT_SCROLL_DISPATCHER_KEY =
            new IdentityKey(M3ScrollPane.class.getName() + ".directScrollDispatcher");

    /// The default wheel line distance used when a platform reports text-line scroll units.
    private static final double DEFAULT_LINE_SCROLL_PIXELS = 40.0;

    /// The minimum meaningful scroll value difference.
    private static final double EPSILON = 0.000001;

    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-scroll-pane";

    /// The style class that enables Material styling for a standalone JavaFX [ScrollBar].
    private static final String SCROLL_BAR_STYLE_CLASS = "m3-scroll-bar";

    /// The default set of user inputs eligible for overscroll decoration.
    private static final M3OverscrollInputMode DEFAULT_OVERSCROLL_INPUT_MODE = M3OverscrollInputMode.CONTINUOUS;

    /// The effect attached to this pane and used by the installed scroll input behavior.
    private @Nullable M3OverscrollEffect attachedOverscrollEffect;

    /// Whether the default stretch effect has not yet been requested or replaced.
    private boolean defaultOverscrollEffectPending = true;

    /// The lazily created property that configures the effect decorating bounded user scrolling.
    private @Nullable ObjectProperty<@Nullable M3OverscrollEffect> overscrollEffect;

    /// The lazily created property that selects which inputs may use the configured overscroll effect.
    private @Nullable ObjectProperty<M3OverscrollInputMode> overscrollInputMode;

    /// Creates an empty Material scroll pane with smooth wheel motion and continuous-input stretch overscroll enabled.
    public M3ScrollPane() {
        initialize();
    }

    /// Creates a Material scroll pane containing the supplied node with smooth wheel motion and continuous-input
    /// stretch overscroll enabled.
    ///
    /// @param content the initial content, or `null` for no content
    public M3ScrollPane(@Nullable Node content) {
        super(content);
        initialize();
    }

    /// Applies the default Material presentation and input behavior to this scroll pane.
    private void initialize() {
        style(this);
        enableSmoothScrolling(this);
    }

    /// Returns which user scroll inputs may use the configured overscroll effect.
    ///
    /// @return the overscroll input mode
    public M3OverscrollInputMode getOverscrollInputMode() {
        @Nullable ObjectProperty<M3OverscrollInputMode> property = overscrollInputMode;
        return property == null
                ? DEFAULT_OVERSCROLL_INPUT_MODE
                : Objects.requireNonNullElse(property.get(), DEFAULT_OVERSCROLL_INPUT_MODE);
    }

    /// Sets which user scroll inputs may use the configured overscroll effect.
    ///
    /// Changing the mode releases an effect already in progress. Setting the mode does not install an effect when
    /// [#getOverscrollEffect()] is `null` and does not change bounded scrolling.
    ///
    /// @param mode the overscroll input mode
    /// @throws NullPointerException if `mode` is `null`
    public void setOverscrollInputMode(M3OverscrollInputMode mode) {
        M3OverscrollInputMode checkedMode = Objects.requireNonNull(mode, "mode");
        @Nullable ObjectProperty<M3OverscrollInputMode> property = overscrollInputMode;
        if (property == null && checkedMode == DEFAULT_OVERSCROLL_INPUT_MODE) {
            return;
        }
        overscrollInputModeProperty().set(checkedMode);
    }

    /// Returns the property selecting which user scroll inputs may use the configured overscroll effect.
    ///
    /// The property is writable and bindable. Its default value is [M3OverscrollInputMode#CONTINUOUS]. A direct
    /// `null` assignment restores that default; bound values must be non-null.
    ///
    /// @return the overscroll input-mode property
    public ObjectProperty<M3OverscrollInputMode> overscrollInputModeProperty() {
        @Nullable ObjectProperty<M3OverscrollInputMode> property = overscrollInputMode;
        if (property == null) {
            property = new ObjectPropertyBase<>(DEFAULT_OVERSCROLL_INPUT_MODE) {
                /// Applies input-policy changes and restores the default after a direct `null` assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_OVERSCROLL_INPUT_MODE);
                        return;
                    }
                    releaseOverscrollAfterInputModeChange();
                }

                /// Returns the scroll pane that owns this property.
                @Override
                public Object getBean() {
                    return M3ScrollPane.this;
                }

                /// Returns the JavaFX property name.
                @Override
                public String getName() {
                    return "overscrollInputMode";
                }
            };
            overscrollInputMode = property;
        }
        return property;
    }

    /// Releases transient overscroll after the accepted input set changes.
    private void releaseOverscrollAfterInputModeChange() {
        @Nullable SmoothScrollState state = smoothScrollState(this);
        if (state != null) {
            state.releaseOverscrollAfterInputModeChange();
        }
    }

    /// Returns the effect that decorates bounded user scrolling.
    ///
    /// The default is a pane-owned [M3StretchOverscrollEffect]. An effect instance is stateful and must not be shared
    /// by multiple panes.
    ///
    /// @return the attached effect, or `null` when overscroll is disabled
    public @Nullable M3OverscrollEffect getOverscrollEffect() {
        @Nullable ObjectProperty<@Nullable M3OverscrollEffect> property = overscrollEffect;
        return property == null ? resolveOverscrollEffect() : property.get();
    }

    /// Sets the effect that decorates bounded user scrolling.
    ///
    /// Replacing an effect synchronously settles and detaches the previous instance. Setting `null` disables
    /// overscroll while retaining smooth bounded scrolling.
    ///
    /// @param effect the pane-owned effect, or `null` to disable overscroll
    /// @throws IllegalStateException if `effect` is already attached to another scroll pane
    public void setOverscrollEffect(@Nullable M3OverscrollEffect effect) {
        @Nullable ObjectProperty<@Nullable M3OverscrollEffect> property = overscrollEffect;
        if (property == null) {
            M3OverscrollEffect.checkAttachable(effect, this);
            defaultOverscrollEffectPending = false;
            updateOverscrollEffect(effect);
        } else {
            property.set(effect);
        }
    }

    /// Returns the property containing the effect that decorates bounded user scrolling.
    ///
    /// @return the writable overscroll-effect property
    public ObjectProperty<@Nullable M3OverscrollEffect> overscrollEffectProperty() {
        @Nullable ObjectProperty<@Nullable M3OverscrollEffect> property = overscrollEffect;
        if (property == null) {
            property = new ObjectPropertyBase<>(resolveOverscrollEffect()) {
                @Override
                public void set(@Nullable M3OverscrollEffect value) {
                    M3OverscrollEffect.checkAttachable(value, M3ScrollPane.this);
                    super.set(value);
                }

                @Override
                protected void invalidated() {
                    updateOverscrollEffect(get());
                }

                @Override
                public Object getBean() {
                    return M3ScrollPane.this;
                }

                @Override
                public String getName() {
                    return "overscrollEffect";
                }
            };
            overscrollEffect = property;
        }
        return property;
    }

    /// Returns the configured effect, creating and attaching the default on first demand.
    ///
    /// @return the attached effect, or `null` after explicit disablement
    private @Nullable M3OverscrollEffect resolveOverscrollEffect() {
        if (defaultOverscrollEffectPending) {
            defaultOverscrollEffectPending = false;
            updateOverscrollEffect(new M3StretchOverscrollEffect());
        }
        return attachedOverscrollEffect;
    }

    /// Attaches a replacement effect and detaches the previously active instance.
    ///
    /// @param effect the replacement effect, or `null`
    private void updateOverscrollEffect(@Nullable M3OverscrollEffect effect) {
        @Nullable M3OverscrollEffect previousEffect = attachedOverscrollEffect;
        if (effect == previousEffect) {
            return;
        }
        if (effect != null) {
            M3OverscrollEffect.attach(effect, this);
        }
        attachedOverscrollEffect = effect;
        if (previousEffect != null) {
            M3OverscrollEffect.detach(previousEffect, this);
        }
    }

    /// Applies Material visual styling to a JavaFX scroll pane and its scroll bars.
    ///
    /// The operation adds the M3FX style class and stylesheet if absent. Repeated calls are idempotent and do not
    /// enable smooth wheel scrolling or replace the scroll pane's content.
    ///
    /// @param scrollPane the scroll pane to style
    /// @throws NullPointerException if `scrollPane` is `null`
    public static void style(ScrollPane scrollPane) {
        ScrollPane target = Objects.requireNonNull(scrollPane, "scrollPane");
        M3ControlStyles.initializeOnce(target, DEFAULT_STYLE_CLASS);
        installScrollStylesheet(target);
    }

    /// Enables Material smooth wheel scrolling for a JavaFX scroll pane.
    ///
    /// Repeated calls are idempotent. This method does not apply Material scrollbar styling; call
    /// [#style(ScrollPane)] separately when both behaviors are required. For an [M3ScrollPane], the installed input
    /// decorator also dispatches direct manipulation and unconsumed boundary delta to its current overscroll effect.
    ///
    /// @param scrollPane the scroll pane that should receive smooth wheel scrolling
    /// @throws NullPointerException if `scrollPane` is `null`
    public static void enableSmoothScrolling(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        if (!isSmoothScrollingEnabled(scrollPane)) {
            scrollPane.getProperties().put(SMOOTH_SCROLL_STATE_KEY, new SmoothScrollState(scrollPane));
        }
    }

    /// Disables Material smooth wheel scrolling for a JavaFX scroll pane.
    ///
    /// Pending smooth movement is stopped and event handlers installed by this class are removed. Calling this
    /// method for a pane that is not enabled has no effect and does not remove visual styling. An active overscroll
    /// effect on an [M3ScrollPane] is released; its configured effect remains attached for a later re-enable.
    ///
    /// @param scrollPane the scroll pane whose smooth wheel scrolling should be removed
    /// @throws NullPointerException if `scrollPane` is `null`
    public static void disableSmoothScrolling(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        if (!scrollPane.hasProperties()) {
            return;
        }
        @Nullable Object state = scrollPane.getProperties().remove(SMOOTH_SCROLL_STATE_KEY);
        if (state instanceof SmoothScrollState smoothScrollState) {
            smoothScrollState.dispose();
        }
    }

    /// Returns whether Material smooth wheel scrolling is enabled for a JavaFX scroll pane.
    ///
    /// @param scrollPane the scroll pane to inspect
    /// @return `true` if smooth wheel scrolling is installed on the scroll pane
    /// @throws NullPointerException if `scrollPane` is `null`
    public static boolean isSmoothScrollingEnabled(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        return smoothScrollState(scrollPane) != null;
    }

    /// Returns the installed input state without allocating a properties map.
    ///
    /// @param scrollPane the scroll pane to inspect
    /// @return the installed state, or `null`
    private static @Nullable SmoothScrollState smoothScrollState(ScrollPane scrollPane) {
        if (!scrollPane.hasProperties()) {
            return null;
        }
        @Nullable Object state = scrollPane.getProperties().get(SMOOTH_SCROLL_STATE_KEY);
        return state instanceof SmoothScrollState smoothScrollState ? smoothScrollState : null;
    }

    /// Applies Material visual styling to a standalone JavaFX scroll bar.
    ///
    /// The operation adds the M3FX style class and stylesheet if absent. Repeated calls are idempotent and do not
    /// change the scrollbar's range, value, orientation, or event handlers.
    ///
    /// @param scrollBar the scroll bar to style
    /// @throws NullPointerException if `scrollBar` is `null`
    public static void style(ScrollBar scrollBar) {
        ScrollBar target = Objects.requireNonNull(scrollBar, "scrollBar");
        M3ControlStyles.initializeOnce(target, SCROLL_BAR_STYLE_CLASS);
        installScrollStylesheet(target);
    }

    /// Installs the standalone scroll stylesheet on one styled JavaFX control.
    private static void installScrollStylesheet(Region control) {
        String stylesheet = M3Stylesheets.controlStylesheet("scroll.css");
        if (!control.getStylesheets().contains(stylesheet)) {
            control.getStylesheets().add(stylesheet);
        }
    }

    /// Returns whether a scroll event target belongs directly to the supplied scroll pane.
    ///
    /// Nested scroll owners keep their wheel input so that virtualized controls and nested scroll panes can scroll
    /// independently inside a styled outer [ScrollPane].
    ///
    /// @param scrollPane the scroll pane that owns the installed smooth scroll behavior
    /// @param target     the original scroll event target
    /// @return `true` if the target belongs to `scrollPane` rather than to a nested scroll owner
    static boolean isEventTargetForScrollPane(ScrollPane scrollPane, EventTarget target) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        if (!(target instanceof Node node)) {
            return true;
        }

        @Nullable Node current = node;
        while (current != null && current != scrollPane) {
            if (isNestedScrollOwner(current)) {
                return false;
            }
            current = current.getParent();
        }
        return current == scrollPane;
    }

    /// Returns whether a node owns its own wheel scrolling inside an outer scroll pane.
    private static boolean isNestedScrollOwner(Node node) {
        return node instanceof ScrollPane
                || node instanceof TextArea
                || node instanceof VirtualFlow<?>
                || node instanceof M3ListView<?>
                || node instanceof ListView<?>
                || node instanceof TreeView<?>
                || node instanceof TableView<?>
                || node instanceof TreeTableView<?>;
    }

    /// Routes direct input and post-scroll remainder through M3 owners before control skins process it.
    @NotNullByDefault
    private static final class DirectScrollDispatcher {
        /// The scene on which the shared capture filter is installed.
        private final Scene scene;

        /// Captures the beginning of a direct scroll gesture.
        private final EventHandler<ScrollEvent> scrollStartedHandler = this::handleScrollStarted;

        /// Captures direct movement only while a routed gesture is active.
        private final EventHandler<ScrollEvent> scrollHandler = this::handleScroll;

        /// Captures the end of a direct scroll gesture.
        private final EventHandler<ScrollEvent> scrollFinishedHandler = this::handleScrollFinished;

        /// Reusable callback that lets the active state end scene-level routing after presentation loss.
        private final Runnable finishGestureAction = this::finishGesture;

        /// The number of installed input states using this dispatcher.
        private int registrations;

        /// The input state owning the current direct gesture, or `null` while idle.
        private @Nullable SmoothScrollState activeState;

        /// Whether the movement filter is installed for an active direct gesture.
        private boolean scrollFilterInstalled;

        /// Creates and installs a dispatcher for one scene.
        ///
        /// @param scene the scene that owns the dispatcher
        private DirectScrollDispatcher(Scene scene) {
            this.scene = scene;
            scene.addEventFilter(ScrollEvent.SCROLL_STARTED, scrollStartedHandler);
            scene.addEventFilter(ScrollEvent.SCROLL_FINISHED, scrollFinishedHandler);
        }

        /// Registers one M3 scroll-pane input state.
        private void acquire() {
            registrations++;
        }

        /// Releases one M3 scroll-pane input state and removes an unused dispatcher.
        ///
        /// @param state the state leaving this scene
        private void release(SmoothScrollState state) {
            if (registrations <= 0) {
                return;
            }
            if (activeState == state) {
                finishGesture();
            } else if (state.scrollGestureActive) {
                state.finishScrollGesture();
            }
            registrations--;
            if (registrations == 0) {
                finishGesture();
                scene.removeEventFilter(ScrollEvent.SCROLL_STARTED, scrollStartedHandler);
                scene.removeEventFilter(ScrollEvent.SCROLL_FINISHED, scrollFinishedHandler);
                scene.getProperties().remove(DIRECT_SCROLL_DISPATCHER_KEY, this);
            }
        }

        /// Begins routing one direct gesture to its nearest M3 owner.
        ///
        /// @param event the gesture-start event captured at scene level
        private void handleScrollStarted(ScrollEvent event) {
            if (!event.isDirect() || event.isConsumed()) {
                return;
            }
            @Nullable SmoothScrollState state = ownerState(event.getTarget());
            if (state == null) {
                return;
            }

            if (activeState != null) {
                finishGesture();
            }
            activeState = state;
            installScrollFilter();
            state.handleScrollStartedFromScene(event, finishGestureAction);
            event.consume();
        }

        /// Routes movement during the active direct gesture.
        ///
        /// Each enclosing M3 owner receives only the delta left by its inner owner. The event remains unconsumed only
        /// when no routed owner accepts any delta, allowing an unrelated native scroll owner to handle it normally.
        ///
        /// @param event the movement event captured at scene level
        private void handleScroll(ScrollEvent event) {
            if (!event.isDirect()) {
                finishGesture();
                return;
            }
            @Nullable SmoothScrollState state = activeState;
            if (state == null || event.isConsumed()) {
                return;
            }

            state.handleScrollFromScene(event);
            boolean consumed = state.lastScrollConsumed;
            double remainingHorizontal = state.lastRemainingHorizontalDelta;
            double remainingVertical = state.lastRemainingVerticalDelta;
            while (!event.isConsumed()
                    && (Math.abs(remainingHorizontal) > EPSILON
                    || Math.abs(remainingVertical) > EPSILON)) {
                state = nextOwnerState(state);
                if (state == null) {
                    break;
                }
                state.startChainedDirectGesture(finishGestureAction);
                state.handleScrollFromScene(event, remainingHorizontal, remainingVertical);
                consumed |= state.lastScrollConsumed;
                remainingHorizontal = state.lastRemainingHorizontalDelta;
                remainingVertical = state.lastRemainingVerticalDelta;
            }
            if (consumed) {
                event.consume();
            }
        }

        /// Finishes routing the current direct gesture.
        ///
        /// @param event the gesture-finish event captured at scene level
        private void handleScrollFinished(ScrollEvent event) {
            if (!event.isDirect() || activeState == null) {
                return;
            }
            finishGesture();
            event.consume();
        }

        /// Installs the movement filter for the duration of a direct gesture.
        private void installScrollFilter() {
            if (!scrollFilterInstalled) {
                scene.addEventFilter(ScrollEvent.SCROLL, scrollHandler);
                scrollFilterInstalled = true;
            }
        }

        /// Releases the active gesture and removes its transient movement filter.
        private void finishGesture() {
            @Nullable SmoothScrollState state = activeState;
            activeState = null;
            if (scrollFilterInstalled) {
                scene.removeEventFilter(ScrollEvent.SCROLL, scrollHandler);
                scrollFilterInstalled = false;
            }
            while (state != null) {
                SmoothScrollState currentState = state;
                state = nextOwnerState(currentState);
                if (currentState.scrollGestureActive) {
                    currentState.finishScrollGesture();
                }
            }
        }

        /// Finds the next enclosing M3 owner without crossing another kind of scroll owner.
        ///
        /// @param state the current direct-scroll owner
        /// @return the next enabled M3 owner, or `null`
        private static @Nullable SmoothScrollState nextOwnerState(SmoothScrollState state) {
            @Nullable Node current = state.scrollPane.getParent();
            while (current != null) {
                if (isNestedScrollOwner(current)) {
                    return current instanceof M3ScrollPane materialScrollPane
                            ? smoothScrollState(materialScrollPane)
                            : null;
                }
                current = current.getParent();
            }
            return null;
        }

        /// Finds the nearest eligible M3 owner for an event target.
        ///
        /// @param target the original event target
        /// @return the owner's input state, or `null`
        private static @Nullable SmoothScrollState ownerState(EventTarget target) {
            if (!(target instanceof Node node)) {
                return null;
            }

            @Nullable M3ScrollPane candidate = null;
            @Nullable Node current = node;
            while (current != null) {
                if (current instanceof M3ScrollPane materialScrollPane) {
                    candidate = materialScrollPane;
                    break;
                }
                current = current.getParent();
            }
            if (candidate == null || !isEventTargetForScrollPane(candidate, target)) {
                return null;
            }

            return smoothScrollState(candidate);
        }

        /// Acquires the scene-shared dispatcher, creating it on first use.
        ///
        /// @param scene the scene receiving a registered M3 scroll pane
        private static void acquire(Scene scene) {
            @Nullable Object value = scene.getProperties().get(DIRECT_SCROLL_DISPATCHER_KEY);
            DirectScrollDispatcher dispatcher;
            if (value instanceof DirectScrollDispatcher existingDispatcher) {
                dispatcher = existingDispatcher;
            } else {
                dispatcher = new DirectScrollDispatcher(scene);
                scene.getProperties().put(DIRECT_SCROLL_DISPATCHER_KEY, dispatcher);
            }
            dispatcher.acquire();
        }

        /// Releases one registration from the scene-shared dispatcher.
        ///
        /// @param scene the scene losing a registered M3 scroll pane
        /// @param state the input state leaving the scene
        private static void release(Scene scene, SmoothScrollState state) {
            if (!scene.hasProperties()) {
                return;
            }
            @Nullable Object value = scene.getProperties().get(DIRECT_SCROLL_DISPATCHER_KEY);
            if (value instanceof DirectScrollDispatcher dispatcher) {
                dispatcher.release(state);
            }
        }
    }

    /// Handles smooth wheel scrolling for one JavaFX scroll pane.
    @NotNullByDefault
    private static final class SmoothScrollState {
        /// The scroll pane receiving smooth wheel behavior.
        private final ScrollPane scrollPane;

        /// The scroll event filter installed on the scroll pane.
        private final EventHandler<ScrollEvent> scrollHandler = this::handleScroll;

        /// The gesture-start filter installed on the scroll pane.
        private final EventHandler<ScrollEvent> scrollStartedHandler = this::handleScrollStarted;

        /// The gesture-finish filter installed on the scroll pane.
        private final EventHandler<ScrollEvent> scrollFinishedHandler = this::handleScrollFinished;

        /// The allocation-free horizontal bounded-scroll callback supplied to an overscroll effect.
        private final DoubleUnaryOperator horizontalScrollOperation = this::performHorizontalScroll;

        /// The allocation-free vertical bounded-scroll callback supplied to an overscroll effect.
        private final DoubleUnaryOperator verticalScrollOperation = this::performVerticalScroll;

        /// Updates a running smooth scroll when motion settings change.
        private final M3MotionSettingsObserver motionSettingsObserver;

        /// The reusable transition that interpolates both scroll axes.
        private final ScrollTransition animation;

        /// Marks cached content metrics dirty after viewport or content geometry changes.
        private final InvalidationListener scrollMetricsInvalidation = observable -> scrollMetricsDirty = true;

        /// Updates the observed content node after the scroll pane content changes.
        private final ChangeListener<@Nullable Node> contentListener =
                (observable, oldContent, newContent) -> updateObservedContent(newContent);

        /// Moves direct-scroll registration when an M3 scroll pane changes scenes.
        private final ChangeListener<@Nullable Scene> sceneListener =
                (observable, oldScene, newScene) -> updateRegisteredScene(newScene);

        /// The content node currently observed for geometry changes.
        private @Nullable Node observedContent;

        /// The observed content when it exposes preferred-size properties.
        private @Nullable Region observedContentRegion;

        /// The scene currently holding this state's direct-scroll registration.
        private @Nullable Scene registeredScene;

        /// Whether content width and height must be measured before the next scroll event.
        private boolean scrollMetricsDirty = true;

        /// The last measured content width.
        private double cachedContentWidth;

        /// The last measured content height.
        private double cachedContentHeight;

        /// The accumulated horizontal target value.
        private double targetHValue;

        /// The accumulated vertical target value.
        private double targetVValue;

        /// The resolved animation setting cached for [motionSettingsRevision].
        private boolean cachedAnimationsEnabled;

        /// The motion-settings revision represented by [cachedAnimationsEnabled].
        private long motionSettingsRevision = Long.MIN_VALUE;

        /// The scrollable horizontal pixel span used by the current target value.
        private double targetHScrollablePixels;

        /// The scrollable vertical pixel span used by the current target value.
        private double targetVScrollablePixels;

        /// Whether one callback invocation applies values synchronously instead of targeting smooth motion.
        private boolean synchronousScrollOperation;

        /// Whether the callback supplied to the current effect invocation has been called.
        private boolean scrollOperationInvoked;

        /// The delta made available to the current effect invocation.
        private double scrollOperationAvailableDelta;

        /// Whether a platform scroll gesture has emitted a start event without a matching finish event.
        private boolean scrollGestureActive;

        /// Observes presentation activity only while a direct gesture is active.
        private @Nullable M3MotionSettingsObserver directGestureObserver;

        /// Ends scene-level routing when the active direct gesture loses its presentation, or `null`.
        private @Nullable Runnable directGestureCancellation;

        /// Whether the next direct control-phase event was already offered by the scene dispatcher.
        private boolean skipNextDirectControlEvent;

        /// The horizontal pixel delta left after the most recent scene-routed direct operation.
        private double lastRemainingHorizontalDelta;

        /// The vertical pixel delta left after the most recent scene-routed direct operation.
        private double lastRemainingVerticalDelta;

        /// Whether the most recent scene-routed direct operation consumed any delta.
        private boolean lastScrollConsumed;

        /// Creates and installs smooth wheel behavior.
        private SmoothScrollState(ScrollPane scrollPane) {
            this.scrollPane = scrollPane;
            animation = new ScrollTransition(scrollPane);
            targetHValue = scrollPane.getHvalue();
            targetVValue = scrollPane.getVvalue();
            updateObservedContent(scrollPane.getContent());
            scrollPane.contentProperty().addListener(contentListener);
            scrollPane.viewportBoundsProperty().addListener(scrollMetricsInvalidation);
            scrollPane.fitToWidthProperty().addListener(scrollMetricsInvalidation);
            scrollPane.fitToHeightProperty().addListener(scrollMetricsInvalidation);
            if (scrollPane instanceof M3ScrollPane) {
                scrollPane.sceneProperty().addListener(sceneListener);
                updateRegisteredScene(scrollPane.getScene());
            }
            scrollPane.addEventFilter(ScrollEvent.SCROLL_STARTED, scrollStartedHandler);
            scrollPane.addEventFilter(ScrollEvent.SCROLL, scrollHandler);
            scrollPane.addEventFilter(ScrollEvent.SCROLL_FINISHED, scrollFinishedHandler);
            motionSettingsObserver = new M3MotionSettingsObserver(scrollPane, this::refreshMotionSettings, false);
            animation.setOnFinished(event -> motionSettingsObserver.stop());
        }

        /// Removes smooth wheel behavior and stops any running animation.
        private void dispose() {
            stopAnimation();
            motionSettingsObserver.dispose();
            animation.setOnFinished(null);
            scrollPane.removeEventFilter(ScrollEvent.SCROLL_STARTED, scrollStartedHandler);
            scrollPane.removeEventFilter(ScrollEvent.SCROLL, scrollHandler);
            scrollPane.removeEventFilter(ScrollEvent.SCROLL_FINISHED, scrollFinishedHandler);
            scrollPane.contentProperty().removeListener(contentListener);
            scrollPane.viewportBoundsProperty().removeListener(scrollMetricsInvalidation);
            scrollPane.fitToWidthProperty().removeListener(scrollMetricsInvalidation);
            scrollPane.fitToHeightProperty().removeListener(scrollMetricsInvalidation);
            if (scrollPane instanceof M3ScrollPane) {
                scrollPane.sceneProperty().removeListener(sceneListener);
                updateRegisteredScene(null);
            }
            updateObservedContent(null);
            skipNextDirectControlEvent = false;
            @Nullable M3MotionSettingsObserver gestureObserver = directGestureObserver;
            if (gestureObserver != null) {
                gestureObserver.dispose();
                directGestureObserver = null;
            }
            directGestureCancellation = null;
            scrollGestureActive = false;
            @Nullable M3OverscrollEffect effect = attachedOverscrollEffect();
            if (effect != null) {
                effect.release();
            }
        }

        /// Transfers this state's registration between scene-shared direct-scroll dispatchers.
        ///
        /// @param scene the new scene, or `null` while detached
        private void updateRegisteredScene(@Nullable Scene scene) {
            skipNextDirectControlEvent = false;
            @Nullable Scene previousScene = registeredScene;
            if (previousScene == scene) {
                return;
            }
            if (previousScene != null) {
                DirectScrollDispatcher.release(previousScene, this);
            }
            registeredScene = scene;
            if (scene != null) {
                DirectScrollDispatcher.acquire(scene);
            }
        }

        /// Starts one platform scroll gesture and cancels stale smooth motion before direct manipulation.
        private void handleScrollStarted(ScrollEvent event) {
            handleScrollStarted(event, null);
        }

        /// Starts a scene-routed direct gesture with a presentation-loss callback.
        ///
        /// @param event the captured gesture-start event
        /// @param cancellation the callback that ends scene-level routing
        private void handleScrollStartedFromScene(ScrollEvent event, Runnable cancellation) {
            handleScrollStarted(event, Objects.requireNonNull(cancellation, "cancellation"));
        }

        /// Starts one platform scroll gesture with optional scene-level cancellation.
        ///
        /// @param event the gesture-start event
        /// @param cancellation the callback that ends scene-level routing, or `null`
        private void handleScrollStarted(ScrollEvent event, @Nullable Runnable cancellation) {
            skipNextDirectControlEvent = false;
            if (!isEventTargetForScrollPane(scrollPane, event.getTarget())) {
                return;
            }
            scrollGestureActive = true;
            if (event.isDirect()) {
                startDirectGesture(cancellation);
            }
        }

        /// Joins an ancestor pane to a scene-routed direct gesture when it receives post-scroll delta.
        ///
        /// @param cancellation the callback that ends scene-level routing
        private void startChainedDirectGesture(Runnable cancellation) {
            if (scrollGestureActive) {
                return;
            }
            skipNextDirectControlEvent = false;
            scrollGestureActive = true;
            startDirectGesture(Objects.requireNonNull(cancellation, "cancellation"));
        }

        /// Starts direct manipulation from the pane's current bounded values.
        ///
        /// @param cancellation the callback that ends scene-level routing, or `null`
        private void startDirectGesture(@Nullable Runnable cancellation) {
            stopAnimation();
            targetHValue = scrollPane.getHvalue();
            targetVValue = scrollPane.getVvalue();
            if (scrollPane instanceof M3ScrollPane) {
                directGestureCancellation = cancellation;
                directGestureObserver().start();
            }
        }

        /// Releases the overscroll effect after one platform scroll gesture finishes.
        private void handleScrollFinished(ScrollEvent event) {
            skipNextDirectControlEvent = false;
            if (!isEventTargetForScrollPane(scrollPane, event.getTarget())) {
                return;
            }
            finishScrollGesture();
        }

        /// Releases the effect and state retained for the current scroll gesture.
        private void finishScrollGesture() {
            skipNextDirectControlEvent = false;
            scrollGestureActive = false;
            directGestureCancellation = null;
            @Nullable M3MotionSettingsObserver observer = directGestureObserver;
            if (observer != null) {
                observer.stop();
            }
            @Nullable M3OverscrollEffect effect = attachedOverscrollEffect();
            if (effect != null) {
                effect.release();
            }
        }

        /// Returns the direct-gesture presentation observer, creating it on first use.
        ///
        /// @return the reusable inactive or active observer
        private M3MotionSettingsObserver directGestureObserver() {
            @Nullable M3MotionSettingsObserver observer = directGestureObserver;
            if (observer == null) {
                observer = new M3MotionSettingsObserver(
                        scrollPane,
                        this::refreshDirectGesturePresentation,
                        false
                );
                directGestureObserver = observer;
            }
            return observer;
        }

        /// Ends a direct gesture when its pane stops receiving rendered pulses.
        private void refreshDirectGesturePresentation() {
            if (!scrollGestureActive || !isPresentationUnavailable()) {
                return;
            }
            @Nullable Runnable cancellation = directGestureCancellation;
            if (cancellation != null) {
                cancellation.run();
            } else {
                finishScrollGesture();
            }
        }

        /// Handles one wheel or trackpad scroll event.
        private void handleScroll(ScrollEvent event) {
            if (event.isDirect() && skipNextDirectControlEvent) {
                skipNextDirectControlEvent = false;
                return;
            }
            if (!isEventTargetForScrollPane(scrollPane, event.getTarget())) {
                return;
            }

            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            handleOwnedScroll(
                    event,
                    finiteScrollDelta(scrollDeltaX(event)),
                    finiteScrollDelta(scrollDeltaY(event, viewportHeight)),
                    true
            );
        }

        /// Applies one event's available pixel deltas to this pane and records any post-scroll remainder.
        ///
        /// @param event the source event
        /// @param horizontalDelta the available horizontal pixel delta
        /// @param verticalDelta the available vertical pixel delta
        /// @param consumeEvent whether this method should consume the JavaFX event after accepting delta
        private void handleOwnedScroll(
                ScrollEvent event,
                double horizontalDelta,
                double verticalDelta,
                boolean consumeEvent
        ) {
            lastRemainingHorizontalDelta = horizontalDelta;
            lastRemainingVerticalDelta = verticalDelta;
            lastScrollConsumed = false;
            @Nullable M3OverscrollEffect effect = scrollOverscrollEffect(event);
            if (event.isDirect() && effect == null && !(scrollPane instanceof M3ScrollPane)) {
                return;
            }

            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            refreshScrollMetricsIfNeeded();
            double horizontalScrollablePixels = Math.max(0.0, cachedContentWidth - viewportWidth);
            double verticalScrollablePixels = Math.max(0.0, cachedContentHeight - viewportHeight);
            boolean canScrollHorizontally = canScroll(
                    scrollPane.getHmin(),
                    scrollPane.getHmax(),
                    horizontalScrollablePixels
            );
            boolean canScrollVertically = canScroll(
                    scrollPane.getVmin(),
                    scrollPane.getVmax(),
                    verticalScrollablePixels
            );
            if (event.isDirect()) {
                stopAnimation();
            }
            if (animation.getStatus() == Animation.Status.STOPPED) {
                targetHValue = scrollPane.getHvalue();
                targetVValue = scrollPane.getVvalue();
            } else {
                targetHValue = retargetScrollValue(
                        targetHValue,
                        targetHScrollablePixels,
                        horizontalScrollablePixels,
                        scrollPane.getHmin(),
                        scrollPane.getHmax()
                );
                targetVValue = retargetScrollValue(
                        targetVValue,
                        targetVScrollablePixels,
                        verticalScrollablePixels,
                        scrollPane.getVmin(),
                        scrollPane.getVmax()
                );
            }
            targetHScrollablePixels = horizontalScrollablePixels;
            targetVScrollablePixels = verticalScrollablePixels;
            if (event.isShiftDown() && canScrollHorizontally && Math.abs(horizontalDelta) <= EPSILON) {
                horizontalDelta = verticalDelta;
                verticalDelta = 0.0;
            } else if (!canScrollVertically
                    && canScrollHorizontally
                    && Math.abs(horizontalDelta) <= EPSILON
                    && Math.abs(verticalDelta) > EPSILON) {
                horizontalDelta = verticalDelta;
                verticalDelta = 0.0;
            }

            lastRemainingHorizontalDelta = horizontalDelta;
            lastRemainingVerticalDelta = verticalDelta;

            if (Math.abs(horizontalDelta) <= EPSILON && Math.abs(verticalDelta) <= EPSILON) {
                return;
            }

            double previousHValue = targetHValue;
            double previousVValue = targetVValue;
            synchronousScrollOperation = event.isDirect();
            double consumedHorizontally = applyScrollOperation(
                    effect,
                    Orientation.HORIZONTAL,
                    horizontalDelta,
                    event,
                    horizontalScrollOperation
            );
            double consumedVertically = applyScrollOperation(
                    effect,
                    Orientation.VERTICAL,
                    verticalDelta,
                    event,
                    verticalScrollOperation
            );
            lastRemainingHorizontalDelta = remainingDelta(horizontalDelta, consumedHorizontally);
            lastRemainingVerticalDelta = remainingDelta(verticalDelta, consumedVertically);
            lastScrollConsumed = Math.abs(consumedHorizontally) > EPSILON
                    || Math.abs(consumedVertically) > EPSILON;

            if (!event.isDirect()
                    && (!close(previousHValue, targetHValue) || !close(previousVValue, targetVValue))) {
                animateToTarget();
            }
            if (consumeEvent && lastScrollConsumed) {
                event.consume();
            }
            if (!event.isDirect() && !scrollGestureActive && effect != null) {
                effect.release();
            }
        }

        /// Offers a direct movement event before control filters and suppresses a duplicate target-phase offer.
        ///
        /// @param event the event captured by the scene dispatcher
        private void handleScrollFromScene(ScrollEvent event) {
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            handleScrollFromScene(
                    event,
                    finiteScrollDelta(scrollDeltaX(event)),
                    finiteScrollDelta(scrollDeltaY(event, viewportHeight))
            );
        }

        /// Offers post-scroll pixel delta from an inner M3 owner before control filters process the event.
        ///
        /// @param event the event captured by the scene dispatcher
        /// @param horizontalDelta the remaining horizontal pixel delta
        /// @param verticalDelta the remaining vertical pixel delta
        private void handleScrollFromScene(
                ScrollEvent event,
                double horizontalDelta,
                double verticalDelta
        ) {
            @Nullable Scene dispatchScene = scrollPane.getScene();
            skipNextDirectControlEvent = false;
            handleOwnedScroll(event, horizontalDelta, verticalDelta, false);
            if (!event.isConsumed() && dispatchScene != null && scrollPane.getScene() == dispatchScene) {
                skipNextDirectControlEvent = true;
            }
        }

        /// Applies an optional overscroll decorator around one bounded axis operation.
        ///
        /// @param effect      the effect to apply, or `null`
        /// @param orientation the affected axis
        /// @param delta       the available pixel delta
        /// @param event       the source event
        /// @param operation   the cached bounded-scroll callback for the axis
        /// @return the delta consumed by the complete operation
        private double applyScrollOperation(
                @Nullable M3OverscrollEffect effect,
                Orientation orientation,
                double delta,
                ScrollEvent event,
                DoubleUnaryOperator operation
        ) {
            if (Math.abs(delta) <= EPSILON) {
                return 0.0;
            }

            scrollOperationAvailableDelta = delta;
            scrollOperationInvoked = false;
            try {
                if (effect == null) {
                    double consumed = operation.applyAsDouble(delta);
                    validateConsumedDelta(delta, consumed, "performScroll return value");
                    return consumed;
                }

                double consumed = effect.applyToScroll(orientation, delta, event, operation);
                if (!scrollOperationInvoked) {
                    throw new IllegalStateException("M3OverscrollEffect.applyToScroll must invoke performScroll once");
                }
                validateConsumedDelta(delta, consumed, "M3OverscrollEffect return value");
                return consumed;
            } finally {
                scrollOperationAvailableDelta = 0.0;
                scrollOperationInvoked = false;
            }
        }

        /// Performs the horizontal bounded scroll requested by the current effect invocation.
        ///
        /// @param delta the available horizontal pixel delta
        /// @return the horizontal pixel delta consumed by logical scrolling
        private double performHorizontalScroll(double delta) {
            beginScrollOperation(delta);
            double currentValue = synchronousScrollOperation ? scrollPane.getHvalue() : targetHValue;
            double nextValue = scrollTargetValue(
                    currentValue,
                    delta,
                    scrollPane.getHmin(),
                    scrollPane.getHmax(),
                    targetHScrollablePixels
            );
            targetHValue = nextValue;
            if (synchronousScrollOperation) {
                scrollPane.setHvalue(nextValue);
            }
            return consumedScrollDelta(
                    currentValue,
                    nextValue,
                    scrollPane.getHmin(),
                    scrollPane.getHmax(),
                    targetHScrollablePixels
            );
        }

        /// Performs the vertical bounded scroll requested by the current effect invocation.
        ///
        /// @param delta the available vertical pixel delta
        /// @return the vertical pixel delta consumed by logical scrolling
        private double performVerticalScroll(double delta) {
            beginScrollOperation(delta);
            double currentValue = synchronousScrollOperation ? scrollPane.getVvalue() : targetVValue;
            double nextValue = scrollTargetValue(
                    currentValue,
                    delta,
                    scrollPane.getVmin(),
                    scrollPane.getVmax(),
                    targetVScrollablePixels
            );
            targetVValue = nextValue;
            if (synchronousScrollOperation) {
                scrollPane.setVvalue(nextValue);
            }
            return consumedScrollDelta(
                    currentValue,
                    nextValue,
                    scrollPane.getVmin(),
                    scrollPane.getVmax(),
                    targetVScrollablePixels
            );
        }

        /// Validates and marks the single callback invocation permitted for one effect application.
        ///
        /// @param delta the delta passed by the effect to bounded scrolling
        private void beginScrollOperation(double delta) {
            if (scrollOperationInvoked) {
                throw new IllegalStateException(
                        "M3OverscrollEffect.applyToScroll invoked performScroll more than once"
                );
            }
            validateConsumedDelta(scrollOperationAvailableDelta, delta, "performScroll argument");
            scrollOperationInvoked = true;
        }

        /// Returns the already attached overscroll effect without creating the default.
        ///
        /// @return the attached effect, or `null` for an ordinary pane, a pending default, or a disabled effect
        private @Nullable M3OverscrollEffect attachedOverscrollEffect() {
            return scrollPane instanceof M3ScrollPane materialScrollPane
                    ? materialScrollPane.attachedOverscrollEffect
                    : null;
        }

        /// Returns the effect for an eligible user scroll event, creating the default on first accepted input.
        ///
        /// @param event the event requesting overscroll decoration
        /// @return the active effect, or `null` for an ordinary JavaFX scroll pane or a disabled effect
        private @Nullable M3OverscrollEffect scrollOverscrollEffect(ScrollEvent event) {
            if (!(scrollPane instanceof M3ScrollPane materialScrollPane)
                    || !materialScrollPane.getOverscrollInputMode().accepts(event.isDirect(), scrollGestureActive)) {
                return null;
            }
            return materialScrollPane.resolveOverscrollEffect();
        }

        /// Releases transient overscroll after this pane's accepted input set changes.
        private void releaseOverscrollAfterInputModeChange() {
            @Nullable M3OverscrollEffect effect = attachedOverscrollEffect();
            if (effect != null) {
                effect.release();
            }
        }

        /// Applies changed animation settings to the current smooth scroll operation.
        private void refreshMotionSettings() {
            if (animation.getStatus() != Animation.Status.RUNNING) {
                return;
            }

            if (isPresentationUnavailable() || animationsDisabled()) {
                animation.finish();
                motionSettingsObserver.stop();
            } else {
                animateToTarget();
            }
        }

        /// Starts an animation toward the accumulated target values.
        private void animateToTarget() {
            targetHValue = clamp(targetHValue, scrollPane.getHmin(), scrollPane.getHmax());
            targetVValue = clamp(targetVValue, scrollPane.getVmin(), scrollPane.getVmax());
            if (isPresentationUnavailable() || animationsDisabled()) {
                animation.stop();
                scrollPane.setHvalue(targetHValue);
                scrollPane.setVvalue(targetVValue);
                return;
            }

            M3MotionSpec spec = M3Animation.defaultSpatial(scrollPane);
            animation.configure(
                    spec,
                    scrollPane.getHvalue(),
                    targetHValue,
                    scrollPane.getVvalue(),
                    targetVValue
            );
            motionSettingsObserver.start();
            animation.playFromStart();
        }

        /// Returns whether the pane cannot currently receive rendered pulses.
        private boolean isPresentationUnavailable() {
            @Nullable Scene scene = scrollPane.getScene();
            if (scene == null) {
                return true;
            }
            @Nullable Window window = scene.getWindow();
            return !M3PresentationActivity.isTreeVisible(scrollPane)
                    || window != null && !M3PresentationActivity.isRenderActive(window);
        }

        /// Returns whether inherited animations are disabled, refreshing the cache after any settings change.
        private boolean animationsDisabled() {
            long revision = M3MotionSettingsObserver.reducedMotionRevision();
            if (motionSettingsRevision != revision) {
                cachedAnimationsEnabled = M3Animation.areAnimationsEnabled(scrollPane);
                motionSettingsRevision = revision;
            }
            return !cachedAnimationsEnabled;
        }

        /// Stops the current scroll animation.
        private void stopAnimation() {
            animation.stop();
            motionSettingsObserver.stop();
        }

        /// Replaces the content geometry listener and invalidates cached scroll metrics.
        private void updateObservedContent(@Nullable Node content) {
            @Nullable Node previousContent = observedContent;
            if (previousContent == content) {
                return;
            }
            if (previousContent != null) {
                previousContent.boundsInLocalProperty().removeListener(scrollMetricsInvalidation);
            }
            @Nullable Region previousRegion = observedContentRegion;
            if (previousRegion != null) {
                previousRegion.prefWidthProperty().removeListener(scrollMetricsInvalidation);
                previousRegion.prefHeightProperty().removeListener(scrollMetricsInvalidation);
            }
            observedContent = content;
            observedContentRegion = content instanceof Region region ? region : null;
            if (content != null) {
                content.boundsInLocalProperty().addListener(scrollMetricsInvalidation);
            }
            @Nullable Region contentRegion = observedContentRegion;
            if (contentRegion != null) {
                contentRegion.prefWidthProperty().addListener(scrollMetricsInvalidation);
                contentRegion.prefHeightProperty().addListener(scrollMetricsInvalidation);
            }
            scrollMetricsDirty = true;
        }

        /// Recomputes content dimensions once after an observed geometry change.
        private void refreshScrollMetricsIfNeeded() {
            if (!scrollMetricsDirty) {
                return;
            }
            cachedContentWidth = contentWidth();
            cachedContentHeight = contentHeight();
            scrollMetricsDirty = false;
        }

        /// Returns the current content width.
        private double contentWidth() {
            @Nullable Node content = scrollPane.getContent();
            if (content == null) {
                return 0.0;
            }

            Bounds bounds = content.getBoundsInLocal();
            double width = bounds.getWidth();
            if (content instanceof Region region) {
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double preferredWidth = region.prefWidth(viewportHeight > 0.0 ? viewportHeight : -1.0);
                width = Math.max(width, preferredWidth);
            }
            return width;
        }

        /// Returns the current content height.
        private double contentHeight() {
            @Nullable Node content = scrollPane.getContent();
            if (content == null) {
                return 0.0;
            }

            Bounds bounds = content.getBoundsInLocal();
            double height = bounds.getHeight();
            if (content instanceof Region region) {
                double viewportWidth = scrollPane.getViewportBounds().getWidth();
                double preferredHeight = region.prefHeight(viewportWidth > 0.0 ? viewportWidth : -1.0);
                height = Math.max(height, preferredHeight);
            }
            return height;
        }
    }

    /// A reusable two-axis transition for one scroll pane.
    @NotNullByDefault
    private static final class ScrollTransition extends Transition {
        /// The scroll pane whose values are interpolated.
        private final ScrollPane scrollPane;

        /// The horizontal value at the beginning of the current transition.
        private double startHValue;

        /// The horizontal value at the end of the current transition.
        private double targetHValue;

        /// The vertical value at the beginning of the current transition.
        private double startVValue;

        /// The vertical value at the end of the current transition.
        private double targetVValue;

        /// Creates a reusable transition for a scroll pane.
        private ScrollTransition(ScrollPane scrollPane) {
            this.scrollPane = scrollPane;
        }

        /// Reconfigures this transition without replacing the animation object.
        private void configure(
                M3MotionSpec spec,
                double startHValue,
                double targetHValue,
                double startVValue,
                double targetVValue
        ) {
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            this.startHValue = clamp(startHValue, scrollPane.getHmin(), scrollPane.getHmax());
            this.targetHValue = clamp(targetHValue, scrollPane.getHmin(), scrollPane.getHmax());
            this.startVValue = clamp(startVValue, scrollPane.getVmin(), scrollPane.getVmax());
            this.targetVValue = clamp(targetVValue, scrollPane.getVmin(), scrollPane.getVmax());
        }

        /// Stops this transition and applies its configured final scroll values synchronously.
        private void finish() {
            stop();
            scrollPane.setHvalue(clamp(targetHValue, scrollPane.getHmin(), scrollPane.getHmax()));
            scrollPane.setVvalue(clamp(targetVValue, scrollPane.getVmin(), scrollPane.getVmax()));
        }

        /// Interpolates both normalized scroll values for the current animation pulse.
        @Override
        protected void interpolate(double fraction) {
            scrollPane.setHvalue(interpolateScrollValue(
                    startHValue,
                    targetHValue,
                    fraction,
                    scrollPane.getHmin(),
                    scrollPane.getHmax()
            ));
            scrollPane.setVvalue(interpolateScrollValue(
                    startVValue,
                    targetVValue,
                    fraction,
                    scrollPane.getVmin(),
                    scrollPane.getVmax()
            ));
        }
    }

    /// Converts an event's horizontal scroll amount to pixels.
    private static double scrollDeltaX(ScrollEvent event) {
        return switch (event.getTextDeltaXUnits()) {
            case CHARACTERS -> event.getTextDeltaX() * DEFAULT_LINE_SCROLL_PIXELS;
            case NONE -> event.getDeltaX();
        };
    }

    /// Converts an event's vertical scroll amount to pixels.
    private static double scrollDeltaY(ScrollEvent event, double viewportHeight) {
        return switch (event.getTextDeltaYUnits()) {
            case LINES -> event.getTextDeltaY() * DEFAULT_LINE_SCROLL_PIXELS;
            case PAGES -> event.getTextDeltaY() * viewportHeight;
            case NONE -> event.getDeltaY();
        };
    }

    /// Returns a finite input delta, treating invalid platform values as no movement.
    ///
    /// @param delta the reported input delta
    /// @return `delta` when finite, otherwise zero
    private static double finiteScrollDelta(double delta) {
        return Double.isFinite(delta) ? delta : 0.0;
    }

    /// Computes the target normalized scroll value after applying a pixel delta.
    private static double scrollTargetValue(
            double currentValue,
            double scrollDelta,
            double minValue,
            double maxValue,
            double scrollablePixels
    ) {
        if (scrollablePixels <= EPSILON || close(minValue, maxValue)) {
            return currentValue;
        }

        double currentPixels = pixelsForValue(currentValue, minValue, maxValue, scrollablePixels);
        double targetPixels = clamp(currentPixels - scrollDelta, 0.0, scrollablePixels);
        return valueForPixels(targetPixels, minValue, maxValue, scrollablePixels);
    }

    /// Computes the signed pixel delta consumed between two bounded scroll values.
    ///
    /// @param currentValue    the value before scrolling
    /// @param nextValue       the value after scrolling
    /// @param minValue        the inclusive axis minimum
    /// @param maxValue        the inclusive axis maximum
    /// @param scrollablePixels the scrollable content span in pixels
    /// @return the signed input delta consumed by the value change
    private static double consumedScrollDelta(
            double currentValue,
            double nextValue,
            double minValue,
            double maxValue,
            double scrollablePixels
    ) {
        if (!canScroll(minValue, maxValue, scrollablePixels)) {
            return 0.0;
        }
        return pixelsForValue(currentValue, minValue, maxValue, scrollablePixels)
                - pixelsForValue(nextValue, minValue, maxValue, scrollablePixels);
    }

    /// Returns the meaningful portion of an available delta left after bounded or effect consumption.
    ///
    /// @param availableDelta the pixel delta offered to one axis
    /// @param consumedDelta the pixel delta accepted on that axis
    /// @return the remaining signed pixel delta, or zero within numerical tolerance
    private static double remainingDelta(double availableDelta, double consumedDelta) {
        double remaining = availableDelta - consumedDelta;
        return Math.abs(remaining) <= EPSILON ? 0.0 : remaining;
    }

    /// Validates a consumed or delegated delta against the amount made available.
    ///
    /// @param availableDelta the complete available delta
    /// @param consumedDelta  the candidate consumed or delegated delta
    /// @param description    the value description used by a validation failure
    /// @throws IllegalArgumentException if the candidate is non-finite, reverses direction, or exceeds the available
    ///                                  magnitude
    private static void validateConsumedDelta(
            double availableDelta,
            double consumedDelta,
            String description
    ) {
        if (!Double.isFinite(consumedDelta)
                || Math.abs(consumedDelta) > Math.abs(availableDelta) + EPSILON
                || Math.abs(consumedDelta) > EPSILON
                && Math.signum(consumedDelta) != Math.signum(availableDelta)) {
            throw new IllegalArgumentException(description
                    + " must be finite, retain the available direction, and not exceed its magnitude");
        }
    }

    /// Maps an in-flight target value from its previous pixel span to the current pixel span.
    private static double retargetScrollValue(
            double currentValue,
            double previousScrollablePixels,
            double currentScrollablePixels,
            double minValue,
            double maxValue
    ) {
        if (previousScrollablePixels <= EPSILON || currentScrollablePixels <= EPSILON || close(minValue, maxValue)) {
            return currentValue;
        }

        double targetPixels = pixelsForValue(currentValue, minValue, maxValue, previousScrollablePixels);
        return valueForPixels(clamp(targetPixels, 0.0, currentScrollablePixels), minValue, maxValue,
                currentScrollablePixels);
    }

    /// Returns whether an axis has a meaningful scroll range.
    private static boolean canScroll(double minValue, double maxValue, double scrollablePixels) {
        return scrollablePixels > EPSILON && !close(minValue, maxValue);
    }

    /// Converts a normalized scroll value to content pixels.
    private static double pixelsForValue(double value, double minValue, double maxValue, double scrollablePixels) {
        double clampedValue = clamp(value, minValue, maxValue);
        return (clampedValue - minValue) / (maxValue - minValue) * scrollablePixels;
    }

    /// Converts content pixels to a normalized scroll value.
    private static double valueForPixels(double pixels, double minValue, double maxValue, double scrollablePixels) {
        return minValue + pixels / scrollablePixels * (maxValue - minValue);
    }

    /// Returns a value clamped into the supplied range, resolving `NaN` to the minimum.
    private static double clamp(double value, double minValue, double maxValue) {
        if (Double.isNaN(value) || value <= minValue) {
            return minValue;
        }
        return Math.min(value, maxValue);
    }

    /// Returns whether two scroll values are effectively equal.
    private static boolean close(double first, double second) {
        return Math.abs(first - second) <= EPSILON;
    }

    /// Interpolates one scroll value and clips spatial overshoot to the configured axis range.
    static double interpolateScrollValue(
            double startValue,
            double targetValue,
            double fraction,
            double minValue,
            double maxValue
    ) {
        return clamp(interpolate(startValue, targetValue, fraction), minValue, maxValue);
    }

    /// Interpolates linearly between two scalar values.
    private static double interpolate(double start, double end, double fraction) {
        return start + (end - start) * fraction;
    }
}
