// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.jetbrains.annotations.NotNullByDefault;

import java.math.BigDecimal;
import java.math.RoundingMode;

/// Computes values and normalized geometry shared by scalar and range sliders.
@NotNullByDefault
public final class M3SliderMath {
    /// The first step index at which double precision cannot represent every adjacent half-step.
    private static final double HALF_STEP_PRECISION_LIMIT = 0x1.0p52;

    /// Prevents utility class instantiation.
    private M3SliderMath() {
    }

    /// Computes a step-aligned value measured from the minimum, with even-index half-step ties.
    ///
    /// The result is rounded to a double and is not clamped to the slider bounds. Non-finite inputs retain ordinary
    /// floating-point propagation.
    ///
    /// @implNote Ordinary ranges use double arithmetic. Large step indices and non-finite intermediate results
    /// use exact arithmetic on the represented input values before the result is converted back to double.
    /// @param value the value already clamped to the slider bounds
    /// @param min the origin of the step grid
    /// @param step the positive step interval
    /// @return the snapped value, which may overflow and require clamping by the caller
    public static double snap(double value, double min, double step) {
        double index = Math.rint((value - min) / step);
        double snapped = min + index * step;
        if (!Double.isFinite(value) || !Double.isFinite(min) || !Double.isFinite(step)
                || step <= 0.0
                || (Math.abs(index) < HALF_STEP_PRECISION_LIMIT && Double.isFinite(snapped))) {
            return snapped;
        }

        // Exact binary-double values preserve the grid origin even when subtracting it loses low-order bits.
        BigDecimal origin = new BigDecimal(min);
        BigDecimal interval = new BigDecimal(step);
        BigDecimal stepIndex = new BigDecimal(value).subtract(origin).divide(interval, 0, RoundingMode.HALF_EVEN);
        return origin.add(stepIndex.multiply(interval)).doubleValue();
    }

    /// Converts a value to a position clamped to the unit interval.
    ///
    /// Finite ordered bounds are mapped without overflowing their span. Empty, inverted, or NaN ranges return
    /// zero. Otherwise a NaN value produces NaN, and non-finite bounds use ordinary floating-point propagation.
    ///
    /// @param value the value to locate
    /// @param min the lower range bound
    /// @param max the upper range bound
    /// @return the normalized position, or NaN when the ratio is undefined
    public static double position(double value, double min, double max) {
        double span = max - min;
        if (!(span > 0.0)) {
            return 0.0;
        }
        double position = Double.isInfinite(span) && Double.isFinite(min) && Double.isFinite(max)
                ? (value * 0.5 - min * 0.5) / (max * 0.5 - min * 0.5)
                : (value - min) / span;
        return Math.max(0.0, Math.min(1.0, position));
    }

    /// Converts a unit-interval position to a value between the supplied bounds.
    ///
    /// Positions outside the unit interval are clamped, and positions zero and one return the exact supplied
    /// endpoints. Finite bounds are interpolated without overflowing their span. A NaN position produces NaN;
    /// non-finite bounds use ordinary floating-point propagation at interior positions.
    ///
    /// @param position the normalized position
    /// @param min the value at position zero
    /// @param max the value at position one
    /// @return the interpolated value
    public static double valueAt(double position, double min, double max) {
        double clamped = Math.max(0.0, Math.min(1.0, position));
        if (clamped == 0.0) {
            return min;
        }
        if (clamped == 1.0) {
            return max;
        }
        double span = max - min;
        return Double.isInfinite(span) && Double.isFinite(min) && Double.isFinite(max)
                ? min * (1.0 - clamped) + max * clamped
                : min + span * clamped;
    }

    /// Returns the fraction of a range occupied by one step interval.
    ///
    /// Finite bounds are handled without overflowing their span. This method does not validate the range or
    /// interval; zero and non-finite arguments may produce an infinite or NaN result.
    ///
    /// @param step the step interval
    /// @param min the lower range bound
    /// @param max the upper range bound
    /// @return the step fraction, which may underflow to zero or overflow to infinity
    public static double stepFraction(double step, double min, double max) {
        double span = max - min;
        return Double.isInfinite(span) && Double.isFinite(min) && Double.isFinite(max)
                ? (step * 0.5) / (max * 0.5 - min * 0.5)
                : step / span;
    }
}
