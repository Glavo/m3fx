// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import org.glavo.m3fx.internal.M3ScrollReveal;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/// Shared child navigation helpers for M3FX selection containers.
@NotNullByDefault
final class M3SelectionNavigation {
    /// The fallback row height used for page navigation before rows have been measured.
    private static final double DEFAULT_PAGE_ROW_HEIGHT = 56.0;

    /// The fallback page step used before the owner has a measured viewport height.
    private static final int DEFAULT_PAGE_STEP = 5;

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

    /// Returns the enabled visible child reached by page navigation without wrapping around list edges.
    static <T extends Node> @Nullable T page(
            Node owner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean forward
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");

        int childCount = children.size();
        if (childCount == 0) {
            return null;
        }

        @Nullable T target = current != null && children.contains(current) && selectable(current, type) != null
                ? current
                : null;
        if (target == null) {
            return forward ? first(children, type) : last(children, type);
        }

        int step = pageStep(owner, children, type);
        for (int offset = 0; offset < step; offset++) {
            @Nullable T next = forward
                    ? nextWithoutWrap(children, target, type)
                    : previousWithoutWrap(children, target, type);
            if (next == null) {
                return target;
            }
            target = next;
        }
        return target;
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
        return handleKeySelectionWithRevealOwner(event, null, children, current, type, horizontal, vertical, false, selector);
    }

    /// Handles a navigation key event, selects the matching child, and reveals it when a scroll owner exists.
    static <T extends Node> boolean handleKeySelection(
            KeyEvent event,
            Node owner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            Consumer<T> selector
    ) {
        return handleKeySelectionWithRevealOwner(event, owner, children, current, type, horizontal, vertical, false, selector);
    }

    /// Handles a navigation key event and selects the matching child when a key applies.
    ///
    /// When `rightToLeft` is true, horizontal arrow keys are mirrored after an anchor exists so focus and
    /// selection move in the same visual direction as the rendered row.
    static <T extends Node> boolean handleKeySelection(
            KeyEvent event,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            boolean rightToLeft,
            Consumer<T> selector
    ) {
        return handleKeySelectionWithRevealOwner(event, null, children, current, type, horizontal, vertical, rightToLeft, selector);
    }

    /// Handles a navigation key event, selects the matching child, and reveals it when a scroll owner exists.
    ///
    /// When `rightToLeft` is true, horizontal arrow keys are mirrored after an anchor exists so focus and
    /// selection move in the same visual direction as the rendered row.
    static <T extends Node> boolean handleKeySelection(
            KeyEvent event,
            Node owner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            boolean rightToLeft,
            Consumer<T> selector
    ) {
        Objects.requireNonNull(owner, "owner");
        return handleKeySelectionWithRevealOwner(event, owner, children, current, type, horizontal, vertical, rightToLeft, selector);
    }

    /// Handles a navigation key event and selects the matching child when a key applies.
    private static <T extends Node> boolean handleKeySelectionWithRevealOwner(
            KeyEvent event,
            @Nullable Node revealOwner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            boolean rightToLeft,
            Consumer<T> selector
    ) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(selector, "selector");
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return false;
        }

        @Nullable T target = targetFromKey(event.getCode(), children, current, type, horizontal, vertical, rightToLeft);
        if (target == null) {
            return false;
        }

        selector.accept(target);
        focusAndRevealTarget(revealOwner, target);
        event.consume();
        return true;
    }

    /// Handles a page navigation key event and selects the matching child when a key applies.
    static <T extends Node> boolean handlePageKeySelection(
            KeyEvent event,
            Node owner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            Consumer<T> selector
    ) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(selector, "selector");
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return false;
        }
        @Nullable T target = pageTargetFromKey(event.getCode(), owner, children, current, type);
        if (target == null) {
            return false;
        }

        selector.accept(target);
        focusAndRevealTarget(owner, target);
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
        return handleKeyFocusWithRevealOwner(event, null, children, current, type, horizontal, vertical, false);
    }

    /// Handles a navigation key event, focuses the matching child, and reveals it when a scroll owner exists.
    static <T extends Node> boolean handleKeyFocus(
            KeyEvent event,
            Node owner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical
    ) {
        return handleKeyFocusWithRevealOwner(event, owner, children, current, type, horizontal, vertical, false);
    }

    /// Handles a navigation key event and focuses the matching child when a key applies.
    ///
    /// When `rightToLeft` is true, horizontal arrow keys are mirrored after an anchor exists so focus moves in the
    /// same visual direction as the rendered row.
    static <T extends Node> boolean handleKeyFocus(
            KeyEvent event,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            boolean rightToLeft
    ) {
        return handleKeyFocusWithRevealOwner(event, null, children, current, type, horizontal, vertical, rightToLeft);
    }

    /// Handles a navigation key event, focuses the matching child, and reveals it when a scroll owner exists.
    ///
    /// When `rightToLeft` is true, horizontal arrow keys are mirrored after an anchor exists so focus moves in the
    /// same visual direction as the rendered row.
    static <T extends Node> boolean handleKeyFocus(
            KeyEvent event,
            Node owner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            boolean rightToLeft
    ) {
        return handleKeyFocusWithRevealOwner(event, owner, children, current, type, horizontal, vertical, rightToLeft);
    }

    /// Handles a navigation key event and focuses the matching child when a key applies.
    private static <T extends Node> boolean handleKeyFocusWithRevealOwner(
            KeyEvent event,
            @Nullable Node revealOwner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            boolean rightToLeft
    ) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return false;
        }

        @Nullable T target = targetFromKey(event.getCode(), children, current, type, horizontal, vertical, rightToLeft);
        if (target == null) {
            return false;
        }

        if (!focusAndRevealTarget(revealOwner, target)) {
            return false;
        }
        event.consume();
        return true;
    }

    /// Handles a page navigation key event and focuses the matching child when a key applies.
    static <T extends Node> boolean handlePageKeyFocus(
            KeyEvent event,
            Node owner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type
    ) {
        Objects.requireNonNull(event, "event");
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return false;
        }
        @Nullable T target = pageTargetFromKey(event.getCode(), owner, children, current, type);
        if (target == null) {
            return false;
        }

        if (!focusAndRevealTarget(owner, target)) {
            return false;
        }
        event.consume();
        return true;
    }

    /// Moves focus to the target and reveals it through the owner when available.
    private static boolean focusAndRevealTarget(@Nullable Node revealOwner, Node target) {
        Objects.requireNonNull(target, "target");
        if (revealOwner == null) {
            return M3Accessible.showItem(target);
        }
        return M3Accessible.showItem(revealOwner, target);
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

    /// Returns the focused child when present, otherwise the current child when it is still navigable.
    static <T extends Node> @Nullable T focusAnchor(
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type
    ) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");

        @Nullable T focused = focused(children, type);
        if (focused != null) {
            return focused;
        }
        if (current != null && children.contains(current) && selectable(current, type) != null) {
            return current;
        }
        return null;
    }

    /// Returns the focused child, the current child, or the first navigable child as an accessibility focus target.
    static <T extends Node> @Nullable T focusTarget(
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type
    ) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");

        @Nullable T anchor = focusAnchor(children, current, type);
        return anchor == null ? first(children, type) : anchor;
    }

    /// Returns the next enabled visible child whose normalized text starts with the supplied prefix.
    static <T extends Node> @Nullable T typeAheadTarget(
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            String prefix,
            Function<T, String> textProvider
    ) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(textProvider, "textProvider");
        if (prefix.isEmpty()) {
            return null;
        }

        int childCount = children.size();
        int currentIndex = current == null ? -1 : children.indexOf(current);
        for (int offset = 1; offset <= childCount; offset++) {
            @Nullable T selectable = selectable(
                    children.get(Math.floorMod(currentIndex + offset, childCount)),
                    type
            );
            if (selectable != null && normalizeTypeAheadText(textProvider.apply(selectable)).startsWith(prefix)) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns text normalized for case-insensitive type-ahead matching.
    static String normalizeTypeAheadText(String text) {
        Objects.requireNonNull(text, "text");
        return text.strip().toLowerCase(Locale.ROOT);
    }

    /// Returns the selection target implied by a navigation key.
    private static <T extends Node> @Nullable T targetFromKey(
            KeyCode keyCode,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            boolean rightToLeft
    ) {
        return switch (keyCode) {
            case LEFT -> horizontal ? horizontalTarget(children, current, type, false, rightToLeft) : null;
            case RIGHT -> horizontal ? horizontalTarget(children, current, type, true, rightToLeft) : null;
            case UP -> vertical ? previous(children, current, type) : null;
            case DOWN -> vertical ? next(children, current, type) : null;
            case HOME -> first(children, type);
            case END -> last(children, type);
            default -> null;
        };
    }

    /// Returns a horizontal navigation target, mirroring anchored movement in right-to-left layouts.
    private static <T extends Node> @Nullable T horizontalTarget(
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type,
            boolean rightKey,
            boolean rightToLeft
    ) {
        if (current == null) {
            return rightKey ? first(children, type) : last(children, type);
        }

        boolean forward = rightToLeft != rightKey;
        return forward ? next(children, current, type) : previous(children, current, type);
    }

    /// Returns the page navigation target implied by a key.
    private static <T extends Node> @Nullable T pageTargetFromKey(
            KeyCode keyCode,
            Node owner,
            ObservableList<Node> children,
            @Nullable T current,
            Class<T> type
    ) {
        return switch (keyCode) {
            case PAGE_UP -> page(owner, children, current, type, false);
            case PAGE_DOWN -> page(owner, children, current, type, true);
            default -> null;
        };
    }

    /// Returns the next enabled visible child after the current child without wrapping.
    private static <T extends Node> @Nullable T nextWithoutWrap(
            ObservableList<Node> children,
            T current,
            Class<T> type
    ) {
        int currentIndex = children.indexOf(current);
        for (int index = Math.max(0, currentIndex + 1); index < children.size(); index++) {
            @Nullable T selectable = selectable(children.get(index), type);
            if (selectable != null) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns the previous enabled visible child before the current child without wrapping.
    private static <T extends Node> @Nullable T previousWithoutWrap(
            ObservableList<Node> children,
            T current,
            Class<T> type
    ) {
        int currentIndex = children.indexOf(current);
        for (int index = Math.min(currentIndex - 1, children.size() - 1); index >= 0; index--) {
            @Nullable T selectable = selectable(children.get(index), type);
            if (selectable != null) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns the page navigation step for a list-like owner and its visible child rows.
    private static <T extends Node> int pageStep(Node owner, ObservableList<Node> children, Class<T> type) {
        double viewportHeight = M3ScrollReveal.pageViewportHeight(owner);
        double rowHeight = estimatedRowHeight(children, type);
        if (viewportHeight <= 0.0 || rowHeight <= 0.0) {
            return DEFAULT_PAGE_STEP;
        }
        return Math.max(1, (int) Math.floor(viewportHeight / rowHeight));
    }

    /// Returns the best available row height estimate for a child list.
    private static <T extends Node> double estimatedRowHeight(ObservableList<Node> children, Class<T> type) {
        for (Node child : children) {
            @Nullable T selectable = selectable(child, type);
            if (selectable == null) {
                continue;
            }

            double height = selectable.getLayoutBounds().getHeight();
            if (height <= 0.0 && selectable instanceof Region region) {
                height = region.prefHeight(-1.0);
            }
            if (height > 0.0) {
                return height;
            }
        }
        return DEFAULT_PAGE_ROW_HEIGHT;
    }

    /// Returns the child when it is a reachable instance of the requested type.
    private static <T extends Node> @Nullable T selectable(Node child, Class<T> type) {
        if (type.isInstance(child) && M3Accessible.isEffectivelyReachable(child)) {
            return type.cast(child);
        }
        return null;
    }
}
