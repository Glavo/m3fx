// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.Event;
import javafx.event.EventType;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.Objects;

/// Describes a lifecycle transition or close request from an [M3Dialog].
///
/// The event's source and target are the dialog that emitted it. Filters, registered handlers, and the dialog's
/// singleton `onXxx` handler properties therefore participate in the ordinary JavaFX event dispatch chain. The
/// [presentation handle][#getHandle()] identifies the exact presentation that emitted the event when a dialog object
/// is reused.
///
/// Only a [#CLOSE_REQUEST] event is cancellable by contract. Calling [#consume()] on that event keeps the dialog
/// visible and prevents its exit transition. Other lifecycle handlers may observe their event but consumption has no
/// effect on the already established transition.
@NotNullByDefault
public final class M3DialogEvent extends Event {
    /// Serialization identifier for JavaFX event compatibility.
    @Serial
    private static final long serialVersionUID = 1L;

    /// The root event type for all M3FX dialog lifecycle events.
    public static final EventType<M3DialogEvent> ANY = new EventType<>(Event.ANY, "M3_DIALOG");

    /// Fired immediately before a dialog surface is installed; its handle is not yet showing.
    public static final EventType<M3DialogEvent> SHOWING = new EventType<>(ANY, "M3_DIALOG_SHOWING");

    /// Fired after a dialog surface has been installed and its handle is showing.
    public static final EventType<M3DialogEvent> SHOWN = new EventType<>(ANY, "M3_DIALOG_SHOWN");

    /// Fired before an accepted close begins its exit transition while the handle remains showing.
    public static final EventType<M3DialogEvent> HIDING = new EventType<>(ANY, "M3_DIALOG_HIDING");

    /// Fired after a dialog surface has been removed, its handle has detached, and any applicable focus restoration has
    /// been scheduled.
    public static final EventType<M3DialogEvent> HIDDEN = new EventType<>(ANY, "M3_DIALOG_HIDDEN");

    /// Fired when code or an action button requests that a showing dialog close.
    public static final EventType<M3DialogEvent> CLOSE_REQUEST =
            new EventType<>(ANY, "M3_DIALOG_CLOSE_REQUEST");

    /// The presentation that emitted this lifecycle event.
    private final M3DialogHandle handle;

    /// The retained action button that initiated this event, or `null` when no action initiated the lifecycle step.
    ///
    /// Action-button activation and Escape cancellation retain the exact button instance through close request,
    /// hiding, and hidden events. Showing events, [M3DialogHandle#requestClose()], scrim dismissal, and forced
    /// host-window cleanup use `null`.
    private final @Nullable M3Button action;

    /// Creates a lifecycle event for one dialog presentation.
    ///
    /// @param dialog    the dialog used as this event's source and target
    /// @param handle    the presentation that emitted the event
    /// @param eventType the lifecycle event type
    /// @param action    the initiating action button, or `null` when no action button initiated the event
    /// @throws NullPointerException if `dialog`, `handle`, or `eventType` is `null`
    M3DialogEvent(
            M3Dialog dialog,
            M3DialogHandle handle,
            EventType<M3DialogEvent> eventType,
            @Nullable M3Button action
    ) {
        super(
                Objects.requireNonNull(dialog, "dialog"),
                dialog,
                Objects.requireNonNull(eventType, "eventType")
        );
        this.handle = Objects.requireNonNull(handle, "handle");
        this.action = action;
    }

    /// Returns the dialog that emitted this event.
    ///
    /// @return the non-null dialog source
    public M3Dialog getDialog() {
        return (M3Dialog) getSource();
    }

    /// Returns the handle for the presentation that emitted this event.
    ///
    /// The handle may already report `false` from [M3DialogHandle#isShowing()] during a [#HIDDEN] event. It cannot
    /// control a later presentation of the same dialog.
    ///
    /// @return the non-null presentation handle
    public M3DialogHandle getHandle() {
        return handle;
    }

    /// Returns the retained action button that initiated this event.
    ///
    /// The returned object is the same instance contained by [M3DialogPane#getActions()]. It remains valid after the
    /// presentation closes and may be compared by identity to distinguish confirmation, cancellation, or another
    /// application-defined action.
    ///
    /// @return the initiating action button, or `null` when no action button initiated the event
    public @Nullable M3Button getAction() {
        return action;
    }
}
