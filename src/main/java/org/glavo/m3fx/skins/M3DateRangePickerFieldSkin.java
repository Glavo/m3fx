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

    /// Creates a date range picker field skin.
    ///
    /// @param control the date range picker field controlled by this skin
    public M3DateRangePickerFieldSkin(M3DateRangePickerField control) {
        super(control);
        container.setManaged(false);
        container.getStyleClass().add(M3DateRangePickerField.CONTAINER_STYLE_CLASS);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        container.getChildren().setAll(inputLayout(0), inputLayout(1));
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
        M3TextInputLayout startInputLayout = inputLayout(0);
        M3TextInputLayout endInputLayout = inputLayout(1);
        double spacing = container.getSpacing();
        double fieldWidth = Math.max(0.0, (width - spacing) / 2.0);
        startInputLayout.setPrefWidth(fieldWidth);
        endInputLayout.setPrefWidth(fieldWidth);
        container.resizeRelocate(x, y, width, height);
    }

    /// Returns one wrapped input layout exposed by the skinnable accessibility tree.
    ///
    /// @param index the indexed input layout to return
    /// @return the indexed input layout
    private M3TextInputLayout inputLayout(int index) {
        Object item = getSkinnable().queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, index);
        if (item instanceof M3TextInputLayout inputLayout) {
            return inputLayout;
        }
        throw new IllegalStateException("date range picker field input layout is unavailable");
    }
}
