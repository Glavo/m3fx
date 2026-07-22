// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.testing.Tier3Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies pulse-driven text-input presentation motion in a real JavaFX window.
@NotNullByDefault
final class M3TextInputMotionTest {
    /// The tolerance used when a visual translation has reached its exact target.
    private static final double SETTLED_EPSILON = 1.0e-6;

    /// The tolerance for scene-space label placement after motion has settled.
    private static final double POSITION_EPSILON = 1.0e-3;

    /// Starts the JavaFX toolkit before creating real windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes windows left open by a failed motion assertion.
    @AfterEach
    void closeWindows() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : java.util.List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
        });
    }

    /// Verifies that floating labels do not cross or recoil from their final baseline.
    @Test
    @Tier3Test
    void floatingLabelSettlesWithoutVerticalRecoil() throws InterruptedException {
        assertFloatingLabelMotion(M3Profile.BASELINE_2021);
        assertFloatingLabelMotion(M3Profile.EXPRESSIVE_2025);
    }

    /// Exercises one profile and verifies the label position over consecutive JavaFX pulses.
    private static void assertFloatingLabelMotion(M3Profile profile) throws InterruptedException {
        MotionScene scene = createMotionScene(profile);
        double[] minimumTranslateY = {Double.POSITIVE_INFINITY};
        double[] settledMinY = {Double.POSITIVE_INFINITY};
        double[] settledMaxY = {Double.NEGATIVE_INFINITY};

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        scene.root.applyCss();
                        scene.root.layout();
                        double translateY = scene.label.getTranslateY();
                        minimumTranslateY[0] = Math.min(minimumTranslateY[0], translateY);
                        if (Math.abs(translateY) <= SETTLED_EPSILON) {
                            Bounds bounds = scene.label.localToScene(scene.label.getBoundsInLocal());
                            settledMinY[0] = Math.min(settledMinY[0], bounds.getMinY());
                            settledMaxY[0] = Math.max(settledMaxY[0], bounds.getMinY());
                        } else {
                            settledMinY[0] = Double.POSITIVE_INFINITY;
                            settledMaxY[0] = Double.NEGATIVE_INFINITY;
                        }
                        return scene.field.isFocused() && Math.abs(translateY) <= SETTLED_EPSILON;
                    },
                    4,
                    () -> profile + " floating label did not settle",
                    scene.field::requestFocus,
                    () -> {
                        assertTrue(minimumTranslateY[0] >= -SETTLED_EPSILON,
                                () -> profile + " floating label crossed its final baseline: translateY="
                                        + minimumTranslateY[0]);
                        assertEquals(0.0, scene.label.getTranslateY(), SETTLED_EPSILON);
                        assertTrue(settledMaxY[0] - settledMinY[0] <= POSITION_EPSILON,
                                () -> profile + " floating label moved after settling: minY="
                                        + settledMinY[0] + ", maxY=" + settledMaxY[0]);
                    }
            );
        } finally {
            scene.close();
        }
    }

    /// Creates a themed, shown text field whose label begins in the resting state.
    private static MotionScene createMotionScene(M3Profile profile) {
        MotionScene[] result = new MotionScene[1];
        FxTestUtils.runOnFxThread(() -> {
            M3Button initialFocus = new M3Button("Initial focus");
            M3TextField field = new M3TextField();
            field.setVariant(M3TextInputVariant.OUTLINED);
            M3TextInputLayout layout = new M3TextInputLayout(field, "Project name");
            VBox root = new VBox(16.0, initialFocus, layout);
            Scene fxScene = new Scene(root, 420.0, 180.0);
            Stage stage = new Stage();
            stage.setScene(fxScene);
            M3ThemeManager.install(fxScene, M3Theme.fromSeed(
                    Color.web("#6750A4"),
                    profile,
                    Brightness.LIGHT
            ));
            M3MotionSettings.setReducedMotionRequested(root, false);
            stage.show();
            initialFocus.requestFocus();
            root.applyCss();
            root.layout();

            Label label = assertInstanceOf(
                    Label.class,
                    layout.lookup("." + M3TextInputLayout.LABEL_STYLE_CLASS)
            );
            assertTrue(initialFocus.isFocused());
            assertEquals(0.0, label.getTranslateY(), SETTLED_EPSILON);
            result[0] = new MotionScene(stage, root, field, label);
        });
        return Objects.requireNonNull(result[0], "motion scene");
    }

    /// Holds the nodes used by one real-window floating-label motion run.
    ///
    /// @param stage the window presenting the field
    /// @param root  the scene root
    /// @param field the field receiving focus
    /// @param label the animated floating label
    private record MotionScene(Stage stage, VBox root, M3TextField field, Label label) {
        /// Closes the window and clears its local motion override.
        private void close() {
            FxTestUtils.runOnFxThread(() -> {
                M3MotionSettings.setReducedMotionRequested(root, false);
                stage.close();
            });
        }
    }
}
