// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Immutable color overrides for an [M3Surface].
///
/// A `null` component leaves that color under the control of the surface variant, active theme, and application
/// stylesheets. The content color is exposed to descendants through the M3FX on-surface color lookups. Elevation,
/// shape, padding, typography, and descendant component colors remain independently configurable.
///
/// See [Material Design color roles](https://m3.material.io/styles/color/roles).
///
/// @param containerColor the surface container color, or `null` to use the variant and theme value
/// @param contentColor   the surface content color, or `null` to use the variant and theme value
@NotNullByDefault
public record M3SurfaceColors(
        @Nullable Color containerColor,
        @Nullable Color contentColor
) {
    /// Returns the surface container color override.
    ///
    /// @return the container color, or `null` when the variant and theme determine it
    @Override
    public @Nullable Color containerColor() {
        return containerColor;
    }

    /// Returns the surface content color override.
    ///
    /// @return the content color, or `null` when the variant and theme determine it
    @Override
    public @Nullable Color contentColor() {
        return contentColor;
    }
}
