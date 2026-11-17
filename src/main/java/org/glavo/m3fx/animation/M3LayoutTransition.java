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
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;

/// Animates direct-child position changes produced by an existing JavaFX layout container.
///
/// This class applies a FLIP-style placement transition to any [Parent]. The parent continues to perform its normal
/// layout and each child reaches its target `layoutX` and `layoutY` immediately while its rendered position animates
/// to the new placement. Existing transforms are retained and are never replaced.
///
/// A layout transition is inactive after construction. Call [#start()] after adding it to the desired lifecycle;
/// if the parent still needs its initial layout, that initial placement is captured without animation. Subsequent
/// moves can be interrupted or retargeted and physical springs preserve per-child velocity. Child additions,
/// removals, and size changes are observed for bookkeeping, but this class animates only position changes of nodes
/// that remain direct children. It does not animate entering, exiting, resizing, clipping, or moves between parents.
///
/// At most one active `M3LayoutTransition` may be installed on a parent. [#stop()] settles all children and stops
/// observation but permits a later [#start()]. [#dispose()] performs the same cleanup permanently.
/// Transform-list listeners may modify the parent's children or invoke lifecycle methods. Child-list changes made
/// during installation or removal are reconciled after the callback returns and must eventually stabilize.
/// A new layout or lifecycle operation initiated by a translation-value callback supersedes the current update's
/// remaining writes. Removed children receive no further writes; retained children are traversed from a stable
/// membership snapshot.
///
/// All lifecycle and property methods must be invoked on the JavaFX Application Thread once the parent is attached
/// to a showing scene. Each run observes the application-wide reduced-motion request and, when the parent
/// implements [M3Animated], its animation setting. An active placement run settles when the parent leaves its adopted scene or window, or
/// when that window is hidden; observation remains active until [#stop()] or [#dispose()] is called. Layout direction
/// requires no special configuration because actual JavaFX layout coordinates are animated.
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

    /// Stable child membership reused by animation pulses and replaced when the parent's children change.
    private @Unmodifiable List<ChildState> childStates = List.of();

    /// Identifies the latest layout delta or explicit completion request across axis-value callbacks.
    private long layoutVersion;

    /// The single finite transition that updates every active child.
    private final LayoutAnimation animation = new LayoutAnimation();

    /// Observes direct child additions and removals while this transition is active.
    private final ListChangeListener<Node> childrenListener = change -> synchronizeChildren();

    /// Whether child ownership is currently being reconciled with a snapshot of the parent.
    private boolean synchronizingChildren;

    /// Whether a callback changed child ownership during the current reconciliation pass.
    private boolean childrenSyncRequested;

    /// Whether an installation is releasing its listeners and transforms.
    private boolean stopping;

    /// Whether a cleanup callback requested installation after the current cleanup completes.
    private boolean restartRequested;

    /// Whether child deltas collected during one parent layout pass still need one shared retarget.
    private boolean retargetPending;

    /// Whether another child delta arrived after the immediate retarget for the current event.
    private boolean retargetDirty;

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
                        animation.retarget();
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
    /// transition is active have no effect. A call from a cleanup callback is deferred until cleanup completes;
    /// a subsequent [#stop()] or [#dispose()] cancels that pending restart.
    ///
    /// @throws IllegalStateException if this transition has been disposed or another layout transition owns
    ///                               the same parent
    public void start() {
        if (disposed) {
            throw new IllegalStateException("a disposed layout transition cannot be restarted");
        }
        if (stopping) {
            restartRequested = true;
            return;
        }
        if (active) {
            return;
        }

        @Nullable Object existing = parent.getProperties().get(ACTIVE_TRANSITION_KEY);
        if (existing != null && existing != this) {
            throw new IllegalStateException("the parent already has an active M3LayoutTransition");
        }

        active = true;
        parent.getChildrenUnmodifiable().addListener(childrenListener);
        parent.needsLayoutProperty().addListener(needsLayoutListener);
        parent.getProperties().put(ACTIVE_TRANSITION_KEY, this);
        synchronizeChildren();
    }

    /// Stops observing layout changes and settles every child at its current layout position.
    ///
    /// The transition becomes inactive and releases coordinate listeners before invoking cleanup callbacks. It
    /// remains reusable and may be started again. Repeated calls while inactive have no effect, except that a call
    /// during cleanup cancels a pending restart requested by an earlier callback.
    public void stop() {
        if (stopping) {
            restartRequested = false;
            return;
        }
        if (!active) {
            return;
        }

        stopping = true;
        active = false;
        retargetPending = false;
        retargetDirty = false;
        parent.getChildrenUnmodifiable().removeListener(childrenListener);
        parent.needsLayoutProperty().removeListener(needsLayoutListener);
        @Unmodifiable List<ChildState> retired = childStates;
        childStates = List.of();
        for (ChildState state : retired) {
            state.stopObserving();
        }
        try {
            animation.stop();
            parent.getProperties().remove(ACTIVE_TRANSITION_KEY, this);
            for (ChildState state : retired) {
                state.detach();
            }
        } finally {
            stopping = false;
        }
        if (restartRequested) {
            restartRequested = false;
            start();
        }
    }

    /// Immediately settles an active run while continuing to observe future layout changes.
    ///
    /// This method has no effect when no placement animation is running.
    public void finish() {
        if (animation.getStatus() == Animation.Status.RUNNING) {
            layoutVersion++;
            M3Animation.finish(animation);
        }
    }

    /// Permanently stops this transition and releases installed listeners and transforms.
    ///
    /// Repeated calls are harmless. Disposal is visible to cleanup callbacks, and calling [#start()] from such a
    /// callback or after disposal throws [IllegalStateException].
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        stop();
    }

    /// Reconciles registered child states, coalescing changes made by transform-list callbacks.
    private void synchronizeChildren() {
        if (!active) {
            return;
        }
        childrenSyncRequested = true;
        if (synchronizingChildren) {
            return;
        }
        synchronizingChildren = true;
        try {
            do {
                childrenSyncRequested = false;
                @Unmodifiable List<Node> children = List.copyOf(parent.getChildrenUnmodifiable());
                @Unmodifiable List<ChildState> previous = childStates;
                IdentityHashMap<Node, ChildState> remaining = new IdentityHashMap<>();
                for (ChildState state : previous) {
                    remaining.put(state.node, state);
                }
                List<ChildState> next = new ArrayList<>(children.size());
                for (Node child : children) {
                    @Nullable ChildState retained = remaining.remove(child);
                    next.add(retained != null ? retained : new ChildState(child));
                }
                childStates = List.copyOf(next);
                // Publish membership before callbacks, then retire every state excluded from that snapshot.
                for (ChildState state : previous) {
                    if (remaining.containsKey(state.node)) {
                        state.detach();
                    }
                }
                if (!active || childrenSyncRequested) {
                    continue;
                }
                for (ChildState state : childStates) {
                    if (!state.attached) {
                        state.install();
                        if (!active || childrenSyncRequested) {
                            break;
                        }
                    }
                }
                if (active && childStates.isEmpty()) {
                    animation.stop();
                }
            } while (active && childrenSyncRequested);
        } finally {
            synchronizingChildren = false;
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
        if (!active || !state.attached) {
            return;
        }
        if (!state.armed || !Double.isFinite(oldValue) || !Double.isFinite(newValue)) {
            state.translation.setX(0.0);
            return;
        }
        double delta = oldValue - newValue;
        if (Double.compare(delta, 0.0) == 0) {
            return;
        }
        addParentDelta(state, delta, 0.0);
        if (active && state.attached) {
            requestRetarget();
        }
    }

    /// Applies a vertical layout-coordinate change to one child state.
    private void childLayoutYChanged(ChildState state, double oldValue, double newValue) {
        if (!active || !state.attached) {
            return;
        }
        if (!state.armed || !Double.isFinite(oldValue) || !Double.isFinite(newValue)) {
            state.translation.setY(0.0);
            return;
        }
        double delta = oldValue - newValue;
        if (Double.compare(delta, 0.0) == 0) {
            return;
        }
        addParentDelta(state, 0.0, delta);
        if (active && state.attached) {
            requestRetarget();
        }
    }

    /// Starts one shared retarget and schedules at most one final retarget for the current event.
    private void requestRetarget() {
        if (retargetPending) {
            retargetDirty = true;
            return;
        }
        retargetPending = true;
        retargetDirty = false;
        animation.retarget();
        Platform.runLater(retargetFlusher);
    }

    /// Reconfigures the shared animation only when more deltas followed the immediate retarget in this event.
    private void flushPendingRetarget() {
        if (!retargetPending) {
            return;
        }
        boolean shouldRetarget = active && retargetDirty;
        retargetPending = false;
        retargetDirty = false;
        if (shouldRetarget) {
            animation.retarget();
        }
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
        long version = ++layoutVersion;
        state.translation.setX(state.translation.getX() + localX);
        if (active && state.attached && version == layoutVersion) {
            state.translation.setY(state.translation.getY() + localY);
        }
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

        /// Whether coordinate listeners belong to the current installation.
        private boolean attached;

        /// The reusable horizontal translation channel.
        private final M3ScalarChannel horizontal = new M3ScalarChannel(POSITION_VISIBILITY_THRESHOLD);

        /// The reusable vertical translation channel.
        private final M3ScalarChannel vertical = new M3ScalarChannel(POSITION_VISIBILITY_THRESHOLD);

        /// Creates unpublished state for one child.
        private ChildState(Node node) {
            this.node = node;
        }

        /// Installs listeners before publishing the transform, after this state has been registered.
        private void install() {
            armed = !parent.isNeedsLayout();
            attached = true;
            node.layoutXProperty().addListener(layoutXListener);
            node.layoutYProperty().addListener(layoutYListener);
            // The first list transform is applied after user list transforms, keeping the FLIP delta in parent axes.
            node.getTransforms().add(0, translation);
        }

        /// Releases coordinate observation before cleanup invokes transform-list listeners.
        private void stopObserving() {
            if (!attached) {
                return;
            }
            attached = false;
            node.layoutXProperty().removeListener(layoutXListener);
            node.layoutYProperty().removeListener(layoutYListener);
        }

        /// Removes all state owned by this layout transition from the child.
        private void detach() {
            stopObserving();
            node.getTransforms().remove(translation);
        }
    }

    /// Shared finite animation that advances every child translation without per-node pulse receivers.
    @NotNullByDefault
    private final class LayoutAnimation extends M3FiniteTransition {
        /// The duration of the longest child channel in the current run, in seconds.
        private double runDurationSeconds;

        /// Reconfigures current children using the specification visible after stopped-status callbacks complete.
        private void retarget() {
            double elapsedSeconds = getStatus() == Animation.Status.RUNNING
                    ? Math.max(0.0, getCurrentTime().toSeconds())
                    : Double.POSITIVE_INFINITY;
            long version = getStopVersion() + 1;
            stop();
            if (!active || version != getStopVersion()) {
                return;
            }
            @Nullable M3MotionSpec explicitSpec = getMotionSpec();
            M3MotionSpec spec = explicitSpec == null ? M3Animation.defaultSpatial(parent) : explicitSpec;
            runDurationSeconds = 0.0;
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < childStates.size(); index++) {
                ChildState state = childStates.get(index);
                state.horizontal.configure(state.translation.getX(), 0.0, spec, elapsedSeconds);
                state.vertical.configure(state.translation.getY(), 0.0, spec, elapsedSeconds);
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
            long version = layoutVersion;
            long stopVersion = getStopVersion();
            @Unmodifiable List<ChildState> states = childStates;
            double elapsedSeconds = Math.max(0.0, fraction) * runDurationSeconds;
            // Keep membership stable across callbacks without allocating a snapshot or iterator on each pulse.
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < states.size(); index++) {
                ChildState state = states.get(index);
                if (!state.attached) {
                    continue;
                }
                state.translation.setX(state.horizontal.valueAt(elapsedSeconds));
                if (!active || version != layoutVersion || stopVersion != getStopVersion()) {
                    return;
                }
                if (state.attached) {
                    state.translation.setY(state.vertical.valueAt(elapsedSeconds));
                    if (!active || version != layoutVersion || stopVersion != getStopVersion()) {
                        return;
                    }
                }
            }
        }
    }
}
