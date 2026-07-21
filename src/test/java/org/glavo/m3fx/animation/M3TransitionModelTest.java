// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.collections.ListChangeListener;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies composable transition values and their retained-content runtime semantics.
@NotNullByDefault
final class M3TransitionModelTest {
    /// Starts the JavaFX toolkit before retained-content tests create scene-graph objects.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies effect factories reject values that cannot represent a visual transition.
    @Test
    void transitionFactoriesValidateTheirArguments() {
        assertThrows(IllegalArgumentException.class, () -> M3EnterTransition.fade(-0.01));
        assertThrows(IllegalArgumentException.class, () -> M3EnterTransition.fade(1.01));
        assertThrows(IllegalArgumentException.class, () -> M3ExitTransition.fade(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> M3EnterTransition.scale(0.0));
        assertThrows(IllegalArgumentException.class, () -> M3ExitTransition.scale(Double.POSITIVE_INFINITY));
        assertThrows(NullPointerException.class, () -> M3EnterTransition.slideFrom(null, 12.0));
        assertThrows(IllegalArgumentException.class, () ->
                M3EnterTransition.slideFrom(M3TransitionEdge.START, -1.0));
        assertThrows(IllegalArgumentException.class, () ->
                M3ExitTransition.slideTo(M3TransitionEdge.BOTTOM, Double.NaN));
        assertThrows(IllegalArgumentException.class, () ->
                M3EnterTransition.fade(0.0).withDelay(Duration.millis(-1.0)));
        assertThrows(IllegalArgumentException.class, () ->
                M3ExitTransition.fade(0.0).withDelay(Duration.INDEFINITE));
    }

    /// Verifies compositions permit independent channels and reject ambiguous duplicates.
    @Test
    void transitionCompositionKeepsChannelsUnique() {
        M3MotionSpec spec = slowMotionSpec();
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .and(M3EnterTransition.scale(0.84))
                .and(M3EnterTransition.slideFrom(M3TransitionEdge.END, 24.0))
                .withDelay(Duration.millis(40.0))
                .withMotionSpec(spec);
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.scale(0.96))
                .and(M3ExitTransition.slideTo(M3TransitionEdge.START, 12.0))
                .withMotionSpec(spec);

        M3ContentTransform transform = new M3ContentTransform(
                enter,
                exit,
                new M3SizeTransform(false, spec),
                -1.0
        );
        assertSame(enter, transform.targetContentEnter());
        assertSame(exit, transform.initialContentExit());
        assertNotNull(transform.sizeTransform());
        assertEquals(-1.0, transform.targetContentZIndex(), 0.0);

        assertThrows(IllegalArgumentException.class, () ->
                enter.and(M3EnterTransition.fade(0.5)));
        assertThrows(IllegalArgumentException.class, () ->
                exit.and(M3ExitTransition.slideTo(M3TransitionEdge.TOP, 8.0)));
        assertThrows(NullPointerException.class, () ->
                new M3ContentTransform(null, exit, null, 0.0));
        assertThrows(IllegalArgumentException.class, () ->
                new M3ContentTransform(enter, exit, null, Double.NaN));
    }

    /// Verifies logical slides, private visual ownership, interactivity, and target drawing order.
    @Test
    void animatedContentAppliesLogicalEffectsAndDrawingOrder() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region first = fixedRegion(80.0, 32.0);
            Region second = fixedRegion(120.0, 48.0);
            M3AnimatedContent animatedContent = new M3AnimatedContent(first);
            animatedContent.setContentTransform(new M3ContentTransform(
                    M3EnterTransition.fade(0.25)
                            .and(M3EnterTransition.scale(0.8))
                            .and(M3EnterTransition.slideFrom(M3TransitionEdge.START, 32.0))
                            .withMotionSpec(slowMotionSpec()),
                    M3ExitTransition.fade(0.0)
                            .and(M3ExitTransition.slideTo(M3TransitionEdge.END, 16.0))
                            .withMotionSpec(slowMotionSpec()),
                    null,
                    1.0
            ));

            Pane root = new Pane(animatedContent);
            new Scene(root);
            root.applyCss();
            root.layout();

            animatedContent.setContent(second);
            StackPane incoming = assertInstanceOf(StackPane.class, second.getParent());
            StackPane outgoing = assertInstanceOf(StackPane.class, first.getParent());
            Pane viewport = assertInstanceOf(Pane.class, incoming.getParent());
            AtomicInteger holderListChanges = new AtomicInteger();
            viewport.getChildren().addListener((ListChangeListener<Node>) change ->
                    holderListChanges.incrementAndGet());

            assertEquals(0.25, incoming.getOpacity(), 0.0);
            assertEquals(0.8, incoming.getScaleX(), 0.0);
            assertEquals(0.8, incoming.getScaleY(), 0.0);
            assertEquals(-32.0, incoming.getTranslateX(), 0.0);
            assertEquals(0.0, incoming.getTranslateY(), 0.0);
            assertFalse(incoming.isMouseTransparent());
            assertTrue(outgoing.isMouseTransparent());
            assertTrue(incoming.getViewOrder() < outgoing.getViewOrder());
            assertEquals(1.0, first.getOpacity(), 0.0);
            assertEquals(1.0, second.getOpacity(), 0.0);

            animatedContent.finish();
            assertEquals(0, holderListChanges.get());
            animatedContent.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            animatedContent.setContentTransform(new M3ContentTransform(
                    M3EnterTransition.slideFrom(M3TransitionEdge.START, 32.0)
                            .withMotionSpec(slowMotionSpec()),
                    M3ExitTransition.none(),
                    null,
                    -1.0
            ));
            animatedContent.setContent(first);

            StackPane rtlIncoming = assertInstanceOf(StackPane.class, first.getParent());
            StackPane rtlOutgoing = assertInstanceOf(StackPane.class, second.getParent());
            assertEquals(32.0, rtlIncoming.getTranslateX(), 0.0);
            assertTrue(rtlIncoming.getViewOrder() > rtlOutgoing.getViewOrder());
            assertEquals(0, holderListChanges.get());
        }));
    }

    /// Verifies size animation and clipping can be enabled or disabled independently of visual effects.
    @Test
    void animatedContentSeparatesSizeAndVisualPolicies() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region first = fixedRegion(72.0, 28.0);
            Region second = fixedRegion(180.0, 56.0);
            M3AnimatedContent animatedContent = new M3AnimatedContent(first);
            M3MotionSpec slow = slowMotionSpec();
            animatedContent.setContentTransform(new M3ContentTransform(
                    M3EnterTransition.fade(0.0).withMotionSpec(slow),
                    M3ExitTransition.fade(0.0).withMotionSpec(slow),
                    new M3SizeTransform(true, slow),
                    0.0
            ));

            Pane root = new Pane(animatedContent);
            new Scene(root);
            root.applyCss();
            root.layout();

            Node holder = assertInstanceOf(Node.class, first.getParent());
            Pane viewport = assertInstanceOf(Pane.class, holder.getParent());
            assertInstanceOf(Rectangle.class, viewport.getClip());

            animatedContent.setContent(second);
            assertEquals(72.0, animatedContent.prefWidth(-1.0), 1.0e-6);
            assertTrue(animatedContent.isTransitioning());
            animatedContent.finish();
            assertEquals(180.0, animatedContent.prefWidth(-1.0), 1.0e-6);

            animatedContent.setContentTransform(new M3ContentTransform(
                    M3EnterTransition.fade(0.0).withMotionSpec(slow),
                    M3ExitTransition.fade(0.0).withMotionSpec(slow),
                    null,
                    0.0
            ));
            assertNull(viewport.getClip());

            animatedContent.setContent(first);
            assertTrue(animatedContent.isTransitioning());
            assertEquals(72.0, animatedContent.prefWidth(-1.0), 1.0e-6);
        }));
    }

    /// Returns a long linear motion specification that keeps initial transition values observable.
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
