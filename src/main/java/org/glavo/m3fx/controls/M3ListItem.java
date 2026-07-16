// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 list item.
///
/// `M3ListItem` represents one row in a Material list or navigation drawer. It supports overline, headline,
/// supporting, and trailing supporting text, leading and trailing slots, one-line through three-line metrics,
/// selection state, action events, and keyboard activation. Container controls such as [M3ListPane], [M3ListView],
/// and [M3NavigationDrawer] can manage groups of list items.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public final class M3ListItem extends M3ListItemBase {
    /// Creates an empty list item.
    public M3ListItem() {
        super();
    }

    /// Creates a one-line list item with headline text.
    ///
    /// @param headlineText the headline text displayed by the list item
    public M3ListItem(String headlineText) {
        super(headlineText);
    }
}
