// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/// Shared child navigation helpers for M3FX selection containers.
@NotNullByDefault
public final class M3SelectionNavigation {
    /// The fallback row height used for page navigation before rows have been measured.
    private static final double DEFAULT_PAGE_ROW_HEIGHT = 56.0;

    /// The fallback page step used before the owner has a measured viewport height.
    private static final int DEFAULT_PAGE_STEP = 5;

    /// Prevents utility class instantiation.
    private M3SelectionNavigation() {
    }

    /// Returns the first enabled visible child matching the requested type.
    ///
    /// @param <T>      the requested node type
    /// @param children the children in visual traversal order
    /// @param type     the node type eligible for selection
    /// @return the first reachable matching child, or `null` when none exists
    /// @throws NullPointerException if `children` or `type` is `null`
    public static <T extends Node> @Nullable T first(List<? extends Node> children, Class<T> type) {
        for (Node child : children) {
            @Nullable T selectable = selectable(child, type);
            if (selectable != null) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns the last enabled visible child matching the requested type.
    ///
    /// @param <T>      the requested node type
    /// @param children the children in visual traversal order
    /// @param type     the node type eligible for selection
    /// @return the last reachable matching child, or `null` when none exists
    /// @throws NullPointerException if `children` or `type` is `null`
    public static <T extends Node> @Nullable T last(List<? extends Node> children, Class<T> type) {
        for (int index = children.size() - 1; index >= 0; index--) {
            @Nullable T selectable = selectable(children.get(index), type);
            if (selectable != null) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns the next enabled visible child after the current child, wrapping at the end.
    ///
    /// @param <T>      the requested node type
    /// @param children the children in traversal order
    /// @param current  the current child, or `null` to begin before the first child
    /// @param type     the node type eligible for selection
    /// @return the next reachable matching child, or `null` when none exists
    /// @throws NullPointerException if `children` or `type` is `null`
    public static <T extends Node> @Nullable T next(List<? extends Node> children, @Nullable T current, Class<T> type) {
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
    ///
    /// @param <T>      the requested node type
    /// @param children the children in traversal order
    /// @param current  the current child, or `null` to begin after the last child
    /// @param type     the node type eligible for selection
    /// @return the previous reachable matching child, or `null` when none exists
    /// @throws NullPointerException if `children` or `type` is `null`
    public static <T extends Node> @Nullable T previous(List<? extends Node> children, @Nullable T current, Class<T> type) {
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
    ///
    /// The page step is derived from the owner's visible viewport and a measured row height, with stable fallback
    /// values before layout. When no valid current child is supplied, navigation begins at the first or last
    /// reachable child according to `forward`.
    ///
    /// @param <T>      the requested node type
    /// @param owner    the list-like node whose viewport determines the page size
    /// @param children the children in traversal order
    /// @param current  the current child, or `null`
    /// @param type     the node type eligible for selection
    /// @param forward  whether to move toward the end of the list
    /// @return the page target, or `null` when no reachable matching child exists
    /// @throws NullPointerException if `owner`, `children`, or `type` is `null`
    public static <T extends Node> @Nullable T page(
            Node owner,
            List<? extends Node> children,
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
    ///
    /// @param <T>        the selectable node type
    /// @param event      the key event to handle
    /// @param children   the children in traversal order
    /// @param current    the current selection, or `null`
    /// @param type       the selectable node type token
    /// @param horizontal whether Left and Right are handled
    /// @param vertical   whether Up and Down are handled
    /// @param selector   the operation that selects the resolved target
    /// @return `true` when a target was selected and the event was consumed
    /// @throws NullPointerException if `event`, `children`, `type`, or `selector` is `null`
    public static <T extends Node> boolean handleKeySelection(
            KeyEvent event,
            List<? extends Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical,
            Consumer<T> selector
    ) {
        return handleKeySelectionWithRevealOwner(event, null, children, current, type, horizontal, vertical, false, selector);
    }

    /// Handles a navigation key event, selects the matching child, and reveals it when a scroll owner exists.
    ///
    /// @param <T>        the selectable node type
    /// @param event      the key event to handle
    /// @param owner      the selection container used to reveal the target
    /// @param children   the children in traversal order
    /// @param current    the current selection, or `null`
    /// @param type       the selectable node type token
    /// @param horizontal whether Left and Right are handled
    /// @param vertical   whether Up and Down are handled
    /// @param selector   the operation that selects the resolved target
    /// @return `true` when a target was selected and the event was consumed
    /// @throws NullPointerException if any non-nullable argument is `null`
    public static <T extends Node> boolean handleKeySelection(
            KeyEvent event,
            Node owner,
            List<? extends Node> children,
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
    ///
    /// @param <T>         the selectable node type
    /// @param event       the key event to handle
    /// @param children    the children in traversal order
    /// @param current     the current selection, or `null`
    /// @param type        the selectable node type token
    /// @param horizontal  whether Left and Right are handled
    /// @param vertical    whether Up and Down are handled
    /// @param rightToLeft whether horizontal traversal follows right-to-left visual order
    /// @param selector    the operation that selects the resolved target
    /// @return `true` when a target was selected and the event was consumed
    /// @throws NullPointerException if `event`, `children`, `type`, or `selector` is `null`
    public static <T extends Node> boolean handleKeySelection(
            KeyEvent event,
            List<? extends Node> children,
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
    ///
    /// @param <T>         the selectable node type
    /// @param event       the key event to handle
    /// @param owner       the selection container used to reveal the target
    /// @param children    the children in traversal order
    /// @param current     the current selection, or `null`
    /// @param type        the selectable node type token
    /// @param horizontal  whether Left and Right are handled
    /// @param vertical    whether Up and Down are handled
    /// @param rightToLeft whether horizontal traversal follows right-to-left visual order
    /// @param selector    the operation that selects the resolved target
    /// @return `true` when a target was selected and the event was consumed
    /// @throws NullPointerException if any non-nullable argument is `null`
    public static <T extends Node> boolean handleKeySelection(
            KeyEvent event,
            Node owner,
            List<? extends Node> children,
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
            List<? extends Node> children,
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
    ///
    /// Page Up and Page Down are the only recognized keys. A handled event is consumed after the target is selected,
    /// focused, and revealed through `owner`.
    ///
    /// @param <T>      the selectable node type
    /// @param event    the key event to handle
    /// @param owner    the selection container whose viewport determines the page step
    /// @param children the children in traversal order
    /// @param current  the current selection, or `null`
    /// @param type     the selectable node type token
    /// @param selector the operation that selects the resolved target
    /// @return `true` when a page target was selected and the event was consumed
    public static <T extends Node> boolean handlePageKeySelection(
            KeyEvent event,
            Node owner,
            List<? extends Node> children,
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
    ///
    /// @param <T>        the focusable node type
    /// @param event      the key event to handle
    /// @param children   the children in traversal order
    /// @param current    the current focus anchor, or `null`
    /// @param type       the focusable node type token
    /// @param horizontal whether Left and Right are handled
    /// @param vertical   whether Up and Down are handled
    /// @return `true` when focus moved and the event was consumed
    /// @throws NullPointerException if `event`, `children`, or `type` is `null`
    public static <T extends Node> boolean handleKeyFocus(
            KeyEvent event,
            List<? extends Node> children,
            @Nullable T current,
            Class<T> type,
            boolean horizontal,
            boolean vertical
    ) {
        return handleKeyFocusWithRevealOwner(event, null, children, current, type, horizontal, vertical, false);
    }

    /// Handles a navigation key event, focuses the matching child, and reveals it when a scroll owner exists.
    ///
    /// @param <T>        the focusable node type
    /// @param event      the key event to handle
    /// @param owner      the container used to reveal the focused target
    /// @param children   the children in traversal order
    /// @param current    the current focus anchor, or `null`
    /// @param type       the focusable node type token
    /// @param horizontal whether Left and Right are handled
    /// @param vertical   whether Up and Down are handled
    /// @return `true` when focus moved and the event was consumed
    /// @throws NullPointerException if `event`, `children`, or `type` is `null`
    public static <T extends Node> boolean handleKeyFocus(
            KeyEvent event,
            Node owner,
            List<? extends Node> children,
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
    ///
    /// @param <T>         the focusable node type
    /// @param event       the key event to handle
    /// @param children    the children in traversal order
    /// @param current     the current focus anchor, or `null`
    /// @param type        the focusable node type token
    /// @param horizontal  whether Left and Right are handled
    /// @param vertical    whether Up and Down are handled
    /// @param rightToLeft whether horizontal traversal follows right-to-left visual order
    /// @return `true` when focus moved and the event was consumed
    /// @throws NullPointerException if `event`, `children`, or `type` is `null`
    public static <T extends Node> boolean handleKeyFocus(
            KeyEvent event,
            List<? extends Node> children,
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
    ///
    /// @param <T>         the focusable node type
    /// @param event       the key event to handle
    /// @param owner       the container used to reveal the focused target
    /// @param children    the children in traversal order
    /// @param current     the current focus anchor, or `null`
    /// @param type        the focusable node type token
    /// @param horizontal  whether Left and Right are handled
    /// @param vertical    whether Up and Down are handled
    /// @param rightToLeft whether horizontal traversal follows right-to-left visual order
    /// @return `true` when focus moved and the event was consumed
    /// @throws NullPointerException if `event`, `children`, or `type` is `null`
    public static <T extends Node> boolean handleKeyFocus(
            KeyEvent event,
            Node owner,
            List<? extends Node> children,
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
            List<? extends Node> children,
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
    ///
    /// @param <T>      the focusable node type
    /// @param event    the key event to handle
    /// @param owner    the container whose viewport determines the page step and reveals the target
    /// @param children the children in traversal order
    /// @param current  the current focus anchor, or `null`
    /// @param type     the focusable node type token
    /// @return `true` when focus moved and the event was consumed
    public static <T extends Node> boolean handlePageKeyFocus(
            KeyEvent event,
            Node owner,
            List<? extends Node> children,
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
    ///
    /// @param <T>      the requested node type
    /// @param children the children to inspect
    /// @param type     the requested node type token
    /// @return the focused reachable matching child, or `null`
    /// @throws NullPointerException if `children` or `type` is `null`
    public static <T extends Node> @Nullable T focused(List<? extends Node> children, Class<T> type) {
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
    ///
    /// @param <T>      the requested node type
    /// @param children the children to inspect
    /// @param current  the current selection or focus anchor, or `null`
    /// @param type     the requested node type token
    /// @return the focused or current reachable child, or `null`
    /// @throws NullPointerException if `children` or `type` is `null`
    public static <T extends Node> @Nullable T focusAnchor(
            List<? extends Node> children,
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
    ///
    /// @param <T>      the requested node type
    /// @param children the children to inspect
    /// @param current  the current selection or focus anchor, or `null`
    /// @param type     the requested node type token
    /// @return the best reachable focus target, or `null`
    /// @throws NullPointerException if `children` or `type` is `null`
    public static <T extends Node> @Nullable T focusTarget(
            List<? extends Node> children,
            @Nullable T current,
            Class<T> type
    ) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(type, "type");

        @Nullable T anchor = focusAnchor(children, current, type);
        return anchor == null ? first(children, type) : anchor;
    }

    /// Returns the next enabled visible child whose normalized text starts with the supplied prefix.
    ///
    /// Search begins after `current`, wraps once, and may therefore return the current item after all other children
    /// have been considered. The supplied prefix is expected to have been normalized by
    /// [#normalizeTypeAheadText(String)].
    ///
    /// @param <T>          the requested node type
    /// @param children     the children in search order
    /// @param current      the current child, or `null` to begin at the first child
    /// @param type         the requested node type token
    /// @param prefix       the nonempty normalized prefix
    /// @param textProvider the function that supplies candidate text
    /// @return the next matching reachable child, or `null`
    /// @throws NullPointerException if `children`, `type`, `prefix`, or `textProvider` is `null`
    public static <T extends Node> @Nullable T typeAheadTarget(
            List<? extends Node> children,
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
            if (selectable != null && matchesTypeAheadPrefix(textProvider.apply(selectable), prefix)) {
                return selectable;
            }
        }
        return null;
    }

    /// Returns text normalized for case-insensitive type-ahead matching.
    ///
    /// The result is stripped of leading and trailing whitespace and converted to lower case with [Locale#ROOT].
    ///
    /// @param text the text to normalize
    /// @return the normalized text
    /// @throws NullPointerException if `text` is `null`
    public static String normalizeTypeAheadText(String text) {
        Objects.requireNonNull(text, "text");
        return text.strip().toLowerCase(Locale.ROOT);
    }

    /// Returns whether text starts with an already normalized type-ahead prefix.
    ///
    /// Leading whitespace is skipped and case is compared without creating a normalized copy of the candidate text.
    ///
    /// @param text             the candidate item text
    /// @param normalizedPrefix the stripped lower-case prefix
    /// @return `true` when the candidate starts with the supplied prefix
    /// @throws NullPointerException if either argument is `null`
    public static boolean matchesTypeAheadPrefix(String text, String normalizedPrefix) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(normalizedPrefix, "normalizedPrefix");
        if (normalizedPrefix.isEmpty()) {
            return true;
        }

        int start = 0;
        int textLength = text.length();
        while (start < textLength && Character.isWhitespace(text.charAt(start))) {
            start++;
        }
        return textLength - start >= normalizedPrefix.length()
                && text.regionMatches(true, start, normalizedPrefix, 0, normalizedPrefix.length());
    }

    /// Returns the selection target implied by a navigation key.
    private static <T extends Node> @Nullable T targetFromKey(
            KeyCode keyCode,
            List<? extends Node> children,
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
            List<? extends Node> children,
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
            List<? extends Node> children,
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
            List<? extends Node> children,
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
            List<? extends Node> children,
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
    private static <T extends Node> int pageStep(Node owner, List<? extends Node> children, Class<T> type) {
        double viewportHeight = M3ScrollReveal.pageViewportHeight(owner);
        double rowHeight = estimatedRowHeight(children, type);
        if (viewportHeight <= 0.0 || rowHeight <= 0.0) {
            return DEFAULT_PAGE_STEP;
        }
        return Math.max(1, (int) Math.floor(viewportHeight / rowHeight));
    }

    /// Returns the best available row height estimate for a child list.
    private static <T extends Node> double estimatedRowHeight(List<? extends Node> children, Class<T> type) {
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
