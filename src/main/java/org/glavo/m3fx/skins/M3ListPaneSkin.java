// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListItem;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default non-virtualized skin for [M3ListPane].
@NotNullByDefault
public final class M3ListPaneSkin extends SkinBase<M3ListPane> {
    /// The internal vertical item container.
    private final VBox container = new VBox();

    /// Mirrors public list item changes into the skin container.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Recomputes margins when the configured segmented gap changes.
    private final InvalidationListener itemSpacingInvalidation = observable -> updateItemMargins();

    /// Creates a static list pane skin.
    ///
    /// @param control the list pane controlled by this skin
    public M3ListPaneSkin(M3ListPane control) {
        super(control);
        container.setManaged(false);
        getChildren().setAll(container);
        control.getItems().addListener(itemsListener);
        control.itemSpacingProperty().addListener(itemSpacingInvalidation);
        updateItems();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemsListener);
        getSkinnable().itemSpacingProperty().removeListener(itemSpacingInvalidation);
        clearItemMargins();
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

    /// Lays out the item container in the full control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        clearItemMargins();
        container.getChildren().setAll(getSkinnable().getItems());
        updateItemMargins();
        getSkinnable().requestLayout();
    }

    /// Applies spacing only between directly adjacent list items.
    private void updateItemMargins() {
        double spacing = getSkinnable().getItemSpacing();
        var items = container.getChildren();
        @Nullable Insets spacingMargin =
                spacing > 0.0 ? new Insets(0.0, 0.0, spacing, 0.0) : null;
        for (int index = 0; index < items.size(); index++) {
            Node item = items.get(index);
            boolean followedByListItem = item instanceof M3ListItem
                    && index + 1 < items.size()
                    && items.get(index + 1) instanceof M3ListItem;
            VBox.setMargin(item, followedByListItem ? spacingMargin : null);
        }
        container.requestLayout();
    }

    /// Removes layout constraints installed on externally owned list item nodes.
    private void clearItemMargins() {
        for (Node item : container.getChildren()) {
            VBox.setMargin(item, null);
        }
    }
}
