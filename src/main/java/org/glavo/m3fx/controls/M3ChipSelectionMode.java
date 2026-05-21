// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3ChipGroup] manages selected chips.
///
/// Chip groups use the selection mode to represent either a set of independent filters or one selected option
/// from several choices. Selection mode does not change the visual chip variant; configure [M3ChipVariant]
/// separately.
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public enum M3ChipSelectionMode {
    /// Allows more than one chip to be selected at the same time.
    MULTIPLE,

    /// Allows at most one chip to be selected at the same time.
    SINGLE
}
