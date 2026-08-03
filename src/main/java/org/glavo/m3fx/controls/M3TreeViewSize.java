// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the nominal row size of an [M3TreeView].
///
/// Each role supplies a default fixed virtualized row height. The value is expressed in JavaFX logical pixels and
/// does not change the hierarchy, selection, or expansion model. An explicit inherited `fixedCellSize` value or an
/// author stylesheet may override the role's default metric.
@NotNullByDefault
public enum M3TreeViewSize {
    /// Uses compact 32-pixel rows.
    SMALL("m3-small-tree-view", 32.0),

    /// Uses the default 40-pixel rows.
    MEDIUM("m3-medium-tree-view", 40.0),

    /// Uses comfortable 48-pixel rows.
    LARGE("m3-large-tree-view", 48.0),

    /// Uses prominent 56-pixel rows.
    EXTRA_LARGE("m3-extra-large-tree-view", 56.0);

    /// The style class selecting this size role.
    private final String styleClass;

    /// The fixed row height in logical pixels.
    private final double rowHeight;

    /// Creates a tree-view size role.
    ///
    /// @param styleClass the style class selecting this size
    /// @param rowHeight  the fixed row height in logical pixels
    M3TreeViewSize(String styleClass, double rowHeight) {
        this.styleClass = styleClass;
        this.rowHeight = rowHeight;
    }

    /// Returns the nominal fixed row height supplied by this role.
    ///
    /// @return the nominal row height in logical pixels
    public double getRowHeight() {
        return rowHeight;
    }

    /// Returns the style class selecting this size.
    ///
    /// @return the size style class
    String styleClass() {
        return styleClass;
    }
}
