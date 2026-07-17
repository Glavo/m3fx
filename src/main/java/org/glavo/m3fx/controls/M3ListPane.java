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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3TypeAheadState;
import org.glavo.m3fx.skins.M3ListPaneSkin;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 static list container for a small number of already-created nodes.
///
/// `M3ListPane` is useful when the application creates the exact nodes to display and the list is small enough
/// that virtualization is unnecessary. [getItems] is a live, ordered list that may contain [M3ListItem], section
/// headers, dividers, and other structural nodes. Only enabled and visible `M3ListItem` entries participate in
/// selection and keyboard traversal. For large or data-driven lists, prefer [M3ListView].
///
/// The default pane is empty, uses [M3ListStyle#STANDARD], has no managed selection, and permits an empty
/// selection. The selected-item list is a live, unmodifiable observable view in child order. Nodes in the item
/// list are owned by this control while displayed and must not belong to another parent.
///
/// ```java
/// M3ListPane listPane = new M3ListPane();
/// M3ListItem inboxItem = new M3ListItem("Inbox");
/// listPane.getItems().addAll(inboxItem, new M3ListItem("Archive"));
/// listPane.setSelectionMode(M3SelectionMode.SINGLE);
/// listPane.select(inboxItem);
/// ```
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public final class M3ListPane extends Control {
    /// The base style class for M3FX static list panes.
    public static final String STYLE_CLASS = "m3-list-pane";

    /// The default list containment style.
    private static final M3ListStyle DEFAULT_LIST_STYLE = M3ListStyle.STANDARD;

    /// The default item spacing used by standard lists.
    private static final double DEFAULT_ITEM_SPACING = 0.0;

    /// The live, mutable, ordered content displayed by this pane.
    ///
    /// The list rejects `null` elements and reports mutations through the `ObservableList` change API. Structural
    /// nodes are displayed but do not participate in selection. Removing a selected list item clears its selected
    /// state.
    private final ObservableList<Node> items = M3ObservableLists.nonNullElementList("item");

    /// The visual containment style used for list items.
    ///
    /// A direct assignment of `null` is replaced with [M3ListStyle#STANDARD]. This property changes presentation
    /// only; it does not alter item order or selection.
    ///
    /// @defaultValue [M3ListStyle#STANDARD]
    private final ObjectProperty<M3ListStyle> listStyle =
            new SimpleObjectProperty<>(this, "listStyle", DEFAULT_LIST_STYLE) {
                /// Updates the list style class after the containment style changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_LIST_STYLE);
                        return;
                    }
                    updateListStyle();
                }
            };

    /// The gap between directly adjacent [M3ListItem] nodes in logical pixels.
    ///
    /// Values must be finite and non-negative. The effective default is supplied by the active list style and
    /// theme; section headers, dividers, and other structural content do not receive this gap.
    private @Nullable StyleableDoubleProperty itemSpacing;

    /// Notifies accessibility clients when focus moves between list items.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(this, getItems(), getSelectedItem(), M3ListItem.class));

    /// The policy applied when a reachable list item is activated.
    ///
    /// [M3SelectionMode#NONE] leaves item activation independent of selection, [M3SelectionMode#SINGLE] retains at
    /// most one selected item, and [M3SelectionMode#MULTIPLE] permits multiple selected items. A direct assignment
    /// of `null` is replaced with [M3SelectionMode#NONE].
    ///
    /// @defaultValue [M3SelectionMode#NONE]
    private final ObjectProperty<M3SelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", M3SelectionMode.NONE) {
                /// Enforces selection invariants when the mode changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SelectionMode.NONE);
                        return;
                    }
                    enforceSelectionPolicy();
                }
            };

    /// Whether the managed selection may be empty.
    ///
    /// Setting the value to `false` selects the first enabled, visible list item when managed selection is active
    /// and currently empty.
    ///
    /// @defaultValue `true`
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
    private final ObservableList<M3ListItem> selectedItems = M3ObservableLists.nonNullElementList("selectedItem");

    /// The read-only selected list item view.
    private final @UnmodifiableView ObservableList<M3ListItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// The first selected list item in child order.
    private final ReadOnlyObjectWrapper<@Nullable M3ListItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// Reusable storage for computing selected items without allocating on every refresh.
    private final List<M3ListItem> selectedItemsScratch = new ArrayList<>();

    /// Handles selected-state invalidation for every installed list item.
    private final InvalidationListener selectedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property && property.getBean() instanceof M3ListItem item) {
            handleItemSelectedChanged(item, item.isSelected());
        }
    };

    /// Handles reachability invalidation for every installed list item.
    private final InvalidationListener reachabilityInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property && property.getBean() instanceof M3ListItem item) {
            handleItemReachabilityChanged(item);
        }
    };

    /// Handles list item actions by applying the configured selection policy.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// The lazily activated printable-key prefix used for list type-ahead focus navigation.
    private final M3TypeAheadState typeAheadState = new M3TypeAheadState(this);

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
        typeAheadState.clear();
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    };

    /// Whether the list is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty standard list pane with selection disabled and empty selection allowed.
    public M3ListPane() {
        initialize();
    }

    /// Returns the live mutable list of nodes displayed by this pane.
    ///
    /// Mutations are observed immediately and insertion order determines layout, selection order, and keyboard
    /// traversal. The list rejects `null`. It does not perform an explicit duplicate check, but each entry is a
    /// JavaFX node and must occur only once and must not simultaneously belong to another parent. Structural nodes
    /// are displayed but do not participate in managed selection.
    ///
    /// @return the live mutable item list
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Returns the list containment style.
    ///
    /// @return the standard or segmented list style
    public final M3ListStyle getListStyle() {
        return listStyle.get();
    }

    /// Sets the list containment style.
    ///
    /// @param listStyle the standard or segmented list style
    /// @throws NullPointerException if `listStyle` is `null`
    public final void setListStyle(M3ListStyle listStyle) {
        this.listStyle.set(Objects.requireNonNull(listStyle, "listStyle"));
    }

    public final ObjectProperty<M3ListStyle> listStyleProperty() {
        return listStyle;
    }

    /// Returns the gap between directly adjacent [M3ListItem] nodes.
    ///
    /// Section headers, dividers, and other content nodes do not receive this gap.
    ///
    /// @return the item spacing in logical pixels
    public final double getItemSpacing() {
        return itemSpacing == null ? DEFAULT_ITEM_SPACING : itemSpacing.get();
    }

    /// Sets the gap between directly adjacent [M3ListItem] nodes.
    ///
    /// An explicit Java value overrides the style default selected by [listStyleProperty()]. Section headers,
    /// dividers, and other content nodes remain contiguous with their neighbors.
    ///
    /// @param itemSpacing the non-negative item spacing in logical pixels
    /// @throws IllegalArgumentException if `itemSpacing` is negative or not finite
    public final void setItemSpacing(double itemSpacing) {
        itemSpacingProperty().set(M3Css.nonNegative(itemSpacing, "itemSpacing"));
    }

    public final StyleableDoubleProperty itemSpacingProperty() {
        if (itemSpacing == null) {
            itemSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ITEM_SPACING,
                    this,
                    "itemSpacing",
                    StyleableProperties.ITEM_SPACING,
                    this::requestLayout
            );
        }
        return itemSpacing;
    }

    /// Returns the list item selection mode.
    ///
    /// @return the list item selection mode
    public final M3SelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the list item selection mode.
    ///
    /// @param selectionMode the list item selection mode
    /// @throws NullPointerException if `selectionMode` is `null`
    public final void setSelectionMode(M3SelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    public final ObjectProperty<M3SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Returns whether this list allows all selectable items to be unselected.
    ///
    /// @return `true` when this list allows all selectable items to be unselected
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this list allows all selectable items to be unselected.
    ///
    /// @param allowEmptySelection whether this list allows all selectable items to be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Returns an unmodifiable observable view of selected list items in child order.
    ///
    /// The returned list is live and reports changes caused by item mutation, item reachability, or selection policy.
    ///
    /// @return the live unmodifiable selected-item view
    public final @UnmodifiableView ObservableList<M3ListItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the first selected list item in child order.
    ///
    /// @return the first selected list item in child order, or `null` when no item is selected
    public final @Nullable M3ListItem getSelectedItem() {
        return selectedItem.get();
    }

    public final ReadOnlyObjectProperty<@Nullable M3ListItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the child index of the first selected list item, or `-1` when no item is selected.
    ///
    /// @return the child index of the first selected list item, or `-1` when no item is selected
    public final int getSelectedIndex() {
        @Nullable M3ListItem item = getSelectedItem();
        return item == null ? -1 : getItems().indexOf(item);
    }

    /// Selects a list item that belongs to this list.
    ///
    /// @param item the list item to select
    /// @throws NullPointerException if `item` is `null`
    /// @throws IllegalArgumentException if `item` is not an effectively reachable member of this pane
    public final void select(M3ListItem item) {
        Objects.requireNonNull(item, "item");
        if (!getItems().contains(item)) {
            throw new IllegalArgumentException("item must belong to this list");
        }
        if (!isSelectableListItem(item)) {
            throw new IllegalArgumentException("item must be selectable");
        }

        if (getSelectionMode() == M3SelectionMode.MULTIPLE) {
            setItemSelected(item, true);
        } else {
            selectOnly(item);
        }
    }

    /// Selects the list item at the given child index.
    ///
    /// @param index the child index to select
    /// @throws IndexOutOfBoundsException if `index` is outside the item list
    /// @throws IllegalArgumentException if the indexed node is not a selectable [M3ListItem]
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
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        M3ListItem firstItem = firstItem();
        if (firstItem != null) {
            select(firstItem);
        }
    }

    /// Selects the last list item when one exists.
    public final void selectLast() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3ListItem lastItem = M3SelectionNavigation.last(getItems(), M3ListItem.class);
        if (lastItem != null) {
            select(lastItem);
        }
    }

    /// Selects the next list item after the current selected item, wrapping at the end.
    public final void selectNext() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3ListItem nextItem =
                M3SelectionNavigation.next(getItems(), getSelectedItem(), M3ListItem.class);
        if (nextItem != null) {
            select(nextItem);
        }
    }

    /// Selects the previous list item before the current selected item, wrapping at the start.
    public final void selectPrevious() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3ListItem previousItem =
                M3SelectionNavigation.previous(getItems(), getSelectedItem(), M3ListItem.class);
        if (previousItem != null) {
            select(previousItem);
        }
    }

    /// Clears the current selection if the active policy allows it.
    ///
    /// If empty selection is disallowed while managed selection is active, this method preserves or restores the
    /// first selectable item instead.
    public final void clearSelection() {
        if (!isAllowEmptySelection() && getSelectionMode() != M3SelectionMode.NONE) {
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
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` if the attribute is not supported
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
                    M3ListItem.class
            );
            case MULTIPLE_SELECTION -> getSelectionMode() == M3SelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for list items.
    ///
    /// @param action the accessibility action to execute
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

    /// Requests focus on the current selected or focused list accessibility target.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleSelectionTarget() {
        if (M3Accessible.showItem(this, M3Accessible.currentOrSelectionFocusTarget(
                this,
                getItems(),
                getSelectedItem(),
                M3ListItem.class
        ))) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Shows a list item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested item
    final boolean showAccessibleItem(Object... parameters) {
        if (M3Accessible.showItemOrDefault(this, M3Accessible.currentOrSelectionFocusTarget(
                this,
                getItems(),
                getSelectedItem(),
                M3ListItem.class
        ), getItems(), parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the list focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Adds base style classes and installs child listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        updateListStyle();
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleSelectionTarget, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        addEventHandler(KeyEvent.KEY_TYPED, this::handleTypeAheadKeyTyped);
        getItems().addListener(childrenListener);
        focusNotifier.start();
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                typeAheadState.clear();
            }
        });
    }

    /// Applies keyboard navigation across enabled list items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (M3FocusTraversal.consumeNavigationKeyIfFocusOwnerInsideTextInput(this, event, false, true)) {
            return;
        }

        if (getSelectionMode() == M3SelectionMode.NONE
                || getSelectionMode() == M3SelectionMode.MULTIPLE) {
            if (M3SelectionNavigation.handleKeyFocus(
                    event,
                    this,
                    getItems(),
                    M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3ListItem.class),
                    M3ListItem.class,
                    false,
                    true
            )) {
                return;
            }
            M3SelectionNavigation.handlePageKeyFocus(
                    event,
                    this,
                    getItems(),
                    M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3ListItem.class),
                    M3ListItem.class
            );
            return;
        }

        if (M3SelectionNavigation.handleKeySelection(
                event,
                this,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3ListItem.class),
                M3ListItem.class,
                false,
                true,
                this::select
        )) {
            return;
        }
        M3SelectionNavigation.handlePageKeySelection(
                event,
                this,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3ListItem.class),
                M3ListItem.class,
                this::select
        );
    }

    /// Moves focus to the next list item whose text matches the printable-key search prefix.
    private void handleTypeAheadKeyTyped(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        if (M3FocusGuards.focusOwnerInsideTextInput(this)) {
            return;
        }

        if (M3KeyEvents.hasShortcutModifier(event)) {
            return;
        }

        String character = event.getCharacter();
        if (character.length() != 1 || Character.isISOControl(character.charAt(0)) || character.isBlank()) {
            return;
        }

        String normalizedCharacter = M3SelectionNavigation.normalizeTypeAheadText(character);
        typeAheadState.append(normalizedCharacter);
        @Nullable M3ListItem target = typeAheadTarget(typeAheadState.getPrefix());
        if (target == null && typeAheadState.length() > 1) {
            typeAheadState.replace(normalizedCharacter);
            target = typeAheadTarget(typeAheadState.getPrefix());
        }
        if (target == null) {
            return;
        }

        focusTypeAheadTarget(target);
        if (getSelectionMode() == M3SelectionMode.SINGLE) {
            select(target);
        }
        event.consume();
    }

    /// Returns the next enabled visible list item matching the normalized type-ahead prefix.
    private @Nullable M3ListItem typeAheadTarget(String prefix) {
        @Nullable M3ListItem anchor =
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3ListItem.class);
        return M3SelectionNavigation.typeAheadTarget(
                getItems(),
                anchor,
                M3ListItem.class,
                prefix,
                M3ListItem::getHeadlineText
        );
    }

    /// Focuses a type-ahead target and notifies accessibility clients.
    private void focusTypeAheadTarget(M3ListItem item) {
        if (M3Accessible.showItem(this, item)) {
            M3Accessible.notifyFocusNodeChanged(this);
            focusNotifier.refresh();
        }
    }

    /// Applies selected list items supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3SelectionMode.NONE) {
            return;
        }

        if (getSelectionMode() == M3SelectionMode.SINGLE) {
            @Nullable M3ListItem item = firstAccessibleSelectableItem(parameters);
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
                if (child instanceof M3ListItem item && isSelectableListItem(item)) {
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
        item.selectedProperty().addListener(selectedInvalidation);
        item.disabledProperty().addListener(reachabilityInvalidation);
        item.visibleProperty().addListener(reachabilityInvalidation);
    }

    /// Removes action and selected-state listeners from a list item.
    private void uninstallItem(M3ListItem item) {
        item.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        item.selectedProperty().removeListener(selectedInvalidation);
        item.disabledProperty().removeListener(reachabilityInvalidation);
        item.visibleProperty().removeListener(reachabilityInvalidation);
    }

    /// Applies the configured selection policy to a list item action.
    private void handleItemAction(ActionEvent event) {
        if (!(event.getSource() instanceof M3ListItem item)
                || !getItems().contains(item)
                || !isSelectableListItem(item)) {
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

        if (!isSelectableListItem(item)) {
            if (selected) {
                setItemSelected(item, false);
                if (!isAllowEmptySelection() && getSelectionMode() != M3SelectionMode.NONE) {
                    selectFirstItemIfNeeded();
                }
            }
            return;
        }

        if (selected && getSelectionMode() == M3SelectionMode.SINGLE) {
            selectOnly(item);
            return;
        }

        refreshSelectedItems();
        if (!selected && !isAllowEmptySelection()
                && getSelectionMode() != M3SelectionMode.NONE
                && selectedItems.isEmpty()) {
            select(item);
        } else {
            enforceSelectionPolicy();
        }
    }

    /// Keeps selection and accessibility state consistent when an item becomes unreachable.
    private void handleItemReachabilityChanged(M3ListItem item) {
        typeAheadState.clear();
        if (item.isSelected() && !isSelectableListItem(item)) {
            setItemSelected(item, false);
        }
        enforceSelectionPolicy();
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Enforces selection invariants for the current selection mode.
    private void enforceSelectionPolicy() {
        refreshSelectedItems();
        if (getSelectionMode() == M3SelectionMode.SINGLE && selectedItems.size() > 1) {
            selectOnly(selectedItems.get(0));
            return;
        }
        if (!isAllowEmptySelection() && getSelectionMode() != M3SelectionMode.NONE) {
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
        setItemSelectedWithoutRefresh(item, selected);
        refreshSelectedItems();
    }

    /// Sets one list item's selected state without refreshing the aggregate selected item list.
    private void setItemSelectedWithoutRefresh(M3ListItem item, boolean selected) {
        updatingSelection = true;
        try {
            item.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
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
        selectedItemsScratch.clear();
        for (Node child : getItems()) {
            if (child instanceof M3ListItem item && item.isSelected()) {
                if (isSelectableListItem(item)) {
                    selectedItemsScratch.add(item);
                } else {
                    setItemSelectedWithoutRefresh(item, false);
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

    /// Returns the first list item child.
    private @Nullable M3ListItem firstItem() {
        return M3SelectionNavigation.first(getItems(), M3ListItem.class);
    }

    /// Returns the first selectable list item referenced by accessibility parameters.
    private @Nullable M3ListItem firstAccessibleSelectableItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getItems()) {
            if (child instanceof M3ListItem item
                    && isSelectableListItem(item)
                    && M3Accessible.containsSelectionTarget(item, parameters)) {
                return item;
            }
        }
        return null;
    }

    /// Returns whether a list item can currently participate in selection.
    private boolean isSelectableListItem(M3ListItem item) {
        return M3Accessible.isEffectivelyReachable(this) && M3Accessible.isEffectivelyReachable(item);
    }

    /// Creates the default Material Design 3 list skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ListPaneSkin(this);
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the immutable CSS metadata list
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Applies the active list containment style class.
    private void updateListStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getListStyle().styleClass(),
                M3ListStyle.STANDARD.styleClass(),
                M3ListStyle.SEGMENTED.styleClass()
        );
        requestLayout();
    }

    /// CSS metadata for static list layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the gap between adjacent list items.
        private static final CssMetaData<M3ListPane, Number> ITEM_SPACING =
                new CssMetaData<>("-m3-list-item-spacing", SizeConverter.getInstance(), DEFAULT_ITEM_SPACING) {
                    /// Returns whether the spacing can be set from CSS.
                    @Override
                    public boolean isSettable(M3ListPane control) {
                        return M3Css.isSettable(control.itemSpacingProperty());
                    }

                    /// Returns the styleable spacing property.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ListPane control) {
                        return control.itemSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ITEM_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

}
