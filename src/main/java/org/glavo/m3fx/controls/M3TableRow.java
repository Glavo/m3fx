// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleRole;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.skins.M3TableRowSkin;
import org.jetbrains.annotations.NotNullByDefault;

/// Renders one reusable Material row for an [M3TableView].
///
/// The row delegates cell creation, column layout, selection, editing, and virtualization to [TableRow]. Its
/// Material skin adds a row-wide state layer and bounded ripple beneath the inherited table cells. A table may
/// reuse one row for unrelated items while scrolling; application subclasses that retain item-dependent state must
/// therefore clear or replace that state whenever the inherited item changes.
///
/// @param <T> the row-item type
@NotNullByDefault
public class M3TableRow<T> extends TableRow<T> {
    /// The default row style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-table-row";

    /// Creates an empty reusable Material table row.
    public M3TableRow() {
        M3ControlStyles.add(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TABLE_ROW);
    }

    /// Creates the Material table-row skin with bounded state-layer and ripple feedback.
    ///
    /// @return a new Material table-row skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TableRowSkin<>(this);
    }
}
