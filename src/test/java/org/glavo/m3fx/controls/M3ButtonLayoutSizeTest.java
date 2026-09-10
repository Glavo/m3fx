// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies button interaction-target sizing without taking ownership of application dimensions.
@NotNullByDefault
final class M3ButtonLayoutSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Keeps explicitly assigned dimensions even when they equal the default interaction target.
    @ParameterizedTest
    @ValueSource(strings = {"button", "menu", "icon", "toggle"})
    void tokenChangesPreserveExplicitInteractionTarget(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            ButtonCase fixture = createCase(kind);
            Control button = fixture.control();
            button.setMinSize(48.0, 48.0);
            button.setPrefSize(48.0, 48.0);
            button.setMaxSize(48.0, 48.0);
            fixture.changeSize().run();
            assertDimensions(button, 48.0);
        });
    }

    /// Keeps the final bound dimensions after unbinding and later token changes.
    @ParameterizedTest
    @ValueSource(strings = {"button", "menu", "icon", "toggle"})
    void unbindingPreservesLastApplicationDimensions(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            ButtonCase fixture = createCase(kind);
            Control button = fixture.control();
            SimpleDoubleProperty dimension = new SimpleDoubleProperty(90.0);
            button.minWidthProperty().bind(dimension);
            button.minHeightProperty().bind(dimension);
            button.prefWidthProperty().bind(dimension);
            button.prefHeightProperty().bind(dimension);
            button.maxWidthProperty().bind(dimension);
            button.maxHeightProperty().bind(dimension);
            fixture.changeSize().run();
            button.minWidthProperty().unbind();
            button.minHeightProperty().unbind();
            button.prefWidthProperty().unbind();
            button.prefHeightProperty().unbind();
            button.maxWidthProperty().unbind();
            button.maxHeightProperty().unbind();
            fixture.changeSize().run();
            assertDimensions(button, 90.0);
        });
    }

    /// Restores token-derived dimensions without requiring a skin to perform sizing.
    @ParameterizedTest
    @ValueSource(strings = {"button", "menu", "icon", "toggle"})
    void resetRestoresComputedInteractionTarget(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            ButtonCase fixture = createCase(kind);
            Control button = fixture.control();
            button.setMinSize(30.0, 30.0);
            button.setPrefSize(30.0, 30.0);
            button.setMaxSize(30.0, 30.0);
            fixture.changeSize().run();
            button.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            button.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            button.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            assertEquals(80.0, button.minHeight(-1));
            assertEquals(80.0, button.prefHeight(-1));
            if (fixture.iconOnly()) {
                assertEquals(96.0, button.minWidth(-1));
                assertEquals(96.0, button.prefWidth(-1));
                assertEquals(96.0, button.maxWidth(-1));
                assertEquals(80.0, button.maxHeight(-1));
            } else {
                assertEquals(48.0, button.minWidth(-1));
            }
        });
    }

    /// Checks all six layout constraints against a shared expected dimension.
    private static void assertDimensions(Control button, double dimension) {
        assertEquals(dimension, button.minWidth(-1));
        assertEquals(dimension, button.minHeight(-1));
        assertEquals(dimension, button.prefWidth(-1));
        assertEquals(dimension, button.prefHeight(-1));
        assertEquals(dimension, button.maxWidth(-1));
        assertEquals(dimension, button.maxHeight(-1));
    }

    /// Creates representative command, menu, and icon buttons with repeatable token changes.
    private static ButtonCase createCase(String kind) {
        if (kind.equals("toggle")) {
            M3IconToggleButton button = new M3IconToggleButton();
            return new ButtonCase(button, () -> {
                button.setContainerHeight(40.0);
                button.setContainerHeight(80.0);
                button.setContainerWidth(96.0);
            }, true);
        }
        M3ButtonBase button = switch (kind) {
            case "button" -> new M3Button("Action");
            case "menu" -> new M3MenuButton("Action");
            case "icon" -> new M3IconButton();
            default -> throw new IllegalArgumentException("Unknown button kind: " + kind);
        };
        return new ButtonCase(button, () -> {
            button.setContainerHeight(40.0);
            button.setContainerHeight(80.0);
            if (button instanceof M3IconButton iconButton) {
                iconButton.setContainerWidth(96.0);
            }
        }, button instanceof M3IconButton);
    }

    /// Supplies a button and token changes for interaction-target sizing.
    ///
    /// @param control the button under test
    /// @param changeSize the repeatable token changes
    /// @param iconOnly whether width and maximum dimensions follow icon container tokens
    @NotNullByDefault
    private record ButtonCase(Control control, Runnable changeSize, boolean iconOnly) {
    }
}
