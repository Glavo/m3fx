// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Interpolator;
import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionSpecImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes one Material Design 3 motion spec.
///
/// A motion spec combines a finite duration with a named [M3MotionEasing]. It is the smallest reusable animation
/// token consumed by M3FX controls for state transitions, popup motion, smooth scrolling, and progress
/// indicators.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3MotionSpec permits M3MotionSpecImpl {
    /// Returns the animation duration.
    ///
    /// @return the animation duration
    Duration duration();

    /// Returns the named easing curve.
    ///
    /// @return the named easing curve
    M3MotionEasing easing();

    /// Returns the JavaFX interpolator for the easing curve.
    ///
    /// @return the JavaFX interpolator for the easing curve
    default Interpolator interpolator() {
        return easing().interpolator();
    }

    /// Creates a motion spec.
    ///
    /// @param duration the animation duration
    /// @param easing the named easing curve
    /// @return an immutable motion spec
    /// @throws NullPointerException if `duration` or `easing` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    static M3MotionSpec of(Duration duration, M3MotionEasing easing) {
        return new M3MotionSpecImpl(duration, easing);
    }
}
