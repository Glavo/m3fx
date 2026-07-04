// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Pos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3FabMenu].
@NotNullByDefault
public final class M3FabMenuSkin extends SkinBase<M3FabMenu> {
    /// The internal vertical container.
    private final VBox container = new VBox();

    /// The action item container owned by the skinnable control.
    private final VBox actions;

    /// Creates a floating action button menu skin.
    ///
    /// @param control the floating action button menu controlled by this skin
    /// @param actions the action item container owned by the control
    /// @param toggleButton the toggle button owned by the control
    public M3FabMenuSkin(M3FabMenu control, VBox actions, M3FloatingActionButton toggleButton) {
        super(control);
        this.actions = actions;
        container.setManaged(false);
        container.setAlignment(Pos.BOTTOM_RIGHT);
        actions.setAlignment(Pos.BOTTOM_RIGHT);
        container.spacingProperty().bind(control.actionSpacingProperty());
        actions.spacingProperty().bind(control.actionSpacingProperty());
        actions.setFillWidth(false);
        container.getChildren().setAll(actions, toggleButton);
        getChildren().add(container);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        container.spacingProperty().unbind();
        actions.spacingProperty().unbind();
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
