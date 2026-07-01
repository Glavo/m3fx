// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.geometry.NodeOrientation;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3SideSheet].
@NotNullByDefault
public final class M3SideSheetSkin extends SkinBase<M3SideSheet> {
    /// The internal sheet layout root.
    private final BorderPane container = new BorderPane();

    /// The header row.
    private final HBox header = new HBox();

    /// The headline label.
    private final Label headlineLabel = new Label();

    /// The flexible header spacer.
    private final Region spacer = new Region();

    /// The trailing action node container.
    private final HBox actions = new HBox();

    /// The content slot.
    private final StackPane contentSlot = new StackPane();

    /// Updates content when the public content property changes.
    private final ChangeListener<@Nullable Node> contentListener =
            (observable, oldValue, newValue) -> updateContent(newValue);

    /// Updates actions when the public action list changes.
    private final ListChangeListener<Node> actionsListener = change -> updateActions();

    /// Creates a side sheet skin.
    ///
    /// @param control the side sheet controlled by this skin
    public M3SideSheetSkin(M3SideSheet control) {
        super(control);
        container.setManaged(false);
        header.getStyleClass().add(M3SideSheet.HEADER_STYLE_CLASS);
        headlineLabel.getStyleClass().add(M3SideSheet.TITLE_STYLE_CLASS);
        actions.getStyleClass().add(M3SideSheet.ACTIONS_STYLE_CLASS);
        contentSlot.getStyleClass().add(M3SideSheet.CONTENT_STYLE_CLASS);
        header.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        actions.alignmentProperty().bind(M3NodeLayout.createLogicalEndCenterAlignmentBinding(control));
        contentSlot.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(control));
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        header.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        contentSlot.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headlineLabel.textProperty().bind(control.headlineProperty());

        control.contentProperty().addListener(contentListener);
        control.getActions().addListener(actionsListener);
        updateContent(control.getContent());
        updateActions();
        header.getChildren().setAll(headlineLabel, spacer, actions);
        container.setTop(header);
        container.setCenter(contentSlot);
        getChildren().add(container);
    }

    /// Removes listeners, bindings, and child references before disposal.
    @Override
    public void dispose() {
        M3SideSheet control = getSkinnable();
        headlineLabel.textProperty().unbind();
        control.contentProperty().removeListener(contentListener);
        control.getActions().removeListener(actionsListener);
        container.nodeOrientationProperty().unbind();
        header.nodeOrientationProperty().unbind();
        header.alignmentProperty().unbind();
        actions.alignmentProperty().unbind();
        contentSlot.nodeOrientationProperty().unbind();
        contentSlot.alignmentProperty().unbind();
        actions.getChildren().clear();
        contentSlot.getChildren().clear();
        header.getChildren().clear();
        container.setTop(null);
        container.setCenter(null);
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

    /// Updates the content slot.
    private void updateContent(@Nullable Node content) {
        contentSlot.getChildren().clear();
        if (content != null) {
            contentSlot.getChildren().add(content);
        }
        getSkinnable().requestLayout();
    }

    /// Updates the action row.
    private void updateActions() {
        actions.getChildren().setAll(getSkinnable().getActions());
        getSkinnable().requestLayout();
    }
}