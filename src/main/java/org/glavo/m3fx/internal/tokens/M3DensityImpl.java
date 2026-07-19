// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3Density;
import org.jetbrains.annotations.NotNullByDefault;

/// Default immutable implementation of [M3Density].
///
/// @param scale the finite density scale in the inclusive range `[-4.0, 4.0]`, where zero is baseline density
@NotNullByDefault
public record M3DensityImpl(double scale) implements M3Density {
    /// Creates a density value after validating its supported range.
    ///
    /// @throws IllegalArgumentException if `scale` is not finite or is outside `[-4.0, 4.0]`
    public M3DensityImpl {
        if (!Double.isFinite(scale) || scale < -4.0 || scale > 4.0) {
            throw new IllegalArgumentException("Density scale must be finite and between -4.0 and 4.0");
        }
    }
}
