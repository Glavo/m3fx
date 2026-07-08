// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the derived line count of an M3FX list item.
///
/// The line count is computed from the list item's headline, overline, supporting text, and trailing supporting
/// text. It drives the default list item height and vertical text placement.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public enum M3ListItemLineCount {
    /// A list item with only headline content.
    ONE_LINE(1),

    /// A list item with headline plus either overline or supporting content.
    TWO_LINE(2),

    /// A list item with headline, overline, and supporting content.
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
