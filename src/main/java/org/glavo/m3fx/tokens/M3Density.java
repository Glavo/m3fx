// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3DensityImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes the density scale applied to layout-sensitive component tokens.
@NotNullByDefault
public sealed interface M3Density permits M3DensityImpl {
    /// Returns the density scale where zero is the baseline Material density.
    double scale();

    /// Returns the baseline density.
    static M3Density standard() {
        return new M3DensityImpl(0.0);
    }

    /// Creates a density value after validating its supported range.
    static M3Density of(double scale) {
        return new M3DensityImpl(scale);
    }

    /// Applies this density to a baseline size.
    default double apply(double value) {
        return Math.max(0.0, value + scale() * 4.0);
    }
}
