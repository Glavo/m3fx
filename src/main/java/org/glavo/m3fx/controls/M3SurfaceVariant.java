// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 surface color variant.
@NotNullByDefault
public enum M3SurfaceVariant {
    /// Uses the base surface color.
    SURFACE("m3-surface-variant-surface"),

    /// Uses the lowest surface container color.
    CONTAINER_LOWEST("m3-surface-variant-container-lowest"),

    /// Uses the low surface container color.
    CONTAINER_LOW("m3-surface-variant-container-low"),

    /// Uses the default surface container color.
    CONTAINER("m3-surface-variant-container"),

    /// Uses the high surface container color.
    CONTAINER_HIGH("m3-surface-variant-container-high"),

    /// Uses the highest surface container color.
    CONTAINER_HIGHEST("m3-surface-variant-container-highest"),

    /// Uses the primary container color pair.
    PRIMARY_CONTAINER("m3-surface-variant-primary-container"),

    /// Uses the secondary container color pair.
    SECONDARY_CONTAINER("m3-surface-variant-secondary-container"),

    /// Uses the tertiary container color pair.
    TERTIARY_CONTAINER("m3-surface-variant-tertiary-container");

    /// The style class applied for this surface variant.
    private final String styleClass;

    /// Creates a surface variant.
    M3SurfaceVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class for this surface variant.
    public String getStyleClass() {
        return styleClass;
    }
}
