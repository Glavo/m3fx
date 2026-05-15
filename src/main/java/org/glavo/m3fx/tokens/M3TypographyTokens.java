package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Holds Material Design 3 typography system tokens.
@NotNullByDefault
public record M3TypographyTokens(
        /// The display large text style.
        M3TextStyle displayLarge,

        /// The headline medium text style.
        M3TextStyle headlineMedium,

        /// The title large text style.
        M3TextStyle titleLarge,

        /// The label large text style.
        M3TextStyle labelLarge,

        /// The body large text style.
        M3TextStyle bodyLarge,

        /// The body medium text style.
        M3TextStyle bodyMedium
) {
    /// Creates typography tokens.
    public M3TypographyTokens {
        Objects.requireNonNull(displayLarge, "displayLarge");
        Objects.requireNonNull(headlineMedium, "headlineMedium");
        Objects.requireNonNull(titleLarge, "titleLarge");
        Objects.requireNonNull(labelLarge, "labelLarge");
        Objects.requireNonNull(bodyLarge, "bodyLarge");
        Objects.requireNonNull(bodyMedium, "bodyMedium");
    }

    /// Returns the baseline Material Design 3 typography tokens.
    public static M3TypographyTokens baseline() {
        return new M3TypographyTokens(
                new M3TextStyle("System", 57.0, 64.0, 400),
                new M3TextStyle("System", 28.0, 36.0, 400),
                new M3TextStyle("System", 22.0, 28.0, 400),
                new M3TextStyle("System", 14.0, 20.0, 500),
                new M3TextStyle("System", 16.0, 24.0, 400),
                new M3TextStyle("System", 14.0, 20.0, 400)
        );
    }

    /// Returns provisional expressive typography tokens.
    public static M3TypographyTokens expressive() {
        return new M3TypographyTokens(
                new M3TextStyle("System", 64.0, 72.0, 500),
                new M3TextStyle("System", 32.0, 40.0, 500),
                new M3TextStyle("System", 24.0, 32.0, 500),
                new M3TextStyle("System", 14.0, 20.0, 600),
                new M3TextStyle("System", 17.0, 26.0, 400),
                new M3TextStyle("System", 15.0, 22.0, 400)
        );
    }
}
