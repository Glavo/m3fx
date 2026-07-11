// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Toolbar;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3Toolbar].
@NotNullByDefault
public final class M3ToolbarSkin extends SkinBase<M3Toolbar> {
    /// The item slot container.
    private final ToolbarPane container = new ToolbarPane();

    /// Rebuilds item slots when the public item list changes.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Updates layout when orientation or styleable metrics change.
    private final InvalidationListener layoutInvalidation = observable -> {
        updateContainerState();
        getSkinnable().requestLayout();
    };

    /// Creates a toolbar skin.
    ///
    /// @param control the toolbar controlled by this skin
    public M3ToolbarSkin(M3Toolbar control) {
        super(control);
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        control.getItems().addListener(itemsListener);
        control.orientationProperty().addListener(layoutInvalidation);
        control.itemSlotSizeProperty().addListener(layoutInvalidation);
        control.itemSpacingProperty().addListener(layoutInvalidation);
        updateContainerState();
        updateItems();
        getChildren().setAll(container);
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3Toolbar control = getSkinnable();
        control.getItems().removeListener(itemsListener);
        control.orientationProperty().removeListener(layoutInvalidation);
        control.itemSlotSizeProperty().removeListener(layoutInvalidation);
        control.itemSpacingProperty().removeListener(layoutInvalidation);
        container.nodeOrientationProperty().unbind();
        clearItemSlots();
        getChildren().remove(container);
        super.dispose();
    }

    /// Computes the minimum width from toolbar orientation and item slots.
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

    /// Computes the minimum height from toolbar orientation and item slots.
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

    /// Computes the preferred width from toolbar orientation and item slots.
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

    /// Computes the preferred height from toolbar orientation and item slots.
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

    /// Computes the maximum width.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return getSkinnable().getOrientation() == Orientation.HORIZONTAL
                ? Double.MAX_VALUE
                : leftInset + container.maxWidth(height) + rightInset;
    }

    /// Computes the maximum height.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return getSkinnable().getOrientation() == Orientation.VERTICAL
                ? Double.MAX_VALUE
                : topInset + container.maxHeight(width) + bottomInset;
    }

    /// Lays out the toolbar item slot container.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
        container.layout();
    }

    /// Rebuilds generated item slots.
    private void updateItems() {
        clearItemSlots();
        for (Node item : getSkinnable().getItems()) {
            container.getChildren().add(createItemSlot(item));
        }
        getSkinnable().requestLayout();
    }

    /// Removes child references from generated item slots before rebuilding or disposing the skin.
    private void clearItemSlots() {
        for (Node child : container.getChildren()) {
            if (child instanceof StackPane slot) {
                slot.getChildren().clear();
            }
        }
        container.getChildren().clear();
    }

    /// Creates one centered Material toolbar item slot.
    private StackPane createItemSlot(Node item) {
        StackPane slot = new StackPane(item);
        slot.setAlignment(Pos.CENTER);
        slot.getStyleClass().add(M3Toolbar.ITEM_SLOT_STYLE_CLASS);
        return slot;
    }

    /// Updates container orientation and metrics.
    private void updateContainerState() {
        M3Toolbar toolbar = getSkinnable();
        container.setOrientation(toolbar.getOrientation());
        container.setSpacing(toolbar.getItemSpacing());
        container.setSlotSize(toolbar.getItemSlotSize());
    }

    /// Layout pane that arranges fixed-size toolbar item slots in one row or column.
    @NotNullByDefault
    private static final class ToolbarPane extends Pane {
        /// The toolbar item flow orientation.
        private Orientation orientation = Orientation.HORIZONTAL;

        /// The spacing between generated item slots.
        private double spacing;

        /// The minimum generated item slot width and height.
        private double slotSize;

        /// Returns the toolbar item flow orientation.
        private Orientation getOrientation() {
            return orientation;
        }

        /// Sets the toolbar item flow orientation.
        private void setOrientation(Orientation orientation) {
            this.orientation = orientation;
            requestLayout();
        }

        /// Sets the spacing between generated item slots.
        private void setSpacing(double spacing) {
            this.spacing = spacing;
            requestLayout();
        }

        /// Sets the minimum generated item slot width and height.
        private void setSlotSize(double slotSize) {
            this.slotSize = slotSize;
            requestLayout();
        }

        /// Computes the minimum toolbar content width.
        @Override
        protected double computeMinWidth(double height) {
            return computePrefWidth(height);
        }

        /// Computes the minimum toolbar content height.
        @Override
        protected double computeMinHeight(double width) {
            return computePrefHeight(width);
        }

        /// Computes the preferred toolbar content width.
        @Override
        protected double computePrefWidth(double height) {
            return getOrientation() == Orientation.HORIZONTAL ? horizontalLength(true) : crossLength(true);
        }

        /// Computes the preferred toolbar content height.
        @Override
        protected double computePrefHeight(double width) {
            return getOrientation() == Orientation.HORIZONTAL ? crossLength(false) : horizontalLength(false);
        }

        /// Lays out item slots in the current orientation.
        @Override
        protected void layoutChildren() {
            if (getOrientation() == Orientation.HORIZONTAL) {
                layoutHorizontalChildren();
            } else {
                layoutVerticalChildren();
            }
        }

        /// Lays out item slots along the horizontal axis.
        private void layoutHorizontalChildren() {
            double spacingValue = snapSpaceX(spacing);
            double position = 0.0;
            for (Node child : getManagedChildren()) {
                double width = snapSizeX(Math.max(slotSize, child.prefWidth(-1.0)));
                double height = snapSizeY(Math.max(slotSize, child.prefHeight(width)));
                child.resizeRelocate(
                        snapPositionX(position),
                        Math.max(0.0, snapPositionY((getHeight() - height) / 2.0)),
                        width,
                        height
                );
                position += width + spacingValue;
            }
        }

        /// Lays out item slots along the vertical axis.
        private void layoutVerticalChildren() {
            double spacingValue = snapSpaceY(spacing);
            double position = 0.0;
            for (Node child : getManagedChildren()) {
                double width = snapSizeX(Math.max(slotSize, child.prefWidth(-1.0)));
                double height = snapSizeY(Math.max(slotSize, child.prefHeight(width)));
                child.resizeRelocate(
                        Math.max(0.0, snapPositionX((getWidth() - width) / 2.0)),
                        snapPositionY(position),
                        width,
                        height
                );
                position += height + spacingValue;
            }
        }

        /// Returns the total length along the toolbar item flow axis.
        private double horizontalLength(boolean widthAxis) {
            double total = 0.0;
            int count = 0;
            for (Node child : getManagedChildren()) {
                double preferred = widthAxis ? child.prefWidth(-1.0) : child.prefHeight(-1.0);
                total += Math.max(slotSize, preferred);
                count++;
            }
            return total + Math.max(0, count - 1) * spacing;
        }

        /// Returns the maximum item length on the toolbar cross axis.
        private double crossLength(boolean widthAxis) {
            double maximum = slotSize;
            for (Node child : getManagedChildren()) {
                double preferred = widthAxis ? child.prefWidth(-1.0) : child.prefHeight(-1.0);
                maximum = Math.max(maximum, preferred);
            }
            return maximum;
        }
    }
}
