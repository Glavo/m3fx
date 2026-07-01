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
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3NavigationDrawerGroupSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A collapsible Material Design 3 navigation drawer destination group.
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

    // The group title displayed by the header list item.
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    // Whether child destination items are visible.
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
    public void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    /// Returns the group title property.
    ///
    /// @return the writable group title property
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

    /// Returns the expanded-state property.
    ///
    /// @return the writable expanded-state property
    public BooleanProperty expandedProperty() {
        return expanded;
    }

    /// Returns the mutable child destination list.
    ///
    /// @return the mutable child destination list
    public ObservableList<M3ListItem> getItems() {
        return items;
    }

    /// Adds one child destination item.
    ///
    /// @param item the non-null child destination item to append
    public void addItem(M3ListItem item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds child destination items.
    ///
    /// @param items the non-null child destination items to append
    public void addItems(M3ListItem... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all child destination items.
    ///
    /// @param items the non-null child destination items that replace the current content
    public void setItems(M3ListItem... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all child destination items.
    public void clearItems() {
        getItems().clear();
    }

    /// Returns the header list item owned by this group.
    ///
    /// @return the header list item owned by this group
    public M3ListItem getHeaderItem() {
        return headerItem;
    }

    /// Returns accessibility attributes for the disclosure state and visible child rows.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
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
    /// @param action the accessibility action to execute
    /// @param parameters optional action-specific parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> setExpanded(!isExpanded());
            case EXPAND -> setExpanded(true);
            case COLLAPSE -> setExpanded(false);
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
        M3ControlStyles.add(this, STYLE_CLASS);
        M3ControlStyles.add(headerItem, HEADER_STYLE_CLASS);
        headerItem.headlineTextProperty().bind(title);
        headerItem.setTrailingMedia(disclosureIcon, M3ListItemSlotSize.ICON);
        headerItem.setOnAction(event -> setExpanded(!isExpanded()));
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
        ObservableList<Node> content = FXCollections.observableArrayList();
        content.add(headerItem);
        if (isExpanded()) {
            content.addAll(items);
        }
        return content;
    }

    /// Expands the group when needed and focuses the requested accessible row.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the requested or current row
    final boolean showAccessibleItem(Object... parameters) {
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
    final boolean focusAccessibleNode() {
        if (M3Accessible.showCurrentOrItem(this, accessibleContent())) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Expands this group without leaving an in-flight reveal animation before an accessibility focus request.
    void expandForAccessibleReveal() {
        @Nullable Boolean previousAnimationsEnabled = M3MotionSettings.getAnimationsEnabled(this);
        M3MotionSettings.setAnimationsEnabled(this, false);
        try {
            setExpanded(true);
            applyCssIfAttached();
            layout();
        } finally {
            M3MotionSettings.setAnimationsEnabled(this, previousAnimationsEnabled);
        }
    }

    /// Applies CSS after an accessibility-driven expansion so newly mounted child slots can receive focus.
    private void applyCssIfAttached() {
        if (getScene() != null) {
            applyCss();
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
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyFocusNodeChanged();
        requestLayout();
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Validates a child destination item array.
    private static void validateItems(M3ListItem... items) {
        Objects.requireNonNull(items, "items");
        for (M3ListItem item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
