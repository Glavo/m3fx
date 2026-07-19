// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Immutable color overrides for an [M3ButtonBase].
///
/// Each component is optional. A `null` base component leaves that color under the control of the button variant,
/// active theme, and application stylesheets. A base container or content override remains the source for disabled
/// rendering unless the corresponding disabled component is supplied. This permits an application to override one
/// semantic color without copying the complete theme or losing later theme changes for unrelated colors.
///
/// The content color is used for button text, icon graphics, disclosure graphics, state layers, and ripples. Disabled
/// colors are expected to contain their final opacity; M3FX does not multiply them by the standard disabled opacity.
/// Shape, outline, elevation, typography, and interaction-state opacity remain controlled independently.
///
/// See [Material Design buttons](https://m3.material.io/components/buttons/overview).
///
/// @param containerColor         the base container color, or `null` to use the variant and theme value
/// @param contentColor           the base content and state-layer color, or `null` to use the variant and theme value
/// @param disabledContainerColor the disabled container replacement, or `null` to retain normal cascading
/// @param disabledContentColor   the disabled content replacement, or `null` to retain normal cascading
@NotNullByDefault
public record M3ButtonColors(
        @Nullable Color containerColor,
        @Nullable Color contentColor,
        @Nullable Color disabledContainerColor,
        @Nullable Color disabledContentColor
) {
    /// Returns the base container color override.
    ///
    /// @return the container color, or `null` when the variant and theme determine it
    @Override
    public @Nullable Color containerColor() {
        return containerColor;
    }

    /// Returns the base content and state-layer color override.
    ///
    /// @return the content color, or `null` when the variant and theme determine it
    @Override
    public @Nullable Color contentColor() {
        return contentColor;
    }

    /// Returns the disabled container color override.
    ///
    /// @return the disabled container color, or `null` when the variant and theme determine it
    @Override
    public @Nullable Color disabledContainerColor() {
        return disabledContainerColor;
    }

    /// Returns the disabled content color override.
    ///
    /// @return the disabled content color, or `null` when the variant and theme determine it
    @Override
    public @Nullable Color disabledContentColor() {
        return disabledContentColor;
    }
}
