// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies Material motion settings and animation lifecycle behavior.
@NotNullByDefault
final class M3AnimationTest {
    /// Starts the JavaFX toolkit before animation tests create transitions.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that disabled motion applies custom finite-transition end values and completion handlers.
    @Test
    void disabledMotionFinishesFiniteTransitionImmediately() {
        Pane owner = new Pane();
        M3MotionSettings.setAnimationsEnabled(owner, false);
        DoubleProperty value = new SimpleDoubleProperty(0.0);
        AtomicBoolean animationFinished = new AtomicBoolean(false);
        TestFiniteTransition transition = new TestFiniteTransition(value);
        transition.setOnFinished(event -> animationFinished.set(true));

        M3Animation.playFromStart(owner, transition);

        assertEquals(1.0, value.get(), 0.0001);
        assertTrue(animationFinished.get());
        assertEquals(Animation.Status.STOPPED, transition.getStatus());
    }

    /// Verifies that animation defaults resolve the theme motion scheme through the parent chain.
    @Test
    void resolvesThemeMotionSchemeFromParentChain() {
        Pane root = new Pane();
        Pane child = new Pane();
        root.getChildren().add(child);
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"), M3Profile.EXPRESSIVE_2025, Brightness.LIGHT);

        assertEquals(M3MotionEasing.STANDARD, M3Animation.defaultEffects(child).easing());

        M3ThemeManager.install(root, theme);

        assertEquals(M3MotionEasing.EMPHASIZED, M3Animation.defaultEffects(child).easing());
        assertEquals(400.0, M3Animation.defaultSpatial(child).duration().toMillis(), 0.0001);
        assertEquals(4000.0, M3Animation.motionBehavior(child).snackbarDisplayDuration().toMillis(), 0.0001);
        assertEquals(150.0, M3Animation.motionBehavior(child).subMenuHoverOpenDelay().toMillis(), 0.0001);
        assertEquals(900.0, M3Animation.motionBehavior(child).typeAheadResetDelay().toMillis(), 0.0001);
        assertEquals(650.0, M3Animation.motionBehavior(child).loadingIndicatorMorphInterval().toMillis(), 0.0001);
    }

    /// Verifies that a node-local motion scheme override takes precedence over an installed theme.
    @Test
    void nodeMotionSchemeOverrideTakesPrecedenceOverTheme() {
        Pane root = new Pane();
        Pane child = new Pane();
        root.getChildren().add(child);
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"), M3Profile.EXPRESSIVE_2025, Brightness.LIGHT);
        M3ThemeManager.install(root, theme);

        M3MotionSettings.setMotionScheme(child, M3MotionScheme.standard());

        assertEquals(M3MotionEasing.STANDARD, M3Animation.defaultEffects(child).easing());
    }

    /// Verifies that detached popup roots can inherit resolved motion settings from their owner controls.
    @Test
    void copiesResolvedMotionSettingsToDetachedTarget() {
        Pane source = new Pane();
        Pane target = new Pane();

        M3MotionSettings.setAnimationsEnabled(source, false);
        M3MotionSettings.setMotionScheme(source, M3MotionScheme.expressive());
        M3MotionSettings.setMotionBehavior(source, M3MotionBehavior.expressive());

        M3Animation.copyResolvedMotionSettings(source, target);

        assertFalse(M3MotionSettings.areAnimationsEnabled(target));
        assertEquals(M3MotionEasing.EMPHASIZED, M3Animation.defaultEffects(target).easing());
        assertEquals(4000.0, M3Animation.motionBehavior(target).snackbarDisplayDuration().toMillis(), 0.0001);
        assertEquals(150.0, M3Animation.motionBehavior(target).subMenuHoverOpenDelay().toMillis(), 0.0001);
        assertEquals(900.0, M3Animation.motionBehavior(target).typeAheadResetDelay().toMillis(), 0.0001);
        assertEquals(650.0, M3Animation.motionBehavior(target).loadingIndicatorMorphInterval().toMillis(), 0.0001);
    }

    /// Verifies that pause-transition duration changes restart only when the caller keeps the timer active.
    @Test
    void updatesPauseDurationWithoutMutatingRunningAnimationTimingInPlace() {
        FxTestUtils.runOnFxThread(() -> {
            PauseTransition transition = new PauseTransition(Duration.seconds(5.0));

            transition.playFromStart();
            assertEquals(Animation.Status.RUNNING, transition.getStatus());

            M3Animation.updatePauseDuration(transition, Duration.millis(250.0), true);

            assertEquals(Duration.millis(250.0), transition.getDuration());
            assertEquals(Animation.Status.RUNNING, transition.getStatus());

            M3Animation.updatePauseDuration(transition, Duration.millis(100.0), false);

            assertEquals(Duration.millis(100.0), transition.getDuration());
            assertEquals(Animation.Status.STOPPED, transition.getStatus());
        });
    }

    /// Finite transition used to verify synchronous reduced-motion completion.
    @NotNullByDefault
    private static final class TestFiniteTransition extends M3FiniteTransition {
        /// The test property receiving interpolated values.
        private final DoubleProperty value;

        /// Creates a finite transition for a test property.
        private TestFiniteTransition(DoubleProperty value) {
            this.value = value;
            setCycleDuration(Duration.millis(100.0));
        }

        /// Applies the current transition fraction to the test property.
        @Override
        protected void interpolate(double fraction) {
            value.set(fraction);
        }
    }
}
