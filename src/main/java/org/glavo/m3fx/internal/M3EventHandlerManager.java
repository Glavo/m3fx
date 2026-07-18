// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
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
        getOrCreateBucket(eventType).addHandler(handler);
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
            bucket.removeHandler(handler);
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
        getOrCreateBucket(eventType).addFilter(filter);
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
            bucket.removeFilter(filter);
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

        Event current = dispatchFilters(event);
        if (current.isConsumed()) {
            return null;
        }

        @Nullable Event returned = tail.dispatchEvent(current);
        if (returned == null) {
            return null;
        }

        current = dispatchHandlers(returned);
        return current.isConsumed() ? null : current;
    }

    /// Dispatches capturing filters from the concrete event type through its supertype chain.
    private Event dispatchFilters(Event event) {
        Event current = event;
        @Nullable EventType<?> eventType = event.getEventType();
        while (eventType != null) {
            @Nullable HandlerBucket<?> bucket = buckets.get(eventType);
            if (bucket != null) {
                if (bucket.hasFilters()) {
                    current = fixEventSource(current);
                    bucket.dispatchFilters(current);
                }
                removeEmptyBucket(eventType, bucket);
            }
            eventType = eventType.getSuperType();
        }
        return current;
    }

    /// Dispatches bubbling handlers from the concrete event type through its supertype chain.
    private Event dispatchHandlers(Event event) {
        Event current = event;
        @Nullable EventType<?> eventType = event.getEventType();
        while (eventType != null) {
            @Nullable HandlerBucket<?> bucket = buckets.get(eventType);
            if (bucket != null) {
                if (bucket.hasHandlers()) {
                    current = fixEventSource(current);
                    bucket.dispatchHandlers(current);
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

    /// Stores capture and bubble registrations for one event type.
    @NotNullByDefault
    private static final class HandlerBucket<E extends Event> {
        /// The first registered capturing filter.
        private @Nullable HandlerNode<E> firstFilter;

        /// The last registered capturing filter.
        private @Nullable HandlerNode<E> lastFilter;

        /// The first registered bubbling handler.
        private @Nullable HandlerNode<E> firstHandler;

        /// The last registered bubbling handler.
        private @Nullable HandlerNode<E> lastHandler;

        /// The singleton bubbling handler used before its property is requested.
        private @Nullable EventHandler<E> singletonHandler;

        /// The lazily created property that takes ownership of the singleton handler value.
        private @Nullable EventHandlerProperty<E> singletonProperty;

        /// Registers one bubbling handler by identity.
        private void addHandler(EventHandler<? super E> handler) {
            if (findHandler(handler) != null) {
                return;
            }
            HandlerNode<E> node = new HandlerNode<>(handler);
            if (lastHandler == null) {
                firstHandler = node;
            } else {
                lastHandler.next = node;
                node.previous = lastHandler;
            }
            lastHandler = node;
        }

        /// Removes one bubbling handler by identity.
        private void removeHandler(EventHandler<? super E> handler) {
            @Nullable HandlerNode<E> node = findHandler(handler);
            if (node != null) {
                unlinkHandler(node);
            }
        }

        /// Registers one capturing filter by identity.
        private void addFilter(EventHandler<? super E> filter) {
            if (findFilter(filter) != null) {
                return;
            }
            HandlerNode<E> node = new HandlerNode<>(filter);
            if (lastFilter == null) {
                firstFilter = node;
            } else {
                lastFilter.next = node;
                node.previous = lastFilter;
            }
            lastFilter = node;
        }

        /// Removes one capturing filter by identity.
        private void removeFilter(EventHandler<? super E> filter) {
            @Nullable HandlerNode<E> node = findFilter(filter);
            if (node != null) {
                unlinkFilter(node);
            }
        }

        /// Replaces the singleton handler through its direct or property-backed storage.
        private void setSingletonHandler(@Nullable EventHandler<E> handler) {
            @Nullable EventHandlerProperty<E> property = singletonProperty;
            if (property == null) {
                singletonHandler = handler;
            } else {
                property.set(handler);
            }
        }

        /// Returns the singleton handler without forcing property creation.
        private @Nullable EventHandler<E> getSingletonHandler() {
            @Nullable EventHandlerProperty<E> property = singletonProperty;
            return property == null ? singletonHandler : property.get();
        }

        /// Returns the stable property that owns this bucket's singleton handler.
        private ObjectProperty<@Nullable EventHandler<E>> eventHandlerProperty(Object bean, String name) {
            @Nullable EventHandlerProperty<E> property = singletonProperty;
            if (property == null) {
                property = new EventHandlerProperty<>(bean, name, singletonHandler);
                singletonHandler = null;
                singletonProperty = property;
            } else if (!property.getName().equals(name)) {
                throw new IllegalArgumentException(
                        "Property name mismatch: " + name + " != " + property.getName()
                );
            }
            return property;
        }

        /// Returns whether at least one active capturing filter is registered.
        private boolean hasFilters() {
            while (firstFilter != null && isDisconnected(firstFilter.handler)) {
                unlinkFilter(firstFilter);
            }
            return firstFilter != null;
        }

        /// Returns whether at least one active bubbling handler is registered.
        private boolean hasHandlers() {
            if (getSingletonHandler() != null) {
                return true;
            }
            while (firstHandler != null && isDisconnected(firstHandler.handler)) {
                unlinkHandler(firstHandler);
            }
            return firstHandler != null;
        }

        /// Dispatches all capturing filters in registration order.
        @SuppressWarnings("unchecked")
        private void dispatchFilters(Event event) {
            E typedEvent = (E) event;
            @Nullable HandlerNode<E> node = firstFilter;
            while (node != null) {
                if (isDisconnected(node.handler)) {
                    @Nullable HandlerNode<E> next = node.next;
                    unlinkFilter(node);
                    node = next;
                } else {
                    node.handler.handle(typedEvent);
                    node = node.next;
                }
            }
        }

        /// Dispatches registered and singleton bubbling handlers in JavaFX property-handler order.
        @SuppressWarnings("unchecked")
        private void dispatchHandlers(Event event) {
            E typedEvent = (E) event;
            @Nullable HandlerNode<E> node = firstHandler;
            while (node != null) {
                if (isDisconnected(node.handler)) {
                    @Nullable HandlerNode<E> next = node.next;
                    unlinkHandler(node);
                    node = next;
                } else {
                    node.handler.handle(typedEvent);
                    node = node.next;
                }
            }

            @Nullable EventHandler<E> propertyHandler = getSingletonHandler();
            if (propertyHandler != null) {
                propertyHandler.handle(typedEvent);
            }
        }

        /// Unlinks one filter while retaining its next pointer for mutation-safe dispatch.
        private void unlinkFilter(HandlerNode<E> node) {
            if (node.previous == null) {
                firstFilter = node.next;
            } else {
                node.previous.next = node.next;
            }
            if (node.next == null) {
                lastFilter = node.previous;
            } else {
                node.next.previous = node.previous;
            }
        }

        /// Unlinks one handler while retaining its next pointer for mutation-safe dispatch.
        private void unlinkHandler(HandlerNode<E> node) {
            if (node.previous == null) {
                firstHandler = node.next;
            } else {
                node.previous.next = node.next;
            }
            if (node.next == null) {
                lastHandler = node.previous;
            } else {
                node.next.previous = node.previous;
            }
        }

        /// Finds a live bubbling handler by identity and removes disconnected weak registrations encountered first.
        private @Nullable HandlerNode<E> findHandler(EventHandler<? super E> handler) {
            @Nullable HandlerNode<E> node = firstHandler;
            while (node != null) {
                @Nullable HandlerNode<E> next = node.next;
                if (isDisconnected(node.handler)) {
                    unlinkHandler(node);
                } else if (node.handler == handler) {
                    return node;
                }
                node = next;
            }
            return null;
        }

        /// Finds a live capturing filter by identity and removes disconnected weak registrations encountered first.
        private @Nullable HandlerNode<E> findFilter(EventHandler<? super E> filter) {
            @Nullable HandlerNode<E> node = firstFilter;
            while (node != null) {
                @Nullable HandlerNode<E> next = node.next;
                if (isDisconnected(node.handler)) {
                    unlinkFilter(node);
                } else if (node.handler == filter) {
                    return node;
                }
                node = next;
            }
            return null;
        }

        /// Returns whether a weak registration has lost its referenced handler.
        private static boolean isDisconnected(EventHandler<?> handler) {
            return handler instanceof WeakEventHandler<?> weakHandler && weakHandler.wasGarbageCollected();
        }

        /// Returns whether this bucket retains no filters or handlers.
        private boolean isEmpty() {
            return firstFilter == null
                    && firstHandler == null
                    && singletonHandler == null
                    && singletonProperty == null;
        }
    }

    /// A JavaFX property that owns one bucket's singleton handler after explicit property access.
    @NotNullByDefault
    private static final class EventHandlerProperty<E extends Event>
            extends ObjectPropertyBase<@Nullable EventHandler<E>> {
        /// The property bean exposed to JavaFX bindings and diagnostics.
        private final Object bean;

        /// The stable JavaFX property name.
        private final String name;

        /// Creates a property initialized from the bucket's previously direct handler value.
        private EventHandlerProperty(
                Object bean,
                String name,
                @Nullable EventHandler<E> initialValue
        ) {
            super(initialValue);
            this.bean = bean;
            this.name = name;
        }

        /// Returns the event target that owns this property.
        ///
        /// @return the non-null event target
        @Override
        public Object getBean() {
            return bean;
        }

        /// Returns the name supplied when this event-handler property was first requested.
        ///
        /// @return the non-null property name
        @Override
        public String getName() {
            return name;
        }
    }

    /// One identity-preserving linked registration node.
    @NotNullByDefault
    private static final class HandlerNode<E extends Event> {
        /// The registered handler or filter.
        private final EventHandler<? super E> handler;

        /// The previous registration in the same phase.
        private @Nullable HandlerNode<E> previous;

        /// The next registration in the same phase.
        private @Nullable HandlerNode<E> next;

        /// Creates one linked registration node.
        private HandlerNode(EventHandler<? super E> handler) {
            this.handler = handler;
        }
    }
}
