// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.internal.M3IconPaints;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Icon].
///
/// The skin renders the glyph with the control's resolved icon font and centers its visual bounds independently of
/// the text baseline. Glyph content and font changes remain observable through bindings, while the icon stylesheet
/// resolves the fill from the control's Material color-role style class.
@NotNullByDefault
public final class M3IconSkin extends SkinBase<M3Icon> {
    /// The layout line-box multiplier used by [M3Icon].
    private static final double ICON_LINE_BOX_SCALE = 1.5;

    /// The rendered glyph node.
    private final Text glyphNode = new Text();

    /// The semantic paint selected by the icon variant and CSS.
    private final ObjectProperty<@Nullable Paint> semanticPaint;

    /// The paint supplied while this icon occupies a containing component's graphic slot.
    private final ObjectProperty<@Nullable Paint> inheritedPaint;

    /// Updates the glyph whenever one of its paint channels changes.
    private final InvalidationListener paintInvalidation = observable -> updateGlyphPaint();

    /// Creates an icon skin.
    ///
    /// @param control the icon controlled by this skin
    /// @throws IllegalArgumentException if `control` is `null`
    public M3IconSkin(M3Icon control) {
        super(control);
        semanticPaint = M3IconPaints.semanticPaintProperty(control);
        inheritedPaint = M3IconPaints.inheritedPaintProperty(control);
        initializeGlyph(control);
        semanticPaint.addListener(paintInvalidation);
        inheritedPaint.addListener(paintInvalidation);
        control.tintProperty().addListener(paintInvalidation);
        getChildren().setAll(glyphNode);
        updateGlyphPaint();
    }

    /// Unbinds glyph properties before disposing this skin.
    @Override
    public void dispose() {
        M3Icon control = getSkinnable();
        semanticPaint.removeListener(paintInvalidation);
        inheritedPaint.removeListener(paintInvalidation);
        control.tintProperty().removeListener(paintInvalidation);
        glyphNode.textProperty().unbind();
        glyphNode.fontProperty().unbind();
        getChildren().remove(glyphNode);
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

    /// Centers the glyph by its rendered local bounds instead of a text baseline.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        Bounds bounds = glyphNode.getLayoutBounds();
        double targetX = x + (width - bounds.getWidth()) / 2.0;
        double targetY = y + (height - bounds.getHeight()) / 2.0;
        glyphNode.relocate(snapPositionX(targetX), snapPositionY(targetY));
        updateGlyphPaint();
    }

    /// Initializes glyph bindings to the skinnable icon state.
    ///
    /// @param control the icon supplying glyph and font state
    private void initializeGlyph(M3Icon control) {
        glyphNode.getStyleClass().addAll("text", "m3-icon-glyph");
        glyphNode.setManaged(false);
        glyphNode.setMouseTransparent(true);
        glyphNode.textProperty().bind(control.glyphProperty());
        glyphNode.fontProperty().bind(control.iconFontProperty());
    }

    /// Returns the current icon line-box size.
    ///
    /// @return the icon line-box width and height
    private double iconLineBoxSize() {
        return Math.ceil(getSkinnable().getIconSize() * ICON_LINE_BOX_SCALE);
    }

    /// Resolves and applies the highest-precedence icon paint.
    private void updateGlyphPaint() {
        @Nullable Paint tint = getSkinnable().getTint();
        @Nullable Paint inherited = inheritedPaint.get();
        @Nullable Paint semantic = semanticPaint.get();
        Paint paint = tint != null ? tint : inherited != null ? inherited : semantic != null ? semantic : Color.BLACK;
        if (!paint.equals(glyphNode.getFill())) {
            glyphNode.setFill(paint);
        }
    }
}
