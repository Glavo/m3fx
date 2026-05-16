// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 toggle icon button color variant.
@NotNullByDefault
public enum M3IconToggleButtonVariant {
    /// Uses the standard transparent icon button container.
    STANDARD("m3-standard-icon-toggle-button"),

    /// Uses the filled icon button container.
    FILLED("m3-filled-icon-toggle-button"),

    /// Uses the filled tonal icon button container.
    TONAL("m3-tonal-icon-toggle-button"),

    /// Uses the outlined icon button container.
    OUTLINED("m3-outlined-icon-toggle-button");

    /// The style class applied for this toggle icon button variant.
    private final String styleClass;

    /// Creates a toggle icon button variant.
    M3IconToggleButtonVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class for this toggle icon button variant.
    public String getStyleClass() {
        return styleClass;
    }
}
