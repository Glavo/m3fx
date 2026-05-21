// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies an M3FX icon color variant.
///
/// Icon variants map [M3Icon] content to Material color roles. They are useful when an icon is used outside a
/// higher-level control and needs to opt into a specific semantic color without writing CSS.
///
/// See [Material Design icons](https://m3.material.io/styles/icons/overview) and
/// [Material Design color roles](https://m3.material.io/styles/color/roles).
@NotNullByDefault
public enum M3IconVariant {
    /// Uses the primary color role.
    PRIMARY("m3-primary-icon"),

    /// Uses the secondary color role.
    SECONDARY("m3-secondary-icon"),

    /// Uses the tertiary color role.
    TERTIARY("m3-tertiary-icon"),

    /// Uses the error color role.
    ERROR("m3-error-icon"),

    /// Uses the on-surface color role.
    ON_SURFACE("m3-on-surface-icon"),

    /// Uses the on-surface-variant color role.
    ON_SURFACE_VARIANT("m3-on-surface-variant-icon"),

    /// Uses the inverse-on-surface color role.
    INVERSE_ON_SURFACE("m3-inverse-on-surface-icon");

    /// The style class applied for this icon variant.
    private final String styleClass;

    /// Creates an icon color variant.
    M3IconVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class for this icon variant.
    ///
    /// @return the style class applied by this variant
    public String getStyleClass() {
        return styleClass;
    }
}
