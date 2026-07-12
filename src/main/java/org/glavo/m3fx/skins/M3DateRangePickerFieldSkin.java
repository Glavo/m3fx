// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.AccessibleAttribute;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3DateRangePickerField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3DateRangePickerField].
@NotNullByDefault
public final class M3DateRangePickerFieldSkin extends SkinBase<M3DateRangePickerField> {
    /// The spacing between the start and end input layouts.
    private static final double FIELD_SPACING = 12.0;

    /// The internal two-field container.
    private final HBox container = new HBox(FIELD_SPACING);

    /// The retained start-date input layout.
    private final M3TextInputLayout startInputLayout;

    /// The retained end-date input layout.
    private final M3TextInputLayout endInputLayout;

    /// Creates a date range picker field skin.
    ///
    /// @param control the date range picker field controlled by this skin
    public M3DateRangePickerFieldSkin(M3DateRangePickerField control) {
        super(control);
        Object startItem = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0);
        Object endItem = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1);
        if (!(startItem instanceof M3TextInputLayout startLayout)
                || !(endItem instanceof M3TextInputLayout endLayout)) {
            throw new IllegalStateException("date range picker field input layouts are unavailable");
        }
        startInputLayout = startLayout;
        endInputLayout = endLayout;
        container.setManaged(false);
        container.getStyleClass().add(M3DateRangePickerField.CONTAINER_STYLE_CLASS);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        container.getChildren().setAll(startInputLayout, endInputLayout);
        getChildren().setAll(container);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
        super.dispose();
    }

    /// Computes the minimum width from the internal container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the internal container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the internal container.
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

    /// Computes the preferred height from the internal container.
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

    /// Computes the maximum width from the internal container.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.maxWidth(height) + rightInset;
    }

    /// Computes the maximum height from the internal container.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.maxHeight(width) + bottomInset;
    }

    /// Lays out the internal two-field container.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double spacing = container.getSpacing();
        double fieldWidth = Math.max(0.0, (width - spacing) / 2.0);
        startInputLayout.setPrefWidth(fieldWidth);
        endInputLayout.setPrefWidth(fieldWidth);
        container.resizeRelocate(x, y, width, height);
    }

}
