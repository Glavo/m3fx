// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 chip that represents a selectable filtering option.
///
/// Activating a filter chip toggles its persistent [selected][M3SelectableChip#selectedProperty()] state and
/// emits an action event. A filter chip can be managed by an [M3ChipGroup] when exclusive or constrained
/// selection is required. Text, leading and trailing graphics, container treatment, and sizing are inherited
/// from [M3Chip]. The default constructors create a flat, unselected chip.
///
/// See [Material Design filter chips](https://m3.material.io/components/chips/specs#filter-chip).
@NotNullByDefault
public final class M3FilterChip extends M3SelectableChip {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-filter-chip";

    /// Creates a flat, unselected filter chip with empty text and no graphics.
    public M3FilterChip() {
        this("", null);
    }

    /// Creates a flat, unselected filter chip with the specified text and no graphics.
    ///
    /// @param text the text displayed by the chip
    /// @throws NullPointerException if `text` is `null`
    public M3FilterChip(String text) {
        this(text, null);
    }

    /// Creates a flat, unselected filter chip with the specified text and leading graphic.
    ///
    /// @param text the text displayed by the chip
    /// @param graphic the leading graphic, or `null` for no leading graphic
    /// @throws NullPointerException if `text` is `null`
    public M3FilterChip(String text, @Nullable Node graphic) {
        super(text, graphic, DEFAULT_STYLE_CLASS);
    }
}
