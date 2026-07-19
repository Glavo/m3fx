// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Interpolator;
import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionSpecImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Describes one immutable Material Design 3 motion specification.
///
/// A motion specification describes either a duration-based interpolation or a physical spring. Every
/// specification also carries a finite duration and named easing that form its deterministic fallback on animation
/// primitives that cannot preserve spring velocity. A duration of [Duration#ZERO] reaches the target immediately.
/// Specifications are immutable value objects and may be shared between controls and themes.
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

    /// Returns the physical spring parameters, when this is a spring specification.
    ///
    /// A `null` result identifies a duration-based specification. Consumers capable of spring animation should use
    /// the returned parameters and treat [#duration()] as the finite settling horizon. Other consumers should use
    /// [#interpolator()] over that duration.
    ///
    /// @return the spring parameters, or `null` for a duration-based specification
    @Nullable M3SpringParameters springParameters();

    /// Returns the JavaFX interpolator associated with [easing].
    ///
    /// For a spring specification, this is the non-interruptible fallback curve rather than the physical spring.
    ///
    /// @return the shared fallback interpolator for this specification; never `null`
    default Interpolator interpolator() {
        return easing().interpolator();
    }

    /// Creates an immutable motion specification.
    ///
    /// @param duration the finite, non-negative transition duration; [Duration#ZERO] is permitted
    /// @param easing   the easing curve to apply over the duration
    /// @return an immutable motion spec
    /// @throws NullPointerException     if `duration` or `easing` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    static M3MotionSpec of(Duration duration, M3MotionEasing easing) {
        return new M3MotionSpecImpl(duration, easing, null);
    }

    /// Creates an immutable spring motion specification.
    ///
    /// Spring-capable animation primitives use `springParameters` and preserve velocity when a running transition is
    /// retargeted. `settlingDuration` defines the finite point at which the target is applied exactly. `fallbackEasing`
    /// approximates the same motion for primitives that only support duration-based interpolation.
    ///
    /// @param springParameters the physical spring parameters
    /// @param settlingDuration the finite, non-negative settling horizon
    /// @param fallbackEasing   the named fallback curve
    /// @return an immutable spring motion specification
    /// @throws NullPointerException     if any argument is `null`
    /// @throws IllegalArgumentException if `settlingDuration` is negative, indefinite, or unknown
    static M3MotionSpec spring(
            M3SpringParameters springParameters,
            Duration settlingDuration,
            M3MotionEasing fallbackEasing
    ) {
        return new M3MotionSpecImpl(
                settlingDuration,
                fallbackEasing,
                Objects.requireNonNull(springParameters, "springParameters")
        );
    }
}
