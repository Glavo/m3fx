// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides shared accessibility query helpers for M3FX controls.
@NotNullByDefault
final class M3Accessible {
    /// The node property key used to provide an accessibility index before a skin attaches nodes.
    private static final Object ACCESSIBLE_INDEX_ITEMS_KEY = new Object();

    /// Prevents utility class instantiation.
    private M3Accessible() {
    }

    /// Returns an accessibility attribute by name when the running JavaFX version provides it.
    static @Nullable AccessibleAttribute attribute(String name) {
        Objects.requireNonNull(name, "name");
        try {
            return AccessibleAttribute.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /// Notifies an optional accessibility attribute when the running JavaFX version provides it.
    static void notifyAttribute(Node node, @Nullable AccessibleAttribute attribute) {
        Objects.requireNonNull(node, "node");
        if (attribute != null) {
            node.notifyAccessibleAttributeChanged(attribute);
        }
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
        showItem(actionItem(items, parameters));
    }

    /// Requests focus for the leading item or one of the indexed trailing items.
    static void showItem(@Nullable Node leading, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        showItem(actionItem(leading, items, parameters));
    }

    /// Requests focus for an accessibility item when it can be reached.
    static void showItem(@Nullable Node item) {
        @Nullable Node focusTarget = focusTarget(item);
        if (focusTarget != null) {
            focusTarget.requestFocus();
        }
    }

    /// Returns the focusable item or descendant used for accessibility focus requests.
    static @Nullable Node focusTarget(@Nullable Node item) {
        if (!canReach(item)) {
            return null;
        }
        if (item.isFocusTraversable()) {
            return item;
        }
        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node focusTarget = focusTarget(child);
                if (focusTarget != null) {
                    return focusTarget;
                }
            }
        }
        return null;
    }

    /// Returns the first focusable item or descendant in the supplied item list.
    static @Nullable Node firstFocusTarget(ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        return firstFocusableItem(items);
    }

    /// Returns the leading focus target, or the first focusable item in the supplied item list.
    static @Nullable Node firstFocusTarget(@Nullable Node leading, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        @Nullable Node leadingTarget = focusTarget(leading);
        return leadingTarget != null ? leadingTarget : firstFocusableItem(items);
    }

    /// Returns the first focusable item in the supplied item list, or the trailing focus target.
    static @Nullable Node firstFocusTarget(ObservableList<? extends Node> items, @Nullable Node trailing) {
        Objects.requireNonNull(items, "items");
        @Nullable Node itemTarget = firstFocusableItem(items);
        return itemTarget != null ? itemTarget : focusTarget(trailing);
    }

    /// Returns the first focusable target among two optional child nodes.
    static @Nullable Node firstFocusTarget(@Nullable Node first, @Nullable Node second) {
        @Nullable Node firstTarget = focusTarget(first);
        return firstTarget != null ? firstTarget : focusTarget(second);
    }

    /// Returns whether a node can receive a direct or descendant focus request.
    static boolean canReach(@Nullable Node node) {
        return node != null && node.isVisible() && !node.isDisabled() && node.getScene() != null;
    }

    /// Returns whether the possible ancestor contains the requested descendant node.
    static boolean containsNode(Node possibleAncestor, Node possibleDescendant) {
        Objects.requireNonNull(possibleAncestor, "possibleAncestor");
        Objects.requireNonNull(possibleDescendant, "possibleDescendant");
        if (possibleAncestor == possibleDescendant) {
            return true;
        }
        if (possibleAncestor instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsNode(child, possibleDescendant)) {
                    return true;
                }
            }
        }
        return false;
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
    static @Nullable Node actionItem(ObservableList<? extends Node> items, Object... parameters) {
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

    /// Returns the leading or child item referenced by accessibility action parameters.
    private static @Nullable Node actionItem(
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return focusTarget(leading) != null ? leading : firstFocusableItem(items);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(leading, items, parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(leading, items, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the leading or child item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Number number) {
            return itemAt(leading, items, number);
        }
        if (parameter instanceof Node node) {
            if (node == leading) {
                return node;
            }
            return items.contains(node) ? node : null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(leading, items, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(leading, items, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the first child item with a focusable target from a list.
    private static @Nullable Node firstFocusableItem(ObservableList<? extends Node> items) {
        for (Node item : items) {
            if (focusTarget(item) != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the child item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(ObservableList<? extends Node> items, @Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return itemAt(items, number);
        }
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
        if (parent != null) {
            return parent.getChildrenUnmodifiable().indexOf(node);
        }
        @Nullable Object ownerItems = node.getProperties().get(ACCESSIBLE_INDEX_ITEMS_KEY);
        return ownerItems instanceof ObservableList<?> items ? items.indexOf(node) : -1;
    }

    /// Sets the owner item list used for accessibility index lookup before skin attachment.
    static void setIndexOwner(Node node, ObservableList<? extends Node> items) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(items, "items");
        node.getProperties().put(ACCESSIBLE_INDEX_ITEMS_KEY, items);
    }

    /// Clears the owner item list used for accessibility index lookup.
    static void clearIndexOwner(Node node) {
        Objects.requireNonNull(node, "node");
        node.getProperties().remove(ACCESSIBLE_INDEX_ITEMS_KEY);
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
