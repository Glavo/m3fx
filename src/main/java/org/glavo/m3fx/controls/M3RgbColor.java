// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.M3ColorMath;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Represents an immutable color using red, green, blue, and alpha channels.
///
/// All components are finite normalized values in the closed range `0.0` through `1.0`. Record equality compares
/// the exact component values. Use [M3Color#isEquivalentTo(M3Color)] to compare canonical rendered RGBA values
/// across color spaces.
///
/// @param red   the finite red channel in the closed range `0.0` through `1.0`
/// @param green the finite green channel in the closed range `0.0` through `1.0`
/// @param blue  the finite blue channel in the closed range `0.0` through `1.0`
/// @param alpha the finite alpha channel in the closed range `0.0` through `1.0`
@NotNullByDefault
public record M3RgbColor(double red, double green, double blue, double alpha) implements M3Color {
    /// Creates an RGB color with an explicit alpha channel.
    ///
    /// @throws IllegalArgumentException if any component is non-finite or outside the closed range `0.0` through
    ///                                  `1.0`
    public M3RgbColor {
        M3ColorMath.requireUnit(red, "red");
        M3ColorMath.requireUnit(green, "green");
        M3ColorMath.requireUnit(blue, "blue");
        M3ColorMath.requireUnit(alpha, "alpha");
    }

    /// Creates an opaque RGB color.
    ///
    /// @param red   the finite red channel in the closed range `0.0` through `1.0`
    /// @param green the finite green channel in the closed range `0.0` through `1.0`
    /// @param blue  the finite blue channel in the closed range `0.0` through `1.0`
    /// @throws IllegalArgumentException if any component is non-finite or outside the closed range `0.0` through
    ///                                  `1.0`
    public M3RgbColor(double red, double green, double blue) {
        this(red, green, blue, 1.0);
    }

    /// Creates an RGB value by copying the channels of a JavaFX color.
    ///
    /// @param color the non-null JavaFX color whose red, green, blue, and opacity channels are copied
    /// @throws NullPointerException if `color` is `null`
    public M3RgbColor(Color color) {
        this(
                Objects.requireNonNull(color, "color").getRed(),
                color.getGreen(),
                color.getBlue(),
                color.getOpacity()
        );
    }

    /// Returns [M3ColorSpace#RGB].
    ///
    /// @return the RGB color space
    @Override
    public M3ColorSpace getColorSpace() {
        return M3ColorSpace.RGB;
    }

    /// Returns the alpha component.
    ///
    /// @return the finite alpha channel in the closed range `0.0` through `1.0`
    @Override
    public double getAlpha() {
        return alpha;
    }

    /// Returns one RGB or alpha channel.
    ///
    /// @param channel the non-null channel to read
    /// @return the finite normalized channel value
    /// @throws NullPointerException if `channel` is `null`
    /// @throws IllegalArgumentException if `channel` is not an RGB or alpha channel
    @Override
    public double getChannel(M3ColorChannel channel) {
        Objects.requireNonNull(channel, "channel");
        return switch (channel) {
            case RED -> red;
            case GREEN -> green;
            case BLUE -> blue;
            case ALPHA -> alpha;
            default -> throw new IllegalArgumentException("RGB does not contain channel " + channel);
        };
    }

    /// Returns an RGB value with one channel replaced.
    ///
    /// The replacement must be finite and in the closed range `0.0` through `1.0`. Other components are preserved.
    ///
    /// @param channel the non-null RGB or alpha channel to replace
    /// @param value   the finite normalized replacement
    /// @return the updated RGB color
    /// @throws NullPointerException if `channel` is `null`
    /// @throws IllegalArgumentException if `channel` is not an RGB or alpha channel, or if `value` is non-finite
    ///                                  or outside the closed range `0.0` through `1.0`
    @Override
    public M3RgbColor withChannel(M3ColorChannel channel, double value) {
        Objects.requireNonNull(channel, "channel");
        return switch (channel) {
            case RED -> new M3RgbColor(value, green, blue, alpha);
            case GREEN -> new M3RgbColor(red, value, blue, alpha);
            case BLUE -> new M3RgbColor(red, green, value, alpha);
            case ALPHA -> new M3RgbColor(red, green, blue, value);
            default -> throw new IllegalArgumentException("RGB does not contain channel " + channel);
        };
    }

    /// Converts this value to the requested color space.
    ///
    /// Converting an achromatic RGB value to HSL or HSB assigns a hue of zero degrees.
    ///
    /// @param colorSpace the target color space
    /// @return this value when `colorSpace` is [M3ColorSpace#RGB], otherwise an equivalent immutable color in the
    ///         requested space
    /// @throws NullPointerException if `colorSpace` is `null`
    @Override
    public M3Color toColorSpace(M3ColorSpace colorSpace) {
        Objects.requireNonNull(colorSpace, "colorSpace");
        return switch (colorSpace) {
            case RGB -> this;
            case HSL -> M3ColorMath.toHsl(this);
            case HSB -> M3ColorMath.toHsb(this);
        };
    }

    /// Returns a JavaFX color with the same channel values.
    ///
    /// @return the equivalent JavaFX color
    @Override
    public Color toFxColor() {
        return new Color(red, green, blue, alpha);
    }
}
