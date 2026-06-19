// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/// Shared keyboard focus traversal helpers for Material containers.
@NotNullByDefault
final class M3FocusTraversal {
    /// Prevents instantiation.
    private M3FocusTraversal() {
    }

    /// Handles horizontal keyboard traversal across the supplied focus targets.
    static boolean handleHorizontalKeyFocus(Node owner, KeyEvent event, List<Node> focusableItems) {
        return handleDirectionalKeyFocus(owner, event, focusableItems, true, false);
    }

    /// Handles directional keyboard traversal across the supplied focus targets.
    static boolean handleDirectionalKeyFocus(
            Node owner,
            KeyEvent event,
            List<Node> focusableItems,
            boolean horizontalEnabled,
            boolean verticalEnabled
    ) {
        return handleDirectionalKeyFocus(owner, event, focusableItems, horizontalEnabled, verticalEnabled, -1);
    }

    /// Handles directional keyboard traversal across the supplied focus targets using a fallback focused index.
    static boolean handleDirectionalKeyFocus(
            Node owner,
            KeyEvent event,
            List<Node> focusableItems,
            boolean horizontalEnabled,
            boolean verticalEnabled,
            int fallbackFocusedIndex
    ) {
        return handleDirectionalKeyFocus(
                owner,
                event,
                focusableItems,
                horizontalEnabled,
                verticalEnabled,
                fallbackFocusedIndex,
                true
        );
    }

    /// Handles directional keyboard traversal with explicit boundary wrapping behavior.
    static boolean handleDirectionalKeyFocus(
            Node owner,
            KeyEvent event,
            List<Node> focusableItems,
            boolean horizontalEnabled,
            boolean verticalEnabled,
            int fallbackFocusedIndex,
            boolean wrap
    ) {
        return handleDirectionalKeyFocus(
                owner,
                event,
                focusableItems,
                horizontalEnabled,
                verticalEnabled,
                fallbackFocusedIndex,
                wrap,
                event.getTarget() instanceof Node eventTarget ? eventTarget : null
        );
    }

    /// Handles directional keyboard traversal with an explicit event target fallback.
    static boolean handleDirectionalKeyFocus(
            Node owner,
            KeyEvent event,
            List<Node> focusableItems,
            boolean horizontalEnabled,
            boolean verticalEnabled,
            int fallbackFocusedIndex,
            boolean wrap,
            @Nullable Node eventTarget
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(focusableItems, "focusableItems");
        if (focusOwnerInsideTextInput(owner)) {
            return false;
        }

        List<Node> reachableFocusItems = reachableFocusItems(focusableItems);
        @Nullable Node target = directionalTarget(
                owner,
                eventTarget,
                event.getCode(),
                reachableFocusItems,
                horizontalEnabled,
                verticalEnabled,
                normalizedFallbackIndex(focusableItems, reachableFocusItems, fallbackFocusedIndex),
                wrap
        );
        if (target == null) {
            return false;
        }

        target.requestFocus();
        event.consume();
        return true;
    }

    /// Returns reachable focus targets from an optional leading node followed by a node list.
    static @Unmodifiable List<Node> focusTargets(@Nullable Node leading, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>();
        addFocusTarget(targets, leading);
        for (Node item : items) {
            addFocusTarget(targets, item);
        }
        return List.copyOf(targets);
    }

    /// Returns reachable focus targets from a node list.
    static @Unmodifiable List<Node> focusTargets(ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>();
        for (Node item : items) {
            addFocusTarget(targets, item);
        }
        return List.copyOf(targets);
    }

    /// Returns every reachable focus target discovered in each item subtree.
    static @Unmodifiable List<Node> focusTargetsInReachableTrees(ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>();
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Node item : items) {
            addFocusTargetsInReachableTree(targets, visited, item);
        }
        return List.copyOf(targets);
    }

    /// Returns every reachable focus target discovered in a nullable item subtree.
    static @Unmodifiable List<Node> focusTargetsInReachableTree(@Nullable Node item) {
        List<Node> targets = new ArrayList<>();
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        addFocusTargetsInReachableTree(targets, visited, item);
        return List.copyOf(targets);
    }

    /// Returns reachable focus targets from an arbitrary ordered item sequence.
    static @Unmodifiable List<Node> focusTargets(Iterable<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>();
        for (Node item : items) {
            addFocusTarget(targets, item);
        }
        return List.copyOf(targets);
    }

    /// Returns reachable focus targets from a node list followed by an optional trailing node.
    static @Unmodifiable List<Node> focusTargets(ObservableList<? extends Node> items, @Nullable Node trailing) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>();
        for (Node item : items) {
            addFocusTarget(targets, item);
        }
        addFocusTarget(targets, trailing);
        return List.copyOf(targets);
    }

    /// Returns reachable focus targets from two optional node slots.
    static @Unmodifiable List<Node> focusTargets(@Nullable Node first, @Nullable Node second) {
        List<Node> targets = new ArrayList<>();
        addFocusTarget(targets, first);
        addFocusTarget(targets, second);
        return List.copyOf(targets);
    }

    /// Returns whether the current scene focus owner is inside the supplied container.
    static boolean focusOwnerInside(Node owner, @Nullable Node container) {
        Objects.requireNonNull(owner, "owner");
        if (container == null) {
            return false;
        }

        @Nullable Scene scene = owner.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(container, focusOwner);
    }

    /// Returns whether the current scene focus owner is inside this owner and an editable text input.
    static boolean focusOwnerInsideTextInput(Node owner) {
        return M3FocusGuards.focusOwnerInsideTextInput(owner);
    }

    /// Consumes owner-level navigation keys when a descendant text input owns focus.
    static boolean consumeNavigationKeyIfFocusOwnerInsideTextInput(
            Node owner,
            KeyEvent event,
            boolean horizontalEnabled,
            boolean verticalEnabled
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(event, "event");
        if (focusOwnerInsideTextInput(owner)
                && isNavigationKeyForEnabledAxis(event.getCode(), horizontalEnabled, verticalEnabled)) {
            if (event.getTarget() == owner) {
                event.consume();
            }
            return true;
        }
        return false;
    }

    /// Adds one accessible focus target when the item can expose focus.
    private static void addFocusTarget(List<Node> targets, @Nullable Node item) {
        @Nullable Node focusTarget = M3Accessible.accessibleFocusTarget(item);
        if (focusTarget != null) {
            targets.add(focusTarget);
        }
    }

    /// Returns unique, currently reachable focus targets from a caller-supplied navigation list.
    private static @Unmodifiable List<Node> reachableFocusItems(List<Node> items) {
        if (items.isEmpty()) {
            return List.of();
        }

        List<Node> targets = new ArrayList<>(items.size());
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Node item : items) {
            if (M3Accessible.canReach(item) && visited.add(item)) {
                targets.add(item);
            }
        }
        return List.copyOf(targets);
    }

    /// Returns the fallback index after resolving a stale caller-supplied target list.
    private static int normalizedFallbackIndex(
            List<Node> originalItems,
            List<Node> reachableItems,
            int fallbackFocusedIndex
    ) {
        if (fallbackFocusedIndex < 0 || fallbackFocusedIndex >= originalItems.size()) {
            return -1;
        }

        Node fallbackTarget = originalItems.get(fallbackFocusedIndex);
        return M3Accessible.canReach(fallbackTarget) ? reachableItems.indexOf(fallbackTarget) : -1;
    }

    /// Adds all reachable focusable descendants in scene-graph order.
    private static void addFocusTargetsInReachableTree(
            List<Node> targets,
            Set<Node> visited,
            @Nullable Node item
    ) {
        if (!M3Accessible.canReach(item) || !visited.add(item)) {
            return;
        }

        int indexedChildCount = indexedChildCount(item);
        if (indexedChildCount == 0 && item.isFocusTraversable()) {
            targets.add(item);
        }
        addIndexedChildFocusTargets(targets, visited, item, indexedChildCount);
        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                addFocusTargetsInReachableTree(targets, visited, child);
            }
        }
    }

    /// Adds focus targets exposed through indexed accessibility children.
    private static void addIndexedChildFocusTargets(
            List<Node> targets,
            Set<Node> visited,
            Node item,
            int indexedChildCount
    ) {
        for (int index = 0; index < indexedChildCount; index++) {
            @Nullable Object child = item.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, index);
            if (child instanceof Node childNode && childNode != item) {
                addFocusTargetsInReachableTree(targets, visited, childNode);
            }
        }
    }

    /// Returns the non-negative indexed accessibility child count for a node.
    private static int indexedChildCount(Node item) {
        @Nullable Object itemCountValue = item.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT);
        return itemCountValue instanceof Number itemCountNumber ? Math.max(0, itemCountNumber.intValue()) : 0;
    }

    /// Returns whether a key belongs to a navigation axis controlled by the owner.
    private static boolean isNavigationKeyForEnabledAxis(
            KeyCode keyCode,
            boolean horizontalEnabled,
            boolean verticalEnabled
    ) {
        return switch (keyCode) {
            case LEFT, RIGHT -> horizontalEnabled;
            case UP, DOWN, PAGE_UP, PAGE_DOWN -> verticalEnabled;
            case HOME, END -> horizontalEnabled || verticalEnabled;
            default -> false;
        };
    }

    /// Returns the focus target selected by a directional navigation key.
    private static @Nullable Node directionalTarget(
            Node owner,
            @Nullable Node eventTarget,
            KeyCode keyCode,
            List<Node> focusableItems,
            boolean horizontalEnabled,
            boolean verticalEnabled,
            int fallbackFocusedIndex,
            boolean wrap
    ) {
        if (focusableItems.isEmpty()) {
            return null;
        }

        int focusedIndex = focusedTargetIndex(owner, focusableItems, eventTarget);
        if (focusedIndex < 0 && fallbackFocusedIndex >= 0 && fallbackFocusedIndex < focusableItems.size()) {
            focusedIndex = fallbackFocusedIndex;
        }

        return switch (keyCode) {
            case HOME -> focusableItems.get(0);
            case END -> focusableItems.get(focusableItems.size() - 1);
            case LEFT -> horizontalEnabled ? horizontalArrowTarget(owner, focusableItems, focusedIndex, false, wrap) : null;
            case RIGHT -> horizontalEnabled ? horizontalArrowTarget(owner, focusableItems, focusedIndex, true, wrap) : null;
            case UP -> verticalEnabled ? verticalArrowTarget(focusableItems, focusedIndex, false, wrap) : null;
            case DOWN -> verticalEnabled ? verticalArrowTarget(focusableItems, focusedIndex, true, wrap) : null;
            default -> null;
        };
    }

    /// Returns the target selected by a horizontal arrow key.
    private static @Nullable Node horizontalArrowTarget(
            Node owner,
            List<Node> focusableItems,
            int focusedIndex,
            boolean rightKey,
            boolean wrap
    ) {
        boolean forward = M3SelectionNavigation.isRightToLeft(owner) != rightKey;
        if (focusedIndex < 0) {
            return focusableItems.get(forward ? 0 : focusableItems.size() - 1);
        }
        return adjacentTarget(focusableItems, focusedIndex, forward, wrap);
    }

    /// Returns the target selected by a vertical arrow key.
    private static @Nullable Node verticalArrowTarget(
            List<Node> focusableItems,
            int focusedIndex,
            boolean downKey,
            boolean wrap
    ) {
        if (focusedIndex < 0) {
            return focusableItems.get(downKey ? 0 : focusableItems.size() - 1);
        }
        return adjacentTarget(focusableItems, focusedIndex, downKey, wrap);
    }

    /// Returns the adjacent focusable item, optionally wrapping at container ends.
    private static @Nullable Node adjacentTarget(
            List<Node> focusableItems,
            int focusedIndex,
            boolean forward,
            boolean wrap
    ) {
        int itemCount = focusableItems.size();
        int targetIndex = focusedIndex + (forward ? 1 : -1);
        if (wrap) {
            targetIndex = Math.floorMod(targetIndex, itemCount);
        } else if (targetIndex < 0 || targetIndex >= itemCount) {
            return null;
        }
        return focusableItems.get(targetIndex);
    }

    /// Returns the index of the focused target in the supplied target list.
    static int focusedTargetIndex(Node owner, List<Node> targets) {
        return focusedTargetIndex(owner, targets, null);
    }

    /// Returns the index of the focused target or event target in the supplied target list.
    private static int focusedTargetIndex(Node owner, List<Node> targets, @Nullable Node eventTarget) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(targets, "targets");

        @Nullable Scene scene = owner.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        for (int index = 0; index < targets.size(); index++) {
            Node target = targets.get(index);
            if (target.isFocused() || target == focusOwner) {
                return index;
            }
        }
        if (eventTarget != null) {
            for (int index = 0; index < targets.size(); index++) {
                Node target = targets.get(index);
                if (target == eventTarget) {
                    return index;
                }
            }
        }
        for (int index = 0; index < targets.size(); index++) {
            Node target = targets.get(index);
            if (focusOwner != null && M3Accessible.containsNode(target, focusOwner)) {
                return index;
            }
        }
        if (eventTarget != null) {
            for (int index = 0; index < targets.size(); index++) {
                Node target = targets.get(index);
                if (M3Accessible.containsNode(target, eventTarget)) {
                    return index;
                }
            }
        }
        return -1;
    }
}
