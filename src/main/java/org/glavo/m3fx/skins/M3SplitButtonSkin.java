// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.AccessibleAttribute;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SplitButton].
@NotNullByDefault
public final class M3SplitButtonSkin extends SkinBase<M3SplitButton> {
    /// The non-shrinking internal button container.
    private final Pane container = new Pane();

    /// The primary action part.
    private final M3Button actionButton;

    /// The trailing menu part.
    private final M3MenuButton menuButton;

    /// Creates a split button skin.
    ///
    /// @param control the split button controlled by this skin
    public M3SplitButtonSkin(M3SplitButton control) {
        super(control);
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        Object actionPart = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0);
        Object menuPart = control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1);
        if (!(actionPart instanceof M3Button resolvedActionButton)) {
            throw new IllegalStateException("Split button action part must be an M3Button");
        }
        if (!(menuPart instanceof M3MenuButton resolvedMenuButton)) {
            throw new IllegalStateException("Split button menu part must be an M3MenuButton");
        }
        actionButton = resolvedActionButton;
        menuButton = resolvedMenuButton;
        container.getChildren().setAll(actionButton, menuButton);
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

    /// Computes the fixed minimum width required by both button parts and their between-space.
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

    /// Computes the minimum height required by the taller button part.
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

    /// Computes the width of both preferred button parts and the exact between-space.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset
                + actionButton.prefWidth(height)
                + getSkinnable().getSpacing()
                + menuButton.prefWidth(height)
                + rightInset;
    }

    /// Computes the preferred height of the taller button part.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset
                + Math.max(actionButton.prefHeight(-1.0), menuButton.prefHeight(-1.0))
                + bottomInset;
    }

    /// Computes the fixed maximum width used by this fixed-format control.
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

    /// Computes the fixed maximum height used by this fixed-format control.
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

    /// Lays out both parts without shrinking them; JavaFX node orientation mirrors the container when needed.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
        double actionWidth = snapSizeX(actionButton.prefWidth(height));
        double menuWidth = snapSizeX(menuButton.prefWidth(height));
        double spacing = getSkinnable().getSpacing();
        double contentWidth = actionWidth + spacing + menuWidth;
        double startX = snapPositionX(Math.max(0.0, (width - contentWidth) / 2.0));
        double actionHeight = snapSizeY(actionButton.prefHeight(actionWidth));
        double menuHeight = snapSizeY(menuButton.prefHeight(menuWidth));
        double actionY = snapPositionY((height - actionHeight) / 2.0);
        double menuY = snapPositionY((height - menuHeight) / 2.0);

        actionButton.resizeRelocate(startX, actionY, actionWidth, actionHeight);
        menuButton.resizeRelocate(
                snapPositionX(startX + actionWidth + spacing),
                menuY,
                menuWidth,
                menuHeight
        );
    }
}