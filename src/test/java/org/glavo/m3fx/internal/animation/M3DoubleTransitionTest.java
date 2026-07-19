// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3SpringParameters;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3SpringSolver;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests reusable scalar motion shared by Material control skins.
@NotNullByDefault
final class M3DoubleTransitionTest {
    /// Starts the JavaFX toolkit before tests exercise animation timing.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that a spring derives its cycle duration from the configured visibility threshold.
    @Test
    void springUsesPhysicalSettlingDuration() {
        FxTestUtils.runOnFxThread(() -> {
            DoubleProperty value = new SimpleDoubleProperty(0.0);
            M3DoubleTransition transition = normalizedTransition(value);
            M3MotionSpec spec = M3MotionScheme.expressive().defaultSpatial();

            transition.configure(spec, 1.0);

            M3SpringParameters spring = Objects.requireNonNull(spec.springParameters(), "spring parameters");
            double expectedSeconds = M3SpringSolver.estimateDurationSeconds(
                    -1.0,
                    0.0,
                    M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
                    spring
            );
            assertEquals(expectedSeconds * 1000.0, transition.getCycleDuration().toMillis(), 1.0e-6);

            M3Animation.finish(transition);
            assertEquals(1.0, value.get(), 0.0);
        });
    }

    /// Verifies that retargeting a running spring carries its velocity into the new run.
    @Test
    void runningSpringPreservesVelocityWhenRetargeted() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            new Scene(owner, 100.0, 40.0);
            M3MotionSpec spec = M3MotionScheme.expressive().defaultSpatial();
            DoubleProperty movingValue = new SimpleDoubleProperty(0.0);
            M3DoubleTransition movingTransition = normalizedTransition(movingValue);

            movingTransition.configure(spec, 1.0);
            M3Animation.playFromStart(owner, movingTransition);
            movingTransition.jumpTo(Duration.millis(movingTransition.getCycleDuration().toMillis() * 0.2));
            assertEquals(Animation.Status.RUNNING, movingTransition.getStatus());
            double retargetStart = movingValue.get();

            movingTransition.configure(spec, 0.0);
            double movingRetargetDuration = movingTransition.getCycleDuration().toMillis();

            DoubleProperty restingValue = new SimpleDoubleProperty(retargetStart);
            M3DoubleTransition restingTransition = normalizedTransition(restingValue);
            restingTransition.configure(spec, 0.0);

            assertTrue(movingRetargetDuration > restingTransition.getCycleDuration().toMillis());
            M3Animation.finish(movingTransition);
            assertEquals(0.0, movingValue.get(), 0.0);
        });
    }

    /// Verifies bounded spring output and duration-based fallback behavior.
    @Test
    void honorsBoundsAndDurationBasedFallback() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            new Scene(owner, 100.0, 40.0);
            DoubleProperty value = new SimpleDoubleProperty(0.0);
            M3DoubleTransition transition = normalizedTransition(value);
            M3MotionSpec springSpec = M3MotionScheme.expressive().fastSpatial();

            transition.configure(springSpec, 1.0);
            M3Animation.playFromStart(owner, transition);
            double durationMillis = transition.getCycleDuration().toMillis();
            for (int sample = 0; sample <= 20; sample++) {
                transition.jumpTo(Duration.millis(durationMillis * sample / 20.0));
                assertTrue(value.get() >= 0.0 && value.get() <= 1.0);
            }
            transition.stop();

            M3MotionSpec fallbackSpec = M3MotionSpec.of(
                    Duration.millis(240.0),
                    M3MotionEasing.STANDARD_SPATIAL
            );
            value.set(0.25);
            transition.configure(fallbackSpec, 0.75);
            assertEquals(240.0, transition.getCycleDuration().toMillis(), 0.0);
            M3Animation.finish(transition);
            assertEquals(0.75, value.get(), 0.0);

            assertThrows(IllegalArgumentException.class, () -> transition.configure(fallbackSpec, 1.1));
            assertThrows(IllegalArgumentException.class, () -> transition.configure(fallbackSpec, Double.NaN));
        });
    }

    /// Creates the bounded normalized transition used by these tests.
    private static M3DoubleTransition normalizedTransition(DoubleProperty property) {
        return new M3DoubleTransition(
                property,
                M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
                0.0,
                1.0
        );
    }
}
