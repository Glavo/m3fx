// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 navigation drawer.
@NotNullByDefault
public class M3NavigationDrawer extends VBox {
    /// The base style class for M3FX navigation drawers.
    public static final String STYLE_CLASS = "m3-navigation-drawer";

    /// The currently selected navigation drawer item.
    private final ReadOnlyObjectWrapper<@Nullable M3ListItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected-state listeners installed on drawer list items.
    private final Map<M3ListItem, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

    /// Handles item actions by selecting the fired item.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// Updates installed item listeners when drawer content changes.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3ListItem item) {
                    uninstallItem(item);
                    if (selectedItem.get() == item) {
                        selectedItem.set(null);
                    }
                    item.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3ListItem item) {
                    installItem(item);
                    if (item.isSelected()) {
                        selectItem(item);
                    }
                }
            }
        }
        selectFirstItemIfNeeded();
    };

    /// Whether the drawer is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty navigation drawer.
    public M3NavigationDrawer() {
        initialize();
    }

    /// Creates a navigation drawer containing the supplied nodes.
    public M3NavigationDrawer(Node... items) {
        initialize();
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
        getItems().addAll(items);
    }

    /// Returns the mutable child list used as drawer content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Returns the selected drawer list item.
    public final @Nullable M3ListItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the selected drawer list item property.
    public final ReadOnlyObjectProperty<@Nullable M3ListItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Selects a drawer list item that belongs to this drawer.
    public final void select(M3ListItem item) {
        Objects.requireNonNull(item, "item");
        if (!getChildren().contains(item)) {
            throw new IllegalArgumentException("item must belong to this navigation drawer");
        }
        selectItem(item);
    }

    /// Selects the first drawer list item when one exists.
    public final void selectFirst() {
        M3ListItem firstItem = firstListItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Returns the user-agent stylesheet for M3FX navigation drawers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-drawer.css");
    }

    /// Lays out drawer content within the drawer padding.
    @Override
    protected void layoutChildren() {
        updateListItemWidths();
        super.layoutChildren();
    }

    /// Adds base style classes and installs content listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setSpacing(4.0);
        getChildren().addListener(childrenListener);
    }

    /// Keeps drawer list item containers inside the drawer content area.
    private void updateListItemWidths() {
        double width = getWidth();
        if (width <= 0.0) {
            return;
        }

        double itemWidth = Math.max(0.0, width - snappedLeftInset() - snappedRightInset());
        for (Node child : getChildren()) {
            if (child instanceof M3ListItem item) {
                if (Double.compare(item.getMinWidth(), 0.0) != 0) {
                    item.setMinWidth(0.0);
                }
                if (Double.compare(item.getMaxWidth(), itemWidth) != 0) {
                    item.setMaxWidth(itemWidth);
                }
            }
        }
    }

    /// Installs action and selected-state listeners on a drawer item.
    private void installItem(M3ListItem item) {
        item.addEventHandler(ActionEvent.ACTION, itemActionHandler);
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleItemSelectedChanged(item, newValue);
        selectedListeners.put(item, listener);
        item.selectedProperty().addListener(listener);
    }

    /// Removes action and selected-state listeners from a drawer item.
    private void uninstallItem(M3ListItem item) {
        item.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        ChangeListener<Boolean> listener = selectedListeners.remove(item);
        if (listener != null) {
            item.selectedProperty().removeListener(listener);
        }
    }

    /// Selects the drawer item that fired an action event.
    private void handleItemAction(ActionEvent event) {
        if (event.getSource() instanceof M3ListItem item && getChildren().contains(item) && !item.isDisabled()) {
            selectItem(item);
        }
    }

    /// Keeps externally changed item selected states mutually exclusive.
    private void handleItemSelectedChanged(M3ListItem item, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (selected) {
            selectItem(item);
        } else if (selectedItem.get() == item) {
            selectedItem.set(null);
            selectFirstItemIfNeeded();
        }
    }

    /// Selects an item and clears selection from the remaining drawer items.
    private void selectItem(@Nullable M3ListItem item) {
        updatingSelection = true;
        try {
            for (Node child : getChildren()) {
                if (child instanceof M3ListItem listItem) {
                    listItem.setSelected(listItem == item);
                }
            }
            selectedItem.set(item);
        } finally {
            updatingSelection = false;
        }
    }

    /// Selects the first drawer list item when selection is empty.
    private void selectFirstItemIfNeeded() {
        if (selectedItem.get() != null) {
            return;
        }

        M3ListItem firstItem = firstListItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Returns the first drawer list item child.
    private @Nullable M3ListItem firstListItem() {
        for (Node child : getChildren()) {
            if (child instanceof M3ListItem item) {
                return item;
            }
        }
        return null;
    }
}
