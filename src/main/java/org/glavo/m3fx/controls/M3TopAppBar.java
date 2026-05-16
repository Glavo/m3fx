// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
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

    /// Returns the user-agent stylesheet for M3FX top app bars.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("top-app-bar.css");
    }

    /// Initializes child nodes, style classes, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        navigationSlot.getStyleClass().add(NAVIGATION_STYLE_CLASS);
        titleLabel.getStyleClass().add(TITLE_STYLE_CLASS);
        actions.getStyleClass().add(ACTIONS_STYLE_CLASS);

        setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleLabel.textProperty().bind(title);
        navigation.addListener((observable, oldValue, newValue) -> updateNavigation(newValue));
        updateNavigation(getNavigation());

        getChildren().addAll(navigationSlot, titleLabel, spacer, actions);
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
    }
}
