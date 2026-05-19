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

/// A Material Design 3 menu surface.
@NotNullByDefault
public class M3Menu extends VBox {
    /// The base style class for M3FX menus.
    public static final String STYLE_CLASS = "m3-menu";

    /// The menu selection mode.
    private final ObjectProperty<M3MenuSelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", M3MenuSelectionMode.NONE) {
                /// Enforces selection invariants when the mode changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3MenuSelectionMode.NONE);
                        return;
                    }
                    enforceSelectionPolicy();
                }
            };

    /// Whether the menu allows all selectable menu items to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection", true) {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

    /// The selected menu items in child order.
    private final ObservableList<M3MenuItem> selectedItems = FXCollections.observableArrayList();

    /// The read-only selected menu item view.
    private final @UnmodifiableView ObservableList<M3MenuItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// The first selected menu item in child order.
    private final ReadOnlyObjectWrapper<@Nullable M3MenuItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected-state listeners installed on menu items.
    private final Map<M3MenuItem, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

    /// Handles menu item actions by applying the configured selection policy.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// Updates item listeners and selection when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3MenuItem item) {
                    if (item instanceof M3SubMenuItem subMenuItem) {
                        subMenuItem.hideSubMenu();
                    }
                    uninstallItem(item);
                    item.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3MenuItem item) {
                    installItem(item);
                }
            }
        }
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
    };

    /// Whether the menu is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty menu.
    public M3Menu() {
        initialize();
    }

    /// Creates a menu containing the supplied items.
    public M3Menu(Node... items) {
        initialize();
        addItems(items);
    }

    /// Returns the mutable child list used as menu content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Adds one menu content node.
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds menu content nodes.
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all menu content nodes.
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all menu content nodes.
    public final void clearItems() {
        getItems().clear();
    }

    /// Hides any open submenu popups owned by this menu.
    final void hideSubMenusExcept(@Nullable M3SubMenuItem exception) {
        for (Node child : getChildren()) {
            if (child instanceof M3SubMenuItem subMenuItem && subMenuItem != exception) {
                subMenuItem.hideSubMenu();
            }
        }
    }

    /// Returns the menu selection mode.
    public final M3MenuSelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the menu selection mode.
    public final void setSelectionMode(M3MenuSelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the menu selection mode property.
    public final ObjectProperty<M3MenuSelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns whether this menu allows all selectable items to be unselected.
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this menu allows all selectable items to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Returns the selected menu items in child order.
    public final @UnmodifiableView ObservableList<M3MenuItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the first selected menu item in child order.
    public final @Nullable M3MenuItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the first selected menu item property.
    public final ReadOnlyObjectProperty<@Nullable M3MenuItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the child index of the first selected menu item, or `-1` when no item is selected.
    public final int getSelectedIndex() {
        @Nullable M3MenuItem item = getSelectedItem();
        return item == null ? -1 : getChildren().indexOf(item);
    }

    /// Selects a menu item that belongs to this menu.
    public final void select(M3MenuItem item) {
        Objects.requireNonNull(item, "item");
        if (!getChildren().contains(item)) {
            throw new IllegalArgumentException("item must belong to this menu");
        }
        if (!isSelectableMenuItem(item)) {
            throw new IllegalArgumentException("item must be selectable");
        }

        if (getSelectionMode() == M3MenuSelectionMode.MULTIPLE) {
            setItemSelected(item, true);
        } else {
            selectOnly(item);
        }
    }

    /// Selects the selectable menu item at the given child index.
    public final void selectIndex(int index) {
        Node child = getChildren().get(index);
        if (child instanceof M3MenuItem item && isSelectableMenuItem(item)) {
            select(item);
            return;
        }
        throw new IllegalArgumentException("child at index is not a selectable M3MenuItem");
    }

    /// Selects the first selectable menu item when one exists.
    public final void selectFirst() {
        M3MenuItem firstItem = firstItem();
        if (firstItem != null) {
            select(firstItem);
        }
    }

    /// Selects the last selectable menu item when one exists.
    public final void selectLast() {
        @Nullable M3MenuItem lastItem = lastItem();
        if (lastItem != null) {
            select(lastItem);
        }
    }

    /// Selects the next selectable menu item after the current selected item, wrapping at the end.
    public final void selectNext() {
        @Nullable M3MenuItem nextItem = nextItem(getSelectedItem());
        if (nextItem != null) {
            select(nextItem);
        }
    }

    /// Selects the previous selectable menu item before the current selected item, wrapping at the start.
    public final void selectPrevious() {
        @Nullable M3MenuItem previousItem = previousItem(getSelectedItem());
        if (previousItem != null) {
            select(previousItem);
        }
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection() && getSelectionMode() != M3MenuSelectionMode.NONE) {
            selectFirstItemIfNeeded();
            return;
        }
        selectOnly(null);
    }

    /// Returns the user-agent stylesheet for M3FX menus.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("menu.css");
    }

    /// Returns accessibility attributes for menu content and selection state.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> getSelectionMode() == M3MenuSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for menu items.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> M3Accessible.showItem(getItems(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.MENU);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getChildren().addListener(childrenListener);
    }

    /// Applies keyboard navigation across enabled menu items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (getSelectionMode() == M3MenuSelectionMode.NONE
                || getSelectionMode() == M3MenuSelectionMode.MULTIPLE) {
            M3SelectionNavigation.handleKeyFocus(
                    event,
                    getChildren(),
                    M3SelectionNavigation.focusAnchor(getChildren(), getSelectedItem(), M3MenuItem.class),
                    M3MenuItem.class,
                    false,
                    true
            );
            return;
        }

        handleSelectionNavigationKeyPressed(event);
    }

    /// Applies keyboard selection across selectable menu items.
    private void handleSelectionNavigationKeyPressed(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        @Nullable M3MenuItem target = switch (event.getCode()) {
            case UP -> previousItem(getSelectedItem());
            case DOWN -> nextItem(getSelectedItem());
            case HOME -> firstItem();
            case END -> lastItem();
            default -> null;
        };
        if (target == null) {
            return;
        }

        select(target);
        if (target.isFocusTraversable()) {
            target.requestFocus();
        }
        event.consume();
    }

    /// Applies selected menu items supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3MenuSelectionMode.NONE) {
            return;
        }

        if (getSelectionMode() == M3MenuSelectionMode.SINGLE) {
            @Nullable M3MenuItem item = firstAccessibleSelectableItem(parameters);
            if (item == null) {
                clearSelection();
            } else {
                select(item);
            }
            return;
        }

        updatingSelection = true;
        try {
            for (Node child : getChildren()) {
                if (child instanceof M3MenuItem item && isSelectableMenuItem(item)) {
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

    /// Installs action and selected-state listeners on a menu item.
    private void installItem(M3MenuItem item) {
        item.addEventHandler(ActionEvent.ACTION, itemActionHandler);
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleItemSelectedChanged(item, newValue);
        selectedListeners.put(item, listener);
        item.selectedProperty().addListener(listener);
    }

    /// Removes action and selected-state listeners from a menu item.
    private void uninstallItem(M3MenuItem item) {
        item.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        ChangeListener<Boolean> listener = selectedListeners.remove(item);
        if (listener != null) {
            item.selectedProperty().removeListener(listener);
        }
    }

    /// Applies menu selection policy to an item action.
    private void handleItemAction(ActionEvent event) {
        if (!(event.getSource() instanceof M3MenuItem item)
                || !getChildren().contains(item)
                || item.isDisabled()
                || !isSelectableMenuItem(item)) {
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

    /// Keeps externally changed item selected states consistent with the current menu policy.
    private void handleItemSelectedChanged(M3MenuItem item, boolean selected) {
        if (updatingSelection) {
            return;
        }
        if (!isSelectableMenuItem(item)) {
            if (selected) {
                setItemSelected(item, false);
            }
            return;
        }

        if (selected && getSelectionMode() == M3MenuSelectionMode.SINGLE) {
            selectOnly(item);
            return;
        }

        refreshSelectedItems();
        if (!selected && !isAllowEmptySelection()
                && getSelectionMode() != M3MenuSelectionMode.NONE
                && selectedItems.isEmpty()) {
            select(item);
        } else {
            enforceSelectionPolicy();
        }
    }

    /// Enforces selection invariants for the current selection mode.
    private void enforceSelectionPolicy() {
        refreshSelectedItems();
        if (getSelectionMode() == M3MenuSelectionMode.SINGLE && selectedItems.size() > 1) {
            selectOnly(selectedItems.get(0));
            return;
        }
        if (!isAllowEmptySelection() && getSelectionMode() != M3MenuSelectionMode.NONE) {
            selectFirstItemIfNeeded();
        }
    }

    /// Selects the first item when selection is empty and empty selection is disabled.
    private void selectFirstItemIfNeeded() {
        if (!selectedItems.isEmpty()) {
            return;
        }

        M3MenuItem firstItem = firstItem();
        if (firstItem != null) {
            select(firstItem);
        }
    }

    /// Sets one menu item's selected state and refreshes selected item state.
    private void setItemSelected(M3MenuItem item, boolean selected) {
        updatingSelection = true;
        try {
            item.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
        refreshSelectedItems();
    }

    /// Selects one item and clears selection from the remaining menu items.
    private void selectOnly(@Nullable M3MenuItem item) {
        updatingSelection = true;
        try {
            for (Node child : getChildren()) {
                if (child instanceof M3MenuItem menuItem) {
                    menuItem.setSelected(menuItem == item);
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedItems();
    }

    /// Refreshes selected item state from current child item states.
    private void refreshSelectedItems() {
        List<M3MenuItem> previousSelection = List.copyOf(selectedItems);
        selectedItems.clear();
        for (Node child : getChildren()) {
            if (child instanceof M3MenuItem item && isSelectableMenuItem(item) && item.isSelected()) {
                selectedItems.add(item);
            }
        }
        selectedItem.set(selectedItems.isEmpty() ? null : selectedItems.get(0));
        if (!selectedItems.equals(previousSelection)) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        }
    }

    /// Returns the first selectable menu item child.
    private @Nullable M3MenuItem firstItem() {
        for (Node child : getChildren()) {
            @Nullable M3MenuItem item = selectableMenuItem(child);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the last selectable menu item child.
    private @Nullable M3MenuItem lastItem() {
        ObservableList<Node> children = getChildren();
        for (int index = children.size() - 1; index >= 0; index--) {
            @Nullable M3MenuItem item = selectableMenuItem(children.get(index));
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the next selectable menu item after the current item.
    private @Nullable M3MenuItem nextItem(@Nullable M3MenuItem current) {
        ObservableList<Node> children = getChildren();
        int childCount = children.size();
        if (childCount == 0) {
            return null;
        }

        int currentIndex = current == null ? -1 : children.indexOf(current);
        for (int offset = 1; offset <= childCount; offset++) {
            @Nullable M3MenuItem item = selectableMenuItem(
                    children.get(Math.floorMod(currentIndex + offset, childCount))
            );
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the previous selectable menu item before the current item.
    private @Nullable M3MenuItem previousItem(@Nullable M3MenuItem current) {
        ObservableList<Node> children = getChildren();
        int childCount = children.size();
        if (childCount == 0) {
            return null;
        }

        int currentIndex = current == null ? childCount : children.indexOf(current);
        if (currentIndex < 0) {
            currentIndex = childCount;
        }

        for (int offset = 1; offset <= childCount; offset++) {
            @Nullable M3MenuItem item = selectableMenuItem(
                    children.get(Math.floorMod(currentIndex - offset, childCount))
            );
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the first selectable menu item referenced by accessibility parameters.
    private @Nullable M3MenuItem firstAccessibleSelectableItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getChildren()) {
            @Nullable M3MenuItem item = selectableMenuItem(child);
            if (item != null && M3Accessible.containsSelectionTarget(item, parameters)) {
                return item;
            }
        }
        return null;
    }

    /// Returns a node as a selectable menu item when possible.
    private static @Nullable M3MenuItem selectableMenuItem(Node child) {
        if (child instanceof M3MenuItem item
                && isSelectableMenuItem(item)
                && !item.isDisabled()
                && item.isVisible()) {
            return item;
        }
        return null;
    }

    /// Returns whether a menu item participates in selection state.
    private static boolean isSelectableMenuItem(M3MenuItem item) {
        return !(item instanceof M3SubMenuItem);
    }

    /// Validates a menu item array.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
