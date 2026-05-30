// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TopAppBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 top app bar.
///
/// `M3TopAppBar` provides navigation, title, and trailing action slots for the top edge of an application view.
/// The variant property selects the small, medium, large, or centered layout metrics, while the action list
/// allows arbitrary JavaFX nodes such as [M3IconButton] instances.
///
/// See [Material Design top app bars](https://m3.material.io/components/top-app-bar/overview).
@NotNullByDefault
public class M3TopAppBar extends Control {
    /// The base style class for M3FX top app bars.
    public static final String STYLE_CLASS = "m3-top-app-bar";

    /// The navigation slot style class.
    public static final String NAVIGATION_STYLE_CLASS = "m3-top-app-bar-navigation";

    /// The title label style class.
    public static final String TITLE_STYLE_CLASS = "m3-top-app-bar-title";

    /// The actions container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-top-app-bar-actions";

    /// The app bar title text property.
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    /// The top app bar variant property.
    private final ObjectProperty<M3TopAppBarVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3TopAppBarVariant.SMALL) {
                /// Updates variant style classes and layout metrics when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TopAppBarVariant.SMALL);
                        return;
                    }
                    updateVariantStyle();
                    updateVariantMetrics();
                }
            };

    /// The optional leading navigation node property.
    private final ObjectProperty<@Nullable Node> navigation = new SimpleObjectProperty<>(this, "navigation");

    /// The mutable trailing action node list.
    private final ObservableList<Node> actions = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between navigation and action children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentFocusTarget(
                    this,
                    getNavigation(),
                    getActions()
            ));

    /// Creates an empty top app bar.
    public M3TopAppBar() {
        this("");
    }

    /// Creates a top app bar with title text.
    public M3TopAppBar(String title) {
        initialize();
        setTitle(title);
    }

    /// Creates a top app bar with title text, navigation content, and trailing actions.
    public M3TopAppBar(String title, @Nullable Node navigation, Node... actions) {
        this(title);
        setNavigation(navigation);
        addActions(actions);
    }

    /// Creates a top app bar with title text, variant, navigation content, and trailing actions.
    public M3TopAppBar(
            String title,
            M3TopAppBarVariant variant,
            @Nullable Node navigation,
            Node... actions
    ) {
        this(title, navigation, actions);
        setVariant(variant);
    }

    /// Returns the app bar title.
    public final String getTitle() {
        return title.get();
    }

    /// Sets the app bar title.
    public final void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    /// Returns the app bar title property.
    public final StringProperty titleProperty() {
        return title;
    }

    /// Returns the top app bar variant.
    public final M3TopAppBarVariant getVariant() {
        return variant.get();
    }

    /// Sets the top app bar variant.
    public final void setVariant(M3TopAppBarVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the top app bar variant property.
    public final ObjectProperty<M3TopAppBarVariant> variantProperty() {
        return variant;
    }

    /// Returns the optional leading navigation node.
    public final @Nullable Node getNavigation() {
        return navigation.get();
    }

    /// Sets the optional leading navigation node.
    public final void setNavigation(@Nullable Node navigation) {
        this.navigation.set(navigation);
    }

    /// Returns the optional leading navigation node property.
    public final ObjectProperty<@Nullable Node> navigationProperty() {
        return navigation;
    }

    /// Returns the mutable trailing action node list.
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Adds one trailing action node.
    public final void addAction(Node action) {
        getActions().add(Objects.requireNonNull(action, "action"));
    }

    /// Adds trailing actions after validating the action array.
    public final void addActions(Node... actions) {
        validateActions(actions);
        getActions().addAll(actions);
    }

    /// Replaces all trailing actions.
    public final void setActions(Node... actions) {
        validateActions(actions);
        getActions().setAll(actions);
    }

    /// Removes all trailing actions.
    public final void clearActions() {
        getActions().clear();
    }

    /// Returns the user-agent stylesheet for M3FX top app bars.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("top-app-bar.css");
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        title.addListener(observable -> updateAccessibleText());
        navigation.addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        focusNotifier.start();
        updateAccessibleText();
        updateVariantStyle();
        updateVariantMetrics();
    }

    /// Returns accessibility attributes for the title and action collection.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> getTitle();
            case ITEM_COUNT -> M3Accessible.itemCount(getNavigation(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getNavigation(), getActions(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getNavigation(), getActions());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed navigation and action children.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3Accessible.firstFocusTarget(getNavigation(), getActions()));
            case SHOW_ITEM -> M3Accessible.showItem(getNavigation(), getActions(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 top app bar skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TopAppBarSkin(this);
    }

    /// Validates a trailing action array.
    private static void validateActions(Node... actions) {
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
    }

    /// Updates the accessible text exposed by the app bar.
    private void updateAccessibleText() {
        setAccessibleText(getTitle());
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Notifies accessibility clients that the indexed app bar item collection changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        focusNotifier.refresh();
    }

    /// Updates the active variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3TopAppBarVariant.SMALL.getStyleClass(),
                M3TopAppBarVariant.CENTER_ALIGNED.getStyleClass(),
                M3TopAppBarVariant.MEDIUM.getStyleClass(),
                M3TopAppBarVariant.LARGE.getStyleClass()
        );
    }

    /// Updates variant-dependent control sizing.
    private void updateVariantMetrics() {
        setMinHeight(Region.USE_COMPUTED_SIZE);
        setPrefHeight(Region.USE_COMPUTED_SIZE);
    }
}
