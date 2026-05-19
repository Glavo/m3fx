// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.IndexedCell;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A reusable virtualized cell used by [M3ListView].
@NotNullByDefault
public class M3ListViewCell<T> extends IndexedCell<T> {
    /// The base style class for M3FX list view cells.
    public static final String STYLE_CLASS = "m3-list-view-cell";

    /// The owning virtualized list view.
    private final M3ListView<T> listView;

    /// The rendered list item currently owned by this cell.
    private @Nullable M3ListItem listItem;

    /// Routes list item actions back into the list view selection policy.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// Creates a reusable list view cell.
    public M3ListViewCell(M3ListView<T> listView) {
        this.listView = Objects.requireNonNull(listView, "listView");
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_ITEM);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setText(null);
    }

    /// Returns the owning virtualized list view.
    public final M3ListView<T> getListView() {
        return listView;
    }

    /// Returns the rendered list item currently owned by this cell.
    public final @Nullable M3ListItem getListItem() {
        return listItem;
    }

    /// Updates this cell's index and selected state.
    @Override
    public void updateIndex(int index) {
        super.updateIndex(index);
        if (index < 0 || index >= getListView().getItems().size()) {
            updateItem(null, true);
        } else {
            updateItem(getListView().getItems().get(index), false);
        }
        refreshSelection();
    }

    /// Updates the rendered list item for the current virtualized item.
    @Override
    protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setListItem(null);
            setGraphic(null);
            setText(null);
            return;
        }

        M3ListItem itemNode = createListItem(item);
        setListItem(itemNode);
        setGraphic(itemNode);
        setText(null);
        refreshListItemWidth();
        refreshSelection();
    }

    /// Lays out the cell and keeps the rendered list item width in sync.
    @Override
    protected void layoutChildren() {
        refreshListItemWidth();
        super.layoutChildren();
    }

    /// Returns accessibility attributes for this virtualized cell.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> getListView().isIndexSelected(getIndex());
            case INDEX -> getIndex();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Creates or delegates creation of the visual list item for one data item.
    private M3ListItem createListItem(@Nullable T item) {
        @Nullable Callback<? super T, ? extends M3ListItem> factory = getListView().getCellFactory();
        M3ListItem itemNode = factory == null
                ? new M3ListItem(String.valueOf(item))
                : Objects.requireNonNull(factory.call(item), "cellFactory result");
        itemNode.setFocusTraversable(false);
        itemNode.setMinWidth(0.0);
        itemNode.setMaxWidth(Double.MAX_VALUE);
        return itemNode;
    }

    /// Replaces the rendered list item and updates event handlers.
    private void setListItem(@Nullable M3ListItem listItem) {
        if (this.listItem != null) {
            this.listItem.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        }
        this.listItem = listItem;
        if (listItem != null) {
            listItem.addEventHandler(ActionEvent.ACTION, itemActionHandler);
        }
    }

    /// Updates the list item's selected state from the owning view.
    public final void refreshSelection() {
        if (listItem != null) {
            listItem.setSelected(!isEmpty() && getListView().isIndexSelected(getIndex()));
        }
    }

    /// Stretches the rendered list item to the current cell width.
    private void refreshListItemWidth() {
        if (listItem != null) {
            double width = getWidth();
            if (width > 0.0) {
                listItem.setPrefWidth(width);
            }
        }
    }

    /// Applies the list view selection policy when the rendered item fires.
    private void handleItemAction(ActionEvent event) {
        getListView().activateIndex(getIndex());
        event.consume();
    }
}
