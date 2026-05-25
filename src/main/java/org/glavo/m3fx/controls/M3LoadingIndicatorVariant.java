// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the visual variant of [M3LoadingIndicator].
///
/// Material Design 3 Expressive defines a default loading indicator and a contained loading indicator. See
/// [Material Design loading indicators](https://m3.material.io/components/loading-indicator/overview).
@NotNullByDefault
public enum M3LoadingIndicatorVariant {
    /// Displays only the active morphing indicator shape.
    DEFAULT,

    /// Displays the active morphing indicator shape inside a colored container.
    CONTAINED
}
