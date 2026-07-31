// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A JavaFX scroll pane with Material styling and smooth wheel motion enabled by default.
///
/// This control retains the complete [ScrollPane] content, viewport, layout, and scrolling API. Construction applies
/// the M3FX scroll stylesheet and installs the smooth wheel behavior provided by [M3ScrollPanes]. Fit policies,
/// scrollbar policies, pannability, focus traversal, and content sizing retain their inherited JavaFX defaults.
///
/// Use [M3ScrollPanes] to apply the same behavior to an existing standard [ScrollPane], style a standalone scroll
/// bar, or temporarily disable smooth scrolling for this control.
@NotNullByDefault
public final class M3ScrollPane extends ScrollPane {
    /// Creates an empty Material scroll pane with smooth wheel motion enabled.
    public M3ScrollPane() {
        initialize();
    }

    /// Creates a Material scroll pane containing the supplied node with smooth wheel motion enabled.
    ///
    /// @param content the initial content, or `null` for no content
    public M3ScrollPane(@Nullable Node content) {
        super(content);
        initialize();
    }

    /// Applies the Material presentation and input behavior shared with standard JavaFX scroll panes.
    private void initialize() {
        M3ScrollPanes.style(this);
        M3ScrollPanes.enableSmoothScrolling(this);
    }
}
