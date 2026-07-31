// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Window;
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
        @Nullable Scene scene = owner.getScene();
        if (needsLayoutRetry && scene != null && target.getScene() == scene) {
            new DeferredReveal(scene, owner, target, containsNode).schedule();
        }
    }

    /// Scrolls a target inside one known scroll pane and reports whether established layout is still required.
    ///
    /// This overload performs no deferred scheduling. It is intended for skins that already coordinate work with a
    /// layout pulse or a longer-running geometry transition.
    ///
    /// @param scrollPane the scroll pane whose viewport should reveal the target
    /// @param target the descendant target to reveal
    /// @return `true` when viewport or target geometry is not established yet
    /// @throws NullPointerException if `scrollPane` or `target` is `null`
    public static boolean revealTargetInScrollPane(ScrollPane scrollPane, Node target) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        Objects.requireNonNull(target, "target");
        return revealTargetInScrollPane(scrollPane, target, M3FocusGuards::containsNode);
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
        needsLayoutRetry |= hasPendingLayout(scrollPane, content, target);
        needsLayoutRetry |= revealTargetHorizontally(scrollPane, content, viewportBounds, targetBounds);
        needsLayoutRetry |= revealTargetVertically(scrollPane, content, viewportBounds, targetBounds);
        return needsLayoutRetry;
    }

    /// Returns whether the viewport or target hierarchy still has unresolved layout.
    private static boolean hasPendingLayout(ScrollPane scrollPane, Node content, Node target) {
        if (scrollPane.isNeedsLayout()) {
            return true;
        }

        @Nullable Node current = target;
        while (current != null) {
            if (current instanceof Parent parent && parent.isNeedsLayout()) {
                return true;
            }
            if (current == content) {
                return false;
            }
            current = current.getParent();
        }
        return false;
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
        Bounds contentBounds = content.getLayoutBounds();
        double contentLeft = contentBounds.getMinX();
        double scrollableWidth = contentBounds.getWidth() - viewportWidth;
        double valueRange = scrollPane.getHmax() - scrollPane.getHmin();
        if (viewportWidth <= 0.0) {
            return true;
        }
        if (scrollableWidth <= 0.0 || valueRange <= 0.0) {
            return targetBounds.getMinX() < contentLeft
                    || targetBounds.getMaxX() > contentLeft + viewportWidth;
        }

        double visibleLeft = contentLeft + scrollValueToContentOffset(
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
            double clampedLeft = Math.max(contentLeft, Math.min(contentLeft + scrollableWidth, nextLeft));
            scrollPane.setHvalue(contentOffsetToScrollValue(
                    scrollPane.getHmin(),
                    clampedLeft - contentLeft,
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
        Bounds contentBounds = content.getLayoutBounds();
        double contentTop = contentBounds.getMinY();
        double scrollableHeight = contentBounds.getHeight() - viewportHeight;
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        if (viewportHeight <= 0.0) {
            return true;
        }
        if (scrollableHeight <= 0.0 || valueRange <= 0.0) {
            return targetBounds.getMinY() < contentTop
                    || targetBounds.getMaxY() > contentTop + viewportHeight;
        }

        double visibleTop = contentTop + scrollValueToContentOffset(
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
            double clampedTop = Math.max(contentTop, Math.min(contentTop + scrollableHeight, nextTop));
            scrollPane.setVvalue(contentOffsetToScrollValue(
                    scrollPane.getVmin(),
                    clampedTop - contentTop,
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

    /// Runs one deferred reveal after the owning scene has completed its next layout pass.
    ///
    /// An offscreen scene has no automatic layout pulse, so its request instead runs on the next FX event turn after
    /// the caller has had an opportunity to establish layout explicitly.
    @NotNullByDefault
    private static final class DeferredReveal implements Runnable {
        /// The scene whose post-layout pulse owns this request.
        private final Scene scene;

        /// The reveal scope owner.
        private final Node owner;

        /// The target to reveal.
        private final Node target;

        /// The containment policy captured from the request.
        private final BiPredicate<Node, Node> containsNode;

        /// Whether this request is currently registered as a scene post-layout callback.
        private boolean postLayoutRegistered;

        /// Creates one deferred reveal request.
        ///
        /// @param scene the scene that will execute the request
        /// @param owner the reveal scope owner
        /// @param target the target to reveal
        /// @param containsNode the containment policy
        private DeferredReveal(
                Scene scene,
                Node owner,
                Node target,
                BiPredicate<Node, Node> containsNode
        ) {
            this.scene = scene;
            this.owner = owner;
            this.target = target;
            this.containsNode = containsNode;
        }

        /// Schedules this request according to whether the scene participates in window layout pulses.
        private void schedule() {
            @Nullable Window window = scene.getWindow();
            if (window == null || !window.isShowing()) {
                Platform.runLater(this);
                return;
            }
            postLayoutRegistered = true;
            scene.addPostLayoutPulseListener(this);
            Platform.requestNextPulse();
        }

        /// Removes this one-shot request and reveals the target when it still belongs to the same scene.
        @Override
        public void run() {
            if (postLayoutRegistered) {
                postLayoutRegistered = false;
                scene.removePostLayoutPulseListener(this);
            }
            if (owner.getScene() == scene && target.getScene() == scene) {
                revealTargetNow(owner, target, containsNode);
            }
        }
    }
}
