// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Interpolator;
import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionSpecImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes one Material Design 3 motion spec.
@NotNullByDefault
public sealed interface M3MotionSpec permits M3MotionSpecImpl {
    /// Returns the animation duration.
    Duration duration();

    /// Returns the named easing curve.
    M3MotionEasing easing();

    /// Returns the JavaFX interpolator for the easing curve.
    default Interpolator interpolator() {
        return easing().interpolator();
    }

    /// Creates a motion spec.
    static M3MotionSpec create(Duration duration, M3MotionEasing easing) {
        return new M3MotionSpecImpl(duration, easing);
    }
}
