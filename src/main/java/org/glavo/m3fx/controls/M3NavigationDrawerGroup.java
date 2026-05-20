// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3NavigationDrawerGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// A collapsible Material Design 3 navigation drawer destination group.
@NotNullByDefault
public final class M3NavigationDrawerGroup extends Control {
    /// The base style class for M3FX navigation drawer groups.
    public static final String STYLE_CLASS = "m3-navigation-drawer-group";

    /// The style class applied to the group header list item.
    public static final String HEADER_STYLE_CLASS = "m3-navigation-drawer-group-header";

    /// The style class applied to child destination list items.
    public static final String CHILD_STYLE_CLASS = "m3-navigation-drawer-group-child";

    /// The expanded pseudo-class used by navigation drawer groups.
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    /// The group title displayed by the header list item.
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    /// Whether child destination items are visible.
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded") {
        /// Updates expanded pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, get());
            requestLayout();
        }
    };

    /// The child destination items shown when the group is expanded.
    private final ObservableList<M3ListItem> items = FXCollections.observableArrayList();

    /// The header list item that toggles the group.
    private final M3ListItem headerItem = new M3ListItem();

    /// The disclosure icon attached to the header item.
    private final M3DisclosureIcon disclosureIcon = new M3DisclosureIcon();

    /// Applies child-item style classes as items are added and removed.
    private final ListChangeListener<M3ListItem> itemsListener = change -> {
        while (change.next()) {
            for (M3ListItem item : change.getAddedSubList()) {
                M3ControlStyles.add(item, CHILD_STYLE_CLASS);
            }
            for (M3ListItem item : change.getRemoved()) {
                item.getStyleClass().remove(CHILD_STYLE_CLASS);
            }
        }
        requestLayout();
    };

    /// Creates an empty navigation drawer group.
    public M3NavigationDrawerGroup() {
        this("");
    }

    /// Creates a navigation drawer group with the supplied title.
    public M3NavigationDrawerGroup(String title) {
        initialize();
        setTitle(title);
    }

    /// Returns the group title displayed by the header list item.
    public String getTitle() {
        return title.get();
    }

    /// Sets the group title displayed by the header list item.
    public void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    /// Returns the group title property.
    public StringProperty titleProperty() {
        return title;
    }

    /// Returns whether child destination items are visible.
    public boolean isExpanded() {
        return expanded.get();
    }

    /// Sets whether child destination items are visible.
    public void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    /// Returns the expanded-state property.
    public BooleanProperty expandedProperty() {
        return expanded;
    }

    /// Returns the mutable child destination list.
    public ObservableList<M3ListItem> getItems() {
        return items;
    }

    /// Adds one child destination item.
    public void addItem(M3ListItem item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds child destination items.
    public void addItems(M3ListItem... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all child destination items.
    public void setItems(M3ListItem... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all child destination items.
    public void clearItems() {
        getItems().clear();
    }

    /// Returns the header list item owned by this group.
    public M3ListItem getHeaderItem() {
        return headerItem;
    }

    /// Returns the user-agent stylesheet for M3FX navigation drawer groups.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-drawer-group.css");
    }

    /// Creates the default Material Design 3 navigation drawer group skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3NavigationDrawerGroupSkin(this);
    }

    /// Adds base style classes and connects the header row with the group state.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        M3ControlStyles.add(headerItem, HEADER_STYLE_CLASS);
        headerItem.headlineTextProperty().bind(title);
        headerItem.setTrailingMedia(disclosureIcon, M3ListItemSlotSize.ICON);
        headerItem.setOnAction(event -> setExpanded(!isExpanded()));
        disclosureIcon.expandedProperty().bind(expanded);
        items.addListener(itemsListener);
        setAccessibleRole(AccessibleRole.NODE);
        setFocusTraversable(false);
    }

    /// Validates a child destination item array.
    private static void validateItems(M3ListItem... items) {
        Objects.requireNonNull(items, "items");
        for (M3ListItem item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
