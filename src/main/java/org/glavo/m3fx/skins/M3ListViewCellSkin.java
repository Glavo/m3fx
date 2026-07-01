// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3ListViewCell;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ListViewCell].
///
/// @param <T> the item type rendered by the skinned cell
@NotNullByDefault
public final class M3ListViewCellSkin<T> extends SkinBase<M3ListViewCell<T>> {
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
    public M3ListViewCellSkin(M3ListViewCell<T> control) {
        super(control);
        control.graphicProperty().addListener(graphicListener);
        control.nodeOrientationProperty().addListener(nodeOrientationInvalidation);
        updateGraphic(control.getGraphic());
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3ListViewCell<T> cell = getSkinnable();
        cell.graphicProperty().removeListener(graphicListener);
        cell.nodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        getChildren().clear();
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
        return row == null ? topInset + bottomInset : topInset + row.minHeight(width) + bottomInset;
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
        return row == null ? topInset + bottomInset : topInset + row.prefHeight(width) + bottomInset;
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
        M3ListViewCell<T> cell = getSkinnable();
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

        double rowWidth = snapSizeX(boundedSize(row.minWidth(height), row.prefWidth(height), row.maxWidth(height), width));
        double rowHeight = snapSizeY(boundedSize(row.minHeight(rowWidth), row.prefHeight(rowWidth), row.maxHeight(rowWidth), height));
        double rowX = alignedX(x, width, rowWidth, horizontalAlignment());
        double rowY = y + (height - rowHeight) / 2.0;
        row.resizeRelocate(snapPositionX(rowX), snapPositionY(rowY), rowWidth, rowHeight);
    }

    /// Returns a child size bounded by its constraints and available size.
    private static double boundedSize(double minimum, double preferred, double maximum, double available) {
        return Math.min(available, Math.max(minimum, Math.min(preferred, maximum)));
    }

    /// Returns the physical x coordinate for one horizontal alignment.
    private static double alignedX(double x, double width, double childWidth, HPos alignment) {
        return switch (alignment) {
            case CENTER -> x + (width - childWidth) / 2.0;
            case RIGHT -> x + width - childWidth;
            default -> x;
        };
    }

    /// Returns the physical alignment for the current logical visual start edge.
    private HPos horizontalAlignment() {
        return switch (getSkinnable().getNodeOrientation()) {
            case RIGHT_TO_LEFT -> HPos.RIGHT;
            case LEFT_TO_RIGHT -> HPos.LEFT;
            default -> M3NodeLayout.logicalStartHorizontalAlignment(getSkinnable());
        };
    }

    /// Replaces the rendered row node owned by this skin.
    private void updateGraphic(@Nullable Node graphic) {
        Node oldGraphic = this.graphic;
        if (oldGraphic == graphic) {
            return;
        }

        if (oldGraphic != null) {
            getChildren().remove(oldGraphic);
        }
        this.graphic = graphic;
        if (graphic != null) {
            getChildren().add(graphic);
        }
        getSkinnable().requestLayout();
    }
}
