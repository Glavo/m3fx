// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import org.jetbrains.annotations.NotNullByDefault;

/// Selects which navigation region an [M3AdaptiveScaffold] presents.
///
/// The scaffold never recreates or reparents a navigation control while switching layouts. Both navigation slots
/// remain installed in stable containers, and only the effective slot participates in layout and input handling.
///
/// See [Material Design navigation](https://m3.material.io/components/navigation-bar/overview).
@NotNullByDefault
public enum M3NavigationLayout {
    /// Uses a navigation bar for compact layouts and an available rail for larger layouts.
    ADAPTIVE,

    /// Hides both navigation regions.
    NONE,

    /// Shows the bottom navigation-bar slot.
    BAR,

    /// Shows the logical leading navigation-rail slot.
    RAIL
}
