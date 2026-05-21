// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests internal animation helpers.
@NotNullByDefault
final class M3AnimationTest {
    /// Verifies that disabled motion completes timelines synchronously.
    @Test
    void disabledMotionFinishesTimelineImmediately() {
        Pane owner = new Pane();
        M3MotionSettings.setAnimationsEnabled(owner, false);
        DoubleProperty value = new SimpleDoubleProperty(0.0);
        AtomicBoolean keyFrameFinished = new AtomicBoolean(false);
        AtomicBoolean animationFinished = new AtomicBoolean(false);
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(100.0),
                event -> keyFrameFinished.set(true),
                new KeyValue(value, 1.0)
        ));
        timeline.setOnFinished(event -> animationFinished.set(true));

        M3Animation.playFromStart(owner, timeline);

        assertEquals(1.0, value.get(), 0.0001);
        assertTrue(keyFrameFinished.get());
        assertTrue(animationFinished.get());
        assertFalse(timeline.getStatus() == Timeline.Status.RUNNING);
    }
}
