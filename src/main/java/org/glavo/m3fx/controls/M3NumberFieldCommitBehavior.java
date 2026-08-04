// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3NumberField] handles a parsed value that is outside its range or between step values.
@NotNullByDefault
public enum M3NumberFieldCommitBehavior {
    /// Clamps parsed values to the configured range and snaps them to the nearest step before committing.
    SNAP,

    /// Rejects parsed values that are outside the configured range or are not aligned to a step.
    VALIDATE
}
