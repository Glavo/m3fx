// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// Dispatches JavaFX events to filters, registered handlers, and singleton property handlers.
///
/// This manager supplies the public event semantics needed by non-Node event targets without relying on internal
/// `com.sun.javafx` APIs. Registrations are retained by identity, duplicate registrations for the same phase and
/// event type are ignored, and event type superclasses participate in both capture and bubble phases.
/// Filters and handlers share a live registration order; each phase invokes only its own registrations.
///
/// Instances are not thread-safe. Registration and dispatch must be serialized by the owning event target and are
/// normally performed on the JavaFX application thread.
@NotNullByDefault
public final class M3EventHandlerManager implements EventDispatcher {
    /// The object installed as the source of events delivered through this manager.
    private final Object eventSource;

    /// The lazily populated handler buckets indexed by registered event type.
    private final Map<EventType<? extends Event>, HandlerBucket<? extends Event>> buckets = new HashMap<>();

    /// Creates an empty event manager for one event source.
    ///
    /// @param eventSource the source exposed to registered filters and handlers
    /// @throws NullPointerException if `eventSource` is `null`
    public M3EventHandlerManager(Object eventSource) {
        this.eventSource = Objects.requireNonNull(eventSource, "eventSource");
    }

    /// Registers a bubbling-phase handler for an event type.
    ///
    /// Re-registering the same handler instance for the same event type has no effect.
    ///
    /// @param eventType the event type received by the handler
    /// @param handler   the handler to register
    /// @param <E>       the event class accepted by the handler
    /// @throws NullPointerException if `eventType` or `handler` is `null`
    public <E extends Event> void addEventHandler(
            EventType<E> eventType,
            EventHandler<? super E> handler
    ) {
        requireEventType(eventType);
        Objects.requireNonNull(handler, "handler");
        getOrCreateBucket(eventType).add(handler, false);
    }

    /// Removes a bubbling-phase handler from an event type.
    ///
    /// The method has no effect when that handler instance is not registered for the supplied type.
    ///
    /// @param eventType the event type from which to remove the handler
    /// @param handler   the handler to remove
    /// @param <E>       the event class accepted by the handler
    /// @throws NullPointerException if `eventType` or `handler` is `null`
    public <E extends Event> void removeEventHandler(
            EventType<E> eventType,
            EventHandler<? super E> handler
    ) {
        requireEventType(eventType);
        Objects.requireNonNull(handler, "handler");
        @Nullable HandlerBucket<E> bucket = getBucket(eventType);
        if (bucket != null) {
            bucket.remove(handler, false);
            removeEmptyBucket(eventType, bucket);
        }
    }

    /// Registers a capturing-phase filter for an event type.
    ///
    /// Re-registering the same filter instance for the same event type has no effect.
    ///
    /// @param eventType the event type received by the filter
    /// @param filter    the filter to register
    /// @param <E>       the event class accepted by the filter
    /// @throws NullPointerException if `eventType` or `filter` is `null`
    public <E extends Event> void addEventFilter(
            EventType<E> eventType,
            EventHandler<? super E> filter
    ) {
        requireEventType(eventType);
        Objects.requireNonNull(filter, "filter");
        getOrCreateBucket(eventType).add(filter, true);
    }

    /// Removes a capturing-phase filter from an event type.
    ///
    /// The method has no effect when that filter instance is not registered for the supplied type.
    ///
    /// @param eventType the event type from which to remove the filter
    /// @param filter    the filter to remove
    /// @param <E>       the event class accepted by the filter
    /// @throws NullPointerException if `eventType` or `filter` is `null`
    public <E extends Event> void removeEventFilter(
            EventType<E> eventType,
            EventHandler<? super E> filter
    ) {
        requireEventType(eventType);
        Objects.requireNonNull(filter, "filter");
        @Nullable HandlerBucket<E> bucket = getBucket(eventType);
        if (bucket != null) {
            bucket.remove(filter, true);
            removeEmptyBucket(eventType, bucket);
        }
    }

    /// Replaces the singleton bubbling handler associated with one convenience property.
    ///
    /// Registered handlers are independent of this slot and run before it. Supplying `null` clears only the
    /// singleton handler.
    ///
    /// @param eventType the event type associated with the singleton handler
    /// @param handler   the new handler, or `null` to clear it
    /// @param <E>       the event class accepted by the handler
    /// @throws NullPointerException if `eventType` is `null`
    /// @throws RuntimeException     if the event-handler property has been created and is currently bound
    public <E extends Event> void setEventHandler(
            EventType<E> eventType,
            @Nullable EventHandler<E> handler
    ) {
        requireEventType(eventType);
        @Nullable HandlerBucket<E> bucket = getBucket(eventType);
        if (bucket == null) {
            if (handler == null) {
                return;
            }
            bucket = getOrCreateBucket(eventType);
        }
        bucket.setSingletonHandler(handler);
        removeEmptyBucket(eventType, bucket);
    }

    /// Returns the singleton bubbling handler associated with an event type.
    ///
    /// This method reads the same storage exposed by [#eventHandlerProperty(EventType, String)]. It does not create
    /// the property when callers have only used [#setEventHandler(EventType, EventHandler)].
    ///
    /// @param eventType the event type associated with the singleton handler
    /// @param <E>       the event class accepted by the handler
    /// @return the installed singleton handler, or `null` when none is installed
    /// @throws NullPointerException if `eventType` is `null`
    public <E extends Event> @Nullable EventHandler<E> getEventHandler(EventType<E> eventType) {
        requireEventType(eventType);
        @Nullable HandlerBucket<E> bucket = getBucket(eventType);
        return bucket == null ? null : bucket.getSingletonHandler();
    }

    /// Returns the lazily created property for an event type's singleton bubbling handler.
    ///
    /// Creating the property preserves any handler previously installed through
    /// [#setEventHandler(EventType, EventHandler)]. Subsequent setter calls, property writes, bindings, listeners, and
    /// event dispatch all observe the same value.
    ///
    /// @param eventType the event type associated with the property
    /// @param name      the JavaFX property name
    /// @param <E>       the event class accepted by the handler
    /// @return the stable event-handler property for the supplied type
    /// @throws NullPointerException     if `eventType` or `name` is `null`
    /// @throws IllegalArgumentException if the property already exists with a different name
    public <E extends Event> ObjectProperty<@Nullable EventHandler<E>> eventHandlerProperty(
            EventType<E> eventType,
            String name
    ) {
        requireEventType(eventType);
        Objects.requireNonNull(name, "name");
        return getOrCreateBucket(eventType).eventHandlerProperty(eventSource, name);
    }

    /// Dispatches one event through capture, the remaining chain, and bubble phases.
    ///
    /// @param event the event being dispatched
    /// @param tail  the remaining event dispatch chain
    /// @return the dispatched event, or `null` when dispatch terminates or consumes it
    /// @throws NullPointerException if `event` or `tail` is `null`
    @Override
    public @Nullable Event dispatchEvent(Event event, EventDispatchChain tail) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(tail, "tail");

        Event current = dispatchPhase(event, true);
        if (current.isConsumed()) {
            return null;
        }

        @Nullable Event returned = tail.dispatchEvent(current);
        if (returned == null) {
            return null;
        }

        current = dispatchPhase(returned, false);
        return current.isConsumed() ? null : current;
    }

    /// Dispatches one phase from the concrete event type through its supertype chain.
    private Event dispatchPhase(Event event, boolean filter) {
        Event current = event;
        @Nullable EventType<?> eventType = event.getEventType();
        while (eventType != null) {
            @Nullable HandlerBucket<?> bucket = buckets.get(eventType);
            if (bucket != null) {
                if (bucket.hasRegistrations(filter)) {
                    current = fixEventSource(current);
                    bucket.dispatch(current, filter);
                }
                removeEmptyBucket(eventType, bucket);
            }
            eventType = eventType.getSuperType();
        }
        return current;
    }

    /// Returns an event whose source is this manager's configured source.
    private Event fixEventSource(Event event) {
        return event.getSource() == eventSource
                ? event
                : event.copyFor(eventSource, event.getTarget());
    }

    /// Returns the existing bucket for an event type.
    @SuppressWarnings("unchecked")
    private <E extends Event> @Nullable HandlerBucket<E> getBucket(EventType<E> eventType) {
        return (HandlerBucket<E>) buckets.get(eventType);
    }

    /// Returns an existing bucket or creates an empty bucket for an event type.
    private <E extends Event> HandlerBucket<E> getOrCreateBucket(EventType<E> eventType) {
        @Nullable HandlerBucket<E> bucket = getBucket(eventType);
        if (bucket == null) {
            bucket = new HandlerBucket<>();
            buckets.put(eventType, bucket);
        }
        return bucket;
    }

    /// Removes a bucket after its last registration is cleared.
    private void removeEmptyBucket(EventType<?> eventType, HandlerBucket<?> bucket) {
        if (bucket.isEmpty()) {
            buckets.remove(eventType, bucket);
        }
    }

    /// Validates a public registration event type.
    private static void requireEventType(@Nullable EventType<?> eventType) {
        Objects.requireNonNull(eventType, "eventType");
    }

    /// Stores one live registration order shared by the capture and bubble phases of an event type.
    @NotNullByDefault
    private static final class HandlerBucket<E extends Event> {
        /// The first registered filter or handler.
        private @Nullable HandlerNode<E> first;

        /// The last registered filter or handler.
        private @Nullable HandlerNode<E> last;

        /// The singleton bubbling handler used before its property is requested.
        private @Nullable EventHandler<E> singletonHandler;

        /// The lazily created property that takes ownership of the singleton handler value.
        private @Nullable ObjectProperty<@Nullable EventHandler<E>> singletonProperty;

        /// Registers one callback by identity within the requested phase.
        private void add(EventHandler<? super E> handler, boolean filter) {
            if (find(handler, filter) != null) {
                return;
            }
            HandlerNode<E> node = new HandlerNode<>(handler, filter);
            if (last == null) {
                first = node;
            } else {
                last.next = node;
                node.previous = last;
            }
            last = node;
        }

        /// Removes one callback by identity from the requested phase.
        private void remove(EventHandler<? super E> handler, boolean filter) {
            @Nullable HandlerNode<E> node = find(handler, filter);
            if (node != null) {
                unlink(node);
            }
        }

        /// Replaces the singleton handler through its direct or property-backed storage.
        private void setSingletonHandler(@Nullable EventHandler<E> handler) {
            @Nullable ObjectProperty<@Nullable EventHandler<E>> property = singletonProperty;
            if (property == null) {
                singletonHandler = handler;
            } else {
                property.set(handler);
            }
        }

        /// Returns the singleton handler without forcing property creation.
        private @Nullable EventHandler<E> getSingletonHandler() {
            @Nullable ObjectProperty<@Nullable EventHandler<E>> property = singletonProperty;
            return property == null ? singletonHandler : property.get();
        }

        /// Returns the stable property that owns this bucket's singleton handler.
        private ObjectProperty<@Nullable EventHandler<E>> eventHandlerProperty(Object bean, String name) {
            @Nullable ObjectProperty<@Nullable EventHandler<E>> property = singletonProperty;
            if (property == null) {
                property = new SimpleObjectProperty<>(bean, name, singletonHandler);
                singletonHandler = null;
                singletonProperty = property;
            } else if (!property.getName().equals(name)) {
                throw new IllegalArgumentException(
                        "Property name mismatch: " + name + " != " + property.getName()
                );
            }
            return property;
        }

        /// Returns whether the requested phase has an active registration, removing expired weak callbacks.
        private boolean hasRegistrations(boolean filter) {
            if (!filter && getSingletonHandler() != null) {
                return true;
            }
            @Nullable HandlerNode<E> node = first;
            while (node != null) {
                if (isDisconnected(node.handler)) {
                    unlink(node);
                } else if (node.filter == filter) {
                    return true;
                }
                node = node.next;
            }
            return false;
        }

        /// Traverses the live registration order for one phase, then invokes the singleton during bubbling.
        @SuppressWarnings("unchecked")
        private void dispatch(Event event, boolean filter) {
            E typedEvent = (E) event;
            @Nullable HandlerNode<E> node = first;
            while (node != null) {
                if (isDisconnected(node.handler)) {
                    unlink(node);
                } else if (node.filter == filter) {
                    node.handler.handle(typedEvent);
                }
                node = node.next;
            }
            if (!filter) {
                @Nullable EventHandler<E> propertyHandler = getSingletonHandler();
                if (propertyHandler != null) {
                    propertyHandler.handle(typedEvent);
                }
            }
        }

        /// Unlinks a registration while retaining its next pointer for dispatch already visiting that node.
        private void unlink(HandlerNode<E> node) {
            if (node.previous == null) {
                first = node.next;
            } else {
                node.previous.next = node.next;
            }
            if (node.next == null) {
                last = node.previous;
            } else {
                node.next.previous = node.previous;
            }
        }

        /// Finds a callback by phase and identity, removing expired weak registrations encountered first.
        private @Nullable HandlerNode<E> find(EventHandler<? super E> handler, boolean filter) {
            @Nullable HandlerNode<E> node = first;
            while (node != null) {
                if (isDisconnected(node.handler)) {
                    unlink(node);
                } else if (node.filter == filter && node.handler == handler) {
                    return node;
                }
                node = node.next;
            }
            return null;
        }

        /// Returns whether a weak registration has lost its referenced handler.
        private static boolean isDisconnected(EventHandler<?> handler) {
            return handler instanceof WeakEventHandler<?> weakHandler && weakHandler.wasGarbageCollected();
        }

        /// Returns whether this bucket retains no registrations or singleton property.
        private boolean isEmpty() {
            return first == null && singletonHandler == null && singletonProperty == null;
        }
    }

    /// One identity-preserving linked registration node.
    @NotNullByDefault
    private static final class HandlerNode<E extends Event> {
        /// The registered handler or filter.
        private final EventHandler<? super E> handler;

        /// Whether this registration participates in capture rather than bubble dispatch.
        private final boolean filter;

        /// The previous registration in the shared order.
        private @Nullable HandlerNode<E> previous;

        /// The next registration in the shared order.
        private @Nullable HandlerNode<E> next;

        /// Creates one linked registration node.
        private HandlerNode(EventHandler<? super E> handler, boolean filter) {
            this.handler = handler;
            this.filter = filter;
        }
    }
}
