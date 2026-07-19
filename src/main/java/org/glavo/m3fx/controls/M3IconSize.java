// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies an M3FX icon size role.
///
/// Size roles provide stable icon metrics for [M3Icon], [M3SVGIcon], and icon-only controls. The default logical-pixel
/// values can be overridden by theme CSS, but the role names allow APIs and demos to describe icon scale
/// semantically.
///
/// See [Material Design icons](https://m3.material.io/styles/icons/overview).
@NotNullByDefault
public enum M3IconSize {
    /// Uses the compact 18-logical-pixel icon size.
    SMALL("m3-small-icon", 18.0),

    /// Uses the default 24-logical-pixel icon size.
    MEDIUM("m3-medium-icon", 24.0),

    /// Uses the prominent 32-logical-pixel icon size.
    LARGE("m3-large-icon", 32.0),

    /// Uses the extra prominent 40-logical-pixel icon size.
    EXTRA_LARGE("m3-extra-large-icon", 40.0);

    /// The style class applied for this icon size.
    private final String styleClass;

    /// The default token value for this icon size.
    private final double defaultSize;

    /// Creates an icon size role.
    M3IconSize(String styleClass, double defaultSize) {
        this.styleClass = styleClass;
        this.defaultSize = defaultSize;
    }

    /// Returns the style class for this icon size.
    ///
    /// @return the style class applied by this size role
    String styleClass() {
        return styleClass;
    }

    /// Returns the default logical-pixel size for this icon size.
    ///
    /// @return the default icon size in logical pixels
    double defaultSize() {
        return defaultSize;
    }
}
