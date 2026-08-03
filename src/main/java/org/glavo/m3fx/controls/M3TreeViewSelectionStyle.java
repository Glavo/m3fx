// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3TreeView] presents selected items.
///
/// Highlight selection is the Material default for choosing the current item. Checkbox selection borrows the
/// explicit bulk-selection affordance from Adobe Spectrum 2 while using [M3CheckBox] and Material state colors.
@NotNullByDefault
public enum M3TreeViewSelectionStyle {
    /// Uses the complete Material row container to indicate selection.
    HIGHLIGHT("m3-highlight-tree-selection"),

    /// Uses a leading Material checkbox and leaves the row container unselected.
    CHECKBOX("m3-checkbox-tree-selection");

    /// The style class representing this selection style.
    private final String styleClass;

    /// Creates a selection style with its stable CSS class.
    ///
    /// @param styleClass the style class representing this selection style
    M3TreeViewSelectionStyle(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the stable CSS class representing this selection style.
    ///
    /// @return the selection-style class
    String styleClass() {
        return styleClass;
    }
}
