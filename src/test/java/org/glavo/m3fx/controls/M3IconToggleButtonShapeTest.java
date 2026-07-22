// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies token-controlled shape states of square Material toggle icon buttons.
///
/// The tests use a detached JavaFX scene so they exercise the normal CSS cascade without incurring real-window
/// rendering cost. They cover the fallback cascade and the distinct baseline and Expressive state-shape contracts.
@NotNullByDefault
final class M3IconToggleButtonShapeTest {
    /// The shape value used by a small square icon button at rest.
    private static final double SMALL_SQUARE_SHAPE = 12.0;

    /// The pressed square shape supplied by Material Expressive tokens.
    private static final double EXPRESSIVE_PRESSED_SHAPE = 8.0;

    /// The fully rounded shape supplied by Material tokens.
    private static final double FULL_SHAPE = 999.0;

    /// Starts the JavaFX toolkit before detached scenes are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that the fallback stylesheet preserves an explicitly square toggle across all interaction states.
    @Test
    void fallbackSquareToggleKeepsItsConfiguredShape() {
        assertSquareStateShapes(null, SMALL_SQUARE_SHAPE, SMALL_SQUARE_SHAPE, SMALL_SQUARE_SHAPE);
    }

    /// Verifies that baseline tokens preserve an explicitly square toggle across all interaction states.
    @Test
    void baselineSquareToggleKeepsItsConfiguredShape() {
        assertSquareStateShapes(
                M3Profile.BASELINE_2021,
                SMALL_SQUARE_SHAPE,
                SMALL_SQUARE_SHAPE,
                SMALL_SQUARE_SHAPE
        );
    }

    /// Verifies that Expressive tokens apply their documented pressed and selected square-toggle shapes.
    @Test
    void expressiveSquareToggleUsesItsStateShapes() {
        assertSquareStateShapes(
                M3Profile.EXPRESSIVE_2025,
                SMALL_SQUARE_SHAPE,
                EXPRESSIVE_PRESSED_SHAPE,
                FULL_SHAPE
        );
    }

    /// Applies and verifies one profile's resting, pressed, released, and selected shape targets.
    ///
    /// @param profile       the optional theme profile, or `null` to exercise fallback CSS
    /// @param restingShape  the expected unselected resting shape
    /// @param pressedShape  the expected armed shape
    /// @param selectedShape the expected selected resting shape
    private static void assertSquareStateShapes(
            @Nullable M3Profile profile,
            double restingShape,
            double pressedShape,
            double selectedShape
    ) {
        ShapeFixture fixture = createFixture(profile);
        try {
            FxTestUtils.runOnFxThread(() -> {
                assertContainerShape(restingShape, fixture.button);

                fixture.button.arm();
                fixture.root.applyCss();
                assertTrue(fixture.button.isArmed());
                assertContainerShape(pressedShape, fixture.button);

                fixture.button.disarm();
                fixture.root.applyCss();
                assertContainerShape(restingShape, fixture.button);

                fixture.button.setSelected(true);
                fixture.root.applyCss();
                assertContainerShape(selectedShape, fixture.button);

                fixture.button.arm();
                fixture.root.applyCss();
                assertContainerShape(pressedShape, fixture.button);

                fixture.button.disarm();
                fixture.root.applyCss();
                assertContainerShape(selectedShape, fixture.button);
            });
        } finally {
            disposeFixture(fixture);
        }
    }

    /// Creates a square small toggle icon button in a detached scene.
    ///
    /// @param profile the optional profile to install, or `null` to use fallback CSS
    /// @return the initialized fixture
    private static ShapeFixture createFixture(@Nullable M3Profile profile) {
        ShapeFixture[] result = new ShapeFixture[1];
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButton button = new M3IconToggleButton(new M3Icon("favorite"));
            button.setSize(M3ButtonSize.SMALL);
            button.setButtonShape(M3ButtonShape.SQUARE);

            StackPane root = new StackPane(button);
            Scene scene = new Scene(root, 120.0, 120.0);
            if (profile != null) {
                M3ThemeManager.install(scene, M3Theme.fromSeed(Color.web("#6750A4"), profile, Brightness.LIGHT));
            }
            root.applyCss();
            root.layout();
            result[0] = new ShapeFixture(root, button);
        });
        return Objects.requireNonNull(result[0], "fixture");
    }

    /// Detaches a fixture scene so that its controls and stylesheet listeners can be collected.
    ///
    /// @param fixture the fixture to detach
    private static void disposeFixture(ShapeFixture fixture) {
        FxTestUtils.runOnFxThread(() -> {
            Scene scene = Objects.requireNonNull(fixture.root.getScene(), "fixture scene");
            scene.setRoot(new StackPane());
        });
    }

    /// Verifies the currently resolved container-shape target.
    ///
    /// @param expected the expected shape in logical pixels
    /// @param button   the button to inspect
    private static void assertContainerShape(double expected, M3IconToggleButton button) {
        assertEquals(expected, button.getContainerShape(), 0.0001);
    }

    /// Holds the nodes required to verify and dispose one CSS fixture.
    ///
    /// @param root   the fixture root
    /// @param button the square toggle icon button
    private record ShapeFixture(StackPane root, M3IconToggleButton button) {
    }
}