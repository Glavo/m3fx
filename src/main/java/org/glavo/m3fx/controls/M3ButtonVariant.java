// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual variant of an M3FX button.
///
/// Variants correspond to the Material Design 3 button emphasis levels. They affect the button container,
/// outline, text color, elevation behavior, and state-layer colors through style classes and component tokens.
/// Use the variant that matches the importance of the action in the current UI context.
///
/// See [Material Design buttons](https://m3.material.io/components/buttons/overview).
@NotNullByDefault
public enum M3ButtonVariant {
    /// A high-emphasis filled button for the most important action in a group.
    FILLED("m3-filled-button"),

    /// A medium-emphasis filled tonal button that uses a softer container color.
    TONAL("m3-tonal-button"),

    /// A medium-emphasis outlined button with a transparent container and visible outline.
    OUTLINED("m3-outlined-button"),

    /// A low-emphasis text button without a visible container in the resting state.
    TEXT("m3-text-button"),

    /// An elevated button that visually separates an action from a surface.
    ELEVATED("m3-elevated-button");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a button variant.
    ///
    /// @param styleClass the style class applied to buttons using this variant
    M3ButtonVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    ///
    /// @return the style class applied by this variant
    String styleClass() {
        return styleClass;
    }
}
