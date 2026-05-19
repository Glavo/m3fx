// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3ListViewCell;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ListViewCell].
@NotNullByDefault
public final class M3ListViewCellSkin<T> extends SkinBase<M3ListViewCell<T>> {
    /// The currently installed rendered row node.
    private @Nullable Node graphic;

    /// Updates the rendered row when the cell graphic changes.
    private final ChangeListener<@Nullable Node> graphicListener =
            (observable, oldValue, newValue) -> updateGraphic(newValue);

    /// Creates a virtualized list view cell skin.
    public M3ListViewCellSkin(M3ListViewCell<T> control) {
        super(control);
        control.graphicProperty().addListener(graphicListener);
        updateGraphic(control.getGraphic());
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().graphicProperty().removeListener(graphicListener);
        getChildren().clear();
        graphic = null;
        super.dispose();
    }

    /// Computes the minimum width from the rendered row.
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
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        Node row = graphic;
        if (row != null) {
            layoutInArea(row, x, y, width, height, 0.0, HPos.LEFT, VPos.CENTER);
        }
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
