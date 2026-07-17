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
/// `M3NavigationBar` arranges [M3NavigationItem] children horizontally and manages their selected state. It
/// supports empty-selection control, read-only selected-item views, keyboard navigation, and JavaFX accessibility
/// selection attributes. The bar is intended for three to five primary destinations at the bottom edge of an
/// application window or region.
///
/// See [Material Design navigation bars](https://m3.material.io/components/navigation-bar/overview).
@NotNullByDefault
public final class M3NavigationBar extends Control {
    /// The base style class for M3FX navigation bars.
    public static final String STYLE_CLASS = "m3-navigation-bar";

    /// The style class used by compact vertical-item navigation bars.
    public static final String VERTICAL_STYLE_CLASS = "m3-navigation-bar-vertical";

    /// The style class used by medium-window horizontal-item navigation bars.
    public static final String HORIZONTAL_STYLE_CLASS = "m3-navigation-bar-horizontal";

    /// The default spacing between navigation bar item target areas.
    private static final double DEFAULT_ITEM_SPACING = 0.0;

    /// The mutable navigation bar content.
    private final ObservableList<M3NavigationItem> items = M3ObservableLists.nonNullElementList("item");

    /// Notifies accessibility clients when focus moves between navigation items.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(
                            this,
                            getItems(),
                            getSelectedItem(),
                            M3NavigationItem.class
                    ));

    /// The currently selected navigation item.
    private final ReadOnlyObjectWrapper<@Nullable M3NavigationItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected navigation items in child order.
    private final ObservableList<M3NavigationItem> selectedItems = M3ObservableLists.nonNullElementList("selectedItem");

    /// The read-only selected navigation item view.
    private final @UnmodifiableView ObservableList<M3NavigationItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// The icon and label arrangement applied to navigation items.
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

    /// The styleable spacing between adjacent vertical navigation item target areas.
    private @Nullable StyleableDoubleProperty itemSpacingStyleable;

    /// Whether the bar allows all navigation items to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection") {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

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

    /// Creates an empty navigation bar.
    public M3NavigationBar() {
        initialize();
    }

    /// Returns the mutable child list used as navigation bar items.
    ///
    /// @return the mutable navigation bar content list
    public final ObservableList<M3NavigationItem> getItems() {
        return items;
    }


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
    /// @throws NullPointerException if any required argument is `null`
    public final void setItemLayout(M3NavigationItemLayout itemLayout) {
        this.itemLayoutState.set(Objects.requireNonNull(itemLayout, "itemLayout"));
    }

    /// Returns the child navigation item layout property.
    ///
    /// @return the writable navigation item layout property
    public final ObjectProperty<M3NavigationItemLayout> itemLayoutProperty() {
        return itemLayoutState;
    }

    /// Returns the spacing between adjacent vertical navigation item target areas.
    ///
    /// The baseline navigation bar resolves this value to zero. The flexible M3 Expressive vertical layout uses
    /// six pixels between target areas. Horizontal items retain fixed widths with no additional spacing.
    ///
    /// @return the item spacing in pixels
    public final double getItemSpacing() {
        return itemSpacingStyleable == null ? DEFAULT_ITEM_SPACING : itemSpacingStyleable.get();
    }

    /// Sets the spacing between adjacent vertical navigation item target areas.
    ///
    /// @param itemSpacing the non-negative item spacing in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setItemSpacing(double itemSpacing) {
        itemSpacingProperty().set(M3Css.nonNegative(itemSpacing, "itemSpacing"));
    }

    /// Returns the styleable item-spacing property.
    ///
    /// @return the styleable item-spacing property
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

    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a navigation item that belongs to this bar.
    ///
    /// @param item the navigation item to select
    /// @throws NullPointerException if any required argument is `null`
    public final void select(M3NavigationItem item) {
        Objects.requireNonNull(item, "item");
        if (!getItems().contains(item)) {
            throw new IllegalArgumentException("item must belong to this navigation bar");
        }
        if (!isSelectableNavigationItem(item)) {
            throw new IllegalArgumentException("item must be selectable");
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

    /// Clears the current selection when empty selection is allowed.
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
    /// @throws NullPointerException if any required argument is `null`
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
    /// @throws NullPointerException if any required argument is `null`
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
        M3ControlStyles.initialize(this, STYLE_CLASS);
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

        if (!isSelectableNavigationItem(item)) {
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

    /// Keeps selection and accessibility state consistent when an item becomes unreachable.
    private void handleItemReachabilityChanged(M3NavigationItem item) {
        if (item.isSelected() && !isSelectableNavigationItem(item)) {
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
                if (isSelectableNavigationItem(item)) {
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
        return M3SelectionNavigation.first(getItems(), M3NavigationItem.class);
    }

    /// Returns the first selectable item referenced by accessibility parameters.
    private @Nullable M3NavigationItem firstAccessibleSelectableItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getItems()) {
            if (child instanceof M3NavigationItem item
                    && isSelectableNavigationItem(item)
                    && M3Accessible.containsSelectionTarget(item, parameters)) {
                return item;
            }
        }
        return null;
    }

    /// Returns whether a navigation item can currently participate in selection.
    private boolean isSelectableNavigationItem(M3NavigationItem item) {
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
