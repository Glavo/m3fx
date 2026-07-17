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
    /// The 40-logical-pixel baseline small FAB.
    ///
    /// Small FABs remain available for baseline Material 3, but Material 3 Expressive recommends using the
    /// regular size or larger.
    SMALL("m3-small-fab"),

    /// The default 56-logical-pixel FAB size.
    ///
    /// This size also supplies the metrics for a small extended FAB.
    REGULAR("m3-regular-fab"),

    /// The 80-logical-pixel medium FAB size introduced by Material 3 Expressive.
    MEDIUM("m3-medium-fab"),

    /// The prominent 96-logical-pixel large FAB size.
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
