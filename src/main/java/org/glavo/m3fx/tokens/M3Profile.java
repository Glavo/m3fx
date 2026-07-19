// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.monetfx.ColorSpecVersion;
import org.glavo.monetfx.ColorStyle;
import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a complete Material Design 3 token profile used by M3FX.
///
/// A profile selects MonetFX color-generation settings and the default M3FX typography, shape, motion, state, and
/// component token families. It is a preset identity, not a runtime capability switch: a token-set builder may
/// replace any derived group, and generated control rules honor those explicit values regardless of the retained
/// profile. Selecting a profile does not by itself change an installed theme.
///
/// A [ColorScheme][org.glavo.monetfx.ColorScheme] supplied to
/// [M3Theme.fromColorScheme][org.glavo.m3fx.theme.M3Theme#fromColorScheme(M3Profile,org.glavo.monetfx.ColorScheme)]
/// is retained without regeneration. Applications may therefore choose component defaults and color generation
/// independently when constructing an explicit theme.
///
/// See [Material Design](https://m3.material.io/) and
/// [Material color](https://m3.material.io/styles/color/overview).
@NotNullByDefault
public enum M3Profile {
    /// Selects the 2021 Material Design 3 color settings and baseline token defaults.
    BASELINE_2021(ColorSpecVersion.SPEC_2021, ColorStyle.TONAL_SPOT),

    /// Selects the 2025 color settings and Material Design 3 Expressive token defaults.
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
