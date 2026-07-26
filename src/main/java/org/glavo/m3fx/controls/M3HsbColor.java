// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.M3ColorMath;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Represents an immutable color using hue, saturation, brightness, and alpha channels.
///
/// Hue is measured in degrees in the closed range `0.0` through `360.0`; zero and 360 degrees render identically
/// but remain distinct record component values. The other components are finite normalized values in the closed
/// range `0.0` through `1.0`. Hue is retained when saturation or brightness is zero. Record equality compares the
/// exact component values. Use [M3Color#isEquivalentTo(M3Color)] to compare canonical rendered RGBA values across
/// color spaces.
///
/// @param hue        the finite hue in degrees in the closed range `0.0` through `360.0`
/// @param saturation the finite saturation in the closed range `0.0` through `1.0`
/// @param brightness the finite brightness in the closed range `0.0` through `1.0`
/// @param alpha      the finite alpha channel in the closed range `0.0` through `1.0`
@NotNullByDefault
public record M3HsbColor(double hue, double saturation, double brightness, double alpha) implements M3Color {
    /// Creates an HSB color with an explicit alpha channel.
    ///
    /// @throws IllegalArgumentException if `hue` is non-finite or outside `0.0` through `360.0`, or if another
    ///                                  component is non-finite or outside `0.0` through `1.0`
    public M3HsbColor {
        M3ColorMath.requireHue(hue, "hue");
        M3ColorMath.requireUnit(saturation, "saturation");
        M3ColorMath.requireUnit(brightness, "brightness");
        M3ColorMath.requireUnit(alpha, "alpha");
    }

    /// Creates an opaque HSB color.
    ///
    /// @param hue        the finite hue in degrees in the closed range `0.0` through `360.0`
    /// @param saturation the finite saturation in the closed range `0.0` through `1.0`
    /// @param brightness the finite brightness in the closed range `0.0` through `1.0`
    /// @throws IllegalArgumentException if `hue` is non-finite or outside `0.0` through `360.0`, or if another
    ///                                  component is non-finite or outside `0.0` through `1.0`
    public M3HsbColor(double hue, double saturation, double brightness) {
        this(hue, saturation, brightness, 1.0);
    }

    /// Returns [M3ColorSpace#HSB].
    ///
    /// @return the HSB color space
    @Override
    public M3ColorSpace getColorSpace() {
        return M3ColorSpace.HSB;
    }

    /// Returns the alpha component.
    ///
    /// @return the finite alpha channel in the closed range `0.0` through `1.0`
    @Override
    public double getAlpha() {
        return alpha;
    }

    /// Returns one HSB or alpha channel.
    ///
    /// @param channel the non-null channel to read
    /// @return the channel value in the unit and range declared by `channel`
    /// @throws NullPointerException if `channel` is `null`
    /// @throws IllegalArgumentException if `channel` is not an HSB or alpha channel
    @Override
    public double getChannel(M3ColorChannel channel) {
        Objects.requireNonNull(channel, "channel");
        return switch (channel) {
            case HUE -> hue;
            case SATURATION -> saturation;
            case BRIGHTNESS -> brightness;
            case ALPHA -> alpha;
            default -> throw new IllegalArgumentException("HSB does not contain channel " + channel);
        };
    }

    /// Returns an HSB value with one channel replaced.
    ///
    /// Other components, including a latent hue that does not currently affect rendering, are preserved.
    ///
    /// @param channel the non-null HSB or alpha channel to replace
    /// @param value   the replacement in the unit used by `channel`
    /// @return the updated HSB color
    /// @throws NullPointerException if `channel` is `null`
    /// @throws IllegalArgumentException if `channel` is not an HSB or alpha channel, or if `value` is non-finite
    ///                                  or outside the channel's closed range
    @Override
    public M3HsbColor withChannel(M3ColorChannel channel, double value) {
        Objects.requireNonNull(channel, "channel");
        return switch (channel) {
            case HUE -> new M3HsbColor(value, saturation, brightness, alpha);
            case SATURATION -> new M3HsbColor(hue, value, brightness, alpha);
            case BRIGHTNESS -> new M3HsbColor(hue, saturation, value, alpha);
            case ALPHA -> new M3HsbColor(hue, saturation, brightness, value);
            default -> throw new IllegalArgumentException("HSB does not contain channel " + channel);
        };
    }

    /// Converts this value to the requested color space.
    ///
    /// @param colorSpace the target color space
    /// @return this value when `colorSpace` is [M3ColorSpace#HSB], otherwise an equivalent immutable color in the
    ///         requested space
    /// @throws NullPointerException if `colorSpace` is `null`
    @Override
    public M3Color toColorSpace(M3ColorSpace colorSpace) {
        Objects.requireNonNull(colorSpace, "colorSpace");
        return switch (colorSpace) {
            case RGB -> M3ColorMath.toRgb(this);
            case HSL -> M3ColorMath.toHsl(this);
            case HSB -> this;
        };
    }

    /// Returns the rendered RGBA value as a JavaFX color.
    ///
    /// @return the equivalent JavaFX color
    @Override
    public Color toFxColor() {
        return M3ColorMath.toRgb(this).toFxColor();
    }
}
