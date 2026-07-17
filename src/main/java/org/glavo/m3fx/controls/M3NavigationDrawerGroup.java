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
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3NavigationDrawerGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A collapsible Material Design 3 navigation drawer destination group.
///
/// The group owns one header row and exposes a live list of child [M3ListItem] destinations. Activating the header
/// toggles [expandedProperty]; collapsed children are removed from layout, focus traversal, and accessibility
/// indexing. The header is managed by the group and is available through [getHeaderItem] for presentation
/// customization, but it must not be reparented.
///
/// See [Material Design navigation drawer](https://m3.material.io/components/navigation-drawer/overview).
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
            boolean expanded = get();
            boolean restoreHeaderFocus = !expanded && isFocusInsideChildItems();
            pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, expanded);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyAccessibleContentChanged();
            if (restoreHeaderFocus) {
                focusAccessibleNode();
            }
        }
    };

    /// The child destination items shown when the group is expanded.
    private final ObservableList<M3ListItem> items = M3ObservableLists.nonNullElementList("item");

    /// The header list item that toggles the group.
    private final M3ListItem headerItem = new M3ListItem();

    /// The disclosure icon attached to the header item.
    private final M3DisclosureIcon disclosureIcon = new M3DisclosureIcon();

    /// Cached header and currently visible child rows used by accessibility queries.
    private final ObservableList<Node> accessibleItems = FXCollections.observableArrayList(headerItem);

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
        notifyAccessibleContentChanged();
    };

    /// Notifies accessibility clients when focus moves between visible group rows.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(
                    this,
                    accessibleContent()
            ));

    /// Creates an empty navigation drawer group.
    public M3NavigationDrawerGroup() {
        this("");
    }

    /// Creates a navigation drawer group with the supplied title.
    ///
    /// @param title the group title displayed by the header row
    /// @throws NullPointerException if `title` is `null`
    public M3NavigationDrawerGroup(String title) {
        initialize();
        setTitle(title);
    }

    /// Returns the group title displayed by the header list item.
    ///
    /// @return the group title displayed by the header row
    public String getTitle() {
        return title.get();
    }

    /// Sets the group title displayed by the header list item.
    ///
    /// @param title the group title displayed by the header row
    /// @throws NullPointerException if `title` is `null`
    public void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    public StringProperty titleProperty() {
        return title;
    }

    /// Returns whether child destination items are visible.
    ///
    /// @return `true` when child destination items are visible
    public boolean isExpanded() {
        return expanded.get();
    }

    /// Sets whether child destination items are visible.
    ///
    /// @param expanded whether child destination items should be visible
    public void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    /// Returns the mutable child destination list.
    ///
    /// Changes to the returned list are observed immediately. The list rejects `null` elements. Items must not
    /// simultaneously belong to another parent.
    ///
    /// @return the live, mutable child destination list
    public ObservableList<M3ListItem> getItems() {
        return items;
    }


    /// Returns the header list item owned by this group.
    ///
    /// The returned item remains owned by this group. Applications may customize its supported presentation
    /// properties but must not add it to another parent or replace its disclosure behavior.
    ///
    /// @return the header list item owned by this group
    public M3ListItem getHeaderItem() {
        return headerItem;
    }

    /// Returns accessibility attributes for the disclosure state and visible child rows.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        ObservableList<Node> content = accessibleContent();
        return switch (attribute) {
            case EXPANDED -> isExpanded();
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, content);
            case ITEM_COUNT -> content.size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(content, parameters);
            case TEXT -> getTitle();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for toggling and focusing the disclosure group.
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
            case FIRE -> {
                if (M3Accessible.isEffectivelyReachable(this)) {
                    setExpanded(!isExpanded());
                }
            }
            case EXPAND -> {
                if (M3Accessible.isEffectivelyReachable(this)) {
                    setExpanded(true);
                }
            }
            case COLLAPSE -> {
                if (M3Accessible.isEffectivelyReachable(this)) {
                    setExpanded(false);
                }
            }
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the user-agent stylesheet for M3FX navigation drawer groups.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-drawer-group.css");
    }

    /// Creates the default Material Design 3 navigation drawer group skin.
    ///
    /// @return the default Material Design 3 navigation drawer group skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3NavigationDrawerGroupSkin(this);
    }

    /// Adds base style classes and connects the header row with the group state.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        M3ControlStyles.add(headerItem, HEADER_STYLE_CLASS);
        headerItem.headlineTextProperty().bind(title);
        disclosureIcon.setMouseTransparent(true);
        headerItem.setTrailingMedia(disclosureIcon, M3ListItemSlotSize.ICON);
        headerItem.setOnAction(event -> {
            if (M3Accessible.isEffectivelyReachable(this)) {
                setExpanded(!isExpanded());
            }
        });
        disclosureIcon.expandedProperty().bind(expanded);
        items.addListener(itemsListener);
        title.addListener((observable, oldValue, newValue) -> {
            setAccessibleText(newValue);
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        setAccessibleRole(AccessibleRole.NODE);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        setAccessibleText(getTitle());
        setFocusTraversable(false);
        focusNotifier.start();
    }

    /// Returns the header row and currently visible child rows for accessibility indexing.
    private ObservableList<Node> accessibleContent() {
        return accessibleItems;
    }

    /// Expands the group when needed and focuses the requested accessible row.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the requested or current row
    boolean showAccessibleItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            expandForAccessibleReveal();
            if (M3Accessible.showCurrentOrItem(this, accessibleContent())) {
                notifyFocusNodeChanged();
                return true;
            }
            return false;
        }

        if (!M3Accessible.canShowItem(headerItem, items, parameters)) {
            return false;
        }

        expandForAccessibleReveal();
        if (M3Accessible.showCurrentOrItem(this, accessibleContent(), parameters)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Requests focus for the current accessible row, or the header row when no child owns focus.
    ///
    /// @return `true` when the current row accepted focus
    boolean focusAccessibleNode() {
        if (M3Accessible.showCurrentOrItem(this, accessibleContent())) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Expands this group without leaving an in-flight reveal animation before an accessibility focus request.
    void expandForAccessibleReveal() {
        boolean previousReducedMotionRequested = M3MotionSettings.isReducedMotionRequested(this);
        M3MotionSettings.setReducedMotionRequested(this, true);
        try {
            setExpanded(true);
            if (getScene() != null) {
                applyCss();
            }
            layout();
        } finally {
            M3MotionSettings.setReducedMotionRequested(this, previousReducedMotionRequested);
        }
    }


    /// Returns whether keyboard focus is currently inside a child destination row.
    private boolean isFocusInsideChildItems() {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner == null) {
            return false;
        }
        for (M3ListItem item : getItems()) {
            if (M3Accessible.containsNode(item, focusOwner)) {
                return true;
            }
        }
        return false;
    }

    /// Notifies accessibility clients that visible group rows changed.
    private void notifyAccessibleContentChanged() {
        refreshAccessibleContent();
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyFocusNodeChanged();
        requestLayout();
    }

    /// Rebuilds the cached accessibility row order after disclosure or child-list changes.
    private void refreshAccessibleContent() {
        accessibleItems.clear();
        accessibleItems.add(headerItem);
        if (isExpanded()) {
            accessibleItems.addAll(items);
        }
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

}
