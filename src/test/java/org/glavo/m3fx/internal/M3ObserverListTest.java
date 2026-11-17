// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies identity-based subscription storage and reentrant notification semantics.
@NotNullByDefault
final class M3ObserverListTest {
    /// Equal application objects remain independently removable subscriptions.
    @Test
    void removesByIdentityAndReusesEmptyStorage() {
        M3ObserverList<String> observers = new M3ObserverList<>();
        String first = new String("equal");
        String second = new String("equal");
        assertTrue(observers.isEmpty());
        assertThrows(NullPointerException.class, () -> observers.add(null));
        observers.add(first);
        observers.add(second);
        assertTrue(observers.remove(second));
        assertFalse(observers.remove(second));
        dispatch(observers, observer -> assertSame(first, observer));
        assertTrue(observers.remove(first));
        assertTrue(observers.isEmpty());
        observers.add(second);
        dispatch(observers, observer -> assertSame(second, observer));
    }

    /// Removing pending callbacks does not shift slots; additions wait for another dispatch.
    @Test
    void mutationDoesNotSkipOrRepeatCallbacks() {
        M3ObserverList<String> observers = new M3ObserverList<>();
        observers.add("first");
        observers.add("removed");
        observers.add("last");
        List<String> delivered = new ArrayList<>();
        dispatch(observers, observer -> {
            delivered.add(observer);
            if (observer.equals("first")) {
                observers.remove("first");
                observers.remove("removed");
                observers.add("new");
            }
        });
        assertEquals(List.of("first", "last"), delivered);
        delivered.clear();
        dispatch(observers, delivered::add);
        assertEquals(List.of("last", "new"), delivered);
    }

    /// An inline subscription can be replaced during notification without reusing its dispatch slot.
    @Test
    void replacingOnlySubscriberDuringDispatchPreservesBounds() {
        M3ObserverList<String> observers = new M3ObserverList<>();
        observers.add("first");
        List<String> delivered = new ArrayList<>();
        dispatch(observers, observer -> {
            delivered.add(observer);
            observers.remove(observer);
            observers.add("second");
            dispatch(observers, delivered::add);
        });
        assertEquals(List.of("first", "second"), delivered);
        delivered.clear();
        dispatch(observers, delivered::add);
        assertEquals(List.of("second"), delivered);
    }

    /// Nested dispatch sees new subscriptions without changing the outer pass's upper bound.
    @Test
    void nestedDispatchRetainsStableIndices() {
        M3ObserverList<String> observers = new M3ObserverList<>();
        observers.add("first");
        observers.add("removed");
        List<String> delivered = new ArrayList<>();
        dispatch(observers, observer -> {
            delivered.add("outer " + observer);
            observers.remove("removed");
            observers.add("new");
            dispatch(observers, nested -> delivered.add("inner " + nested));
            assertTrue(observers.isDispatching());
        });
        assertEquals(List.of("outer first", "inner first", "inner new"), delivered);
        assertFalse(observers.isDispatching());
    }

    /// Callback exceptions still allow teardown and subsequent registration and notification.
    @Test
    void exceptionalDispatchCanBeReleasedAndRestarted() {
        M3ObserverList<String> observers = new M3ObserverList<>();
        observers.add("first");
        assertThrows(IllegalArgumentException.class, () -> dispatch(observers, observer -> {
            observers.remove(observer);
            throw new IllegalArgumentException("callback failed");
        }));
        assertTrue(observers.isEmpty());
        assertFalse(observers.isDispatching());
        observers.add("second");
        List<String> delivered = new ArrayList<>();
        dispatch(observers, delivered::add);
        assertEquals(List.of("second"), delivered);
    }

    /// Growth, compaction, and transition back to inline storage preserve registration order.
    @Test
    void compactionPreservesSurvivingSubscriptions() {
        M3ObserverList<String> observers = new M3ObserverList<>();
        List<String> registered = new ArrayList<>();
        for (int index = 0; index < 100; index++) {
            String value = "observer " + index;
            registered.add(value);
            observers.add(value);
        }
        dispatch(observers, observer -> {
            if (observer != registered.get(99)) {
                observers.remove(observer);
            }
        });
        List<String> delivered = new ArrayList<>();
        observers.add("last");
        dispatch(observers, delivered::add);
        assertEquals(List.of(registered.get(99), "last"), delivered);
        dispatch(observers, observers::remove);
        assertTrue(observers.isEmpty());
    }

    /// Exercises the required try/finally dispatch protocol with a test callback.
    private static <T> void dispatch(M3ObserverList<T> observers, Consumer<T> action) {
        int limit = observers.beginDispatch();
        try {
            for (int index = 0; index < limit; index++) {
                @Nullable T observer = observers.get(index);
                if (observer != null) {
                    action.accept(observer);
                }
            }
        } finally {
            observers.endDispatch();
        }
    }
}
