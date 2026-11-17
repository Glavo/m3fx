// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import org.glavo.m3fx.localization.M3Localizer;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/// Observes inherited localization through shared, reference-counted ancestor contexts.
///
/// Only subscribed nodes and their unoverridden ancestor paths allocate contexts. Reparenting reconnects the path;
/// releasing the final subscription removes all listeners and property entries on unused ancestors. There is no
/// application-wide registry retaining scenes or controls. Construct and close observers on the JavaFX thread.
@NotNullByDefault
public final class M3LocalizerObserver implements AutoCloseable {
    /// Stores a directly installed localizer without allocating a JavaFX property.
    private static final IdentityKey LOCALIZER_KEY = new IdentityKey("M3Localizer.localizer");

    /// Supplies owner context to detached popup roots without replacing an application installation.
    private static final IdentityKey INHERITED_KEY = new IdentityKey("M3Localizer.inherited");

    /// Stores shared observation state only while a node has subscribers.
    private static final IdentityKey CONTEXT_KEY = new IdentityKey("M3Localizer.context");

    /// The shared context, or null after this subscription is closed.
    private @Nullable Context context;

    /// Receives the latest effective localizer.
    private final Consumer<M3Localizer> action;

    /// Last delivered instance, used to suppress notifications hidden by local overrides.
    private @Nullable M3Localizer delivered;

    /// Prevents callbacks from escaping before subscription construction has completed.
    private boolean ready;

    /// Subscribes to a node and immediately delivers its effective localizer.
    ///
    /// @param node the node whose inherited context is observed
    /// @param action the callback, invoked initially and when the resolved instance changes
    /// @throws NullPointerException if either argument is null
    public M3LocalizerObserver(Node node, Consumer<M3Localizer> action) {
        this(node, action, true);
    }

    /// Connects an inherited dependency without notifying before its owner can retain the subscription.
    private M3LocalizerObserver(Node node, Consumer<M3Localizer> action, boolean notifyInitially) {
        Objects.requireNonNull(node, "node");
        this.action = Objects.requireNonNull(action, "action");
        Object existing = node.getProperties().get(CONTEXT_KEY);
        Context shared;
        if (existing instanceof Context value) {
            shared = value;
        } else {
            shared = new Context(node);
            node.getProperties().put(CONTEXT_KEY, shared);
        }
        context = shared;
        shared.observers.add(this);
        try {
            shared.start();
            ready = true;
            if (notifyInitially) {
                refresh();
            }
        } catch (RuntimeException | Error exception) {
            close();
            throw exception;
        }
    }

    /// Removes this subscription; repeated calls have no effect.
    @Override
    public void close() {
        @Nullable Context shared = context;
        if (shared == null) {
            return;
        }
        context = null;
        ready = false;
        delivered = null;
        shared.observers.remove(this);
        if (shared.observers.isEmpty()) {
            shared.stop();
        }
    }

    /// Delivers the current value, ignoring stale notifications after a reentrant change or disposal.
    private void refresh() {
        @Nullable Context shared = context;
        if (ready && shared != null) {
            M3Localizer next = resolve(shared.node);
            if (next != delivered) {
                delivered = next;
                action.accept(next);
            }
        }
    }

    /// Resolves the nearest node installation without creating observation state.
    public static M3Localizer resolve(Node node) {
        for (@Nullable Node current = node; current != null; current = current.getParent()) {
            if (current.hasProperties() && current.getProperties().get(LOCALIZER_KEY) instanceof M3Localizer value) {
                return value;
            }
            if (current.hasProperties() && current.getProperties().get(INHERITED_KEY) instanceof M3Localizer value) {
                return value;
            }
        }
        return Fallback.INSTANCE;
    }

    /// Updates one direct installation; null restores ordinary ancestor inheritance.
    public static void install(Node node, @Nullable M3Localizer localizer) {
        @Nullable Object previous = node.hasProperties() ? node.getProperties().get(LOCALIZER_KEY) : null;
        if (previous == localizer) {
            return;
        }
        if (localizer == null) {
            if (node.hasProperties()) {
                node.getProperties().remove(LOCALIZER_KEY);
            }
        } else {
            node.getProperties().put(LOCALIZER_KEY, localizer);
        }
        if (node.getProperties().get(CONTEXT_KEY) instanceof Context context) {
            context.reconnect();
        }
    }

    /// Copies owner context as an inherited default, leaving explicit popup-root installations authoritative.
    public static void inherit(Node node, M3Localizer localizer) {
        if (!node.hasProperties() || node.getProperties().get(INHERITED_KEY) != localizer) {
            node.getProperties().put(INHERITED_KEY, localizer);
            if (node.getProperties().get(CONTEXT_KEY) instanceof Context context) {
                context.reconnect();
            }
        }
    }

    /// Defers fallback construction until an unlocalized node actually needs it.
    @NotNullByDefault
    private static final class Fallback {
        /// English messages with the process's initial observed regional format settings.
        private static final M3Localizer INSTANCE = new M3Localizer(Locale.getDefault(Locale.Category.FORMAT));

        /// Prevents instantiation.
        private Fallback() {
        }
    }

    /// Shares one parent subscription among a node's consumers.
    @NotNullByDefault
    private static final class Context {
        /// The node owning this context.
        private final Node node;

        /// Active subscriptions, including those representing child contexts.
        private final ArrayList<M3LocalizerObserver> observers = new ArrayList<>();

        /// The parent whose inherited context is currently observed.
        private @Nullable Node observedParent;

        /// The subscription to that parent's shared context.
        private @Nullable M3LocalizerObserver parentObserver;

        /// Whether node listeners have been installed.
        private boolean started;

        /// Reconnects inheritance when this node changes parents.
        private final InvalidationListener parentListener = observable -> reconnect();

        /// Creates an inactive node-owned context.
        private Context(Node node) {
            this.node = node;
        }

        /// Begins shared observation after the first consumer has registered.
        private void start() {
            if (!started) {
                started = true;
                node.parentProperty().addListener(parentListener);
                reconnect();
            }
        }

        /// Rebuilds the single inherited dependency, stopping at a direct installation.
        private void reconnect() {
            @Nullable Node nextParent = node.getProperties().get(LOCALIZER_KEY) instanceof M3Localizer
                    || node.getProperties().get(INHERITED_KEY) instanceof M3Localizer
                    ? null : node.getParent();
            if (nextParent != observedParent) {
                observedParent = nextParent;
                @Nullable M3LocalizerObserver previous = parentObserver;
                parentObserver = null;
                if (previous != null) {
                    previous.close();
                }
                if (nextParent != null) {
                    M3LocalizerObserver replacement = new M3LocalizerObserver(nextParent, ignored -> refresh(), false);
                    // Installing node metadata can invoke application map listeners that dispose or reparent us.
                    if (started && observedParent == nextParent && parentObserver == null) {
                        parentObserver = replacement;
                    } else {
                        replacement.close();
                    }
                }
            }
            refresh();
        }

        /// Notifies current consumers; a snapshot permits callbacks to subscribe, reparent, or close observers.
        private void refresh() {
            for (M3LocalizerObserver observer : observers.toArray(M3LocalizerObserver[]::new)) {
                observer.refresh();
            }
        }

        /// Releases all ancestor references and node metadata after the final subscriber leaves.
        private void stop() {
            started = false;
            node.parentProperty().removeListener(parentListener);
            node.getProperties().remove(CONTEXT_KEY, this);
            observedParent = null;
            @Nullable M3LocalizerObserver previous = parentObserver;
            parentObserver = null;
            if (previous != null) {
                previous.close();
            }
        }
    }
}
