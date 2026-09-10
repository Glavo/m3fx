// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Requests keyboard focus and reports the target's immediate focus state.
@NotNullByDefault
public final class M3FocusRequests {
    /// Prevents utility class instantiation.
    private M3FocusRequests() {
    }

    /// Requests keyboard focus for the target node.
    ///
    /// Returns the value of [Node#isFocused()] immediately after [Node#requestFocus()]. A `true` result does not
    /// require a change of focus. Becoming the scene focus owner alone does not imply success:
    /// the target's window must also have input focus. This method does not wait for later window activation.
    ///
    /// @param target the node that should receive focus
    /// @return `true` when the target has input focus after the request
    /// @throws NullPointerException if `target` is `null`
    public static boolean requestFocus(Node target) {
        Objects.requireNonNull(target, "target");
        target.requestFocus();
        return target.isFocused();
    }

    /// Requests keyboard focus only when the target participates in focus traversal.
    ///
    /// Invokes [Node#requestFocus()] when [Node#isFocusTraversable()] is `true`. The request remains subject to
    /// JavaFX focus eligibility and does not guarantee that the target receives input focus.
    ///
    /// @param target the node that should receive focus
    /// @throws NullPointerException if `target` is `null`
    public static void requestFocusIfTraversable(Node target) {
        Objects.requireNonNull(target, "target");
        if (target.isFocusTraversable()) {
            target.requestFocus();
        }
    }
}
