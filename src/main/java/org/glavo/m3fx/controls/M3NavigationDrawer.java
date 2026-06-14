// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3NavigationDrawerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 navigation drawer for persistent or modal destination lists.
///
/// `M3NavigationDrawer` hosts [M3ListItem] entries and [M3NavigationDrawerGroup] sections, tracks selected
/// items, and applies a drawer-specific selection policy across nested groups. It supports keyboard traversal,
/// empty-selection control, and JavaFX accessibility selection attributes while leaving application layout to the
/// surrounding container.
///
/// Use a drawer for larger destination sets or grouped navigation. See
/// [Material Design navigation drawer](https://m3.material.io/components/navigation-drawer/overview).
@NotNullByDefault
public class M3NavigationDrawer extends Control {
    /// The base style class for M3FX navigation drawers.
    public static final String STYLE_CLASS = "m3-navigation-drawer";

    /// The mutable navigation drawer content.
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between visible drawer rows.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    // The currently selected navigation drawer item.
    private final ReadOnlyObjectWrapper<@Nullable M3ListItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected drawer list items in child order.
    private final ObservableList<M3ListItem> selectedItems = FXCollections.observableArrayList();

    /// The read-only selected drawer list item view.
    private final @UnmodifiableView ObservableList<M3ListItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    // Whether the drawer allows all list items to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection") {
        /// Restores a selected item when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstItemIfNeeded();
            }
        }
    };

    /// The selected-state listeners installed on drawer list items.
    private final Map<M3ListItem, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

    /// The reachability listeners installed on drawer list items.
    private final Map<M3ListItem, ChangeListener<Boolean>> reachabilityListeners = new HashMap<>();

    /// The item-list listeners installed on nested drawer groups.
    private final Map<M3NavigationDrawerGroup, ListChangeListener<M3ListItem>> groupItemsListeners =
            new HashMap<>();

    /// The expanded-state listeners installed on nested drawer groups.
    private final Map<M3NavigationDrawerGroup, ChangeListener<Boolean>> groupExpandedListeners =
            new HashMap<>();

    /// Handles item actions by selecting the fired item.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// The current printable-key prefix used for drawer type-ahead focus navigation.
    private final StringBuilder typeAheadBuffer = new StringBuilder();

    /// Clears the type-ahead prefix after the user stops typing.
    private final PauseTransition typeAheadResetDelay = new PauseTransition();

    /// Updates type-ahead timing when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

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
        clearTypeAheadBuffer();
        enforceSelectionPolicy();
        notifyDrawerContentChanged();
    };

    /// Whether the drawer is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty navigation drawer.
    public M3NavigationDrawer() {
        initialize();
    }

    /// Creates a navigation drawer containing the supplied nodes.
    ///
    /// @param items the initial non-null drawer content nodes
    public M3NavigationDrawer(Node... items) {
        initialize();
        addItems(items);
    }

    /// Returns the mutable child list used as drawer content.
    ///
    /// @return the mutable drawer content list
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Adds one drawer content node.
    ///
    /// @param item the non-null drawer content node to append
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds drawer content nodes.
    ///
    /// @param items the non-null drawer content nodes to append
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all drawer content nodes.
    ///
    /// @param items the non-null drawer content nodes that replace the current content
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all drawer content nodes.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the selected drawer list items in child order.
    ///
    /// @return an unmodifiable observable view of selected drawer list items
    public final @UnmodifiableView ObservableList<M3ListItem> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns the selected drawer list item.
    ///
    /// @return the selected drawer list item, or `null` when no item is selected
    public final @Nullable M3ListItem getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the selected drawer list item property.
    ///
    /// @return the read-only selected drawer list item property
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

    /// Returns the empty-selection policy property.
    ///
    /// @return the writable empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a drawer list item that belongs to this drawer.
    ///
    /// @param item the drawer list item to select
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

    /// Clears the current selection when empty selection is allowed.
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

    /// Returns accessibility attributes for navigation drawer content and selection state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
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
    /// @param action the accessibility action to execute
    /// @param parameters optional action-specific parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(accessibleFocusNode());
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and installs content listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        addEventHandler(KeyEvent.KEY_TYPED, this::handleTypeAheadKeyTyped);
        getItems().addListener(childrenListener);
        focusNotifier.start();
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

    /// Applies keyboard navigation across enabled drawer items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (handleGroupDisclosureKey(event)) {
            return;
        }

        ObservableList<Node> content = flattenedContent();
        @Nullable M3ListItem anchor = M3SelectionNavigation.focusAnchor(content, getSelectedItem(), M3ListItem.class);
        if (M3SelectionNavigation.handleKeySelection(
                event,
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
        if (event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShortcutDown()) {
            return;
        }

        String character = event.getCharacter();
        if (character.length() != 1 || Character.isISOControl(character.charAt(0)) || character.isBlank()) {
            return;
        }

        appendTypeAheadCharacter(character);
        typeAheadResetDelay.setDuration(M3Animation.motionBehavior(this).typeAheadResetDelay());
        typeAheadResetDelay.playFromStart();
        @Nullable M3ListItem target = typeAheadTarget(typeAheadBuffer.toString());
        if (target == null && typeAheadBuffer.length() > 1) {
            resetTypeAheadBuffer(character);
            target = typeAheadTarget(typeAheadBuffer.toString());
        }
        if (target == null) {
            return;
        }

        if (target.isFocusTraversable()) {
            target.requestFocus();
        }
        select(target);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
        event.consume();
    }

    /// Appends one printable typed character to the current type-ahead prefix.
    private void appendTypeAheadCharacter(String character) {
        typeAheadBuffer.append(M3SelectionNavigation.normalizeTypeAheadText(character));
    }

    /// Replaces the current type-ahead prefix with one printable typed character.
    private void resetTypeAheadBuffer(String character) {
        clearTypeAheadBuffer();
        appendTypeAheadCharacter(character);
    }

    /// Clears buffered type-ahead text and stops the pending reset timer.
    private void clearTypeAheadBuffer() {
        typeAheadBuffer.setLength(0);
        typeAheadResetDelay.stop();
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
        KeyCode code = event.getCode();
        if (code != KeyCode.LEFT && code != KeyCode.RIGHT) {
            return false;
        }

        ObservableList<Node> content = flattenedContent();
        @Nullable M3ListItem anchor = M3SelectionNavigation.focusAnchor(content, getSelectedItem(), M3ListItem.class);
        if (anchor == null) {
            return false;
        }

        boolean rightToLeft = getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        KeyCode expandKey = rightToLeft ? KeyCode.LEFT : KeyCode.RIGHT;
        KeyCode collapseKey = rightToLeft ? KeyCode.RIGHT : KeyCode.LEFT;

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

        if (code != collapseKey) {
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
    private void showAccessibleItem(Object... parameters) {
        ObservableList<Node> content = flattenedContent();
        if (parameters.length == 0) {
            M3Accessible.showItem(accessibleFocusNode(content));
            return;
        }

        @Nullable Node item = accessibleActionItem(parameters);
        if (item == null) {
            M3Accessible.showItem(content, parameters);
        } else {
            M3Accessible.showItem(item);
        }
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
        if (isSelectableDrawerItem(headerItem) && M3Accessible.containsNodeTarget(headerItem, parameters)) {
            return headerItem;
        }

        for (M3ListItem item : group.getItems()) {
            if (M3Accessible.isEffectivelyReachable(item) && M3Accessible.containsNodeTarget(item, parameters)) {
                group.setExpanded(true);
                if (isSelectableDrawerItem(item)) {
                    return item;
                }
            }
        }
        return null;
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

        for (M3ListItem item : group.getItems()) {
            if (M3Accessible.isEffectivelyReachable(item) && M3Accessible.containsNodeTarget(item, parameters)) {
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
        if (selectedListeners.containsKey(item)) {
            return;
        }
        item.addEventHandler(ActionEvent.ACTION, itemActionHandler);
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleItemSelectedChanged(item, newValue);
        selectedListeners.put(item, listener);
        item.selectedProperty().addListener(listener);
        ChangeListener<Boolean> reachabilityListener = (observable, oldValue, newValue) ->
                handleItemReachabilityChanged(item);
        reachabilityListeners.put(item, reachabilityListener);
        item.disabledProperty().addListener(reachabilityListener);
        item.visibleProperty().addListener(reachabilityListener);
    }

    /// Installs action, selection, and content listeners on a nested drawer group.
    private void installGroup(M3NavigationDrawerGroup group) {
        installItem(group.getHeaderItem());
        for (M3ListItem item : group.getItems()) {
            installItem(item);
        }

        ListChangeListener<M3ListItem> itemsListener = change -> {
            while (change.next()) {
                for (M3ListItem item : change.getRemoved()) {
                    uninstallItem(item);
                    item.setSelected(false);
                }
                for (M3ListItem item : change.getAddedSubList()) {
                    installItem(item);
                }
            }
            clearTypeAheadBuffer();
            enforceSelectionPolicy();
            notifyDrawerContentChanged();
        };
        groupItemsListeners.put(group, itemsListener);
        group.getItems().addListener(itemsListener);

        ChangeListener<Boolean> expandedListener = (observable, oldValue, newValue) -> {
            clearTypeAheadBuffer();
            if (!newValue) {
                boolean restoreFocus = isFocusInsideGroupItems(group);
                if (group.getItems().contains(selectedItem.get())) {
                    selectItem(group.getHeaderItem());
                }
                if (restoreFocus) {
                    focusItem(group.getHeaderItem());
                }
            }
            enforceSelectionPolicy();
            notifyDrawerContentChanged();
        };
        groupExpandedListeners.put(group, expandedListener);
        group.expandedProperty().addListener(expandedListener);
    }

    /// Removes action and selected-state listeners from a drawer item.
    private void uninstallItem(M3ListItem item) {
        item.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        ChangeListener<Boolean> listener = selectedListeners.remove(item);
        if (listener != null) {
            item.selectedProperty().removeListener(listener);
        }
        ChangeListener<Boolean> reachabilityListener = reachabilityListeners.remove(item);
        if (reachabilityListener != null) {
            item.disabledProperty().removeListener(reachabilityListener);
            item.visibleProperty().removeListener(reachabilityListener);
        }
    }

    /// Removes action, selection, and content listeners from a nested drawer group.
    private void uninstallGroup(M3NavigationDrawerGroup group) {
        ListChangeListener<M3ListItem> itemsListener = groupItemsListeners.remove(group);
        if (itemsListener != null) {
            group.getItems().removeListener(itemsListener);
        }

        ChangeListener<Boolean> expandedListener = groupExpandedListeners.remove(group);
        if (expandedListener != null) {
            group.expandedProperty().removeListener(expandedListener);
        }

        uninstallItem(group.getHeaderItem());
        group.getHeaderItem().setSelected(false);
        for (M3ListItem item : group.getItems()) {
            uninstallItem(item);
            item.setSelected(false);
        }
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
                setItemSelected(item, false);
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
        clearTypeAheadBuffer();
        if (item.isSelected() && !isSelectableDrawerItem(item)) {
            setItemSelected(item, false);
        }
        enforceSelectionPolicy();
        notifyDrawerContentChanged();
    }

    /// Sets one drawer item's selected state and refreshes selected item state.
    private void setItemSelected(M3ListItem item, boolean selected) {
        setItemSelectedWithoutRefresh(item, selected);
        refreshSelectedItems();
    }

    /// Sets one drawer item's selected state without refreshing the aggregate selected item list.
    private void setItemSelectedWithoutRefresh(M3ListItem item, boolean selected) {
        updatingSelection = true;
        try {
            item.setSelected(selected);
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
        if (M3Accessible.canReach(item) && item.isFocusTraversable()) {
            item.requestFocus();
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
        List<M3ListItem> previousSelection = List.copyOf(selectedItems);
        selectedItems.clear();
        for (M3ListItem item : allListItems()) {
            if (item.isSelected()) {
                if (isSelectableDrawerItem(item)) {
                    selectedItems.add(item);
                } else {
                    setItemSelectedWithoutRefresh(item, false);
                }
            }
        }
        selectedItem.set(selectedItems.isEmpty() ? null : selectedItems.get(0));
        if (!selectedItems.equals(previousSelection)) {
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
        ObservableList<Node> content = FXCollections.observableArrayList();
        for (Node child : getItems()) {
            if (child instanceof M3NavigationDrawerGroup group) {
                content.add(group.getHeaderItem());
                if (group.isExpanded()) {
                    content.addAll(group.getItems());
                }
            } else {
                content.add(child);
            }
        }
        return content;
    }

    /// Returns all drawer list items in content order, including collapsed group children.
    private List<M3ListItem> allListItems() {
        List<M3ListItem> listItems = new ArrayList<>();
        for (Node child : getItems()) {
            if (child instanceof M3ListItem item) {
                listItems.add(item);
            } else if (child instanceof M3NavigationDrawerGroup group) {
                listItems.add(group.getHeaderItem());
                listItems.addAll(group.getItems());
            }
        }
        return listItems;
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

    /// Validates a drawer item array.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
