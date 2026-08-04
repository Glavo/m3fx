// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3TableRow;
import org.glavo.m3fx.controls.M3TableView;
import org.glavo.m3fx.internal.M3FocusVisibleTracker;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// The default virtualized skin for [M3TableView].
///
/// The skin preserves JavaFX table columns, sorting, editing, selection, focus, accessibility, and virtualization.
/// It applies the standalone Material scrollbar contract and delegates keyboard-visible focus from the composite
/// table to its current materialized row.
///
/// @param <T> the row-item type
@NotNullByDefault
public final class M3TableViewSkin<T> extends TableViewSkin<T> {
    /// Tracks keyboard-visible focus on the composite table for its logical focused row.
    private final M3FocusVisibleTracker focusVisibleTracker =
            new M3FocusVisibleTracker(getSkinnable(), this::refreshFocusedRowFeedback, null);

    /// Refreshes logical row focus when the table's focus eligibility changes.
    private final InvalidationListener focusEligibilityListener = observable -> {
        focusVisibleTracker.refresh();
        refreshFocusedRowFeedback();
    };

    /// Refreshes logical row focus when the focused row index changes.
    private final InvalidationListener focusedIndexListener = observable -> refreshFocusedRowFeedback();

    /// Moves focused-index observation when an application replaces the inherited focus model.
    private final ChangeListener<@Nullable TableViewFocusModel<T>> focusModelListener =
            (observable, oldModel, newModel) -> updateObservedFocusModel(oldModel, newModel);

    /// Refreshes focus feedback when row or cell selection mode changes.
    private final InvalidationListener cellSelectionListener = observable -> refreshFocusedRowFeedback();

    /// Moves cell-selection observation when an application replaces the inherited selection model.
    private final ChangeListener<@Nullable TableViewSelectionModel<T>> selectionModelListener =
            (observable, oldModel, newModel) -> updateObservedSelectionModel(oldModel, newModel);

    /// Whether this skin has released its listeners and must ignore deferred work.
    private boolean disposed;

    /// Creates a Material table-view skin.
    ///
    /// @param control the skinned Material table view
    public M3TableViewSkin(M3TableView<T> control) {
        super(control);
        materialFlow().setLayoutCompletion(this::refreshFocusedRowFeedback);
        control.focusedProperty().addListener(focusEligibilityListener);
        control.disabledProperty().addListener(focusEligibilityListener);
        control.focusModelProperty().addListener(focusModelListener);
        control.selectionModelProperty().addListener(selectionModelListener);
        updateObservedFocusModel(null, control.getFocusModel());
        updateObservedSelectionModel(null, control.getSelectionModel());
        focusVisibleTracker.install();
    }

    /// Removes focus observation and virtual-flow callbacks before disposal.
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        M3TableView<T> control = materialTableView();
        control.focusedProperty().removeListener(focusEligibilityListener);
        control.disabledProperty().removeListener(focusEligibilityListener);
        control.focusModelProperty().removeListener(focusModelListener);
        control.selectionModelProperty().removeListener(selectionModelListener);
        updateObservedFocusModel(control.getFocusModel(), null);
        updateObservedSelectionModel(control.getSelectionModel(), null);
        focusVisibleTracker.uninstall();
        materialFlow().setLayoutCompletion(null);
        super.dispose();
    }

    /// Creates the virtual flow whose scrollbars use the shared Material scroll contract.
    ///
    /// @return a Material-styled table virtual flow
    @Override
    protected VirtualFlow<TableRow<T>> createVirtualFlow() {
        return new TableViewVirtualFlow<>();
    }

    /// Moves focused-index observation between inherited focus-model instances.
    ///
    /// @param oldModel the previously observed focus model, or `null`
    /// @param newModel the focus model to observe, or `null`
    private void updateObservedFocusModel(
            @Nullable TableViewFocusModel<T> oldModel,
            @Nullable TableViewFocusModel<T> newModel
    ) {
        if (oldModel != null) {
            oldModel.focusedIndexProperty().removeListener(focusedIndexListener);
        }
        if (newModel != null && !disposed) {
            newModel.focusedIndexProperty().addListener(focusedIndexListener);
        }
        refreshFocusedRowFeedback();
    }

    /// Moves cell-selection observation between inherited selection-model instances.
    ///
    /// @param oldModel the previously observed selection model, or `null`
    /// @param newModel the selection model to observe, or `null`
    private void updateObservedSelectionModel(
            @Nullable TableViewSelectionModel<T> oldModel,
            @Nullable TableViewSelectionModel<T> newModel
    ) {
        if (oldModel != null) {
            oldModel.cellSelectionEnabledProperty().removeListener(cellSelectionListener);
        }
        if (newModel != null && !disposed) {
            newModel.cellSelectionEnabledProperty().addListener(cellSelectionListener);
        }
        refreshFocusedRowFeedback();
    }

    /// Delegates composite keyboard focus to the currently focused materialized row.
    private void refreshFocusedRowFeedback() {
        if (disposed) {
            return;
        }

        M3TableView<T> tableView = materialTableView();
        @Nullable TableViewFocusModel<T> focusModel = tableView.getFocusModel();
        @Nullable TableViewSelectionModel<T> selectionModel = tableView.getSelectionModel();
        int focusedIndex = focusModel == null ? -1 : focusModel.getFocusedIndex();
        boolean focusVisible = tableView.isFocused()
                && !tableView.isDisabled()
                && (selectionModel == null || !selectionModel.isCellSelectionEnabled())
                && tableView.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS);
        for (TableRow<T> row : materialFlow().materializedRows()) {
            if (row instanceof M3TableRow<?> && row.getSkin() instanceof M3TableRowSkin<?> materialSkin) {
                boolean rowFocusVisible = focusVisible && !row.isEmpty() && row.getIndex() == focusedIndex;
                row.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, rowFocusVisible);
                materialSkin.setLogicalFocusVisible(rowFocusVisible);
            }
        }
    }

    /// Returns the Material subtype supplied to this skin's constructor.
    ///
    /// @return the skinned Material table view
    @SuppressWarnings("unchecked")
    private M3TableView<T> materialTableView() {
        return (M3TableView<T>) getSkinnable();
    }

    /// Returns the virtual flow created by this skin as its Material subtype.
    ///
    /// @return the Material table virtual flow
    @SuppressWarnings("unchecked")
    private TableViewVirtualFlow<T> materialFlow() {
        return (TableViewVirtualFlow<T>) getVirtualFlow();
    }

    /// A virtual flow that exposes its protected scrollbars and active rows to the enclosing skin.
    @NotNullByDefault
    private static final class TableViewVirtualFlow<T> extends VirtualFlow<TableRow<T>> {
        /// The callback invoked after active rows finish a virtual-flow layout, or `null`.
        private @Nullable Runnable layoutCompletion;

        /// Creates a flow and applies the shared standalone scrollbar style.
        private TableViewVirtualFlow() {
            M3ScrollPane.style(getHbar());
            M3ScrollPane.style(getVbar());
        }

        /// Runs the inherited virtual layout and then reports stable row assignments to the enclosing skin.
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            @Nullable Runnable completion = layoutCompletion;
            if (completion != null) {
                completion.run();
            }
        }

        /// Replaces the callback invoked after virtual-flow layout.
        ///
        /// @param completion the callback, or `null` during disposal
        private void setLayoutCompletion(@Nullable Runnable completion) {
            layoutCompletion = completion;
        }

        /// Returns a stable copy of the rows currently maintained in the flow sheet.
        ///
        /// @return the active materialized rows
        private List<TableRow<T>> materializedRows() {
            return List.copyOf(getCells());
        }
    }
}
