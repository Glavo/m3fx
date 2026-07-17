// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an M3FX selection container manages selected items.
///
/// A container may support all modes or restrict the modes that are meaningful for its component contract. Changing
/// the mode causes the container to immediately enforce the corresponding selection invariant.
///
/// See [Material Design interaction states](https://m3.material.io/foundations/interaction/states/overview).
@NotNullByDefault
public enum M3SelectionMode {
    /// Disables selection and clears any existing selection.
    NONE,

    /// Allows at most one item to be selected.
    SINGLE,

    /// Allows any number of items to be selected.
    MULTIPLE
}
