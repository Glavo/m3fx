// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    /// Returns whether accessibility action parameters contain the requested selection target or one of its descendants.
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

    /// Returns whether accessibility action parameters reference the target node or one of its descendants.
    static boolean containsNodeTarget(Node target, Object... parameters) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsNodeTarget(target, parameter)) {
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

    /// Requests focus for one of the indexed items or the trailing item.
    static void showItem(ObservableList<? extends Node> items, @Nullable Node trailing, Object... parameters) {
        Objects.requireNonNull(items, "items");
        showItem(actionItem(items, trailing, parameters));
    }

    /// Requests focus for one of two optional indexed items.
    static void showItem(@Nullable Node first, @Nullable Node second, Object... parameters) {
        showItem(actionItem(first, second, parameters));
    }

    /// Requests focus for the default item when no parameter is supplied, or for the requested indexed item.
    static void showItemOrDefault(
            @Nullable Node defaultItem,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        @Nullable Node item = parameters.length == 0
                ? (focusTarget(defaultItem) == null ? firstFocusableItem(items) : defaultItem)
                : actionItem(items, parameters);
        showItem(item);
    }

    /// Requests focus for an accessibility item when it can be reached.
    static void showItem(@Nullable Node item) {
        @Nullable Node focusTarget = focusTarget(item);
        if (focusTarget != null) {
            focusTarget.requestFocus();
        }
    }

    /// Returns a node's currently exposed accessibility focus target when available.
    static @Nullable Node accessibleFocusTarget(@Nullable Node item) {
        if (!canReach(item)) {
            return null;
        }
        @Nullable Object focusNode = item.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && canReach(node)) {
            return node;
        }
        return focusTarget(item);
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

    /// Returns the current focus target inside the supplied item list, or the first focusable item.
    static @Nullable Node currentOrFirstFocusTarget(Node owner, ObservableList<? extends Node> items) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, items);
        return currentTarget != null ? currentTarget : firstFocusTarget(items);
    }

    /// Returns the current focus target inside a leading item or item list, or the first focusable item.
    static @Nullable Node currentOrFirstFocusTarget(
            Node owner,
            @Nullable Node leading,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, leading, items);
        return currentTarget != null ? currentTarget : firstFocusTarget(leading, items);
    }

    /// Returns the current focus target inside an item list or trailing item, or the first focusable item.
    static @Nullable Node currentOrFirstFocusTarget(
            Node owner,
            ObservableList<? extends Node> items,
            @Nullable Node trailing
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, items, trailing);
        return currentTarget != null ? currentTarget : firstFocusTarget(items, trailing);
    }

    /// Returns the current focus target inside either optional child node, or the first focusable item.
    static @Nullable Node currentOrFirstFocusTarget(Node owner, @Nullable Node first, @Nullable Node second) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node currentTarget = currentFocusTarget(owner, first, second);
        return currentTarget != null ? currentTarget : firstFocusTarget(first, second);
    }

    /// Returns the current focus owner when it belongs to one item in the supplied list.
    static @Nullable Node currentFocusTarget(Node owner, ObservableList<? extends Node> items) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        for (Node item : items) {
            @Nullable Node target = containedFocusTarget(item, focusOwner);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns the current focus owner when it belongs to a leading item or one item in the supplied list.
    static @Nullable Node currentFocusTarget(
            Node owner,
            @Nullable Node leading,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node leadingTarget = containedFocusTarget(leading, focusOwner);
        if (leadingTarget != null) {
            return leadingTarget;
        }

        for (Node item : items) {
            @Nullable Node target = containedFocusTarget(item, focusOwner);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns the current focus owner when it belongs to one item in the supplied list or a trailing item.
    static @Nullable Node currentFocusTarget(
            Node owner,
            ObservableList<? extends Node> items,
            @Nullable Node trailing
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        for (Node item : items) {
            @Nullable Node target = containedFocusTarget(item, focusOwner);
            if (target != null) {
                return target;
            }
        }

        return containedFocusTarget(trailing, focusOwner);
    }

    /// Returns the current focus owner when it belongs to either optional child node.
    static @Nullable Node currentFocusTarget(Node owner, @Nullable Node first, @Nullable Node second) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node firstTarget = containedFocusTarget(first, focusOwner);
        return firstTarget != null ? firstTarget : containedFocusTarget(second, focusOwner);
    }

    /// Returns the current scene focus owner for an owner node.
    private static @Nullable Node focusOwner(Node owner) {
        @Nullable Scene scene = owner.getScene();
        return scene == null ? null : scene.getFocusOwner();
    }

    /// Returns a focus owner when it is contained by an item that can expose focus.
    private static @Nullable Node containedFocusTarget(@Nullable Node item, Node focusOwner) {
        if (item == null) {
            return null;
        }
        @Nullable Node itemFocusTarget = focusTarget(item);
        if (itemFocusTarget == null || !containsNode(item, focusOwner)) {
            return null;
        }
        return canReach(focusOwner) ? focusOwner : itemFocusTarget;
    }

    /// Returns whether a node can receive a direct or descendant focus request.
    static boolean canReach(@Nullable Node node) {
        return node != null && node.isVisible() && !node.isDisabled() && node.getScene() != null;
    }

    /// Notifies ancestor nodes that a descendant's accessible focus target changed.
    static void notifyFocusNodeChangedInAncestors(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            parent.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            parent = parent.getParent();
        }
    }

    /// Returns whether the possible ancestor contains the requested descendant node.
    static boolean containsNode(Node possibleAncestor, Node possibleDescendant) {
        Objects.requireNonNull(possibleAncestor, "possibleAncestor");
        Objects.requireNonNull(possibleDescendant, "possibleDescendant");
        if (possibleAncestor == possibleDescendant) {
            return true;
        }
        if (possibleAncestor instanceof M3ListItem listItem
                && (containsOptionalNode(possibleAncestor, listItem.getLeading(), possibleDescendant)
                || containsOptionalNode(possibleAncestor, listItem.getTrailing(), possibleDescendant))) {
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

    /// Returns whether a nullable logical content node contains the requested descendant.
    private static boolean containsOptionalNode(Node owner, @Nullable Node possibleAncestor, Node possibleDescendant) {
        if (possibleAncestor == null) {
            return false;
        }
        if (possibleAncestor == possibleDescendant) {
            return true;
        }
        return possibleAncestor != owner && containsNode(possibleAncestor, possibleDescendant);
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

    /// Returns the indexed item or the item containing the node referenced by accessibility parameters.
    static @Nullable Node containingItem(ObservableList<? extends Node> items, Object... parameters) {
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
            @Nullable Node item = containingItem(items, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns whether one accessibility action parameter references the requested selection target or one of its descendants.
    private static boolean containsSelectionTarget(Node target, @Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return containsNode(target, node);
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

    /// Returns whether one accessibility action parameter references a target node or its descendant.
    private static boolean containsNodeTarget(Node target, @Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return containsNode(target, node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (containsNodeTarget(target, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (containsNodeTarget(target, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns the indexed item or the item containing one referenced node.
    private static @Nullable Node containingItem(ObservableList<? extends Node> items, @Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return itemAt(items, number);
        }
        if (parameter instanceof Node node) {
            for (Node item : items) {
                if (containsNode(item, node)) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = containingItem(items, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = containingItem(items, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the child item referenced by accessibility action parameters.
    static @Nullable Node actionItem(ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return firstFocusableItem(items);
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

    /// Returns a single node or contained descendant referenced by accessibility action parameters.
    static @Nullable Node actionItem(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return focusTarget(item) == null ? null : item;
        }
        for (Object parameter : parameters) {
            @Nullable Node target = actionItem(item, parameter);
            if (target != null) {
                return target;
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
            @Nullable Node leadingTarget = containedActionTarget(leading, node);
            if (leadingTarget != null) {
                return leadingTarget;
            }
            for (Node item : items) {
                @Nullable Node target = containedActionTarget(item, node);
                if (target != null) {
                    return target;
                }
            }
            return null;
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

    /// Returns the child or trailing item referenced by accessibility action parameters.
    private static @Nullable Node actionItem(
            ObservableList<? extends Node> items,
            @Nullable Node trailing,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            @Nullable Node item = firstFocusableItem(items);
            return item != null ? item : (focusTarget(trailing) == null ? null : trailing);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(items, trailing, parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(items, trailing, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the child or trailing item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(
            ObservableList<? extends Node> items,
            @Nullable Node trailing,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Number number) {
            return itemAt(items, trailing, number);
        }
        if (parameter instanceof Node node) {
            for (Node item : items) {
                @Nullable Node target = containedActionTarget(item, node);
                if (target != null) {
                    return target;
                }
            }
            @Nullable Node trailingTarget = containedActionTarget(trailing, node);
            if (trailingTarget != null) {
                return trailingTarget;
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(items, trailing, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(items, trailing, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the first or second optional item referenced by accessibility action parameters.
    private static @Nullable Node actionItem(@Nullable Node first, @Nullable Node second, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return focusTarget(first) != null ? first : (focusTarget(second) == null ? null : second);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(first, second, firstParameter);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(first, second, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the first or second optional item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(@Nullable Node first, @Nullable Node second, @Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return itemAt(first, second, number);
        }
        if (parameter instanceof Node node) {
            @Nullable Node firstTarget = containedActionTarget(first, node);
            if (firstTarget != null) {
                return firstTarget;
            }
            @Nullable Node secondTarget = containedActionTarget(second, node);
            if (secondTarget != null) {
                return secondTarget;
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, value);
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
            for (Node item : items) {
                @Nullable Node target = containedActionTarget(item, node);
                if (target != null) {
                    return target;
                }
            }
            return null;
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

    /// Returns the action target contained by one item for a requested node.
    private static @Nullable Node containedActionTarget(@Nullable Node item, Node requestedNode) {
        if (item == null || !containsNode(item, requestedNode)) {
            return null;
        }
        return focusTarget(requestedNode) == null ? item : requestedNode;
    }

    /// Returns the single-node action target referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(@Nullable Node item, @Nullable Object parameter) {
        if (item == null) {
            return null;
        }
        if (parameter instanceof Number number) {
            return number.intValue() == 0 ? item : null;
        }
        if (parameter instanceof Node node) {
            return containedActionTarget(item, node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node target = actionItem(item, value);
                if (target != null) {
                    return target;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node target = actionItem(item, value);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    /// Returns the indexed first or second optional item.
    private static @Nullable Node itemAt(@Nullable Node first, @Nullable Node second, Object... parameters) {
        int index = indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (first != null) {
            if (index == 0) {
                return first;
            }
            index--;
        }
        return index == 0 ? second : null;
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
