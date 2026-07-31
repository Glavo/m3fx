// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3NavigationBarSkin;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 navigation bar for top-level destinations in compact layouts.
///
/// `M3NavigationBar` presents an ordered set of top-level destinations and maintains single selection across its
/// [M3NavigationItem] children. The bar is intended for three to five destinations at the bottom edge of a compact
/// or medium-width application layout. Arrow keys move between reachable items and activation selects the item
/// before its action event continues through the event dispatch chain.
///
/// [#getItems()] is a live mutable list. The default bar uses vertical item layout, does not add spacing between
/// targets, and requires one selected destination whenever a visible, directly enabled item exists.
/// [#getSelectedItems()] is a live, unmodifiable observable view containing zero or one item. Item nodes are owned by
/// the bar while displayed and must not belong to another parent.
///
/// Selection is retained when the bar or one of its ancestors is hidden or disabled. This allows a navigation bar
/// and rail to mirror the same route while an adaptive scaffold presents only one of them.
///
/// ```java
/// M3NavigationBar navigationBar = new M3NavigationBar();
/// M3NavigationItem homeItem = new M3NavigationItem("Home");
/// navigationBar.getItems().addAll(homeItem, new M3NavigationItem("Settings"));
/// navigationBar.select(homeItem);
/// ```
///
/// See [Material Design navigation bars](https://m3.material.io/components/navigation-bar/overview).
@NotNullByDefault
public final class M3NavigationBar extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-navigation-bar";

    /// The style class used by compact vertical-item navigation bars.
    private static final String VERTICAL_STYLE_CLASS = "m3-navigation-bar-vertical";

    /// The style class used by medium-window horizontal-item navigation bars.
    private static final String HORIZONTAL_STYLE_CLASS = "m3-navigation-bar-horizontal";

    /// The default spacing between navigation bar item target areas.
    private static final double DEFAULT_ITEM_SPACING = 0.0;

    /// The live, mutable, ordered destination list.
    ///
    /// The list rejects `null` elements and reports mutations through the `ObservableList` change API. Removing
    /// an item clears its selected state. A navigation item may occur only once because a JavaFX node can occupy
    /// only one position in a parent.
    private final ObservableList<M3NavigationItem> items =
            M3ObservableLists.identityDistinctElementList("item");

    /// Notifies accessibility clients when focus moves between navigation items.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(
                            this,
                            getItems(),
                            getSelectedItem(),
                            M3NavigationItem.class
                    ));

    /// The selected navigation items in child order.
    private final ObservableList<M3NavigationItem> selectedItems = M3ObservableLists.nonNullElementList("selectedItem");

    /// The read-only selected navigation item view.
    private final @UnmodifiableView ObservableList<M3NavigationItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// Reusable storage for computing selected items without allocating on every refresh.
    private final List<M3NavigationItem> selectedItemsScratch = new ArrayList<>();

    /// Handles selected-state invalidation for every installed navigation item.
    private final InvalidationListener selectedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3NavigationItem item) {
            handleItemSelectedChanged(item, item.isSelected());
        }
    };

    /// Handles reachability invalidation for every installed navigation item.
    private final InvalidationListener reachabilityInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3NavigationItem item) {
            handleItemReachabilityChanged(item);
        }
    };

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
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    };

    /// Whether the navigation bar is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty vertical navigation bar that requires selection when a visible, directly enabled item is added.
    public M3NavigationBar() {
        initialize();
    }

    /// The icon and label arrangement propagated to every item in the bar.
    ///
    /// A direct assignment of `null` is replaced with [M3NavigationItemLayout#VERTICAL].
    ///
    /// @defaultValue [M3NavigationItemLayout#VERTICAL]
    private final ObjectProperty<M3NavigationItemLayout> itemLayoutState =
            new SimpleObjectProperty<>(this, "itemLayout", M3NavigationItemLayout.VERTICAL) {
                /// Propagates layout changes to the installed navigation items.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3NavigationItemLayout.VERTICAL);
                        return;
                    }
                    updateItemLayoutStyle();
                    updateItemLayouts();
                    requestLayout();
                }
            };

    /// Returns how child navigation items arrange their icon and label.
    ///
    /// @return the navigation item layout
    public final M3NavigationItemLayout getItemLayout() {
        return itemLayoutState.get();
    }

    /// Sets how child navigation items arrange their icon and label.
    ///
    /// Use [M3NavigationItemLayout#VERTICAL] in compact windows and
    /// [M3NavigationItemLayout#HORIZONTAL] in medium windows.
    ///
    /// @param itemLayout the navigation item layout
    /// @throws NullPointerException if `itemLayout` is `null`
    public final void setItemLayout(M3NavigationItemLayout itemLayout) {
        this.itemLayoutState.set(Objects.requireNonNull(itemLayout, "itemLayout"));
    }

    /// Returns the observable, bindable child navigation-item layout property.
    ///
    /// The property is [M3NavigationItemLayout#VERTICAL] by default. A direct `null` assignment restores that
    /// default; changes update the bar style and every installed item.
    ///
    /// @return the child navigation-item layout property
    public final ObjectProperty<M3NavigationItemLayout> itemLayoutProperty() {
        return itemLayoutState;
    }

    /// The spacing between adjacent vertical item target areas in logical pixels.
    ///
    /// Values must be finite and non-negative. The Java default is `0.0`; stylesheets may supply a profile-specific
    /// value through `-m3-item-spacing`.
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty itemSpacingStyleable;

    /// Returns the spacing between adjacent vertical navigation item target areas.
    ///
    /// The baseline navigation bar resolves this value to zero. The flexible M3 Expressive vertical layout uses
    /// six logical pixels between target areas. Horizontal items retain fixed widths with no additional spacing.
    ///
    /// @return the item spacing in logical pixels
    public final double getItemSpacing() {
        return itemSpacingStyleable == null ? DEFAULT_ITEM_SPACING : itemSpacingStyleable.get();
    }

    /// Sets the spacing between adjacent vertical navigation item target areas.
    ///
    /// @param itemSpacing the non-negative item spacing in logical pixels
    /// @throws IllegalArgumentException if `itemSpacing` is negative or not finite
    public final void setItemSpacing(double itemSpacing) {
        itemSpacingProperty().set(M3Css.nonNegative(itemSpacing, "itemSpacing"));
    }

    /// Returns the observable, bindable, CSS-styleable item-spacing property.
    ///
    /// The property is `0.0` logical pixels before CSS is applied and accepts only finite non-negative values. It is
    /// styleable through `-m3-item-spacing`; changes request layout.
    ///
    /// @return the item-spacing property
    public final StyleableDoubleProperty itemSpacingProperty() {
        if (itemSpacingStyleable == null) {
            itemSpacingStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ITEM_SPACING,
                    this,
                    "itemSpacing",
                    StyleableProperties.ITEM_SPACING,
                    this::requestLayout
            );
        }
        return itemSpacingStyleable;
    }

    /// The currently selected navigation item.
    ///
    /// @defaultValue `null`
    private final ReadOnlyObjectWrapper<@Nullable M3NavigationItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// Returns the selected navigation item.
    ///
    /// @return the selected navigation item, or `null` when no item is selected
    public final @Nullable M3NavigationItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the observable, read-only selected-item property.
    ///
    /// The property is `null` by default and tracks the selected visible, directly enabled destination. Ancestor
    /// visibility and disable state do not clear the value, so selection survives adaptive presentation changes.
    ///
    /// @return the read-only selected-item property
    public final ReadOnlyObjectProperty<@Nullable M3NavigationItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Whether selection may be empty while visible, directly enabled items exist.
    ///
    /// Setting the value to `false` selects the first visible, directly enabled item if the selection is empty.
    ///
    /// @defaultValue `false`
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection") {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

    /// Returns whether this bar allows all navigation items to be unselected.
    ///
    /// @return `true` when all navigation items may be unselected
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this bar allows all navigation items to be unselected.
    ///
    /// @param allowEmptySelection whether all navigation items may be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the observable, bindable empty-selection policy property.
    ///
    /// The property is `false` by default. Setting it to `false` while selection is empty selects the first visible,
    /// directly enabled destination, when one exists. Ancestor reachability does not affect the retained navigation
    /// state.
    ///
    /// @return the empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Returns the live mutable destination list.
    ///
    /// Mutations are observed immediately and insertion order determines layout and keyboard traversal. The list
    /// rejects `null` elements and repeated occurrences of the same destination instance. Bulk mutations are
    /// validated before the list changes, and each item must satisfy the JavaFX single-parent rule. Removing an
    /// item clears its selected state.
    ///
    /// @return the live mutable destination list
    public final ObservableList<M3NavigationItem> getItems() {
        return items;
    }

    /// Returns an unmodifiable observable view containing the selected destination.
    ///
    /// The returned view is live and contains zero or one item in destination order.
    ///
    /// @return the live unmodifiable selected-item view
    public final @UnmodifiableView ObservableList<M3NavigationItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the child index of the selected navigation item, or `-1` when no item is selected.
    ///
    /// @return the child index of the selected navigation item, or `-1` when no item is selected
    public final int getSelectedIndex() {
        @Nullable M3NavigationItem item = getSelectedItem();
        return item == null ? -1 : getItems().indexOf(item);
    }

    /// Selects a navigation item that belongs to this bar.
    ///
    /// The bar and its ancestors may be hidden or disabled. This permits an application to keep alternate adaptive
    /// navigation surfaces synchronized before or while one surface is not interactive.
    ///
    /// @param item the navigation item to select
    /// @throws NullPointerException     if `item` is `null`
    /// @throws IllegalArgumentException if `item` is hidden, disabled, or does not belong to this bar
    public final void select(M3NavigationItem item) {
        Objects.requireNonNull(item, "item");
        if (!getItems().contains(item)) {
            throw new IllegalArgumentException("item must belong to this navigation bar");
        }
        if (!isModelSelectableNavigationItem(item)) {
            throw new IllegalArgumentException("item must be selectable");
        }
        selectItem(item);
    }

    /// Selects the navigation item at the given child index.
    ///
    /// @param index the child index of the navigation item
    /// @throws IndexOutOfBoundsException if `index` is outside the item list
    /// @throws IllegalArgumentException  if the indexed item is not selectable
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
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        M3NavigationItem firstItem = firstNavigationItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Selects the last navigation item when one exists.
    public final void selectLast() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3NavigationItem lastItem =
                M3SelectionNavigation.last(getItems(), M3NavigationItem.class);
        if (lastItem != null) {
            selectItem(lastItem);
        }
    }

    /// Selects the next navigation item after the current selected item, wrapping at the end.
    public final void selectNext() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3NavigationItem nextItem =
                M3SelectionNavigation.next(getItems(), getSelectedItem(), M3NavigationItem.class);
        if (nextItem != null) {
            selectItem(nextItem);
        }
    }

    /// Selects the previous navigation item before the current selected item, wrapping at the start.
    public final void selectPrevious() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3NavigationItem previousItem =
                M3SelectionNavigation.previous(getItems(), getSelectedItem(), M3NavigationItem.class);
        if (previousItem != null) {
            selectItem(previousItem);
        }
    }

    /// Clears the current selection if empty selection is allowed.
    ///
    /// Otherwise, this method preserves or restores the first visible, directly enabled destination.
    public final void clearSelection() {
        if (!isAllowEmptySelection()) {
            selectFirstItemIfNeeded();
            return;
        }
        selectItem(null);
    }

    /// Returns the user-agent stylesheet for M3FX navigation controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-bar.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the immutable CSS metadata list for this control
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for navigation bar content and selection state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrSelectionFocusTarget(
                    this,
                    getItems(),
                    getSelectedItem(),
                    M3NavigationItem.class
            );
            case MULTIPLE_SELECTION -> false;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for navigation items.
    ///
    /// @param action     the accessibility action to execute
    /// @param parameters optional action-specific parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleSelectionTarget();
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Requests focus on the current selected or focused accessibility target.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleSelectionTarget() {
        if (M3Accessible.showItem(this, M3Accessible.currentOrSelectionFocusTarget(
                this,
                getItems(),
                getSelectedItem(),
                M3NavigationItem.class
        ))) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Shows an item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested item
    final boolean showAccessibleItem(Object... parameters) {
        if (M3Accessible.showItemOrDefault(this, M3Accessible.currentOrSelectionFocusTarget(
                this,
                getItems(),
                getSelectedItem(),
                M3NavigationItem.class
        ), getItems(), parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the group focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Adds base style classes and installs selection listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        updateItemLayoutStyle();
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleSelectionTarget, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(childrenListener);
        focusNotifier.start();
    }

    /// Applies keyboard navigation across enabled navigation items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeySelection(
                event,
                this,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3NavigationItem.class),
                M3NavigationItem.class,
                true,
                false,
                M3NodeLayout.isRightToLeft(this),
                this::select
        );
    }

    /// Applies the selected navigation item supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        @Nullable M3NavigationItem item =
                firstAccessibleSelectableItem(parameters);
        if (item == null) {
            clearSelection();
        } else {
            select(item);
        }
    }

    /// Installs a selected-state listener on a navigation item.
    private void installItem(M3NavigationItem item) {
        item.setItemLayout(getItemLayout());
        item.selectedProperty().addListener(selectedInvalidation);
        item.disabledProperty().addListener(reachabilityInvalidation);
        item.visibleProperty().addListener(reachabilityInvalidation);
    }

    /// Removes the selected-state listener from a navigation item.
    private void uninstallItem(M3NavigationItem item) {
        item.selectedProperty().removeListener(selectedInvalidation);
        item.disabledProperty().removeListener(reachabilityInvalidation);
        item.visibleProperty().removeListener(reachabilityInvalidation);
    }

    /// Applies the style class matching the compact or medium-window item arrangement.
    private void updateItemLayoutStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getItemLayout() == M3NavigationItemLayout.HORIZONTAL
                        ? HORIZONTAL_STYLE_CLASS
                        : VERTICAL_STYLE_CLASS,
                VERTICAL_STYLE_CLASS,
                HORIZONTAL_STYLE_CLASS
        );
    }

    /// Applies the configured layout to every navigation item child.
    private void updateItemLayouts() {
        for (Node child : getItems()) {
            if (child instanceof M3NavigationItem item) {
                item.setItemLayout(getItemLayout());
            }
        }
    }

    /// Keeps externally changed item selected states mutually exclusive.
    private void handleItemSelectedChanged(M3NavigationItem item, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (!isModelSelectableNavigationItem(item)) {
            if (selected) {
                clearItemSelection(item);
                if (!isAllowEmptySelection()) {
                    selectFirstItemIfNeeded();
                }
            }
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

    /// Keeps selection and accessibility state consistent when an item itself becomes hidden or disabled.
    private void handleItemReachabilityChanged(M3NavigationItem item) {
        if (item.isSelected() && !isModelSelectableNavigationItem(item)) {
            clearItemSelection(item);
        }
        enforceSelectionPolicy();
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
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

    /// Clears one navigation item's selected state and refreshes selected item state.
    private void clearItemSelection(M3NavigationItem item) {
        clearItemSelectionWithoutRefresh(item);
        refreshSelectedItems();
    }

    /// Clears one navigation item's selected state without refreshing the aggregate selected item list.
    private void clearItemSelectionWithoutRefresh(M3NavigationItem item) {
        updatingSelection = true;
        try {
            item.setSelected(false);
        } finally {
            updatingSelection = false;
        }
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
        selectedItemsScratch.clear();
        for (Node child : getItems()) {
            if (child instanceof M3NavigationItem item && item.isSelected()) {
                if (isModelSelectableNavigationItem(item)) {
                    selectedItemsScratch.add(item);
                } else {
                    clearItemSelectionWithoutRefresh(item);
                }
            }
        }
        boolean selectionChanged = !selectedItems.equals(selectedItemsScratch);
        if (selectionChanged) {
            selectedItems.setAll(selectedItemsScratch);
        }
        selectedItemsScratch.clear();

        selectedItem.set(selectedItems.isEmpty() ? null : selectedItems.get(0));
        if (selectionChanged) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            M3Accessible.notifyFocusNodeChanged(this);
            focusNotifier.refresh();
        }
    }

    /// Returns the first navigation item child.
    private @Nullable M3NavigationItem firstNavigationItem() {
        for (M3NavigationItem item : getItems()) {
            if (isModelSelectableNavigationItem(item)) {
                return item;
            }
        }
        return null;
    }

    /// Returns the first selectable item referenced by accessibility parameters.
    private @Nullable M3NavigationItem firstAccessibleSelectableItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getItems()) {
            if (child instanceof M3NavigationItem item
                    && isAccessibleNavigationItem(item)
                    && M3Accessible.containsSelectionTarget(item, parameters)) {
                return item;
            }
        }
        return null;
    }

    /// Returns whether a navigation item can participate in the retained selection model.
    private static boolean isModelSelectableNavigationItem(M3NavigationItem item) {
        return item.isVisible() && !item.isDisable();
    }

    /// Returns whether a navigation item can currently be addressed by an accessibility action.
    private boolean isAccessibleNavigationItem(M3NavigationItem item) {
        return M3Accessible.isEffectivelyReachable(this) && M3Accessible.isEffectivelyReachable(item);
    }

    /// Creates the default Material Design 3 navigation bar skin.
    ///
    /// @return the default Material Design 3 navigation bar skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3NavigationBarSkin(this);
    }

    /// CSS metadata for navigation bar styleable properties.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for spacing between adjacent vertical navigation items.
        private static final CssMetaData<M3NavigationBar, Number> ITEM_SPACING =
                new CssMetaData<>("-m3-item-spacing", SizeConverter.getInstance(), DEFAULT_ITEM_SPACING) {
                    /// Returns whether CSS can set the item-spacing property.
                    @Override
                    public boolean isSettable(M3NavigationBar control) {
                        return M3Css.isSettable(control.itemSpacingProperty());
                    }

                    /// Returns the corresponding styleable property.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationBar control) {
                        return control.itemSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ITEM_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents instantiation.
        private StyleableProperties() {
        }
    }

}
