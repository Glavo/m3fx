// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3BottomAppBar].
@NotNullByDefault
public final class M3BottomAppBarSkin extends SkinBase<M3BottomAppBar> {
    /// The internal horizontal container.
    private final HBox container = new HBox();

    /// The regular action node container.
    private final HBox actions = new HBox();

    /// The flexible spacer before the floating action slot.
    private final Region leadingSpacer = new Region();

    /// The flexible spacer after the floating action slot.
    private final Region trailingSpacer = new Region();

    /// The slot that hosts the optional floating action node.
    private final StackPane floatingActionSlot = new StackPane();

    /// Updates the visual action list when public actions change.
    private final ListChangeListener<Node> actionsListener = change -> updateActions();

    /// Updates logical floating-action placement when node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateLayoutOrder();

    /// Creates a bottom app bar skin.
    ///
    /// @param control the bottom app bar controlled by this skin
    public M3BottomAppBarSkin(M3BottomAppBar control) {
        super(control);
        container.setManaged(false);
        container.spacingProperty().bind(control.contentSpacingProperty());
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        actions.spacingProperty().bind(control.actionSpacingProperty());
        actions.getStyleClass().add(M3BottomAppBar.ACTIONS_STYLE_CLASS);
        floatingActionSlot.getStyleClass().add(M3BottomAppBar.FLOATING_ACTION_STYLE_CLASS);
        HBox.setHgrow(leadingSpacer, Priority.ALWAYS);
        HBox.setHgrow(trailingSpacer, Priority.ALWAYS);

        control.floatingActionProperty().addListener((observable, oldValue, newValue) -> updateFloatingAction(newValue));
        control.floatingActionAlignmentProperty().addListener((observable, oldValue, newValue) -> updateLayoutOrder());
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
        control.getActions().addListener(actionsListener);

        updateActions();
        updateFloatingAction(control.getFloatingAction());
        updateLayoutOrder();
        getChildren().add(container);
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3BottomAppBar control = getSkinnable();
        control.getActions().removeListener(actionsListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.spacingProperty().unbind();
        container.nodeOrientationProperty().unbind();
        actions.spacingProperty().unbind();
        actions.getChildren().clear();
        floatingActionSlot.getChildren().clear();
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

    /// Updates the regular action container.
    private void updateActions() {
        actions.getChildren().setAll(getSkinnable().getActions());
        getSkinnable().requestLayout();
    }

    /// Updates the floating action slot.
    private void updateFloatingAction(@Nullable Node node) {
        floatingActionSlot.getChildren().clear();
        floatingActionSlot.setVisible(node != null);
        floatingActionSlot.setManaged(node != null);
        if (node != null) {
            floatingActionSlot.getChildren().add(node);
        }
        getSkinnable().requestLayout();
    }

    /// Updates child order and slot alignment from the floating action alignment.
    private void updateLayoutOrder() {
        container.getChildren().clear();
        boolean rightToLeft = getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        container.setAlignment(rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        actions.setAlignment(rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        switch (getSkinnable().getFloatingActionAlignment()) {
            case START -> {
                floatingActionSlot.setAlignment(rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                container.getChildren().addAll(floatingActionSlot, actions, trailingSpacer);
            }
            case CENTER -> {
                floatingActionSlot.setAlignment(Pos.CENTER);
                container.getChildren().addAll(actions, leadingSpacer, floatingActionSlot, trailingSpacer);
            }
            case END -> {
                floatingActionSlot.setAlignment(rightToLeft ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
                container.getChildren().addAll(actions, leadingSpacer, floatingActionSlot);
            }
        }
        getSkinnable().requestLayout();
    }
}
