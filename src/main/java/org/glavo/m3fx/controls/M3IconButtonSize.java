// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the Material Expressive size used by icon buttons and toggle icon buttons.
///
/// The size controls the visual container height, glyph size, size-specific widths, outline width, and shape
/// morph target tokens through CSS. Small is the default Material icon button size.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/specs).
@NotNullByDefault
public enum M3IconButtonSize {
    /// Extra-small icon buttons with 32dp containers and 20dp icons.
    EXTRA_SMALL("m3-icon-button-extra-small"),

    /// Small icon buttons with 40dp containers and 24dp icons.
    SMALL("m3-icon-button-small"),

    /// Medium icon buttons with 56dp containers and 24dp icons.
    MEDIUM("m3-icon-button-medium"),

    /// Large icon buttons with 96dp containers and 32dp icons.
    LARGE("m3-icon-button-large"),

    /// Extra-large icon buttons with 136dp containers and 40dp icons.
    EXTRA_LARGE("m3-icon-button-extra-large");

    /// The style class used by this size.
    private final String styleClass;

    /// Creates an icon button size with its style class.
    ///
    /// @param styleClass the style class applied to icon buttons using this size
    M3IconButtonSize(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class applied to icon buttons using this size.
    ///
    /// @return the style class for this size
    String styleClass() {
        return styleClass;
    }
}
