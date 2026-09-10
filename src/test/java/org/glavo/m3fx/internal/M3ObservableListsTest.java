// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies bulk mutation, traversal, and notification invariants of control-owned lists.
@NotNullByDefault
final class M3ObservableListsTest {
    /// Verifies that all bulk insertion entry points invalidate existing traversal state.
    @Test
    void bulkAdditionsInvalidateIteratorsAndSubLists() {
        assertInvalidatesTraversal(list -> list.addAll(List.of("d", "e")));
        assertInvalidatesTraversal(list -> list.addAll(0, List.of("d", "e")));
        assertInvalidatesTraversal(list -> list.addAll("d", "e"));
    }

    /// Verifies that replacement invalidates traversal even when the list retains its size.
    @Test
    void bulkReplacementsInvalidateIteratorsAndSubLists() {
        assertInvalidatesTraversal(list -> list.setAll(List.of("d", "e")));
        assertInvalidatesTraversal(list -> list.setAll("d", "e", "f"));
        assertInvalidatesTraversal(list -> list.setAll(List.of()));
        assertInvalidatesTraversal(list -> list.replaceAll(String::toUpperCase));
    }

    /// Verifies that a reordered list cannot be modified through an iterator over the old order.
    @Test
    void sortingInvalidatesIteratorsAndSubLists() {
        assertInvalidatesTraversal(list -> list.sort(java.util.Comparator.reverseOrder()));
    }

    /// Verifies that a sublist cannot delete an unrelated element after an insertion shifts its range.
    @Test
    void staleSubListCannotRemoveShiftedElement() {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("item");
        list.setAll("a", "b", "c");
        List<String> view = list.subList(1, 3);

        list.addAll(0, List.of("d", "e"));

        assertThrows(ConcurrentModificationException.class, () -> view.remove(0));
        assertEquals(List.of("d", "e", "a", "b", "c"), list);
    }

    /// Verifies that listeners see invalidated old traversal state and the complete replacement.
    @Test
    void traversalIsInvalidatedBeforeNotifyingListeners() {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("item");
        list.setAll("a", "b");
        Iterator<String> iterator = list.iterator();
        List<Boolean> invalidated = new ArrayList<>();
        list.addListener((ListChangeListener<String>) change -> {
            try {
                iterator.next();
                invalidated.add(false);
            } catch (ConcurrentModificationException expected) {
                invalidated.add(true);
            }
            assertEquals(List.of("c", "d", "e"), list);
        });

        list.setAll("c", "d", "e");

        assertEquals(List.of(true), invalidated);
    }

    /// Verifies that a bulk insertion through nested sublists preserves those views' ranges.
    @Test
    void subListBulkInsertionKeepsParticipatingViewsUsable() {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("item");
        list.setAll("a", "b", "c");
        List<String> outer = list.subList(1, 3);
        List<String> inner = outer.subList(0, 1);

        inner.addAll(0, List.of("d", "e"));

        assertEquals(List.of("a", "d", "e", "b", "c"), list);
        assertEquals(List.of("d", "e", "b", "c"), outer);
        assertEquals(List.of("d", "e", "b"), inner);
        inner.remove(1);
        assertEquals(List.of("a", "d", "b", "c"), list);
    }

    /// Verifies that rejected mutations preserve contents, traversal state, and notifications.
    @Test
    void invalidBulkMutationsLeaveTraversalUsable() {
        ObservableList<String> list = M3ObservableLists.distinctElementList("item", String::equalsIgnoreCase);
        list.setAll("a", "b", "c");
        Iterator<String> iterator = list.iterator();
        List<String> view = list.subList(1, 3);
        List<String> notifications = new ArrayList<>();
        list.addListener((ListChangeListener<String>) change -> notifications.add("changed"));

        assertThrows(IllegalArgumentException.class, () -> list.addAll("d", "A"));
        assertThrows(IllegalArgumentException.class, () -> list.setAll(List.of("d", "D")));
        assertThrows(IllegalArgumentException.class, () -> list.replaceAll(value -> "x"));
        assertThrows(NullPointerException.class, () -> list.addAll("d", null));

        assertEquals(List.of("a", "b", "c"), list);
        assertEquals("a", iterator.next());
        assertEquals(List.of("b", "c"), view);
        assertTrue(notifications.isEmpty());
    }

    /// Verifies that no-op mutations neither invalidate traversal nor publish changes.
    @Test
    void unchangedBulkMutationsLeaveTraversalUsable() {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("item");
        list.setAll("a", "b", "c");
        Iterator<String> iterator = list.iterator();
        List<String> view = list.subList(1, 3);
        List<String> notifications = new ArrayList<>();
        list.addListener((ListChangeListener<String>) change -> notifications.add("changed"));

        assertFalse(list.addAll(List.of()));
        assertFalse(list.setAll(list));
        list.replaceAll(value -> value);
        list.sort(null);

        assertEquals("a", iterator.next());
        assertEquals(List.of("b", "c"), view);
        assertTrue(notifications.isEmpty());
    }

    /// Verifies that each changed bulk operation invalidates old readers before they can mutate the list.
    private static void assertInvalidatesTraversal(Consumer<ObservableList<String>> mutation) {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("item");
        list.setAll("a", "b", "c");
        Iterator<String> iterator = list.iterator();
        assertEquals("a", iterator.next());
        List<String> view = list.subList(1, 3);

        mutation.accept(list);
        List<String> result = new ArrayList<>(list);

        assertThrows(ConcurrentModificationException.class, iterator::remove);
        assertThrows(ConcurrentModificationException.class, () -> view.remove(0));
        assertEquals(result, list);
    }
}
