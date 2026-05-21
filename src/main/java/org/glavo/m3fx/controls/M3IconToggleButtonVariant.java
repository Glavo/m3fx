// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 toggle icon button color variant.
///
/// The variant controls the resting container treatment and selected-state emphasis for [M3IconToggleButton].
/// Use standard buttons for low-emphasis icon toggles, filled and tonal variants for selected or higher-emphasis
/// states, and outlined buttons when the control needs a persistent boundary.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/overview).
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
    ///
    /// @return the style class applied by this variant
    public String getStyleClass() {
        return styleClass;
    }
}
