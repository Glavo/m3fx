// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the reusable value, visibility, and existing-container layout animation APIs.
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

    /// Verifies visibility transitions preserve content-owned visual properties and collapse after completion.
    @Test
    void animatedVisibilityOwnsOnlyItsPrivateWrapper() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Region content = new Region();
            content.setMinSize(80.0, 32.0);
            content.setPrefSize(80.0, 32.0);
            content.setOpacity(0.7);
            content.setScaleX(1.2);
            content.setScaleY(0.8);
            content.setTranslateX(6.0);

            M3AnimatedVisibility visibility = new M3AnimatedVisibility(content);
            Pane root = new Pane(visibility);
            new Scene(root);
            root.applyCss();
            root.layout();

            assertSame(content, visibility.getContent());
            assertTrue(visibility.prefWidth(-1.0) >= 80.0);

            visibility.setShowing(false);
            assertTrue(visibility.isTransitioning());
            visibility.snapToCurrentState();

            assertFalse(visibility.isShowing());
            assertFalse(visibility.isTransitioning());
            assertEquals(0.0, visibility.prefWidth(-1.0), 0.0);
            assertEquals(0.7, content.getOpacity(), 0.0);
            assertEquals(1.2, content.getScaleX(), 0.0);
            assertEquals(0.8, content.getScaleY(), 0.0);
            assertEquals(6.0, content.getTranslateX(), 0.0);

            M3MotionSettings.setReducedMotionRequested(root, true);
            visibility.setShowing(true);

            assertTrue(visibility.isShowing());
            assertFalse(visibility.isTransitioning());
            assertTrue(visibility.prefWidth(-1.0) >= 80.0);
        }));
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
}
