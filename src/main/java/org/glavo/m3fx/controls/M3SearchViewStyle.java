// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the visual treatment of a [M3SearchView].
///
/// A contained search view separates its search bar and result surface from a lower-emphasis outer container.
/// A divided search view uses one continuous surface and separates its header from the result content with a
/// divider. The selected style is independent of [M3SearchViewLayout], so either treatment can be presented as a
/// docked view or a full-screen view.
///
/// See [Material Design search specifications](https://m3.material.io/components/search/specs).
@NotNullByDefault
public enum M3SearchViewStyle {
    /// Uses separate rounded search-bar and result surfaces inside a containing surface.
    CONTAINED,

    /// Uses a continuous surface with a divider below the search header.
    DIVIDED
}
