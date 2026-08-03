// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Renders a reusable Material one-line row for an [M3TreeView].
///
/// The default representation uses [String#valueOf(Object)] for each non-null value and displays the graphic owned
/// by its current [TreeItem]. When the owning tree uses [checkbox selection][M3TreeViewSelectionStyle#CHECKBOX], a
/// leading [M3CheckBox] mirrors this cell's selection and can toggle the inherited selection model. Text that does
/// not fit is ellipsized; while truncation is active, the complete text is exposed through a retained [M3Tooltip]
/// and the cell's accessible help text. A tree view may reuse one cell for unrelated items while scrolling.
///
/// Applications may subclass this type and override [#updateItem(Object, boolean)] or replace the owning tree view's
/// cell factory. An override must fully replace all item-dependent state because the cell is virtualized.
///
/// @param <T> the tree-item value type
@NotNullByDefault
public class M3TreeCell<T> extends TreeCell<T> {
    /// The default cell style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-tree-cell";

    /// The retained tooltip used only while the label is visually truncated.
    private final M3Tooltip fullTextTooltip = new M3Tooltip();

    /// The retained checkbox used by checkbox-selection trees.
    private final M3CheckBox selectionCheckBox = new M3CheckBox();

    /// The leading content that combines checkbox selection with an optional item graphic.
    private final HBox checkboxGraphic = new HBox();

    /// The complete text assigned during the latest non-empty item update.
    private @Nullable String fullText;

    /// Refreshes the cell when the owning Material tree changes its selection presentation.
    private final ChangeListener<M3TreeViewSelectionStyle> selectionStyleListener =
            (observable, oldValue, newValue) -> refreshPresentation();

    /// Avoids retaining discarded virtualized cells after an owning tree replaces its skin.
    private final WeakChangeListener<M3TreeViewSelectionStyle> weakSelectionStyleListener =
            new WeakChangeListener<>(selectionStyleListener);

    /// Creates an empty reusable Material tree cell.
    public M3TreeCell() {
        M3ControlStyles.add(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TREE_ITEM);
        setContentDisplay(ContentDisplay.LEFT);
        setTextOverrun(OverrunStyle.ELLIPSIS);
        setPrefWidth(0.0);
        checkboxGraphic.getStyleClass().add("m3-tree-cell-leading");
        selectionCheckBox.getStyleClass().add("m3-tree-selection-checkbox");
        selectionCheckBox.setFocusTraversable(false);
        selectionCheckBox.setOnAction(this::handleSelectionCheckBoxAction);
        selectedProperty().addListener((observable, oldValue, newValue) -> refreshSelectionIndicator());
        treeViewProperty().addListener((observable, oldValue, newValue) -> updateTreeView(oldValue, newValue));
        M3Tooltip.install(this, fullTextTooltip);
    }

    /// Returns the retained tooltip that exposes truncated text.
    ///
    /// Its text is `null` when the current label fits. Applications may configure timing and placement but should not
    /// replace its text, which is maintained during cell layout and reuse.
    ///
    /// @return the retained full-text tooltip
    public M3Tooltip getFullTextTooltip() {
        return fullTextTooltip;
    }

    /// Returns the retained Material checkbox used by checkbox-selection trees.
    ///
    /// The checkbox is not part of the cell graphic while the owning tree uses highlight selection. Its selected
    /// state is controlled by the tree selection model.
    ///
    /// @return the retained selection checkbox
    public M3CheckBox getSelectionCheckBox() {
        return selectionCheckBox;
    }

    /// Updates this cell for a virtualized tree item.
    ///
    /// Empty cells clear their text, graphic, and tooltip. A non-empty item whose value is `null` displays an empty
    /// string. The graphic is read from the current [TreeItem] on every update.
    ///
    /// @param item  the item value, or `null`
    /// @param empty whether this cell is empty
    @Override
    protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            fullText = null;
            setText(null);
            clearGraphic();
            fullTextTooltip.setText(null);
            return;
        }

        fullText = item == null ? "" : String.valueOf(item);
        setText(fullText);
        refreshPresentation();
        fullTextTooltip.setText(null);
    }

    /// Lays out the cell and updates full-text help from the text rendered by the JavaFX labeled skin.
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        updateFullTextTooltip();
    }

    /// Enables the retained tooltip only when the rendered label differs from its complete text.
    private void updateFullTextTooltip() {
        @Nullable String completeText = fullText;
        if (completeText == null || completeText.isEmpty()) {
            fullTextTooltip.setText(null);
            return;
        }

        boolean truncated = false;
        for (Node node : lookupAll(".text")) {
            if (!(node instanceof Text renderedText)) {
                continue;
            }
            String renderedValue = renderedText.getText();
            if (completeText.equals(renderedValue)) {
                fullTextTooltip.setText(null);
                return;
            }
            truncated |= isEllipsizedRendering(completeText, renderedValue);
        }
        fullTextTooltip.setText(truncated ? completeText : null);
    }

    /// Returns whether a rendered value is an ellipsized prefix of the complete label.
    private static boolean isEllipsizedRendering(String completeText, String renderedText) {
        int suffixLength;
        if (renderedText.endsWith("\u2026")) {
            suffixLength = 1;
        } else if (renderedText.endsWith("...")) {
            suffixLength = 3;
        } else {
            return false;
        }
        String prefix = renderedText.substring(0, renderedText.length() - suffixLength);
        return !prefix.isEmpty() && completeText.startsWith(prefix);
    }

    /// Rebinds the selection-style listener after this virtualized cell moves between tree views.
    private void updateTreeView(@Nullable TreeView<T> oldTreeView, @Nullable TreeView<T> newTreeView) {
        if (oldTreeView instanceof M3TreeView<?> oldMaterialTreeView) {
            oldMaterialTreeView.selectionStyleProperty().removeListener(weakSelectionStyleListener);
        }
        if (newTreeView instanceof M3TreeView<?> newMaterialTreeView) {
            newMaterialTreeView.selectionStyleProperty().addListener(weakSelectionStyleListener);
        }
        refreshPresentation();
    }

    /// Rebuilds the leading content for the current tree item and selection style.
    private void refreshPresentation() {
        checkboxGraphic.getChildren().clear();
        if (isEmpty()) {
            setGraphic(null);
            return;
        }

        @Nullable TreeItem<T> treeItem = getTreeItem();
        @Nullable Node itemGraphic = treeItem == null ? null : treeItem.getGraphic();
        if (usesCheckboxSelection()) {
            checkboxGraphic.getChildren().add(selectionCheckBox);
            if (itemGraphic != null) {
                checkboxGraphic.getChildren().add(itemGraphic);
            }
            setGraphic(checkboxGraphic);
        } else {
            setGraphic(itemGraphic);
        }
        refreshSelectionIndicator();
    }

    /// Clears item-dependent graphic state when this cell becomes empty.
    private void clearGraphic() {
        checkboxGraphic.getChildren().clear();
        selectionCheckBox.setSelected(false);
        setGraphic(null);
    }

    /// Returns whether the owning Material tree uses checkbox selection presentation.
    private boolean usesCheckboxSelection() {
        return getTreeView() instanceof M3TreeView<?> materialTreeView
                && materialTreeView.getSelectionStyle() == M3TreeViewSelectionStyle.CHECKBOX;
    }

    /// Mirrors this cell's selected state into its retained checkbox.
    private void refreshSelectionIndicator() {
        selectionCheckBox.setSelected(!isEmpty() && isSelected());
    }

    /// Applies a checkbox activation to the owning tree's inherited selection model.
    private void handleSelectionCheckBoxAction(ActionEvent event) {
        @Nullable TreeView<T> treeView = getTreeView();
        int index = getIndex();
        if (treeView == null || index < 0 || index >= treeView.getExpandedItemCount()) {
            event.consume();
            return;
        }

        @Nullable MultipleSelectionModel<TreeItem<T>> selectionModel = treeView.getSelectionModel();
        if (selectionModel == null) {
            event.consume();
            return;
        }
        if (selectionCheckBox.isSelected()) {
            if (selectionModel.getSelectionMode() == javafx.scene.control.SelectionMode.SINGLE) {
                selectionModel.clearAndSelect(index);
            } else {
                selectionModel.select(index);
            }
        } else {
            selectionModel.clearSelection(index);
        }
        @Nullable FocusModel<TreeItem<T>> focusModel = treeView.getFocusModel();
        if (focusModel != null) {
            focusModel.focus(index);
        }
        treeView.requestFocus();
        event.consume();
    }
}
