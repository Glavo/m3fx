// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 breakpoint from the available layout width.
///
/// Breakpoints describe the space assigned to a layout rather than a device category. The width is expressed in
/// JavaFX logical pixels, which serve the same role as density-independent pixels for ordinary JavaFX layout. A
/// freely resizable desktop window can therefore cross several breakpoints during its lifetime.
///
/// Each constant includes the standard leading and trailing content margin, pane spacer, and recommended visible
/// pane count for that width range. The pane count is guidance rather than a hard maximum; use
/// [#supportsPaneCount(int)] to test the pane totals permitted by the Material specification.
///
/// See [Material Design breakpoints](https://m3.material.io/foundations/layout/breakpoints/overview).
@NotNullByDefault
public enum M3Breakpoint {
    /// Widths below 600 logical pixels, with one visible pane and 16-pixel margins.
    COMPACT(0.0, 600.0, 16.0, 0.0, 1, 1, 1),

    /// Widths in `[600, 840)` logical pixels, normally with one pane and 24-pixel margins.
    MEDIUM(600.0, 840.0, 24.0, 24.0, 1, 1, 2),

    /// Widths in `[840, 1200)` logical pixels, normally with two panes.
    EXPANDED(840.0, 1_200.0, 24.0, 24.0, 2, 1, 2),

    /// Widths in `[1200, 1600)` logical pixels, normally with two panes.
    LARGE(1_200.0, 1_600.0, 24.0, 24.0, 2, 1, 2),

    /// Widths of at least 1600 logical pixels, normally with two panes and optionally with a third pane.
    EXTRA_LARGE(1_600.0, Double.POSITIVE_INFINITY, 24.0, 24.0, 2, 1, 3);

    /// The inclusive lower bound of this breakpoint in logical pixels.
    private final double minimumWidth;

    /// The exclusive upper bound of this breakpoint in logical pixels.
    private final double maximumWidth;

    /// The standard leading and trailing content margin in logical pixels.
    private final double contentMargin;

    /// The standard spacer between adjacent panes in logical pixels.
    private final double paneSpacing;

    /// The pane total recommended by Material for this breakpoint.
    private final int recommendedPaneCount;

    /// The minimum pane total permitted by Material for this breakpoint.
    private final int minimumPaneCount;

    /// The maximum pane total permitted by Material for this breakpoint.
    private final int maximumPaneCount;

    /// Creates a breakpoint descriptor.
    M3Breakpoint(
            double minimumWidth,
            double maximumWidth,
            double contentMargin,
            double paneSpacing,
            int recommendedPaneCount,
            int minimumPaneCount,
            int maximumPaneCount
    ) {
        this.minimumWidth = minimumWidth;
        this.maximumWidth = maximumWidth;
        this.contentMargin = contentMargin;
        this.paneSpacing = paneSpacing;
        this.recommendedPaneCount = recommendedPaneCount;
        this.minimumPaneCount = minimumPaneCount;
        this.maximumPaneCount = maximumPaneCount;
    }

    /// Returns the breakpoint containing the supplied available width.
    ///
    /// Boundary values belong to the wider breakpoint: 600 is [#MEDIUM], 840 is [#EXPANDED], 1200 is [#LARGE],
    /// and 1600 is [#EXTRA_LARGE].
    ///
    /// @param width the available width in logical pixels
    /// @return the breakpoint containing `width`
    /// @throws IllegalArgumentException if `width` is negative or not finite
    public static M3Breakpoint forWidth(double width) {
        if (!Double.isFinite(width) || width < 0.0) {
            throw new IllegalArgumentException("width must be finite and non-negative");
        }
        if (width < MEDIUM.minimumWidth) {
            return COMPACT;
        }
        if (width < EXPANDED.minimumWidth) {
            return MEDIUM;
        }
        if (width < LARGE.minimumWidth) {
            return EXPANDED;
        }
        if (width < EXTRA_LARGE.minimumWidth) {
            return LARGE;
        }
        return EXTRA_LARGE;
    }

    /// Returns the inclusive lower width bound of this breakpoint.
    ///
    /// @return the inclusive minimum width in logical pixels
    public double getMinimumWidth() {
        return minimumWidth;
    }

    /// Returns the exclusive upper width bound of this breakpoint.
    ///
    /// The extra-large breakpoint has an upper bound of positive infinity.
    ///
    /// @return the exclusive maximum width in logical pixels
    public double getMaximumWidth() {
        return maximumWidth;
    }

    /// Returns whether this breakpoint contains the supplied available width.
    ///
    /// @param width the available width in logical pixels
    /// @return `true` if `width` is within this breakpoint's half-open range
    /// @throws IllegalArgumentException if `width` is negative or not finite
    public boolean contains(double width) {
        if (!Double.isFinite(width) || width < 0.0) {
            throw new IllegalArgumentException("width must be finite and non-negative");
        }
        return width >= minimumWidth && width < maximumWidth;
    }

    /// Returns the standard leading and trailing content margin for this breakpoint.
    ///
    /// @return the content margin in logical pixels
    public double getContentMargin() {
        return contentMargin;
    }

    /// Returns the standard spacer between adjacent panes for this breakpoint.
    ///
    /// Compact layouts use one pane and therefore return zero. Other breakpoints return 24 logical pixels.
    ///
    /// @return the pane spacer in logical pixels
    public double getPaneSpacing() {
        return paneSpacing;
    }

    /// Returns the visible pane total recommended by Material for this breakpoint.
    ///
    /// @return the recommended pane count
    public int getRecommendedPaneCount() {
        return recommendedPaneCount;
    }

    /// Returns whether Material permits the supplied visible pane total at this breakpoint.
    ///
    /// This method describes the general breakpoint guidance. A particular canonical layout may impose a narrower
    /// choice according to its content density and navigation model.
    ///
    /// @param paneCount the proposed number of visible panes
    /// @return `true` if `paneCount` is permitted by the general breakpoint guidance
    public boolean supportsPaneCount(int paneCount) {
        return paneCount >= minimumPaneCount && paneCount <= maximumPaneCount;
    }
}
