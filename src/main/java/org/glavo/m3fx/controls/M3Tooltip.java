// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 tooltip.
@NotNullByDefault
public class M3Tooltip extends Tooltip {
    /// The base style class for M3FX tooltips.
    public static final String STYLE_CLASS = "m3-tooltip";

    /// Creates an empty tooltip.
    public M3Tooltip() {
        initialize();
    }

    /// Creates a tooltip with text.
    public M3Tooltip(String text) {
        super(text);
        initialize();
    }

    /// Adds base style classes and Material timing defaults.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setWrapText(true);
        setShowDelay(Duration.millis(500.0));
        setHideDelay(Duration.millis(0.0));
        setShowDuration(Duration.seconds(5.0));
    }
}
