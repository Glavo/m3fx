// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the presentation variant of an M3FX sheet.
///
/// Standard sheets are integrated into the surrounding layout and remain visible as part of the page structure.
/// Modal sheets are presented above the content, normally with a scrim and dismissal behavior.
///
/// See [Material Design bottom sheets](https://m3.material.io/components/bottom-sheets/overview) and
/// [Material Design side sheets](https://m3.material.io/components/side-sheets/overview).
@NotNullByDefault
public enum M3SheetVariant {
    /// A sheet integrated into the surrounding layout.
    STANDARD("m3-standard-sheet"),

    /// A sheet displayed above surrounding content.
    MODAL("m3-modal-sheet");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a sheet variant.
    M3SheetVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    ///
    /// @return the style class applied by this variant
    String styleClass() {
        return styleClass;
    }
}
