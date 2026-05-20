// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ListPaneSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 static list container for a small number of already-created nodes.
@NotNullByDefault
public class M3ListPane extends Control {
    /// The base style class for M3FX static list panes.
    public static final String STYLE_CLASS = "m3-list-pane";

    /// The mutable list content.
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    /// The list item selection mode.
    private final ObjectProperty<@Nullable M3ListSelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", M3ListSelectionMode.NONE) {
                /// Enforces selection invariants when the mode changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3ListSelectionMode.NONE);
                        return;
                    }
                    enforceSelectionPolicy();
                }
            };

    /// Whether this list allows all selectable items to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection", true) {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

    /// The selected list items in child order.
    private final ObservableList<M3ListItem> selectedItems = FXCollections.observableArrayList();

    /// The read-only selected list item view.
    private final @UnmodifiableView ObservableList<M3ListItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// The first selected list item in child order.
    private final ReadOnlyObjectWrapper<@Nullable M3ListItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected-state listeners installed on list items.
    private final Map<M3ListItem, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

    /// Handles list item actions by applying the configured selection policy.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// Updates item listeners and selection when children change.
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

    /// Whether the list is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty list.
    public M3ListPane() {
        initialize();
    }

    /// Creates a list containing the supplied nodes.
    public M3ListPane(Node... items) {
        initialize();
        addItems(items);
    }

    /// Returns the mutable child list used as list content.
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Adds one list content node.
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds list content nodes.
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all list content nodes.
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all list content nodes.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the list item selection mode.
    public final M3ListSelectionMode getSelectionMode() {
        return Objects.requireNonNull(selectionMode.get(), "selectionMode");
    }

    /// Sets the list item selection mode.
    public final void setSelectionMode(M3ListSelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the list item selection mode property.
    public final ObjectProperty<@Nullable M3ListSelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns whether this list allows all selectable items to be unselected.
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this list allows all selectable items to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Returns the selected list items in child order.
    public final @UnmodifiableView ObservableList<M3ListItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the first selected list item in child order.
    public final @Nullable M3ListItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the first selected list item property.
    public final ReadOnlyObjectProperty<@Nullable M3ListItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the child index of the first selected list item, or `-1` when no item is selected.
    public final int getSelectedIndex() {
        @Nullable M3ListItem item = getSelectedItem();
        return item == null ? -1 : getItems().indexOf(item);
    }

    /// Selects a list item that belongs to this list.
    public final void select(M3ListItem item) {
        Objects.requireNonNull(item, "item");
        if (!getItems().contains(item)) {
            throw new IllegalArgumentException("item must belong to this list");
        }

        if (getSelectionMode() == M3ListSelectionMode.MULTIPLE) {
            setItemSelected(item, true);
        } else {
            selectOnly(item);
        }
    }

    /// Selects the list item at the given child index.
    public final void selectIndex(int index) {
        Node child = getItems().get(index);
        if (child instanceof M3ListItem item) {
            select(item);
            return;
        }
        throw new IllegalArgumentException("child at index is not an M3ListItem");
    }

    /// Selects the first list item when one exists.
    public final void selectFirst() {
        M3ListItem firstItem = firstItem();
        if (firstItem != null) {
            select(firstItem);
        }
    }

    /// Selects the last list item when one exists.
    public final void selectLast() {
        @Nullable M3ListItem lastItem = M3SelectionNavigation.last(getItems(), M3ListItem.class);
        if (lastItem != null) {
            select(lastItem);
        }
    }

    /// Selects the next list item after the current selected item, wrapping at the end.
    public final void selectNext() {
        @Nullable M3ListItem nextItem =
                M3SelectionNavigation.next(getItems(), getSelectedItem(), M3ListItem.class);
        if (nextItem != null) {
            select(nextItem);
        }
    }

    /// Selects the previous list item before the current selected item, wrapping at the start.
    public final void selectPrevious() {
        @Nullable M3ListItem previousItem =
                M3SelectionNavigation.previous(getItems(), getSelectedItem(), M3ListItem.class);
        if (previousItem != null) {
            select(previousItem);
        }
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection() && getSelectionMode() != M3ListSelectionMode.NONE) {
            selectFirstItemIfNeeded();
            return;
        }
        selectOnly(null);
    }

    /// Returns the user-agent stylesheet for M3FX lists.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("list-item.css");
    }

    /// Returns accessibility attributes for list content and selection state.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> getSelectionMode() == M3ListSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for list items.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> M3Accessible.showItem(getItems(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and installs child listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(childrenListener);
    }

    /// Applies keyboard navigation across enabled list items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (getSelectionMode() == M3ListSelectionMode.NONE
                || getSelectionMode() == M3ListSelectionMode.MULTIPLE) {
            M3SelectionNavigation.handleKeyFocus(
                    event,
                    getItems(),
                    M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3ListItem.class),
                    M3ListItem.class,
                    false,
                    true
            );
            return;
        }

        M3SelectionNavigation.handleKeySelection(
                event,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3ListItem.class),
                M3ListItem.class,
                false,
                true,
                this::select
        );
    }

    /// Applies selected list items supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3ListSelectionMode.NONE) {
            return;
        }

        if (getSelectionMode() == M3ListSelectionMode.SINGLE) {
            @Nullable M3ListItem item = M3Accessible.firstSelectionTarget(getItems(), M3ListItem.class, parameters);
            if (item == null) {
                clearSelection();
            } else {
                select(item);
            }
            return;
        }

        updatingSelection = true;
        try {
            for (Node child : getItems()) {
                if (child instanceof M3ListItem item) {
                    item.setSelected(M3Accessible.containsSelectionTarget(item, parameters));
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedItems();
        if (!isAllowEmptySelection()) {
            selectFirstItemIfNeeded();
        }
    }

    /// Installs action and selected-state listeners on a list item.
    private void installItem(M3ListItem item) {
        item.addEventHandler(ActionEvent.ACTION, itemActionHandler);
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleItemSelectedChanged(item, newValue);
        selectedListeners.put(item, listener);
        item.selectedProperty().addListener(listener);
    }

    /// Removes action and selected-state listeners from a list item.
    private void uninstallItem(M3ListItem item) {
        item.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        ChangeListener<Boolean> listener = selectedListeners.remove(item);
        if (listener != null) {
            item.selectedProperty().removeListener(listener);
        }
    }

    /// Applies the configured selection policy to a list item action.
    private void handleItemAction(ActionEvent event) {
        if (!(event.getSource() instanceof M3ListItem item)
                || !getItems().contains(item)
                || item.isDisabled()) {
            return;
        }

        switch (getSelectionMode()) {
            case NONE -> {
            }
            case SINGLE -> selectOnly(item);
            case MULTIPLE -> {
                if (item.isSelected() && !isAllowEmptySelection() && selectedItems.size() == 1) {
                    selectOnly(item);
                } else {
                    setItemSelected(item, !item.isSelected());
                }
            }
        }
    }

    /// Keeps externally changed item selected states consistent with the current list policy.
    private void handleItemSelectedChanged(M3ListItem item, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (selected && getSelectionMode() == M3ListSelectionMode.SINGLE) {
            selectOnly(item);
            return;
        }

        refreshSelectedItems();
        if (!selected && !isAllowEmptySelection()
                && getSelectionMode() != M3ListSelectionMode.NONE
                && selectedItems.isEmpty()) {
            select(item);
        } else {
            enforceSelectionPolicy();
        }
    }

    /// Enforces selection invariants for the current selection mode.
    private void enforceSelectionPolicy() {
        refreshSelectedItems();
        if (getSelectionMode() == M3ListSelectionMode.SINGLE && selectedItems.size() > 1) {
            selectOnly(selectedItems.get(0));
            return;
        }
        if (!isAllowEmptySelection() && getSelectionMode() != M3ListSelectionMode.NONE) {
            selectFirstItemIfNeeded();
        }
    }

    /// Selects the first item when selection is empty and empty selection is disabled.
    private void selectFirstItemIfNeeded() {
        if (!selectedItems.isEmpty()) {
            return;
        }

        M3ListItem firstItem = firstItem();
        if (firstItem != null) {
            select(firstItem);
        }
    }

    /// Sets one list item's selected state and refreshes selected item state.
    private void setItemSelected(M3ListItem item, boolean selected) {
        updatingSelection = true;
        try {
            item.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
        refreshSelectedItems();
    }

    /// Selects one item and clears selection from the remaining list items.
    private void selectOnly(@Nullable M3ListItem item) {
        updatingSelection = true;
        try {
            for (Node child : getItems()) {
                if (child instanceof M3ListItem listItem) {
                    listItem.setSelected(listItem == item);
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedItems();
    }

    /// Refreshes selected item state from current child item states.
    private void refreshSelectedItems() {
        List<M3ListItem> previousSelection = List.copyOf(selectedItems);
        selectedItems.clear();
        for (Node child : getItems()) {
            if (child instanceof M3ListItem item && item.isSelected()) {
                selectedItems.add(item);
            }
        }
        selectedItem.set(selectedItems.isEmpty() ? null : selectedItems.get(0));
        if (!selectedItems.equals(previousSelection)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        }
    }

    /// Returns the first list item child.
    private @Nullable M3ListItem firstItem() {
        return M3SelectionNavigation.first(getItems(), M3ListItem.class);
    }

    /// Creates the default Material Design 3 list skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ListPaneSkin(this);
    }

    /// Validates a list item array.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
