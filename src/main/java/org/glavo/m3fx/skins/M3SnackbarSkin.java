// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.css.StyleOrigin;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Snackbar].
@NotNullByDefault
public class M3SnackbarSkin extends SkinBase<M3Snackbar> {

    /// The snackbar layout container.
    private final HBox container = new HBox();

    /// The snackbar message label.
    private final Label textLabel = new Label();

    /// The snackbar action button.
    private final M3Button actionButton = new M3Button();

    /// Updates action visibility after action text changes.
    private final InvalidationListener actionTextInvalidation =
            observable -> updateActionVisibility(getSkinnable().getActionText());

    /// Applies token changes to snackbar geometry.
    private final InvalidationListener tokenInvalidation = observable -> updateTokenStyles();

    /// Recomputes logical padding when the snackbar direction changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateTokenStyles();

    /// Creates a snackbar skin.
    ///
    /// @param control the snackbar controlled by this skin
    public M3SnackbarSkin(M3Snackbar control) {
        super(control);

        container.getStyleClass().add("m3-snackbar-container");
        textLabel.getStyleClass().add("m3-snackbar-text");
        actionButton.getStyleClass().add("m3-snackbar-action");
        actionButton.setVariant(M3ButtonVariant.TEXT);

        textLabel.textProperty().bind(control.textProperty());
        textLabel.setWrapText(true);
        actionButton.textProperty().bind(control.actionTextProperty());
        actionButton.setOnAction(this::fireAction);
        control.actionTextProperty().addListener(actionTextInvalidation);
        control.containerShapeProperty().addListener(tokenInvalidation);
        control.contentPaddingProperty().addListener(tokenInvalidation);
        control.containerMinWidthProperty().addListener(tokenInvalidation);
        control.containerMaxWidthProperty().addListener(tokenInvalidation);
        control.singleLineContainerHeightProperty().addListener(tokenInvalidation);
        control.twoLineContainerHeightProperty().addListener(tokenInvalidation);
        control.actionContainerHeightProperty().addListener(tokenInvalidation);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);

        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        container.getChildren().addAll(textLabel, actionButton);
        getChildren().add(container);
        updateActionVisibility(control.getActionText());
        updateTokenStyles();
    }

    /// Returns the action button when the snackbar currently exposes an action.
    ///
    /// @return the rendered action button, or `null` when no action is visible
    public final @Nullable M3Button getActionButton() {
        return actionButton.isManaged() ? actionButton : null;
    }

    /// Unbinds skin nodes and removes listeners before disposal.
    @Override
    public void dispose() {
        M3Snackbar snackbar = getSkinnable();
        textLabel.textProperty().unbind();
        actionButton.textProperty().unbind();
        actionButton.setOnAction(null);
        snackbar.actionTextProperty().removeListener(actionTextInvalidation);
        snackbar.containerShapeProperty().removeListener(tokenInvalidation);
        snackbar.contentPaddingProperty().removeListener(tokenInvalidation);
        snackbar.containerMinWidthProperty().removeListener(tokenInvalidation);
        snackbar.containerMaxWidthProperty().removeListener(tokenInvalidation);
        snackbar.singleLineContainerHeightProperty().removeListener(tokenInvalidation);
        snackbar.twoLineContainerHeightProperty().removeListener(tokenInvalidation);
        snackbar.actionContainerHeightProperty().removeListener(tokenInvalidation);
        snackbar.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        super.dispose();
    }

    /// Fires the snackbar action handler if one is present.
    private void fireAction(ActionEvent event) {
        event.consume();
        getSkinnable().fireAction();
    }

    /// Updates the action button visibility from its text.
    private void updateActionVisibility(@Nullable String actionText) {
        boolean visible = actionText != null && !actionText.isBlank();
        actionButton.setVisible(visible);
        actionButton.setManaged(visible);
        updateTokenStyles();
    }

    /// Applies styleable component tokens to the snackbar container.
    private void updateTokenStyles() {
        M3Snackbar snackbar = getSkinnable();
        double padding = snackbar.getContentPadding();
        double verticalPadding = padding / 2.0;
        double leadingPadding = padding;
        double trailingPadding = actionButton.isManaged() ? padding / 2.0 : padding;
        container.setPadding(snackbarPadding(verticalPadding, leadingPadding, trailingPadding));
        container.setMinHeight(snackbar.getSingleLineContainerHeight());
        actionButton.containerHeightProperty().applyStyle(StyleOrigin.USER_AGENT, snackbar.getActionContainerHeight());
        String shape = formatPixels(snackbar.getContainerShape());
        container.setStyle("-fx-background-radius: " + shape + ";");
    }

    /// Computes the minimum snackbar width from the container width token.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + effectiveContainerMinWidth() + rightInset;
    }

    /// Computes the preferred snackbar width from content and container width tokens.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + boundedContainerWidth(container.prefWidth(-1.0)) + rightInset;
    }

    /// Computes the minimum snackbar height for the current text wrapping state.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the preferred snackbar height for single-line and two-line content tokens.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double contentWidth = width < 0.0 ? -1.0 : Math.max(0.0, width - leftInset - rightInset);
        return topInset + snackbarContainerHeight(contentWidth) + bottomInset;
    }

    /// Lays out the snackbar container inside the control content area.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double containerHeight = Math.max(height, snackbarContainerHeight(width));
        container.resizeRelocate(
                snapPositionX(x),
                snapPositionY(y),
                snapSizeX(width),
                snapSizeY(containerHeight)
        );
    }

    /// Returns the preferred container width clamped by the width tokens.
    private double boundedContainerWidth(double preferredWidth) {
        return Math.min(effectiveContainerMaxWidth(), Math.max(effectiveContainerMinWidth(), preferredWidth));
    }

    /// Returns the effective minimum container width.
    private double effectiveContainerMinWidth() {
        M3Snackbar snackbar = getSkinnable();
        return Math.min(snackbar.getContainerMinWidth(), effectiveContainerMaxWidth());
    }

    /// Returns the effective maximum container width.
    private double effectiveContainerMaxWidth() {
        M3Snackbar snackbar = getSkinnable();
        return Math.max(snackbar.getContainerMinWidth(), snackbar.getContainerMaxWidth());
    }

    /// Returns the snackbar container height selected for the available width.
    private double snackbarContainerHeight(double width) {
        M3Snackbar snackbar = getSkinnable();
        double tokenHeight = textWrapsAt(width)
                ? snackbar.getTwoLineContainerHeight()
                : snackbar.getSingleLineContainerHeight();
        return Math.max(tokenHeight, snapSizeY(container.prefHeight(width)));
    }

    /// Returns whether the message label needs more than one line at the available container width.
    private boolean textWrapsAt(double containerWidth) {
        if (containerWidth < 0.0 || getSkinnable().getText().isBlank()) {
            return false;
        }

        Insets padding = container.getPadding();
        double labelWidth = containerWidth - padding.getLeft() - padding.getRight();
        if (actionButton.isManaged()) {
            labelWidth -= actionButton.prefWidth(-1.0) + container.getSpacing();
        }
        labelWidth = Math.max(0.0, labelWidth);
        double singleLineHeight = textLabel.prefHeight(-1.0);
        double wrappedHeight = textLabel.prefHeight(labelWidth);
        return wrappedHeight > singleLineHeight + 0.5;
    }

    /// Returns physical container padding for logical snackbar content edges.
    private Insets snackbarPadding(double verticalPadding, double leadingPadding, double trailingPadding) {
        return M3NodeLayout.logicalInsets(getSkinnable(), verticalPadding, leadingPadding, verticalPadding, trailingPadding);
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }
}
