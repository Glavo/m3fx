package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Describes a Material Design 3 text style token.
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

    /// Creates a text style token.
    static M3TextStyle create(String fontFamily, double size, double lineHeight, int weight) {
        return new M3TextStyleImpl(fontFamily, size, lineHeight, weight);
    }
}

/// Default immutable implementation of {@link M3TextStyle}.
///
/// @param fontFamily the font family name
/// @param size the font size in pixels
/// @param lineHeight the line height in pixels
/// @param weight the font weight
@NotNullByDefault
record M3TextStyleImpl(
        String fontFamily,
        double size,
        double lineHeight,
        int weight
) implements M3TextStyle {
    /// Creates a text style token.
    M3TextStyleImpl {
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
