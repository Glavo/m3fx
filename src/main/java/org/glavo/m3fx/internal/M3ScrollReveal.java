// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiPredicate;

/// Internal helpers for revealing focused descendants inside ancestor [ScrollPane] viewports.
@NotNullByDefault
public final class M3ScrollReveal {
    /// Prevents utility class instantiation.
    private M3ScrollReveal() {
    }

    /// Requests focus on the target and scrolls containing scroll panes so the target is visible.
    ///
    /// @param owner the owner used to locate the containing scroll pane
    /// @param target the target that should receive focus and become visible
    /// @return `true` when focus moved to the target
    public static boolean requestFocusAndReveal(Node owner, Node target) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(target, "target");
        if (M3FocusRequests.requestFocus(target)) {
            revealTarget(owner, target);
            return true;
        }
        return false;
    }

    /// Scrolls the target into view through containing scroll panes.
    ///
    /// @param owner the node whose ancestor scroll pane should be adjusted
    /// @param target the node that should become visible inside the scroll pane content
    public static void revealTarget(Node owner, Node target) {
        revealTarget(owner, target, M3FocusGuards::containsNode);
    }

    /// Scrolls the target into view through containing scroll panes when the target belongs to their content.
    ///
    /// @param owner the node whose ancestor scroll pane should be adjusted
    /// @param target the node that should become visible inside the scroll pane content
    /// @param containsNode the containment predicate used to decide whether the target belongs to the scroll content
    public static void revealTarget(Node owner, Node target, BiPredicate<Node, Node> containsNode) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(containsNode, "containsNode");

        boolean needsLayoutRetry = revealTargetNow(owner, target, containsNode);
        if (needsLayoutRetry && owner.getScene() != null && target.getScene() != null) {
            Platform.runLater(() -> {
                if (owner.getScene() != null && target.getScene() != null) {
                    revealTargetNow(owner, target, containsNode);
                }
            });
        }
    }

    /// Scrolls the target into view and returns whether a later layout pulse should retry the reveal.
    private static boolean revealTargetNow(Node owner, Node target, BiPredicate<Node, Node> containsNode) {
        boolean foundScrollPane = false;
        boolean needsLayoutRetry = false;
        @Nullable Node current = target;
        while (current != null) {
            if (current instanceof ScrollPane scrollPane
                    && scrollPane.getContent() != null
                    && containsNode.test(scrollPane.getContent(), target)
                    && belongsToRevealScope(owner, scrollPane, containsNode)) {
                foundScrollPane = true;
                needsLayoutRetry |= revealTargetInScrollPane(scrollPane, target, containsNode);
            }
            current = current.getParent();
        }

        if (foundScrollPane) {
            return needsLayoutRetry;
        }

        @Nullable ScrollPane scrollPane = containingScrollPane(owner);
        return scrollPane != null && revealTargetInScrollPane(scrollPane, target, containsNode);
    }

    /// Returns whether a scroll pane belongs to the owner's reveal scope.
    private static boolean belongsToRevealScope(
            Node owner,
            ScrollPane scrollPane,
            BiPredicate<Node, Node> containsNode
    ) {
        @Nullable Node content = scrollPane.getContent();
        return owner == scrollPane
                || containsNode.test(owner, scrollPane)
                || content != null && containsNode.test(content, owner);
    }

    /// Scrolls one containing scroll pane when the target belongs to the pane content.
    private static boolean revealTargetInScrollPane(
            ScrollPane scrollPane,
            Node target,
            BiPredicate<Node, Node> containsNode
    ) {
        @Nullable Node content = scrollPane.getContent();
        if (content == null || !containsNode.test(content, target)) {
            return false;
        }

        Bounds viewportBounds = scrollPane.getViewportBounds();
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        boolean needsLayoutRetry = targetBounds.getWidth() <= 0.0 || targetBounds.getHeight() <= 0.0;
        needsLayoutRetry |= revealTargetHorizontally(scrollPane, content, viewportBounds, targetBounds);
        needsLayoutRetry |= revealTargetVertically(scrollPane, content, viewportBounds, targetBounds);
        return needsLayoutRetry;
    }

    /// Returns the nearest ancestor scroll pane whose content subtree contains the owner.
    ///
    /// @param owner the node whose ancestors should be searched
    /// @return the nearest containing scroll pane, or `null` when none contains the owner
    public static @Nullable ScrollPane containingScrollPane(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node current = owner.getParent();
        while (current != null) {
            if (current instanceof ScrollPane scrollPane) {
                @Nullable Node content = scrollPane.getContent();
                if (content != null && M3FocusGuards.containsNode(content, owner)) {
                    return scrollPane;
                }
            }
            current = current.getParent();
        }
        return null;
    }

    /// Returns the visible viewport height that should define Page Up and Page Down navigation distance.
    ///
    /// @param owner the owner whose own or containing scroll viewport should be measured
    /// @return the measured viewport height, or the owner's layout height when no viewport is available
    public static double pageViewportHeight(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable ScrollPane scrollPane = owner instanceof ScrollPane ownerScrollPane
                ? ownerScrollPane
                : containingScrollPane(owner);
        if (scrollPane != null) {
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            if (viewportHeight > 0.0) {
                return viewportHeight;
            }
        }
        return owner.getLayoutBounds().getHeight();
    }

    /// Reveals the target bounds along the horizontal scroll axis when that axis can scroll.
    private static boolean revealTargetHorizontally(
            ScrollPane scrollPane,
            Node content,
            Bounds viewportBounds,
            Bounds targetBounds
    ) {
        double viewportWidth = viewportBounds.getWidth();
        double scrollableWidth = scrollContentWidth(content, viewportBounds.getHeight()) - viewportWidth;
        double valueRange = scrollPane.getHmax() - scrollPane.getHmin();
        if (viewportWidth <= 0.0) {
            return true;
        }
        if (scrollableWidth <= 0.0 || valueRange <= 0.0) {
            return targetBounds.getMinX() < 0.0 || targetBounds.getMaxX() > viewportWidth;
        }

        double visibleLeft = scrollValueToContentOffset(
                scrollPane.getHvalue(),
                scrollPane.getHmin(),
                scrollableWidth,
                valueRange
        );
        double nextLeft = revealedContentStart(
                visibleLeft,
                viewportWidth,
                targetBounds.getMinX(),
                targetBounds.getMaxX()
        );
        if (nextLeft != visibleLeft) {
            double clampedLeft = Math.max(0.0, Math.min(scrollableWidth, nextLeft));
            scrollPane.setHvalue(contentOffsetToScrollValue(
                    scrollPane.getHmin(),
                    clampedLeft,
                    scrollableWidth,
                    valueRange
            ));
        }
        return false;
    }

    /// Reveals the target bounds along the vertical scroll axis when that axis can scroll.
    private static boolean revealTargetVertically(
            ScrollPane scrollPane,
            Node content,
            Bounds viewportBounds,
            Bounds targetBounds
    ) {
        double viewportHeight = viewportBounds.getHeight();
        double scrollableHeight = scrollContentHeight(content, viewportBounds.getWidth()) - viewportHeight;
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        if (viewportHeight <= 0.0) {
            return true;
        }
        if (scrollableHeight <= 0.0 || valueRange <= 0.0) {
            return targetBounds.getMinY() < 0.0 || targetBounds.getMaxY() > viewportHeight;
        }

        double visibleTop = scrollValueToContentOffset(
                scrollPane.getVvalue(),
                scrollPane.getVmin(),
                scrollableHeight,
                valueRange
        );
        double nextTop = revealedContentStart(
                visibleTop,
                viewportHeight,
                targetBounds.getMinY(),
                targetBounds.getMaxY()
        );
        if (nextTop != visibleTop) {
            double clampedTop = Math.max(0.0, Math.min(scrollableHeight, nextTop));
            scrollPane.setVvalue(contentOffsetToScrollValue(
                    scrollPane.getVmin(),
                    clampedTop,
                    scrollableHeight,
                    valueRange
            ));
        }
        return false;
    }

    /// Returns the content-space viewport start that reveals the target interval.
    private static double revealedContentStart(
            double visibleStart,
            double viewportLength,
            double targetStart,
            double targetEnd
    ) {
        double visibleEnd = visibleStart + viewportLength;
        double targetLength = targetEnd - targetStart;
        if (targetLength >= viewportLength) {
            return targetStart;
        }
        if (targetStart < visibleStart) {
            return targetStart;
        }
        if (targetEnd > visibleEnd) {
            return targetEnd - viewportLength;
        }
        return visibleStart;
    }

    /// Returns the content-space offset represented by one scroll value.
    private static double scrollValueToContentOffset(
            double scrollValue,
            double minValue,
            double scrollableLength,
            double valueRange
    ) {
        double fraction = (scrollValue - minValue) / valueRange;
        return Math.max(0.0, Math.min(1.0, fraction)) * scrollableLength;
    }

    /// Returns the scroll value that represents one content-space offset.
    private static double contentOffsetToScrollValue(
            double minValue,
            double contentOffset,
            double scrollableLength,
            double valueRange
    ) {
        return minValue + (contentOffset / scrollableLength) * valueRange;
    }

    /// Returns the current scroll content width, including height-dependent preferred width updates.
    private static double scrollContentWidth(Node content, double viewportHeight) {
        double width = content.getBoundsInLocal().getWidth();
        if (content instanceof Region region) {
            double preferredWidth = region.prefWidth(viewportHeight > 0.0 ? viewportHeight : -1.0);
            width = Math.max(width, preferredWidth);
        }
        return width;
    }

    /// Returns the current scroll content height, including width-dependent preferred height updates.
    private static double scrollContentHeight(Node content, double viewportWidth) {
        double height = content.getBoundsInLocal().getHeight();
        if (content instanceof Region region) {
            double preferredHeight = region.prefHeight(viewportWidth > 0.0 ? viewportWidth : -1.0);
            height = Math.max(height, preferredHeight);
        }
        return height;
    }
}