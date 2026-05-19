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

    /// Updates selection and cells when data items change.
    private final ListChangeListener<T> itemsListener = change -> {
        trimSelectedIndices();
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

    /// Returns whether the supplied item index is selected.
    public final boolean isIndexSelected(int index) {
        return selectedIndices.contains(index);
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

    /// Scrolls the virtual flow to the supplied item index.
    public final void scrollTo(int index) {
        checkItemIndex(index);
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.scrollTo(index);
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
            scrollTo(index);
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
