// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the visual grouping model used by [M3ButtonGroup].
///
/// Standard groups keep each button's own rounded container and only apply Material group spacing and sizing.
/// Connected groups render adjacent buttons as one connected control with shared outer corners and smaller inner
/// corners.
///
/// See [Material Design button groups](https://m3.material.io/components/button-groups/overview).
@NotNullByDefault
public enum M3ButtonGroupVariant {
    /// A standard Material Expressive group with separated button containers.
    STANDARD("m3-standard-button-group"),

    /// A connected Material group with adjacent button containers visually joined by coordinated shapes.
    CONNECTED("m3-connected-button-group");

    /// The style class used by this variant.
    private final String styleClass;

    /// Creates a variant with its style class.
    ///
    /// @param styleClass the style class applied to button groups using this variant
    M3ButtonGroupVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class applied to button groups using this variant.
    ///
    /// @return the style class for this variant
    String styleClass() {
        return styleClass;
    }
}
