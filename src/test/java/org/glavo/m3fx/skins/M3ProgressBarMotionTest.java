// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.animation.M3Motion;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies deterministic linear expressive wave propagation.
@NotNullByDefault
final class M3ProgressBarMotionTest {
    /// The tolerance used for normalized phase and displacement comparisons.
    private static final double EPSILON = 0.000_001;

    /// Verifies one-wavelength-per-second phase propagation across an activity-cycle boundary.
    @Test
    void propagatesWavePhaseContinuouslyAcrossCycleBoundary() {
        double direct = M3ProgressBarSkin.propagatedWavePhase(0.82, 0.98, 0.02, 1750.0);

        double split = M3ProgressBarSkin.propagatedWavePhase(0.82, 0.98, 1.0, 1750.0);
        split = M3ProgressBarSkin.propagatedWavePhase(split, 1.0, 0.0, 1750.0);
        split = M3ProgressBarSkin.propagatedWavePhase(split, 0.0, 0.02, 1750.0);

        assertEquals(0.89, direct, EPSILON);
        assertEquals(direct, split, EPSILON);
        assertEquals(
                0.5,
                M3ProgressBarSkin.propagatedWavePhase(0.0, 0.0, 2.0 / 7.0, 1750.0),
                EPSILON
        );
    }

    /// Verifies the AndroidX three-wavelength indeterminate offset and seamless cycle boundary.
    @Test
    void followsAndroidXIndeterminateWavePhase() {
        assertEquals(0.0, M3ProgressBarSkin.indeterminateWavePhase(0.0), EPSILON);
        assertEquals(0.0, M3ProgressBarSkin.indeterminateWavePhase(1.0), EPSILON);

        double easedQuarter = M3Motion.EMPHASIZED_ACCELERATE.interpolate(
                0.0,
                1.0,
                0.25
        );
        double expectedQuarter = easedQuarter * 3.0;
        expectedQuarter -= Math.floor(expectedQuarter);
        assertEquals(expectedQuarter, M3ProgressBarSkin.indeterminateWavePhase(0.25), EPSILON);

        double beforeBoundary = M3ProgressBarSkin.indeterminateWavePhase(0.999_999);
        double afterBoundary = M3ProgressBarSkin.indeterminateWavePhase(0.000_001);
        assertEquals(0.0, Math.min(beforeBoundary, 1.0 - beforeBoundary), 0.000_1);
        assertEquals(0.0, afterBoundary, 0.000_1);
    }

    /// Verifies that increasing phase advances a crest in the positive x direction.
    @Test
    void movesWaveCrestForwardWithIncreasingPhase() {
        double wavelength = 48.0;
        double amplitude = 3.0;

        assertEquals(
                amplitude,
                M3ProgressBarSkin.linearWaveDisplacement(12.0, wavelength, 0.0, amplitude),
                EPSILON
        );
        assertEquals(
                amplitude,
                M3ProgressBarSkin.linearWaveDisplacement(24.0, wavelength, 0.25, amplitude),
                EPSILON
        );
    }
}
