// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Toolbar;
import org.glavo.m3fx.controls.M3ToolbarVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

/// The default Material Design 3 skin for [M3Toolbar].
///
/// The skin arranges toolbar items in logical order and applies the active docked or floating variant's spacing and
/// alignment. Items retain their own preferred sizes; remaining horizontal space is distributed by the toolbar row
/// without changing the public item list.
@NotNullByDefault
public final class M3ToolbarSkin extends SkinBase<M3Toolbar> {
    /// The toolbar item slot style class.
    private static final String ITEM_SLOT_STYLE_CLASS = "m3-toolbar-item-slot";

    /// The item slot container.
    private final ToolbarPane container;

    /// Rebuilds item slots when the public item list changes.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Invalidates content measurements when layout properties change.
    private final ChangeListener<Object> layoutListener;

    /// Creates a toolbar skin.
    ///
    /// @param control the toolbar controlled by this skin
    public M3ToolbarSkin(M3Toolbar control) {
        super(control);
        container = new ToolbarPane(control);
        layoutListener = (observable, oldValue, newValue) -> {
            container.requestLayout();
            control.requestLayout();
        };
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        control.getItems().addListener(itemsListener);
        control.orientationProperty().addListener(layoutListener);
        control.variantProperty().addListener(layoutListener);
        control.itemSlotSizeProperty().addListener(layoutListener);
        control.itemSpacingProperty().addListener(layoutListener);
        control.dockedMaxItemSpacingProperty().addListener(layoutListener);
        updateItems();
        getChildren().setAll(container);
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3Toolbar control = getSkinnable();
        control.getItems().removeListener(itemsListener);
        control.orientationProperty().removeListener(layoutListener);
        control.variantProperty().removeListener(layoutListener);
        control.itemSlotSizeProperty().removeListener(layoutListener);
        control.itemSpacingProperty().removeListener(layoutListener);
        control.dockedMaxItemSpacingProperty().removeListener(layoutListener);
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
        slot.getStyleClass().add(ITEM_SLOT_STYLE_CLASS);
        return slot;
    }

    /// Layout pane that arranges fixed-size toolbar item slots in one row or column.
    @NotNullByDefault
    private static final class ToolbarPane extends Pane {
        /// The toolbar supplying live orientation, variant, and layout tokens.
        private final M3Toolbar toolbar;

        /// Creates a content pane for the supplied toolbar.
        private ToolbarPane(M3Toolbar toolbar) {
            this.toolbar = toolbar;
        }

        /// Returns whether docked spacing and centering apply.
        private boolean isDocked() {
            return toolbar.getVariant() == M3ToolbarVariant.DOCKED;
        }

        /// Computes the minimum toolbar content width.
        @Override
        protected double computeMinWidth(double height) {
            return toolbar.getOrientation() == Orientation.HORIZONTAL
                    ? mainAxisLength(true, toolbar.getItemSpacing())
                    : crossLength(true);
        }

        /// Computes the minimum toolbar content height.
        @Override
        protected double computeMinHeight(double width) {
            return toolbar.getOrientation() == Orientation.HORIZONTAL
                    ? crossLength(false)
                    : mainAxisLength(false, toolbar.getItemSpacing());
        }

        /// Computes the preferred toolbar content width.
        @Override
        protected double computePrefWidth(double height) {
            double preferredSpacing = isDocked()
                    ? Math.max(toolbar.getItemSpacing(), toolbar.getDockedMaxItemSpacing())
                    : toolbar.getItemSpacing();
            return toolbar.getOrientation() == Orientation.HORIZONTAL
                    ? mainAxisLength(true, preferredSpacing)
                    : crossLength(true);
        }

        /// Computes the preferred toolbar content height.
        @Override
        protected double computePrefHeight(double width) {
            double preferredSpacing = isDocked()
                    ? Math.max(toolbar.getItemSpacing(), toolbar.getDockedMaxItemSpacing())
                    : toolbar.getItemSpacing();
            return toolbar.getOrientation() == Orientation.HORIZONTAL
                    ? crossLength(false)
                    : mainAxisLength(false, preferredSpacing);
        }

        /// Lays out item slots in the current orientation.
        @Override
        protected void layoutChildren() {
            if (toolbar.getOrientation() == Orientation.HORIZONTAL) {
                layoutHorizontalChildren();
            } else {
                layoutVerticalChildren();
            }
        }

        /// Lays out item slots along the horizontal axis.
        private void layoutHorizontalChildren() {
            List<Node> children = getManagedChildren();
            int count = children.size();
            double childrenLength = 0.0;
            for (Node child : children) {
                childrenLength += snapSizeX(Math.max(toolbar.getItemSlotSize(), child.prefWidth(-1.0)));
            }
            double spacingValue = resolvedSpacing(getWidth(), childrenLength, count, true);
            double groupLength = childrenLength + Math.max(0, count - 1) * spacingValue;
            double position = isDocked() ? Math.max(0.0, (getWidth() - groupLength) / 2.0) : 0.0;
            for (Node child : children) {
                double width = snapSizeX(Math.max(toolbar.getItemSlotSize(), child.prefWidth(-1.0)));
                double height = snapSizeY(Math.max(toolbar.getItemSlotSize(), child.prefHeight(width)));
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
            List<Node> children = getManagedChildren();
            int count = children.size();
            double childrenLength = 0.0;
            for (Node child : children) {
                childrenLength += snapSizeY(Math.max(toolbar.getItemSlotSize(), child.prefHeight(-1.0)));
            }
            double spacingValue = resolvedSpacing(getHeight(), childrenLength, count, false);
            double groupLength = childrenLength + Math.max(0, count - 1) * spacingValue;
            double position = isDocked() ? Math.max(0.0, (getHeight() - groupLength) / 2.0) : 0.0;
            for (Node child : children) {
                double width = snapSizeX(Math.max(toolbar.getItemSlotSize(), child.prefWidth(-1.0)));
                double height = snapSizeY(Math.max(toolbar.getItemSlotSize(), child.prefHeight(width)));
                child.resizeRelocate(
                        Math.max(0.0, snapPositionX((getWidth() - width) / 2.0)),
                        snapPositionY(position),
                        width,
                        height
                );
                position += height + spacingValue;
            }
        }

        /// Resolves fixed floating spacing or adaptive docked spacing for the available main-axis length.
        private double resolvedSpacing(double availableLength, double childrenLength, int count, boolean horizontal) {
            double minimum = horizontal ? snapSpaceX(toolbar.getItemSpacing()) : snapSpaceY(toolbar.getItemSpacing());
            if (!isDocked() || count < 2) {
                return minimum;
            }
            double maximum = horizontal
                    ? snapSpaceX(toolbar.getDockedMaxItemSpacing())
                    : snapSpaceY(toolbar.getDockedMaxItemSpacing());
            double availableSpacing = (availableLength - childrenLength) / (count - 1);
            return Math.max(minimum, Math.min(maximum, availableSpacing));
        }

        /// Returns the total length along the toolbar item flow axis.
        private double mainAxisLength(boolean widthAxis, double spacingValue) {
            double total = 0.0;
            int count = 0;
            for (Node child : getManagedChildren()) {
                double preferred = widthAxis ? child.prefWidth(-1.0) : child.prefHeight(-1.0);
                total += Math.max(toolbar.getItemSlotSize(), preferred);
                count++;
            }
            return total + Math.max(0, count - 1) * spacingValue;
        }

        /// Returns the maximum item length on the toolbar cross axis.
        private double crossLength(boolean widthAxis) {
            double maximum = toolbar.getItemSlotSize();
            for (Node child : getManagedChildren()) {
                double preferred = widthAxis ? child.prefWidth(-1.0) : child.prefHeight(-1.0);
                maximum = Math.max(maximum, preferred);
            }
            return maximum;
        }
    }
}
