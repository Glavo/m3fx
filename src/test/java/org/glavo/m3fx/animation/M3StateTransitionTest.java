// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies multi-component values and externally driven progress in [M3StateTransition].
@NotNullByDefault
final class M3StateTransitionTest {
    /// Starts the JavaFX toolkit before state-transition tests create scene-graph objects.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies built-in converters expose stable component order and reconstruct valid values.
    @Test
    void builtInVectorConvertersRoundTripCommonJavaFxValues() {
        Color color = Color.color(0.1, 0.3, 0.7, 0.8);
        assertEquals(color, roundTrip(color, M3VectorConverters.COLOR));

        Point2D point = new Point2D(-12.5, 40.25);
        assertEquals(point, roundTrip(point, M3VectorConverters.POINT_2D));
        Point3D point3D = new Point3D(-2.0, 4.0, 8.0);
        assertEquals(point3D, roundTrip(point3D, M3VectorConverters.POINT_3D));
        Rectangle2D rectangle = new Rectangle2D(10.0, 20.0, 80.0, 40.0);
        assertEquals(rectangle, roundTrip(rectangle, M3VectorConverters.RECTANGLE_2D));

        assertEquals(4, M3VectorConverters.COLOR.getComponentCount());
        assertEquals(1.0 / 255.0, M3VectorConverters.COLOR.getVisibilityThreshold(3), 0.0);
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> M3VectorConverters.POINT_2D.getComponent(point, 2)
        );
    }

    /// Verifies one seek clock evaluates scalar and immutable-value channels in both directions.
    @Test
    void seekCoordinatesVectorValuesAndContinuesWithoutJumping() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            new Scene(owner);
            Color collapsedColor = Color.color(0.0, 0.2, 0.4);
            Color expandedColor = Color.color(0.8, 0.6, 0.2);
            Point2D collapsedPoint = new Point2D(0.0, 20.0);
            Point2D expandedPoint = new Point2D(100.0, 220.0);
            SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(Color.TRANSPARENT);
            SimpleObjectProperty<Point2D> point = new SimpleObjectProperty<>(Point2D.ZERO);

            M3StateTransition<Boolean> transition = new M3StateTransition<>(owner, false);
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(2.0), M3MotionEasing.LINEAR));
            transition.addValue(
                    color,
                    expanded -> expanded ? expandedColor : collapsedColor,
                    M3VectorConverters.COLOR
            );
            transition.addValue(
                    point,
                    expanded -> expanded ? expandedPoint : collapsedPoint,
                    M3VectorConverters.POINT_2D
            );

            assertSame(collapsedColor, color.get());
            assertSame(collapsedPoint, point.get());
            assertEquals(1.0, transition.getProgress(), 0.0);
            assertFalse(transition.isSeeking());

            transition.seekTo(true, 0.25);

            assertTrue(transition.isSeeking());
            assertEquals(Animation.Status.STOPPED, transition.getStatus());
            assertFalse(transition.getCurrentState());
            assertTrue(transition.getTargetState());
            assertEquals(0.25, transition.getProgress(), 0.0);
            assertColorEquals(interpolate(collapsedColor, expandedColor, 0.25), color.get());
            assertEquals(new Point2D(25.0, 70.0), point.get());

            transition.seekTo(true, 0.75);
            assertColorEquals(interpolate(collapsedColor, expandedColor, 0.75), color.get());
            assertEquals(new Point2D(75.0, 170.0), point.get());

            transition.seekTo(true, 0.25);
            assertColorEquals(interpolate(collapsedColor, expandedColor, 0.25), color.get());
            assertEquals(new Point2D(25.0, 70.0), point.get());

            transition.animateToTarget();
            assertFalse(transition.isSeeking());
            assertTrue(transition.isRunning());
            transition.finish();

            assertFalse(transition.isRunning());
            assertTrue(transition.getCurrentState());
            assertEquals(1.0, transition.getProgress(), 0.0);
            assertSame(expandedColor, color.get());
            assertSame(expandedPoint, point.get());
        }));
    }

    /// Verifies changing a seek target captures the currently rendered value as the next path's start.
    @Test
    void changingSeekTargetStartsFromRenderedValue() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            SimpleObjectProperty<Point2D> point = new SimpleObjectProperty<>(Point2D.ZERO);
            M3StateTransition<Target> transition = new M3StateTransition<>(owner, Target.START);
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(1.0), M3MotionEasing.LINEAR));
            transition.addValue(
                    point,
                    target -> new Point2D(target.x, target.x * 2.0),
                    M3VectorConverters.POINT_2D
            );

            transition.seekTo(Target.MIDDLE, 0.5);
            assertEquals(new Point2D(50.0, 100.0), point.get());

            transition.seekTo(Target.END, 0.5);
            assertEquals(new Point2D(125.0, 250.0), point.get());
            assertEquals(Target.START, transition.getCurrentState());
            assertEquals(Target.END, transition.getTargetState());

            transition.finish();
            assertEquals(new Point2D(200.0, 400.0), point.get());
            assertEquals(Target.END, transition.getCurrentState());
        });
    }

    /// Verifies each channel may select timing from the active state segment while sharing normalized play time.
    @Test
    void channelSpecificationsUseSourceAndTargetStates() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            SimpleDoubleProperty fast = new SimpleDoubleProperty();
            SimpleDoubleProperty slow = new SimpleDoubleProperty();
            M3StateTransition<Boolean> transition = new M3StateTransition<>(owner, false);
            transition.addDouble(
                    fast,
                    expanded -> expanded ? 100.0 : 0.0,
                    0.01,
                    (initialState, targetState) -> {
                        assertFalse(initialState);
                        assertTrue(targetState);
                        return M3MotionSpec.of(Duration.seconds(1.0), M3MotionEasing.LINEAR);
                    }
            );
            transition.addDouble(
                    slow,
                    expanded -> expanded ? 100.0 : 0.0,
                    0.01,
                    (initialState, targetState) -> M3MotionSpec.of(
                            Duration.seconds(2.0),
                            M3MotionEasing.LINEAR
                    )
            );

            transition.seekTo(true, 0.25);
            assertEquals(50.0, fast.get(), 1.0e-9);
            assertEquals(25.0, slow.get(), 1.0e-9);

            transition.seekTo(true, 0.5);
            assertEquals(100.0, fast.get(), 1.0e-9);
            assertEquals(50.0, slow.get(), 1.0e-9);
        });
    }

    /// Verifies a failing target function or segment selector cannot partially replace an active seek configuration.
    @Test
    void failedRetargetValidationLeavesActiveConfigurationIntact() {
        FxTestUtils.runOnFxThread(() -> {
            M3MotionSpec oneSecond = M3MotionSpec.of(Duration.seconds(1.0), M3MotionEasing.LINEAR);

            SimpleDoubleProperty stagedValue = new SimpleDoubleProperty();
            SimpleDoubleProperty rejectedTargetValue = new SimpleDoubleProperty();
            SimpleDoubleProperty stableValue = new SimpleDoubleProperty();
            AtomicBoolean rejectTarget = new AtomicBoolean();
            M3StateTransition<Boolean> targetTransition = new M3StateTransition<>(new Pane(), false);
            targetTransition.setMotionSpec(oneSecond);
            targetTransition.addDouble(stagedValue, target -> target ? 100.0 : 0.0, 0.01);
            targetTransition.addDouble(
                    rejectedTargetValue,
                    target -> {
                        if (!target && rejectTarget.get()) {
                            throw new IllegalArgumentException("rejected target");
                        }
                        return target ? 100.0 : 0.0;
                    },
                    0.01
            );
            targetTransition.addDouble(stableValue, target -> target ? 100.0 : 0.0, 0.01);
            targetTransition.seekTo(true, 0.5);
            rejectTarget.set(true);

            assertThrows(IllegalArgumentException.class, () -> targetTransition.seekTo(false, 0.25));
            assertTrue(targetTransition.getTargetState());
            assertTrue(targetTransition.removeChannel(rejectedTargetValue));
            assertEquals(50.0, stagedValue.get(), 1.0e-9);
            assertEquals(50.0, stableValue.get(), 1.0e-9);

            SimpleDoubleProperty selectedSpecValue = new SimpleDoubleProperty();
            SimpleDoubleProperty rejectedSpecValue = new SimpleDoubleProperty();
            SimpleDoubleProperty sharedSpecValue = new SimpleDoubleProperty();
            AtomicBoolean rejectSpec = new AtomicBoolean();
            M3StateTransition<Boolean> specTransition = new M3StateTransition<>(new Pane(), false);
            specTransition.setMotionSpec(oneSecond);
            specTransition.addDouble(
                    selectedSpecValue,
                    target -> target ? 100.0 : 0.0,
                    0.01,
                    (initial, target) -> target
                            ? oneSecond
                            : M3MotionSpec.of(Duration.seconds(4.0), M3MotionEasing.LINEAR)
            );
            specTransition.addDouble(
                    rejectedSpecValue,
                    target -> target ? 100.0 : 0.0,
                    0.01,
                    (initial, target) -> {
                        if (!target && rejectSpec.get()) {
                            throw new IllegalStateException("rejected specification");
                        }
                        return oneSecond;
                    }
            );
            specTransition.addDouble(sharedSpecValue, target -> target ? 100.0 : 0.0, 0.01);
            specTransition.seekTo(true, 0.5);
            rejectSpec.set(true);

            assertThrows(IllegalStateException.class, () -> specTransition.seekTo(false, 0.25));
            assertTrue(specTransition.getTargetState());
            assertTrue(specTransition.removeChannel(rejectedSpecValue));
            assertEquals(50.0, selectedSpecValue.get(), 1.0e-9);
            assertEquals(50.0, sharedSpecValue.get(), 1.0e-9);
        });
    }

    /// Verifies reduced motion settles autonomous continuation but preserves direct seek feedback.
    @Test
    void reducedMotionPreservesDirectSeekAndSettlesContinuation() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            M3MotionSettings.setReducedMotionRequested(owner, true);
            SimpleObjectProperty<Point2D> point = new SimpleObjectProperty<>(Point2D.ZERO);
            M3StateTransition<Boolean> transition = new M3StateTransition<>(owner, false);
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(1.0), M3MotionEasing.LINEAR));
            transition.addValue(
                    point,
                    expanded -> expanded ? new Point2D(80.0, 40.0) : Point2D.ZERO,
                    M3VectorConverters.POINT_2D
            );

            transition.seekTo(true, 0.5);
            assertTrue(transition.isSeeking());
            assertEquals(new Point2D(40.0, 20.0), point.get());

            transition.animateToTarget();
            assertFalse(transition.isSeeking());
            assertFalse(transition.isRunning());
            assertTrue(transition.getCurrentState());
            assertEquals(new Point2D(80.0, 40.0), point.get());
        }));
    }

    /// Verifies malformed value converters and bound properties fail before a channel is installed.
    @Test
    void valueChannelsRejectInvalidContractsAtomically() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            M3StateTransition<Boolean> transition = new M3StateTransition<>(owner, false);
            SimpleObjectProperty<Point2D> point = new SimpleObjectProperty<>(Point2D.ZERO);

            M3VectorConverter<Point2D> emptyConverter = new M3VectorConverter<>() {
                /// Returns an invalid empty component count for contract testing.
                @Override
                public int getComponentCount() {
                    return 0;
                }

                /// This method is unreachable because registration rejects the component count first.
                @Override
                public double getComponent(Point2D value, int index) {
                    throw new AssertionError();
                }

                /// This method is unreachable because registration rejects the component count first.
                @Override
                public Point2D createValue(java.util.function.IntToDoubleFunction components) {
                    throw new AssertionError();
                }
            };

            assertThrows(
                    IllegalArgumentException.class,
                    () -> transition.addValue(point, state -> Point2D.ZERO, emptyConverter)
            );
            assertEquals(Point2D.ZERO, point.get());

            assertThrows(
                    NullPointerException.class,
                    () -> transition.addValue(
                            point,
                            state -> null,
                            M3VectorConverters.POINT_2D
                    )
            );
            assertEquals(Point2D.ZERO, point.get());

            SimpleObjectProperty<Point2D> bound = new SimpleObjectProperty<>(Point2D.ZERO);
            bound.bind(new SimpleObjectProperty<>(Point2D.ZERO));
            assertThrows(
                    IllegalStateException.class,
                    () -> transition.addValue(
                            bound,
                            state -> Point2D.ZERO,
                            M3VectorConverters.POINT_2D
                    )
            );
        });
    }

    /// Reconstructs one value using the converter's own components.
    private static <T> T roundTrip(T value, M3VectorConverter<T> converter) {
        double[] components = new double[converter.getComponentCount()];
        for (int index = 0; index < components.length; index++) {
            components[index] = converter.getComponent(value, index);
        }
        return converter.createValue(index -> components[index]);
    }

    /// Returns a component-wise linear color interpolation.
    private static Color interpolate(Color start, Color end, double fraction) {
        return new Color(
                start.getRed() + (end.getRed() - start.getRed()) * fraction,
                start.getGreen() + (end.getGreen() - start.getGreen()) * fraction,
                start.getBlue() + (end.getBlue() - start.getBlue()) * fraction,
                start.getOpacity() + (end.getOpacity() - start.getOpacity()) * fraction
        );
    }

    /// Compares color components without relying on object identity.
    private static void assertColorEquals(Color expected, Color actual) {
        assertEquals(expected.getRed(), actual.getRed(), 1.0e-9);
        assertEquals(expected.getGreen(), actual.getGreen(), 1.0e-9);
        assertEquals(expected.getBlue(), actual.getBlue(), 1.0e-9);
        assertEquals(expected.getOpacity(), actual.getOpacity(), 1.0e-9);
    }

    /// Defines deterministic coordinates for retargeted seek tests.
    private enum Target {
        /// The initial coordinate.
        START(0.0),

        /// The first seek target.
        MIDDLE(100.0),

        /// The replacement seek target.
        END(200.0);

        /// The x coordinate represented by this state.
        private final double x;

        /// Creates a target coordinate.
        Target(double x) {
            this.x = x;
        }
    }
}
