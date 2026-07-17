// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Interpolator;
import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionSpecImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes one immutable Material Design 3 motion specification.
///
/// A motion specification combines a finite, non-negative duration with a named [M3MotionEasing]. A duration of
/// [Duration#ZERO] is valid and represents a transition that reaches its end value immediately. Specifications
/// are value objects and may be shared between controls and themes.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3MotionSpec permits M3MotionSpecImpl {
    /// Returns the duration of the transition.
    ///
    /// @return a finite, non-negative duration; never `null`
    Duration duration();

    /// Returns the named easing curve.
    ///
    /// @return the named easing curve; never `null`
    M3MotionEasing easing();

    /// Returns the JavaFX interpolator associated with [easing].
    ///
    /// @return the shared interpolator for this specification; never `null`
    default Interpolator interpolator() {
        return easing().interpolator();
    }

    /// Creates an immutable motion specification.
    ///
    /// @param duration the finite, non-negative transition duration; [Duration#ZERO] is permitted
    /// @param easing the easing curve to apply over the duration
    /// @return an immutable motion spec
    /// @throws NullPointerException if `duration` or `easing` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    static M3MotionSpec of(Duration duration, M3MotionEasing easing) {
        return new M3MotionSpecImpl(duration, easing);
    }
}
