// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.text.Text;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Renders a reusable text-and-graphic row for an [M3TreeView].
///
/// The default representation uses [String#valueOf(Object)] for each non-null value and displays the graphic owned
/// by its current [TreeItem]. Text that does not fit is ellipsized; while truncation is active, the complete text is
/// exposed through a retained [M3Tooltip] and the cell's accessible help text. A tree view may reuse one cell for
/// unrelated items while scrolling.
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

    /// The complete text assigned during the latest non-empty item update.
    private @Nullable String fullText;

    /// Creates an empty reusable Material tree cell.
    public M3TreeCell() {
        M3ControlStyles.add(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TREE_ITEM);
        setContentDisplay(ContentDisplay.LEFT);
        setTextOverrun(OverrunStyle.ELLIPSIS);
        setPrefWidth(0.0);
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
            setGraphic(null);
            fullTextTooltip.setText(null);
            return;
        }

        fullText = item == null ? "" : String.valueOf(item);
        setText(fullText);
        @Nullable TreeItem<T> treeItem = getTreeItem();
        setGraphic(treeItem == null ? null : treeItem.getGraphic());
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
}
