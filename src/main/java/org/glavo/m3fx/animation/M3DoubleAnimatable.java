// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.animation.M3DoubleTransition;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Animates a writable double property with an interruptible Material motion specification.
///
/// An animatable has one [owner][#getOwner()], one writable [value][#valueProperty()], and one visibility
/// threshold. Calling [#animateTo(double, M3MotionSpec)] while an animation is running starts from the property's
/// current value. Physical spring specifications also retain the current velocity, so changing the target does not
/// introduce an instantaneous velocity discontinuity. No key frames are allocated while the animation is running.
///
/// The owner supplies the effective [motion setting][M3MotionSettings] for each run. If reduced motion is requested
/// before or during a run, the property is set to the target value synchronously and the status becomes
/// [Animation.Status#STOPPED]. A run adopts the owner's first scene and presenting window; it also finishes
/// synchronously if the owner leaves that presentation context or the window is hidden. The owner does not have to
/// be the node whose property is animated, but it should be in the same themed subtree so that motion policy is
/// resolved consistently.
///
/// The value property must remain writable while a run is active. A caller may observe or bind from it, but must not
/// bind it or independently write it until the run has stopped. All methods that start, stop, or finish an animation
/// must be invoked on the JavaFX Application Thread.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3DoubleAnimatable {
    /// The node whose effective motion setting controls animation playback.
    private final Node owner;

    /// The writable property updated by each animation pulse.
    private final DoubleProperty value;

    /// The value delta at which a physical spring is considered visually settled.
    private final double visibilityThreshold;

    /// The most recently requested target value.
    private final ReadOnlyDoubleWrapper targetValue;

    /// The reusable transition that performs interpolation without allocating key frames.
    private final M3DoubleTransition transition;

    /// Creates an animatable for a writable double property.
    ///
    /// The initial target value is the property's value at construction time. The visibility threshold is expressed
    /// in the same units as the property and determines the settling duration of physical springs; it does not
    /// round, clamp, or otherwise alter the final target value.
    ///
    /// @param owner               the node whose effective motion setting controls each run
    /// @param value               the writable property to animate
    /// @param visibilityThreshold the finite, positive value delta at which a physical spring is visually settled
    /// @throws NullPointerException     if `owner` or `value` is `null`
    /// @throws IllegalArgumentException if `visibilityThreshold` is not finite and greater than zero
    public M3DoubleAnimatable(Node owner, DoubleProperty value, double visibilityThreshold) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.value = Objects.requireNonNull(value, "value");
        this.visibilityThreshold = visibilityThreshold;
        this.targetValue = new ReadOnlyDoubleWrapper(this, "targetValue", value.get());
        this.transition = new M3DoubleTransition(value, visibilityThreshold);
    }

    /// Returns the node whose effective motion setting controls animation playback.
    ///
    /// @return the motion owner supplied at construction time
    public Node getOwner() {
        return owner;
    }

    /// Returns the current value.
    ///
    /// The value changes on animation pulses and may be between the start and target values while an animation is
    /// running. A physical spring may temporarily move outside that interval.
    ///
    /// @return the current property value
    public double getValue() {
        return value.get();
    }

    /// Returns the writable property animated by this object.
    ///
    /// The returned property is the same instance supplied to the constructor. Binding from the property is
    /// permitted. Binding or writing the property while an animation is running violates this class's contract.
    ///
    /// @return the animated property
    public DoubleProperty valueProperty() {
        return value;
    }

    /// Returns the value delta used to determine when a physical spring has visually settled.
    ///
    /// @return the finite, positive visibility threshold, in the value property's units
    public double getVisibilityThreshold() {
        return visibilityThreshold;
    }

    /// Returns the most recently requested target value.
    ///
    /// Calling [#stop()] does not change this value. Calling [#snapTo(double)] or
    /// [#animateTo(double, M3MotionSpec)] replaces it before changing the animated property.
    ///
    /// @return the current target value
    public double getTargetValue() {
        return targetValue.get();
    }

    /// Returns the read-only target-value property.
    ///
    /// @return the target-value property
    public ReadOnlyDoubleProperty targetValueProperty() {
        return targetValue.getReadOnlyProperty();
    }

    /// Returns the current animation status.
    ///
    /// @return the status of the underlying finite animation
    public Animation.Status getStatus() {
        return transition.getStatus();
    }

    /// Returns the read-only animation-status property.
    ///
    /// @return the animation-status property
    public ReadOnlyObjectProperty<Animation.Status> statusProperty() {
        return transition.statusProperty();
    }

    /// Returns whether the value is currently being animated.
    ///
    /// @return `true` if the status is [Animation.Status#RUNNING]
    public boolean isRunning() {
        return getStatus() == Animation.Status.RUNNING;
    }

    /// Animates the value to a new target.
    ///
    /// The active theme's default spatial motion role is resolved from the owner when this method is called.
    ///
    /// @param targetValue the finite target value
    /// @throws IllegalArgumentException if `targetValue` is not finite
    /// @throws IllegalStateException    if the value property is bound
    public void animateTo(double targetValue) {
        animateTo(targetValue, M3Animation.defaultSpatial(owner));
    }

    /// Animates the value to a new target with an explicit specification.
    ///
    /// If another run is active, the new run begins at the current value. Spring motion retains the current physical
    /// velocity; duration-based motion estimates the current eased velocity before retargeting. If reduced motion is
    /// requested for the owner, this method applies the target and completes synchronously.
    ///
    /// @param targetValue the finite target value
    /// @param motionSpec  the motion specification for this run
    /// @throws NullPointerException     if `motionSpec` is `null`
    /// @throws IllegalArgumentException if `targetValue` is not finite
    /// @throws IllegalStateException    if the value property is bound
    public void animateTo(double targetValue, M3MotionSpec motionSpec) {
        requireWritableValue();
        transition.configure(Objects.requireNonNull(motionSpec, "motionSpec"), targetValue);
        this.targetValue.set(targetValue);
        M3Animation.playFromStart(owner, transition);
    }

    /// Stops the current run at its current value.
    ///
    /// This method has no effect when no animation is running. The target-value property is not changed, and a later
    /// call to [#animateTo(double, M3MotionSpec)] starts from the value retained by this method.
    public void stop() {
        transition.stop();
    }

    /// Completes the current run at its target value.
    ///
    /// The completion is synchronous. This method also applies the target when the animation has already stopped,
    /// making repeated calls idempotent with respect to the value and target properties.
    ///
    /// @throws IllegalStateException if the value property is bound
    public void finish() {
        requireWritableValue();
        if (transition.getStatus() == Animation.Status.RUNNING) {
            M3Animation.finish(transition);
        } else {
            value.set(getTargetValue());
        }
    }

    /// Stops animation and immediately replaces both the current and target values.
    ///
    /// @param value the finite value to apply
    /// @throws IllegalArgumentException if `value` is not finite
    /// @throws IllegalStateException    if the value property is bound
    public void snapTo(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be finite");
        }
        requireWritableValue();
        transition.stop();
        targetValue.set(value);
        this.value.set(value);
    }

    /// Verifies that an animation pulse may write the configured value property.
    private void requireWritableValue() {
        if (value.isBound()) {
            throw new IllegalStateException("value property must not be bound");
        }
    }
}
