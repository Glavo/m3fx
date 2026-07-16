// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionBehaviorImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes motion-adjacent interaction timings that are not animation specs.
///
/// Behavior timings cover delays and cycle durations that are part of the interaction model but are not direct
/// property animations, such as tooltip show delays, submenu hover delays, and indeterminate progress cycles.
/// M3FX resolves these values from the active theme's motion tokens. Install a local theme on a subtree when one
/// feature area requires different interaction timings.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3MotionBehavior permits M3MotionBehaviorImpl {
    /// Returns the delay before a tooltip opens after pointer entry or keyboard focus.
    ///
    /// @return the delay before a tooltip opens
    Duration tooltipShowDelay();

    /// Returns the delay before a tooltip closes after pointer exit or keyboard focus loss.
    ///
    /// @return the delay before a tooltip closes
    Duration tooltipHideDelay();

    /// Returns the default visible duration for plain tooltips.
    ///
    /// @return the visible duration for plain tooltips
    Duration tooltipShowDuration();

    /// Returns the default visible duration for rich tooltips.
    ///
    /// @return the visible duration for rich tooltips
    Duration richTooltipShowDuration();

    /// Returns the default display duration before a snackbar automatically dismisses.
    ///
    /// @return the default snackbar display duration
    Duration snackbarDisplayDuration();

    /// Returns the delay before pointer hover opens a submenu.
    ///
    /// @return the delay before pointer hover opens a submenu
    Duration subMenuHoverOpenDelay();

    /// Returns the idle delay after which type-ahead keyboard search starts a new prefix.
    ///
    /// @return the idle delay after which type-ahead search resets
    Duration typeAheadResetDelay();

    /// Returns the delay before pointer exit closes a submenu.
    ///
    /// @return the delay before pointer exit closes a submenu
    Duration subMenuHoverCloseDelay();

    /// Returns the cycle duration for indeterminate linear progress.
    ///
    /// @return the cycle duration for indeterminate linear progress
    Duration linearProgressIndeterminateCycleDuration();

    /// Returns the cycle duration for indeterminate circular progress.
    ///
    /// @return the cycle duration for indeterminate circular progress
    Duration circularProgressIndeterminateCycleDuration();

    /// Returns the duration of one loading indicator shape morph segment.
    ///
    /// @return the duration of one loading indicator shape morph segment
    Duration loadingIndicatorMorphInterval();

    /// Returns the duration of one loading indicator global rotation loop.
    ///
    /// @return the duration of one loading indicator global rotation loop
    Duration loadingIndicatorGlobalRotationDuration();

    /// Creates a builder initialized with the standard interaction timings.
    ///
    /// @return a mutable motion behavior builder
    static M3MotionBehaviorBuilder builder() {
        return new M3MotionBehaviorBuilder(standard());
    }

    /// Creates a builder initialized from an existing behavior.
    ///
    /// @param behavior the behavior to copy
    /// @return a mutable motion behavior builder
    static M3MotionBehaviorBuilder builder(M3MotionBehavior behavior) {
        return new M3MotionBehaviorBuilder(behavior);
    }

    /// Returns the standard M3FX interaction timings.
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

    /// Returns the expressive M3FX interaction timings.
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
