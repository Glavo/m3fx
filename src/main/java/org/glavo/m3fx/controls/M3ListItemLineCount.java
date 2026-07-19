// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the derived line count of an M3FX list item.
///
/// The count starts with the headline and increases for populated overline and supporting-text slots. Trailing
/// supporting text does not affect it. The result drives the default list item height and vertical text placement.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public enum M3ListItemLineCount {
    /// A one-line layout with neither overline nor supporting content.
    ONE_LINE(1),

    /// A two-line layout with exactly one populated overline or supporting-text slot.
    TWO_LINE(2),

    /// A three-line layout with populated overline and supporting-text slots.
    THREE_LINE(3);

    /// The number of text lines represented by this value.
    private final int lineCount;

    /// Creates a line count value.
    M3ListItemLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    /// Returns the number of text lines represented by this value.
    ///
    /// @return the number of text lines represented by this value
    int getLineCount() {
        return lineCount;
    }
}
