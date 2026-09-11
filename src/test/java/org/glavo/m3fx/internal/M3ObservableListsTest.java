// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies bulk mutation, traversal, and notification invariants of control-owned lists.
@NotNullByDefault
final class M3ObservableListsTest {
    /// Keeps transformed views and bound contents synchronized across every structural mutation shape.
    @Test
    void transformationsAndContentBindingsFollowMutations() {
        ObservableList<Integer> source = M3ObservableLists.nonNullElementList("element");
        source.addAll(5, 3, 2, 4, 1, 2);
        FilteredList<Integer> filtered = new FilteredList<>(source, value -> value % 2 == 0);
        SortedList<Integer> sorted = new SortedList<>(source, Comparator.naturalOrder());
        SortedList<Integer> sortedFiltered = new SortedList<>(filtered, Comparator.reverseOrder());
        ObservableList<Integer> bound = FXCollections.observableArrayList();
        Bindings.bindContent(bound, source);
        try {
            @Unmodifiable List<Consumer<ObservableList<Integer>>> mutations = List.of(
                    list -> list.add(0, 6),
                    list -> list.set(1, 8),
                    list -> list.remove(2),
                    list -> list.addAll(2, List.of(7, 10)),
                    list -> list.replaceAll(value -> value + 1),
                    list -> list.setAll(8, 6, 4, 2, 6, 9),
                    list -> list.sort(Comparator.naturalOrder()),
                    list -> list.removeAll(6),
                    list -> list.retainAll(2, 8, 9),
                    list -> list.removeIf(value -> value % 2 != 0),
                    list -> list.subList(0, 1).clear(),
                    list -> list.subList(0, 0).addAll(List.of(10, 12)),
                    list -> list.subList(0, 2).subList(1, 2).addAll(List.of(14, 16)),
                    ObservableList::clear);
            for (Consumer<ObservableList<Integer>> mutation : mutations) {
                mutation.accept(source);
                assertEquals(source, bound);
                assertEquals(source.stream().filter(value -> value % 2 == 0).toList(), filtered);
                assertEquals(source.stream().sorted().toList(), sorted);
                assertEquals(source.stream().filter(value -> value % 2 == 0).sorted(Comparator.reverseOrder()).toList(), sortedFiltered);
            }
        } finally {
            Bindings.unbindContent(bound, source);
        }
    }

    /// Delivers a nested removal after every observer has received the original addition snapshot.
    @Test
    void observerRemovalRetainsBothNotificationsInOrder() {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("element");
        List<String> notifications = new ArrayList<>();
        list.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    list.clear();
                    assertEquals(List.of("a", "b"), change.getAddedSubList());
                }
            }
        });
        list.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                notifications.add(change.getRemoved() + " -> " + change.getAddedSubList());
            }
        });
        list.addAll("a", "b");
        assertTrue(list.isEmpty());
        assertEquals(List.of("[] -> [a, b]", "[a, b] -> []"), notifications);
    }

    /// Preserves successive replacements requested by an observer without corrupting their added snapshots.
    @Test
    void observerReplacementsRetainMutationOrder() {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("element");
        List<String> notifications = new ArrayList<>();
        list.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.getAddedSubList().equals(List.of("a"))) {
                    list.setAll("b");
                    list.setAll("c");
                }
            }
        });
        list.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                notifications.add(change.getRemoved() + " -> " + change.getAddedSubList());
            }
        });
        list.add("a");
        assertEquals(List.of("c"), list);
        assertEquals(List.of("[] -> [a]", "[a] -> [b]", "[b] -> [c]"), notifications);
    }

    /// Reports disjoint removals in sequential index space with one notification.
    @Test
    void bulkRemovalCanBeReplayedFromItsSegments() {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("element");
        list.addAll("a", "b", "c", "d", "e");
        List<String> mirror = new ArrayList<>(list);
        AtomicInteger notifications = new AtomicInteger();
        list.addListener((ListChangeListener<String>) change -> {
            notifications.incrementAndGet();
            while (change.next()) {
                assertEquals(mirror.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()), change.getRemoved());
                mirror.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                mirror.addAll(change.getFrom(), change.getAddedSubList());
            }
        });
        assertTrue(list.removeAll("b", "d"));
        assertEquals(List.of("a", "c", "e"), list);
        assertEquals(list, mirror);
        assertEquals(1, notifications.get());
    }

    /// Preserves list contents when a removal predicate fails after matching an earlier element.
    @Test
    void failedRemovalPredicateLeavesListUnchanged() {
        ObservableList<String> list = M3ObservableLists.nonNullElementList("element");
        list.addAll("a", "b");
        assertThrows(IllegalStateException.class, () -> list.removeIf(value -> {
            if (value.equals("b")) {
                throw new IllegalStateException("Removal failed");
            }
            return true;
        }));
        assertEquals(List.of("a", "b"), list);
    }

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
