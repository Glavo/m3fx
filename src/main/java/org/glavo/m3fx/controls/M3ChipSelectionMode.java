// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3ChipGroup] manages selected chips.
@NotNullByDefault
public enum M3ChipSelectionMode {
    /// Allows more than one chip to be selected at the same time.
    MULTIPLE,

    /// Allows at most one chip to be selected at the same time.
    SINGLE
}
