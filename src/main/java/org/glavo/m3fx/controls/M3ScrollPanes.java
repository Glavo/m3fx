// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Applies M3FX Material scroll styling to JavaFX scroll controls.
@NotNullByDefault
public final class M3ScrollPanes {
    /// The style class that enables Material styling for a JavaFX [ScrollPane].
    public static final String STYLE_CLASS = "m3-scroll-pane";

    /// The style class that enables Material styling for a standalone JavaFX [ScrollBar].
    public static final String SCROLL_BAR_STYLE_CLASS = "m3-scroll-bar";

    /// Prevents utility class instantiation.
    private M3ScrollPanes() {
    }

    /// Adds the Material scroll style class to a JavaFX scroll pane.
    public static void style(ScrollPane scrollPane) {
        M3ControlStyles.add(Objects.requireNonNull(scrollPane, "scrollPane"), STYLE_CLASS);
    }

    /// Adds the Material scroll style class to a standalone JavaFX scroll bar.
    public static void style(ScrollBar scrollBar) {
        M3ControlStyles.add(Objects.requireNonNull(scrollBar, "scrollBar"), SCROLL_BAR_STYLE_CLASS);
    }
}
