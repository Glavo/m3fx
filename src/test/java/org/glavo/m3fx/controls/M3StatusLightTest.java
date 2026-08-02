// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3StatusLightSkin;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies status-light state, styling, layout direction, and accessibility contracts.
@NotNullByDefault
final class M3StatusLightTest {
    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies default values, property ownership, validation, and semantic style classes.
    @Test
    void exposesStableStateProperties() {
        FxTestUtils.runOnFxThread(() -> {
            M3StatusLight light = new M3StatusLight("Available");

            assertEquals("Available", light.getText());
            assertEquals(M3StatusLightVariant.NEUTRAL, light.getVariant());
            assertEquals(M3StatusLightSize.MEDIUM, light.getSize());
            assertSame(light, light.textProperty().getBean());
            assertSame(light, light.variantProperty().getBean());
            assertSame(light, light.sizeProperty().getBean());
            assertSame(light, light.indicatorColorProperty().getBean());
            assertEquals(AccessibleRole.TEXT, light.getAccessibleRole());
            assertEquals("Available", light.getAccessibleText());
            assertFalse(light.isFocusTraversable());
            assertTrue(light.getStyleClass().contains("m3-neutral-status-light"));
            assertTrue(light.getStyleClass().contains("m3-medium-status-light"));

            light.setVariant(M3StatusLightVariant.POSITIVE);
            assertTrue(light.getStyleClass().contains("m3-positive-status-light"));
            assertFalse(light.getStyleClass().contains("m3-neutral-status-light"));
            light.setSize(M3StatusLightSize.EXTRA_LARGE);
            assertTrue(light.getStyleClass().contains("m3-extra-large-status-light"));
            assertFalse(light.getStyleClass().contains("m3-medium-status-light"));

            light.variantProperty().set(null);
            light.sizeProperty().set(null);
            assertEquals(M3StatusLightVariant.NEUTRAL, light.getVariant());
            assertEquals(M3StatusLightSize.MEDIUM, light.getSize());
            assertThrows(NullPointerException.class, () -> light.setText(null));
            assertThrows(NullPointerException.class, () -> light.setVariant(null));
            assertThrows(NullPointerException.class, () -> light.setSize(null));
            assertThrows(NullPointerException.class, () -> light.setIndicatorColor(null));

            light.textProperty().set(null);
            assertEquals("", light.getText());
            assertEquals("", light.getAccessibleText());
        });
    }

    /// Verifies resolved semantic colors, explicit paint overrides, metrics, and skin replacement.
    @Test
    void skinRendersSemanticIndicatorAndLabel() {
        FxTestUtils.runOnFxThread(() -> {
            M3StatusLight light = new M3StatusLight("Connected", M3StatusLightVariant.POSITIVE);
            StackPane root = new StackPane(light);
            Scene scene = new Scene(root, 320.0, 120.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            layout(root);

            assertInstanceOf(M3StatusLightSkin.class, light.getSkin());
            Region indicator = assertInstanceOf(Region.class, light.lookup(".m3-status-light-indicator"));
            Label label = assertInstanceOf(Label.class, light.lookup(".m3-status-light-label"));
            assertEquals("Connected", label.getText());
            assertEquals(8.0, indicator.getWidth(), 0.001);
            assertEquals(8.0, indicator.getHeight(), 0.001);
            assertNotNull(indicator.getBackground());
            assertFalse(indicator.getBackground().getFills().isEmpty());
            Color positiveColor = assertInstanceOf(
                    Color.class,
                    indicator.getBackground().getFills().get(0).getFill()
            );

            light.setVariant(M3StatusLightVariant.NEGATIVE);
            layout(root);
            Color negativeColor = assertInstanceOf(
                    Color.class,
                    indicator.getBackground().getFills().get(0).getFill()
            );
            assertNotEquals(positiveColor, negativeColor);

            light.setStyle("-m3-indicator-color: #13579B;");
            layout(root);
            assertEquals(Color.web("#13579B"), indicator.getBackground().getFills().get(0).getFill());

            light.setIndicatorColor(Color.DEEPSKYBLUE);
            layout(root);
            assertEquals(Color.DEEPSKYBLUE, indicator.getBackground().getFills().get(0).getFill());

            light.setSize(M3StatusLightSize.EXTRA_LARGE);
            layout(root);
            assertEquals(12.0, indicator.getWidth(), 0.001);
            assertEquals(18.0, label.getFont().getSize(), 0.001);

            FxTestUtils.replaceSkin(light, M3StatusLightSkin::new);
            layout(root);
            assertEquals(1, light.lookupAll(".m3-status-light-indicator").size());
            assertEquals(1, light.lookupAll(".m3-status-light-label").size());
        });
    }

    /// Verifies that the indicator remains at logical leading in both layout directions.
    @Test
    void indicatorFollowsLogicalLeadingEdge() {
        FxTestUtils.runOnFxThread(() -> {
            M3StatusLight light = new M3StatusLight("Synchronized", M3StatusLightVariant.INFO);
            StackPane root = new StackPane(light);
            Scene scene = new Scene(root, 320.0, 120.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            layout(root);

            Region indicator = assertInstanceOf(Region.class, light.lookup(".m3-status-light-indicator"));
            Label label = assertInstanceOf(Label.class, light.lookup(".m3-status-light-label"));
            assertTrue(indicator.localToScene(indicator.getBoundsInLocal()).getCenterX()
                    < label.localToScene(label.getBoundsInLocal()).getCenterX());

            light.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout(root);
            assertTrue(indicator.localToScene(indicator.getBoundsInLocal()).getCenterX()
                    > label.localToScene(label.getBoundsInLocal()).getCenterX());
        });
    }

    /// Applies CSS and performs one deterministic layout pass.
    ///
    /// @param root the scene root
    private static void layout(StackPane root) {
        root.applyCss();
        root.resize(320.0, 120.0);
        root.layout();
    }

}
