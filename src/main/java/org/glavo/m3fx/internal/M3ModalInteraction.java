// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNullByDefault;

/// Coordinates transient interaction feedback below modal presentation layers.
///
/// A blocked subtree remains visually and structurally present, but registered targets suppress transient pointer
/// and focus feedback until the subtree becomes interactive again. Markers are inherited through the parent chain,
/// which preserves blocking when overlay panes are nested.
@NotNullByDefault
public final class M3ModalInteraction {
    /// Opaque node-property key used to mark the root of a blocked subtree.
    private static final IdentityKey BLOCKED_KEY =
            new IdentityKey(M3ModalInteraction.class.getName() + ".blocked");

    /// Prevents construction.
    private M3ModalInteraction() {
    }

    /// Changes modal interaction blocking for a subtree and synchronizes existing feedback targets.
    ///
    /// Repeated calls with the current state have no effect. Newly created targets resolve inherited blocking when
    /// they install themselves, so an unchanged marker does not require another subtree traversal.
    ///
    /// @param root    the subtree root
    /// @param blocked whether transient interaction feedback below the root is blocked
    public static void setBlocked(Node root, boolean blocked) {
        boolean current = root.hasProperties() && root.getProperties().containsKey(BLOCKED_KEY);
        if (current == blocked) {
            return;
        }

        if (blocked) {
            root.getProperties().put(BLOCKED_KEY, Boolean.TRUE);
        } else {
            root.getProperties().remove(BLOCKED_KEY);
        }
        synchronizeSubtree(root);
    }

    /// Returns whether a node is inside a subtree blocked by an active modal presentation.
    ///
    /// @param node the node to inspect
    /// @return `true` when the node or one of its ancestors carries a blocking marker
    public static boolean isBlocked(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current.hasProperties() && current.getProperties().containsKey(BLOCKED_KEY)) {
                return true;
            }
        }
        return false;
    }

    /// Synchronizes registered targets below one changed marker.
    ///
    /// @param node the current subtree node
    private static void synchronizeSubtree(Node node) {
        if (node instanceof Target target) {
            target.setModalInteractionBlocked(isBlocked(node));
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                synchronizeSubtree(child);
            }
        }
    }

    /// Receives effective modal interaction state from an ancestor presentation host.
    @NotNullByDefault
    public interface Target {
        /// Applies effective modal interaction blocking to this target.
        ///
        /// @param blocked whether transient interaction feedback is blocked
        void setModalInteractionBlocked(boolean blocked);
    }
}
