// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import org.jetbrains.annotations.NotNullByDefault;

/// Selects which navigation region an [M3AdaptiveScaffold] presents.
///
/// Changing the layout selects which assigned navigation region is visible and participates in input handling. It
/// does not clear either navigation slot, so a later layout change can present the same assigned navigation control.
///
/// See [Material Design navigation bars](https://m3.material.io/components/navigation-bar/overview) and
/// [Material Design navigation rails](https://m3.material.io/components/navigation-rail/overview).
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
