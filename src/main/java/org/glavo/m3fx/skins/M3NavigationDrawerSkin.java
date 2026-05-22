// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3NavigationDrawer].
@NotNullByDefault
public final class M3NavigationDrawerSkin extends SkinBase<M3NavigationDrawer> {
    /// The internal vertical drawer item container.
    private final VBox container = new VBox();

    /// Mirrors public item changes into the skin container.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Creates a navigation drawer skin.
    ///
    /// @param control the skinned navigation drawer
    public M3NavigationDrawerSkin(M3NavigationDrawer control) {
        super(control);
        container.setManaged(false);
        container.setSpacing(4.0);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        getChildren().add(container);
        control.getItems().addListener(itemsListener);
        updateItems();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemsListener);
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the internal item container.
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

    /// Computes the minimum height from the internal item container.
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

    /// Computes the preferred width from the internal item container.
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

    /// Computes the preferred height from the internal item container.
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

    /// Computes the maximum width from the internal item container.
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

    /// Computes the maximum height from the internal item container.
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

    /// Lays out the item container in the full control content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        updateListItemWidths(width);
        container.resizeRelocate(x, y, width, height);
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        container.getChildren().setAll(getSkinnable().getItems());
        getSkinnable().requestLayout();
    }

    /// Keeps drawer list item containers inside the drawer content area.
    private void updateListItemWidths(double width) {
        double itemWidth = Math.max(0.0, width);
        for (Node child : getSkinnable().getItems()) {
            if (child instanceof Region region) {
                if (Double.compare(region.getMinWidth(), 0.0) != 0) {
                    region.setMinWidth(0.0);
                }
                if (Double.compare(region.getMaxWidth(), itemWidth) != 0) {
                    region.setMaxWidth(itemWidth);
                }
            }
        }
    }
}
