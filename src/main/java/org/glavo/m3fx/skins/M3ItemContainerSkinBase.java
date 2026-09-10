// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Base skin for controls that mirror an observable item list into one layout container.
///
/// While the skin is installed, item nodes are children of the supplied container and list membership, insertion,
/// removal, and permutation changes are mirrored into its child list. Disposing the skin releases those child
/// references without modifying the source item list.
@NotNullByDefault
abstract class M3ItemContainerSkinBase<C extends Control, P extends Pane, N extends Node> extends SkinBase<C> {
    /// The public item list mirrored by this skin.
    private final ObservableList<N> items;

    /// The internal layout container that owns item nodes while this skin is installed.
    private final P container;

    /// Mirrors public item changes into the skin container.
    private final InvalidationListener itemsListener = observable -> updateItems();

    /// Whether item nodes are currently being synchronized with the container.
    private boolean updatingItems;

    /// Whether a nested item mutation requires another synchronization pass.
    private boolean itemsRefreshPending;

    /// Whether disposal has released this skin's listeners and child references.
    private boolean disposed;

    /// Creates an item-container skin and installs its list listener.
    ///
    /// @param control   the control represented by this skin
    /// @param items     the observable item list to mirror
    /// @param container the container that hosts item nodes while this skin is installed
    M3ItemContainerSkinBase(C control, ObservableList<N> items, P container) {
        super(control);
        this.items = items;
        this.container = container;
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        getChildren().setAll(container);
        items.addListener(itemsListener);
        updateItems();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        items.removeListener(itemsListener);
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
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

    /// Reconciles actual item membership after mutations, including changes from parent or scene callbacks.
    private void updateItems() {
        if (disposed) {
            return;
        }
        itemsRefreshPending = true;
        if (updatingItems) {
            return;
        }
        updatingItems = true;
        try {
            do {
                itemsRefreshPending = false;
                @Unmodifiable List<N> currentItems = List.copyOf(items);
                ObservableList<Node> children = container.getChildren();
                boolean changed = children.size() != currentItems.size();
                for (int index = 0; !changed && index < currentItems.size(); index++) {
                    changed = children.get(index) != currentItems.get(index);
                }
                if (changed) {
                    children.setAll(currentItems);
                    if (!disposed) {
                        getSkinnable().requestLayout();
                    }
                }
            } while (itemsRefreshPending && !disposed);
        } finally {
            updatingItems = false;
        }
    }
}
