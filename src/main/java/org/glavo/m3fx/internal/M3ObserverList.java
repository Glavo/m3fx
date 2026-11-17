// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/// Identity-based subscriptions with stable indices during nested dispatch.
///
/// Each dispatch captures its own upper bound. Removed entries are skipped, and entries added during a dispatch
/// are visible only to subsequent dispatches, including nested ones. Callers must pair [#beginDispatch()] with
/// [#endDispatch()] in a `finally` block. All access is confined to the JavaFX Application Thread.
@NotNullByDefault
final class M3ObserverList<T> {
    /// Single subscription stored inline when no backing array is needed.
    private @Nullable T single;

    /// Multiple entries and temporary holes, or null while using inline storage.
    private @Nullable Object @Nullable [] entries;

    /// Number of occupied slots, including holes.
    private int slots;

    /// Number of live entries.
    private int size;

    /// Number of nested dispatches whose indices must remain stable.
    private int dispatchDepth;

    /// Creates an empty subscription list without allocating backing storage.
    M3ObserverList() {
    }

    /// Returns whether there are no live subscriptions.
    boolean isEmpty() {
        return size == 0;
    }

    /// Returns whether a callback dispatch is in progress.
    boolean isDispatching() {
        return dispatchDepth != 0;
    }

    /// Appends a non-null subscription; each registration must have a distinct identity.
    void add(T observer) {
        Objects.requireNonNull(observer, "observer");
        if (entries == null) {
            if (slots == 0) {
                single = observer;
                slots = size = 1;
                return;
            }
            entries = new Object[4];
            entries[0] = single;
            single = null;
        }
        if (slots == entries.length) {
            int capacity = entries.length;
            entries = Arrays.copyOf(entries, capacity + (capacity >> 1));
        }
        entries[slots++] = observer;
        size++;
    }

    /// Removes the identity-matching subscription, returning whether it was registered.
    boolean remove(T observer) {
        for (int index = 0; index < slots; index++) {
            if (get(index) == observer) {
                if (entries == null) {
                    single = null;
                } else {
                    entries[index] = null;
                }
                size--;
                compact();
                return true;
            }
        }
        return false;
    }

    /// Begins a dispatch and returns its exclusive upper slot bound.
    int beginDispatch() {
        dispatchDepth++;
        return slots;
    }

    /// Returns the current entry in a dispatch slot, or null if it has been removed.
    @SuppressWarnings("unchecked")
    @Nullable T get(int index) {
        return entries == null ? single : (T) entries[index];
    }

    /// Ends a dispatch, reclaiming holes and excess storage after the outermost dispatch.
    void endDispatch() {
        dispatchDepth--;
        compact();
    }

    /// Releases removed references without shifting indices used by active dispatches.
    private void compact() {
        if (dispatchDepth != 0) {
            return;
        }
        if (size == 0) {
            entries = null;
            single = null;
            slots = 0;
            return;
        }
        if (entries == null) {
            return;
        }
        if (size == 1) {
            for (int index = 0; index < slots; index++) {
                @Nullable T entry = get(index);
                if (entry != null) {
                    single = entry;
                    break;
                }
            }
            entries = null;
            slots = 1;
            return;
        }
        if (slots != size) {
            int destination = 0;
            for (int source = 0; source < slots; source++) {
                @Nullable Object entry = entries[source];
                if (entry != null) {
                    entries[destination++] = entry;
                }
            }
            Arrays.fill(entries, destination, slots, null);
            slots = destination;
        }
        if (entries.length > 8 && size <= (entries.length >> 2)) {
            entries = Arrays.copyOf(entries, Math.max(4, size + (size >> 1)));
        }
    }
}
