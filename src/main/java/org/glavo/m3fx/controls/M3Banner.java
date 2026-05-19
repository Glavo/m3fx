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
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 banner for persistent contextual messages and actions.
@NotNullByDefault
public class M3Banner extends HBox {
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

    /// The slot that hosts the optional leading icon.
    private final StackPane iconSlot = new StackPane();

    /// The label that renders the banner message.
    private final Label textLabel = new Label();

    /// The trailing action node container.
    private final HBox actions = new HBox();

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
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed icon and action children.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SHOW_ITEM -> M3Accessible.showItem(M3Accessible.itemAt(getIcon(), getActions(), parameters));
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Initializes child nodes, style classes, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);

        iconSlot.getStyleClass().add(ICON_STYLE_CLASS);
        textLabel.getStyleClass().add(TEXT_STYLE_CLASS);
        actions.getStyleClass().add(ACTIONS_STYLE_CLASS);

        setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textLabel, Priority.ALWAYS);
        textLabel.setMaxWidth(Double.MAX_VALUE);
        textLabel.setWrapText(true);
        textLabel.textProperty().bind(text);

        text.addListener(observable -> updateAccessibleText());
        icon.addListener((observable, oldValue, newValue) -> updateIcon(newValue));
        actions.getChildren().addListener((ListChangeListener<Node>) change -> {
            updateActionsVisibility();
            notifyAccessibleItemsChanged();
        });

        updateIcon(getIcon());
        updateActionsVisibility();
        updateAccessibleText();

        getChildren().addAll(iconSlot, textLabel, actions);
    }

    /// Validates a trailing action array.
    private static void validateActions(Node... actions) {
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
    }

    /// Updates the leading icon slot.
    private void updateIcon(@Nullable Node node) {
        boolean visible = node != null;
        iconSlot.getChildren().clear();
        if (node != null) {
            iconSlot.getChildren().add(node);
        }
        iconSlot.setVisible(visible);
        iconSlot.setManaged(visible);
        notifyAccessibleItemsChanged();
    }

    /// Updates the trailing action container visibility.
    private void updateActionsVisibility() {
        boolean visible = !actions.getChildren().isEmpty();
        actions.setVisible(visible);
        actions.setManaged(visible);
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
    }
}
