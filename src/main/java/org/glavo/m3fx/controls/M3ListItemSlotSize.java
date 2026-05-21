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
    AUTO(-1.0, -1.0, 0.0),

    /// Uses a 24px icon slot.
    ICON(24.0, 24.0, 0.0),

    /// Uses a 40px avatar slot.
    AVATAR(40.0, 40.0, 20.0),

    /// Uses a 56px square thumbnail slot.
    THUMBNAIL(56.0, 56.0, 4.0),

    /// Uses a 64px by 56px thumbnail slot.
    WIDE_THUMBNAIL(64.0, 56.0, 4.0);

    /// The fixed slot width, or a negative value when the slot uses computed sizing.
    private final double width;

    /// The fixed slot height, or a negative value when the slot uses computed sizing.
    private final double height;

    /// The clipping radius used for fixed-size slots.
    private final double shapeRadius;

    /// Creates a list item slot size.
    M3ListItemSlotSize(double width, double height, double shapeRadius) {
        this.width = width;
        this.height = height;
        this.shapeRadius = shapeRadius;
    }

    /// Returns whether this slot uses fixed width and height metrics.
    ///
    /// @return `true` when this value supplies fixed width and height metrics
    public boolean isFixedSize() {
        return width >= 0.0 && height >= 0.0;
    }

    /// Returns the fixed slot width.
    ///
    /// @return the fixed slot width, or a negative value when computed sizing is used
    public double getWidth() {
        return width;
    }

    /// Returns the fixed slot height.
    ///
    /// @return the fixed slot height, or a negative value when computed sizing is used
    public double getHeight() {
        return height;
    }

    /// Returns the clipping radius for fixed-size slot content.
    ///
    /// @return the clipping radius in pixels
    public double getShapeRadius() {
        return shapeRadius;
    }
}
