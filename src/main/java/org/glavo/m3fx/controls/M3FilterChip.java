// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 filter chip representing a selectable filtering option.
///
/// Filter chips toggle their persistent selected state when fired and can participate in an [M3ChipGroup].
///
/// See [Material Design filter chips](https://m3.material.io/components/chips/specs#filter-chip).
@NotNullByDefault
public final class M3FilterChip extends M3SelectableChip {
    /// The style class identifying filter chips.
    public static final String STYLE_CLASS = "m3-filter-chip";

    /// Creates an empty filter chip.
    public M3FilterChip() {
        this("", null);
    }

    /// Creates a filter chip with text.
    ///
    /// @param text the text displayed by the chip
    public M3FilterChip(String text) {
        this(text, null);
    }

    /// Creates a filter chip with text and graphic content.
    ///
    /// @param text the text displayed by the chip
    /// @param graphic the optional graphic displayed with the text
    public M3FilterChip(String text, @Nullable Node graphic) {
        super(text, graphic, STYLE_CLASS);
    }
}
