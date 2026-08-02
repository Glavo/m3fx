// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the nominal size of an [M3StatusLight].
///
/// A size role controls the indicator diameter, spacing, and label typography together. Dimensions are expressed
/// in JavaFX logical pixels.
@NotNullByDefault
public enum M3StatusLightSize {
    /// Uses a compact 6-pixel indicator with compact label typography.
    SMALL("m3-small-status-light", 6.0, 6.0),

    /// Uses the default 8-pixel indicator and label typography.
    MEDIUM("m3-medium-status-light", 8.0, 8.0),

    /// Uses a prominent 10-pixel indicator and larger label typography.
    LARGE("m3-large-status-light", 10.0, 10.0),

    /// Uses an extra-prominent 12-pixel indicator and label typography.
    EXTRA_LARGE("m3-extra-large-status-light", 12.0, 12.0);

    /// The style class selecting this size role.
    private final String styleClass;

    /// The indicator diameter in logical pixels.
    private final double indicatorSize;

    /// The spacing between the indicator and label in logical pixels.
    private final double spacing;

    /// Creates a status-light size role.
    ///
    /// @param styleClass the style class selecting the size
    /// @param indicatorSize the indicator diameter in logical pixels
    /// @param spacing the indicator-to-label spacing in logical pixels
    M3StatusLightSize(String styleClass, double indicatorSize, double spacing) {
        this.styleClass = styleClass;
        this.indicatorSize = indicatorSize;
        this.spacing = spacing;
    }

    /// Returns the indicator diameter.
    ///
    /// @return the indicator diameter in logical pixels
    public double getIndicatorSize() {
        return indicatorSize;
    }

    /// Returns the spacing between the indicator and label.
    ///
    /// @return the indicator-to-label spacing in logical pixels
    public double getSpacing() {
        return spacing;
    }

    /// Returns the style class selecting this size.
    ///
    /// @return the size style class
    String styleClass() {
        return styleClass;
    }
}
