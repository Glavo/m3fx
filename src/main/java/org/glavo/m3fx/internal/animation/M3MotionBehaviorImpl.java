// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3MotionBehavior].
///
/// @param tooltipShowDelay the tooltip show delay
/// @param tooltipHideDelay the tooltip hide delay
/// @param tooltipShowDuration the plain tooltip visible duration
/// @param richTooltipShowDuration the rich tooltip visible duration
/// @param subMenuHoverOpenDelay the submenu hover open delay
/// @param typeAheadResetDelay the type-ahead search reset delay
/// @param subMenuHoverCloseDelay the submenu hover close delay
/// @param linearProgressIndeterminateCycleDuration the indeterminate linear progress cycle duration
/// @param circularProgressIndeterminateCycleDuration the indeterminate circular progress cycle duration
/// @param loadingIndicatorMorphInterval the loading indicator morph segment duration
/// @param loadingIndicatorGlobalRotationDuration the loading indicator global rotation loop duration
@NotNullByDefault
public record M3MotionBehaviorImpl(
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
) implements M3MotionBehavior {
    /// Creates a motion behavior implementation.
    public M3MotionBehaviorImpl {
        validate(tooltipShowDelay, "tooltipShowDelay");
        validate(tooltipHideDelay, "tooltipHideDelay");
        validate(tooltipShowDuration, "tooltipShowDuration");
        validate(richTooltipShowDuration, "richTooltipShowDuration");
        validate(subMenuHoverOpenDelay, "subMenuHoverOpenDelay");
        validate(typeAheadResetDelay, "typeAheadResetDelay");
        validate(subMenuHoverCloseDelay, "subMenuHoverCloseDelay");
        validate(linearProgressIndeterminateCycleDuration, "linearProgressIndeterminateCycleDuration");
        validate(circularProgressIndeterminateCycleDuration, "circularProgressIndeterminateCycleDuration");
        validate(loadingIndicatorMorphInterval, "loadingIndicatorMorphInterval");
        validate(loadingIndicatorGlobalRotationDuration, "loadingIndicatorGlobalRotationDuration");
    }

    /// Validates one behavior duration.
    private static void validate(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isUnknown() || duration.isIndefinite()) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        if (duration.lessThan(Duration.ZERO)) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
