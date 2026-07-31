// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.glavo.m3fx.controls.M3ListItem;
import org.jetbrains.annotations.NotNullByDefault;

/// Provides presentation-level operations required by [org.glavo.m3fx.controls.M3NavigationDrawer].
///
/// This contract keeps drawer behavior independent of a concrete skin implementation.
@NotNullByDefault
public interface M3NavigationDrawerPresentation {
    /// Reveals a destination without changing its selection or focus state.
    ///
    /// @param item the destination to reveal
    void revealItem(M3ListItem item);
}
