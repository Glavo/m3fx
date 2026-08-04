// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.skins.M3TreeCellSkin;
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

    /// The style class used when a checkbox is the row's only leading graphic.
    private static final String CHECKBOX_ONLY_STYLE_CLASS = "m3-tree-cell-checkbox-only";

    /// The horizontal overlap that removes the disclosure and checkbox touch-target insets from visual spacing.
    private static final double CHECKBOX_DISCLOSURE_OVERLAP = 25.0;

    /// The retained tooltip used only while the label is visually truncated.
    private final M3Tooltip fullTextTooltip = new M3Tooltip();

    /// The animated Material indicator that mirrors the current tree item's expanded state.
    private final M3DisclosureIcon disclosureIcon = new M3DisclosureIcon();

    /// The fixed-width leading slot that aligns disclosure indicators across hierarchy levels.
    private final StackPane disclosureNode = new StackPane(disclosureIcon);

    /// The retained checkbox used by checkbox-selection trees.
    private final M3CheckBox selectionCheckBox = new M3CheckBox();

    /// The leading content that combines checkbox selection with an optional item graphic.
    private final CheckboxLeadingBox checkboxGraphic = new CheckboxLeadingBox();

    /// The complete text assigned during the latest non-empty item update.
    private @Nullable String fullText;

    /// Whether the retained full-text tooltip is currently installed on this reusable cell.
    private boolean fullTextTooltipInstalled;

    /// Rebinds disclosure state when virtualization assigns a different tree item to this cell.
    private final ChangeListener<@Nullable TreeItem<T>> treeItemListener =
            (observable, oldItem, newItem) -> updateTreeItem(oldItem, newItem);

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
        disclosureNode.getStyleClass().add("m3-tree-disclosure");
        disclosureNode.setAlignment(Pos.CENTER);
        disclosureIcon.setMouseTransparent(true);
        setDisclosureNode(disclosureNode);
        checkboxGraphic.getStyleClass().add("m3-tree-cell-leading");
        selectionCheckBox.getStyleClass().add("m3-tree-selection-checkbox");
        selectionCheckBox.setFocusTraversable(false);
        selectionCheckBox.setOnAction(this::handleSelectionCheckBoxAction);
        selectedProperty().addListener((observable, oldValue, newValue) -> refreshSelectionIndicator());
        treeViewProperty().addListener((observable, oldValue, newValue) -> updateTreeView(oldValue, newValue));
        treeItemProperty().addListener(treeItemListener);
        updateTreeItem(null, getTreeItem());
    }

    /// Returns the retained tooltip that exposes truncated text.
    ///
    /// The tooltip is installed on this cell only while the current label is truncated, and its text is `null`
    /// otherwise. Applications may configure timing and placement but should not replace its text, which is
    /// maintained during cell layout and reuse.
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

    /// Creates the Material tree-cell skin with bounded state-layer and ripple feedback.
    ///
    /// @return a new Material tree-cell skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TreeCellSkin<>(this);
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
            setFullTextTooltipText(null);
            return;
        }

        fullText = item == null ? "" : String.valueOf(item);
        setText(fullText);
        refreshPresentation();
        setFullTextTooltipText(null);
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
            setFullTextTooltipText(null);
            return;
        }

        boolean truncated = false;
        for (Node node : lookupAll(".text")) {
            if (!(node instanceof Text renderedText)) {
                continue;
            }
            String renderedValue = renderedText.getText();
            if (completeText.equals(renderedValue)) {
                setFullTextTooltipText(null);
                return;
            }
            truncated |= isEllipsizedRendering(completeText, renderedValue);
        }
        setFullTextTooltipText(truncated ? completeText : null);
    }

    /// Installs or removes full-text help as truncation begins or ends.
    ///
    /// @param text the complete truncated text, or `null` when no tooltip is needed
    private void setFullTextTooltipText(@Nullable String text) {
        fullTextTooltip.setText(text);
        if (text != null) {
            if (!fullTextTooltipInstalled) {
                M3Tooltip.install(this, fullTextTooltip);
                fullTextTooltipInstalled = true;
            }
        } else if (fullTextTooltipInstalled) {
            M3Tooltip.uninstall(this, fullTextTooltip);
            fullTextTooltipInstalled = false;
        }
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

    /// Binds the retained Material disclosure indicator to the assigned tree item.
    ///
    /// @param oldItem the previously assigned tree item, or `null`
    /// @param newItem the newly assigned tree item, or `null`
    private void updateTreeItem(@Nullable TreeItem<T> oldItem, @Nullable TreeItem<T> newItem) {
        if (oldItem != null && disclosureIcon.expandedProperty().isBound()) {
            disclosureIcon.expandedProperty().unbind();
        }
        if (newItem == null) {
            disclosureIcon.setExpanded(false);
        } else {
            disclosureIcon.expandedProperty().bind(newItem.expandedProperty());
        }
        refreshPresentation();
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
        setCheckboxOnlyStyle(false);
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
            } else {
                setCheckboxOnlyStyle(true);
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
        setCheckboxOnlyStyle(false);
        selectionCheckBox.setSelected(false);
        setGraphic(null);
    }

    /// Applies the spacing variant used when no item graphic follows the selection checkbox.
    ///
    /// @param active whether the checkbox is the only leading graphic
    private void setCheckboxOnlyStyle(boolean active) {
        if (active) {
            if (!getStyleClass().contains(CHECKBOX_ONLY_STYLE_CLASS)) {
                getStyleClass().add(CHECKBOX_ONLY_STYLE_CLASS);
            }
        } else {
            getStyleClass().remove(CHECKBOX_ONLY_STYLE_CLASS);
        }
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

    /// Reports compact leading width while allowing the checkbox touch target to overlap the disclosure slot.
    @NotNullByDefault
    private static final class CheckboxLeadingBox extends HBox {
        /// Creates an empty compact leading-content container.
        private CheckboxLeadingBox() {
        }

        /// Returns the minimum width after removing redundant disclosure and checkbox target insets.
        ///
        /// @param height the available height
        /// @return the compact minimum width
        @Override
        protected double computeMinWidth(double height) {
            return Math.max(0.0, super.computeMinWidth(height) - CHECKBOX_DISCLOSURE_OVERLAP);
        }

        /// Returns the preferred width after removing redundant disclosure and checkbox target insets.
        ///
        /// @param height the available height
        /// @return the compact preferred width
        @Override
        protected double computePrefWidth(double height) {
            return Math.max(0.0, super.computePrefWidth(height) - CHECKBOX_DISCLOSURE_OVERLAP);
        }

        /// Lays out leading controls and shifts their visuals into the retained disclosure target.
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            setTranslateX(-CHECKBOX_DISCLOSURE_OVERLAP);
        }
    }
}
