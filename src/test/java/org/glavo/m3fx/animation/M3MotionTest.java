// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests Material Design 3 motion constants.
@NotNullByDefault
final class M3MotionTest {
    /// Verifies that duration constants expose Material duration token values.
    @Test
    void durationConstantsExposeMaterialTokens() {
        assertEquals(50.0, M3Motion.SHORT1.toMillis(), 0.0001);
        assertEquals(200.0, M3Motion.SHORT4.toMillis(), 0.0001);
        assertEquals(250.0, M3Motion.MEDIUM1.toMillis(), 0.0001);
        assertEquals(600.0, M3Motion.LONG4.toMillis(), 0.0001);
        assertEquals(1000.0, M3Motion.EXTRA_LONG4.toMillis(), 0.0001);
    }

    /// Verifies that Material easing curves preserve endpoints and monotonic progress.
    @Test
    void materialEasingCurvesAreStable() {
        assertEquals(0.0, M3Motion.STANDARD.interpolate(0.0, 1.0, 0.0), 0.0001);
        assertEquals(1.0, M3Motion.STANDARD.interpolate(0.0, 1.0, 1.0), 0.0001);

        double early = M3Motion.STANDARD.interpolate(0.0, 1.0, 0.25);
        double middle = M3Motion.STANDARD.interpolate(0.0, 1.0, 0.5);
        double late = M3Motion.STANDARD.interpolate(0.0, 1.0, 0.75);

        assertTrue(early < middle);
        assertTrue(middle < late);
    }

    /// Verifies that accelerate and decelerate curves move progress in opposite directions.
    @Test
    void accelerateAndDecelerateCurvesHaveExpectedMidpointBias() {
        double accelerated = M3Motion.STANDARD_ACCELERATE.interpolate(0.0, 1.0, 0.5);
        double decelerated = M3Motion.STANDARD_DECELERATE.interpolate(0.0, 1.0, 0.5);

        assertTrue(accelerated < 0.5);
        assertTrue(decelerated > 0.5);
        assertTrue(accelerated < decelerated);
    }

    /// Verifies that emphasized easing advances quickly after the initial emphasis.
    @Test
    void emphasizedCurveAdvancesQuickly() {
        double emphasized = M3Motion.EMPHASIZED.interpolate(0.0, 1.0, 0.5);

        assertTrue(emphasized > 0.75);
    }

    /// Verifies that named easing constants expose their JavaFX interpolators.
    @Test
    void namedEasingsExposeInterpolators() {
        assertEquals("standard", M3MotionEasing.STANDARD.tokenName());
        assertEquals("emphasized", M3MotionEasing.EMPHASIZED.tokenName());
        assertSame(M3Motion.STANDARD, M3MotionEasing.STANDARD.interpolator());
        assertSame(M3Motion.EMPHASIZED, M3MotionEasing.EMPHASIZED.interpolator());
    }

    /// Verifies that standard and expressive motion schemes expose distinct semantic specs.
    @Test
    void motionSchemesExposeSemanticSpecs() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionScheme expressive = M3MotionScheme.expressive();

        assertEquals(M3Motion.SHORT1, standard.fastEffects().duration());
        assertEquals(M3Motion.SHORT3, standard.fastSpatial().duration());
        assertEquals(M3Motion.SHORT4, standard.defaultEffects().duration());
        assertEquals(M3MotionEasing.STANDARD, standard.defaultEffects().easing());
        assertEquals(M3Motion.MEDIUM3, standard.defaultSpatial().duration());
        assertEquals(M3MotionEasing.STANDARD, standard.defaultSpatial().easing());

        assertEquals(M3Motion.MEDIUM1, expressive.defaultEffects().duration());
        assertEquals(M3MotionEasing.EMPHASIZED, expressive.defaultEffects().easing());
        assertEquals(M3Motion.MEDIUM4, expressive.defaultSpatial().duration());
        assertEquals(M3MotionEasing.EMPHASIZED, expressive.defaultSpatial().easing());
        assertSame(M3Motion.EMPHASIZED, expressive.defaultSpatial().interpolator());
    }
}
