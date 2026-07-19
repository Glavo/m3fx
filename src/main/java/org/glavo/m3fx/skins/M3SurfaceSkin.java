// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import org.glavo.m3fx.controls.M3Surface;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Surface].
///
/// The skin renders the surface container behind the control's observable content list. Container shape, color, and
/// elevation are applied independently from content layout, and the visual container remains mouse transparent so
/// application content receives input normally.
@NotNullByDefault
public final class M3SurfaceSkin extends SkinBase<M3Surface> {
    /// The visual container that renders surface color, shape, and elevation.
    private final Region surfaceContainer = new Region();

    /// The concrete container paint rendered above the CSS surface.
    private final Region containerPaintLayer = new Region();

    /// The internal content container.
    private final StackPane container = new StackPane();

    /// Updates rendered content when the surface content list changes.
    private final ListChangeListener<Node> contentListener = change -> updateContent();

    /// Updates the rendered container shape after its token changes.
    private final InvalidationListener shapeInvalidation = observable -> updateContainerShape();

    /// Requests layout when the styleable container paint changes.
    private final InvalidationListener paintInvalidation = observable -> getSkinnable().requestLayout();

    /// The paint represented by the current concrete container background.
    private @Nullable Paint renderedContainerPaint;

    /// The corner radius represented by the current concrete container background.
    private double renderedContainerRadius = Double.NaN;

    /// Creates a surface skin.
    ///
    /// @param control the surface controlled by this skin
    public M3SurfaceSkin(M3Surface control) {
        super(control);
        surfaceContainer.getStyleClass().add("m3-surface-container");
        surfaceContainer.setManaged(false);
        surfaceContainer.setMouseTransparent(true);
        containerPaintLayer.getStyleClass().add(M3StateLayer.CONTAINER_PAINT_STYLE_CLASS);
        containerPaintLayer.setManaged(false);
        containerPaintLayer.setMouseTransparent(true);
        container.setManaged(false);
        getChildren().setAll(surfaceContainer, containerPaintLayer, container);
        control.getContent().addListener(contentListener);
        control.containerShapeProperty().addListener(shapeInvalidation);
        control.containerColorProperty().addListener(paintInvalidation);
        updateContent();
        updateContainerShape();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getContent().removeListener(contentListener);
        getSkinnable().containerShapeProperty().removeListener(shapeInvalidation);
        getSkinnable().containerColorProperty().removeListener(paintInvalidation);
        container.getChildren().clear();
        getChildren().removeAll(surfaceContainer, containerPaintLayer, container);
        super.dispose();
    }

    /// Computes preferred width from current content and control insets.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.prefWidth(height) + rightInset;
    }

    /// Computes preferred height from current content and control insets.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.prefHeight(width) + bottomInset;
    }

    /// Lays out content inside the padded surface area.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3Surface surface = getSkinnable();
        surfaceContainer.resizeRelocate(0.0, 0.0, surface.getWidth(), surface.getHeight());
        containerPaintLayer.resizeRelocate(0.0, 0.0, surface.getWidth(), surface.getHeight());
        updateContainerPaint();
        container.resizeRelocate(x, y, width, height);
    }

    /// Mirrors the public content list into the internal container.
    private void updateContent() {
        container.getChildren().setAll(getSkinnable().getContent());
        getSkinnable().requestLayout();
    }

    /// Applies the token-backed corner radius to the visual surface container.
    private void updateContainerShape() {
        surfaceContainer.setStyle(
                "-fx-background-radius: " + getSkinnable().getContainerShape() + "px;"
        );
        getSkinnable().requestLayout();
    }

    /// Applies the current styleable paint and shape to the concrete container layer.
    private void updateContainerPaint() {
        M3Surface surface = getSkinnable();
        Paint paint = surface.getContainerColor();
        double radius = surface.getContainerShape();
        if (paint.equals(renderedContainerPaint)
                && Double.compare(radius, renderedContainerRadius) == 0) {
            return;
        }
        renderedContainerPaint = paint;
        renderedContainerRadius = radius;
        containerPaintLayer.setBackground(new Background(
                new BackgroundFill(paint, new CornerRadii(radius), javafx.geometry.Insets.EMPTY)
        ));
    }
}
