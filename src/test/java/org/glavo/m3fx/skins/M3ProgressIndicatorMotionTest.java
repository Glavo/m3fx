// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.animation.M3Motion;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/// Verifies deterministic AndroidX Material 3 circular progress motion and wave propagation.
@NotNullByDefault
final class M3ProgressIndicatorMotionTest {
    /// The tolerance used for normalized progress and degree comparisons.
    private static final double EPSILON = 0.000_001;

    /// Verifies that the active sweep grows linearly and contracts with the standard easing.
    @Test
    void samplesCircularSweepKeyframes() {
        assertEquals(0.10, M3ProgressIndicatorSkin.indeterminateSweepFraction(0.0), EPSILON);
        assertEquals(0.485, M3ProgressIndicatorSkin.indeterminateSweepFraction(0.25), EPSILON);
        assertEquals(0.87, M3ProgressIndicatorSkin.indeterminateSweepFraction(0.5), EPSILON);

        double expectedContractingSweep = M3Motion.STANDARD.interpolate(0.87, 0.10, 0.5);
        double actualContractingSweep = M3ProgressIndicatorSkin.indeterminateSweepFraction(0.75);
        assertEquals(expectedContractingSweep, actualContractingSweep, EPSILON);
        assertNotEquals(0.485, actualContractingSweep, 0.01);
        assertEquals(0.10, M3ProgressIndicatorSkin.indeterminateSweepFraction(1.0), EPSILON);
    }

    /// Verifies the four 300 ms emphasized-decelerate quarter-turn pulses and their intervening holds.
    @Test
    void samplesCircularAdditionalRotationKeyframes() {
        double easedHalfTurn = M3Motion.EMPHASIZED_DECELERATE.interpolate(0.0, 90.0, 0.5);

        assertEquals(0.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.0), EPSILON);
        assertEquals(easedHalfTurn, M3ProgressIndicatorSkin.additionalRotationDegrees(0.025), EPSILON);
        assertNotEquals(45.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.025), 0.01);
        assertEquals(90.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.05), EPSILON);
        assertEquals(90.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.20), EPSILON);
        assertEquals(90.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.25), EPSILON);
        assertEquals(90.0 + easedHalfTurn,
                M3ProgressIndicatorSkin.additionalRotationDegrees(0.275), EPSILON);
        assertEquals(180.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.30), EPSILON);
        assertEquals(180.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.50), EPSILON);
        assertEquals(270.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.75), EPSILON);
        assertEquals(360.0, M3ProgressIndicatorSkin.additionalRotationDegrees(1.0), EPSILON);
    }

    /// Verifies that circular wave geometry uses a bounded whole-wave count and closes without a seam.
    @Test
    void resolvesStableWholeWaveGeometry() {
        assertEquals(9, M3ProgressIndicatorSkin.circularWaveCount(22.0, 15.0));
        assertEquals(5, M3ProgressIndicatorSkin.circularWaveCount(22.0, 1000.0));
        assertEquals(18, M3ProgressIndicatorSkin.circularWaveCount(22.0, 1.0));

        int waveCount = M3ProgressIndicatorSkin.circularWaveCount(22.0, 15.0);
        double startRadius = M3ProgressIndicatorSkin.circularWaveRadius(22.0, 1.6, waveCount, 0.0, 0.37);
        double endRadius = M3ProgressIndicatorSkin.circularWaveRadius(22.0, 1.6, waveCount, 1.0, 0.37);
        assertEquals(startRadius, endRadius, EPSILON);
    }

    /// Verifies linear one-wavelength-per-second phase propagation across an activity-cycle boundary.
    @Test
    void propagatesWavePhaseContinuouslyAcrossCycleBoundary() {
        double direct = M3ProgressIndicatorSkin.propagatedWavePhase(0.82, 0.98, 0.02, 6000.0);

        double split = M3ProgressIndicatorSkin.propagatedWavePhase(0.82, 0.98, 1.0, 6000.0);
        split = M3ProgressIndicatorSkin.propagatedWavePhase(split, 1.0, 0.0, 6000.0);
        split = M3ProgressIndicatorSkin.propagatedWavePhase(split, 0.0, 0.02, 6000.0);

        assertEquals(0.06, direct, EPSILON);
        assertEquals(direct, split, EPSILON);
        assertEquals(0.5,
                M3ProgressIndicatorSkin.propagatedWavePhase(0.0, 0.0, 1.0 / 12.0, 6000.0),
                EPSILON);
        assertEquals(0.42,
                M3ProgressIndicatorSkin.propagatedWavePhase(0.42, 0.2, 0.8, 0.0),
                EPSILON);
    }

    /// Verifies that increasing phase moves a crest forward along clockwise circular progress.
    @Test
    void movesWaveCrestForwardWithIncreasingPhase() {
        int waveCount = M3ProgressIndicatorSkin.circularWaveCount(22.0, 15.0);
        double initialCrestProgress = 0.25 / waveCount;
        double propagatedCrestProgress = 0.50 / waveCount;

        assertEquals(23.6,
                M3ProgressIndicatorSkin.circularWaveRadius(
                        22.0, 1.6, waveCount, initialCrestProgress, 0.0),
                EPSILON);
        assertEquals(23.6,
                M3ProgressIndicatorSkin.circularWaveRadius(
                        22.0, 1.6, waveCount, propagatedCrestProgress, 0.25),
                EPSILON);
    }
}
