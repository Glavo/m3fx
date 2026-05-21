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
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3NavigationRailSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 navigation rail for primary destinations in medium-width layouts.
///
/// `M3NavigationRail` arranges [M3NavigationItem] children vertically and manages their selected state. It
/// supports empty-selection control, read-only selected-item views, keyboard navigation, and JavaFX accessibility
/// selection attributes. Use a rail when there is enough horizontal space for persistent navigation but a full
/// drawer would be too wide.
///
/// See [Material Design navigation rails](https://m3.material.io/components/navigation-rail/overview).
@NotNullByDefault
public class M3NavigationRail extends Control {
    /// The base style class for M3FX navigation rails.
    public static final String STYLE_CLASS = "m3-navigation-rail";

    /// The mutable navigation rail content.
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    // The currently selected navigation item.
    private final ReadOnlyObjectWrapper<@Nullable M3NavigationItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected navigation items in child order.
    private final ObservableList<M3NavigationItem> selectedItems = FXCollections.observableArrayList();

    /// The read-only selected navigation item view.
    private final @UnmodifiableView ObservableList<M3NavigationItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    // Whether the rail allows all navigation items to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection") {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

    /// The selected-state listeners installed on navigation items.
    private final Map<M3NavigationItem, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

    /// Updates navigation item selection listeners when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3NavigationItem item) {
                    uninstallItem(item);
                    item.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3NavigationItem item) {
                    installItem(item);
                }
            }
        }
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    };

    /// Whether the navigation rail is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty navigation rail.
    public M3NavigationRail() {
        initialize();
    }

    /// Creates a navigation rail containing the supplied items.
    ///
    /// @param items the initial non-null navigation items
    public M3NavigationRail(M3NavigationItem... items) {
        initialize();
        addItems(items);
    }

    /// Returns the mutable child list used as navigation rail items.
    ///
    /// @return the mutable navigation rail content list
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Adds one navigation item.
    ///
    /// @param item the non-null navigation item to append
    public final void addItem(M3NavigationItem item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds navigation items.
    ///
    /// @param items the non-null navigation items to append
    public final void addItems(M3NavigationItem... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all navigation items.
    ///
    /// @param items the non-null navigation items that replace the current content
    public final void setItems(M3NavigationItem... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all navigation rail content.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the selected navigation items in child order.
    ///
    /// @return an unmodifiable observable view of selected navigation items
    public final @UnmodifiableView ObservableList<M3NavigationItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the selected navigation item.
    ///
    /// @return the selected navigation item, or `null` when no item is selected
    public final @Nullable M3NavigationItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the selected navigation item property.
    ///
    /// @return the read-only selected navigation item property
    public final ReadOnlyObjectProperty<@Nullable M3NavigationItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the child index of the selected navigation item, or `-1` when no item is selected.
    ///
    /// @return the child index of the selected navigation item, or `-1` when no item is selected
    public final int getSelectedIndex() {
        @Nullable M3NavigationItem item = getSelectedItem();
        return item == null ? -1 : getItems().indexOf(item);
    }

    /// Returns whether this rail allows all navigation items to be unselected.
    ///
    /// @return `true` when all navigation items may be unselected
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this rail allows all navigation items to be unselected.
    ///
    /// @param allowEmptySelection whether all navigation items may be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    ///
    /// @return the writable empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a navigation item that belongs to this rail.
    ///
    /// @param item the navigation item to select
    public final void select(M3NavigationItem item) {
        Objects.requireNonNull(item, "item");
        if (!getItems().contains(item)) {
            throw new IllegalArgumentException("item must belong to this navigation rail");
        }
        selectItem(item);
    }

    /// Selects the navigation item at the given child index.
    ///
    /// @param index the child index of the navigation item
    public final void selectIndex(int index) {
        Node child = getItems().get(index);
        if (child instanceof M3NavigationItem item) {
            select(item);
            return;
        }
        throw new IllegalArgumentException("child at index is not an M3NavigationItem");
    }

    /// Selects the first navigation item when one exists.
    public final void selectFirst() {
        M3NavigationItem firstItem = firstNavigationItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Selects the last navigation item when one exists.
    public final void selectLast() {
        @Nullable M3NavigationItem lastItem =
                M3SelectionNavigation.last(getItems(), M3NavigationItem.class);
        if (lastItem != null) {
            selectItem(lastItem);
        }
    }

    /// Selects the next navigation item after the current selected item, wrapping at the end.
    public final void selectNext() {
        @Nullable M3NavigationItem nextItem =
                M3SelectionNavigation.next(getItems(), getSelectedItem(), M3NavigationItem.class);
        if (nextItem != null) {
            selectItem(nextItem);
        }
    }

    /// Selects the previous navigation item before the current selected item, wrapping at the start.
    public final void selectPrevious() {
        @Nullable M3NavigationItem previousItem =
                M3SelectionNavigation.previous(getItems(), getSelectedItem(), M3NavigationItem.class);
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

    /// Returns the user-agent stylesheet for M3FX navigation rails.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-rail.css");
    }

    /// Returns accessibility attributes for navigation rail content and selection state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case FOCUS_NODE -> M3Accessible.focusTarget(M3SelectionNavigation.focusTarget(
                    getItems(),
                    getSelectedItem(),
                    M3NavigationItem.class
            ));
            case MULTIPLE_SELECTION -> false;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for navigation items.
    ///
    /// @param action the accessibility action to execute
    /// @param parameters optional action-specific parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3SelectionNavigation.focusTarget(
                    getItems(),
                    getSelectedItem(),
                    M3NavigationItem.class
            ));
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> M3Accessible.showItem(getItems(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and installs selection listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(childrenListener);
    }

    /// Applies keyboard navigation across enabled navigation items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeySelection(
                event,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3NavigationItem.class),
                M3NavigationItem.class,
                false,
                true,
                this::select
        );
    }

    /// Applies the selected navigation item supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        @Nullable M3NavigationItem item =
                M3Accessible.firstSelectionTarget(getItems(), M3NavigationItem.class, parameters);
        if (item == null) {
            clearSelection();
        } else {
            select(item);
        }
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
            refreshSelectedItems();
            if (!isAllowEmptySelection()) {
                selectFirstItemIfNeeded();
            }
        }
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

    /// Selects the first navigation item when selection is empty.
    private void selectFirstItemIfNeeded() {
        M3NavigationItem firstItem = firstNavigationItem();
        if (!selectedItems.isEmpty() || firstItem == null) {
            return;
        }

        selectItem(firstItem);
    }

    /// Selects an item and clears selection from the remaining navigation items.
    private void selectItem(@Nullable M3NavigationItem item) {
        updatingSelection = true;
        try {
            for (Node child : getItems()) {
                if (child instanceof M3NavigationItem navigationItem) {
                    navigationItem.setSelected(navigationItem == item);
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedItems();
    }

    /// Refreshes selected item state from current child states.
    private void refreshSelectedItems() {
        List<M3NavigationItem> previousSelection = List.copyOf(selectedItems);
        selectedItems.clear();
        for (Node child : getItems()) {
            if (child instanceof M3NavigationItem item && item.isSelected()) {
                selectedItems.add(item);
            }
        }
        selectedItem.set(selectedItems.isEmpty() ? null : selectedItems.get(0));
        if (!selectedItems.equals(previousSelection)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        }
    }

    /// Returns the first navigation item child.
    private @Nullable M3NavigationItem firstNavigationItem() {
        return M3SelectionNavigation.first(getItems(), M3NavigationItem.class);
    }

    /// Creates the default Material Design 3 navigation rail skin.
    ///
    /// @return the default Material Design 3 navigation rail skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3NavigationRailSkin(this);
    }

    /// Validates a navigation item array.
    private static void validateItems(M3NavigationItem... items) {
        Objects.requireNonNull(items, "items");
        for (M3NavigationItem item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
