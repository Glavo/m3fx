// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Checks resolved control colors, shapes, and CSS properties.
@NotNullByDefault
final class ControlStyleAssertions {
    /// Prevents instantiation of this utility class.
    private ControlStyleAssertions() {
    }

    /// Verifies the rendered container fill and text fill for a labeled control.
    static void assertLabeledColors(Labeled control, Color expectedBackground, Color expectedText) {
        if (control instanceof ButtonBase button) {
            Node containerPaint = button.lookup(".m3-container-paint");
            if (containerPaint instanceof Region region && region.getBackground() != null) {
                assertRegionFill(region, expectedBackground);
            } else {
                assertRegionFill(control, expectedBackground);
            }
        } else {
            assertRegionFill(control, expectedBackground);
        }
        assertEquals(expectedText, control.getTextFill());
    }

    /// Verifies that a node currently has a drop shadow effect.
    static DropShadow assertDropShadow(Node node) {
        return assertInstanceOf(DropShadow.class, node.getEffect());
    }

    /// Returns the first top border paint for a region.
    static Paint firstBorderTopPaint(Region region) {
        assertTrue(region.getBorder() != null, "region should have a border");
        assertFalse(region.getBorder().getStrokes().isEmpty(), "region should have at least one border stroke");
        return region.getBorder().getStrokes().get(0).getTopStroke();
    }

    /// Returns the fill resolved for an icon's rendered glyph.
    static Paint iconFill(M3Icon icon) {
        icon.applyCss();
        Node glyphNode = icon.lookup(".m3-icon-glyph");
        return assertInstanceOf(Text.class, glyphNode).getFill();
    }

    /// Verifies the resolved corner radii used by a control's state layer.
    static void assertStateLayerRadii(
            Node node,
            double topLeft,
            double topRight,
            double bottomRight,
            double bottomLeft
    ) {
        Region overlay = lookupRegion(node, ".m3-state-layer");
        Node clippedContent = Objects.requireNonNull(overlay.getParent(), "clipped state content");
        Node clip = Objects.requireNonNull(clippedContent.getClip(), "state layer clip");
        if (clip instanceof Circle circle) {
            assertEquals(topLeft, topRight, 0.0001, "circular clip top-right radius");
            assertEquals(topLeft, bottomRight, 0.0001, "circular clip bottom-right radius");
            assertEquals(topLeft, bottomLeft, 0.0001, "circular clip bottom-left radius");
            assertEquals(topLeft, circle.getRadius(), 0.0001, "circular clip radius");
            assertEquals(circle.getRadius(), circle.getCenterX(), 0.0001, "circular clip centerX");
            assertEquals(circle.getRadius(), circle.getCenterY(), 0.0001, "circular clip centerY");
            return;
        }
        Path path = assertInstanceOf(Path.class, clip);
        assertPathRadii(path, topLeft, topRight, bottomRight, bottomLeft);
    }

    /// Verifies which corners are rounded on a region's background.
    static void assertRegionRoundedCorners(
            Region region,
            boolean topLeft,
            boolean topRight,
            boolean bottomRight,
            boolean bottomLeft
    ) {
        javafx.scene.layout.CornerRadii radii = region.getBackground().getFills().get(0).getRadii();
        assertEquals(topLeft, radii.getTopLeftHorizontalRadius() > 0.0);
        assertEquals(topRight, radii.getTopRightHorizontalRadius() > 0.0);
        assertEquals(bottomRight, radii.getBottomRightHorizontalRadius() > 0.0);
        assertEquals(bottomLeft, radii.getBottomLeftHorizontalRadius() > 0.0);
    }

    /// Verifies a region's concrete background corner radii.
    static void assertRegionRadii(
            Region region,
            double topLeft,
            double topRight,
            double bottomRight,
            double bottomLeft
    ) {
        if (region.getShape() instanceof Path path && path.getElements().size() >= 9) {
            assertPathRadii(path, topLeft, topRight, bottomRight, bottomLeft);
            return;
        }

        javafx.scene.layout.CornerRadii radii = region.getBackground().getFills().get(0).getRadii();
        assertEquals(topLeft, radii.getTopLeftHorizontalRadius(), 0.0001);
        assertEquals(topLeft, radii.getTopLeftVerticalRadius(), 0.0001);
        assertEquals(topRight, radii.getTopRightHorizontalRadius(), 0.0001);
        assertEquals(topRight, radii.getTopRightVerticalRadius(), 0.0001);
        assertEquals(bottomRight, radii.getBottomRightHorizontalRadius(), 0.0001);
        assertEquals(bottomRight, radii.getBottomRightVerticalRadius(), 0.0001);
        assertEquals(bottomLeft, radii.getBottomLeftHorizontalRadius(), 0.0001);
        assertEquals(bottomLeft, radii.getBottomLeftVerticalRadius(), 0.0001);
    }

    /// Verifies the four corners represented by a reusable rounded-rectangle path.
    static void assertPathRadii(
            Path path,
            double topLeft,
            double topRight,
            double bottomRight,
            double bottomLeft
    ) {
        assertEquals(topLeft, pathCornerRadius(path, 8), 0.0001);
        assertEquals(topRight, pathCornerRadius(path, 2), 0.0001);
        assertEquals(bottomRight, pathCornerRadius(path, 4), 0.0001);
        assertEquals(bottomLeft, pathCornerRadius(path, 6), 0.0001);
    }

    /// Returns one corner radius from a reusable rounded-rectangle path.
    static double pathCornerRadius(Path path, int index) {
        PathElement element = path.getElements().get(index);
        return element instanceof javafx.scene.shape.ArcTo arc ? arc.getRadiusX() : 0.0;
    }

    /// Returns a shape looked up below a node.
    static Shape lookupShape(Node node, String selector) {
        Node child = node.lookup(selector);
        assertInstanceOf(Shape.class, child);
        return (Shape) child;
    }

    /// Verifies that the supplied path still uses the same sampled path elements.
    static void assertSamePathElements(Path path, List<PathElement> elements, String description) {
        assertEquals(elements.size(), path.getElements().size(), description + " path element count");
        for (int i = 0; i < elements.size(); i++) {
            assertSame(elements.get(i), path.getElements().get(i), description + " path element " + i);
        }
    }

    /// Verifies the first background fill for a region.
    static void assertRegionFill(Region region, Color expectedFill) {
        assertEquals(1, region.getBackground().getFills().size());
        assertEquals(expectedFill, region.getBackground().getFills().get(0).getFill());
    }

    /// Verifies that a label uses a light-on-dark text fill instead of the light fallback on-surface token.
    static void assertDarkThemeLabelFill(Label label, String description) {
        Color fill = assertInstanceOf(Color.class, label.getTextFill(), () -> description + " text fill");
        Color lightFallbackOnSurface = Color.rgb(29, 27, 32);
        assertTrue(colorDistance(fill, lightFallbackOnSurface) > 0.45,
                () -> description + " resolved to the light fallback on-surface token: " + fill);
        assertTrue(fill.getBrightness() >= 0.45,
                () -> description + " should resolve to a readable dark-theme text color: " + fill);
    }

    /// Returns a color with the same RGB channels and the requested opacity.
    static Color withOpacity(Color color, double opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    /// Verifies that a region has no visible border strokes.
    static void assertNoBorder(Region region) {
        if (region.getBorder() == null) {
            return;
        }

        for (javafx.scene.layout.BorderStroke stroke : region.getBorder().getStrokes()) {
            boolean zeroWidth = stroke.getWidths().getTop() == 0.0
                    && stroke.getWidths().getRight() == 0.0
                    && stroke.getWidths().getBottom() == 0.0
                    && stroke.getWidths().getLeft() == 0.0;
            boolean transparent = isTransparent(stroke.getTopStroke())
                    && isTransparent(stroke.getRightStroke())
                    && isTransparent(stroke.getBottomStroke())
                    && isTransparent(stroke.getLeftStroke());
            assertTrue(zeroWidth || transparent);
        }
    }

    /// Returns whether a paint is fully transparent.
    static boolean isTransparent(Paint paint) {
        return paint instanceof Color color && color.getOpacity() == 0.0;
    }

    /// Verifies the first border stroke color for a region.
    static void assertBorderColor(Region region, Color expectedColor) {
        assertEquals(1, region.getBorder().getStrokes().size());
        assertEquals(expectedColor, region.getBorder().getStrokes().get(0).getTopStroke());
    }

    /// Returns the insets of the surface fill used for rendered corner sampling.
    static Insets firstBackgroundInsets(Region region) {
        var background = Objects.requireNonNull(region.getBackground(), "region background");
        assertFalse(background.getFills().isEmpty(), "region background has no fills");
        return background.getFills().get(0).getInsets();
    }

    /// Verifies that a node is an internal icon with the expected glyph.
    static void assertInternalIcon(
            @Nullable Node node,
            M3InternalIcon.Glyph expectedGlyph,
            String description
    ) {
        M3InternalIcon icon = assertInstanceOf(
                M3InternalIcon.class,
                Objects.requireNonNull(node, description),
                description
        );
        assertEquals(expectedGlyph, icon.getGlyph(), description);
        assertFalse(icon.getPath().getContent().isBlank(), description);
        assertTrue(icon.getStyleClass().contains("m3-internal-icon"), description);
        assertTrue(icon.getStyleClass().contains(internalIconColorRoleStyleClass(icon.getColorRole())), description);
        assertTrue(icon.getPath().getStyleClass().contains(M3InternalIcon.PATH_STYLE_CLASS), description);
        assertTrue(icon.getPath().getStyle().isBlank(), description);
    }

    /// Verifies that an internal icon has exactly the style class for its active color role.
    static void assertInternalIconColorRole(M3InternalIcon icon, M3InternalIcon.ColorRole expectedRole) {
        assertEquals(expectedRole, icon.getColorRole());
        for (M3InternalIcon.ColorRole role : M3InternalIcon.ColorRole.values()) {
            String styleClass = internalIconColorRoleStyleClass(role);
            assertEquals(
                    role == expectedRole,
                    icon.getStyleClass().contains(styleClass),
                    () -> "Unexpected internal icon color role style class " + styleClass
                            + " for " + expectedRole
            );
        }
        assertTrue(icon.getPath().getStyle().isBlank(), "internal icon path should not use inline CSS");
    }

    /// Returns the public style class used by an internal icon color role.
    static String internalIconColorRoleStyleClass(M3InternalIcon.ColorRole colorRole) {
        return switch (colorRole) {
            case PRIMARY -> M3InternalIcon.PRIMARY_STYLE_CLASS;
            case ON_PRIMARY -> M3InternalIcon.ON_PRIMARY_STYLE_CLASS;
            case ON_SECONDARY -> M3InternalIcon.ON_SECONDARY_STYLE_CLASS;
            case ON_TERTIARY -> M3InternalIcon.ON_TERTIARY_STYLE_CLASS;
            case ON_SURFACE -> M3InternalIcon.ON_SURFACE_STYLE_CLASS;
            case INVERSE_ON_SURFACE -> M3InternalIcon.INVERSE_ON_SURFACE_STYLE_CLASS;
            case ON_SURFACE_VARIANT -> M3InternalIcon.ON_SURFACE_VARIANT_STYLE_CLASS;
            case ON_PRIMARY_CONTAINER -> M3InternalIcon.ON_PRIMARY_CONTAINER_STYLE_CLASS;
        };
    }

    /// Verifies that one CSS metadata list contains a property.
    static void assertCssProperty(List<CssMetaData<? extends Styleable, ?>> cssMetaData, String property) {
        assertTrue(
                cssMetaData.stream().anyMatch(metaData -> metaData.getProperty().equals(property)),
                () -> "Missing CSS property " + property
        );
    }
}
