// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListPane;
import org.jetbrains.annotations.NotNullByDefault;

/// The default non-virtualized skin for [M3ListPane].
@NotNullByDefault
public final class M3ListPaneSkin extends SkinBase<M3ListPane> {
    /// The internal vertical item container.
    private final VBox container = new VBox();

    /// Mirrors public list item changes into the skin container.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Creates a static list pane skin.
    public M3ListPaneSkin(M3ListPane control) {
        super(control);
        container.setManaged(false);
        getChildren().add(container);
        control.getItems().addListener(itemsListener);
        updateItems();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemsListener);
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

    /// Lays out the item container in the full control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        container.getChildren().setAll(getSkinnable().getItems());
        getSkinnable().requestLayout();
    }
}
