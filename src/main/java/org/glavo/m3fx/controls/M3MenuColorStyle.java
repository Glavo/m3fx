// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the color mapping used by a Material Design 3 menu.
///
/// Standard menus use surface-based container and selected-item colors. Vibrant menus use tertiary color roles
/// for higher emphasis and should be used sparingly.
///
/// See [Material Design menus](https://m3.material.io/components/menus/specs#b29bad9f-4d87-48d5-b345-a62a25d4dbb8).
@NotNullByDefault
public enum M3MenuColorStyle {
    /// Uses the lower-emphasis surface-based menu color mapping.
    STANDARD,

    /// Uses the higher-emphasis tertiary-based menu color mapping.
    VIBRANT
}
