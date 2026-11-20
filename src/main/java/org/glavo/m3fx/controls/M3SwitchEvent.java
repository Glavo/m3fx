// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.Serial;

/// Requests a change to the selected state of an [M3Switch] or [M3SwitchSettingItem].
///
/// The switch dispatches [#TOGGLE_REQUEST] before changing its value. A filter or handler may
/// [consume][#consume()] the event to prevent the default change and its subsequent action event.
/// To defer a decision, consume the request synchronously and update the model after confirmation.
@NotNullByDefault
public final class M3SwitchEvent extends Event {
    /// The serialization version identifier.
    @Serial
    private static final long serialVersionUID = 1L;

    /// Fired by an enabled switch before it attempts to change selection.
    public static final EventType<M3SwitchEvent> TOGGLE_REQUEST = new EventType<>(Event.ANY, "M3_SWITCH_TOGGLE_REQUEST");

    /// The selected state requested by this activation.
    private final boolean requestedValue;

    /// The originating request, shared by the copies JavaFX delivers to ancestor handlers.
    private final M3SwitchEvent request = this;

    /// Creates a request targeted at the activating switch.
    M3SwitchEvent(EventTarget source, boolean requestedValue) {
        super(source, source, TOGGLE_REQUEST);
        this.requestedValue = requestedValue;
    }

    /// Returns the proposed selected value, not the switch's current value.
    ///
    /// @return `true` to turn the switch on, or `false` to turn it off
    public boolean getRequestedValue() {
        return requestedValue;
    }

    /// Consumes this delivery and vetoes the originating switch's default change.
    @Override
    public void consume() {
        super.consume();
        request.consumed = true;
    }
}
