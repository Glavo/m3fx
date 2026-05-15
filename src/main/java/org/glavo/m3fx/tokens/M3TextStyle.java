package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Defines a typography style token.
///
/// @param fontFamily the JavaFX font family name
/// @param size the font size in pixels
/// @param lineHeight the line height in pixels
/// @param weight the numeric font weight
@NotNullByDefault
public record M3TextStyle(
        String fontFamily,
        double size,
        double lineHeight,
        int weight
) {
    /// Creates a typography style token.
    public M3TextStyle {
        Objects.requireNonNull(fontFamily, "fontFamily");
        if (size <= 0.0) {
            throw new IllegalArgumentException("Font size must be positive");
        }
        if (lineHeight <= 0.0) {
            throw new IllegalArgumentException("Line height must be positive");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Font weight must be positive");
        }
    }

    /// Converts this style into JavaFX CSS declarations.
    public String toStyleDeclarations() {
        return "-fx-font-family: \"" + fontFamily + "\"; "
                + "-fx-font-size: " + M3TokenCss.format(size) + "px; "
                + "-fx-font-weight: " + weight + ";";
    }
}
