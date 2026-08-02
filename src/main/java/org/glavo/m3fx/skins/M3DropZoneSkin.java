// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3DropZone;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3DropZone].
///
/// The skin retains only the public content node. Resizable content fills the padded control area up to its own
/// maximum size; non-resizable content is centered at its preferred size.
@NotNullByDefault
public final class M3DropZoneSkin extends SkinBase<M3DropZone> {
    /// Updates the rendered child after the public content property changes.
    private final InvalidationListener contentInvalidation = observable -> updateContent();

    /// Creates a drop-zone skin.
    ///
    /// @param control the drop zone controlled by this skin
    public M3DropZoneSkin(M3DropZone control) {
        super(control);
        control.contentProperty().addListener(contentInvalidation);
        updateContent();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3DropZone control = getSkinnable();
        control.contentProperty().removeListener(contentInvalidation);
        if (control.getSkin() == null || control.getSkin() == this) {
            getChildren().clear();
        }
        super.dispose();
    }

    /// Computes the minimum width from the current content.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Node content = getSkinnable().getContent();
        return leftInset + (content == null ? 0.0 : content.minWidth(height)) + rightInset;
    }

    /// Computes the minimum height from the current content.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Node content = getSkinnable().getContent();
        return topInset + (content == null ? 0.0 : content.minHeight(width)) + bottomInset;
    }

    /// Computes the preferred width from the current content.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Node content = getSkinnable().getContent();
        return leftInset + (content == null ? 0.0 : content.prefWidth(height)) + rightInset;
    }

    /// Computes the preferred height from the current content.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        @Nullable Node content = getSkinnable().getContent();
        return topInset + (content == null ? 0.0 : content.prefHeight(width)) + bottomInset;
    }

    /// Allows the drop zone to grow horizontally with its parent.
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

    /// Allows the drop zone to grow vertically with its parent.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return Double.MAX_VALUE;
    }

    /// Centers and sizes the current content in the padded control area.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        @Nullable Node content = getSkinnable().getContent();
        if (content != null && getChildren().contains(content)) {
            layoutInArea(content, x, y, width, height, 0.0, HPos.CENTER, VPos.CENTER);
        }
    }

    /// Replaces the rendered child with the current public content node.
    private void updateContent() {
        @Nullable Node content = getSkinnable().getContent();
        if (content == null) {
            getChildren().clear();
        } else {
            getChildren().setAll(content);
        }
        getSkinnable().requestLayout();
    }
}
