// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.skins.M3ListViewCellSkin;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A reusable virtualized cell used by [M3ListView].
///
/// A list view creates cells through its cell factory and reuses each cell for different data items while scrolling.
/// Subclasses customize rows by overriding [createListItem()] and [updateListItem(M3ListItem, Object)] rather than
/// creating a new scene-graph node for every item update.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
///
/// @param <T> the item type rendered by this cell
@NotNullByDefault
public class M3ListCell<T> extends IndexedCell<T> {
    /// The base style class for M3FX list view cells.
    public static final String STYLE_CLASS = "m3-list-view-cell";


    /// The pseudo-class used when this virtualized row owns logical keyboard focus.
    private static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// The owning virtualized list view.
    private final M3ListView<T> listView;

    /// Routes list item actions back into the list view selection policy.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;


    /// The rendered list item currently owned by this cell.
    private @Nullable M3ListItem listItem;

    /// Creates a reusable list view cell.
    ///
    /// @param listView the owning virtualized list view
    /// @throws NullPointerException if any required argument is `null`
    public M3ListCell(M3ListView<T> listView) {
        this.listView = Objects.requireNonNull(listView, "listView");
        if (!getStyleClass().contains(STYLE_CLASS)) {
            getStyleClass().add(STYLE_CLASS);
        }

        setAccessibleRole(AccessibleRole.LIST_ITEM);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setText(null);
    }

    /// Returns the owning virtualized list view.
    ///
    /// @return the owning virtualized list view
    public M3ListView<T> getListView() {
        return listView;
    }

    /// Returns the rendered list item currently owned by this cell.
    ///
    /// @return the reusable rendered list item, or `null` before this cell first renders a non-empty item
    public @Nullable M3ListItem getListItem() {
        return listItem;
    }

    /// Updates this cell's index and selected state.
    ///
    /// @param index the virtualized row index
    @Override
    public void updateIndex(int index) {
        super.updateIndex(index);
        if (index < 0 || index >= getListView().getItems().size()) {
            updateItem(null, true);
        } else {
            updateItem(getListView().getItems().get(index), false);
        }
        refreshFocus();
    }

    /// Updates the rendered list item for the current virtualized item.
    ///
    /// @param item the data item assigned to this cell, or `null` for an empty cell
    /// @param empty whether this cell is empty
    @Override
    @SuppressWarnings("DataFlowIssue")
    protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            clearContent();
            return;
        }

        T value = Objects.requireNonNull(item, "non-empty item");
        @Nullable M3ListItem itemNode = listItem;
        if (itemNode == null) {
            itemNode = Objects.requireNonNull(createListItem(), "createListItem result");
            installListItem(itemNode);
            copyThemeContext(itemNode);
        }
        updateListItem(itemNode, value);
        setGraphic(itemNode);
        setText(null);
        refreshSelection();
    }

    /// Returns accessibility attributes for this virtualized cell.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if any required argument is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> getListView().isIndexSelected(getIndex());
            case INDEX -> getIndex();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Creates the default skin that lays out this virtualized cell's rendered list item.
    ///
    /// @return the default virtualized cell skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ListViewCellSkin<>(this);
    }

    /// Creates the row node retained by this cell across virtualized item updates.
    ///
    /// Subclasses may override this method to create a configured [M3ListItem]. The method is called at most once for
    /// each cell instance unless the subclass explicitly replaces the row through [setGraphic(Node)].
    ///
    /// @return the reusable row node
    protected M3ListItem createListItem() {
        return new M3ListItem();
    }

    /// Updates the reusable row node for a non-empty data item.
    ///
    /// The default implementation displays [String#valueOf(Object)] as the headline. Implementations must update all
    /// item-dependent state because the same row is subsequently reused for unrelated items.
    ///
    /// @param listItem the reusable row node created by [createListItem()]
    /// @param item the current non-null data item
    protected void updateListItem(M3ListItem listItem, T item) {
        String headline = String.valueOf(item);
        if (!listItem.getHeadlineText().equals(headline)) {
            listItem.setHeadlineText(headline);
        }
    }

    /// Copies the current list view theme context into a virtualized row for early CSS passes.
    private void copyThemeContext(M3ListItem itemNode) {
        @Nullable Parent themeRoot = M3ThemeResolver.findThemeRoot(getListView());
        if (themeRoot != null) {
            M3ThemeRuntime.copyThemeContext(themeRoot, itemNode);
        } else {
            M3ThemeRuntime.clearThemeStyleClasses(itemNode);
            M3ThemeMetadata.clearTheme(itemNode);
        }
    }

    /// Installs the row retained for the lifetime of this cell.
    private void installListItem(M3ListItem listItem) {
        this.listItem = listItem;
        if (!listItem.focusTraversableProperty().isBound()) {
            listItem.setFocusTraversable(false);
        }
        listItem.addEventHandler(ActionEvent.ACTION, itemActionHandler);
    }

    /// Clears rendered content while the cell is empty or detached from the scene.
    private void clearContent() {
        setGraphic(null);
        setText(null);
    }

    /// Updates the list item's selected state from the owning view.
    public void refreshSelection() {
        boolean selected = !isEmpty() && getListView().isIndexSelected(getIndex());
        updateSelected(selected);
        if (listItem != null && !listItem.selectedProperty().isBound()) {
            listItem.setSelected(selected);
        }
    }

    /// Reapplies the current list view theme context to the rendered row item.
    public void refreshThemeContext() {
        if (listItem != null) {
            copyThemeContext(listItem);
            listItem.applyCss();
            listItem.layout();
        }
    }

    /// Requests list focus when this cell owns logical row focus.
    ///
    /// @return `true` when the owning list view accepted focus
    public boolean focusCell() {
        if (isEmpty() || !getListView().isIndexFocused(getIndex())) {
            return false;
        }

        getListView().requestFocus();
        refreshFocus();
        return getListView().isFocused();
    }

    /// Updates logical focus pseudo-class state for this virtualized row.
    public void refreshFocus() {
        boolean focusVisible = !isEmpty()
                && getListView().isIndexFocused(getIndex())
                && getListView().getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS);
        pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, focusVisible);
        if (listItem != null) {
            listItem.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, focusVisible);
        }
    }

    /// Applies the list view selection policy when the rendered item fires.
    private void handleItemAction(ActionEvent event) {
        int index = getIndex();
        if (index >= 0 && index < getListView().getItems().size()) {
            getListView().focusIndex(index);
            switch (getListView().getSelectionMode()) {
                case NONE -> {
                }
                case SINGLE -> getListView().selectIndex(index);
                case MULTIPLE -> getListView().setIndexSelected(index, !getListView().isIndexSelected(index));
            }
        }
        event.consume();
    }
}
