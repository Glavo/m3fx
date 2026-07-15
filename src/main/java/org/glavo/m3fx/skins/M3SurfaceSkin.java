// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Surface;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Surface].
@NotNullByDefault
public final class M3SurfaceSkin extends SkinBase<M3Surface> {
    /// The visual container that renders surface color, shape, and elevation.
    private final Region surfaceContainer = new Region();

    /// The internal content container.
    private final StackPane container = new StackPane();

    /// Updates rendered content when the surface content list changes.
    private final ListChangeListener<Node> contentListener = change -> updateContent();

    /// Updates the rendered container shape after its token changes.
    private final InvalidationListener shapeInvalidation = observable -> updateContainerShape();

    /// Creates a surface skin.
    ///
    /// @param control the surface controlled by this skin
    public M3SurfaceSkin(M3Surface control) {
        super(control);
        surfaceContainer.getStyleClass().add("m3-surface-container");
        surfaceContainer.setManaged(false);
        surfaceContainer.setMouseTransparent(true);
        container.setManaged(false);
        getChildren().setAll(surfaceContainer, container);
        control.getContent().addListener(contentListener);
        control.containerShapeProperty().addListener(shapeInvalidation);
        updateContent();
        updateContainerShape();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getContent().removeListener(contentListener);
        getSkinnable().containerShapeProperty().removeListener(shapeInvalidation);
        container.getChildren().clear();
        getChildren().removeAll(surfaceContainer, container);
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
                "-fx-background-radius: " + Double.toString(getSkinnable().getContainerShape()) + "px;"
        );
    }
}
