// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Immutable color overrides for an [M3Card].
///
/// A `null` base component inherits the card variant, active theme, and application stylesheet value. A base
/// container or content override remains active while the card is disabled unless the corresponding disabled
/// replacement is supplied. Content colors are exposed to card descendants through the normal M3FX on-surface color
/// lookups and are also used by the card's state layer and ripple.
///
/// This value does not configure the outline of an outlined card or the elevation of an elevated card. Those
/// treatments remain controlled by the card variant, component tokens, and CSS.
///
/// See [Material Design cards](https://m3.material.io/components/cards/overview).
///
/// @param containerColor         the base container color, or `null` to use the variant and theme value
/// @param contentColor           the base content color, or `null` to use the variant and theme value
/// @param disabledContainerColor the disabled container replacement, or `null` to retain normal cascading
/// @param disabledContentColor   the disabled content replacement, or `null` to retain normal cascading
@NotNullByDefault
public record M3CardColors(
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

    /// Returns the base content color override.
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
