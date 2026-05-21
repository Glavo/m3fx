// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNullByDefault;

/// Base skin for controls that mirror an item list into one internal layout container.
@NotNullByDefault
abstract class M3ItemContainerSkinBase<C extends Control, P extends Pane> extends SkinBase<C> {
    /// The public item list mirrored by this skin.
    private final ObservableList<Node> items;

    /// The internal layout container that owns item nodes while this skin is installed.
    private final P container;

    /// Mirrors public item changes into the skin container.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Creates an item-container skin.
    M3ItemContainerSkinBase(C control, ObservableList<Node> items, P container) {
        super(control);
        this.items = items;
        this.container = container;
        container.setManaged(false);
        getChildren().add(container);
        items.addListener(itemsListener);
        updateItems();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        items.removeListener(itemsListener);
        container.getChildren().clear();
        super.dispose();
    }

    /// Returns the internal layout container.
    ///
    /// @return the internal layout container that mirrors the public item list
    protected final P getContainer() {
        return container;
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
        container.resizeRelocate(x, y, width, height);
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        container.getChildren().setAll(items);
        getSkinnable().requestLayout();
    }
}
