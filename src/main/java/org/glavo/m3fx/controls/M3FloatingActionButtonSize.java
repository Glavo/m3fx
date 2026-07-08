// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the size variant of an M3FX floating action button.
///
/// Size variants select the Material container size and related icon or label metrics for
/// [M3FloatingActionButton]. They can be combined with [M3FloatingActionButtonVariant] to choose both scale and
/// color role.
///
/// See [Material Design floating action buttons](https://m3.material.io/components/floating-action-button/overview).
@NotNullByDefault
public enum M3FloatingActionButtonSize {
    /// A compact floating action button.
    SMALL("m3-small-fab"),

    /// The default floating action button size.
    REGULAR("m3-regular-fab"),

    /// A prominent floating action button.
    LARGE("m3-large-fab");

    /// The JavaFX style class used by this size.
    private final String styleClass;

    /// Creates a floating action button size.
    M3FloatingActionButtonSize(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this size.
    ///
    /// @return the style class applied by this size
    String styleClass() {
        return styleClass;
    }
}
