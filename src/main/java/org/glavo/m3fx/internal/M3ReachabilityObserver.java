// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Objects;

/// Observes a node and its ancestor chain for reachability-affecting changes.
@NotNullByDefault
public final class M3ReachabilityObserver {
    /// The owner node whose reachability chain is observed.
    private final Node owner;

    /// Runs after any observed reachability-affecting property changes.
    private final Runnable invalidationAction;

    /// The nodes in the currently observed ancestor chain.
    private final ArrayList<Node> observedNodes = new ArrayList<>();

    /// Reports a reachability-affecting state change without rebuilding the stable ancestor chain.
    private final InvalidationListener stateInvalidationListener = observable -> handleStateInvalidated();

    /// Refreshes the observed chain after one of its parent links changes.
    private final InvalidationListener parentInvalidationListener = observable -> handleParentInvalidated();

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

    /// Reports a possible reachability state change.
    private void handleStateInvalidated() {
        if (!installed) {
            return;
        }
        invalidationAction.run();
    }

    /// Refreshes ancestor listeners and reports a possible reachability change.
    private void handleParentInvalidated() {
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
            observedNodes.add(current);
            current.visibleProperty().addListener(stateInvalidationListener);
            current.parentProperty().addListener(parentInvalidationListener);
            Parent parent = current.getParent();
            if (parent == null) {
                break;
            }
            current = parent;
        }
        owner.disabledProperty().addListener(stateInvalidationListener);
        owner.sceneProperty().addListener(stateInvalidationListener);
    }

    /// Removes listeners from all currently observed nodes.
    private void removeObservedChain() {
        for (Node node : observedNodes) {
            node.visibleProperty().removeListener(stateInvalidationListener);
            node.parentProperty().removeListener(parentInvalidationListener);
        }
        if (!observedNodes.isEmpty()) {
            owner.disabledProperty().removeListener(stateInvalidationListener);
            owner.sceneProperty().removeListener(stateInvalidationListener);
        }
        observedNodes.clear();
    }
}