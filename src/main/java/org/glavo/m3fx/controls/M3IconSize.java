// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 icon size role.
@NotNullByDefault
public enum M3IconSize {
    /// Uses the compact 18px icon size.
    SMALL("m3-small-icon", 18.0),

    /// Uses the default 24px icon size.
    MEDIUM("m3-medium-icon", 24.0),

    /// Uses the prominent 32px icon size.
    LARGE("m3-large-icon", 32.0),

    /// Uses the extra prominent 40px icon size.
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
    public String getStyleClass() {
        return styleClass;
    }

    /// Returns the default pixel size for this icon size.
    public double getDefaultSize() {
        return defaultSize;
    }
}
