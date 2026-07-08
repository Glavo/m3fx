// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a fixed Material Design 3 list item slot size.
///
/// Slot sizes define how leading and trailing list item content is measured and clipped. They provide the
/// standard metrics for icons, avatars, and thumbnails while still allowing [AUTO] for arbitrary custom nodes.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public enum M3ListItemSlotSize {
    /// Uses the node's computed size without clipping.
    AUTO,

    /// Uses a 24px icon slot.
    ICON,

    /// Uses a 40px avatar slot.
    AVATAR,

    /// Uses a 56px square thumbnail slot.
    THUMBNAIL,

    /// Uses a 64px by 56px thumbnail slot.
    WIDE_THUMBNAIL
}
