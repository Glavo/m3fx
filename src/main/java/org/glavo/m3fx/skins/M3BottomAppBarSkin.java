// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3BottomAppBar].
@NotNullByDefault
public final class M3BottomAppBarSkin extends SkinBase<M3BottomAppBar> {
    /// The minimum Material hit slot used by bottom app bar regular action icons.
    private static final double MINIMUM_ACTION_SLOT_SIZE = 48.0;

    /// The regular action node container.
    private final HBox actions = new HBox();

    /// The slot that hosts the optional floating action node.
    private final StackPane floatingActionSlot = new StackPane();

    /// Updates the visual action list when public actions change.
    private final ListChangeListener<Node> actionsListener = change -> updateActions();

    /// Updates logical floating-action placement when node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateLayoutState();

    /// Creates a bottom app bar skin.
    ///
    /// @param control the bottom app bar controlled by this skin
    public M3BottomAppBarSkin(M3BottomAppBar control) {
        super(control);
        actions.setManaged(false);
        floatingActionSlot.setManaged(false);
        actions.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        actions.spacingProperty().bind(control.actionSpacingProperty());
        actions.getStyleClass().add(M3BottomAppBar.ACTIONS_STYLE_CLASS);
        floatingActionSlot.getStyleClass().add(M3BottomAppBar.FLOATING_ACTION_STYLE_CLASS);

        control.floatingActionProperty().addListener((observable, oldValue, newValue) -> updateFloatingAction(newValue));
        control.floatingActionAlignmentProperty().addListener((observable, oldValue, newValue) -> updateLayoutState());
        control.nodeOrientationProperty().addListener(nodeOrientationInvalidation);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
        control.getActions().addListener(actionsListener);

        updateActions();
        updateFloatingAction(control.getFloatingAction());
        updateLayoutState();
        getChildren().addAll(actions, floatingActionSlot);
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3BottomAppBar control = getSkinnable();
        control.getActions().removeListener(actionsListener);
        control.nodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        actions.nodeOrientationProperty().unbind();
        actions.spacingProperty().unbind();
        clearActionSlots();
        floatingActionSlot.getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width needed by the action and floating action slots.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + computeContentWidth(height, true) + rightInset;
    }

    /// Computes the minimum height from the Material container height token.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getContainerHeight() + bottomInset;
    }

    /// Computes the preferred width needed by the action and floating action slots.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + computeContentWidth(height, false) + rightInset;
    }

    /// Computes the preferred height from the Material container height token.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getContainerHeight() + bottomInset;
    }

    /// Computes the maximum width.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return Double.MAX_VALUE;
    }

    /// Computes the maximum height from the Material container height token.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getContainerHeight() + bottomInset;
    }

    /// Lays out the action and floating action slots using full app bar geometry.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3BottomAppBar control = getSkinnable();
        M3BottomAppBarFloatingActionAlignment alignment = control.getFloatingActionAlignment();
        boolean hasFloatingAction = floatingActionSlot.isManaged();
        double contentSpacing = hasFloatingAction ? control.getContentSpacing() : 0.0;
        double actionsWidth = snappedPrefWidth(actions, height);
        double actionsHeight = snappedPrefHeight(actions, actionsWidth);
        double floatingActionWidth = hasFloatingAction ? snappedPrefWidth(floatingActionSlot, height) : 0.0;
        double floatingActionHeight = hasFloatingAction ? snappedPrefHeight(floatingActionSlot, floatingActionWidth) : 0.0;
        double floatingActionX = computeFloatingActionX(
                x,
                width,
                floatingActionWidth,
                alignment,
                hasFloatingAction
        );
        double actionsX = computeActionsX(
                x,
                floatingActionWidth,
                floatingActionX,
                contentSpacing,
                alignment,
                hasFloatingAction
        );
        double actionsY = y + snappedOffset((height - actionsHeight) / 2.0);
        double floatingActionY = y + snappedOffset((height - floatingActionHeight) / 2.0);

        actions.resizeRelocate(actionsX, actionsY, actionsWidth, actionsHeight);
        actions.layout();
        if (hasFloatingAction) {
            floatingActionSlot.resizeRelocate(
                    floatingActionX,
                    floatingActionY,
                    floatingActionWidth,
                    floatingActionHeight
            );
            floatingActionSlot.layout();
        }
    }

    /// Updates the regular action container.
    private void updateActions() {
        clearActionSlots();
        for (Node action : getSkinnable().getActions()) {
            actions.getChildren().add(createActionSlot(action));
        }
        getSkinnable().requestLayout();
    }

    /// Removes child references from generated action slots before rebuilding or disposing the skin.
    private void clearActionSlots() {
        for (Node child : actions.getChildren()) {
            if (child instanceof SlotPane slot) {
                slot.getChildren().clear();
            }
        }
        actions.getChildren().clear();
    }

    /// Creates a fixed Material action slot for a public regular action node.
    private static SlotPane createActionSlot(Node action) {
        SlotPane slot = new SlotPane();
        slot.getStyleClass().add(M3BottomAppBar.ACTION_SLOT_STYLE_CLASS);
        slot.getChildren().add(action);
        return slot;
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

    /// Updates slot alignment from orientation and floating action placement.
    private void updateLayoutState() {
        actions.setAlignment(M3NodeLayout.logicalStartCenterAlignment(getSkinnable()));
        switch (getSkinnable().getFloatingActionAlignment()) {
            case START -> floatingActionSlot.setAlignment(M3NodeLayout.logicalStartCenterAlignment(getSkinnable()));
            case CENTER -> floatingActionSlot.setAlignment(Pos.CENTER);
            case END -> floatingActionSlot.setAlignment(M3NodeLayout.logicalEndCenterAlignment(getSkinnable()));
        }
        getSkinnable().requestLayout();
    }

    /// Computes the content width needed by visible slots.
    private double computeContentWidth(double height, boolean minimum) {
        double actionsWidth = minimum ? actions.minWidth(height) : actions.prefWidth(height);
        double floatingActionWidth = floatingActionSlot.isManaged()
                ? (minimum ? floatingActionSlot.minWidth(height) : floatingActionSlot.prefWidth(height))
                : 0.0;
        double spacing = actionsWidth > 0.0 && floatingActionWidth > 0.0 ? getSkinnable().getContentSpacing() : 0.0;
        return actionsWidth + floatingActionWidth + spacing;
    }

    /// Computes the logical x coordinate for the floating action slot.
    private double computeFloatingActionX(
            double x,
            double width,
            double floatingActionWidth,
            M3BottomAppBarFloatingActionAlignment alignment,
            boolean hasFloatingAction
    ) {
        if (!hasFloatingAction) {
            return x;
        }
        return switch (alignment) {
            case START -> x;
            case CENTER -> x + snapPositionX(Math.max(0.0, (width - floatingActionWidth) / 2.0));
            case END -> x + width - floatingActionWidth;
        };
    }

    /// Computes the logical x coordinate for regular actions.
    private double computeActionsX(
            double x,
            double floatingActionWidth,
            double floatingActionX,
            double contentSpacing,
            M3BottomAppBarFloatingActionAlignment alignment,
            boolean hasFloatingAction
    ) {
        if (!hasFloatingAction) {
            return x;
        }
        return switch (alignment) {
            case START -> floatingActionX + floatingActionWidth + contentSpacing;
            case CENTER, END -> x;
        };
    }

    /// Returns a child node's snapped preferred width.
    private double snappedPrefWidth(Node node, double height) {
        return snapSizeX(node.prefWidth(height));
    }

    /// Returns a child node's snapped preferred height.
    private double snappedPrefHeight(Node node, double width) {
        return snapSizeY(node.prefHeight(width));
    }

    /// Snaps an offset to the vertical pixel grid.
    private double snappedOffset(double value) {
        return snapPositionY(Math.max(0.0, value));
    }

    /// A slot pane that preserves the 48 dp Material bottom app bar action target while centering smaller controls.
    @NotNullByDefault
    private static final class SlotPane extends StackPane {
        /// Creates a bottom app bar action slot.
        private SlotPane() {
            setAlignment(Pos.CENTER);
        }

        /// Computes the minimum slot width.
        @Override
        protected double computeMinWidth(double height) {
            return Math.max(MINIMUM_ACTION_SLOT_SIZE, super.computeMinWidth(height));
        }

        /// Computes the minimum slot height.
        @Override
        protected double computeMinHeight(double width) {
            return Math.max(MINIMUM_ACTION_SLOT_SIZE, super.computeMinHeight(width));
        }

        /// Computes the preferred slot width.
        @Override
        protected double computePrefWidth(double height) {
            return Math.max(MINIMUM_ACTION_SLOT_SIZE, super.computePrefWidth(height));
        }

        /// Computes the preferred slot height.
        @Override
        protected double computePrefHeight(double width) {
            return Math.max(MINIMUM_ACTION_SLOT_SIZE, super.computePrefHeight(width));
        }
    }
}
