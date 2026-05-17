// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

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

    /// Installs a Material Design 3 tooltip with the supplied text on a node.
    public static M3Tooltip install(Node node, String text) {
        M3Tooltip tooltip = new M3Tooltip(text);
        install(node, tooltip);
        return tooltip;
    }

    /// Installs a Material Design 3 tooltip on a node.
    public static void install(Node node, M3Tooltip tooltip) {
        Tooltip.install(
                Objects.requireNonNull(node, "node"),
                Objects.requireNonNull(tooltip, "tooltip")
        );
    }

    /// Uninstalls a Material Design 3 tooltip from a node.
    public static void uninstall(Node node, M3Tooltip tooltip) {
        Tooltip.uninstall(
                Objects.requireNonNull(node, "node"),
                Objects.requireNonNull(tooltip, "tooltip")
        );
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
