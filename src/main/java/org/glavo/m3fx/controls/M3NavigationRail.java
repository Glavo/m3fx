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
import javafx.css.PseudoClass;
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
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3NavigationRailSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 navigation rail for primary destinations in medium-width layouts.
///
/// `M3NavigationRail` arranges [M3NavigationItem] children vertically and manages their selected state. It
/// supports empty-selection control, read-only selected-item views, keyboard navigation, and JavaFX accessibility
/// selection attributes. Use a rail when there is enough horizontal space for persistent navigation but a full
/// drawer would be too wide.
///
/// See [Material Design navigation rails](https://m3.material.io/components/navigation-rail/overview).
@NotNullByDefault
public final class M3NavigationRail extends Control {
    /// The base style class for M3FX navigation rails.
    public static final String STYLE_CLASS = "m3-navigation-rail";

    /// The expanded pseudo-class used by navigation rail styling.
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    /// The standard expanded-rail pseudo-class.
    private static final PseudoClass STANDARD_PSEUDO_CLASS = PseudoClass.getPseudoClass("standard");

    /// The modal expanded-rail pseudo-class.
    private static final PseudoClass MODAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("modal");

    /// The optional full-width active-indicator pseudo-class.
    private static final PseudoClass FULL_WIDTH_INDICATOR_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("full-width-indicator");

    /// The narrow collapsed-rail pseudo-class.
    private static final PseudoClass NARROW_PSEUDO_CLASS = PseudoClass.getPseudoClass("narrow");

    /// The immersive hide-on-collapse pseudo-class.
    private static final PseudoClass HIDE_WHEN_COLLAPSED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("hide-when-collapsed");

    /// The centered destination-group pseudo-class.
    private static final PseudoClass ITEMS_CENTERED_PSEUDO_CLASS = PseudoClass.getPseudoClass("items-centered");

    /// The right-to-left layout pseudo-class.
    private static final PseudoClass RTL_PSEUDO_CLASS = PseudoClass.getPseudoClass("rtl");

    /// The default spacing between navigation rail items.
    private static final double DEFAULT_ITEM_SPACING = 8.0;

    /// The default collapsed navigation rail width.
    private static final double DEFAULT_COLLAPSED_CONTAINER_WIDTH = 80.0;

    /// The default expanded navigation rail width.
    private static final double DEFAULT_EXPANDED_CONTAINER_WIDTH = 280.0;

    /// The default minimum expanded navigation rail width.
    private static final double DEFAULT_EXPANDED_MINIMUM_CONTAINER_WIDTH = 220.0;

    /// The default maximum expanded navigation rail width.
    private static final double DEFAULT_EXPANDED_MAXIMUM_CONTAINER_WIDTH = 360.0;

    /// The default minimum distance between the rail header and destination items.
    private static final double DEFAULT_HEADER_SPACING = 40.0;

    /// The mutable navigation rail content.
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

    // The currently selected navigation item.
    private final ReadOnlyObjectWrapper<@Nullable M3NavigationItem> selectedItem =
            new ReadOnlyObjectWrapper<>(this, "selectedItem");

    /// The selected navigation items in child order.
    private final ObservableList<M3NavigationItem> selectedItems = M3ObservableLists.nonNullElementList("selectedItem");

    /// The read-only selected navigation item view.
    private final @UnmodifiableView ObservableList<M3NavigationItem> selectedItemsView =
            FXCollections.unmodifiableObservableList(selectedItems);

    // The styleable spacing between navigation item rows.
    private @Nullable StyleableDoubleProperty itemSpacing;

    /// The styleable collapsed navigation rail width.
    private @Nullable StyleableDoubleProperty collapsedContainerWidthStyleable;

    /// The styleable expanded navigation rail width.
    private @Nullable StyleableDoubleProperty expandedContainerWidthStyleable;

    /// The styleable minimum expanded navigation rail width.
    private @Nullable StyleableDoubleProperty expandedMinimumContainerWidthStyleable;

    /// The styleable maximum expanded navigation rail width.
    private @Nullable StyleableDoubleProperty expandedMaximumContainerWidthStyleable;

    /// The styleable spacing between the optional header and destination items.
    private @Nullable StyleableDoubleProperty headerSpacingStyleable;

    /// The optional menu and action area rendered above destination items.
    private final ObjectProperty<@Nullable Node> headerState = new SimpleObjectProperty<>(this, "header") {
        /// Requests a new skin layout when the header changes.
        @Override
        protected void invalidated() {
            requestLayout();
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        }
    };

    /// Whether expanded active indicators fill the available destination row width.
    private final BooleanProperty fullWidthIndicatorState = new SimpleBooleanProperty(this, "fullWidthIndicator") {
        /// Updates indicator geometry and the corresponding CSS pseudo-class.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(FULL_WIDTH_INDICATOR_PSEUDO_CLASS, get());
            requestNavigationItemLayouts();
        }
    };

    /// Whether the collapsed rail uses the 80-pixel narrow configuration.
    private final BooleanProperty narrowState = new SimpleBooleanProperty(this, "narrow") {
        /// Updates the width token selector when the collapsed configuration changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(NARROW_PSEUDO_CLASS, get());
            requestLayout();
        }
    };

    /// Whether an immersive expanded rail disappears instead of becoming a collapsed rail.
    private final BooleanProperty hideWhenCollapsedState = new SimpleBooleanProperty(this, "hideWhenCollapsed") {
        /// Updates the collapse target and CSS state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(HIDE_WHEN_COLLAPSED_PSEUDO_CLASS, get());
            requestLayout();
        }
    };

    /// Whether destination rows are centered in the space below the header.
    private final BooleanProperty itemsCenteredState = new SimpleBooleanProperty(this, "itemsCentered") {
        /// Updates destination-group alignment.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(ITEMS_CENTERED_PSEUDO_CLASS, get());
            requestLayout();
        }
    };

    /// Whether the rail presents expanded horizontal destination rows.
    private final BooleanProperty expandedState = new SimpleBooleanProperty(this, "expanded") {
        /// Updates the visual state and child item layouts.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, get());
            updateItemLayouts();
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            requestLayout();
        }
    };

    /// Backing property for the expanded rail presentation variant.
    private final ObjectProperty<M3NavigationRailVariant> railVariant =
            new SimpleObjectProperty<>(this, "variant", M3NavigationRailVariant.STANDARD) {
                /// Updates variant pseudo-classes when the variant changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3NavigationRailVariant.STANDARD);
                        return;
                    }
                    updateVariantPseudoClasses();
                }
            };

    // Whether the rail allows all navigation items to be unselected.
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

    /// Whether the navigation rail is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty navigation rail.
    public M3NavigationRail() {
        initialize();
    }

    /// Returns the mutable child list used as navigation rail items.
    ///
    /// @return the mutable navigation rail content list
    public final ObservableList<M3NavigationItem> getItems() {
        return items;
    }

    /// Returns the optional rail header rendered before destination items.
    ///
    /// The header is intended for a menu button, an optional floating action button, or a small container holding
    /// both. It is excluded from destination selection and arrow-key navigation.
    ///
    /// @return the current header node, or `null` when no header is installed
    public final @Nullable Node getHeader() {
        return headerState.get();
    }

    /// Sets the optional rail header rendered before destination items.
    ///
    /// @param header the header node, or `null` to remove it
    public final void setHeader(@Nullable Node header) {
        this.headerState.set(header);
    }

    /// Returns the optional rail header property.
    ///
    /// @return the writable header property
    public final ObjectProperty<@Nullable Node> headerProperty() {
        return headerState;
    }

    /// Returns whether expanded active indicators fill the destination row width.
    ///
    /// The default value is `false`, which follows the expressive navigation-rail specification by wrapping the
    /// icon, label, and badge with 16-pixel leading and trailing space. Set this property to `true` for the
    /// documented full-width customization.
    ///
    /// @return `true` when expanded indicators use the full destination row width
    public final boolean isFullWidthIndicator() {
        return fullWidthIndicatorState.get();
    }

    /// Sets whether expanded active indicators fill the destination row width.
    ///
    /// @param fullWidthIndicator whether expanded indicators use the full destination row width
    public final void setFullWidthIndicator(boolean fullWidthIndicator) {
        this.fullWidthIndicatorState.set(fullWidthIndicator);
    }

    /// Returns the expanded active-indicator width-mode property.
    ///
    /// @return the writable full-width indicator property
    public final BooleanProperty fullWidthIndicatorProperty() {
        return fullWidthIndicatorState;
    }

    /// Returns whether this rail uses the narrow collapsed configuration.
    ///
    /// A regular Material Design 3 Expressive collapsed rail is 96 pixels wide. The narrow configuration is
    /// 80 pixels wide and preserves the same destination indicator and target geometry. This property has no
    /// effect on the width of an expanded rail.
    ///
    /// @return `true` when the collapsed rail uses its narrow width token
    public final boolean isNarrow() {
        return narrowState.get();
    }

    /// Selects the regular or narrow collapsed configuration.
    ///
    /// @param narrow `true` for the narrow collapsed rail, or `false` for the regular collapsed rail
    public final void setNarrow(boolean narrow) {
        narrowState.set(narrow);
    }

    /// Returns the narrow collapsed-rail property.
    ///
    /// @return the writable narrow property
    public final BooleanProperty narrowProperty() {
        return narrowState;
    }

    /// Returns whether this rail disappears when its expanded state is cleared.
    ///
    /// The default value is `false`, so an expanded rail transitions into the persistent collapsed rail. Set this
    /// property only for the immersive configuration documented by Material Design 3 Expressive, where the rail
    /// is summoned by a menu action and must leave no collapsed rail behind. A regular collapsed rail should not
    /// be hidden.
    ///
    /// @return `true` when the collapsed target has zero width
    public final boolean isHideWhenCollapsed() {
        return hideWhenCollapsedState.get();
    }

    /// Selects whether clearing the expanded state collapses or hides the rail.
    ///
    /// @param hideWhenCollapsed `true` to hide an immersive expanded rail, or `false` to retain a collapsed rail
    public final void setHideWhenCollapsed(boolean hideWhenCollapsed) {
        hideWhenCollapsedState.set(hideWhenCollapsed);
    }

    /// Returns the immersive hide-on-collapse property.
    ///
    /// @return the writable hide-on-collapse property
    public final BooleanProperty hideWhenCollapsedProperty() {
        return hideWhenCollapsedState;
    }

    /// Returns whether destination items are centered vertically below the rail header.
    ///
    /// Header content, including menu buttons and floating action buttons, always remains top-aligned. When this
    /// property is `true`, only the destination group is centered in the remaining height. The default is
    /// `false`, which places destinations immediately after the header and its configured spacing.
    ///
    /// @return `true` when the destination group is centered
    public final boolean isItemsCentered() {
        return itemsCenteredState.get();
    }

    /// Selects top or center alignment for the destination group.
    ///
    /// @param itemsCentered `true` to center destinations, or `false` to top-align them
    public final void setItemsCentered(boolean itemsCentered) {
        itemsCenteredState.set(itemsCentered);
    }

    /// Returns the destination-group alignment property.
    ///
    /// @return the writable centered-items property
    public final BooleanProperty itemsCenteredProperty() {
        return itemsCenteredState;
    }

    /// Returns whether this navigation rail is expanded.
    ///
    /// @return true when destination labels are arranged horizontally in an expanded rail
    public final boolean isExpanded() {
        return expandedState.get();
    }

    /// Expands or collapses this navigation rail.
    ///
    /// @param expanded true to expand the rail, or false to collapse it
    public final void setExpanded(boolean expanded) {
        this.expandedState.set(expanded);
    }

    /// Returns the expanded state property.
    ///
    /// @return the writable expanded state property
    public final BooleanProperty expandedProperty() {
        return expandedState;
    }

    /// Returns the expanded rail presentation variant.
    ///
    /// @return the current rail variant
    public final M3NavigationRailVariant getVariant() {
        return railVariant.get();
    }

    /// Sets the expanded rail presentation variant.
    ///
    /// The variant affects surface treatment when the rail is expanded. Collapsed rails retain the collapsed
    /// navigation rail surface.
    ///
    /// @param variant the expanded rail variant
    public final void setVariant(M3NavigationRailVariant variant) {
        railVariant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the expanded rail variant property.
    ///
    /// @return the writable rail variant property
    public final ObjectProperty<M3NavigationRailVariant> variantProperty() {
        return railVariant;
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

    /// Returns the selected navigation item property.
    ///
    /// @return the read-only selected navigation item property
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

    /// Returns whether this rail allows all navigation items to be unselected.
    ///
    /// @return `true` when all navigation items may be unselected
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this rail allows all navigation items to be unselected.
    ///
    /// @param allowEmptySelection whether all navigation items may be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    ///
    /// @return the writable empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a navigation item that belongs to this rail.
    ///
    /// @param item the navigation item to select
    public final void select(M3NavigationItem item) {
        Objects.requireNonNull(item, "item");
        if (!getItems().contains(item)) {
            throw new IllegalArgumentException("item must belong to this navigation rail");
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

    /// Returns the spacing between navigation rail items.
    ///
    /// @return the spacing between navigation rail items in pixels
    public final double getItemSpacing() {
        return itemSpacing == null ? DEFAULT_ITEM_SPACING : itemSpacing.get();
    }

    /// Sets the spacing between navigation rail items.
    ///
    /// @param itemSpacing the spacing between navigation rail items in pixels
    public final void setItemSpacing(double itemSpacing) {
        itemSpacingProperty().set(M3Css.nonNegative(itemSpacing, "itemSpacing"));
    }

    /// Returns the spacing between navigation rail items property.
    ///
    /// @return the styleable item spacing property
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

    /// Returns the collapsed navigation rail width.
    ///
    /// @return the collapsed container width in pixels
    public final double getCollapsedContainerWidth() {
        return collapsedContainerWidthStyleable == null
                ? DEFAULT_COLLAPSED_CONTAINER_WIDTH
                : collapsedContainerWidthStyleable.get();
    }

    /// Sets the collapsed navigation rail width.
    ///
    /// @param collapsedContainerWidth the collapsed container width in pixels
    public final void setCollapsedContainerWidth(double collapsedContainerWidth) {
        collapsedContainerWidthProperty().set(
                M3Css.nonNegative(collapsedContainerWidth, "collapsedContainerWidth")
        );
    }

    /// Returns the collapsed navigation rail width property.
    ///
    /// @return the styleable collapsed container width property
    public final StyleableDoubleProperty collapsedContainerWidthProperty() {
        if (collapsedContainerWidthStyleable == null) {
            collapsedContainerWidthStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_COLLAPSED_CONTAINER_WIDTH,
                    this,
                    "collapsedContainerWidth",
                    StyleableProperties.COLLAPSED_CONTAINER_WIDTH,
                    this::requestLayout
            );
        }
        return collapsedContainerWidthStyleable;
    }

    /// Returns the minimum width accepted by the expanded rail.
    ///
    /// The Material Design 3 Expressive default is 220 pixels. Layout containers may resize an expanded rail
    /// between this value and [#getExpandedMaximumContainerWidth()]. If the configured minimum exceeds the
    /// maximum, the minimum takes precedence until the properties become consistent again.
    ///
    /// @return the expanded minimum container width in pixels
    public final double getExpandedMinimumContainerWidth() {
        return expandedMinimumContainerWidthStyleable == null
                ? DEFAULT_EXPANDED_MINIMUM_CONTAINER_WIDTH
                : expandedMinimumContainerWidthStyleable.get();
    }

    /// Sets the minimum width accepted by the expanded rail.
    ///
    /// @param expandedMinimumContainerWidth the non-negative expanded minimum width in pixels
    public final void setExpandedMinimumContainerWidth(double expandedMinimumContainerWidth) {
        expandedMinimumContainerWidthProperty().set(
                M3Css.nonNegative(expandedMinimumContainerWidth, "expandedMinimumContainerWidth")
        );
    }

    /// Returns the styleable expanded minimum-width property.
    ///
    /// @return the expanded minimum-width property
    public final StyleableDoubleProperty expandedMinimumContainerWidthProperty() {
        if (expandedMinimumContainerWidthStyleable == null) {
            expandedMinimumContainerWidthStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_EXPANDED_MINIMUM_CONTAINER_WIDTH,
                    this,
                    "expandedMinimumContainerWidth",
                    StyleableProperties.EXPANDED_MINIMUM_CONTAINER_WIDTH,
                    this::requestLayout
            );
        }
        return expandedMinimumContainerWidthStyleable;
    }

    /// Returns the expanded navigation rail width.
    ///
    /// This is the preferred expanded width. The skin clamps it to the resolved minimum and maximum widths when
    /// computing layout bounds.
    ///
    /// @return the preferred expanded container width in pixels
    public final double getExpandedContainerWidth() {
        return expandedContainerWidthStyleable == null
                ? DEFAULT_EXPANDED_CONTAINER_WIDTH
                : expandedContainerWidthStyleable.get();
    }

    /// Sets the expanded navigation rail width.
    ///
    /// @param expandedContainerWidth the expanded container width in pixels
    public final void setExpandedContainerWidth(double expandedContainerWidth) {
        expandedContainerWidthProperty().set(
                M3Css.nonNegative(expandedContainerWidth, "expandedContainerWidth")
        );
    }

    /// Returns the expanded navigation rail width property.
    ///
    /// @return the styleable expanded container width property
    public final StyleableDoubleProperty expandedContainerWidthProperty() {
        if (expandedContainerWidthStyleable == null) {
            expandedContainerWidthStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_EXPANDED_CONTAINER_WIDTH,
                    this,
                    "expandedContainerWidth",
                    StyleableProperties.EXPANDED_CONTAINER_WIDTH,
                    this::requestLayout
            );
        }
        return expandedContainerWidthStyleable;
    }

    /// Returns the maximum width accepted by the expanded rail.
    ///
    /// The Material Design 3 Expressive default is 360 pixels. When this value is less than the resolved minimum,
    /// the minimum width takes precedence.
    ///
    /// @return the expanded maximum container width in pixels
    public final double getExpandedMaximumContainerWidth() {
        return expandedMaximumContainerWidthStyleable == null
                ? DEFAULT_EXPANDED_MAXIMUM_CONTAINER_WIDTH
                : expandedMaximumContainerWidthStyleable.get();
    }

    /// Sets the maximum width accepted by the expanded rail.
    ///
    /// @param expandedMaximumContainerWidth the non-negative expanded maximum width in pixels
    public final void setExpandedMaximumContainerWidth(double expandedMaximumContainerWidth) {
        expandedMaximumContainerWidthProperty().set(
                M3Css.nonNegative(expandedMaximumContainerWidth, "expandedMaximumContainerWidth")
        );
    }

    /// Returns the styleable expanded maximum-width property.
    ///
    /// @return the expanded maximum-width property
    public final StyleableDoubleProperty expandedMaximumContainerWidthProperty() {
        if (expandedMaximumContainerWidthStyleable == null) {
            expandedMaximumContainerWidthStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_EXPANDED_MAXIMUM_CONTAINER_WIDTH,
                    this,
                    "expandedMaximumContainerWidth",
                    StyleableProperties.EXPANDED_MAXIMUM_CONTAINER_WIDTH,
                    this::requestLayout
            );
        }
        return expandedMaximumContainerWidthStyleable;
    }

    /// Returns the minimum spacing between the optional header and destination items.
    ///
    /// @return the header spacing in pixels
    public final double getHeaderSpacing() {
        return headerSpacingStyleable == null ? DEFAULT_HEADER_SPACING : headerSpacingStyleable.get();
    }

    /// Sets the minimum spacing between the optional header and destination items.
    ///
    /// @param headerSpacing the non-negative header spacing in pixels
    public final void setHeaderSpacing(double headerSpacing) {
        headerSpacingProperty().set(M3Css.nonNegative(headerSpacing, "headerSpacing"));
    }

    /// Returns the styleable header spacing property.
    ///
    /// @return the styleable header spacing property
    public final StyleableDoubleProperty headerSpacingProperty() {
        if (headerSpacingStyleable == null) {
            headerSpacingStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HEADER_SPACING,
                    this,
                    "headerSpacing",
                    StyleableProperties.HEADER_SPACING,
                    this::requestLayout
            );
        }
        return headerSpacingStyleable;
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection()) {
            selectFirstItemIfNeeded();
            return;
        }
        selectItem(null);
    }

    /// Returns the user-agent stylesheet for M3FX navigation rails.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-rail.css");
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

    /// Returns accessibility attributes for navigation rail content and selection state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isExpanded();
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
        pseudoClassStateChanged(FULL_WIDTH_INDICATOR_PSEUDO_CLASS, false);
        pseudoClassStateChanged(NARROW_PSEUDO_CLASS, false);
        pseudoClassStateChanged(HIDE_WHEN_COLLAPSED_PSEUDO_CLASS, false);
        pseudoClassStateChanged(ITEMS_CENTERED_PSEUDO_CLASS, false);
        updateVariantPseudoClasses();
        updateOrientationPseudoClass();
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleSelectionTarget, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(childrenListener);
        effectiveNodeOrientationProperty().addListener(observable -> updateOrientationPseudoClass());
        focusNotifier.start();
    }

    /// Updates pseudo-classes representing the expanded rail presentation variant.
    private void updateVariantPseudoClasses() {
        M3NavigationRailVariant currentVariant = getVariant();
        pseudoClassStateChanged(STANDARD_PSEUDO_CLASS, currentVariant == M3NavigationRailVariant.STANDARD);
        pseudoClassStateChanged(MODAL_PSEUDO_CLASS, currentVariant == M3NavigationRailVariant.MODAL);
    }

    /// Updates the pseudo-class representing effective right-to-left orientation.
    private void updateOrientationPseudoClass() {
        pseudoClassStateChanged(RTL_PSEUDO_CLASS, M3NodeLayout.isRightToLeft(this));
    }

    /// Applies keyboard navigation across enabled navigation items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeySelection(
                event,
                this,
                getItems(),
                M3SelectionNavigation.focusAnchor(getItems(), getSelectedItem(), M3NavigationItem.class),
                M3NavigationItem.class,
                false,
                true,
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
        item.setItemLayout(isExpanded()
                ? M3NavigationItemLayout.HORIZONTAL
                : M3NavigationItemLayout.VERTICAL);
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

    /// Applies the current collapsed or expanded layout to every navigation item child.
    private void updateItemLayouts() {
        M3NavigationItemLayout layout = isExpanded()
                ? M3NavigationItemLayout.HORIZONTAL
                : M3NavigationItemLayout.VERTICAL;
        for (Node child : getItems()) {
            if (child instanceof M3NavigationItem item) {
                item.setItemLayout(layout);
            }
        }
    }

    /// Requests a new layout from every installed navigation item.
    private void requestNavigationItemLayouts() {
        for (Node child : getItems()) {
            if (child instanceof M3NavigationItem item) {
                item.requestLayout();
            }
        }
        requestLayout();
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

    /// Creates the default Material Design 3 navigation rail skin.
    ///
    /// @return the default Material Design 3 navigation rail skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3NavigationRailSkin(this);
    }

    /// CSS metadata for navigation rail styleable properties.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the item spacing token.
        private static final CssMetaData<M3NavigationRail, Number> ITEM_SPACING =
                new CssMetaData<>("-m3-item-spacing", SizeConverter.getInstance(), DEFAULT_ITEM_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationRail control) {
                        return M3Css.isSettable(control.itemSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationRail control) {
                        return control.itemSpacingProperty();
                    }
                };

        /// CSS metadata for the collapsed container width token.
        private static final CssMetaData<M3NavigationRail, Number> COLLAPSED_CONTAINER_WIDTH =
                new CssMetaData<>(
                        "-m3-collapsed-container-width",
                        SizeConverter.getInstance(),
                        DEFAULT_COLLAPSED_CONTAINER_WIDTH
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationRail control) {
                        return M3Css.isSettable(control.collapsedContainerWidthProperty());
                    }

                    /// Returns the corresponding styleable property.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationRail control) {
                        return control.collapsedContainerWidthProperty();
                    }
                };

        /// CSS metadata for the minimum expanded container width token.
        private static final CssMetaData<M3NavigationRail, Number> EXPANDED_MINIMUM_CONTAINER_WIDTH =
                new CssMetaData<>(
                        "-m3-expanded-minimum-container-width",
                        SizeConverter.getInstance(),
                        DEFAULT_EXPANDED_MINIMUM_CONTAINER_WIDTH
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationRail control) {
                        return M3Css.isSettable(control.expandedMinimumContainerWidthProperty());
                    }

                    /// Returns the corresponding styleable property.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationRail control) {
                        return control.expandedMinimumContainerWidthProperty();
                    }
                };

        /// CSS metadata for the expanded container width token.
        private static final CssMetaData<M3NavigationRail, Number> EXPANDED_CONTAINER_WIDTH =
                new CssMetaData<>(
                        "-m3-expanded-container-width",
                        SizeConverter.getInstance(),
                        DEFAULT_EXPANDED_CONTAINER_WIDTH
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationRail control) {
                        return M3Css.isSettable(control.expandedContainerWidthProperty());
                    }

                    /// Returns the corresponding styleable property.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationRail control) {
                        return control.expandedContainerWidthProperty();
                    }
                };

        /// CSS metadata for the maximum expanded container width token.
        private static final CssMetaData<M3NavigationRail, Number> EXPANDED_MAXIMUM_CONTAINER_WIDTH =
                new CssMetaData<>(
                        "-m3-expanded-maximum-container-width",
                        SizeConverter.getInstance(),
                        DEFAULT_EXPANDED_MAXIMUM_CONTAINER_WIDTH
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationRail control) {
                        return M3Css.isSettable(control.expandedMaximumContainerWidthProperty());
                    }

                    /// Returns the corresponding styleable property.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationRail control) {
                        return control.expandedMaximumContainerWidthProperty();
                    }
                };

        /// CSS metadata for the spacing between the optional header and destination items.
        private static final CssMetaData<M3NavigationRail, Number> HEADER_SPACING =
                new CssMetaData<>("-m3-header-spacing", SizeConverter.getInstance(), DEFAULT_HEADER_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationRail control) {
                        return M3Css.isSettable(control.headerSpacingProperty());
                    }

                    /// Returns the corresponding styleable property.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationRail control) {
                        return control.headerSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ITEM_SPACING);
            styleables.add(COLLAPSED_CONTAINER_WIDTH);
            styleables.add(EXPANDED_MINIMUM_CONTAINER_WIDTH);
            styleables.add(EXPANDED_CONTAINER_WIDTH);
            styleables.add(EXPANDED_MAXIMUM_CONTAINER_WIDTH);
            styleables.add(HEADER_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents instantiation.
        private StyleableProperties() {
        }
    }

}
