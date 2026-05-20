// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.List;

/// The default Material Design 3 skin for [M3NavigationDrawerGroup].
@NotNullByDefault
public final class M3NavigationDrawerGroupSkin extends SkinBase<M3NavigationDrawerGroup> {
    /// The internal vertical item container.
    private final VBox container = new VBox();

    /// Mirrors child destination item changes into the skin container.
    private final ListChangeListener<M3ListItem> itemsListener = change -> updateItems();

    /// Mirrors expanded-state changes into the skin container.
    private final ChangeListener<Boolean> expandedListener = (observable, oldValue, newValue) -> updateItems();

    /// Creates a navigation drawer group skin.
    public M3NavigationDrawerGroupSkin(M3NavigationDrawerGroup control) {
        super(control);
        container.setManaged(false);
        container.setSpacing(4.0);
        getChildren().add(container);
        control.getItems().addListener(itemsListener);
        control.expandedProperty().addListener(expandedListener);
        updateItems();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemsListener);
        getSkinnable().expandedProperty().removeListener(expandedListener);
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

    /// Mirrors the header and visible child item list into the internal container.
    private void updateItems() {
        List<Node> visibleItems = new ArrayList<>();
        visibleItems.add(getSkinnable().getHeaderItem());
        if (getSkinnable().isExpanded()) {
            visibleItems.addAll(getSkinnable().getItems());
        }
        container.getChildren().setAll(visibleItems);
        getSkinnable().requestLayout();
    }

    /// Keeps header and child list item containers inside the group content area.
    private void updateListItemWidths(double width) {
        double itemWidth = Math.max(0.0, width);
        updateListItemWidth(getSkinnable().getHeaderItem(), itemWidth);
        for (M3ListItem item : getSkinnable().getItems()) {
            updateListItemWidth(item, itemWidth);
        }
    }

    /// Keeps one list item container inside the group content area.
    private static void updateListItemWidth(M3ListItem item, double itemWidth) {
        if (Double.compare(item.getMinWidth(), 0.0) != 0) {
            item.setMinWidth(0.0);
        }
        if (Double.compare(item.getMaxWidth(), itemWidth) != 0) {
            item.setMaxWidth(itemWidth);
        }
    }
}
