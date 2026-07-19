// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a logical content-pane position in an [M3AdaptiveScaffold].
///
/// Leading and trailing are resolved from the scaffold's effective node orientation. They therefore map to left
/// and right respectively in a left-to-right layout and to right and left in a right-to-left layout.
///
/// See [Material Design panes](https://m3.material.io/foundations/layout/scaffold/panes).
@NotNullByDefault
public enum M3PaneRole {
    /// The pane at the logical leading edge, commonly used for a list or other peer content.
    LEADING,

    /// The principal flexible pane.
    MAIN,

    /// The pane at the logical trailing edge, commonly used for supporting content or a side sheet.
    TRAILING
}
