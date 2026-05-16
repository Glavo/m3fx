// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3SegmentedButtonGroup] manages selected segments.
@NotNullByDefault
public enum M3SegmentedButtonSelectionMode {
    /// Allows at most one segment to be selected at the same time.
    SINGLE,

    /// Allows more than one segment to be selected at the same time.
    MULTIPLE
}
