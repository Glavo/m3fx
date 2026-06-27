// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the Material Expressive size used by [M3SplitButton].
///
/// The size controls the primary action height, menu-side width, horizontal padding, spacing between the two
/// button parts, and connected inner-corner shape through CSS tokens.
///
/// See [Material Design split buttons](https://m3.material.io/components/split-button/specs).
@NotNullByDefault
public enum M3SplitButtonSize {
    /// Extra-small split buttons with 32dp containers.
    EXTRA_SMALL("m3-split-button-extra-small"),

    /// Small split buttons with 40dp containers.
    SMALL("m3-split-button-small"),

    /// Medium split buttons with 56dp containers.
    MEDIUM("m3-split-button-medium"),

    /// Large split buttons with 96dp containers.
    LARGE("m3-split-button-large"),

    /// Extra-large split buttons with 136dp containers.
    EXTRA_LARGE("m3-split-button-extra-large");

    /// The style class used by this size.
    private final String styleClass;

    /// Creates a split button size with its style class.
    ///
    /// @param styleClass the style class applied to split buttons using this size
    M3SplitButtonSize(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class applied to split buttons using this size.
    ///
    /// @return the style class for this size
    public String getStyleClass() {
        return styleClass;
    }
}
