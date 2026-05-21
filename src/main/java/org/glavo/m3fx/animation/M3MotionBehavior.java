// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionBehaviorImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Describes motion-adjacent interaction timings that are not animation specs.
@NotNullByDefault
public sealed interface M3MotionBehavior permits M3MotionBehaviorImpl {
    /// Returns the delay before a tooltip opens after pointer entry or keyboard focus.
    Duration tooltipShowDelay();

    /// Returns the delay before a tooltip closes after pointer exit or keyboard focus loss.
    Duration tooltipHideDelay();

    /// Returns the default visible duration for plain tooltips.
    Duration tooltipShowDuration();

    /// Returns the default visible duration for rich tooltips.
    Duration richTooltipShowDuration();

    /// Returns the delay before pointer hover opens a submenu.
    Duration subMenuHoverOpenDelay();

    /// Returns the delay before pointer exit closes a submenu.
    Duration subMenuHoverCloseDelay();

    /// Returns the cycle duration for indeterminate linear progress.
    Duration linearProgressIndeterminateCycleDuration();

    /// Returns the cycle duration for indeterminate circular progress.
    Duration circularProgressIndeterminateCycleDuration();

    /// Creates finite non-negative motion behavior timings.
    static M3MotionBehavior create(
            Duration tooltipShowDelay,
            Duration tooltipHideDelay,
            Duration tooltipShowDuration,
            Duration richTooltipShowDuration,
            Duration subMenuHoverOpenDelay,
            Duration subMenuHoverCloseDelay,
            Duration linearProgressIndeterminateCycleDuration,
            Duration circularProgressIndeterminateCycleDuration
    ) {
        return new M3MotionBehaviorImpl(
                Objects.requireNonNull(tooltipShowDelay, "tooltipShowDelay"),
                Objects.requireNonNull(tooltipHideDelay, "tooltipHideDelay"),
                Objects.requireNonNull(tooltipShowDuration, "tooltipShowDuration"),
                Objects.requireNonNull(richTooltipShowDuration, "richTooltipShowDuration"),
                Objects.requireNonNull(subMenuHoverOpenDelay, "subMenuHoverOpenDelay"),
                Objects.requireNonNull(subMenuHoverCloseDelay, "subMenuHoverCloseDelay"),
                Objects.requireNonNull(
                        linearProgressIndeterminateCycleDuration,
                        "linearProgressIndeterminateCycleDuration"
                ),
                Objects.requireNonNull(
                        circularProgressIndeterminateCycleDuration,
                        "circularProgressIndeterminateCycleDuration"
                )
        );
    }

    /// Returns the standard M3FX interaction timings.
    static M3MotionBehavior standard() {
        return create(
                Duration.millis(500.0),
                Duration.ZERO,
                Duration.seconds(5.0),
                Duration.seconds(10.0),
                M3Motion.SHORT4,
                M3Motion.SHORT4,
                Duration.millis(1400.0),
                Duration.millis(1332.0)
        );
    }

    /// Returns the expressive M3FX interaction timings.
    static M3MotionBehavior expressive() {
        return create(
                Duration.millis(500.0),
                Duration.ZERO,
                Duration.seconds(5.0),
                Duration.seconds(10.0),
                M3Motion.SHORT3,
                M3Motion.SHORT3,
                Duration.millis(1400.0),
                Duration.millis(1332.0)
        );
    }
}
