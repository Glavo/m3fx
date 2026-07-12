// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how a navigation item's icon and label are arranged.
///
/// Vertical items place the label below the active indicator and are intended for compact navigation bars and
/// collapsed navigation rails. Horizontal items place the icon and label together inside the active indicator and
/// are intended for medium-window navigation bars and expanded navigation rails.
///
/// See [Material Design navigation](https://m3.material.io/components/navigation-bar/overview).
@NotNullByDefault
public enum M3NavigationItemLayout {
    /// Places the icon above the label.
    VERTICAL("m3-navigation-item-vertical"),

    /// Places the icon and label side by side.
    HORIZONTAL("m3-navigation-item-horizontal");

    /// The style class representing this layout.
    private final String styleClass;

    /// Creates a navigation item layout.
    ///
    /// @param styleClass the style class representing this layout
    M3NavigationItemLayout(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class representing this layout.
    ///
    /// @return the layout style class
    String styleClass() {
        return styleClass;
    }
}