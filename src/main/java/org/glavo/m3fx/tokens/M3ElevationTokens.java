package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 elevation system tokens.
///
/// @param level0 elevation level zero
/// @param level1 elevation level one
/// @param level2 elevation level two
/// @param level3 elevation level three
/// @param level4 elevation level four
/// @param level5 elevation level five
@NotNullByDefault
public record M3ElevationTokens(
        double level0,
        double level1,
        double level2,
        double level3,
        double level4,
        double level5
) {
    /// Creates elevation tokens.
    public M3ElevationTokens {
        validate(level0, "level0");
        validate(level1, "level1");
        validate(level2, "level2");
        validate(level3, "level3");
        validate(level4, "level4");
        validate(level5, "level5");
    }

    /// Returns baseline elevation tokens.
    public static M3ElevationTokens baseline() {
        return new M3ElevationTokens(0.0, 1.0, 3.0, 6.0, 8.0, 12.0);
    }

    /// Converts elevation tokens into inline JavaFX CSS declarations.
    public String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        M3TokenCss.append(builder, "-m3-elevation-level0", M3TokenCss.pixels(level0));
        M3TokenCss.append(builder, "-m3-elevation-level1", M3TokenCss.pixels(level1));
        M3TokenCss.append(builder, "-m3-elevation-level2", M3TokenCss.pixels(level2));
        M3TokenCss.append(builder, "-m3-elevation-level3", M3TokenCss.pixels(level3));
        M3TokenCss.append(builder, "-m3-elevation-level4", M3TokenCss.pixels(level4));
        M3TokenCss.append(builder, "-m3-elevation-level5", M3TokenCss.pixels(level5));
        return builder.toString().trim();
    }

    /// Validates an elevation token.
    private static void validate(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
