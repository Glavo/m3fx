// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionBehaviorImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds an immutable [M3MotionBehavior] by replacing individual interaction timings.
///
/// A builder is initialized from a complete behavior so applications can replace only the timings that differ
/// from the selected profile. Every duration must be non-null, finite, and non-negative; [Duration#UNKNOWN] and
/// [Duration#INDEFINITE] are rejected. Replacement methods validate before changing the builder and return this
/// builder for method chaining.
///
/// [build] creates an independent immutable snapshot. Later builder changes do not affect previously built
/// values. A builder may be reused but is not thread-safe.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3MotionBehaviorBuilder {
    /// The delay before a tooltip opens.
    private Duration tooltipShowDelay;

    /// The delay before a tooltip closes.
    private Duration tooltipHideDelay;

    /// The visible duration for a plain tooltip.
    private Duration tooltipShowDuration;

    /// The visible duration for a rich tooltip.
    private Duration richTooltipShowDuration;

    /// The snackbar display duration.
    private Duration snackbarDisplayDuration;

    /// The delay before hover opens a submenu.
    private Duration subMenuHoverOpenDelay;

    /// The idle duration before type-ahead search resets.
    private Duration typeAheadResetDelay;

    /// The delay before hover exit closes a submenu.
    private Duration subMenuHoverCloseDelay;

    /// The indeterminate linear progress cycle duration.
    private Duration linearProgressIndeterminateCycleDuration;

    /// The indeterminate circular progress cycle duration.
    private Duration circularProgressIndeterminateCycleDuration;

    /// The loading indicator morph segment duration.
    private Duration loadingIndicatorMorphInterval;

    /// The loading indicator global rotation duration.
    private Duration loadingIndicatorGlobalRotationDuration;

    /// Creates a builder initialized from an existing behavior.
    ///
    /// @param behavior the behavior to copy
    M3MotionBehaviorBuilder(M3MotionBehavior behavior) {
        M3MotionBehavior source = Objects.requireNonNull(behavior, "behavior");
        tooltipShowDelay = source.tooltipShowDelay();
        tooltipHideDelay = source.tooltipHideDelay();
        tooltipShowDuration = source.tooltipShowDuration();
        richTooltipShowDuration = source.richTooltipShowDuration();
        snackbarDisplayDuration = source.snackbarDisplayDuration();
        subMenuHoverOpenDelay = source.subMenuHoverOpenDelay();
        typeAheadResetDelay = source.typeAheadResetDelay();
        subMenuHoverCloseDelay = source.subMenuHoverCloseDelay();
        linearProgressIndeterminateCycleDuration = source.linearProgressIndeterminateCycleDuration();
        circularProgressIndeterminateCycleDuration = source.circularProgressIndeterminateCycleDuration();
        loadingIndicatorMorphInterval = source.loadingIndicatorMorphInterval();
        loadingIndicatorGlobalRotationDuration = source.loadingIndicatorGlobalRotationDuration();
    }

    /// Replaces the delay before a tooltip opens.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder tooltipShowDelay(Duration duration) {
        tooltipShowDelay = validDuration(duration, "tooltipShowDelay");
        return this;
    }

    /// Replaces the delay before a tooltip closes.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder tooltipHideDelay(Duration duration) {
        tooltipHideDelay = validDuration(duration, "tooltipHideDelay");
        return this;
    }

    /// Replaces the visible duration for a plain tooltip.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder tooltipShowDuration(Duration duration) {
        tooltipShowDuration = validDuration(duration, "tooltipShowDuration");
        return this;
    }

    /// Replaces the visible duration for a rich tooltip.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder richTooltipShowDuration(Duration duration) {
        richTooltipShowDuration = validDuration(duration, "richTooltipShowDuration");
        return this;
    }

    /// Replaces the snackbar display duration.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder snackbarDisplayDuration(Duration duration) {
        snackbarDisplayDuration = validDuration(duration, "snackbarDisplayDuration");
        return this;
    }

    /// Replaces the delay before hover opens a submenu.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder subMenuHoverOpenDelay(Duration duration) {
        subMenuHoverOpenDelay = validDuration(duration, "subMenuHoverOpenDelay");
        return this;
    }

    /// Replaces the idle duration before type-ahead search resets.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder typeAheadResetDelay(Duration duration) {
        typeAheadResetDelay = validDuration(duration, "typeAheadResetDelay");
        return this;
    }

    /// Replaces the delay before hover exit closes a submenu.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder subMenuHoverCloseDelay(Duration duration) {
        subMenuHoverCloseDelay = validDuration(duration, "subMenuHoverCloseDelay");
        return this;
    }

    /// Replaces the indeterminate linear progress cycle duration.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder linearProgressIndeterminateCycleDuration(Duration duration) {
        linearProgressIndeterminateCycleDuration = validDuration(
                duration,
                "linearProgressIndeterminateCycleDuration"
        );
        return this;
    }

    /// Replaces the indeterminate circular progress cycle duration.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder circularProgressIndeterminateCycleDuration(Duration duration) {
        circularProgressIndeterminateCycleDuration = validDuration(
                duration,
                "circularProgressIndeterminateCycleDuration"
        );
        return this;
    }

    /// Replaces the loading indicator morph segment duration.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder loadingIndicatorMorphInterval(Duration duration) {
        loadingIndicatorMorphInterval = validDuration(duration, "loadingIndicatorMorphInterval");
        return this;
    }

    /// Replaces the loading indicator global rotation duration.
    ///
    /// @param duration the replacement duration
    /// @return this builder
    /// @throws NullPointerException if `duration` is `null`
    /// @throws IllegalArgumentException if `duration` is negative, indefinite, or unknown
    public M3MotionBehaviorBuilder loadingIndicatorGlobalRotationDuration(Duration duration) {
        loadingIndicatorGlobalRotationDuration = validDuration(
                duration,
                "loadingIndicatorGlobalRotationDuration"
        );
        return this;
    }

    /// Creates an immutable snapshot of the current timings.
    ///
    /// @return a new immutable motion behavior; never `null`
    public M3MotionBehavior build() {
        return new M3MotionBehaviorImpl(
                tooltipShowDelay,
                tooltipHideDelay,
                tooltipShowDuration,
                richTooltipShowDuration,
                snackbarDisplayDuration,
                subMenuHoverOpenDelay,
                typeAheadResetDelay,
                subMenuHoverCloseDelay,
                linearProgressIndeterminateCycleDuration,
                circularProgressIndeterminateCycleDuration,
                loadingIndicatorMorphInterval,
                loadingIndicatorGlobalRotationDuration
        );
    }

    /// Validates a builder duration eagerly.
    private static Duration validDuration(Duration duration, String name) {
        Duration value = Objects.requireNonNull(duration, name);
        if (value.isUnknown() || value.isIndefinite() || value.lessThan(Duration.ZERO)) {
            throw new IllegalArgumentException(name + " must be a finite non-negative duration");
        }
        return value;
    }
}
