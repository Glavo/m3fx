// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
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
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SearchBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 search bar for entering or activating search.
///
/// `M3SearchBar` contains a text editor, leading slot, trailing action list, prompt text, and active state. It
/// can be used as a standalone search field or as the primary input inside [M3SearchView]. The control exposes
/// action events, input forwarding methods, and accessibility text while the skin renders Material container,
/// state-layer, focus, and motion feedback.
///
/// See [Material Design search](https://m3.material.io/components/search/overview).
@NotNullByDefault
public class M3SearchBar extends Control {
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

    // Backing property for the public leading slot API.
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading") {
        /// Updates accessibility state when the leading slot changes.
        @Override
        protected void invalidated() {
            notifyAccessibleItemsChanged();
        }
    };

    // Backing property for the public action handler API.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    // Backing property for the public active state API.
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
            notifyFocusNodeChanged();
        }
    };

    /// The editable search input.
    private final TextField editor = new TextField();

    /// The mutable trailing action list.
    private final ObservableList<Node> trailingActions = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between search bar slots.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentFocusNode);

    /// Creates an empty search bar.
    public M3SearchBar() {
        this("");
    }

    /// Creates a search bar with prompt text.
    ///
    /// @param promptText the prompt text displayed when the search text is empty
    public M3SearchBar(String promptText) {
        initialize();
        setPromptText(promptText);
    }

    /// Returns the text entered in this search bar.
    ///
    /// @return the text entered in this search bar
    public final String getText() {
        return editor.getText();
    }

    /// Sets the text entered in this search bar.
    ///
    /// @param text the text entered in this search bar
    public final void setText(String text) {
        editor.setText(Objects.requireNonNull(text, "text"));
    }

    /// Returns the text property.
    ///
    /// @return the embedded editor text property
    public final StringProperty textProperty() {
        return editor.textProperty();
    }

    /// Returns the prompt text displayed when the search text is empty.
    ///
    /// @return the prompt text displayed when the search text is empty
    public final String getPromptText() {
        @Nullable String promptText = editor.getPromptText();
        return promptText == null ? "" : promptText;
    }

    /// Sets the prompt text displayed when the search text is empty.
    ///
    /// @param promptText the prompt text displayed when the search text is empty
    public final void setPromptText(String promptText) {
        editor.setPromptText(Objects.requireNonNull(promptText, "promptText"));
    }

    /// Returns the prompt text property.
    ///
    /// @return the embedded editor prompt text property
    public final StringProperty promptTextProperty() {
        return editor.promptTextProperty();
    }

    /// Returns whether this search bar is in its active input state.
    ///
    /// @return `true` if this search bar is in its active input state
    public final boolean isActive() {
        return active.get();
    }

    /// Sets whether this search bar is in its active input state.
    ///
    /// @param active whether this search bar is in its active input state
    public final void setActive(boolean active) {
        this.active.set(active);
    }

    /// Returns the active input state property.
    ///
    /// @return the active input state property
    public final BooleanProperty activeProperty() {
        return active;
    }

    /// Returns the editable search input used by this search bar.
    ///
    /// @return the embedded editable search input
    public final TextField getEditor() {
        return editor;
    }

    /// Returns the leading content node.
    ///
    /// @return the leading content node, or `null` if none is set
    public final @Nullable Node getLeading() {
        return leading.get();
    }

    /// Sets the leading content node.
    ///
    /// @param leading the leading content node, or `null` to clear it
    public final void setLeading(@Nullable Node leading) {
        this.leading.set(leading);
    }

    /// Returns the leading content node property.
    ///
    /// @return the leading content node property
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return leading;
    }

    /// Returns the mutable trailing action list.
    ///
    /// @return the mutable trailing action list
    public final ObservableList<Node> getTrailingActions() {
        return trailingActions;
    }

    /// Adds one trailing action node.
    ///
    /// @param action the trailing action node to add
    public final void addTrailingAction(Node action) {
        getTrailingActions().add(Objects.requireNonNull(action, "action"));
    }

    /// Adds trailing action nodes.
    ///
    /// @param actions the trailing action nodes to add
    public final void addTrailingActions(Node... actions) {
        validateActions(actions);
        getTrailingActions().addAll(actions);
    }

    /// Replaces all trailing action nodes.
    ///
    /// @param actions the replacement trailing action nodes
    public final void setTrailingActions(Node... actions) {
        validateActions(actions);
        getTrailingActions().setAll(actions);
    }

    /// Removes all trailing action nodes.
    public final void clearTrailingActions() {
        getTrailingActions().clear();
    }

    /// Returns the action handler.
    ///
    /// @return the action handler, or `null` if none is set
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    ///
    /// @param onAction the action handler, or `null` to clear it
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the action handler property.
    ///
    /// @return the action handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Returns the user-agent stylesheet for M3FX search bars.
    ///
    /// @return the search user-agent stylesheet URL
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
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case CONTENTS -> editor;
            case EXPANDED -> isActive();
            case FOCUS_NODE -> accessibleFocusNode();
            case ITEM_COUNT -> accessibleItemCount();
            case ITEM_AT_INDEX -> accessibleItemAt(parameters);
            case TEXT -> getText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by the search bar.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> fire();
            case REQUEST_FOCUS -> focusEditor();
            case SHOW_ITEM -> focusAccessibleItem(accessibleActionItem(parameters));
            case EXPAND -> activate();
            case COLLAPSE -> deactivate();
            case SET_TEXT -> {
                if (parameters.length > 0 && parameters[0] instanceof String text) {
                    setText(text);
                }
            }
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 search bar skin.
    ///
    /// @return the default Material Design 3 search bar skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SearchBarSkin(this);
    }

    /// Adds base style classes, default slots, and search behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        editor.getStyleClass().add(INPUT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(true);
        setMinHeight(DEFAULT_HEIGHT);
        setPrefHeight(DEFAULT_HEIGHT);
        setMaxHeight(USE_PREF_SIZE);
        setPadding(new Insets(0.0, DEFAULT_HORIZONTAL_PADDING, 0.0, DEFAULT_HORIZONTAL_PADDING));

        setLeading(defaultLeadingNode());
        trailingActions.addListener((ListChangeListener<Node>) change -> {
            notifyAccessibleItemsChanged();
            requestLayout();
        });
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
        focusNotifier.start();
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
            case ENTER, SPACE -> {
                if (!isActive()) {
                    focusEditor();
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Focuses the embedded editor and enters active input state.
    private void focusEditor() {
        activate();
        editor.requestFocus();
        notifyFocusNodeChanged();
    }

    /// Returns the number of indexed child items exposed by the search bar.
    private int accessibleItemCount() {
        return (getLeading() == null ? 0 : 1) + 1 + getTrailingActions().size();
    }

    /// Returns the child item at an accessibility index.
    private @Nullable Node accessibleItemAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }

        @Nullable Node leading = getLeading();
        if (leading != null) {
            if (index == 0) {
                return leading;
            }
            index--;
        }

        if (index == 0) {
            return editor;
        }
        index--;

        ObservableList<Node> trailingActions = getTrailingActions();
        return index < trailingActions.size() ? trailingActions.get(index) : null;
    }

    /// Returns the child item referenced by accessibility action parameters.
    private @Nullable Node accessibleActionItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return editor;
        }
        if (parameters[0] instanceof Number) {
            return accessibleItemAt(parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = accessibleActionItem(parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the child item referenced by one accessibility action parameter.
    private @Nullable Node accessibleActionItem(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return accessibleItemAt(number);
        }
        if (parameter instanceof Node node && containsAccessibleItem(node)) {
            return node;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = accessibleActionItem(value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = accessibleActionItem(value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns whether a node is one of the search bar's indexed accessibility items.
    private boolean containsAccessibleItem(Node node) {
        @Nullable Node leading = getLeading();
        if (node == leading || node == editor || M3Accessible.containsNode(editor, node)) {
            return true;
        }
        if (leading != null && M3Accessible.containsNode(leading, node)) {
            return true;
        }
        for (Node action : getTrailingActions()) {
            if (node == action || M3Accessible.containsNode(action, node)) {
                return true;
            }
        }
        return false;
    }

    /// Focuses an indexed child item, falling back to the editor when the item is not focusable.
    private void focusAccessibleItem(@Nullable Node item) {
        activate();
        if (M3Accessible.focusTarget(item) == null) {
            editor.requestFocus();
            notifyFocusNodeChanged();
            return;
        }
        M3Accessible.showItem(item);
        notifyFocusNodeChanged();
    }

    /// Returns the current accessibility focus node.
    ///
    /// @return the focused indexed item when one owns focus, otherwise the embedded editor
    private Node accessibleFocusNode() {
        @Nullable Node focusNode = currentFocusNode();
        return focusNode == null ? editor : focusNode;
    }

    /// Returns the current focused slot target, or `null` when focus is outside the search bar.
    private @Nullable Node currentFocusNode() {
        if (getScene() != null) {
            @Nullable Node focusOwner = getScene().getFocusOwner();
            if (focusOwner != null) {
                if (focusOwner == this) {
                    return this;
                }

                int count = accessibleItemCount();
                for (int index = 0; index < count; index++) {
                    @Nullable Node item = accessibleItemAt(index);
                    if (item != null && M3Accessible.containsNode(item, focusOwner)) {
                        @Nullable Node focusTarget = M3Accessible.focusTarget(item);
                        if (focusTarget != null) {
                            return M3Accessible.canReach(focusOwner) ? focusOwner : focusTarget;
                        }
                    }
                }
            }
        }
        return null;
    }

    /// Notifies accessibility clients that indexed child items changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyFocusNodeChanged();
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        focusNotifier.refresh();
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
