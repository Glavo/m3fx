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
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3TypeAheadState;
import org.glavo.m3fx.skins.M3NavigationDrawerSkin;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 navigation drawer for persistent or modal destination lists.
///
/// `M3NavigationDrawer` hosts top-level [M3ListItem] destinations, collapsible [M3NavigationDrawerGroup] sections,
/// and structural nodes. It maintains single selection across both direct and grouped destinations. Keyboard and
/// accessibility traversal use the currently visible order; destinations in a collapsed group remain part of the
/// model but are not reachable until the group expands.
///
/// The default drawer is empty, uses [M3NavigationDrawerVariant#STANDARD], and requires one selected destination
/// whenever a reachable item exists. [getItems] is a live mutable top-level list, while [getSelectedItems] is a live,
/// unmodifiable observable view. When content exceeds the available height, it can be scrolled vertically.
///
/// The surrounding application is responsible for positioning a standard drawer. For a modal drawer, it is also
/// responsible for presenting a scrim, establishing modality for the application content, and controlling the
/// drawer's enter and exit lifecycle.
///
/// ```java
/// M3NavigationDrawer drawer = new M3NavigationDrawer();
/// M3ListItem homeItem = new M3ListItem("Home");
/// M3NavigationDrawerGroup libraryGroup = new M3NavigationDrawerGroup("Library");
/// libraryGroup.getItems().addAll(new M3ListItem("Albums"), new M3ListItem("Artists"));
/// drawer.getItems().addAll(homeItem, libraryGroup);
/// drawer.select(homeItem);
/// ```
///
/// Material Design 3 Expressive no longer recommends navigation drawers. Applications using the Expressive profile
/// should normally use an expanded [M3NavigationRail] for the same destination hierarchy.
///
/// Use a drawer for larger destination sets or grouped navigation. See
/// [Material Design navigation drawer](https://m3.material.io/components/navigation-drawer/overview).
@NotNullByDefault
public final class M3NavigationDrawer extends Control {
    /// The base style class for M3FX navigation drawers.
    public static final String STYLE_CLASS = "m3-navigation-drawer";

    /// The standard drawer pseudo-class.
    private static final PseudoClass STANDARD_PSEUDO_CLASS = PseudoClass.getPseudoClass("standard");

    /// The modal drawer pseudo-class.
    private static final PseudoClass MODAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("modal");

    /// The right-to-left layout pseudo-class.
    private static final PseudoClass RTL_PSEUDO_CLASS = PseudoClass.getPseudoClass("rtl");

    /// The pseudo-class applied to list items while they belong to a navigation drawer.
    private static final PseudoClass NAVIGATION_DRAWER_ITEM_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("navigation-drawer");

    /// The default spacing between top-level drawer items.
    private static final double DEFAULT_ITEM_SPACING = 0.0;

    /// The live, mutable, ordered top-level drawer content.
    ///
    /// The list rejects `null` elements and reports mutations through the `ObservableList` change API. Direct
    /// list items and drawer groups participate in navigation; other nodes are structural content. Nodes are owned
    /// by the drawer while displayed and must not belong to another parent.
    private final ObservableList<Node> items = M3ObservableLists.nonNullElementList("item");

    /// Notifies accessibility clients when focus moves between visible drawer rows.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    /// The currently selected navigation drawer item.
    private final ReadOnlyObjectWrapper<@Nullable M3ListItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected drawer list items in child order.
    private final ObservableList<M3ListItem> selectedItems = M3ObservableLists.nonNullElementList("selectedItem");

    /// The read-only selected drawer list item view.
    private final @UnmodifiableView ObservableList<M3ListItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// The spacing between adjacent top-level drawer entries in logical pixels.
    ///
    /// Values must be finite and non-negative. This spacing does not alter the child spacing inside a
    /// [M3NavigationDrawerGroup].
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty itemSpacing;

    /// The drawer presentation and color treatment.
    ///
    /// A direct assignment of `null` is replaced with [M3NavigationDrawerVariant#STANDARD].
    ///
    /// @defaultValue [M3NavigationDrawerVariant#STANDARD]
    private final ObjectProperty<M3NavigationDrawerVariant> drawerVariant =
            new SimpleObjectProperty<>(this, "variant", M3NavigationDrawerVariant.STANDARD) {
                /// Updates variant pseudo-classes when the variant changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3NavigationDrawerVariant.STANDARD);
                        return;
                    }
                    updateVariantPseudoClasses();
                }
            };

    /// Whether selection may be empty while a reachable destination exists.
    ///
    /// Setting the value to `false` selects the first reachable destination if selection is empty.
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

    /// Cached direct drawer content with expanded groups flattened into visible rows.
    private final ObservableList<Node> flattenedItems = FXCollections.observableArrayList();

    /// Cached drawer list items including children of collapsed groups.
    private final List<M3ListItem> allItems = new ArrayList<>();

    /// Reusable storage for computing selected items without allocating on every refresh.
    private final List<M3ListItem> selectedItemsScratch = new ArrayList<>();

    /// Handles selected-state invalidation for every installed drawer item.
    private final InvalidationListener selectedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property && property.getBean() instanceof M3ListItem item) {
            handleItemSelectedChanged(item, item.isSelected());
        }
    };

    /// Handles reachability invalidation for every installed drawer item.
    private final InvalidationListener reachabilityInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property && property.getBean() instanceof M3ListItem item) {
            handleItemReachabilityChanged(item);
        }
    };

    /// Handles child-list changes for every installed drawer group.
    private final ListChangeListener<M3ListItem> groupItemsListener = this::handleGroupItemsChanged;

    /// Handles expanded-state invalidation for every installed drawer group.
    private final InvalidationListener groupExpandedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property
                && property.getBean() instanceof M3NavigationDrawerGroup group) {
            handleGroupExpandedChanged(group);
        }
    };

    /// Handles item actions by selecting the fired item.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// The lazily activated printable-key prefix used for drawer type-ahead focus navigation.
    private final M3TypeAheadState typeAheadState = new M3TypeAheadState(this);

    /// Updates installed item listeners when drawer content changes.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                uninstallChild(child);
            }
            for (Node child : change.getAddedSubList()) {
                installChild(child);
            }
        }
        refreshContentCaches();
        typeAheadState.clear();
        enforceSelectionPolicy();
        notifyDrawerContentChanged();
    };

    /// Whether the drawer is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty standard drawer that requires selection when a reachable destination is added.
    public M3NavigationDrawer() {
        initialize();
    }

    /// Returns the live mutable top-level drawer content.
    ///
    /// Mutations are observed immediately and insertion order determines top-level layout and traversal. The list
    /// rejects `null`. It does not perform an explicit duplicate check, but each entry is a JavaFX node and must
    /// occur only once and must not simultaneously belong to another parent. Child destinations of a
    /// [M3NavigationDrawerGroup] are ordered at the position of their group.
    ///
    /// @return the live mutable top-level content list
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Returns the navigation drawer presentation variant.
    ///
    /// @return the current drawer variant
    public final M3NavigationDrawerVariant getVariant() {
        return drawerVariant.get();
    }

    /// Sets the navigation drawer presentation variant.
    ///
    /// @param variant the drawer variant
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3NavigationDrawerVariant variant) {
        drawerVariant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the navigation drawer variant property.
    ///
    /// @return the drawer variant property
    public final ObjectProperty<M3NavigationDrawerVariant> variantProperty() {
        return drawerVariant;
    }


    /// Returns an unmodifiable observable view of selected destinations in hierarchy order.
    ///
    /// The returned view is live and contains zero or one item.
    ///
    /// @return the live unmodifiable selected-item view
    public final @UnmodifiableView ObservableList<M3ListItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the selected drawer list item.
    ///
    /// @return the selected drawer list item, or `null` when no item is selected
    public final @Nullable M3ListItem getSelectedItem() {
        return selectedItem.get();
    }

    public final ReadOnlyObjectProperty<@Nullable M3ListItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the child index of the selected drawer list item, or `-1` when no item is selected.
    ///
    /// @return the flattened child index of the selected drawer list item, or `-1` when no item is selected
    public final int getSelectedIndex() {
        @Nullable M3ListItem item = getSelectedItem();
        return item == null ? -1 : flattenedContent().indexOf(item);
    }

    /// Returns whether this drawer allows all list items to be unselected.
    ///
    /// @return `true` when all drawer list items may be unselected
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this drawer allows all list items to be unselected.
    ///
    /// @param allowEmptySelection whether all drawer list items may be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a drawer list item that belongs to this drawer.
    ///
    /// @param item the drawer list item to select
    /// @throws NullPointerException if `item` is `null`
    /// @throws IllegalArgumentException if `item` is not a reachable member of this drawer
    public final void select(M3ListItem item) {
        Objects.requireNonNull(item, "item");
        if (!containsListItem(item)) {
            throw new IllegalArgumentException("item must belong to this navigation drawer");
        }
        if (!isSelectableDrawerItem(item)) {
            throw new IllegalArgumentException("item must be selectable");
        }
        selectItem(item);
    }

    /// Selects the drawer list item at the given child index.
    ///
    /// @param index the flattened child index of the drawer list item
    /// @throws IndexOutOfBoundsException if `index` is outside the visible flattened content
    /// @throws IllegalArgumentException if the indexed node is not a selectable [M3ListItem]
    public final void selectIndex(int index) {
        Node child = flattenedContent().get(index);
        if (child instanceof M3ListItem item) {
            select(item);
            return;
        }
        throw new IllegalArgumentException("child at index is not an M3ListItem");
    }

    /// Selects the first drawer list item when one exists.
    public final void selectFirst() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        M3ListItem firstItem = firstListItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Selects the last drawer list item when one exists.
    public final void selectLast() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3ListItem lastItem = M3SelectionNavigation.last(flattenedContent(), M3ListItem.class);
        if (lastItem != null) {
            selectItem(lastItem);
        }
    }

    /// Selects the next drawer list item after the current selected item, wrapping at the end.
    public final void selectNext() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3ListItem nextItem =
                M3SelectionNavigation.next(flattenedContent(), getSelectedItem(), M3ListItem.class);
        if (nextItem != null) {
            selectItem(nextItem);
        }
    }

    /// Selects the previous drawer list item before the current selected item, wrapping at the start.
    public final void selectPrevious() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3ListItem previousItem =
                M3SelectionNavigation.previous(flattenedContent(), getSelectedItem(), M3ListItem.class);
        if (previousItem != null) {
            selectItem(previousItem);
        }
    }

    /// Returns the spacing between top-level drawer items.
    ///
    /// @return the spacing between top-level drawer items in logical pixels
    public final double getItemSpacing() {
        return itemSpacing == null ? DEFAULT_ITEM_SPACING : itemSpacing.get();
    }

    /// Sets the spacing between top-level drawer items.
    ///
    /// @param itemSpacing the spacing between top-level drawer items in logical pixels
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

    /// Clears the current selection if empty selection is allowed.
    ///
    /// Otherwise, this method preserves or restores the first reachable destination.
    public final void clearSelection() {
        if (!isAllowEmptySelection()) {
            selectFirstItemIfNeeded();
            return;
        }
        selectItem(null);
    }

    /// Returns the user-agent stylesheet for M3FX navigation drawers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-drawer.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the CSS metadata for this control
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for navigation drawer content and selection state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        ObservableList<Node> content = flattenedContent();
        return switch (attribute) {
            case ITEM_COUNT -> content.size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(content, parameters);
            case FOCUS_NODE -> accessibleFocusNode(content);
            case MULTIPLE_SELECTION -> false;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for drawer list items.
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

    /// Adds base style classes and installs content listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        updateVariantPseudoClasses();
        updateOrientationPseudoClass();
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(
                this,
                this::requestAccessibleFocus,
                this::showAccessibleItem,
                this::containsAccessibleRevealTarget
        );
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        addEventHandler(KeyEvent.KEY_TYPED, this::handleTypeAheadKeyTyped);
        getItems().addListener(childrenListener);
        effectiveNodeOrientationProperty().addListener(observable -> updateOrientationPseudoClass());
        focusNotifier.start();
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                typeAheadState.clear();
            }
        });
    }

    /// Updates pseudo-classes representing the current drawer variant.
    private void updateVariantPseudoClasses() {
        M3NavigationDrawerVariant currentVariant = getVariant();
        pseudoClassStateChanged(STANDARD_PSEUDO_CLASS, currentVariant == M3NavigationDrawerVariant.STANDARD);
        pseudoClassStateChanged(MODAL_PSEUDO_CLASS, currentVariant == M3NavigationDrawerVariant.MODAL);
    }

    /// Updates the pseudo-class used for logical trailing-edge container corners.
    private void updateOrientationPseudoClass() {
        pseudoClassStateChanged(RTL_PSEUDO_CLASS, M3NodeLayout.isRightToLeft(this));
    }

    /// Applies keyboard navigation across enabled drawer items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (M3FocusTraversal.consumeNavigationKeyIfFocusOwnerInsideTextInput(this, event, true, true)) {
            return;
        }

        if (handleGroupDisclosureKey(event)) {
            return;
        }

        ObservableList<Node> content = flattenedContent();
        @Nullable M3ListItem anchor = M3SelectionNavigation.focusAnchor(content, getSelectedItem(), M3ListItem.class);
        if (M3SelectionNavigation.handleKeySelection(
                event,
                this,
                content,
                anchor,
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
                content,
                anchor,
                M3ListItem.class,
                this::select
        );
    }

    /// Moves focus to the next drawer item whose text matches the printable-key search prefix.
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

        if (!M3Accessible.showItem(this, target)) {
            return;
        }
        select(target);
        notifyAccessibleFocusChanged();
        event.consume();
    }

    /// Returns the next enabled visible drawer item matching the normalized type-ahead prefix.
    private @Nullable M3ListItem typeAheadTarget(String prefix) {
        ObservableList<Node> content = flattenedContent();
        @Nullable M3ListItem anchor = M3SelectionNavigation.focusAnchor(content, getSelectedItem(), M3ListItem.class);
        return M3SelectionNavigation.typeAheadTarget(
                content,
                anchor,
                M3ListItem.class,
                prefix,
                M3ListItem::getHeadlineText
        );
    }

    /// Handles left and right arrow disclosure behavior for focused or selected drawer groups.
    private boolean handleGroupDisclosureKey(KeyEvent event) {
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return false;
        }

        KeyCode code = event.getCode();
        if (code != KeyCode.LEFT && code != KeyCode.RIGHT) {
            return false;
        }

        ObservableList<Node> content = flattenedContent();
        @Nullable M3ListItem anchor = M3SelectionNavigation.focusAnchor(content, getSelectedItem(), M3ListItem.class);
        if (anchor == null) {
            return false;
        }

        boolean rightToLeft = M3NodeLayout.isRightToLeft(this);
        KeyCode expandKey = rightToLeft ? KeyCode.LEFT : KeyCode.RIGHT;
        if (code == expandKey) {
            @Nullable M3NavigationDrawerGroup headerGroup = groupForHeader(anchor);
            if (headerGroup != null && !headerGroup.isExpanded()) {
                headerGroup.setExpanded(true);
                selectAndFocusItem(headerGroup.getHeaderItem());
                event.consume();
                return true;
            }
            return false;
        }

        @Nullable M3NavigationDrawerGroup headerGroup = groupForHeader(anchor);
        if (headerGroup != null && headerGroup.isExpanded()) {
            headerGroup.setExpanded(false);
            selectAndFocusItem(headerGroup.getHeaderItem());
            event.consume();
            return true;
        }

        @Nullable M3NavigationDrawerGroup childGroup = groupForChild(anchor);
        if (childGroup != null && childGroup.isExpanded()) {
            childGroup.setExpanded(false);
            selectAndFocusItem(childGroup.getHeaderItem());
            event.consume();
            return true;
        }

        return false;
    }

    /// Applies the selected drawer item supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        @Nullable M3ListItem item = accessibleSelectionTarget(parameters);
        if (item == null) {
            clearSelection();
        } else {
            select(item);
        }
    }

    /// Focuses the drawer item supplied by an accessibility client, expanding a group when needed.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to a drawer item or nested target
    final boolean showAccessibleItem(Object... parameters) {
        ObservableList<Node> content = flattenedContent();
        if (parameters.length == 0) {
            if (M3Accessible.showItem(this, accessibleFocusNode(content))) {
                notifyAccessibleFocusChanged();
                return true;
            }
            return false;
        }

        @Nullable Node item = accessibleActionItem(parameters);
        if (item == null) {
            if (M3Accessible.showIndexedItem(this, content, parameters)) {
                notifyAccessibleFocusChanged();
                return true;
            }
            return false;
        }

        if (M3Accessible.showResolvedAccessibleActionTarget(this, item, parameters)
                || M3Accessible.showItem(this, item)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Moves focus to the current drawer accessibility focus target when it can be shown.
    ///
    /// @return `true` when the drawer focus target accepted focus
    final boolean requestAccessibleFocus() {
        if (M3Accessible.showItem(this, accessibleFocusNode())) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the drawer focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the active drawer focus target, or the selected visible item when focus is outside the drawer.
    private @Nullable Node accessibleFocusNode() {
        return accessibleFocusNode(flattenedContent());
    }

    /// Returns the active drawer focus target for a flattened drawer content snapshot.
    private @Nullable Node accessibleFocusNode(ObservableList<Node> content) {
        @Nullable Node currentFocusTarget = M3Accessible.currentFocusTarget(this, content);
        if (currentFocusTarget != null) {
            return currentFocusTarget;
        }
        return M3Accessible.focusTarget(M3SelectionNavigation.focusTarget(
                content,
                getSelectedItem(),
                M3ListItem.class
        ));
    }

    /// Returns the list item referenced by accessibility selection parameters.
    private @Nullable M3ListItem accessibleSelectionTarget(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : flattenedContent()) {
            if (child instanceof M3ListItem item
                    && isSelectableDrawerItem(item)
                    && M3Accessible.containsSelectionTarget(item, parameters)) {
                return item;
            }
        }

        for (Node child : getItems()) {
            if (child instanceof M3NavigationDrawerGroup group) {
                @Nullable M3ListItem groupItem = accessibleGroupSelectionTarget(group, parameters);
                if (groupItem != null) {
                    return groupItem;
                }
            }
        }
        return null;
    }

    /// Returns the group item referenced by accessibility selection parameters.
    private @Nullable M3ListItem accessibleGroupSelectionTarget(
            M3NavigationDrawerGroup group,
            Object... parameters
    ) {
        M3ListItem headerItem = group.getHeaderItem();
        if (isSelectableDrawerItem(headerItem) && M3Accessible.containsSelectionTarget(headerItem, parameters)) {
            return headerItem;
        }

        for (M3ListItem item : group.getItems()) {
            if (M3Accessible.isEffectivelyReachable(item) && M3Accessible.containsSelectionTarget(item, parameters)) {
                group.setExpanded(true);
                if (isSelectableDrawerItem(item)) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns whether this drawer can reveal the supplied accessibility target.
    private boolean containsAccessibleRevealTarget(@Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return containsAccessibleRevealNode(node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (containsAccessibleRevealTarget(value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (containsAccessibleRevealTarget(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether this drawer owns the supplied reveal node directly or through a collapsed group.
    private boolean containsAccessibleRevealNode(Node node) {
        for (Node child : getItems()) {
            if (!M3Accessible.containsUnrevealableActionNodeTarget(child, node)
                    && (M3Accessible.containsNodeTarget(child, node)
                    || M3Accessible.containsAccessibleActionTarget(child, node))) {
                return true;
            }
            if (child instanceof M3NavigationDrawerGroup group && containsGroupRevealNode(group, node)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a drawer group owns the supplied reveal node through any header or child row.
    private boolean containsGroupRevealNode(M3NavigationDrawerGroup group, Node node) {
        M3ListItem headerItem = group.getHeaderItem();
        if (!M3Accessible.containsUnrevealableActionNodeTarget(headerItem, node)
                && (M3Accessible.containsNodeTarget(headerItem, node)
                || M3Accessible.containsAccessibleActionTarget(headerItem, node))) {
            return true;
        }
        for (M3ListItem item : group.getItems()) {
            if (!M3Accessible.containsUnrevealableActionNodeTarget(item, node)
                    && (M3Accessible.containsNodeTarget(item, node)
                    || M3Accessible.containsAccessibleActionTarget(item, node))) {
                return true;
            }
        }
        return false;
    }

    /// Returns the drawer content node referenced by accessibility action parameters.
    private @Nullable Node accessibleActionItem(Object... parameters) {
        if (parameters.length == 0) {
            return null;
        }

        if (parameters[0] instanceof Number) {
            return M3Accessible.itemAt(flattenedContent(), parameters);
        }

        for (Node child : getItems()) {
            @Nullable Node target = M3Accessible.actionItem(child, parameters);
            if (target != null) {
                return target;
            }
            if (child instanceof M3NavigationDrawerGroup group) {
                @Nullable Node groupItem = accessibleGroupActionItem(group, parameters);
                if (groupItem != null) {
                    return groupItem;
                }
            }
        }
        return null;
    }

    /// Returns the group content node referenced by accessibility action parameters.
    private @Nullable Node accessibleGroupActionItem(M3NavigationDrawerGroup group, Object... parameters) {
        M3ListItem headerItem = group.getHeaderItem();
        @Nullable Node headerTarget = M3Accessible.actionItem(headerItem, parameters);
        if (headerTarget != null) {
            return headerTarget;
        }
        if (M3Accessible.containsAccessibleActionTarget(headerItem, parameters)) {
            return headerItem;
        }

        for (M3ListItem item : group.getItems()) {
            if (M3Accessible.isEffectivelyReachable(item)
                    && !M3Accessible.containsUnrevealableActionNodeTarget(item, parameters)
                    && (M3Accessible.containsNodeTarget(item, parameters)
                    || M3Accessible.containsAccessibleActionTarget(item, parameters))) {
                group.expandForAccessibleReveal();
                @Nullable Node target = M3Accessible.actionItem(item, parameters);
                return target == null ? item : target;
            }
        }
        return null;
    }

    /// Installs listeners on one drawer child node.
    private void installChild(Node child) {
        if (child instanceof M3ListItem item) {
            installItem(item);
        } else if (child instanceof M3NavigationDrawerGroup group) {
            installGroup(group);
        }
    }

    /// Removes listeners from one drawer child node.
    private void uninstallChild(Node child) {
        if (child instanceof M3ListItem item) {
            uninstallItem(item);
            item.setSelected(false);
        } else if (child instanceof M3NavigationDrawerGroup group) {
            uninstallGroup(group);
        }
    }

    /// Installs action and selected-state listeners on a drawer item.
    private void installItem(M3ListItem item) {
        item.pseudoClassStateChanged(NAVIGATION_DRAWER_ITEM_PSEUDO_CLASS, true);
        item.addEventHandler(ActionEvent.ACTION, itemActionHandler);
        item.selectedProperty().addListener(selectedInvalidation);
        item.disabledProperty().addListener(reachabilityInvalidation);
        item.visibleProperty().addListener(reachabilityInvalidation);
    }

    /// Installs action, selection, and content listeners on a nested drawer group.
    private void installGroup(M3NavigationDrawerGroup group) {
        installItem(group.getHeaderItem());
        for (M3ListItem item : group.getItems()) {
            installItem(item);
        }

        group.getItems().addListener(groupItemsListener);
        group.expandedProperty().addListener(groupExpandedInvalidation);
    }

    /// Removes action and selected-state listeners from a drawer item.
    private void uninstallItem(M3ListItem item) {
        item.pseudoClassStateChanged(NAVIGATION_DRAWER_ITEM_PSEUDO_CLASS, false);
        item.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        item.selectedProperty().removeListener(selectedInvalidation);
        item.disabledProperty().removeListener(reachabilityInvalidation);
        item.visibleProperty().removeListener(reachabilityInvalidation);
    }

    /// Removes action, selection, and content listeners from a nested drawer group.
    private void uninstallGroup(M3NavigationDrawerGroup group) {
        group.getItems().removeListener(groupItemsListener);
        group.expandedProperty().removeListener(groupExpandedInvalidation);

        uninstallItem(group.getHeaderItem());
        group.getHeaderItem().setSelected(false);
        for (M3ListItem item : group.getItems()) {
            uninstallItem(item);
            item.setSelected(false);
        }
    }

    /// Updates installed item listeners and content caches after one drawer group changes its child list.
    private void handleGroupItemsChanged(ListChangeListener.Change<? extends M3ListItem> change) {
        while (change.next()) {
            for (M3ListItem item : change.getRemoved()) {
                uninstallItem(item);
                item.setSelected(false);
            }
            for (M3ListItem item : change.getAddedSubList()) {
                installItem(item);
            }
        }
        refreshContentCaches();
        typeAheadState.clear();
        enforceSelectionPolicy();
        notifyDrawerContentChanged();
    }

    /// Updates visible content and selection after one drawer group expands or collapses.
    private void handleGroupExpandedChanged(M3NavigationDrawerGroup group) {
        typeAheadState.clear();
        if (!group.isExpanded()) {
            boolean restoreFocus = isFocusInsideGroupItems(group);
            if (group.getItems().contains(selectedItem.get())) {
                selectItem(group.getHeaderItem());
            }
            if (restoreFocus) {
                focusItem(group.getHeaderItem());
            }
        }
        refreshContentCaches();
        enforceSelectionPolicy();
        notifyDrawerContentChanged();
    }

    /// Selects the drawer item that fired an action event.
    private void handleItemAction(ActionEvent event) {
        if (event.getSource() instanceof M3ListItem item && containsListItem(item) && isSelectableDrawerItem(item)) {
            selectItem(item);
        }
    }

    /// Keeps externally changed item selected states mutually exclusive.
    private void handleItemSelectedChanged(M3ListItem item, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (!isSelectableDrawerItem(item)) {
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
    private void handleItemReachabilityChanged(M3ListItem item) {
        typeAheadState.clear();
        if (item.isSelected() && !isSelectableDrawerItem(item)) {
            clearItemSelection(item);
        }
        enforceSelectionPolicy();
        notifyDrawerContentChanged();
    }

    /// Clears one drawer item's selected state and refreshes selected item state.
    private void clearItemSelection(M3ListItem item) {
        clearItemSelectionWithoutRefresh(item);
        refreshSelectedItems();
    }

    /// Clears one drawer item's selected state without refreshing the aggregate selected item list.
    private void clearItemSelectionWithoutRefresh(M3ListItem item) {
        updatingSelection = true;
        try {
            item.setSelected(false);
        } finally {
            updatingSelection = false;
        }
    }

    /// Selects an item and clears selection from the remaining drawer items.
    private void selectItem(@Nullable M3ListItem item) {
        updatingSelection = true;
        try {
            for (M3ListItem listItem : allListItems()) {
                listItem.setSelected(listItem == item);
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedItems();
    }

    /// Selects the supplied drawer item and moves keyboard focus to it when it is reachable.
    ///
    /// @param item the drawer item to select and focus
    private void selectAndFocusItem(M3ListItem item) {
        selectItem(item);
        focusItem(item);
    }

    /// Moves keyboard focus to one drawer item when it is reachable.
    ///
    /// @param item the drawer item to focus
    private void focusItem(M3ListItem item) {
        if (M3Accessible.showItem(this, item)) {
            notifyAccessibleFocusChanged();
        }
    }

    /// Returns whether keyboard focus is currently inside one expanded group child item.
    ///
    /// @param group the navigation drawer group to inspect
    /// @return `true` when scene focus is inside one child item owned by the group
    private boolean isFocusInsideGroupItems(M3NavigationDrawerGroup group) {
        if (getScene() == null) {
            return false;
        }

        @Nullable Node focusOwner = getScene().getFocusOwner();
        if (focusOwner == null) {
            return false;
        }

        for (M3ListItem item : group.getItems()) {
            if (M3Accessible.containsNode(item, focusOwner)) {
                return true;
            }
        }
        return false;
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

    /// Refreshes selected item state from current child states.
    private void refreshSelectedItems() {
        selectedItemsScratch.clear();
        for (M3ListItem item : allListItems()) {
            if (item.isSelected()) {
                if (isSelectableDrawerItem(item)) {
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

    /// Selects the first drawer list item when selection is empty.
    private void selectFirstItemIfNeeded() {
        if (!selectedItems.isEmpty()) {
            return;
        }

        M3ListItem firstItem = firstListItem();
        if (firstItem != null) {
            selectItem(firstItem);
        }
    }

    /// Returns the first drawer list item child.
    private @Nullable M3ListItem firstListItem() {
        return M3SelectionNavigation.first(flattenedContent(), M3ListItem.class);
    }

    /// Returns whether a list item belongs to this drawer.
    private boolean containsListItem(M3ListItem item) {
        return allListItems().contains(item);
    }

    /// Returns whether a drawer list item can currently participate in selection.
    private boolean isSelectableDrawerItem(M3ListItem item) {
        return M3Accessible.isEffectivelyReachable(this)
                && M3Accessible.isEffectivelyReachable(item)
                && flattenedContent().contains(item);
    }

    /// Returns the drawer group that owns the supplied header item.
    private @Nullable M3NavigationDrawerGroup groupForHeader(M3ListItem item) {
        for (Node child : getItems()) {
            if (child instanceof M3NavigationDrawerGroup group && group.getHeaderItem() == item) {
                return group;
            }
        }
        return null;
    }

    /// Returns the expanded drawer group that owns the supplied child item.
    private @Nullable M3NavigationDrawerGroup groupForChild(M3ListItem item) {
        for (Node child : getItems()) {
            if (child instanceof M3NavigationDrawerGroup group
                    && group.isExpanded()
                    && group.getItems().contains(item)) {
                return group;
            }
        }
        return null;
    }

    /// Returns direct drawer content with expanded groups flattened into visible rows.
    private ObservableList<Node> flattenedContent() {
        return flattenedItems;
    }

    /// Returns all drawer list items in content order, including collapsed group children.
    private List<M3ListItem> allListItems() {
        return allItems;
    }

    /// Rebuilds cached visible and complete drawer content after structural or disclosure changes.
    private void refreshContentCaches() {
        flattenedItems.clear();
        allItems.clear();
        for (Node child : getItems()) {
            if (child instanceof M3ListItem item) {
                flattenedItems.add(item);
                allItems.add(item);
            } else if (child instanceof M3NavigationDrawerGroup group) {
                M3ListItem headerItem = group.getHeaderItem();
                flattenedItems.add(headerItem);
                allItems.add(headerItem);
                allItems.addAll(group.getItems());
                if (group.isExpanded()) {
                    flattenedItems.addAll(group.getItems());
                }
            } else {
                flattenedItems.add(child);
            }
        }
    }

    /// Notifies accessibility clients that drawer content geometry changed.
    private void notifyDrawerContentChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Creates the default Material Design 3 navigation drawer skin.
    ///
    /// @return the default Material Design 3 navigation drawer skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3NavigationDrawerSkin(this);
    }

    /// CSS metadata for navigation drawer styleable properties.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the top-level item spacing token.
        private static final CssMetaData<M3NavigationDrawer, Number> ITEM_SPACING =
                new CssMetaData<>("-m3-item-spacing", SizeConverter.getInstance(), DEFAULT_ITEM_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationDrawer control) {
                        return M3Css.isSettable(control.itemSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationDrawer control) {
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
