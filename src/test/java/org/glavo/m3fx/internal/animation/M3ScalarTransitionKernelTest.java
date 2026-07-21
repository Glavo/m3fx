// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3SpringParameters;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the scalar-channel semantics shared by finite property and node transitions.
@NotNullByDefault
final class M3ScalarTransitionKernelTest {
    /// Starts the JavaFX toolkit before tests exercise transition timing.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies property animation samples and running retargets match one scalar channel exactly.
    @Test
    void doubleTransitionMatchesScalarChannelAcrossRetargets() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            new Scene(owner, 120.0, 48.0);
            M3MotionSpec spec = M3MotionScheme.expressive().defaultSpatial();
            DoubleProperty value = new SimpleDoubleProperty(0.15);
            M3DoubleTransition transition = new M3DoubleTransition(
                    value,
                    M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
                    0.0,
                    1.0
            );
            M3ScalarChannel reference = new M3ScalarChannel(
                    M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD
            );

            reference.configure(0.15, 0.9, spec, Double.POSITIVE_INFINITY);
            transition.configure(spec, 0.9);
            assertDuration(reference.getDurationSeconds(), transition);

            M3Animation.playFromStart(owner, transition);
            double firstElapsedSeconds = reference.getDurationSeconds() * 0.28;
            transition.jumpTo(Duration.seconds(firstElapsedSeconds));
            firstElapsedSeconds = transition.getCurrentTime().toSeconds();
            assertEquals(reference.valueAt(firstElapsedSeconds), value.get(), 2.0e-4);

            double retargetStart = value.get();
            reference.configure(retargetStart, 0.1, spec, firstElapsedSeconds);
            transition.configure(spec, 0.1);
            assertDuration(reference.getDurationSeconds(), transition);

            M3Animation.playFromStart(owner, transition);
            double secondElapsedSeconds = reference.getDurationSeconds() * 0.22;
            transition.jumpTo(Duration.seconds(secondElapsedSeconds));
            secondElapsedSeconds = transition.getCurrentTime().toSeconds();
            assertEquals(reference.valueAt(secondElapsedSeconds), value.get(), 2.0e-4);

            M3Animation.finish(transition);
            assertEquals(0.1, value.get(), 0.0);
        });
    }

    /// Verifies bounded output remains clamped and discards velocity directed farther beyond a bound.
    @Test
    void boundedDoubleTransitionDropsOutwardVelocityAtClamp() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            new Scene(owner, 120.0, 48.0);
            M3MotionSpec spec = M3MotionSpec.spring(
                    new M3SpringParameters(0.28, 180.0),
                    Duration.millis(400.0),
                    M3MotionEasing.LINEAR
            );
            DoubleProperty value = new SimpleDoubleProperty(0.0);
            M3DoubleTransition transition = new M3DoubleTransition(
                    value,
                    M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
                    0.0,
                    1.0
            );
            M3ScalarChannel reference = new M3ScalarChannel(
                    M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD
            );

            reference.configure(0.0, 1.0, spec, Double.POSITIVE_INFINITY);
            transition.configure(spec, 1.0);
            M3Animation.playFromStart(owner, transition);

            double durationSeconds = reference.getDurationSeconds();
            double outwardElapsedSeconds = -1.0;
            for (int sample = 0; sample <= 400; sample++) {
                double elapsedSeconds = durationSeconds * sample / 400.0;
                transition.jumpTo(Duration.seconds(elapsedSeconds));
                elapsedSeconds = transition.getCurrentTime().toSeconds();
                assertTrue(value.get() >= 0.0 && value.get() <= 1.0);
                if (reference.valueAt(elapsedSeconds) >= 1.0
                        && reference.velocityAt(elapsedSeconds) > 0.0) {
                    outwardElapsedSeconds = elapsedSeconds;
                    break;
                }
            }

            assertTrue(outwardElapsedSeconds >= 0.0, "the underdamped spring should cross the upper bound");
            assertEquals(1.0, value.get(), 0.0);
            transition.configure(spec, 0.0);

            M3ScalarChannel zeroVelocityReference = new M3ScalarChannel(
                    M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD
            );
            zeroVelocityReference.configure(1.0, 0.0, spec, Double.POSITIVE_INFINITY);
            assertDuration(zeroVelocityReference.getDurationSeconds(), transition);
        });
    }

    /// Verifies the five node channels share one clock while retaining independent duration and velocity state.
    @Test
    void nodeTransitionCoordinatesIndependentScalarChannels() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            node.setOpacity(0.2);
            node.setScaleX(0.82);
            node.setScaleY(1.08);
            node.setTranslateX(-18.0);
            node.setTranslateY(7.0);
            Pane root = new Pane(node);
            new Scene(root, 180.0, 90.0);

            M3MotionSpec spec = M3MotionScheme.expressive().defaultSpatial();
            M3NodeTransition transition = new M3NodeTransition(node);
            M3ScalarChannel opacity = new M3ScalarChannel(1.0e-2);
            M3ScalarChannel scaleX = new M3ScalarChannel(5.0e-4);
            M3ScalarChannel scaleY = new M3ScalarChannel(5.0e-4);
            M3ScalarChannel translateX = new M3ScalarChannel(5.0e-1);
            M3ScalarChannel translateY = new M3ScalarChannel(5.0e-1);

            opacity.configure(0.2, 0.95, spec, Double.POSITIVE_INFINITY);
            scaleX.configure(0.82, 1.15, spec, Double.POSITIVE_INFINITY);
            scaleY.configure(1.08, 0.9, spec, Double.POSITIVE_INFINITY);
            translateX.configure(-18.0, 24.0, spec, Double.POSITIVE_INFINITY);
            translateY.configure(7.0, -12.0, spec, Double.POSITIVE_INFINITY);
            transition.configure(spec, 0.95, 1.15, 0.9, 24.0, -12.0);

            double firstDurationSeconds = longestDuration(opacity, scaleX, scaleY, translateX, translateY);
            assertDuration(firstDurationSeconds, transition);
            M3Animation.playFromStart(node, transition);
            double firstElapsedSeconds = firstDurationSeconds * 0.31;
            transition.jumpTo(Duration.seconds(firstElapsedSeconds));
            firstElapsedSeconds = transition.getCurrentTime().toSeconds();
            assertNodeValues(node, opacity, scaleX, scaleY, translateX, translateY, firstElapsedSeconds);

            opacity.configure(node.getOpacity(), 1.4, spec, firstElapsedSeconds);
            scaleX.configure(node.getScaleX(), 0.75, spec, firstElapsedSeconds);
            scaleY.configure(node.getScaleY(), 1.2, spec, firstElapsedSeconds);
            translateX.configure(node.getTranslateX(), -6.0, spec, firstElapsedSeconds);
            translateY.configure(node.getTranslateY(), 16.0, spec, firstElapsedSeconds);
            transition.configure(spec, 1.4, 0.75, 1.2, -6.0, 16.0);

            double secondDurationSeconds = longestDuration(opacity, scaleX, scaleY, translateX, translateY);
            assertDuration(secondDurationSeconds, transition);
            M3Animation.playFromStart(node, transition);
            double secondElapsedSeconds = secondDurationSeconds * 0.24;
            transition.jumpTo(Duration.seconds(secondElapsedSeconds));
            secondElapsedSeconds = transition.getCurrentTime().toSeconds();
            assertNodeValues(node, opacity, scaleX, scaleY, translateX, translateY, secondElapsedSeconds);

            M3Animation.finish(transition);
            assertEquals(1.0, node.getOpacity(), 0.0);
            assertEquals(0.75, node.getScaleX(), 0.0);
            assertEquals(1.2, node.getScaleY(), 0.0);
            assertEquals(-6.0, node.getTranslateX(), 0.0);
            assertEquals(16.0, node.getTranslateY(), 0.0);
        });
    }

    /// Verifies concurrent node transitions do not overwrite channels owned by another transition.
    @Test
    void concurrentNodeTransitionsPreserveDisjointChannels() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            node.setOpacity(0.0);
            node.setScaleY(0.8);
            Pane root = new Pane(node);
            new Scene(root, 120.0, 48.0);

            M3MotionSpec spec = M3MotionSpec.of(Duration.seconds(1.0), M3MotionEasing.LINEAR);
            M3NodeTransition effectsTransition = new M3NodeTransition(node);
            M3NodeTransition spatialTransition = new M3NodeTransition(node);
            effectsTransition.configure(spec, 1.0, 1.0, 0.8, 0.0, 0.0);
            spatialTransition.configure(spec, 0.0, 1.0, 1.0, 0.0, 0.0);

            M3Animation.playFromStart(node, effectsTransition);
            M3Animation.playFromStart(node, spatialTransition);
            effectsTransition.jumpTo(Duration.seconds(0.5));
            spatialTransition.jumpTo(Duration.seconds(0.5));

            assertEquals(0.5, node.getOpacity(), 1.0e-6);
            assertEquals(0.9, node.getScaleY(), 1.0e-6);

            M3Animation.finish(effectsTransition);
            M3Animation.finish(spatialTransition);
            assertEquals(1.0, node.getOpacity(), 0.0);
            assertEquals(1.0, node.getScaleY(), 0.0);
        });
    }

    /// Verifies reduced motion and scene detachment still settle scalar-backed transitions synchronously.
    @Test
    void scalarBackedTransitionsHonorMotionAndSceneLifecycle() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            Scene scene = new Scene(owner, 120.0, 48.0);
            M3MotionSpec slow = M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR);
            DoubleProperty value = new SimpleDoubleProperty(0.0);
            M3DoubleTransition propertyTransition = new M3DoubleTransition(value, 1.0e-3);

            M3MotionSettings.setReducedMotionRequested(owner, true);
            propertyTransition.configure(slow, 1.0);
            M3Animation.playFromStart(owner, propertyTransition);
            assertEquals(Animation.Status.STOPPED, propertyTransition.getStatus());
            assertEquals(1.0, value.get(), 0.0);

            M3MotionSettings.setReducedMotionRequested(owner, false);
            value.set(0.0);
            propertyTransition.configure(slow, 1.0);
            M3Animation.playFromStart(owner, propertyTransition);
            assertEquals(Animation.Status.RUNNING, propertyTransition.getStatus());

            scene.setRoot(new Pane());
            assertEquals(Animation.Status.STOPPED, propertyTransition.getStatus());
            assertEquals(1.0, value.get(), 0.0);

            Pane node = new Pane();
            Pane nodeRoot = new Pane(node);
            new Scene(nodeRoot, 120.0, 48.0);
            M3MotionSettings.setReducedMotionRequested(nodeRoot, true);
            M3NodeTransition nodeTransition = new M3NodeTransition(node);
            nodeTransition.configure(slow, 0.3, 0.8, 0.9, 12.0, -8.0);
            M3Animation.playFromStart(node, nodeTransition);

            assertEquals(Animation.Status.STOPPED, nodeTransition.getStatus());
            assertEquals(0.3, node.getOpacity(), 0.0);
            assertEquals(0.8, node.getScaleX(), 0.0);
            assertEquals(0.9, node.getScaleY(), 0.0);
            assertEquals(12.0, node.getTranslateX(), 0.0);
            assertEquals(-8.0, node.getTranslateY(), 0.0);
        }));
    }

    /// Asserts that a transition exposes the supplied scalar duration.
    private static void assertDuration(double expectedSeconds, Animation transition) {
        assertEquals(expectedSeconds * 1000.0, transition.getCycleDuration().toMillis(), 1.0e-6);
    }

    /// Returns the longest duration among five scalar channels.
    private static double longestDuration(
            M3ScalarChannel first,
            M3ScalarChannel second,
            M3ScalarChannel third,
            M3ScalarChannel fourth,
            M3ScalarChannel fifth
    ) {
        return Math.max(
                Math.max(first.getDurationSeconds(), Math.max(second.getDurationSeconds(), third.getDurationSeconds())),
                Math.max(fourth.getDurationSeconds(), fifth.getDurationSeconds())
        );
    }

    /// Asserts that a node contains the sampled values of five reference channels.
    private static void assertNodeValues(
            Pane node,
            M3ScalarChannel opacity,
            M3ScalarChannel scaleX,
            M3ScalarChannel scaleY,
            M3ScalarChannel translateX,
            M3ScalarChannel translateY,
            double elapsedSeconds
    ) {
        assertEquals(clampOpacity(opacity.valueAt(elapsedSeconds)), node.getOpacity(), 2.0e-4);
        assertEquals(scaleX.valueAt(elapsedSeconds), node.getScaleX(), 2.0e-4);
        assertEquals(scaleY.valueAt(elapsedSeconds), node.getScaleY(), 2.0e-4);
        assertEquals(translateX.valueAt(elapsedSeconds), node.getTranslateX(), 2.0e-2);
        assertEquals(translateY.valueAt(elapsedSeconds), node.getTranslateY(), 2.0e-2);
    }

    /// Restricts an expected opacity value to the JavaFX rendering interval.
    private static double clampOpacity(double opacity) {
        return Math.max(0.0, Math.min(1.0, opacity));
    }
}
