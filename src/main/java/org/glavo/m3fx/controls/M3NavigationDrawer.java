// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
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

    /// The selected drawer list items in child order.
    private final ObservableList<M3ListItem> selectedItems = FXCollections.observableArrayList();

    /// The read-only selected drawer list item view.
    private final @UnmodifiableView ObservableList<M3ListItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// Whether the drawer allows all list items to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection") {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

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
                    item.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3ListItem item) {
                    installItem(item);
                }
            }
        }
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
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
        addItems(items);
    }

    /// Returns the mutable child list used as drawer content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Adds one drawer content node.
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds drawer content nodes.
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all drawer content nodes.
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all drawer content nodes.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the selected drawer list items in child order.
    public final @UnmodifiableView ObservableList<M3ListItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the selected drawer list item.
    public final @Nullable M3ListItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the selected drawer list item property.
    public final ReadOnlyObjectProperty<@Nullable M3ListItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the child index of the selected drawer list item, or `-1` when no item is selected.
    public final int getSelectedIndex() {
        @Nullable M3ListItem item = getSelectedItem();
        return item == null ? -1 : getChildren().indexOf(item);
    }

    /// Returns whether this drawer allows all list items to be unselected.
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this drawer allows all list items to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a drawer list item that belongs to this drawer.
    public final void select(M3ListItem item) {
        Objects.requireNonNull(item, "item");
        if (!getChildren().contains(item)) {
            throw new IllegalArgumentException("item must belong to this navigation drawer");
        }
        selectItem(item);
    }

    /// Selects the drawer list item at the given child index.
    public final void selectIndex(int index) {
        Node child = getChildren().get(index);
        if (child instanceof M3ListItem item) {
            select(item);
            return;
        }
        throw new IllegalArgumentException("child at index is not an M3ListItem");
    }

    /// Selects the first drawer list item when one exists.
    public final void selectFirst() {
        M3ListItem firstItem = firstListItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Selects the last drawer list item when one exists.
    public final void selectLast() {
        @Nullable M3ListItem lastItem = M3SelectionNavigation.last(getChildren(), M3ListItem.class);
        if (lastItem != null) {
            selectItem(lastItem);
        }
    }

    /// Selects the next drawer list item after the current selected item, wrapping at the end.
    public final void selectNext() {
        @Nullable M3ListItem nextItem =
                M3SelectionNavigation.next(getChildren(), getSelectedItem(), M3ListItem.class);
        if (nextItem != null) {
            selectItem(nextItem);
        }
    }

    /// Selects the previous drawer list item before the current selected item, wrapping at the start.
    public final void selectPrevious() {
        @Nullable M3ListItem previousItem =
                M3SelectionNavigation.previous(getChildren(), getSelectedItem(), M3ListItem.class);
        if (previousItem != null) {
            selectItem(previousItem);
        }
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection()) {
            selectFirstItemIfNeeded();
            return;
        }
        selectItem(null);
    }

    /// Returns the user-agent stylesheet for M3FX navigation drawers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-drawer.css");
    }

    /// Returns accessibility attributes for navigation drawer content and selection state.
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> false;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for drawer list items.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
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
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        setSpacing(4.0);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getChildren().addListener(childrenListener);
    }

    /// Applies keyboard navigation across enabled drawer items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeySelection(
                event,
                getChildren(),
                getSelectedItem(),
                M3ListItem.class,
                false,
                true,
                this::select
        );
    }

    /// Applies the selected drawer item supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        @Nullable M3ListItem item = M3Accessible.firstSelectionTarget(getItems(), M3ListItem.class, parameters);
        if (item == null) {
            clearSelection();
        } else {
            select(item);
        }
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
            refreshSelectedItems();
            if (!isAllowEmptySelection()) {
                selectFirstItemIfNeeded();
            }
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
        } finally {
            updatingSelection = false;
        }
        refreshSelectedItems();
    }

    /// Enforces single-selection and non-empty selection invariants.
    private void enforceSelectionPolicy() {
        refreshSelectedItems();
        if (selectedItems.size() > 1) {
            selectItem(selectedItems.get(0));
            return;
        }
        if (!isAllowEmptySelection()) {
            selectFirstItemIfNeeded();
        }
    }

    /// Refreshes selected item state from current child states.
    private void refreshSelectedItems() {
        List<M3ListItem> previousSelection = List.copyOf(selectedItems);
        selectedItems.clear();
        for (Node child : getChildren()) {
            if (child instanceof M3ListItem item && item.isSelected()) {
                selectedItems.add(item);
            }
        }
        selectedItem.set(selectedItems.isEmpty() ? null : selectedItems.get(0));
        if (!selectedItems.equals(previousSelection)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        }
    }

    /// Selects the first drawer list item when selection is empty.
    private void selectFirstItemIfNeeded() {
        if (!selectedItems.isEmpty()) {
            return;
        }

        M3ListItem firstItem = firstListItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Returns the first drawer list item child.
    private @Nullable M3ListItem firstListItem() {
        return M3SelectionNavigation.first(getChildren(), M3ListItem.class);
    }

    /// Validates a drawer item array.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
