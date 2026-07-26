// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SearchBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/// A Material Design 3 search bar for entering or activating search.
///
/// `M3SearchBar` combines an editable text value with leading and trailing content slots. It can be used as a
/// standalone search entry or as the primary input of [M3SearchView]. Activating the bar focuses its editor;
/// focusing the editor also makes the bar active. Deactivation changes the visual and accessibility state but does
/// not clear the entered text.
///
/// Pressing Enter in the editor, invoking [#fire()], or using the accessibility fire action emits an [ActionEvent].
/// Disabled search bars do not fire. The default leading content is a search indicator, the trailing action list is
/// empty, and the bar is inactive with empty text and prompt text.
///
/// See [Material Design search](https://m3.material.io/components/search/overview).
@NotNullByDefault
public final class M3SearchBar extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-search-bar";

    /// The active pseudo-class used when the search bar owns active search input.
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

    /// The style class applied to the search editor.
    private static final String INPUT_STYLE_CLASS = "m3-search-bar-input";

    /// The default search bar height.
    private static final double DEFAULT_HEIGHT = 56.0;

    /// The default horizontal padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The editable search input.
    private final TextField editor = new TextField();

    /// The mutable trailing action list.
    private final ObservableList<Node> trailingActions =
            M3ObservableLists.identityDistinctElementList("action");

    /// Whether the next active-state change should preserve the current slot focus.
    private boolean suppressActiveEditorFocus;

    /// Notifies accessibility clients when focus moves between search bar slots.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentFocusNode);

    /// Creates an inactive search bar with empty text and prompt text.
    public M3SearchBar() {
        this("");
    }

    /// Creates an inactive search bar with the specified prompt and empty search text.
    ///
    /// @param promptText the prompt text displayed when the search text is empty
    /// @throws NullPointerException if `promptText` is `null`
    public M3SearchBar(String promptText) {
        initialize();
        setPromptText(promptText);
    }

    /// The non-null search text edited by the user.
    ///
    /// Assigning or binding a `null` value throws [NullPointerException].
    ///
    /// @defaultValue `""`
    private final StringProperty text = new SimpleStringProperty(this, "text", "") {
        /// Keeps search text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "text"));
        }
    };

    /// Returns the text entered in this search bar.
    ///
    /// @return the text entered in this search bar
    public final String getText() {
        return text.get();
    }

    /// Sets the text entered in this search bar.
    ///
    /// @param text the text entered in this search bar
    /// @throws NullPointerException if `text` is `null`
    public final void setText(String text) {
        this.text.set(text);
    }

    /// Returns the observable, bidirectionally bindable search-text property.
    ///
    /// The property has an initial value of `""` and rejects `null` values, including values supplied by a binding.
    ///
    /// @return the search-text property
    public final StringProperty textProperty() {
        return text;
    }

    /// The non-null prompt displayed when [#getText()] is empty.
    ///
    /// Assigning or binding a `null` value throws [NullPointerException].
    ///
    /// @defaultValue `""`
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "") {
        /// Keeps prompt text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "promptText"));
        }
    };

    /// Returns the prompt text displayed when the search text is empty.
    ///
    /// @return the prompt text displayed when the search text is empty
    public final String getPromptText() {
        return promptText.get();
    }

    /// Sets the prompt text displayed when the search text is empty.
    ///
    /// @param promptText the prompt text displayed when the search text is empty
    /// @throws NullPointerException if `promptText` is `null`
    public final void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    /// Returns the observable, bidirectionally bindable prompt-text property.
    ///
    /// The property has an initial value of `""` and rejects `null` values, including values supplied by a binding.
    ///
    /// @return the prompt-text property
    public final StringProperty promptTextProperty() {
        return promptText;
    }

    /// Whether this bar is in its active input state.
    ///
    /// Changing the value to `true` requests focus for the editor when the control is reachable. Changing it to
    /// `false` does not clear the text or forcibly move focus.
    ///
    /// @defaultValue `false`
    private final BooleanProperty active = new SimpleBooleanProperty(this, "active") {
        /// Updates active pseudo-class state and input focus.
        @Override
        protected void invalidated() {
            boolean active = get();
            pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, active);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            if (active && !suppressActiveEditorFocus && M3Accessible.canReach(editor)) {
                M3Accessible.showItem(M3SearchBar.this, editor);
            }
            notifyFocusNodeChanged();
        }
    };

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

    /// Returns the observable, bindable active-state property.
    ///
    /// The property is `false` by default. Setting it to `true` requests focus for editable search text when the
    /// control is reachable; setting it to `false` does not clear text or forcibly move focus.
    ///
    /// @return the active-state property
    public final BooleanProperty activeProperty() {
        return active;
    }

    /// The node displayed before the editable text.
    ///
    /// The default is a search indicator supplied by the control. A `null` value leaves the leading slot empty.
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading") {
        /// Updates accessibility state when the leading slot changes.
        @Override
        protected void invalidated() {
            notifyAccessibleItemsChanged();
        }
    };

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

    /// Returns the observable, bindable leading-content property.
    ///
    /// A new search bar contains a search indicator. The property accepts `null` to leave the leading slot empty;
    /// changes update the indexed accessibility children.
    ///
    /// @return the leading-content property
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return leading;
    }

    /// The handler invoked for search action events.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

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

    /// Returns the observable, bindable search-action handler property.
    ///
    /// The property is `null` by default. Changing it replaces the handler registered for [ActionEvent#ACTION].
    ///
    /// @return the search-action handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Returns the editable search input used by this search bar.
    ///
    /// @return the editable search input
    final TextField editor() {
        return editor;
    }

    /// Returns the mutable trailing action list.
    ///
    /// The returned list is live, mutable, and ordered. Changes update the visible action row immediately. `null`
    /// elements are rejected. Each node must satisfy the normal JavaFX single-parent rule when the control displays
    /// it, and the same node must not be inserted more than once.
    ///
    /// @return the live trailing action list
    public final ObservableList<Node> getTrailingActions() {
        return trailingActions;
    }

    /// Returns the user-agent stylesheet for M3FX search bars.
    ///
    /// @return the search user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("search.css");
    }

    /// Fires this search bar's action event unless the control is disabled.
    ///
    /// Firing does not activate, deactivate, or clear the search bar.
    public final void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    /// Moves the search bar into its active input state and requests focus for editable search text when reachable.
    public final void activate() {
        setActive(true);
    }

    /// Moves the search bar out of its active input state without changing its text.
    public final void deactivate() {
        setActive(false);
    }

    /// Replaces the current search text with an empty string without changing the active state.
    public final void clear() {
        setText("");
    }

    /// Clears the current search text and moves the search bar out of its active input state.
    public final void clearAndDeactivate() {
        clear();
        deactivate();
    }

    /// Returns accessibility attributes for this search bar.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
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
    /// @param action     the requested accessibility action
    /// @param parameters the optional action parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case FIRE -> fire();
            case REQUEST_FOCUS -> focusAccessibleItem(accessibleFocusNode());
            case SHOW_ITEM -> showAccessibleItem(parameters);
            case EXPAND -> {
                if (M3Accessible.canReach(this)) {
                    activate();
                }
            }
            case COLLAPSE -> deactivate();
            case SET_TEXT -> {
                if (parameters.length > 0 && parameters[0] instanceof String replacementText) {
                    setText(replacementText);
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
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        editor.getStyleClass().add(INPUT_STYLE_CLASS);
        editor.setPromptText("");
        editor.textProperty().bindBidirectional(text);
        editor.promptTextProperty().bindBidirectional(promptText);
        setAccessibleRole(AccessibleRole.PARENT);
        M3Accessible.installAccessibleActionRoute(this, () -> focusAccessibleItem(accessibleFocusNode()), this::showAccessibleItem);
        setFocusTraversable(true);
        M3Css.setMinHeightIfUnbound(this, DEFAULT_HEIGHT);
        M3Css.setPrefHeightIfUnbound(this, DEFAULT_HEIGHT);
        M3Css.setMaxHeightIfUnbound(this, USE_PREF_SIZE);
        M3Css.setPaddingIfUnbound(this, new Insets(0.0, DEFAULT_HORIZONTAL_PADDING, 0.0, DEFAULT_HORIZONTAL_PADDING));

        setLeading(defaultLeadingNode());
        trailingActions.addListener((ListChangeListener<Node>) change -> {
            notifyAccessibleItemsChanged();
            requestLayout();
        });
        text.addListener((observable, oldValue, newValue) ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT));
        editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setActive(true);
            }
        });
        editor.setOnAction(event -> fire());
        setOnMouseClicked(this::handleMouseClicked);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        focusNotifier.start();
    }

    /// Activates the search bar unless a slot action owns the pointer click.
    private void handleMouseClicked(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        Node target = event.getPickResult().getIntersectedNode();
        @Nullable Node leading = getLeading();
        if (leading != null
                && M3Accessible.structuralFocusTarget(leading) != null
                && M3Accessible.containsNode(leading, target)) {
            return;
        }
        for (Node action : getTrailingActions()) {
            if (M3Accessible.structuralFocusTarget(action) != null
                    && M3Accessible.containsNode(action, target)) {
                return;
            }
        }
        activate();
    }

    /// Handles keyboard shortcuts owned by the search bar container.
    private void handleKeyPressed(KeyEvent event) {
        if (handleSlotNavigationKey(event)) {
            return;
        }

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

    /// Handles keyboard focus traversal between the leading slot, editor, and trailing actions.
    private boolean handleSlotNavigationKey(KeyEvent event) {
        return M3FocusTraversal.handleHorizontalKeyFocus(this, event, slotFocusTargets());
    }

    /// Focuses editable search text and enters active input state.
    private void focusEditor() {
        if (!M3Accessible.canReach(this)) {
            return;
        }
        activate();
        if (M3Accessible.canReach(editor)) {
            M3Accessible.showItem(this, editor);
        }
        notifyFocusNodeChanged();
    }

    /// Returns the number of indexed child items exposed by the search bar.
    private int accessibleItemCount() {
        return M3Accessible.itemCount(getLeading(), editor, getTrailingActions());
    }

    /// Returns the child item at an accessibility index.
    private @Nullable Node accessibleItemAt(Object... parameters) {
        return M3Accessible.itemAt(getLeading(), editor, getTrailingActions(), parameters);
    }

    /// Focuses an indexed child item, falling back to the editor when the item is not focusable.
    ///
    /// @param item the item to focus, or `null` to use the editor fallback
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleItem(@Nullable Node item) {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return false;
        }
        activate();
        if (M3Accessible.structuralFocusTarget(item) == null) {
            if (M3Accessible.canReach(editor) && M3Accessible.showItem(this, editor)) {
                notifyFocusNodeChanged();
                return true;
            }
            return false;
        }
        if (M3Accessible.showItem(this, item)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Shows and focuses the requested accessible child or a descendant popup target.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested target
    final boolean showAccessibleItem(Object... parameters) {
        if (!M3Accessible.isEffectivelyReachable(this)) {
            return false;
        }
        if (parameters.length > 0 && !M3Accessible.canShowItem(getLeading(), editor, getTrailingActions(), parameters)) {
            return false;
        }
        activateWithoutEditorFocus();
        if (M3Accessible.showCurrentOrItem(this, getLeading(), editor, getTrailingActions(), parameters)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Moves the search bar into its active state while preserving the current accessibility child focus.
    private void activateWithoutEditorFocus() {
        boolean previousSuppressActiveEditorFocus = suppressActiveEditorFocus;
        suppressActiveEditorFocus = true;
        try {
            activate();
        } finally {
            suppressActiveEditorFocus = previousSuppressActiveEditorFocus;
        }
    }

    /// Returns the current accessibility focus node.
    ///
    /// @return the focused indexed item, first interactive leading item, or editable search text
    private Node accessibleFocusNode() {
        @Nullable Node focusNode = currentFocusNode();
        if (focusNode != null) {
            return focusNode;
        }
        @Nullable Node leadingFocusTarget = M3Accessible.structuralFocusTarget(getLeading());
        return leadingFocusTarget == null ? editor : leadingFocusTarget;
    }

    /// Returns the current focused slot target, or `null` when focus is outside the search bar.
    private @Nullable Node currentFocusNode() {
        if (!M3Accessible.canReach(this)) {
            return null;
        }
        if (isFocused()) {
            return this;
        }
        return M3Accessible.currentFocusTarget(this, getLeading(), editor, getTrailingActions());
    }

    /// Returns the current reachable focus targets in logical search bar slot order.
    private @Unmodifiable List<Node> slotFocusTargets() {
        return M3FocusTraversal.focusTargets(getLeading(), editor, getTrailingActions());
    }

    /// Notifies accessibility clients that indexed child items changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyFocusNodeChanged();
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Creates the default leading search glyph node.
    private static Node defaultLeadingNode() {
        M3InternalIcon icon = new M3InternalIcon(
                M3InternalIcon.Glyph.SEARCH,
                M3InternalIcon.ColorRole.ON_SURFACE
        );
        icon.getStyleClass().add("m3-search-bar-default-leading");
        return icon;
    }
}
