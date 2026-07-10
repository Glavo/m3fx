// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3Banner].
@NotNullByDefault
public final class M3BannerSkin extends SkinBase<M3Banner> {
    /// The internal horizontal container.
    private final HBox container = new HBox();

    /// The slot that hosts the optional leading icon.
    private final StackPane iconSlot = new StackPane();

    /// The label that renders the banner message.
    private final Label textLabel = new Label();

    /// The trailing action node container.
    private final HBox actions = new HBox();

    /// Updates the visual action list when public actions change.
    private final ListChangeListener<Node> actionsListener = change -> updateActions();

    /// Applies spacing token changes to internal layout nodes.
    private final InvalidationListener tokenInvalidation = observable -> updateTokenStyles();

    /// Updates the icon slot when the public icon node changes.
    private final ChangeListener<@Nullable Node> iconListener =
            (observable, oldValue, newValue) -> updateIcon(newValue);

    /// Creates a banner skin.
    ///
    /// @param control the banner controlled by this skin
    public M3BannerSkin(M3Banner control) {
        super(control);
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        container.getStyleClass().add(M3Banner.CONTAINER_STYLE_CLASS);
        iconSlot.getStyleClass().add(M3Banner.ICON_STYLE_CLASS);
        textLabel.getStyleClass().add(M3Banner.TEXT_STYLE_CLASS);
        actions.getStyleClass().add(M3Banner.ACTIONS_STYLE_CLASS);
        HBox.setHgrow(textLabel, Priority.ALWAYS);
        textLabel.setMinWidth(0.0);
        textLabel.setMaxWidth(Double.MAX_VALUE);
        textLabel.setWrapText(true);
        textLabel.textProperty().bind(control.textProperty());
        actions.setMinWidth(Region.USE_PREF_SIZE);
        actions.setMaxWidth(Region.USE_PREF_SIZE);
        container.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        actions.alignmentProperty().bind(M3NodeLayout.createLogicalEndCenterAlignmentBinding(control));

        control.iconProperty().addListener(iconListener);
        control.getActions().addListener(actionsListener);
        control.contentSpacingProperty().addListener(tokenInvalidation);
        control.actionSpacingProperty().addListener(tokenInvalidation);

        updateIcon(control.getIcon());
        updateActions();
        updateTokenStyles();
        container.getChildren().setAll(iconSlot, textLabel, actions);
        getChildren().add(container);
    }

    /// Removes listeners, bindings, and child references before disposal.
    @Override
    public void dispose() {
        M3Banner control = getSkinnable();
        textLabel.textProperty().unbind();
        control.getActions().removeListener(actionsListener);
        control.iconProperty().removeListener(iconListener);
        control.contentSpacingProperty().removeListener(tokenInvalidation);
        control.actionSpacingProperty().removeListener(tokenInvalidation);
        container.nodeOrientationProperty().unbind();
        container.alignmentProperty().unbind();
        actions.alignmentProperty().unbind();
        actions.getChildren().clear();
        iconSlot.getChildren().clear();
        container.getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the internal container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the internal container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the internal container.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the internal container.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.prefHeight(width) + bottomInset;
    }

    /// Computes the maximum width from the internal container.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.maxWidth(height) + rightInset;
    }

    /// Computes the maximum height from the internal container.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.maxHeight(width) + bottomInset;
    }

    /// Lays out the internal container in the full control content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Updates internal spacing from banner tokens.
    private void updateTokenStyles() {
        M3Banner banner = getSkinnable();
        container.setSpacing(banner.getContentSpacing());
        actions.setSpacing(banner.getActionSpacing());
        banner.requestLayout();
    }

    /// Updates the optional icon slot.
    private void updateIcon(@Nullable Node node) {
        iconSlot.getChildren().clear();
        iconSlot.setVisible(node != null);
        iconSlot.setManaged(node != null);
        if (node != null) {
            iconSlot.getChildren().add(node);
        }
        getSkinnable().requestLayout();
    }

    /// Updates the trailing action container.
    private void updateActions() {
        actions.getChildren().setAll(getSkinnable().getActions());
        boolean visible = !actions.getChildren().isEmpty();
        actions.setVisible(visible);
        actions.setManaged(visible);
        getSkinnable().requestLayout();
    }
}
