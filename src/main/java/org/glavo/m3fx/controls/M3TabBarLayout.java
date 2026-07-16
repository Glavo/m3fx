// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Specifies how an [M3TabBar] distributes tabs across its horizontal container.
///
/// Fixed tab bars keep every tab visible and assign each tab the width of the widest item. Scrollable tab bars use
/// each tab's content-derived width and permit horizontal scrolling when the complete row does not fit. Applications
/// should prefer fixed tabs for small peer sets and use scrollable tabs for longer labels or larger sets.
///
/// See [Material Design tabs](https://m3.material.io/components/tabs/guidelines#behavior).
@NotNullByDefault
public enum M3TabBarLayout {
    /// Displays all tabs simultaneously in equal-width cells.
    FIXED,

    /// Displays content-width tabs in a horizontally scrollable row.
    SCROLLABLE
}
