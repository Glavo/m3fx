// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3SearchBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies text-input token sizing and search-bar computed height constraints.
@NotNullByDefault
final class M3InputLayoutSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves explicit heights that equal the initial container-height token.
    @ParameterizedTest
    @ValueSource(strings = {"field", "password", "area"})
    void explicitHeightsSurviveTokenChanges(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            TextInputControl control = createInput(kind);
            M3TextInput input = (M3TextInput) control;
            double height = input.getContainerHeight();
            control.setMinHeight(height);
            control.setPrefHeight(height);
            input.setContainerHeight(80.0);
            input.setHorizontalPadding(30.0);
            assertHeights(control, height);
        });
    }

    /// Keeps the last bound height through later geometry changes after unbinding.
    @ParameterizedTest
    @ValueSource(strings = {"field", "password", "area"})
    void unbindingPreservesApplicationHeights(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            TextInputControl control = createInput(kind);
            M3TextInput input = (M3TextInput) control;
            SimpleDoubleProperty height = new SimpleDoubleProperty(90.0);
            control.minHeightProperty().bind(height);
            control.prefHeightProperty().bind(height);
            input.setContainerHeight(72.0);
            control.minHeightProperty().unbind();
            control.prefHeightProperty().unbind();
            input.setContainerHeight(80.0);
            input.setVerticalPadding(12.0);
            assertHeights(control, 90.0);
        });
    }

    /// Restores token-derived total heights and invalidates cached dimensions without a skin.
    @ParameterizedTest
    @ValueSource(strings = {"field", "password", "area"})
    void resetRestoresComputedHeight(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            TextInputControl control = createInput(kind);
            M3TextInput input = (M3TextInput) control;
            input.setContainerHeight(72.0);
            control.setMinHeight(20.0);
            control.setPrefHeight(30.0);
            control.setMinHeight(Region.USE_COMPUTED_SIZE);
            control.setPrefHeight(Region.USE_COMPUTED_SIZE);
            assertHeights(control, 72.0);
            input.setContainerHeight(80.0);
            assertHeights(control, 80.0);
            input.setVerticalPadding(24.0);
            assertHeights(control, 80.0);
        });
    }

    /// Restores the default search height and keeps computed maximum height tied to preferred height.
    @Test
    void searchBarResetRestoresComputedHeight() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar bar = new M3SearchBar();
            bar.setMinHeight(20.0);
            bar.setPrefHeight(30.0);
            bar.setMaxHeight(40.0);
            bar.setMinHeight(Region.USE_COMPUTED_SIZE);
            bar.setPrefHeight(Region.USE_COMPUTED_SIZE);
            bar.setMaxHeight(Region.USE_COMPUTED_SIZE);
            assertHeights(bar, 56.0);
            assertEquals(56.0, bar.maxHeight(-1));
            bar.setSkin(new M3SearchBarSkin(bar));
            try {
                assertHeights(bar, 56.0);
                assertEquals(56.0, bar.maxHeight(-1));
                bar.setPrefHeight(72.0);
                assertEquals(72.0, bar.maxHeight(-1));
                bar.setPrefHeight(Region.USE_COMPUTED_SIZE);
                assertEquals(56.0, bar.maxHeight(-1));
            } finally {
                bar.setSkin(null);
            }
        });
    }

    /// Checks both effective height constraints.
    private static void assertHeights(Region control, double height) {
        assertEquals(height, control.minHeight(-1));
        assertEquals(height, control.prefHeight(-1));
    }

    /// Creates one of the text controls implementing the shared token API.
    private static TextInputControl createInput(String kind) {
        return switch (kind) {
            case "field" -> new M3TextField();
            case "password" -> new M3PasswordField();
            case "area" -> new M3TextArea();
            default -> throw new IllegalArgumentException("Unknown input kind: " + kind);
        };
    }
}
