// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3SpringParameters;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3SpringSolver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Reusable finite transition for one writable double property.
///
/// Each call to [#configure(M3MotionSpec, double)] captures the property's current value, allowing one transition
/// instance to be retargeted without replacing the property or allocating key frames.
@NotNullByDefault
public final class M3DoubleTransition extends M3FiniteTransition {
    /// The fraction interval used to estimate velocity for duration-based fallback curves.
    private static final double VELOCITY_SAMPLE_FRACTION = 1.0e-4;

    /// The shortest non-zero spring run accepted by a JavaFX transition, in seconds.
    private static final double MIN_SPRING_DURATION_SECONDS = 1.0e-3;

    /// A visibility threshold suitable for values normalized to the closed unit interval.
    public static final double NORMALIZED_VISIBILITY_THRESHOLD = 5.0e-4;

    /// A visibility threshold suitable for logical-pixel positions.
    public static final double PIXEL_VISIBILITY_THRESHOLD = 5.0e-1;

    /// A visibility threshold suitable for angular values expressed in degrees.
    public static final double ANGLE_VISIBILITY_THRESHOLD = 5.0e-1;

    /// The property whose value is animated.
    private final DoubleProperty property;

    /// The value delta below which a spring is considered visually settled.
    private final double visibilityThreshold;

    /// The inclusive lower bound applied to each rendered value.
    private final double minimumValue;

    /// The inclusive upper bound applied to each rendered value.
    private final double maximumValue;

    /// The specification configured for the current or most recent run.
    private @Nullable M3MotionSpec motionSpec;

    /// The physical parameters used by the current run, or `null` for duration-based interpolation.
    private @Nullable M3SpringParameters springParameters;

    /// The duration of the current run, in seconds.
    private double runDurationSeconds;

    /// The value at the beginning of the current transition.
    private double startValue;

    /// The value at the end of the current transition.
    private double targetValue;

    /// The value velocity at the beginning of the current transition, in units per second.
    private double initialVelocity;

    /// Creates a transition for a writable double property.
    ///
    /// @param property            the property updated by each animation pulse
    /// @param visibilityThreshold the finite, positive value delta at which a spring is visually settled
    /// @throws NullPointerException     if `property` is `null`
    /// @throws IllegalArgumentException if `visibilityThreshold` is not finite and positive
    public M3DoubleTransition(DoubleProperty property, double visibilityThreshold) {
        this(property, visibilityThreshold, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /// Creates a bounded transition for a writable double property.
    ///
    /// @param property            the property updated by each animation pulse
    /// @param visibilityThreshold the finite, positive value delta at which a spring is visually settled
    /// @param minimumValue        the inclusive lower bound, which may be negative infinity
    /// @param maximumValue        the inclusive upper bound, which may be positive infinity
    /// @throws NullPointerException     if `property` is `null`
    /// @throws IllegalArgumentException if the threshold is not finite and positive, either bound is `NaN`, or the
    ///                                  lower bound is greater than the upper bound
    public M3DoubleTransition(
            DoubleProperty property,
            double visibilityThreshold,
            double minimumValue,
            double maximumValue
    ) {
        this.property = Objects.requireNonNull(property, "property");
        if (!Double.isFinite(visibilityThreshold) || visibilityThreshold <= 0.0) {
            throw new IllegalArgumentException("visibilityThreshold must be finite and greater than zero");
        }
        if (Double.isNaN(minimumValue) || Double.isNaN(maximumValue) || minimumValue > maximumValue) {
            throw new IllegalArgumentException("minimumValue must not be greater than maximumValue");
        }
        this.visibilityThreshold = visibilityThreshold;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    /// Reconfigures this transition from the property's current value.
    ///
    /// A spring specification preserves the current velocity when a running transition is retargeted and derives
    /// its cycle duration from this transition's visibility threshold. A duration-based specification uses its
    /// declared duration and interpolator.
    ///
    /// @param spec        the motion specification for the next run
    /// @param targetValue the value to apply at the end of the next run
    /// @throws NullPointerException     if `spec` is `null`
    /// @throws IllegalArgumentException if `targetValue` is non-finite or outside this transition's configured
    ///                                  bounds
    public void configure(M3MotionSpec spec, double targetValue) {
        M3MotionSpec checkedSpec = Objects.requireNonNull(spec, "spec");
        if (!Double.isFinite(targetValue) || targetValue < minimumValue || targetValue > maximumValue) {
            throw new IllegalArgumentException("targetValue must be finite and inside the configured bounds");
        }

        double retainedVelocity = currentVelocity();
        stop();
        motionSpec = checkedSpec;
        springParameters = checkedSpec.springParameters();
        startValue = property.get();
        this.targetValue = targetValue;
        initialVelocity = retainedVelocity;

        configureDuration(checkedSpec);
        setCycleDuration(Duration.seconds(runDurationSeconds));
        setInterpolator(springParameters == null ? checkedSpec.interpolator() : Interpolator.LINEAR);
    }

    /// Applies the spring or eased fallback value for the current pulse.
    @Override
    protected void interpolate(double fraction) {
        @Nullable M3MotionSpec spec = motionSpec;
        if (spec == null) {
            return;
        }
        if (fraction >= 1.0 || runDurationSeconds <= 0.0) {
            property.set(targetValue);
            return;
        }

        @Nullable M3SpringParameters spring = springParameters;
        double value = spring == null
                ? startValue + (targetValue - startValue) * fraction
                : M3SpringSolver.value(
                startValue,
                targetValue,
                initialVelocity,
                Math.max(0.0, fraction) * runDurationSeconds,
                spring
        );
        property.set(clamp(value));
    }

    /// Computes the current run duration from the physical spring or deterministic fallback.
    private void configureDuration(M3MotionSpec spec) {
        double fallbackDurationSeconds = spec.duration().toSeconds();
        @Nullable M3SpringParameters spring = springParameters;
        if (spring == null) {
            runDurationSeconds = fallbackDurationSeconds;
            return;
        }
        if (Double.compare(startValue, targetValue) == 0 && Double.compare(initialVelocity, 0.0) == 0) {
            runDurationSeconds = 0.0;
            return;
        }

        double estimatedDurationSeconds = M3SpringSolver.estimateDurationSeconds(
                startValue - targetValue,
                initialVelocity,
                visibilityThreshold,
                spring
        );
        runDurationSeconds = Double.isFinite(estimatedDurationSeconds)
                ? Math.max(MIN_SPRING_DURATION_SECONDS, estimatedDurationSeconds)
                : fallbackDurationSeconds;
    }

    /// Returns the current physical or estimated fallback velocity before retargeting.
    private double currentVelocity() {
        @Nullable M3MotionSpec spec = motionSpec;
        if (spec == null || getStatus() == Animation.Status.STOPPED || runDurationSeconds <= 0.0) {
            return 0.0;
        }

        double elapsedSeconds = Math.max(0.0, getCurrentTime().toSeconds());
        if (elapsedSeconds >= runDurationSeconds) {
            return 0.0;
        }
        @Nullable M3SpringParameters spring = springParameters;
        if (spring != null) {
            double value = M3SpringSolver.value(
                    startValue,
                    targetValue,
                    initialVelocity,
                    elapsedSeconds,
                    spring
            );
            double velocity = M3SpringSolver.velocity(
                    startValue,
                    targetValue,
                    initialVelocity,
                    elapsedSeconds,
                    spring
            );
            if ((value <= minimumValue && velocity < 0.0)
                    || (value >= maximumValue && velocity > 0.0)) {
                return 0.0;
            }
            return velocity;
        }

        double fraction = elapsedSeconds / runDurationSeconds;
        double lowerFraction = Math.max(0.0, fraction - VELOCITY_SAMPLE_FRACTION);
        double upperFraction = Math.min(1.0, fraction + VELOCITY_SAMPLE_FRACTION);
        if (Double.compare(lowerFraction, upperFraction) == 0) {
            return 0.0;
        }
        double lowerValue = spec.interpolator().interpolate(startValue, targetValue, lowerFraction);
        double upperValue = spec.interpolator().interpolate(startValue, targetValue, upperFraction);
        return (upperValue - lowerValue) / ((upperFraction - lowerFraction) * runDurationSeconds);
    }

    /// Clamps a rendered value to this transition's configured inclusive bounds.
    private double clamp(double value) {
        return Math.max(minimumValue, Math.min(maximumValue, value));
    }
}
