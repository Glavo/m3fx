// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the Material Expressive size used by [M3ButtonGroup].
///
/// The size controls the grouped button container height and default group spacing. Grouped button content
/// padding follows the active button tokens.
///
/// See [Material Design button groups](https://m3.material.io/components/button-groups/specs).
@NotNullByDefault
public enum M3ButtonGroupSize {
    /// Extra-small button groups with 32dp containers.
    EXTRA_SMALL("m3-button-group-extra-small"),

    /// Small button groups with 40dp containers.
    SMALL("m3-button-group-small"),

    /// Medium button groups with 56dp containers.
    MEDIUM("m3-button-group-medium"),

    /// Large button groups with 96dp containers.
    LARGE("m3-button-group-large"),

    /// Extra-large button groups with 136dp containers.
    EXTRA_LARGE("m3-button-group-extra-large");

    /// The style class used by this size.
    private final String styleClass;

    /// Creates a size with its style class.
    ///
    /// @param styleClass the style class applied to button groups using this size
    M3ButtonGroupSize(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class applied to button groups using this size.
    ///
    /// @return the style class for this size
    String styleClass() {
        return styleClass;
    }
}
