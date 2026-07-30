// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Captures the restorable controls and vertical position of one Catalog browser route.
///
/// @param query the unmodified search query
/// @param filterIndex the selected filter index, from `0` through `2`
/// @param scrollPosition the vertical scroll position, from `0.0` through `1.0`
@NotNullByDefault
record CatalogBrowserState(String query, int filterIndex, double scrollPosition) {
    /// The initial state used before a browser route has been visited.
    static final CatalogBrowserState INITIAL = new CatalogBrowserState("", 0, 0.0);

    /// Validates and stores a browser-state snapshot.
    CatalogBrowserState {
        Objects.requireNonNull(query, "query");
        if (filterIndex < 0 || filterIndex > 2) {
            throw new IllegalArgumentException("filterIndex must be between 0 and 2");
        }
        if (!Double.isFinite(scrollPosition) || scrollPosition < 0.0 || scrollPosition > 1.0) {
            throw new IllegalArgumentException("scrollPosition must be between 0.0 and 1.0");
        }
    }
}
