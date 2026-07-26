// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a channel of an [M3Color] and its numeric domain.
///
/// Hue is measured in degrees in the closed range `0.0` through `360.0`. Every other channel is normalized to the
/// closed range `0.0` through `1.0`. The unit and block increments are the default amounts used by color controls
/// for fine and coarse keyboard adjustment; they do not quantize channel values.
@NotNullByDefault
public enum M3ColorChannel {
    /// The normalized red channel of an [M3RgbColor], in the closed range `0.0` through `1.0`.
    RED(0.0, 1.0, 1.0 / 255.0, 0.1),

    /// The normalized green channel of an [M3RgbColor], in the closed range `0.0` through `1.0`.
    GREEN(0.0, 1.0, 1.0 / 255.0, 0.1),

    /// The normalized blue channel of an [M3RgbColor], in the closed range `0.0` through `1.0`.
    BLUE(0.0, 1.0, 1.0 / 255.0, 0.1),

    /// The hue channel, measured in degrees in the closed range `0.0` through `360.0`.
    HUE(0.0, 360.0, 1.0, 10.0),

    /// The normalized saturation channel of an [M3HslColor] or [M3HsbColor], in the closed range `0.0` through
    /// `1.0`.
    SATURATION(0.0, 1.0, 0.01, 0.1),

    /// The normalized lightness channel of an [M3HslColor], in the closed range `0.0` through `1.0`.
    LIGHTNESS(0.0, 1.0, 0.01, 0.1),

    /// The normalized brightness channel of an [M3HsbColor], in the closed range `0.0` through `1.0`.
    BRIGHTNESS(0.0, 1.0, 0.01, 0.1),

    /// The normalized alpha channel supported by every M3FX color space, in the closed range `0.0` through `1.0`.
    ALPHA(0.0, 1.0, 1.0 / 255.0, 0.1);

    /// The inclusive minimum channel value.
    private final double minimum;

    /// The inclusive maximum channel value.
    private final double maximum;

    /// The default increment for arrow-key adjustment.
    private final double unitIncrement;

    /// The default increment for page-key adjustment.
    private final double blockIncrement;

    /// Creates a channel descriptor.
    ///
    /// @param minimum       the inclusive minimum value
    /// @param maximum       the inclusive maximum value
    /// @param unitIncrement the default arrow-key increment
    /// @param blockIncrement the default page-key increment
    M3ColorChannel(double minimum, double maximum, double unitIncrement, double blockIncrement) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.unitIncrement = unitIncrement;
        this.blockIncrement = blockIncrement;
    }

    /// Returns the minimum valid value of this channel.
    ///
    /// @return the inclusive minimum in this channel's unit
    public double getMinimum() {
        return minimum;
    }

    /// Returns the maximum valid value of this channel.
    ///
    /// @return the inclusive maximum in this channel's unit
    public double getMaximum() {
        return maximum;
    }

    /// Returns the default fine-adjustment increment for this channel.
    ///
    /// @return the positive unit increment in this channel's unit
    public double getUnitIncrement() {
        return unitIncrement;
    }

    /// Returns the default coarse-adjustment increment for this channel.
    ///
    /// @return the positive block increment in this channel's unit
    public double getBlockIncrement() {
        return blockIncrement;
    }

    /// Constrains a finite value to this channel's closed range.
    ///
    /// A value already within the range is returned unchanged. A smaller or larger value is replaced by
    /// [#getMinimum()] or [#getMaximum()], respectively.
    ///
    /// @param value the value to constrain
    /// @return `value` constrained to the closed range from [#getMinimum()] through [#getMaximum()]
    /// @throws IllegalArgumentException if `value` is not finite
    public double constrain(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be finite: " + value);
        }
        return Math.max(minimum, Math.min(maximum, value));
    }

    /// Converts a channel value to a normalized position.
    ///
    /// Values outside the channel range are constrained before applying a linear mapping. The minimum maps to
    /// `0.0` and the maximum maps to `1.0`.
    ///
    /// @param value the finite channel value
    /// @return the finite normalized position in the closed range `0.0` through `1.0`
    /// @throws IllegalArgumentException if `value` is not finite
    public double toPosition(double value) {
        return (constrain(value) - minimum) / (maximum - minimum);
    }

    /// Converts a normalized position to a channel value.
    ///
    /// Positions outside the closed range `0.0` through `1.0` are constrained before applying a linear mapping.
    ///
    /// @param position the finite normalized position
    /// @return the corresponding value in this channel's closed range
    /// @throws IllegalArgumentException if `position` is not finite
    public double fromPosition(double position) {
        if (!Double.isFinite(position)) {
            throw new IllegalArgumentException("position must be finite: " + position);
        }
        double constrainedPosition = Math.max(0.0, Math.min(1.0, position));
        return minimum + (maximum - minimum) * constrainedPosition;
    }
}
