// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3PickerField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3PickerField].
///
/// @param <T> the value type edited by the picker field
/// @param <P> the popup picker control type
@NotNullByDefault
public final class M3PickerFieldSkin<T, P extends Control> extends SkinBase<M3PickerField<T, P>> {
    /// Creates a picker field skin.
    ///
    /// @param control the skinned picker field
    public M3PickerFieldSkin(M3PickerField<T, P> control) {
        super(control);
        control.getInputLayout().nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        getChildren().add(control.getInputLayout());
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getInputLayout().nodeOrientationProperty().unbind();
        getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the wrapped input layout.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3TextInputLayout inputLayout = getSkinnable().getInputLayout();
        return leftInset + inputLayout.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the wrapped input layout.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3TextInputLayout inputLayout = getSkinnable().getInputLayout();
        return topInset + inputLayout.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the wrapped input layout.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3TextInputLayout inputLayout = getSkinnable().getInputLayout();
        return leftInset + inputLayout.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the wrapped input layout.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3TextInputLayout inputLayout = getSkinnable().getInputLayout();
        return topInset + inputLayout.prefHeight(width) + bottomInset;
    }

    /// Computes the maximum width from the wrapped input layout.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3TextInputLayout inputLayout = getSkinnable().getInputLayout();
        return leftInset + inputLayout.maxWidth(height) + rightInset;
    }

    /// Computes the maximum height from the wrapped input layout.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3TextInputLayout inputLayout = getSkinnable().getInputLayout();
        return topInset + inputLayout.maxHeight(width) + bottomInset;
    }

    /// Lays out the wrapped input layout in the full skin bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        getSkinnable().getInputLayout().resizeRelocate(x, y, width, height);
    }
}
