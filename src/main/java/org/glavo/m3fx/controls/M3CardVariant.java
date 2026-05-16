// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual variant of an m3fx card.
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
    public String getStyleClass() {
        return styleClass;
    }
}
