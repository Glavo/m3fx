// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the Material Expressive width used by icon buttons and toggle icon buttons.
///
/// The width role combines with [M3IconButtonSize] so compact, default, and wide icon-only actions can use the
/// measurements from the Material Design 3 icon button specification.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/specs).
@NotNullByDefault
public enum M3IconButtonWidth {
    /// Uses the narrow width for the active icon button size.
    NARROW("m3-icon-button-narrow-width"),

    /// Uses the default width for the active icon button size.
    DEFAULT("m3-icon-button-default-width"),

    /// Uses the wide width for the active icon button size.
    WIDE("m3-icon-button-wide-width");

    /// The style class used by this width role.
    private final String styleClass;

    /// Creates an icon button width role with its style class.
    ///
    /// @param styleClass the style class applied to icon buttons using this width
    M3IconButtonWidth(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class applied to icon buttons using this width.
    ///
    /// @return the style class for this width role
    public String getStyleClass() {
        return styleClass;
    }
}
