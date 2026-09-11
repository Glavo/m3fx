// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies discrete snapping without overflow or lost low-order values in either slider control.
@NotNullByDefault
final class M3SliderSnappingTest {
    /// Starts JavaFX before constructing slider controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Keeps an exactly representable selection when its step index exceeds the double range.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void tinyStepDoesNotJumpToMaximum(boolean range) {
        assertSnaps(range, 0.0, 1.0, Double.MIN_VALUE, 0.5, 0.5);
    }

    /// Handles a finite selected value whose distance from the minimum overflows.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void overflowingOffsetRetainsSelectedStep(boolean range) {
        double step = Double.MAX_VALUE / 2.0;
        assertSnaps(range, -Double.MAX_VALUE, Double.MAX_VALUE, step, step, step);
    }

    /// Cancels the minimum against an overflowing step product before rounding the result to double.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void overflowingProductRetainsFiniteResult(boolean range) {
        double step = Double.MAX_VALUE * 0.6;
        double expected = Math.fma(2.0, step, -Double.MAX_VALUE);
        assertSnaps(range, -Double.MAX_VALUE, Double.MAX_VALUE, step, 0.0, expected);
    }

    /// Preserves small representable values when subtracting a distant minimum loses their low-order bits.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void distantMinimumDoesNotEraseSelection(boolean range) {
        assertSnaps(range, -0x1.0p60, 0x1.0p60, 1.0, 1.0, 1.0);
        assertSnaps(range, -0x1.0p60, 0x1.0p60, 1.0, -1.0, -1.0);
    }

    /// Retains round-to-even half-step behavior for ordinary and very large step indices.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void halfStepsRoundToEven(boolean range) {
        assertSnaps(range, 0.0, 10.0, 2.0, 1.0, 0.0);
        assertSnaps(range, 0.0, 10.0, 2.0, 3.0, 4.0);
        assertSnaps(range, -0x1.0p60, 0x1.0p60, 2.0, 1.0, 0.0);
        assertSnaps(range, -0x1.0p60, 0x1.0p60, 2.0, 3.0, 4.0);
    }

    /// Applies the same requested selection to a scalar slider or both range endpoints.
    private static void assertSnaps(boolean range, double min, double max, double step,
                                    double value, double expected) {
        FxTestUtils.runOnFxThread(() -> {
            if (range) {
                M3RangeSlider slider = new M3RangeSlider(min, max, min, max);
                slider.setStepSize(step);
                slider.setLowValue(value);
                assertEquals(expected, slider.getLowValue());
                slider.setHighValue(value);
                assertEquals(expected, slider.getHighValue());
            } else {
                M3Slider slider = new M3Slider(min, max, min);
                slider.setStepSize(step);
                slider.setValue(value);
                assertEquals(expected, slider.getValue());
            }
        });
    }
}
