// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual and semantic variant of an M3FX chip.
///
/// Chip variants communicate the task represented by a chip. The variant controls the default style class and
/// should match the user's interaction: assist actions, filter choices, input entities, or suggestions.
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public enum M3ChipVariant {
    /// An assist chip that performs a contextual action.
    ASSIST("m3-assist-chip"),

    /// A filter chip that represents a selectable filtering option.
    FILTER("m3-filter-chip"),

    /// An input chip that represents user-provided input or an entity.
    INPUT("m3-input-chip"),

    /// A suggestion chip that offers a suggested action or value.
    SUGGESTION("m3-suggestion-chip");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a chip variant.
    M3ChipVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    ///
    /// @return the style class applied by this variant
    public String getStyleClass() {
        return styleClass;
    }
}
