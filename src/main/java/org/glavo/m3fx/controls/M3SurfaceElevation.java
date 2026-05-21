// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 surface elevation level.
///
/// Elevation levels select the shadow and tonal elevation treatment used by [M3Surface]. Level zero is flush
/// with the surrounding surface, while higher levels visually separate content layers.
///
/// See [Material Design elevation](https://m3.material.io/styles/elevation/overview).
@NotNullByDefault
public enum M3SurfaceElevation {
    /// Uses no shadow.
    LEVEL0("m3-surface-elevation-level0"),

    /// Uses elevation level one.
    LEVEL1("m3-surface-elevation-level1"),

    /// Uses elevation level two.
    LEVEL2("m3-surface-elevation-level2"),

    /// Uses elevation level three.
    LEVEL3("m3-surface-elevation-level3"),

    /// Uses elevation level four.
    LEVEL4("m3-surface-elevation-level4"),

    /// Uses elevation level five.
    LEVEL5("m3-surface-elevation-level5");

    /// The style class applied for this elevation level.
    private final String styleClass;

    /// Creates a surface elevation.
    M3SurfaceElevation(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class for this elevation level.
    ///
    /// @return the style class applied by this elevation level
    public String getStyleClass() {
        return styleClass;
    }
}
