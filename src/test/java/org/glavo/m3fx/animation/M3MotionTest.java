// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        assertSame(standard, M3MotionScheme.standard());
        assertSame(expressive, M3MotionScheme.expressive());
        assertEquals(M3Motion.SHORT3, standard.fastEffects().duration());
        assertEquals(M3Motion.MEDIUM3, standard.fastSpatial().duration());
        assertEquals(M3Motion.SHORT4, standard.defaultEffects().duration());
        assertEquals(M3MotionEasing.DEFAULT_EFFECTS, standard.defaultEffects().easing());
        assertEquals(M3Motion.LONG2, standard.defaultSpatial().duration());
        assertEquals(M3MotionEasing.STANDARD_SPATIAL, standard.defaultSpatial().easing());
        assertEquals(new M3SpringParameters(0.9, 700.0), standard.defaultSpatial().springParameters());

        assertEquals(M3Motion.SHORT4, expressive.defaultEffects().duration());
        assertEquals(M3MotionEasing.DEFAULT_EFFECTS, expressive.defaultEffects().easing());
        assertEquals(M3Motion.LONG2, expressive.defaultSpatial().duration());
        assertEquals(M3MotionEasing.EXPRESSIVE_DEFAULT_SPATIAL, expressive.defaultSpatial().easing());
        assertEquals(new M3SpringParameters(0.8, 380.0), expressive.defaultSpatial().springParameters());
        assertSame(M3Motion.EXPRESSIVE_DEFAULT_SPATIAL, expressive.defaultSpatial().interpolator());
    }

    /// Verifies spring parameter validation and duration-based specification identity.
    @Test
    void springSpecificationsExposeValidatedPhysicalParameters() {
        M3MotionSpec tween = M3MotionSpec.of(M3Motion.SHORT4, M3MotionEasing.STANDARD);
        M3MotionSpec spring = M3MotionSpec.spring(
                new M3SpringParameters(0.8, 380.0),
                M3Motion.LONG2,
                M3MotionEasing.EXPRESSIVE_DEFAULT_SPATIAL
        );

        assertNull(tween.springParameters());
        assertEquals(new M3SpringParameters(0.8, 380.0), spring.springParameters());
        assertThrows(IllegalArgumentException.class, () -> new M3SpringParameters(0.0, 380.0));
        assertThrows(IllegalArgumentException.class, () -> new M3SpringParameters(0.8, Double.NaN));
    }

    /// Verifies that standard and expressive motion behavior timings remain distinct where needed.
    @Test
    void motionBehaviorsExposeInteractionTimings() {
        M3MotionBehavior standard = M3MotionBehavior.standard();
        M3MotionBehavior expressive = M3MotionBehavior.expressive();

        assertSame(standard, M3MotionBehavior.standard());
        assertSame(expressive, M3MotionBehavior.expressive());
        assertEquals(Duration.millis(500.0), standard.tooltipShowDelay());
        assertEquals(Duration.millis(1500.0), standard.tooltipHideDelay());
        assertEquals(Duration.seconds(5.0), standard.tooltipShowDuration());
        assertEquals(Duration.seconds(10.0), standard.richTooltipShowDuration());
        assertEquals(Duration.millis(200.0), standard.subMenuHoverOpenDelay());
        assertEquals(Duration.millis(1750.0), standard.linearProgressIndeterminateCycleDuration());
        assertEquals(Duration.millis(6000.0), standard.circularProgressIndeterminateCycleDuration());

        assertEquals(Duration.millis(1500.0), expressive.tooltipHideDelay());
        assertEquals(Duration.millis(150.0), expressive.subMenuHoverOpenDelay());
        assertEquals(Duration.millis(150.0), expressive.subMenuHoverCloseDelay());
    }

    /// Verifies that motion builders copy complete defaults and override only named values.
    @Test
    void motionBuildersSupportTargetedOverrides() {
        M3MotionSpec customSpec = M3MotionSpec.of(Duration.millis(275.0), M3MotionEasing.EMPHASIZED);
        M3MotionScheme scheme = M3MotionScheme.builder(M3MotionScheme.standard())
                .defaultSpatial(customSpec)
                .build();
        M3MotionBehavior behavior = M3MotionBehavior.builder(M3MotionBehavior.expressive())
                .snackbarDisplayDuration(Duration.seconds(7.0))
                .build();

        assertNotSame(M3MotionScheme.standard(), scheme);
        assertNotSame(M3MotionBehavior.expressive(), behavior);
        assertSame(customSpec, scheme.defaultSpatial());
        assertEquals(M3MotionScheme.standard().fastEffects(), scheme.fastEffects());
        assertEquals(Duration.seconds(7.0), behavior.snackbarDisplayDuration());
        assertEquals(M3MotionBehavior.expressive().subMenuHoverOpenDelay(), behavior.subMenuHoverOpenDelay());
        assertThrows(
                IllegalArgumentException.class,
                () -> M3MotionBehavior.builder().tooltipShowDelay(Duration.INDEFINITE)
        );
    }
}
