// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Reusable finite transition for one writable double property.
///
/// Each call to [#configure(M3MotionSpec, double)] captures the property's current value. Retargeting an active run
/// retains its scalar velocity, while retargeting at a configured bound discards velocity directed farther outside
/// that bound. Interpolation is delegated to one reusable scalar channel and does not allocate key frames or other
/// per-pulse objects.
@NotNullByDefault
public final class M3DoubleTransition extends M3FiniteTransition {
    /// A visibility threshold suitable for values normalized to the closed unit interval.
    public static final double NORMALIZED_VISIBILITY_THRESHOLD = 5.0e-4;

    /// A visibility threshold suitable for logical-pixel positions.
    public static final double PIXEL_VISIBILITY_THRESHOLD = 5.0e-1;

    /// A visibility threshold suitable for angular values expressed in degrees.
    public static final double ANGLE_VISIBILITY_THRESHOLD = 5.0e-1;

    /// The property whose value is animated.
    private final DoubleProperty property;

    /// The scalar interpolation and velocity state reused by every run.
    private final M3ScalarChannel channel;

    /// The inclusive lower bound applied to each rendered value.
    private final double minimumValue;

    /// The inclusive upper bound applied to each rendered value.
    private final double maximumValue;

    /// Whether this transition has been configured at least once.
    private boolean configured;

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
        this.channel = new M3ScalarChannel(visibilityThreshold);
        if (Double.isNaN(minimumValue) || Double.isNaN(maximumValue) || minimumValue > maximumValue) {
            throw new IllegalArgumentException("minimumValue must not be greater than maximumValue");
        }
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    /// Reconfigures this transition from the property's current value.
    ///
    /// A running transition contributes its current velocity to the new run. Velocity directed beyond an inclusive
    /// bound is discarded because rendered output cannot continue in that direction. A stopped transition starts
    /// with zero velocity.
    ///
    /// @param spec        the motion specification for the next run
    /// @param targetValue the value to apply at the end of the next run
    /// @throws NullPointerException     if `spec` is `null`
    /// @throws IllegalArgumentException if `targetValue` or the property's current value is non-finite, or if the
    ///                                  target lies outside this transition's configured bounds
    public void configure(M3MotionSpec spec, double targetValue) {
        M3MotionSpec checkedSpec = Objects.requireNonNull(spec, "spec");
        if (!Double.isFinite(targetValue) || targetValue < minimumValue || targetValue > maximumValue) {
            throw new IllegalArgumentException("targetValue must be finite and inside the configured bounds");
        }

        double currentValue = property.get();
        if (!Double.isFinite(currentValue)) {
            throw new IllegalArgumentException("the property's current value must be finite");
        }

        double previousElapsedSeconds = previousElapsedSeconds();
        if (configured && Double.isFinite(previousElapsedSeconds)) {
            double previousValue = channel.valueAt(previousElapsedSeconds);
            double previousVelocity = channel.velocityAt(previousElapsedSeconds);
            if ((previousValue <= minimumValue && previousVelocity < 0.0)
                    || (previousValue >= maximumValue && previousVelocity > 0.0)) {
                previousElapsedSeconds = Double.POSITIVE_INFINITY;
            }
        }

        stop();
        channel.configure(currentValue, targetValue, checkedSpec, previousElapsedSeconds);
        configured = true;
        setCycleDuration(Duration.seconds(channel.getDurationSeconds()));
        setInterpolator(Interpolator.LINEAR);
    }

    /// Applies the scalar channel value for the current pulse.
    @Override
    protected void interpolate(double fraction) {
        if (!configured) {
            return;
        }

        double durationSeconds = channel.getDurationSeconds();
        double elapsedSeconds = fraction >= 1.0 || durationSeconds <= 0.0
                ? Double.POSITIVE_INFINITY
                : Math.max(0.0, fraction) * durationSeconds;
        property.set(clamp(channel.valueAt(elapsedSeconds)));
    }

    /// Returns the elapsed time of the active run, or positive infinity when no velocity should be retained.
    private double previousElapsedSeconds() {
        return getStatus() == Animation.Status.STOPPED
                ? Double.POSITIVE_INFINITY
                : Math.max(0.0, getCurrentTime().toSeconds());
    }

    /// Clamps a rendered value to this transition's configured inclusive bounds.
    private double clamp(double value) {
        return Math.max(minimumValue, Math.min(maximumValue, value));
    }
}
