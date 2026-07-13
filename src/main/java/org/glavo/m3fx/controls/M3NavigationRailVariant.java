// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the expanded presentation variant of a Material Design 3 navigation rail.
///
/// See [Material Design navigation rail](https://m3.material.io/components/navigation-rail/specs).
@NotNullByDefault
public enum M3NavigationRailVariant {
    /// A persistent expanded rail using the surface color and level-zero elevation.
    STANDARD,

    /// A temporary expanded rail using the surface-container color, large corners, and level-two elevation.
    MODAL
}
