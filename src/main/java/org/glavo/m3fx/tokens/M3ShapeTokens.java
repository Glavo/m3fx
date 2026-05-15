package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 shape system tokens.
///
/// @param extraSmall the extra-small corner radius
/// @param small the small corner radius
/// @param medium the medium corner radius
/// @param large the large corner radius
/// @param extraLarge the extra-large corner radius
/// @param full the full corner radius used for pills
@NotNullByDefault
public record M3ShapeTokens(
        double extraSmall,
        double small,
        double medium,
        double large,
        double extraLarge,
        double full
) {
    /// Creates shape tokens.
    public M3ShapeTokens {
        validate(extraSmall, "extraSmall");
        validate(small, "small");
        validate(medium, "medium");
        validate(large, "large");
        validate(extraLarge, "extraLarge");
        validate(full, "full");
    }

    /// Returns baseline Material Design 3 shape tokens.
    public static M3ShapeTokens baseline() {
        return new M3ShapeTokens(4.0, 8.0, 12.0, 16.0, 28.0, 999.0);
    }

    /// Returns provisional expressive shape tokens.
    public static M3ShapeTokens expressive() {
        return new M3ShapeTokens(6.0, 10.0, 16.0, 24.0, 32.0, 999.0);
    }

    /// Validates a radius token.
    private static void validate(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
