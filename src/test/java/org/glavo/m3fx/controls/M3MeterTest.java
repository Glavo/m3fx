// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3MeterSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies meter state, semantic styling, layout direction, accessibility, and skin lifecycle contracts.
@NotNullByDefault
final class M3MeterTest {
    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies defaults, property ownership, normalization, and semantic style classes.
    @Test
    void exposesStableStateProperties() {
        FxTestUtils.runOnFxThread(() -> {
            M3Meter meter = new M3Meter("Tutorials completed", 0.25);

            assertEquals("Tutorials completed", meter.getLabel());
            assertEquals(0.25, meter.getValue());
            assertNull(meter.getValueText());
            assertEquals(M3MeterVariant.INFORMATIVE, meter.getVariant());
            assertEquals(M3MeterSize.LARGE, meter.getSize());
            assertFalse(meter.isSideLabel());
            assertSame(meter, meter.labelProperty().getBean());
            assertSame(meter, meter.valueProperty().getBean());
            assertSame(meter, meter.valueTextProperty().getBean());
            assertSame(meter, meter.variantProperty().getBean());
            assertSame(meter, meter.sizeProperty().getBean());
            assertSame(meter, meter.sideLabelProperty().getBean());
            assertEquals(AccessibleRole.PROGRESS_INDICATOR, meter.getAccessibleRole());
            assertEquals("Tutorials completed", meter.getAccessibleText());
            assertFalse(meter.isFocusTraversable());
            assertTrue(meter.getStyleClass().contains("m3-informative-meter"));
            assertTrue(meter.getStyleClass().contains("m3-large-meter"));

            meter.setValue(-0.5);
            assertEquals(0.0, meter.getValue());
            meter.setValue(1.5);
            assertEquals(1.0, meter.getValue());
            meter.setValue(Double.NaN);
            assertEquals(0.0, meter.getValue());

            SimpleDoubleProperty source = new SimpleDoubleProperty(2.0);
            meter.valueProperty().bind(source);
            assertEquals(2.0, meter.getValue());
            assertEquals(1.0, meter.getEffectiveValue());
            assertEquals(1.0, meter.queryAccessibleAttribute(AccessibleAttribute.VALUE));
            meter.valueProperty().unbind();

            meter.setVariant(M3MeterVariant.NEGATIVE);
            meter.setSize(M3MeterSize.SMALL);
            meter.setSideLabel(true);
            assertTrue(meter.getStyleClass().contains("m3-negative-meter"));
            assertFalse(meter.getStyleClass().contains("m3-informative-meter"));
            assertTrue(meter.getStyleClass().contains("m3-small-meter"));
            assertFalse(meter.getStyleClass().contains("m3-large-meter"));
            assertTrue(meter.getPseudoClassStates().stream()
                    .anyMatch(pseudoClass -> pseudoClass.getPseudoClassName().equals("side-label")));

            meter.variantProperty().set(null);
            meter.sizeProperty().set(null);
            assertEquals(M3MeterVariant.INFORMATIVE, meter.getVariant());
            assertEquals(M3MeterSize.LARGE, meter.getSize());
            assertThrows(NullPointerException.class, () -> meter.setLabel(null));
            assertThrows(NullPointerException.class, () -> meter.setVariant(null));
            assertThrows(NullPointerException.class, () -> meter.setSize(null));

            meter.labelProperty().set(null);
            assertEquals("", meter.getLabel());
            assertEquals("", meter.getAccessibleText());
        });
    }

    /// Verifies value text and numeric accessibility attributes.
    @Test
    void exposesMeasuredValueToAssistiveTechnology() {
        FxTestUtils.runOnFxThread(() -> {
            M3Meter meter = new M3Meter("Storage space", 0.68);
            assertEquals(0.0, meter.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
            assertEquals(1.0, meter.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
            assertEquals(0.68, meter.queryAccessibleAttribute(AccessibleAttribute.VALUE));
            assertEquals(Orientation.HORIZONTAL, meter.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));

            meter.setValueText("68 GB of 100 GB");
            assertEquals("68 GB of 100 GB", meter.getValueText());
            meter.setValueText(null);
            assertNull(meter.getValueText());
        });
    }

    /// Verifies resolved variant colors, fill geometry, size metrics, wrapping, and skin replacement.
    @Test
    void skinRendersLabelsAndSemanticFill() {
        FxTestUtils.runOnFxThread(() -> {
            M3Meter meter = new M3Meter("Tutorials completed across the current learning pathway", 0.25);
            meter.setValueText("2 of 8");
            meter.setMaxWidth(180.0);
            StackPane root = themedRoot(meter, 360.0, 180.0);
            layout(root, 360.0, 180.0);

            assertInstanceOf(M3MeterSkin.class, meter.getSkin());
            Region track = assertInstanceOf(Region.class, meter.lookup(".m3-meter-track"));
            Region fill = assertInstanceOf(Region.class, meter.lookup(".m3-meter-fill"));
            Label label = assertInstanceOf(Label.class, meter.lookup(".m3-meter-label"));
            Label valueLabel = assertInstanceOf(Label.class, meter.lookup(".m3-meter-value-label"));
            assertEquals("Tutorials completed across the current learning pathway", label.getText());
            assertEquals("2 of 8", valueLabel.getText());
            assertEquals(8.0, track.getHeight(), 0.001);
            assertEquals(track.getWidth() * 0.25, fill.getWidth(), 0.001);
            assertTrue(label.getHeight() > label.getFont().getSize() * 1.5, "long labels should wrap");
            assertNotNull(fill.getBackground());
            Color informativeColor = assertInstanceOf(
                    Color.class,
                    fill.getBackground().getFills().get(0).getFill()
            );

            meter.setVariant(M3MeterVariant.NEGATIVE);
            root.applyCss();
            layout(root, 360.0, 180.0);
            Color negativeColor = assertInstanceOf(
                    Color.class,
                    fill.getBackground().getFills().get(0).getFill()
            );
            assertNotEquals(informativeColor, negativeColor);

            meter.setSize(M3MeterSize.SMALL);
            root.applyCss();
            layout(root, 360.0, 180.0);
            assertEquals(4.0, track.getHeight(), 0.001);
            assertEquals(12.0, label.getFont().getSize(), 0.001);

            meter.setValue(0.75);
            layout(root, 360.0, 180.0);
            assertEquals(track.getWidth() * 0.75, fill.getWidth(), 0.001);

            FxTestUtils.replaceSkin(meter, M3MeterSkin::new);
            layout(root, 360.0, 180.0);
            assertEquals(1, meter.lookupAll(".m3-meter-track").size());
            assertEquals(1, meter.lookupAll(".m3-meter-fill").size());
            assertEquals(1, meter.lookupAll(".m3-meter-label").size());
            assertEquals(1, meter.lookupAll(".m3-meter-value-label").size());
        });
    }

    /// Verifies side-label ordering and logical-leading fill in both layout directions.
    @Test
    void sideLabelsAndFillMirrorForRtl() {
        FxTestUtils.runOnFxThread(() -> {
            M3Meter meter = new M3Meter("Storage space", 0.25);
            meter.setValueText("25%");
            meter.setSideLabel(true);
            meter.setMaxWidth(360.0);
            StackPane root = themedRoot(meter, 440.0, 120.0);
            layout(root, 440.0, 120.0);

            Region track = assertInstanceOf(Region.class, meter.lookup(".m3-meter-track"));
            Region fill = assertInstanceOf(Region.class, meter.lookup(".m3-meter-fill"));
            Label label = assertInstanceOf(Label.class, meter.lookup(".m3-meter-label"));
            Label valueLabel = assertInstanceOf(Label.class, meter.lookup(".m3-meter-value-label"));
            assertTrue(centerX(label) < centerX(track));
            assertTrue(centerX(track) < centerX(valueLabel));
            assertTrue(centerX(fill) < centerX(track));

            meter.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout(root, 440.0, 120.0);
            assertTrue(centerX(label) > centerX(track));
            assertTrue(centerX(track) > centerX(valueLabel));
            assertTrue(centerX(fill) > centerX(track));
        });
    }

    /// Creates a themed scene root containing one meter.
    ///
    /// @param meter the meter to place in the scene
    /// @param width the scene width
    /// @param height the scene height
    /// @return the scene root
    private static StackPane themedRoot(M3Meter meter, double width, double height) {
        StackPane root = new StackPane(meter);
        Scene scene = new Scene(root, width, height);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        return root;
    }

    /// Applies CSS and performs one deterministic layout pass.
    ///
    /// @param root the scene root
    /// @param width the root width
    /// @param height the root height
    private static void layout(StackPane root, double width, double height) {
        root.applyCss();
        root.resize(width, height);
        root.layout();
    }

    /// Returns one node's horizontal center in scene coordinates.
    ///
    /// @param region the region to inspect
    /// @return the center x coordinate
    private static double centerX(Region region) {
        return region.localToScene(region.getBoundsInLocal()).getCenterX();
    }
}
