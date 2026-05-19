// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.application.Platform;
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

    /// Updates logical focused-row visuals when the list view focus owner state changes.
    private final InvalidationListener focusedInvalidation = observable -> refreshCells();

    /// Whether a focused cell should refresh logical row focus after the next layout pass.
    private boolean focusRequestPending;

    /// Whether a deferred focus retry has already been queued for the next pulse.
    private boolean focusRetryScheduled;

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
        control.focusedProperty().addListener(focusedInvalidation);
        refreshItemCount();
    }

    /// Stops bindings and removes listeners before disposal.
    @Override
    public void dispose() {
        M3ListView<T> listView = getSkinnable();
        listView.getItems().removeListener(itemsListener);
        listView.cellFactoryProperty().removeListener(cellFactoryInvalidation);
        listView.getSelectedIndices().removeListener(selectedIndicesListener);
        listView.focusedProperty().removeListener(focusedInvalidation);
        flow.fixedCellSizeProperty().unbind();
        flow.setCellFactory(null);
        super.dispose();
    }

    /// Lays out the virtual flow in the available bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        flow.resizeRelocate(x, y, width, height);
        if (focusRequestPending) {
            flow.applyCss();
            flow.layout();
        }
        focusVisibleCellIfNeeded();
        scheduleFocusRetry();
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

    /// Requests visible cell focus updates, optionally asking the list view to own node focus.
    public void refreshFocus(boolean requestNodeFocus) {
        focusRequestPending |= requestNodeFocus;
        flow.refreshCells();
        if (getSkinnable().getFocusedIndex() >= 0) {
            flow.scrollTo(getSkinnable().getFocusedIndex());
        }
        getSkinnable().requestLayout();
        focusVisibleCellIfNeeded();
        scheduleFocusRetry();
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

    /// Refreshes the visible cell that owns the focused index when it has been materialized.
    private void focusVisibleCellIfNeeded() {
        if (!focusRequestPending) {
            return;
        }

        int index = getSkinnable().getFocusedIndex();
        if (index < 0) {
            getSkinnable().requestFocus();
            focusRequestPending = false;
            return;
        }

        @Nullable M3ListViewCell<T> cell = flow.findVisibleCell(index);
        if (cell != null && cell.focusCell()) {
            focusRequestPending = false;
        }
    }

    /// Queues a next-pulse focus retry when `VirtualFlow` has not materialized the requested row yet.
    private void scheduleFocusRetry() {
        if (!focusRequestPending || focusRetryScheduled) {
            return;
        }

        focusRetryScheduled = true;
        Platform.runLater(() -> {
            focusRetryScheduled = false;
            if (!focusRequestPending) {
                return;
            }

            getSkinnable().applyCss();
            flow.applyCss();
            flow.layout();
            focusVisibleCellIfNeeded();
            if (focusRequestPending) {
                getSkinnable().requestLayout();
            }
        });
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
                cell.refreshFocus();
            }
        }

        /// Returns a currently attached cell for the requested index.
        private @Nullable M3ListViewCell<T> findVisibleCell(int index) {
            for (M3ListViewCell<T> cell : getCells()) {
                if (!cell.isEmpty() && cell.getIndex() == index && cell.getScene() != null) {
                    return cell;
                }
            }
            return null;
        }

        /// Rebuilds the virtual flow cell pile from the current cell factory.
        private void rebuildAllCells() {
            recreateCells();
            requestLayout();
        }
    }
}
