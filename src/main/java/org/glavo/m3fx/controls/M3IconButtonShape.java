// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the resting container shape used by Material icon buttons.
///
/// The shape controls the normal icon button container geometry. Toggle icon buttons can invert the resting
/// shape while selected according to Material Design 3 Expressive icon button guidance.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/specs).
@NotNullByDefault
public enum M3IconButtonShape {
    /// Uses the fully round icon button container.
    ROUND("m3-icon-button-round"),

    /// Uses the square-family icon button container for the active size.
    SQUARE("m3-icon-button-square");

    /// The style class used by this shape.
    private final String styleClass;

    /// Creates an icon button shape with its style class.
    ///
    /// @param styleClass the style class applied to icon buttons using this shape
    M3IconButtonShape(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class applied to icon buttons using this shape.
    ///
    /// @return the style class for this shape
    public String getStyleClass() {
        return styleClass;
    }
}
