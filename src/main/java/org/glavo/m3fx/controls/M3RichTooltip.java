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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// A Material Design 3 rich tooltip with title, supporting text, and optional actions.
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
    private static final double ACTION_CONTENT_MIN_HEIGHT = 136.0;

    /// The rich tooltip title property.
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    /// The rich tooltip supporting text property.
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "");

    /// The root graphic node rendered by the tooltip skin.
    private final VBox container = new VBox();

    /// The title label.
    private final Label titleLabel = new Label();

    /// The supporting text label.
    private final Label supportingTextLabel = new Label();

    /// The action node row.
    private final HBox actions = new HBox();

    /// Creates an empty rich tooltip.
    public M3RichTooltip() {
        this("", "");
    }

    /// Creates a rich tooltip with title and supporting text.
    public M3RichTooltip(String title, String supportingText) {
        initializeRichTooltip();
        setTitle(title);
        setSupportingText(supportingText);
    }

    /// Creates a rich tooltip with title, supporting text, and action nodes.
    public M3RichTooltip(String title, String supportingText, Node... actions) {
        this(title, supportingText);
        addActions(actions);
    }

    /// Installs a rich tooltip with title and supporting text on a node.
    public static M3RichTooltip install(Node node, String title, String supportingText) {
        M3RichTooltip tooltip = new M3RichTooltip(title, supportingText);
        M3Tooltip.install(node, tooltip);
        return tooltip;
    }

    /// Installs a rich tooltip with title, supporting text, and action nodes on a node.
    public static M3RichTooltip install(Node node, String title, String supportingText, Node... actions) {
        M3RichTooltip tooltip = new M3RichTooltip(title, supportingText, actions);
        M3Tooltip.install(node, tooltip);
        return tooltip;
    }

    /// Uninstalls a Material Design 3 rich tooltip from a node.
    public static void uninstall(Node node, M3RichTooltip tooltip) {
        M3Tooltip.uninstall(node, tooltip);
    }

    /// Returns the rich tooltip title.
    public final String getTitle() {
        return title.get();
    }

    /// Sets the rich tooltip title.
    public final void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    /// Returns the rich tooltip title property.
    public final StringProperty titleProperty() {
        return title;
    }

    /// Returns the rich tooltip supporting text.
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the rich tooltip supporting text.
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(Objects.requireNonNull(supportingText, "supportingText"));
    }

    /// Returns the rich tooltip supporting text property.
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// Returns the mutable action node list.
    public final ObservableList<Node> getActions() {
        return actions.getChildren();
    }

    /// Adds one action node.
    public final void addAction(Node action) {
        getActions().add(Objects.requireNonNull(action, "action"));
    }

    /// Adds action nodes after validating the action array.
    public final void addActions(Node... actions) {
        validateActions(actions);
        getActions().addAll(actions);
    }

    /// Replaces all action nodes.
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
        setShowDuration(Duration.seconds(10.0));

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
        container.setMinHeight(visible ? ACTION_CONTENT_MIN_HEIGHT : Region.USE_COMPUTED_SIZE);
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
}
