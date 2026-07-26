// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.M3ColorMath;
import org.jetbrains.annotations.NotNullByDefault;

/// Represents an immutable color in one of the color spaces supported by M3FX.
///
/// Each value retains its represented [color space][M3ColorSpace] and all channels of that space. Consequently,
/// an HSL or HSB value may retain a hue that does not affect its rendered color while saturation is zero or while
/// lightness or brightness is at an achromatic boundary. Direct conversion between HSL and HSB preserves that
/// latent hue. RGB has no hue channel, so converting an achromatic RGB value to HSL or HSB assigns a hue of zero
/// degrees.
///
/// Implementations reject non-finite or out-of-range channels when values are created. Color-space conversion
/// preserves alpha and the rendered RGBA color, subject to floating-point precision. Use [#toFxColor()] when a
/// JavaFX [Color] is required.
///
/// Record equality compares the represented color space and its exact channel values. Use
/// [#isEquivalentTo(M3Color)] when equality of the canonical rendered RGBA value is required across color spaces.
@NotNullByDefault
public sealed interface M3Color permits M3RgbColor, M3HslColor, M3HsbColor {
    /// Returns the color space whose channels are stored by this value.
    ///
    /// @return the non-null represented color space
    M3ColorSpace getColorSpace();

    /// Returns the alpha channel.
    ///
    /// @return the finite alpha value in the closed range `0.0` through `1.0`
    double getAlpha();

    /// Returns the value of a channel represented by this color.
    ///
    /// [M3ColorChannel#ALPHA] is available in every color space. Other channels must belong to
    /// [#getColorSpace()]. The returned value uses the unit and closed range declared by the requested channel.
    ///
    /// @param channel the non-null channel to read
    /// @return the value of `channel`
    /// @throws NullPointerException if `channel` is `null`
    /// @throws IllegalArgumentException if `channel` is not available in [#getColorSpace()]
    double getChannel(M3ColorChannel channel);

    /// Returns a color in the same color space with one channel replaced.
    ///
    /// All channels other than `channel` are preserved. The replacement must be finite and within the closed range
    /// declared by `channel`.
    ///
    /// @param channel the non-null channel to replace
    /// @param value   the replacement in the unit used by `channel`
    /// @return an immutable color in [#getColorSpace()] with the replacement applied
    /// @throws NullPointerException if `channel` is `null`
    /// @throws IllegalArgumentException if `channel` is not available in [#getColorSpace()], or if `value` is
    ///                                  non-finite or outside the channel's range
    M3Color withChannel(M3ColorChannel channel, double value);

    /// Converts this value to another supported color space.
    ///
    /// The conversion preserves alpha and the rendered RGBA color, subject to floating-point precision. Direct
    /// conversion between HSL and HSB preserves hue even when the rendered color is achromatic. Conversion from an
    /// achromatic RGB value assigns a hue of zero degrees because RGB does not represent a latent hue. This method
    /// may return `this` when `colorSpace` is [#getColorSpace()].
    ///
    /// @param colorSpace the non-null target color space
    /// @return an immutable color represented in `colorSpace`
    /// @throws NullPointerException if `colorSpace` is `null`
    M3Color toColorSpace(M3ColorSpace colorSpace);

    /// Returns the rendered RGBA value as a JavaFX color.
    ///
    /// The returned value does not retain this value's editing color space or any latent channel state.
    ///
    /// @return a non-null JavaFX color with equivalent rendered channels
    Color toFxColor();

    /// Returns whether another value has the same canonical 16-bit RGBA representation.
    ///
    /// Color-space identity and latent channel state are intentionally ignored. Each RGB and alpha channel is
    /// rounded to the nearest unsigned 16-bit sample before comparison, making the relation reflexive, symmetric,
    /// and transitive. This canonical representation is used for swatch and preset uniqueness; it does not describe
    /// platform-dependent rasterization or display color management.
    ///
    /// This relation is distinct from the structural equality implemented by the color records.
    ///
    /// @param other the non-null color to compare
    /// @return `true` when the canonical 16-bit RGBA representations are equal
    /// @throws NullPointerException if `other` is `null`
    default boolean isEquivalentTo(M3Color other) {
        return M3ColorMath.canonicalRgbaKey(this) == M3ColorMath.canonicalRgbaKey(other);
    }
}
