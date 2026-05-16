// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 bottom app bar.
@NotNullByDefault
public class M3BottomAppBar extends HBox {
    /// The base style class for M3FX bottom app bars.
    public static final String STYLE_CLASS = "m3-bottom-app-bar";

    /// The actions container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-bottom-app-bar-actions";

    /// The floating action slot style class.
    public static final String FLOATING_ACTION_STYLE_CLASS = "m3-bottom-app-bar-floating-action";

    /// The optional floating action node property.
    private final ObjectProperty<@Nullable Node> floatingAction = new SimpleObjectProperty<>(this, "floatingAction");

    /// The trailing action node container.
    private final HBox actions = new HBox();

    /// The flexible spacer between action and floating action regions.
    private final Region spacer = new Region();

    /// The slot that hosts the optional floating action node.
    private final StackPane floatingActionSlot = new StackPane();

    /// Creates an empty bottom app bar.
    public M3BottomAppBar() {
        initialize();
    }

    /// Creates a bottom app bar containing the supplied action nodes.
    public M3BottomAppBar(Node... actions) {
        initialize();
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
        getActions().addAll(actions);
    }

    /// Returns the mutable action node list.
    public final ObservableList<Node> getActions() {
        return actions.getChildren();
    }

    /// Returns the optional floating action node.
    public final @Nullable Node getFloatingAction() {
        return floatingAction.get();
    }

    /// Sets the optional floating action node.
    public final void setFloatingAction(@Nullable Node floatingAction) {
        this.floatingAction.set(floatingAction);
    }

    /// Returns the optional floating action node property.
    public final ObjectProperty<@Nullable Node> floatingActionProperty() {
        return floatingAction;
    }

    /// Returns the user-agent stylesheet for M3FX bottom app bars.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("bottom-app-bar.css");
    }

    /// Initializes child nodes, style classes, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        actions.getStyleClass().add(ACTIONS_STYLE_CLASS);
        floatingActionSlot.getStyleClass().add(FLOATING_ACTION_STYLE_CLASS);

        setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        floatingAction.addListener((observable, oldValue, newValue) -> updateFloatingAction(newValue));
        updateFloatingAction(getFloatingAction());

        getChildren().addAll(actions, spacer, floatingActionSlot);
    }

    /// Updates the floating action slot.
    private void updateFloatingAction(@Nullable Node node) {
        boolean visible = node != null;
        floatingActionSlot.getChildren().clear();
        if (node != null) {
            floatingActionSlot.getChildren().add(node);
        }
        floatingActionSlot.setVisible(visible);
        floatingActionSlot.setManaged(visible);
    }
}
