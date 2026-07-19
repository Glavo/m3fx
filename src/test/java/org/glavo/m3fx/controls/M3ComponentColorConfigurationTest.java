// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the public component-paint configuration contract.
///
/// These Tier 1 tests cover JavaFX property ownership, CSS configuration, icon paint precedence, and the concrete
/// skin nodes that render component paints. They intentionally avoid screenshots and real windows so paint
/// regressions remain part of the fast default suite.
@NotNullByDefault
final class M3ComponentColorConfigurationTest {
    /// Maximum per-channel difference accepted after JavaFX parses an RGBA color.
    private static final double COLOR_EPSILON = 1.0 / 255.0;

    /// Starts the JavaFX toolkit before controls and scenes are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that button paints render directly and remain bindable across state and theme changes.
    @Test
    void buttonPaintPropertiesDriveRenderedContent() {
        FxTestUtils.runOnFxThread(() -> {
            Paint initialContainer = gradient(Color.web("#006A6A"), Color.web("#4A6363"));
            Color initialContent = Color.WHITE;
            M3Icon icon = new M3Icon("S");
            M3Button button = new M3Button("Save", icon, M3ButtonVariant.FILLED);
            button.setContainerColor(initialContainer);
            button.setContentColor(initialContent);
            StackPane root = materialRoot(button);

            applyCss(root);
            assertSame(button, button.containerColorProperty().getBean());
            assertSame(button, button.contentColorProperty().getBean());
            assertPaint(initialContainer, backgroundFill(region(button, ".m3-container-paint")));
            assertPaint(initialContent, button.getTextFill());
            assertPaint(initialContent, backgroundFill(region(button, ".m3-state-layer")));
            assertPaint(initialContent, ((Text) requiredLookup(icon, ".m3-icon-glyph")).getFill());
            assertTrue(button.getStylesheets().isEmpty());

            SimpleObjectProperty<@Nullable Paint> boundContainer =
                    new SimpleObjectProperty<>(Color.web("#7D5260"));
            button.containerColorProperty().bind(boundContainer);
            applyCss(root);
            assertPaint(Color.web("#7D5260"), backgroundFill(region(button, ".m3-container-paint")));
            boundContainer.set(Color.web("#6750A4"));
            applyCss(root);
            assertPaint(Color.web("#6750A4"), backgroundFill(region(button, ".m3-container-paint")));

            button.setDisable(true);
            M3ThemeManager.install(root.getScene(), M3Theme.fromSeed(Color.web("#386A20")));
            applyCss(root);
            assertPaint(Color.web("#6750A4"), backgroundFill(region(button, ".m3-container-paint")));
            assertPaint(initialContent, button.getTextFill());
            button.containerColorProperty().unbind();
        });
    }

    /// Verifies that application CSS can configure the same button paint properties.
    @Test
    void buttonPaintPropertiesAreStyleable() {
        FxTestUtils.runOnFxThread(() -> {
            Color container = Color.web("#123456");
            Color content = Color.web("#FEDCBA");
            M3Button button = new M3Button("Styled", M3ButtonVariant.FILLED);
            StackPane root = materialRoot(button);

            applyCss(root);
            assertNotEquals(Color.TRANSPARENT, button.getContainerColor());
            assertPaint(button.getContainerColor(), backgroundFill(region(button, ".m3-container-paint")));
            assertPaint(button.getContentColor(), button.getTextFill());

            button.setVariant(M3ButtonVariant.OUTLINED);
            button.setStyle("-m3-container-color: #123456; -m3-content-color: #FEDCBA;");
            applyCss(root);
            assertPaint(container, button.getContainerColor());
            assertPaint(content, button.getContentColor());
            assertPaint(container, backgroundFill(region(button, ".m3-container-paint")));
            assertPaint(content, button.getTextFill());

            List<String> properties = M3Button.getClassCssMetaData().stream()
                    .map(CssMetaData::getProperty)
                    .toList();
            assertTrue(properties.contains("-m3-container-color"));
            assertTrue(properties.contains("-m3-content-color"));
        });
    }

    /// Verifies explicit, inherited, and semantic icon paint precedence for both icon implementations.
    @Test
    void iconPaintPrecedenceIsConsistent() {
        FxTestUtils.runOnFxThread(() -> {
            Color inherited = Color.web("#21005D");
            Color tint = Color.web("#B3261E");
            M3Icon fontIcon = new M3Icon("S", M3IconSize.MEDIUM, M3IconVariant.PRIMARY);
            M3SVGIcon svgIcon = new M3SVGIcon("M 0 0 L 24 0 L 12 24 Z");
            svgIcon.setViewBox(new javafx.geometry.Rectangle2D(0, 0, 24, 24));
            M3IconButton fontButton = new M3IconButton(fontIcon);
            M3IconButton svgButton = new M3IconButton(svgIcon);
            fontButton.setContentColor(inherited);
            svgButton.setContentColor(inherited);
            StackPane root = materialRoot(fontButton, svgButton);

            applyCss(root);
            assertPaint(inherited, ((Text) requiredLookup(fontIcon, ".m3-icon-glyph")).getFill());
            assertPaint(inherited, ((Shape) requiredLookup(svgIcon, ".m3-svg-icon-path")).getFill());

            fontIcon.setTint(tint);
            svgIcon.setTint(tint);
            applyCss(root);
            assertPaint(tint, ((Text) requiredLookup(fontIcon, ".m3-icon-glyph")).getFill());
            assertPaint(tint, ((Shape) requiredLookup(svgIcon, ".m3-svg-icon-path")).getFill());

            fontIcon.setTint(null);
            svgIcon.setTint(null);
            applyCss(root);
            assertPaint(inherited, ((Text) requiredLookup(fontIcon, ".m3-icon-glyph")).getFill());
            assertPaint(inherited, ((Shape) requiredLookup(svgIcon, ".m3-svg-icon-path")).getFill());
            assertTrue(fontIcon.getStylesheets().isEmpty());
            assertTrue(svgIcon.getStylesheets().isEmpty());
        });
    }

    /// Verifies that cards and surfaces render their styleable container paints without recoloring descendants.
    @Test
    void containerControlsRenderLocalPaints() {
        FxTestUtils.runOnFxThread(() -> {
            Paint cardPaint = gradient(Color.web("#FFF3E0"), Color.web("#FFDDB8"));
            Paint surfacePaint = gradient(Color.web("#E8F5E9"), Color.web("#B7F0C0"));
            M3Card themedCard = new M3Card(new M3Icon("T"), M3CardVariant.FILLED);
            M3Surface themedSurface = new M3Surface();
            themedSurface.getContent().add(new M3Icon("T"));
            M3Card card = new M3Card(new M3Icon("C"), M3CardVariant.OUTLINED);
            M3Surface surface = new M3Surface();
            surface.getContent().add(new M3Icon("S"));
            card.setContainerColor(cardPaint);
            surface.setContainerColor(surfacePaint);
            StackPane root = materialRoot(themedCard, themedSurface, card, surface);

            applyCss(root);
            assertNotEquals(Color.TRANSPARENT, themedCard.getContainerColor());
            assertNotEquals(Color.TRANSPARENT, themedSurface.getContainerColor());
            assertPaint(
                    themedCard.getContainerColor(),
                    backgroundFill(region(themedCard, ".m3-container-paint"))
            );
            assertPaint(
                    themedSurface.getContainerColor(),
                    backgroundFill(region(themedSurface, ".m3-container-paint"))
            );
            Region cardPaintLayer = region(card, ".m3-container-paint");
            assertSame(card, card.containerColorProperty().getBean());
            assertSame(surface, surface.containerColorProperty().getBean());
            assertPaint(cardPaint, backgroundFill(cardPaintLayer));
            assertPaint(surfacePaint, backgroundFill(region(surface, ".m3-container-paint")));
            assertTrue(cardPaintLayer.getLayoutX() > 0.0);

            M3ThemeManager.install(root.getScene(), M3Theme.fromSeed(Color.web("#006A6A")));
            applyCss(root);
            assertPaint(cardPaint, backgroundFill(cardPaintLayer));
            assertPaint(surfacePaint, backgroundFill(region(surface, ".m3-container-paint")));
            assertTrue(card.getStylesheets().isEmpty());
            assertTrue(surface.getStylesheets().isEmpty());
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

    /// Returns a required node selected beneath a root.
    private static Node requiredLookup(Node root, String selector) {
        return Objects.requireNonNull(root.lookup(selector), () -> "Missing node: " + selector);
    }

    /// Returns a required rendered region selected beneath a root.
    private static Region region(Node root, String selector) {
        return (Region) requiredLookup(root, selector);
    }

    /// Returns the first background-fill paint of a rendered region.
    private static Paint backgroundFill(Region region) {
        Background background = Objects.requireNonNull(region.getBackground(), "Missing background");
        return background.getFills().get(0).getFill();
    }

    /// Creates a two-stop gradient used to verify non-color paint support.
    private static LinearGradient gradient(Color start, Color end) {
        return new LinearGradient(
                0.0,
                0.0,
                1.0,
                1.0,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, start),
                new Stop(1.0, end)
        );
    }

    /// Asserts equality for arbitrary paints and channel-tolerant equality for colors.
    private static void assertPaint(Paint expected, Paint actual) {
        if (expected instanceof Color expectedColor && actual instanceof Color actualColor) {
            assertEquals(expectedColor.getRed(), actualColor.getRed(), COLOR_EPSILON);
            assertEquals(expectedColor.getGreen(), actualColor.getGreen(), COLOR_EPSILON);
            assertEquals(expectedColor.getBlue(), actualColor.getBlue(), COLOR_EPSILON);
            assertEquals(expectedColor.getOpacity(), actualColor.getOpacity(), 0.0001);
        } else {
            assertEquals(expected, actual);
        }
    }
}
