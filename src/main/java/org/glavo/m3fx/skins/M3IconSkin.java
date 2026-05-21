// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Bounds;
import javafx.scene.control.SkinBase;
import javafx.scene.text.Text;
import org.glavo.m3fx.controls.M3Icon;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Icon].
@NotNullByDefault
public final class M3IconSkin extends SkinBase<M3Icon> {
    /// The layout line box multiplier used by [M3Icon].
    private static final double ICON_LINE_BOX_SCALE = 1.25;

    /// The rendered glyph node.
    private final Text glyph = new Text();

    /// Creates an icon skin.
    public M3IconSkin(M3Icon control) {
        super(control);
        initializeGlyph(control);
        getChildren().add(glyph);
    }

    /// Unbinds glyph properties before disposing this skin.
    @Override
    public void dispose() {
        glyph.textProperty().unbind();
        glyph.fontProperty().unbind();
        glyph.fillProperty().unbind();
        glyph.underlineProperty().unbind();
        super.dispose();
    }

    /// Computes the minimum width from the resolved icon line box.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + iconLineBoxSize() + rightInset;
    }

    /// Computes the minimum height from the resolved icon line box.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + iconLineBoxSize() + bottomInset;
    }

    /// Computes the preferred width from the resolved icon line box.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + iconLineBoxSize() + rightInset;
    }

    /// Computes the preferred height from the resolved icon line box.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + iconLineBoxSize() + bottomInset;
    }

    /// Centers the glyph by its rendered local bounds instead of JavaFX labeled baselines.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        Bounds bounds = glyph.getLayoutBounds();
        double targetX = x + (width - bounds.getWidth()) / 2.0;
        double targetY = y + (height - bounds.getHeight()) / 2.0;
        glyph.relocate(snapPositionX(targetX), snapPositionY(targetY));
    }

    /// Initializes glyph bindings to the skinnable icon state.
    private void initializeGlyph(M3Icon control) {
        glyph.getStyleClass().add("text");
        glyph.setManaged(false);
        glyph.setMouseTransparent(true);
        glyph.textProperty().bind(control.textProperty());
        glyph.fontProperty().bind(control.fontProperty());
        glyph.fillProperty().bind(control.textFillProperty());
        glyph.underlineProperty().bind(control.underlineProperty());
    }

    /// Returns the current icon line box size.
    private double iconLineBoxSize() {
        return Math.ceil(getSkinnable().getIconSize() * ICON_LINE_BOX_SCALE);
    }
}
