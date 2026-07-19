// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 slider size configuration.
///
/// The five sizes select the track height, track shape, handle height, and optional inset-icon metrics published
/// by Material Design 3 Expressive. [#EXTRA_SMALL] is the default configuration and corresponds to the baseline
/// slider geometry. Larger sizes are intended for prominent controls or touch-oriented layouts and do not change
/// the slider's numeric range or adjustment behavior.
///
/// See [Material Design slider specifications](https://m3.material.io/components/sliders/specs).
@NotNullByDefault
public enum M3SliderSize {
    /// Uses the 16-pixel track and 44-pixel handle configuration.
    EXTRA_SMALL("extra-small"),

    /// Uses the 24-pixel track and 44-pixel handle configuration.
    SMALL("small"),

    /// Uses the 40-pixel track and 52-pixel handle configuration.
    MEDIUM("medium"),

    /// Uses the 56-pixel track and 68-pixel handle configuration.
    LARGE("large"),

    /// Uses the 96-pixel track and 108-pixel handle configuration.
    EXTRA_LARGE("extra-large");

    /// The suffix used by the slider size style class.
    private final String cssSuffix;

    /// Creates a slider size.
    ///
    /// @param cssSuffix the suffix used by the slider size style class
    M3SliderSize(String cssSuffix) {
        this.cssSuffix = cssSuffix;
    }

    /// Returns the suffix used by the slider size style class.
    ///
    /// @return the CSS suffix for this size
    String cssSuffix() {
        return cssSuffix;
    }
}
