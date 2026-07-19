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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3TypeAheadState;
import org.glavo.m3fx.skins.M3MenuSkin;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 menu surface.
///
/// `M3Menu` is a menu surface with a live ordered content list. The list may contain [M3MenuItem],
/// [M3SubMenuItem], section headers, dividers, and other structural nodes. Only enabled and visible menu items
/// participate in focus navigation and managed selection.
///
/// The control does not create or own a window. Add it to a scene graph for an inline menu, or use [M3MenuButton]
/// to present it in an auto-hiding popup. The default menu is empty, uses [M3MenuColorStyle#STANDARD], disables
/// managed selection, and permits an empty selection. Item nodes are owned by the menu while displayed and must
/// not belong to another parent.
///
/// A menu with two actions can be created as follows:
///
/// ```java
/// M3Menu menu = new M3Menu();
/// M3MenuItem openItem = new M3MenuItem("Open");
/// openItem.setOnAction(event -> System.out.println("Open"));
/// menu.getItems().addAll(openItem, new M3MenuItem("Save"));
/// ```
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
@NotNullByDefault
public final class M3Menu extends Control {
    /// The pseudo-class applied when the menu uses the vibrant color style.
    private static final PseudoClass VIBRANT_PSEUDO_CLASS = PseudoClass.getPseudoClass("vibrant");

    /// The base style class for M3FX menus.
    public static final String STYLE_CLASS = "m3-menu";

    /// The style class for the internal item container used by menu skins.
    public static final String CONTAINER_STYLE_CLASS = "m3-menu-container";

    /// The live, mutable, ordered content displayed by this menu.
    ///
    /// The list rejects `null` elements and reports mutations through the `ObservableList` change API. Removing a
    /// selected item clears its selected state; removing a submenu owner also hides its open submenu.
    private final ObservableList<Node> items = M3ObservableLists.nonNullElementList("item");

    /// The selected menu items in child order.
    private final ObservableList<M3MenuItem> selectedItems = M3ObservableLists.nonNullElementList("selectedItem");

    /// The read-only selected menu item view.
    private final @UnmodifiableView ObservableList<M3MenuItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// Reusable storage for computing selected items without allocating on every refresh.
    private final List<M3MenuItem> selectedItemsScratch = new ArrayList<>();

    /// Handles actions from every installed menu item.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// Handles selected-state invalidation for every installed menu item.
    private final InvalidationListener selectedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property && property.getBean() instanceof M3MenuItem item) {
            handleItemSelectedChanged(item, item.isSelected());
        }
    };

    /// Handles reachability invalidation for every installed menu item.
    private final InvalidationListener reachabilityInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property && property.getBean() instanceof M3MenuItem item) {
            handleItemReachabilityChanged(item);
        }
    };

    /// The lazily activated printable-key prefix used for menu type-ahead focus navigation.
    private final M3TypeAheadState typeAheadState = new M3TypeAheadState(this);

    /// Reports focused menu-item changes to accessibility clients.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::focusedAccessibleNode, this::notifyFocusNodeChanged);

    /// The single composite owner notified when this menu's reported focus node changes.
    private @Nullable Runnable accessibleFocusNodeListener;

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
                } else {
                    clearChildColorStylePseudoClass(child);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3MenuItem item) {
                    installItem(item);
                } else {
                    updateChildColorStylePseudoClass(child);
                }
            }
        }
        typeAheadState.clear();
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyFocusNodeChanged();
    };

    /// Whether the menu is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty standard-color menu with selection disabled and empty selection allowed.
    public M3Menu() {
        initialize();
    }

    /// Creates a menu containing the supplied items.
    ///
    /// @param items the initial non-null menu content nodes
    /// @throws NullPointerException if `items` or any element of `items` is `null`
    public M3Menu(Node... items) {
        initialize();
        getItems().addAll(items);
    }

    /// The Material color mapping used by the menu and its direct content.
    ///
    /// A direct assignment of `null` is replaced with [M3MenuColorStyle#STANDARD]. The color style is propagated
    /// to nested submenus.
    ///
    /// @defaultValue [M3MenuColorStyle#STANDARD]
    private final ObjectProperty<M3MenuColorStyle> colorStyle =
            new SimpleObjectProperty<>(this, "colorStyle", M3MenuColorStyle.STANDARD) {
                /// Updates the color style pseudo-class when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3MenuColorStyle.STANDARD);
                        return;
                    }
                    updateColorStylePseudoClass();
                }
            };

    /// Returns the menu color style.
    ///
    /// @return the current menu color style
    public final M3MenuColorStyle getColorStyle() {
        return colorStyle.get();
    }

    /// Sets the menu color style.
    ///
    /// @param colorStyle the menu color style
    /// @throws NullPointerException if `colorStyle` is `null`
    public final void setColorStyle(M3MenuColorStyle colorStyle) {
        this.colorStyle.set(Objects.requireNonNull(colorStyle, "colorStyle"));
    }

    /// Returns the observable, bindable menu color-style property.
    ///
    /// The property is [M3MenuColorStyle#STANDARD] by default. A direct `null` assignment restores that default;
    /// changes update the menu and its direct content, including nested submenus.
    ///
    /// @return the menu color-style property
    public final ObjectProperty<M3MenuColorStyle> colorStyleProperty() {
        return colorStyle;
    }

    /// The policy applied when a reachable menu item is activated.
    ///
    /// [M3SelectionMode#NONE] leaves actions independent of selection, [M3SelectionMode#SINGLE] retains at most one
    /// selected item, and [M3SelectionMode#MULTIPLE] permits multiple selections. Changing the mode reconciles the
    /// current selection. A direct assignment of `null` is replaced with [M3SelectionMode#NONE].
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

    /// Returns the menu selection mode.
    ///
    /// @return the active menu selection mode
    public final M3SelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /// Sets the menu selection mode.
    ///
    /// @param selectionMode the active menu selection mode
    /// @throws NullPointerException if `selectionMode` is `null`
    public final void setSelectionMode(M3SelectionMode selectionMode) {
        this.selectionMode.set(Objects.requireNonNull(selectionMode, "selectionMode"));
    }

    /// Returns the observable, bindable menu selection-mode property.
    ///
    /// The property is [M3SelectionMode#NONE] by default. A direct `null` assignment restores that default; changes
    /// reconcile selected items with the new policy.
    ///
    /// @return the menu selection-mode property
    public final ObjectProperty<M3SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /// Whether the managed selection may be empty.
    ///
    /// Setting the value to `false` selects the first enabled, visible menu item when managed selection is active
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

    /// Returns whether this menu allows all selectable items to be unselected.
    ///
    /// @return `true` when all selectable menu items may be unselected
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this menu allows all selectable items to be unselected.
    ///
    /// @param allowEmptySelection whether all selectable menu items may be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the observable, bindable empty-selection policy property.
    ///
    /// The property is `true` by default. Setting it to `false` selects the first enabled, visible menu item when
    /// managed selection is active and currently empty.
    ///
    /// @return the empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// The first selected menu item in child order.
    ///
    /// @defaultValue `null`
    private final ReadOnlyObjectWrapper<@Nullable M3MenuItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// Returns the first selected menu item in child order.
    ///
    /// @return the first selected menu item, or `null` when selection is empty
    public final @Nullable M3MenuItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the observable, read-only first-selected-item property.
    ///
    /// The property is `null` by default. It tracks the first selected, enabled, visible menu item in content order
    /// and becomes `null` when the managed selection is empty.
    ///
    /// @return the read-only first-selected-item property
    public final ReadOnlyObjectProperty<@Nullable M3MenuItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the live mutable list of nodes displayed by this menu.
    ///
    /// Mutations are observed immediately and insertion order determines layout and keyboard traversal. The list
    /// rejects `null`. It does not perform an explicit duplicate check, but each entry is a JavaFX node and must
    /// occur only once and must not simultaneously belong to another parent. Structural nodes are displayed but do
    /// not participate in managed selection.
    ///
    /// @return the live mutable menu content list
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Returns an unmodifiable observable view of selected menu items in content order.
    ///
    /// The returned view is live and reports item, reachability, and selection-policy changes.
    ///
    /// @return the live unmodifiable selected-item view
    public final @UnmodifiableView ObservableList<M3MenuItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Hides any open submenu popups owned by this menu other than the supplied submenu owner.
    ///
    /// @param exception the submenu owner to leave open, or `null` to hide every open submenu
    final void hideSubMenusExcept(@Nullable M3SubMenuItem exception) {
        for (Node child : getItems()) {
            if (child instanceof M3SubMenuItem subMenuItem && subMenuItem != exception) {
                subMenuItem.hideSubMenu();
            }
        }
    }

    /// Sets the composite owner callback for accessible focus-node changes.
    ///
    /// @param listener the owner callback, or `null` to detach the owner
    final void setAccessibleFocusNodeListener(@Nullable Runnable listener) {
        accessibleFocusNodeListener = listener;
    }

    /// Focuses the first enabled visible menu item when one exists.
    ///
    /// @return `true` if an item was focused
    final boolean focusFirstItem() {
        @Nullable M3MenuItem firstItem = M3SelectionNavigation.first(getItems(), M3MenuItem.class);
        if (firstItem == null) {
            return false;
        }
        return focusMenuItem(firstItem);
    }

    /// Focuses the last enabled visible menu item when one exists.
    ///
    /// @return `true` if an item was focused
    final boolean focusLastItem() {
        @Nullable M3MenuItem lastItem = M3SelectionNavigation.last(getItems(), M3MenuItem.class);
        if (lastItem == null) {
            return false;
        }
        return focusMenuItem(lastItem);
    }

    /// Focuses the current, selected, or first enabled visible menu item.
    ///
    /// @return `true` if an item was focused or revealed
    final boolean focusDefaultItem() {
        @Nullable Node focusNode = focusedAccessibleNode();
        if (focusNode != null && M3Accessible.showItem(this, focusNode)) {
            notifyFocusNodeChanged();
            return true;
        }

        @Nullable M3MenuItem item =
                M3SelectionNavigation.focusTarget(getItems(), getSelectedItem(), M3MenuItem.class);
        return item != null && focusMenuItem(item);
    }

    /// Returns the child index of the first selected menu item, or `-1` when no item is selected.
    ///
    /// @return the child index of the first selected menu item, or `-1` when no item is selected
    public final int getSelectedIndex() {
        @Nullable M3MenuItem item = getSelectedItem();
        return item == null ? -1 : getItems().indexOf(item);
    }

    /// Selects a menu item that belongs to this menu.
    ///
    /// @param item the selectable menu item to select
    /// @throws NullPointerException     if `item` is `null`
    /// @throws IllegalArgumentException if `item` is not an effectively reachable, non-submenu member of this menu
    public final void select(M3MenuItem item) {
        Objects.requireNonNull(item, "item");
        if (!getItems().contains(item)) {
            throw new IllegalArgumentException("item must belong to this menu");
        }
        if (!isReachableSelectableMenuItem(item)) {
            throw new IllegalArgumentException("item must be selectable");
        }

        if (getSelectionMode() == M3SelectionMode.MULTIPLE) {
            setItemSelected(item, true);
        } else {
            selectOnly(item);
        }
    }

    /// Selects the selectable menu item at the given child index.
    ///
    /// @param index the child index of the selectable menu item
    /// @throws IndexOutOfBoundsException if `index` is outside the content list
    /// @throws IllegalArgumentException  if the indexed node is not a selectable [M3MenuItem]
    public final void selectIndex(int index) {
        Node child = getItems().get(index);
        if (child instanceof M3MenuItem item && isReachableSelectableMenuItem(item)) {
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

    /// Returns the user-agent stylesheet for M3FX menus.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("menu.css");
    }

    /// Returns accessibility attributes for menu content and selection state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case FOCUS_NODE -> focusedAccessibleNode();
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> getSelectionMode() == M3SelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for menu items.
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
            case REQUEST_FOCUS -> requestAccessibleFocus();
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.MENU);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::requestAccessibleFocus, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        addEventHandler(KeyEvent.KEY_TYPED, this::handleTypeAheadKeyTyped);
        getItems().addListener(childrenListener);
        focusNotifier.start();
        updateColorStylePseudoClass();
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                typeAheadState.clear();
            }
        });
    }

    /// Updates the pseudo-class that represents the current menu color mapping.
    private void updateColorStylePseudoClass() {
        pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, getColorStyle() == M3MenuColorStyle.VIBRANT);
        for (Node child : getItems()) {
            updateChildColorStylePseudoClass(child);
        }
    }

    /// Updates one direct child pseudo-class that depends on the current menu color style.
    private void updateChildColorStylePseudoClass(Node child) {
        child.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, getColorStyle() == M3MenuColorStyle.VIBRANT);
        if (child instanceof M3SubMenuItem subMenuItem) {
            subMenuItem.getSubMenu().setColorStyle(getColorStyle());
        }
    }

    /// Clears menu-owned color style pseudo-classes from a removed child.
    private static void clearChildColorStylePseudoClass(Node child) {
        child.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, false);
        if (child instanceof M3SubMenuItem subMenuItem) {
            subMenuItem.getSubMenu().setColorStyle(M3MenuColorStyle.STANDARD);
        }
    }

    /// Applies keyboard navigation across enabled menu items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }
        if (handleFocusedItemKey(event)) {
            return;
        }

        handleFocusNavigationKeyPressed(event);
    }

    /// Handles activation and submenu opening for the currently focused menu item.
    private boolean handleFocusedItemKey(KeyEvent event) {
        @Nullable M3MenuItem focusedItem = M3SelectionNavigation.focused(getItems(), M3MenuItem.class);
        if (focusedItem == null) {
            return false;
        }

        KeyCode code = event.getCode();
        if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            focusedItem.fire();
            event.consume();
            return true;
        }
        if (isOpenSubMenuKey(code) && focusedItem instanceof M3SubMenuItem subMenuItem) {
            subMenuItem.showSubMenuAndFocusFirstItem();
            event.consume();
            return true;
        }
        return false;
    }

    /// Returns whether a key opens a submenu for the current node orientation.
    private boolean isOpenSubMenuKey(KeyCode keyCode) {
        KeyCode openKey = M3NodeLayout.isRightToLeft(this) ? KeyCode.LEFT : KeyCode.RIGHT;
        return keyCode == openKey;
    }

    /// Applies keyboard focus movement across menu items.
    private void handleFocusNavigationKeyPressed(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        @Nullable M3MenuItem target = focusNavigationTarget(event.getCode());
        if (target == null) {
            return;
        }

        focusMenuItem(target);
        applyKeyboardSelection(target);
        event.consume();
    }

    /// Returns the target item for a menu keyboard navigation key.
    private @Nullable M3MenuItem focusNavigationTarget(KeyCode keyCode) {
        @Nullable M3MenuItem anchor =
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3MenuItem.class);
        return switch (keyCode) {
            case UP -> M3SelectionNavigation.previous(getItems(), anchor, M3MenuItem.class);
            case DOWN -> M3SelectionNavigation.next(getItems(), anchor, M3MenuItem.class);
            case HOME -> M3SelectionNavigation.first(getItems(), M3MenuItem.class);
            case END -> M3SelectionNavigation.last(getItems(), M3MenuItem.class);
            case PAGE_UP -> M3SelectionNavigation.page(this, getItems(), anchor, M3MenuItem.class, false);
            case PAGE_DOWN -> M3SelectionNavigation.page(this, getItems(), anchor, M3MenuItem.class, true);
            default -> null;
        };
    }

    /// Selects a keyboard-focused item when the current selection policy does so.
    private void applyKeyboardSelection(M3MenuItem target) {
        if (getSelectionMode() == M3SelectionMode.SINGLE && isSelectableMenuItem(target)) {
            select(target);
        }
    }

    /// Moves focus to the next menu item whose text matches the printable-key search prefix.
    private void handleTypeAheadKeyTyped(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        if (M3KeyEvents.hasShortcutModifier(event)) {
            return;
        }

        String character = event.getCharacter();
        if (character.length() != 1 || Character.isISOControl(character.charAt(0)) || character.isBlank()) {
            return;
        }

        String normalizedCharacter = M3SelectionNavigation.normalizeTypeAheadText(character);
        typeAheadState.append(normalizedCharacter);
        @Nullable M3MenuItem target = typeAheadTarget(typeAheadState.getPrefix());
        if (target == null && typeAheadState.length() > 1) {
            typeAheadState.replace(normalizedCharacter);
            target = typeAheadTarget(typeAheadState.getPrefix());
        }
        if (target == null) {
            return;
        }

        focusMenuItem(target);
        applyKeyboardSelection(target);
        event.consume();
    }

    /// Returns the next enabled visible menu item matching the normalized type-ahead prefix.
    private @Nullable M3MenuItem typeAheadTarget(String prefix) {
        @Nullable M3MenuItem anchor =
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3MenuItem.class);
        return M3SelectionNavigation.typeAheadTarget(
                getItems(),
                anchor,
                M3MenuItem.class,
                prefix,
                M3MenuItem::getHeadlineText
        );
    }

    /// Focuses a menu item and hides sibling submenu popups that no longer own focus.
    private boolean focusMenuItem(M3MenuItem item) {
        Objects.requireNonNull(item, "item");
        if (!getItems().contains(item)
                || !M3Accessible.isEffectivelyReachable(this)
                || !M3Accessible.isEffectivelyReachable(item)) {
            return false;
        }

        @Nullable M3SubMenuItem retainedSubMenu =
                item instanceof M3SubMenuItem subMenuItem && subMenuItem.isSubMenuShowing() ? subMenuItem : null;
        hideSubMenusExcept(retainedSubMenu);
        if (!M3Accessible.showItem(this, item)) {
            return false;
        }
        notifyFocusNodeChanged();
        return true;
    }

    /// Applies selected menu items supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        if (getSelectionMode() == M3SelectionMode.NONE) {
            return;
        }

        if (getSelectionMode() == M3SelectionMode.SINGLE) {
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
            for (Node child : getItems()) {
                if (child instanceof M3MenuItem item && isReachableSelectableMenuItem(item)) {
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

    /// Requests focus for the default menu item or this menu surface when no enabled item exists.
    final boolean requestAccessibleFocus() {
        if (focusDefaultItem()) {
            return true;
        }
        if (M3Accessible.showDirectItem(this, this)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns whether this menu can route an accessibility item request without changing popup or focus state.
    final boolean canShowAccessibleItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return true;
        }
        if (containsUnrevealableMenuActionTarget(getItems(), parameters)) {
            return false;
        }
        if (parameters[0] instanceof Number) {
            @Nullable Node item = M3Accessible.itemAt(getItems(), parameters);
            if (isMenuActionItemUnreachable(item)) {
                return false;
            }
            if (parameters.length == 1) {
                return true;
            }
            Object[] nestedParameters = new Object[parameters.length - 1];
            System.arraycopy(parameters, 1, nestedParameters, 0, nestedParameters.length);
            return containsMenuActionTarget(item, nestedParameters);
        }
        return containsNestedAccessibleTarget(getItems(), parameters);
    }

    /// Focuses the item supplied by an accessibility action parameter.
    final boolean showAccessibleItem(Object... parameters) {
        if (parameters.length == 0 && focusDefaultItem()) {
            return true;
        }
        if (parameters.length > 1 && parameters[0] instanceof Number) {
            @Nullable Node indexedItem = M3Accessible.itemAt(getItems(), parameters);
            if (indexedItem == null) {
                return false;
            }
            Object[] nestedParameters = new Object[parameters.length - 1];
            System.arraycopy(parameters, 1, nestedParameters, 0, nestedParameters.length);
            if (indexedItem instanceof M3SubMenuItem subMenuItem && focusMenuItem(subMenuItem)) {
                return subMenuItem.showAccessibleSubMenuItem(nestedParameters);
            }
            if (M3Accessible.showAccessibleActionTarget(this, indexedItem, nestedParameters)) {
                notifyFocusNodeChanged();
                return true;
            }
            return false;
        }

        @Nullable Node item = M3Accessible.actionItem(getItems(), parameters);
        if (item instanceof M3MenuItem menuItem && focusMenuItem(menuItem)) {
            return true;
        }
        if (focusNestedAccessibleItem(parameters)) {
            return true;
        }
        if (M3Accessible.showAccessibleActionTarget(this, getItems(), parameters)) {
            notifyFocusNodeChanged();
            return true;
        }
        if (M3Accessible.showItem(this, item)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Opens a nested submenu branch and focuses the requested descendant item when possible.
    private boolean focusNestedAccessibleItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length > 0 && parameters[0] instanceof Number) {
            return false;
        }

        for (Node child : getItems()) {
            if (child instanceof M3SubMenuItem subMenuItem
                    && containsNestedAccessibleTarget(subMenuItem.getItems(), parameters)
                    && focusMenuItem(subMenuItem)) {
                return subMenuItem.showAccessibleSubMenuItem(parameters);
            }
        }
        return false;
    }

    /// Returns whether the supplied menu subtree contains an accessibility action target.
    private static boolean containsNestedAccessibleTarget(
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        return containsNestedAccessibleTarget(items, 0, parameters);
    }

    /// Returns whether the supplied menu subtree contains an accessibility action target after an outer index prefix.
    private static boolean containsNestedAccessibleTarget(
            ObservableList<? extends Node> items,
            int firstTargetParameter,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (firstTargetParameter >= parameters.length || parameters[firstTargetParameter] instanceof Number) {
            return false;
        }
        for (int index = firstTargetParameter; index < parameters.length; index++) {
            if (M3Accessible.actionItem(items, parameters[index]) != null) {
                return true;
            }
        }
        for (Node item : items) {
            for (int index = firstTargetParameter; index < parameters.length; index++) {
                if (M3Accessible.containsAccessibleActionTarget(item, parameters[index])) {
                    return true;
                }
            }
            if (item instanceof M3SubMenuItem subMenuItem
                    && containsNestedAccessibleTarget(subMenuItem.getItems(), firstTargetParameter, parameters)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether accessibility parameters target a menu node that cannot become reachable when popups open.
    private static boolean containsUnrevealableMenuActionTarget(
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsUnrevealableMenuActionTarget(items, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one accessibility parameter targets an unreachable node in a menu tree.
    private static boolean containsUnrevealableMenuActionTarget(
            ObservableList<? extends Node> items,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (containsUnrevealableMenuActionTarget(items, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (containsUnrevealableMenuActionTarget(items, value)) {
                    return true;
                }
            }
            return false;
        }

        for (Node item : items) {
            if (containsUnrevealableMenuActionTarget(item, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one menu item owns an unreachable requested target.
    private static boolean containsUnrevealableMenuActionTarget(Node item, @Nullable Object parameter) {
        if (isMenuActionItemUnreachable(item) && containsMenuActionTarget(item, parameter)) {
            return true;
        }
        if (parameter instanceof Node target && M3Accessible.containsNode(item, target)) {
            return !canRevealMenuActionNode(item, target);
        }
        if (item instanceof M3SubMenuItem subMenuItem) {
            return containsUnrevealableMenuActionTarget(subMenuItem.getItems(), parameter);
        }
        return false;
    }

    /// Returns whether one menu item or submenu tree contains any accessibility target.
    private static boolean containsMenuActionTarget(Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsMenuActionTarget(item, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one menu item or submenu tree contains an accessibility target.
    private static boolean containsMenuActionTarget(Node item, @Nullable Object parameter) {
        if (parameter instanceof Node target) {
            if (item == target || M3Accessible.containsNode(item, target)) {
                return true;
            }
            return item instanceof M3SubMenuItem subMenuItem
                    && containsMenuActionTarget(subMenuItem.getItems(), target);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (containsMenuActionTarget(item, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (containsMenuActionTarget(item, value)) {
                    return true;
                }
            }
            return false;
        }
        return (parameter != null && M3Accessible.containsAccessibleActionTarget(item, parameter))
                || (item instanceof M3SubMenuItem subMenuItem
                && containsMenuActionTarget(subMenuItem.getItems(), parameter));
    }

    /// Returns whether a menu tree contains an accessibility target.
    private static boolean containsMenuActionTarget(
            ObservableList<? extends Node> items,
            @Nullable Object parameter
    ) {
        for (Node item : items) {
            if (containsMenuActionTarget(item, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a menu item cannot become reachable when its owner popup is shown.
    private static boolean isMenuActionItemUnreachable(@Nullable Node item) {
        return item == null || !item.isVisible() || item.isDisabled();
    }

    /// Returns whether a physical descendant target can become reachable after the menu item is shown.
    private static boolean canRevealMenuActionNode(Node item, Node target) {
        if (isMenuActionItemUnreachable(item) || !target.isVisible() || target.isDisabled()) {
            return false;
        }
        if (item == target) {
            return true;
        }

        @Nullable Parent parent = target.getParent();
        while (parent != null && parent != item) {
            if (!parent.isVisible() || parent.isDisabled()) {
                return false;
            }
            parent = parent.getParent();
        }
        return parent == item;
    }

    /// Installs action and selected-state listeners on a menu item.
    private void installItem(M3MenuItem item) {
        updateChildColorStylePseudoClass(item);
        M3Accessible.setIndexOwner(item, getItems());
        if (item instanceof M3SubMenuItem subMenuItem) {
            subMenuItem.setOwnerMenu(this);
        }
        item.addEventHandler(ActionEvent.ACTION, itemActionHandler);
        item.selectedProperty().addListener(selectedInvalidation);
        item.disabledProperty().addListener(reachabilityInvalidation);
        item.visibleProperty().addListener(reachabilityInvalidation);
    }

    /// Removes action and selected-state listeners from a menu item.
    private void uninstallItem(M3MenuItem item) {
        clearChildColorStylePseudoClass(item);
        M3Accessible.clearIndexOwner(item);
        if (item instanceof M3SubMenuItem subMenuItem) {
            subMenuItem.setOwnerMenu(null);
        }
        item.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        item.selectedProperty().removeListener(selectedInvalidation);
        item.disabledProperty().removeListener(reachabilityInvalidation);
        item.visibleProperty().removeListener(reachabilityInvalidation);
    }

    /// Applies menu selection policy to an item action.
    private void handleItemAction(ActionEvent event) {
        @Nullable M3MenuItem sourceItem = event.getSource() instanceof M3MenuItem item ? item : null;
        @Nullable M3MenuItem directItem = directActionItem(sourceItem);
        boolean shouldForwardDetachedAction = shouldForwardDetachedAction(sourceItem, directItem);
        if (sourceItem == null
                || !getItems().contains(sourceItem)
                || !isReachableSelectableMenuItem(sourceItem)) {
            if (shouldForwardDetachedAction) {
                fireEvent(new ActionEvent(event.getSource(), this));
            }
            return;
        }

        switch (getSelectionMode()) {
            case NONE -> {
            }
            case SINGLE -> selectOnly(sourceItem);
            case MULTIPLE -> {
                if (sourceItem.isSelected() && !isAllowEmptySelection() && selectedItems.size() == 1) {
                    selectOnly(sourceItem);
                } else {
                    setItemSelected(sourceItem, !sourceItem.isSelected());
                }
            }
        }
        if (shouldForwardDetachedAction) {
            fireEvent(new ActionEvent(event.getSource(), this));
        }
    }

    /// Returns the direct child item that owns an action source in this menu tree.
    private @Nullable M3MenuItem directActionItem(@Nullable M3MenuItem sourceItem) {
        if (sourceItem == null) {
            return null;
        }
        for (Node child : getItems()) {
            if (child instanceof M3MenuItem item && containsMenuActionTarget(item, sourceItem)) {
                return item;
            }
        }
        return null;
    }

    /// Keeps externally changed item selected states consistent with the current menu policy.
    private void handleItemSelectedChanged(M3MenuItem item, boolean selected) {
        if (updatingSelection) {
            return;
        }
        if (!isReachableSelectableMenuItem(item)) {
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
    private void handleItemReachabilityChanged(M3MenuItem item) {
        typeAheadState.clear();
        if (item.isSelected() && !isReachableSelectableMenuItem(item)) {
            setItemSelected(item, false);
        }
        enforceSelectionPolicy();
        notifyFocusNodeChanged();
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

        M3MenuItem firstItem = firstItem();
        if (firstItem != null) {
            select(firstItem);
        }
    }

    /// Sets one menu item's selected state and refreshes selected item state.
    private void setItemSelected(M3MenuItem item, boolean selected) {
        setItemSelectedWithoutRefresh(item, selected);
        refreshSelectedItems();
    }

    /// Sets one menu item's selected state without refreshing the aggregate selected item list.
    private void setItemSelectedWithoutRefresh(M3MenuItem item, boolean selected) {
        updatingSelection = true;
        try {
            item.setSelected(selected);
        } finally {
            updatingSelection = false;
        }
    }

    /// Selects one item and clears selection from the remaining menu items.
    private void selectOnly(@Nullable M3MenuItem item) {
        updatingSelection = true;
        try {
            for (Node child : getItems()) {
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
        selectedItemsScratch.clear();
        for (Node child : getItems()) {
            if (child instanceof M3MenuItem item && item.isSelected()) {
                if (isReachableSelectableMenuItem(item)) {
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
        }
    }

    /// Returns the first selectable menu item child.
    private @Nullable M3MenuItem firstItem() {
        for (Node child : getItems()) {
            @Nullable M3MenuItem item = selectableMenuItem(child);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the last selectable menu item child.
    private @Nullable M3MenuItem lastItem() {
        ObservableList<Node> children = getItems();
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
        ObservableList<Node> children = getItems();
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
        ObservableList<Node> children = getItems();
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

    /// Returns the focused item inside this menu or one of its open submenus.
    private @Nullable Node focusedAccessibleNode() {
        @Nullable Node currentFocus = M3Accessible.currentFocusTarget(this, getItems());
        if (currentFocus != null) {
            return currentFocus;
        }

        @Nullable Node nestedFocus = focusedOpenSubMenuNode(true);
        if (nestedFocus != null) {
            return nestedFocus;
        }

        @Nullable M3MenuItem focusedItem = M3SelectionNavigation.focused(getItems(), M3MenuItem.class);
        if (focusedItem != null) {
            return focusedItem;
        }

        return focusedOpenSubMenuNode(false);
    }

    /// Notifies popup owners that a descendant's external popup focus node changed.
    void notifyDescendantFocusNodeChanged() {
        notifyFocusNodeChanged();
    }

    /// Notifies clients and popup owners that the current accessible focus node changed.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
        @Nullable Runnable listener = accessibleFocusNodeListener;
        if (listener != null) {
            listener.run();
        }
    }

    /// Returns the focus node reported by an open submenu.
    private @Nullable Node focusedOpenSubMenuNode(boolean requireNestedFocus) {
        for (Node child : getItems()) {
            if (child instanceof M3SubMenuItem subMenuItem && subMenuItem.isSubMenuShowing()) {
                @Nullable Object focusNode =
                        subMenuItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
                if (focusNode instanceof Node node && (!requireNestedFocus || node != subMenuItem)) {
                    return node;
                }
            }
        }
        return null;
    }

    /// Returns the first selectable menu item referenced by accessibility parameters.
    private @Nullable M3MenuItem firstAccessibleSelectableItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getItems()) {
            @Nullable M3MenuItem item = selectableMenuItem(child);
            if (item != null && M3Accessible.containsSelectionTarget(item, parameters)) {
                return item;
            }
        }
        return null;
    }

    /// Returns a node as a selectable menu item when possible.
    private @Nullable M3MenuItem selectableMenuItem(Node child) {
        if (child instanceof M3MenuItem item
                && isReachableSelectableMenuItem(item)) {
            return item;
        }
        return null;
    }

    /// Returns whether a menu item can currently participate in selection.
    private boolean isReachableSelectableMenuItem(M3MenuItem item) {
        return isSelectableMenuItem(item)
                && M3Accessible.isEffectivelyReachable(this)
                && M3Accessible.isEffectivelyReachable(item);
    }

    /// Returns whether a menu item participates in selection state.
    private static boolean isSelectableMenuItem(M3MenuItem item) {
        return !(item instanceof M3SubMenuItem);
    }

    /// Returns whether an action handled by a detached item must be forwarded through this menu.
    private boolean shouldForwardDetachedAction(
            @Nullable M3MenuItem sourceItem,
            @Nullable M3MenuItem directItem
    ) {
        if (sourceItem == null || directItem == null || M3Accessible.containsNode(this, directItem)) {
            return false;
        }
        if (sourceItem == directItem) {
            return getItems().contains(directItem) && (isSelectableMenuItem(sourceItem)
                    || sourceItem instanceof M3SubMenuItem subMenuItem && subMenuItem.isForwardingSubMenuAction());
        }
        return getItems().contains(directItem);
    }

    /// Creates the default Material Design 3 menu skin.
    ///
    /// @return the default Material Design 3 menu skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3MenuSkin(this);
    }

}
