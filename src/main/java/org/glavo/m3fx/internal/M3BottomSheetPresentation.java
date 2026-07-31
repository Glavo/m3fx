// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Provides presentation-level nodes required by [org.glavo.m3fx.controls.M3BottomSheet] accessibility behavior.
@NotNullByDefault
public interface M3BottomSheetPresentation {
    /// Returns the actionable drag-handle target.
    ///
    /// @return the reachable drag-handle target, or `null` when it is not actionable
    @Nullable Node dragHandleFocusTarget();
}
