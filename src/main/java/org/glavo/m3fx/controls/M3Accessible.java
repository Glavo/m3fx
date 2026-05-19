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

    /// Returns the number of indexed accessibility items with an optional leading item.
    static int itemCount(@Nullable Node leading, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        return (leading == null ? 0 : 1) + items.size();
    }

    /// Returns the indexed accessibility item from an optional leading item and trailing item list.
    static @Nullable Node itemAt(@Nullable Node leading, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        int index = indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (leading != null) {
            if (index == 0) {
                return leading;
            }
            index--;
        }
        return index < items.size() ? items.get(index) : null;
    }

    /// Returns the number of indexed accessibility items with an optional trailing item.
    static int itemCount(ObservableList<? extends Node> items, @Nullable Node trailing) {
        Objects.requireNonNull(items, "items");
        return items.size() + (trailing == null ? 0 : 1);
    }

    /// Returns the indexed accessibility item from a leading item list and optional trailing item.
    static @Nullable Node itemAt(ObservableList<? extends Node> items, @Nullable Node trailing, Object... parameters) {
        Objects.requireNonNull(items, "items");
        int index = indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (index < items.size()) {
            return items.get(index);
        }
        return index == items.size() ? trailing : null;
    }

    /// Returns whether accessibility action parameters contain the requested selection target.
    static boolean containsSelectionTarget(Node target, Object... parameters) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsSelectionTarget(target, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Requests focus for the item referenced by accessibility action parameters.
    static void showItem(ObservableList<? extends Node> items, Object... parameters) {
        @Nullable Node item = actionItem(items, parameters);
        if (item != null && item.isVisible() && !item.isDisabled()) {
            item.requestFocus();
        }
    }

    /// Returns the first child item referenced by accessibility selection parameters.
    static <T extends Node> @Nullable T firstSelectionTarget(
            ObservableList<? extends Node> items,
            Class<T> itemType,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(itemType, "itemType");
        for (Node item : items) {
            if (itemType.isInstance(item) && containsSelectionTarget(item, parameters)) {
                return itemType.cast(item);
            }
        }
        return null;
    }

    /// Returns whether one accessibility action parameter references the requested selection target.
    private static boolean containsSelectionTarget(Node target, @Nullable Object parameter) {
        if (parameter == target) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (containsSelectionTarget(target, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (containsSelectionTarget(target, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns the child item referenced by accessibility action parameters.
    private static @Nullable Node actionItem(ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return null;
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(items, parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(items, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the child item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(ObservableList<? extends Node> items, @Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return items.contains(node) ? node : null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(items, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(items, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
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
