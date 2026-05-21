// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3DensityImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes the density scale applied to layout-sensitive component tokens.
///
/// Density adjusts component metrics that are safe to compact or expand, such as heights, padding, and touch
/// targets. A scale of `0.0` represents the baseline Material Design 3 density, and each step changes
/// applicable dimensions by four device-independent pixels.
///
/// See [Material Design layout](https://m3.material.io/foundations/layout/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public sealed interface M3Density permits M3DensityImpl {
    /// Returns the density scale where zero is the baseline Material density.
    ///
    /// @return the density scale where zero is the baseline Material density
    double scale();

    /// Returns the baseline density.
    ///
    /// @return the baseline density
    static M3Density standard() {
        return new M3DensityImpl(0.0);
    }

    /// Creates a density value after validating its supported range.
    ///
    /// @param scale the density scale where zero is the baseline Material density
    /// @return a density value for the supplied scale
    static M3Density of(double scale) {
        return new M3DensityImpl(scale);
    }

    /// Applies this density to a baseline size.
    ///
    /// @param value the baseline size to adjust
    /// @return the density-adjusted size, never less than zero
    default double apply(double value) {
        return Math.max(0.0, value + scale() * 4.0);
    }
}
