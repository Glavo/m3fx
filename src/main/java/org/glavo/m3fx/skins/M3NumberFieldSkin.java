// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.AccessibleAttribute;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3NumberField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3NumberField].
///
/// The skin presents the field-owned text input layout as its sole child and sizes it to the snapped control bounds.
@NotNullByDefault
public final class M3NumberFieldSkin extends SkinBase<M3NumberField> {
    /// The retained input layout measured and positioned by this skin.
    private final M3TextInputLayout inputLayout;

    /// Creates a number field skin.
    ///
    /// @param control the skinned number field
    /// @throws IllegalArgumentException if `control` is `null`
    /// @throws IllegalStateException    if the number field does not expose its input layout
    public M3NumberFieldSkin(M3NumberField control) {
        super(control);
        Object item = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0);
        if (!(item instanceof M3TextInputLayout layout)) {
            throw new IllegalStateException("number field input layout is unavailable");
        }
        inputLayout = layout;
        getChildren().setAll(inputLayout);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        M3NumberField control = getSkinnable();
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
