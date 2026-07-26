// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the corner treatment of a color swatch or swatch-picker cell.
@NotNullByDefault
public enum M3ColorSwatchRounding {
    /// Uses the corner radius supplied by the active style.
    DEFAULT,

    /// Uses square corners with a zero corner radius.
    NONE,

    /// Uses the greatest corner radius that fits the rendered bounds, producing a circle for square bounds and a
    /// capsule for rectangular bounds.
    FULL
}
