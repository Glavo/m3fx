// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies how a Material Design toolbar is positioned relative to application content.
///
/// Floating and docked toolbars use different container shapes, elevation, padding, and item-spacing rules. Their
/// color mapping is selected independently through [M3Toolbar#setColorStyle(M3ToolbarColorStyle)].
///
/// See [Material Design toolbars](https://m3.material.io/components/toolbars/overview).
@NotNullByDefault
public enum M3ToolbarVariant {
    /// A rounded, elevated toolbar placed over or between application content.
    FLOATING("m3-toolbar-floating"),

    /// A flat toolbar attached to an application edge.
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
    String styleClass() {
        return styleClass;
    }
}
