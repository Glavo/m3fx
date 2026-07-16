// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Specifies the visual and hierarchical role of an [M3TabBar].
///
/// Both variants use the same selection, keyboard, and accessibility behavior. Primary tabs select the principal
/// peer views in a content region. Secondary tabs provide another level of organization below primary tabs and use
/// a simpler full-width active indicator.
///
/// See [Material Design tabs](https://m3.material.io/components/tabs/overview).
@NotNullByDefault
public enum M3TabBarVariant {
    /// A primary tab bar for the principal peer views in a content region.
    PRIMARY,

    /// A secondary tab bar placed below primary tabs to divide related content further.
    SECONDARY
}
