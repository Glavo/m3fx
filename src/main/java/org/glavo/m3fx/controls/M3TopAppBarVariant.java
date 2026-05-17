// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the visual arrangement used by an [M3TopAppBar].
@NotNullByDefault
public enum M3TopAppBarVariant {
    /// Uses the baseline small top app bar layout.
    SMALL("m3-top-app-bar-small"),

    /// Uses a small top app bar layout with centered title text.
    CENTER_ALIGNED("m3-top-app-bar-center-aligned"),

    /// Uses the medium top app bar container height.
    MEDIUM("m3-top-app-bar-medium"),

    /// Uses the large top app bar container height.
    LARGE("m3-top-app-bar-large");

    /// The style class associated with this variant.
    private final String styleClass;

    /// Creates a top app bar variant.
    M3TopAppBarVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class associated with this variant.
    public String getStyleClass() {
        return styleClass;
    }
}
