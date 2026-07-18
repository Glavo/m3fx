// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.testing.Tier3Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies pulse-driven Material Expressive shape motion across the button families that expose morphing states.
@NotNullByDefault
final class M3ButtonShapeMotionTest {
    /// The duration that keeps intermediate corner frames observable across slower test machines.
    private static final Duration OBSERVABLE_MORPH_DURATION = Duration.millis(400.0);

    /// The active real-window scene, or `null` between tests.
    private @Nullable ShapeMotionScene testScene;

    /// Starts the JavaFX toolkit before a real window is created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes the real window and clears local motion settings after each test.
    @AfterEach
    void closeTestScene() {
        @Nullable ShapeMotionScene scene = testScene;
        testScene = null;
        if (scene != null) {
            scene.close();
        }
    }

    /// Verifies press, release, selection, reversal, and connected-corner morphs through intermediate frames.
    @Test
    @Tier3Test
    void buttonFamiliesMorphThroughIntermediateFrames() throws InterruptedException {
        ShapeMotionScene scene = showShapeMotionScene();
        testScene = scene;

        assertNull(scene.commandButton.getShape());
        assertEquals(999.0, backgroundTopLeftRadius(scene.commandButton), 0.0001);
        awaitIntermediateCorner(
                scene,
                scene.commandButton,
                8,
                10.0,
                20.0,
                () -> scene.commandButton.fireEvent(primaryMouseEvent(
                        MouseEvent.MOUSE_PRESSED,
                        scene.commandButton.getWidth() / 2.0,
                        scene.commandButton.getHeight() / 2.0,
                        true
                )),
                "command button press"
        );
        awaitSettledShape(scene, scene.commandButton, 10.0, "command button pressed settle");
        awaitIntermediateCorner(
                scene,
                scene.commandButton,
                8,
                10.0,
                20.0,
                () -> scene.commandButton.fireEvent(primaryMouseEvent(
                        MouseEvent.MOUSE_RELEASED,
                        scene.commandButton.getWidth() / 2.0,
                        scene.commandButton.getHeight() / 2.0,
                        false
                )),
                "command button release"
        );
        awaitSettledShape(scene, scene.commandButton, 999.0, "command button released settle");

        awaitIntermediateCorner(
                scene,
                scene.toggleButton,
                8,
                12.0,
                20.0,
                () -> scene.toggleButton.setSelected(true),
                "toggle button selection"
        );
        awaitSettledShape(scene, scene.toggleButton, 12.0, "toggle button selected settle");
        awaitIntermediateCorner(
                scene,
                scene.toggleButton,
                8,
                12.0,
                20.0,
                () -> scene.toggleButton.setSelected(false),
                "toggle button deselection"
        );
        awaitSettledShape(scene, scene.toggleButton, 999.0, "toggle button deselected settle");

        awaitIntermediateCorner(
                scene,
                scene.groupedButton,
                2,
                6.0,
                10.0,
                scene.groupedButton::arm,
                "connected button inner-corner press"
        );
        awaitSettledShape(scene, scene.groupedButton, 20.0, "connected button pressed settle");
        FxTestUtils.runOnFxThread(() ->
                assertEquals(6.0, backgroundTopRightRadius(scene.groupedButton), 0.0001));
        awaitIntermediateCorner(
                scene,
                scene.groupedButton,
                2,
                6.0,
                10.0,
                scene.groupedButton::disarm,
                "connected button inner-corner release"
        );
        awaitSettledShape(scene, scene.groupedButton, 20.0, "connected button released settle");
        FxTestUtils.runOnFxThread(() ->
                assertEquals(10.0, backgroundTopRightRadius(scene.groupedButton), 0.0001));
    }

    /// Verifies that retargeting a running selection morph preserves its current path and reverses continuously.
    @Test
    @Tier3Test
    void toggleShapeMorphReversesWithoutRestartingFromAnEndpoint() throws InterruptedException {
        ShapeMotionScene scene = showShapeMotionScene();
        testScene = scene;
        awaitIntermediateCorner(
                scene,
                scene.toggleButton,
                8,
                12.0,
                20.0,
                () -> scene.toggleButton.setSelected(true),
                "toggle button selection before reversal"
        );

        Path[] activeShape = new Path[1];
        double[] reversalStartRadius = new double[1];
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    applyShapeCss(scene, scene.toggleButton);
                    Path shape = Objects.requireNonNull(activeShape[0], "active shape");
                    if (scene.toggleButton.getShape() != shape) {
                        return false;
                    }
                    double radius = pathCornerRadius(shape, 8);
                    return radius > reversalStartRadius[0] + 0.1 && radius < 20.0;
                },
                1,
                () -> {
                    Path shape = assertInstanceOf(Path.class, scene.toggleButton.getShape());
                    activeShape[0] = shape;
                    reversalStartRadius[0] = pathCornerRadius(shape, 8);
                    scene.toggleButton.setSelected(false);
                    applyShapeCss(scene, scene.toggleButton);
                    assertSame(shape, scene.toggleButton.getShape(),
                            "retargeting should keep the retained surface path");
                    assertEquals(reversalStartRadius[0], pathCornerRadius(shape, 8), 0.0001,
                            "retargeting should start from the rendered radius");
                },
                () -> {
                    Path shape = Objects.requireNonNull(activeShape[0], "active shape");
                    assertSame(shape, scene.toggleButton.getShape());
                    assertTrue(pathCornerRadius(shape, 8) > reversalStartRadius[0]);
                }
        );
        awaitSettledShape(scene, scene.toggleButton, 999.0, "reversed toggle button settle");
    }

    /// Verifies that reduced motion applies final CSS corners without installing a temporary surface shape.
    @Test
    @Tier3Test
    void reducedMotionSettlesShapeChangesSynchronously() {
        ShapeMotionScene scene = showShapeMotionScene();
        testScene = scene;
        FxTestUtils.runOnFxThread(() -> {
            M3MotionSettings.setReducedMotionRequested(scene.root, true);
            scene.toggleButton.setSelected(true);
            applyShapeCss(scene, scene.toggleButton);
            assertNull(scene.toggleButton.getShape());
            assertEquals(12.0, backgroundTopLeftRadius(scene.toggleButton), 0.0001);

            scene.toggleButton.setSelected(false);
            applyShapeCss(scene, scene.toggleButton);
            assertNull(scene.toggleButton.getShape());
            assertEquals(999.0, backgroundTopLeftRadius(scene.toggleButton), 0.0001);
        });
    }

    /// Creates and shows the controls used by the shape-motion matrix.
    private static ShapeMotionScene showShapeMotionScene() {
        ShapeMotionScene[] result = new ShapeMotionScene[1];
        FxTestUtils.runOnFxThread(() -> {
            M3Button commandButton = new M3Button("Create", M3ButtonVariant.FILLED);
            commandButton.setButtonShape(M3ButtonShape.ROUND);

            M3IconToggleButton toggleButton = new M3IconToggleButton(new M3Icon("favorite"));
            toggleButton.setVariant(M3IconToggleButtonVariant.TONAL);
            toggleButton.setButtonShape(M3ButtonShape.ROUND);

            M3Button groupedButton = new M3Button("Day", M3ButtonVariant.TONAL);
            M3Button groupedTrailingButton = new M3Button("Week", M3ButtonVariant.TONAL);
            M3ButtonGroup buttonGroup = new M3ButtonGroup();
            buttonGroup.setVariant(M3ButtonGroupVariant.CONNECTED);
            buttonGroup.getItems().setAll(groupedButton, groupedTrailingButton);

            HBox root = new HBox(24.0, commandButton, toggleButton, buttonGroup);
            Scene scene = new Scene(root, 640.0, 180.0);
            Stage stage = new Stage();
            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750A4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            M3MotionSettings.setReducedMotionRequested(root, false);
            FxTestUtils.setMotionScheme(root, observableMotionScheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();

            result[0] = new ShapeMotionScene(
                    stage,
                    root,
                    commandButton,
                    toggleButton,
                    groupedButton
            );
        });
        return Objects.requireNonNull(result[0], "shape motion scene");
    }

    /// Waits for one concrete button node to expose an intermediate path radius.
    private static void awaitIntermediateCorner(
            ShapeMotionScene scene,
            Node button,
            int pathElementIndex,
            double lowerExclusive,
            double upperExclusive,
            Runnable stateChange,
            String description
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    applyShapeCss(scene, button);
                    double radius = activeCornerRadius(button, pathElementIndex);
                    return radius > lowerExclusive && radius < upperExclusive;
                },
                1,
                () -> {
                    stateChange.run();
                    applyShapeCss(scene, button);
                },
                () -> {
                    Path shape = Objects.requireNonNull(
                            assertInstanceOf(Path.class, buttonShape(button), description + " shape"),
                            description + " shape"
                    );
                    double radius = pathCornerRadius(shape, pathElementIndex);
                    assertTrue(radius > lowerExclusive && radius < upperExclusive,
                            () -> description + " radius=" + radius);
                }
        );
    }

    /// Waits until a transition returns outline ownership to CSS and verifies one resting corner.
    private static void awaitSettledShape(
            ShapeMotionScene scene,
            Node button,
            double expectedTopLeftRadius,
            String description
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    applyShapeCss(scene, button);
                    return buttonShape(button) == null;
                },
                2,
                () -> {
                },
                () -> {
                    assertNull(buttonShape(button), description + " should release the temporary path");
                    assertEquals(expectedTopLeftRadius, backgroundTopLeftRadius(button), 0.0001, description);
                }
        );
    }

    /// Applies pending CSS and layout work for one tested button.
    private static void applyShapeCss(ShapeMotionScene scene, Node button) {
        scene.root.applyCss();
        scene.root.layout();
        if (button instanceof javafx.scene.Parent parent) {
            parent.layout();
        }
    }

    /// Returns the temporary region shape currently installed on a tested button.
    private static @Nullable Node buttonShape(Node button) {
        return button instanceof javafx.scene.layout.Region region ? region.getShape() : null;
    }

    /// Returns one active path corner or `NaN` when no animated path is installed.
    private static double activeCornerRadius(Node button, int pathElementIndex) {
        @Nullable Node shape = buttonShape(button);
        return shape instanceof Path path ? pathCornerRadius(path, pathElementIndex) : Double.NaN;
    }

    /// Returns one corner radius from the retained rounded-rectangle path.
    private static double pathCornerRadius(Path path, int pathElementIndex) {
        PathElement element = path.getElements().get(pathElementIndex);
        return element instanceof ArcTo arc ? arc.getRadiusX() : 0.0;
    }

    /// Returns the first background fill's top-left radius.
    private static double backgroundTopLeftRadius(Node button) {
        javafx.scene.layout.Region region = assertInstanceOf(javafx.scene.layout.Region.class, button);
        CornerRadii radii = region.getBackground().getFills().get(0).getRadii();
        return radii.getTopLeftHorizontalRadius();
    }

    /// Returns the first background fill's top-right radius.
    private static double backgroundTopRightRadius(Node button) {
        javafx.scene.layout.Region region = assertInstanceOf(javafx.scene.layout.Region.class, button);
        CornerRadii radii = region.getBackground().getFills().get(0).getRadii();
        return radii.getTopRightHorizontalRadius();
    }

    /// Returns a motion scheme whose spatial transition remains observable for several pulses.
    private static M3MotionScheme observableMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec spec = M3MotionSpec.of(OBSERVABLE_MORPH_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.builder(standard)
                .fastSpatial(spec)
                .defaultSpatial(spec)
                .build();
    }

    /// Creates a primary-button event at one control-local coordinate.
    private static MouseEvent primaryMouseEvent(
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        return new MouseEvent(
                eventType,
                x,
                y,
                x,
                y,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                null
        );
    }

    /// Holds the real window and controls used by one shape-motion test.
    ///
    /// @param stage the real window that supplies JavaFX pulses
    /// @param root the root that owns local motion settings
    /// @param commandButton the ordinary button used for pressed and released morphs
    /// @param toggleButton the toggle icon button used for selected and deselected morphs
    /// @param groupedButton the first connected button used for asymmetric corner morphs
    @NotNullByDefault
    private record ShapeMotionScene(
            Stage stage,
            HBox root,
            M3Button commandButton,
            M3IconToggleButton toggleButton,
            M3Button groupedButton
    ) {
        /// Closes the window and removes local motion overrides.
        private void close() {
            FxTestUtils.runOnFxThread(() -> {
                M3MotionSettings.setReducedMotionRequested(root, false);
                FxTestUtils.clearMotionScheme(root);
                stage.close();
            });
        }
    }
}
