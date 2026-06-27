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
    EXTRA_SMALL("m3-icon-button-extra-small", 20.0),

    /// Small icon buttons with 40dp containers and 24dp icons.
    SMALL("m3-icon-button-small", 24.0),

    /// Medium icon buttons with 56dp containers and 24dp icons.
    MEDIUM("m3-icon-button-medium", 24.0),

    /// Large icon buttons with 96dp containers and 32dp icons.
    LARGE("m3-icon-button-large", 32.0),

    /// Extra-large icon buttons with 136dp containers and 40dp icons.
    EXTRA_LARGE("m3-icon-button-extra-large", 40.0);

    /// The style class used by this size.
    private final String styleClass;

    /// The default icon glyph size for this icon button size.
    private final double defaultIconSize;

    /// Creates an icon button size with its style class and default glyph size.
    ///
    /// @param styleClass the style class applied to icon buttons using this size
    /// @param defaultIconSize the default icon glyph size for this size
    M3IconButtonSize(String styleClass, double defaultIconSize) {
        this.styleClass = styleClass;
        this.defaultIconSize = defaultIconSize;
    }

    /// Returns the style class applied to icon buttons using this size.
    ///
    /// @return the style class for this size
    public String getStyleClass() {
        return styleClass;
    }

    /// Returns the default icon glyph size for this icon button size.
    ///
    /// @return the default icon glyph size in pixels
    public double getDefaultIconSize() {
        return defaultIconSize;
    }
}
