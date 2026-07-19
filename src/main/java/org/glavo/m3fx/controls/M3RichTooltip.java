// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 rich tooltip with title, supporting text, and optional actions.
///
/// `M3RichTooltip` builds on [M3Tooltip] and supplies the structured content used by rich Material tooltips:
/// title text, supporting text, and an optional row of action nodes. Empty title or supporting text is omitted from
/// layout. The concatenated visible text is exposed as accessible help for the owner node.
///
/// Rich tooltips are interactive: the pointer may move from the owner into the popup without immediately hiding it,
/// and reachable action nodes participate in keyboard traversal. When [#isPersistent()] is `false`, installation
/// through [M3Tooltip#install(Node,M3Tooltip)] uses hover and focus activation. Persistent tooltips instead open from
/// explicit mouse or keyboard activation and remain visible until auto-hide, Escape, or a second owner activation.
///
/// The following example installs an interactive rich tooltip and closes it from an action:
///
/// ```java
/// M3Button helpButton = new M3Button("Help");
/// M3RichTooltip tooltip = new M3RichTooltip(
///         "Keyboard shortcuts", "Review the available keyboard commands.");
/// M3Button dismissButton = new M3Button("Dismiss");
/// dismissButton.setOnAction(event -> tooltip.hide());
/// tooltip.getActions().add(dismissButton);
/// M3Tooltip.install(helpButton, tooltip);
/// ```
///
/// See [Material Design tooltips](https://m3.material.io/components/tooltips/overview).
@NotNullByDefault
public final class M3RichTooltip extends M3Tooltip {
    /// The base style class for M3FX rich tooltips.
    public static final String STYLE_CLASS = "m3-rich-tooltip";

    /// The rich tooltip content container style class.
    public static final String CONTAINER_STYLE_CLASS = "m3-rich-tooltip-container";

    /// The title label style class.
    public static final String TITLE_STYLE_CLASS = "m3-rich-tooltip-title";

    /// The supporting text label style class.
    public static final String SUPPORTING_TEXT_STYLE_CLASS = "m3-rich-tooltip-supporting-text";

    /// The action row style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-rich-tooltip-actions";

    /// The title label.
    private final Label titleLabel = new Label();

    /// The supporting text label.
    private final Label supportingTextLabel = new Label();

    /// The action node row.
    private final HBox actions = new HBox();

    /// The root node containing the rich tooltip content.
    private final VBox container = new VBox();

    /// Creates a non-persistent rich tooltip with no title, supporting text, or actions.
    public M3RichTooltip() {
        this("", "");
    }

    /// Creates a non-persistent rich tooltip with the specified title and supporting text.
    ///
    /// @param title          the title displayed at the top of the tooltip
    /// @param supportingText the supporting text displayed below the title
    /// @throws NullPointerException if `title` or `supportingText` is `null`
    public M3RichTooltip(String title, String supportingText) {
        initializeRichTooltip();
        setTitle(title);
        setSupportingText(supportingText);
    }

    /// The non-null title displayed above the supporting text.
    ///
    /// An empty or blank value omits the title from layout. The property must not be set or bound to `null`.
    ///
    /// @defaultValue `""`
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    /// Returns the rich tooltip title.
    ///
    /// @return the title displayed at the top of the tooltip
    public final String getTitle() {
        return title.get();
    }

    /// Sets the rich tooltip title.
    ///
    /// @param title the title displayed at the top of the tooltip
    /// @throws NullPointerException if `title` is `null`
    public final void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    /// Returns the observable, bindable title property.
    ///
    /// The property has an initial value of `""`; empty or blank values omit the title from layout. It must not be
    /// set or bound to `null`.
    ///
    /// @return the title property
    public final StringProperty titleProperty() {
        return title;
    }

    /// The non-null supporting text displayed below the title.
    ///
    /// An empty or blank value omits the supporting text from layout. The property must not be set or bound to
    /// `null`.
    ///
    /// @defaultValue `""`
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "");

    /// Returns the rich tooltip supporting text.
    ///
    /// @return the supporting text displayed below the title
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the rich tooltip supporting text.
    ///
    /// @param supportingText the supporting text displayed below the title
    /// @throws NullPointerException if `supportingText` is `null`
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(Objects.requireNonNull(supportingText, "supportingText"));
    }

    /// Returns the observable, bindable supporting-text property.
    ///
    /// The property has an initial value of `""`; empty or blank values omit the supporting text from layout. It
    /// must not be set or bound to `null`.
    ///
    /// @return the supporting-text property
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// Whether installation uses explicit persistent activation rather than hover and focus activation.
    ///
    /// This property affects installed activation semantics; it does not prevent an application from calling
    /// [#show(Node,double,double)] or [#hide()] directly.
    ///
    /// @defaultValue `false`
    private final BooleanProperty persistent = new SimpleBooleanProperty(this, "persistent");

    /// Returns whether this rich tooltip uses persistent interaction behavior.
    ///
    /// Persistent rich tooltips open from an explicit click or keyboard activation rather than hover or focus.
    /// They remain visible when the pointer leaves the owner and close when another UI interaction auto-hides the
    /// popup, the owner is activated again, or Escape is pressed.
    ///
    /// @return `true` if this tooltip uses persistent behavior
    public final boolean isPersistent() {
        return persistent.get();
    }

    /// Sets whether this rich tooltip uses persistent interaction behavior.
    ///
    /// @param persistent whether explicit persistent behavior is enabled
    public final void setPersistent(boolean persistent) {
        this.persistent.set(persistent);
    }

    /// Returns the observable, bindable persistent-interaction property.
    ///
    /// The property is `false` by default. Changing it affects how an installed tooltip is activated; it does not
    /// prevent direct calls to [#show(Node,double,double)] or [#hide()].
    ///
    /// @return the persistent-interaction property
    public final BooleanProperty persistentProperty() {
        return persistent;
    }

    /// Returns the mutable action node list.
    ///
    /// The returned list is live, mutable, and ordered. Changes are reflected immediately while the tooltip is
    /// showing. `null` elements and duplicate node instances are not permitted. Each action node must be available
    /// for this tooltip to own; JavaFX rejects incompatible parent ownership. Material rich tooltips support at most
    /// two brief text-button actions, but this list does not enforce that content recommendation.
    ///
    /// @return the mutable action node list displayed in the tooltip action row
    public final ObservableList<Node> getActions() {
        return actions.getChildren();
    }

    /// Initializes rich tooltip content nodes, style classes, and property bindings.
    private void initializeRichTooltip() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setDefaultShowDuration(M3MotionBehavior.standard().richTooltipShowDuration());

        container.getStyleClass().add(CONTAINER_STYLE_CLASS);
        titleLabel.getStyleClass().add(TITLE_STYLE_CLASS);
        supportingTextLabel.getStyleClass().add(SUPPORTING_TEXT_STYLE_CLASS);
        actions.getStyleClass().add(ACTIONS_STYLE_CLASS);

        titleLabel.textProperty().bind(title);
        supportingTextLabel.textProperty().bind(supportingText);
        titleLabel.setWrapText(true);
        supportingTextLabel.setWrapText(true);

        title.addListener(observable -> updateTextState());
        supportingText.addListener(observable -> updateTextState());
        actions.getChildren().addListener((ListChangeListener<Node>) change -> updateActionsVisibility());

        container.getChildren().addAll(titleLabel, supportingTextLabel, actions);
        setGraphic(container);
        updateTextState();
        updateActionsVisibility();
    }

    /// Updates visible labels and the inherited tooltip text used for accessible help.
    private void updateTextState() {
        boolean titleVisible = !getTitle().isBlank();
        boolean supportingTextVisible = !getSupportingText().isBlank();
        titleLabel.setVisible(titleVisible);
        titleLabel.setManaged(titleVisible);
        supportingTextLabel.setVisible(supportingTextVisible);
        supportingTextLabel.setManaged(supportingTextVisible);
        setText(accessibleText());
    }

    /// Updates the action row visibility.
    private void updateActionsVisibility() {
        boolean visible = !actions.getChildren().isEmpty();
        actions.setVisible(visible);
        actions.setManaged(visible);
        container.requestLayout();
    }

    /// Returns the text exposed by the inherited tooltip accessible help binding.
    private String accessibleText() {
        String currentTitle = getTitle();
        String currentSupportingText = getSupportingText();
        if (currentTitle.isBlank()) {
            return currentSupportingText;
        }
        if (currentSupportingText.isBlank()) {
            return currentTitle;
        }
        return currentTitle + " " + currentSupportingText;
    }

    /// Returns the default visible duration for rich tooltip behavior profiles.
    ///
    /// @param behavior the motion behavior profile used by this tooltip
    /// @return the visible duration for rich tooltips
    @Override
    protected Duration defaultShowDuration(M3MotionBehavior behavior) {
        return behavior.richTooltipShowDuration();
    }

    /// Returns whether popup hover participates in rich tooltip lifetime management.
    ///
    /// @return `true` because rich tooltips may contain interactive action nodes
    @Override
    protected boolean isInteractive() {
        return true;
    }

    /// Returns whether installed activation uses explicit persistent behavior.
    ///
    /// @return the configured persistent state
    @Override
    protected boolean usesPersistentActivation() {
        return isPersistent();
    }

    /// Returns the first action focus target inside the rich tooltip popup.
    @Override
    protected @Nullable Node firstInteractiveFocusTarget() {
        return interactiveFocusTarget(false);
    }

    /// Returns the last action focus target inside the rich tooltip popup.
    @Override
    protected @Nullable Node lastInteractiveFocusTarget() {
        return interactiveFocusTarget(true);
    }

    /// Returns the next action focus target inside the rich tooltip popup.
    @Override
    protected @Nullable Node nextInteractiveFocusTarget(Node currentFocus, boolean backward) {
        Objects.requireNonNull(currentFocus, "currentFocus");
        int currentIndex = interactiveFocusIndex(currentFocus);
        int nextIndex = currentIndex < 0
                ? (backward ? actions.getChildren().size() - 1 : 0)
                : currentIndex + (backward ? -1 : 1);
        while (nextIndex >= 0 && nextIndex < actions.getChildren().size()) {
            Node action = actions.getChildren().get(nextIndex);
            if (M3Accessible.focusTarget(action) != null) {
                return action;
            }
            nextIndex += backward ? -1 : 1;
        }
        return null;
    }

    /// Returns whether an action row node can handle the requested interactive target.
    @Override
    protected boolean containsInteractiveActionTarget(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        return M3Accessible.canShowItem(null, actions.getChildren(), parameters)
                && interactiveActionOwnerFor(parameters) != null;
    }

    /// Shows an action row node or delegates to an action-owned popup target.
    @Override
    protected boolean showInteractiveActionTarget(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (!M3Accessible.canShowItem(null, actions.getChildren(), parameters)) {
            return false;
        }
        @Nullable Node action = interactiveActionOwnerFor(parameters);
        return action != null && M3Accessible.showAccessibleActionTarget(action, parameters);
    }

    /// Returns the action-row target that contains a requested node.
    @Override
    protected @Nullable Node interactiveFocusTargetFor(Node requestedNode) {
        Objects.requireNonNull(requestedNode, "requestedNode");
        for (Node action : actions.getChildren()) {
            if (action == requestedNode || M3Accessible.containsNode(action, requestedNode)) {
                if (M3Accessible.structuralFocusTarget(requestedNode) != null) {
                    return requestedNode;
                }
                return M3Accessible.structuralFocusTarget(action) == null ? null : action;
            }
        }
        return null;
    }

    /// Returns the action row owner that exposes one requested interactive target.
    private @Nullable Node interactiveActionOwnerFor(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Node action : actions.getChildren()) {
            if (M3Accessible.containsAccessibleActionTarget(action, parameters)) {
                return action;
            }
        }
        return null;
    }

    /// Returns the first or last reachable action node for keyboard traversal.
    private @Nullable Node interactiveFocusTarget(boolean backward) {
        int index = backward ? actions.getChildren().size() - 1 : 0;
        while (index >= 0 && index < actions.getChildren().size()) {
            Node action = actions.getChildren().get(index);
            if (M3Accessible.focusTarget(action) != null) {
                return action;
            }
            index += backward ? -1 : 1;
        }
        return null;
    }

    /// Returns the action index that contains the current popup focus owner.
    private int interactiveFocusIndex(Node currentFocus) {
        Objects.requireNonNull(currentFocus, "currentFocus");
        for (int index = 0; index < actions.getChildren().size(); index++) {
            Node action = actions.getChildren().get(index);
            if (action == currentFocus || M3Accessible.containsNode(action, currentFocus)) {
                return index;
            }
        }
        return -1;
    }

}
