// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Affine;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3SVGIcon].
///
/// The skin owns one unmanaged [SVGPath] and one reusable [Affine] transform. Layout derives a uniform scale from
/// the configured viewport and updates the existing transform, avoiding scene-graph churn and per-layout path
/// reconstruction.
@NotNullByDefault
public final class M3SVGIconSkin extends SkinBase<M3SVGIcon> {
    /// The rendered SVG path node.
    private final SVGPath path = new SVGPath();

    /// The transform mapping source viewport coordinates into the icon's logical-pixel viewport.
    private final Affine viewportTransform = new Affine();

    /// Requests layout when effective node orientation changes.
    private final InvalidationListener orientationListener;

    /// Creates an SVG icon skin.
    ///
    /// @param control the icon controlled by this skin
    /// @throws NullPointerException if `control` is `null`
    public M3SVGIconSkin(M3SVGIcon control) {
        super(control);
        orientationListener = observable -> control.requestLayout();
        initializePath(control);
        control.effectiveNodeOrientationProperty().addListener(orientationListener);
        getChildren().setAll(path);
    }

    /// Releases bindings, listeners, and child nodes owned by this skin.
    @Override
    public void dispose() {
        M3SVGIcon control = getSkinnable();
        control.effectiveNodeOrientationProperty().removeListener(orientationListener);
        path.contentProperty().unbind();
        path.fillRuleProperty().unbind();
        path.getTransforms().remove(viewportTransform);
        getChildren().remove(path);
        super.dispose();
    }

    /// Computes the minimum width from the effective icon size.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + getSkinnable().getIconSize() + rightInset;
    }

    /// Computes the minimum height from the effective icon size.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getIconSize() + bottomInset;
    }

    /// Computes the preferred width from the effective icon size.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + getSkinnable().getIconSize() + rightInset;
    }

    /// Computes the preferred height from the effective icon size.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getIconSize() + bottomInset;
    }

    /// Computes the maximum width from the effective icon size.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the maximum height from the effective icon size.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Maps the source viewport into the available layout area with centered meet semantics.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3SVGIcon control = getSkinnable();
        if (control.getContent().isEmpty()) {
            path.setVisible(false);
            return;
        }

        @Nullable Rectangle2D explicitViewBox = control.getViewBox();
        double sourceX;
        double sourceY;
        double sourceWidth;
        double sourceHeight;
        if (explicitViewBox != null) {
            sourceX = explicitViewBox.getMinX();
            sourceY = explicitViewBox.getMinY();
            sourceWidth = explicitViewBox.getWidth();
            sourceHeight = explicitViewBox.getHeight();
        } else {
            Bounds bounds = path.getLayoutBounds();
            sourceX = bounds.getMinX();
            sourceY = bounds.getMinY();
            sourceWidth = bounds.getWidth();
            sourceHeight = bounds.getHeight();
        }

        if (!isUsableViewport(sourceX, sourceY, sourceWidth, sourceHeight)) {
            path.setVisible(false);
            return;
        }

        double targetSize = Math.min(control.getIconSize(), Math.min(width, height));
        if (!(targetSize > 0.0) || !Double.isFinite(targetSize)) {
            path.setVisible(false);
            return;
        }

        double scale = Math.min(targetSize / sourceWidth, targetSize / sourceHeight);
        double renderedWidth = sourceWidth * scale;
        double renderedHeight = sourceHeight * scale;
        double targetX = snapPositionX(x + (width - renderedWidth) / 2.0);
        double targetY = snapPositionY(y + (height - renderedHeight) / 2.0);
        boolean mirror = control.isAutoMirrored()
                && control.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;

        viewportTransform.setMxx(mirror ? -scale : scale);
        viewportTransform.setMxy(0.0);
        viewportTransform.setMyx(0.0);
        viewportTransform.setMyy(scale);
        viewportTransform.setTx(mirror
                ? targetX + (sourceX + sourceWidth) * scale
                : targetX - sourceX * scale);
        viewportTransform.setTy(targetY - sourceY * scale);
        path.setVisible(true);
    }

    /// Initializes the rendered path and binds it to control state.
    ///
    /// @param control the SVG icon supplying path state
    private void initializePath(M3SVGIcon control) {
        path.getStyleClass().add(M3SVGIcon.PATH_STYLE_CLASS);
        path.setManaged(false);
        path.setMouseTransparent(true);
        path.contentProperty().bind(control.contentProperty());
        path.fillRuleProperty().bind(control.fillRuleProperty());
        path.getTransforms().add(viewportTransform);
    }

    /// Returns whether source coordinates describe a finite, positive-area viewport.
    ///
    /// @param x      the source minimum x-coordinate
    /// @param y      the source minimum y-coordinate
    /// @param width  the source viewport width
    /// @param height the source viewport height
    /// @return `true` when the viewport can be scaled
    private static boolean isUsableViewport(double x, double y, double width, double height) {
        return Double.isFinite(x)
                && Double.isFinite(y)
                && Double.isFinite(width)
                && Double.isFinite(height)
                && width > 0.0
                && height > 0.0;
    }
}
