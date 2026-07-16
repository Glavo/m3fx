// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3Density;
import org.jetbrains.annotations.NotNullByDefault;

/// Default immutable implementation of [M3Density].
///
/// @param scale the density scale where zero is the baseline Material density
@NotNullByDefault
public record M3DensityImpl(double scale) implements M3Density {
    /// Creates a density value after validating its supported range.
    public M3DensityImpl {
        if (!Double.isFinite(scale) || scale < -4.0 || scale > 4.0) {
            throw new IllegalArgumentException("Density scale must be finite and between -4.0 and 4.0");
        }
    }
}
