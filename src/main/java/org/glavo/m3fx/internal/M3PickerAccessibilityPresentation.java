// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Provides indexed access to the currently rendered accessible items of a picker skin.
///
/// This contract separates a picker's public accessibility behavior from the concrete skin that renders its
/// selectable items. Implementations expose only nodes that currently participate in the skin's presentation.
/// Reusable nodes may represent different model values after the skin refreshes.
///
/// @implSpec Implementations must return the same node instances used by the skin and must not construct a
/// scene-graph snapshot for each query. The item count and index mapping must describe the presentation that exists
/// when the method is called.
@NotNullByDefault
public interface M3PickerAccessibilityPresentation {
    /// Returns the number of selectable nodes currently exposed by the skin.
    ///
    /// @return the number of indexed accessible items; never negative
    int accessibleItemCount();

    /// Returns the selectable node at the specified presentation index.
    ///
    /// @param index the zero-based presentation index
    /// @return the rendered node, or `null` if `index` is outside the current presentation
    @Nullable Node accessibleItemAt(int index);
}
