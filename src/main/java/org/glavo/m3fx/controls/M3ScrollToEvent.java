// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.Serial;
import java.util.Objects;

/// Requests that a control reveal an item at a specified index.
///
/// Controls fire this event when a programmatic scrolling method is invoked. The presentation responsible for
/// indexed content should handle [#SCROLL_TO_INDEX], reveal the requested item when possible, and consume the event
/// after accepting the request. Applications may observe or filter the request through the ordinary JavaFX event
/// dispatch chain.
///
/// The event describes a request rather than a completion notification. Handling it does not guarantee that the item
/// is already visible when the handler returns; movement may be deferred until layout information becomes available.
@NotNullByDefault
public final class M3ScrollToEvent extends Event {
    /// The serialization version identifier.
    @Serial
    private static final long serialVersionUID = 1L;

    /// The root event type for M3FX indexed scrolling requests.
    public static final EventType<M3ScrollToEvent> ANY =
            new EventType<>(Event.ANY, "M3_SCROLL_TO");

    /// Requests that the indexed item be revealed.
    public static final EventType<M3ScrollToEvent> SCROLL_TO_INDEX =
            new EventType<>(ANY, "M3_SCROLL_TO_INDEX");

    /// The requested item index.
    private final int index;

    /// Whether the presentation should animate the movement when motion is available.
    private final boolean animated;

    /// Creates an indexed scrolling request.
    ///
    /// @param source   the object that issued the request
    /// @param target   the initial event target
    /// @param index    the non-negative item index to reveal
    /// @param animated whether movement should animate when the target supports animation
    /// @throws NullPointerException     if `source` or `target` is `null`
    /// @throws IllegalArgumentException if `index` is negative
    public M3ScrollToEvent(Object source, EventTarget target, int index, boolean animated) {
        super(
                Objects.requireNonNull(source, "source"),
                Objects.requireNonNull(target, "target"),
                SCROLL_TO_INDEX
        );
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative: " + index);
        }
        this.index = index;
        this.animated = animated;
    }

    /// Returns the requested item index.
    ///
    /// @return the non-negative item index
    public int getIndex() {
        return index;
    }

    /// Returns whether animated movement was requested.
    ///
    /// A value of `true` does not override reduced-motion settings or require a presentation to animate when motion
    /// is unavailable.
    ///
    /// @return `true` if animated movement was requested
    public boolean isAnimated() {
        return animated;
    }

    /// Creates a copy of this event for a different source and target.
    ///
    /// @param newSource the source for the copied event
    /// @param newTarget the target for the copied event
    /// @return the copied event
    /// @throws NullPointerException if `newSource` or `newTarget` is `null`
    @Override
    public M3ScrollToEvent copyFor(Object newSource, EventTarget newTarget) {
        return (M3ScrollToEvent) super.copyFor(
                Objects.requireNonNull(newSource, "newSource"),
                Objects.requireNonNull(newTarget, "newTarget")
        );
    }
}
