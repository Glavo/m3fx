// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.control.SkinBase;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3SplitButton;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SplitButton].
@NotNullByDefault
public final class M3SplitButtonSkin extends SkinBase<M3SplitButton> {
    /// The spacing that lets the split button borders overlap.
    private static final double SPLIT_BUTTON_SPACING = -1.0;

    /// The internal horizontal button container.
    private final HBox container = new HBox(SPLIT_BUTTON_SPACING);

    /// Creates a split button skin.
    public M3SplitButtonSkin(M3SplitButton control) {
        super(control);
        container.setManaged(false);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().setAll(control.getActionButton(), control.getMenuButton());
        getChildren().add(container);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        container.getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the internal button container.
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

    /// Computes the minimum height from the internal button container.
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

    /// Computes the preferred width from the internal button container.
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

    /// Computes the preferred height from the internal button container.
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

    /// Computes the maximum width from the internal button container.
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

    /// Computes the maximum height from the internal button container.
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

    /// Lays out the internal button container in the full control content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }
}
