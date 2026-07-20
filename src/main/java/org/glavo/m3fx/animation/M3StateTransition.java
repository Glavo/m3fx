// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.util.Duration;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.animation.M3ScalarChannel;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/// Coordinates multiple writable double properties from one typed state transition.
///
/// A state transition retains a [current state][#currentStateProperty()] and a writable
/// [target state][#targetStateProperty()]. Each property registered through
/// [#addDouble(DoubleProperty, ToDoubleFunction, double)] derives its target value from that state. Changing the
/// target starts one shared finite animation that advances every registered property. The current state remains the
/// last settled state until all channels finish; the target state changes before animation begins. A transition
/// without registered channels settles synchronously.
///
/// Registering related visual values on one object keeps their start and completion boundaries synchronized and
/// uses one JavaFX pulse receiver regardless of channel count. No key frames or per-pulse collections are allocated.
/// Physical spring channels preserve their velocity when the target state or motion specification changes during a
/// run. Duration-based channels restart from their currently rendered values.
///
/// The owner supplies the effective [motion setting][M3MotionSettings] and default spatial motion role. If reduced
/// motion is requested before or during a run, all channels settle synchronously. A run adopts the owner's first
/// scene and presenting window, and settles if the owner leaves that presentation context or the window is hidden.
///
/// Registered properties must remain writable and finite while a run is active. A caller may observe or bind from
/// them, but must not bind or independently write them until the run has stopped. Target-value functions must be
/// deterministic functions of their state argument and must return finite values. They are evaluated when a target
/// is requested, not on every animation pulse. All methods that mutate channels or animation state must be invoked
/// on the JavaFX Application Thread once the owner is attached to a showing scene.
///
/// This API provides the retained JavaFX counterpart of a type-safe Compose `Transition`: callers continue to own
/// the animated JavaFX properties and scene-graph nodes rather than providing a recomposition callback.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
///
/// @param <S> the non-null state type
@NotNullByDefault
public final class M3StateTransition<S> {
    /// The node whose effective motion setting controls animation playback.
    private final Node owner;

    /// The registered channels, retained and reused across runs.
    private final ArrayList<DoubleChannel<S>> channels = new ArrayList<>();

    /// The single finite transition that advances every registered channel.
    private final StateAnimation animation = new StateAnimation();

    /// The most recently accepted non-null target state.
    private S acceptedTargetState;

    /// Prevents an internally synchronized target assignment from starting a second run.
    private boolean targetMutationSuppressed;

    /// The state toward which registered properties are moving.
    private final ObjectProperty<S> targetState;

    /// Returns the state toward which registered properties are moving.
    ///
    /// @return the non-null target state
    public S getTargetState() {
        return targetState.get();
    }

    /// Changes the state toward which all registered properties move.
    ///
    /// Target functions are evaluated and validated before a run starts. If `state` is equal to the current target,
    /// this method has no effect. Otherwise, an active run is retargeted from its currently rendered values. If no
    /// channels are registered or effective motion is disabled, the transition settles synchronously.
    ///
    /// @param state the non-null target state
    /// @throws NullPointerException     if `state` is `null`
    /// @throws IllegalArgumentException if a target function returns a non-finite value or a registered property
    ///                                  currently contains a non-finite value
    /// @throws IllegalStateException    if the target-state property or a registered property is bound
    public void setTargetState(S state) {
        S checkedState = Objects.requireNonNull(state, "state");
        if (Objects.equals(checkedState, getTargetState())) {
            return;
        }
        requireWritableTargetState();
        prepareTargets(checkedState);
        targetState.set(checkedState);
    }

    /// Returns the writable target-state property.
    ///
    /// Binding this property is permitted. A binding must never produce `null`, and every produced state must obey
    /// the same target-function and property constraints as [#setTargetState(Object)]. An invalid value delivered by
    /// a binding is rejected with the corresponding runtime exception, but the source binding remains responsible
    /// for replacing that value.
    ///
    /// @return the target-state property
    public ObjectProperty<S> targetStateProperty() {
        return targetState;
    }

    /// The explicit shared motion specification, or `null` to resolve the default spatial role from the owner.
    private final ObjectProperty<@Nullable M3MotionSpec> motionSpec =
            new SimpleObjectProperty<>(this, "motionSpec") {
                /// Retargets an active run when its explicit specification changes.
                @Override
                protected void invalidated() {
                    if (animation.getStatus() == Animation.Status.RUNNING) {
                        prepareTargets(getTargetState());
                        animation.retarget(resolveMotionSpec());
                    }
                }
            };

    /// Returns the explicit motion specification shared by registered channels.
    ///
    /// @return the explicit specification, or `null` when the owner's default spatial role is used
    public @Nullable M3MotionSpec getMotionSpec() {
        return motionSpec.get();
    }

    /// Sets the explicit motion specification shared by registered channels.
    ///
    /// Passing `null` restores semantic resolution through the owner's active theme. Changing this value during a
    /// run retargets every channel from its currently rendered value and preserves spring velocity.
    ///
    /// @param motionSpec the explicit specification, or `null` to use the default spatial role
    /// @throws IllegalArgumentException if a registered property contains a non-finite value or its target function
    ///                                  returns a non-finite value while a run is active
    /// @throws IllegalStateException    if a registered property is bound while a run is active
    public void setMotionSpec(@Nullable M3MotionSpec motionSpec) {
        this.motionSpec.set(motionSpec);
    }

    /// Returns the observable explicit motion-specification property.
    ///
    /// @return the motion-specification property, whose value may be `null`
    public ObjectProperty<@Nullable M3MotionSpec> motionSpecProperty() {
        return motionSpec;
    }

    /// Creates a transition with an initial settled state.
    ///
    /// The initial state is installed as both the current and target state. Properties registered while the
    /// transition is idle are immediately initialized from this state.
    ///
    /// @param owner        the node whose effective motion setting and theme control each run
    /// @param initialState the non-null initial settled state
    /// @throws NullPointerException if `owner` or `initialState` is `null`
    public M3StateTransition(Node owner, S initialState) {
        this.owner = Objects.requireNonNull(owner, "owner");
        S checkedState = Objects.requireNonNull(initialState, "initialState");
        currentState = new ReadOnlyObjectWrapper<>(this, "currentState", checkedState);
        acceptedTargetState = checkedState;
        targetState = new SimpleObjectProperty<>(this, "targetState", checkedState) {
            /// Starts a coordinated run after direct property mutation or binding invalidation.
            @Override
            protected void invalidated() {
                if (!targetMutationSuppressed) {
                    targetStateInvalidated();
                }
            }
        };
        animation.setOnFinished(event -> completeRun());
    }

    /// Returns the node whose effective motion setting controls animation playback.
    ///
    /// @return the owner supplied at construction time
    public Node getOwner() {
        return owner;
    }

    /// The state at which all registered properties most recently settled.
    private final ReadOnlyObjectWrapper<S> currentState;

    /// Returns the state at which every registered property most recently settled.
    ///
    /// During a run this value remains the preceding settled state. It becomes the target state only after all
    /// channels reach their exact target values or are settled by [#finish()] or [#snapTo(Object)].
    ///
    /// @return the non-null current state
    public S getCurrentState() {
        return currentState.get();
    }

    /// Returns the read-only current-state property.
    ///
    /// @return the current-state property
    public ReadOnlyObjectProperty<S> currentStateProperty() {
        return currentState.getReadOnlyProperty();
    }

    /// Returns the current shared animation status.
    ///
    /// @return the status of the underlying finite animation
    public Animation.Status getStatus() {
        return animation.getStatus();
    }

    /// Returns the read-only shared animation-status property.
    ///
    /// @return the animation-status property
    public ReadOnlyObjectProperty<Animation.Status> statusProperty() {
        return animation.statusProperty();
    }

    /// Returns whether any registered channel is currently moving toward the target state.
    ///
    /// @return `true` if the status is [Animation.Status#RUNNING]
    public boolean isRunning() {
        return getStatus() == Animation.Status.RUNNING;
    }

    /// Registers one writable double property as a state-derived animation channel.
    ///
    /// The target function is evaluated immediately for the current target state. If the transition is idle, the
    /// property is set to that target synchronously. If a run is active, all channels are retargeted together and
    /// this property starts from its current value. A property may be registered only once by identity.
    ///
    /// The visibility threshold is expressed in the property's units and determines when physical springs are
    /// considered settled. It does not round, clamp, or otherwise alter the exact final value.
    ///
    /// @param value               the writable property updated by animation pulses
    /// @param targetValue         the deterministic function mapping a state to a finite property value
    /// @param visibilityThreshold the finite, positive spring visibility threshold in property units
    /// @throws NullPointerException     if `value` or `targetValue` is `null`
    /// @throws IllegalArgumentException if the property is already registered, the threshold is invalid, the
    ///                                  property's current value is non-finite, or the target function returns a
    ///                                  non-finite value
    /// @throws IllegalStateException    if `value` is bound
    public void addDouble(
            DoubleProperty value,
            ToDoubleFunction<? super S> targetValue,
            double visibilityThreshold
    ) {
        DoubleProperty checkedValue = Objects.requireNonNull(value, "value");
        ToDoubleFunction<? super S> checkedTargetValue = Objects.requireNonNull(targetValue, "targetValue");
        requireUnregistered(checkedValue);
        requireWritable(checkedValue);
        requireFinite(checkedValue.get(), "property value");

        DoubleChannel<S> channel = new DoubleChannel<>(
                checkedValue,
                checkedTargetValue,
                visibilityThreshold
        );
        channel.pendingTarget = requireFinite(
                checkedTargetValue.applyAsDouble(getTargetState()),
                "target function result"
        );

        if (animation.getStatus() == Animation.Status.RUNNING) {
            prepareTargets(getTargetState());
            channels.add(channel);
            animation.retarget(resolveMotionSpec());
        } else {
            channels.add(channel);
            checkedValue.set(channel.pendingTarget);
            channel.scalar.reset(channel.pendingTarget);
        }
    }

    /// Removes the channel associated with a writable double property.
    ///
    /// The removed property retains its current value and is not modified again. Remaining channels are retargeted
    /// from their current values. If the last channel is removed, the typed state settles synchronously without
    /// changing the removed property's value.
    ///
    /// @param value the property whose channel should be removed
    /// @return `true` if an identity-equal registered property was removed; otherwise `false`
    /// @throws NullPointerException  if `value` is `null`
    /// @throws IllegalStateException if another registered property is bound while an active run must be retargeted
    public boolean removeDouble(DoubleProperty value) {
        DoubleProperty checkedValue = Objects.requireNonNull(value, "value");
        for (int index = 0; index < channels.size(); index++) {
            if (channels.get(index).value == checkedValue) {
                boolean running = animation.getStatus() == Animation.Status.RUNNING;
                if (running) {
                    prepareTargets(getTargetState());
                }
                channels.remove(index);
                if (running) {
                    if (channels.isEmpty()) {
                        animation.stop();
                        currentState.set(getTargetState());
                    } else {
                        animation.retarget(resolveMotionSpec());
                    }
                }
                return true;
            }
        }
        return false;
    }

    /// Removes every registered channel.
    ///
    /// Registered properties retain their current values. If a run is active, it stops and the current state becomes
    /// the target state because no remaining property can be in transition. Repeated calls when no channels are
    /// registered have no effect.
    public void clearChannels() {
        if (channels.isEmpty()) {
            return;
        }
        channels.clear();
        if (animation.getStatus() == Animation.Status.RUNNING) {
            animation.stop();
            currentState.set(getTargetState());
        }
    }

    /// Completes the current run at its target values.
    ///
    /// Completion is synchronous. If no run is active, this method reapplies the state-derived target values and
    /// clears retained channel velocity. Repeated calls are therefore idempotent when target functions are stable.
    ///
    /// @throws IllegalArgumentException if a property or target function produces a non-finite value
    /// @throws IllegalStateException    if a registered property is bound
    public void finish() {
        requireWritableChannels();
        if (animation.getStatus() == Animation.Status.RUNNING) {
            M3Animation.finish(animation);
            return;
        }

        prepareTargets(getTargetState());
        applyPreparedTargets();
        completeRun();
    }

    /// Stops any active run and installs one state and all of its derived values synchronously.
    ///
    /// The supplied state becomes both the current and target state. Target functions are evaluated before the
    /// animation or target-state property is changed, so a failed evaluation leaves the running transition intact.
    ///
    /// @param state the non-null state to install
    /// @throws NullPointerException     if `state` is `null`
    /// @throws IllegalArgumentException if a target function returns a non-finite value or a property contains a
    ///                                  non-finite value
    /// @throws IllegalStateException    if the target-state property or a registered property is bound
    public void snapTo(S state) {
        S checkedState = Objects.requireNonNull(state, "state");
        requireWritableTargetState();
        prepareTargets(checkedState);
        animation.stop();
        setTargetStateInternally(checkedState);
        acceptedTargetState = checkedState;
        applyPreparedTargets();
        currentState.set(checkedState);
    }

    /// Handles a target-state property invalidation from direct mutation or binding.
    private void targetStateInvalidated() {
        @Nullable S requestedState = targetState.get();
        try {
            S checkedState = Objects.requireNonNull(requestedState, "targetState");
            if (Objects.equals(checkedState, acceptedTargetState)) {
                acceptedTargetState = checkedState;
                if (animation.getStatus() != Animation.Status.RUNNING) {
                    currentState.set(checkedState);
                }
                return;
            }
            prepareTargets(checkedState);
            acceptedTargetState = checkedState;
            startPreparedRun();
        } catch (RuntimeException exception) {
            if (!targetState.isBound()) {
                setTargetStateInternally(acceptedTargetState);
            }
            throw exception;
        }
    }

    /// Evaluates and validates all channel targets without starting a run.
    private void prepareTargets(S state) {
        Objects.requireNonNull(state, "state");
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            DoubleChannel<S> channel = channels.get(index);
            requireWritable(channel.value);
            requireFinite(channel.value.get(), "property value");
            channel.pendingTarget = requireFinite(
                    channel.targetValue.applyAsDouble(state),
                    "target function result"
            );
        }
    }

    /// Starts a coordinated run toward targets prepared for the current target state.
    private void startPreparedRun() {
        if (channels.isEmpty()) {
            animation.stop();
            currentState.set(getTargetState());
            return;
        }
        animation.retarget(resolveMotionSpec());
    }

    /// Applies all prepared targets and clears each channel's retained velocity.
    private void applyPreparedTargets() {
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            DoubleChannel<S> channel = channels.get(index);
            channel.value.set(channel.pendingTarget);
            channel.scalar.reset(channel.pendingTarget);
        }
    }

    /// Publishes completion after all properties have reached exact target values.
    private void completeRun() {
        currentState.set(getTargetState());
    }

    /// Resolves the explicit or theme-derived shared motion specification.
    private M3MotionSpec resolveMotionSpec() {
        @Nullable M3MotionSpec explicit = getMotionSpec();
        return explicit == null ? M3Animation.defaultSpatial(owner) : explicit;
    }

    /// Assigns the target property without recursively starting a run.
    private void setTargetStateInternally(S state) {
        targetMutationSuppressed = true;
        try {
            targetState.set(state);
        } finally {
            targetMutationSuppressed = false;
        }
    }

    /// Verifies that a property is not already registered by identity.
    private void requireUnregistered(DoubleProperty value) {
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            if (channels.get(index).value == value) {
                throw new IllegalArgumentException("value property is already registered");
            }
        }
    }

    /// Verifies that a property may be written by an animation pulse.
    private static void requireWritable(DoubleProperty value) {
        if (value.isBound()) {
            throw new IllegalStateException("registered value properties must not be bound");
        }
    }

    /// Verifies that a state-changing method may assign the target property.
    private void requireWritableTargetState() {
        if (targetState.isBound()) {
            throw new IllegalStateException("targetState property must not be bound");
        }
    }

    /// Returns a finite value or throws for a broken channel contract.
    private static double requireFinite(double value, String description) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(description + " must be finite");
        }
        return value;
    }

    /// Verifies that every registered property may be written.
    private void requireWritableChannels() {
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            requireWritable(channels.get(index).value);
        }
    }

    /// Stores one property, state mapping, pending target, and reusable scalar channel.
    @NotNullByDefault
    private static final class DoubleChannel<S> {
        /// The writable property updated by animation pulses.
        private final DoubleProperty value;

        /// The state-to-target mapping evaluated when a target is requested.
        private final ToDoubleFunction<? super S> targetValue;

        /// The reusable interpolation and velocity state.
        private final M3ScalarChannel scalar;

        /// The target computed before the next run starts.
        private double pendingTarget;

        /// Creates one property channel.
        private DoubleChannel(
                DoubleProperty value,
                ToDoubleFunction<? super S> targetValue,
                double visibilityThreshold
        ) {
            this.value = value;
            this.targetValue = targetValue;
            scalar = new M3ScalarChannel(visibilityThreshold);
        }
    }

    /// Shared finite animation that advances every registered scalar channel.
    @NotNullByDefault
    private final class StateAnimation extends M3FiniteTransition {
        /// The duration of the longest active channel, in seconds.
        private double runDurationSeconds;

        /// Reconfigures every channel from its currently rendered value.
        private void retarget(M3MotionSpec motionSpec) {
            M3MotionSpec checkedSpec = Objects.requireNonNull(motionSpec, "motionSpec");
            double elapsedSeconds = getStatus() == Animation.Status.RUNNING
                    ? Math.max(0.0, getCurrentTime().toSeconds())
                    : Double.POSITIVE_INFINITY;

            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < channels.size(); index++) {
                DoubleChannel<S> channel = channels.get(index);
                channel.scalar.configure(
                        channel.value.get(),
                        channel.pendingTarget,
                        checkedSpec,
                        elapsedSeconds
                );
            }

            stop();
            runDurationSeconds = 0.0;
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < channels.size(); index++) {
                runDurationSeconds = Math.max(
                        runDurationSeconds,
                        channels.get(index).scalar.getDurationSeconds()
                );
            }

            if (runDurationSeconds <= 0.0) {
                setCycleDuration(Duration.ZERO);
                M3Animation.finish(this);
                return;
            }
            setCycleDuration(Duration.seconds(runDurationSeconds));
            setInterpolator(Interpolator.LINEAR);
            M3Animation.playFromStart(owner, this);
        }

        /// Applies one shared elapsed time to every registered channel.
        @Override
        protected void interpolate(double fraction) {
            double elapsedSeconds = Math.max(0.0, fraction) * runDurationSeconds;
            // Avoid allocating an ArrayList iterator on every animation pulse.
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < channels.size(); index++) {
                DoubleChannel<S> channel = channels.get(index);
                channel.value.set(channel.scalar.valueAt(elapsedSeconds));
            }
        }
    }
}
