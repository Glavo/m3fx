// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 top app bar.
@NotNullByDefault
public class M3TopAppBar extends HBox {
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
                /// Updates variant style classes and layout state when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TopAppBarVariant.SMALL);
                        return;
                    }
                    updateVariantStyle();
                    updateVariantLayout();
                }
            };

    /// The optional leading navigation node property.
    private final ObjectProperty<@Nullable Node> navigation = new SimpleObjectProperty<>(this, "navigation");

    /// The slot that hosts the optional navigation node.
    private final StackPane navigationSlot = new StackPane();

    /// The label that renders the app bar title.
    private final Label titleLabel = new Label();

    /// The flexible spacer between the title and actions.
    private final Region spacer = new Region();

    /// The trailing action node container.
    private final HBox actions = new HBox();

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
        return actions.getChildren();
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

    /// Initializes child nodes, style classes, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        navigationSlot.getStyleClass().add(NAVIGATION_STYLE_CLASS);
        titleLabel.getStyleClass().add(TITLE_STYLE_CLASS);
        actions.getStyleClass().add(ACTIONS_STYLE_CLASS);

        setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleLabel.textProperty().bind(title);
        title.addListener(observable -> updateAccessibleText());
        navigation.addListener((observable, oldValue, newValue) -> updateNavigation(newValue));
        actions.getChildren().addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        updateNavigation(getNavigation());
        updateAccessibleText();
        updateVariantStyle();
        updateVariantLayout();

        getChildren().addAll(navigationSlot, titleLabel, spacer, actions);
    }

    /// Returns accessibility attributes for the title and action collection.
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> getTitle();
            case ITEM_COUNT -> M3Accessible.itemCount(getNavigation(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getNavigation(), getActions(), parameters);
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Validates a trailing action array.
    private static void validateActions(Node... actions) {
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
    }

    /// Updates the leading navigation slot.
    private void updateNavigation(@Nullable Node node) {
        boolean visible = node != null;
        navigationSlot.getChildren().clear();
        if (node != null) {
            navigationSlot.getChildren().add(node);
        }
        navigationSlot.setVisible(visible);
        navigationSlot.setManaged(visible);
        notifyAccessibleItemsChanged();
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

    /// Updates layout state that cannot be expressed reliably through user-agent CSS.
    private void updateVariantLayout() {
        M3TopAppBarVariant variant = getVariant();
        boolean centerAligned = variant == M3TopAppBarVariant.CENTER_ALIGNED;
        boolean tall = variant == M3TopAppBarVariant.MEDIUM || variant == M3TopAppBarVariant.LARGE;

        if (centerAligned) {
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            titleLabel.setAlignment(Pos.CENTER);
            spacer.setVisible(false);
            spacer.setManaged(false);
        } else {
            HBox.setHgrow(titleLabel, null);
            titleLabel.setAlignment(Pos.CENTER_LEFT);
            spacer.setVisible(true);
            spacer.setManaged(true);
        }

        if (variant == M3TopAppBarVariant.MEDIUM) {
            setMinHeight(112.0);
            setPrefHeight(112.0);
        } else if (variant == M3TopAppBarVariant.LARGE) {
            setMinHeight(152.0);
            setPrefHeight(152.0);
        } else {
            setMinHeight(Region.USE_COMPUTED_SIZE);
            setPrefHeight(Region.USE_COMPUTED_SIZE);
        }

        setAlignment(tall ? Pos.BOTTOM_LEFT : Pos.CENTER_LEFT);
    }
}
