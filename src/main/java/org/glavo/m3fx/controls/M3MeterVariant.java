// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the semantic meaning conveyed by an [M3Meter] fill.
///
/// Each variant selects a theme-aware fill color. Applications must also provide label or value text that
/// communicates the measured state without relying on color alone.
@NotNullByDefault
public enum M3MeterVariant {
    /// Indicates a neutral quantity or achievement without positive or negative meaning.
    INFORMATIVE("m3-informative-meter"),

    /// Indicates a favorable quantity, achievement, or remaining capacity.
    POSITIVE("m3-positive-meter"),

    /// Indicates a quantity that may require attention soon.
    NOTICE("m3-notice-meter"),

    /// Indicates a critical quantity that requires urgent attention.
    NEGATIVE("m3-negative-meter");

    /// The style class selecting this semantic variant.
    private final String styleClass;

    /// Creates a semantic meter variant.
    ///
    /// @param styleClass the style class selecting the variant
    M3MeterVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class selecting this variant.
    ///
    /// @return the variant style class
    String styleClass() {
        return styleClass;
    }
}
