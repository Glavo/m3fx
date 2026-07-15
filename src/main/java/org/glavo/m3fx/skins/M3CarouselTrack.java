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

    /// Default Material carousel item corner radius.
    private static final double DEFAULT_ITEM_SHAPE = 28.0;

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

    /// Styleable Material corner radius applied to item masks.
    private final StyleableDoubleProperty itemShape =
            new SimpleStyleableDoubleProperty(
                    StyleableProperties.ITEM_SHAPE,
                    this,
                    "itemShape",
                    DEFAULT_ITEM_SHAPE
            ) {
                /// Validates this metric and requests layout.
                @Override
                protected void invalidated() {
                    validateMetric(get(), "itemShape");
                    requestLayout();
                }
            };

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

    /// Whether the current width interpolation follows direct viewport movement.
    private boolean trackingViewport;

    /// Width of the visible viewport.
    private double viewportWidth;

    /// Solved large-item width for the current pass.
    private double solvedLargeWidth;

    /// Solved medium-item width for the current pass.
    private double solvedMediumWidth;

    /// Solved small-item width for the current pass.
    private double solvedSmallWidth;

    /// Number of adjacent large items in the current arrangement.
    private int solvedLargeCount = 1;

    /// Number of medium items in the current arrangement.
    private int solvedMediumCount;

    /// Number of small items in the current arrangement.
    private int solvedSmallCount;

    /// Reused mapping from public item indices to visible managed layout ordinals.
    private int[] layoutOrdinals = new int[0];

    /// Reused mapping from visible managed layout ordinals to public item indices.
    private int[] layoutIndices = new int[0];

    /// Number of items participating in the current layout pass.
    private int layoutItemCount;

    /// Layout ordinal represented by the cached arrangement.
    private int arrangedLayoutOrdinal = -1;

    /// Visible item count represented by the cached arrangement.
    private int arrangedItemCount = -1;

    /// Available width represented by the cached arrangement.
    private double arrangedAvailable = Double.NaN;

    /// Preferred large-item width represented by the cached arrangement.
    private double arrangedTargetLarge = Double.NaN;

    /// Item spacing represented by the cached arrangement.
    private double arrangedSpacing = Double.NaN;

    /// Minimum small-item width represented by the cached arrangement.
    private double arrangedMinimumSmall = Double.NaN;

    /// Maximum small-item width represented by the cached arrangement.
    private double arrangedMaximumSmall = Double.NaN;

    /// Creates a track for one carousel.
    ///
    /// @param owner the owning carousel
    M3CarouselTrack(M3Carousel owner) {
        this.owner = owner;
        fromSelection = normalizedSelection(owner.getSelectedIndex());
        toSelection = fromSelection;
        setFillHeight(true);
    }

    /// Stops the active width transition and releases internal item masks.
    void dispose() {
        selectionTransition.stop();
        disposeItemSlots();
        getChildren().clear();
    }

    /// Rebuilds internal mask slots after the public item collection changes.
    ///
    /// Item collection edits are not an animation hot path, so rebuilding slots avoids persistent mapping objects
    /// while preserving application-owned content clips and sizing properties.
    ///
    /// @param items the current application-owned carousel items
    void setItems(List<Node> items) {
        disposeItemSlots();
        getChildren().clear();
        if (layoutOrdinals.length < items.size()) {
            layoutOrdinals = new int[items.size()];
            layoutIndices = new int[items.size()];
        }
        for (Node item : items) {
            getChildren().add(new M3CarouselItemSlot(item));
        }
        refreshItems();
    }

    /// Releases every internal item mask before its content is reparented or discarded.
    private void disposeItemSlots() {
        for (Node child : getChildren()) {
            ((M3CarouselItemSlot) child).dispose();
        }
    }

    /// Refreshes state after the public item collection changes.
    private void refreshItems() {
        int selection = normalizedSelection(owner.getSelectedIndex());
        fromSelection = selection;
        toSelection = selection;
        trackingViewport = false;
        selectionProgress.set(1.0);
        requestLayout();
    }

    /// Settles the previous arrangement after the public layout strategy changes.
    void refreshLayoutStrategy() {
        selectionTransition.stop();
        int selection = normalizedSelection(owner.getSelectedIndex());
        fromSelection = selection;
        toSelection = selection;
        trackingViewport = false;
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
        selectionTransition.stop();
        M3CarouselLayout layout = owner.getCarouselLayout();
        boolean animate = !layout.preservesAuthoredWidths()
                && layout != M3CarouselLayout.FULL_SCREEN
                && !M3Animation.shouldReduceMotion(owner);
        if (trackingViewport && animate) {
            trackingViewport = false;
            if (normalizedNewSelection == toSelection && fromSelection != toSelection) {
                selectionTransition.configure(M3Animation.defaultSpatial(owner), 1.0);
                M3Animation.playFromStart(owner, selectionTransition);
                return;
            }
            if (normalizedNewSelection == fromSelection && fromSelection != toSelection) {
                int previousFrom = fromSelection;
                fromSelection = toSelection;
                toSelection = previousFrom;
                selectionProgress.set(1.0 - selectionProgress.get());
                selectionTransition.configure(M3Animation.defaultSpatial(owner), 1.0);
                M3Animation.playFromStart(owner, selectionTransition);
                return;
            }
        }

        trackingViewport = false;
        fromSelection = normalizedOldSelection;
        toSelection = normalizedNewSelection;
        selectionProgress.set(0.0);
        if (!animate || fromSelection == toSelection) {
            selectionProgress.set(1.0);
            return;
        }

        selectionTransition.configure(M3Animation.defaultSpatial(owner), 1.0);
        M3Animation.playFromStart(owner, selectionTransition);
    }

    /// Follows direct viewport movement by interpolating between neighboring snapping arrangements.
    ///
    /// @param hvalue the normalized viewport position
    /// @param visibleWidth the visible viewport width
    void followViewportPosition(double hvalue, double visibleWidth) {
        M3CarouselLayout layout = owner.getCarouselLayout();
        if (!layout.usesSnapScrolling()
                || layout == M3CarouselLayout.FULL_SCREEN
                || M3Animation.shouldReduceMotion(owner)
                || visibleWidth <= 0.0) {
            return;
        }

        prepareArrangement();
        if (layoutItemCount <= 1) {
            return;
        }
        double position = clamp(hvalue);
        int insertionOrdinal = targetInsertionOrdinal(position, visibleWidth);
        int firstOrdinal = Math.max(0, Math.min(layoutItemCount - 1, insertionOrdinal - 1));
        int secondOrdinal = Math.max(0, Math.min(layoutItemCount - 1, insertionOrdinal));
        int firstIndex = layoutIndices[firstOrdinal];
        int secondIndex = layoutIndices[secondOrdinal];
        double firstPosition = targetHValue(firstIndex, visibleWidth);
        double secondPosition = targetHValue(secondIndex, visibleWidth);
        int lowerIndex;
        int upperIndex;
        double lowerPosition;
        double upperPosition;
        if (firstPosition <= secondPosition) {
            lowerIndex = firstIndex;
            lowerPosition = firstPosition;
            upperIndex = secondIndex;
            upperPosition = secondPosition;
        } else {
            lowerIndex = secondIndex;
            lowerPosition = secondPosition;
            upperIndex = firstIndex;
            upperPosition = firstPosition;
        }

        selectionTransition.stop();
        trackingViewport = true;
        fromSelection = lowerIndex;
        toSelection = upperIndex;
        double range = upperPosition - lowerPosition;
        double progress = range <= 0.000001 ? 1.0 : clamp((position - lowerPosition) / range);
        if (Double.compare(selectionProgress.get(), progress) != 0) {
            selectionProgress.set(progress);
        } else {
            requestLayout();
        }
    }
    /// Returns the insertion ordinal surrounding one normalized viewport target.
    private int targetInsertionOrdinal(double position, double visibleWidth) {
        double firstTarget = targetHValue(layoutIndices[0], visibleWidth);
        double lastTarget = targetHValue(layoutIndices[layoutItemCount - 1], visibleWidth);
        boolean ascending = firstTarget <= lastTarget;
        int low = 0;
        int high = layoutItemCount;
        while (low < high) {
            int middle = (low + high) >>> 1;
            double target = targetHValue(layoutIndices[middle], visibleWidth);
            boolean beforeInsertion = ascending ? target < position : target > position;
            if (beforeInsertion) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
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
            M3CarouselItemSlot slot = (M3CarouselItemSlot) child;
            if (slot.participatesInLayout()) {
                height = Math.max(height, childPrefHeight(slot, -1.0));
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
            M3CarouselItemSlot slot = slotAt(index);
            if (!slot.participatesInLayout()) {
                continue;
            }

            double width = interpolatedWidth(index, fromSelection, toSelection, progress, contentHeight);
            double focalContentWidth = focalContentWidth(index, fromSelection, toSelection, progress, contentHeight);
            slot.configure(focalContentWidth, itemShape.get(), occupiesSmallKeyline(width));
            double height = Math.min(contentHeight, childPrefHeight(slot, focalContentWidth));
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
            layoutChild(slot, x, y, width, height);
            slot.layout();
        }
    }

    /// Returns whether an interpolated item width occupies the solved small-item role.
    ///
    /// The midpoint between the small role and the next larger role prevents content from changing at every pulse
    /// while an item moves between keylines. Layouts that preserve authored widths never expose a small role.
    ///
    /// @param width the current interpolated item width
    /// @return `true` when the item should present compact small-item content
    private boolean occupiesSmallKeyline(double width) {
        M3CarouselLayout layout = owner.getCarouselLayout();
        if (layout.preservesAuthoredWidths() || solvedSmallCount == 0) {
            return false;
        }
        double nextRoleWidth = solvedMediumCount > 0 ? solvedMediumWidth : solvedLargeWidth;
        return width <= (solvedSmallWidth + nextRoleWidth) / 2.0;
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
                if (slotAt(index).participatesInLayout()) {
                    x += widthFor(index, selection, height) + spacing;
                }
            }
            return x;
        }

        double x = contentWidth - logicalLeadingPadding();
        for (int index = 0; index <= itemIndex; index++) {
            if (slotAt(index).participatesInLayout()) {
                x -= widthFor(index, selection, height);
                if (index < itemIndex) {
                    x -= spacing;
                }
            }
        }
        return x;
    }

    /// Solves width roles for the current layout and viewport using the Material arrangement cost model.
    private void prepareArrangement() {
        prepareLayoutOrdinals();
        M3CarouselLayout layout = owner.getCarouselLayout();
        double available = availableContentWidth();
        double targetLarge = preferredLargeWidth();
        double spacing = effectiveSpacing();
        double minimumSmall = effectiveSmallItemMinWidth();
        double maximumSmall = effectiveSmallItemMaxWidth();
        int layoutOrdinal = layout.ordinal();
        if (arrangedLayoutOrdinal == layoutOrdinal
                && arrangedItemCount == layoutItemCount
                && Double.compare(arrangedAvailable, available) == 0
                && Double.compare(arrangedTargetLarge, targetLarge) == 0
                && Double.compare(arrangedSpacing, spacing) == 0
                && Double.compare(arrangedMinimumSmall, minimumSmall) == 0
                && Double.compare(arrangedMaximumSmall, maximumSmall) == 0) {
            return;
        }
        arrangedLayoutOrdinal = layoutOrdinal;
        arrangedItemCount = layoutItemCount;
        arrangedAvailable = available;
        arrangedTargetLarge = targetLarge;
        arrangedSpacing = spacing;
        arrangedMinimumSmall = minimumSmall;
        arrangedMaximumSmall = maximumSmall;

        solvedSmallWidth = clamp(targetLarge / 3.0, minimumSmall, maximumSmall);
        solvedLargeWidth = Math.max(solvedSmallWidth, Math.min(targetLarge, available));
        solvedMediumWidth = (solvedLargeWidth + solvedSmallWidth) / 2.0;
        solvedLargeCount = 1;
        solvedMediumCount = 0;
        solvedSmallCount = 0;

        if (layout == M3CarouselLayout.MULTI_BROWSE) {
            applyArrangement(solveMultiBrowseArrangement(
                    available,
                    spacing,
                    targetLarge,
                    minimumSmall,
                    maximumSmall,
                    layoutItemCount
            ));
        } else if (layout == M3CarouselLayout.HERO
                || layout == M3CarouselLayout.CENTER_ALIGNED_HERO) {
            applyArrangement(solveHeroArrangement(
                    available,
                    spacing,
                    targetLarge,
                    minimumSmall,
                    maximumSmall,
                    layoutItemCount,
                    layout == M3CarouselLayout.CENTER_ALIGNED_HERO
            ));
        } else if (layout == M3CarouselLayout.FULL_SCREEN) {
            solvedLargeWidth = Math.max(MIN_LAYOUT_ITEM_WIDTH, viewportWidth);
            solvedMediumWidth = solvedLargeWidth;
            solvedSmallWidth = solvedLargeWidth;
        }
    }

    /// Rebuilds the reused public-index to layout-ordinal map for visible managed items.
    private void prepareLayoutOrdinals() {
        int childCount = getChildren().size();
        if (layoutOrdinals.length < childCount) {
            layoutOrdinals = new int[childCount];
            layoutIndices = new int[childCount];
        }
        int ordinal = 0;
        for (int index = 0; index < childCount; index++) {
            if (slotAt(index).participatesInLayout()) {
                layoutOrdinals[index] = ordinal;
                layoutIndices[ordinal++] = index;
            } else {
                layoutOrdinals[index] = -1;
            }
        }
        layoutItemCount = ordinal;
    }

    /// Applies one solved Material arrangement to reusable track fields.
    ///
    /// @param arrangement the solved arrangement
    private void applyArrangement(Arrangement arrangement) {
        solvedSmallWidth = arrangement.smallSize();
        solvedMediumWidth = arrangement.mediumSize();
        solvedLargeWidth = arrangement.largeSize();
        solvedSmallCount = arrangement.smallCount();
        solvedMediumCount = arrangement.mediumCount();
        solvedLargeCount = arrangement.largeCount();
    }

    /// Solves the Material multi-browse arrangement with the least preferred-large-size adjustment.
    private static Arrangement solveMultiBrowseArrangement(
            double available,
            double spacing,
            double targetLarge,
            double minimumSmall,
            double maximumSmall,
            int itemCount
    ) {
        int smallCount = available < minimumSmall * 2.0 ? 0 : 1;
        double targetSmall = clamp(targetLarge / 3.0, minimumSmall, maximumSmall);
        double targetMedium = (targetLarge + targetSmall) / 2.0;
        double minimumAvailableLargeSpace = available - targetMedium - maximumSmall * smallCount;
        int minimumLargeCount = Math.max(1, (int) Math.floor(minimumAvailableLargeSpace / targetLarge));
        int maximumLargeCount = Math.max(minimumLargeCount, (int) Math.ceil(available / targetLarge));
        Arrangement arrangement = findLowestCostArrangement(
                available,
                spacing,
                targetSmall,
                minimumSmall,
                maximumSmall,
                smallCount,
                targetMedium,
                1,
                0,
                targetLarge,
                minimumLargeCount,
                maximumLargeCount
        );

        if (arrangement.itemCount() <= itemCount) {
            return arrangement;
        }

        int surplus = arrangement.itemCount() - itemCount;
        int adjustedSmallCount = arrangement.smallCount();
        int adjustedMediumCount = arrangement.mediumCount();
        while (surplus-- > 0) {
            if (adjustedSmallCount > 0) {
                adjustedSmallCount--;
            } else if (adjustedMediumCount > 1) {
                adjustedMediumCount--;
            }
        }
        return findLowestCostArrangement(
                available,
                spacing,
                targetSmall,
                minimumSmall,
                maximumSmall,
                adjustedSmallCount,
                targetMedium,
                adjustedMediumCount,
                adjustedMediumCount,
                targetLarge,
                minimumLargeCount,
                maximumLargeCount
        );
    }

    /// Solves a start- or center-aligned Material hero arrangement.
    private static Arrangement solveHeroArrangement(
            double available,
            double spacing,
            double targetLarge,
            double minimumSmall,
            double maximumSmall,
            int itemCount,
            boolean centered
    ) {
        int smallCount = itemCount <= 1 ? 0 : centered && itemCount >= 3 ? 2 : 1;
        double targetSmall = clamp(targetLarge / 3.0, minimumSmall, maximumSmall);
        double fullScreenThreshold = minimumSmall * smallCount + minimumSmall * 1.25;
        if (available < fullScreenThreshold) {
            smallCount = 0;
        }
        double minimumAvailableLargeSpace = available - minimumSmall * smallCount;
        int minimumLargeCount = Math.max(1, (int) Math.floor(minimumAvailableLargeSpace / targetLarge));
        int maximumLargeCount = Math.max(minimumLargeCount, (int) Math.ceil(available / targetLarge));
        return findLowestCostArrangement(
                available,
                spacing,
                targetSmall,
                minimumSmall,
                maximumSmall,
                smallCount,
                0.0,
                0,
                0,
                targetLarge,
                minimumLargeCount,
                maximumLargeCount
        );
    }

    /// Searches valid arrangement permutations in Material priority order.
    private static Arrangement findLowestCostArrangement(
            double available,
            double spacing,
            double targetSmall,
            double minimumSmall,
            double maximumSmall,
            int smallCount,
            double targetMedium,
            int firstMediumCount,
            int lastMediumCount,
            double targetLarge,
            int minimumLargeCount,
            int maximumLargeCount
    ) {
        Arrangement best = Arrangement.fallback(targetLarge);
        double bestCost = Double.POSITIVE_INFINITY;
        int priority = 1;
        for (int largeCount = maximumLargeCount; largeCount >= minimumLargeCount; largeCount--) {
            for (int mediumCount = firstMediumCount; mediumCount >= lastMediumCount; mediumCount--) {
                Arrangement candidate = fitArrangement(
                        priority++,
                        available,
                        spacing,
                        targetSmall,
                        minimumSmall,
                        maximumSmall,
                        smallCount,
                        targetMedium,
                        mediumCount,
                        targetLarge,
                        largeCount
                );
                double cost = candidate.cost(targetLarge);
                if (cost < bestCost) {
                    best = candidate;
                    bestCost = cost;
                    if (cost == 0.0) {
                        return best;
                    }
                }
            }
        }
        return best;
    }

    /// Fits one arrangement by adjusting small, medium, and finally large item widths.
    private static Arrangement fitArrangement(
            int priority,
            double available,
            double spacing,
            double targetSmall,
            double minimumSmall,
            double maximumSmall,
            int smallCount,
            double targetMedium,
            int mediumCount,
            double targetLarge,
            int largeCount
    ) {
        int totalItemCount = largeCount + mediumCount + smallCount;
        double availableWithoutSpacing = available - Math.max(0, totalItemCount - 1) * spacing;
        double small = clamp(targetSmall, minimumSmall, maximumSmall);
        double total = targetLarge * largeCount + targetMedium * mediumCount + small * smallCount;
        double delta = availableWithoutSpacing - total;
        if (smallCount > 0 && delta > 0.0) {
            small += Math.min(delta / smallCount, maximumSmall - small);
        } else if (smallCount > 0 && delta < 0.0) {
            small += Math.max(delta / smallCount, minimumSmall - small);
        }
        if (smallCount == 0) {
            small = 0.0;
        }

        double large = (availableWithoutSpacing - (smallCount + mediumCount / 2.0) * small)
                / (largeCount + mediumCount / 2.0);
        double medium = (large + small) / 2.0;
        if (mediumCount > 0 && Double.compare(large, targetLarge) != 0) {
            double targetAdjustment = (targetLarge - large) * largeCount;
            double availableMediumFlex = medium * 0.1 * mediumCount;
            double distributed = Math.min(Math.abs(targetAdjustment), availableMediumFlex);
            if (targetAdjustment > 0.0) {
                medium -= distributed / mediumCount;
                large += distributed / largeCount;
            } else {
                medium += distributed / mediumCount;
                large -= distributed / largeCount;
            }
        }
        return new Arrangement(priority, small, smallCount, medium, mediumCount, large, largeCount);
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
            return childPrefWidth(slotAt(index), height);
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
        if (layout == M3CarouselLayout.HERO) {
            return startAlignedHeroWidth(index, selection);
        }
        if (layout == M3CarouselLayout.CENTER_ALIGNED_HERO) {
            return centeredHeroWidth(index, selection);
        }
        return childPrefWidth(slotAt(index), height);
    }

    /// Returns focal-width content geometry before a smaller keyline mask is applied.
    private double focalContentWidth(
            int index,
            int startSelection,
            int targetSelection,
            double progress,
            double height
    ) {
        M3CarouselLayout layout = owner.getCarouselLayout();
        if (layout.preservesAuthoredWidths() || layout == M3CarouselLayout.FULL_SCREEN) {
            return interpolatedWidth(index, startSelection, targetSelection, progress, height);
        }
        return solvedLargeWidth;
    }

    /// Returns one multi-browse item's large, medium, or small role width.
    private double multiBrowseWidth(int index, int selection) {
        int indexOrdinal = layoutOrdinal(index);
        int selectionOrdinal = layoutOrdinal(selection);
        int visibleSlots = solvedLargeCount + solvedMediumCount + solvedSmallCount;
        boolean reverseTail = selectionOrdinal > Math.max(0, layoutItemCount - visibleSlots);
        int distance = reverseTail
                ? selectionOrdinal - indexOrdinal
                : indexOrdinal - selectionOrdinal;
        if (distance >= 0 && distance < solvedLargeCount) {
            return solvedLargeWidth;
        }
        if (distance >= solvedLargeCount && distance < solvedLargeCount + solvedMediumCount) {
            return solvedMediumWidth;
        }
        return solvedSmallWidth;
    }

    /// Returns one start-aligned hero item's large or small role width.
    private double startAlignedHeroWidth(int index, int selection) {
        int indexOrdinal = layoutOrdinal(index);
        int selectionOrdinal = layoutOrdinal(selection);
        int visibleSlots = solvedLargeCount + solvedSmallCount;
        boolean reverseTail = selectionOrdinal > Math.max(0, layoutItemCount - visibleSlots);
        int distance = reverseTail
                ? selectionOrdinal - indexOrdinal
                : indexOrdinal - selectionOrdinal;
        return distance >= 0 && distance < solvedLargeCount ? solvedLargeWidth : solvedSmallWidth;
    }

    /// Returns one center-aligned hero item's large or small role width.
    private double centeredHeroWidth(int index, int selection) {
        int indexOrdinal = layoutOrdinal(index);
        int selectionOrdinal = layoutOrdinal(selection);
        int leadingLargeCount = (solvedLargeCount - 1) / 2;
        int firstLargeOrdinal = selectionOrdinal - leadingLargeCount;
        int lastLargeOrdinal = firstLargeOrdinal + solvedLargeCount - 1;
        if (firstLargeOrdinal < 0) {
            lastLargeOrdinal -= firstLargeOrdinal;
            firstLargeOrdinal = 0;
        }
        if (lastLargeOrdinal >= layoutItemCount) {
            int overflow = lastLargeOrdinal - layoutItemCount + 1;
            firstLargeOrdinal = Math.max(0, firstLargeOrdinal - overflow);
            lastLargeOrdinal = layoutItemCount - 1;
        }
        return indexOrdinal >= firstLargeOrdinal && indexOrdinal <= lastLargeOrdinal
                ? solvedLargeWidth
                : solvedSmallWidth;
    }

    /// Returns the visible managed layout ordinal for one public item index.
    private int layoutOrdinal(int index) {
        if (index < 0 || index >= getChildren().size()) {
            return 0;
        }
        int ordinal = layoutOrdinals[index];
        return ordinal >= 0 ? ordinal : 0;
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
            M3CarouselItemSlot slot = slotAt(index);
            if (slot.participatesInLayout()) {
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
            M3CarouselItemSlot slot = (M3CarouselItemSlot) child;
            if (slot.participatesInLayout()) {
                preferredWidth = Math.max(preferredWidth, childPrefWidth(slot, getHeight()));
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
                ? getSpacing() * 2.0
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

    /// Returns a selected index valid for the current child list.
    private int normalizedSelection(int selection) {
        if (getChildren().isEmpty()) {
            return 0;
        }
        return Math.max(0, Math.min(selection, getChildren().size() - 1));
    }

    /// Returns the internal item slot at one public item index.
    ///
    /// @param index the public item index
    /// @return the corresponding internal mask slot
    private M3CarouselItemSlot slotAt(int index) {
        return (M3CarouselItemSlot) getChildren().get(index);
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

    /// One fitted combination of Material small, medium, and large item roles.
    ///
    /// @param priority the source permutation priority
    /// @param smallSize the fitted small-item width
    /// @param smallCount the number of small items
    /// @param mediumSize the fitted medium-item width
    /// @param mediumCount the number of medium items
    /// @param largeSize the fitted large-item width
    /// @param largeCount the number of large items
    @NotNullByDefault
    private record Arrangement(
            int priority,
            double smallSize,
            int smallCount,
            double mediumSize,
            int mediumCount,
            double largeSize,
            int largeCount
    ) {
        /// Creates a one-large-item fallback arrangement.
        ///
        /// @param largeSize the fallback large-item width
        /// @return the fallback arrangement
        private static Arrangement fallback(double largeSize) {
            return new Arrangement(1, 0.0, 0, 0.0, 0, largeSize, 1);
        }

        /// Returns the total number of keyline roles in this arrangement.
        ///
        /// @return the role count
        private int itemCount() {
            return smallCount + mediumCount + largeCount;
        }

        /// Returns the Material desirability cost for this arrangement.
        ///
        /// @param targetLargeSize the preferred large-item width
        /// @return the weighted adjustment cost, or positive infinity when invalid
        private double cost(double targetLargeSize) {
            if (!isValid()) {
                return Double.POSITIVE_INFINITY;
            }
            return Math.abs(targetLargeSize - largeSize) * priority;
        }

        /// Returns whether item widths retain the required strict role ordering.
        ///
        /// @return `true` when this arrangement is geometrically valid
        private boolean isValid() {
            if (!Double.isFinite(largeSize) || largeSize <= 0.0) {
                return false;
            }
            if (largeCount > 0 && smallCount > 0 && mediumCount > 0) {
                return largeSize > mediumSize && mediumSize > smallSize;
            }
            if (largeCount > 0 && smallCount > 0) {
                return largeSize > smallSize;
            }
            return true;
        }
    }
    /// CSS metadata for internal carousel track metrics.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the Material carousel item corner radius.
        private static final CssMetaData<M3CarouselTrack, Number> ITEM_SHAPE =
                sizeMetadata(
                        "-m3-carousel-item-mask-shape",
                        DEFAULT_ITEM_SHAPE,
                        track -> track.itemShape
                );

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
            styleables.add(ITEM_SHAPE);
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