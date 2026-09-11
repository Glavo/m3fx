// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies layout-animation ownership when transform listeners change the lifecycle or child list.
@NotNullByDefault
final class M3LayoutTransitionLifecycleTest {
    /// Starts JavaFX before constructing layout containers.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Releases a partially installed child when its transform listener stops or disposes the owner.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void installationCanStopLifecycle(boolean dispose) {
        runWithoutListenerFailures(() -> {
            Pane first = new Pane();
            Pane second = new Pane();
            Pane parent = parent(first, second);
            M3LayoutTransition transition = new M3LayoutTransition(parent);
            AtomicBoolean stopped = new AtomicBoolean();
            first.getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (!first.getTransforms().isEmpty() && stopped.compareAndSet(false, true)) {
                    if (dispose) {
                        transition.dispose();
                    } else {
                        transition.stop();
                    }
                }
            });
            transition.start();
            assertFalse(transition.isActive());
            assertEquals(dispose, transition.isDisposed());
            assertTrue(first.getTransforms().isEmpty());
            assertTrue(second.getTransforms().isEmpty());
            first.setLayoutX(20.0);
            assertFalse(transition.isRunning());
            M3LayoutTransition replacement = new M3LayoutTransition(parent);
            replacement.start();
            assertEquals(1, first.getTransforms().size());
            replacement.stop();
            assertTrue(first.getTransforms().isEmpty());
        });
    }

    /// Reconciles a new child list published while the first child's transform is being installed.
    @Test
    void installationCanReplaceChildren() {
        runWithoutListenerFailures(() -> {
            Pane first = new Pane();
            Pane second = new Pane();
            Pane third = new Pane();
            Pane parent = parent(first, second);
            M3LayoutTransition transition = new M3LayoutTransition(parent);
            AtomicBoolean replaced = new AtomicBoolean();
            first.getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (!first.getTransforms().isEmpty() && replaced.compareAndSet(false, true)) {
                    parent.getChildren().setAll(second, third);
                }
            });
            try {
                transition.start();
                assertTrue(first.getTransforms().isEmpty());
                assertEquals(1, second.getTransforms().size());
                assertEquals(1, third.getTransforms().size());
            } finally {
                transition.stop();
            }
            assertTrue(second.getTransforms().isEmpty());
            assertTrue(third.getTransforms().isEmpty());
        });
    }

    /// Does not leave a newly added child attached after its installation stops observation.
    @Test
    void addedChildCanStopLifecycle() {
        runWithoutListenerFailures(() -> {
            Pane parent = parent();
            Pane child = new Pane();
            M3LayoutTransition transition = new M3LayoutTransition(parent);
            AtomicBoolean stopped = new AtomicBoolean();
            child.getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (!child.getTransforms().isEmpty() && stopped.compareAndSet(false, true)) {
                    transition.stop();
                }
            });
            transition.start();
            parent.getChildren().add(child);
            assertFalse(transition.isActive());
            assertTrue(child.getTransforms().isEmpty());
            child.setLayoutX(20.0);
            assertFalse(transition.isRunning());
        });
    }

    /// Preserves a new installation started while the previous installation removes its transforms.
    @Test
    void removalCanRestartLifecycle() {
        runWithoutListenerFailures(() -> {
            Pane first = new Pane();
            Pane second = new Pane();
            Pane parent = parent(first, second);
            M3LayoutTransition transition = new M3LayoutTransition(parent);
            transition.start();
            AtomicBoolean restarted = new AtomicBoolean();
            second.getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (second.getTransforms().isEmpty() && restarted.compareAndSet(false, true)) {
                    transition.start();
                }
            });
            transition.stop();
            try {
                assertTrue(transition.isActive());
                assertEquals(1, first.getTransforms().size());
                assertEquals(1, second.getTransforms().size());
            } finally {
                transition.stop();
            }
            assertTrue(first.getTransforms().isEmpty());
            assertTrue(second.getTransforms().isEmpty());
        });
    }

    /// Does not use a stale state-list index after a transform callback removes an earlier child.
    @Test
    void removalCanRemoveAnotherChild() {
        runWithoutListenerFailures(() -> {
            Pane first = new Pane();
            Pane second = new Pane();
            Pane third = new Pane();
            Pane parent = parent(first, second, third);
            M3LayoutTransition transition = new M3LayoutTransition(parent);
            transition.start();
            third.getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (third.getTransforms().isEmpty()) {
                    parent.getChildren().remove(first);
                }
            });
            try {
                parent.getChildren().remove(third);
                assertEquals(List.of(second), parent.getChildren());
                assertTrue(first.getTransforms().isEmpty());
                assertTrue(third.getTransforms().isEmpty());
                assertEquals(1, second.getTransforms().size());
            } finally {
                transition.stop();
            }
            assertTrue(second.getTransforms().isEmpty());
        });
    }

    /// Makes permanent disposal visible before transform-removal callbacks attempt to restart.
    @Test
    void disposalRejectsCallbackRestart() {
        runWithoutListenerFailures(() -> {
            Pane child = new Pane();
            M3LayoutTransition transition = new M3LayoutTransition(parent(child));
            transition.start();
            AtomicBoolean rejected = new AtomicBoolean();
            child.getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (child.getTransforms().isEmpty()) {
                    try {
                        transition.start();
                    } catch (IllegalStateException expected) {
                        rejected.set(true);
                    }
                }
            });
            transition.dispose();
            assertTrue(rejected.get());
            assertTrue(transition.isDisposed());
            assertFalse(transition.isActive());
            assertTrue(child.getTransforms().isEmpty());
        });
    }

    /// Lets the latest cleanup request cancel a deferred restart.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cleanupCanCancelPendingRestart(boolean dispose) {
        runWithoutListenerFailures(() -> {
            Pane child = new Pane();
            M3LayoutTransition transition = new M3LayoutTransition(parent(child));
            transition.start();
            child.getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (child.getTransforms().isEmpty()) {
                    transition.start();
                    if (dispose) {
                        transition.dispose();
                    } else {
                        transition.stop();
                    }
                }
            });
            transition.stop();
            assertFalse(transition.isActive());
            assertEquals(dispose, transition.isDisposed());
            assertTrue(child.getTransforms().isEmpty());
        });
    }

    /// Does not restart a retargeted animation after its stopped-status callback ends the lifecycle.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stoppedStatusCanEndLifecycleDuringRetarget(boolean dispose) {
        runWithoutListenerFailures(() -> {
            Pane child = new Pane();
            M3LayoutTransition transition = new M3LayoutTransition(parent(child));
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));
            transition.start();
            child.setLayoutX(20.0);
            assertTrue(transition.isRunning());
            transition.statusProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == javafx.animation.Animation.Status.STOPPED) {
                    if (dispose) {
                        transition.dispose();
                    } else {
                        transition.stop();
                    }
                }
            });
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(4.0), M3MotionEasing.LINEAR));
            assertFalse(transition.isRunning());
            assertFalse(transition.isActive());
            assertEquals(dispose, transition.isDisposed());
            assertTrue(child.getTransforms().isEmpty());
        });
    }

    /// Does not write a retired transform after an axis callback disposes its owner.
    @Test
    void finishCallbackCanDisposeBeforeNextAxis() {
        runWithoutListenerFailures(() -> {
            Pane child = new Pane();
            M3LayoutTransition transition = new M3LayoutTransition(parent(child));
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));
            transition.start();
            child.setLayoutX(20.0);
            child.setLayoutY(30.0);
            Translate translation = assertInstanceOf(Translate.class, child.getTransforms().get(0));
            translation.xProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 0.0) {
                    transition.dispose();
                    translation.setY(123.0);
                }
            });
            transition.finish();
            assertTrue(transition.isDisposed());
            assertTrue(child.getTransforms().isEmpty());
            assertEquals(123.0, translation.getY());
        });
    }

    /// Preserves a new layout delta created while an earlier finish writes its horizontal channel.
    @Test
    void finishCallbackCanChangeLayout() {
        runWithoutListenerFailures(() -> {
            Pane child = new Pane();
            M3LayoutTransition transition = new M3LayoutTransition(parent(child));
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));
            transition.start();
            child.setLayoutX(20.0);
            child.setLayoutY(30.0);
            Translate translation = assertInstanceOf(Translate.class, child.getTransforms().get(0));
            AtomicBoolean changed = new AtomicBoolean();
            translation.xProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 0.0 && changed.compareAndSet(false, true)) {
                    child.setLayoutY(60.0);
                }
            });
            try {
                transition.finish();
                assertEquals(60.0, child.getLayoutY());
                assertEquals(-60.0, translation.getY());
            } finally {
                transition.stop();
            }
        });
    }

    /// Finishes retained children without skipping one when a callback removes an earlier state.
    @Test
    void finishCallbackCanRemoveEarlierChild() {
        runWithoutListenerFailures(() -> {
            Pane first = new Pane();
            Pane second = new Pane();
            Pane third = new Pane();
            Pane parent = parent(first, second, third);
            M3LayoutTransition transition = new M3LayoutTransition(parent);
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));
            transition.start();
            first.setLayoutX(10.0);
            second.setLayoutX(20.0);
            third.setLayoutX(30.0);
            Translate secondTranslation = assertInstanceOf(Translate.class, second.getTransforms().get(0));
            Translate thirdTranslation = assertInstanceOf(Translate.class, third.getTransforms().get(0));
            secondTranslation.xProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 0.0) {
                    parent.getChildren().remove(first);
                }
            });
            try {
                transition.finish();
                assertTrue(first.getTransforms().isEmpty());
                assertEquals(0.0, thirdTranslation.getX());
                assertFalse(transition.isRunning());
            } finally {
                transition.stop();
            }
        });
    }

    /// Does not apply a pending rotated-axis delta after its first axis callback disposes ownership.
    @Test
    void layoutDeltaCallbackCanDisposeBeforeNextAxis() {
        runWithoutListenerFailures(() -> {
            Pane child = new Pane();
            child.setRotate(45.0);
            M3LayoutTransition transition = new M3LayoutTransition(parent(child));
            transition.start();
            Translate translation = assertInstanceOf(Translate.class, child.getTransforms().get(0));
            translation.xProperty().addListener((observable, oldValue, newValue) -> {
                transition.dispose();
                translation.setY(123.0);
            });
            child.setLayoutX(20.0);
            assertTrue(transition.isDisposed());
            assertTrue(child.getTransforms().isEmpty());
            assertEquals(123.0, translation.getY());
        });
    }

    /// Uses the latest motion specification when a stopped-status callback changes it during retargeting.
    @Test
    void stoppedStatusCanReplaceMotionSpec() {
        runWithoutListenerFailures(() -> {
            Pane child = new Pane();
            M3LayoutTransition transition = new M3LayoutTransition(parent(child));
            transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR));
            transition.start();
            child.setLayoutX(20.0);
            AtomicBoolean changed = new AtomicBoolean();
            transition.statusProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == javafx.animation.Animation.Status.STOPPED && changed.compareAndSet(false, true)) {
                    transition.setMotionSpec(M3MotionSpec.of(Duration.ZERO, M3MotionEasing.LINEAR));
                }
            });
            try {
                transition.setMotionSpec(M3MotionSpec.of(Duration.seconds(4.0), M3MotionEasing.LINEAR));
                assertFalse(transition.isRunning());
                Translate translation = assertInstanceOf(Translate.class, child.getTransforms().get(0));
                assertEquals(0.0, translation.getX());
            } finally {
                transition.stop();
            }
        });
    }

    /// Creates a laid-out parent with stable initial coordinates.
    private static Pane parent(Pane... children) {
        Pane parent = new Pane(children);
        new Scene(parent);
        parent.resize(300.0, 100.0);
        parent.layout();
        return parent;
    }

    /// Reports JavaFX listener exceptions alongside failures propagated to the caller.
    private static void runWithoutListenerFailures(Runnable task) {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            Thread thread = Thread.currentThread();
            @Nullable Thread.UncaughtExceptionHandler previous = thread.getUncaughtExceptionHandler();
            List<Throwable> failures = new ArrayList<>();
            thread.setUncaughtExceptionHandler((ignored, failure) -> failures.add(failure));
            try {
                task.run();
                assertTrue(failures.isEmpty(), failures::toString);
            } finally {
                thread.setUncaughtExceptionHandler(previous);
            }
        }));
    }
}
