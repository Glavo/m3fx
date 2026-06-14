// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the visual treatment of a Material Design toolbar.
///
/// Toolbar variants change the container color, shape, and elevation while keeping the same item layout API.
/// Use `STANDARD` for inline tool rows, `FLOATING` for prominent contextual tool palettes, and `DOCKED` when the
/// toolbar sits flush against an application edge.
///
/// See [Material Design toolbars](https://m3.material.io/components/toolbars/overview).
@NotNullByDefault
public enum M3ToolbarVariant {
    /// A flat toolbar that blends with the surrounding surface.
    STANDARD("m3-toolbar-standard"),

    /// A rounded elevated toolbar for floating contextual tools.
    FLOATING("m3-toolbar-floating"),

    /// A full-width or full-height toolbar docked to an application edge.
    DOCKED("m3-toolbar-docked");

    /// The style class representing this variant.
    private final String styleClass;

    /// Creates a toolbar variant.
    ///
    /// @param styleClass the style class representing this variant
    M3ToolbarVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class representing this variant.
    ///
    /// @return the style class representing this variant
    public String getStyleClass() {
        return styleClass;
    }
}
