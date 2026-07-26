// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorSwatch;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default retained-mode skin for [M3ColorSwatch].
///
/// The skin keeps one checkerboard, fill layer, outline, and no-color diagonal for its lifetime. Color changes
/// update the retained fill rather than replacing scene-graph nodes. The swatch remains passive; this skin does
/// not install pointer or keyboard handlers.
@NotNullByDefault
public final class M3ColorSwatchSkin extends SkinBase<M3ColorSwatch> {
    /// The fallback radius used by the baseline Material medium corner token.
    private static final double DEFAULT_CORNER_RADIUS = 12.0;

    /// The thickness of the clipped no-color diagonal.
    private static final double NO_COLOR_DIAGONAL_THICKNESS = 2.0;

    /// The retained container clipped to the current swatch shape.
    private final Pane container = new Pane();

    /// The retained transparency checkerboard.
    private final M3ColorCheckerboard checkerboard = new M3ColorCheckerboard();

    /// The retained solid color layer.
    private final Region fill = new Region();

    /// The retained outline layer.
    private final Region outline = new Region();

    /// The retained diagonal shown for an absent or fully transparent color.
    private final Region noColorDiagonal = new Region();

    /// The retained clip applied to all swatch layers.
    private final Rectangle clip = new Rectangle();

    /// Requests visual updates when the displayed color changes.
    private final InvalidationListener colorInvalidation = observable -> updateColor();

    /// Requests geometry updates when size or rounding changes.
    private final InvalidationListener geometryInvalidation = observable -> {
        invalidateClipGeometry();
        getSkinnable().requestLayout();
    };

    /// The JavaFX color currently installed on the retained fill layer.
    private @Nullable Color renderedColor;

    /// The width represented by the clip geometry.
    private double clipWidth = Double.NaN;

    /// The height represented by the clip geometry.
    private double clipHeight = Double.NaN;

    /// The corner radius represented by the clip geometry.
    private double clipRadius = Double.NaN;

    /// Creates a color-swatch skin.
    ///
    /// @param control the swatch controlled by this skin
    public M3ColorSwatchSkin(M3ColorSwatch control) {
        super(control);

        container.setManaged(false);
        container.setMouseTransparent(true);
        container.setClip(clip);

        checkerboard.getStyleClass().add("color-swatch-checker");
        fill.getStyleClass().add("color-swatch-fill");
        outline.getStyleClass().add("color-swatch-outline");
        noColorDiagonal.getStyleClass().add("color-swatch-no-color");

        checkerboard.setManaged(false);
        fill.setManaged(false);
        outline.setManaged(false);
        noColorDiagonal.setManaged(false);
        checkerboard.setMouseTransparent(true);
        fill.setMouseTransparent(true);
        outline.setMouseTransparent(true);
        noColorDiagonal.setMouseTransparent(true);
        noColorDiagonal.setRotate(-45.0);

        container.getChildren().addAll(checkerboard, fill, outline, noColorDiagonal);
        getChildren().setAll(container);

        control.colorProperty().addListener(colorInvalidation);
        control.sizeProperty().addListener(geometryInvalidation);
        control.roundingProperty().addListener(geometryInvalidation);
        updateColor();
    }

    /// Removes listeners and retained child references before disposal.
    @Override
    public void dispose() {
        M3ColorSwatch control = getSkinnable();
        control.colorProperty().removeListener(colorInvalidation);
        control.sizeProperty().removeListener(geometryInvalidation);
        control.roundingProperty().removeListener(geometryInvalidation);

        container.setClip(null);
        container.getChildren().clear();
        getChildren().remove(container);
        super.dispose();
    }

    /// Computes the minimum width from the configured swatch size.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + getSkinnable().getSize().getSize() + rightInset;
    }

    /// Computes the minimum height from the configured swatch size.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getSize().getSize() + bottomInset;
    }

    /// Computes the preferred width from the configured swatch size.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + getSkinnable().getSize().getSize() + rightInset;
    }

    /// Computes the preferred height from the configured swatch size.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getSize().getSize() + bottomInset;
    }

    /// Computes the maximum width from the configured swatch size.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + getSkinnable().getSize().getSize() + rightInset;
    }

    /// Computes the maximum height from the configured swatch size.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getSize().getSize() + bottomInset;
    }

    /// Lays out retained swatch layers and updates their shared rounded clip.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double boundedWidth = Math.max(0.0, width);
        double boundedHeight = Math.max(0.0, height);
        container.resizeRelocate(x, y, boundedWidth, boundedHeight);
        checkerboard.resizeRelocate(0.0, 0.0, boundedWidth, boundedHeight);
        fill.resizeRelocate(0.0, 0.0, boundedWidth, boundedHeight);
        outline.resizeRelocate(0.0, 0.0, boundedWidth, boundedHeight);

        double diagonalLength = Math.hypot(boundedWidth, boundedHeight);
        noColorDiagonal.resizeRelocate(
                (boundedWidth - diagonalLength) / 2.0,
                (boundedHeight - NO_COLOR_DIAGONAL_THICKNESS) / 2.0,
                diagonalLength,
                NO_COLOR_DIAGONAL_THICKNESS
        );

        updateClipGeometry(boundedWidth, boundedHeight, resolvedCornerRadius(boundedWidth, boundedHeight));
    }

    /// Updates retained visibility and fill paint from the current color.
    private void updateColor() {
        @Nullable M3Color color = getSkinnable().getColor();
        boolean noColor = color == null || color.getAlpha() <= 0.0;
        boolean showsTransparency = color == null || color.getAlpha() < 1.0;

        checkerboard.setVisible(showsTransparency);
        fill.setVisible(!noColor);
        noColorDiagonal.setVisible(noColor);

        @Nullable Color nextColor = color == null ? null : color.toFxColor();
        if (nextColor == null) {
            if (renderedColor != null) {
                renderedColor = null;
                fill.setBackground(Background.EMPTY);
            }
        } else if (!nextColor.equals(renderedColor)) {
            renderedColor = nextColor;
            fill.setBackground(new Background(
                    new BackgroundFill(nextColor, CornerRadii.EMPTY, Insets.EMPTY)
            ));
        }
    }

    /// Returns the current corner radius, constrained to the rendered bounds.
    private double resolvedCornerRadius(double width, double height) {
        double maximumRadius = Math.max(0.0, Math.min(width, height) / 2.0);
        return switch (getSkinnable().getRounding()) {
            case NONE -> 0.0;
            case FULL -> maximumRadius;
            case DEFAULT -> Math.min(maximumRadius, resolvedCssCornerRadius());
        };
    }

    /// Returns the corner radius resolved by CSS, or the baseline medium radius before CSS is available.
    private double resolvedCssCornerRadius() {
        Border border = outline.getBorder();
        if (border == null || border.getStrokes().isEmpty()) {
            return DEFAULT_CORNER_RADIUS;
        }

        BorderStroke stroke = border.getStrokes().get(0);
        double radius = stroke.getRadii().getTopLeftHorizontalRadius();
        return Double.isFinite(radius) && radius >= 0.0 ? radius : DEFAULT_CORNER_RADIUS;
    }

    /// Updates the retained rounded-rectangle clip when its geometry changes.
    private void updateClipGeometry(double width, double height, double radius) {
        if (Double.compare(clipWidth, width) == 0
                && Double.compare(clipHeight, height) == 0
                && Double.compare(clipRadius, radius) == 0) {
            return;
        }

        clipWidth = width;
        clipHeight = height;
        clipRadius = radius;
        clip.setWidth(width);
        clip.setHeight(height);
        clip.setArcWidth(radius * 2.0);
        clip.setArcHeight(radius * 2.0);
    }

    /// Invalidates the cached clip geometry.
    private void invalidateClipGeometry() {
        clipWidth = Double.NaN;
        clipHeight = Double.NaN;
        clipRadius = Double.NaN;
    }
}
