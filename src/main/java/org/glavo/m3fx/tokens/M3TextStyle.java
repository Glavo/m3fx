// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TextStyleImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes one immutable Material Design 3 text style token.
///
/// A style contains a JavaFX font-family name, font size, line height, numeric weight, and letter tracking. Length
/// values use JavaFX logical pixels. The font family may name a family that is not currently installed; JavaFX
/// performs any platform font fallback when the style is rendered. This type describes typography only and does
/// not define text color, alignment, wrapping, or truncation.
///
/// See [Material Design typography](https://m3.material.io/styles/typography/overview).
@NotNullByDefault
public sealed interface M3TextStyle permits M3TextStyleImpl {
    /// Returns the font family name.
    ///
    /// @return the retained JavaFX font family name; never `null`
    String fontFamily();

    /// Returns the font size in pixels.
    ///
    /// @return a finite, non-negative font size
    double size();

    /// Returns the line height in pixels.
    ///
    /// @return a finite, non-negative line height
    double lineHeight();

    /// Returns the font weight.
    ///
    /// @return a positive numeric font weight
    int weight();

    /// Returns the letter tracking in pixels.
    ///
    /// Material Design defines tracking as an absolute design value. Platforms that render letter spacing as an
    /// em value can divide this value by [size()].
    ///
    /// @return a finite absolute tracking value
    double tracking();

    /// Creates a text style token with letter tracking of `0.0`.
    ///
    /// @param fontFamily the JavaFX font family name
    /// @param size the finite, non-negative font size in pixels
    /// @param lineHeight the finite, non-negative line height in pixels
    /// @param weight the positive numeric font weight
    /// @return an immutable text style
    /// @throws NullPointerException if `fontFamily` is `null`
    /// @throws IllegalArgumentException if `size` or `lineHeight` is negative or non-finite, or if `weight`
    ///         is not positive
    static M3TextStyle of(String fontFamily, double size, double lineHeight, int weight) {
        return of(fontFamily, size, lineHeight, weight, 0.0);
    }

    /// Creates a text style token.
    ///
    /// @param fontFamily the JavaFX font family name
    /// @param size the finite, non-negative font size in pixels
    /// @param lineHeight the finite, non-negative line height in pixels
    /// @param weight the positive numeric font weight
    /// @param tracking the finite absolute letter tracking in pixels
    /// @return an immutable text style
    /// @throws NullPointerException if `fontFamily` is `null`
    /// @throws IllegalArgumentException if `size` or `lineHeight` is negative or non-finite, if `weight`
    ///         is not positive, or if `tracking` is non-finite
    static M3TextStyle of(String fontFamily, double size, double lineHeight, int weight, double tracking) {
        return new M3TextStyleImpl(fontFamily, size, lineHeight, weight, tracking);
    }
}
