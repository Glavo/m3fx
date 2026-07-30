// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the axis shared by two spatially related content states.
///
/// Horizontal motion follows logical start and end edges, so the rendered direction mirrors automatically for a
/// right-to-left [M3AnimatedContent]. Vertical and depth motion are independent of node orientation.
///
/// See [Material Design shared-axis transitions](https://m3.material.io/styles/motion/transitions/applying-transitions).
@NotNullByDefault
public enum M3TransitionAxis {
    /// Uses logical horizontal translation.
    X,

    /// Uses vertical translation.
    Y,

    /// Uses uniform scale to represent depth.
    Z
}
