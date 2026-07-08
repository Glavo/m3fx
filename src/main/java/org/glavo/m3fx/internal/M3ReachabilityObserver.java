// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/// Observes a node and its ancestor chain for reachability-affecting changes.
@NotNullByDefault
public final class M3ReachabilityObserver {
    /// The owner node whose reachability chain is observed.
    private final Node owner;

    /// Runs after any observed reachability-affecting property changes.
    private final Runnable invalidationAction;

    /// The nodes in the currently observed ancestor chain.
    private final Set<Node> observedNodes = Collections.newSetFromMap(new IdentityHashMap<>());

    /// Refreshes the observed chain and reports a reachability-affecting change.
    private final InvalidationListener invalidationListener = observable -> handleInvalidated();

    /// Whether the observer is currently installed.
    private boolean installed;

    /// Creates a reachability observer.
    ///
    /// @param owner the owner node whose reachability chain is observed
    /// @param invalidationAction the action to run after a reachability-affecting change
    public M3ReachabilityObserver(Node owner, Runnable invalidationAction) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.invalidationAction = Objects.requireNonNull(invalidationAction, "invalidationAction");
    }

    /// Starts observing the owner and its current ancestor chain.
    public void install() {
        if (installed) {
            return;
        }
        installed = true;
        refreshObservedChain();
    }

    /// Stops observing the owner and its ancestor chain.
    public void uninstall() {
        if (!installed) {
            return;
        }
        installed = false;
        removeObservedChain();
    }

    /// Refreshes ancestor listeners and reports a possible reachability change.
    private void handleInvalidated() {
        if (!installed) {
            return;
        }
        refreshObservedChain();
        invalidationAction.run();
    }

    /// Rebuilds the observed node chain from the owner to the current root.
    private void refreshObservedChain() {
        removeObservedChain();
        Node current = owner;
        while (true) {
            observe(current);
            Parent parent = current.getParent();
            if (parent == null) {
                break;
            }
            current = parent;
        }
    }

    /// Adds listeners to one node in the observed chain.
    private void observe(Node node) {
        if (!observedNodes.add(node)) {
            return;
        }
        node.visibleProperty().addListener(invalidationListener);
        node.disabledProperty().addListener(invalidationListener);
        node.parentProperty().addListener(invalidationListener);
        node.sceneProperty().addListener(invalidationListener);
    }

    /// Removes listeners from all currently observed nodes.
    private void removeObservedChain() {
        for (Node node : observedNodes) {
            node.visibleProperty().removeListener(invalidationListener);
            node.disabledProperty().removeListener(invalidationListener);
            node.parentProperty().removeListener(invalidationListener);
            node.sceneProperty().removeListener(invalidationListener);
        }
        observedNodes.clear();
    }
}