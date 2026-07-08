// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual variant of an M3FX floating action button.
///
/// The variant selects the Material color role used for the floating action button container. It does not change
/// the button's size; combine it with [M3FloatingActionButtonSize] when both color and scale need to vary.
///
/// See [Material Design floating action buttons](https://m3.material.io/components/floating-action-button/overview).
@NotNullByDefault
public enum M3FloatingActionButtonVariant {
    /// A floating action button using the surface container color.
    SURFACE("m3-surface-fab"),

    /// A floating action button using the primary container color.
    PRIMARY("m3-primary-fab"),

    /// A floating action button using the secondary container color.
    SECONDARY("m3-secondary-fab"),

    /// A floating action button using the tertiary container color.
    TERTIARY("m3-tertiary-fab");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a floating action button variant.
    M3FloatingActionButtonVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    ///
    /// @return the style class applied by this variant
    String styleClass() {
        return styleClass;
    }
}
