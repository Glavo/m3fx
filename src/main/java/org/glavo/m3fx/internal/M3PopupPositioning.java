// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Shared popup positioning helpers for menu-like M3FX controls.
@NotNullByDefault
public final class M3PopupPositioning {
    /// The distance kept between popup content and the screen's visual bounds.
    private static final double SCREEN_MARGIN = 8.0;

    /// Prevents instantiation.
    private M3PopupPositioning() {
    }

    /// Returns a menu popup position below the owner, flipping above when needed.
    public static @Nullable Placement menuBelowOrAbove(Node owner, Region content, double offsetY) {
        @Nullable Bounds ownerBounds = owner.localToScreen(owner.getBoundsInLocal());
        if (ownerBounds == null) {
            return null;
        }

        double contentWidth = contentWidth(content);
        double contentHeight = contentHeight(content, contentWidth);
        return menuBelowOrAbove(
                ownerBounds,
                visualBoundsFor(ownerBounds),
                contentWidth,
                contentHeight,
                offsetY
        );
    }

    /// Returns a submenu popup position beside the owner, flipping left when needed.
    public static @Nullable Placement subMenuBeside(Node owner, Region content, double offsetX) {
        @Nullable Bounds ownerBounds = owner.localToScreen(owner.getBoundsInLocal());
        if (ownerBounds == null) {
            return null;
        }

        double contentWidth = contentWidth(content);
        double contentHeight = contentHeight(content, contentWidth);
        return subMenuBeside(
                ownerBounds,
                visualBoundsFor(ownerBounds),
                contentWidth,
                contentHeight,
                offsetX,
                M3NodeLayout.isRightToLeft(owner)
        );
    }

    /// Returns a menu popup position for known owner, screen, and content bounds.
    public static Placement menuBelowOrAbove(
            Bounds ownerBounds,
            Rectangle2D visualBounds,
            double contentWidth,
            double contentHeight,
            double offsetY
    ) {
        double x = clampStart(ownerBounds.getMinX(), visualBounds.getMinX(), visualBounds.getMaxX(), contentWidth);
        double belowY = ownerBounds.getMaxY() + offsetY;
        double aboveY = ownerBounds.getMinY() - offsetY - contentHeight;
        boolean belowFits = belowY + contentHeight <= visualBounds.getMaxY();
        boolean aboveFits = aboveY >= visualBounds.getMinY();
        boolean opensAbove = !belowFits && aboveFits;
        double y = opensAbove ? aboveY : belowY;
        return new Placement(
                x,
                clampStart(y, visualBounds.getMinY(), visualBounds.getMaxY(), contentHeight),
                false,
                opensAbove
        );
    }

    /// Returns a submenu popup position for known owner, screen, and content bounds.
    public static Placement subMenuBeside(
            Bounds ownerBounds,
            Rectangle2D visualBounds,
            double contentWidth,
            double contentHeight,
            double offsetX
    ) {
        return subMenuBeside(ownerBounds, visualBounds, contentWidth, contentHeight, offsetX, false);
    }

    /// Returns a submenu popup position for known owner screen bounds and preferred side.
    public static Placement subMenuBeside(
            Bounds ownerBounds,
            Region content,
            double offsetX,
            boolean preferLeft
    ) {
        double contentWidth = contentWidth(content);
        double contentHeight = contentHeight(content, contentWidth);
        return subMenuBeside(
                ownerBounds,
                visualBoundsFor(ownerBounds),
                contentWidth,
                contentHeight,
                offsetX,
                preferLeft
        );
    }

    /// Returns a submenu popup position for known owner, screen, content bounds, and preferred side.
    public static Placement subMenuBeside(
            Bounds ownerBounds,
            Rectangle2D visualBounds,
            double contentWidth,
            double contentHeight,
            double offsetX,
            boolean preferLeft
    ) {
        double rightX = ownerBounds.getMaxX() + offsetX;
        double leftX = ownerBounds.getMinX() - contentWidth - offsetX;
        boolean rightFits = rightX + contentWidth <= visualBounds.getMaxX();
        boolean leftFits = leftX >= visualBounds.getMinX();
        double leftSpace = ownerBounds.getMinX() - visualBounds.getMinX();
        double rightSpace = visualBounds.getMaxX() - rightX;
        boolean opensToLeft = preferLeft
                ? leftFits || !rightFits && leftSpace > rightSpace
                : !rightFits && (leftFits || leftSpace > rightSpace);
        double x = opensToLeft ? leftX : rightX;
        double y = clampStart(ownerBounds.getMinY(), visualBounds.getMinY(), visualBounds.getMaxY(), contentHeight);
        return new Placement(
                clampStart(x, visualBounds.getMinX(), visualBounds.getMaxX(), contentWidth),
                y,
                opensToLeft,
                false
        );
    }

    /// Returns the preferred width of popup content after CSS is applied.
    private static double contentWidth(Region content) {
        content.applyCss();
        double width = content.prefWidth(-1.0);
        if (!Double.isFinite(width) || width < 0.0) {
            width = content.minWidth(-1.0);
        }
        if (!Double.isFinite(width) || width < 0.0) {
            width = content.getLayoutBounds().getWidth();
        }
        return Math.max(0.0, width);
    }

    /// Returns the preferred height of popup content after CSS is applied.
    private static double contentHeight(Region content, double contentWidth) {
        double height = content.prefHeight(contentWidth);
        if (!Double.isFinite(height) || height < 0.0) {
            height = content.minHeight(contentWidth);
        }
        if (!Double.isFinite(height) || height < 0.0) {
            height = content.getLayoutBounds().getHeight();
        }
        return Math.max(0.0, height);
    }

    /// Returns the visual screen bounds for an owner already resolved to screen coordinates.
    private static Rectangle2D visualBoundsFor(Bounds ownerBounds) {
        List<Screen> screens = Screen.getScreensForRectangle(
                ownerBounds.getMinX(),
                ownerBounds.getMinY(),
                ownerBounds.getWidth(),
                ownerBounds.getHeight()
        );
        Screen screen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
        Rectangle2D bounds = screen.getVisualBounds();
        double minX = bounds.getMinX() + SCREEN_MARGIN;
        double minY = bounds.getMinY() + SCREEN_MARGIN;
        double width = Math.max(0.0, bounds.getWidth() - SCREEN_MARGIN * 2.0);
        double height = Math.max(0.0, bounds.getHeight() - SCREEN_MARGIN * 2.0);
        return new Rectangle2D(minX, minY, width, height);
    }

    /// Clamps a popup start coordinate to a visual axis range.
    private static double clampStart(double value, double min, double max, double length) {
        double upper = Math.max(min, max - Math.max(0.0, length));
        return Math.max(min, Math.min(value, upper));
    }

    /// A computed popup position and its resolved opening directions.
    ///
    /// @param x the popup x-coordinate in screen coordinates
    /// @param y the popup y-coordinate in screen coordinates
    /// @param opensToLeft whether a side popup opens to the left of its owner
    /// @param opensAbove whether a vertical popup opens above its owner
    @NotNullByDefault
    public record Placement(double x, double y, boolean opensToLeft, boolean opensAbove) {
    }
}
