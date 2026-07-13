// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.animation.M3Motion;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/// Verifies the deterministic AndroidX Material 3 circular progress keyframe sampling.
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

    /// Verifies the four 300 ms linear quarter-turn pulses and their intervening holds.
    @Test
    void samplesCircularAdditionalRotationKeyframes() {
        assertEquals(0.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.0), EPSILON);
        assertEquals(45.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.025), EPSILON);
        assertEquals(90.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.05), EPSILON);
        assertEquals(90.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.20), EPSILON);
        assertEquals(90.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.25), EPSILON);
        assertEquals(135.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.275), EPSILON);
        assertEquals(180.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.30), EPSILON);
        assertEquals(180.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.50), EPSILON);
        assertEquals(270.0, M3ProgressIndicatorSkin.additionalRotationDegrees(0.75), EPSILON);
        assertEquals(360.0, M3ProgressIndicatorSkin.additionalRotationDegrees(1.0), EPSILON);
    }
}
