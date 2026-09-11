// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3FormPane;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3FormPane].
///
/// Content padding is applied outside the internal container's own padding. Row spacing follows the control's
/// property, and the form may grow beyond the internal container's maximum dimensions.
@NotNullByDefault
public final class M3FormPaneSkin extends M3ItemContainerSkinBase<M3FormPane, VBox, Node> {
    /// The internal form-content style class.
    private static final String CONTENT_STYLE_CLASS = "m3-form-pane-content";

    /// Creates a form pane skin.
    ///
    /// @param control the form pane controlled by this skin
    public M3FormPaneSkin(M3FormPane control) {
        super(control, control.getItems(), new VBox());
        getContainer().getStyleClass().add(CONTENT_STYLE_CLASS);
        getContainer().spacingProperty().bind(control.rowSpacingProperty());
    }

    /// Removes listeners and child references; repeated calls have no effect.
    @Override
    public void dispose() {
        getContainer().spacingProperty().unbind();
        super.dispose();
    }

    /// Computes the minimum width from the content container and padding tokens.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double padding = getSkinnable().getContentPadding();
        return super.computeMinWidth(height, topInset + padding, rightInset + padding,
                bottomInset + padding, leftInset + padding);
    }

    /// Computes the minimum height from the content container and padding tokens.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double padding = getSkinnable().getContentPadding();
        return super.computeMinHeight(width, topInset + padding, rightInset + padding,
                bottomInset + padding, leftInset + padding);
    }

    /// Computes the preferred width from the content container and padding tokens.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double padding = getSkinnable().getContentPadding();
        return super.computePrefWidth(height, topInset + padding, rightInset + padding,
                bottomInset + padding, leftInset + padding);
    }

    /// Computes the preferred height from the content container and padding tokens.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double padding = getSkinnable().getContentPadding();
        return super.computePrefHeight(width, topInset + padding, rightInset + padding,
                bottomInset + padding, leftInset + padding);
    }

    /// Allows the form to grow beyond its content's maximum width.
    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset,
                                     double bottomInset, double leftInset) {
        return Double.MAX_VALUE;
    }

    /// Allows the form to grow beyond its content's maximum height.
    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        return Double.MAX_VALUE;
    }

    /// Lays out the content container inside the padded form bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double padding = getSkinnable().getContentPadding();
        super.layoutChildren(x + padding, y + padding,
                Math.max(0.0, width - padding - padding), Math.max(0.0, height - padding - padding));
    }
}
