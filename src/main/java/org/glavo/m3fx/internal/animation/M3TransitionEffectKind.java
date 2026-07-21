// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the mutually exclusive visual channels represented by one transition effect.
@NotNullByDefault
public enum M3TransitionEffectKind {
    /// Holder opacity.
    FADE,

    /// Uniform holder scale.
    SCALE,

    /// Holder translation along one logical edge.
    SLIDE
}
