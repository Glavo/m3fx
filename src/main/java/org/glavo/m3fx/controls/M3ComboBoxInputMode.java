// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Determines how the user finds a choice in an [M3ComboBox].
@NotNullByDefault
public enum M3ComboBoxInputMode {
    /// Displays the committed value and opens the menu when the field is activated.
    READ_ONLY,

    /// Lets the user type to narrow the menu. Only a menu choice can be committed.
    FILTERED_SELECTION
}
