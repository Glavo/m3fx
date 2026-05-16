// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3TextStyle;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of {@link M3TextStyle}.
///
/// @param fontFamily the font family name
/// @param size the font size in pixels
/// @param lineHeight the line height in pixels
/// @param weight the font weight
@NotNullByDefault
public record M3TextStyleImpl(
        String fontFamily,
        double size,
        double lineHeight,
        int weight
) implements M3TextStyle {
    /// Creates a text style token.
    public M3TextStyleImpl {
        Objects.requireNonNull(fontFamily, "fontFamily");
        validate(size, "size");
        validate(lineHeight, "lineHeight");
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive");
        }
    }

    /// Validates a non-negative text metric.
    private static void validate(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
