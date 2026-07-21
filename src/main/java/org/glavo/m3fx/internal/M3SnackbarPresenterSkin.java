// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.css.StyleOrigin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3Snackbar;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Renders the current message of an [M3SnackbarPresenter] with one reusable node tree.
@NotNullByDefault
final class M3SnackbarPresenterSkin extends SkinBase<M3SnackbarPresenter> {
    /// The reusable snackbar surface and horizontal layout container.
    private final HBox container = new HBox();

    /// The reusable supporting-text label.
    private final Label textLabel = new Label();

    /// The reusable optional text-action button.
    private final M3Button actionButton = new M3Button();

    /// The reusable optional close affordance.
    private final M3IconButton closeButton = new M3IconButton(new M3InternalIcon(
            M3InternalIcon.Glyph.CLOSE,
            M3InternalIcon.ColorRole.INVERSE_ON_SURFACE
    ));

    /// The observable message currently bound to the reusable node tree.
    private @Nullable M3Snackbar renderedSnackbar;

    /// Replaces rendered content when the current message changes.
    private final ChangeListener<@Nullable M3Snackbar> snackbarListener =
            (observable, oldValue, newValue) -> updateSnackbar(newValue);

    /// Applies changed component geometry tokens.
    private final InvalidationListener tokenInvalidation = observable -> updateTokenStyles();

    /// Creates the reusable presenter skin.
    ///
    /// @param control the snackbar presenter controlled by this skin
    M3SnackbarPresenterSkin(M3SnackbarPresenter control) {
        super(control);

        container.getStyleClass().add("m3-snackbar-container");
        textLabel.getStyleClass().add("m3-snackbar-text");
        actionButton.getStyleClass().add("m3-snackbar-action");
        closeButton.getStyleClass().add("m3-snackbar-close");
        actionButton.setVariant(M3ButtonVariant.TEXT);
        closeButton.setAccessibleText("Dismiss");

        textLabel.setWrapText(true);
        textLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textLabel, Priority.ALWAYS);
        actionButton.setOnAction(event -> {
            event.consume();
            getSkinnable().fireCurrentAction();
        });
        closeButton.setOnAction(event -> {
            event.consume();
            getSkinnable().dismiss();
        });

        control.snackbarProperty().addListener(snackbarListener);
        control.containerShapeProperty().addListener(tokenInvalidation);
        control.contentPaddingProperty().addListener(tokenInvalidation);
        control.containerMinWidthProperty().addListener(tokenInvalidation);
        control.containerMaxWidthProperty().addListener(tokenInvalidation);
        control.singleLineContainerHeightProperty().addListener(tokenInvalidation);
        control.twoLineContainerHeightProperty().addListener(tokenInvalidation);
        control.actionContainerHeightProperty().addListener(tokenInvalidation);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());

        container.getChildren().addAll(textLabel, actionButton, closeButton);
        getChildren().setAll(container);
        updateSnackbar(control.getSnackbar());
        updateTokenStyles();
    }

    /// Returns the stable node animated for snackbar entrance and exit.
    ///
    /// @return the reusable snackbar surface
    Node getPresentationNode() {
        return container;
    }

    /// Returns the first rendered interactive affordance.
    ///
    /// @return the action or close button, or `null` when the current message is passive
    @Nullable Node getAccessibleFocusNode() {
        if (actionButton.isManaged()) {
            return actionButton;
        }
        return closeButton.isManaged() ? closeButton : null;
    }

    /// Returns the rendered interactive item at one accessibility index.
    ///
    /// @param index the zero-based interactive-item index
    /// @return the rendered item, or `null` when the index is outside the rendered action set
    @Nullable Node getInteractiveItem(int index) {
        if (index < 0) {
            return null;
        }
        if (actionButton.isManaged()) {
            if (index == 0) {
                return actionButton;
            }
            index--;
        }
        return index == 0 && closeButton.isManaged() ? closeButton : null;
    }

    /// Returns the number of rendered interactive affordances.
    ///
    /// @return zero, one, or two
    int getInteractiveItemCount() {
        return (actionButton.isManaged() ? 1 : 0) + (closeButton.isManaged() ? 1 : 0);
    }

    /// Removes listeners, handlers, bindings, and rendered message content.
    @Override
    public void dispose() {
        M3SnackbarPresenter control = getSkinnable();
        control.snackbarProperty().removeListener(snackbarListener);
        control.containerShapeProperty().removeListener(tokenInvalidation);
        control.contentPaddingProperty().removeListener(tokenInvalidation);
        control.containerMinWidthProperty().removeListener(tokenInvalidation);
        control.containerMaxWidthProperty().removeListener(tokenInvalidation);
        control.singleLineContainerHeightProperty().removeListener(tokenInvalidation);
        control.twoLineContainerHeightProperty().removeListener(tokenInvalidation);
        control.actionContainerHeightProperty().removeListener(tokenInvalidation);
        container.nodeOrientationProperty().unbind();
        actionButton.setOnAction(null);
        closeButton.setOnAction(null);
        detachRenderedSnackbar();
        textLabel.setText("");
        actionButton.setText("");
        getChildren().clear();
        super.dispose();
    }

    /// Computes minimum width from the presenter's minimum-container token.
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

    /// Computes preferred width from current content and container width tokens.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double preferredWidth = container.prefWidth(-1.0);
        double boundedWidth = Math.min(
                effectiveContainerMaxWidth(),
                Math.max(effectiveContainerMinWidth(), preferredWidth)
        );
        return leftInset + boundedWidth + rightInset;
    }

    /// Computes minimum height from the current rendered snackbar.
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

    /// Computes preferred height from single-line and two-line height tokens.
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

    /// Centers the snackbar horizontally and aligns it to the logical overlay bottom.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        if (!container.isManaged()) {
            return;
        }

        double preferredWidth = container.prefWidth(-1.0);
        double snackbarWidth = Math.min(
                width,
                Math.min(effectiveContainerMaxWidth(), Math.max(effectiveContainerMinWidth(), preferredWidth))
        );
        double snackbarHeight = Math.min(height, snackbarContainerHeight(snackbarWidth));
        double snackbarX = x + (width - snackbarWidth) / 2.0;
        double snackbarY = y + height - snackbarHeight;
        container.resizeRelocate(
                snapPositionX(snackbarX),
                snapPositionY(snackbarY),
                snapSizeX(snackbarWidth),
                snapSizeY(snackbarHeight)
        );
    }

    /// Replaces the reusable nodes' bindings with one observable message.
    private void updateSnackbar(@Nullable M3Snackbar snackbar) {
        detachRenderedSnackbar();
        renderedSnackbar = snackbar;
        boolean present = snackbar != null;
        getSkinnable().setMouseTransparent(!present);
        container.setManaged(present);
        container.setVisible(present);
        if (!present) {
            textLabel.setText("");
            actionButton.setText("");
            actionButton.setManaged(false);
            actionButton.setVisible(false);
            closeButton.setManaged(false);
            closeButton.setVisible(false);
            getSkinnable().requestLayout();
            return;
        }

        textLabel.textProperty().bind(snackbar.textProperty());
        actionButton.textProperty().bind(snackbar.actionTextProperty());
        updateAffordanceVisibility();
    }

    /// Disconnects the reusable nodes from the previously rendered message.
    private void detachRenderedSnackbar() {
        renderedSnackbar = null;
        textLabel.textProperty().unbind();
        actionButton.textProperty().unbind();
    }

    /// Applies action-label and close-button visibility from the current observable message.
    void updateAffordanceVisibility() {
        @Nullable M3Snackbar snackbar = renderedSnackbar;
        boolean actionVisible = snackbar != null && snackbar.hasAction();
        boolean closeVisible = snackbar != null && snackbar.isCloseButtonVisible();
        actionButton.setManaged(actionVisible);
        actionButton.setVisible(actionVisible);
        closeButton.setManaged(closeVisible);
        closeButton.setVisible(closeVisible);
        updateTokenStyles();
        getSkinnable().requestLayout();
    }

    /// Applies logical alignment, padding, height, and shape tokens.
    private void updateTokenStyles() {
        M3SnackbarPresenter presenter = getSkinnable();
        container.setAlignment(Pos.CENTER_LEFT);
        textLabel.setAlignment(Pos.CENTER_LEFT);

        double padding = presenter.getContentPadding();
        double verticalPadding = padding / 2.0;
        double trailingPadding = actionButton.isManaged() || closeButton.isManaged() ? padding / 2.0 : padding;
        container.setPadding(new Insets(verticalPadding, trailingPadding, verticalPadding, padding));
        container.setMinHeight(presenter.getSingleLineContainerHeight());
        actionButton.containerHeightProperty().applyStyle(
                StyleOrigin.USER_AGENT,
                presenter.getActionContainerHeight()
        );
        container.setStyle("-fx-background-radius: " + formatPixels(presenter.getContainerShape()) + ";");
    }

    /// Returns the effective minimum container width.
    private double effectiveContainerMinWidth() {
        M3SnackbarPresenter presenter = getSkinnable();
        return Math.min(presenter.getContainerMinWidth(), effectiveContainerMaxWidth());
    }

    /// Returns the effective maximum container width.
    private double effectiveContainerMaxWidth() {
        M3SnackbarPresenter presenter = getSkinnable();
        return Math.max(presenter.getContainerMinWidth(), presenter.getContainerMaxWidth());
    }

    /// Returns the effective container height for current wrapping state.
    private double snackbarContainerHeight(double width) {
        M3SnackbarPresenter presenter = getSkinnable();
        double tokenHeight = textWrapsAt(width)
                ? presenter.getTwoLineContainerHeight()
                : presenter.getSingleLineContainerHeight();
        return Math.max(tokenHeight, snapSizeY(container.prefHeight(width)));
    }

    /// Returns whether supporting text wraps at the supplied container width.
    private boolean textWrapsAt(double containerWidth) {
        if (containerWidth < 0.0 || textLabel.getText().isBlank()) {
            return false;
        }

        Insets padding = container.getPadding();
        double labelWidth = containerWidth - padding.getLeft() - padding.getRight();
        if (actionButton.isManaged()) {
            labelWidth -= actionButton.prefWidth(-1.0) + container.getSpacing();
        }
        if (closeButton.isManaged()) {
            labelWidth -= closeButton.prefWidth(-1.0) + container.getSpacing();
        }
        labelWidth = Math.max(0.0, labelWidth);
        return textLabel.prefHeight(labelWidth) > textLabel.prefHeight(-1.0) + 0.5;
    }

    /// Formats one CSS pixel value without unnecessary decimal digits.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return (long) value + "px";
        }
        return value + "px";
    }
}
