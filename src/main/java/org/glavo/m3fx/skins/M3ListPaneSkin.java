// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListItemBase;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// The default non-virtualized skin for [M3ListPane].
@NotNullByDefault
public final class M3ListPaneSkin extends SkinBase<M3ListPane> {
    /// Matches the first child in item order.
    private static final PseudoClass FIRST_CHILD = PseudoClass.getPseudoClass("first-child");

    /// Matches the last child in item order.
    private static final PseudoClass LAST_CHILD = PseudoClass.getPseudoClass("last-child");

    /// Matches a child without siblings.
    private static final PseudoClass ONLY_CHILD = PseudoClass.getPseudoClass("only-child");

    /// The internal vertical item container.
    private final VBox container = new VBox();

    /// Whether item position pseudo-classes need to be supplied for JavaFX 17 through 23.
    private final boolean emulateChildPositions;

    /// Mirrors public list item changes into the skin container.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Recomputes margins when the configured segmented gap changes.
    private final InvalidationListener itemSpacingInvalidation = observable -> updateItemMargins();

    /// Creates a static list pane skin.
    ///
    /// @param control the list pane controlled by this skin
    public M3ListPaneSkin(M3ListPane control) {
        super(control);
        getChildren().setAll(container);
        // JavaFX 24+ marks the container as the sole child of the control here.
        emulateChildPositions = !container.getPseudoClassStates().contains(ONLY_CHILD);
        control.getItems().addListener(itemsListener);
        control.itemSpacingProperty().addListener(itemSpacingInvalidation);
        updateItems();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemsListener);
        getSkinnable().itemSpacingProperty().removeListener(itemSpacingInvalidation);
        replaceItems(List.of());
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
        return topInset + container.minHeight(width < 0 ? -1 : Math.max(0, width - leftInset - rightInset)) + bottomInset;
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
        return topInset + container.prefHeight(width < 0 ? -1 : Math.max(0, width - leftInset - rightInset)) + bottomInset;
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
        return topInset + container.maxHeight(width < 0 ? -1 : Math.max(0, width - leftInset - rightInset)) + bottomInset;
    }

    /// Lays out the item container in the full control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        replaceItems(getSkinnable().getItems());
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
            boolean followedByListItem = item instanceof M3ListItemBase
                    && index + 1 < items.size()
                    && items.get(index + 1) instanceof M3ListItemBase;
            VBox.setMargin(item, followedByListItem ? spacingMargin : null);
        }
        container.requestLayout();
    }

    /// Replaces the displayed nodes, removing constraints and updating structural states on older runtimes.
    private void replaceItems(List<Node> items) {
        var children = container.getChildren();
        for (Node item : children) {
            VBox.setMargin(item, null);
        }
        if (!emulateChildPositions) {
            children.setAll(items);
            return;
        }

        @Nullable Node oldFirst = children.isEmpty() ? null : children.get(0);
        @Nullable Node oldLast = children.isEmpty() ? null : children.get(children.size() - 1);
        children.setAll(items);
        @Nullable Node first = children.isEmpty() ? null : children.get(0);
        @Nullable Node last = children.isEmpty() ? null : children.get(children.size() - 1);
        movePosition(FIRST_CHILD, oldFirst, first);
        movePosition(LAST_CHILD, oldLast, last);
        movePosition(ONLY_CHILD, oldFirst == oldLast ? oldFirst : null, first == last ? first : null);
    }

    /// Transfers one structural state only when the node occupying that position has changed.
    private static void movePosition(PseudoClass state, @Nullable Node previous, @Nullable Node current) {
        if (previous != current) {
            if (previous != null) {
                previous.pseudoClassStateChanged(state, false);
            }
            if (current != null) {
                current.pseudoClassStateChanged(state, true);
            }
        }
    }
}
