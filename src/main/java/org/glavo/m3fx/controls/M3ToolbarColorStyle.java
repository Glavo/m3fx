// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the color mapping used by a Material Design toolbar.
///
/// The color style is independent of whether the toolbar is [M3ToolbarVariant#FLOATING] or
/// [M3ToolbarVariant#DOCKED]. Standard toolbars use surface and secondary-container roles. Vibrant toolbars use
/// primary-container roles for the toolbar and surface roles for selected actions.
///
/// See [Material Design toolbar specifications](https://m3.material.io/components/toolbars/specs).
@NotNullByDefault
public enum M3ToolbarColorStyle {
    /// Uses the surface-based toolbar color mapping.
    STANDARD,

    /// Uses the higher-emphasis primary-container color mapping.
    VIBRANT
}
