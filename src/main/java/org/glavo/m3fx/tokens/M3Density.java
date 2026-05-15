package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the density scale applied to layout-sensitive component tokens.
///
/// @param scale the density scale where zero is the baseline Material density
@NotNullByDefault
public record M3Density(
        double scale
) {
    /// Creates a density value after validating its supported range.
    public M3Density {
        if (scale < -4.0 || scale > 4.0) {
            throw new IllegalArgumentException("Density scale must be between -4.0 and 4.0");
        }
    }

    /// Returns the baseline density.
    public static M3Density standard() {
        return new M3Density(0.0);
    }

    /// Applies this density to a baseline size.
    public double apply(double value) {
        return Math.max(0.0, value + scale * 4.0);
    }
}
