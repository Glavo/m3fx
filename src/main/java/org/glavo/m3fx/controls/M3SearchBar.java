// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 search bar.
@NotNullByDefault
public class M3SearchBar extends HBox {
    /// The base style class for M3FX search bars.
    public static final String STYLE_CLASS = "m3-search-bar";

    /// The active pseudo-class used when the search bar owns active search input.
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

    /// The style class applied to the search editor.
    public static final String INPUT_STYLE_CLASS = "m3-search-bar-input";

    /// The style class applied to the leading slot.
    public static final String LEADING_STYLE_CLASS = "m3-search-bar-leading";

    /// The style class applied to the trailing action container.
    public static final String TRAILING_STYLE_CLASS = "m3-search-bar-trailing";

    /// The default search bar height.
    private static final double DEFAULT_HEIGHT = 56.0;

    /// The default horizontal padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default spacing between content slots.
    private static final double DEFAULT_CONTENT_SPACING = 12.0;

    /// The leading content node property.
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading") {
        /// Updates the leading slot.
        @Override
        protected void invalidated() {
            updateLeading();
        }
    };

    /// The action handler property.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    /// Whether this search bar is in its active input state.
    private final BooleanProperty active = new SimpleBooleanProperty(this, "active") {
        /// Updates active pseudo-class state and input focus.
        @Override
        protected void invalidated() {
            boolean active = get();
            pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, active);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            if (active) {
                editor.requestFocus();
            }
        }
    };

    /// The leading slot.
    private final StackPane leadingSlot = new StackPane();

    /// The editable search input.
    private final TextField editor = new TextField();

    /// The trailing action slot.
    private final HBox trailingBox = new HBox();

    /// Creates an empty search bar.
    public M3SearchBar() {
        this("");
    }

    /// Creates a search bar with prompt text.
    public M3SearchBar(String promptText) {
        initialize();
        setPromptText(promptText);
    }

    /// Returns the text entered in this search bar.
    public final String getText() {
        return editor.getText();
    }

    /// Sets the text entered in this search bar.
    public final void setText(String text) {
        editor.setText(Objects.requireNonNull(text, "text"));
    }

    /// Returns the text property.
    public final StringProperty textProperty() {
        return editor.textProperty();
    }

    /// Returns the prompt text displayed when the search text is empty.
    public final String getPromptText() {
        @Nullable String promptText = editor.getPromptText();
        return promptText == null ? "" : promptText;
    }

    /// Sets the prompt text displayed when the search text is empty.
    public final void setPromptText(String promptText) {
        editor.setPromptText(Objects.requireNonNull(promptText, "promptText"));
    }

    /// Returns the prompt text property.
    public final StringProperty promptTextProperty() {
        return editor.promptTextProperty();
    }

    /// Returns whether this search bar is in its active input state.
    public final boolean isActive() {
        return active.get();
    }

    /// Sets whether this search bar is in its active input state.
    public final void setActive(boolean active) {
        this.active.set(active);
    }

    /// Returns the active input state property.
    public final BooleanProperty activeProperty() {
        return active;
    }

    /// Returns the editable search input used by this search bar.
    public final TextField getEditor() {
        return editor;
    }

    /// Returns the leading content node.
    public final @Nullable Node getLeading() {
        return leading.get();
    }

    /// Sets the leading content node.
    public final void setLeading(@Nullable Node leading) {
        this.leading.set(leading);
    }

    /// Returns the leading content node property.
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return leading;
    }

    /// Returns the mutable trailing action list.
    public final ObservableList<Node> getTrailingActions() {
        return trailingBox.getChildren();
    }

    /// Adds one trailing action node.
    public final void addTrailingAction(Node action) {
        getTrailingActions().add(Objects.requireNonNull(action, "action"));
    }

    /// Adds trailing action nodes.
    public final void addTrailingActions(Node... actions) {
        validateActions(actions);
        getTrailingActions().addAll(actions);
    }

    /// Replaces all trailing action nodes.
    public final void setTrailingActions(Node... actions) {
        validateActions(actions);
        getTrailingActions().setAll(actions);
    }

    /// Removes all trailing action nodes.
    public final void clearTrailingActions() {
        getTrailingActions().clear();
    }

    /// Returns the action handler.
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the action handler property.
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Returns the user-agent stylesheet for M3FX search bars.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("search.css");
    }

    /// Fires this search bar's action event.
    public final void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    /// Moves the search bar into its active input state.
    public final void activate() {
        setActive(true);
    }

    /// Moves the search bar out of its active input state.
    public final void deactivate() {
        setActive(false);
    }

    /// Clears the current search text.
    public final void clear() {
        setText("");
    }

    /// Clears the current search text and moves the search bar out of its active input state.
    public final void clearAndDeactivate() {
        clear();
        deactivate();
    }

    /// Returns accessibility attributes for the embedded search editor.
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isActive();
            case FOCUS_NODE -> editor;
            case TEXT -> getText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by the search bar.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> fire();
            case REQUEST_FOCUS -> activate();
            case SET_TEXT -> {
                if (parameters.length > 0 && parameters[0] instanceof String text) {
                    setText(text);
                }
            }
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes, default slots, and search behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setMinHeight(DEFAULT_HEIGHT);
        setPrefHeight(DEFAULT_HEIGHT);
        setPadding(new Insets(0.0, DEFAULT_HORIZONTAL_PADDING, 0.0, DEFAULT_HORIZONTAL_PADDING));
        setSpacing(DEFAULT_CONTENT_SPACING);

        leadingSlot.getStyleClass().add(LEADING_STYLE_CLASS);
        editor.getStyleClass().add(INPUT_STYLE_CLASS);
        trailingBox.getStyleClass().add(TRAILING_STYLE_CLASS);
        HBox.setHgrow(editor, Priority.ALWAYS);

        setLeading(defaultLeadingNode());
        trailingBox.getChildren().addListener((ListChangeListener<Node>) change -> updateTrailingVisibility());
        editor.textProperty().addListener((observable, oldValue, newValue) ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT));
        editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setActive(true);
            }
        });
        editor.setOnAction(event -> fire());
        setOnMouseClicked(event -> activate());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        getChildren().addAll(leadingSlot, editor, trailingBox);
        updateLeading();
        updateTrailingVisibility();
    }

    /// Handles keyboard shortcuts owned by the search bar container.
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (isActive()) {
                    deactivate();
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Updates the leading slot content.
    private void updateLeading() {
        @Nullable Node node = getLeading();
        leadingSlot.getChildren().clear();
        leadingSlot.setVisible(node != null);
        leadingSlot.setManaged(node != null);
        if (node != null) {
            leadingSlot.getChildren().add(node);
        }
    }

    /// Updates trailing action container visibility.
    private void updateTrailingVisibility() {
        boolean visible = !trailingBox.getChildren().isEmpty();
        trailingBox.setVisible(visible);
        trailingBox.setManaged(visible);
    }

    /// Validates a trailing action array.
    private static void validateActions(Node... actions) {
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
    }

    /// Creates the default leading search glyph node.
    private static Node defaultLeadingNode() {
        Label label = new Label("S");
        label.getStyleClass().add("m3-search-bar-default-leading");
        return label;
    }
}
