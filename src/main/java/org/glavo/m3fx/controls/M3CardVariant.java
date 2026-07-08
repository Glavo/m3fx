// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual variant of an M3FX card.
///
/// Card variants control how the card separates grouped content from the surrounding surface. Elevated cards use
/// shadow, filled cards use a tonal container, and outlined cards use a boundary stroke.
///
/// See [Material Design cards](https://m3.material.io/components/cards/overview).
@NotNullByDefault
public enum M3CardVariant {
    /// An elevated card.
    ELEVATED("m3-elevated-card"),

    /// A filled card.
    FILLED("m3-filled-card"),

    /// An outlined card.
    OUTLINED("m3-outlined-card");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a card variant.
    M3CardVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    ///
    /// @return the style class applied by this variant
    String styleClass() {
        return styleClass;
    }
}
