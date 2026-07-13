// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.SimpleStyleableDoubleProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CarouselLayout;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/// Internal carousel track that computes and animates Material keyline arrangements.
///
/// The track owns layout geometry but never changes application-owned minimum, preferred, or maximum child sizes.
/// One reusable transition interpolates width roles without allocating per-frame collections or path objects.
@NotNullByDefault
final class M3CarouselTrack extends HBox {
    /// Default maximum preferred viewport width used before a real viewport is available.
    static final double DEFAULT_MAX_PREF_WIDTH = 480.0;

    /// Default minimum small-item width.
    private static final double DEFAULT_SMALL_ITEM_MIN_WIDTH = 40.0;

    /// Default maximum small-item width.
    private static final double DEFAULT_SMALL_ITEM_MAX_WIDTH = 56.0;

    /// Default preferred maximum large-item width.
    private static final double DEFAULT_LARGE_ITEM_MAX_WIDTH = 320.0;

    /// Minimum useful layout item width.
    private static final double MIN_LAYOUT_ITEM_WIDTH = 1.0;

    /// The owning carousel.
    private final M3Carousel owner;

    /// Progress between previous and current focal arrangements.
    private final DoubleProperty selectionProgress = new SimpleDoubleProperty(this, "selectionProgress", 1.0) {
        /// Requests layout after an animation pulse.
        @Override
        protected void invalidated() {
            requestLayout();
        }
    };

    /// Reusable focal-width transition.
    private final M3DoubleTransition selectionTransition = new M3DoubleTransition(selectionProgress);

    /// Styleable minimum width of a contained small item.
    private final StyleableDoubleProperty smallItemMinWidth =
            new SimpleStyleableDoubleProperty(
                    StyleableProperties.SMALL_ITEM_MIN_WIDTH,
                    this,
                    "smallItemMinWidth",
                    DEFAULT_SMALL_ITEM_MIN_WIDTH
            ) {
                /// Validates this metric and requests layout.
                @Override
                protected void invalidated() {
                    validateMetric(get(), "smallItemMinWidth");
                    requestLayout();
                }
            };

    /// Styleable maximum width of a contained small item.
    private final StyleableDoubleProperty smallItemMaxWidth =
            new SimpleStyleableDoubleProperty(
                    StyleableProperties.SMALL_ITEM_MAX_WIDTH,
                    this,
                    "smallItemMaxWidth",
                    DEFAULT_SMALL_ITEM_MAX_WIDTH
            ) {
                /// Validates this metric and requests layout.
                @Override
                protected void invalidated() {
                    validateMetric(get(), "smallItemMaxWidth");
                    requestLayout();
                }
            };

    /// Styleable preferred maximum width of a contained large item.
    private final StyleableDoubleProperty largeItemMaxWidth =
            new SimpleStyleableDoubleProperty(
                    StyleableProperties.LARGE_ITEM_MAX_WIDTH,
                    this,
                    "largeItemMaxWidth",
                    DEFAULT_LARGE_ITEM_MAX_WIDTH
            ) {
                /// Validates this metric and requests layout.
                @Override
                protected void invalidated() {
                    validateMetric(get(), "largeItemMaxWidth");
                    requestLayout();
                }
            };

    /// Selected index at the beginning of the active transition.
    private int fromSelection;

    /// Selected index at the end of the active transition.
    private int toSelection;

    /// Width of the visible viewport.
    private double viewportWidth;

    /// Solved large-item width for the current pass.
    private double solvedLargeWidth;

    /// Solved medium-item width for the current pass.
    private double solvedMediumWidth;

    /// Solved small-item width for the current pass.
    private double solvedSmallWidth;

    /// Number of adjacent large items in multi-browse.
    private int solvedLargeCount = 1;

    /// Creates a track for one carousel.
    ///
    /// @param owner the owning carousel
    M3CarouselTrack(M3Carousel owner) {
        this.owner = owner;
        fromSelection = normalizedSelection(owner.getSelectedIndex());
        toSelection = fromSelection;
        setFillHeight(true);
    }

    /// Stops the active width transition.
    void dispose() {
        selectionTransition.stop();
    }

    /// Refreshes state after the public item collection changes.
    void refreshItems() {
        int selection = normalizedSelection(owner.getSelectedIndex());
        fromSelection = selection;
        toSelection = selection;
        selectionProgress.set(1.0);
        requestLayout();
    }

    /// Settles the previous arrangement after the public layout strategy changes.
    void refreshLayoutStrategy() {
        selectionTransition.stop();
        int selection = normalizedSelection(owner.getSelectedIndex());
        fromSelection = selection;
        toSelection = selection;
        selectionProgress.set(1.0);
        requestLayout();
    }

    /// Animates from one focal arrangement to another.
    ///
    /// @param oldSelection the previous selected index
    /// @param newSelection the new selected index
    void animateSelection(int oldSelection, int newSelection) {
        int normalizedNewSelection = normalizedSelection(newSelection);
        int normalizedOldSelection = oldSelection < 0
                ? normalizedNewSelection
                : normalizedSelection(oldSelection);
        fromSelection = normalizedOldSelection;
        toSelection = normalizedNewSelection;
        selectionTransition.stop();
        selectionProgress.set(0.0);

        M3CarouselLayout layout = owner.getCarouselLayout();
        if (layout.preservesAuthoredWidths()
                || layout == M3CarouselLayout.FULL_SCREEN
                || M3Animation.shouldReduceMotion(owner)
                || fromSelection == toSelection) {
            selectionProgress.set(1.0);
            return;
        }

        selectionTransition.configure(M3Animation.defaultSpatial(owner), 1.0);
        M3Animation.playFromStart(owner, selectionTransition);
    }

    /// Settles dynamic widths when reduced motion becomes active.
    void refreshMotionSettings() {
        M3Animation.finishRunningAnimationsIfDisabled(owner, selectionTransition);
        requestLayout();
    }

    /// Updates the viewport width used by arrangement solving.
    ///
    /// @param viewportWidth the visible viewport width
    void setViewportWidth(double viewportWidth) {
        double normalizedWidth = Math.max(0.0, viewportWidth);
        if (Math.abs(this.viewportWidth - normalizedWidth) < 0.001) {
            return;
        }
        this.viewportWidth = normalizedWidth;
        requestLayout();
    }

    /// Computes minimum width from the active arrangement.
    @Override
    protected double computeMinWidth(double height) {
        return computePrefWidth(height);
    }

    /// Computes preferred width from the active arrangement.
    @Override
    protected double computePrefWidth(double height) {
        prepareArrangement();
        return totalWidth(fromSelection, toSelection, selectionProgress.get(), height);
    }

    /// Computes minimum height from child content and track padding.
    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    /// Computes preferred height from child content and vertical padding.
    @Override
    protected double computePrefHeight(double width) {
        double height = 0.0;
        for (Node child : getChildren()) {
            if (child.isManaged()) {
                height = Math.max(height, childPrefHeight(child, -1.0));
            }
        }
        return verticalLeadingPadding() + height + verticalTrailingPadding();
    }
    /// Positions children using the interpolated focal arrangement.
    @Override
    protected void layoutChildren() {
        prepareArrangement();
        double progress = selectionProgress.get();
        double contentHeight = Math.max(
                0.0,
                getHeight() - verticalLeadingPadding() - verticalTrailingPadding()
        );
        double spacing = effectiveSpacing();
        boolean rightToLeft = M3NodeLayout.isRightToLeft(owner);
        double cursor = rightToLeft
                ? getWidth() - logicalLeadingPadding()
                : logicalLeadingPadding();

        for (int index = 0; index < getChildren().size(); index++) {
            Node child = getChildren().get(index);
            if (!child.isManaged()) {
                continue;
            }

            double width = interpolatedWidth(index, fromSelection, toSelection, progress, contentHeight);
            double height = Math.min(contentHeight, childPrefHeight(child, width));
            double y = verticalLeadingPadding() + (contentHeight - height) / 2.0;
            double x;
            if (rightToLeft) {
                cursor -= width;
                x = cursor;
                cursor -= spacing;
            } else {
                x = cursor;
                cursor += width + spacing;
            }
            layoutChild(child, x, y, width, height);
        }
    }

    /// Returns the final normalized scroll value for a selected item.
    ///
    /// @param index the selected item index
    /// @param visibleWidth the viewport width
    /// @return the normalized horizontal scroll value
    double targetHValue(int index, double visibleWidth) {
        prepareArrangement();
        int selection = normalizedSelection(index);
        double contentWidth = totalWidth(selection, selection, 1.0, getHeight());
        double maxPixel = Math.max(0.0, contentWidth - visibleWidth);
        if (maxPixel <= 0.0) {
            return 0.0;
        }

        double itemWidth = widthFor(index, selection, getHeight());
        double itemMinX = finalItemMinX(index, selection, contentWidth, getHeight());
        M3CarouselLayout layout = owner.getCarouselLayout();
        double targetPixel;
        if (layout.centersFocalItem() || layout.preservesAuthoredWidths()) {
            targetPixel = itemMinX - (visibleWidth - itemWidth) / 2.0;
        } else if (M3NodeLayout.isRightToLeft(owner)) {
            targetPixel = itemMinX + itemWidth - visibleWidth;
        } else {
            targetPixel = itemMinX;
        }
        return clamp(targetPixel / maxPixel);
    }

    /// Returns one item's final physical minimum X coordinate.
    private double finalItemMinX(int itemIndex, int selection, double contentWidth, double height) {
        double spacing = effectiveSpacing();
        if (!M3NodeLayout.isRightToLeft(owner)) {
            double x = logicalLeadingPadding();
            for (int index = 0; index < itemIndex; index++) {
                if (getChildren().get(index).isManaged()) {
                    x += widthFor(index, selection, height) + spacing;
                }
            }
            return x;
        }

        double x = contentWidth - logicalLeadingPadding();
        for (int index = 0; index <= itemIndex; index++) {
            if (getChildren().get(index).isManaged()) {
                x -= widthFor(index, selection, height);
                if (index < itemIndex) {
                    x -= spacing;
                }
            }
        }
        return x;
    }

    /// Solves width roles for the current layout and viewport.
    private void prepareArrangement() {
        M3CarouselLayout layout = owner.getCarouselLayout();
        double available = availableContentWidth();
        double targetLarge = preferredLargeWidth();
        solvedSmallWidth = clamp(
                targetLarge / 3.0,
                effectiveSmallItemMinWidth(),
                effectiveSmallItemMaxWidth()
        );
        solvedLargeWidth = Math.max(solvedSmallWidth, Math.min(targetLarge, available));
        solvedMediumWidth = (solvedLargeWidth + solvedSmallWidth) / 2.0;
        solvedLargeCount = 1;

        if (layout == M3CarouselLayout.MULTI_BROWSE) {
            solveMultiBrowseArrangement(available, targetLarge);
        } else if (layout == M3CarouselLayout.HERO) {
            solvedLargeWidth = Math.max(
                    MIN_LAYOUT_ITEM_WIDTH,
                    available - solvedSmallWidth - effectiveSpacing()
            );
            solvedMediumWidth = (solvedLargeWidth + solvedSmallWidth) / 2.0;
        } else if (layout == M3CarouselLayout.CENTER_ALIGNED_HERO) {
            solvedLargeWidth = Math.max(
                    MIN_LAYOUT_ITEM_WIDTH,
                    available - 2.0 * solvedSmallWidth - 2.0 * effectiveSpacing()
            );
            solvedMediumWidth = (solvedLargeWidth + solvedSmallWidth) / 2.0;
        }
    }

    /// Chooses the multi-browse arrangement requiring the least large-item adjustment.
    private void solveMultiBrowseArrangement(double available, double targetLarge) {
        int itemCount = managedChildCount();
        if (itemCount <= 1) {
            solvedLargeWidth = Math.max(MIN_LAYOUT_ITEM_WIDTH, available);
            solvedMediumWidth = solvedLargeWidth;
            solvedSmallWidth = solvedLargeWidth;
            solvedLargeCount = 1;
            return;
        }
        if (itemCount == 2) {
            solvedLargeWidth = Math.max(
                    MIN_LAYOUT_ITEM_WIDTH,
                    available - solvedSmallWidth - effectiveSpacing()
            );
            solvedMediumWidth = solvedSmallWidth;
            solvedLargeCount = 1;
            return;
        }

        double bestCost = Double.POSITIVE_INFINITY;
        double bestLarge = solvedLargeWidth;
        double bestMedium = solvedMediumWidth;
        int bestLargeCount = 1;
        int maximumLargeCount = Math.max(1, itemCount - 2);
        double spacing = effectiveSpacing();

        for (int largeCount = 1; largeCount <= maximumLargeCount; largeCount++) {
            int visibleSlots = largeCount + 2;
            double remaining = available - spacing * (visibleSlots - 1);
            double large = (remaining - 1.5 * solvedSmallWidth) / (largeCount + 0.5);
            double medium = (large + solvedSmallWidth) / 2.0;
            if (large < solvedSmallWidth || medium < solvedSmallWidth) {
                continue;
            }

            double overflowPenalty = large > effectiveLargeItemMaxWidth()
                    ? (large - effectiveLargeItemMaxWidth()) * 4.0
                    : 0.0;
            double cost = Math.abs(targetLarge - large) * largeCount + overflowPenalty;
            if (cost < bestCost) {
                bestCost = cost;
                bestLarge = large;
                bestMedium = medium;
                bestLargeCount = largeCount;
            }
        }

        solvedLargeWidth = Math.max(MIN_LAYOUT_ITEM_WIDTH, bestLarge);
        solvedMediumWidth = Math.max(solvedSmallWidth, bestMedium);
        solvedLargeCount = bestLargeCount;
    }

    /// Returns the interpolated width for one item.
    private double interpolatedWidth(
            int index,
            int startSelection,
            int targetSelection,
            double progress,
            double height
    ) {
        double startWidth = widthFor(index, startSelection, height);
        if (progress >= 1.0 || startSelection == targetSelection) {
            return widthFor(index, targetSelection, height);
        }
        double targetWidth = widthFor(index, targetSelection, height);
        return startWidth + (targetWidth - startWidth) * progress;
    }

    /// Returns one item's width in a final focal arrangement.
    private double widthFor(int index, int selection, double height) {
        M3CarouselLayout layout = owner.getCarouselLayout();
        if (layout.preservesAuthoredWidths()) {
            return childPrefWidth(getChildren().get(index), height);
        }
        if (layout == M3CarouselLayout.FULL_SCREEN) {
            return Math.max(MIN_LAYOUT_ITEM_WIDTH, viewportWidth);
        }
        if (M3Animation.shouldReduceMotion(owner)) {
            return solvedLargeWidth;
        }
        if (layout == M3CarouselLayout.MULTI_BROWSE) {
            return multiBrowseWidth(index, selection);
        }
        if (layout == M3CarouselLayout.HERO || layout == M3CarouselLayout.CENTER_ALIGNED_HERO) {
            return index == selection ? solvedLargeWidth : solvedSmallWidth;
        }
        return childPrefWidth(getChildren().get(index), height);
    }

    /// Returns one multi-browse item's large, medium, or small role width.
    private double multiBrowseWidth(int index, int selection) {
        int itemCount = getChildren().size();
        boolean reverseTail = selection > Math.max(0, itemCount - solvedLargeCount - 2);
        int distance = reverseTail ? selection - index : index - selection;
        if (distance >= 0 && distance < solvedLargeCount) {
            return solvedLargeWidth;
        }
        if (distance == solvedLargeCount) {
            return solvedMediumWidth;
        }
        return solvedSmallWidth;
    }
    /// Computes total track width for an interpolated arrangement.
    private double totalWidth(
            int startSelection,
            int targetSelection,
            double progress,
            double height
    ) {
        double width = logicalLeadingPadding() + logicalTrailingPadding();
        int managedChildren = 0;
        for (int index = 0; index < getChildren().size(); index++) {
            Node child = getChildren().get(index);
            if (child.isManaged()) {
                width += interpolatedWidth(index, startSelection, targetSelection, progress, height);
                managedChildren++;
            }
        }
        if (managedChildren > 1) {
            width += effectiveSpacing() * (managedChildren - 1);
        }
        return width;
    }

    /// Returns content width available inside Material keyline padding.
    private double availableContentWidth() {
        double effectiveViewportWidth = viewportWidth > 0.0 ? viewportWidth : DEFAULT_MAX_PREF_WIDTH;
        return Math.max(
                MIN_LAYOUT_ITEM_WIDTH,
                effectiveViewportWidth - logicalLeadingPadding() - logicalTrailingPadding()
        );
    }

    /// Returns preferred large width derived from authored widths and the component-token cap.
    private double preferredLargeWidth() {
        double preferredWidth = 0.0;
        for (Node child : getChildren()) {
            if (child.isManaged()) {
                preferredWidth = Math.max(preferredWidth, childPrefWidth(child, getHeight()));
            }
        }
        if (preferredWidth <= 0.0) {
            preferredWidth = effectiveLargeItemMaxWidth();
        }
        return Math.max(
                effectiveSmallItemMaxWidth(),
                Math.min(preferredWidth, effectiveLargeItemMaxWidth())
        );
    }

    /// Returns effective spacing for the selected Material layout.
    private double effectiveSpacing() {
        return owner.getCarouselLayout() == M3CarouselLayout.FULL_SCREEN
                ? Math.max(getSpacing(), getInsets().getLeft())
                : getSpacing();
    }

    /// Returns logical leading padding for the selected layout.
    private double logicalLeadingPadding() {
        return owner.getCarouselLayout() == M3CarouselLayout.FULL_SCREEN ? 0.0 : getInsets().getLeft();
    }

    /// Returns logical trailing padding for the selected layout.
    private double logicalTrailingPadding() {
        M3CarouselLayout layout = owner.getCarouselLayout();
        return layout == M3CarouselLayout.FULL_SCREEN || layout.preservesAuthoredWidths()
                ? 0.0
                : getInsets().getRight();
    }

    /// Returns top padding for the selected layout.
    private double verticalLeadingPadding() {
        return owner.getCarouselLayout() == M3CarouselLayout.FULL_SCREEN ? 0.0 : getInsets().getTop();
    }

    /// Returns bottom padding for the selected layout.
    private double verticalTrailingPadding() {
        return owner.getCarouselLayout() == M3CarouselLayout.FULL_SCREEN ? 0.0 : getInsets().getBottom();
    }

    /// Returns validated effective minimum small-item width.
    private double effectiveSmallItemMinWidth() {
        return Math.max(MIN_LAYOUT_ITEM_WIDTH, smallItemMinWidth.get());
    }

    /// Returns validated effective maximum small-item width.
    private double effectiveSmallItemMaxWidth() {
        return Math.max(effectiveSmallItemMinWidth(), smallItemMaxWidth.get());
    }

    /// Returns validated effective maximum large-item width.
    private double effectiveLargeItemMaxWidth() {
        return Math.max(effectiveSmallItemMaxWidth(), largeItemMaxWidth.get());
    }

    /// Returns the number of managed carousel items.
    private int managedChildCount() {
        int count = 0;
        for (Node child : getChildren()) {
            if (child.isManaged()) {
                count++;
            }
        }
        return count;
    }

    /// Returns a selected index valid for the current child list.
    private int normalizedSelection(int selection) {
        if (getChildren().isEmpty()) {
            return 0;
        }
        return Math.max(0, Math.min(selection, getChildren().size() - 1));
    }

    /// Returns a child's bounded preferred width without changing its sizing properties.
    private static double childPrefWidth(Node child, double height) {
        if (!(child instanceof Region region)) {
            return Math.max(MIN_LAYOUT_ITEM_WIDTH, child.getLayoutBounds().getWidth());
        }
        return Math.max(
                MIN_LAYOUT_ITEM_WIDTH,
                boundedSize(region.prefWidth(height), region.minWidth(height), region.maxWidth(height))
        );
    }

    /// Returns a child's bounded preferred height without changing its sizing properties.
    private static double childPrefHeight(Node child, double width) {
        if (!(child instanceof Region region)) {
            return Math.max(MIN_LAYOUT_ITEM_WIDTH, child.getLayoutBounds().getHeight());
        }
        return Math.max(
                MIN_LAYOUT_ITEM_WIDTH,
                boundedSize(region.prefHeight(width), region.minHeight(width), region.maxHeight(width))
        );
    }

    /// Clamps a preferred size between finite minimum and maximum constraints.
    private static double boundedSize(double preferred, double minimum, double maximum) {
        double finitePreferred = Double.isFinite(preferred) ? preferred : 0.0;
        double finiteMinimum = Double.isFinite(minimum) ? minimum : 0.0;
        double finiteMaximum = Double.isFinite(maximum) ? maximum : Double.MAX_VALUE;
        return Math.max(finiteMinimum, Math.min(finitePreferred, finiteMaximum));
    }

    /// Resizes and positions one child inside its assigned keyline slot.
    private static void layoutChild(Node child, double x, double y, double width, double height) {
        if (child.isResizable()) {
            child.resize(width, height);
            child.relocate(x, y);
            return;
        }
        double childWidth = child.getLayoutBounds().getWidth();
        double childHeight = child.getLayoutBounds().getHeight();
        child.relocate(x + (width - childWidth) / 2.0, y + (height - childHeight) / 2.0);
    }

    /// Validates a finite non-negative styleable layout metric.
    private static void validateMetric(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and not negative");
        }
    }

    /// Clamps one value to a finite range.
    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }

    /// Clamps one normalized value.
    private static double clamp(double value) {
        if (value <= 0.0) {
            return 0.0;
        }
        return Math.min(value, 1.0);
    }

    /// Returns CSS metadata for internal track metrics.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// CSS metadata for internal carousel track metrics.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for minimum contained small-item width.
        private static final CssMetaData<M3CarouselTrack, Number> SMALL_ITEM_MIN_WIDTH =
                sizeMetadata(
                        "-m3-carousel-small-item-min-width",
                        DEFAULT_SMALL_ITEM_MIN_WIDTH,
                        track -> track.smallItemMinWidth
                );

        /// CSS metadata for maximum contained small-item width.
        private static final CssMetaData<M3CarouselTrack, Number> SMALL_ITEM_MAX_WIDTH =
                sizeMetadata(
                        "-m3-carousel-small-item-max-width",
                        DEFAULT_SMALL_ITEM_MAX_WIDTH,
                        track -> track.smallItemMaxWidth
                );

        /// CSS metadata for preferred maximum contained large-item width.
        private static final CssMetaData<M3CarouselTrack, Number> LARGE_ITEM_MAX_WIDTH =
                sizeMetadata(
                        "-m3-carousel-large-item-max-width",
                        DEFAULT_LARGE_ITEM_MAX_WIDTH,
                        track -> track.largeItemMaxWidth
                );

        /// Complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(HBox.getClassCssMetaData());
            styleables.add(SMALL_ITEM_MIN_WIDTH);
            styleables.add(SMALL_ITEM_MAX_WIDTH);
            styleables.add(LARGE_ITEM_MAX_WIDTH);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Creates CSS metadata for one non-negative track size.
        private static CssMetaData<M3CarouselTrack, Number> sizeMetadata(
                String property,
                double initialValue,
                Function<M3CarouselTrack, StyleableDoubleProperty> accessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether CSS may assign the property.
                @Override
                public boolean isSettable(M3CarouselTrack track) {
                    return !accessor.apply(track).isBound();
                }

                /// Returns the writable styleable property.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3CarouselTrack track) {
                    return accessor.apply(track);
                }
            };
        }

        /// Prevents instantiation.
        private StyleableProperties() {
        }
    }
}