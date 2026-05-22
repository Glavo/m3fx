// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3TopAppBar].
@NotNullByDefault
public final class M3TopAppBarSkin extends SkinBase<M3TopAppBar> {
    /// The spacing between top app bar content slots.
    private static final double CONTENT_SPACING = 16.0;

    /// The spacing between trailing action nodes.
    private static final double ACTION_SPACING = 8.0;

    /// The internal horizontal container.
    private final HBox container = new HBox(CONTENT_SPACING);

    /// The slot that hosts the optional navigation node.
    private final StackPane navigationSlot = new StackPane();

    /// The label that renders the app bar title.
    private final Label titleLabel = new Label();

    /// The flexible spacer between the title and actions.
    private final Region spacer = new Region();

    /// The trailing action node container.
    private final HBox actions = new HBox(ACTION_SPACING);

    /// Updates the visual action list when public actions change.
    private final ListChangeListener<Node> actionsListener = change -> updateActions();

    /// Updates logical alignment when node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateVariantLayout();

    /// Creates a top app bar skin.
    public M3TopAppBarSkin(M3TopAppBar control) {
        super(control);
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        navigationSlot.getStyleClass().add(M3TopAppBar.NAVIGATION_STYLE_CLASS);
        titleLabel.getStyleClass().add(M3TopAppBar.TITLE_STYLE_CLASS);
        actions.getStyleClass().add(M3TopAppBar.ACTIONS_STYLE_CLASS);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleLabel.textProperty().bind(control.titleProperty());

        control.navigationProperty().addListener((observable, oldValue, newValue) -> updateNavigation(newValue));
        control.getActions().addListener(actionsListener);
        control.variantProperty().addListener((observable, oldValue, newValue) -> updateVariantLayout());
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);

        updateNavigation(control.getNavigation());
        updateActions();
        updateVariantLayout();
        container.getChildren().setAll(navigationSlot, titleLabel, spacer, actions);
        getChildren().add(container);
    }

    /// Removes listeners, bindings, and child references before disposal.
    @Override
    public void dispose() {
        M3TopAppBar control = getSkinnable();
        titleLabel.textProperty().unbind();
        control.getActions().removeListener(actionsListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        actions.getChildren().clear();
        navigationSlot.getChildren().clear();
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

    /// Updates the optional navigation slot.
    private void updateNavigation(@Nullable Node node) {
        navigationSlot.getChildren().clear();
        navigationSlot.setVisible(node != null);
        navigationSlot.setManaged(node != null);
        if (node != null) {
            navigationSlot.getChildren().add(node);
        }
    }

    /// Updates the trailing action container.
    private void updateActions() {
        actions.getChildren().setAll(getSkinnable().getActions());
        getSkinnable().requestLayout();
    }

    /// Updates layout details that depend on the top app bar variant.
    private void updateVariantLayout() {
        M3TopAppBarVariant variant = getSkinnable().getVariant();
        boolean centerAligned = variant == M3TopAppBarVariant.CENTER_ALIGNED;
        boolean tall = variant == M3TopAppBarVariant.MEDIUM || variant == M3TopAppBarVariant.LARGE;
        boolean rightToLeft = getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;

        actions.setAlignment(rightToLeft ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        if (centerAligned) {
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            titleLabel.setAlignment(Pos.CENTER);
            spacer.setVisible(false);
            spacer.setManaged(false);
        } else {
            HBox.setHgrow(titleLabel, null);
            titleLabel.setAlignment(rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            spacer.setVisible(true);
            spacer.setManaged(true);
        }
        if (rightToLeft) {
            container.setAlignment(tall ? Pos.BOTTOM_RIGHT : Pos.CENTER_RIGHT);
        } else {
            container.setAlignment(tall ? Pos.BOTTOM_LEFT : Pos.CENTER_LEFT);
        }
        getSkinnable().requestLayout();
    }
}
