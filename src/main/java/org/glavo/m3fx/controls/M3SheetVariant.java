// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual variant of an M3FX sheet.
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
    public String getStyleClass() {
        return styleClass;
    }
}
