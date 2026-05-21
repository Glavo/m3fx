// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines how an [M3Menu] manages selected menu items.
///
/// Selection modes are useful for menus that represent view options, filters, or persistent preferences. The
/// default [NONE] mode keeps ordinary command menus from changing item selection when an action fires.
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
@NotNullByDefault
public enum M3MenuSelectionMode {
    /// Does not change item selection in response to menu item actions.
    NONE,

    /// Allows at most one menu item to be selected at the same time.
    SINGLE,

    /// Allows more than one menu item to be selected at the same time.
    MULTIPLE
}
