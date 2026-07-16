// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TextStyleImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Describes a Material Design 3 text style token.
///
/// See [Material Design typography](https://m3.material.io/styles/typography/overview).
@NotNullByDefault
public sealed interface M3TextStyle permits M3TextStyleImpl {
    /// Returns the font family name.
    String fontFamily();

    /// Returns the font size in pixels.
    double size();

    /// Returns the line height in pixels.
    double lineHeight();

    /// Returns the font weight.
    int weight();

    /// Returns the letter tracking in pixels.
    ///
    /// Material Design defines tracking as an absolute design value. Platforms that render letter spacing as an
    /// em value can divide this value by [size()].
    double tracking();

    /// Creates a text style token.
    static M3TextStyle of(String fontFamily, double size, double lineHeight, int weight) {
        return of(fontFamily, size, lineHeight, weight, 0.0);
    }

    /// Creates a text style token.
    static M3TextStyle of(String fontFamily, double size, double lineHeight, int weight, double tracking) {
        return new M3TextStyleImpl(fontFamily, size, lineHeight, weight, tracking);
    }
}
