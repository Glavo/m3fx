// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import org.glavo.m3fx.localization.M3Localized;
import org.glavo.m3fx.localization.M3Localizer;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/// Subscribes to one component's localizer and delivers its initial value immediately.
///
/// This subscription does not inspect or observe parent nodes. Skins close it on disposal; a component may retain
/// it for its own lifetime. Construction and callbacks for live controls run on the JavaFX Application Thread.
@NotNullByDefault
public final class M3LocalizerObserver implements AutoCloseable {
    /// The directly observed property, cleared when the subscription closes.
    private @Nullable ObjectProperty<M3Localizer> property;

    /// Receives the component's current localizer.
    private final Consumer<M3Localizer> action;

    /// The most recently delivered instance, used to suppress stale reentrant notifications.
    private @Nullable M3Localizer delivered;

    /// Defers reentrant replacements until the active callback has finished updating its component.
    private boolean refreshing;

    /// Reads and revalidates the property so distinct localizers are delivered even when they compare equal.
    private final InvalidationListener listener = observable -> refresh();

    /// Subscribes and delivers the current value; a failed initial callback removes the subscription.
    ///
    /// @param owner the component being observed
    /// @param action the callback for initial and replacement values
    /// @throws NullPointerException if either argument is `null`
    public M3LocalizerObserver(M3Localized owner, Consumer<M3Localizer> action) {
        this.action = Objects.requireNonNull(action, "action");
        property = Objects.requireNonNull(owner, "owner").localizerProperty();
        property.addListener(listener);
        try {
            refresh();
        } catch (RuntimeException | Error exception) {
            close();
            throw exception;
        }
    }

    /// Serializes replacements so an older callback cannot finish after the latest one.
    private void refresh() {
        if (property == null) {
            return;
        }
        // Read even during reentrancy to rearm invalidation notifications on the property.
        M3Localizer next = property.get();
        if (refreshing) {
            return;
        }
        refreshing = true;
        @Nullable RuntimeException failure = null;
        try {
            while (property != null && next != delivered) {
                delivered = next;
                try {
                    action.accept(next);
                } catch (RuntimeException exception) {
                    if (failure == null) {
                        failure = exception;
                    } else if (failure != exception) {
                        failure.addSuppressed(exception);
                    }
                }
                if (property != null) {
                    next = property.get();
                }
            }
        } finally {
            refreshing = false;
        }
        if (failure != null) {
            throw failure;
        }
    }

    /// Removes the listener; repeated calls have no effect.
    @Override
    public void close() {
        if (property != null) {
            property.removeListener(listener);
            property = null;
            delivered = null;
        }
    }
}
