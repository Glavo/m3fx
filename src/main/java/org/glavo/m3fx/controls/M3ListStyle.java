// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the visual containment style used by Material Design 3 lists.
///
/// Standard lists form one continuous vertical sequence and normally use dividers only when stronger separation
/// is required. Segmented lists render contained items with the Material segmented gap and expressive shape
/// treatment without changing list selection or navigation behavior.
///
/// See [Material Design lists](https://m3.material.io/components/lists/specs).
@NotNullByDefault
public enum M3ListStyle {
    /// Uses a continuous list with no automatic gap between adjacent items.
    STANDARD("m3-standard-list"),

    /// Uses contained list items separated by the Material segmented gap.
    SEGMENTED("m3-segmented-list");

    /// The style class representing this list style.
    private final String styleClass;

    /// Creates a list style with its stable CSS class.
    ///
    /// @param styleClass the style class representing the list style
    M3ListStyle(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class representing this list style.
    ///
    /// @return the stable list style class
    String styleClass() {
        return styleClass;
    }
}
