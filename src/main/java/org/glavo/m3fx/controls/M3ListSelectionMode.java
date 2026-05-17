// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3List] manages selected list items.
@NotNullByDefault
public enum M3ListSelectionMode {
    /// Does not change item selection in response to list item actions.
    NONE,

    /// Allows at most one list item to be selected at the same time.
    SINGLE,

    /// Allows more than one list item to be selected at the same time.
    MULTIPLE
}
