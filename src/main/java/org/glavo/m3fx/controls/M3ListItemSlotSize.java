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

    /// Uses a 24-logical-pixel icon slot.
    ICON,

    /// Uses a 40-logical-pixel avatar slot.
    AVATAR,

    /// Uses a 56-logical-pixel square thumbnail slot.
    THUMBNAIL,

    /// Uses a thumbnail slot measuring 64 by 56 logical pixels.
    WIDE_THUMBNAIL
}
