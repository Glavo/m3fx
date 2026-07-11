// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Surface;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Surface].
@NotNullByDefault
public final class M3SurfaceSkin extends SkinBase<M3Surface> {
    /// The internal content container.
    private final StackPane container = new StackPane();

    /// Updates rendered content when the surface content list changes.
    private final ListChangeListener<Node> contentListener = change -> updateContent();

    /// Creates a surface skin.
    ///
    /// @param control the surface controlled by this skin
    public M3SurfaceSkin(M3Surface control) {
        super(control);
        container.setManaged(false);
        getChildren().setAll(container);
        control.getContent().addListener(contentListener);
        updateContent();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().getContent().removeListener(contentListener);
        container.getChildren().clear();
        getChildren().remove(container);
        super.dispose();
    }

    /// Computes preferred width from current content and control insets.
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

    /// Computes preferred height from current content and control insets.
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

    /// Lays out content inside the padded surface area.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Mirrors the public content list into the internal container.
    private void updateContent() {
        container.getChildren().setAll(getSkinnable().getContent());
        getSkinnable().requestLayout();
    }
}
