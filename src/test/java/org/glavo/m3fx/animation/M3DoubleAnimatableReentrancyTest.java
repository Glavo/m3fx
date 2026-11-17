// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Scrim;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Verifies that scalar animation callbacks cannot restore superseded requests.
@NotNullByDefault
final class M3DoubleAnimatableReentrancyTest {
    /// Keeps playback active until the test explicitly completes or stops it.
    private static final M3MotionSpec SPEC = M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR);

    /// Starts JavaFX before constructing animation owners.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves a snap requested while an earlier operation publishes its target.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void targetListenerCanSnap(boolean animate) {
        withAnimatable(animatable -> {
            animatable.targetValueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 10.0) {
                    animatable.snapTo(20.0);
                }
            });
            request(animatable, animate);
            assertEquals(20.0, animatable.getTargetValue());
            assertEquals(20.0, animatable.getValue());
            assertEquals(Animation.Status.STOPPED, animatable.getStatus());
        });
    }

    /// Starts a replacement run from the value preceding the superseded target publication.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void targetListenerCanRetarget(boolean animate) {
        withAnimatable(animatable -> {
            animatable.targetValueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 10.0) {
                    animatable.animateTo(20.0, SPEC);
                }
            });
            request(animatable, animate);
            assertEquals(20.0, animatable.getTargetValue());
            assertEquals(0.0, animatable.getValue());
            assertEquals(Animation.Status.RUNNING, animatable.getStatus());
            animatable.finish();
            assertEquals(20.0, animatable.getValue());
        });
    }

    /// Cancels a configured run before its target notification returns to the playback call.
    @Test
    void targetListenerCanStopBeforePlayback() {
        withAnimatable(animatable -> {
            animatable.targetValueProperty().addListener(observable -> animatable.stop());
            animatable.animateTo(10.0, SPEC);
            assertEquals(10.0, animatable.getTargetValue());
            assertEquals(0.0, animatable.getValue());
            assertEquals(Animation.Status.STOPPED, animatable.getStatus());
        });
    }

    /// Completes a configured run without allowing its original caller to restart playback.
    @Test
    void targetListenerCanFinishBeforePlayback() {
        withAnimatable(animatable -> {
            animatable.targetValueProperty().addListener(observable -> animatable.finish());
            animatable.animateTo(10.0, SPEC);
            assertEquals(10.0, animatable.getValue());
            assertEquals(Animation.Status.STOPPED, animatable.getStatus());
        });
    }

    /// Preserves a replacement run started by the stopped-status notification of an earlier request.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stoppedListenerCanRetarget(boolean finish) {
        withAnimatable(animatable -> {
            animatable.animateTo(5.0, SPEC);
            AtomicBoolean replaced = new AtomicBoolean();
            ChangeListener<Animation.Status> listener = (observable, oldValue, newValue) -> {
                if (newValue == Animation.Status.STOPPED && replaced.compareAndSet(false, true)) {
                    animatable.animateTo(20.0, SPEC);
                }
            };
            animatable.statusProperty().addListener(listener);
            try {
                if (finish) {
                    animatable.finish();
                } else {
                    animatable.animateTo(10.0, SPEC);
                }
                assertEquals(20.0, animatable.getTargetValue());
                assertEquals(0.0, animatable.getValue());
                assertEquals(Animation.Status.RUNNING, animatable.getStatus());
                animatable.finish();
                assertEquals(20.0, animatable.getValue());
            } finally {
                animatable.statusProperty().removeListener(listener);
            }
        });
    }

    /// Does not cancel a valid request when a callback attempts an invalid replacement.
    @Test
    void rejectedReplacementKeepsPendingRun() {
        withAnimatable(animatable -> {
            animatable.targetValueProperty().addListener(observable ->
                    assertThrows(IllegalArgumentException.class, () -> animatable.animateTo(Double.NaN, SPEC)));
            animatable.animateTo(10.0, SPEC);
            assertEquals(10.0, animatable.getTargetValue());
            assertEquals(Animation.Status.RUNNING, animatable.getStatus());
            animatable.finish();
            assertEquals(10.0, animatable.getValue());
        });
    }

    /// Keeps a stopped-status callback authoritative when motion policy synchronously ends a run.
    @Test
    void reducedMotionFinishPreservesCallbackSnap() {
        withAnimatable(animatable -> {
            animatable.animateTo(10.0, SPEC);
            animatable.statusProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Animation.Status.STOPPED) {
                    animatable.snapTo(20.0);
                }
            });
            ((M3Scrim) animatable.getOwner()).setAnimationsEnabled(false);
            assertEquals(20.0, animatable.getTargetValue());
            assertEquals(20.0, animatable.getValue());
            assertEquals(Animation.Status.STOPPED, animatable.getStatus());
        });
    }

    /// Starts either kind of target-changing operation.
    private static void request(M3DoubleAnimatable animatable, boolean animate) {
        if (animate) {
            animatable.animateTo(10.0, SPEC);
        } else {
            animatable.snapTo(10.0);
        }
    }

    /// Exercises synchronous callbacks with playback enabled and stops any surviving run.
    private static void withAnimatable(Consumer<M3DoubleAnimatable> task) {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            M3Scrim owner = new M3Scrim();
            new Scene(owner);
            M3DoubleAnimatable animatable = new M3DoubleAnimatable(owner, new SimpleDoubleProperty(), 0.01);
            try {
                task.accept(animatable);
            } finally {
                animatable.stop();
            }
        }));
    }
}
