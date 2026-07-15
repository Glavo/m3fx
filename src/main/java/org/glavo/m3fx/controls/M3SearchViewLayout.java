// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the window-relative layout used by a [M3SearchView].
///
/// Docked search views remain bounded surfaces intended for larger windows. Full-screen search views occupy the
/// available parent area and remove the outer container shape. Applications remain responsible for placing and
/// sizing the control in an appropriate adaptive layout.
///
/// See [Material Design search specifications](https://m3.material.io/components/search/specs).
@NotNullByDefault
public enum M3SearchViewLayout {
    /// Presents search as a bounded surface with the specified docked size constraints.
    DOCKED,

    /// Presents search as an edge-to-edge surface within the available parent area.
    FULL_SCREEN
}
