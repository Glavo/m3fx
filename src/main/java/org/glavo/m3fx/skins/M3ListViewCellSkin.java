// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3ListCell;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ListCell].
///
/// @param <T> the item type rendered by the skinned cell
@NotNullByDefault
public final class M3ListViewCellSkin<T> extends SkinBase<M3ListCell<T>> {
    /// The fallback height used to measure empty trailing virtual-flow cells.
    private static final double DEFAULT_ROW_HEIGHT = 56.0;

    /// The currently installed rendered row node.
    private @Nullable Node graphic;

    /// Updates the rendered row when the cell graphic changes.
    private final ChangeListener<@Nullable Node> graphicListener =
            (observable, oldValue, newValue) -> updateGraphic(newValue);

    /// Requests row re-layout when the cell direction changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> {
        getSkinnable().requestLayout();
        layoutCurrentRow();
    };

    /// Creates a virtualized list view cell skin.
    ///
    /// @param control the skinned virtualized list cell
    public M3ListViewCellSkin(M3ListCell<T> control) {
        super(control);
        control.graphicProperty().addListener(graphicListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
        updateGraphic(control.getGraphic());
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3ListCell<T> cell = getSkinnable();
        cell.graphicProperty().removeListener(graphicListener);
        cell.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        if (cell.getSkin() == null || cell.getSkin() == this) {
            getChildren().clear();
        }
        graphic = null;
        super.dispose();
    }

    /// Computes the minimum width from the rendered row.
    ///
    /// @param height the height that should be used if width depends on it
    /// @param topInset the snapped top inset
    /// @param rightInset the snapped right inset
    /// @param bottomInset the snapped bottom inset
    /// @param leftInset the snapped left inset
    /// @return the minimum width
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        Node row = graphic;
        return row == null ? leftInset + rightInset : leftInset + row.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the rendered row.
    ///
    /// @param width the width that should be used if height depends on it
    /// @param topInset the snapped top inset
    /// @param rightInset the snapped right inset
    /// @param bottomInset the snapped bottom inset
    /// @param leftInset the snapped left inset
    /// @return the minimum height
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        Node row = graphic;
        double itemHeight = fixedOrMeasuredHeight(row == null ? fallbackRowHeight() : row.minHeight(width));
        return topInset + itemHeight + trailingSpacing() + bottomInset;
    }

    /// Computes the preferred width from the rendered row.
    ///
    /// @param height the height that should be used if width depends on it
    /// @param topInset the snapped top inset
    /// @param rightInset the snapped right inset
    /// @param bottomInset the snapped bottom inset
    /// @param leftInset the snapped left inset
    /// @return the preferred width
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        Node row = graphic;
        return row == null ? leftInset + rightInset : leftInset + row.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the rendered row.
    ///
    /// @param width the width that should be used if height depends on it
    /// @param topInset the snapped top inset
    /// @param rightInset the snapped right inset
    /// @param bottomInset the snapped bottom inset
    /// @param leftInset the snapped left inset
    /// @return the preferred height
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        Node row = graphic;
        double itemHeight = fixedOrMeasuredHeight(row == null ? fallbackRowHeight() : row.prefHeight(width));
        return topInset + itemHeight + trailingSpacing() + bottomInset;
    }

    /// Lays out the rendered row in the full cell content area.
    ///
    /// @param x the layout area's x coordinate
    /// @param y the layout area's y coordinate
    /// @param width the layout area's width
    /// @param height the layout area's height
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        layoutRow(x, y, width, height);
    }

    /// Lays out the current rendered row within the current skinnable bounds.
    private void layoutCurrentRow() {
        M3ListCell<T> cell = getSkinnable();
        double width = cell.getWidth();
        double height = cell.getHeight();
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        layoutRow(0.0, 0.0, width, height);
    }

    /// Lays out the rendered row in the supplied area.
    private void layoutRow(double x, double y, double width, double height) {
        Node row = graphic;
        if (row == null) {
            return;
        }

        double itemAreaHeight = Math.max(0.0, height - trailingSpacing());
        double rowWidth = snapSizeX(boundedSize(
                rowMinimumWidth(row, height),
                width,
                rowMaximumWidth(row, height),
                width
        ));
        double fixedCellSize = getSkinnable().getListView().getFixedCellSize();
        double rowHeight = fixedCellSize > 0.0
                ? snapSizeY(Math.min(fixedCellSize, itemAreaHeight))
                : snapSizeY(boundedSize(
                        row.minHeight(rowWidth),
                        row.prefHeight(rowWidth),
                        row.maxHeight(rowWidth),
                        itemAreaHeight
                ));
        double rowX = alignedX(
                x,
                width,
                rowWidth,
                M3NodeLayout.logicalStartHorizontalAlignment(getSkinnable())
        );
        double rowY = y + (itemAreaHeight - rowHeight) / 2.0;
        row.resizeRelocate(snapPositionX(rowX), snapPositionY(rowY), rowWidth, rowHeight);
    }

    /// Returns the row minimum width while allowing default computed constraints to shrink inside the viewport.
    private static double rowMinimumWidth(Node row, double height) {
        return row instanceof Region region && region.getMinWidth() == Region.USE_COMPUTED_SIZE
                ? 0.0
                : row.minWidth(height);
    }

    /// Returns the row maximum width while allowing default computed constraints to fill the viewport.
    private static double rowMaximumWidth(Node row, double height) {
        return row instanceof Region region && region.getMaxWidth() == Region.USE_COMPUTED_SIZE
                ? Double.MAX_VALUE
                : row.maxWidth(height);
    }

    /// Returns a child size bounded by its constraints and available size.
    private static double boundedSize(double minimum, double preferred, double maximum, double available) {
        return Math.min(available, Math.max(minimum, Math.min(preferred, maximum)));
    }

    /// Returns the row height used before a rendered item supplies content metrics.
    private double fallbackRowHeight() {
        double fixedCellSize = getSkinnable().getListView().getFixedCellSize();
        return fixedCellSize > 0.0 ? fixedCellSize : DEFAULT_ROW_HEIGHT;
    }

    /// Returns a configured fixed item height or the supplied measured item height.
    private double fixedOrMeasuredHeight(double measuredHeight) {
        double fixedCellSize = getSkinnable().getListView().getFixedCellSize();
        return fixedCellSize > 0.0 ? fixedCellSize : measuredHeight;
    }

    /// Returns the gap following this cell, excluding the final data item.
    private double trailingSpacing() {
        M3ListCell<T> cell = getSkinnable();
        int index = cell.getIndex();
        return index >= 0 && index + 1 < cell.getListView().getItems().size()
                ? cell.getListView().getItemSpacing()
                : 0.0;
    }

    /// Returns the physical x coordinate for one horizontal alignment.
    private static double alignedX(double x, double width, double childWidth, HPos alignment) {
        return switch (alignment) {
            case CENTER -> x + (width - childWidth) / 2.0;
            case RIGHT -> x + width - childWidth;
            default -> x;
        };
    }

    /// Replaces the rendered row node owned by this skin.
    private void updateGraphic(@Nullable Node graphic) {
        Node oldGraphic = this.graphic;
        if (oldGraphic == graphic) {
            return;
        }

        this.graphic = graphic;
        if (graphic == null) {
            getChildren().clear();
        } else {
            getChildren().setAll(graphic);
        }
        getSkinnable().requestLayout();
    }
}
