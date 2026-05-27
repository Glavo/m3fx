// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionBehaviorImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Describes motion-adjacent interaction timings that are not animation specs.
///
/// Behavior timings cover delays and cycle durations that are part of the interaction model but are not direct
/// property animations, such as tooltip show delays, submenu hover delays, and indeterminate progress cycles.
/// M3FX resolves these values from [M3MotionSettings] so applications can switch motion behavior globally or for
/// a scene graph subtree.
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

    /// Creates finite non-negative motion behavior timings.
    ///
    /// @param tooltipShowDelay the delay before a tooltip opens
    /// @param tooltipHideDelay the delay before a tooltip closes
    /// @param tooltipShowDuration the visible duration for plain tooltips
    /// @param richTooltipShowDuration the visible duration for rich tooltips
    /// @param subMenuHoverOpenDelay the delay before hover opens a submenu
    /// @param typeAheadResetDelay the idle delay after which type-ahead search resets
    /// @param subMenuHoverCloseDelay the delay before hover exit closes a submenu
    /// @param linearProgressIndeterminateCycleDuration the linear progress indeterminate cycle duration
    /// @param circularProgressIndeterminateCycleDuration the circular progress indeterminate cycle duration
    /// @param loadingIndicatorMorphInterval the loading indicator morph segment duration
    /// @param loadingIndicatorGlobalRotationDuration the loading indicator global rotation loop duration
    /// @return immutable motion behavior timings
    static M3MotionBehavior create(
            Duration tooltipShowDelay,
            Duration tooltipHideDelay,
            Duration tooltipShowDuration,
            Duration richTooltipShowDuration,
            Duration subMenuHoverOpenDelay,
            Duration typeAheadResetDelay,
            Duration subMenuHoverCloseDelay,
            Duration linearProgressIndeterminateCycleDuration,
            Duration circularProgressIndeterminateCycleDuration,
            Duration loadingIndicatorMorphInterval,
            Duration loadingIndicatorGlobalRotationDuration
    ) {
        return new M3MotionBehaviorImpl(
                Objects.requireNonNull(tooltipShowDelay, "tooltipShowDelay"),
                Objects.requireNonNull(tooltipHideDelay, "tooltipHideDelay"),
                Objects.requireNonNull(tooltipShowDuration, "tooltipShowDuration"),
                Objects.requireNonNull(richTooltipShowDuration, "richTooltipShowDuration"),
                Objects.requireNonNull(subMenuHoverOpenDelay, "subMenuHoverOpenDelay"),
                Objects.requireNonNull(typeAheadResetDelay, "typeAheadResetDelay"),
                Objects.requireNonNull(subMenuHoverCloseDelay, "subMenuHoverCloseDelay"),
                Objects.requireNonNull(
                        linearProgressIndeterminateCycleDuration,
                        "linearProgressIndeterminateCycleDuration"
                ),
                Objects.requireNonNull(
                        circularProgressIndeterminateCycleDuration,
                        "circularProgressIndeterminateCycleDuration"
                ),
                Objects.requireNonNull(
                        loadingIndicatorMorphInterval,
                        "loadingIndicatorMorphInterval"
                ),
                Objects.requireNonNull(
                        loadingIndicatorGlobalRotationDuration,
                        "loadingIndicatorGlobalRotationDuration"
                )
        );
    }

    /// Returns the standard M3FX interaction timings.
    ///
    /// @return the baseline behavior timings
    static M3MotionBehavior standard() {
        return create(
                Duration.millis(500.0),
                Duration.ZERO,
                Duration.seconds(5.0),
                Duration.seconds(10.0),
                M3Motion.SHORT4,
                Duration.millis(1000.0),
                M3Motion.SHORT4,
                Duration.millis(1400.0),
                Duration.millis(1332.0),
                Duration.millis(650.0),
                Duration.millis(4666.0)
        );
    }

    /// Returns the expressive M3FX interaction timings.
    ///
    /// @return the expressive behavior timings
    static M3MotionBehavior expressive() {
        return create(
                Duration.millis(500.0),
                Duration.ZERO,
                Duration.seconds(5.0),
                Duration.seconds(10.0),
                M3Motion.SHORT3,
                Duration.millis(900.0),
                M3Motion.SHORT3,
                Duration.millis(1400.0),
                Duration.millis(1332.0),
                Duration.millis(650.0),
                Duration.millis(4666.0)
        );
    }
}
