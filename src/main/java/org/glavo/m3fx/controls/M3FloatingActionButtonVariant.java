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
    /// A baseline floating action button using a surface container.
    ///
    /// Surface FABs remain available for baseline Material 3 but are not recommended for Expressive layouts.
    SURFACE("m3-surface-fab"),

    /// A tonal floating action button using the primary-container color role.
    PRIMARY_CONTAINER("m3-primary-container-fab"),

    /// A tonal floating action button using the secondary-container color role.
    SECONDARY_CONTAINER("m3-secondary-container-fab"),

    /// A tonal floating action button using the tertiary-container color role.
    TERTIARY_CONTAINER("m3-tertiary-container-fab"),

    /// A high-emphasis floating action button using the primary color role.
    PRIMARY("m3-primary-fab"),

    /// A high-emphasis floating action button using the secondary color role.
    SECONDARY("m3-secondary-fab"),

    /// A high-emphasis floating action button using the tertiary color role.
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
