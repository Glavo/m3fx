// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies navigation identity and page boundaries independently of window focus.
@NotNullByDefault
final class M3SelectionNavigationTest {
    /// Uses the exact current node when equal but distinct nodes share a navigation list.
    @Test
    void adjacentNavigationUsesNodeIdentity() {
        EqualRegion first = new EqualRegion();
        EqualRegion second = new EqualRegion();
        EqualRegion third = new EqualRegion();
        @Unmodifiable List<EqualRegion> children = List.of(first, second, third);

        assertSame(third, M3SelectionNavigation.next(children, second, EqualRegion.class));
        assertSame(first, M3SelectionNavigation.previous(children, second, EqualRegion.class));
        assertSame(first, M3SelectionNavigation.next(children, third, EqualRegion.class));
        assertSame(third, M3SelectionNavigation.previous(children, first, EqualRegion.class));
    }

    /// Advances type-ahead search beyond the actual current node before wrapping.
    @Test
    void typeAheadUsesNodeIdentity() {
        EqualRegion first = new EqualRegion();
        EqualRegion second = new EqualRegion();
        EqualRegion third = new EqualRegion();
        @Unmodifiable List<EqualRegion> children = List.of(first, second, third);

        assertSame(third, M3SelectionNavigation.typeAheadTarget(
                children, second, EqualRegion.class, "match", node -> "Match"));
    }

    /// Does not accept an equal node outside the list as a selection or focus anchor.
    @Test
    void foreignEqualNodesDoNotBecomeAnchors() {
        EqualRegion first = new EqualRegion();
        EqualRegion last = new EqualRegion();
        EqualRegion foreign = new EqualRegion();
        @Unmodifiable List<EqualRegion> children = List.of(first, last);
        Region owner = new Region();

        assertNull(M3SelectionNavigation.focusAnchor(children, foreign, EqualRegion.class));
        assertSame(first, M3SelectionNavigation.focusTarget(children, foreign, EqualRegion.class));
        assertSame(first, M3SelectionNavigation.page(owner, children, foreign, EqualRegion.class, true));
        assertSame(last, M3SelectionNavigation.page(owner, children, foreign, EqualRegion.class, false));
        assertSame(first, M3SelectionNavigation.next(children, foreign, EqualRegion.class));
        assertSame(last, M3SelectionNavigation.previous(children, foreign, EqualRegion.class));
    }

    /// Keeps horizontal keys physical until a node in the list establishes an RTL anchor.
    @Test
    void foreignNodesDoNotReverseUnanchoredRtlNavigation() {
        EqualRegion first = new EqualRegion();
        EqualRegion last = new EqualRegion();
        EqualRegion foreign = new EqualRegion();
        @Unmodifiable List<EqualRegion> children = List.of(first, last);
        List<EqualRegion> selections = new ArrayList<>();

        assertTrue(M3SelectionNavigation.handleKeySelection(
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.RIGHT, false, false, false, false),
                null, children, foreign, EqualRegion.class, true, false, true, selections::add));
        assertTrue(M3SelectionNavigation.handleKeySelection(
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.LEFT, false, false, false, false),
                null, children, foreign, EqualRegion.class, true, false, true, selections::add));

        assertSame(first, selections.get(0));
        assertSame(last, selections.get(1));
    }

    /// Counts reachable nodes in either direction and stops at list edges without wrapping.
    @Test
    void pagesSkipUnreachableNodesAndClampAtEdges() {
        @Unmodifiable List<EqualRegion> children = List.of(
                new EqualRegion(), new EqualRegion(), new EqualRegion(), new EqualRegion(),
                new EqualRegion(), new EqualRegion(), new EqualRegion(), new EqualRegion());
        children.get(1).setDisable(true);
        children.get(3).setVisible(false);
        Region owner = new Region();

        assertSame(children.get(7), M3SelectionNavigation.page(
                owner, children, children.get(0), EqualRegion.class, true));
        assertSame(children.get(0), M3SelectionNavigation.page(
                owner, children, children.get(7), EqualRegion.class, false));
        assertSame(children.get(7), M3SelectionNavigation.page(
                owner, children, children.get(7), EqualRegion.class, true));
        assertSame(children.get(0), M3SelectionNavigation.page(
                owner, children, children.get(0), EqualRegion.class, false));
    }

    /// Restarts navigation from an edge when the current member becomes unreachable.
    @Test
    void pagesRecoverFromUnreachableAnchors() {
        Region first = new Region();
        Region disabled = new Region();
        Region last = new Region();
        disabled.setDisable(true);
        @Unmodifiable List<Region> children = List.of(first, disabled, last);
        Region owner = new Region();

        assertSame(first, M3SelectionNavigation.page(owner, children, disabled, Region.class, true));
        assertSame(last, M3SelectionNavigation.page(owner, children, disabled, Region.class, false));
        first.setVisible(false);
        last.setVisible(false);
        assertNull(M3SelectionNavigation.page(owner, children, disabled, Region.class, true));
    }

    /// Enforces the declared type precondition even when no children need inspection.
    @Test
    void emptyListsStillRejectNullTypes() {
        assertThrows(NullPointerException.class, () -> M3SelectionNavigation.first(List.of(), null));
        assertThrows(NullPointerException.class, () -> M3SelectionNavigation.last(List.of(), null));
        assertThrows(NullPointerException.class, () -> M3SelectionNavigation.next(List.of(), null, null));
        assertThrows(NullPointerException.class, () -> M3SelectionNavigation.previous(List.of(), null, null));
    }

    /// A node whose value equality deliberately differs from its scene-graph identity.
    @NotNullByDefault
    private static final class EqualRegion extends Region {
        /// Treats instances of this test node type as equal values.
        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof EqualRegion;
        }

        /// Returns the shared hash code required by value equality.
        @Override
        public int hashCode() {
            return 0;
        }
    }
}
