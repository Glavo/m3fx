// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.monetfx.ColorSpecVersion;
import org.glavo.monetfx.ColorStyle;
import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a complete Material Design 3 token profile used by M3FX.
///
/// A profile selects both MonetFX color-generation settings and M3FX defaults for typography, shape, motion, and
/// component tokens. Selecting a profile does not by itself change an installed theme; it is consumed when a theme
/// or token-set builder derives its defaults.
///
/// See [Material Design](https://m3.material.io/) and
/// [Material color](https://m3.material.io/styles/color/overview).
@NotNullByDefault
public enum M3Profile {
    /// Uses the 2021 Material Design 3 color specification with the tonal-spot color style and baseline tokens.
    BASELINE_2021(ColorSpecVersion.SPEC_2021, ColorStyle.TONAL_SPOT),

    /// Uses the 2025 color specification with the expressive color style and Expressive component tokens.
    EXPRESSIVE_2025(ColorSpecVersion.SPEC_2025, ColorStyle.EXPRESSIVE);

    /// The MonetFX color specification version used by this profile.
    private final ColorSpecVersion colorSpecVersion;

    /// The MonetFX color style used by this profile.
    private final ColorStyle colorStyle;

    /// Creates a profile with the MonetFX color settings that match it.
    M3Profile(ColorSpecVersion colorSpecVersion, ColorStyle colorStyle) {
        this.colorSpecVersion = colorSpecVersion;
        this.colorStyle = colorStyle;
    }

    /// Returns the MonetFX color specification version for this profile.
    ///
    /// @return the MonetFX color specification version for this profile
    public ColorSpecVersion colorSpecVersion() {
        return colorSpecVersion;
    }

    /// Returns the MonetFX color style for this profile.
    ///
    /// @return the MonetFX color style for this profile
    public ColorStyle colorStyle() {
        return colorStyle;
    }
}
