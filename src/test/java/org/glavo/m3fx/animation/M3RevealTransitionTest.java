// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies composable expand and shrink effects and their retained-holder lifecycle.
@NotNullByDefault
final class M3RevealTransitionTest {
    /// Starts the JavaFX toolkit before retained scene-graph tests run.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies reveal factories validate axes and remain mutually exclusive within a transition.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void revealFactoriesValidateCompositionAndAnchors() {
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .and(M3EnterTransition.scale(0.9))
                .and(M3EnterTransition.slideFrom(M3TransitionEdge.START, 12.0))
                .and(M3EnterTransition.expandIn(M3TransitionEdge.START, M3TransitionEdge.TOP));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.scale(0.9))
                .and(M3ExitTransition.slideTo(M3TransitionEdge.END, 12.0))
                .and(M3ExitTransition.shrinkOut(M3TransitionEdge.END, M3TransitionEdge.BOTTOM));

        assertNotNull(enter);
        assertNotNull(exit);
        assertNotNull(M3EnterTransition.expandIn());
        assertNotNull(M3EnterTransition.expandVertically());
        assertNotNull(M3ExitTransition.shrinkOut());
        assertNotNull(M3ExitTransition.shrinkHorizontally());
        assertThrows(IllegalArgumentException.class, () ->
                enter.and(M3EnterTransition.expandHorizontally()));
        assertThrows(IllegalArgumentException.class, () ->
                exit.and(M3ExitTransition.shrinkVertically()));
        assertThrows(IllegalArgumentException.class, () ->
                M3EnterTransition.expandHorizontally(M3TransitionEdge.TOP));
        assertThrows(IllegalArgumentException.class, () ->
                M3EnterTransition.expandVertically(M3TransitionEdge.START));
        assertThrows(IllegalArgumentException.class, () ->
                M3ExitTransition.shrinkOut(M3TransitionEdge.TOP, M3TransitionEdge.END));
        assertThrows(NullPointerException.class, () ->
                M3EnterTransition.expandIn(null, M3TransitionEdge.BOTTOM));
        assertThrows(NullPointerException.class, () ->
                M3ExitTransition.shrinkHorizontally(null));
    }

    /// Verifies a composed two-dimensional reveal clips only the private holder.
    @Test
    void expandRevealPreservesContentPropertiesAndLayoutSize() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region first = fixedRegion(80.0, 32.0);
            Region second = fixedRegion(120.0, 48.0);
            M3AnimatedContent animatedContent = new M3AnimatedContent(first);
            M3MotionSpec slow = slowMotionSpec();
            animatedContent.setContentTransform(new M3ContentTransform(
                    M3EnterTransition.fade(0.25)
                            .and(M3EnterTransition.scale(0.9))
                            .and(M3EnterTransition.slideFrom(M3TransitionEdge.START, 8.0))
                            .and(M3EnterTransition.expandIn(
                                    M3TransitionEdge.START,
                                    M3TransitionEdge.TOP
                            ))
                            .withMotionSpec(slow),
                    M3ExitTransition.none(),
                    null,
                    0.0
            ));

            Pane root = install(animatedContent);
            animatedContent.setContent(second);
            root.layout();

            StackPane incoming = holderOf(second);
            Rectangle clip = clipOf(second);
            assertEquals(120.0, incoming.getWidth(), 1.0e-6);
            assertEquals(48.0, incoming.getHeight(), 1.0e-6);
            assertEquals(0.0, clip.getX(), 1.0e-6);
            assertEquals(0.0, clip.getY(), 1.0e-6);
            assertEquals(0.0, clip.getWidth(), 1.0e-6);
            assertEquals(0.0, clip.getHeight(), 1.0e-6);
            assertEquals(120.0, animatedContent.prefWidth(-1.0), 1.0e-6);
            assertEquals(48.0, animatedContent.prefHeight(-1.0), 1.0e-6);
            assertContentVisualsUnchanged(second);

            animatedContent.finish();
            assertFalse(animatedContent.isTransitioning());
            assertNull(incoming.getClip());
            assertContentVisualsUnchanged(second);
        }));
    }

    /// Verifies one-axis reveals retain the other axis and resolve logical edges in RTL.
    @Test
    void revealAnchorsResolveAcrossAxesAndNodeOrientations() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region first = fixedRegion(80.0, 32.0);
            Region second = fixedRegion(120.0, 48.0);
            M3AnimatedContent animatedContent = new M3AnimatedContent(first);
            Pane root = install(animatedContent);
            animatedContent.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            animatedContent.setContentTransform(enterOnly(
                    M3EnterTransition.expandHorizontally(M3TransitionEdge.START)
            ));
            animatedContent.setContent(second);
            root.layout();
            StackPane startHolder = holderOf(second);
            Rectangle startClip = clipOf(second);
            assertEquals(startHolder.getWidth(), startClip.getX(), 1.0e-6);
            assertEquals(0.0, startClip.getWidth(), 1.0e-6);
            assertEquals(startHolder.getHeight(), startClip.getHeight(), 1.0e-6);
            animatedContent.finish();

            animatedContent.setContentTransform(enterOnly(M3EnterTransition.expandHorizontally()));
            animatedContent.setContent(first);
            root.layout();
            Rectangle endClip = clipOf(first);
            assertEquals(0.0, endClip.getX(), 1.0e-6);
            assertEquals(0.0, endClip.getWidth(), 1.0e-6);
            assertEquals(holderOf(first).getHeight(), endClip.getHeight(), 1.0e-6);
            animatedContent.finish();

            animatedContent.setContentTransform(enterOnly(
                    M3EnterTransition.expandVertically(M3TransitionEdge.TOP)
            ));
            animatedContent.setContent(second);
            root.layout();
            StackPane topHolder = holderOf(second);
            Rectangle topClip = clipOf(second);
            assertEquals(0.0, topClip.getY(), 1.0e-6);
            assertEquals(0.0, topClip.getHeight(), 1.0e-6);
            assertEquals(topHolder.getWidth(), topClip.getWidth(), 1.0e-6);
        }));
    }

    /// Verifies reversal, explicit completion, snapping, and reduced motion release every private clip.
    @Test
    void revealLifecycleHandlesReversalCompletionAndReducedMotion() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region first = fixedRegion(80.0, 32.0);
            Region second = fixedRegion(120.0, 48.0);
            M3AnimatedContent animatedContent = new M3AnimatedContent(first);
            animatedContent.setContentTransform(new M3ContentTransform(
                    M3EnterTransition.expandIn(M3TransitionEdge.START, M3TransitionEdge.TOP)
                            .withMotionSpec(slowMotionSpec()),
                    M3ExitTransition.shrinkOut(M3TransitionEdge.END, M3TransitionEdge.BOTTOM)
                            .withMotionSpec(slowMotionSpec()),
                    null,
                    0.0
            ));
            Pane root = install(animatedContent);

            animatedContent.setContent(second);
            assertNotNull(holderOf(first).getClip());
            assertNotNull(holderOf(second).getClip());
            animatedContent.setContent(first);
            assertTrue(animatedContent.isTransitioning());
            assertNotNull(holderOf(first).getClip());
            assertNotNull(holderOf(second).getClip());
            assertContentVisualsUnchanged(first);
            assertContentVisualsUnchanged(second);

            animatedContent.finish();
            assertFalse(animatedContent.isTransitioning());
            assertNull(holderOf(first).getClip());
            assertNull(second.getParent());

            animatedContent.setContent(second);
            assertNotNull(holderOf(second).getClip());
            animatedContent.snapToCurrentState();
            assertFalse(animatedContent.isTransitioning());
            assertNull(holderOf(second).getClip());

            M3MotionSettings.setReducedMotionRequested(root, true);
            animatedContent.setContent(first);
            assertFalse(animatedContent.isTransitioning());
            assertNull(holderOf(first).getClip());
            assertNull(second.getParent());
            assertContentVisualsUnchanged(first);
        }));
    }

    /// Verifies animated visibility uses the same reveal channel for enter, exit, reversal, and cleanup.
    @Test
    void animatedVisibilityComposesRevealLifecycle() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region content = fixedRegion(96.0, 40.0);
            M3AnimatedVisibility visibility = new M3AnimatedVisibility(content);
            visibility.setEnterTransition(
                    M3EnterTransition.expandHorizontally(M3TransitionEdge.START)
                            .and(M3EnterTransition.fade(0.0))
                            .withMotionSpec(slowMotionSpec())
            );
            visibility.setExitTransition(
                    M3ExitTransition.shrinkVertically(M3TransitionEdge.BOTTOM)
                            .and(M3ExitTransition.fade(0.0))
                            .withMotionSpec(slowMotionSpec())
            );
            Pane root = new Pane(visibility);
            new Scene(root);
            root.applyCss();
            root.layout();

            visibility.setShowing(false);
            assertTrue(visibility.isTransitioning());
            assertNotNull(holderOf(content).getClip());
            visibility.setShowing(true);
            assertFalse(visibility.isTransitioning());
            assertNull(holderOf(content).getClip());
            visibility.finish();
            assertFalse(visibility.isTransitioning());
            assertNull(holderOf(content).getClip());

            visibility.setShowing(false);
            visibility.finish();
            assertNull(content.getParent());
            visibility.setShowing(true);
            assertNotNull(holderOf(content).getClip());
            visibility.snapToCurrentState();
            assertNull(holderOf(content).getClip());
            assertContentVisualsUnchanged(content);
        }));
    }

    /// Creates a content transform with only the supplied enter effect and a long deterministic specification.
    private static M3ContentTransform enterOnly(M3EnterTransition transition) {
        return new M3ContentTransform(
                transition.withMotionSpec(slowMotionSpec()),
                M3ExitTransition.none(),
                null,
                0.0
        );
    }

    /// Attaches an animated-content region to a scene and performs its initial CSS and layout pass.
    private static Pane install(M3AnimatedContent animatedContent) {
        Pane root = new Pane(animatedContent);
        new Scene(root);
        root.applyCss();
        root.layout();
        return root;
    }

    /// Returns the private holder that currently owns the supplied content node.
    private static StackPane holderOf(Node content) {
        return assertInstanceOf(StackPane.class, content.getParent());
    }

    /// Returns the private reveal rectangle installed for the supplied content node.
    private static Rectangle clipOf(Node content) {
        return assertInstanceOf(Rectangle.class, holderOf(content).getClip());
    }

    /// Verifies transition effects have not modified caller-owned visual properties.
    private static void assertContentVisualsUnchanged(Node content) {
        assertEquals(1.0, content.getOpacity(), 0.0);
        assertEquals(1.0, content.getScaleX(), 0.0);
        assertEquals(1.0, content.getScaleY(), 0.0);
        assertEquals(0.0, content.getTranslateX(), 0.0);
        assertEquals(0.0, content.getTranslateY(), 0.0);
        assertNull(content.getClip());
        assertTrue(content.getTransforms().isEmpty());
    }

    /// Returns a long linear specification that keeps initial reveal geometry observable.
    private static M3MotionSpec slowMotionSpec() {
        return M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR);
    }

    /// Creates a region with fixed minimum and preferred dimensions.
    private static Region fixedRegion(double width, double height) {
        Region region = new Region();
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        return region;
    }
}
