// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Shared child navigation helpers for M3FX selection containers.
@NotNullByDefault
final class M3SelectionNavigation {
    /// Prevents utility class instantiation.
    private M3SelectionNavigation() {
    }

    /// Returns the last child matching the requested type.
    static <T extends Node> @Nullable T last(ObservableList<Node> children, Class<T> type) {
        for (int index = children.size() - 1; index >= 0; index--) {
            Node child = children.get(index);
            if (type.isInstance(child)) {
                return type.cast(child);
            }
        }
        return null;
    }

    /// Returns the next matching child after the current child, wrapping at the end.
    static <T extends Node> @Nullable T next(ObservableList<Node> children, @Nullable T current, Class<T> type) {
        int childCount = children.size();
        if (childCount == 0) {
            return null;
        }

        int currentIndex = current == null ? -1 : children.indexOf(current);
        for (int offset = 1; offset <= childCount; offset++) {
            Node child = children.get(Math.floorMod(currentIndex + offset, childCount));
            if (type.isInstance(child)) {
                return type.cast(child);
            }
        }
        return null;
    }

    /// Returns the previous matching child before the current child, wrapping at the start.
    static <T extends Node> @Nullable T previous(ObservableList<Node> children, @Nullable T current, Class<T> type) {
        int childCount = children.size();
        if (childCount == 0) {
            return null;
        }

        int currentIndex = current == null ? childCount : children.indexOf(current);
        if (currentIndex < 0) {
            currentIndex = childCount;
        }

        for (int offset = 1; offset <= childCount; offset++) {
            Node child = children.get(Math.floorMod(currentIndex - offset, childCount));
            if (type.isInstance(child)) {
                return type.cast(child);
            }
        }
        return null;
    }
}
