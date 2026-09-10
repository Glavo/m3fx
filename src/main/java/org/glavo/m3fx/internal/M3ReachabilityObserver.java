// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/// Observes a node and its ancestor chain for reachability-affecting changes.
///
/// Tracks disabled state, scene attachment, parent links, and visibility. Visibility follows [M3TreeVisibility]:
/// native effective visibility is used when available; otherwise visible properties along public parent links are
/// observed. The fallback cannot observe visibility changes across a [javafx.scene.SubScene] root.
@NotNullByDefault
public final class M3ReachabilityObserver {
    /// The owner node whose reachability chain is observed.
    private final Node owner;

    /// Runs after any observed reachability-affecting property changes.
    private final Runnable invalidationAction;

    /// The native effective-visibility property, or `null` when public ancestor observation is required.
    private final @Nullable ObservableBooleanValue treeVisibleProperty;

    /// The nodes in the currently observed ancestor chain.
    private final ArrayList<Node> observedNodes = new ArrayList<>();

    /// Reports a reachability-affecting state change without rebuilding the stable ancestor chain.
    private final InvalidationListener stateInvalidationListener = observable -> handleStateInvalidated();

    /// Reports each native visibility transition without relying on the callback to read the property.
    private final ChangeListener<Boolean> treeVisibilityListener =
            (observable, oldValue, newValue) -> handleStateInvalidated();

    /// Refreshes the observed chain after one of its parent links changes.
    private final InvalidationListener parentInvalidationListener = observable -> handleParentInvalidated();

    /// Whether the observer is currently installed.
    private boolean installed;

    /// Creates a reachability observer.
    ///
    /// @param owner the owner node whose reachability chain is observed
    /// @param invalidationAction the action to run after a reachability-affecting change
    /// @throws NullPointerException if either argument is `null`
    public M3ReachabilityObserver(Node owner, Runnable invalidationAction) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.invalidationAction = Objects.requireNonNull(invalidationAction, "invalidationAction");
        treeVisibleProperty = M3TreeVisibility.treeVisibleProperty(owner);
    }

    /// Starts observing the owner and its current ancestor chain.
    ///
    /// Repeated calls while installed have no effect. Installation does not invoke the invalidation action.
    public void install() {
        if (installed) {
            return;
        }
        installed = true;
        owner.disabledProperty().addListener(stateInvalidationListener);
        owner.sceneProperty().addListener(stateInvalidationListener);
        if (treeVisibleProperty != null) {
            treeVisibleProperty.addListener(treeVisibilityListener);
        }
        refreshObservedChain();
    }

    /// Stops observing the owner and its ancestor chain.
    ///
    /// Repeated calls while uninstalled have no effect. The observer may subsequently be installed again.
    public void uninstall() {
        if (!installed) {
            return;
        }
        installed = false;
        removeObservedChain();
        owner.disabledProperty().removeListener(stateInvalidationListener);
        owner.sceneProperty().removeListener(stateInvalidationListener);
        if (treeVisibleProperty != null) {
            treeVisibleProperty.removeListener(treeVisibilityListener);
        }
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
            if (treeVisibleProperty == null) {
                current.visibleProperty().addListener(stateInvalidationListener);
            }
            current.parentProperty().addListener(parentInvalidationListener);
            @Nullable Parent parent = current.getParent();
            if (parent == null) {
                break;
            }
            current = parent;
        }
    }

    /// Removes listeners from all currently observed nodes.
    private void removeObservedChain() {
        for (Node node : observedNodes) {
            if (treeVisibleProperty == null) {
                node.visibleProperty().removeListener(stateInvalidationListener);
            }
            node.parentProperty().removeListener(parentInvalidationListener);
        }
        observedNodes.clear();
    }
}
