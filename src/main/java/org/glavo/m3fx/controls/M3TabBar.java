// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// A Material Design 3 tab bar.
@NotNullByDefault
public class M3TabBar extends HBox {
    /// The base style class for M3FX tab bars.
    public static final String STYLE_CLASS = "m3-tab-bar";

    /// The currently selected tab.
    private final ReadOnlyObjectWrapper<@Nullable M3Tab> selectedTab =
            new ReadOnlyObjectWrapper<>(this, "selectedTab");

    /// The selected tabs in child order.
    private final ObservableList<M3Tab> selectedTabs = FXCollections.observableArrayList();

    /// The read-only selected tab view.
    private final @UnmodifiableView ObservableList<M3Tab> selectedTabsView =
            FXCollections.unmodifiableObservableList(selectedTabs);

    /// Whether the tab bar allows all tabs to be unselected.
    private final BooleanProperty allowEmptySelection = new SimpleBooleanProperty(this, "allowEmptySelection") {
        /// Restores a selected tab when empty selection is disabled.
        @Override
        protected void invalidated() {
            if (!get()) {
                selectFirstTabIfNeeded();
            }
        }
    };

    /// The selected-state listeners installed on tabs.
    private final Map<M3Tab, ChangeListener<Boolean>> selectedListeners = new HashMap<>();

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
    };

    /// Whether the tab bar is currently synchronizing selected states.
    private boolean updatingSelection;

    /// Creates an empty tab bar.
    public M3TabBar() {
        initialize();
    }

    /// Creates a tab bar containing the supplied tabs.
    public M3TabBar(M3Tab... tabs) {
        initialize();
        Objects.requireNonNull(tabs, "tabs");
        for (M3Tab tab : tabs) {
            Objects.requireNonNull(tab, "tab");
        }
        getTabs().addAll(tabs);
    }

    /// Returns the mutable child list used as tabs.
    public final ObservableList<Node> getTabs() {
        return getChildren();
    }

    /// Returns the selected tabs in child order.
    public final @UnmodifiableView ObservableList<M3Tab> getSelectedTabs() {
        return selectedTabsView;
    }

    /// Returns the selected tab.
    public final @Nullable M3Tab getSelectedTab() {
        return selectedTab.get();
    }

    /// Returns the selected tab property.
    public final ReadOnlyObjectProperty<@Nullable M3Tab> selectedTabProperty() {
        return selectedTab.getReadOnlyProperty();
    }

    /// Returns whether this tab bar allows all tabs to be unselected.
    public final boolean isAllowEmptySelection() {
        return allowEmptySelection.get();
    }

    /// Sets whether this tab bar allows all tabs to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection.set(allowEmptySelection);
    }

    /// Returns the empty-selection policy property.
    public final BooleanProperty allowEmptySelectionProperty() {
        return allowEmptySelection;
    }

    /// Selects a tab that belongs to this tab bar.
    public final void select(M3Tab tab) {
        Objects.requireNonNull(tab, "tab");
        if (!getChildren().contains(tab)) {
            throw new IllegalArgumentException("tab must belong to this tab bar");
        }
        selectTab(tab);
    }

    /// Selects the first tab when one exists.
    public final void selectFirst() {
        M3Tab firstTab = firstTab();
        if (firstTab != null) {
            selectTab(firstTab);
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

    /// Adds base style classes and installs selection listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addListener(childrenListener);
    }

    /// Installs a selected-state listener on a tab.
    private void installTab(M3Tab tab) {
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                handleTabSelectedChanged(tab, newValue);
        selectedListeners.put(tab, listener);
        tab.selectedProperty().addListener(listener);
    }

    /// Removes the selected-state listener from a tab.
    private void uninstallTab(M3Tab tab) {
        ChangeListener<Boolean> listener = selectedListeners.remove(tab);
        if (listener != null) {
            tab.selectedProperty().removeListener(listener);
        }
    }

    /// Keeps externally changed tab selected states mutually exclusive.
    private void handleTabSelectedChanged(M3Tab tab, boolean selected) {
        if (updatingSelection) {
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

    /// Selects a tab and clears selection from the remaining tabs.
    private void selectTab(@Nullable M3Tab tab) {
        updatingSelection = true;
        try {
            for (Node child : getChildren()) {
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
        selectedTabs.clear();
        for (Node child : getChildren()) {
            if (child instanceof M3Tab tab && tab.isSelected()) {
                selectedTabs.add(tab);
            }
        }
        selectedTab.set(selectedTabs.isEmpty() ? null : selectedTabs.get(0));
    }

    /// Returns the first tab child.
    private @Nullable M3Tab firstTab() {
        for (Node child : getChildren()) {
            if (child instanceof M3Tab tab) {
                return tab;
            }
        }
        return null;
    }
}
