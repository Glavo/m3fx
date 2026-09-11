// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3FloatingActionButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies floating-action-button dimensions across square and extended presentations.
@NotNullByDefault
final class M3FloatingActionButtonTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains explicitly assigned default dimensions through text and token changes.
    @Test
    void presentationChangesPreserveExplicitDimensions() {
        FxTestUtils.runOnFxThread(() -> {
            M3FloatingActionButton button = new M3FloatingActionButton();
            button.setMinSize(56.0, 56.0);
            button.setPrefSize(56.0, 56.0);
            button.setText("Create");
            assertDimensions(button, 56.0);
            button.setContainerSize(72.0);
            assertDimensions(button, 56.0);
            button.setText("");
            assertDimensions(button, 56.0);
        });
    }

    /// Keeps the last bound preferred dimensions after the binding is removed.
    @Test
    void unbindingDoesNotRestoreInternalDimensionOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            M3FloatingActionButton button = new M3FloatingActionButton();
            SimpleDoubleProperty dimension = new SimpleDoubleProperty(90.0);
            button.prefWidthProperty().bind(dimension);
            button.prefHeightProperty().bind(dimension);
            button.setText("Create");
            button.setContainerSize(80.0);
            button.prefWidthProperty().unbind();
            button.prefHeightProperty().unbind();
            button.setText("");
            assertEquals(90.0, button.prefWidth(-1));
            assertEquals(90.0, button.prefHeight(-1));
        });
    }

    /// Restores square dimensions and recomputes extended content widths as text changes.
    @Test
    void computedDimensionsTrackTextPresentation() {
        FxTestUtils.runOnFxThread(() -> {
            M3FloatingActionButton button = new M3FloatingActionButton();
            button.setContainerSize(64.0);
            button.setMinSize(10.0, 10.0);
            button.setPrefSize(20.0, 20.0);
            button.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            button.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            assertDimensions(button, 64.0);
            button.setSkin(new M3FloatingActionButtonSkin(button));
            assertDimensions(button, 64.0);
            button.setText("Create a new workspace");
            double extendedWidth = button.prefWidth(-1);
            assertTrue(extendedWidth > 64.0);
            assertEquals(64.0, button.prefHeight(-1));
            button.setText("Go");
            assertTrue(button.prefWidth(-1) < extendedWidth);
            button.setText(" \t\n");
            assertDimensions(button, 64.0);
            button.setText(null);
            assertDimensions(button, 64.0);
            button.setSkin(null);
        });
    }

    /// Checks the minimum and preferred dimensions of a square presentation.
    private static void assertDimensions(M3FloatingActionButton button, double size) {
        assertEquals(size, button.minWidth(-1));
        assertEquals(size, button.minHeight(-1));
        assertEquals(size, button.prefWidth(-1));
        assertEquals(size, button.prefHeight(-1));
    }
}
