// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3StatusLight;
import org.glavo.m3fx.controls.M3StatusLightSize;
import org.jetbrains.annotations.NotNullByDefault;

/// The default retained-mode skin for [M3StatusLight].
///
/// The skin retains one circular indicator and one label. Their local order places the indicator first; JavaFX
/// node-orientation mirroring moves it to logical leading in right-to-left layouts.
@NotNullByDefault
public final class M3StatusLightSkin extends SkinBase<M3StatusLight> {
    /// The retained circular status indicator.
    private final Region indicator = new Region();

    /// The retained descriptive label.
    private final Label label = new Label();

    /// Updates label content after text changes.
    private final InvalidationListener textInvalidation = observable -> updateText();

    /// Updates geometry after the size role changes.
    private final InvalidationListener sizeInvalidation = observable -> updateMetrics();

    /// Updates the indicator fill after its paint changes.
    private final InvalidationListener colorInvalidation = observable -> updateIndicatorPaint();

    /// Creates a status-light skin.
    ///
    /// @param control the status light controlled by this skin
    public M3StatusLightSkin(M3StatusLight control) {
        super(control);
        indicator.setManaged(false);
        indicator.setMouseTransparent(true);
        indicator.getStyleClass().add("m3-status-light-indicator");
        label.setManaged(false);
        label.setMouseTransparent(true);
        label.getStyleClass().add("m3-status-light-label");
        getChildren().setAll(indicator, label);

        control.textProperty().addListener(textInvalidation);
        control.sizeProperty().addListener(sizeInvalidation);
        control.indicatorColorProperty().addListener(colorInvalidation);
        updateText();
        updateMetrics();
    }

    /// Removes listeners and retained nodes before disposal.
    @Override
    public void dispose() {
        M3StatusLight control = getSkinnable();
        control.textProperty().removeListener(textInvalidation);
        control.sizeProperty().removeListener(sizeInvalidation);
        control.indicatorColorProperty().removeListener(colorInvalidation);
        getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the indicator and label.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the minimum height from the indicator and label.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the preferred width from the indicator, spacing, and label.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3StatusLightSize size = getSkinnable().getSize();
        boolean hasText = !getSkinnable().getText().isEmpty();
        double labelWidth = hasText ? label.prefWidth(height) : 0.0;
        double spacing = hasText ? size.getSpacing() : 0.0;
        return leftInset + size.getIndicatorSize() + spacing + labelWidth + rightInset;
    }

    /// Computes the preferred height from the larger of the indicator and label.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        boolean hasText = !getSkinnable().getText().isEmpty();
        double labelHeight = hasText ? label.prefHeight(width) : 0.0;
        double contentHeight = Math.max(getSkinnable().getSize().getIndicatorSize(), labelHeight);
        return topInset + contentHeight + bottomInset;
    }

    /// Prevents horizontal growth beyond the preferred status-light width.
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

    /// Prevents vertical growth beyond the preferred status-light height.
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

    /// Places the indicator at logical leading and vertically centers both retained nodes.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3StatusLight control = getSkinnable();
        M3StatusLightSize sizeRole = control.getSize();
        double indicatorSize = snapSizeX(sizeRole.getIndicatorSize());
        boolean hasText = !control.getText().isEmpty();
        double labelWidth = hasText ? snapSizeX(label.prefWidth(height)) : 0.0;
        double labelHeight = hasText ? snapSizeY(label.prefHeight(labelWidth)) : 0.0;
        double spacing = hasText ? snapSpaceX(sizeRole.getSpacing()) : 0.0;
        double indicatorY = snapPositionY(y + (height - indicatorSize) / 2.0);
        double labelY = snapPositionY(y + (height - labelHeight) / 2.0);
        indicator.resizeRelocate(x, indicatorY, indicatorSize, indicatorSize);
        label.resizeRelocate(x + indicatorSize + spacing, labelY, labelWidth, labelHeight);
    }

    /// Updates the retained label text and visibility.
    private void updateText() {
        String text = getSkinnable().getText();
        label.setText(text);
        label.setVisible(!text.isEmpty());
        getSkinnable().requestLayout();
    }

    /// Updates indicator dimensions and paint after a size change.
    private void updateMetrics() {
        double size = getSkinnable().getSize().getIndicatorSize();
        indicator.setMinSize(size, size);
        indicator.setPrefSize(size, size);
        indicator.setMaxSize(size, size);
        updateIndicatorPaint();
        getSkinnable().requestLayout();
    }

    /// Applies the effective indicator paint to the retained circle.
    private void updateIndicatorPaint() {
        double radius = getSkinnable().getSize().getIndicatorSize() / 2.0;
        indicator.setBackground(new Background(new BackgroundFill(
                getSkinnable().getIndicatorColor(),
                new CornerRadii(radius),
                Insets.EMPTY
        )));
    }
}
