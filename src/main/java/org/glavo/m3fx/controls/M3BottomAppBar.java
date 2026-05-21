// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BottomAppBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 bottom app bar.
///
/// `M3BottomAppBar` hosts horizontal action nodes and an optional floating action slot aligned according to
/// [M3BottomAppBarFloatingActionAlignment]. Use it for screen-level actions at the bottom edge of an
/// application, especially when paired with a [M3FloatingActionButton].
///
/// See [Material Design bottom app bars](https://m3.material.io/components/bottom-app-bar/overview).
@NotNullByDefault
public class M3BottomAppBar extends Control {
    /// The base style class for M3FX bottom app bars.
    public static final String STYLE_CLASS = "m3-bottom-app-bar";

    /// The actions container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-bottom-app-bar-actions";

    /// The floating action slot style class.
    public static final String FLOATING_ACTION_STYLE_CLASS = "m3-bottom-app-bar-floating-action";

    /// The optional floating action node property.
    private final ObjectProperty<@Nullable Node> floatingAction = new SimpleObjectProperty<>(this, "floatingAction");

    /// The floating action node alignment property.
    private final ObjectProperty<M3BottomAppBarFloatingActionAlignment> floatingActionAlignment =
            new SimpleObjectProperty<>(this, "floatingActionAlignment", M3BottomAppBarFloatingActionAlignment.END) {
                /// Updates alignment style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3BottomAppBarFloatingActionAlignment.END);
                        return;
                    }
                    updateFloatingActionAlignmentStyle();
                    requestLayout();
                }
            };

    /// The mutable regular action node list.
    private final ObservableList<Node> actions = FXCollections.observableArrayList();

    /// Creates an empty bottom app bar.
    public M3BottomAppBar() {
        initialize();
    }

    /// Creates a bottom app bar containing the supplied action nodes.
    public M3BottomAppBar(Node... actions) {
        initialize();
        addActions(actions);
    }

    /// Creates a bottom app bar with floating action content, alignment, and regular actions.
    public M3BottomAppBar(
            M3BottomAppBarFloatingActionAlignment floatingActionAlignment,
            @Nullable Node floatingAction,
            Node... actions
    ) {
        this(actions);
        setFloatingActionAlignment(floatingActionAlignment);
        setFloatingAction(floatingAction);
    }

    /// Returns the mutable action node list.
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Adds one regular action node.
    public final void addAction(Node action) {
        getActions().add(Objects.requireNonNull(action, "action"));
    }

    /// Adds regular actions after validating the action array.
    public final void addActions(Node... actions) {
        validateActions(actions);
        getActions().addAll(actions);
    }

    /// Replaces all regular action nodes.
    public final void setActions(Node... actions) {
        validateActions(actions);
        getActions().setAll(actions);
    }

    /// Removes all regular action nodes.
    public final void clearActions() {
        getActions().clear();
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

    /// Returns the floating action node alignment.
    public final M3BottomAppBarFloatingActionAlignment getFloatingActionAlignment() {
        return floatingActionAlignment.get();
    }

    /// Sets the floating action node alignment.
    public final void setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment floatingActionAlignment) {
        this.floatingActionAlignment.set(Objects.requireNonNull(floatingActionAlignment, "floatingActionAlignment"));
    }

    /// Returns the floating action node alignment property.
    public final ObjectProperty<M3BottomAppBarFloatingActionAlignment> floatingActionAlignmentProperty() {
        return floatingActionAlignment;
    }

    /// Returns the user-agent stylesheet for M3FX bottom app bars.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("bottom-app-bar.css");
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        floatingAction.addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        updateFloatingActionAlignmentStyle();
    }

    /// Returns accessibility attributes for the action and floating action collection.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case ITEM_COUNT -> M3Accessible.itemCount(getActions(), getFloatingAction());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getActions(), getFloatingAction(), parameters);
            case FOCUS_NODE -> M3Accessible.firstFocusTarget(getActions(), getFloatingAction());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed action and floating action children.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3Accessible.firstFocusTarget(
                    getActions(),
                    getFloatingAction()
            ));
            case SHOW_ITEM -> M3Accessible.showItem(M3Accessible.itemAt(getActions(), getFloatingAction(), parameters));
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 bottom app bar skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BottomAppBarSkin(this);
    }

    /// Validates a regular action array.
    private static void validateActions(Node... actions) {
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
    }

    /// Notifies accessibility clients that the indexed bottom app bar item collection changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    }

    /// Updates the active floating action alignment style class.
    private void updateFloatingActionAlignmentStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getFloatingActionAlignment().getStyleClass(),
                M3BottomAppBarFloatingActionAlignment.START.getStyleClass(),
                M3BottomAppBarFloatingActionAlignment.CENTER.getStyleClass(),
                M3BottomAppBarFloatingActionAlignment.END.getStyleClass()
        );
    }
}
