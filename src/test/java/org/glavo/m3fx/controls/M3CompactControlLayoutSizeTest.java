// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies token-derived heights for chips, segments, and tabs.
@NotNullByDefault
final class M3CompactControlLayoutSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves explicit heights even when they match the initial token value.
    @ParameterizedTest
    @ValueSource(strings = {"chip", "segment", "tab"})
    void explicitHeightsSurviveTokenChanges(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            SizeCase fixture = createCase(kind);
            double initialHeight = fixture.height().get();
            fixture.control().setMinHeight(initialHeight);
            fixture.control().setPrefHeight(initialHeight);
            fixture.height().set(72.0);
            assertHeights(fixture.control(), initialHeight);
        });
    }

    /// Keeps the last bound height until the caller explicitly restores computed sizing.
    @ParameterizedTest
    @ValueSource(strings = {"chip", "segment", "tab"})
    void unbindingAndResetRespectApplicationOwnership(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            SizeCase fixture = createCase(kind);
            Control control = fixture.control();
            SimpleDoubleProperty height = new SimpleDoubleProperty(90.0);
            control.minHeightProperty().bind(height);
            control.prefHeightProperty().bind(height);
            fixture.height().set(64.0);
            control.minHeightProperty().unbind();
            control.prefHeightProperty().unbind();
            fixture.height().set(72.0);
            assertHeights(control, 90.0);
            control.setMinHeight(Region.USE_COMPUTED_SIZE);
            control.setPrefHeight(Region.USE_COMPUTED_SIZE);
            assertHeights(control, 72.0);
            fixture.height().set(80.0);
            assertHeights(control, 80.0);
        });
    }

    /// Preserves an explicit tab minimum width and restores its current token on reset.
    @Test
    void tabMinimumWidthFollowsStandardLayoutConstraints() {
        FxTestUtils.runOnFxThread(() -> {
            M3Tab tab = new M3Tab();
            tab.setMinWidth(90.0);
            tab.setTabMinWidth(120.0);
            assertEquals(90.0, tab.minWidth(-1));
            SimpleDoubleProperty width = new SimpleDoubleProperty(150.0);
            tab.minWidthProperty().bind(width);
            tab.setTabMinWidth(100.0);
            tab.minWidthProperty().unbind();
            tab.setTabMinWidth(130.0);
            assertEquals(150.0, tab.minWidth(-1));
            tab.setMinWidth(Region.USE_COMPUTED_SIZE);
            assertEquals(130.0, tab.minWidth(-1));
            tab.setTabMinWidth(110.0);
            assertEquals(110.0, tab.minWidth(-1));
        });
    }

    /// Checks the effective minimum and preferred heights.
    private static void assertHeights(Control control, double height) {
        assertEquals(height, control.minHeight(-1));
        assertEquals(height, control.prefHeight(-1));
    }

    /// Creates a representative control and exposes its container-height token.
    private static SizeCase createCase(String kind) {
        return switch (kind) {
            case "chip" -> {
                M3Chip control = new M3AssistChip("Action");
                yield new SizeCase(control, control.containerHeightProperty());
            }
            case "segment" -> {
                M3SegmentedButton control = new M3SegmentedButton("Choice");
                yield new SizeCase(control, control.containerHeightProperty());
            }
            case "tab" -> {
                M3Tab control = new M3Tab("Page");
                yield new SizeCase(control, control.containerHeightProperty());
            }
            default -> throw new IllegalArgumentException("Unknown control kind: " + kind);
        };
    }

    /// Supplies a control and its container-height token.
    ///
    /// @param control the control under test
    /// @param height the container-height token
    @NotNullByDefault
    private record SizeCase(Control control, StyleableDoubleProperty height) {
    }
}
