// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the container treatment used by an [M3Chip].
///
/// Material chips are flat by default and may be elevated when they sit on visually complex surfaces.
/// The selected style class controls the container color, outline, and elevation behavior while the chip variant
/// keeps the chip's semantic role.
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public enum M3ChipStyle {
    /// Uses the default flat chip container with an outline for unselected chips.
    FLAT("m3-flat-chip"),

    /// Uses an elevated chip container for chips placed on visually complex backgrounds.
    ELEVATED("m3-elevated-chip");

    /// The JavaFX style class used by this chip style.
    private final String styleClass;

    /// Creates a chip style.
    M3ChipStyle(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this chip style.
    ///
    /// @return the style class applied by this chip style
    String styleClass() {
        return styleClass;
    }
}
