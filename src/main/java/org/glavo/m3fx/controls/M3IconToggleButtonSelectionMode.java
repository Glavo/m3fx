// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3IconToggleButtonGroup] manages selected icon toggle buttons.
///
/// Use [SINGLE] for mutually exclusive icon choices and [MULTIPLE] for independent icon toggles that should
/// remain visually grouped.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/overview).
@NotNullByDefault
public enum M3IconToggleButtonSelectionMode {
    /// Allows at most one icon toggle button to be selected at the same time.
    SINGLE,

    /// Allows more than one icon toggle button to be selected at the same time.
    MULTIPLE
}
