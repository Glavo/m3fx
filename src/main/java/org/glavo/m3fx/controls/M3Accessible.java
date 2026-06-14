// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

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

    /// Returns the number of indexed accessibility items with three optional child slots.
    static int itemCount(@Nullable Node first, @Nullable Node second, @Nullable Node third) {
        return (first == null ? 0 : 1) + (second == null ? 0 : 1) + (third == null ? 0 : 1);
    }

    /// Returns the indexed accessibility item from three optional child slots.
    static @Nullable Node itemAt(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
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
        if (second != null) {
            if (index == 0) {
                return second;
            }
            index--;
        }
        return index == 0 ? third : null;
    }

    /// Returns the number of indexed accessibility items with two optional leading slots and a trailing list.
    static int itemCount(@Nullable Node first, @Nullable Node second, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        return (first == null ? 0 : 1) + (second == null ? 0 : 1) + items.size();
    }

    /// Returns the indexed accessibility item from two optional leading slots and a trailing list.
    static @Nullable Node itemAt(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
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
        if (second != null) {
            if (index == 0) {
                return second;
            }
            index--;
        }
        return index < items.size() ? items.get(index) : null;
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

    /// Returns whether an item exposes the supplied target through its accessibility item tree.
    static boolean containsAccessibleActionTarget(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (item == null || parameters.length == 0) {
            return false;
        }

        for (Object parameter : parameters) {
            Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            if (containsAccessibleActionTarget(item, parameter, visited)) {
                return true;
            }
        }
        return false;
    }

    /// Requests focus for the item referenced by accessibility action parameters.
    static void showItem(ObservableList<? extends Node> items, Object... parameters) {
        showItemOrAccessibleActionTarget(actionItem(items, parameters), items, parameters);
    }

    /// Requests focus for the leading item or one of the indexed trailing items.
    static void showItem(@Nullable Node leading, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        showItemOrAccessibleActionTarget(actionItem(leading, items, parameters), leading, items, parameters);
    }

    /// Requests focus for one of the indexed items or the trailing item.
    static void showItem(ObservableList<? extends Node> items, @Nullable Node trailing, Object... parameters) {
        Objects.requireNonNull(items, "items");
        showItemOrAccessibleActionTarget(actionItem(items, trailing, parameters), items, trailing, parameters);
    }

    /// Requests focus for one of two optional indexed items.
    static void showItem(@Nullable Node first, @Nullable Node second, Object... parameters) {
        showItemOrAccessibleActionTarget(actionItem(first, second, parameters), first, second, parameters);
    }

    /// Requests focus for one of three optional indexed items.
    static void showItem(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        showItemOrAccessibleActionTarget(actionItem(first, second, third, parameters), first, second, third, parameters);
    }

    /// Requests focus for one of two optional leading items or an indexed trailing item.
    static void showItem(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        showItemOrAccessibleActionTarget(actionItem(first, second, items, parameters), first, second, items, parameters);
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
        showItemOrAccessibleActionTarget(item, items, parameters);
    }

    /// Requests focus for the current focus target in an indexed container, or for the requested item.
    static void showCurrentOrItem(Node owner, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            showItem(currentOrFirstFocusTarget(owner, items));
        } else {
            showItem(items, parameters);
        }
    }

    /// Requests focus for the current focus target in a leading/list container, or for the requested item.
    static void showCurrentOrItem(
            Node owner,
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            showItem(currentOrFirstFocusTarget(owner, leading, items));
        } else {
            showItem(leading, items, parameters);
        }
    }

    /// Requests focus for the current focus target in a list/trailing container, or for the requested item.
    static void showCurrentOrItem(
            Node owner,
            ObservableList<? extends Node> items,
            @Nullable Node trailing,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            showItem(currentOrFirstFocusTarget(owner, items, trailing));
        } else {
            showItem(items, trailing, parameters);
        }
    }

    /// Requests focus for the current focus target among two optional children, or for the requested item.
    static void showCurrentOrItem(Node owner, @Nullable Node first, @Nullable Node second, Object... parameters) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            showItem(currentOrFirstFocusTarget(owner, first, second));
        } else {
            showItem(first, second, parameters);
        }
    }

    /// Requests focus for the current focus target among three optional children, or for the requested item.
    static void showCurrentOrItem(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            showItem(currentOrFirstFocusTarget(owner, first, second, third));
        } else {
            showItem(first, second, third, parameters);
        }
    }

    /// Requests focus for the current focus target in two leading slots or a trailing list.
    static void showCurrentOrItem(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            showItem(currentOrFirstFocusTarget(owner, first, second, items));
        } else {
            showItem(first, second, items, parameters);
        }
    }

    /// Requests focus for an accessibility item when it can be reached.
    static void showItem(@Nullable Node item) {
        showItemIfPresent(item);
    }

    /// Requests focus for an accessibility item when it can be reached.
    private static boolean showItemIfPresent(@Nullable Node item) {
        @Nullable Node focusTarget = currentContainedFocusTarget(item);
        if (focusTarget == null) {
            focusTarget = accessibleFocusTarget(item);
        }
        if (focusTarget != null) {
            focusTarget.requestFocus();
            return true;
        }
        return false;
    }

    /// Focuses a direct action target or delegates explicit reveal to nested accessible popup owners.
    private static void showItemOrAccessibleActionTarget(
            @Nullable Node item,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        if (!showItemIfPresent(item) && parameters.length > 0) {
            showAccessibleActionTarget(items, parameters);
        }
    }

    /// Focuses a direct action target or delegates explicit reveal to a leading/list child.
    private static void showItemOrAccessibleActionTarget(
            @Nullable Node item,
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        if (!showItemIfPresent(item) && parameters.length > 0 && !showAccessibleActionTarget(leading, parameters)) {
            showAccessibleActionTarget(items, parameters);
        }
    }

    /// Focuses a direct action target or delegates explicit reveal to a list/trailing child.
    private static void showItemOrAccessibleActionTarget(
            @Nullable Node item,
            ObservableList<? extends Node> items,
            @Nullable Node trailing,
            Object... parameters
    ) {
        if (!showItemIfPresent(item) && parameters.length > 0 && !showAccessibleActionTarget(items, parameters)) {
            showAccessibleActionTarget(trailing, parameters);
        }
    }

    /// Focuses a direct action target or delegates explicit reveal to either optional child.
    private static void showItemOrAccessibleActionTarget(
            @Nullable Node item,
            @Nullable Node first,
            @Nullable Node second,
            Object... parameters
    ) {
        if (!showItemIfPresent(item) && parameters.length > 0 && !showAccessibleActionTarget(first, parameters)) {
            showAccessibleActionTarget(second, parameters);
        }
    }

    /// Focuses a direct action target or delegates explicit reveal to any of three optional children.
    private static void showItemOrAccessibleActionTarget(
            @Nullable Node item,
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        if (!showItemIfPresent(item)
                && parameters.length > 0
                && !showAccessibleActionTarget(first, parameters)
                && !showAccessibleActionTarget(second, parameters)) {
            showAccessibleActionTarget(third, parameters);
        }
    }

    /// Focuses a direct action target or delegates explicit reveal to two leading slots or a trailing list.
    private static void showItemOrAccessibleActionTarget(
            @Nullable Node item,
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        if (!showItemIfPresent(item)
                && parameters.length > 0
                && !showAccessibleActionTarget(first, parameters)
                && !showAccessibleActionTarget(second, parameters)) {
            showAccessibleActionTarget(items, parameters);
        }
    }

    /// Delegates an explicit reveal request to the first child that exposes the requested accessibility target.
    static boolean showAccessibleActionTarget(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (!canReach(item) || parameters.length == 0) {
            return false;
        }

        @Nullable Node directTarget = actionItem(item, parameters);
        if (showItemIfPresent(directTarget)) {
            return true;
        }

        if (containsAccessibleActionTarget(item, parameters)) {
            item.executeAccessibleAction(AccessibleAction.SHOW_ITEM, parameters);
            return true;
        }

        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (showAccessibleActionTarget(child, parameters)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Delegates an explicit reveal request to the first indexed child that exposes the requested target.
    private static boolean showAccessibleActionTarget(
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            if (showAccessibleActionTarget(item, parameters)) {
                return true;
            }
        }
        return false;
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
        return focusTargetInReachableTree(item);
    }

    /// Returns a focusable item or descendant without requiring attachment to a scene.
    ///
    /// This helper is for structural reveal APIs that can decide which node would receive focus before a control is
    /// attached to a live scene. Callers that actually request keyboard focus must still check [canReach].
    static @Nullable Node structuralFocusTarget(@Nullable Node item) {
        if (!isEffectivelyReachable(item)) {
            return null;
        }
        return focusTargetInReachableTree(Objects.requireNonNull(item, "item"));
    }

    /// Returns a focusable item or descendant in an already reachable tree.
    private static @Nullable Node focusTargetInReachableTree(Node item) {
        if (item.isFocusTraversable()) {
            return item;
        }
        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node focusTarget = structuralFocusTarget(child);
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
        return firstAccessibleFocusTarget(items);
    }

    /// Returns the leading focus target, or the first focusable item in the supplied item list.
    static @Nullable Node firstFocusTarget(@Nullable Node leading, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        @Nullable Node leadingTarget = accessibleFocusTarget(leading);
        return leadingTarget != null ? leadingTarget : firstAccessibleFocusTarget(items);
    }

    /// Returns the first focusable item in the supplied item list, or the trailing focus target.
    static @Nullable Node firstFocusTarget(ObservableList<? extends Node> items, @Nullable Node trailing) {
        Objects.requireNonNull(items, "items");
        @Nullable Node itemTarget = firstAccessibleFocusTarget(items);
        return itemTarget != null ? itemTarget : accessibleFocusTarget(trailing);
    }

    /// Returns the first focusable target among two optional child nodes.
    static @Nullable Node firstFocusTarget(@Nullable Node first, @Nullable Node second) {
        @Nullable Node firstTarget = accessibleFocusTarget(first);
        return firstTarget != null ? firstTarget : accessibleFocusTarget(second);
    }

    /// Returns the first focusable target among three optional child nodes.
    static @Nullable Node firstFocusTarget(@Nullable Node first, @Nullable Node second, @Nullable Node third) {
        @Nullable Node firstTarget = accessibleFocusTarget(first);
        if (firstTarget != null) {
            return firstTarget;
        }
        @Nullable Node secondTarget = accessibleFocusTarget(second);
        return secondTarget != null ? secondTarget : accessibleFocusTarget(third);
    }

    /// Returns the first focusable target among two optional child nodes and a trailing list.
    static @Nullable Node firstFocusTarget(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(items, "items");
        @Nullable Node firstTarget = accessibleFocusTarget(first);
        if (firstTarget != null) {
            return firstTarget;
        }
        @Nullable Node secondTarget = accessibleFocusTarget(second);
        return secondTarget != null ? secondTarget : firstAccessibleFocusTarget(items);
    }

    /// Returns the current focus target inside the supplied item list, or the first focusable item.
    static @Nullable Node currentOrFirstFocusTarget(Node owner, ObservableList<? extends Node> items) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, items);
        return currentTarget != null ? currentTarget : firstFocusTarget(items);
    }

    /// Returns the current focus target inside a selection container, or its selected or first reachable item.
    static <T extends Node> @Nullable Node currentOrSelectionFocusTarget(
            Node owner,
            ObservableList<Node> items,
            @Nullable T selectedItem,
            Class<T> itemType
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(itemType, "itemType");

        @Nullable Node currentTarget = currentFocusTarget(owner, items);
        if (currentTarget != null) {
            return currentTarget;
        }
        return focusTarget(M3SelectionNavigation.focusTarget(items, selectedItem, itemType));
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

    /// Returns the current focus target inside three optional child nodes, or the first focusable item.
    static @Nullable Node currentOrFirstFocusTarget(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third
    ) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node currentTarget = currentFocusTarget(owner, first, second, third);
        return currentTarget != null ? currentTarget : firstFocusTarget(first, second, third);
    }

    /// Returns the current focus target inside two optional child nodes and a list, or the first focusable item.
    static @Nullable Node currentOrFirstFocusTarget(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, first, second, items);
        return currentTarget != null ? currentTarget : firstFocusTarget(first, second, items);
    }

    /// Returns the current focus owner when it belongs to one item in the supplied list.
    static @Nullable Node currentFocusTarget(Node owner, ObservableList<? extends Node> items) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node externalTarget = activeExternalFocusTarget(owner, items);
        if (externalTarget != null) {
            return externalTarget;
        }

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
        @Nullable Node externalLeadingTarget = activeExternalFocusTarget(owner, leading);
        if (externalLeadingTarget != null) {
            return externalLeadingTarget;
        }
        @Nullable Node externalItemTarget = activeExternalFocusTarget(owner, items);
        if (externalItemTarget != null) {
            return externalItemTarget;
        }

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
        @Nullable Node externalItemTarget = activeExternalFocusTarget(owner, items);
        if (externalItemTarget != null) {
            return externalItemTarget;
        }
        @Nullable Node externalTrailingTarget = activeExternalFocusTarget(owner, trailing);
        if (externalTrailingTarget != null) {
            return externalTrailingTarget;
        }

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
        @Nullable Node externalFirstTarget = activeExternalFocusTarget(owner, first);
        if (externalFirstTarget != null) {
            return externalFirstTarget;
        }
        @Nullable Node externalSecondTarget = activeExternalFocusTarget(owner, second);
        if (externalSecondTarget != null) {
            return externalSecondTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node firstTarget = containedFocusTarget(first, focusOwner);
        return firstTarget != null ? firstTarget : containedFocusTarget(second, focusOwner);
    }

    /// Returns the current focus owner when it belongs to one of three optional child nodes.
    static @Nullable Node currentFocusTarget(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third
    ) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node externalFirstTarget = activeExternalFocusTarget(owner, first);
        if (externalFirstTarget != null) {
            return externalFirstTarget;
        }
        @Nullable Node externalSecondTarget = activeExternalFocusTarget(owner, second);
        if (externalSecondTarget != null) {
            return externalSecondTarget;
        }
        @Nullable Node externalThirdTarget = activeExternalFocusTarget(owner, third);
        if (externalThirdTarget != null) {
            return externalThirdTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node firstTarget = containedFocusTarget(first, focusOwner);
        if (firstTarget != null) {
            return firstTarget;
        }
        @Nullable Node secondTarget = containedFocusTarget(second, focusOwner);
        return secondTarget != null ? secondTarget : containedFocusTarget(third, focusOwner);
    }

    /// Returns the current focus owner when it belongs to one of two optional children or a list item.
    static @Nullable Node currentFocusTarget(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node externalFirstTarget = activeExternalFocusTarget(owner, first);
        if (externalFirstTarget != null) {
            return externalFirstTarget;
        }
        @Nullable Node externalSecondTarget = activeExternalFocusTarget(owner, second);
        if (externalSecondTarget != null) {
            return externalSecondTarget;
        }
        @Nullable Node externalItemTarget = activeExternalFocusTarget(owner, items);
        if (externalItemTarget != null) {
            return externalItemTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node firstTarget = containedFocusTarget(first, focusOwner);
        if (firstTarget != null) {
            return firstTarget;
        }
        @Nullable Node secondTarget = containedFocusTarget(second, focusOwner);
        if (secondTarget != null) {
            return secondTarget;
        }
        for (Node item : items) {
            @Nullable Node itemTarget = containedFocusTarget(item, focusOwner);
            if (itemTarget != null) {
                return itemTarget;
            }
        }
        return null;
    }

    /// Returns the current scene focus owner for an owner node.
    private static @Nullable Node focusOwner(Node owner) {
        @Nullable Scene scene = owner.getScene();
        return scene == null ? null : scene.getFocusOwner();
    }

    /// Returns an active external focus target exposed by one item outside the owner subtree.
    static @Nullable Node activeExternalFocusTarget(Node owner, @Nullable Node item) {
        Objects.requireNonNull(owner, "owner");
        if (!canReach(item)) {
            return null;
        }

        @Nullable Node tooltipFocusTarget = M3Tooltip.activeInstalledTooltipFocusTarget(item);
        if (tooltipFocusTarget != null && isActiveExternalFocusTarget(owner, tooltipFocusTarget)) {
            @Nullable Node itemFocusTarget = focusTarget(item);
            return itemFocusTarget != null ? itemFocusTarget : item;
        }

        @Nullable Object focusNode = item.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && isActiveExternalFocusTarget(owner, node)) {
            return node;
        }

        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node childTarget = activeExternalFocusTarget(owner, child);
                if (childTarget != null) {
                    return childTarget;
                }
            }
        }
        return null;
    }

    /// Returns the first active external focus target exposed by one item list.
    private static @Nullable Node activeExternalFocusTarget(Node owner, ObservableList<? extends Node> items) {
        for (Node item : items) {
            @Nullable Node target = activeExternalFocusTarget(owner, item);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns whether a focus target belongs to a live popup or overlay outside the owner subtree.
    private static boolean isActiveExternalFocusTarget(Node owner, Node focusTarget) {
        if (!canReach(focusTarget) || containsNode(owner, focusTarget)) {
            return false;
        }

        @Nullable Scene focusTargetScene = focusTarget.getScene();
        @Nullable Node focusOwner = focusTargetScene == null ? null : focusTargetScene.getFocusOwner();
        return focusOwner != null && containsNode(focusTarget, focusOwner);
    }

    /// Returns a focus owner when it is contained by an item that can expose focus.
    private static @Nullable Node containedFocusTarget(@Nullable Node item, Node focusOwner) {
        if (item == null) {
            return null;
        }
        @Nullable Node itemFocusTarget = accessibleFocusTarget(item);
        if (itemFocusTarget == null || !containsNode(item, focusOwner)) {
            return null;
        }
        return canReach(focusOwner) && focusOwner != item ? focusOwner : itemFocusTarget;
    }

    /// Returns the current scene focus owner when it is already inside the supplied item.
    private static @Nullable Node currentContainedFocusTarget(@Nullable Node item) {
        if (!canReach(item)) {
            return null;
        }
        @Nullable Scene scene = item.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner == null || !canReach(focusOwner) || !containsNode(item, focusOwner)) {
            return null;
        }
        return focusOwner;
    }

    /// Returns whether a node can receive a direct or descendant focus request.
    static boolean canReach(@Nullable Node node) {
        return node != null && node.getScene() != null && isEffectivelyReachable(node);
    }

    /// Returns whether a node can be revealed from a collapsed or hidden-self state.
    ///
    /// Unlike [canReach], this allows the node itself to be invisible because several Material surfaces use
    /// visibility to represent their collapsed state. The node may be detached for structural tests, but it must be
    /// enabled and every ancestor must be visible and enabled.
    static boolean canReveal(@Nullable Node node) {
        if (node == null || node.isDisabled()) {
            return false;
        }

        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (!parent.isVisible() || parent.isDisabled()) {
                return false;
            }
            parent = parent.getParent();
        }
        return true;
    }

    /// Returns whether a node and its ancestor chain are visible and enabled.
    static boolean isEffectivelyReachable(@Nullable Node node) {
        @Nullable Node current = node;
        while (current != null) {
            if (!current.isVisible() || current.isDisabled()) {
                return false;
            }
            current = current.getParent();
        }
        return node != null;
    }

    /// Notifies a node and its ancestors that the node's exposed accessibility focus target changed.
    static void notifyFocusNodeChanged(Node node) {
        Objects.requireNonNull(node, "node");
        node.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        notifyFocusNodeChangedInAncestors(node);
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

    /// Returns whether an item exposes one accessibility action target parameter.
    private static boolean containsAccessibleActionTarget(
            Node item,
            @Nullable Object parameter,
            Set<Node> visited
    ) {
        if (parameter instanceof Node node) {
            return containsAccessibleNode(item, node, visited);
        }
        if (containsPickerValueTarget(item, parameter)) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                Set<Node> branchVisited = Collections.newSetFromMap(new IdentityHashMap<>());
                if (containsAccessibleActionTarget(item, value, branchVisited)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                Set<Node> branchVisited = Collections.newSetFromMap(new IdentityHashMap<>());
                if (containsAccessibleActionTarget(item, value, branchVisited)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether an item can reveal the supplied picker value target through `SHOW_ITEM`.
    private static boolean containsPickerValueTarget(Node item, @Nullable Object parameter) {
        if (parameter instanceof LocalDate) {
            return item instanceof M3DatePicker
                    || item instanceof M3DateRangePicker
                    || item instanceof M3DatePickerField
                    || item instanceof M3DateRangePickerField;
        }
        if (parameter instanceof LocalTime) {
            return item instanceof M3TimePicker || item instanceof M3TimePickerField;
        }
        return false;
    }

    /// Returns whether an owner node exposes a requested node directly or through indexed accessibility children.
    private static boolean containsAccessibleNode(Node owner, Node requestedNode, Set<Node> visited) {
        if (!visited.add(owner)) {
            return false;
        }
        if (owner == requestedNode || containsNode(owner, requestedNode)) {
            return true;
        }

        @Nullable Object itemCountValue = owner.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT);
        if (!(itemCountValue instanceof Number itemCountNumber)) {
            return false;
        }

        int itemCount = Math.max(0, itemCountNumber.intValue());
        for (int index = 0; index < itemCount; index++) {
            @Nullable Object child = owner.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, index);
            if (child instanceof Node childNode && containsAccessibleNode(childNode, requestedNode, visited)) {
                return true;
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
            return accessibleFocusTarget(item) == null ? null : item;
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
            return accessibleFocusTarget(leading) != null ? leading : firstFocusableItem(items);
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
            return item != null ? item : (accessibleFocusTarget(trailing) == null ? null : trailing);
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
            return accessibleFocusTarget(first) != null
                    ? first
                    : (accessibleFocusTarget(second) == null ? null : second);
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

    /// Returns one of three optional items referenced by accessibility action parameters.
    private static @Nullable Node actionItem(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            if (accessibleFocusTarget(first) != null) {
                return first;
            }
            return accessibleFocusTarget(second) != null
                    ? second
                    : (accessibleFocusTarget(third) == null ? null : third);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(first, second, third, firstParameter);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(first, second, third, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns one of three optional items referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Number number) {
            return itemAt(first, second, third, number);
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
            @Nullable Node thirdTarget = containedActionTarget(third, node);
            if (thirdTarget != null) {
                return thirdTarget;
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, third, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, third, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns one of two optional leading items or a trailing-list item referenced by action parameters.
    private static @Nullable Node actionItem(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            if (accessibleFocusTarget(first) != null) {
                return first;
            }
            @Nullable Node secondTarget = accessibleFocusTarget(second);
            return secondTarget != null ? second : firstFocusableItem(items);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(first, second, items, firstParameter);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(first, second, items, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns one of two optional leading items or a trailing-list item referenced by one action parameter.
    private static @Nullable Node actionItem(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Number number) {
            return itemAt(first, second, items, number);
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
                @Nullable Node item = actionItem(first, second, items, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, items, value);
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
            if (accessibleFocusTarget(item) != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the first exposed accessible focus target from a list.
    private static @Nullable Node firstAccessibleFocusTarget(ObservableList<? extends Node> items) {
        for (Node item : items) {
            @Nullable Node target = accessibleFocusTarget(item);
            if (target != null) {
                return target;
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
        return accessibleFocusTarget(requestedNode) == null ? item : requestedNode;
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
