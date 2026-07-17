// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3DensityImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes an immutable density scale for layout-sensitive component tokens.
///
/// Density adjusts metrics that are safe to compact or expand, such as heights and padding. A scale of `0.0`
/// represents the baseline Material Design 3 density. Each scale unit adds four JavaFX logical pixels to a metric;
/// negative scales subtract the same amount. Supported scales are finite values in the closed interval
/// `[-4.0, 4.0]`.
///
/// Density is applied when component tokens are derived. Changing the density object used to construct a theme
/// does not dynamically resize an existing token set.
///
/// See [Material Design layout](https://m3.material.io/foundations/layout/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public sealed interface M3Density permits M3DensityImpl {
    /// Returns the density scale.
    ///
    /// @return a finite scale in `[-4.0, 4.0]`, where zero is the baseline density
    double scale();

    /// Returns a density whose scale is `0.0`.
    ///
    /// @return the immutable baseline density
    static M3Density standard() {
        return new M3DensityImpl(0.0);
    }

    /// Creates a density with the specified scale.
    ///
    /// @param scale the density scale where zero is the baseline Material density
    /// @return a density value for the supplied scale
    /// @throws IllegalArgumentException if `scale` is not finite or lies outside `[-4.0, 4.0]`
    static M3Density of(double scale) {
        return new M3DensityImpl(scale);
    }

    /// Applies this density to a baseline metric.
    ///
    /// The result is `max(0.0, value + scale() * 4.0)`. Negative input values are accepted but may therefore be
    /// clamped to zero.
    ///
    /// @param value the baseline size to adjust
    /// @return the density-adjusted metric, never less than zero
    /// @throws IllegalArgumentException if `value` is not finite
    default double apply(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be finite");
        }
        return Math.max(0.0, value + scale() * 4.0);
    }
}
