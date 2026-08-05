// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3PickerField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// The default Material Design 3 skin for [M3PickerField].
///
/// The skin presents the field's text-input layout as its sole content and sizes that layout to the snapped control
/// bounds. The input layout is owned by the picker field and must be available when the skin is created.
///
/// @param <T> the value type edited by the picker field
/// @param <P> the popup picker control type
@NotNullByDefault
public final class M3PickerFieldSkin<T, P extends Control> extends SkinBase<M3PickerField<T, P>> {
    /// The retained input layout measured and positioned by this skin.
    private final M3TextInputLayout inputLayout;

    /// Creates a picker field skin.
    ///
    /// @param control     the skinned picker field
    /// @param inputLayout the field-owned input layout to present
    /// @throws IllegalArgumentException if `control` is `null`
    /// @throws NullPointerException     if `inputLayout` is `null`
    public M3PickerFieldSkin(M3PickerField<T, P> control, M3TextInputLayout inputLayout) {
        super(control);
        this.inputLayout = Objects.requireNonNull(inputLayout, "inputLayout");
        getChildren().setAll(this.inputLayout);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        M3PickerField<T, P> control = getSkinnable();
        if (control.getSkin() == null || control.getSkin() == this) {
            getChildren().clear();
        }
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
        return topInset + inputLayout.maxHeight(width) + bottomInset;
    }

    /// Lays out the wrapped input layout in the full skin bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        inputLayout.resizeRelocate(x, y, width, height);
    }
}
