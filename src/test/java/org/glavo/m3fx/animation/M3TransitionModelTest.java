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
import org.glavo.m3fx.internal.animation.M3EnterTransitionImpl;
import org.glavo.m3fx.internal.animation.M3ExitTransitionImpl;
import org.glavo.m3fx.internal.animation.M3TransitionEffect;
import org.glavo.m3fx.internal.animation.M3TransitionEffectKind;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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

    /// Verifies Material content-transition presets are cached, axis-aware, and immutably configurable.
    @Test
    void contentTransformPresetsExposeMaterialPatterns() {
        assertSame(M3ContentTransform.FADE_THROUGH, M3ContentTransform.DEFAULT);
        assertNull(M3ContentTransform.NONE.sizeTransform());
        assertNotNull(M3ContentTransform.FADE.sizeTransform());
        assertNotNull(M3ContentTransform.FADE_THROUGH.sizeTransform());

        M3TransitionEffect fadeScale = effect(
                M3ContentTransform.FADE.targetContentEnter(),
                M3TransitionEffectKind.SCALE
        );
        assertEquals(0.8, fadeScale.value(), 0.0);
        assertNull(effectOrNull(
                M3ContentTransform.FADE.initialContentExit(),
                M3TransitionEffectKind.SCALE
        ));

        M3TransitionEffect fadeThroughFade = effect(
                M3ContentTransform.FADE_THROUGH.targetContentEnter(),
                M3TransitionEffectKind.FADE
        );
        M3TransitionEffect fadeThroughScale = effect(
                M3ContentTransform.FADE_THROUGH.targetContentEnter(),
                M3TransitionEffectKind.SCALE
        );
        assertEquals(Duration.millis(90.0), fadeThroughFade.delay());
        assertEquals(Duration.ZERO, fadeThroughScale.delay());

        M3ContentTransform forwardX = M3ContentTransform.sharedAxis(M3TransitionAxis.X, true);
        M3ContentTransform backwardX = M3ContentTransform.sharedAxis(M3TransitionAxis.X, false);
        assertSame(forwardX, M3ContentTransform.sharedAxis(M3TransitionAxis.X, true));
        assertNotSame(forwardX, backwardX);
        assertEquals(
                M3TransitionEdge.END,
                effect(forwardX.targetContentEnter(), M3TransitionEffectKind.SLIDE).edge()
        );
        assertEquals(
                M3TransitionEdge.START,
                effect(forwardX.initialContentExit(), M3TransitionEffectKind.SLIDE).edge()
        );
        assertEquals(
                M3TransitionEdge.START,
                effect(backwardX.targetContentEnter(), M3TransitionEffectKind.SLIDE).edge()
        );
        assertEquals(
                30.0,
                effect(forwardX.targetContentEnter(), M3TransitionEffectKind.SLIDE).value(),
                0.0
        );

        M3ContentTransform forwardY = M3ContentTransform.sharedAxis(M3TransitionAxis.Y, true);
        assertEquals(
                M3TransitionEdge.BOTTOM,
                effect(forwardY.targetContentEnter(), M3TransitionEffectKind.SLIDE).edge()
        );
        assertEquals(
                M3TransitionEdge.TOP,
                effect(forwardY.initialContentExit(), M3TransitionEffectKind.SLIDE).edge()
        );

        M3ContentTransform forwardZ = M3ContentTransform.sharedAxis(M3TransitionAxis.Z, true);
        M3ContentTransform backwardZ = M3ContentTransform.sharedAxis(M3TransitionAxis.Z, false);
        assertEquals(
                0.8,
                effect(forwardZ.targetContentEnter(), M3TransitionEffectKind.SCALE).value(),
                0.0
        );
        assertEquals(
                1.1,
                effect(forwardZ.initialContentExit(), M3TransitionEffectKind.SCALE).value(),
                0.0
        );
        assertEquals(
                1.1,
                effect(backwardZ.targetContentEnter(), M3TransitionEffectKind.SCALE).value(),
                0.0
        );
        assertThrows(NullPointerException.class, () -> M3ContentTransform.sharedAxis(null, true));

        M3ContentTransform unclipped = forwardX.withSizeTransform(null);
        assertNull(unclipped.sizeTransform());
        assertSame(unclipped, unclipped.withSizeTransform(null));
        assertSame(forwardX, forwardX.withTargetContentZIndex(0.0));
        assertEquals(-1.0, forwardX.withTargetContentZIndex(-1.0).targetContentZIndex(), 0.0);
        assertThrows(
                IllegalArgumentException.class,
                () -> forwardX.withTargetContentZIndex(Double.POSITIVE_INFINITY)
        );
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

    /// Returns one required effect from an enter transition.
    ///
    /// @param transition the transition to inspect
    /// @param kind       the required effect kind
    /// @return the matching effect
    private static M3TransitionEffect effect(
            M3EnterTransition transition,
            M3TransitionEffectKind kind
    ) {
        M3TransitionEffect effect = effectOrNull(transition, kind);
        assertNotNull(effect);
        return effect;
    }

    /// Returns one required effect from an exit transition.
    ///
    /// @param transition the transition to inspect
    /// @param kind       the required effect kind
    /// @return the matching effect
    private static M3TransitionEffect effect(
            M3ExitTransition transition,
            M3TransitionEffectKind kind
    ) {
        M3TransitionEffect effect = effectOrNull(transition, kind);
        assertNotNull(effect);
        return effect;
    }

    /// Returns one matching enter effect when present.
    ///
    /// @param transition the transition to inspect
    /// @param kind       the requested effect kind
    /// @return the matching effect, or `null`
    private static @Nullable M3TransitionEffect effectOrNull(
            M3EnterTransition transition,
            M3TransitionEffectKind kind
    ) {
        for (M3TransitionEffect effect : ((M3EnterTransitionImpl) transition).effects()) {
            if (effect.kind() == kind) {
                return effect;
            }
        }
        return null;
    }

    /// Returns one matching exit effect when present.
    ///
    /// @param transition the transition to inspect
    /// @param kind       the requested effect kind
    /// @return the matching effect, or `null`
    private static @Nullable M3TransitionEffect effectOrNull(
            M3ExitTransition transition,
            M3TransitionEffectKind kind
    ) {
        for (M3TransitionEffect effect : ((M3ExitTransitionImpl) transition).effects()) {
            if (effect.kind() == kind) {
                return effect;
            }
        }
        return null;
    }

    /// Creates a region with fixed minimum and preferred dimensions.
    private static Region fixedRegion(double width, double height) {
        Region region = new Region();
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        return region;
    }
}
