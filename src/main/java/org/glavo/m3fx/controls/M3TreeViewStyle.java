// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the visual containment style used by an [M3TreeView].
///
/// The standard style presents one continuous hierarchy. The detached style gives each visible row an individually
/// contained surface without changing expansion, selection, or keyboard behavior.
@NotNullByDefault
public enum M3TreeViewStyle {
    /// Uses continuous edge-to-edge rows.
    STANDARD("m3-standard-tree-view"),

    /// Uses inset, individually contained rows.
    DETACHED("m3-detached-tree-view");

    /// The style class representing this containment style.
    private final String styleClass;

    /// Creates a containment style with its stable CSS class.
    ///
    /// @param styleClass the style class representing this containment style
    M3TreeViewStyle(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class representing this containment style.
    ///
    /// @return the stable containment style class
    String styleClass() {
        return styleClass;
    }
}
