// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Provides presentation-level nodes required by [org.glavo.m3fx.controls.M3TextInputLayout] behavior.
@NotNullByDefault
public interface M3TextInputLayoutPresentation {
    /// Returns the visible built-in clear button.
    ///
    /// @return the clear button, or `null` while it is not presented
    @Nullable Node clearButton();
}
