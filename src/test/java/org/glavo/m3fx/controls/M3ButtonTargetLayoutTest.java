// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/// Verifies the independent visual-container and interaction-target geometry of buttons.
@NotNullByDefault
final class M3ButtonTargetLayoutTest {
    /// Starts the JavaFX toolkit before creating styled controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that compact text buttons retain a 48dp target while feedback follows the visual container.
    @Test
    void compactTextButtonsSeparateVisualAndInteractionBounds() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button extraSmall = new M3Button("Add");
            extraSmall.setVariant(M3ButtonVariant.OUTLINED);
            extraSmall.setSize(M3ButtonSize.EXTRA_SMALL);
            M3Button small = new M3Button("Save");
            small.setVariant(M3ButtonVariant.OUTLINED);
            HBox root = new HBox(extraSmall, small);
            Scene scene = new Scene(root);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());

            root.applyCss();
            root.layout();

            assertEquals(32.0, extraSmall.getContainerHeight(), 0.0001);
            assertEquals(48.0, extraSmall.getPrefHeight(), 0.0001);
            assertEquals(48.0, extraSmall.getMinWidth(), 0.0001);
            assertVisualContainer(extraSmall, extraSmall.getWidth(), 32.0);
            assertBorderInsets(extraSmall, new Insets(8.0, 0.0, 8.0, 0.0));

            assertEquals(40.0, small.getContainerHeight(), 0.0001);
            assertEquals(48.0, small.getPrefHeight(), 0.0001);
            assertEquals(48.0, small.getMinWidth(), 0.0001);
            assertVisualContainer(small, small.getWidth(), 40.0);
            assertBorderInsets(small, new Insets(4.0, 0.0, 4.0, 0.0));
        });
    }

    /// Verifies that compact icon buttons retain a 48dp target while feedback follows the visual container.
    @Test
    void compactButtonsSeparateVisualAndInteractionBounds() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconButton button = new M3IconButton(new M3Icon("A"));
            button.setVariant(M3ButtonVariant.OUTLINED);
            M3IconToggleButton toggleButton = new M3IconToggleButton(new M3Icon("B"));
            toggleButton.setVariant(M3IconToggleButtonVariant.OUTLINED);
            HBox root = new HBox(button, toggleButton);
            Scene scene = new Scene(root);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());

            root.applyCss();
            root.layout();

            assertEquals(40.0, button.getContainerWidth(), 0.0001);
            assertEquals(40.0, button.getContainerHeight(), 0.0001);
            assertEquals(48.0, button.getPrefWidth(), 0.0001);
            assertEquals(48.0, button.getPrefHeight(), 0.0001);
            assertVisualContainer(button, 40.0, 40.0);
            assertBorderInsets(button, new Insets(4.0));

            assertEquals(40.0, toggleButton.getContainerWidth(), 0.0001);
            assertEquals(40.0, toggleButton.getContainerHeight(), 0.0001);
            assertEquals(48.0, toggleButton.getPrefWidth(), 0.0001);
            assertEquals(48.0, toggleButton.getPrefHeight(), 0.0001);
            assertVisualContainer(toggleButton, 40.0, 40.0);
            assertBorderInsets(toggleButton, new Insets(4.0));
        });
    }

    /// Verifies the asymmetric insets needed by an extra-small narrow visual container.
    @Test
    void narrowExtraSmallButtonKeepsCenteredVisualGeometry() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconButton button = new M3IconButton(new M3Icon("A"));
            button.setVariant(M3ButtonVariant.OUTLINED);
            button.setSize(M3ButtonSize.EXTRA_SMALL);
            button.setWidthRole(M3IconButtonWidth.NARROW);
            HBox root = new HBox(button);
            Scene scene = new Scene(root);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());

            root.applyCss();
            root.layout();

            assertEquals(28.0, button.getContainerWidth(), 0.0001);
            assertEquals(32.0, button.getContainerHeight(), 0.0001);
            assertEquals(48.0, button.getPrefWidth(), 0.0001);
            assertEquals(48.0, button.getPrefHeight(), 0.0001);
            assertVisualContainer(button, 28.0, 32.0);
            assertBorderInsets(button, new Insets(8.0, 10.0, 8.0, 10.0));
        });
    }

    /// Verifies one centered state-layer container.
    private static void assertVisualContainer(Control button, double width, double height) {
        Node stateLayer = button.lookup(".m3-state-layer-container");
        assertNotNull(stateLayer);
        assertEquals(width, stateLayer.getBoundsInParent().getWidth(), 0.0001);
        assertEquals(height, stateLayer.getBoundsInParent().getHeight(), 0.0001);
        assertEquals(button.getWidth() / 2.0, stateLayer.getBoundsInParent().getCenterX(), 0.0001);
        assertEquals(button.getHeight() / 2.0, stateLayer.getBoundsInParent().getCenterY(), 0.0001);
    }

    /// Verifies the CSS-resolved border insets around a visual container.
    private static void assertBorderInsets(Control button, Insets expected) {
        assertNotNull(button.getBorder());
        assertEquals(expected, button.getBorder().getStrokes().get(0).getInsets());
    }
}
