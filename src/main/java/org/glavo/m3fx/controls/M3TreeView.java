// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleRole;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TreeViewSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents a virtualized, expandable hierarchy using Material Design 3 list-row conventions.
///
/// Material Design 3 does not define a tree-view component. This extension therefore derives its visual contract
/// from Material lists: one-line rows use the Material list height, selection uses Material color roles, and hover,
/// focus, disabled, and pointer feedback use the same state vocabulary as other M3FX collection controls. Adobe
/// Spectrum 2 informs only tree-specific capabilities such as expandable hierarchy, optional item graphics,
/// highlight or checkbox selection presentation, and full-text help for truncated labels.
///
/// The inherited [TreeView] model remains authoritative. [TreeItem] owns hierarchy and expansion state, while the
/// inherited selection, focus, editing, scrolling, accessibility, and keyboard contracts remain available. A new
/// tree view uses [highlight selection][M3TreeViewSelectionStyle#HIGHLIGHT] and creates [M3TreeCell] instances.
/// Applications may replace the cell factory for richer rows. An explicit inherited `fixedCellSize` value or an
/// author stylesheet may override the default Material one-line row height.
///
/// The tree view is one scene traversal stop. Its virtualized cells and checkbox-selection indicators do not enter
/// the scene Tab sequence independently. After keyboard traversal enters the tree, arrow keys move the focused row;
/// in checkbox-selection presentation, an unmodified Space press toggles only that focused row.
///
/// See [Spectrum 2 TreeView](https://react-spectrum.adobe.com/TreeView).
///
/// @param <T> the value type stored by tree items
@NotNullByDefault
public final class M3TreeView<T> extends TreeView<T> {
    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-tree-view";

    /// The Material one-line row height used before CSS resolves an explicit fixed cell size.
    private static final double DEFAULT_ROW_HEIGHT = 56.0;

    /// The way selected tree items are presented.
    ///
    /// A direct `null` assignment restores [M3TreeViewSelectionStyle#HIGHLIGHT]. Bound values must be non-null.
    ///
    /// @defaultValue [M3TreeViewSelectionStyle#HIGHLIGHT]
    private final ObjectProperty<M3TreeViewSelectionStyle> selectionStyle =
            new SimpleObjectProperty<>(this, "selectionStyle", M3TreeViewSelectionStyle.HIGHLIGHT) {
                /// Restores the default or updates selection styling after assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TreeViewSelectionStyle.HIGHLIGHT);
                        return;
                    }
                    updateSelectionStyle();
                }
            };

    /// Creates an empty tree view with no root item.
    public M3TreeView() {
        initialize();
    }

    /// Creates a tree view with the specified root item.
    ///
    /// A `null` root represents an empty tree and may later be replaced through the inherited root property.
    ///
    /// @param root the root item, or `null` for an empty tree
    public M3TreeView(@Nullable TreeItem<T> root) {
        super(root);
        initialize();
    }

    /// Returns the way selected tree items are presented.
    ///
    /// @return the non-null selection style
    public M3TreeViewSelectionStyle getSelectionStyle() {
        return selectionStyle.get();
    }

    /// Sets the way selected tree items are presented.
    ///
    /// Checkbox presentation is most useful with the inherited selection model configured for multiple selection.
    /// This method changes presentation only and does not change the selection mode or selected items.
    ///
    /// @param selectionStyle the selection style
    /// @throws NullPointerException if `selectionStyle` is `null`
    public void setSelectionStyle(M3TreeViewSelectionStyle selectionStyle) {
        this.selectionStyle.set(Objects.requireNonNull(selectionStyle, "selectionStyle"));
    }

    /// Returns the property containing the selection presentation.
    ///
    /// A direct `null` assignment restores [M3TreeViewSelectionStyle#HIGHLIGHT]. A unidirectional binding must
    /// supply non-null values. Changing the property does not change the inherited selection mode or selected items.
    ///
    /// @return the selection-style property
    public ObjectProperty<M3TreeViewSelectionStyle> selectionStyleProperty() {
        return selectionStyle;
    }

    /// Returns the user-agent stylesheet for Material tree views.
    ///
    /// @return the tree-view stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("tree-view.css");
    }

    /// Creates the default Material tree-view skin.
    ///
    /// @return a skin that preserves JavaFX tree behavior and styles its virtualized scrollbars
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TreeViewSkin<>(this);
    }

    /// Initializes styling, accessibility, and the default cell factory.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TREE_VIEW);
        setFocusTraversable(true);
        setCellFactory(treeView -> new M3TreeCell<>());
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                ensureTraversalFocus();
            }
        });
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleCheckboxSelectionKeyPressed);
        updateSelectionStyle();
    }

    /// Ensures keyboard traversal into the tree has one logical row focus without changing selection.
    private void ensureTraversalFocus() {
        @Nullable FocusModel<TreeItem<T>> focusModel = getFocusModel();
        int itemCount = getExpandedItemCount();
        if (focusModel == null || itemCount <= 0) {
            return;
        }

        int index = focusModel.getFocusedIndex();
        if (index >= 0 && index < itemCount) {
            return;
        }
        @Nullable MultipleSelectionModel<TreeItem<T>> selectionModel = getSelectionModel();
        int selectedIndex = selectionModel == null ? -1 : selectionModel.getSelectedIndex();
        focusModel.focus(selectedIndex >= 0 && selectedIndex < itemCount ? selectedIndex : 0);
    }

    /// Handles focus-only navigation and focused-row toggling for checkbox selection.
    ///
    /// @param event the key-pressed event dispatched through this tree
    private void handleCheckboxSelectionKeyPressed(KeyEvent event) {
        if (getSelectionStyle() != M3TreeViewSelectionStyle.CHECKBOX
                || !isFocused()
                || isDisabled()
                || M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }

        ensureTraversalFocus();
        @Nullable FocusModel<TreeItem<T>> focusModel = getFocusModel();
        if (focusModel == null) {
            return;
        }

        int index = focusModel.getFocusedIndex();
        if (index < 0 || index >= getExpandedItemCount()) {
            return;
        }
        switch (event.getCode()) {
            case SPACE -> toggleFocusedCheckboxSelection(index, event);
            case UP -> moveCheckboxFocus(index - 1, event);
            case DOWN -> moveCheckboxFocus(index + 1, event);
            case HOME -> moveCheckboxFocus(0, event);
            case END -> moveCheckboxFocus(getExpandedItemCount() - 1, event);
            case PAGE_UP -> moveCheckboxFocus(index - checkboxPageSize(), event);
            case PAGE_DOWN -> moveCheckboxFocus(index + checkboxPageSize(), event);
            case LEFT, RIGHT -> handleCheckboxHierarchyKey(index, event);
            default -> {
            }
        }
    }

    /// Toggles one valid focused row through the inherited selection model.
    ///
    /// @param index the focused visible row index
    /// @param event the Space key event to consume after activation
    private void toggleFocusedCheckboxSelection(int index, KeyEvent event) {
        @Nullable MultipleSelectionModel<TreeItem<T>> selectionModel = getSelectionModel();
        if (selectionModel == null) {
            return;
        }
        if (selectionModel.isSelected(index)) {
            selectionModel.clearSelection(index);
        } else if (selectionModel.getSelectionMode() == SelectionMode.SINGLE) {
            selectionModel.clearAndSelect(index);
        } else {
            selectionModel.select(index);
        }
        event.consume();
    }

    /// Moves logical row focus without changing checkbox selection.
    ///
    /// @param requestedIndex the requested visible row index
    /// @param event the navigation event to consume
    private void moveCheckboxFocus(int requestedIndex, KeyEvent event) {
        int itemCount = getExpandedItemCount();
        @Nullable FocusModel<TreeItem<T>> focusModel = getFocusModel();
        if (focusModel == null || itemCount <= 0) {
            return;
        }

        int index = Math.max(0, Math.min(requestedIndex, itemCount - 1));
        focusModel.focus(index);
        scrollTo(index);
        event.consume();
    }

    /// Expands, collapses, or moves to the adjacent hierarchy level without changing checkbox selection.
    ///
    /// @param index the focused visible row index
    /// @param event the horizontal navigation event
    private void handleCheckboxHierarchyKey(int index, KeyEvent event) {
        @Nullable TreeItem<T> item = getTreeItem(index);
        if (item == null) {
            return;
        }

        boolean expands = event.getCode() == KeyCode.RIGHT;
        if (getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            expands = !expands;
        }
        if (expands) {
            if (!item.isLeaf() && !item.isExpanded()) {
                item.setExpanded(true);
            } else if (item.isExpanded() && !item.getChildren().isEmpty()) {
                moveCheckboxFocus(index + 1, event);
                return;
            }
        } else if (!item.isLeaf() && item.isExpanded()) {
            item.setExpanded(false);
        } else {
            @Nullable TreeItem<T> parent = item.getParent();
            int parentIndex = parent == null ? -1 : getRow(parent);
            if (parentIndex >= 0) {
                moveCheckboxFocus(parentIndex, event);
                return;
            }
        }
        event.consume();
    }

    /// Returns the approximate number of fully visible rows traversed by a Page Up or Page Down key.
    ///
    /// @return at least one row
    private int checkboxPageSize() {
        double rowHeight = getFixedCellSize();
        if (rowHeight <= 0.0) {
            rowHeight = DEFAULT_ROW_HEIGHT;
        }
        return Math.max(1, (int) Math.floor(getHeight() / rowHeight) - 1);
    }

    /// Applies the style class for the current selection presentation.
    private void updateSelectionStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSelectionStyle().styleClass(),
                M3TreeViewSelectionStyle.HIGHLIGHT.styleClass(),
                M3TreeViewSelectionStyle.CHECKBOX.styleClass()
        );
        requestLayout();
    }
}
