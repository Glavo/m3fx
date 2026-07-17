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
import javafx.css.PseudoClass;
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
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3TabBarSkin;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 tab bar.
///
/// `M3TabBar` manages a list of [M3Tab] nodes, keeps selected tabs synchronized, exposes read-only selected-tab
/// views, and supports keyboard traversal. It is intended for navigation between peer pages or views at the same
/// hierarchy level. Use [M3TabBarVariant#PRIMARY] for the principal peer views in a content region and
/// [M3TabBarVariant#SECONDARY] for a subordinate level placed below primary tabs. [M3TabBarLayout#FIXED] keeps a
/// small tab set visible in equal-width cells, while [M3TabBarLayout#SCROLLABLE] preserves content-derived widths
/// for longer labels and larger sets.
///
/// See [Material Design tabs](https://m3.material.io/components/tabs/overview).
@NotNullByDefault
public final class M3TabBar extends Control {
    /// The base style class for M3FX tab bars.
    public static final String STYLE_CLASS = "m3-tab-bar";

    /// The style class applied to the internal tab row container.
    public static final String CONTAINER_STYLE_CLASS = "m3-tab-bar-container";

    /// The style class applied to the bottom divider rendered behind tab indicators.
    public static final String DIVIDER_STYLE_CLASS = "m3-tab-bar-divider";

    /// The pseudo-class applied to secondary tab bars and their tabs.
    private static final PseudoClass SECONDARY_PSEUDO_CLASS = PseudoClass.getPseudoClass("secondary");

    /// The pseudo-class applied while the tab row uses scrollable layout.
    private static final PseudoClass SCROLLABLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("scrollable");

    /// The tab bar variant property.
    private final ObjectProperty<M3TabBarVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3TabBarVariant.PRIMARY) {
                /// Updates the visual role of this bar and its installed tabs.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TabBarVariant.PRIMARY);
                        return;
                    }
                    updateVariantState();
                }
            };

    /// The tab layout property.
    private final ObjectProperty<M3TabBarLayout> tabLayout =
            new SimpleObjectProperty<>(this, "tabLayout", M3TabBarLayout.FIXED) {
                /// Normalizes null assignments and refreshes layout state.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TabBarLayout.FIXED);
                        return;
                    }
                    pseudoClassStateChanged(SCROLLABLE_PSEUDO_CLASS, get() == M3TabBarLayout.SCROLLABLE);
                    requestLayout();
                }
            };

    /// The mutable tab content.
    private final ObservableList<M3Tab> tabs = M3ObservableLists.nonNullElementList("tab");

    /// Notifies accessibility clients when focus moves between tabs.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () ->
                    M3Accessible.currentOrSelectionFocusTarget(this, getTabs(), getSelectedTab(), M3Tab.class));

    /// The selected tab property.
    private final ReadOnlyObjectWrapper<@Nullable M3Tab> selectedTab =
            new ReadOnlyObjectWrapper<>(this, "selectedTab");

    /// The selected tabs in child order.
    private final ObservableList<M3Tab> selectedTabs = M3ObservableLists.nonNullElementList("selectedTab");

    /// The read-only selected tab view.
    private final @UnmodifiableView ObservableList<M3Tab> selectedTabsView =
            FXCollections.unmodifiableObservableList(selectedTabs);

    /// The empty-selection policy property.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection") {
        /// Restores a selected tab when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstTabIfNeeded();
            }
        }
    };

    /// Reusable storage for computing selected tabs without allocating on every refresh.
    private final List<M3Tab> selectedTabsScratch = new ArrayList<>();

    /// Handles selected-state invalidation for every installed tab.
    private final InvalidationListener selectedInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property && property.getBean() instanceof M3Tab tab) {
            handleTabSelectedChanged(tab, tab.isSelected());
        }
    };

    /// Handles reachability invalidation for every installed tab.
    private final InvalidationListener reachabilityInvalidation = observable -> {
        if (observable instanceof ReadOnlyProperty<?> property && property.getBean() instanceof M3Tab tab) {
            handleTabReachabilityChanged(tab);
        }
    };

    /// Updates tab selection listeners when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3Tab tab) {
                    uninstallTab(tab);
                    tab.setSelected(false);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3Tab tab) {
                    installTab(tab);
                }
            }
        }
        enforceSelectionPolicy();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    };

    /// Whether the tab bar is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty tab bar.
    public M3TabBar() {
        initialize();
    }

    /// Returns the mutable child list used as tabs.
    ///
    /// The list rejects `null` elements. Adding or removing tabs updates selection listeners immediately. A tab
    /// can belong to only one scene-graph parent at a time.
    ///
    /// @return the live mutable tab list
    public final ObservableList<M3Tab> getTabs() {
        return tabs;
    }

    /// Returns the visual and hierarchical role of this tab bar.
    ///
    /// @return the tab bar variant
    public final M3TabBarVariant getVariant() {
        return variant.get();
    }

    /// Sets the visual and hierarchical role of this tab bar.
    ///
    /// Changing the variant preserves the selected tab and all keyboard and accessibility state.
    ///
    /// @param variant the tab bar variant
    /// @throws NullPointerException if any required argument is `null`
    public final void setVariant(M3TabBarVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    public final ObjectProperty<M3TabBarVariant> variantProperty() {
        return variant;
    }

    /// Returns the strategy used to distribute tabs in this bar.
    ///
    /// @return the current tab layout
    public final M3TabBarLayout getTabLayout() {
        return tabLayout.get();
    }

    /// Sets the strategy used to distribute tabs in this bar.
    ///
    /// Changing the layout preserves selection. A scrollable layout automatically reveals the selected or focused
    /// tab when keyboard, pointer, or accessibility interaction moves to an item outside the viewport.
    ///
    /// @param tabLayout the tab layout
    /// @throws NullPointerException if any required argument is `null`
    public final void setTabLayout(M3TabBarLayout tabLayout) {
        this.tabLayout.set(Objects.requireNonNull(tabLayout, "tabLayout"));
    }

    public final ObjectProperty<M3TabBarLayout> tabLayoutProperty() {
        return tabLayout;
    }

    /// Returns the selected tabs in child order.
    ///
    /// @return an unmodifiable live view of selected, reachable tabs
    public final @UnmodifiableView ObservableList<M3Tab> getSelectedTabs() {
        return selectedTabsView;
    }

    /// Returns the selected tab.
    ///
    /// @return the selected tab, or `null` when empty selection is allowed and no tab is selected
    public final @Nullable M3Tab getSelectedTab() {
        return selectedTab.get();
    }

    public final ReadOnlyObjectProperty<@Nullable M3Tab> selectedTabProperty() {
        return selectedTab.getReadOnlyProperty();
    }

    /// Returns the child index of the selected tab, or `-1` when no tab is selected.
    ///
    /// @return the selected tab index, or `-1`
    public final int getSelectedIndex() {
        @Nullable M3Tab tab = getSelectedTab();
        return tab == null ? -1 : getTabs().indexOf(tab);
    }

    /// Returns whether this tab bar allows all tabs to be unselected.
    ///
    /// @return whether empty selection is permitted; the default is `false`
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this tab bar allows all tabs to be unselected.
    ///
    /// Changing this value to `false` immediately selects the first selectable tab when the current selection is
    /// empty.
    ///
    /// @param allowEmptySelection whether empty selection is permitted
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a tab that belongs to this tab bar.
    ///
    /// @param tab the reachable, enabled, visible tab to select
    /// @throws NullPointerException if `tab` is `null`
    /// @throws IllegalArgumentException if `tab` does not belong to this bar or is not selectable
    public final void select(M3Tab tab) {
        Objects.requireNonNull(tab, "tab");
        if (!getTabs().contains(tab)) {
            throw new IllegalArgumentException("tab must belong to this tab bar");
        }
        if (!isSelectableTab(tab)) {
            throw new IllegalArgumentException("tab must be selectable");
        }
        selectTab(tab);
    }

    /// Selects the tab at the given child index.
    ///
    /// @param index the zero-based tab index
    /// @throws IndexOutOfBoundsException if `index` is outside the tab list
    /// @throws IllegalArgumentException if the indexed tab is not selectable
    public final void selectIndex(int index) {
        Node child = getTabs().get(index);
        if (child instanceof M3Tab tab) {
            select(tab);
            return;
        }
        throw new IllegalArgumentException("child at index is not an M3Tab");
    }

    /// Selects the first tab when one exists.
    public final void selectFirst() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        M3Tab firstTab = firstTab();
        if (firstTab != null) {
            selectTab(firstTab);
        }
    }

    /// Selects the last tab when one exists.
    public final void selectLast() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3Tab lastTab = M3SelectionNavigation.last(getTabs(), M3Tab.class);
        if (lastTab != null) {
            selectTab(lastTab);
        }
    }

    /// Selects the next tab after the current selected tab, wrapping at the end.
    public final void selectNext() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3Tab nextTab = M3SelectionNavigation.next(getTabs(), getSelectedTab(), M3Tab.class);
        if (nextTab != null) {
            selectTab(nextTab);
        }
    }

    /// Selects the previous tab before the current selected tab, wrapping at the start.
    public final void selectPrevious() {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return;
        }
        @Nullable M3Tab previousTab =
                M3SelectionNavigation.previous(getTabs(), getSelectedTab(), M3Tab.class);
        if (previousTab != null) {
            selectTab(previousTab);
        }
    }

    /// Clears the current selection when empty selection is allowed.
    public final void clearSelection() {
        if (!isAllowEmptySelection()) {
            selectFirstTabIfNeeded();
            return;
        }
        selectTab(null);
    }

    /// Returns the user-agent stylesheet for M3FX tabs.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("tab.css");
    }

    /// Returns accessibility attributes for tab bar content and selection state.
    ///
    /// @throws NullPointerException if any required argument is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getTabs().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getTabs(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrSelectionFocusTarget(
                    this,
                    getTabs(),
                    getSelectedTab(),
                    M3Tab.class
            );
            case MULTIPLE_SELECTION -> false;
            case SELECTED_ITEMS -> selectedTabsView;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility selection actions for tabs.
    ///
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
                getTabs(),
                getSelectedTab(),
                M3Tab.class
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
                getTabs(),
                getSelectedTab(),
                M3Tab.class
        ), getTabs(), parameters)) {
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
        setAccessibleRole(AccessibleRole.TAB_PANE);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleSelectionTarget, this::showAccessibleItem);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getTabs().addListener(childrenListener);
        focusNotifier.start();
        updateVariantState();
        pseudoClassStateChanged(SCROLLABLE_PSEUDO_CLASS, getTabLayout() == M3TabBarLayout.SCROLLABLE);
    }

    /// Applies keyboard navigation across enabled tabs.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeySelection(
                event,
                this,
                getTabs(),
                M3SelectionNavigation.focusAnchor(getTabs(), getSelectedTab(), M3Tab.class),
                M3Tab.class,
                true,
                false,
                M3NodeLayout.isRightToLeft(this),
                this::select
        );
    }

    /// Applies the selected tab supplied by an accessibility client.
    private void setAccessibleSelectedItems(Object... parameters) {
        @Nullable M3Tab tab = firstAccessibleSelectableTab(parameters);
        if (tab == null) {
            clearSelection();
        } else {
            select(tab);
        }
    }

    /// Installs a selected-state listener on a tab.
    private void installTab(M3Tab tab) {
        tab.updateNavigationVariant(getVariant());
        tab.selectedProperty().addListener(selectedInvalidation);
        tab.disabledProperty().addListener(reachabilityInvalidation);
        tab.visibleProperty().addListener(reachabilityInvalidation);
    }

    /// Removes the selected-state listener from a tab.
    private void uninstallTab(M3Tab tab) {
        tab.selectedProperty().removeListener(selectedInvalidation);
        tab.disabledProperty().removeListener(reachabilityInvalidation);
        tab.visibleProperty().removeListener(reachabilityInvalidation);
        tab.updateNavigationVariant(M3TabBarVariant.PRIMARY);
    }

    /// Applies the current variant pseudo-class to this bar and its installed tabs.
    private void updateVariantState() {
        M3TabBarVariant currentVariant = getVariant();
        boolean secondary = currentVariant == M3TabBarVariant.SECONDARY;
        pseudoClassStateChanged(SECONDARY_PSEUDO_CLASS, secondary);
        for (Node child : getTabs()) {
            if (child instanceof M3Tab tab) {
                tab.updateNavigationVariant(currentVariant);
            }
        }
    }

    /// Keeps externally changed tab selected states mutually exclusive.
    private void handleTabSelectedChanged(M3Tab tab, boolean selected) {
        if (updatingSelection) {
            return;
        }

        if (!isSelectableTab(tab)) {
            if (selected) {
                clearTabSelection(tab);
                if (!isAllowEmptySelection()) {
                    selectFirstTabIfNeeded();
                }
            }
            return;
        }

        if (selected) {
            selectTab(tab);
        } else if (selectedTab.get() == tab) {
            refreshSelectedTabs();
            if (!isAllowEmptySelection()) {
                selectFirstTabIfNeeded();
            }
        }
    }

    /// Keeps selection and accessibility state consistent when a tab becomes unreachable.
    private void handleTabReachabilityChanged(M3Tab tab) {
        if (tab.isSelected() && !isSelectableTab(tab)) {
            clearTabSelection(tab);
        }
        enforceSelectionPolicy();
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Enforces single-selection and non-empty selection invariants.
    private void enforceSelectionPolicy() {
        refreshSelectedTabs();
        if (selectedTabs.size() > 1) {
            selectTab(selectedTabs.get(0));
            return;
        }
        if (!isAllowEmptySelection()) {
            selectFirstTabIfNeeded();
        }
    }

    /// Selects the first tab when selection is empty.
    private void selectFirstTabIfNeeded() {
        M3Tab firstTab = firstTab();
        if (!selectedTabs.isEmpty() || firstTab == null) {
            return;
        }

        selectTab(firstTab);
    }

    /// Clears one tab's selected state and refreshes selected tab state.
    private void clearTabSelection(M3Tab tab) {
        clearTabSelectionWithoutRefresh(tab);
        refreshSelectedTabs();
    }

    /// Clears one tab's selected state without refreshing the aggregate selected tab list.
    private void clearTabSelectionWithoutRefresh(M3Tab tab) {
        updatingSelection = true;
        try {
            tab.setSelected(false);
        } finally {
            updatingSelection = false;
        }
    }

    /// Selects a tab and clears selection from the remaining tabs.
    private void selectTab(@Nullable M3Tab tab) {
        updatingSelection = true;
        try {
            for (Node child : getTabs()) {
                if (child instanceof M3Tab item) {
                    item.setSelected(item == tab);
                }
            }
        } finally {
            updatingSelection = false;
        }
        refreshSelectedTabs();
    }

    /// Refreshes selected tab state from current child states.
    private void refreshSelectedTabs() {
        selectedTabsScratch.clear();
        for (Node child : getTabs()) {
            if (child instanceof M3Tab tab && tab.isSelected()) {
                if (isSelectableTab(tab)) {
                    selectedTabsScratch.add(tab);
                } else {
                    clearTabSelectionWithoutRefresh(tab);
                }
            }
        }
        boolean selectionChanged = !selectedTabs.equals(selectedTabsScratch);
        if (selectionChanged) {
            selectedTabs.setAll(selectedTabsScratch);
        }
        selectedTabsScratch.clear();

        selectedTab.set(selectedTabs.isEmpty() ? null : selectedTabs.get(0));
        if (selectionChanged) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
            M3Accessible.notifyFocusNodeChanged(this);
            focusNotifier.refresh();
        }
    }

    /// Returns the first tab child.
    private @Nullable M3Tab firstTab() {
        return M3SelectionNavigation.first(getTabs(), M3Tab.class);
    }

    /// Returns the first selectable tab referenced by accessibility parameters.
    private @Nullable M3Tab firstAccessibleSelectableTab(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node child : getTabs()) {
            if (child instanceof M3Tab tab
                    && isSelectableTab(tab)
                    && M3Accessible.containsSelectionTarget(tab, parameters)) {
                return tab;
            }
        }
        return null;
    }

    /// Returns whether a tab can currently participate in selection.
    private boolean isSelectableTab(M3Tab tab) {
        return M3Accessible.isEffectivelyReachable(this) && M3Accessible.isEffectivelyReachable(tab);
    }

    /// Creates the default Material Design 3 tab bar skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TabBarSkin(this);
    }

}
