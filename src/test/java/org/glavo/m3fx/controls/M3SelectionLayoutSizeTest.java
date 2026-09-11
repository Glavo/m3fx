// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies selection-control heights without replacing application layout constraints.
@NotNullByDefault
final class M3SelectionLayoutSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains explicit heights even when they equal the default touch target.
    @ParameterizedTest
    @ValueSource(strings = {"checkbox", "radio", "switch"})
    void tokenChangesPreserveExplicitHeights(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            SelectionCase fixture = createCase(kind);
            Control control = fixture.control();
            control.setMinHeight(48.0);
            control.setPrefHeight(48.0);
            fixture.touchTarget().set(72.0);
            fixture.stateLayer().set(80.0);
            assertHeights(control, 48.0);
        });
    }

    /// Retains the final bound values after unbinding and subsequent token changes.
    @ParameterizedTest
    @ValueSource(strings = {"checkbox", "radio", "switch"})
    void unbindingPreservesApplicationHeights(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            SelectionCase fixture = createCase(kind);
            Control control = fixture.control();
            SimpleDoubleProperty height = new SimpleDoubleProperty(90.0);
            control.minHeightProperty().bind(height);
            control.prefHeightProperty().bind(height);
            fixture.touchTarget().set(72.0);
            assertHeights(control, 90.0);
            control.minHeightProperty().unbind();
            control.prefHeightProperty().unbind();
            fixture.touchTarget().set(80.0);
            assertHeights(control, 90.0);
        });
    }

    /// Restores computed total heights and invalidates cached sizes for either token.
    @ParameterizedTest
    @ValueSource(strings = {"checkbox", "radio", "switch"})
    void resetRestoresComputedHeights(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            SelectionCase fixture = createCase(kind);
            Control control = fixture.control();
            control.setPadding(new Insets(3.0));
            control.setMinHeight(20.0);
            control.setPrefHeight(30.0);
            fixture.touchTarget().set(64.0);
            control.setMinHeight(Region.USE_COMPUTED_SIZE);
            control.setPrefHeight(Region.USE_COMPUTED_SIZE);
            assertHeights(control, 64.0);
            fixture.stateLayer().set(80.0);
            assertHeights(control, 80.0);
            fixture.stateLayer().set(40.0);
            assertHeights(control, 64.0);
        });
    }

    /// Includes a changed state layer or track in the switch height before skin installation.
    @Test
    void switchHeightTracksStateLayerAndTrackIndependently() {
        FxTestUtils.runOnFxThread(() -> {
            M3Switch control = new M3Switch();
            assertHeights(control, 48.0);
            control.setStateLayerSize(72.0);
            assertHeights(control, 72.0);
            control.setTrackHeight(84.0);
            assertHeights(control, 84.0);
            control.setTrackHeight(32.0);
            assertHeights(control, 72.0);
            control.setStateLayerSize(40.0);
            assertHeights(control, 48.0);
        });
    }

    /// Checks both effective height constraints.
    private static void assertHeights(Control control, double height) {
        assertEquals(height, control.minHeight(-1));
        assertEquals(height, control.prefHeight(-1));
    }

    /// Creates a selection control with its two shared geometry tokens.
    private static SelectionCase createCase(String kind) {
        return switch (kind) {
            case "checkbox" -> {
                M3CheckBox control = new M3CheckBox();
                yield new SelectionCase(control, control.touchTargetSizeProperty(), control.stateLayerSizeProperty());
            }
            case "radio" -> {
                M3RadioButton control = new M3RadioButton();
                yield new SelectionCase(control, control.touchTargetSizeProperty(), control.stateLayerSizeProperty());
            }
            case "switch" -> {
                M3Switch control = new M3Switch();
                yield new SelectionCase(control, control.touchTargetSizeProperty(), control.stateLayerSizeProperty());
            }
            default -> throw new IllegalArgumentException("Unknown selection control: " + kind);
        };
    }

    /// Supplies the control and geometry properties under test.
    ///
    /// @param control the selection control
    /// @param touchTarget the touch-target size property
    /// @param stateLayer the state-layer size property
    @NotNullByDefault
    private record SelectionCase(Control control, StyleableDoubleProperty touchTarget, StyleableDoubleProperty stateLayer) {
    }
}
