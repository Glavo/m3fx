// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the Material breakpoint ranges and the layout guidance carried by each range.
@NotNullByDefault
final class M3BreakpointTest {
    /// Verifies exact lower boundaries and values immediately below each transition.
    @Test
    void resolvesExactBreakpointBoundaries() {
        assertSame(M3Breakpoint.COMPACT, M3Breakpoint.forWidth(0.0));
        assertSame(M3Breakpoint.COMPACT, M3Breakpoint.forWidth(Math.nextDown(600.0)));
        assertSame(M3Breakpoint.MEDIUM, M3Breakpoint.forWidth(600.0));
        assertSame(M3Breakpoint.MEDIUM, M3Breakpoint.forWidth(Math.nextDown(840.0)));
        assertSame(M3Breakpoint.EXPANDED, M3Breakpoint.forWidth(840.0));
        assertSame(M3Breakpoint.EXPANDED, M3Breakpoint.forWidth(Math.nextDown(1_200.0)));
        assertSame(M3Breakpoint.LARGE, M3Breakpoint.forWidth(1_200.0));
        assertSame(M3Breakpoint.LARGE, M3Breakpoint.forWidth(Math.nextDown(1_600.0)));
        assertSame(M3Breakpoint.EXTRA_LARGE, M3Breakpoint.forWidth(1_600.0));
        assertSame(M3Breakpoint.EXTRA_LARGE, M3Breakpoint.forWidth(Double.MAX_VALUE));
    }

    /// Verifies that every breakpoint exposes a half-open range consistent with [M3Breakpoint#forWidth(double)].
    @Test
    void exposesConsistentHalfOpenRanges() {
        assertRange(M3Breakpoint.COMPACT, 0.0, 600.0);
        assertRange(M3Breakpoint.MEDIUM, 600.0, 840.0);
        assertRange(M3Breakpoint.EXPANDED, 840.0, 1_200.0);
        assertRange(M3Breakpoint.LARGE, 1_200.0, 1_600.0);

        assertEquals(1_600.0, M3Breakpoint.EXTRA_LARGE.getMinimumWidth());
        assertEquals(Double.POSITIVE_INFINITY, M3Breakpoint.EXTRA_LARGE.getMaximumWidth());
        assertTrue(M3Breakpoint.EXTRA_LARGE.contains(1_600.0));
        assertTrue(M3Breakpoint.EXTRA_LARGE.contains(Double.MAX_VALUE));
    }

    /// Verifies rejection of negative and non-finite available widths.
    @Test
    void rejectsInvalidAvailableWidths() {
        double[] invalidWidths = {-1.0, Math.nextDown(0.0), Double.NaN,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};

        for (double width : invalidWidths) {
            assertThrows(IllegalArgumentException.class, () -> M3Breakpoint.forWidth(width));
            for (M3Breakpoint breakpoint : M3Breakpoint.values()) {
                assertThrows(IllegalArgumentException.class, () -> breakpoint.contains(width));
            }
        }
    }

    /// Verifies the standard content margins, pane spacers, and recommended pane totals.
    @Test
    void exposesMaterialLayoutGuidance() {
        assertGuidance(M3Breakpoint.COMPACT, 16.0, 0.0, 1);
        assertGuidance(M3Breakpoint.MEDIUM, 24.0, 24.0, 1);
        assertGuidance(M3Breakpoint.EXPANDED, 24.0, 24.0, 2);
        assertGuidance(M3Breakpoint.LARGE, 24.0, 24.0, 2);
        assertGuidance(M3Breakpoint.EXTRA_LARGE, 24.0, 24.0, 2);
    }

    /// Verifies the permitted pane-count ranges independently from the recommended pane total.
    @Test
    void reportsSupportedPaneCounts() {
        assertSupportedPaneCounts(M3Breakpoint.COMPACT, false, false);
        assertSupportedPaneCounts(M3Breakpoint.MEDIUM, true, false);
        assertSupportedPaneCounts(M3Breakpoint.EXPANDED, true, false);
        assertSupportedPaneCounts(M3Breakpoint.LARGE, true, false);
        assertSupportedPaneCounts(M3Breakpoint.EXTRA_LARGE, true, true);
    }

    /// Verifies one finite half-open breakpoint range.
    ///
    /// @param breakpoint the breakpoint under test
    /// @param minimumWidth the expected inclusive lower bound
    /// @param maximumWidth the expected exclusive upper bound
    private static void assertRange(M3Breakpoint breakpoint, double minimumWidth, double maximumWidth) {
        assertEquals(minimumWidth, breakpoint.getMinimumWidth());
        assertEquals(maximumWidth, breakpoint.getMaximumWidth());
        assertTrue(breakpoint.contains(minimumWidth));
        assertTrue(breakpoint.contains(Math.nextDown(maximumWidth)));
        assertFalse(breakpoint.contains(maximumWidth));
        assertSame(breakpoint, M3Breakpoint.forWidth(minimumWidth));
        assertSame(breakpoint, M3Breakpoint.forWidth(Math.nextDown(maximumWidth)));
    }

    /// Verifies the layout metrics associated with one breakpoint.
    ///
    /// @param breakpoint the breakpoint under test
    /// @param contentMargin the expected leading and trailing content margin
    /// @param paneSpacing the expected adjacent-pane spacing
    /// @param recommendedPaneCount the expected recommended visible pane total
    private static void assertGuidance(
            M3Breakpoint breakpoint,
            double contentMargin,
            double paneSpacing,
            int recommendedPaneCount
    ) {
        assertEquals(contentMargin, breakpoint.getContentMargin());
        assertEquals(paneSpacing, breakpoint.getPaneSpacing());
        assertEquals(recommendedPaneCount, breakpoint.getRecommendedPaneCount());
    }

    /// Verifies support for pane totals one through three and rejection of out-of-range totals.
    ///
    /// @param breakpoint the breakpoint under test
    /// @param supportsTwo whether two panes are supported
    /// @param supportsThree whether three panes are supported
    private static void assertSupportedPaneCounts(
            M3Breakpoint breakpoint,
            boolean supportsTwo,
            boolean supportsThree
    ) {
        assertFalse(breakpoint.supportsPaneCount(-1));
        assertFalse(breakpoint.supportsPaneCount(0));
        assertTrue(breakpoint.supportsPaneCount(1));
        assertEquals(supportsTwo, breakpoint.supportsPaneCount(2));
        assertEquals(supportsThree, breakpoint.supportsPaneCount(3));
        assertFalse(breakpoint.supportsPaneCount(4));
    }
}
