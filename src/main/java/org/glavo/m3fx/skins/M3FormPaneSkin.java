// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3FormPane;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3FormPane].
@NotNullByDefault
public final class M3FormPaneSkin extends SkinBase<M3FormPane> {
    /// The internal form-content style class.
    private static final String CONTENT_STYLE_CLASS = "m3-form-pane-content";

    /// The internal vertical content container.
    private final VBox content = new VBox();

    /// Mirrors public item changes into the skin content container.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Updates skin layout metrics when styleable tokens change.
    private final InvalidationListener metricsListener = observable -> updateMetrics();

    /// Creates a form pane skin.
    ///
    /// @param control the form pane controlled by this skin
    public M3FormPaneSkin(M3FormPane control) {
        super(control);
        content.setManaged(false);
        content.getStyleClass().add(CONTENT_STYLE_CLASS);
        getChildren().setAll(content);

        control.getItems().addListener(itemsListener);
        control.contentPaddingProperty().addListener(metricsListener);
        control.rowSpacingProperty().addListener(metricsListener);
        updateItems();
        updateMetrics();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3FormPane control = getSkinnable();
        control.getItems().removeListener(itemsListener);
        control.contentPaddingProperty().removeListener(metricsListener);
        control.rowSpacingProperty().removeListener(metricsListener);
        content.getChildren().clear();
        getChildren().remove(content);
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
        return leftInset + padding + content.minWidth(height) + padding + rightInset;
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
        return topInset + padding + content.minHeight(width) + padding + bottomInset;
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
        return leftInset + padding + content.prefWidth(height) + padding + rightInset;
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
        return topInset + padding + content.prefHeight(width) + padding + bottomInset;
    }

    /// Lays out the content container inside the padded form bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double padding = getSkinnable().getContentPadding();
        double contentX = x + padding;
        double contentY = y + padding;
        double contentWidth = Math.max(0.0, width - padding - padding);
        double contentHeight = Math.max(0.0, height - padding - padding);
        content.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    /// Mirrors public form items into the internal container.
    private void updateItems() {
        content.getChildren().setAll(getSkinnable().getItems());
        getSkinnable().requestLayout();
    }

    /// Applies styleable layout metrics to the internal container.
    private void updateMetrics() {
        content.setSpacing(getSkinnable().getRowSpacing());
        getSkinnable().requestLayout();
    }
}
