// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual variant of an M3FX text input control.
///
/// Text input variants select the Material field container treatment. Filled fields use a tonal container and
/// underline, while outlined fields use a transparent container with an outline and floating-label notch.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public enum M3TextInputVariant {
    /// A filled text input.
    FILLED("m3-filled-field"),

    /// An outlined text input.
    OUTLINED("m3-outlined-field");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a text input variant.
    M3TextInputVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    ///
    /// @return the style class applied by this variant
    String styleClass() {
        return styleClass;
    }
}
