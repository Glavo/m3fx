// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.glavo.m3fx.internal.IdentityKey;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.animation.M3ScalarChannel;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/// Animates direct-child position changes produced by an existing JavaFX layout container.
///
/// This class applies a FLIP-style placement transition to any [Parent]. The parent continues to perform its normal
/// layout and each child reaches its target `layoutX` and `layoutY` immediately. A private [Translate] transform then
/// preserves the child's previous rendered position and animates to the new position without requesting layout on
/// every pulse. Existing transforms are retained and are never replaced.
///
/// A layout transition is inactive after construction. Call [#start()] after adding it to the desired lifecycle;
/// if the parent still needs its initial layout, that initial placement is captured without animation. Subsequent
/// moves can be interrupted or retargeted and physical springs preserve per-child velocity. Child additions,
/// removals, and size changes are observed for bookkeeping, but this class animates only position changes of nodes
/// that remain direct children. It does not animate entering, exiting, resizing, clipping, or moves between parents.
///
/// At most one active `M3LayoutTransition` may be installed on a parent. [#stop()] settles all children and detaches
/// listeners but permits a later [#start()]. [#dispose()] performs the same cleanup permanently. The transition
/// stores one state object per direct child and uses one shared JavaFX transition for every active child, avoiding a
/// separate pulse receiver per node and avoiding per-pulse allocation.
///
/// All lifecycle and property methods must be invoked on the JavaFX Application Thread once the parent is attached
/// to a showing scene. Effective reduced-motion settings are resolved from the parent for each run and are observed
/// while that run is active. An active placement run settles when the parent leaves the scene where the run started;
/// observation remains active until [#stop()] or [#dispose()] is called. Layout direction requires no special
/// configuration because actual JavaFX layout coordinates are animated.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3LayoutTransition {
    /// The parent property key that prevents multiple active transitions from owning child transforms.
    private static final IdentityKey ACTIVE_TRANSITION_KEY =
            new IdentityKey(M3LayoutTransition.class.getName() + ".activeTransition");

    /// The position delta below which a physical spring is visually settled, in logical pixels.
    private static final double POSITION_VISIBILITY_THRESHOLD = 5.0e-1;

    /// The parent whose direct children are observed.
    private final Parent parent;

    /// Mutable child states reused by the shared transition.
    private final ArrayList<ChildState> childStates = new ArrayList<>();

    /// The single finite transition that updates every active child.
    private final LayoutAnimation animation = new LayoutAnimation();

    /// Observes direct child additions and removals while this transition is active.
    private final ListChangeListener<Node> childrenListener = this::childrenChanged;

    /// Whether child deltas collected during one parent layout pass still need one shared retarget.
    private boolean retargetPending;

    /// Reusable deferred action that coalesces all remaining deltas before the next pulse.
    private final Runnable retargetFlusher = this::flushPendingRetarget;

    /// Arms newly added child states when the parent completes their initial layout.
    private final ChangeListener<Boolean> needsLayoutListener =
            (observable, neededLayout, needsLayout) -> {
                if (!needsLayout) {
                    armChildStates();
                }
            };

    /// Whether child placement is currently observed.
    private boolean active;

    /// Whether this transition has permanently released its lifecycle.
    private boolean disposed;

    /// The explicit placement motion specification, or `null` to inherit the default spatial role.
    private final ObjectProperty<@Nullable M3MotionSpec> motionSpec =
            new SimpleObjectProperty<>(this, "motionSpec") {
                /// Retargets an active run when its explicit specification changes.
                @Override
                protected void invalidated() {
                    if (animation.getStatus() == Animation.Status.RUNNING) {
                        animation.retarget(resolveMotionSpec());
                    }
                }
            };

    /// Returns the explicit motion specification used for child placement.
    ///
    /// @return the explicit specification, or `null` when the active theme's default spatial role is used
    public @Nullable M3MotionSpec getMotionSpec() {
        return motionSpec.get();
    }

    /// Sets the motion specification used for child placement.
    ///
    /// Passing `null` restores semantic resolution through the active theme. If a placement transition is running,
    /// it is retargeted from its current rendered positions with the new specification.
    ///
    /// @param motionSpec the explicit specification, or `null` to use the active theme
    public void setMotionSpec(@Nullable M3MotionSpec motionSpec) {
        this.motionSpec.set(motionSpec);
    }

    /// Returns the observable explicit motion-specification property.
    ///
    /// @return the motion-specification property, whose value may be `null`
    public ObjectProperty<@Nullable M3MotionSpec> motionSpecProperty() {
        return motionSpec;
    }

    /// Creates an inactive placement transition for a parent.
    ///
    /// Construction does not add listeners or transforms. Call [#start()] to begin observing position changes.
    ///
    /// @param parent the parent whose direct children will be animated
    /// @throws NullPointerException if `parent` is `null`
    public M3LayoutTransition(Parent parent) {
        this.parent = Objects.requireNonNull(parent, "parent");
    }

    /// Returns the parent whose direct children are animated.
    ///
    /// @return the parent supplied at construction time
    public Parent getParent() {
        return parent;
    }

    /// Returns whether this transition is observing child placement changes.
    ///
    /// @return `true` after [#start()] and before [#stop()] or [#dispose()]
    public boolean isActive() {
        return active;
    }

    /// Returns whether a placement animation is currently running.
    ///
    /// @return `true` if at least one observed child is moving toward its layout position
    public boolean isRunning() {
        return getStatus() == Animation.Status.RUNNING;
    }

    /// Returns the current placement animation status.
    ///
    /// @return the status of the shared finite animation
    public Animation.Status getStatus() {
        return animation.getStatus();
    }

    /// Returns the read-only placement animation-status property.
    ///
    /// @return the shared animation-status property
    public ReadOnlyObjectProperty<Animation.Status> statusProperty() {
        return animation.statusProperty();
    }

    /// Returns whether this transition has been permanently disposed.
    ///
    /// @return `true` after [#dispose()] has been called
    public boolean isDisposed() {
        return disposed;
    }

    /// Starts observing direct-child placement changes.
    ///
    /// Existing child coordinates become the initial baseline. If the parent currently needs layout, changes made
    /// by that pending layout pass are treated as baseline placement and are not animated. Repeated calls while this
    /// transition is active have no effect.
    ///
    /// @throws IllegalStateException if this transition has been disposed or another active layout transition owns
    ///                               the same parent
    public void start() {
        if (disposed) {
            throw new IllegalStateException("a disposed layout transition cannot be restarted");
        }
        if (active) {
            return;
        }

        @Nullable Object existing = parent.getProperties().get(ACTIVE_TRANSITION_KEY);
        if (existing != null && existing != this) {
            throw new IllegalStateException("the parent already has an active M3LayoutTransition");
        }

        parent.getProperties().put(ACTIVE_TRANSITION_KEY, this);
        active = true;
        boolean armed = !parent.isNeedsLayout();
        for (Node child : parent.getChildrenUnmodifiable()) {
            addChild(child, armed);
        }
        parent.getChildrenUnmodifiable().addListener(childrenListener);
        parent.needsLayoutProperty().addListener(needsLayoutListener);
    }

    /// Stops observing layout changes and settles every child at its current layout position.
    ///
    /// All listeners and private transforms installed by this object are removed before this method returns. The
    /// transition remains reusable and may be started again. Repeated calls while inactive have no effect.
    public void stop() {
        if (!active) {
            return;
        }

        animation.stop();
        retargetPending = false;
        parent.getChildrenUnmodifiable().removeListener(childrenListener);
        parent.needsLayoutProperty().removeListener(needsLayoutListener);
        for (int index = childStates.size() - 1; index >= 0; index--) {
            childStates.get(index).detach();
        }
        childStates.clear();
        parent.getProperties().remove(ACTIVE_TRANSITION_KEY, this);
        active = false;
    }

    /// Immediately settles an active run while continuing to observe future layout changes.
    ///
    /// This method has no effect when no placement animation is running.
    public void finish() {
        if (animation.getStatus() == Animation.Status.RUNNING) {
            M3Animation.finish(animation);
        }
    }

    /// Permanently stops this transition and releases installed listeners and transforms.
    ///
    /// Repeated calls are harmless. Calling [#start()] after disposal throws [IllegalStateException].
    public void dispose() {
        if (disposed) {
            return;
        }
        stop();
        disposed = true;
    }

    /// Applies one direct-child list change to the reusable state collection.
    private void childrenChanged(ListChangeListener.Change<? extends Node> change) {
        while (change.next()) {
            for (Node removed : change.getRemoved()) {
                removeChild(removed);
            }
            boolean armed = !parent.isNeedsLayout();
            for (Node added : change.getAddedSubList()) {
                addChild(added, armed);
            }
        }
    }

    /// Installs listeners and a private translation transform for one child.
    private void addChild(Node child, boolean armed) {
        ChildState state = new ChildState(child, armed);
        childStates.add(state);
    }

    /// Removes and detaches the state associated with one child.
    private void removeChild(Node child) {
        for (int index = 0; index < childStates.size(); index++) {
            ChildState state = childStates.get(index);
            if (state.node == child) {
                state.detach();
                childStates.remove(index);
                if (childStates.isEmpty()) {
                    animation.stop();
                }
                return;
            }
        }
    }

    /// Marks all current child states ready to animate subsequent coordinate changes.
    private void armChildStates() {
        for (ChildState state : childStates) {
            state.armed = true;
        }
    }

    /// Applies a horizontal layout-coordinate change to one child state.
    private void childLayoutXChanged(ChildState state, double oldValue, double newValue) {
        if (!state.armed || !Double.isFinite(oldValue) || !Double.isFinite(newValue)) {
            state.translation.setX(0.0);
            return;
        }
        double delta = oldValue - newValue;
        if (Double.compare(delta, 0.0) == 0) {
            return;
        }
        addParentDelta(state, delta, 0.0);
        requestRetarget();
    }

    /// Applies a vertical layout-coordinate change to one child state.
    private void childLayoutYChanged(ChildState state, double oldValue, double newValue) {
        if (!state.armed || !Double.isFinite(oldValue) || !Double.isFinite(newValue)) {
            state.translation.setY(0.0);
            return;
        }
        double delta = oldValue - newValue;
        if (Double.compare(delta, 0.0) == 0) {
            return;
        }
        addParentDelta(state, 0.0, delta);
        requestRetarget();
    }

    /// Starts one shared retarget and schedules at most one final retarget for the current event.
    private void requestRetarget() {
        if (retargetPending) {
            return;
        }
        retargetPending = true;
        animation.retarget(resolveMotionSpec());
        Platform.runLater(retargetFlusher);
    }

    /// Reconfigures the shared animation once after all changes from the current event have been collected.
    private void flushPendingRetarget() {
        if (!active || !retargetPending) {
            return;
        }
        retargetPending = false;
        animation.retarget(resolveMotionSpec());
    }

    /// Adds a parent-coordinate FLIP delta in the private transform's coordinate system.
    ///
    /// JavaFX applies the node-level scale and rotation properties after transform-list entries. The private
    /// translation is inserted so caller-owned transform-list entries remain inside it, leaving only those node-level
    /// transforms to invert here. A zero scale is non-invertible; its corresponding delta falls back to zero because
    /// no finite local translation can preserve a rendered position on that axis.
    private void addParentDelta(ChildState state, double deltaX, double deltaY) {
        Node node = state.node;
        double radians = Math.toRadians(node.getRotate());
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double unrotatedX = cosine * deltaX + sine * deltaY;
        double unrotatedY = -sine * deltaX + cosine * deltaY;
        double scaleX = node.getScaleX();
        double scaleY = node.getScaleY();
        double localX = Double.compare(scaleX, 0.0) == 0 ? 0.0 : unrotatedX / scaleX;
        double localY = Double.compare(scaleY, 0.0) == 0 ? 0.0 : unrotatedY / scaleY;
        state.translation.setX(state.translation.getX() + localX);
        state.translation.setY(state.translation.getY() + localY);
    }

    /// Resolves the explicit or theme-derived placement specification for a new run.
    private M3MotionSpec resolveMotionSpec() {
        @Nullable M3MotionSpec explicitSpec = getMotionSpec();
        return explicitSpec == null ? M3Animation.defaultSpatial(parent) : explicitSpec;
    }

    /// Stores reusable animation channels and listeners for one direct child.
    @NotNullByDefault
    private final class ChildState {
        /// The observed direct child.
        private final Node node;

        /// The private transform that preserves the previous rendered position.
        private final Translate translation = new Translate();

        /// The horizontal layout-coordinate listener.
        private final ChangeListener<Number> layoutXListener =
                (observable, oldValue, newValue) -> childLayoutXChanged(
                        this,
                        oldValue.doubleValue(),
                        newValue.doubleValue()
                );

        /// The vertical layout-coordinate listener.
        private final ChangeListener<Number> layoutYListener =
                (observable, oldValue, newValue) -> childLayoutYChanged(
                        this,
                        oldValue.doubleValue(),
                        newValue.doubleValue()
                );

        /// Whether coordinate changes should animate rather than establish an initial baseline.
        private boolean armed;

        /// The reusable horizontal translation channel.
        private final M3ScalarChannel horizontal = new M3ScalarChannel(POSITION_VISIBILITY_THRESHOLD);

        /// The reusable vertical translation channel.
        private final M3ScalarChannel vertical = new M3ScalarChannel(POSITION_VISIBILITY_THRESHOLD);

        /// Installs state for one child.
        private ChildState(Node node, boolean armed) {
            this.node = node;
            this.armed = armed;
            // The first list transform is applied after user list transforms, keeping the FLIP delta in parent axes.
            node.getTransforms().add(0, translation);
            node.layoutXProperty().addListener(layoutXListener);
            node.layoutYProperty().addListener(layoutYListener);
        }

        /// Removes all state owned by this layout transition from the child.
        private void detach() {
            node.layoutXProperty().removeListener(layoutXListener);
            node.layoutYProperty().removeListener(layoutYListener);
            node.getTransforms().remove(translation);
        }
    }

    /// Shared finite animation that advances every child translation without per-node pulse receivers.
    @NotNullByDefault
    private final class LayoutAnimation extends M3FiniteTransition {
        /// The duration of the longest child channel in the current run, in seconds.
        private double runDurationSeconds;

        /// Reconfigures the shared run from every child's current rendered position.
        private void retarget(M3MotionSpec spec) {
            M3MotionSpec checkedSpec = Objects.requireNonNull(spec, "spec");
            double elapsedSeconds = getStatus() == Animation.Status.RUNNING
                    ? Math.max(0.0, getCurrentTime().toSeconds())
                    : Double.POSITIVE_INFINITY;

            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < childStates.size(); index++) {
                ChildState state = childStates.get(index);
                state.horizontal.configure(state.translation.getX(), 0.0, checkedSpec, elapsedSeconds);
                state.vertical.configure(state.translation.getY(), 0.0, checkedSpec, elapsedSeconds);
            }

            stop();
            runDurationSeconds = 0.0;

            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < childStates.size(); index++) {
                ChildState state = childStates.get(index);
                runDurationSeconds = Math.max(
                        runDurationSeconds,
                        Math.max(
                                state.horizontal.getDurationSeconds(),
                                state.vertical.getDurationSeconds()
                        )
                );
            }

            if (runDurationSeconds <= 0.0) {
                interpolate(1.0);
                return;
            }
            setCycleDuration(Duration.seconds(runDurationSeconds));
            setInterpolator(Interpolator.LINEAR);
            M3Animation.playFromStart(parent, this);
        }

        /// Applies the current shared fraction to every child channel.
        @Override
        protected void interpolate(double fraction) {
            double elapsedSeconds = Math.max(0.0, fraction) * runDurationSeconds;
            // Avoid allocating an ArrayList iterator on every animation pulse.
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < childStates.size(); index++) {
                ChildState state = childStates.get(index);
                state.translation.setX(state.horizontal.valueAt(elapsedSeconds));
                state.translation.setY(state.vertical.valueAt(elapsedSeconds));
            }
        }
    }
}
