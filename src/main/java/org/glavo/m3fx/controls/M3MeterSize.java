// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the visual size of an [M3Meter].
@NotNullByDefault
public enum M3MeterSize {
    /// Uses compact typography and a four-pixel track for confined layouts such as cards and tables.
    SMALL("m3-small-meter", 4.0, 6.0),

    /// Uses standard typography and an eight-pixel track.
    LARGE("m3-large-meter", 8.0, 8.0);

    /// The style class selecting this size.
    private final String styleClass;

    /// The track and fill thickness in logical pixels.
    private final double trackThickness;

    /// The gap between the labels and track in logical pixels.
    private final double labelGap;

    /// Creates a meter size role.
    ///
    /// @param styleClass the style class selecting the size
    /// @param trackThickness the track and fill thickness in logical pixels
    /// @param labelGap the gap between the labels and track in logical pixels
    M3MeterSize(String styleClass, double trackThickness, double labelGap) {
        this.styleClass = styleClass;
        this.trackThickness = trackThickness;
        this.labelGap = labelGap;
    }

    /// Returns the track and fill thickness.
    ///
    /// @return the thickness in logical pixels
    public double getTrackThickness() {
        return trackThickness;
    }

    /// Returns the gap between the labels and track.
    ///
    /// @return the gap in logical pixels
    public double getLabelGap() {
        return labelGap;
    }

    /// Returns the style class selecting this size.
    ///
    /// @return the size style class
    String styleClass() {
        return styleClass;
    }
}
