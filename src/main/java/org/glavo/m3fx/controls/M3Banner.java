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
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BannerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 banner for persistent contextual messages and actions.
///
/// `M3Banner` displays a message, optional leading icon, and trailing action nodes inside the current layout
/// instead of in an overlay. Use it for important contextual information that should remain visible until the
/// user acts or the application state changes.
///
/// See [Material Design](https://m3.material.io/) for the component and interaction principles used by M3FX.
@NotNullByDefault
public class M3Banner extends Control {
    /// The base style class for M3FX banners.
    public static final String STYLE_CLASS = "m3-banner";

    /// The leading icon slot style class.
    public static final String ICON_STYLE_CLASS = "m3-banner-icon";

    /// The text label style class.
    public static final String TEXT_STYLE_CLASS = "m3-banner-text";

    /// The actions container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-banner-actions";

    /// The banner message text property.
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    /// The optional leading icon property.
    private final ObjectProperty<@Nullable Node> icon = new SimpleObjectProperty<>(this, "icon");

    /// The mutable trailing action node list.
    private final ObservableList<Node> actions = FXCollections.observableArrayList();

    /// Creates an empty banner.
    public M3Banner() {
        this("");
    }

    /// Creates a banner with message text.
    public M3Banner(String text) {
        initialize();
        setText(text);
    }

    /// Creates a banner with message text and trailing actions.
    public M3Banner(String text, Node... actions) {
        this(text);
        addActions(actions);
    }

    /// Creates a banner with message text, a leading icon, and trailing actions.
    public static M3Banner withIcon(String text, Node icon, Node... actions) {
        M3Banner banner = new M3Banner(text, actions);
        banner.setIcon(Objects.requireNonNull(icon, "icon"));
        return banner;
    }

    /// Returns the banner message text.
    public final String getText() {
        return text.get();
    }

    /// Sets the banner message text.
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the banner message text property.
    public final StringProperty textProperty() {
        return text;
    }

    /// Returns the optional leading icon node.
    public final @Nullable Node getIcon() {
        return icon.get();
    }

    /// Sets the optional leading icon node.
    public final void setIcon(@Nullable Node icon) {
        this.icon.set(icon);
    }

    /// Returns the optional leading icon property.
    public final ObjectProperty<@Nullable Node> iconProperty() {
        return icon;
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

    /// Replaces all trailing action nodes.
    public final void setActions(Node... actions) {
        validateActions(actions);
        getActions().setAll(actions);
    }

    /// Removes all trailing action nodes.
    public final void clearActions() {
        getActions().clear();
    }

    /// Returns the user-agent stylesheet for M3FX banners.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("banner.css");
    }

    /// Returns accessibility attributes for the message and action collection.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> getText();
            case ITEM_COUNT -> M3Accessible.itemCount(getIcon(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getIcon(), getActions(), parameters);
            case FOCUS_NODE -> M3Accessible.firstFocusTarget(getActions());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed icon and action children.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showItem(M3Accessible.firstFocusTarget(getActions()));
            case SHOW_ITEM -> M3Accessible.showItem(M3Accessible.itemAt(getIcon(), getActions(), parameters));
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 banner skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BannerSkin(this);
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        text.addListener(observable -> updateAccessibleText());
        icon.addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        updateAccessibleText();
    }

    /// Validates a trailing action array.
    private static void validateActions(Node... actions) {
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
    }

    /// Updates the accessible text exposed by the banner.
    private void updateAccessibleText() {
        setAccessibleText(getText());
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Notifies accessibility clients that the indexed banner item collection changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    }
}
