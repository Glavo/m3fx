// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3SearchBar].
@NotNullByDefault
public final class M3SearchBarSkin extends SkinBase<M3SearchBar> {
    /// The spacing between search bar content slots.
    private static final double CONTENT_SPACING = 12.0;

    /// The spacing between trailing action nodes.
    private static final double ACTION_SPACING = 4.0;

    /// The internal horizontal container.
    private final HBox container = new HBox(CONTENT_SPACING);

    /// The slot that hosts the optional leading content.
    private final StackPane leadingSlot = new StackPane();

    /// The trailing action container.
    private final HBox trailingBox = new HBox(ACTION_SPACING);

    /// Updates the leading slot when the public leading node changes.
    private final ChangeListener<@Nullable Node> leadingListener =
            (observable, oldValue, newValue) -> updateLeading(newValue);

    /// Updates the trailing action container when public actions change.
    private final ListChangeListener<Node> trailingActionsListener = change -> updateTrailingActions();

    /// Creates a search bar skin.
    ///
    /// @param control the search bar controlled by this skin
    public M3SearchBarSkin(M3SearchBar control) {
        super(control);
        container.setManaged(false);
        container.getStyleClass().add(M3SearchBar.CONTENT_STYLE_CLASS);
        container.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        leadingSlot.getStyleClass().add(M3SearchBar.LEADING_STYLE_CLASS);
        trailingBox.getStyleClass().add(M3SearchBar.TRAILING_STYLE_CLASS);
        trailingBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(control.getEditor(), Priority.ALWAYS);

        control.leadingProperty().addListener(leadingListener);
        control.getTrailingActions().addListener(trailingActionsListener);

        updateLeading(control.getLeading());
        updateTrailingActions();
        container.getChildren().setAll(leadingSlot, control.getEditor(), trailingBox);
        getChildren().add(container);
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3SearchBar control = getSkinnable();
        control.leadingProperty().removeListener(leadingListener);
        control.getTrailingActions().removeListener(trailingActionsListener);
        container.alignmentProperty().unbind();
        container.nodeOrientationProperty().unbind();
        trailingBox.getChildren().clear();
        leadingSlot.getChildren().clear();
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

    /// Updates the leading slot content.
    private void updateLeading(@Nullable Node node) {
        leadingSlot.getChildren().clear();
        leadingSlot.setVisible(node != null);
        leadingSlot.setManaged(node != null);
        if (node != null) {
            leadingSlot.getChildren().add(node);
        }
        getSkinnable().requestLayout();
    }

    /// Updates trailing action container content and visibility.
    private void updateTrailingActions() {
        trailingBox.getChildren().setAll(getSkinnable().getTrailingActions());
        boolean visible = !trailingBox.getChildren().isEmpty();
        trailingBox.setVisible(visible);
        trailingBox.setManaged(visible);
        getSkinnable().requestLayout();
    }
}
