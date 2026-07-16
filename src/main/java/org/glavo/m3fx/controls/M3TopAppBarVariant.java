// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the visual arrangement used by an [M3TopAppBar].
///
/// Top app bar variants choose the title placement, typography, and height metrics for different page hierarchy
/// levels. The flexible variants support subtitles, two-line headlines, custom title content, and transformation
/// into the small arrangement when content scrolls beneath the app bar. Baseline medium and large variants remain
/// available for applications that have not adopted the Expressive replacements.
///
/// See [Material Design app bars](https://m3.material.io/components/app-bars/overview).
@NotNullByDefault
public enum M3TopAppBarVariant {
    /// Uses the baseline small top app bar layout.
    SMALL("m3-top-app-bar-small"),

    /// Uses a small top app bar layout with centered title text.
    CENTER_ALIGNED("m3-top-app-bar-center-aligned"),

    /// Uses the medium top app bar container height.
    MEDIUM("m3-top-app-bar-medium"),

    /// Uses the large top app bar container height.
    LARGE("m3-top-app-bar-large"),

    /// Uses the Material Expressive medium flexible arrangement.
    MEDIUM_FLEXIBLE("m3-top-app-bar-medium-flexible"),

    /// Uses the Material Expressive large flexible arrangement.
    LARGE_FLEXIBLE("m3-top-app-bar-large-flexible");

    /// The style class associated with this variant.
    private final String styleClass;

    /// Creates a top app bar variant.
    M3TopAppBarVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class associated with this variant.
    ///
    /// @return the style class applied by this variant
    String styleClass() {
        return styleClass;
    }
}
