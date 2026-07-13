// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.ListChangeListener;
import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3NavigationBar].
@NotNullByDefault
public final class M3NavigationBarSkin extends SkinBase<M3NavigationBar> {
    /// The internal item container that applies compact or medium-window distribution.
    private final NavigationContainer container = new NavigationContainer();

    /// Mirrors public item changes into the skin container.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Requests a new item distribution when the resolved spacing token changes.
    private final InvalidationListener itemSpacingListener = observable -> {
        container.requestLayout();
        getSkinnable().requestLayout();
    };

    /// Creates a navigation bar skin.
    ///
    /// @param control the skinned navigation bar
    public M3NavigationBarSkin(M3NavigationBar control) {
        super(control);
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        getChildren().setAll(container);
        control.getItems().addListener(itemsListener);
        control.itemSpacingProperty().addListener(itemSpacingListener);
        updateItems();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemsListener);
        getSkinnable().itemSpacingProperty().removeListener(itemSpacingListener);
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
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

    /// Computes the maximum width supported by a full-width navigation bar.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return Double.MAX_VALUE;
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
        container.resizeRelocate(x, y, width, height);
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        container.getChildren().setAll(getSkinnable().getItems());
        container.requestLayout();
        getSkinnable().requestLayout();
    }

    /// Returns the summed minimum or preferred width of managed navigation items.
    private double itemWidthSum(double height, boolean minimum) {
        double width = 0.0;
        int itemCount = 0;
        for (Node child : container.getChildren()) {
            if (child.isManaged()) {
                width += minimum ? child.minWidth(height) : child.prefWidth(height);
                itemCount++;
            }
        }
        if (itemCount > 1 && getSkinnable().getItemLayout() == M3NavigationItemLayout.VERTICAL) {
            width += getSkinnable().getItemSpacing() * (itemCount - 1);
        }
        return width;
    }

    /// Returns the largest minimum or preferred height of managed navigation items.
    private double itemHeightMaximum(double width, boolean minimum) {
        double height = 0.0;
        for (Node child : container.getChildren()) {
            if (child.isManaged()) {
                height = Math.max(height, minimum ? child.minHeight(width) : child.prefHeight(width));
            }
        }
        return height;
    }

    /// Returns the number of managed navigation items.
    private int managedItemCount() {
        int count = 0;
        for (Node child : container.getChildren()) {
            if (child.isManaged()) {
                count++;
            }
        }
        return count;
    }

    /// Lays out compact items at equal widths or centers fixed-width medium items.
    private void layoutItems(double width, double height) {
        int itemCount = managedItemCount();
        if (itemCount == 0) {
            return;
        }

        if (getSkinnable().getItemLayout() == M3NavigationItemLayout.VERTICAL) {
            double spacing = getSkinnable().getItemSpacing();
            double availableWidth = Math.max(0.0, width - spacing * (itemCount - 1));
            int itemIndex = 0;
            for (Node child : container.getChildren()) {
                if (!child.isManaged()) {
                    continue;
                }
                double start = container.snapPositionX(
                        availableWidth * itemIndex / itemCount + spacing * itemIndex
                );
                double end = container.snapPositionX(
                        availableWidth * (itemIndex + 1) / itemCount + spacing * itemIndex
                );
                container.layoutChild(
                        child,
                        start,
                        Math.max(0.0, end - start),
                        height
                );
                itemIndex++;
            }
            return;
        }

        double preferredWidth = itemWidthSum(height, false);
        double currentX = Math.max(0.0, (width - preferredWidth) / 2.0);
        for (Node child : container.getChildren()) {
            if (!child.isManaged()) {
                continue;
            }
            double childWidth = child.prefWidth(height);
            container.layoutChild(child, currentX, childWidth, height);
            currentX += childWidth;
        }
    }

    /// Pane that delegates navigation-specific size and layout calculations to the skin.
    @NotNullByDefault
    private final class NavigationContainer extends Pane {
        /// Computes the minimum content width.
        @Override
        protected double computeMinWidth(double height) {
            return itemWidthSum(height, true);
        }

        /// Computes the minimum content height.
        @Override
        protected double computeMinHeight(double width) {
            return itemHeightMaximum(width, true);
        }

        /// Computes the preferred content width.
        @Override
        protected double computePrefWidth(double height) {
            return itemWidthSum(height, false);
        }

        /// Computes the preferred content height.
        @Override
        protected double computePrefHeight(double width) {
            return itemHeightMaximum(width, false);
        }

        /// Allows the compact bar to stretch across its parent width.
        @Override
        protected double computeMaxWidth(double height) {
            return Double.MAX_VALUE;
        }

        /// Returns the largest child maximum height.
        @Override
        protected double computeMaxHeight(double width) {
            return itemHeightMaximum(width, false);
        }

        /// Lays out one child inside a navigation item slot.
        private void layoutChild(Node child, double x, double width, double height) {
            if (child.isResizable()) {
                child.resizeRelocate(x, 0.0, width, height);
            } else {
                layoutInArea(child, x, 0.0, width, height, 0.0, HPos.CENTER, VPos.CENTER);
            }
        }

        /// Applies the current compact or medium-window item layout.
        @Override
        protected void layoutChildren() {
            layoutItems(getWidth(), getHeight());
        }
    }
}