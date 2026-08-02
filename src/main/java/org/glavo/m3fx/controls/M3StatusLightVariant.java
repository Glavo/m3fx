// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the semantic meaning conveyed by an [M3StatusLight].
///
/// Each variant selects a theme-aware indicator color. The accompanying text remains the authoritative status
/// description; applications must not rely on the color alone.
@NotNullByDefault
public enum M3StatusLightVariant {
    /// Indicates a neutral state or category.
    NEUTRAL("m3-neutral-status-light"),

    /// Indicates success, approval, or healthy operation.
    POSITIVE("m3-positive-status-light"),

    /// Indicates failure, rejection, or an unhealthy state.
    NEGATIVE("m3-negative-status-light"),

    /// Indicates a warning or state requiring attention.
    NOTICE("m3-notice-status-light"),

    /// Indicates informational status without positive or negative meaning.
    INFO("m3-info-status-light");

    /// The style class selecting this semantic variant.
    private final String styleClass;

    /// Creates a semantic status-light variant.
    ///
    /// @param styleClass the style class selecting the variant
    M3StatusLightVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class selecting this variant.
    ///
    /// @return the variant style class
    String styleClass() {
        return styleClass;
    }
}
