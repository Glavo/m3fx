// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.WeakEventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3CarouselSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 control for browsing an ordered sequence of item nodes.
///
/// A carousel owns a live, ordered list of arbitrary JavaFX nodes and maintains at most one selected item.
/// Selection can be changed by pointer, keyboard, accessibility action, or the selection methods. Selecting an item
/// does not activate application-specific behavior within that item; nested controls retain their own action
/// semantics.
///
/// Items installed in the carousel become focus traversable so keyboard traversal operates on items rather than
/// the container. Their previous focus-traversable values are restored when they are removed. Invisible, unmanaged,
/// or disabled items are not selectable. The no-argument constructor creates an empty uncontained carousel with no
/// selection, wrapping disabled, and animated programmatic scrolling enabled.
///
/// See [Material Design carousel](https://m3.material.io/components/carousel/overview).
@NotNullByDefault
public final class M3Carousel extends Control {
    /// The default carousel layout.
    private static final M3CarouselLayout DEFAULT_CAROUSEL_LAYOUT = M3CarouselLayout.UNCONTAINED;

    /// The base style class for M3FX carousels.
    public static final String STYLE_CLASS = "m3-carousel";

    /// The style class applied to the carousel scroll viewport.
    public static final String VIEWPORT_STYLE_CLASS = "m3-carousel-viewport";

    /// The style class applied to the carousel item track.
    public static final String TRACK_STYLE_CLASS = "m3-carousel-track";

    /// The style class applied to each carousel item node.
    public static final String ITEM_STYLE_CLASS = "m3-carousel-item";

    /// The style class applied to the selected carousel item node.
    public static final String SELECTED_ITEM_STYLE_CLASS = "m3-carousel-selected-item";

    /// The pseudo-class applied to the selected carousel item node.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// Creates an empty uncontained carousel with no selection.
    public M3Carousel() {
        initialize();
    }

    /// The Material layout strategy used to size and position items.
    ///
    /// The default value is [M3CarouselLayout#UNCONTAINED]. A direct `null` assignment restores the default; bound
    /// values must be non-null.
    ///
    /// @defaultValue [M3CarouselLayout#UNCONTAINED]
    private final ObjectProperty<M3CarouselLayout> carouselLayout =
            new SimpleObjectProperty<>(this, "carouselLayout", DEFAULT_CAROUSEL_LAYOUT) {
                /// Normalizes null assignments and refreshes layout-specific styles.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_CAROUSEL_LAYOUT);
                        return;
                    }
                    updateCarouselLayoutStyle();
                    requestLayout();
                }
            };

    /// Returns the Material layout strategy used by this carousel.
    ///
    /// @return the current carousel layout
    public final M3CarouselLayout getCarouselLayout() {
        return carouselLayout.get();
    }

    /// Sets the Material layout strategy used by this carousel.
    ///
    /// A `null` assignment restores [M3CarouselLayout#UNCONTAINED].
    ///
    /// @param carouselLayout the carousel layout, or `null` to restore the default
    public final void setCarouselLayout(@Nullable M3CarouselLayout carouselLayout) {
        this.carouselLayout.set(carouselLayout);
    }

    /// Returns the observable property that stores the carousel layout strategy.
    ///
    /// The property can be observed and bound. Its default value is [M3CarouselLayout#UNCONTAINED], and a `null`
    /// assignment restores that default.
    ///
    /// @return the carousel layout property
    public final ObjectProperty<M3CarouselLayout> carouselLayoutProperty() {
        return carouselLayout;
    }

    /// The selected item index, or `-1` when selection is empty.
    ///
    /// The default value is `-1`. Negative assignments clear selection. Values beyond the end of the list are
    /// bounded to the last item; if that item cannot be selected, the nearest reachable item is selected instead.
    /// The value is also normalized when items are added, removed, hidden, unmanaged, or disabled.
    ///
    /// @defaultValue `-1`
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

    /// Returns the selected item index, or `-1` when no item is selected.
    ///
    /// @return the selected item index, or `-1` when selection is empty
    public final int getSelectedIndex() {
        return selectedIndex.get();
    }

    /// Requests selection of the item at the specified index.
    ///
    /// A negative value clears selection. A value beyond the last item is bounded to the current list, and an
    /// unreachable target is replaced with the nearest reachable item. Use [selectIndex][#selectIndex(int)] when
    /// invalid indices or unreachable targets should be reported instead of normalized.
    ///
    /// @param selectedIndex the requested selected index, or a negative value to clear selection
    public final void setSelectedIndex(int selectedIndex) {
        this.selectedIndex.set(selectedIndex);
    }

    /// Returns the observable property that stores the selected item index.
    ///
    /// The property can be observed and bound. Its default value is `-1`. Negative values clear selection, and
    /// other values are normalized to a reachable index in the current item list.
    ///
    /// @return the selected index property
    public final IntegerProperty selectedIndexProperty() {
        return selectedIndex;
    }

    /// Whether relative keyboard and method-based navigation wraps around the first and last selectable items.
    ///
    /// The default value is `false`.
    ///
    /// @defaultValue `false`
    private final BooleanProperty wrapAround = new SimpleBooleanProperty(this, "wrapAround", false);

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

    /// Returns the observable property that controls edge wrapping during relative navigation.
    ///
    /// The property can be observed and bound. Its default value is `false`.
    ///
    /// @return the wrap-around property
    public final BooleanProperty wrapAroundProperty() {
        return wrapAround;
    }

    /// Whether selection changes request animated scrolling to the selected item.
    ///
    /// The default value is `true`. Global or inherited reduced-motion settings may still suppress animation.
    ///
    /// @defaultValue `true`
    private final BooleanProperty animatedScroll = new SimpleBooleanProperty(this, "animatedScroll", true);

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

    /// Returns the observable property that requests animated scrolling after selection changes.
    ///
    /// The property can be observed and bound. Its default value is `true`; reduced-motion settings may still
    /// suppress animation.
    ///
    /// @return the animated-scroll property
    public final BooleanProperty animatedScrollProperty() {
        return animatedScroll;
    }

    /// The currently selected item.
    private final ReadOnlyObjectWrapper<@Nullable Node> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// Returns the selected item, or `null` when no item is selected.
    ///
    /// @return the selected item node, or `null` when selection is empty
    public final @Nullable Node getSelectedItem() {
        return selectedItem.get();
    }

    /// Returns the read-only observable property that reports the selected item.
    ///
    /// The property can be observed and used as a binding source. Its default value is `null`, and its value is
    /// kept consistent with [selectedIndex][#selectedIndexProperty()] and the current item list.
    ///
    /// @return the read-only selected item property
    public final ReadOnlyObjectProperty<@Nullable Node> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    /// The live, mutable item list in visual order.
    ///
    /// The list rejects `null`, preserves insertion order, and is observed for subsequent changes. An item is a
    /// scene-graph node and cannot simultaneously be a child of another parent.
    private final ObservableList<Node> items = M3ObservableLists.nonNullElementList("item");

    /// The focus-traversable value restored when an application-owned item leaves this carousel.
    private final Map<Node, Boolean> originalItemFocusTraversable = new IdentityHashMap<>();

    /// The selected item exposed as an immutable observable list for accessibility clients.
    private final ObservableList<Node> selectedItems = M3ObservableLists.nonNullElementList("selectedItem");

    /// The read-only selected item view.
    private final @UnmodifiableView ObservableList<Node> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    /// Observes descendant focus changes for the public `FOCUS_NODE` attribute.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    /// Refreshes selection when a carousel item becomes visible, hidden, enabled, or disabled.
    private final InvalidationListener itemReachabilityListener = observable -> applySelectedIndex(getSelectedIndex(), false);

    /// Weak wrapper installed on application-owned item reachability properties.
    private final WeakInvalidationListener weakItemReachabilityListener =
            new WeakInvalidationListener(itemReachabilityListener);

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
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    };

    /// Selects clicked items.
    private final javafx.event.EventHandler<MouseEvent> itemMouseHandler = this::handleItemMouseClicked;

    /// Weak wrapper installed on application-owned item nodes.
    private final WeakEventHandler<MouseEvent> weakItemMouseHandler = new WeakEventHandler<>(itemMouseHandler);

    /// Whether the selection property is being updated from normalization logic.
    private boolean updatingSelection;

    /// Returns the live list of carousel items.
    ///
    /// Each installed item becomes focus traversable so Tab and arrow-key navigation operate on items rather than the
    /// carousel container. Removing an item restores the focus-traversable value it had when installed.
    ///
    /// The list preserves insertion order and rejects `null` elements. Changes update selection immediately.
    ///
    /// @return the live, mutable item list
    public final ObservableList<Node> getItems() {
        return items;
    }

    /// Returns the selected item as a read-only observable list.
    ///
    /// The returned live view contains either zero or one element and updates whenever selection changes. Attempts
    /// to mutate it fail with [UnsupportedOperationException].
    ///
    /// @return the live, read-only selected-item view
    public final @UnmodifiableView ObservableList<Node> getSelectedItems() {
        return selectedItemsView;
    }

    /// Selects the supplied item node.
    ///
    /// @param item the item node to select
    /// @throws NullPointerException     if `item` is `null`
    /// @throws IllegalArgumentException if `item` is not in [items][#getItems()]
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
    /// @throws IndexOutOfBoundsException if `index` is outside the item list
    /// @throws IllegalArgumentException  if the item at the index is not reachable for selection
    public final void selectIndex(int index) {
        Node item = getItems().get(index);
        if (!isSelectable(item)) {
            throw new IllegalArgumentException("item at index is not reachable for selection");
        }
        setSelectedIndex(index);
    }

    /// Selects the first reachable item, if one exists.
    public final void selectFirst() {
        int index = firstSelectableIndex();
        if (index >= 0) {
            selectIndex(index);
        }
    }

    /// Selects the last reachable item, if one exists.
    public final void selectLast() {
        int index = lastSelectableIndex();
        if (index >= 0) {
            selectIndex(index);
        }
    }

    /// Selects the next reachable item according to [wrapAround][#wrapAroundProperty()].
    public final void selectNext() {
        int index = relativeSelectableIndex(1);
        if (index >= 0) {
            selectIndex(index);
        }
    }

    /// Selects the previous reachable item according to [wrapAround][#wrapAroundProperty()].
    public final void selectPrevious() {
        int index = relativeSelectableIndex(-1);
        if (index >= 0) {
            selectIndex(index);
        }
    }

    /// Clears the current selection.
    ///
    /// Calling this method when selection is already empty has no effect.
    public final void clearSelection() {
        setSelectedIndex(-1);
    }

    /// Requests that the selected item be scrolled into view using the configured animation policy.
    ///
    /// This method has no effect when selection is empty or the control is not ready to present a viewport.
    public final void scrollSelectedItemIntoView() {
        scrollSelectedItemIntoView(isAnimatedScroll());
    }

    /// Requests that the selected item be scrolled into view.
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
    ///
    /// @throws NullPointerException if `attribute` is `null`
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
    ///
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

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
        M3ControlStyles.initialize(this, STYLE_CLASS);
        updateCarouselLayoutStyle();
        setAccessibleRole(AccessibleRole.LIST_VIEW);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        setFocusTraversable(false);
        focusNotifier.start();
        getItems().addListener(itemsListener);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
    }

    /// Applies the style class associated with the current Material carousel layout.
    private void updateCarouselLayoutStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getCarouselLayout().styleClass(),
                M3CarouselLayout.MULTI_BROWSE.styleClass(),
                M3CarouselLayout.UNCONTAINED.styleClass(),
                M3CarouselLayout.UNCONTAINED_MULTI_ASPECT_RATIO.styleClass(),
                M3CarouselLayout.HERO.styleClass(),
                M3CarouselLayout.CENTER_ALIGNED_HERO.styleClass(),
                M3CarouselLayout.FULL_SCREEN.styleClass()
        );
    }

    /// Handles keyboard selection and scrolling.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (M3FocusTraversal.consumeNavigationKeyIfFocusOwnerInsideTextInput(this, event, true, false)) {
            return;
        }

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
            case TAB -> {
                @Nullable Node focusOwner = getScene() == null ? null : getScene().getFocusOwner();
                @Nullable Node focusedItem = focusOwner == null
                        ? null
                        : M3Accessible.containingItem(getItems(), focusOwner);
                int currentIndex = focusedItem == null ? getSelectedIndex() : getItems().indexOf(focusedItem);
                int direction = event.isShiftDown() ? -1 : 1;
                int targetIndex = -1;
                for (int index = currentIndex + direction;
                     index >= 0 && index < getItems().size();
                     index += direction) {
                    if (isSelectable(getItems().get(index))) {
                        targetIndex = index;
                        break;
                    }
                }
                if (targetIndex >= 0) {
                    selectIndex(targetIndex);
                    yield true;
                }
                yield false;
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

        boolean forward = M3NodeLayout.isRightToLeft(this) != rightKey;
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
        if (source instanceof Node item && getItems().contains(item) && isSelectable(item)) {
            select(item);
        }
    }

    /// Shows an item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested target
    final boolean showAccessibleItem(Object... parameters) {
        if (parameters.length == 0) {
            boolean focused = focusAccessibleNode();
            if (focused) {
                scrollSelectedItemIntoView();
            }
            return focused;
        }

        @Nullable Node selectedTarget = M3Accessible.containingItem(getItems(), parameters);
        if (selectedTarget != null) {
            if (!isSelectable(selectedTarget)) {
                return false;
            }
            boolean nestedTarget = referencesNestedActionTarget(selectedTarget, parameters);
            boolean shown = nestedTarget
                    ? M3Accessible.showAccessibleActionTarget(this, selectedTarget, parameters)
                    : false;
            if (nestedTarget && !shown) {
                return false;
            }
            select(selectedTarget);
            scrollSelectedItemIntoView();
            if (!shown) {
                shown = M3Accessible.showAccessibleActionTarget(this, selectedTarget, parameters);
                if (!shown) {
                    @Nullable Node focusTarget = M3Accessible.actionItem(getItems(), parameters);
                    shown = M3Accessible.showItem(this, focusTarget == null ? selectedTarget : focusTarget);
                }
            }
            if (shown) {
                notifyAccessibleFocusChanged();
            }
            return shown;
        }
        scrollSelectedItemIntoView();
        return false;
    }

    /// Returns the current carousel item focus target, or the selected or first selectable item when focus is outside
    /// the carousel.
    private @Nullable Node accessibleFocusNode() {
        @Nullable Node currentFocusTarget = M3Accessible.currentFocusTarget(this, getItems());
        if (currentFocusTarget != null) {
            return currentFocusTarget;
        }
        @Nullable Node item = getSelectedItem();
        if (item == null) {
            int firstIndex = firstSelectableIndex();
            item = firstIndex < 0 ? null : getItems().get(firstIndex);
        }
        return M3Accessible.focusTarget(item);
    }

    /// Requests focus on the accessible carousel focus target.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleNode() {
        @Nullable Node item = getSelectedItem();
        if (item == null) {
            int firstIndex = firstSelectableIndex();
            if (firstIndex >= 0) {
                applySelectedIndex(firstIndex, false);
                item = getSelectedItem();
            }
        }
        @Nullable Node focusTarget = accessibleFocusNode();
        if (item == null
                || focusTarget == null
                || (!M3Accessible.containsNode(item, focusTarget)
                && !M3Accessible.containsAccessibleActionTarget(item, focusTarget))) {
            focusTarget = M3Accessible.focusTarget(item);
        }
        if (M3Accessible.showItem(this, focusTarget)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Applies accessible single selection parameters.
    private void setAccessibleSelectedItems(Object... parameters) {
        @Nullable Node target = accessibleSelectionTarget(parameters);
        if (target == null) {
            clearSelection();
        } else if (isSelectable(target)) {
            select(target);
        }
    }

    /// Returns the first item referenced by accessibility selection parameters.
    private @Nullable Node accessibleSelectionTarget(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return null;
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return M3Accessible.itemAt(getItems(), parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = accessibleSelectionTarget(parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the item referenced by one accessibility selection parameter.
    private @Nullable Node accessibleSelectionTarget(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return M3Accessible.itemAt(getItems(), number);
        }
        if (parameter instanceof Node node) {
            if (!M3Accessible.isEffectivelyReachable(node)) {
                return null;
            }
            for (Node item : getItems()) {
                if (M3Accessible.containsNode(item, node)) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = accessibleSelectionTarget(value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = accessibleSelectionTarget(value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns whether action parameters reference a nested accessibility target inside the supplied item.
    private static boolean referencesNestedActionTarget(Node item, Object... parameters) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (referencesNestedActionTarget(item, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one action parameter references a nested accessibility target inside the supplied item.
    private static boolean referencesNestedActionTarget(Node item, @Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return node != item
                    && (M3Accessible.containsNode(item, node)
                    || M3Accessible.containsAccessibleActionTarget(item, node));
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (referencesNestedActionTarget(item, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (referencesNestedActionTarget(item, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Installs carousel behavior and styles on one item.
    private void installItem(Node item) {
        M3ControlStyles.add(item, ITEM_STYLE_CLASS);
        originalItemFocusTraversable.put(item, item.isFocusTraversable());
        item.setFocusTraversable(true);
        item.addEventHandler(MouseEvent.MOUSE_CLICKED, weakItemMouseHandler);
        item.visibleProperty().addListener(weakItemReachabilityListener);
        item.managedProperty().addListener(weakItemReachabilityListener);
        item.disabledProperty().addListener(weakItemReachabilityListener);
    }

    /// Removes carousel behavior and transient styles from one item.
    private void uninstallItem(Node item) {
        item.visibleProperty().removeListener(weakItemReachabilityListener);
        item.managedProperty().removeListener(weakItemReachabilityListener);
        item.disabledProperty().removeListener(weakItemReachabilityListener);
        item.removeEventHandler(MouseEvent.MOUSE_CLICKED, weakItemMouseHandler);
        @Nullable Boolean focusTraversable = originalItemFocusTraversable.remove(item);
        if (focusTraversable != null) {
            item.setFocusTraversable(focusTraversable);
        }
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
        if (previousItem != null && previousItem != nextItem) {
            previousItem.getStyleClass().remove(SELECTED_ITEM_STYLE_CLASS);
            previousItem.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
        }
        if (nextItem != null) {
            M3ControlStyles.add(nextItem, SELECTED_ITEM_STYLE_CLASS);
            nextItem.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
        }
        selectedItem.set(nextItem);
        if (nextItem == null) {
            selectedItems.clear();
        } else if (selectedItems.isEmpty()) {
            selectedItems.add(nextItem);
        } else if (selectedItems.get(0) != nextItem) {
            selectedItems.set(0, nextItem);
        }
        if (previousItem != nextItem) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            notifyAccessibleFocusChanged();
        }
        if (scroll && nextItem != null) {
            scrollSelectedItemIntoView();
        }
    }

    /// Notifies accessibility clients that the carousel focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the valid selected index for the current item list.
    private int normalizeIndex(int index) {
        if (getItems().isEmpty() || index < 0) {
            return -1;
        }
        int boundedIndex = Math.min(index, getItems().size() - 1);
        if (isSelectableItem(getItems().get(boundedIndex))) {
            return boundedIndex;
        }
        for (int nextIndex = boundedIndex + 1; nextIndex < getItems().size(); nextIndex++) {
            if (isSelectableItem(getItems().get(nextIndex))) {
                return nextIndex;
            }
        }
        for (int previousIndex = boundedIndex - 1; previousIndex >= 0; previousIndex--) {
            if (isSelectableItem(getItems().get(previousIndex))) {
                return previousIndex;
            }
        }
        return -1;
    }

    /// Requests focus on the selected item when it can accept focus.
    private void requestFocusOnSelectedItem() {
        if (M3Accessible.showItem(this, getSelectedItem())) {
            notifyAccessibleFocusChanged();
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
    private boolean isSelectable(Node item) {
        return M3Accessible.isEffectivelyReachable(this) && isSelectableItem(item);
    }

    /// Returns whether an item node itself can participate in selection.
    private static boolean isSelectableItem(Node item) {
        return item.isVisible() && item.isManaged() && !item.isDisabled();
    }

}
