// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Verifies deterministic delay behavior for reusable scalar transition channels.
@NotNullByDefault
final class M3DelayedScalarChannelTest {
    /// Verifies a delayed channel holds its initial value before advancing through its scalar specification.
    @Test
    void channelIncludesDelayInDurationAndSampling() {
        M3DelayedScalarChannel channel = new M3DelayedScalarChannel(0.001);
        M3MotionSpec spec = M3MotionSpec.of(Duration.seconds(1.0), M3MotionEasing.LINEAR);

        channel.configure(2.0, 12.0, spec, 0.25, Double.POSITIVE_INFINITY);

        assertEquals(1.25, channel.getDurationSeconds(), 1.0e-9);
        assertEquals(2.0, channel.valueAt(0.0), 0.0);
        assertEquals(2.0, channel.valueAt(0.249), 0.0);
        assertEquals(7.0, channel.valueAt(0.75), 1.0e-9);
        assertEquals(12.0, channel.valueAt(1.25), 1.0e-9);
    }

    /// Verifies reset removes both interpolation history and pending delay.
    @Test
    void resetSettlesChannelWithoutRetainingDelay() {
        M3DelayedScalarChannel channel = new M3DelayedScalarChannel(0.001);
        channel.configure(
                0.0,
                1.0,
                M3MotionSpec.of(Duration.seconds(1.0), M3MotionEasing.LINEAR),
                0.5,
                Double.POSITIVE_INFINITY
        );

        channel.reset(0.75);

        assertEquals(0.0, channel.getDurationSeconds(), 0.0);
        assertEquals(0.75, channel.valueAt(0.0), 0.0);
        assertEquals(0.75, channel.valueAt(Double.POSITIVE_INFINITY), 0.0);
    }

    /// Verifies invalid timing inputs cannot corrupt channel state.
    @Test
    void channelRejectsInvalidTimingInputs() {
        M3DelayedScalarChannel channel = new M3DelayedScalarChannel(0.001);
        M3MotionSpec spec = M3MotionSpec.of(Duration.seconds(1.0), M3MotionEasing.LINEAR);

        assertThrows(IllegalArgumentException.class, () ->
                channel.configure(0.0, 1.0, spec, -0.1, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () ->
                channel.configure(0.0, 1.0, spec, Double.NaN, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () ->
                channel.configure(0.0, 1.0, spec, 0.0, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> channel.valueAt(-0.1));
        assertThrows(IllegalArgumentException.class, () -> channel.valueAt(Double.NaN));
    }
}
