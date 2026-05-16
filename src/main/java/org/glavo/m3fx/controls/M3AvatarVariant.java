// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 avatar color variant.
@NotNullByDefault
public enum M3AvatarVariant {
    /// Uses the primary container color pair.
    PRIMARY("m3-primary-avatar"),

    /// Uses the secondary container color pair.
    SECONDARY("m3-secondary-avatar"),

    /// Uses the tertiary container color pair.
    TERTIARY("m3-tertiary-avatar"),

    /// Uses the surface container color pair.
    SURFACE("m3-surface-avatar");

    /// The style class applied for this avatar variant.
    private final String styleClass;

    /// Creates an avatar variant.
    M3AvatarVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class for this avatar variant.
    public String getStyleClass() {
        return styleClass;
    }
}
