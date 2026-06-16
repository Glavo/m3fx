// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(focusableItems, "focusableItems");
        if (focusOwnerInsideTextInput(owner)) {
            return false;
        }

        @Nullable Node target = directionalTarget(
                owner,
                event.getCode(),
                focusableItems,
                horizontalEnabled,
                verticalEnabled,
                fallbackFocusedIndex,
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

    /// Adds one accessible focus target when the item can expose focus.
    private static void addFocusTarget(List<Node> targets, @Nullable Node item) {
        @Nullable Node focusTarget = M3Accessible.accessibleFocusTarget(item);
        if (focusTarget != null) {
            targets.add(focusTarget);
        }
    }

    /// Returns the focus target selected by a directional navigation key.
    private static @Nullable Node directionalTarget(
            Node owner,
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

        int focusedIndex = focusedTargetIndex(owner, focusableItems);
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
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(targets, "targets");

        @Nullable Scene scene = owner.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        for (int index = 0; index < targets.size(); index++) {
            Node target = targets.get(index);
            if (target.isFocused() || focusOwner != null && M3Accessible.containsNode(target, focusOwner)) {
                return index;
            }
        }
        return -1;
    }
}
