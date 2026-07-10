// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the resting container shape used by Material Design 3 buttons.
///
/// Round is the baseline and Material Expressive default. Square uses the size-specific rounded-square token;
/// pressed and selected controls can temporarily morph to the corresponding state shape.
///
/// See [Material Design buttons](https://m3.material.io/components/buttons/specs) and
/// [Material Design icon buttons](https://m3.material.io/components/icon-buttons/specs).
@NotNullByDefault
public enum M3ButtonShape {
    /// Uses the fully round button container.
    ROUND("round"),

    /// Uses the rounded-square button container for the active size.
    SQUARE("square");

    /// The suffix used to form control-specific shape style classes.
    private final String cssSuffix;

    /// Creates a button shape with its CSS suffix.
    ///
    /// @param cssSuffix the suffix used to form control-specific shape style classes
    M3ButtonShape(String cssSuffix) {
        this.cssSuffix = cssSuffix;
    }

    /// Returns the suffix used to form control-specific shape style classes.
    ///
    /// @return the CSS class suffix for this shape
    String cssSuffix() {
        return cssSuffix;
    }
}
