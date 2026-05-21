// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3MotionSpec].
///
/// @param duration the animation duration
/// @param easing the named easing curve
@NotNullByDefault
public record M3MotionSpecImpl(
        Duration duration,
        M3MotionEasing easing
) implements M3MotionSpec {
    /// Creates a motion spec implementation.
    public M3MotionSpecImpl {
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(easing, "easing");
        if (duration.lessThan(Duration.ZERO) || duration.isUnknown() || duration.isIndefinite()) {
            throw new IllegalArgumentException("duration must be a finite non-negative duration");
        }
    }
}
