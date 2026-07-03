// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies scroll reveal behavior used by focus navigation.
@NotNullByDefault
final class M3ScrollRevealTest {
    /// Starts the JavaFX toolkit before creating scroll panes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes real stages opened by focus-aware tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : java.util.List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
        });
    }

    /// Verifies that the default reveal behavior scrolls a descendant target into view.
    @Test
    void defaultRevealScrollsDescendantIntoView() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            Label target = new Label("Target");
            VBox content = new VBox(spacer, target);
            ScrollPane scrollPane = scrollPane(content, true, false);

            M3ScrollReveal.revealTarget(content, target);

            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTargetVerticallyVisible(scrollPane, content, target);
        });
    }

    /// Verifies focus-and-reveal requests move focus and scroll the target into view.
    @Test
    void requestFocusAndRevealMovesFocusAndScrollsTargetIntoView() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            Label target = new Label("Target");
            target.setFocusTraversable(true);
            VBox content = new VBox(spacer, target);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 120.0);
            show(scrollPane, 180.0, 120.0);

            assertTrue(M3ScrollReveal.requestFocusAndReveal(content, target));

            assertTrue(target.isFocused());
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTargetVerticallyVisible(scrollPane, content, target);
        });
    }

    /// Verifies reveal retries after the next pulse when scroll metrics are not laid out yet.
    @Test
    void revealRetriesAfterLayoutPulseWhenScrollMetricsAreMissing() throws InterruptedException {
        AtomicReference<@Nullable ScrollPane> scrollPaneRef = new AtomicReference<>();
        FxTestUtils.runOnFxThreadWhen(
                () -> scrollPaneValue(scrollPaneRef) > 0.0,
                () -> "Timed out waiting for deferred reveal; vvalue=" + scrollPaneValue(scrollPaneRef),
                () -> {
                    Pane spacer = new Pane();
                    spacer.setPrefHeight(240.0);
                    Label target = new Label("Target");
                    VBox content = new VBox(spacer, target);
                    ScrollPane scrollPane = new ScrollPane(content);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setPrefSize(180.0, 120.0);
                    new Scene(scrollPane, 180.0, 120.0);
                    scrollPane.applyCss();
                    scrollPaneRef.set(scrollPane);

                    M3ScrollReveal.revealTarget(content, target);

                    scrollPane.resize(180.0, 120.0);
                    scrollPane.layout();
                    content.layout();
                },
                () -> assertTrue(scrollPaneValue(scrollPaneRef) > 0.0)
        );
    }

    /// Verifies nested scroll panes are all adjusted when a target is hidden in multiple viewports.
    @Test
    void nestedScrollPanesRevealTargetThroughEveryViewport() {
        FxTestUtils.runOnFxThread(() -> {
            Pane innerSpacer = new Pane();
            innerSpacer.setPrefHeight(220.0);
            Label target = new Label("Target");
            VBox innerContent = new VBox(innerSpacer, target);
            ScrollPane innerScrollPane = new ScrollPane(innerContent);
            innerScrollPane.setFitToWidth(true);
            innerScrollPane.setPrefSize(180.0, 120.0);

            Pane outerSpacer = new Pane();
            outerSpacer.setPrefHeight(220.0);
            VBox outerContent = new VBox(outerSpacer, innerScrollPane);
            ScrollPane outerScrollPane = scrollPane(outerContent, true, false);

            M3ScrollReveal.revealTarget(outerContent, target);

            assertTrue(innerScrollPane.getVvalue() > 0.0,
                    () -> "inner vvalue=" + innerScrollPane.getVvalue());
            assertTrue(outerScrollPane.getVvalue() > 0.0,
                    () -> "outer vvalue=" + outerScrollPane.getVvalue());
            assertTargetVerticallyVisible(innerScrollPane, innerContent, target);
            assertTargetVerticallyVisible(outerScrollPane, outerContent, target);
        });
    }

    /// Verifies nested horizontal scroll panes are all adjusted when a target is hidden in multiple viewports.
    @Test
    void nestedHorizontalScrollPanesRevealTargetThroughEveryViewport() {
        FxTestUtils.runOnFxThread(() -> {
            Pane innerSpacer = new Pane();
            innerSpacer.setPrefWidth(220.0);
            Label target = new Label("Target");
            HBox innerContent = new HBox(innerSpacer, target);
            ScrollPane innerScrollPane = new ScrollPane(innerContent);
            innerScrollPane.setFitToHeight(true);
            innerScrollPane.setPrefSize(180.0, 120.0);

            Pane outerSpacer = new Pane();
            outerSpacer.setPrefWidth(220.0);
            HBox outerContent = new HBox(outerSpacer, innerScrollPane);
            ScrollPane outerScrollPane = scrollPane(outerContent, false, true);

            M3ScrollReveal.revealTarget(outerContent, target);

            assertTrue(innerScrollPane.getHvalue() > 0.0,
                    () -> "inner hvalue=" + innerScrollPane.getHvalue());
            assertTrue(outerScrollPane.getHvalue() > 0.0,
                    () -> "outer hvalue=" + outerScrollPane.getHvalue());
            assertTargetHorizontallyVisible(innerScrollPane, innerContent, target);
            assertTargetHorizontallyVisible(outerScrollPane, outerContent, target);
        });
    }

    /// Verifies a scroll pane owner can reveal a target in its own content.
    @Test
    void scrollPaneOwnerRevealsTargetInOwnContent() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            Label target = new Label("Target");
            VBox content = new VBox(spacer, target);
            ScrollPane scrollPane = scrollPane(content, true, false);

            M3ScrollReveal.revealTarget(scrollPane, target);

            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTargetVerticallyVisible(scrollPane, content, target);
        });
    }

    /// Verifies an owner that contains a nested scroll pane reveals targets inside that nested pane.
    @Test
    void ownerContainingNestedScrollPaneRevealsNestedTarget() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            Label target = new Label("Target");
            VBox nestedContent = new VBox(spacer, target);
            ScrollPane nestedScrollPane = new ScrollPane(nestedContent);
            nestedScrollPane.setFitToWidth(true);
            nestedScrollPane.setPrefSize(180.0, 120.0);
            VBox owner = new VBox(nestedScrollPane);
            new Scene(owner, 180.0, 120.0);
            owner.applyCss();
            owner.resize(180.0, 120.0);
            owner.layout();
            nestedScrollPane.layout();
            nestedContent.layout();

            M3ScrollReveal.revealTarget(owner, target);

            assertTrue(nestedScrollPane.getVvalue() > 0.0,
                    () -> "nested vvalue=" + nestedScrollPane.getVvalue());
            assertTargetVerticallyVisible(nestedScrollPane, nestedContent, target);
        });
    }

    /// Verifies an owner inside the same scroll content can reveal a sibling target.
    @Test
    void ownerInsideScrollContentRevealsSiblingTarget() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            Label target = new Label("Target");
            VBox content = new VBox(owner, spacer, target);
            ScrollPane scrollPane = scrollPane(content, true, false);

            M3ScrollReveal.revealTarget(owner, target);

            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTargetVerticallyVisible(scrollPane, content, target);
        });
    }

    /// Verifies page navigation height uses the viewport when the owner is the scroll pane itself.
    @Test
    void pageViewportHeightUsesScrollPaneOwnerViewport() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(spacer);
            ScrollPane scrollPane = scrollPane(content, true, false);

            assertEquals(
                    scrollPane.getViewportBounds().getHeight(),
                    M3ScrollReveal.pageViewportHeight(scrollPane),
                    0.0001
            );
        });
    }

    /// Verifies page navigation height uses the containing scroll pane viewport for content descendants.
    @Test
    void pageViewportHeightUsesContainingScrollPaneViewport() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(spacer);
            ScrollPane scrollPane = scrollPane(content, true, false);

            assertEquals(
                    scrollPane.getViewportBounds().getHeight(),
                    M3ScrollReveal.pageViewportHeight(content),
                    0.0001
            );
        });
    }

    /// Verifies page navigation height falls back to owner layout bounds without a scroll viewport.
    @Test
    void pageViewportHeightFallsBackToOwnerLayoutBounds() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.resize(160.0, 72.0);

            assertEquals(72.0, M3ScrollReveal.pageViewportHeight(owner), 0.0001);
        });
    }

    /// Verifies reveal calls do not scroll an unrelated target outside the owner scope.
    @Test
    void unrelatedTargetScrollPaneIsNotRevealed() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            Label target = new Label("Target");
            VBox unrelatedContent = new VBox(spacer, target);
            ScrollPane unrelatedScrollPane = new ScrollPane(unrelatedContent);
            unrelatedScrollPane.setFitToWidth(true);
            unrelatedScrollPane.setPrefSize(180.0, 120.0);
            HBox root = new HBox(owner, unrelatedScrollPane);
            new Scene(root, 360.0, 120.0);
            root.applyCss();
            root.resize(360.0, 120.0);
            root.layout();
            unrelatedScrollPane.layout();
            unrelatedContent.layout();

            M3ScrollReveal.revealTarget(owner, target);

            assertEquals(0.0, unrelatedScrollPane.getVvalue(), 0.0001);
        });
    }

    /// Verifies callers can preserve logical-slot containment semantics when revealing a target.
    @Test
    void customContainmentPredicateControlsReveal() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            Label target = new Label("Target");
            VBox content = new VBox(spacer, target);
            ScrollPane scrollPane = scrollPane(content, true, false);

            M3ScrollReveal.revealTarget(content, target, (owner, item) -> false);

            assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

            M3ScrollReveal.revealTarget(content, target, (owner, item) -> true);

            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTargetVerticallyVisible(scrollPane, content, target);
        });
    }

    /// Shows and lays out a scroll pane in a real stage.
    private static Scene show(ScrollPane scrollPane, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(scrollPane, width, height);
        stage.setScene(scene);
        stage.show();
        scrollPane.applyCss();
        scrollPane.resize(width, height);
        scrollPane.layout();
        Node content = scrollPane.getContent();
        if (content instanceof Region region) {
            region.layout();
        }
        return scene;
    }

    /// Verifies that the target is visible in the scroll pane's vertical viewport.
    private static void assertTargetVerticallyVisible(ScrollPane scrollPane, Node content, Node target) {
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollableHeight = Math.max(0.0, scrollContentHeight(content, scrollPane.getViewportBounds().getWidth())
                - viewportHeight);
        double visibleTop = scrollOffset(scrollPane.getVvalue(), scrollPane.getVmin(), scrollableHeight,
                scrollPane.getVmax() - scrollPane.getVmin());
        double visibleBottom = visibleTop + viewportHeight;
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        assertTrue(targetBounds.getMinY() >= visibleTop - 0.5,
                () -> "targetTop=" + targetBounds.getMinY() + ", visibleTop=" + visibleTop);
        assertTrue(targetBounds.getMaxY() <= visibleBottom + 0.5,
                () -> "targetBottom=" + targetBounds.getMaxY() + ", visibleBottom=" + visibleBottom);
    }

    /// Verifies that the target is visible in the scroll pane's horizontal viewport.
    private static void assertTargetHorizontallyVisible(ScrollPane scrollPane, Node content, Node target) {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double scrollableWidth = Math.max(0.0, scrollContentWidth(content, scrollPane.getViewportBounds().getHeight())
                - viewportWidth);
        double visibleLeft = scrollOffset(scrollPane.getHvalue(), scrollPane.getHmin(), scrollableWidth,
                scrollPane.getHmax() - scrollPane.getHmin());
        double visibleRight = visibleLeft + viewportWidth;
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        assertTrue(targetBounds.getMinX() >= visibleLeft - 0.5,
                () -> "targetLeft=" + targetBounds.getMinX() + ", visibleLeft=" + visibleLeft);
        assertTrue(targetBounds.getMaxX() <= visibleRight + 0.5,
                () -> "targetRight=" + targetBounds.getMaxX() + ", visibleRight=" + visibleRight);
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

    /// Returns the content offset represented by one scroll value.
    private static double scrollOffset(double value, double minValue, double scrollableLength, double valueRange) {
        if (scrollableLength <= 0.0 || valueRange <= 0.0) {
            return 0.0;
        }
        double fraction = (value - minValue) / valueRange;
        return Math.max(0.0, Math.min(1.0, fraction)) * scrollableLength;
    }

    /// Returns the current vertical scroll value for diagnostics.
    private static double scrollPaneValue(AtomicReference<@Nullable ScrollPane> scrollPaneRef) {
        @Nullable ScrollPane scrollPane = scrollPaneRef.get();
        return scrollPane == null ? Double.NaN : scrollPane.getVvalue();
    }

    /// Creates and lays out a small scroll pane for reveal checks.
    private static ScrollPane scrollPane(Pane content, boolean fitToWidth, boolean fitToHeight) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(fitToWidth);
        scrollPane.setFitToHeight(fitToHeight);
        scrollPane.setPrefSize(180.0, 120.0);
        new Scene(scrollPane, 180.0, 120.0);
        scrollPane.applyCss();
        scrollPane.resize(180.0, 120.0);
        scrollPane.layout();
        content.layout();
        return scrollPane;
    }
}