// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how [M3ListPane] and [M3ListView] manage selected list items.
///
/// Selection modes determine how item activation changes selection state. They do not prevent applications from
/// setting item selection programmatically, but containers normalize their selected-item views after changes.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public enum M3ListSelectionMode {
    /// Does not change item selection in response to list item actions.
    NONE,

    /// Allows at most one list item to be selected at the same time.
    SINGLE,

    /// Allows more than one list item to be selected at the same time.
    MULTIPLE
}
