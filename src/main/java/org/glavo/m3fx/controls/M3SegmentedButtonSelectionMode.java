// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3SegmentedButtonGroup] manages selected segments.
///
/// Use [SINGLE] when the group represents one choice from a set, and [MULTIPLE] when each segment acts as an
/// independent toggle in the same visual group.
///
/// See [Material Design segmented buttons](https://m3.material.io/components/segmented-buttons/overview).
@NotNullByDefault
public enum M3SegmentedButtonSelectionMode {
    /// Allows at most one segment to be selected at the same time.
    SINGLE,

    /// Allows more than one segment to be selected at the same time.
    MULTIPLE
}
