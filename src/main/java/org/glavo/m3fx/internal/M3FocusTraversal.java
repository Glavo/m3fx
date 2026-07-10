// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
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
public final class M3FocusTraversal {
    /// The fallback row height used for page focus traversal before targets have been measured.
    private static final double DEFAULT_PAGE_ROW_HEIGHT = 56.0;

    /// The fallback page step used before the owner has a measured viewport height.
    private static final int DEFAULT_PAGE_STEP = 5;

    /// Prevents instantiation.
    private M3FocusTraversal() {
    }

    /// Handles horizontal keyboard traversal across the supplied focus targets.
    public static boolean handleHorizontalKeyFocus(Node owner, KeyEvent event, List<Node> focusableItems) {
        return handleDirectionalKeyFocus(owner, event, focusableItems, true, false);
    }

    /// Handles directional keyboard traversal across the supplied focus targets.
    public static boolean handleDirectionalKeyFocus(
            Node owner,
            KeyEvent event,
            List<Node> focusableItems,
            boolean horizontalEnabled,
            boolean verticalEnabled
    ) {
        return handleDirectionalKeyFocus(owner, event, focusableItems, horizontalEnabled, verticalEnabled, -1);
    }

    /// Handles directional keyboard traversal across the supplied focus targets using a fallback focused index.
    public static boolean handleDirectionalKeyFocus(
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
    public static boolean handleDirectionalKeyFocus(
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
    public static boolean handleDirectionalKeyFocus(
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
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return false;
        }
        if (consumeNavigationKeyIfFocusOwnerInsideTextInput(owner, event, horizontalEnabled, verticalEnabled)) {
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

        boolean focused = target.isFocusTraversable()
                ? M3Accessible.showDirectItem(owner, target)
                : M3Accessible.showItem(owner, target);
        if (!focused) {
            return false;
        }
        event.consume();
        return true;
    }

    /// Handles cyclic Tab and F6 keyboard traversal across the supplied focus targets.
    public static boolean handleCyclicTabKeyFocus(Node owner, KeyEvent event, List<Node> focusableItems) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(focusableItems, "focusableItems");
        if (!isCyclicTraversalKey(event)) {
            return false;
        }

        List<Node> reachableFocusItems = reachableFocusItems(focusableItems);
        if (reachableFocusItems.isEmpty()) {
            event.consume();
            return true;
        }

        boolean backward = event.isShiftDown();
        int focusedIndex = focusedTargetIndex(owner, reachableFocusItems);
        int targetIndex;
        if (focusedIndex < 0) {
            targetIndex = backward ? reachableFocusItems.size() - 1 : 0;
        } else {
            targetIndex = Math.floorMod(focusedIndex + (backward ? -1 : 1), reachableFocusItems.size());
        }

        Node target = reachableFocusItems.get(targetIndex);
        if (target.isFocusTraversable()) {
            M3Accessible.showDirectItem(owner, target);
        } else {
            M3Accessible.showItem(owner, target);
        }
        event.consume();
        return true;
    }

    /// Returns whether an event requests cyclic focus traversal without application shortcut modifiers.
    private static boolean isCyclicTraversalKey(KeyEvent event) {
        if (event.getCode() != KeyCode.TAB && event.getCode() != KeyCode.F6) {
            return false;
        }
        return !event.isControlDown()
                && !event.isAltDown()
                && !event.isMetaDown()
                && !event.isShortcutDown();
    }

    /// Returns reachable focus targets from an optional leading node followed by a node list.
    public static @Unmodifiable List<Node> focusTargets(@Nullable Node leading, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>(items.size() + 1);
        addFocusTarget(targets, leading);
        for (Node item : items) {
            addFocusTarget(targets, item);
        }
        return List.copyOf(targets);
    }

    /// Returns reachable focus targets from a node list.
    public static @Unmodifiable List<Node> focusTargets(ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>(items.size());
        for (Node item : items) {
            addFocusTarget(targets, item);
        }
        return List.copyOf(targets);
    }

    /// Returns every reachable focus target discovered in each item subtree.
    public static @Unmodifiable List<Node> focusTargetsInReachableTrees(ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>();
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Node item : items) {
            addFocusTargetsInReachableTree(targets, visited, item);
        }
        return List.copyOf(targets);
    }

    /// Returns every reachable focus target discovered in a nullable item subtree.
    public static @Unmodifiable List<Node> focusTargetsInReachableTree(@Nullable Node item) {
        List<Node> targets = new ArrayList<>();
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        addFocusTargetsInReachableTree(targets, visited, item);
        return List.copyOf(targets);
    }

    /// Returns reachable focus targets from an arbitrary ordered item sequence.
    public static @Unmodifiable List<Node> focusTargets(Iterable<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>();
        for (Node item : items) {
            addFocusTarget(targets, item);
        }
        return List.copyOf(targets);
    }

    /// Returns reachable focus targets from a node list followed by an optional trailing node.
    public static @Unmodifiable List<Node> focusTargets(ObservableList<? extends Node> items, @Nullable Node trailing) {
        Objects.requireNonNull(items, "items");
        List<Node> targets = new ArrayList<>(items.size() + 1);
        for (Node item : items) {
            addFocusTarget(targets, item);
        }
        addFocusTarget(targets, trailing);
        return List.copyOf(targets);
    }

    /// Returns reachable focus targets from two optional node slots.
    public static @Unmodifiable List<Node> focusTargets(@Nullable Node first, @Nullable Node second) {
        List<Node> targets = new ArrayList<>(2);
        addFocusTarget(targets, first);
        addFocusTarget(targets, second);
        return List.copyOf(targets);
    }

    /// Returns whether the current scene focus owner is inside the supplied container.
    public static boolean focusOwnerInside(Node owner, @Nullable Node container) {
        Objects.requireNonNull(owner, "owner");
        if (container == null) {
            return false;
        }

        @Nullable Scene scene = owner.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(container, focusOwner);
    }

    /// Returns whether the current scene focus owner is inside this owner and an editable text input.
    public static boolean focusOwnerInsideTextInput(Node owner) {
        return M3FocusGuards.focusOwnerInsideTextInput(owner);
    }

    /// Consumes owner-level navigation keys when a descendant text input owns focus.
    public static boolean consumeNavigationKeyIfFocusOwnerInsideTextInput(
            Node owner,
            KeyEvent event,
            boolean horizontalEnabled,
            boolean verticalEnabled
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(event, "event");

        if (focusOwnerInsideTextInput(owner)
                && isNavigationKeyForEnabledAxis(event.getCode(), horizontalEnabled, verticalEnabled)) {
            if (!eventTargetsFocusedTextInput(owner, event)) {
                event.consume();
            }
            return true;
        }
        return false;
    }

    /// Returns whether the key event is targeted at the focused text input itself.
    private static boolean eventTargetsFocusedTextInput(Node owner, KeyEvent event) {
        @Nullable Scene scene = owner.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        return focusOwner != null
                && (eventNodeBelongsToFocusOwner(focusOwner, event.getTarget())
                || eventNodeBelongsToFocusOwner(focusOwner, event.getSource()));
    }

    /// Returns whether one event endpoint belongs to the current text input focus owner.
    private static boolean eventNodeBelongsToFocusOwner(Node focusOwner, @Nullable Object eventEndpoint) {
        return eventEndpoint instanceof Node node
                && (node == focusOwner || M3Accessible.containsNode(focusOwner, node));
    }
    /// Adds one accessible focus target when the item can expose focus.
    private static void addFocusTarget(List<Node> targets, @Nullable Node item) {
        @Nullable Node focusTarget = M3Accessible.accessibleFocusTarget(item);
        if (focusTarget != null) {
            targets.add(focusTarget);
        }
    }

    /// Returns unique, currently reachable focus targets from a caller-supplied navigation list.
    private static List<Node> reachableFocusItems(List<Node> items) {
        if (items.isEmpty()) {
            return List.of();
        }

        @Nullable ArrayList<Node> filteredTargets = null;
        for (int index = 0; index < items.size(); index++) {
            Node item = items.get(index);
            boolean duplicate = false;
            if (filteredTargets == null) {
                for (int previousIndex = 0; previousIndex < index; previousIndex++) {
                    if (items.get(previousIndex) == item) {
                        duplicate = true;
                        break;
                    }
                }
            } else {
                for (Node target : filteredTargets) {
                    if (target == item) {
                        duplicate = true;
                        break;
                    }
                }
            }

            if (M3Accessible.canReach(item) && !duplicate) {
                if (filteredTargets != null) {
                    filteredTargets.add(item);
                }
                continue;
            }

            if (filteredTargets == null) {
                filteredTargets = new ArrayList<>(items.size());
                for (int previousIndex = 0; previousIndex < index; previousIndex++) {
                    filteredTargets.add(items.get(previousIndex));
                }
            }
        }
        return filteredTargets == null ? items : filteredTargets;
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
            case PAGE_UP -> verticalEnabled ? pageTarget(owner, focusableItems, focusedIndex, false) : null;
            case PAGE_DOWN -> verticalEnabled ? pageTarget(owner, focusableItems, focusedIndex, true) : null;
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
        if (focusedIndex < 0) {
            return focusableItems.get(rightKey ? 0 : focusableItems.size() - 1);
        }

        boolean forward = M3NodeLayout.isRightToLeft(owner) != rightKey;
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

    /// Returns the target selected by a page-navigation key.
    private static Node pageTarget(
            Node owner,
            List<Node> focusableItems,
            int focusedIndex,
            boolean forward
    ) {
        if (focusedIndex < 0) {
            return focusableItems.get(forward ? 0 : focusableItems.size() - 1);
        }

        int step = pageStep(owner, focusableItems);
        int targetIndex = focusedIndex + (forward ? step : -step);
        targetIndex = Math.max(0, Math.min(focusableItems.size() - 1, targetIndex));
        return focusableItems.get(targetIndex);
    }

    /// Returns the page-navigation step for an owner and its focus targets.
    private static int pageStep(Node owner, List<Node> focusableItems) {
        double viewportHeight = M3ScrollReveal.pageViewportHeight(owner);
        double rowHeight = estimatedTargetHeight(focusableItems);
        if (viewportHeight <= 0.0 || rowHeight <= 0.0) {
            return DEFAULT_PAGE_STEP;
        }
        return Math.max(1, (int) Math.floor(viewportHeight / rowHeight));
    }

    /// Returns the best available height estimate for one focus target.
    private static double estimatedTargetHeight(List<Node> focusableItems) {
        for (Node item : focusableItems) {
            Bounds bounds = item.getLayoutBounds();
            double height = bounds.getHeight();
            if (height <= 0.0 && item instanceof Region region) {
                height = region.prefHeight(-1.0);
            }
            if (height > 0.0) {
                return height;
            }
        }
        return DEFAULT_PAGE_ROW_HEIGHT;
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
    public static int focusedTargetIndex(Node owner, List<Node> targets) {
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
