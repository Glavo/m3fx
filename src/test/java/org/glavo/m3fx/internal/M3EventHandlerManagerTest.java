// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the JavaFX-compatible dispatch contract implemented by [M3EventHandlerManager].
@NotNullByDefault
final class M3EventHandlerManagerTest {
    /// A parent type used to verify event-type hierarchy traversal.
    private static final EventType<Event> PARENT_EVENT =
            new EventType<>(Event.ANY, "M3_EVENT_HANDLER_MANAGER_PARENT");

    /// A concrete event type used by the manager contract tests.
    private static final EventType<Event> TEST_EVENT =
            new EventType<>(PARENT_EVENT, "M3_EVENT_HANDLER_MANAGER_TEST");

    /// Verifies capture and bubble ordering, source correction, singleton ordering, and consumption behavior.
    @Test
    void dispatchesUsingJavaFxCaptureAndBubbleSemantics() {
        ManagedTarget target = new ManagedTarget();
        List<String> order = new ArrayList<>();
        List<Event> observedEvents = new ArrayList<>();

        target.manager.addEventFilter(TEST_EVENT, event -> {
            order.add("test-filter");
            observedEvents.add(event);
        });
        target.manager.addEventFilter(PARENT_EVENT, event -> order.add("parent-filter"));
        target.manager.addEventFilter(Event.ANY, event -> order.add("any-filter"));
        target.manager.addEventHandler(TEST_EVENT, event -> order.add("test-handler"));
        target.manager.setEventHandler(TEST_EVENT, event -> order.add("singleton-handler"));
        target.manager.addEventHandler(PARENT_EVENT, event -> order.add("parent-handler"));
        target.manager.addEventHandler(Event.ANY, event -> order.add("any-handler"));

        Object foreignSource = new Object();
        Event original = new Event(foreignSource, target, TEST_EVENT);
        Event.fireEvent(target, original);

        assertEquals(List.of(
                "test-filter",
                "parent-filter",
                "any-filter",
                "test-handler",
                "singleton-handler",
                "parent-handler",
                "any-handler"
        ), order);
        assertEquals(1, observedEvents.size());
        Event delivered = observedEvents.get(0);
        assertNotSame(original, delivered);
        assertSame(target, delivered.getSource());
        assertSame(target, delivered.getTarget());
        assertSame(foreignSource, original.getSource());

        ManagedTarget consumingTarget = new ManagedTarget();
        List<String> consumedOrder = new ArrayList<>();
        consumingTarget.manager.addEventFilter(TEST_EVENT, event -> {
            consumedOrder.add("consume");
            event.consume();
        });
        consumingTarget.manager.addEventFilter(TEST_EVENT, event -> consumedOrder.add("peer-filter"));
        consumingTarget.manager.addEventFilter(PARENT_EVENT, event -> consumedOrder.add("parent-filter"));
        consumingTarget.manager.addEventHandler(TEST_EVENT, event -> consumedOrder.add("handler"));

        Event consumed = new Event(consumingTarget, consumingTarget, TEST_EVENT);
        Event.fireEvent(consumingTarget, consumed);

        assertTrue(consumed.isConsumed());
        assertEquals(List.of("consume", "peer-filter", "parent-filter"), consumedOrder);

        ManagedTarget bubblingTarget = new ManagedTarget();
        List<String> bubblingOrder = new ArrayList<>();
        bubblingTarget.manager.addEventHandler(TEST_EVENT, event -> {
            bubblingOrder.add("consume");
            event.consume();
        });
        bubblingTarget.manager.addEventHandler(TEST_EVENT, event -> bubblingOrder.add("peer-handler"));
        bubblingTarget.manager.setEventHandler(TEST_EVENT, event -> bubblingOrder.add("singleton-handler"));
        bubblingTarget.manager.addEventHandler(PARENT_EVENT, event -> bubblingOrder.add("parent-handler"));

        Event bubblingConsumed = new Event(bubblingTarget, bubblingTarget, TEST_EVENT);
        Event.fireEvent(bubblingTarget, bubblingConsumed);

        assertTrue(bubblingConsumed.isConsumed());
        assertEquals(
                List.of("consume", "peer-handler", "singleton-handler", "parent-handler"),
                bubblingOrder
        );
    }

    /// Verifies that registration changes made during dispatch follow JavaFX's mutation-safe linked traversal.
    @Test
    void supportsRegistrationMutationDuringDispatch() {
        ManagedTarget target = new ManagedTarget();
        List<String> order = new ArrayList<>();
        EventHandler<Event> removedHandler = event -> order.add("removed");
        EventHandler<Event> appendedHandler = event -> order.add("appended");
        EventHandler<Event> appendedFilter = event -> order.add("appended-filter");
        EventHandler<Event> mutatingHandler = event -> {
            order.add("mutating");
            target.manager.removeEventHandler(TEST_EVENT, removedHandler);
            target.manager.addEventHandler(TEST_EVENT, appendedHandler);
            target.manager.addEventFilter(TEST_EVENT, appendedFilter);
        };

        target.manager.addEventHandler(TEST_EVENT, mutatingHandler);
        target.manager.addEventHandler(TEST_EVENT, removedHandler);

        Event.fireEvent(target, new Event(TEST_EVENT));
        assertEquals(List.of("mutating", "appended"), order);

        order.clear();
        Event.fireEvent(target, new Event(TEST_EVENT));
        assertEquals(List.of("appended-filter", "mutating", "appended"), order);
    }

    /// A minimal event target whose dispatch chain is backed only by the manager under test.
    @NotNullByDefault
    private static final class ManagedTarget implements EventTarget {
        /// The manager installed in this target's event dispatch chain.
        private final M3EventHandlerManager manager = new M3EventHandlerManager(this);

        /// Prepends the manager to the supplied JavaFX event dispatch chain.
        ///
        /// @param tail the remaining event dispatch chain
        /// @return the supplied chain with this target's manager prepended
        @Override
        public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
            return tail.prepend(manager);
        }
    }
}
