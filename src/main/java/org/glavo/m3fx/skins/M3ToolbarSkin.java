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
import org.glavo.m3fx.controls.M3ToolbarVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

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
        control.variantProperty().addListener(layoutInvalidation);
        control.itemSlotSizeProperty().addListener(layoutInvalidation);
        control.itemSpacingProperty().addListener(layoutInvalidation);
        control.dockedMaxItemSpacingProperty().addListener(layoutInvalidation);
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
        control.variantProperty().removeListener(layoutInvalidation);
        control.itemSlotSizeProperty().removeListener(layoutInvalidation);
        control.itemSpacingProperty().removeListener(layoutInvalidation);
        control.dockedMaxItemSpacingProperty().removeListener(layoutInvalidation);
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
        container.setDocked(toolbar.getVariant() == M3ToolbarVariant.DOCKED);
        container.setSpacing(toolbar.getItemSpacing());
        container.setDockedMaxSpacing(toolbar.getDockedMaxItemSpacing());
        container.setSlotSize(toolbar.getItemSlotSize());
    }

    /// Layout pane that arranges fixed-size toolbar item slots in one row or column.
    @NotNullByDefault
    private static final class ToolbarPane extends Pane {
        /// The toolbar item flow orientation.
        private Orientation orientation = Orientation.HORIZONTAL;

        /// Whether the toolbar uses docked adaptive spacing and centering.
        private boolean docked;

        /// The spacing between generated item slots.
        private double spacing;

        /// The preferred maximum spacing between docked toolbar items.
        private double dockedMaxSpacing;

        /// The minimum generated item slot width and height.
        private double slotSize;

        /// Returns the toolbar item flow orientation.
        private Orientation getOrientation() {
            return orientation;
        }

        /// Sets the toolbar item flow orientation.
        private void setOrientation(Orientation orientation) {
            if (this.orientation == orientation) {
                return;
            }
            this.orientation = orientation;
            requestLayout();
        }

        /// Selects floating or docked layout behavior.
        private void setDocked(boolean docked) {
            if (this.docked == docked) {
                return;
            }
            this.docked = docked;
            requestLayout();
        }

        /// Sets the spacing between generated item slots.
        private void setSpacing(double spacing) {
            if (Double.compare(this.spacing, spacing) == 0) {
                return;
            }
            this.spacing = spacing;
            requestLayout();
        }

        /// Sets the preferred maximum spacing between docked toolbar items.
        private void setDockedMaxSpacing(double dockedMaxSpacing) {
            if (Double.compare(this.dockedMaxSpacing, dockedMaxSpacing) == 0) {
                return;
            }
            this.dockedMaxSpacing = dockedMaxSpacing;
            requestLayout();
        }

        /// Sets the minimum generated item slot width and height.
        private void setSlotSize(double slotSize) {
            if (Double.compare(this.slotSize, slotSize) == 0) {
                return;
            }
            this.slotSize = slotSize;
            requestLayout();
        }

        /// Computes the minimum toolbar content width.
        @Override
        protected double computeMinWidth(double height) {
            return getOrientation() == Orientation.HORIZONTAL ? horizontalLength(true, spacing) : crossLength(true);
        }

        /// Computes the minimum toolbar content height.
        @Override
        protected double computeMinHeight(double width) {
            return getOrientation() == Orientation.HORIZONTAL ? crossLength(false) : horizontalLength(false, spacing);
        }

        /// Computes the preferred toolbar content width.
        @Override
        protected double computePrefWidth(double height) {
            double preferredSpacing = docked ? Math.max(spacing, dockedMaxSpacing) : spacing;
            return getOrientation() == Orientation.HORIZONTAL
                    ? horizontalLength(true, preferredSpacing)
                    : crossLength(true);
        }

        /// Computes the preferred toolbar content height.
        @Override
        protected double computePrefHeight(double width) {
            double preferredSpacing = docked ? Math.max(spacing, dockedMaxSpacing) : spacing;
            return getOrientation() == Orientation.HORIZONTAL
                    ? crossLength(false)
                    : horizontalLength(false, preferredSpacing);
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
            List<Node> children = getManagedChildren();
            int count = children.size();
            double childrenLength = 0.0;
            for (Node child : children) {
                childrenLength += snapSizeX(Math.max(slotSize, child.prefWidth(-1.0)));
            }
            double spacingValue = resolvedSpacing(getWidth(), childrenLength, count, true);
            double groupLength = childrenLength + Math.max(0, count - 1) * spacingValue;
            double position = docked ? Math.max(0.0, (getWidth() - groupLength) / 2.0) : 0.0;
            for (Node child : children) {
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
            List<Node> children = getManagedChildren();
            int count = children.size();
            double childrenLength = 0.0;
            for (Node child : children) {
                childrenLength += snapSizeY(Math.max(slotSize, child.prefHeight(-1.0)));
            }
            double spacingValue = resolvedSpacing(getHeight(), childrenLength, count, false);
            double groupLength = childrenLength + Math.max(0, count - 1) * spacingValue;
            double position = docked ? Math.max(0.0, (getHeight() - groupLength) / 2.0) : 0.0;
            for (Node child : children) {
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

        /// Resolves fixed floating spacing or adaptive docked spacing for the available main-axis length.
        private double resolvedSpacing(double availableLength, double childrenLength, int count, boolean horizontal) {
            double minimum = horizontal ? snapSpaceX(spacing) : snapSpaceY(spacing);
            if (!docked || count < 2) {
                return minimum;
            }
            double maximum = horizontal ? snapSpaceX(dockedMaxSpacing) : snapSpaceY(dockedMaxSpacing);
            double availableSpacing = (availableLength - childrenLength) / (count - 1);
            return Math.max(minimum, Math.min(maximum, availableSpacing));
        }

        /// Returns the total length along the toolbar item flow axis.
        private double horizontalLength(boolean widthAxis, double spacingValue) {
            double total = 0.0;
            int count = 0;
            for (Node child : getManagedChildren()) {
                double preferred = widthAxis ? child.prefWidth(-1.0) : child.prefHeight(-1.0);
                total += Math.max(slotSize, preferred);
                count++;
            }
            return total + Math.max(0, count - 1) * spacingValue;
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
