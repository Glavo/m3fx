// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ListViewSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 data-driven virtualized list view.
///
/// `M3ListView` displays an observable item list through a cell factory that creates [M3ListItem] nodes for
/// visible rows. Unlike [M3ListPane], it is intended for large or dynamic data sets and uses virtualization so
/// the number of scene graph nodes is bounded by the viewport. The control exposes selection mode,
/// selected-index views, fixed-cell-size hints, animated scrolling, and keyboard navigation.
///
/// Use this control for application data lists and feeds. See
/// [Material Design lists](https://m3.material.io/components/lists/overview).
///
/// @param <T> the item type rendered by this list view
@NotNullByDefault
public class M3ListView<T> extends Control {
    /// The base style class for M3FX virtualized list views.
    public static final String STYLE_CLASS = "m3-list-view";

    /// The default fixed cell size hint, disabled by default.
    private static final double DEFAULT_FIXED_CELL_SIZE = 0.0;

    /// The backing data items rendered by this view.
    private final ObservableList<T> items = FXCollections.observableArrayList();

    /// The list item factory used by virtualized cells.
    private final ObjectProperty<@Nullable Callback<? super T, ? extends M3ListItem>> cellFactory =
            new SimpleObjectProperty<>(this, "cellFactory") {
                /// Rebuilds visible cells when the factory changes.
                @Override
                protected void invalidated() {
                    requestCellRebuild();
                }
            };

    /// The selection mode used by this virtualized list.
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

    /// Whether this list view allows all selectable items to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection", true) {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

    /// The fixed cell size hint forwarded to the virtual flow.
    private final DoubleProperty fixedCellSize =
            new SimpleDoubleProperty(this, "fixedCellSize", DEFAULT_FIXED_CELL_SIZE) {
                /// Validates updated fixed cell size values.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "fixedCellSize"));
                }
            };

    /// Whether focus and programmatic scrolling animate the virtual flow position.
    private final BooleanProperty animatedScroll = new SimpleBooleanProperty(this, "animatedScroll", true);

    /// The selected indices in ascending order.
    private final ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();

    /// The read-only selected index view.
    private final @UnmodifiableView ObservableList<Integer> selectedIndicesView =
            FXCollections.unmodifiableObservableList(selectedIndices);

    /// The selected item values in ascending index order.
    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();

    /// The read-only selected item view.
    private final @UnmodifiableView ObservableList<T> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// The first selected index, or `-1` when selection is empty.
    private final ReadOnlyIntegerWrapper selectedIndex = new ReadOnlyIntegerWrapper(this, "selectedIndex", -1);

    /// The first selected item, or `null` when selection is empty.
    private final ReadOnlyObjectWrapper<@Nullable T> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The keyboard-focused data index, or `-1` when no row has list focus.
    private final ReadOnlyIntegerWrapper focusedIndex = new ReadOnlyIntegerWrapper(this, "focusedIndex", -1);

    /// The keyboard-focused data item, or `null` when no row has list focus.
    private final ReadOnlyObjectWrapper<@Nullable T> focusedItem =
            new ReadOnlyObjectWrapper<>(this, "focusedItem");

    /// Updates selection and cells when data items change.
    private final ListChangeListener<T> itemsListener = change -> {
        trimSelectedIndices();
        trimFocusedIndex();
        if (!isAllowEmptySelection() && getSelectionMode() != M3ListSelectionMode.NONE && selectedIndices.isEmpty()) {
            selectFirstItemIfNeeded();
        } else {
            refreshSelectedItems();
        }
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        requestCellCountRefresh();
    };

    /// Creates an empty virtualized list view.
    public M3ListView() {
        initialize();
    }

    /// Creates a virtualized list view containing the supplied data items.
    @SafeVarargs
    public M3ListView(T... items) {
        initialize();
        addItems(items);
    }

    /// Returns the mutable backing data list.
    public final ObservableList<T> getItems() {
        return items;
    }

    /// Adds one data item.
    public final void addItem(T item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds data items.
    @SafeVarargs
    public final void addItems(T... items) {
        Objects.requireNonNull(items, "items");
        validateItems(items);
        Collections.addAll(getItems(), items);
    }

    /// Replaces all data items.
    @SafeVarargs
    public final void setItems(T... items) {
        Objects.requireNonNull(items, "items");
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all data items.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the factory used to create a list item for one data item.
    public final @Nullable Callback<? super T, ? extends M3ListItem> getCellFactory() {
        return cellFactory.get();
    }

    /// Sets the factory used to create a list item for one data item.
    public final void setCellFactory(@Nullable Callback<? super T, ? extends M3ListItem> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    /// Returns the list item factory property.
    public final ObjectProperty<@Nullable Callback<? super T, ? extends M3ListItem>> cellFactoryProperty() {
        return cellFactory;
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

    /// Returns whether this list view allows all selectable items to be unselected.
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this list view allows all selectable items to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Returns the fixed cell size hint used by the virtual flow.
    public final double getFixedCellSize() {
        return fixedCellSize.get();
    }

    /// Sets the fixed cell size hint used by the virtual flow.
    public final void setFixedCellSize(double fixedCellSize) {
        this.fixedCellSize.set(M3Css.nonNegative(fixedCellSize, "fixedCellSize"));
    }

    /// Returns the fixed cell size property.
    public final DoubleProperty fixedCellSizeProperty() {
        return fixedCellSize;
    }

    /// Returns whether focus and programmatic scrolling animate the virtual flow position.
    public final boolean isAnimatedScroll() {
        return animatedScroll.get();
    }

    /// Sets whether focus and programmatic scrolling animate the virtual flow position.
    public final void setAnimatedScroll(boolean animatedScroll) {
        this.animatedScroll.set(animatedScroll);
    }

    /// Returns the animated virtual flow scrolling property.
    public final BooleanProperty animatedScrollProperty() {
        return animatedScroll;
    }

    /// Returns the selected indices in ascending order.
    public final @UnmodifiableView ObservableList<Integer> getSelectedIndices() {
        return selectedIndicesView;
    }

    /// Returns the selected item values in ascending index order.
    public final @UnmodifiableView ObservableList<T> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the first selected index, or `-1` when selection is empty.
    public final int getSelectedIndex() {
        return selectedIndex.get();
    }

    /// Returns the first selected index property.
    public final ReadOnlyIntegerProperty selectedIndexProperty() {
        return selectedIndex.getReadOnlyProperty();
    }

    /// Returns the first selected item, or `null` when selection is empty.
    public final @Nullable T getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the first selected item property.
    public final ReadOnlyObjectProperty<@Nullable T> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the keyboard-focused data index, or `-1` when no row has list focus.
    public final int getFocusedIndex() {
        return focusedIndex.get();
    }

    /// Returns the keyboard-focused data index property.
    public final ReadOnlyIntegerProperty focusedIndexProperty() {
        return focusedIndex.getReadOnlyProperty();
    }

    /// Returns the keyboard-focused data item, or `null` when no row has list focus.
    public final @Nullable T getFocusedItem() {
        return focusedItem.get();
    }

    /// Returns the keyboard-focused data item property.
    public final ReadOnlyObjectProperty<@Nullable T> focusedItemProperty() {
        return focusedItem.getReadOnlyProperty();
    }

    /// Returns whether the supplied item index is selected.
    public final boolean isIndexSelected(int index) {
        return selectedIndices.contains(index);
    }

    /// Returns whether the supplied item index owns list keyboard focus.
    public final boolean isIndexFocused(int index) {
        return focusedIndex.get() == index;
    }

    /// Selects the item at the supplied index.
    public final void selectIndex(int index) {
        checkItemIndex(index);
        if (getSelectionMode() == M3ListSelectionMode.MULTIPLE) {
            setIndexSelected(index, true);
        } else {
            selectOnly(index);
        }
    }

    /// Selects the first item equal to the supplied value.
    public final void selectItem(T item) {
        Objects.requireNonNull(item, "item");
        int index = getItems().indexOf(item);
        if (index < 0) {
            throw new IllegalArgumentException("item must belong to this list view");
        }
        selectIndex(index);
    }

    /// Sets one index's selected state.
    public final void setIndexSelected(int index, boolean selected) {
        checkItemIndex(index);
        if (getSelectionMode() == M3ListSelectionMode.NONE) {
            if (!selected) {
                removeSelectedIndex(index);
            }
            return;
        }

        if (getSelectionMode() == M3ListSelectionMode.SINGLE) {
            if (selected) {
                selectOnly(index);
            } else if (isIndexSelected(index)) {
                clearSelection();
            }
            return;
        }

        if (selected) {
            addSelectedIndex(index);
        } else if (!isAllowEmptySelection() && selectedIndices.size() == 1 && isIndexSelected(index)) {
            selectOnly(index);
        } else {
            removeSelectedIndex(index);
        }
    }

    /// Toggles the selected state for one item index.
    public final void toggleIndex(int index) {
        setIndexSelected(index, !isIndexSelected(index));
    }

    /// Clears all selected indices when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection() && getSelectionMode() != M3ListSelectionMode.NONE) {
            selectFirstItemIfNeeded();
            return;
        }
        selectOnly(-1);
    }

    /// Clears one selected index when empty selection is allowed.
    public final void clearSelection(int index) {
        checkItemIndex(index);
        setIndexSelected(index, false);
    }

    /// Selects the first data item when one exists.
    public final void selectFirst() {
        if (!getItems().isEmpty()) {
            selectIndex(0);
        }
    }

    /// Selects the last data item when one exists.
    public final void selectLast() {
        int lastIndex = getItems().size() - 1;
        if (lastIndex >= 0) {
            selectIndex(lastIndex);
        }
    }

    /// Selects the next item after the first selected item, wrapping at the end.
    public final void selectNext() {
        int size = getItems().size();
        if (size == 0) {
            return;
        }
        int index = getSelectedIndex();
        selectIndex(index < 0 || index + 1 >= size ? 0 : index + 1);
    }

    /// Selects the previous item before the first selected item, wrapping at the start.
    public final void selectPrevious() {
        int size = getItems().size();
        if (size == 0) {
            return;
        }
        int index = getSelectedIndex();
        selectIndex(index <= 0 ? size - 1 : index - 1);
    }

    /// Moves list keyboard focus to the supplied item index and scrolls it into view.
    public final void focusIndex(int index) {
        checkItemIndex(index);
        updateFocusedIndex(index, true);
    }

    /// Clears list keyboard focus without changing selection.
    public final void clearFocus() {
        updateFocusedIndex(-1, false);
    }

    /// Moves list keyboard focus to the first data item when one exists.
    public final void focusFirst() {
        if (!getItems().isEmpty()) {
            focusIndex(0);
        }
    }

    /// Moves list keyboard focus to the last data item when one exists.
    public final void focusLast() {
        int lastIndex = getItems().size() - 1;
        if (lastIndex >= 0) {
            focusIndex(lastIndex);
        }
    }

    /// Moves list keyboard focus to the next item, wrapping at the end.
    public final void focusNext() {
        int target = nextIndex(navigationAnchorIndex());
        if (target >= 0) {
            focusIndex(target);
        }
    }

    /// Moves list keyboard focus to the previous item, wrapping at the start.
    public final void focusPrevious() {
        int target = previousIndex(navigationAnchorIndex());
        if (target >= 0) {
            focusIndex(target);
        }
    }

    /// Scrolls the virtual flow to the supplied item index using the configured animation policy.
    public final void scrollTo(int index) {
        scrollTo(index, isAnimatedScroll());
    }

    /// Scrolls the virtual flow to the supplied item index.
    public final void scrollTo(int index, boolean animated) {
        checkItemIndex(index);
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.scrollTo(index, animated);
        }
    }

    /// Returns the user-agent stylesheet for M3FX virtualized list views.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("list-item.css");
    }

    /// Returns accessibility attributes for list data and rendered cells.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case FOCUS_NODE -> accessibleFocusNode();
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> accessibleItemAt(parameters);
            case MULTIPLE_SELECTION -> getSelectionMode() == M3ListSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection and scrolling actions for list items.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default virtualized list skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ListViewSkin<>(this);
    }

    /// Adds base style classes and installs data listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(itemsListener);
    }

    /// Applies the configured selection policy to a cell activation.
    void activateIndex(int index) {
        if (index < 0 || index >= getItems().size()) {
            return;
        }
        switch (getSelectionMode()) {
            case NONE -> {
            }
            case SINGLE -> selectOnly(index);
            case MULTIPLE -> setIndexSelected(index, !isIndexSelected(index));
        }
    }

    /// Handles list keyboard navigation and focused-row activation.
    private void handleNavigationKeyPressed(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        KeyCode code = event.getCode();
        switch (code) {
            case UP -> moveKeyboardFocus(previousIndex(navigationAnchorIndex()), event);
            case DOWN -> moveKeyboardFocus(nextIndex(navigationAnchorIndex()), event);
            case HOME -> moveKeyboardFocus(firstIndex(), event);
            case END -> moveKeyboardFocus(lastIndex(), event);
            case ENTER, SPACE -> activateFocusedIndex(event);
            default -> {
            }
        }
    }

    /// Moves keyboard focus from a navigation event and selects rows in single-selection mode.
    private void moveKeyboardFocus(int index, KeyEvent event) {
        if (index < 0) {
            return;
        }

        updateFocusedIndex(index, true);
        if (getSelectionMode() == M3ListSelectionMode.SINGLE) {
            selectOnly(index);
        }
        event.consume();
    }

    /// Applies the selection policy to the focused row for Enter and Space keys.
    private void activateFocusedIndex(KeyEvent event) {
        int index = focusedIndex.get();
        if (index < 0) {
            index = getSelectedIndex();
        }
        if (index < 0 || index >= getItems().size()) {
            return;
        }

        updateFocusedIndex(index, true);
        activateIndex(index);
        event.consume();
    }

    /// Returns the visible accessibility item at the supplied index when available.
    private @Nullable Object accessibleItemAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0 || index >= getItems().size()) {
            return null;
        }

        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            @Nullable Node visibleItem = skin.getVisibleItem(index);
            if (visibleItem != null) {
                return visibleItem;
            }
        }

        T item = getItems().get(index);
        return item instanceof Node node ? node : null;
    }

    /// Returns the current visible focus node for accessibility clients.
    private Node accessibleFocusNode() {
        return this;
    }

    /// Moves focus to the focused row, selected row, or first row for accessibility clients.
    private void focusAccessibleNode() {
        int index = focusedIndex.get();
        if (index < 0) {
            index = getSelectedIndex();
        }
        if (index < 0 && !getItems().isEmpty()) {
            index = 0;
        }
        if (index >= 0) {
            focusIndex(index);
        } else {
            requestFocus();
        }
    }

    /// Applies selected items supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3ListSelectionMode.NONE) {
            return;
        }

        List<Integer> indices = selectionIndicesFromParameters(parameters);
        if (getSelectionMode() == M3ListSelectionMode.SINGLE) {
            if (indices.isEmpty()) {
                clearSelection();
            } else {
                selectIndex(indices.get(0));
            }
            return;
        }

        selectedIndices.setAll(indices);
        refreshSelectedItems();
        if (!isAllowEmptySelection()) {
            selectFirstItemIfNeeded();
        }
    }

    /// Scrolls the item referenced by accessibility parameters into view.
    private void showAccessibleItem(Object... parameters) {
        int index = firstSelectionIndex(parameters);
        if (index >= 0) {
            focusIndex(index);
        }
    }

    /// Returns selection indices referenced by accessibility parameters.
    private List<Integer> selectionIndicesFromParameters(Object... parameters) {
        ArrayList<Integer> indices = new ArrayList<>();
        for (Object parameter : parameters) {
            collectSelectionIndices(indices, parameter);
        }
        Collections.sort(indices);
        return indices;
    }

    /// Collects selection indices from one accessibility parameter.
    private void collectSelectionIndices(List<Integer> indices, @Nullable Object parameter) {
        int index = selectionIndex(parameter);
        if (index >= 0 && !indices.contains(index)) {
            indices.add(index);
            return;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                collectSelectionIndices(indices, value);
            }
        } else if (parameter instanceof Object[] values) {
            for (Object value : values) {
                collectSelectionIndices(indices, value);
            }
        }
    }

    /// Returns the first referenced selection index.
    private int firstSelectionIndex(Object... parameters) {
        for (Object parameter : parameters) {
            int index = selectionIndex(parameter);
            if (index >= 0) {
                return index;
            }
            if (parameter instanceof Iterable<?> values) {
                for (Object value : values) {
                    int nestedIndex = firstSelectionIndex(value);
                    if (nestedIndex >= 0) {
                        return nestedIndex;
                    }
                }
            } else if (parameter instanceof Object[] values) {
                int nestedIndex = firstSelectionIndex(values);
                if (nestedIndex >= 0) {
                    return nestedIndex;
                }
            }
        }
        return -1;
    }

    /// Returns the item index referenced by one parameter.
    private int selectionIndex(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            int index = number.intValue();
            return index >= 0 && index < getItems().size() ? index : -1;
        }
        if (parameter instanceof M3ListViewCell<?> cell) {
            int index = cell.getIndex();
            return index >= 0 && index < getItems().size() ? index : -1;
        }
        return getItems().indexOf(parameter);
    }

    /// Enforces selection invariants for the current selection mode.
    private void enforceSelectionPolicy() {
        if (getSelectionMode() == M3ListSelectionMode.NONE) {
            selectOnly(-1);
            return;
        }

        trimSelectedIndices();
        if (getSelectionMode() == M3ListSelectionMode.SINGLE && selectedIndices.size() > 1) {
            selectOnly(selectedIndices.get(0));
            return;
        }
        if (!isAllowEmptySelection()) {
            selectFirstItemIfNeeded();
        } else {
            refreshSelectedItems();
        }
    }

    /// Selects the first item when selection is empty and empty selection is disabled.
    private void selectFirstItemIfNeeded() {
        if (!selectedIndices.isEmpty() || getItems().isEmpty() || getSelectionMode() == M3ListSelectionMode.NONE) {
            return;
        }
        selectIndex(0);
    }

    /// Selects one index and clears selection from the remaining items.
    private void selectOnly(int index) {
        if (index < 0) {
            selectedIndices.clear();
        } else {
            checkItemIndex(index);
            selectedIndices.setAll(index);
        }
        refreshSelectedItems();
    }

    /// Adds one selected index while preserving ascending order.
    private void addSelectedIndex(int index) {
        if (!selectedIndices.contains(index)) {
            selectedIndices.add(index);
            Collections.sort(selectedIndices);
            refreshSelectedItems();
        }
    }

    /// Removes one selected index.
    private void removeSelectedIndex(int index) {
        if (selectedIndices.remove(Integer.valueOf(index))) {
            refreshSelectedItems();
        }
    }

    /// Removes selected indices outside the current item range.
    private void trimSelectedIndices() {
        int size = getItems().size();
        selectedIndices.removeIf(index -> index < 0 || index >= size);
    }

    /// Clears the focused index when it no longer points at a data item.
    private void trimFocusedIndex() {
        int index = focusedIndex.get();
        if (index >= getItems().size()) {
            updateFocusedIndex(-1, false);
        } else if (index >= 0) {
            focusedItem.set(getItems().get(index));
        }
    }

    /// Refreshes selected item state from selected indices.
    private void refreshSelectedItems() {
        List<T> previousItems = new ArrayList<>(selectedItems);
        int previousIndex = selectedIndex.get();

        selectedItems.clear();
        for (Integer index : selectedIndices) {
            selectedItems.add(getItems().get(index));
        }

        int firstIndex = selectedIndices.isEmpty() ? -1 : selectedIndices.get(0);
        selectedIndex.set(firstIndex);
        selectedItem.set(firstIndex < 0 ? null : getItems().get(firstIndex));
        if (!selectedItems.equals(previousItems) || previousIndex != firstIndex) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            requestVisibleCellRefresh();
        }
    }

    /// Updates the focused data item and asks the skin to keep its cell visible.
    private void updateFocusedIndex(int index, boolean requestNodeFocus) {
        int previousIndex = focusedIndex.get();
        @Nullable T previousItem = focusedItem.get();
        focusedIndex.set(index);
        focusedItem.set(index < 0 ? null : getItems().get(index));
        if (previousIndex != index || !Objects.equals(previousItem, focusedItem.get())) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        }
        if (requestNodeFocus) {
            requestFocus();
        }
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.refreshFocus(requestNodeFocus, isAnimatedScroll());
        }
    }

    /// Returns the current navigation anchor index.
    private int navigationAnchorIndex() {
        int focused = focusedIndex.get();
        if (focused >= 0 && focused < getItems().size()) {
            return focused;
        }
        int selected = getSelectedIndex();
        return selected >= 0 && selected < getItems().size() ? selected : -1;
    }

    /// Returns the first data item index.
    private int firstIndex() {
        return getItems().isEmpty() ? -1 : 0;
    }

    /// Returns the last data item index.
    private int lastIndex() {
        return getItems().size() - 1;
    }

    /// Returns the next data item index, wrapping at the end.
    private int nextIndex(int currentIndex) {
        int size = getItems().size();
        if (size == 0) {
            return -1;
        }
        return currentIndex < 0 || currentIndex + 1 >= size ? 0 : currentIndex + 1;
    }

    /// Returns the previous data item index, wrapping at the start.
    private int previousIndex(int currentIndex) {
        int size = getItems().size();
        if (size == 0) {
            return -1;
        }
        return currentIndex <= 0 ? size - 1 : currentIndex - 1;
    }

    /// Requests visible cell state updates from the installed skin.
    private void requestVisibleCellRefresh() {
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.refreshCells();
        }
    }

    /// Requests a full visible cell rebuild from the installed skin.
    private void requestCellRebuild() {
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.rebuildCells();
        }
    }

    /// Requests item count updates from the installed skin.
    private void requestCellCountRefresh() {
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.refreshItemCount();
        }
    }

    /// Verifies that an index belongs to the backing data list.
    private void checkItemIndex(int index) {
        if (index < 0 || index >= getItems().size()) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + getItems().size());
        }
    }

    /// Validates a data item array.
    private static <T> void validateItems(T[] items) {
        for (T item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
