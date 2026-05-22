// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3BottomSheet].
@NotNullByDefault
public final class M3BottomSheetSkin extends SkinBase<M3BottomSheet> {
    /// The internal sheet layout root.
    private final BorderPane container = new BorderPane();

    /// The top area containing the drag handle and header.
    private final VBox topArea = new VBox();

    /// The drag handle slot.
    private final StackPane dragHandleSlot = new StackPane();

    /// The drag handle region.
    private final Region dragHandle = new Region();

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

    /// Updates drag handle visibility when the public property changes.
    private final ChangeListener<Boolean> dragHandleVisibleListener =
            (observable, oldValue, newValue) -> updateDragHandleVisibility();

    /// Updates actions when the public action list changes.
    private final ListChangeListener<Node> actionsListener = change -> updateActions();

    /// Updates logical layout when the effective node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateNodeOrientationLayout();

    /// Creates a bottom sheet skin.
    ///
    /// @param control the bottom sheet controlled by this skin
    public M3BottomSheetSkin(M3BottomSheet control) {
        super(control);
        container.setManaged(false);
        dragHandleSlot.getStyleClass().add(M3BottomSheet.DRAG_HANDLE_CONTAINER_STYLE_CLASS);
        dragHandle.getStyleClass().add(M3BottomSheet.DRAG_HANDLE_STYLE_CLASS);
        header.getStyleClass().add(M3BottomSheet.HEADER_STYLE_CLASS);
        headlineLabel.getStyleClass().add(M3BottomSheet.TITLE_STYLE_CLASS);
        actions.getStyleClass().add(M3BottomSheet.ACTIONS_STYLE_CLASS);
        contentSlot.getStyleClass().add(M3BottomSheet.CONTENT_STYLE_CLASS);
        contentSlot.setAlignment(Pos.TOP_LEFT);
        header.setAlignment(Pos.CENTER_LEFT);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        topArea.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        header.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        contentSlot.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headlineLabel.textProperty().bind(control.headlineProperty());

        control.contentProperty().addListener(contentListener);
        control.dragHandleVisibleProperty().addListener(dragHandleVisibleListener);
        control.getActions().addListener(actionsListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
        updateContent(control.getContent());
        updateActions();
        updateDragHandleVisibility();
        dragHandleSlot.getChildren().setAll(dragHandle);
        header.getChildren().setAll(headlineLabel, spacer, actions);
        topArea.getChildren().setAll(dragHandleSlot, header);
        container.setTop(topArea);
        container.setCenter(contentSlot);
        updateNodeOrientationLayout();
        getChildren().add(container);
    }

    /// Removes listeners, bindings, and child references before disposal.
    @Override
    public void dispose() {
        M3BottomSheet control = getSkinnable();
        headlineLabel.textProperty().unbind();
        control.contentProperty().removeListener(contentListener);
        control.dragHandleVisibleProperty().removeListener(dragHandleVisibleListener);
        control.getActions().removeListener(actionsListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        topArea.nodeOrientationProperty().unbind();
        header.nodeOrientationProperty().unbind();
        contentSlot.nodeOrientationProperty().unbind();
        actions.getChildren().clear();
        contentSlot.getChildren().clear();
        dragHandleSlot.getChildren().clear();
        header.getChildren().clear();
        topArea.getChildren().clear();
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

    /// Updates the drag handle slot visibility.
    private void updateDragHandleVisibility() {
        boolean visible = getSkinnable().isDragHandleVisible();
        dragHandleSlot.setVisible(visible);
        dragHandleSlot.setManaged(visible);
        getSkinnable().requestLayout();
    }

    /// Updates orientation-dependent alignments for header and content slots.
    private void updateNodeOrientationLayout() {
        boolean rightToLeft = getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        header.setAlignment(rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        actions.setAlignment(rightToLeft ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        contentSlot.setAlignment(Pos.TOP_LEFT);
        getSkinnable().requestLayout();
    }
}
