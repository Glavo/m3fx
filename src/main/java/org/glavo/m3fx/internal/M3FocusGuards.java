// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal focus guard predicates shared by keyboard handlers.
@NotNullByDefault
public final class M3FocusGuards {
    /// Prevents instantiation.
    private M3FocusGuards() {
    }

    /// Returns whether the current scene focus owner is inside the owner and belongs to a text input control.
    ///
    /// @param owner the component that owns the keyboard handler
    /// @return `true` when the current focus owner is a nested [TextInputControl]
    public static boolean focusOwnerInsideTextInput(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Scene scene = owner.getScene();
        @Nullable Node current = scene == null ? null : scene.getFocusOwner();
        if (current == null || !containsNode(owner, current)) {
            return false;
        }

        while (current != null) {
            if (current instanceof TextInputControl) {
                return true;
            }

            @Nullable Parent parent = current.getParent();
            current = parent;
        }
        return false;
    }

    /// Returns whether the possible ancestor is the node itself or owns it through parent links.
    ///
    /// @param possibleAncestor the node that may contain the possible descendant
    /// @param possibleDescendant the node that may be contained by the possible ancestor
    /// @return `true` when the possible ancestor is the possible descendant or one of its parents
    public static boolean containsNode(Node possibleAncestor, Node possibleDescendant) {
        Objects.requireNonNull(possibleAncestor, "possibleAncestor");
        Objects.requireNonNull(possibleDescendant, "possibleDescendant");

        @Nullable Node current = possibleDescendant;
        while (current != null) {
            if (current == possibleAncestor) {
                return true;
            }

            current = current.getParent();
        }
        return false;
    }
}
