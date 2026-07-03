// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

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
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 rich tooltip with title, supporting text, and optional actions.
///
/// `M3RichTooltip` builds on [M3Tooltip] and supplies the structured content used by rich Material tooltips:
/// title text, supporting text, and an optional row of action nodes. It inherits popup timing, accessibility,
/// owner-window tracking, theme propagation, and motion from the base tooltip.
///
/// See [Material Design tooltips](https://m3.material.io/components/tooltips/overview).
@NotNullByDefault
public class M3RichTooltip extends M3Tooltip {
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

    /// The minimum rich tooltip height when actions are visible.
    private static final double ACTION_CONTENT_MIN_HEIGHT = 160.0;

    // Backing property for the public rich tooltip title API.
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    // Backing property for the public supporting text API.
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "");

    /// The title label.
    private final Label titleLabel = new Label();

    /// The supporting text label.
    private final Label supportingTextLabel = new Label();

    /// The action node row.
    private final HBox actions = new HBox();

    /// The root graphic node rendered by the tooltip skin.
    private final VBox container = new RichTooltipContainer();

    /// Creates an empty rich tooltip.
    public M3RichTooltip() {
        this("", "");
    }

    /// Creates a rich tooltip with title and supporting text.
    ///
    /// @param title the title displayed at the top of the tooltip
    /// @param supportingText the supporting text displayed below the title
    public M3RichTooltip(String title, String supportingText) {
        initializeRichTooltip();
        setTitle(title);
        setSupportingText(supportingText);
    }

    /// Creates a rich tooltip with title, supporting text, and action nodes.
    ///
    /// @param title the title displayed at the top of the tooltip
    /// @param supportingText the supporting text displayed below the title
    /// @param actions the action nodes displayed in the tooltip action row
    public M3RichTooltip(String title, String supportingText, Node... actions) {
        this(title, supportingText);
        addActions(actions);
    }

    /// Installs a rich tooltip with title and supporting text on a node.
    ///
    /// @param node the node that owns the tooltip
    /// @param title the title displayed at the top of the tooltip
    /// @param supportingText the supporting text displayed below the title
    /// @return the installed rich tooltip
    public static M3RichTooltip install(Node node, String title, String supportingText) {
        M3RichTooltip tooltip = new M3RichTooltip(title, supportingText);
        M3Tooltip.install(node, tooltip);
        return tooltip;
    }

    /// Installs a rich tooltip with title, supporting text, and action nodes on a node.
    ///
    /// @param node the node that owns the tooltip
    /// @param title the title displayed at the top of the tooltip
    /// @param supportingText the supporting text displayed below the title
    /// @param actions the action nodes displayed in the tooltip action row
    /// @return the installed rich tooltip
    public static M3RichTooltip install(Node node, String title, String supportingText, Node... actions) {
        M3RichTooltip tooltip = new M3RichTooltip(title, supportingText, actions);
        M3Tooltip.install(node, tooltip);
        return tooltip;
    }

    /// Uninstalls a Material Design 3 rich tooltip from a node.
    ///
    /// @param node the node that owns the tooltip
    /// @param tooltip the rich tooltip to uninstall
    public static void uninstall(Node node, M3RichTooltip tooltip) {
        M3Tooltip.uninstall(node, tooltip);
    }

    /// Returns the rich tooltip title.
    ///
    /// @return the title displayed at the top of the tooltip
    public final String getTitle() {
        return title.get();
    }

    /// Sets the rich tooltip title.
    ///
    /// @param title the title displayed at the top of the tooltip
    public final void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    /// Returns the rich tooltip title property.
    ///
    /// @return the rich tooltip title property
    public final StringProperty titleProperty() {
        return title;
    }

    /// Returns the rich tooltip supporting text.
    ///
    /// @return the supporting text displayed below the title
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the rich tooltip supporting text.
    ///
    /// @param supportingText the supporting text displayed below the title
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(Objects.requireNonNull(supportingText, "supportingText"));
    }

    /// Returns the rich tooltip supporting text property.
    ///
    /// @return the rich tooltip supporting text property
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// Returns the mutable action node list.
    ///
    /// @return the mutable action node list displayed in the tooltip action row
    public final ObservableList<Node> getActions() {
        return actions.getChildren();
    }

    /// Adds one action node.
    ///
    /// @param action the action node to add
    public final void addAction(Node action) {
        getActions().add(Objects.requireNonNull(action, "action"));
    }

    /// Adds action nodes after validating the action array.
    ///
    /// @param actions the action nodes to add
    public final void addActions(Node... actions) {
        validateActions(actions);
        getActions().addAll(actions);
    }

    /// Replaces all action nodes.
    ///
    /// @param actions the replacement action nodes
    public final void setActions(Node... actions) {
        validateActions(actions);
        getActions().setAll(actions);
    }

    /// Removes all action nodes.
    public final void clearActions() {
        getActions().clear();
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

    /// Validates an action array.
    private static void validateActions(Node... actions) {
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
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

    /// Returns whether the tooltip root should receive plain tooltip container metrics.
    @Override
    protected boolean usesPlainContainerStyle() {
        return false;
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

    /// Rich tooltip content container that preserves the action minimum height without clipping longer content.
    @NotNullByDefault
    private final class RichTooltipContainer extends VBox {
        /// Computes the preferred height from content and the Material action minimum height.
        @Override
        protected double computePrefHeight(double width) {
            double computedHeight = super.computePrefHeight(width);
            return actions.isManaged() ? Math.max(computedHeight, ACTION_CONTENT_MIN_HEIGHT) : computedHeight;
        }

        /// Computes the minimum height from the preferred height so popup sizing does not compress actions.
        @Override
        protected double computeMinHeight(double width) {
            return computePrefHeight(width);
        }
    }
}
