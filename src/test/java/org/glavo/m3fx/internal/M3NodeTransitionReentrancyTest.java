// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/// Verifies cancellation between observable node-channel writes and during configuration.
@NotNullByDefault
final class M3NodeTransitionReentrancyTest {
    /// Keeps any started transition active until it is explicitly stopped.
    private static final M3MotionSpec SPEC = M3MotionSpec.of(Duration.seconds(5.0), M3MotionEasing.LINEAR);

    /// Starts JavaFX before constructing nodes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Leaves later channels untouched when a property listener stops the current update.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void propertyListenerCanStopRemainingWrites(int channel) {
        verifyChannelReplacement(channel, false, false);
    }

    /// Does not evaluate replacement channels using the previous run's elapsed time.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void propertyListenerCanConfigureReplacement(int channel) {
        verifyChannelReplacement(channel, true, false);
    }

    /// Stops a partially rendered pulse when a listener replaces its scalar configuration.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void pulseListenerCanConfigureReplacement(int channel) {
        verifyChannelReplacement(channel, true, true);
    }

    /// Preserves the configuration installed by a stopped-status listener.
    @Test
    void stoppedListenerCanReplaceConfiguration() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            Pane node = new Pane();
            new Scene(node);
            M3NodeTransition transition = new M3NodeTransition(node);
            transition.configure(SPEC, 0.5, 2.0, 3.0, 4.0, 5.0);
            M3Animation.playFromStart(node, transition);
            AtomicBoolean replaced = new AtomicBoolean();
            transition.statusProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Animation.Status.STOPPED && replaced.compareAndSet(false, true)) {
                    transition.configure(SPEC, 0.75, 3.0, 4.0, 5.0, 6.0);
                }
            });
            try {
                transition.configure(SPEC, 0.25, 4.0, 5.0, 6.0, 7.0);
                M3Animation.finish(transition);
                assertValues(node, new double[]{0.75, 3.0, 4.0, 5.0, 6.0});
            } finally {
                transition.stop();
            }
        }));
    }

    /// Executes a callback after one channel changes and verifies the remaining properties and next completion.
    private static void verifyChannelReplacement(int channel, boolean reconfigure, boolean pulse) {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            Pane node = new Pane();
            new Scene(node);
            DoubleProperty @Unmodifiable [] properties = properties(node);
            double @Unmodifiable [] initial = {1.0, 1.0, 1.0, 0.0, 0.0};
            double @Unmodifiable [] originalTargets = {0.5, 2.0, 3.0, 4.0, 5.0};
            M3NodeTransition transition = new M3NodeTransition(node);
            transition.configure(SPEC, 0.5, 2.0, 3.0, 4.0, 5.0);
            AtomicBoolean replaced = new AtomicBoolean();
            AtomicBoolean finished = new AtomicBoolean();
            transition.setOnFinished(event -> finished.set(true));
            properties[channel].addListener((observable, oldValue, newValue) -> {
                if (replaced.compareAndSet(false, true)) {
                    if (reconfigure) {
                        transition.configure(SPEC, 0.75, 3.0, 4.0, 5.0, 6.0);
                    } else {
                        transition.stop();
                    }
                }
            });
            try {
                if (pulse) {
                    M3Animation.playFromStart(node, transition);
                    transition.jumpTo(Duration.seconds(2.5));
                } else {
                    M3Animation.finish(transition);
                }
                for (int index = 0; index < properties.length; index++) {
                    double expected = initial[index];
                    if (index <= channel) {
                        expected += (originalTargets[index] - initial[index]) * (pulse ? 0.5 : 1.0);
                    }
                    assertEquals(expected, properties[index].get(), 1.0e-10);
                }
                assertFalse(finished.get());
                M3Animation.finish(transition);
                assertValues(node, reconfigure ? new double[]{0.75, 3.0, 4.0, 5.0, 6.0} : originalTargets);
            } finally {
                transition.stop();
            }
        }));
    }

    /// Returns the observable channels in the order written by the transition.
    private static DoubleProperty @Unmodifiable [] properties(Pane node) {
        return new DoubleProperty[]{node.opacityProperty(), node.scaleXProperty(), node.scaleYProperty(),
                node.translateXProperty(), node.translateYProperty()};
    }

    /// Verifies the rendered value of every supported channel.
    private static void assertValues(Pane node, double @Unmodifiable [] expected) {
        DoubleProperty @Unmodifiable [] properties = properties(node);
        for (int index = 0; index < properties.length; index++) {
            assertEquals(expected[index], properties[index].get());
        }
    }
}
