// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.AccessibleAttribute;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SplitButton].
@NotNullByDefault
public final class M3SplitButtonSkin extends SkinBase<M3SplitButton> {
    /// The internal horizontal button container.
    private final HBox container = new HBox();

    /// Creates a split button skin.
    ///
    /// @param control the split button controlled by this skin
    public M3SplitButtonSkin(M3SplitButton control) {
        super(control);
        container.setManaged(false);
        container.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        container.spacingProperty().bind(control.spacingProperty());
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        Object actionPart = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0);
        Object menuPart = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1);
        if (!(actionPart instanceof M3Button actionButton)) {
            throw new IllegalStateException("Split button action part must be an M3Button");
        }
        if (!(menuPart instanceof M3MenuButton menuButton)) {
            throw new IllegalStateException("Split button menu part must be an M3MenuButton");
        }
        container.getChildren().setAll(actionButton, menuButton);
        getChildren().setAll(container);
    }

    /// Removes child references before disposal.
    @Override
    public void dispose() {
        container.alignmentProperty().unbind();
        container.spacingProperty().unbind();
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
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
