// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.internal.M3ColorMath;
import org.jetbrains.annotations.NotNullByDefault;

/// Presents a retained color thumb shared by color-selection skins.
///
/// The outline and surface ring are CSS-styled retained shapes. Updating the selected color changes only the
/// innermost shape's fill and does not rebuild a JavaFX background or border.
@NotNullByDefault
final class M3ColorThumb extends Region {
    /// The width of the outer outline.
    private static final double OUTLINE_WIDTH = 1.0;

    /// The width of the surface-colored ring inside the outline.
    private static final double SURFACE_RING_WIDTH = 2.0;

    /// The outer outline shape.
    private final Circle outline = new Circle();

    /// The surface-colored ring shape.
    private final Circle surfaceRing = new Circle();

    /// The selected-color fill shape.
    private final Circle colorFill = new Circle();

    /// The last opaque RGB sample applied to the color fill.
    private int renderedRgb = -1;

    /// Creates a retained thumb with the supplied root style class.
    ///
    /// @param styleClass the style class applied to the thumb
    M3ColorThumb(String styleClass) {
        getStyleClass().add(styleClass);
        outline.getStyleClass().add("m3-color-thumb-outline");
        surfaceRing.getStyleClass().add("m3-color-thumb-surface-ring");
        colorFill.getStyleClass().add("m3-color-thumb-fill");

        outline.setManaged(false);
        surfaceRing.setManaged(false);
        colorFill.setManaged(false);
        outline.setMouseTransparent(true);
        surfaceRing.setMouseTransparent(true);
        colorFill.setMouseTransparent(true);
        getChildren().setAll(outline, surfaceRing, colorFill);
    }

    /// Applies an opaque representation of the supplied color to the retained fill shape.
    ///
    /// @param color the color to present
    void setColor(M3Color color) {
        int rgb = M3ColorMath.toArgb(color) & 0x00FF_FFFF;
        if (rgb == renderedRgb) {
            return;
        }

        renderedRgb = rgb;
        colorFill.setFill(Color.rgb(rgb >>> 16, rgb >>> 8 & 0xFF, rgb & 0xFF));
    }

    /// Centers the retained rings and fill within the current thumb bounds.
    @Override
    protected void layoutChildren() {
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;
        double radius = Math.max(0.0, Math.min(getWidth(), getHeight()) / 2.0);

        layoutCircle(outline, centerX, centerY, radius);
        layoutCircle(surfaceRing, centerX, centerY, Math.max(0.0, radius - OUTLINE_WIDTH));
        layoutCircle(
                colorFill,
                centerX,
                centerY,
                Math.max(0.0, radius - OUTLINE_WIDTH - SURFACE_RING_WIDTH)
        );
    }

    /// Applies center and radius geometry to one retained circle.
    ///
    /// @param circle  the circle to lay out
    /// @param centerX the center x-coordinate
    /// @param centerY the center y-coordinate
    /// @param radius  the non-negative radius
    private static void layoutCircle(Circle circle, double centerX, double centerY, double radius) {
        circle.setCenterX(centerX);
        circle.setCenterY(centerY);
        circle.setRadius(radius);
    }
}
