// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNullByDefault;

/// Internal mask container that preserves authored carousel content geometry while exposing a dynamic keyline width.
///
/// Material carousel items are measured at their focal width and revealed through a smaller rounded mask as they
/// move through medium and small keylines. The container owns only the mask; it does not replace an application
/// clip installed on the content node.
@NotNullByDefault
final class M3CarouselItemSlot extends Region {
    /// The internal style class used for rendered carousel item masks.
    static final String STYLE_CLASS = "m3-carousel-item-container";

    /// The pseudo-class applied while this slot occupies a Material small-item keyline.
    private static final PseudoClass SMALL_ITEM_PSEUDO_CLASS = PseudoClass.getPseudoClass("small-item");

    /// The application-owned item rendered inside this slot.
    private final Node content;

    /// The reusable rounded rectangle that clips item content to the current keyline width.
    private final Rectangle mask = new Rectangle();

    /// The width assigned to content before the keyline mask is applied.
    private double contentWidth;

    /// The current Material item corner radius.
    private double shapeRadius;

    /// Whether this slot currently occupies a Material small-item keyline.
    private boolean smallItem;

    /// Creates a mask slot for one application-owned item.
    ///
    /// @param content the carousel item content
    M3CarouselItemSlot(Node content) {
        this.content = content;
        getStyleClass().add(STYLE_CLASS);
        setClip(mask);
        setPickOnBounds(false);
        getChildren().add(content);
    }

    /// Returns the application-owned item inside this slot.
    ///
    /// @return the carousel item content
    Node content() {
        return content;
    }

    /// Returns whether this item participates in keyline layout.
    ///
    /// @return `true` when the content is visible and managed
    boolean participatesInLayout() {
        return content.isVisible() && content.isManaged();
    }

    /// Updates the focal content width and mask shape without allocating new scene-graph nodes.
    ///
    /// @param contentWidth the width at which content is laid out before masking
    /// @param shapeRadius the Material mask corner radius
    /// @param smallItem whether the slot currently occupies a small-item keyline
    void configure(double contentWidth, double shapeRadius, boolean smallItem) {
        double normalizedContentWidth = Math.max(0.0, contentWidth);
        double normalizedShapeRadius = Math.max(0.0, shapeRadius);
        if (Double.compare(this.contentWidth, normalizedContentWidth) != 0
                || Double.compare(this.shapeRadius, normalizedShapeRadius) != 0) {
            this.contentWidth = normalizedContentWidth;
            this.shapeRadius = normalizedShapeRadius;
            requestLayout();
        }
        if (this.smallItem != smallItem) {
            this.smallItem = smallItem;
            pseudoClassStateChanged(SMALL_ITEM_PSEUDO_CLASS, smallItem);
        }
    }

    /// Detaches the application-owned content before this slot is discarded.
    void dispose() {
        setClip(null);
        getChildren().clear();
    }

    /// Computes minimum width from the authored item.
    @Override
    protected double computeMinWidth(double height) {
        if (!(content instanceof Region region)) {
            return content.getLayoutBounds().getWidth();
        }
        return boundedSize(region.minWidth(height));
    }

    /// Computes preferred width from the authored item.
    @Override
    protected double computePrefWidth(double height) {
        if (!(content instanceof Region region)) {
            return content.getLayoutBounds().getWidth();
        }
        return boundedSize(region.prefWidth(height), region.minWidth(height), region.maxWidth(height));
    }

    /// Computes minimum height from the authored item.
    @Override
    protected double computeMinHeight(double width) {
        if (!(content instanceof Region region)) {
            return content.getLayoutBounds().getHeight();
        }
        return boundedSize(region.minHeight(width));
    }

    /// Computes preferred height from the authored item at its unmasked focal width.
    @Override
    protected double computePrefHeight(double width) {
        if (!(content instanceof Region region)) {
            return content.getLayoutBounds().getHeight();
        }
        double effectiveWidth = contentWidth > 0.0 ? contentWidth : width;
        return boundedSize(
                region.prefHeight(effectiveWidth),
                region.minHeight(effectiveWidth),
                region.maxHeight(effectiveWidth)
        );
    }

    /// Lays out focal-width content behind the current rounded keyline mask.
    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        double radius = Math.min(shapeRadius, Math.min(width, height) / 2.0);
        mask.setWidth(width);
        mask.setHeight(height);
        mask.setArcWidth(radius * 2.0);
        mask.setArcHeight(radius * 2.0);

        double resolvedContentWidth = Math.max(width, contentWidth);
        if (content.isResizable()) {
            content.resize(resolvedContentWidth, height);
            content.relocate((width - resolvedContentWidth) / 2.0, 0.0);
            return;
        }

        double childWidth = content.getLayoutBounds().getWidth();
        double childHeight = content.getLayoutBounds().getHeight();
        content.relocate((width - childWidth) / 2.0, (height - childHeight) / 2.0);
    }

    /// Returns a finite non-negative size.
    ///
    /// @param value the candidate size
    /// @return the normalized size
    private static double boundedSize(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }

    /// Clamps a preferred size between finite minimum and maximum constraints.
    ///
    /// @param preferred the preferred size
    /// @param minimum the minimum size
    /// @param maximum the maximum size
    /// @return the bounded size
    private static double boundedSize(double preferred, double minimum, double maximum) {
        double finitePreferred = boundedSize(preferred);
        double finiteMinimum = boundedSize(minimum);
        double finiteMaximum = Double.isFinite(maximum) ? Math.max(0.0, maximum) : Double.MAX_VALUE;
        return Math.max(finiteMinimum, Math.min(finitePreferred, finiteMaximum));
    }
}
