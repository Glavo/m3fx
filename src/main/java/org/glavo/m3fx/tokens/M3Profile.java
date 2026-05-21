// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.monetfx.ColorSpecVersion;
import org.glavo.monetfx.ColorStyle;
import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 token profile used by M3FX.
///
/// A profile selects a compatible group of color-generation settings and token defaults. M3FX currently exposes
/// the original baseline Material Design 3 profile and a Material Design 3 Expressive profile that uses newer
/// dynamic color and component token defaults.
///
/// See [Material Design](https://m3.material.io/) and
/// [Material color](https://m3.material.io/styles/color/overview).
@NotNullByDefault
public enum M3Profile {
    /// Uses the baseline Material Design 3 token defaults.
    BASELINE_2021(ColorSpecVersion.SPEC_2021, ColorStyle.TONAL_SPOT),

    /// Uses the Material Design 3 Expressive 2025 token defaults.
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
    public ColorSpecVersion getColorSpecVersion() {
        return colorSpecVersion;
    }

    /// Returns the MonetFX color style for this profile.
    public ColorStyle getColorStyle() {
        return colorStyle;
    }
}
