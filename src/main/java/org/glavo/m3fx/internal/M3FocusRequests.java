// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Internal helpers for requesting keyboard focus and reporting whether JavaFX accepted the request.
@NotNullByDefault
public final class M3FocusRequests {
    /// Prevents utility class instantiation.
    private M3FocusRequests() {
    }

    /// Requests keyboard focus for the target node.
    ///
    /// @param target the node that should receive focus
    /// @return `true` when the target became the scene focus owner
    public static boolean requestFocus(Node target) {
        Objects.requireNonNull(target, "target");
        target.requestFocus();
        return target.isFocused();
    }

    /// Requests keyboard focus only when the target participates in focus traversal.
    ///
    /// @param target the node that should receive focus
    public static void requestFocusIfTraversable(Node target) {
        Objects.requireNonNull(target, "target");
        if (target.isFocusTraversable()) {
            target.requestFocus();
        }
    }
}