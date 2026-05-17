// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/// Shared child navigation helpers for M3FX selection containers.
@NotNullByDefault
final class M3SelectionNavigation {
    /// Prevents utility class instantiation.
    private M3SelectionNavigation() {
    }

    /// Returns the first enabled visible child matching the requested type.
    static <T extends Node> @Nullable T first(ObservableList<Node> children, Class<T> type) {
        for (Node child : children) {
            @Nullable T selectable = selectable(child, type);
            if (selectable != null) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns the last enabled visible child matching the requested type.
    static <T extends Node> @Nullable T last(ObservableList<Node> children, Class<T> type) {
        for (int index = children.size() - 1; index >= 0; index--) {
            @Nullable T selectable = selectable(children.get(index), type);
            if (selectable != null) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns the next enabled visible child after the current child, wrapping at the end.
    static <T extends Node> @Nullable T next(ObservableList<Node> children, @Nullable T current, Class<T> type) {
        int childCount = children.size();
        if (childCount == 0) {
            return null;
        }

        int currentIndex = current == null ? -1 : children.indexOf(current);
        for (int offset = 1; offset <= childCount; offset++) {
            @Nullable T selectable = selectable(children.get(Math.floorMod(currentIndex + offset, childCount)), type);
            if (selectable != null) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns the previous enabled visible child before the current child, wrapping at the start.
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
            @Nullable T selectable = selectable(children.get(Math.floorMod(currentIndex - offset, childCount)), type);
            if (selectable != null) {
                return selectable;
            }
        }
        return null;
    }

    /// Handles a navigation key event and selects the matching child when a key applies.
    static <T extends Node> boolean handleKeySelection(
            KeyEvent event,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            Consumer<T> selector
    ) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(selector, "selector");

        @Nullable T target = targetFromKey(event.getCode(), children, current, type, horizontal, vertical);
        if (target == null) {
            return false;
        }

        selector.accept(target);
        if (target.isFocusTraversable()) {
            target.requestFocus();
        }
        event.consume();
        return true;
    }

    /// Handles a navigation key event and focuses the matching child when a key applies.
    static <T extends Node> boolean handleKeyFocus(
            KeyEvent event,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical
    ) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");

        @Nullable T target = targetFromKey(event.getCode(), children, current, type, horizontal, vertical);
        if (target == null) {
            return false;
        }

        target.requestFocus();
        event.consume();
        return true;
    }

    /// Returns the focused enabled visible child matching the requested type.
    static <T extends Node> @Nullable T focused(ObservableList<Node> children, Class<T> type) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");

        for (Node child : children) {
            @Nullable T selectable = selectable(child, type);
            if (selectable != null && selectable.isFocused()) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns the selection target implied by a navigation key.
    private static <T extends Node> @Nullable T targetFromKey(
            KeyCode keyCode,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical
    ) {
        return switch (keyCode) {
            case LEFT -> horizontal ? previous(children, current, type) : null;
            case RIGHT -> horizontal ? next(children, current, type) : null;
            case UP -> vertical ? previous(children, current, type) : null;
            case DOWN -> vertical ? next(children, current, type) : null;
            case HOME -> first(children, type);
            case END -> last(children, type);
            default -> null;
        };
    }

    /// Returns the child when it is an enabled visible instance of the requested type.
    private static <T extends Node> @Nullable T selectable(Node child, Class<T> type) {
        if (type.isInstance(child) && !child.isDisabled() && child.isVisible()) {
            return type.cast(child);
        }
        return null;
    }
}
