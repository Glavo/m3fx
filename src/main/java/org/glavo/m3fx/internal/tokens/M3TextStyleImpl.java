// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3TextStyle;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3TextStyle].
///
/// @param fontFamily the font family name
/// @param size       the finite, non-negative font size in JavaFX logical pixels
/// @param lineHeight the finite, non-negative line height in JavaFX logical pixels
/// @param weight     the font weight
/// @param tracking   the finite letter tracking in JavaFX logical pixels
@NotNullByDefault
public record M3TextStyleImpl(
        String fontFamily,
        double size,
        double lineHeight,
        int weight,
        double tracking
) implements M3TextStyle {
    /// Creates a text style token.
    ///
    /// @throws NullPointerException     if `fontFamily` is `null`
    /// @throws IllegalArgumentException if `size` or `lineHeight` is negative or not finite, `weight` is not
    ///                                  positive, or `tracking` is not finite
    public M3TextStyleImpl {
        Objects.requireNonNull(fontFamily, "fontFamily");
        validate(size, "size");
        validate(lineHeight, "lineHeight");
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive");
        }
        validateFinite(tracking, "tracking");
    }

    /// Validates a non-negative text metric.
    private static void validate(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }

    /// Validates a finite text metric.
    private static void validateFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
