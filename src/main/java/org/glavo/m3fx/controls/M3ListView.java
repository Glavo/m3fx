// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
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
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ListViewSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    /// The fallback row height used when no fixed cell size or measured cell height is available.
    private static final double DEFAULT_ROW_HEIGHT = 56.0;

    /// The backing data items rendered by this view.
    private final ObservableList<T> items = FXCollections.observableArrayList();

    // The list item factory used by virtualized cells.
    private final ObjectProperty<@Nullable Callback<? super T, ? extends M3ListItem>> cellFactory =
            new SimpleObjectProperty<>(this, "cellFactory") {
                /// Rebuilds visible cells when the factory changes.
                @Override
                protected void invalidated() {
                    requestCellRebuild();
                }
            };

    // The selection mode used by this virtualized list.
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

    // Whether this list view allows all selectable items to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection", true) {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

    // The fixed cell size hint forwarded to the virtual flow.
    private final DoubleProperty fixedCellSize =
            new SimpleDoubleProperty(this, "fixedCellSize", DEFAULT_FIXED_CELL_SIZE) {
                /// Validates updated fixed cell size values.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "fixedCellSize"));
                }
            };

    // Whether focus and programmatic scrolling animate the virtual flow position.
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

    // The first selected index, or `-1` when selection is empty.
    private final ReadOnlyIntegerWrapper selectedIndex = new ReadOnlyIntegerWrapper(this, "selectedIndex", -1);

    // The first selected item, or `null` when selection is empty.
    private final ReadOnlyObjectWrapper<@Nullable T> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    // The keyboard-focused data index, or `-1` when no row has list focus.
    private final ReadOnlyIntegerWrapper focusedIndex = new ReadOnlyIntegerWrapper(this, "focusedIndex", -1);

    // The keyboard-focused data item, or `null` when no row has list focus.
    private final ReadOnlyObjectWrapper<@Nullable T> focusedItem =
            new ReadOnlyObjectWrapper<>(this, "focusedItem");

    /// The current printable-key prefix used for list view type-ahead focus navigation.
    private final StringBuilder typeAheadBuffer = new StringBuilder();

    /// Clears the type-ahead prefix after the user stops typing.
    private final PauseTransition typeAheadResetDelay = new PauseTransition();

    /// The row index for a deferred explicit accessibility reveal request.
    private int pendingAccessibleRevealIndex = -1;

    /// The parameters for a deferred explicit accessibility reveal request.
    private Object @Nullable [] pendingAccessibleRevealParameters;

    /// The remaining next-pulse retries for a deferred accessibility reveal request.
    private int pendingAccessibleRevealRetries;

    /// Whether a deferred accessibility reveal retry is already queued.
    private boolean pendingAccessibleRevealScheduled;

    /// Updates type-ahead timing when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// Updates selection and focus state when a node data item or its ancestor reachability changes.
    private final InvalidationListener itemReachabilityListener = observable -> refreshDataItemReachabilityState();

    /// Nodes in current node data item ancestry chains observed for navigation reachability changes.
    private final Set<Node> observedItemReachabilityNodes = Collections.newSetFromMap(new IdentityHashMap<>());

    /// Updates selection and cells when data items change.
    private final ListChangeListener<T> itemsListener = change -> {
        refreshDataItemReachabilityState();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        requestCellCountRefresh();
    };

    /// Creates an empty virtualized list view.
    public M3ListView() {
        initialize();
    }

    /// Creates a virtualized list view containing the supplied data items.
    ///
    /// @param items the initial non-null data items
    @SafeVarargs
    public M3ListView(T... items) {
        initialize();
        addItems(items);
    }

    /// Returns the mutable backing data list.
    ///
    /// @return the mutable observable data list rendered by this view
    public final ObservableList<T> getItems() {
        return items;
    }

    /// Adds one data item.
    ///
    /// @param item the non-null data item to append
    public final void addItem(T item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds data items.
    ///
    /// @param items the non-null data items to append
    @SafeVarargs
    public final void addItems(T... items) {
        Objects.requireNonNull(items, "items");
        validateItems(items);
        Collections.addAll(getItems(), items);
    }

    /// Replaces all data items.
    ///
    /// @param items the non-null data items that replace the current list content
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
    ///
    /// @return the cell factory, or `null` to use the default string-based item
    public final @Nullable Callback<? super T, ? extends M3ListItem> getCellFactory() {
        return cellFactory.get();
    }

    /// Sets the factory used to create a list item for one data item.
    ///
    /// @param cellFactory the cell factory, or `null` to use the default string-based item
    public final void setCellFactory(@Nullable Callback<? super T, ? extends M3ListItem> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    /// Returns the list item factory property.
    ///
    /// @return the writable cell factory property
    public final ObjectProperty<@Nullable Callback<? super T, ? extends M3ListItem>> cellFactoryProperty() {
        return cellFactory;
    }

    /// Returns the list item selection mode.
    ///
    /// @return the active selection mode
    public final M3ListSelectionMode getSelectionMode() {
        return Objects.requireNonNull(selectionMode.get(), "selectionMode");
    }

    /// Sets the list item selection mode.
    ///
    /// @param selectionMode the active selection mode
    public final void setSelectionMode(M3ListSelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the list item selection mode property.
    ///
    /// @return the writable selection mode property
    public final ObjectProperty<@Nullable M3ListSelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns whether this list view allows all selectable items to be unselected.
    ///
    /// @return `true` when empty selection is allowed
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this list view allows all selectable items to be unselected.
    ///
    /// @param allowEmptySelection whether empty selection is allowed
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    ///
    /// @return the writable empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Returns the fixed cell size hint used by the virtual flow.
    ///
    /// @return the fixed cell size hint in pixels, or `0` when variable cell heights are allowed
    public final double getFixedCellSize() {
        return fixedCellSize.get();
    }

    /// Sets the fixed cell size hint used by the virtual flow.
    ///
    /// @param fixedCellSize the fixed cell size in pixels, or `0` to allow variable cell heights
    public final void setFixedCellSize(double fixedCellSize) {
        this.fixedCellSize.set(M3Css.nonNegative(fixedCellSize, "fixedCellSize"));
    }

    /// Returns the fixed cell size property.
    ///
    /// @return the writable fixed cell size property
    public final DoubleProperty fixedCellSizeProperty() {
        return fixedCellSize;
    }

    /// Returns whether focus and programmatic scrolling animate the virtual flow position.
    ///
    /// @return `true` when focus and programmatic scrolling should animate
    public final boolean isAnimatedScroll() {
        return animatedScroll.get();
    }

    /// Sets whether focus and programmatic scrolling animate the virtual flow position.
    ///
    /// @param animatedScroll whether focus and programmatic scrolling should animate
    public final void setAnimatedScroll(boolean animatedScroll) {
        this.animatedScroll.set(animatedScroll);
    }

    /// Returns the animated virtual flow scrolling property.
    ///
    /// @return the writable animated scrolling property
    public final BooleanProperty animatedScrollProperty() {
        return animatedScroll;
    }

    /// Returns the selected indices in ascending order.
    ///
    /// @return an unmodifiable observable view of selected item indices
    public final @UnmodifiableView ObservableList<Integer> getSelectedIndices() {
        return selectedIndicesView;
    }

    /// Returns the selected item values in ascending index order.
    ///
    /// @return an unmodifiable observable view of selected item values
    public final @UnmodifiableView ObservableList<T> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the first selected index, or `-1` when selection is empty.
    ///
    /// @return the first selected index, or `-1` when selection is empty
    public final int getSelectedIndex() {
        return selectedIndex.get();
    }

    /// Returns the first selected index property.
    ///
    /// @return the read-only first selected index property
    public final ReadOnlyIntegerProperty selectedIndexProperty() {
        return selectedIndex.getReadOnlyProperty();
    }

    /// Returns the first selected item, or `null` when selection is empty.
    ///
    /// @return the first selected item, or `null` when selection is empty
    public final @Nullable T getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the first selected item property.
    ///
    /// @return the read-only first selected item property
    public final ReadOnlyObjectProperty<@Nullable T> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the keyboard-focused data index, or `-1` when no row has list focus.
    ///
    /// @return the keyboard-focused data index, or `-1` when no row has list focus
    public final int getFocusedIndex() {
        return focusedIndex.get();
    }

    /// Returns the keyboard-focused data index property.
    ///
    /// @return the read-only keyboard-focused data index property
    public final ReadOnlyIntegerProperty focusedIndexProperty() {
        return focusedIndex.getReadOnlyProperty();
    }

    /// Returns the keyboard-focused data item, or `null` when no row has list focus.
    ///
    /// @return the keyboard-focused data item, or `null` when no row has list focus
    public final @Nullable T getFocusedItem() {
        return focusedItem.get();
    }

    /// Returns the keyboard-focused data item property.
    ///
    /// @return the read-only keyboard-focused data item property
    public final ReadOnlyObjectProperty<@Nullable T> focusedItemProperty() {
        return focusedItem.getReadOnlyProperty();
    }

    /// Returns whether the supplied item index is selected.
    ///
    /// @param index the data item index to query
    /// @return `true` when the item index is selected
    public final boolean isIndexSelected(int index) {
        return selectedIndices.contains(index);
    }

    /// Returns whether the supplied item index owns list keyboard focus.
    ///
    /// @param index the data item index to query
    /// @return `true` when the item index owns list keyboard focus
    public final boolean isIndexFocused(int index) {
        return focusedIndex.get() == index;
    }

    /// Selects the reachable item at the supplied index.
    ///
    /// @param index the data item index to select
    public final void selectIndex(int index) {
        checkItemIndex(index);
        if (!isIndexNavigable(index)) {
            return;
        }
        if (getSelectionMode() == M3ListSelectionMode.MULTIPLE) {
            setIndexSelected(index, true);
        } else {
            selectOnly(index);
        }
    }

    /// Selects the first item equal to the supplied value.
    ///
    /// @param item the non-null data item to select
    public final void selectItem(T item) {
        Objects.requireNonNull(item, "item");
        int index = getItems().indexOf(item);
        if (index < 0) {
            throw new IllegalArgumentException("item must belong to this list view");
        }
        selectIndex(index);
    }

    /// Sets one reachable index's selected state.
    ///
    /// @param index the data item index to update
    /// @param selected whether the index should be selected
    public final void setIndexSelected(int index, boolean selected) {
        checkItemIndex(index);
        if (selected && !isIndexNavigable(index)) {
            return;
        }
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

    /// Toggles the selected state for one reachable item index.
    ///
    /// @param index the data item index to toggle
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
    ///
    /// @param index the data item index to clear from selection
    public final void clearSelection(int index) {
        checkItemIndex(index);
        setIndexSelected(index, false);
    }

    /// Selects the first enabled visible data item when one exists.
    public final void selectFirst() {
        int target = firstIndex();
        if (target >= 0) {
            selectIndex(target);
        }
    }

    /// Selects the last enabled visible data item when one exists.
    public final void selectLast() {
        int target = lastIndex();
        if (target >= 0) {
            selectIndex(target);
        }
    }

    /// Selects the next enabled visible item after the first selected item, wrapping at the end.
    public final void selectNext() {
        int target = nextIndex(getSelectedIndex());
        if (target >= 0) {
            selectIndex(target);
        }
    }

    /// Selects the previous enabled visible item before the first selected item, wrapping at the start.
    public final void selectPrevious() {
        int target = previousIndex(getSelectedIndex());
        if (target >= 0) {
            selectIndex(target);
        }
    }

    /// Moves list keyboard focus to the supplied enabled visible item index and scrolls it into view.
    ///
    /// Disabled or invisible node items are not reachable by keyboard traversal, so requests for those rows are ignored.
    ///
    /// @param index the data item index to focus
    public final void focusIndex(int index) {
        checkItemIndex(index);
        if (!isIndexNavigable(index)) {
            return;
        }
        updateFocusedIndex(index, true);
    }

    /// Clears list keyboard focus without changing selection.
    public final void clearFocus() {
        updateFocusedIndex(-1, false);
    }

    /// Moves list keyboard focus to the first enabled visible data item when one exists.
    public final void focusFirst() {
        int target = firstIndex();
        if (target >= 0) {
            focusIndex(target);
        }
    }

    /// Moves list keyboard focus to the last enabled visible data item when one exists.
    public final void focusLast() {
        int target = lastIndex();
        if (target >= 0) {
            focusIndex(target);
        }
    }

    /// Moves list keyboard focus to the next enabled visible item, wrapping at the end.
    public final void focusNext() {
        int target = nextIndex(navigationAnchorIndex());
        if (target >= 0) {
            focusIndex(target);
        }
    }

    /// Moves list keyboard focus to the previous enabled visible item, wrapping at the start.
    public final void focusPrevious() {
        int target = previousIndex(navigationAnchorIndex());
        if (target >= 0) {
            focusIndex(target);
        }
    }

    /// Scrolls the virtual flow to the supplied item index using the configured animation policy.
    ///
    /// @param index the data item index to reveal
    public final void scrollTo(int index) {
        scrollTo(index, isAnimatedScroll());
    }

    /// Scrolls the virtual flow to the supplied item index.
    ///
    /// @param index the data item index to reveal
    /// @param animated whether the scroll should animate when animations are enabled
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
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
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
    ///
    /// @param action the accessibility action to execute
    /// @param parameters optional action-specific parameters
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
    ///
    /// @return the default virtualized list skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ListViewSkin<>(this);
    }

    /// Adds base style classes and installs data listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        M3PopupStyles.addFallbackRootStyleClass(this);
        M3PopupStyles.addStylesheet(this, M3Stylesheets.fallbackStylesheet());
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        addEventHandler(KeyEvent.KEY_TYPED, this::handleTypeAheadKeyTyped);
        getItems().addListener(itemsListener);
        updateItemReachabilityObservers();
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                clearTypeAheadBuffer();
            }
        });
        typeAheadResetDelay.setOnFinished(event -> clearTypeAheadBuffer());
    }

    /// Applies changed runtime motion settings to the type-ahead reset delay.
    private void refreshMotionSettings() {
        M3Animation.updatePauseDuration(
                typeAheadResetDelay,
                M3Animation.motionBehavior(this).typeAheadResetDelay(),
                typeAheadBuffer.length() > 0
        );
    }

    /// Refreshes listeners for node data items whose visibility or disabled state affects navigation.
    private void updateItemReachabilityObservers() {
        removeItemReachabilityObservers();
        for (T item : getItems()) {
            if (item instanceof Node node) {
                observeItemReachabilityChain(node);
            }
        }
    }

    /// Observes one node data item and its current parent chain.
    private void observeItemReachabilityChain(Node node) {
        @Nullable Node current = node;
        while (current != null) {
            if (observedItemReachabilityNodes.add(current)) {
                current.visibleProperty().addListener(itemReachabilityListener);
                current.disabledProperty().addListener(itemReachabilityListener);
                current.parentProperty().addListener(itemReachabilityListener);
            }
            current = current.getParent();
        }
    }

    /// Removes all node data item reachability listeners.
    private void removeItemReachabilityObservers() {
        for (Node node : observedItemReachabilityNodes) {
            node.visibleProperty().removeListener(itemReachabilityListener);
            node.disabledProperty().removeListener(itemReachabilityListener);
            node.parentProperty().removeListener(itemReachabilityListener);
        }
        observedItemReachabilityNodes.clear();
    }

    /// Refreshes selection, focus, and visible rows after node data item reachability changes.
    private void refreshDataItemReachabilityState() {
        clearTypeAheadBuffer();
        updateItemReachabilityObservers();
        trimSelectedIndices();
        trimFocusedIndex();
        if (!isAllowEmptySelection() && getSelectionMode() != M3ListSelectionMode.NONE && selectedIndices.isEmpty()) {
            selectFirstItemIfNeeded();
        } else {
            refreshSelectedItems();
        }
        M3Accessible.notifyFocusNodeChanged(this);
        requestVisibleCellRefresh();
    }

    /// Applies the configured selection policy to a reachable cell activation.
    void activateIndex(int index) {
        if (!isIndexNavigable(index)) {
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
        if (M3FocusTraversal.consumeNavigationKeyIfFocusOwnerInsideTextInput(this, event, false, true)) {
            return;
        }

        KeyCode code = event.getCode();
        switch (code) {
            case UP -> moveKeyboardFocus(previousIndex(navigationAnchorIndex()), event);
            case DOWN -> moveKeyboardFocus(nextIndex(navigationAnchorIndex()), event);
            case HOME -> moveKeyboardFocus(firstIndex(), event);
            case END -> moveKeyboardFocus(lastIndex(), event);
            case PAGE_UP -> moveKeyboardFocus(pageIndex(navigationAnchorIndex(), false), event);
            case PAGE_DOWN -> moveKeyboardFocus(pageIndex(navigationAnchorIndex(), true), event);
            case ENTER, SPACE -> activateFocusedIndex(event);
            default -> {
            }
        }
    }

    /// Moves focus to the next data item whose text matches the printable-key search prefix.
    private void handleTypeAheadKeyTyped(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        if (M3FocusTraversal.focusOwnerInsideTextInput(this)) {
            return;
        }

        if (event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShortcutDown()) {
            return;
        }

        String character = event.getCharacter();
        if (character.length() != 1 || Character.isISOControl(character.charAt(0)) || character.isBlank()) {
            return;
        }

        String normalizedCharacter = M3SelectionNavigation.normalizeTypeAheadText(character);
        typeAheadBuffer.append(normalizedCharacter);
        typeAheadResetDelay.setDuration(M3Animation.motionBehavior(this).typeAheadResetDelay());
        typeAheadResetDelay.playFromStart();
        int target = typeAheadTarget(typeAheadBuffer.toString());
        if (target < 0 && typeAheadBuffer.length() > 1) {
            clearTypeAheadBuffer();
            typeAheadBuffer.append(normalizedCharacter);
            target = typeAheadTarget(typeAheadBuffer.toString());
        }
        if (target < 0) {
            return;
        }

        updateFocusedIndex(target, true);
        if (getSelectionMode() == M3ListSelectionMode.SINGLE) {
            selectOnly(target);
        }
        event.consume();
    }

    /// Clears buffered type-ahead text and stops the pending reset timer.
    private void clearTypeAheadBuffer() {
        typeAheadBuffer.setLength(0);
        typeAheadResetDelay.stop();
    }

    /// Returns the next data item index matching the normalized type-ahead prefix.
    private int typeAheadTarget(String prefix) {
        if (prefix.isEmpty() || getItems().isEmpty()) {
            return -1;
        }

        int itemCount = getItems().size();
        int anchor = navigationAnchorIndex();
        for (int offset = 1; offset <= itemCount; offset++) {
            int index = Math.floorMod(anchor + offset, itemCount);
            T item = getItems().get(index);
            if (isItemNavigable(item)
                    && M3SelectionNavigation.normalizeTypeAheadText(typeAheadText(item)).startsWith(prefix)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the text used for one data item's type-ahead matching.
    private String typeAheadText(T item) {
        return item instanceof M3ListItem listItem ? listItem.getHeadlineText() : String.valueOf(item);
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
        if (!isIndexNavigable(index)) {
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
        @Nullable Node externalFocus = currentVisibleExternalFocusNode();
        if (externalFocus != null) {
            return externalFocus;
        }

        @Nullable Node currentFocus = currentVisibleFocusNode();
        if (currentFocus != null) {
            return currentFocus;
        }

        int index = focusedIndex.get();
        if (!isIndexNavigable(index)) {
            index = getSelectedIndex();
        }
        if (!isIndexNavigable(index)) {
            index = firstIndex();
        }
        if (index >= 0 && getSkin() instanceof M3ListViewSkin<?> skin) {
            @Nullable Node visibleItem = skin.getAttachedVisibleItem(index);
            if (visibleItem != null) {
                @Nullable Node focusOwner = sceneFocusOwner();
                if (focusOwner != null
                        && focusOwner != this
                        && M3Accessible.canReach(focusOwner)
                        && M3Accessible.containsNode(visibleItem, focusOwner)) {
                    return focusOwner;
                }
                return visibleItem;
            }
            visibleItem = skin.getVisibleItem(index);
            if (visibleItem != null) {
                return visibleItem;
            }
        }
        return this;
    }

    /// Moves focus to an active row-owned popup target, focused row, selected row, or first row for accessibility clients.
    ///
    /// @return `true` when a focus target was found and focus was requested
    final boolean focusAccessibleNode() {
        @Nullable Node externalFocus = currentVisibleExternalFocusNode();
        if (externalFocus != null && M3Accessible.showItem(this, externalFocus)) {
            return true;
        }

        @Nullable Node currentFocus = currentVisibleFocusNode();
        if (currentFocus != null && M3Accessible.showItem(this, currentFocus)) {
            return true;
        }

        int index = focusedIndex.get();
        if (index < 0) {
            index = getSelectedIndex();
        }
        if (!isIndexNavigable(index)) {
            index = firstIndex();
        }
        if (index >= 0) {
            focusIndex(index);
            return true;
        }
        return M3Accessible.showDirectItem(this, this);
    }

    /// Returns an active external popup focus node exposed by an attached visible row.
    private @Nullable Node currentVisibleExternalFocusNode() {
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            @Nullable Node visibleItem = skin.findAttachedVisibleItem(
                    item -> M3Accessible.activeExternalFocusTarget(this, item) != null
            );
            return visibleItem == null ? null : M3Accessible.activeExternalFocusTarget(this, visibleItem);
        }
        return null;
    }

    /// Returns the current scene focus owner when it belongs to an attached visible row.
    private @Nullable Node currentVisibleFocusNode() {
        @Nullable Node focusOwner = sceneFocusOwner();
        if (focusOwner == null || focusOwner == this || !M3Accessible.canReach(focusOwner)) {
            return null;
        }
        return visibleNodeIndex(focusOwner) >= 0 ? focusOwner : null;
    }

    /// Returns the current scene focus owner, or `null` when this list is not attached.
    private @Nullable Node sceneFocusOwner() {
        @Nullable Scene scene = getScene();
        return scene == null ? null : scene.getFocusOwner();
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
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when a row target was focused or a nested reveal request was accepted
    final boolean showAccessibleItem(Object... parameters) {
        if (parameters.length == 0) {
            return focusAccessibleNode();
        }

        @Nullable Node requestedNode = firstNodeParameter(parameters);
        if (requestedNode != null) {
            int requestedIndex = selectionIndex(requestedNode);
            if (requestedIndex >= 0) {
                if (visibleNodeIndex(requestedNode) == requestedIndex) {
                    updateFocusedIndexForAttachedNode(requestedIndex);
                    return M3Accessible.showItem(this, requestedNode);
                }
                int previousFocusedIndex = getFocusedIndex();
                focusAccessibleIndex(requestedIndex);
                boolean shown = showMaterializedOrDeferAccessibleActionTarget(requestedIndex, parameters);
                if (!shown) {
                    updateFocusedIndex(previousFocusedIndex, false, false);
                }
                return shown;
            }
        }

        int index = firstSelectionIndex(parameters);
        if (index >= 0) {
            int previousFocusedIndex = getFocusedIndex();
            focusAccessibleIndex(index);
            boolean shown = showMaterializedOrDeferAccessibleActionTarget(index, parameters);
            if (!shown) {
                updateFocusedIndex(previousFocusedIndex, false, false);
            }
            return shown;
        }

        return showVisibleAccessibleActionTarget(parameters);
    }

    /// Moves accessibility focus to one row using synchronous scrolling so explicit nested targets are materialized.
    private void focusAccessibleIndex(int index) {
        updateFocusedIndex(index, true, false);
    }

    /// Delegates an explicit reveal request to an attached visible row that exposes the target.
    private boolean showVisibleAccessibleActionTarget(Object... parameters) {
        if (!(getSkin() instanceof M3ListViewSkin<?> skin)) {
            return false;
        }

        @Nullable Node visibleItem = skin.findAttachedVisibleItem(
                item -> M3Accessible.containsAccessibleActionTarget(item, parameters)
        );
        if (visibleItem == null || !M3Accessible.showAccessibleActionTarget(this, visibleItem, parameters)) {
            return false;
        }

        int visibleIndex = visibleNodeIndex(visibleItem);
        if (visibleIndex >= 0) {
            updateFocusedIndexForAttachedNode(visibleIndex);
        }
        return true;
    }

    /// Delegates an explicit reveal request now, or retries after the virtualized row is attached.
    ///
    /// @return `true` when the row target was handled immediately or queued for deferred reveal
    private boolean showMaterializedOrDeferAccessibleActionTarget(int index, Object... parameters) {
        Object[] targetParameters = nestedRevealParameters(index, parameters);
        if (targetParameters.length == 0) {
            return true;
        }
        if (showMaterializedAccessibleActionTarget(index, targetParameters)
                || !hasNonIndexRevealParameter(targetParameters)) {
            return true;
        }

        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.scrollTo(index, false);
            applyCss();
            layout();
            if (showMaterializedAccessibleActionTarget(index, targetParameters)) {
                return true;
            }
            if (skin.getAttachedVisibleItem(index) != null) {
                return false;
            }
        }

        pendingAccessibleRevealIndex = index;
        pendingAccessibleRevealParameters = targetParameters.clone();
        pendingAccessibleRevealRetries = 8;
        completePendingAccessibleReveal();
        return true;
    }

    /// Returns reveal parameters with one explicit row selector removed.
    private Object[] nestedRevealParameters(int index, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length <= 1 || !isRowSelector(index, parameters[0])) {
            return parameters;
        }
        Object[] nestedParameters = new Object[parameters.length - 1];
        System.arraycopy(parameters, 1, nestedParameters, 0, nestedParameters.length);
        return nestedParameters;
    }

    /// Returns whether a parameter identifies the row itself rather than a nested row target.
    private boolean isRowSelector(int index, @Nullable Object parameter) {
        if (parameter instanceof Number || parameter instanceof M3ListViewCell<?>) {
            return selectionIndex(parameter) == index;
        }
        T item = getItems().get(index);
        if (Objects.equals(item, parameter)) {
            return true;
        }
        if (parameter instanceof Node node) {
            if (item == node) {
                return true;
            }
            if (getSkin() instanceof M3ListViewSkin<?> skin) {
                return skin.getAttachedVisibleItem(index) == node;
            }
        }
        return false;
    }

    /// Delegates an explicit reveal request after a virtualized row has been synchronously materialized.
    private boolean showMaterializedAccessibleActionTarget(int index, Object... parameters) {
        if (parameters.length == 0 || !(getSkin() instanceof M3ListViewSkin<?> skin)) {
            return false;
        }

        @Nullable Node visibleItem = skin.getAttachedVisibleItem(index);
        if (visibleItem == null || !M3Accessible.showAccessibleActionTarget(this, visibleItem, parameters)) {
            return false;
        }

        updateFocusedIndexForAttachedNode(index);
        return true;
    }

    /// Queues a next-pulse retry for a deferred explicit accessibility reveal request.
    private void schedulePendingAccessibleReveal() {
        if (pendingAccessibleRevealScheduled) {
            return;
        }

        pendingAccessibleRevealScheduled = true;
        Platform.runLater(() -> {
            pendingAccessibleRevealScheduled = false;
            completePendingAccessibleReveal();
        });
    }

    /// Completes a deferred explicit accessibility reveal request when its virtualized row is attached.
    private void completePendingAccessibleReveal() {
        Object @Nullable [] parameters = pendingAccessibleRevealParameters;
        int index = pendingAccessibleRevealIndex;
        if (parameters == null || index < 0 || getScene() == null) {
            clearPendingAccessibleReveal();
            return;
        }

        if (showMaterializedAccessibleActionTarget(index, parameters)) {
            clearPendingAccessibleReveal();
            return;
        }

        if (pendingAccessibleRevealRetries <= 0) {
            clearPendingAccessibleReveal();
            return;
        }

        pendingAccessibleRevealRetries--;
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.scrollTo(index, false);
        }
        applyCss();
        layout();
        if (showMaterializedAccessibleActionTarget(index, parameters)) {
            clearPendingAccessibleReveal();
            return;
        }
        schedulePendingAccessibleReveal();
    }

    /// Clears the deferred explicit accessibility reveal request.
    private void clearPendingAccessibleReveal() {
        pendingAccessibleRevealIndex = -1;
        pendingAccessibleRevealParameters = null;
        pendingAccessibleRevealRetries = 0;
        pendingAccessibleRevealScheduled = false;
    }

    /// Returns whether accessibility parameters include a nested target beyond a row index.
    private boolean hasNonIndexRevealParameter(Object... parameters) {
        for (Object parameter : parameters) {
            if (hasNonIndexRevealParameter(parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one accessibility parameter is not only a row index.
    private boolean hasNonIndexRevealParameter(@Nullable Object parameter) {
        if (parameter instanceof Number) {
            return false;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (hasNonIndexRevealParameter(value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (hasNonIndexRevealParameter(value)) {
                    return true;
                }
            }
            return false;
        }
        return parameter != null;
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
            return isIndexNavigable(index) ? index : -1;
        }
        if (parameter instanceof M3ListViewCell<?> cell) {
            int index = cell.getIndex();
            return isIndexNavigable(index) ? index : -1;
        }
        if (parameter instanceof Node node) {
            if (!M3Accessible.isEffectivelyReachable(node)) {
                return -1;
            }
            int visibleIndex = visibleNodeIndex(node);
            if (visibleIndex >= 0) {
                return visibleIndex;
            }
            int dataNodeIndex = dataNodeIndex(node);
            if (dataNodeIndex >= 0) {
                return dataNodeIndex;
            }
        }
        return dataValueIndex(parameter);
    }

    /// Returns the data index for an attached visible row node or descendant.
    private int visibleNodeIndex(Node node) {
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            int index = skin.getAttachedVisibleItemIndex(node);
            return isIndexNavigable(index) ? index : -1;
        }
        return -1;
    }

    /// Returns the data index for a data item node or descendant.
    private int dataNodeIndex(Node node) {
        for (int index = 0; index < getItems().size(); index++) {
            T item = getItems().get(index);
            if (isIndexNavigable(index)
                    && item instanceof Node itemNode
                    && M3Accessible.containsSelectionTarget(itemNode, node)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the data index for a parameter equal to one backing data item.
    private int dataValueIndex(@Nullable Object parameter) {
        for (int index = 0; index < getItems().size(); index++) {
            if (isIndexNavigable(index) && Objects.equals(getItems().get(index), parameter)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the first node supplied directly or inside nested accessibility parameters.
    private @Nullable Node firstNodeParameter(Object... parameters) {
        for (Object parameter : parameters) {
            @Nullable Node node = firstNodeParameter(parameter);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    /// Returns the first node supplied by one accessibility parameter.
    private @Nullable Node firstNodeParameter(@Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return node;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node node = firstNodeParameter(value);
                if (node != null) {
                    return node;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            return firstNodeParameter(values);
        }
        return null;
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
        int firstIndex = firstIndex();
        if (firstIndex >= 0) {
            selectIndex(firstIndex);
        }
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

    /// Removes selected indices outside the current item range or no longer reachable by row navigation.
    private void trimSelectedIndices() {
        int size = getItems().size();
        selectedIndices.removeIf(index -> index < 0 || index >= size || !isIndexNavigable(index));
    }

    /// Clears the focused index when it no longer points at a reachable data item.
    private void trimFocusedIndex() {
        int index = focusedIndex.get();
        if (index >= getItems().size()) {
            updateFocusedIndex(-1, false);
        } else if (index >= 0 && !isIndexNavigable(index)) {
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
        updateFocusedIndex(index, requestNodeFocus, isAnimatedScroll());
    }

    /// Updates the focused data item and asks the skin to keep its cell visible.
    private void updateFocusedIndex(int index, boolean requestNodeFocus, boolean animated) {
        int previousIndex = focusedIndex.get();
        @Nullable T previousItem = focusedItem.get();
        focusedIndex.set(index);
        focusedItem.set(index < 0 ? null : getItems().get(index));
        if (previousIndex != index || !Objects.equals(previousItem, focusedItem.get())) {
            M3Accessible.notifyFocusNodeChanged(this);
        }
        if (requestNodeFocus) {
            M3Accessible.showDirectItem(this, this);
        }
        if (getSkin() instanceof M3ListViewSkin<?> skin) {
            skin.refreshFocus(requestNodeFocus, animated);
        }
    }

    /// Updates logical list focus for an already attached row without scrolling or stealing child focus.
    private void updateFocusedIndexForAttachedNode(int index) {
        int previousIndex = focusedIndex.get();
        @Nullable T previousItem = focusedItem.get();
        focusedIndex.set(index);
        focusedItem.set(index < 0 ? null : getItems().get(index));
        if (previousIndex != index || !Objects.equals(previousItem, focusedItem.get())) {
            M3Accessible.notifyFocusNodeChanged(this);
        }
    }

    /// Returns the current navigation anchor index.
    private int navigationAnchorIndex() {
        int focused = focusedIndex.get();
        if (isIndexNavigable(focused)) {
            return focused;
        }
        int selected = getSelectedIndex();
        return isIndexNavigable(selected) ? selected : -1;
    }

    /// Returns the first enabled visible data item index.
    private int firstIndex() {
        int size = getItems().size();
        for (int index = 0; index < size; index++) {
            if (isIndexNavigable(index)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the last enabled visible data item index.
    private int lastIndex() {
        for (int index = getItems().size() - 1; index >= 0; index--) {
            if (isIndexNavigable(index)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the next enabled visible data item index, wrapping at the end.
    private int nextIndex(int currentIndex) {
        int size = getItems().size();
        if (size == 0) {
            return -1;
        }

        for (int offset = 1; offset <= size; offset++) {
            int index = Math.floorMod(currentIndex + offset, size);
            if (isIndexNavigable(index)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the previous enabled visible data item index, wrapping at the start.
    private int previousIndex(int currentIndex) {
        int size = getItems().size();
        if (size == 0) {
            return -1;
        }

        int anchor = currentIndex < 0 ? size : currentIndex;
        for (int offset = 1; offset <= size; offset++) {
            int index = Math.floorMod(anchor - offset, size);
            if (isIndexNavigable(index)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the index reached by one page-navigation operation.
    private int pageIndex(int currentIndex, boolean forward) {
        if (getItems().isEmpty()) {
            return -1;
        }
        if (!isIndexNavigable(currentIndex)) {
            return forward ? firstIndex() : lastIndex();
        }

        int target = currentIndex;
        int step = pageStep();
        for (int offset = 0; offset < step; offset++) {
            int next = forward ? nextIndexWithoutWrap(target) : previousIndexWithoutWrap(target);
            if (next < 0) {
                return target;
            }
            target = next;
        }
        return target;
    }

    /// Returns the next enabled visible index after the current index without wrapping.
    private int nextIndexWithoutWrap(int currentIndex) {
        for (int index = Math.max(0, currentIndex + 1); index < getItems().size(); index++) {
            if (isIndexNavigable(index)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the previous enabled visible index before the current index without wrapping.
    private int previousIndexWithoutWrap(int currentIndex) {
        for (int index = Math.min(currentIndex - 1, getItems().size() - 1); index >= 0; index--) {
            if (isIndexNavigable(index)) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the number of rows used by page-up and page-down keyboard navigation.
    private int pageStep() {
        double rowHeight = getFixedCellSize() > 0.0 ? getFixedCellSize() : DEFAULT_ROW_HEIGHT;
        double viewportHeight = getHeight();
        if (viewportHeight <= 0.0) {
            viewportHeight = prefHeight(getWidth());
        }
        return Math.max(1, (int) Math.floor(viewportHeight / rowHeight));
    }

    /// Returns whether a data item index can receive list keyboard navigation.
    private boolean isIndexNavigable(int index) {
        return index >= 0 && index < getItems().size() && isItemNavigable(getItems().get(index));
    }

    /// Returns whether a data item can receive list keyboard navigation.
    private boolean isItemNavigable(T item) {
        return M3Accessible.isEffectivelyReachable(this)
                && (!(item instanceof Node node) || M3Accessible.isEffectivelyReachable(node));
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
