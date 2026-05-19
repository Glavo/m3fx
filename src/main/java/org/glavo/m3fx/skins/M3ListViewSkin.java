// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.util.Callback;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3ListViewCell;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default virtualized skin for [M3ListView].
@NotNullByDefault
public final class M3ListViewSkin<T> extends SkinBase<M3ListView<T>> {
    /// The virtualized cell container.
    private final ListViewVirtualFlow<T> flow = new ListViewVirtualFlow<>();

    /// Updates virtual flow cell count when data items change.
    private final ListChangeListener<T> itemsListener = change -> refreshItemCount();

    /// Rebuilds visible cells when the cell factory changes.
    private final InvalidationListener cellFactoryInvalidation = observable -> rebuildCells();

    /// Updates visible cells when selection changes.
    private final ListChangeListener<Integer> selectedIndicesListener = change -> refreshCells();

    /// Creates a virtualized list view skin.
    public M3ListViewSkin(M3ListView<T> control) {
        super(control);
        flow.getStyleClass().add("m3-list-view-flow");
        flow.setVertical(true);
        flow.setPannable(true);
        flow.setCellFactory(createCellFactory(control));
        flow.fixedCellSizeProperty().bind(control.fixedCellSizeProperty());
        getChildren().add(flow);

        control.getItems().addListener(itemsListener);
        control.cellFactoryProperty().addListener(cellFactoryInvalidation);
        control.getSelectedIndices().addListener(selectedIndicesListener);
        refreshItemCount();
    }

    /// Stops bindings and removes listeners before disposal.
    @Override
    public void dispose() {
        M3ListView<T> listView = getSkinnable();
        listView.getItems().removeListener(itemsListener);
        listView.cellFactoryProperty().removeListener(cellFactoryInvalidation);
        listView.getSelectedIndices().removeListener(selectedIndicesListener);
        flow.fixedCellSizeProperty().unbind();
        flow.setCellFactory(null);
        super.dispose();
    }

    /// Lays out the virtual flow in the available bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        flow.resizeRelocate(x, y, width, height);
    }

    /// Computes the preferred width from the virtual flow.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + Math.max(240.0, flow.prefWidth(height)) + rightInset;
    }

    /// Computes the preferred height from visible row metrics.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double fixedCellSize = getSkinnable().getFixedCellSize();
        double visibleRows = Math.min(8.0, Math.max(1.0, getSkinnable().getItems().size()));
        double rowHeight = fixedCellSize > 0.0 ? fixedCellSize : 56.0;
        return topInset + rowHeight * visibleRows + bottomInset;
    }

    /// Updates the number of cells owned by the virtual flow.
    public void refreshItemCount() {
        flow.setCellCount(getSkinnable().getItems().size());
        flow.refreshCells();
    }

    /// Requests visible cell state and layout updates.
    public void refreshCells() {
        flow.refreshCells();
    }

    /// Recreates visible cells after the cell factory changes.
    public void rebuildCells() {
        flow.rebuildAllCells();
    }

    /// Scrolls the virtual flow to the supplied index.
    public void scrollTo(int index) {
        flow.scrollTo(index);
    }

    /// Returns the rendered list item for a visible or reusable cell index.
    public @Nullable Node getVisibleItem(int index) {
        @Nullable M3ListViewCell<T> cell = flow.getVisibleCell(index);
        if (cell == null) {
            cell = flow.getCell(index);
        }
        return cell == null ? null : cell.getListItem();
    }

    /// Creates the virtual flow cell factory.
    private static <T> Callback<VirtualFlow<M3ListViewCell<T>>, M3ListViewCell<T>> createCellFactory(
            M3ListView<T> listView
    ) {
        return flow -> new M3ListViewCell<>(listView);
    }

    /// A public-API wrapper exposing protected virtual flow refresh hooks to this skin.
    @NotNullByDefault
    private static final class ListViewVirtualFlow<T> extends VirtualFlow<M3ListViewCell<T>> {
        /// Requests visible cell relayout and selection refresh.
        private void refreshCells() {
            requestCellLayout();
            requestLayout();
            for (M3ListViewCell<T> cell : getCells()) {
                cell.refreshSelection();
            }
        }

        /// Rebuilds the virtual flow cell pile from the current cell factory.
        private void rebuildAllCells() {
            recreateCells();
            requestLayout();
        }
    }
}
