// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 navigation rail.
@NotNullByDefault
public class M3NavigationRail extends VBox {
    /// The base style class for M3FX navigation rails.
    public static final String STYLE_CLASS = "m3-navigation-rail";

    /// The currently selected navigation item.
    private final ReadOnlyObjectWrapper<@Nullable M3NavigationItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected-state listeners installed on navigation items.
    private final Map<M3NavigationItem, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

    /// Updates navigation item selection listeners when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3NavigationItem item) {
                    uninstallItem(item);
                    if (selectedItem.get() == item) {
                        selectedItem.set(null);
                    }
                    item.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3NavigationItem item) {
                    installItem(item);
                    if (item.isSelected()) {
                        selectItem(item);
                    }
                }
            }
        }
        selectFirstItemIfNeeded();
    };

    /// Whether the navigation rail is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty navigation rail.
    public M3NavigationRail() {
        initialize();
    }

    /// Creates a navigation rail containing the supplied items.
    public M3NavigationRail(M3NavigationItem... items) {
        initialize();
        Objects.requireNonNull(items, "items");
        for (M3NavigationItem item : items) {
            Objects.requireNonNull(item, "item");
        }
        getItems().addAll(items);
    }

    /// Returns the mutable child list used as navigation rail items.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Returns the selected navigation item.
    public final @Nullable M3NavigationItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the selected navigation item property.
    public final ReadOnlyObjectProperty<@Nullable M3NavigationItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Selects a navigation item that belongs to this rail.
    public final void select(M3NavigationItem item) {
        Objects.requireNonNull(item, "item");
        if (!getChildren().contains(item)) {
            throw new IllegalArgumentException("item must belong to this navigation rail");
        }
        selectItem(item);
    }

    /// Selects the first navigation item when one exists.
    public final void selectFirst() {
        M3NavigationItem firstItem = firstNavigationItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Returns the user-agent stylesheet for M3FX navigation rails.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-rail.css");
    }

    /// Adds base style classes and installs selection listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAlignment(Pos.TOP_CENTER);
        setSpacing(8.0);
        getChildren().addListener(childrenListener);
    }

    /// Installs a selected-state listener on a navigation item.
    private void installItem(M3NavigationItem item) {
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleItemSelectedChanged(item, newValue);
        selectedListeners.put(item, listener);
        item.selectedProperty().addListener(listener);
    }

    /// Removes the selected-state listener from a navigation item.
    private void uninstallItem(M3NavigationItem item) {
        ChangeListener<Boolean> listener = selectedListeners.remove(item);
        if (listener != null) {
            item.selectedProperty().removeListener(listener);
        }
    }

    /// Keeps externally changed item selected states mutually exclusive.
    private void handleItemSelectedChanged(M3NavigationItem item, boolean selected) {
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

    /// Selects the first navigation item when selection is empty.
    private void selectFirstItemIfNeeded() {
        M3NavigationItem firstItem = firstNavigationItem();
        if (selectedItem.get() != null || firstItem == null) {
            return;
        }

        selectItem(firstItem);
    }

    /// Selects an item and clears selection from the remaining navigation items.
    private void selectItem(@Nullable M3NavigationItem item) {
        updatingSelection = true;
        try {
            for (Node child : getChildren()) {
                if (child instanceof M3NavigationItem navigationItem) {
                    navigationItem.setSelected(navigationItem == item);
                }
            }
            selectedItem.set(item);
        } finally {
            updatingSelection = false;
        }
    }

    /// Returns the first navigation item child.
    private @Nullable M3NavigationItem firstNavigationItem() {
        for (Node child : getChildren()) {
            if (child instanceof M3NavigationItem item) {
                return item;
            }
        }
        return null;
    }
}
