// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies reusable value, visibility, retained-content, and existing-container layout animation APIs.
@NotNullByDefault
final class M3LayoutAnimationTest {
    /// Starts the JavaFX toolkit before animation tests create scene-graph objects.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies scalar animation target state, reduced-motion completion, and bound-property rejection.
    @Test
    void doubleAnimatableHonorsWritableAndReducedMotionContracts() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Pane owner = new Pane();
            SimpleDoubleProperty value = new SimpleDoubleProperty(2.0);
            M3DoubleAnimatable animatable = new M3DoubleAnimatable(owner, value, 0.01);

            assertSame(owner, animatable.getOwner());
            assertSame(value, animatable.valueProperty());
            assertEquals(2.0, animatable.getValue(), 0.0);
            assertEquals(2.0, animatable.getTargetValue(), 0.0);
            assertEquals(0.01, animatable.getVisibilityThreshold(), 0.0);

            animatable.animateTo(8.0);

            assertEquals(8.0, value.get(), 0.0);
            assertEquals(8.0, animatable.getTargetValue(), 0.0);
            assertEquals(Animation.Status.STOPPED, animatable.getStatus());
            assertFalse(animatable.isRunning());

            animatable.snapTo(-3.0);
            animatable.finish();
            assertEquals(-3.0, value.get(), 0.0);
            assertEquals(-3.0, animatable.getTargetValue(), 0.0);

            value.bind(new SimpleDoubleProperty(4.0));
            assertThrows(IllegalStateException.class, () -> animatable.animateTo(5.0));
            assertThrows(IllegalStateException.class, () -> animatable.snapTo(5.0));
            assertThrows(IllegalStateException.class, animatable::finish);
        });
    }

    /// Verifies invalid scalar targets do not replace the last valid target.
    @Test
    void doubleAnimatableRejectsInvalidTargetsAtomically() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            SimpleDoubleProperty value = new SimpleDoubleProperty(1.0);
            M3DoubleAnimatable animatable = new M3DoubleAnimatable(owner, value, 0.01);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> animatable.animateTo(Double.NaN, M3MotionScheme.standard().defaultSpatial())
            );
            assertEquals(1.0, animatable.getTargetValue(), 0.0);
            assertThrows(IllegalArgumentException.class, () -> animatable.snapTo(Double.POSITIVE_INFINITY));
            assertEquals(1.0, animatable.getTargetValue(), 0.0);
        });
    }

    /// Verifies typed state, coordinated channels, property-driven retargeting, and synchronous completion.
    @Test
    void stateTransitionCoordinatesTypedDoubleChannels() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            new Scene(owner);
            SimpleDoubleProperty opacity = new SimpleDoubleProperty(-1.0);
            SimpleDoubleProperty translation = new SimpleDoubleProperty(-1.0);
            M3StateTransition<Boolean> transition = new M3StateTransition<>(owner, false);
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));

            transition.addDouble(opacity, expanded -> expanded ? 1.0 : 0.4, 0.01);
            transition.addDouble(translation, expanded -> expanded ? 72.0 : 0.0, 0.5);

            assertSame(owner, transition.getOwner());
            assertFalse(transition.getCurrentState());
            assertFalse(transition.getTargetState());
            assertEquals(0.4, opacity.get(), 0.0);
            assertEquals(0.0, translation.get(), 0.0);
            assertFalse(transition.isRunning());

            transition.setTargetState(true);

            assertTrue(transition.isRunning());
            assertFalse(transition.getCurrentState());
            assertTrue(transition.getTargetState());
            assertEquals(Animation.Status.RUNNING, transition.statusProperty().get());

            transition.setTargetState(false);
            assertFalse(transition.isRunning());
            assertFalse(transition.getCurrentState());
            assertEquals(0.4, opacity.get(), 0.0);
            assertEquals(0.0, translation.get(), 0.0);

            transition.setTargetState(true);
            assertTrue(transition.isRunning());
            transition.finish();

            assertFalse(transition.isRunning());
            assertTrue(transition.getCurrentState());
            assertTrue(transition.currentStateProperty().get());
            assertEquals(1.0, opacity.get(), 0.0);
            assertEquals(72.0, translation.get(), 0.0);

            transition.targetStateProperty().set(false);
            assertTrue(transition.isRunning());
            assertFalse(transition.getTargetState());
            transition.snapTo(false);

            assertFalse(transition.isRunning());
            assertFalse(transition.getCurrentState());
            assertEquals(0.4, opacity.get(), 0.0);
            assertEquals(0.0, translation.get(), 0.0);
        }));
    }

    /// Verifies target validation is atomic and registration constraints fail before animation begins.
    @Test
    void stateTransitionRejectsBrokenChannelsAtomically() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            SimpleDoubleProperty first = new SimpleDoubleProperty();
            SimpleDoubleProperty second = new SimpleDoubleProperty();
            M3StateTransition<Integer> transition = new M3StateTransition<>(owner, 0);
            transition.addDouble(first, state -> state, 0.01);
            transition.addDouble(second, state -> state == 2 ? Double.NaN : state * 2.0, 0.01);

            assertThrows(IllegalArgumentException.class, () -> transition.setTargetState(2));
            assertEquals(0, transition.getCurrentState());
            assertEquals(0, transition.getTargetState());
            assertEquals(0.0, first.get(), 0.0);
            assertEquals(0.0, second.get(), 0.0);

            assertThrows(IllegalArgumentException.class, () -> transition.targetStateProperty().set(2));
            assertEquals(0, transition.getTargetState());
            assertEquals(0.0, first.get(), 0.0);
            assertEquals(0.0, second.get(), 0.0);

            assertThrows(IllegalArgumentException.class, () -> transition.addDouble(first, state -> state, 0.01));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> transition.addDouble(new SimpleDoubleProperty(), state -> state, 0.0)
            );
            assertThrows(
                    IllegalArgumentException.class,
                    () -> transition.addDouble(
                            new SimpleDoubleProperty(Double.POSITIVE_INFINITY),
                            state -> state,
                            0.01
                    )
            );

            SimpleDoubleProperty bound = new SimpleDoubleProperty();
            bound.bind(new SimpleDoubleProperty(1.0));
            assertThrows(IllegalStateException.class, () -> transition.addDouble(bound, state -> state, 0.01));

            transition.targetStateProperty().bind(new SimpleObjectProperty<>(0));
            assertThrows(IllegalStateException.class, () -> transition.setTargetState(1));
            assertThrows(IllegalStateException.class, () -> transition.snapTo(1));
            transition.targetStateProperty().unbind();
        });
    }

    /// Verifies active channel mutation, reduced motion, and scene detachment settle shared state correctly.
    @Test
    void stateTransitionHandlesChannelAndOwnerLifecycle() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            Scene scene = new Scene(owner);
            SimpleDoubleProperty first = new SimpleDoubleProperty();
            SimpleDoubleProperty second = new SimpleDoubleProperty(8.0);
            M3StateTransition<Boolean> transition = new M3StateTransition<>(owner, false);
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));
            transition.addDouble(first, expanded -> expanded ? 40.0 : 0.0, 0.5);

            transition.setTargetState(true);
            transition.addDouble(second, expanded -> expanded ? 80.0 : 8.0, 0.5);
            assertTrue(transition.isRunning());
            assertTrue(transition.removeChannel(first));
            assertFalse(transition.removeChannel(first));
            transition.finish();

            assertEquals(0.0, first.get(), 0.0);
            assertEquals(80.0, second.get(), 0.0);
            assertTrue(transition.getCurrentState());

            transition.setTargetState(false);
            assertTrue(transition.isRunning());
            scene.setRoot(new Pane());

            assertFalse(transition.isRunning());
            assertFalse(transition.getCurrentState());
            assertEquals(8.0, second.get(), 0.0);

            Pane reducedOwner = new Pane();
            M3MotionSettings.setReducedMotionRequested(reducedOwner, true);
            SimpleDoubleProperty reducedValue = new SimpleDoubleProperty();
            M3StateTransition<Boolean> reduced = new M3StateTransition<>(reducedOwner, false);
            reduced.addDouble(reducedValue, expanded -> expanded ? 24.0 : 0.0, 0.5);
            reduced.setTargetState(true);

            assertFalse(reduced.isRunning());
            assertTrue(reduced.getCurrentState());
            assertEquals(24.0, reducedValue.get(), 0.0);

            reduced.setTargetState(false);
            reduced.clearChannels();
            assertFalse(reduced.isRunning());
            assertFalse(reduced.getCurrentState());
        }));
    }

    /// Verifies the complete visibility lifecycle, rapid reversal, detachment, and content-owned visual properties.
    @Test
    void animatedVisibilityTracksLifecycleAndOwnsOnlyPrivateVisuals() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region content = fixedRegion(80.0, 32.0);
            content.setOpacity(0.7);
            content.setScaleX(1.2);
            content.setScaleY(0.8);
            content.setTranslateX(6.0);

            M3AnimatedVisibility visibility = new M3AnimatedVisibility(content);
            configureSlowVisibility(visibility);
            AtomicBoolean detachedWhenHiddenWasPublished = new AtomicBoolean();
            visibility.stateProperty().addListener((observable, oldState, newState) -> {
                if (newState == M3VisibilityState.HIDDEN) {
                    detachedWhenHiddenWasPublished.set(content.getParent() == null);
                }
            });
            Pane root = new Pane(visibility);
            new Scene(root);
            root.applyCss();
            root.layout();

            assertSame(content, visibility.getContent());
            assertSame(M3VisibilityState.VISIBLE, visibility.getState());
            assertSame(M3VisibilityState.VISIBLE, visibility.stateProperty().get());
            assertNotNull(content.getParent());
            assertEquals(80.0, visibility.prefWidth(-1.0), 1.0e-6);

            visibility.setShowing(false);
            assertSame(M3VisibilityState.EXITING, visibility.getState());
            assertTrue(visibility.isTransitioning());
            assertNotNull(content.getParent());
            assertEquals(80.0, visibility.prefWidth(-1.0), 1.0e-6);

            visibility.setShowing(true);
            assertSame(M3VisibilityState.VISIBLE, visibility.getState());
            assertFalse(visibility.isTransitioning());
            assertNotNull(content.getParent());
            visibility.finish();

            assertTrue(visibility.isShowing());
            assertFalse(visibility.isTransitioning());
            assertSame(M3VisibilityState.VISIBLE, visibility.getState());
            assertNotNull(content.getParent());
            assertEquals(80.0, visibility.prefWidth(-1.0), 1.0e-6);

            visibility.setShowing(false);
            visibility.finish();

            assertFalse(visibility.isShowing());
            assertFalse(visibility.isTransitioning());
            assertSame(M3VisibilityState.HIDDEN, visibility.getState());
            assertNull(content.getParent());
            assertTrue(detachedWhenHiddenWasPublished.get());
            assertEquals(0.0, visibility.prefWidth(-1.0), 0.0);
            assertEquals(0.7, content.getOpacity(), 0.0);
            assertEquals(1.2, content.getScaleX(), 0.0);
            assertEquals(0.8, content.getScaleY(), 0.0);
            assertEquals(6.0, content.getTranslateX(), 0.0);

            visibility.setShowing(true);
            assertSame(M3VisibilityState.ENTERING, visibility.getState());
            assertNotNull(content.getParent());
            visibility.snapToCurrentState();

            assertSame(M3VisibilityState.VISIBLE, visibility.getState());
            assertNotNull(content.getParent());
            assertEquals(80.0, visibility.prefWidth(-1.0), 1.0e-6);
        }));
    }

    /// Verifies empty, hidden replacement, configuration, and reduced-motion visibility contracts together.
    @Test
    void animatedVisibilityHandlesReplacementConfigurationAndReducedMotion() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3AnimatedVisibility visibility = new M3AnimatedVisibility();
            assertTrue(visibility.isShowing());
            assertSame(M3VisibilityState.HIDDEN, visibility.getState());
            assertFalse(visibility.isTransitioning());
            assertEquals(Pos.CENTER, visibility.getAlignment());

            visibility.alignmentProperty().set(null);
            visibility.setSizeTransform(null);
            assertEquals(Pos.CENTER, visibility.getAlignment());
            assertNull(visibility.getSizeTransform());

            visibility.enterTransitionProperty().set(null);
            visibility.exitTransitionProperty().set(null);
            assertNotNull(visibility.getEnterTransition());
            assertNotNull(visibility.getExitTransition());

            visibility.setShowing(false);
            Region first = fixedRegion(72.0, 28.0);
            Region second = fixedRegion(144.0, 48.0);
            visibility.setContent(first);
            assertNull(first.getParent());
            assertSame(M3VisibilityState.HIDDEN, visibility.getState());
            visibility.setContent(second);
            assertNull(first.getParent());
            assertNull(second.getParent());

            Pane root = new Pane(visibility);
            new Scene(root);
            root.applyCss();
            root.layout();
            M3MotionSettings.setReducedMotionRequested(root, true);
            visibility.setShowing(true);

            assertTrue(visibility.isShowing());
            assertFalse(visibility.isTransitioning());
            assertSame(M3VisibilityState.VISIBLE, visibility.getState());
            assertNotNull(second.getParent());
            assertEquals(144.0, visibility.prefWidth(-1.0), 1.0e-6);

            Region third = fixedRegion(196.0, 56.0);
            visibility.setContent(third);
            assertNull(second.getParent());
            assertNotNull(third.getParent());
            assertSame(M3VisibilityState.VISIBLE, visibility.getState());
            assertEquals(196.0, visibility.prefWidth(-1.0), 1.0e-6);

            visibility.setContent(null);
            assertNull(third.getParent());
            assertSame(M3VisibilityState.HIDDEN, visibility.getState());
            assertEquals(0.0, visibility.prefWidth(-1.0), 0.0);
        }));
    }

    /// Verifies an active visibility exit settles and releases content when its starting scene is detached.
    @Test
    void animatedVisibilitySettlesWhenLeavingItsStartingScene() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region content = fixedRegion(96.0, 40.0);
            M3AnimatedVisibility visibility = new M3AnimatedVisibility(content);
            configureSlowVisibility(visibility);
            Pane root = new Pane(visibility);
            Scene scene = new Scene(root);
            root.applyCss();
            root.layout();

            visibility.setShowing(false);
            assertSame(M3VisibilityState.EXITING, visibility.getState());
            assertTrue(visibility.isTransitioning());
            assertNotNull(content.getParent());

            scene.setRoot(new Pane());

            assertFalse(visibility.isTransitioning());
            assertSame(M3VisibilityState.HIDDEN, visibility.getState());
            assertNull(content.getParent());
            assertEquals(0.0, visibility.prefWidth(-1.0), 0.0);
        }));
    }

    /// Verifies content replacement retains at most one outgoing node and animates the reported content size.
    @Test
    void animatedContentOwnsTransitionVisualsAndReleasesOutgoingNodes() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region first = fixedRegion(80.0, 32.0);
            first.setOpacity(0.7);
            first.setScaleX(1.2);
            first.setScaleY(0.8);
            first.setTranslateX(6.0);

            M3AnimatedContent animatedContent = new M3AnimatedContent(first);
            animatedContent.setContentTransform(slowContentTransform(true));

            Pane root = new Pane(animatedContent);
            new Scene(root);
            root.applyCss();
            root.layout();

            Region second = fixedRegion(160.0, 64.0);
            second.setOpacity(0.6);
            second.setScaleX(0.9);
            second.setScaleY(1.1);
            second.setTranslateY(4.0);
            animatedContent.setContent(second);

            assertTrue(animatedContent.isTransitioning());
            assertNotNull(first.getParent());
            assertNotNull(second.getParent());
            assertEquals(80.0, animatedContent.prefWidth(-1.0), 1.0e-6);
            assertEquals(32.0, animatedContent.prefHeight(-1.0), 1.0e-6);

            animatedContent.finish();

            assertFalse(animatedContent.isTransitioning());
            assertNull(first.getParent());
            assertNotNull(second.getParent());
            assertEquals(160.0, animatedContent.prefWidth(-1.0), 1.0e-6);
            assertEquals(64.0, animatedContent.prefHeight(-1.0), 1.0e-6);
            assertEquals(0.7, first.getOpacity(), 0.0);
            assertEquals(1.2, first.getScaleX(), 0.0);
            assertEquals(0.8, first.getScaleY(), 0.0);
            assertEquals(6.0, first.getTranslateX(), 0.0);
            assertEquals(0.6, second.getOpacity(), 0.0);
            assertEquals(0.9, second.getScaleX(), 0.0);
            assertEquals(1.1, second.getScaleY(), 0.0);
            assertEquals(4.0, second.getTranslateY(), 0.0);

            animatedContent.setContent(null);
            animatedContent.finish();
            assertNull(second.getParent());
            assertEquals(0.0, animatedContent.prefWidth(-1.0), 0.0);
            assertEquals(0.0, animatedContent.prefHeight(-1.0), 0.0);
        }));
    }

    /// Verifies rapid target reversal reuses the outgoing holder without resetting the target node.
    @Test
    void animatedContentReversesToOutgoingContentAndHonorsSizePolicy() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region first = fixedRegion(72.0, 28.0);
            Region second = fixedRegion(180.0, 56.0);
            M3AnimatedContent animatedContent = new M3AnimatedContent(first);
            animatedContent.setContentTransform(slowContentTransform(true));

            Pane root = new Pane(animatedContent);
            new Scene(root);
            root.applyCss();
            root.layout();

            animatedContent.setContent(second);
            animatedContent.setContent(first);

            assertSame(first, animatedContent.getContent());
            assertNotNull(first.getParent());
            assertNull(second.getParent());
            assertFalse(animatedContent.isTransitioning());

            animatedContent.finish();
            assertNotNull(first.getParent());
            assertNull(second.getParent());
            assertEquals(72.0, animatedContent.prefWidth(-1.0), 1.0e-6);

            animatedContent.setContentTransform(slowContentTransform(false));
            animatedContent.setContent(second);
            assertTrue(animatedContent.isTransitioning());
            assertEquals(180.0, animatedContent.prefWidth(-1.0), 1.0e-6);
            animatedContent.snapToCurrentState();
            assertNull(first.getParent());
            assertNotNull(second.getParent());

            Region third = fixedRegion(128.0, 44.0);
            animatedContent.setContent(first);
            animatedContent.setContent(third);
            assertNull(second.getParent());
            assertNotNull(first.getParent());
            assertNotNull(third.getParent());
            animatedContent.finish();
            assertNull(first.getParent());
            assertNotNull(third.getParent());
        }));
    }

    /// Verifies animated-content transition properties enforce their documented value contracts.
    @Test
    void animatedContentValidatesConfigurationProperties() {
        FxTestUtils.runOnFxThread(() -> {
            M3AnimatedContent animatedContent = new M3AnimatedContent();

            animatedContent.contentTransformProperty().set(null);
            assertSame(M3ContentTransform.DEFAULT, animatedContent.getContentTransform());
            animatedContent.alignmentProperty().set(null);
            assertEquals(javafx.geometry.Pos.TOP_LEFT, animatedContent.getAlignment());
        });
    }

    /// Verifies first attachment, reduced motion, and active-scene detachment settle content lifecycle correctly.
    @Test
    void animatedContentHandlesAttachmentAndReducedMotionLifecycle() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3AnimatedContent animatedContent = new M3AnimatedContent();
            animatedContent.setContentTransform(slowContentTransform(true));
            Pane root = new Pane(animatedContent);
            Scene scene = new Scene(root);
            root.applyCss();
            root.layout();

            Region first = fixedRegion(96.0, 40.0);
            animatedContent.setContent(first);
            assertTrue(animatedContent.isTransitioning());
            assertEquals(0.0, animatedContent.prefWidth(-1.0), 1.0e-6);

            scene.setRoot(new Pane());
            assertFalse(animatedContent.isTransitioning());
            assertEquals(96.0, animatedContent.prefWidth(-1.0), 1.0e-6);

            Pane nextRoot = new Pane(animatedContent);
            scene.setRoot(nextRoot);
            M3MotionSettings.setReducedMotionRequested(nextRoot, true);
            Region second = fixedRegion(144.0, 52.0);
            animatedContent.setContent(second);

            assertFalse(animatedContent.isTransitioning());
            assertNull(first.getParent());
            assertNotNull(second.getParent());
            assertEquals(144.0, animatedContent.prefWidth(-1.0), 1.0e-6);
        }));
    }

    /// Verifies an existing target node can invalidate and retarget the animated preferred size.
    @Test
    void animatedContentRemeasuresCurrentNodeWithoutReplacingIt() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region content = fixedRegion(84.0, 30.0);
            M3AnimatedContent animatedContent = new M3AnimatedContent(content);
            animatedContent.setContentTransform(slowContentTransform(true));
            Pane root = new Pane(animatedContent);
            new Scene(root);
            root.applyCss();
            root.layout();

            content.setMinWidth(196.0);
            content.setPrefWidth(196.0);
            animatedContent.layout();

            assertTrue(animatedContent.isTransitioning());
            assertEquals(84.0, animatedContent.prefWidth(-1.0), 1.0e-6);
            animatedContent.finish();
            assertEquals(196.0, animatedContent.prefWidth(-1.0), 1.0e-6);
            assertSame(content, animatedContent.getContent());
        }));
    }

    /// Verifies finite property animations settle when their owner leaves the scene that started the run.
    @Test
    void finiteAnimationSettlesWhenOwnerLeavesStartingScene() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            Scene scene = new Scene(owner);
            SimpleDoubleProperty value = new SimpleDoubleProperty(0.0);
            M3DoubleAnimatable animatable = new M3DoubleAnimatable(owner, value, 0.01);
            animatable.animateTo(
                    12.0,
                    M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR)
            );

            assertTrue(animatable.isRunning());
            scene.setRoot(new Pane());

            assertFalse(animatable.isRunning());
            assertEquals(12.0, value.get(), 0.0);
        }));
    }

    /// Verifies a finite animation adopts its first scene and settles after subsequent detachment.
    @Test
    void finiteAnimationsAdoptFirstSceneAndSettleAfterDetachment() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane detachedOwner = new Pane();
            SimpleDoubleProperty detachedValue = new SimpleDoubleProperty();
            M3DoubleAnimatable detachedAnimation = new M3DoubleAnimatable(detachedOwner, detachedValue, 0.01);
            detachedAnimation.animateTo(
                    18.0,
                    M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR)
            );

            assertTrue(detachedAnimation.isRunning());
            Pane root = new Pane(detachedOwner);
            new Scene(root);
            assertTrue(detachedAnimation.isRunning());

            root.getChildren().remove(detachedOwner);
            assertFalse(detachedAnimation.isRunning());
            assertEquals(18.0, detachedValue.get(), 0.0);
        }));
    }

    /// Verifies finite animations settle when their presenting window is hidden or already hidden.
    @Test
    @Tier2Test
    void finiteAnimationsSettleWhenWindowHides() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Stage stage = new Stage();
            try {
                Pane visibleOwner = new Pane();
                stage.setScene(new Scene(visibleOwner));
                stage.show();

                SimpleDoubleProperty visibleValue = new SimpleDoubleProperty();
                M3DoubleAnimatable visibleAnimation = new M3DoubleAnimatable(visibleOwner, visibleValue, 0.01);
                visibleAnimation.animateTo(
                        24.0,
                        M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR)
                );
                assertTrue(visibleAnimation.isRunning());

                stage.hide();
                assertFalse(visibleAnimation.isRunning());
                assertEquals(24.0, visibleValue.get(), 0.0);

                SimpleDoubleProperty hiddenValue = new SimpleDoubleProperty();
                M3DoubleAnimatable hiddenAnimation = new M3DoubleAnimatable(visibleOwner, hiddenValue, 0.01);
                hiddenAnimation.animateTo(
                        30.0,
                        M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR)
                );
                assertFalse(hiddenAnimation.isRunning());
                assertEquals(30.0, hiddenValue.get(), 0.0);
            } finally {
                stage.close();
            }
        }));
    }

    /// Verifies one placement change does not publish a redundant animation restart on the next event turn.
    @Test
    void layoutTransitionDoesNotRestartAfterSingleCoordinateChange() {
        AtomicInteger runningTransitions = new AtomicInteger();
        AtomicReference<@Nullable M3LayoutTransition> transitionReference = new AtomicReference<>();

        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane parent = new Pane();
            Region child = new Region();
            parent.getChildren().add(child);
            new Scene(parent);
            parent.applyCss();
            parent.layout();

            M3LayoutTransition transition = new M3LayoutTransition(parent);
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));
            transition.statusProperty().addListener((observable, oldStatus, newStatus) -> {
                if (newStatus == Animation.Status.RUNNING) {
                    runningTransitions.incrementAndGet();
                }
            });
            transition.start();
            transitionReference.set(transition);

            child.setLayoutX(80.0);
            assertTrue(transition.isRunning());
            assertEquals(1, runningTransitions.get());
        }));

        FxTestUtils.runOnFxThread(() -> {
            M3LayoutTransition transition = transitionReference.get();
            assertNotNull(transition);
            assertEquals(1, runningTransitions.get());
            transition.stop();
        });
    }

    /// Verifies layout animation preserves rendered position, supports retargeting, and retains user transforms.
    @Test
    void layoutTransitionAnimatesExistingChildPlacementWithoutRelayoutPulses() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane parent = new Pane();
            parent.resize(400.0, 120.0);
            Region child = new Region();
            child.resize(40.0, 24.0);
            child.relocate(20.0, 16.0);
            child.setScaleX(1.2);
            child.setScaleY(0.9);
            child.setRotate(18.0);
            Scale userScale = new Scale(1.1, 1.1);
            child.getTransforms().add(userScale);
            parent.getChildren().add(child);
            new Scene(parent);
            parent.applyCss();
            parent.layout();

            int parentPropertyCount = parent.getProperties().size();
            M3LayoutTransition transition = new M3LayoutTransition(parent);
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));
            transition.start();

            double renderedX = child.getBoundsInParent().getMinX();
            child.setLayoutX(120.0);

            assertTrue(transition.isActive());
            assertTrue(transition.isRunning());
            assertEquals(renderedX, child.getBoundsInParent().getMinX(), 1.0e-6);
            assertTrue(child.getTransforms().contains(userScale));

            child.setLayoutX(200.0);
            assertEquals(renderedX, child.getBoundsInParent().getMinX(), 1.0e-6);

            transition.finish();
            assertFalse(transition.isRunning());
            assertEquals(renderedX + 180.0, child.getBoundsInParent().getMinX(), 1.0e-6);

            transition.stop();
            assertFalse(transition.isActive());
            assertEquals(parentPropertyCount, parent.getProperties().size());
            assertEquals(1, child.getTransforms().size());
            assertSame(userScale, child.getTransforms().get(0));
        }));
    }

    /// Verifies lifecycle exclusivity, initial placement suppression, and permanent disposal.
    @Test
    void layoutTransitionCleansUpItsLifecycle() {
        FxTestUtils.runOnFxThread(() -> {
            Pane parent = new Pane();
            parent.resize(300.0, 100.0);
            Region first = new Region();
            parent.getChildren().add(first);
            new Scene(parent);
            parent.applyCss();
            parent.layout();

            M3LayoutTransition transition = new M3LayoutTransition(parent);
            M3LayoutTransition competing = new M3LayoutTransition(parent);
            transition.start();
            assertThrows(IllegalStateException.class, competing::start);

            Region added = new Region();
            parent.requestLayout();
            parent.getChildren().add(added);
            added.setLayoutX(64.0);
            assertFalse(transition.isRunning());
            parent.layout();

            added.setLayoutX(128.0);
            assertTrue(transition.isRunning());
            parent.getChildren().remove(added);
            assertTrue(added.getTransforms().isEmpty());

            transition.dispose();
            assertTrue(transition.isDisposed());
            assertFalse(transition.isActive());
            assertThrows(IllegalStateException.class, transition::start);

            competing.start();
            competing.stop();
        });
    }

    /// Applies an intentionally long specification to every visibility channel used by lifecycle assertions.
    private static void configureSlowVisibility(M3AnimatedVisibility visibility) {
        M3MotionSpec slow = slowMotionSpec();
        visibility.setEnterTransition(
                M3EnterTransition.fade(0.0)
                        .and(M3EnterTransition.scale(0.92))
                        .withMotionSpec(slow)
        );
        visibility.setExitTransition(
                M3ExitTransition.fade(0.0)
                        .and(M3ExitTransition.scale(0.92))
                        .withMotionSpec(slow)
        );
        visibility.setSizeTransform(new M3SizeTransform(true, slow));
    }

    /// Creates a deterministic content transform with optionally animated size for lifecycle assertions.
    private static M3ContentTransform slowContentTransform(boolean animateSize) {
        M3MotionSpec slow = slowMotionSpec();
        return new M3ContentTransform(
                M3EnterTransition.fade(0.0)
                        .and(M3EnterTransition.scale(0.92))
                        .withMotionSpec(slow),
                M3ExitTransition.fade(0.0)
                        .and(M3ExitTransition.scale(0.92))
                        .withMotionSpec(slow),
                animateSize ? new M3SizeTransform(true, slow) : null,
                0.0
        );
    }

    /// Returns the long linear specification shared by transition lifecycle tests.
    private static M3MotionSpec slowMotionSpec() {
        return M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR);
    }

    /// Creates a region whose minimum and preferred dimensions are fixed for layout assertions.
    private static Region fixedRegion(double width, double height) {
        Region region = new Region();
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        return region;
    }
}
