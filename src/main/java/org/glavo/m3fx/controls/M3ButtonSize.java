// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the Material Design 3 size shared by buttons and button-based action controls.
///
/// The size controls the visual container height, typography, icon size, padding, outline width, and shape targets.
/// [M3Button], [M3IconButton], [M3IconToggleButton], [M3ButtonGroup], and [M3SplitButton] use the same five-step
/// Material Expressive size scale. Small is the default size and is also the only size defined by baseline
/// Material Design 3.
///
/// See [Material Design buttons](https://m3.material.io/components/buttons/specs),
/// [Material Design icon buttons](https://m3.material.io/components/icon-buttons/specs), and
/// [Material Design button groups](https://m3.material.io/components/button-groups/specs).
@NotNullByDefault
public enum M3ButtonSize {
    /// Extra-small action controls with 32-logical-pixel containers.
    EXTRA_SMALL("extra-small"),

    /// Small action controls with 40-logical-pixel containers.
    SMALL("small"),

    /// Medium action controls with 56-logical-pixel containers.
    MEDIUM("medium"),

    /// Large action controls with 96-logical-pixel containers.
    LARGE("large"),

    /// Extra-large action controls with 136-logical-pixel containers.
    EXTRA_LARGE("extra-large");

    /// The suffix used to form control-specific size style classes.
    private final String cssSuffix;

    /// Creates a button size with its CSS suffix.
    ///
    /// @param cssSuffix the suffix used to form control-specific size style classes
    M3ButtonSize(String cssSuffix) {
        this.cssSuffix = cssSuffix;
    }

    /// Returns the suffix used to form control-specific size style classes.
    ///
    /// @return the CSS class suffix for this size
    String cssSuffix() {
        return cssSuffix;
    }
}
