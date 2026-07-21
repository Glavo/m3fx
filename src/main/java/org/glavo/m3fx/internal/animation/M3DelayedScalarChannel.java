// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import org.glavo.m3fx.animation.M3MotionSpec;
import org.jetbrains.annotations.NotNullByDefault;

/// Adds a reusable start delay to one allocation-stable scalar animation channel.
///
/// The wrapped scalar channel retains physical velocity only after the previous run has passed its delay. Retargeting
/// while the previous effect is still waiting starts the new channel with zero velocity, which prevents delayed
/// effects from inheriting movement that was never rendered.
@NotNullByDefault
public final class M3DelayedScalarChannel {
    /// The reusable scalar interpolation and velocity state.
    private final M3ScalarChannel scalar;

    /// The value held before the current effect starts.
    private double delayedValue;

    /// The delay configured for the current run, in seconds.
    private double delaySeconds;

    /// Creates a delayed scalar channel.
    ///
    /// @param visibilityThreshold the finite, positive delta at which a spring is visually settled
    /// @throws IllegalArgumentException if `visibilityThreshold` is not finite and greater than zero
    public M3DelayedScalarChannel(double visibilityThreshold) {
        scalar = new M3ScalarChannel(visibilityThreshold);
    }

    /// Returns the complete effect duration including its delay.
    ///
    /// @return the non-negative duration, in seconds
    public double getDurationSeconds() {
        double scalarDuration = scalar.getDurationSeconds();
        return scalarDuration <= 0.0 ? 0.0 : delaySeconds + scalarDuration;
    }

    /// Reconfigures the effect from its currently rendered value.
    ///
    /// @param currentValue           the finite currently rendered value
    /// @param targetValue            the finite target value
    /// @param motionSpec             the new motion specification
    /// @param delaySeconds           the finite, non-negative new delay in seconds
    /// @param previousElapsedSeconds elapsed time in the previous run, or positive infinity when inactive
    /// @throws NullPointerException     if `motionSpec` is `null`
    /// @throws IllegalArgumentException if a numeric argument violates its documented range
    public void configure(
            double currentValue,
            double targetValue,
            M3MotionSpec motionSpec,
            double delaySeconds,
            double previousElapsedSeconds
    ) {
        if (!Double.isFinite(delaySeconds) || delaySeconds < 0.0) {
            throw new IllegalArgumentException("delaySeconds must be finite and non-negative");
        }
        if (Double.isNaN(previousElapsedSeconds) || previousElapsedSeconds < 0.0) {
            throw new IllegalArgumentException("previousElapsedSeconds must be non-negative");
        }
        double previousChannelElapsed = previousElapsedSeconds >= this.delaySeconds
                ? previousElapsedSeconds - this.delaySeconds
                : Double.POSITIVE_INFINITY;
        scalar.configure(currentValue, targetValue, motionSpec, previousChannelElapsed);
        delayedValue = currentValue;
        this.delaySeconds = delaySeconds;
    }

    /// Clears timing and velocity history at one settled value.
    ///
    /// @param value the finite settled value
    /// @throws IllegalArgumentException if `value` is not finite
    public void reset(double value) {
        delayedValue = value;
        delaySeconds = 0.0;
        scalar.reset(value);
    }

    /// Returns the value rendered at the supplied complete-run time.
    ///
    /// @param elapsedSeconds the non-negative elapsed time, in seconds
    /// @return the delayed or interpolated value
    /// @throws IllegalArgumentException if `elapsedSeconds` is negative or `NaN`
    public double valueAt(double elapsedSeconds) {
        if (Double.isNaN(elapsedSeconds) || elapsedSeconds < 0.0) {
            throw new IllegalArgumentException("elapsedSeconds must be non-negative");
        }
        return elapsedSeconds < delaySeconds
                ? delayedValue
                : scalar.valueAt(elapsedSeconds - delaySeconds);
    }
}
