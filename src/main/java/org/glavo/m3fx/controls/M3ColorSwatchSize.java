// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the nominal content size of an [M3ColorSwatch].
///
/// Sizes are expressed in JavaFX logical pixels. Control insets are added outside the square content area.
@NotNullByDefault
public enum M3ColorSwatchSize {
    /// A swatch with a 20 by 20 logical-pixel content area.
    EXTRA_SMALL(20.0),

    /// A swatch with a 28 by 28 logical-pixel content area.
    SMALL(28.0),

    /// A swatch with a 36 by 36 logical-pixel content area.
    MEDIUM(36.0),

    /// A swatch with a 48 by 48 logical-pixel content area.
    LARGE(48.0);

    /// The square swatch size in logical pixels.
    private final double size;

    /// Creates a swatch size descriptor.
    ///
    /// @param size the square size in logical pixels
    M3ColorSwatchSize(double size) {
        this.size = size;
    }

    /// Returns the width and height of the square swatch content area.
    ///
    /// @return the size in logical pixels
    public double getSize() {
        return size;
    }
}
