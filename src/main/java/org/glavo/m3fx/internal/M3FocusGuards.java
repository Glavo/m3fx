// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
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
    /// @return `true` when the focus owner is inside `owner` and it or an ancestor is a [TextInputControl]
    /// @throws NullPointerException if `owner` is `null`
    public static boolean focusOwnerInsideTextInput(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Scene scene = owner.getScene();
        @Nullable Node current = scene == null ? null : scene.getFocusOwner();
        boolean insideOwner = false;
        boolean insideTextInput = false;
        while (current != null) {
            insideOwner |= current == owner;
            insideTextInput |= current instanceof TextInputControl;
            if (insideOwner && insideTextInput) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /// Returns whether the possible ancestor is the node itself or owns it through parent links.
    ///
    /// @param possibleAncestor the node that may contain the possible descendant
    /// @param possibleDescendant the node that may be contained by the possible ancestor
    /// @return `true` when the possible ancestor is the possible descendant or one of its parents
    /// @throws NullPointerException if either argument is `null`
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
