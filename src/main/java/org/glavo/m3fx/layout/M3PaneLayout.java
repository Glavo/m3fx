// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import org.jetbrains.annotations.NotNullByDefault;

/// Selects how an [M3AdaptiveScaffold] arranges its content panes.
///
/// Explicit layouts remain selected when the scaffold crosses a breakpoint. A two-pane request collapses to
/// [#SINGLE] while either required slot is empty. A [#THREE_PANE] request with one empty side slot collapses to the
/// corresponding fixed two-pane layout, and otherwise collapses to [#SINGLE]. [#ADAPTIVE] follows Material's
/// recommended pane total: one pane at compact and medium widths and two panes at expanded, large, and extra-large
/// widths. Three panes are never selected automatically because Material treats that arrangement as an optional
/// extra-large configuration rather than the default.
///
/// See [Material Design panes](https://m3.material.io/foundations/layout/scaffold/panes).
@NotNullByDefault
public enum M3PaneLayout {
    /// Selects one or two panes from the current breakpoint and installed pane slots.
    ADAPTIVE,

    /// Shows one flexible pane selected by [M3AdaptiveScaffold#activePaneProperty()].
    SINGLE,

    /// Shows the leading and main panes as flexible panes separated at the configured split position.
    SPLIT_LEADING,

    /// Shows the main and trailing panes as flexible panes separated at the configured split position.
    SPLIT_TRAILING,

    /// Shows a fixed-width leading pane and a flexible main pane.
    FIXED_LEADING,

    /// Shows a flexible main pane and a fixed-width trailing pane.
    FIXED_TRAILING,

    /// Shows the leading, main, and trailing panes; the main pane remains flexible.
    THREE_PANE
}
