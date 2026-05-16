// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3ListItem;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ListItem].
@NotNullByDefault
public class M3ListItemSkin extends SkinBase<M3ListItem> {
    /// The root container that receives background styling.
    private final HBox container = new HBox();

    /// The bounded state layer for list item feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// The text column.
    private final VBox textBox = new VBox();

    /// The overline text label.
    private final Label overlineLabel = new Label();

    /// The headline text label.
    private final Label headlineLabel = new Label();

    /// The supporting text label.
    private final Label supportingLabel = new Label();

    /// The leading node slot.
    private final StackPane leadingSlot = new StackPane();

    /// The trailing node slot.
    private final StackPane trailingSlot = new StackPane();

    /// Handles mouse activation.
    private final EventHandler<MouseEvent> mouseClickedHandler = this::handleMouseClicked;

    /// Handles keyboard activation.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Updates text nodes and metrics after text changes.
    private final InvalidationListener textInvalidation = observable -> updateTextAndMetrics();

    /// Updates optional node slots after slot content changes.
    private final InvalidationListener slotInvalidation = observable -> updateSlots();

    /// Applies metric token changes to the list item layout.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Creates a list item skin.
    public M3ListItemSkin(M3ListItem control) {
        super(control);
        container.getStyleClass().add("m3-list-item-container");
        textBox.getStyleClass().add("m3-list-item-text");
        overlineLabel.getStyleClass().add("m3-list-item-overline");
        headlineLabel.getStyleClass().add("m3-list-item-headline");
        supportingLabel.getStyleClass().add("m3-list-item-supporting");
        leadingSlot.getStyleClass().add("m3-list-item-leading");
        trailingSlot.getStyleClass().add("m3-list-item-trailing");

        textBox.getChildren().addAll(overlineLabel, headlineLabel, supportingLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        container.getChildren().addAll(leadingSlot, textBox, trailingSlot);
        getChildren().addAll(container, stateLayer);

        stateLayer.installStateTransitions(control);
        updateText();
        updateSlots();
        updateMetrics();
        installBehaviorHandlers(control);
        control.overlineTextProperty().addListener(textInvalidation);
        control.headlineTextProperty().addListener(textInvalidation);
        control.supportingTextProperty().addListener(textInvalidation);
        control.leadingProperty().addListener(slotInvalidation);
        control.trailingProperty().addListener(slotInvalidation);
        control.lineCountProperty().addListener(metricsInvalidation);
        control.oneLineHeightProperty().addListener(metricsInvalidation);
        control.twoLineHeightProperty().addListener(metricsInvalidation);
        control.threeLineHeightProperty().addListener(metricsInvalidation);
        control.containerShapeProperty().addListener(metricsInvalidation);
        control.horizontalPaddingProperty().addListener(metricsInvalidation);
        control.verticalPaddingProperty().addListener(metricsInvalidation);
        control.contentSpacingProperty().addListener(metricsInvalidation);
    }

    /// Removes behavior handlers before the skin is disposed.
    @Override
    public void dispose() {
        M3ListItem item = getSkinnable();
        stateLayer.uninstallStateTransitions();
        stateLayer.reset();
        item.overlineTextProperty().removeListener(textInvalidation);
        item.headlineTextProperty().removeListener(textInvalidation);
        item.supportingTextProperty().removeListener(textInvalidation);
        item.leadingProperty().removeListener(slotInvalidation);
        item.trailingProperty().removeListener(slotInvalidation);
        item.lineCountProperty().removeListener(metricsInvalidation);
        item.oneLineHeightProperty().removeListener(metricsInvalidation);
        item.twoLineHeightProperty().removeListener(metricsInvalidation);
        item.threeLineHeightProperty().removeListener(metricsInvalidation);
        item.containerShapeProperty().removeListener(metricsInvalidation);
        item.horizontalPaddingProperty().removeListener(metricsInvalidation);
        item.verticalPaddingProperty().removeListener(metricsInvalidation);
        item.contentSpacingProperty().removeListener(metricsInvalidation);
        item.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        item.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        super.dispose();
    }

    /// Lays out the container and bounded state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
        stateLayer.layoutLayer(x, y, width, height, getSkinnable().getContainerShape());
    }

    /// Updates text and layout after text content changes.
    private void updateTextAndMetrics() {
        updateText();
        updateMetrics();
    }

    /// Updates label text and visibility.
    private void updateText() {
        M3ListItem item = getSkinnable();
        updateLabel(overlineLabel, item.getOverlineText());
        updateLabel(headlineLabel, item.getHeadlineText());
        updateLabel(supportingLabel, item.getSupportingText());
    }

    /// Updates one label from a string value.
    private static void updateLabel(Label label, String text) {
        boolean visible = !text.isBlank();
        label.setText(text);
        label.setVisible(visible);
        label.setManaged(visible);
    }

    /// Updates leading and trailing slot content.
    private void updateSlots() {
        M3ListItem item = getSkinnable();
        updateSlot(leadingSlot, item.getLeading());
        updateSlot(trailingSlot, item.getTrailing());
    }

    /// Updates a slot with an optional node.
    private static void updateSlot(StackPane slot, @Nullable Node node) {
        if (node == null) {
            slot.getChildren().clear();
            slot.setVisible(false);
            slot.setManaged(false);
            return;
        }
        slot.getChildren().setAll(node);
        slot.setVisible(true);
        slot.setManaged(true);
    }

    /// Applies token-driven layout metrics.
    private void updateMetrics() {
        M3ListItem item = getSkinnable();
        double height = preferredHeight(item);
        double horizontalPadding = item.getHorizontalPadding();
        double verticalPadding = item.getVerticalPadding();
        double spacing = item.getContentSpacing();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setSpacing(spacing);
        container.setMinHeight(height);
        container.setPrefHeight(height);
        container.setMaxHeight(height);
        container.setPadding(new Insets(verticalPadding, horizontalPadding, verticalPadding, horizontalPadding));
        container.setStyle("-fx-background-radius: " + formatPixels(item.getContainerShape()) + ";");
    }

    /// Returns the preferred height for the current text structure.
    private static double preferredHeight(M3ListItem item) {
        return switch (item.getLineCount()) {
            case ONE_LINE -> item.getOneLineHeight();
            case TWO_LINE -> item.getTwoLineHeight();
            case THREE_LINE -> item.getThreeLineHeight();
        };
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }

    /// Installs behavior handlers for pointer and keyboard activation.
    private void installBehaviorHandlers(M3ListItem item) {
        item.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        item.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
    }

    /// Fires the list item on primary mouse clicks.
    private void handleMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && !getSkinnable().isDisabled()) {
            stateLayer.playRipple(event.getX(), event.getY());
            getSkinnable().fire();
            event.consume();
        }
    }

    /// Fires the list item on enter or space key presses.
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if ((code == KeyCode.ENTER || code == KeyCode.SPACE) && !getSkinnable().isDisabled()) {
            stateLayer.playCenteredRipple();
            getSkinnable().fire();
            event.consume();
        }
    }

}
