// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionBehaviorImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes timing values that coordinate motion-related control behavior.
///
/// These values are delays, visibility periods, and repeating cycle durations rather than finite property
/// transition specifications. Every returned duration is finite and non-negative. A zero delay takes effect
/// without waiting; a zero visibility period or cycle duration remains a valid token value, although the consuming
/// control determines how such a value affects its behavior.
///
/// Behavior values are immutable and are normally obtained from the active theme's
/// [M3MotionTokens][org.glavo.m3fx.tokens.M3MotionTokens]. [standard] and [expressive] provide the built-in
/// profiles, while [builder][#builder()] creates a mutable copy for application-specific timing changes.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3MotionBehavior permits M3MotionBehaviorImpl {
    /// Returns the delay before a tooltip opens after pointer entry or keyboard focus.
    ///
    /// @return the finite, non-negative delay before a tooltip opens
    Duration tooltipShowDelay();

    /// Returns the delay before a tooltip closes after pointer exit or keyboard focus loss.
    ///
    /// @return the finite, non-negative delay before a tooltip closes
    Duration tooltipHideDelay();

    /// Returns the default visible duration for plain tooltips.
    ///
    /// @return the finite, non-negative visible duration for plain tooltips
    Duration tooltipShowDuration();

    /// Returns the default visible duration for rich tooltips.
    ///
    /// @return the finite, non-negative visible duration for rich tooltips
    Duration richTooltipShowDuration();

    /// Returns the default display duration before a snackbar automatically dismisses.
    ///
    /// @return the finite, non-negative default snackbar display duration
    Duration snackbarDisplayDuration();

    /// Returns the delay before pointer hover opens a submenu.
    ///
    /// @return the finite, non-negative delay before pointer hover opens a submenu
    Duration subMenuHoverOpenDelay();

    /// Returns the idle delay after which type-ahead keyboard search starts a new prefix.
    ///
    /// @return the finite, non-negative idle delay after which type-ahead search resets
    Duration typeAheadResetDelay();

    /// Returns the delay before pointer exit closes a submenu.
    ///
    /// @return the finite, non-negative delay before pointer exit closes a submenu
    Duration subMenuHoverCloseDelay();

    /// Returns the cycle duration for indeterminate linear progress.
    ///
    /// @return the finite, non-negative cycle duration for indeterminate linear progress
    Duration linearProgressIndeterminateCycleDuration();

    /// Returns the cycle duration for indeterminate circular progress.
    ///
    /// @return the finite, non-negative cycle duration for indeterminate circular progress
    Duration circularProgressIndeterminateCycleDuration();

    /// Returns the duration of one loading indicator shape morph segment.
    ///
    /// @return the finite, non-negative duration of one loading indicator shape morph segment
    Duration loadingIndicatorMorphInterval();

    /// Returns the duration of one loading indicator global rotation loop.
    ///
    /// @return the finite, non-negative duration of one loading indicator global rotation loop
    Duration loadingIndicatorGlobalRotationDuration();

    /// Creates a builder initialized with all timings from [standard].
    ///
    /// @return a mutable motion behavior builder
    static M3MotionBehaviorBuilder builder() {
        return new M3MotionBehaviorBuilder(standard());
    }

    /// Creates a builder initialized with all timings from an existing behavior.
    ///
    /// @param behavior the behavior to copy
    /// @return a mutable motion behavior builder
    /// @throws NullPointerException if `behavior` is `null`
    static M3MotionBehaviorBuilder builder(M3MotionBehavior behavior) {
        return new M3MotionBehaviorBuilder(behavior);
    }

    /// Returns a complete set of Standard interaction timings.
    ///
    /// @return the baseline behavior timings
    static M3MotionBehavior standard() {
        return new M3MotionBehaviorImpl(
                Duration.millis(500.0),
                Duration.ZERO,
                Duration.seconds(5.0),
                Duration.seconds(10.0),
                Duration.seconds(4.0),
                M3Motion.SHORT4,
                Duration.millis(1000.0),
                M3Motion.SHORT4,
                Duration.millis(1750.0),
                Duration.millis(6000.0),
                Duration.millis(650.0),
                Duration.millis(4666.0)
        );
    }

    /// Returns a complete set of Expressive interaction timings.
    ///
    /// @return the expressive behavior timings
    static M3MotionBehavior expressive() {
        return new M3MotionBehaviorImpl(
                Duration.millis(500.0),
                Duration.ZERO,
                Duration.seconds(5.0),
                Duration.seconds(10.0),
                Duration.seconds(4.0),
                M3Motion.SHORT3,
                Duration.millis(900.0),
                M3Motion.SHORT3,
                Duration.millis(1750.0),
                Duration.millis(6000.0),
                Duration.millis(650.0),
                Duration.millis(4666.0)
        );
    }
}
