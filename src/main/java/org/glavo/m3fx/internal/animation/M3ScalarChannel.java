// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3SpringParameters;
import org.glavo.m3fx.internal.M3SpringSolver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Stores the interpolation and velocity state of one reusable scalar animation channel.
///
/// This internal value object has no pulse receiver and allocates no objects while evaluating values. A coordinating
/// transition configures one or more channels, selects their longest duration, and applies [#valueAt(double)] from
/// its single pulse callback. Reconfiguration may retain physical or estimated eased velocity from the preceding
/// run.
@NotNullByDefault
public final class M3ScalarChannel {
    /// The fraction interval used to estimate velocity for duration-based easing curves.
    private static final double VELOCITY_SAMPLE_FRACTION = 1.0e-4;

    /// The shortest non-zero spring run accepted by a JavaFX transition, in seconds.
    private static final double MIN_SPRING_DURATION_SECONDS = 1.0e-3;

    /// The delta at which a physical spring is considered visually settled.
    private final double visibilityThreshold;

    /// The specification configured for the current run, or `null` before configuration or after reset.
    private @Nullable M3MotionSpec motionSpec;

    /// The spring parameters for the current run, or `null` for duration interpolation.
    private @Nullable M3SpringParameters springParameters;

    /// The value at the start of the current run.
    private double startValue;

    /// The value at the end of the current run.
    private double targetValue;

    /// The retained velocity at the start of the current run, in value units per second.
    private double initialVelocity;

    /// The settling duration of the current run, in seconds.
    private double durationSeconds;

    /// Creates a reusable scalar channel.
    ///
    /// @param visibilityThreshold the finite, positive delta at which a physical spring is visually settled
    /// @throws IllegalArgumentException if `visibilityThreshold` is not finite and greater than zero
    public M3ScalarChannel(double visibilityThreshold) {
        if (!Double.isFinite(visibilityThreshold) || visibilityThreshold <= 0.0) {
            throw new IllegalArgumentException("visibilityThreshold must be finite and greater than zero");
        }
        this.visibilityThreshold = visibilityThreshold;
    }

    /// Returns the settling duration of the current run.
    ///
    /// @return the non-negative duration, in seconds
    public double getDurationSeconds() {
        return durationSeconds;
    }

    /// Reconfigures this channel while retaining its current velocity.
    ///
    /// Pass the elapsed time of the preceding run to preserve its velocity. Pass positive infinity when the preceding
    /// run is no longer active; that explicitly discards stale velocity while retaining the supplied current value.
    ///
    /// @param currentValue           the finite value rendered when reconfiguration begins
    /// @param targetValue            the finite value requested at completion
    /// @param motionSpec             the specification for the new run
    /// @param previousElapsedSeconds the non-negative elapsed time of the preceding run, or positive infinity
    /// @throws NullPointerException     if `motionSpec` is `null`
    /// @throws IllegalArgumentException if either value is non-finite or elapsed time is negative or `NaN`
    public void configure(
            double currentValue,
            double targetValue,
            M3MotionSpec motionSpec,
            double previousElapsedSeconds
    ) {
        if (!Double.isFinite(currentValue) || !Double.isFinite(targetValue)) {
            throw new IllegalArgumentException("channel values must be finite");
        }
        if (Double.isNaN(previousElapsedSeconds) || previousElapsedSeconds < 0.0) {
            throw new IllegalArgumentException("previousElapsedSeconds must be non-negative");
        }

        double retainedVelocity = velocityAt(previousElapsedSeconds);
        M3MotionSpec checkedSpec = Objects.requireNonNull(motionSpec, "motionSpec");
        this.motionSpec = checkedSpec;
        springParameters = checkedSpec.springParameters();
        startValue = currentValue;
        this.targetValue = targetValue;
        initialVelocity = retainedVelocity;
        durationSeconds = estimateDuration(checkedSpec);
    }

    /// Clears motion history and establishes one settled value.
    ///
    /// @param value the finite settled value
    /// @throws IllegalArgumentException if `value` is not finite
    public void reset(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be finite");
        }
        motionSpec = null;
        springParameters = null;
        startValue = value;
        targetValue = value;
        initialVelocity = 0.0;
        durationSeconds = 0.0;
    }

    /// Returns this channel's value at the supplied elapsed time.
    ///
    /// Times at or beyond the settling duration return the exact target. Positive infinity is accepted and therefore
    /// also returns the target.
    ///
    /// @param elapsedSeconds the non-negative elapsed time, in seconds
    /// @return the interpolated or settled value
    /// @throws IllegalArgumentException if `elapsedSeconds` is negative or `NaN`
    public double valueAt(double elapsedSeconds) {
        validateElapsedTime(elapsedSeconds);
        @Nullable M3MotionSpec spec = motionSpec;
        if (spec == null || durationSeconds <= 0.0 || elapsedSeconds >= durationSeconds) {
            return targetValue;
        }
        @Nullable M3SpringParameters spring = springParameters;
        if (spring != null) {
            return M3SpringSolver.value(
                    startValue,
                    targetValue,
                    initialVelocity,
                    elapsedSeconds,
                    spring
            );
        }
        return spec.interpolator().interpolate(
                startValue,
                targetValue,
                elapsedSeconds / durationSeconds
        );
    }

    /// Returns this channel's velocity at the supplied elapsed time.
    ///
    /// Times at or beyond the settling duration return zero. Duration-based motion uses a finite difference over the
    /// configured easing curve; spring motion uses the analytic spring velocity.
    ///
    /// @param elapsedSeconds the non-negative elapsed time, in seconds
    /// @return velocity in value units per second
    /// @throws IllegalArgumentException if `elapsedSeconds` is negative or `NaN`
    public double velocityAt(double elapsedSeconds) {
        validateElapsedTime(elapsedSeconds);
        @Nullable M3MotionSpec spec = motionSpec;
        if (spec == null || durationSeconds <= 0.0 || elapsedSeconds >= durationSeconds) {
            return 0.0;
        }
        @Nullable M3SpringParameters spring = springParameters;
        if (spring != null) {
            return M3SpringSolver.velocity(
                    startValue,
                    targetValue,
                    initialVelocity,
                    elapsedSeconds,
                    spring
            );
        }

        double fraction = elapsedSeconds / durationSeconds;
        double lowerFraction = Math.max(0.0, fraction - VELOCITY_SAMPLE_FRACTION);
        double upperFraction = Math.min(1.0, fraction + VELOCITY_SAMPLE_FRACTION);
        if (Double.compare(lowerFraction, upperFraction) == 0) {
            return 0.0;
        }
        double lowerValue = spec.interpolator().interpolate(startValue, targetValue, lowerFraction);
        double upperValue = spec.interpolator().interpolate(startValue, targetValue, upperFraction);
        return (upperValue - lowerValue) / ((upperFraction - lowerFraction) * durationSeconds);
    }

    /// Computes the finite duration of this channel's configured run.
    private double estimateDuration(M3MotionSpec motionSpec) {
        @Nullable M3SpringParameters spring = springParameters;
        if (spring == null) {
            return Double.compare(startValue, targetValue) == 0 ? 0.0 : motionSpec.duration().toSeconds();
        }
        if (Double.compare(startValue, targetValue) == 0 && Double.compare(initialVelocity, 0.0) == 0) {
            return 0.0;
        }
        double duration = M3SpringSolver.estimateDurationSeconds(
                startValue - targetValue,
                initialVelocity,
                visibilityThreshold,
                spring
        );
        if (!Double.isFinite(duration)) {
            return motionSpec.duration().toSeconds();
        }
        return duration <= 0.0 ? 0.0 : Math.max(MIN_SPRING_DURATION_SECONDS, duration);
    }

    /// Validates an elapsed-time argument shared by value and velocity evaluation.
    private static void validateElapsedTime(double elapsedSeconds) {
        if (Double.isNaN(elapsedSeconds) || elapsedSeconds < 0.0) {
            throw new IllegalArgumentException("elapsedSeconds must be non-negative");
        }
    }
}
