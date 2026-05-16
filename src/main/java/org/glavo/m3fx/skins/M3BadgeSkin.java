// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3Badge;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Badge].
@NotNullByDefault
public class M3BadgeSkin extends SkinBase<M3Badge> {
    /// The visible badge label.
    private final Label label = new Label();

    /// Updates text and metrics after display text inputs change.
    private final InvalidationListener textInvalidation = observable -> updateTextAndMetrics();

    /// Applies size token changes to badge geometry.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Creates a badge skin.
    public M3BadgeSkin(M3Badge control) {
        super(control);
        label.getStyleClass().add("m3-badge-label");
        getChildren().add(label);

        updateText();
        updateMetrics();
        control.textProperty().addListener(textInvalidation);
        control.maxCharacterCountProperty().addListener(textInvalidation);
        control.smallSizeProperty().addListener(metricsInvalidation);
        control.largeHeightProperty().addListener(metricsInvalidation);
        control.largeMinWidthProperty().addListener(metricsInvalidation);
        control.containerShapeProperty().addListener(metricsInvalidation);
        control.horizontalPaddingProperty().addListener(metricsInvalidation);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3Badge badge = getSkinnable();
        badge.textProperty().removeListener(textInvalidation);
        badge.maxCharacterCountProperty().removeListener(textInvalidation);
        badge.smallSizeProperty().removeListener(metricsInvalidation);
        badge.largeHeightProperty().removeListener(metricsInvalidation);
        badge.largeMinWidthProperty().removeListener(metricsInvalidation);
        badge.containerShapeProperty().removeListener(metricsInvalidation);
        badge.horizontalPaddingProperty().removeListener(metricsInvalidation);
        super.dispose();
    }

    /// Updates text and layout together after display text changes.
    private void updateTextAndMetrics() {
        updateText();
        updateMetrics();
    }

    /// Updates the rendered badge text.
    private void updateText() {
        label.setText(getSkinnable().getDisplayText());
    }

    /// Applies badge tokens to the skin layout.
    private void updateMetrics() {
        M3Badge badge = getSkinnable();
        if (badge.getDisplayText().isEmpty()) {
            double size = badge.getSmallSize();
            label.setMinSize(size, size);
            label.setPrefSize(size, size);
            label.setMaxSize(size, size);
            label.setPadding(Insets.EMPTY);
            label.setStyle("-fx-background-radius: " + formatPixels(size / 2.0) + ";");
        } else {
            double height = badge.getLargeHeight();
            label.setMinSize(badge.getLargeMinWidth(), height);
            label.setPrefHeight(height);
            label.setMaxHeight(height);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setPadding(new Insets(0.0, badge.getHorizontalPadding(), 0.0, badge.getHorizontalPadding()));
            label.setStyle("-fx-background-radius: " + formatPixels(badge.getContainerShape()) + ";");
        }
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }
}
