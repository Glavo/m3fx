// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides shared accessibility query helpers for M3FX controls.
@NotNullByDefault
final class M3Accessible {
    /// Prevents utility class instantiation.
    private M3Accessible() {
    }

    /// Returns the child requested by an accessibility index parameter.
    static @Nullable Node itemAt(ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        int index = indexParameter(parameters);
        return index >= 0 && index < items.size() ? items.get(index) : null;
    }

    /// Returns this node's index in its parent child list, or `-1` when it is detached.
    static int indexInParent(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Parent parent = node.getParent();
        return parent == null ? -1 : parent.getChildrenUnmodifiable().indexOf(node);
    }

    /// Returns the first integer accessibility parameter, or `-1` when none was supplied.
    static int indexParameter(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0 || !(parameters[0] instanceof Number number)) {
            return -1;
        }
        return number.intValue();
    }
}
