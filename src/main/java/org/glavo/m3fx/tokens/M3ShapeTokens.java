package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 shape system tokens.
@NotNullByDefault
public record M3ShapeTokens(
        /// The extra-small corner radius.
        double extraSmall,

        /// The small corner radius.
        double small,

        /// The medium corner radius.
        double medium,

        /// The large corner radius.
        double large,

        /// The extra-large corner radius.
        double extraLarge,

        /// The full corner radius used for pills.
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
