// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3TransitionEdge;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Stores one immutable enter or exit effect after public API validation.
///
/// @param kind       the visual channel controlled by the effect
/// @param value      the effect's initial or target scalar value, or zero for a clip effect
/// @param edge       the logical edge used by a slide, or `null` for every other effect
/// @param clipBounds the normalized logical reveal bounds used by a clip effect, or `null` for other effects
/// @param motionSpec the explicit motion specification, or `null` for semantic theme resolution
/// @param delay      the finite, non-negative delay before the effect starts
@NotNullByDefault
public record M3TransitionEffect(
        M3TransitionEffectKind kind,
        double value,
        @Nullable M3TransitionEdge edge,
        @Nullable Rectangle2D clipBounds,
        @Nullable M3MotionSpec motionSpec,
        Duration delay
) {
    /// Creates a validated transition effect.
    ///
    /// @throws NullPointerException     if `kind` or `delay` is `null`, a slide has no edge, or a clip has no
    ///                                  bounds
    /// @throws IllegalArgumentException if the value is invalid for the effect or the delay is not finite and
    ///                                  non-negative
    public M3TransitionEffect {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(delay, "delay");
        if (delay.lessThan(Duration.ZERO) || delay.isIndefinite() || delay.isUnknown()) {
            throw new IllegalArgumentException("delay must be a finite non-negative duration");
        }
        switch (kind) {
            case FADE -> {
                if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
                    throw new IllegalArgumentException("opacity must be finite and in the range 0.0 through 1.0");
                }
                if (edge != null) {
                    throw new IllegalArgumentException("a fade effect must not define an edge");
                }
                if (clipBounds != null) {
                    throw new IllegalArgumentException("a fade effect must not define clip bounds");
                }
            }
            case SCALE -> {
                if (!Double.isFinite(value) || value <= 0.0) {
                    throw new IllegalArgumentException("scale must be finite and greater than zero");
                }
                if (edge != null) {
                    throw new IllegalArgumentException("a scale effect must not define an edge");
                }
                if (clipBounds != null) {
                    throw new IllegalArgumentException("a scale effect must not define clip bounds");
                }
            }
            case SLIDE -> {
                if (!Double.isFinite(value) || value < 0.0) {
                    throw new IllegalArgumentException("slide distance must be finite and non-negative");
                }
                Objects.requireNonNull(edge, "edge");
                if (clipBounds != null) {
                    throw new IllegalArgumentException("a slide effect must not define clip bounds");
                }
            }
            case CLIP -> {
                if (edge != null) {
                    throw new IllegalArgumentException("a clip effect must not define a slide edge");
                }
                Rectangle2D bounds = Objects.requireNonNull(clipBounds, "clipBounds");
                if (!Double.isFinite(bounds.getMinX())
                        || !Double.isFinite(bounds.getMinY())
                        || !Double.isFinite(bounds.getWidth())
                        || !Double.isFinite(bounds.getHeight())
                        || bounds.getMinX() < 0.0
                        || bounds.getMinY() < 0.0
                        || bounds.getMaxX() > 1.0
                        || bounds.getMaxY() > 1.0) {
                    throw new IllegalArgumentException("clip bounds must be finite and contained in the unit square");
                }
            }
        }
    }

    /// Returns a copy using the supplied explicit motion specification.
    ///
    /// @param motionSpec the explicit specification, or `null` for semantic theme resolution
    /// @return the copied effect
    public M3TransitionEffect withMotionSpec(@Nullable M3MotionSpec motionSpec) {
        return new M3TransitionEffect(kind, value, edge, clipBounds, motionSpec, delay);
    }

    /// Returns a copy using the supplied delay.
    ///
    /// @param delay the finite, non-negative delay
    /// @return the copied effect
    /// @throws NullPointerException     if `delay` is `null`
    /// @throws IllegalArgumentException if `delay` is negative, indefinite, or unknown
    public M3TransitionEffect withDelay(Duration delay) {
        return new M3TransitionEffect(kind, value, edge, clipBounds, motionSpec, delay);
    }
}
