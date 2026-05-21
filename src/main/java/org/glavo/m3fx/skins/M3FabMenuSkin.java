// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Pos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3FabMenu;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3FabMenu].
@NotNullByDefault
public final class M3FabMenuSkin extends SkinBase<M3FabMenu> {
    /// The spacing between floating action buttons.
    private static final double BUTTON_SPACING = 12.0;

    /// The internal vertical container.
    private final VBox container = new VBox(BUTTON_SPACING);

    /// Creates a floating action button menu skin.
    ///
    /// @param control the floating action button menu controlled by this skin
    public M3FabMenuSkin(M3FabMenu control) {
        super(control);
        container.setManaged(false);
        container.setAlignment(Pos.BOTTOM_RIGHT);
        control.getActionsContainer().setAlignment(Pos.BOTTOM_RIGHT);
        control.getActionsContainer().setSpacing(BUTTON_SPACING);
        control.getActionsContainer().setFillWidth(false);
        container.getChildren().setAll(control.getActionsContainer(), control.getToggleButton());
        getChildren().add(container);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        container.getChildren().clear();
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

    /// Lays out the internal container in the full control content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }
}
