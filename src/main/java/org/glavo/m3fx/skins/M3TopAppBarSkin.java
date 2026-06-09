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
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3TopAppBar].
@NotNullByDefault
public final class M3TopAppBarSkin extends SkinBase<M3TopAppBar> {
    /// The spacing between trailing action nodes.
    private static final double ACTION_SPACING = 8.0;

    /// The slot that hosts the optional navigation node.
    private final StackPane navigationSlot = new StackPane();

    /// The label that renders the app bar title.
    private final Label titleLabel = new Label();

    /// The trailing action node container.
    private final HBox actions = new HBox(ACTION_SPACING);

    /// Updates the visual action list when public actions change.
    private final ListChangeListener<Node> actionsListener = change -> updateActions();

    /// Updates logical alignment when node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateVariantLayout();

    /// Requests slot layout again when the control size changes outside a normal parent layout pass.
    private final InvalidationListener sizeInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a top app bar skin.
    public M3TopAppBarSkin(M3TopAppBar control) {
        super(control);
        navigationSlot.setManaged(false);
        titleLabel.setManaged(false);
        actions.setManaged(false);
        actions.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        navigationSlot.getStyleClass().add(M3TopAppBar.NAVIGATION_STYLE_CLASS);
        titleLabel.getStyleClass().add(M3TopAppBar.TITLE_STYLE_CLASS);
        actions.getStyleClass().add(M3TopAppBar.ACTIONS_STYLE_CLASS);
        titleLabel.textProperty().bind(control.titleProperty());

        control.navigationProperty().addListener((observable, oldValue, newValue) -> updateNavigation(newValue));
        control.getActions().addListener(actionsListener);
        control.variantProperty().addListener((observable, oldValue, newValue) -> updateVariantLayout());
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
        control.widthProperty().addListener(sizeInvalidation);
        control.heightProperty().addListener(sizeInvalidation);

        updateNavigation(control.getNavigation());
        updateActions();
        updateVariantLayout();
        getChildren().addAll(navigationSlot, titleLabel, actions);
    }

    /// Removes listeners, bindings, and child references before disposal.
    @Override
    public void dispose() {
        M3TopAppBar control = getSkinnable();
        titleLabel.textProperty().unbind();
        control.getActions().removeListener(actionsListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        control.widthProperty().removeListener(sizeInvalidation);
        control.heightProperty().removeListener(sizeInvalidation);
        actions.nodeOrientationProperty().unbind();
        actions.getChildren().clear();
        navigationSlot.getChildren().clear();
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
        return leftInset + computeContentMinWidth(height) + rightInset;
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
        return topInset + Math.min(getSkinnable().getContainerHeight(), computeVariantHeight()) + bottomInset;
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
        return leftInset + computeContentPrefWidth(height) + rightInset;
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
        return topInset + computeVariantHeight() + bottomInset;
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
        return Double.MAX_VALUE;
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
        return topInset + computeVariantHeight() + bottomInset;
    }

    /// Lays out the app bar slots according to the active variant and node orientation.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3TopAppBar control = getSkinnable();
        M3TopAppBarVariant variant = control.getVariant();
        boolean rightToLeft = isRightToLeft();

        double navigationWidth = navigationSlot.isManaged() ? snappedPrefWidth(navigationSlot, height) : 0.0;
        double navigationHeight = navigationSlot.isManaged() ? snappedPrefHeight(navigationSlot, navigationWidth) : 0.0;
        double actionsWidth = snappedPrefWidth(actions, height);
        double actionsHeight = snappedPrefHeight(actions, actionsWidth);
        double rowHeight = Math.min(height, control.getContainerHeight());
        double navigationY = y + snappedOffset((rowHeight - navigationHeight) / 2.0);
        double actionsY = y + snappedOffset((rowHeight - actionsHeight) / 2.0);

        // JavaFX mirrors child coordinates for RTL parents, so slot positions are specified in logical LTR space.
        if (navigationSlot.isManaged()) {
            navigationSlot.resizeRelocate(x, navigationY, navigationWidth, navigationHeight);
            navigationSlot.layout();
        }
        actions.resizeRelocate(x + width - actionsWidth, actionsY, actionsWidth, actionsHeight);
        actions.layout();

        if (variant == M3TopAppBarVariant.CENTER_ALIGNED) {
            layoutCenterAlignedTitle(x, y, width, rowHeight, navigationWidth, actionsWidth);
        } else if (variant == M3TopAppBarVariant.MEDIUM || variant == M3TopAppBarVariant.LARGE) {
            layoutTallTitle(x, y, width, height, rightToLeft);
        } else {
            layoutSmallTitle(x, y, width, rowHeight, navigationWidth, actionsWidth);
        }
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
        boolean rightToLeft = isRightToLeft();

        actions.setAlignment(rightToLeft ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        if (centerAligned) {
            titleLabel.setAlignment(Pos.CENTER);
        } else {
            titleLabel.setAlignment(rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        }
        getSkinnable().requestLayout();
    }

    /// Computes the minimum content width needed by app bar slots.
    private double computeContentMinWidth(double height) {
        return snappedPrefWidth(navigationSlot, height)
                + snappedPrefWidth(titleLabel, height)
                + snappedPrefWidth(actions, height)
                + getSkinnable().getContentSpacing() * 2.0;
    }

    /// Computes the preferred content width needed by app bar slots.
    private double computeContentPrefWidth(double height) {
        return computeContentMinWidth(height);
    }

    /// Computes the active top app bar variant height.
    private double computeVariantHeight() {
        return switch (getSkinnable().getVariant()) {
            case MEDIUM -> getSkinnable().getMediumContainerHeight();
            case LARGE -> getSkinnable().getLargeContainerHeight();
            case SMALL, CENTER_ALIGNED -> getSkinnable().getContainerHeight();
        };
    }

    /// Lays out the centered title variant while avoiding overlap with navigation and action slots.
    private void layoutCenterAlignedTitle(
            double x,
            double y,
            double width,
            double rowHeight,
            double navigationWidth,
            double actionsWidth
    ) {
        double titleHeight = snappedPrefHeight(titleLabel, width);
        double spacing = getSkinnable().getContentSpacing();
        double maximumTitleWidth = Math.max(0.0, width - navigationWidth - actionsWidth - spacing * 2.0);
        double titleWidth = Math.min(snappedPrefWidth(titleLabel, titleHeight), maximumTitleWidth);
        double titleX = x + (width - titleWidth) / 2.0;
        double leadingReserved = navigationWidth + spacingAfter(navigationWidth);
        double trailingReserved = actionsWidth + spacingAfter(actionsWidth);
        titleX = clamp(titleX, x + leadingReserved, x + width - trailingReserved - titleWidth);
        double titleY = y + snappedOffset((rowHeight - titleHeight) / 2.0);
        titleLabel.resizeRelocate(titleX, titleY, titleWidth, titleHeight);
    }

    /// Lays out a medium or large title at the bottom edge of the app bar.
    private void layoutTallTitle(double x, double y, double width, double height, boolean rightToLeft) {
        double titleHeight = snappedPrefHeight(titleLabel, width);
        double titleY = y + height - titleHeight;
        titleLabel.resizeRelocate(x, titleY, width, titleHeight);
        titleLabel.setAlignment(rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    }

    /// Lays out a small title between the leading navigation slot and trailing actions.
    private void layoutSmallTitle(
            double x,
            double y,
            double width,
            double rowHeight,
            double navigationWidth,
            double actionsWidth
    ) {
        double titleHeight = snappedPrefHeight(titleLabel, width);
        double titleY = y + snappedOffset((rowHeight - titleHeight) / 2.0);
        double titleX = x + navigationWidth + spacingAfter(navigationWidth);
        double titleWidth = Math.max(0.0,
                width - navigationWidth - actionsWidth - spacingAfter(navigationWidth) - spacingAfter(actionsWidth));
        titleLabel.resizeRelocate(titleX, titleY, titleWidth, titleHeight);
    }

    /// Returns the logical spacing after a visible slot.
    private double spacingAfter(double slotWidth) {
        return slotWidth > 0.0 ? getSkinnable().getContentSpacing() : 0.0;
    }

    /// Returns whether the control should lay out logical leading slots on the physical right edge.
    private boolean isRightToLeft() {
        NodeOrientation orientation = getSkinnable().getNodeOrientation();
        return orientation == NodeOrientation.RIGHT_TO_LEFT
                || orientation == NodeOrientation.INHERIT
                && getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
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

    /// Restricts a value to a closed range.
    private static double clamp(double value, double minimum, double maximum) {
        if (maximum < minimum) {
            return minimum;
        }
        return Math.max(minimum, Math.min(value, maximum));
    }
}
