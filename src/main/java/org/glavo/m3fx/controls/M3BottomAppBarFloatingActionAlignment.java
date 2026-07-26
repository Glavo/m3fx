// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines where an [M3BottomAppBar] places its floating action node.
///
/// This alignment affects only the optional floating action node. Regular action nodes remain in the action list.
///
/// See [Material Design bottom app bars](https://m3.material.io/components/bottom-app-bar/overview).
@NotNullByDefault
public enum M3BottomAppBarFloatingActionAlignment {
    /// Places the floating action node before the regular actions.
    START("m3-bottom-app-bar-floating-action-start"),

    /// Places the floating action node near the center of the available bar space.
    CENTER("m3-bottom-app-bar-floating-action-center"),

    /// Places the floating action node at the trailing edge.
    END("m3-bottom-app-bar-floating-action-end");

    /// The style class associated with this alignment.
    private final String styleClass;

    /// Creates a floating action alignment.
    ///
    /// @param styleClass the style class applied for this alignment
    M3BottomAppBarFloatingActionAlignment(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class associated with this alignment.
    ///
    /// @return the style class applied by this alignment
    String styleClass() {
        return styleClass;
    }
}
