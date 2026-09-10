// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3ToolbarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies toolbar dimension ownership and live orientation-dependent layout metrics.
@NotNullByDefault
final class M3ToolbarTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves explicit dimensions equal to token defaults across orientation changes.
    @Test
    void orientationChangesPreserveExplicitDimensions() {
        FxTestUtils.runOnFxThread(() -> {
            M3Toolbar toolbar = new M3Toolbar();
            toolbar.setMinHeight(64.0);
            toolbar.setPrefHeight(64.0);
            toolbar.setOrientation(Orientation.VERTICAL);
            assertEquals(64.0, toolbar.minHeight(-1));
            assertEquals(64.0, toolbar.prefHeight(-1));
            toolbar.setMinWidth(64.0);
            toolbar.setPrefWidth(64.0);
            toolbar.setOrientation(Orientation.HORIZONTAL);
            assertEquals(64.0, toolbar.minWidth(-1));
            assertEquals(64.0, toolbar.prefWidth(-1));
            toolbar.setContainerHeight(72.0);
            assertEquals(64.0, toolbar.minHeight(-1));
            assertEquals(64.0, toolbar.prefHeight(-1));
        });
    }

    /// Keeps the last bound value after unbinding and further orientation changes.
    @Test
    void unboundDimensionsRemainApplicationValues() {
        FxTestUtils.runOnFxThread(() -> {
            M3Toolbar toolbar = new M3Toolbar();
            SimpleDoubleProperty dimension = new SimpleDoubleProperty(90.0);
            toolbar.minWidthProperty().bind(dimension);
            toolbar.minHeightProperty().bind(dimension);
            toolbar.prefWidthProperty().bind(dimension);
            toolbar.prefHeightProperty().bind(dimension);
            toolbar.setOrientation(Orientation.VERTICAL);
            toolbar.minWidthProperty().unbind();
            toolbar.minHeightProperty().unbind();
            toolbar.prefWidthProperty().unbind();
            toolbar.prefHeightProperty().unbind();
            toolbar.setOrientation(Orientation.HORIZONTAL);
            assertEquals(90.0, toolbar.minWidth(-1));
            assertEquals(90.0, toolbar.minHeight(-1));
            assertEquals(90.0, toolbar.prefWidth(-1));
            assertEquals(90.0, toolbar.prefHeight(-1));
        });
    }

    /// Restores token-derived cross-axis dimensions before a skin is installed.
    @Test
    void computedCrossAxisDimensionsDoNotRequireSkin() {
        FxTestUtils.runOnFxThread(() -> {
            M3Toolbar toolbar = new M3Toolbar();
            toolbar.setContainerHeight(72.0);
            toolbar.setContainerWidth(80.0);
            toolbar.setMinSize(10.0, 10.0);
            toolbar.setPrefSize(20.0, 20.0);
            toolbar.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            toolbar.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            assertEquals(72.0, toolbar.minHeight(-1));
            assertEquals(72.0, toolbar.prefHeight(-1));
            toolbar.setOrientation(Orientation.VERTICAL);
            assertEquals(80.0, toolbar.minWidth(-1));
            assertEquals(80.0, toolbar.prefWidth(-1));
        });
    }

    /// Invalidates content measurements when orientation, variant, spacing, or slot size changes.
    @Test
    void contentDimensionsTrackLiveLayoutTokens() {
        FxTestUtils.runOnFxThread(() -> {
            M3Toolbar toolbar = new M3Toolbar();
            toolbar.setContainerWidth(80.0);
            toolbar.setContentPadding(2.0);
            toolbar.setDockedContentPadding(3.0);
            toolbar.setItemSlotSize(10.0);
            toolbar.setItemSpacing(4.0);
            toolbar.setDockedMaxItemSpacing(12.0);
            Region first = new Region();
            first.setPrefSize(20.0, 30.0);
            Region second = new Region();
            second.setPrefSize(40.0, 50.0);
            toolbar.getItems().setAll(first, second);
            toolbar.setSkin(new M3ToolbarSkin(toolbar));

            assertEquals(68.0, toolbar.minWidth(-1));
            assertEquals(68.0, toolbar.prefWidth(-1));
            assertEquals(Double.MAX_VALUE, toolbar.maxWidth(-1));
            assertEquals(Double.MAX_VALUE, toolbar.maxHeight(-1));
            toolbar.setOrientation(Orientation.VERTICAL);
            assertEquals(80.0, toolbar.prefWidth(-1));
            assertEquals(88.0, toolbar.minHeight(-1));
            assertEquals(88.0, toolbar.prefHeight(-1));
            assertEquals(Double.MAX_VALUE, toolbar.maxWidth(-1));
            assertEquals(Double.MAX_VALUE, toolbar.maxHeight(-1));
            toolbar.setVariant(M3ToolbarVariant.DOCKED);
            assertEquals(90.0, toolbar.minHeight(-1));
            assertEquals(98.0, toolbar.prefHeight(-1));
            toolbar.setDockedMaxItemSpacing(16.0);
            assertEquals(102.0, toolbar.prefHeight(-1));
            toolbar.setItemSlotSize(60.0);
            assertEquals(130.0, toolbar.minHeight(-1));
            assertEquals(142.0, toolbar.prefHeight(-1));
            toolbar.setItemSpacing(8.0);
            assertEquals(134.0, toolbar.minHeight(-1));
            toolbar.setOrientation(Orientation.HORIZONTAL);
            assertEquals(134.0, toolbar.minWidth(-1));
            assertEquals(142.0, toolbar.prefWidth(-1));
            toolbar.setSkin(null);
        });
    }
}
