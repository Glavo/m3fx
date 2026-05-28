// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3CarouselSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;

/// A Material Design 3 carousel for horizontally browsing arbitrary item nodes.
///
/// `M3Carousel` manages an ordered item list, selected index, keyboard navigation, wrap-around behavior, pointer
/// selection, and animated movement through the visible item track. It can host any JavaFX node, allowing cards,
/// media previews, or custom content to use Material carousel selection behavior.
///
/// See [Material Design carousel](https://m3.material.io/components/carousel/overview).
@NotNullByDefault
public class M3Carousel extends Control {
    /// The base style class for M3FX carousels.
    public static final String STYLE_CLASS = "m3-carousel";

    /// The style class applied to the internal scroll viewport.
    public static final String VIEWPORT_STYLE_CLASS = "m3-carousel-viewport";

    /// The style class applied to the internal item track.
    public static final String TRACK_STYLE_CLASS = "m3-carousel-track";

    /// The style class applied to each carousel item node.
    public static final String ITEM_STYLE_CLASS = "m3-carousel-item";

    /// The style class applied to the selected carousel item node.
    public static final String SELECTED_ITEM_STYLE_CLASS = "m3-carousel-selected-item";

    /// The pseudo-class applied to the selected carousel item node.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The mutable carousel item list.
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    // The selected item index, or `-1` when no item is selected.
    private final IntegerProperty selectedIndex = new SimpleIntegerProperty(this, "selectedIndex", -1) {
        /// Applies selection changes and keeps the index inside the current item range.
        @Override
        protected void invalidated() {
            if (updatingSelection) {
                return;
            }
            applySelectedIndex(get(), true);
        }
    };

    // Whether keyboard previous and next navigation wraps around list edges.
    private final BooleanProperty wrapAround = new SimpleBooleanProperty(this, "wrapAround", true);

    // Whether programmatic selection changes animate viewport scrolling.
    private final BooleanProperty animatedScroll = new SimpleBooleanProperty(this, "animatedScroll", true);

    // The currently selected item.
    private final ReadOnlyObjectWrapper<@Nullable Node> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected item exposed as an immutable observable list for accessibility clients.
    private final ObservableList<Node> selectedItems = FXCollections.observableArrayList();

    /// The read-only selected item view.
    private final @UnmodifiableView ObservableList<Node> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// Updates item installation, selection invariants, and accessibility metadata when items change.
    private final ListChangeListener<Node> itemsListener = change -> {
        while (change.next()) {
            for (Node removed : change.getRemoved()) {
                uninstallItem(removed);
            }
            for (Node added : change.getAddedSubList()) {
                installItem(added);
            }
        }
        applySelectedIndex(getSelectedIndex(), false);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    };

    /// Selects clicked items.
    private final javafx.event.EventHandler<MouseEvent> itemMouseHandler = this::handleItemMouseClicked;

    /// Whether the selection property is being updated from normalization logic.
    private boolean updatingSelection;

    /// Creates an empty carousel.
    public M3Carousel() {
        initialize();
    }

    /// Creates a carousel containing the supplied item nodes.
    ///
    /// @param items the item nodes displayed by the carousel
    public M3Carousel(Node... items) {
        initialize();
        addItems(items);
    }

    /// Returns the mutable carousel item list.
    ///
    /// @return the mutable carousel item list
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Adds one carousel item.
    ///
    /// @param item the item node to add
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds carousel items.
    ///
    /// @param items the item nodes to add
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all carousel items.
    ///
    /// @param items the replacement item nodes
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all carousel items.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the selected item index, or `-1` when no item is selected.
    ///
    /// @return the selected item index, or `-1` when selection is empty
    public final int getSelectedIndex() {
        return selectedIndex.get();
    }

    /// Sets the selected item index, or `-1` to clear selection.
    ///
    /// @param selectedIndex the selected item index, or `-1` to clear selection
    public final void setSelectedIndex(int selectedIndex) {
        this.selectedIndex.set(selectedIndex);
    }

    /// Returns the selected item index property.
    ///
    /// @return the selected item index property
    public final IntegerProperty selectedIndexProperty() {
        return selectedIndex;
    }

    /// Returns the selected item, or `null` when no item is selected.
    ///
    /// @return the selected item node, or `null` when selection is empty
    public final @Nullable Node getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the selected item property.
    ///
    /// @return the read-only selected item property
    public final ReadOnlyObjectProperty<@Nullable Node> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// Returns the selected item as an immutable observable list.
    ///
    /// @return an immutable observable list containing the selected item, or empty when selection is empty
    public final @UnmodifiableView ObservableList<Node> getSelectedItems() {
        return selectedItemsView;
    }

    /// Returns whether keyboard previous and next navigation wraps around item edges.
    ///
    /// @return `true` when keyboard navigation wraps around carousel edges
    public final boolean isWrapAround() {
        return wrapAround.get();
    }

    /// Sets whether keyboard previous and next navigation wraps around item edges.
    ///
    /// @param wrapAround whether keyboard navigation should wrap around carousel edges
    public final void setWrapAround(boolean wrapAround) {
        this.wrapAround.set(wrapAround);
    }

    /// Returns the wrap-around navigation property.
    ///
    /// @return the wrap-around navigation property
    public final BooleanProperty wrapAroundProperty() {
        return wrapAround;
    }

    /// Returns whether selection changes animate viewport scrolling.
    ///
    /// @return `true` when selection changes animate viewport scrolling
    public final boolean isAnimatedScroll() {
        return animatedScroll.get();
    }

    /// Sets whether selection changes animate viewport scrolling.
    ///
    /// @param animatedScroll whether selection changes should animate viewport scrolling
    public final void setAnimatedScroll(boolean animatedScroll) {
        this.animatedScroll.set(animatedScroll);
    }

    /// Returns the animated viewport scrolling property.
    ///
    /// @return the animated viewport scrolling property
    public final BooleanProperty animatedScrollProperty() {
        return animatedScroll;
    }

    /// Selects the supplied item node.
    ///
    /// @param item the item node to select
    public final void select(Node item) {
        Objects.requireNonNull(item, "item");
        int index = getItems().indexOf(item);
        if (index < 0) {
            throw new IllegalArgumentException("item must belong to this carousel");
        }
        selectIndex(index);
    }

    /// Selects the item at the supplied index.
    ///
    /// @param index the item index to select
    public final void selectIndex(int index) {
        setSelectedIndex(index);
    }

    /// Selects the first item when one exists.
    public final void selectFirst() {
        int index = firstSelectableIndex();
        if (index >= 0) {
            selectIndex(index);
        }
    }

    /// Selects the last item when one exists.
    public final void selectLast() {
        int index = lastSelectableIndex();
        if (index >= 0) {
            selectIndex(index);
        }
    }

    /// Selects the next enabled and visible item.
    public final void selectNext() {
        int index = relativeSelectableIndex(1);
        if (index >= 0) {
            selectIndex(index);
        }
    }

    /// Selects the previous enabled and visible item.
    public final void selectPrevious() {
        int index = relativeSelectableIndex(-1);
        if (index >= 0) {
            selectIndex(index);
        }
    }

    /// Clears the current selection.
    public final void clearSelection() {
        setSelectedIndex(-1);
    }

    /// Scrolls the selected item into view using the configured animation policy.
    public final void scrollSelectedItemIntoView() {
        scrollSelectedItemIntoView(isAnimatedScroll());
    }

    /// Scrolls the selected item into view.
    ///
    /// @param animated whether the viewport scroll should animate
    public final void scrollSelectedItemIntoView(boolean animated) {
        Skin<?> skin = getSkin();
        if (skin instanceof M3CarouselSkin carouselSkin) {
            carouselSkin.scrollSelectedItemIntoView(animated);
        }
    }

    /// Returns the user-agent stylesheet for M3FX carousels.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("carousel.css");
    }

    /// Returns accessibility attributes for carousel items and selection.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case FOCUS_NODE -> accessibleFocusNode();
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> false;
            case SELECTED_ITEMS -> selectedItemsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection and reveal actions.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            case SET_SELECTED_ITEMS -> setAccessibleSelectedItems(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 carousel skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3CarouselSkin(this);
    }

    /// Adds base styles, accessibility role, and input behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        setFocusTraversable(true);
        getItems().addListener(itemsListener);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
    }

    /// Handles keyboard selection and scrolling.
    private void handleNavigationKeyPressed(KeyEvent event) {
        boolean handled = switch (event.getCode()) {
            case LEFT -> {
                selectHorizontal(false);
                yield true;
            }
            case RIGHT -> {
                selectHorizontal(true);
                yield true;
            }
            case HOME -> {
                selectFirst();
                yield true;
            }
            case END -> {
                selectLast();
                yield true;
            }
            default -> false;
        };
        if (handled) {
            requestFocusOnSelectedItem();
            event.consume();
        }
    }

    /// Selects the visually previous or next carousel item for horizontal arrow keys.
    private void selectHorizontal(boolean rightKey) {
        if (getSelectedIndex() < 0) {
            if (rightKey) {
                selectFirst();
            } else {
                selectLast();
            }
            return;
        }

        boolean forward = M3SelectionNavigation.isRightToLeft(this) != rightKey;
        if (forward) {
            selectNext();
        } else {
            selectPrevious();
        }
    }

    /// Selects a clicked carousel item.
    private void handleItemMouseClicked(MouseEvent event) {
        Objects.requireNonNull(event, "event");
        if (event.getButton() != MouseButton.PRIMARY || event.isConsumed()) {
            return;
        }
        Object source = event.getSource();
        if (source instanceof Node item && getItems().contains(item) && !item.isDisabled()) {
            select(item);
        }
    }

    /// Shows an item requested by an accessibility client.
    private void showAccessibleItem(Object... parameters) {
        @Nullable Node target = accessibleTarget(parameters);
        if (target != null) {
            select(target);
            M3Accessible.showItem(target);
            return;
        }
        scrollSelectedItemIntoView();
    }

    /// Returns the selected carousel item focus target, or this carousel when no item can receive focus.
    private @Nullable Node accessibleFocusNode() {
        @Nullable Node selectedFocusTarget = M3Accessible.focusTarget(getSelectedItem());
        return selectedFocusTarget != null ? selectedFocusTarget : M3Accessible.focusTarget(this);
    }

    /// Requests focus on the accessible carousel focus target.
    private void focusAccessibleNode() {
        @Nullable Node focusTarget = accessibleFocusNode();
        if (focusTarget != null) {
            focusTarget.requestFocus();
        }
    }

    /// Applies accessible single selection parameters.
    private void setAccessibleSelectedItems(Object... parameters) {
        @Nullable Node target = accessibleTarget(parameters);
        if (target == null) {
            clearSelection();
        } else {
            select(target);
        }
    }

    /// Returns the first item referenced by accessibility parameters.
    private @Nullable Node accessibleTarget(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        @Nullable Node byIndex = M3Accessible.itemAt(getItems(), parameters);
        if (byIndex != null) {
            return byIndex;
        }
        for (Node item : getItems()) {
            if (M3Accessible.containsSelectionTarget(item, parameters)) {
                return item;
            }
        }
        return null;
    }

    /// Installs carousel behavior and styles on one item.
    private void installItem(Node item) {
        M3ControlStyles.add(item, ITEM_STYLE_CLASS);
        item.addEventHandler(MouseEvent.MOUSE_CLICKED, itemMouseHandler);
    }

    /// Removes carousel behavior and transient styles from one item.
    private void uninstallItem(Node item) {
        item.removeEventHandler(MouseEvent.MOUSE_CLICKED, itemMouseHandler);
        item.getStyleClass().remove(ITEM_STYLE_CLASS);
        item.getStyleClass().remove(SELECTED_ITEM_STYLE_CLASS);
        item.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
    }

    /// Applies a selected index after normalizing it against the current item list.
    private void applySelectedIndex(int requestedIndex, boolean scroll) {
        int normalizedIndex = normalizeIndex(requestedIndex);
        if (normalizedIndex != getSelectedIndex()) {
            updatingSelection = true;
            try {
                selectedIndex.set(normalizedIndex);
            } finally {
                updatingSelection = false;
            }
        }

        @Nullable Node previousItem = selectedItem.get();
        @Nullable Node nextItem = normalizedIndex < 0 ? null : getItems().get(normalizedIndex);
        updateItemSelectionStyles(normalizedIndex);
        selectedItem.set(nextItem);
        selectedItems.setAll(nextItem == null ? java.util.List.of() : java.util.List.of(nextItem));
        if (previousItem != nextItem) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        }
        if (scroll && nextItem != null) {
            scrollSelectedItemIntoView();
        }
    }

    /// Applies selected item style classes and pseudo-classes.
    private void updateItemSelectionStyles(int selectedIndex) {
        for (int index = 0; index < getItems().size(); index++) {
            Node item = getItems().get(index);
            boolean selected = index == selectedIndex;
            if (selected) {
                M3ControlStyles.add(item, SELECTED_ITEM_STYLE_CLASS);
            } else {
                item.getStyleClass().remove(SELECTED_ITEM_STYLE_CLASS);
            }
            item.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
        }
    }

    /// Returns the valid selected index for the current item list.
    private int normalizeIndex(int index) {
        if (getItems().isEmpty() || index < 0) {
            return -1;
        }
        return Math.min(index, getItems().size() - 1);
    }

    /// Requests focus on the selected item when it can accept focus.
    private void requestFocusOnSelectedItem() {
        @Nullable Node item = getSelectedItem();
        if (item != null && item.isFocusTraversable() && !item.isDisabled()) {
            item.requestFocus();
        }
    }

    /// Returns the first enabled and visible item index.
    private int firstSelectableIndex() {
        for (int index = 0; index < getItems().size(); index++) {
            if (isSelectable(getItems().get(index))) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the last enabled and visible item index.
    private int lastSelectableIndex() {
        for (int index = getItems().size() - 1; index >= 0; index--) {
            if (isSelectable(getItems().get(index))) {
                return index;
            }
        }
        return -1;
    }

    /// Returns a selectable index relative to the current selection.
    private int relativeSelectableIndex(int direction) {
        int itemCount = getItems().size();
        if (itemCount == 0) {
            return -1;
        }

        int startIndex = getSelectedIndex();
        if (startIndex < 0) {
            return direction >= 0 ? firstSelectableIndex() : lastSelectableIndex();
        }

        for (int offset = 1; offset <= itemCount; offset++) {
            int candidate = startIndex + direction * offset;
            if (isWrapAround()) {
                candidate = Math.floorMod(candidate, itemCount);
            } else if (candidate < 0 || candidate >= itemCount) {
                return -1;
            }
            if (isSelectable(getItems().get(candidate))) {
                return candidate;
            }
        }
        return -1;
    }

    /// Returns whether a node can be targeted by navigation.
    private static boolean isSelectable(Node item) {
        return item.isVisible() && !item.isDisabled();
    }

    /// Validates a carousel item array.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
