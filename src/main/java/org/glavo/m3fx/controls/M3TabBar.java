// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 tab bar.
@NotNullByDefault
public class M3TabBar extends HBox {
    /// The base style class for M3FX tab bars.
    public static final String STYLE_CLASS = "m3-tab-bar";

    /// The toggle group that owns tab selection.
    private final ToggleGroup toggleGroup = new ToggleGroup();

    /// The currently selected tab.
    private final ReadOnlyObjectWrapper<@Nullable M3Tab> selectedTab =
            new ReadOnlyObjectWrapper<>(this, "selectedTab");

    /// Updates tab toggle groups when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3Tab tab && tab.getToggleGroup() == toggleGroup) {
                    tab.setToggleGroup(null);
                }
            }
            for (Node child : change.getAddedSubList()) {
                if (child instanceof M3Tab tab) {
                    tab.setToggleGroup(toggleGroup);
                }
            }
        }
        selectFirstTabIfNeeded();
    };

    /// Tracks the selected tab from the internal toggle group.
    private final ChangeListener<@Nullable Toggle> selectedToggleListener = (observable, oldValue, newValue) -> {
        selectedTab.set(newValue instanceof M3Tab tab ? tab : null);
        selectFirstTabIfNeeded();
    };

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

    /// Returns the selected tab.
    public final @Nullable M3Tab getSelectedTab() {
        return selectedTab.get();
    }

    /// Returns the selected tab property.
    public final ReadOnlyObjectProperty<@Nullable M3Tab> selectedTabProperty() {
        return selectedTab.getReadOnlyProperty();
    }

    /// Selects a tab that belongs to this tab bar.
    public final void select(M3Tab tab) {
        Objects.requireNonNull(tab, "tab");
        if (!getChildren().contains(tab)) {
            throw new IllegalArgumentException("tab must belong to this tab bar");
        }
        tab.setSelected(true);
    }

    /// Selects the first tab when one exists.
    public final void selectFirst() {
        M3Tab firstTab = firstTab();
        if (firstTab != null) {
            firstTab.setSelected(true);
        }
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
        toggleGroup.selectedToggleProperty().addListener(selectedToggleListener);
    }

    /// Selects the first tab when selection is empty.
    private void selectFirstTabIfNeeded() {
        M3Tab firstTab = firstTab();
        if (toggleGroup.getSelectedToggle() != null || firstTab == null) {
            return;
        }

        firstTab.setSelected(true);
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
