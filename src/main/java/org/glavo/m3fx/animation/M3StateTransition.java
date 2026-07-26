// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleFunction;

/// Coordinates multiple writable properties from one typed, seekable state transition.
///
/// A state transition retains a [current state][#currentStateProperty()] and a writable
/// [target state][#targetStateProperty()]. Each registered property derives its target value from that state.
/// [#addDouble(DoubleProperty, ToDoubleFunction, double)] registers a scalar channel, while
/// [#addValue(Property, Function, M3VectorConverter)] coordinates immutable multi-component values such as colors,
/// points, dimensions, and insets. Changing the target starts one animation that advances all registered channels
/// from the same play time. A channel may use the shared motion specification or select one from the active source
/// and target states.
///
/// The transition may also be driven directly with [#seekTo(Object, double)]. A seek fraction represents normalized
/// play time across the complete transition; each channel still evaluates its own easing or physical spring at that
/// time. [#animateToTarget()] continues from the sought position. Automatic playback and seeking expose their
/// normalized position through [#progressProperty()].
///
/// Registering related visual values on one object keeps their start and completion boundaries synchronized.
/// Physical spring channels preserve their velocity when an automatic run is interrupted or retargeted.
/// Duration-based channels restart from their currently rendered values.
///
/// The owner supplies the effective [motion setting][M3MotionSettings] and default spatial motion role. Reduced
/// motion settles automatic playback synchronously. Explicit seeking remains available because it represents direct
/// manipulation rather than autonomous motion. An automatic run adopts the owner's first scene and presenting
/// window, and settles if the owner leaves that presentation context or the window is hidden.
///
/// Registered properties must remain writable and non-null where applicable while controlled by this transition.
/// A caller may observe or bind from them, but must not bind or independently write them until playback or seeking
/// has ended. Target-value functions and converters must be deterministic and must obey their documented finite-value
/// contracts. They are evaluated when a target is requested, not on every animation pulse. Once the owner is attached
/// to a showing scene, all state-changing methods must be invoked on the JavaFX Application Thread.
///
/// This API serves the same state-driven role as a Compose `Transition` and `SeekableTransitionState`, while callers
/// supply writable JavaFX properties and scene-graph nodes.
///
/// See [Compose value-based animations](https://developer.android.com/develop/ui/compose/animation/value-based)
/// and [Material Design motion](https://m3.material.io/styles/motion/overview).
///
/// @param <S> the non-null state type
@NotNullByDefault
public final class M3StateTransition<S> {
    /// The node whose effective motion setting controls automatic playback.
    private final Node owner;

    /// The registered channels, retained and reused across runs.
    private final ArrayList<Channel<S>> channels = new ArrayList<>();

    /// Reusable storage that makes channel-specification selection atomic without per-run collection allocation.
    private @Nullable M3MotionSpec[] motionSpecBuffer = new M3MotionSpec[0];

    /// The single finite transition that advances every registered channel.
    private final StateAnimation animation = new StateAnimation();

    /// The most recently accepted non-null target state.
    private S acceptedTargetState;

    /// The source state used to resolve channel-specific specifications for the active transition.
    private S activeInitialState;

    /// The destination state used to resolve channel-specific specifications for the active transition.
    private S activeTargetState;

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
    /// Target functions are evaluated and validated before the target property changes. If `state` differs from the
    /// current target, an active automatic run or seek is retargeted from its currently rendered values. If `state`
    /// equals the target of an active seek, this method continues automatic playback from the sought position. If no
    /// channels are registered or effective motion is disabled, the transition settles synchronously.
    ///
    /// @param state the non-null target state
    /// @throws NullPointerException     if `state` or a selected channel specification is `null`
    /// @throws IllegalArgumentException if a target function, converter, or registered property produces an invalid
    ///                                  component
    /// @throws IllegalStateException    if the target-state property or a registered property is bound
    public void setTargetState(S state) {
        S checkedState = Objects.requireNonNull(state, "state");
        if (Objects.equals(checkedState, getTargetState())) {
            if (isSeeking()) {
                animateToTarget();
            }
            return;
        }
        requireWritableTargetState();
        S initialState = getTargetState();
        stageTargets(checkedState);
        prepareMotionSpecs(resolveMotionSpec(), initialState, checkedState);
        commitStagedTargets();
        setTargetStateInternally(checkedState);
        acceptedTargetState = checkedState;
        startPreparedRun(initialState, checkedState);
    }

    /// Returns the writable target-state property.
    ///
    /// Binding this property is permitted. A binding must never produce `null`, and every produced state must obey
    /// the same target-function and property constraints as [#setTargetState(Object)]. Direct property mutation
    /// always starts automatic playback; use [#seekTo(Object, double)] for externally driven progress.
    ///
    /// @return the target-state property
    public ObjectProperty<S> targetStateProperty() {
        return targetState;
    }

    /// The explicit default motion specification, or `null` to resolve the default spatial role from the owner.
    private final ObjectProperty<@Nullable M3MotionSpec> motionSpec =
            new SimpleObjectProperty<>(this, "motionSpec") {
                /// Reconfigures active automatic or seekable motion after specification changes.
                @Override
                protected void invalidated() {
                    if (animation.getStatus() == Animation.Status.RUNNING) {
                        prepareTargets(getTargetState());
                        refreshDefaultMotionSpecs(resolveMotionSpec());
                        animation.retarget();
                    } else if (isSeeking()) {
                        prepareTargets(getTargetState());
                        refreshDefaultMotionSpecs(resolveMotionSpec());
                        animation.reconfigureSeek(getProgress());
                        settleIfMotionlessSeek();
                    }
                }
            };

    /// Returns the explicit default motion specification for registered channels.
    ///
    /// @return the explicit specification, or `null` when the owner's default spatial role is used
    public @Nullable M3MotionSpec getMotionSpec() {
        return motionSpec.get();
    }

    /// Sets the explicit default motion specification for registered channels.
    ///
    /// Passing `null` restores semantic resolution through the owner's active theme. Changing this value during an
    /// automatic run retargets every channel from its currently rendered value and preserves spring velocity.
    /// Changing it during a seek recomputes the same sought fraction from the originally captured start values.
    ///
    /// @param motionSpec the explicit specification, or `null` to use the default spatial role
    /// @throws IllegalArgumentException if a registered property or target function produces an invalid component
    /// @throws IllegalStateException    if a registered property is bound
    public void setMotionSpec(@Nullable M3MotionSpec motionSpec) {
        this.motionSpec.set(motionSpec);
    }

    /// Returns the observable explicit default motion-specification property.
    ///
    /// @return the motion-specification property, whose value may be `null`
    public ObjectProperty<@Nullable M3MotionSpec> motionSpecProperty() {
        return motionSpec;
    }

    /// The state at which all registered properties most recently settled.
    private final ReadOnlyObjectWrapper<S> currentState;

    /// Returns the state at which every registered property most recently settled.
    ///
    /// During automatic playback or seeking this value remains the preceding settled state. It becomes the target
    /// only after the transition completes, [#finish()] is called, or [#snapTo(Object)] installs a state.
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

    /// The normalized play-time position of the current run or seek.
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", 1.0);

    /// Returns the normalized play-time position of the current run or seek.
    ///
    /// The value is in the inclusive range `0.0` through `1.0`. It is `1.0` for a settled transition, advances
    /// during automatic playback, and equals the last fraction supplied to [#seekTo(Object, double)] while seeking.
    ///
    /// @return the normalized progress
    public double getProgress() {
        return progress.get();
    }

    /// Returns the read-only normalized progress property.
    ///
    /// @return the progress property
    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    /// Whether progress is currently controlled by [#seekTo(Object, double)].
    private final ReadOnlyBooleanWrapper seeking = new ReadOnlyBooleanWrapper(this, "seeking");

    /// Returns whether this transition is waiting for externally supplied seek progress.
    ///
    /// Automatic playback has [Animation.Status#RUNNING] status and is not seeking. A seek is stopped from JavaFX's
    /// animation perspective and remains active until it is continued, finished, snapped, or retargeted.
    ///
    /// @return `true` while seek progress controls the registered values
    public boolean isSeeking() {
        return seeking.get();
    }

    /// Returns the read-only seeking-state property.
    ///
    /// @return the seeking property
    public ReadOnlyBooleanProperty seekingProperty() {
        return seeking.getReadOnlyProperty();
    }

    /// Creates a transition with an initial settled state.
    ///
    /// The initial state is installed as both the current and target state. Properties registered while the
    /// transition is idle are immediately initialized from this state.
    ///
    /// @param owner        the node whose effective motion setting and theme control automatic runs
    /// @param initialState the non-null initial settled state
    /// @throws NullPointerException if `owner` or `initialState` is `null`
    public M3StateTransition(Node owner, S initialState) {
        this.owner = Objects.requireNonNull(owner, "owner");
        S checkedState = Objects.requireNonNull(initialState, "initialState");
        currentState = new ReadOnlyObjectWrapper<>(this, "currentState", checkedState);
        acceptedTargetState = checkedState;
        activeInitialState = checkedState;
        activeTargetState = checkedState;
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

    /// Returns the node whose effective motion setting controls automatic playback.
    ///
    /// @return the owner supplied at construction time
    public Node getOwner() {
        return owner;
    }

    /// Returns the current shared animation status.
    ///
    /// Seeking does not start a JavaFX animation and therefore reports [Animation.Status#STOPPED]. Use
    /// [#isSeeking()] to distinguish an externally driven seek from a settled transition.
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

    /// Returns whether any registered channel is playing automatically.
    ///
    /// @return `true` if the status is [Animation.Status#RUNNING]
    public boolean isRunning() {
        return getStatus() == Animation.Status.RUNNING;
    }

    /// Registers one writable double property as a state-derived animation channel.
    ///
    /// The target function is evaluated immediately for the current target state. If the transition is idle, the
    /// property is set to that target synchronously. If an automatic run or seek is active, all channels are
    /// reconfigured together from their defined starts. A property may be registered only once by identity.
    ///
    /// The visibility threshold is expressed in the property's units and determines when physical springs are
    /// considered settled. It does not round, clamp, or otherwise alter the exact final value.
    ///
    /// @param value               the writable property updated by animation pulses or seeks
    /// @param targetValue         the deterministic function mapping a state to a finite property value
    /// @param visibilityThreshold the finite, positive spring visibility threshold in property units
    /// @throws NullPointerException     if `value` or `targetValue` is `null`
    /// @throws IllegalArgumentException if the property is already registered, the threshold is invalid, or a
    ///                                  current or target value is not finite
    /// @throws IllegalStateException    if `value` is bound
    public void addDouble(
            DoubleProperty value,
            ToDoubleFunction<? super S> targetValue,
            double visibilityThreshold
    ) {
        addDoubleChannel(value, targetValue, visibilityThreshold, null);
    }

    /// Registers one writable double property with a state-segment-specific motion specification.
    ///
    /// The specification function is evaluated once for each new automatic target or seek target. Its first argument
    /// is the preceding target state and its second argument is the requested state. This allows, for example, an
    /// expansion to use different timing from a collapse without rebuilding the transition. The function must return
    /// a non-null specification. Its result overrides [#motionSpecProperty()] for this channel only.
    ///
    /// @param value               the writable property updated by animation pulses or seeks
    /// @param targetValue         the deterministic function mapping a state to a finite property value
    /// @param visibilityThreshold the finite, positive spring visibility threshold in property units
    /// @param transitionSpec      the deterministic function selecting a specification from source and target states
    /// @throws NullPointerException     if an argument is `null`
    /// @throws IllegalArgumentException if the property is already registered, the threshold is invalid, or a
    ///                                  current or target value is not finite
    /// @throws IllegalStateException    if `value` is bound
    public void addDouble(
            DoubleProperty value,
            ToDoubleFunction<? super S> targetValue,
            double visibilityThreshold,
            BiFunction<? super S, ? super S, ? extends M3MotionSpec> transitionSpec
    ) {
        addDoubleChannel(
                value,
                targetValue,
                visibilityThreshold,
                Objects.requireNonNull(transitionSpec, "transitionSpec")
        );
    }

    /// Validates and installs one primitive channel with an optional local specification selector.
    private void addDoubleChannel(
            DoubleProperty value,
            ToDoubleFunction<? super S> targetValue,
            double visibilityThreshold,
            @Nullable BiFunction<? super S, ? super S, ? extends M3MotionSpec> transitionSpec
    ) {
        DoubleProperty checkedValue = Objects.requireNonNull(value, "value");
        ToDoubleFunction<? super S> checkedTargetValue = Objects.requireNonNull(targetValue, "targetValue");
        requireUnregistered(checkedValue);
        requireWritable(checkedValue);
        requireFinite(checkedValue.get(), "property value");

        DoubleChannel<S> channel = new DoubleChannel<>(
                checkedValue,
                checkedTargetValue,
                visibilityThreshold,
                transitionSpec
        );
        channel.stageTarget(getTargetState());
        channel.commitStagedTarget();
        addPreparedChannel(channel);
    }

    /// Registers one writable immutable value as a state-derived animation channel.
    ///
    /// The converter determines the fixed component count, ordering, and spring visibility thresholds. Current and
    /// target values must be non-null, and every decomposed component must be finite. Component arrays and scalar
    /// channels are allocated once during registration; only the converter's reconstructed immutable value may be
    /// allocated on an animation pulse.
    ///
    /// @param value       the writable non-null property updated by animation pulses or seeks
    /// @param targetValue the deterministic function mapping a state to a non-null target value
    /// @param converter   the stateless converter for the value type
    /// @param <T>         the non-null value type
    /// @throws NullPointerException     if an argument, current value, target value, or reconstructed value is `null`
    /// @throws IllegalArgumentException if the property is already registered, the converter has no components, or
    ///                                  a component or visibility threshold violates its finite-value contract
    /// @throws IllegalStateException    if `value` is bound
    public <T> void addValue(
            Property<T> value,
            Function<? super S, ? extends T> targetValue,
            M3VectorConverter<T> converter
    ) {
        addValueChannel(value, targetValue, converter, null);
    }

    /// Registers one immutable value with a state-segment-specific motion specification.
    ///
    /// The specification function receives the preceding target state and requested target state once per new run.
    /// Its non-null result overrides [#motionSpecProperty()] for every scalar component of this value channel.
    /// Component ordering, visibility thresholds, and reconstruction remain controlled by `converter`.
    ///
    /// @param value          the writable non-null property updated by animation pulses or seeks
    /// @param targetValue    the deterministic function mapping a state to a non-null target value
    /// @param converter      the stateless converter for the value type
    /// @param transitionSpec the deterministic function selecting a specification from source and target states
    /// @param <T>            the non-null value type
    /// @throws NullPointerException     if an argument, current value, target value, or reconstructed value is
    ///                                  `null`
    /// @throws IllegalArgumentException if the property is already registered, the converter has no components, or
    ///                                  a component or visibility threshold violates its finite-value contract
    /// @throws IllegalStateException    if `value` is bound
    public <T> void addValue(
            Property<T> value,
            Function<? super S, ? extends T> targetValue,
            M3VectorConverter<T> converter,
            BiFunction<? super S, ? super S, ? extends M3MotionSpec> transitionSpec
    ) {
        addValueChannel(
                value,
                targetValue,
                converter,
                Objects.requireNonNull(transitionSpec, "transitionSpec")
        );
    }

    /// Validates and installs one value channel with an optional local specification selector.
    private <T> void addValueChannel(
            Property<T> value,
            Function<? super S, ? extends T> targetValue,
            M3VectorConverter<T> converter,
            @Nullable BiFunction<? super S, ? super S, ? extends M3MotionSpec> transitionSpec
    ) {
        Property<T> checkedValue = Objects.requireNonNull(value, "value");
        Function<? super S, ? extends T> checkedTargetValue = Objects.requireNonNull(targetValue, "targetValue");
        M3VectorConverter<T> checkedConverter = Objects.requireNonNull(converter, "converter");
        requireUnregistered(checkedValue);
        requireWritable(checkedValue);

        ValueChannel<S, T> channel = new ValueChannel<>(
                checkedValue,
                checkedTargetValue,
                checkedConverter,
                transitionSpec
        );
        channel.validateCurrent();
        channel.stageTarget(getTargetState());
        channel.commitStagedTarget();
        addPreparedChannel(channel);
    }

    /// Removes the channel associated with a writable property.
    ///
    /// The removed property retains its current value and is not modified again. Remaining channels are reconfigured
    /// from their current automatic values or retained seek starts. If the last channel is removed, the typed state
    /// settles synchronously without changing the removed property.
    ///
    /// @param value the property whose channel should be removed
    /// @return `true` if an identity-equal registered property was removed; otherwise `false`
    /// @throws NullPointerException  if `value` is `null`
    /// @throws IllegalStateException if another registered property is bound while channels must be reconfigured
    public boolean removeChannel(Property<?> value) {
        Property<?> checkedValue = Objects.requireNonNull(value, "value");
        for (int index = 0; index < channels.size(); index++) {
            if (channels.get(index).owns(checkedValue)) {
                boolean running = isRunning();
                if (running) {
                    prepareTargets(getTargetState());
                }
                channels.remove(index);
                if (channels.isEmpty()) {
                    settleWithoutChannels();
                } else if (running) {
                    animation.retarget();
                } else if (isSeeking()) {
                    animation.reconfigureSeek(getProgress());
                    settleIfMotionlessSeek();
                }
                return true;
            }
        }
        return false;
    }

    /// Removes every registered channel.
    ///
    /// Registered properties retain their current values. Automatic playback or seeking stops, and the current state
    /// becomes the target because no remaining property can be in transition. Repeated calls when no channels are
    /// registered have no effect.
    public void clearChannels() {
        if (channels.isEmpty()) {
            return;
        }
        channels.clear();
        settleWithoutChannels();
    }

    /// Seeks all registered channels toward one state at a normalized play-time fraction.
    ///
    /// The first call for a target captures the currently rendered values as the seek start. Later calls for an equal
    /// target reuse those starts, so moving the fraction backward exactly retraces the configured easing or spring
    /// paths. Changing the target captures a new start from the current rendered values. Seeking stops automatic
    /// playback but intentionally ignores reduced-motion settings because the caller directly controls the motion.
    ///
    /// A fraction of `0.0` applies captured start values; `1.0` renders target values but leaves the transition in
    /// seeking state until [#animateToTarget()], [#finish()], or [#snapTo(Object)] commits a settled state.
    ///
    /// @param state    the non-null target state
    /// @param fraction the finite fraction in the inclusive range `0.0` through `1.0`
    /// @throws NullPointerException     if `state` or a selected channel specification is `null`
    /// @throws IllegalArgumentException if `fraction` is outside its range or a property, target function, or
    ///                                  converter produces an invalid component
    /// @throws IllegalStateException    if the target-state property or a registered property is bound
    public void seekTo(S state, double fraction) {
        S checkedState = Objects.requireNonNull(state, "state");
        double checkedFraction = requireFraction(fraction);
        requireWritableTargetState();

        boolean continuesExistingSeek = isSeeking() && Objects.equals(checkedState, getTargetState());
        if (continuesExistingSeek) {
            requireWritableChannels();
            animation.applySeek(checkedFraction);
            settleIfMotionlessSeek();
            return;
        }

        S initialState = getTargetState();
        stageTargets(checkedState);
        prepareMotionSpecs(resolveMotionSpec(), initialState, checkedState);
        commitStagedTargets();
        animation.stop();
        setTargetStateInternally(checkedState);
        acceptedTargetState = checkedState;
        activeInitialState = initialState;
        activeTargetState = checkedState;
        seeking.set(true);
        if (channels.isEmpty()) {
            settleWithoutChannels();
            return;
        }
        animation.beginSeek(checkedFraction);
        settleIfMotionlessSeek();
    }

    /// Continues automatic playback from the current seek position to its target.
    ///
    /// If this transition is not seeking, this method has no effect. The continuation preserves the seek path and
    /// begins at its current play time rather than reconfiguring a full-duration animation from the rendered value.
    /// Reduced motion or a non-presenting owner applies the exact target synchronously.
    ///
    /// @throws IllegalStateException if a registered property is bound
    public void animateToTarget() {
        if (!isSeeking()) {
            return;
        }
        requireWritableChannels();
        seeking.set(false);
        animation.resumeSeek(getProgress());
    }

    /// Completes the current automatic run or seek at its target values.
    ///
    /// Completion is synchronous. If no run is active, this method reevaluates and reapplies state-derived target
    /// values and clears retained channel velocity. Repeated calls are idempotent while target functions are stable.
    ///
    /// @throws IllegalArgumentException if a property, target function, or converter produces an invalid component
    /// @throws IllegalStateException    if a registered property is bound
    public void finish() {
        requireWritableChannels();
        if (isRunning()) {
            M3Animation.finish(animation);
            return;
        }

        prepareTargets(getTargetState());
        animation.stop();
        applyPreparedTargets();
        completeRun();
    }

    /// Stops any active run or seek and installs one state and all of its derived values synchronously.
    ///
    /// The supplied state becomes both the current and target state. Target functions are evaluated before playback
    /// or the target-state property changes, so a failed evaluation leaves the running transition intact.
    ///
    /// @param state the non-null state to install
    /// @throws NullPointerException     if `state` is `null`
    /// @throws IllegalArgumentException if a property, target function, or converter produces an invalid component
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
        progress.set(1.0);
        seeking.set(false);
    }

    /// Adds a channel after its current and target values have been validated.
    private void addPreparedChannel(Channel<S> channel) {
        if (isRunning()) {
            prepareTargets(getTargetState());
            channel.prepareMotionSpec(resolveMotionSpec(), activeInitialState, activeTargetState);
            channels.add(channel);
            animation.retarget();
        } else if (isSeeking()) {
            channel.captureSeekStart();
            channel.prepareMotionSpec(resolveMotionSpec(), activeInitialState, activeTargetState);
            channels.add(channel);
            animation.reconfigureSeek(getProgress());
            settleIfMotionlessSeek();
        } else {
            channels.add(channel);
            channel.applyTargetAndReset();
        }
    }

    /// Handles a target-state property invalidation from direct mutation or binding.
    private void targetStateInvalidated() {
        @Nullable S requestedState = targetState.get();
        try {
            S checkedState = Objects.requireNonNull(requestedState, "targetState");
            if (Objects.equals(checkedState, acceptedTargetState)) {
                if (!isRunning() && !isSeeking()) {
                    currentState.set(checkedState);
                }
                return;
            }
            S initialState = acceptedTargetState;
            stageTargets(checkedState);
            prepareMotionSpecs(resolveMotionSpec(), initialState, checkedState);
            commitStagedTargets();
            acceptedTargetState = checkedState;
            startPreparedRun(initialState, checkedState);
        } catch (RuntimeException exception) {
            if (!targetState.isBound()) {
                setTargetStateInternally(acceptedTargetState);
            }
            throw exception;
        }
    }

    /// Evaluates and validates all channel targets without starting a run.
    private void prepareTargets(S state) {
        stageTargets(state);
        commitStagedTargets();
    }

    /// Evaluates and validates every channel target without replacing active targets.
    private void stageTargets(S state) {
        Objects.requireNonNull(state, "state");
        // Avoid allocating an ArrayList iterator in a frequently used retarget path.
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            Channel<S> channel = channels.get(index);
            channel.validateCurrent();
            channel.stageTarget(state);
        }
    }

    /// Commits every staged channel target after target and specification validation succeeds.
    private void commitStagedTargets() {
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            channels.get(index).commitStagedTarget();
        }
    }

    /// Resolves every channel specification before target or animation state changes.
    private void prepareMotionSpecs(M3MotionSpec sharedSpec, S initialState, S targetState) {
        ensureMotionSpecBufferCapacity();
        int resolvedCount = 0;
        try {
            // Resolve the complete segment before replacing any channel's active specification.
            for (; resolvedCount < channels.size(); resolvedCount++) {
                motionSpecBuffer[resolvedCount] = channels.get(resolvedCount)
                        .resolveMotionSpec(sharedSpec, initialState, targetState);
            }
        } catch (RuntimeException exception) {
            clearMotionSpecBuffer(resolvedCount);
            throw exception;
        }

        for (int index = 0; index < channels.size(); index++) {
            channels.get(index).installMotionSpec(
                    Objects.requireNonNull(motionSpecBuffer[index], "resolved motion specification")
            );
            motionSpecBuffer[index] = null;
        }
    }

    /// Replaces only channels that inherit the shared default specification.
    private void refreshDefaultMotionSpecs(M3MotionSpec sharedSpec) {
        ensureMotionSpecBufferCapacity();
        for (int index = 0; index < channels.size(); index++) {
            motionSpecBuffer[index] = channels.get(index).resolveRefreshedDefaultMotionSpec(sharedSpec);
        }
        for (int index = 0; index < channels.size(); index++) {
            channels.get(index).installMotionSpec(
                    Objects.requireNonNull(motionSpecBuffer[index], "refreshed motion specification")
            );
            motionSpecBuffer[index] = null;
        }
    }

    /// Grows the reusable specification buffer when a channel was added since its last use.
    private void ensureMotionSpecBufferCapacity() {
        if (motionSpecBuffer.length < channels.size()) {
            motionSpecBuffer = new M3MotionSpec[Math.max(channels.size(), motionSpecBuffer.length * 2 + 1)];
        }
    }

    /// Releases resolved specifications after a selector failed before commit.
    private void clearMotionSpecBuffer(int count) {
        for (int index = 0; index < count; index++) {
            motionSpecBuffer[index] = null;
        }
    }

    /// Starts an automatic run toward previously prepared targets.
    private void startPreparedRun(S initialState, S targetState) {
        seeking.set(false);
        activeInitialState = initialState;
        activeTargetState = targetState;
        if (channels.isEmpty()) {
            settleWithoutChannels();
            return;
        }
        animation.retarget();
    }

    /// Applies all prepared targets and clears retained channel velocity.
    private void applyPreparedTargets() {
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            channels.get(index).applyTargetAndReset();
        }
    }

    /// Publishes completion after all properties have reached exact target values.
    private void completeRun() {
        applyPreparedTargets();
        currentState.set(getTargetState());
        progress.set(1.0);
        seeking.set(false);
    }

    /// Settles typed state after all visual channels have been removed.
    private void settleWithoutChannels() {
        animation.stop();
        currentState.set(getTargetState());
        progress.set(1.0);
        seeking.set(false);
    }

    /// Commits a seek whose channels have no non-zero motion duration.
    private void settleIfMotionlessSeek() {
        if (isSeeking() && animation.getRunDurationSeconds() <= 0.0) {
            completeRun();
        }
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
    private void requireUnregistered(Property<?> value) {
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            if (channels.get(index).owns(value)) {
                throw new IllegalArgumentException("value property is already registered");
            }
        }
    }

    /// Verifies that a property may be written by an animation pulse or seek.
    private static void requireWritable(Property<?> value) {
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

    /// Verifies that every registered property may be written.
    private void requireWritableChannels() {
        //noinspection ForLoopReplaceableByForEach
        for (int index = 0; index < channels.size(); index++) {
            channels.get(index).requireWritable();
        }
    }

    /// Returns a finite value or throws for a broken channel contract.
    private static double requireFinite(double value, String description) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(description + " must be finite");
        }
        return value;
    }

    /// Returns a valid normalized seek fraction.
    private static double requireFraction(double fraction) {
        if (!Double.isFinite(fraction) || fraction < 0.0 || fraction > 1.0) {
            throw new IllegalArgumentException("fraction must be finite and in the range 0.0 through 1.0");
        }
        return fraction;
    }

    /// Stores the lifecycle shared by one state-derived property channel.
    @NotNullByDefault
    private abstract static class Channel<S> {
        /// The writable property owned by this channel.
        private final Property<?> value;

        /// The optional state-segment-specific motion selector.
        private final @Nullable BiFunction<? super S, ? super S, ? extends M3MotionSpec> transitionSpec;

        /// The specification validated before the next run mutates transition state.
        private @Nullable M3MotionSpec preparedMotionSpec;

        /// Creates a channel for one writable property.
        private Channel(
                Property<?> value,
                @Nullable BiFunction<? super S, ? super S, ? extends M3MotionSpec> transitionSpec
        ) {
            this.value = value;
            this.transitionSpec = transitionSpec;
        }

        /// Returns whether this channel owns the supplied property by identity.
        private boolean owns(Property<?> candidate) {
            return value == candidate;
        }

        /// Verifies that the property remains writable.
        final void requireWritable() {
            M3StateTransition.requireWritable(value);
        }

        /// Resolves and stores the shared or state-segment-specific specification for one newly added channel.
        final void prepareMotionSpec(M3MotionSpec sharedSpec, S initialState, S targetState) {
            installMotionSpec(resolveMotionSpec(sharedSpec, initialState, targetState));
        }

        /// Resolves the shared or state-segment-specific specification without changing active channel state.
        final M3MotionSpec resolveMotionSpec(M3MotionSpec sharedSpec, S initialState, S targetState) {
            @Nullable BiFunction<? super S, ? super S, ? extends M3MotionSpec> selector = transitionSpec;
            return selector == null
                    ? sharedSpec
                    : Objects.requireNonNull(selector.apply(initialState, targetState), "transitionSpec result");
        }

        /// Resolves a changed shared default while preserving a channel-specific segment specification.
        final M3MotionSpec resolveRefreshedDefaultMotionSpec(M3MotionSpec sharedSpec) {
            return transitionSpec == null ? sharedSpec : getPreparedMotionSpec();
        }

        /// Commits a specification after every channel selector has completed successfully.
        final void installMotionSpec(M3MotionSpec motionSpec) {
            preparedMotionSpec = Objects.requireNonNull(motionSpec, "motionSpec");
        }

        /// Returns the specification prepared before run configuration.
        final M3MotionSpec getPreparedMotionSpec() {
            return Objects.requireNonNull(preparedMotionSpec, "preparedMotionSpec");
        }

        /// Verifies the currently rendered property value.
        abstract void validateCurrent();

        /// Evaluates and stages the target for one state without replacing the active target.
        abstract void stageTarget(S state);

        /// Commits a target after every channel has staged successfully.
        abstract void commitStagedTarget();

        /// Configures automatic motion from the currently rendered value.
        abstract void configureAutomatic(M3MotionSpec motionSpec, double previousElapsedSeconds);

        /// Captures the currently rendered value as a stable seek start.
        abstract void captureSeekStart();

        /// Configures seek motion from its captured start to the prepared target.
        abstract void configureSeek(M3MotionSpec motionSpec);

        /// Returns the longest scalar duration owned by this channel.
        abstract double getDurationSeconds();

        /// Applies this channel at one shared elapsed time.
        abstract void applyAt(double elapsedSeconds);

        /// Applies the exact prepared target and clears retained motion state.
        abstract void applyTargetAndReset();
    }

    /// Stores one primitive double property channel.
    @NotNullByDefault
    private static final class DoubleChannel<S> extends Channel<S> {
        /// The primitive writable property updated without boxing.
        private final DoubleProperty doubleValue;

        /// The state-to-target mapping evaluated when a target is requested.
        private final ToDoubleFunction<? super S> targetValue;

        /// The reusable interpolation and velocity state.
        private final M3ScalarChannel scalar;

        /// The target computed before the next run starts.
        private double pendingTarget;

        /// The target staged while all channel functions are validated.
        private double stagedTarget;

        /// The stable start captured for repeated seeks.
        private double seekStart;

        /// Creates one primitive property channel.
        private DoubleChannel(
                DoubleProperty value,
                ToDoubleFunction<? super S> targetValue,
                double visibilityThreshold,
                @Nullable BiFunction<? super S, ? super S, ? extends M3MotionSpec> transitionSpec
        ) {
            super(value, transitionSpec);
            doubleValue = value;
            this.targetValue = targetValue;
            scalar = new M3ScalarChannel(visibilityThreshold);
        }

        /// Verifies that the current primitive value is finite and writable.
        @Override
        void validateCurrent() {
            requireWritable();
            requireFinite(doubleValue.get(), "property value");
        }

        /// Evaluates one finite primitive target.
        @Override
        void stageTarget(S state) {
            stagedTarget = requireFinite(targetValue.applyAsDouble(state), "target function result");
        }

        /// Commits the staged primitive target.
        @Override
        void commitStagedTarget() {
            pendingTarget = stagedTarget;
        }

        /// Configures motion from the current primitive value.
        @Override
        void configureAutomatic(M3MotionSpec motionSpec, double previousElapsedSeconds) {
            scalar.configure(doubleValue.get(), pendingTarget, motionSpec, previousElapsedSeconds);
        }

        /// Captures the current primitive value for seek reuse.
        @Override
        void captureSeekStart() {
            seekStart = doubleValue.get();
        }

        /// Configures deterministic seek motion without retained automatic velocity.
        @Override
        void configureSeek(M3MotionSpec motionSpec) {
            scalar.configure(seekStart, pendingTarget, motionSpec, Double.POSITIVE_INFINITY);
        }

        /// Returns the scalar channel duration.
        @Override
        double getDurationSeconds() {
            return scalar.getDurationSeconds();
        }

        /// Applies the primitive value at a shared elapsed time.
        @Override
        void applyAt(double elapsedSeconds) {
            doubleValue.set(scalar.valueAt(elapsedSeconds));
        }

        /// Applies and resets the exact primitive target.
        @Override
        void applyTargetAndReset() {
            doubleValue.set(pendingTarget);
            scalar.reset(pendingTarget);
        }
    }

    /// Stores one immutable multi-component value channel.
    @NotNullByDefault
    private static final class ValueChannel<S, T> extends Channel<S> {
        /// The typed writable property updated after component reconstruction.
        private final Property<T> objectValue;

        /// The state-to-target mapping evaluated when a target is requested.
        private final Function<? super S, ? extends T> targetValue;

        /// The stateless component converter.
        private final M3VectorConverter<T> converter;

        /// The reusable scalar channels, one per converted component.
        private final M3ScalarChannel[] scalars;

        /// The component values of the prepared exact target.
        private final double[] pendingComponents;

        /// The component values staged while all channel functions are validated.
        private final double[] stagedComponents;

        /// The stable component values captured for repeated seeks.
        private final double[] seekStartComponents;

        /// The component buffer reconstructed on each applied frame.
        private final double[] renderedComponents;

        /// The reusable read-only view over rendered components.
        private final VectorView vector;

        /// The exact target object applied when the transition settles.
        private T pendingTarget;

        /// The exact target object staged while every channel target is validated.
        private T stagedTarget;

        /// Creates one multi-component value channel and its reusable storage.
        private ValueChannel(
                Property<T> value,
                Function<? super S, ? extends T> targetValue,
                M3VectorConverter<T> converter,
                @Nullable BiFunction<? super S, ? super S, ? extends M3MotionSpec> transitionSpec
        ) {
            super(value, transitionSpec);
            objectValue = value;
            this.targetValue = targetValue;
            this.converter = converter;
            pendingTarget = Objects.requireNonNull(value.getValue(), "property value");
            stagedTarget = pendingTarget;

            int componentCount = converter.getComponentCount();
            if (componentCount <= 0) {
                throw new IllegalArgumentException("converter component count must be greater than zero");
            }
            scalars = new M3ScalarChannel[componentCount];
            pendingComponents = new double[componentCount];
            stagedComponents = new double[componentCount];
            seekStartComponents = new double[componentCount];
            renderedComponents = new double[componentCount];
            vector = new VectorView(renderedComponents);
            for (int index = 0; index < componentCount; index++) {
                double threshold = requireFinite(
                        converter.getVisibilityThreshold(index),
                        "converter visibility threshold"
                );
                if (threshold <= 0.0) {
                    throw new IllegalArgumentException("converter visibility thresholds must be greater than zero");
                }
                scalars[index] = new M3ScalarChannel(threshold);
            }
        }

        /// Verifies the current value and all of its components.
        @Override
        void validateCurrent() {
            requireWritable();
            T current = Objects.requireNonNull(objectValue.getValue(), "property value");
            readComponents(current, renderedComponents, "property component");
        }

        /// Evaluates and decomposes one exact target value.
        @Override
        void stageTarget(S state) {
            stagedTarget = Objects.requireNonNull(targetValue.apply(state), "target function result");
            readComponents(stagedTarget, stagedComponents, "target component");
        }

        /// Commits the staged exact object and component vector.
        @Override
        void commitStagedTarget() {
            pendingTarget = stagedTarget;
            System.arraycopy(stagedComponents, 0, pendingComponents, 0, stagedComponents.length);
        }

        /// Configures every component from the currently rendered value.
        @Override
        void configureAutomatic(M3MotionSpec motionSpec, double previousElapsedSeconds) {
            for (int index = 0; index < scalars.length; index++) {
                scalars[index].configure(
                        renderedComponents[index],
                        pendingComponents[index],
                        motionSpec,
                        previousElapsedSeconds
                );
            }
        }

        /// Captures every current component for repeated seeks.
        @Override
        void captureSeekStart() {
            T current = Objects.requireNonNull(objectValue.getValue(), "property value");
            readComponents(current, seekStartComponents, "property component");
        }

        /// Configures deterministic seek motion for every component.
        @Override
        void configureSeek(M3MotionSpec motionSpec) {
            for (int index = 0; index < scalars.length; index++) {
                scalars[index].configure(
                        seekStartComponents[index],
                        pendingComponents[index],
                        motionSpec,
                        Double.POSITIVE_INFINITY
                );
            }
        }

        /// Returns the longest component duration.
        @Override
        double getDurationSeconds() {
            double duration = 0.0;
            for (M3ScalarChannel scalar : scalars) {
                duration = Math.max(duration, scalar.getDurationSeconds());
            }
            return duration;
        }

        /// Reconstructs and applies one immutable value at a shared elapsed time.
        @Override
        void applyAt(double elapsedSeconds) {
            for (int index = 0; index < scalars.length; index++) {
                renderedComponents[index] = scalars[index].valueAt(elapsedSeconds);
            }
            objectValue.setValue(Objects.requireNonNull(converter.createValue(vector), "converter result"));
        }

        /// Applies the exact target object and resets every component channel.
        @Override
        void applyTargetAndReset() {
            objectValue.setValue(pendingTarget);
            for (int index = 0; index < scalars.length; index++) {
                scalars[index].reset(pendingComponents[index]);
            }
        }

        /// Decomposes and validates one value into reusable component storage.
        private void readComponents(T value, double[] destination, String description) {
            for (int index = 0; index < destination.length; index++) {
                destination[index] = requireFinite(converter.getComponent(value, index), description);
            }
        }
    }

    /// Provides a reusable read-only view over one component buffer.
    @NotNullByDefault
    private static final class VectorView implements IntToDoubleFunction {
        /// The component array owned by one value channel.
        private final double[] components;

        /// Creates a view over reusable component storage.
        private VectorView(double[] components) {
            this.components = components;
        }

        /// Returns one current component without boxing.
        @Override
        public double applyAsDouble(int index) {
            return components[index];
        }
    }

    /// Shared finite animation that advances every registered scalar channel.
    @NotNullByDefault
    private final class StateAnimation extends M3FiniteTransition {
        /// The duration of the longest active channel, in seconds.
        private double runDurationSeconds;

        /// The scalar play time corresponding to the beginning of this JavaFX run.
        private double playbackStartSeconds;

        /// Returns the duration of the complete configured transition.
        private double getRunDurationSeconds() {
            return runDurationSeconds;
        }

        /// Reconfigures every channel from its currently rendered automatic value.
        private void retarget() {
            double previousElapsedSeconds = currentScalarElapsedSeconds();

            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < channels.size(); index++) {
                Channel<S> channel = channels.get(index);
                channel.configureAutomatic(channel.getPreparedMotionSpec(), previousElapsedSeconds);
            }

            stop();
            playbackStartSeconds = 0.0;
            updateRunDuration();
            progress.set(0.0);
            playConfiguredSpan();
        }

        /// Captures start values and applies the first externally supplied seek fraction.
        private void beginSeek(double fraction) {
            stop();
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < channels.size(); index++) {
                Channel<S> channel = channels.get(index);
                channel.captureSeekStart();
                channel.configureSeek(channel.getPreparedMotionSpec());
            }
            playbackStartSeconds = 0.0;
            updateRunDuration();
            applySeek(fraction);
        }

        /// Reconfigures a seek from retained start values and reapplies its fraction.
        private void reconfigureSeek(double fraction) {
            stop();
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < channels.size(); index++) {
                Channel<S> channel = channels.get(index);
                channel.configureSeek(channel.getPreparedMotionSpec());
            }
            playbackStartSeconds = 0.0;
            updateRunDuration();
            applySeek(fraction);
        }

        /// Applies one externally supplied normalized play-time fraction.
        private void applySeek(double fraction) {
            double checkedFraction = requireFraction(fraction);
            applyChannelsAt(checkedFraction * runDurationSeconds);
            progress.set(checkedFraction);
        }

        /// Continues automatic playback from one sought fraction.
        private void resumeSeek(double fraction) {
            double checkedFraction = requireFraction(fraction);
            stop();
            playbackStartSeconds = checkedFraction * runDurationSeconds;
            progress.set(checkedFraction);
            playConfiguredSpan();
        }

        /// Starts the configured remaining span or completes it synchronously.
        private void playConfiguredSpan() {
            double remainingSeconds = Math.max(0.0, runDurationSeconds - playbackStartSeconds);
            if (remainingSeconds <= 0.0) {
                setCycleDuration(Duration.ZERO);
                M3Animation.finish(this);
                return;
            }
            setCycleDuration(Duration.seconds(remainingSeconds));
            setInterpolator(Interpolator.LINEAR);
            M3Animation.playFromStart(owner, this);
        }

        /// Recomputes the longest configured component duration.
        private void updateRunDuration() {
            runDurationSeconds = 0.0;
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < channels.size(); index++) {
                runDurationSeconds = Math.max(runDurationSeconds, channels.get(index).getDurationSeconds());
            }
        }

        /// Returns elapsed scalar time for velocity-preserving automatic retargeting.
        private double currentScalarElapsedSeconds() {
            if (getStatus() != Animation.Status.RUNNING) {
                return Double.POSITIVE_INFINITY;
            }
            return playbackStartSeconds + Math.max(0.0, getCurrentTime().toSeconds());
        }

        /// Applies one shared elapsed time to every registered channel.
        private void applyChannelsAt(double elapsedSeconds) {
            //noinspection ForLoopReplaceableByForEach
            for (int index = 0; index < channels.size(); index++) {
                channels.get(index).applyAt(elapsedSeconds);
            }
        }

        /// Applies one shared elapsed time and publishes normalized progress.
        @Override
        protected void interpolate(double fraction) {
            double checkedFraction = Math.max(0.0, Math.min(1.0, fraction));
            double elapsedSeconds = playbackStartSeconds
                    + checkedFraction * Math.max(0.0, runDurationSeconds - playbackStartSeconds);
            applyChannelsAt(elapsedSeconds);
            progress.set(runDurationSeconds <= 0.0 ? 1.0 : elapsedSeconds / runDurationSeconds);
        }
    }
}
