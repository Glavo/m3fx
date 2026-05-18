// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a fixed Material Design 3 list item slot size.
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
    public boolean isFixedSize() {
        return width >= 0.0 && height >= 0.0;
    }

    /// Returns the fixed slot width.
    public double getWidth() {
        return width;
    }

    /// Returns the fixed slot height.
    public double getHeight() {
        return height;
    }

    /// Returns the clipping radius for fixed-size slot content.
    public double getShapeRadius() {
        return shapeRadius;
    }
}
