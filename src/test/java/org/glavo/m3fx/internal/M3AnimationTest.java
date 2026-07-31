// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3SpringParameters;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
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
        M3MotionSettings.setReducedMotionRequested(owner, true);
        DoubleProperty value = new SimpleDoubleProperty(0.0);
        AtomicBoolean animationFinished = new AtomicBoolean(false);
        TestFiniteTransition transition = new TestFiniteTransition(value);
        transition.setOnFinished(event -> animationFinished.set(true));

        M3Animation.playFromStart(owner, transition);

        assertEquals(1.0, value.get(), 0.0001);
        assertTrue(animationFinished.get());
        assertEquals(Animation.Status.STOPPED, transition.getStatus());
    }

    /// Verifies finite transitions observe motion settings only for the duration of an active run.
    @Test
    void finiteTransitionObservesMotionSettingsOnlyWhileRunning() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            Scene scene = new Scene(owner);
            int ownerPropertyCount = owner.getProperties().size();
            int scenePropertyCount = scene.getProperties().size();
            DoubleProperty value = new SimpleDoubleProperty(0.0);
            TestFiniteTransition transition = new TestFiniteTransition(value);

            M3Animation.playFromStart(owner, transition);

            assertEquals(Animation.Status.RUNNING, transition.getStatus());
            assertTrue(owner.getProperties().size() > ownerPropertyCount);
            assertTrue(scene.getProperties().size() > scenePropertyCount);

            M3MotionSettings.setReducedMotionRequested(owner, true);

            assertEquals(Animation.Status.STOPPED, transition.getStatus());
            assertEquals(1.0, value.get(), 0.0001);
            M3MotionSettings.setReducedMotionRequested(owner, false);
            assertEquals(ownerPropertyCount, owner.getProperties().size());
            assertEquals(scenePropertyCount, scene.getProperties().size());
        }));
    }

    /// Verifies that tree visibility settles a finite transition before its scene acquires a window.
    @Test
    void hidingAncestorFinishesFiniteTransitionInSceneWithoutWindow() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane parent = new Pane(owner);
            Scene scene = new Scene(parent);
            DoubleProperty value = new SimpleDoubleProperty(0.0);
            AtomicBoolean animationFinished = new AtomicBoolean(false);
            TestFiniteTransition transition = new TestFiniteTransition(value);
            transition.setOnFinished(event -> animationFinished.set(true));

            M3Animation.playFromStart(owner, transition);
            assertEquals(Animation.Status.RUNNING, transition.getStatus());

            parent.setVisible(false);

            assertEquals(Animation.Status.STOPPED, transition.getStatus());
            assertEquals(1.0, value.get(), 0.0001);
            assertTrue(animationFinished.get());
            assertFalse(owner.hasProperties());
            assertFalse(scene.hasProperties());
        });
    }

    /// Verifies that iconifying a presenting stage settles a finite transition and releases its observers.
    @Tier2Test
    @Test
    void iconifyingWindowFinishesFiniteTransition() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Scene scene = new Scene(owner);
            Stage stage = new Stage();
            DoubleProperty value = new SimpleDoubleProperty(0.0);
            AtomicBoolean animationFinished = new AtomicBoolean(false);
            TestFiniteTransition transition = new TestFiniteTransition(value);
            transition.setOnFinished(event -> animationFinished.set(true));

            try {
                stage.setScene(scene);
                stage.show();
                M3Animation.playFromStart(owner, transition);
                assertEquals(Animation.Status.RUNNING, transition.getStatus());

                stage.setIconified(true);

                assertEquals(Animation.Status.STOPPED, transition.getStatus());
                assertEquals(1.0, value.get(), 0.0001);
                assertTrue(animationFinished.get());
                assertFalse(owner.hasProperties());
                assertFalse(scene.hasProperties());
            } finally {
                transition.stop();
                stage.close();
            }
        });
    }

    /// Verifies that hiding an owner ancestor settles a finite transition and releases its observers.
    @Tier2Test
    @Test
    void hidingAncestorFinishesFiniteTransition() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane parent = new Pane(owner);
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            DoubleProperty value = new SimpleDoubleProperty(0.0);
            AtomicBoolean animationFinished = new AtomicBoolean(false);
            TestFiniteTransition transition = new TestFiniteTransition(value);
            transition.setOnFinished(event -> animationFinished.set(true));

            try {
                stage.setScene(scene);
                stage.show();
                M3Animation.playFromStart(owner, transition);
                assertEquals(Animation.Status.RUNNING, transition.getStatus());

                parent.setVisible(false);

                assertEquals(Animation.Status.STOPPED, transition.getStatus());
                assertEquals(1.0, value.get(), 0.0001);
                assertTrue(animationFinished.get());
                assertFalse(owner.hasProperties());
                assertFalse(scene.hasProperties());
            } finally {
                transition.stop();
                stage.close();
            }
        });
    }

    /// Verifies that animation defaults resolve the theme motion scheme through the parent chain.
    @Test
    void resolvesThemeMotionSchemeFromParentChain() {
        Pane root = new Pane();
        Pane child = new Pane();
        root.getChildren().add(child);
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"), M3Profile.EXPRESSIVE_2025, Brightness.LIGHT);

        assertEquals(M3MotionEasing.DEFAULT_EFFECTS, M3Animation.defaultEffects(child).easing());

        M3ThemeManager.install(root, theme);

        assertEquals(M3MotionEasing.DEFAULT_EFFECTS, M3Animation.defaultEffects(child).easing());
        assertEquals(500.0, M3Animation.defaultSpatial(child).duration().toMillis(), 0.0001);
        assertEquals(4000.0, M3Animation.motionBehavior(child).snackbarDisplayDuration().toMillis(), 0.0001);
        assertEquals(150.0, M3Animation.motionBehavior(child).subMenuHoverOpenDelay().toMillis(), 0.0001);
        assertEquals(900.0, M3Animation.motionBehavior(child).typeAheadResetDelay().toMillis(), 0.0001);
        assertEquals(650.0, M3Animation.motionBehavior(child).loadingIndicatorMorphInterval().toMillis(), 0.0001);
    }

    /// Verifies that a locally installed theme takes precedence over an ancestor theme.
    @Test
    void localMotionThemeTakesPrecedenceOverAncestorTheme() {
        Pane root = new Pane();
        Pane child = new Pane();
        root.getChildren().add(child);
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"), M3Profile.EXPRESSIVE_2025, Brightness.LIGHT);
        M3ThemeManager.install(root, theme);

        FxTestUtils.setMotionScheme(child, M3MotionScheme.standard());

        assertEquals(M3MotionEasing.DEFAULT_EFFECTS, M3Animation.defaultEffects(child).easing());
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

    /// Verifies a scale spring ends at its channel threshold instead of waiting for its fallback horizon.
    @Test
    void nodeSpringUsesPhysicalScaleSettlingDuration() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            node.setScaleY(0.92);
            M3MotionSpec spec = M3MotionScheme.expressive().defaultSpatial();
            M3NodeTransition transition = new M3NodeTransition(node);

            transition.configure(
                    spec,
                    node.getOpacity(),
                    node.getScaleX(),
                    1.0,
                    node.getTranslateX(),
                    node.getTranslateY()
            );

            M3SpringParameters spring =
                    Objects.requireNonNull(spec.springParameters(), "spring parameters");
            double expectedSeconds = M3SpringSolver.estimateDurationSeconds(
                    -0.08,
                    0.0,
                    5.0e-4,
                    spring
            );
            assertEquals(expectedSeconds * 1000.0, transition.getCycleDuration().toMillis(), 1.0e-6);
            assertTrue(transition.getCycleDuration().lessThan(spec.duration()));

            M3Animation.finish(transition);

            assertEquals(1.0, node.getScaleY(), 0.0);
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
