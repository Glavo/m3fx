// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3DensityImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes an immutable compactness scale for density-sensitive component metrics.
///
/// A scale of `0.0` represents baseline Material Design 3 density. Component token generation applies a density
/// only to selected vertical layout metrics, such as component heights, vertical padding, and vertical item gaps.
/// It does not apply the scale to every geometric token. Icon sizes, outline widths, radii, horizontal widths, and
/// touch-target sizes retain their component-specific values.
///
/// Each scale unit changes an eligible metric by four JavaFX logical pixels. Negative scales compact eligible
/// metrics; positive scales expand them. Supported scales are finite values in the closed interval `[-4.0, 4.0]`.
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

    /// Returns a compacted or expanded vertical layout metric.
    ///
    /// This operation is intended only for a component metric that the component specification permits density to
    /// change, such as a height, vertical padding, or vertical gap. Callers must not use it for a stroke width,
    /// icon size, radius, horizontal dimension, or touch target.
    ///
    /// The result is `max(0.0, baseline + scale() * 4.0)`.
    ///
    /// @param baseline the baseline vertical layout metric to adjust
    /// @return the density-adjusted metric, never less than zero
    /// @throws IllegalArgumentException if `baseline` is not finite
    default double compact(double baseline) {
        if (!Double.isFinite(baseline)) {
            throw new IllegalArgumentException("baseline must be finite");
        }
        return Math.max(0.0, baseline + scale() * 4.0);
    }
}
