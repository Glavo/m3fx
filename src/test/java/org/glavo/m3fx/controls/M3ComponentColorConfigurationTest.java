// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the type-safe, component-local color configuration contract.
///
/// These Tier 1 tests exercise both JavaFX property ownership and actual CSS resolution. They intentionally avoid
/// screenshots and real windows so local color regressions remain part of the fast default suite.
@NotNullByDefault
final class M3ComponentColorConfigurationTest {
    /// Maximum per-channel difference accepted after JavaFX serializes and parses an RGBA color.
    private static final double COLOR_EPSILON = 1.0 / 255.0;

    /// Starts the JavaFX toolkit before controls and scenes are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies independent, bindable button colors, disabled colors, and complete removal.
    @Test
    void buttonColorPropertiesAreIndependentBindableAndReversible() {
        FxTestUtils.runOnFxThread(() -> {
            Color container = Color.web("#275DAD");
            Color content = Color.web("#F8FAFF");
            Color disabledContainer = Color.rgb(31, 31, 31, 0.12);
            Color disabledContent = Color.rgb(31, 31, 31, 0.38);
            M3Button button = new M3Button("Save", M3ButtonVariant.FILLED);
            button.setStyle("-fx-translate-x: 0;");

            assertNull(button.getContainerColor());
            assertNull(button.getContentColor());
            assertNull(button.getDisabledContainerColor());
            assertNull(button.getDisabledContentColor());
            assertSame(button, button.containerColorProperty().getBean());
            assertSame(button, button.contentColorProperty().getBean());
            assertSame(button, button.disabledContainerColorProperty().getBean());
            assertSame(button, button.disabledContentColorProperty().getBean());

            button.setContainerColor(container);
            button.setContentColor(content);
            button.setDisabledContainerColor(disabledContainer);
            button.setDisabledContentColor(disabledContent);
            StackPane root = materialRoot(button);
            applyCss(root);
            assertColor(container, backgroundFill(button));
            assertColor(content, button.getTextFill());
            assertColor(content, backgroundFill(region(button, ".m3-state-layer")));
            assertEquals("-fx-translate-x: 0;", button.getStyle());
            assertEquals(1, button.getStylesheets().size());

            button.setVariant(M3ButtonVariant.TONAL);
            applyCss(root);
            assertColor(container, backgroundFill(button));
            assertColor(content, button.getTextFill());

            M3ThemeManager.install(root.getScene(), M3Theme.fromSeed(Color.web("#006A6A")));
            applyCss(root);
            assertColor(container, backgroundFill(button));
            assertColor(content, button.getTextFill());

            button.setDisable(true);
            applyCss(root);
            assertColor(disabledContainer, backgroundFill(button));
            assertColor(disabledContent, button.getTextFill());
            Node text = button.lookup(".text");
            assertTrue(text == null || Math.abs(text.getOpacity() - 1.0) < 0.0001);

            button.setDisable(false);
            button.setContainerColor(null);
            button.setContentColor(Color.web("#FFDAD6"));
            button.setDisabledContainerColor(null);
            button.setDisabledContentColor(null);
            applyCss(root);
            assertColor(Color.web("#FFDAD6"), button.getTextFill());
            assertNotEquals(container, backgroundFill(button));
            assertEquals(1, button.getStylesheets().size());

            SimpleObjectProperty<@Nullable Color> boundContainer = new SimpleObjectProperty<>(Color.DARKGREEN);
            button.containerColorProperty().bind(boundContainer);
            applyCss(root);
            assertColor(Color.DARKGREEN, backgroundFill(button));
            boundContainer.set(Color.DARKBLUE);
            applyCss(root);
            assertColor(Color.DARKBLUE, backgroundFill(button));
            button.containerColorProperty().unbind();

            button.setContainerColor(null);
            button.setContentColor(null);
            applyCss(root);
            assertTrue(button.getStylesheets().isEmpty());
            assertNotEquals(Color.web("#FFDAD6"), button.getTextFill());
            assertEquals("-fx-translate-x: 0;", button.getStyle());
        });
    }

    /// Verifies that null setters and explicitly created null properties install no local stylesheet.
    @Test
    void nullButtonColorPropertiesAreEquivalentToNoOverride() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Inherit");
            button.setContainerColor(null);
            button.setContentColor(null);
            button.setDisabledContainerColor(null);
            button.setDisabledContentColor(null);
            button.containerColorProperty().set(null);

            assertTrue(button.getStylesheets().isEmpty());
            assertTrue(button.getStyleClass().stream().noneMatch("m3-custom-button-colors"::equals));
        });
    }

    /// Verifies that managed entries restored after external removal are removed when the property is cleared.
    @Test
    void restoredButtonColorEntriesRemainOwnedByTheProperty() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button source = new M3Button("Source");
            source.setContainerColor(Color.RED);
            String stylesheet = source.getStylesheets().get(0);

            M3Button button = new M3Button("Restore");
            button.getStyleClass().add("m3-custom-button-colors");
            button.getStylesheets().add(stylesheet);
            button.setContainerColor(Color.RED);

            button.getStyleClass().remove("m3-custom-button-colors");
            button.getStylesheets().remove(stylesheet);
            button.setContainerColor(Color.color(1.0, 0.0, 0.0));

            assertTrue(button.getStyleClass().contains("m3-custom-button-colors"));
            assertTrue(button.getStylesheets().contains(stylesheet));

            button.setContainerColor(null);
            assertTrue(button.getStyleClass().stream().noneMatch("m3-custom-button-colors"::equals));
            assertTrue(button.getStylesheets().isEmpty());
        });
    }

    /// Verifies that font and SVG icons share the same explicit-tint contract.
    @Test
    void iconTintOverridesSemanticAndContainingComponentColors() {
        FxTestUtils.runOnFxThread(() -> {
            Color tint = Color.web("#B3261E");
            M3Icon fontIcon = new M3Icon("S", M3IconSize.MEDIUM, M3IconVariant.PRIMARY);
            M3SVGIcon svgIcon = new M3SVGIcon("M 0 0 L 24 0 L 12 24 Z");
            svgIcon.setViewBox(new javafx.geometry.Rectangle2D(0, 0, 24, 24));
            M3IconButton button = new M3IconButton(fontIcon);
            button.setContainerColor(Color.web("#EADDFF"));
            button.setContentColor(Color.web("#21005D"));
            StackPane root = materialRoot(button, svgIcon);

            fontIcon.setTint(tint);
            svgIcon.setTint(tint);
            applyCss(root);
            assertSame(fontIcon, fontIcon.tintProperty().getBean());
            assertSame(svgIcon, svgIcon.tintProperty().getBean());
            assertColor(tint, ((Text) fontIcon.lookup(".m3-icon-glyph")).getFill());
            assertColor(tint, ((Shape) svgIcon.lookup(".m3-svg-icon-path")).getFill());

            fontIcon.setTint(null);
            svgIcon.setTint(null);
            applyCss(root);
            assertTrue(fontIcon.getStylesheets().isEmpty());
            assertTrue(svgIcon.getStylesheets().isEmpty());
            assertNotEquals(tint, ((Text) fontIcon.lookup(".m3-icon-glyph")).getFill());
            assertNotEquals(tint, ((Shape) svgIcon.lookup(".m3-svg-icon-path")).getFill());
        });
    }

    /// Verifies card container, content, state, and disabled color resolution.
    @Test
    void cardColorsReachTheRenderedContainerAndContentScope() {
        FxTestUtils.runOnFxThread(() -> {
            Color container = Color.web("#D5E3FF");
            Color content = Color.web("#001B3C");
            Color disabledContainer = Color.rgb(31, 31, 31, 0.12);
            Color disabledContent = Color.rgb(31, 31, 31, 0.38);
            M3Icon icon = new M3Icon("C");
            M3Card card = new M3Card(icon, M3CardVariant.OUTLINED);
            card.setContainerColor(container);
            card.setContentColor(content);
            card.setDisabledContainerColor(disabledContainer);
            card.setDisabledContentColor(disabledContent);
            StackPane root = materialRoot(card);

            applyCss(root);
            assertSame(card, card.containerColorProperty().getBean());
            assertSame(card, card.contentColorProperty().getBean());
            assertSame(card, card.disabledContainerColorProperty().getBean());
            assertSame(card, card.disabledContentColorProperty().getBean());
            assertColor(container, backgroundFill(region(card, ".m3-card-container")));
            assertColor(content, ((Text) icon.lookup(".m3-icon-glyph")).getFill());
            assertColor(content, backgroundFill(region(card, ".m3-state-layer")));

            card.setDisable(true);
            applyCss(root);
            assertColor(disabledContainer, backgroundFill(region(card, ".m3-card-container")));
            assertColor(disabledContent, ((Text) icon.lookup(".m3-icon-glyph")).getFill());

            card.setContainerColor(null);
            card.setContentColor(null);
            card.setDisabledContainerColor(null);
            card.setDisabledContentColor(null);
            applyCss(root);
            assertTrue(card.getStylesheets().isEmpty());
            assertNotEquals(disabledContainer, backgroundFill(region(card, ".m3-card-container")));
        });
    }

    /// Verifies surface container colors and scoped content color inheritance.
    @Test
    void surfaceColorsReachTheRenderedContainerAndContentScope() {
        FxTestUtils.runOnFxThread(() -> {
            Color container = Color.web("#FFE2E0");
            Color content = Color.web("#3B0908");
            M3Icon icon = new M3Icon("S");
            M3Surface surface = new M3Surface();
            surface.getContent().add(icon);
            surface.setContainerColor(container);
            surface.setContentColor(content);
            StackPane root = materialRoot(surface);

            applyCss(root);
            assertSame(surface, surface.containerColorProperty().getBean());
            assertSame(surface, surface.contentColorProperty().getBean());
            assertColor(container, backgroundFill(region(surface, ".m3-surface-container")));
            assertColor(content, ((Text) icon.lookup(".m3-icon-glyph")).getFill());

            surface.setVariant(M3SurfaceVariant.PRIMARY_CONTAINER);
            applyCss(root);
            assertColor(container, backgroundFill(region(surface, ".m3-surface-container")));
            assertColor(content, ((Text) icon.lookup(".m3-icon-glyph")).getFill());

            surface.setContainerColor(null);
            surface.setContentColor(null);
            applyCss(root);
            assertTrue(surface.getStylesheets().isEmpty());
            assertNotEquals(container, backgroundFill(region(surface, ".m3-surface-container")));
        });
    }

    /// Creates a themed scene root containing the supplied nodes.
    private static StackPane materialRoot(Node... nodes) {
        StackPane root = new StackPane(nodes);
        Scene scene = new Scene(root, 640, 360);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        return root;
    }

    /// Applies CSS and performs one layout pass for the complete test root.
    private static void applyCss(StackPane root) {
        root.applyCss();
        root.resize(640, 360);
        root.layout();
    }

    /// Returns a rendered region found beneath a control.
    private static Region region(Node root, String selector) {
        return (Region) root.lookup(selector);
    }

    /// Returns the first background fill paint of a rendered region.
    private static Paint backgroundFill(Region region) {
        return region.getBackground().getFills().get(0).getFill();
    }

    /// Asserts that two paints contain perceptually identical RGBA channels.
    private static void assertColor(Color expected, Paint actualPaint) {
        Color actual = (Color) actualPaint;
        assertEquals(expected.getRed(), actual.getRed(), COLOR_EPSILON);
        assertEquals(expected.getGreen(), actual.getGreen(), COLOR_EPSILON);
        assertEquals(expected.getBlue(), actual.getBlue(), COLOR_EPSILON);
        assertEquals(expected.getOpacity(), actual.getOpacity(), 0.0001);
    }
}
