package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 shape system tokens.
@NotNullByDefault
public sealed interface M3ShapeTokens permits M3ShapeTokensImpl {
    /// Returns the extra-small corner radius.
    double extraSmall();

    /// Returns the small corner radius.
    double small();

    /// Returns the medium corner radius.
    double medium();

    /// Returns the large corner radius.
    double large();

    /// Returns the extra-large corner radius.
    double extraLarge();

    /// Returns the full corner radius used for pills.
    double full();

    /// Returns baseline Material Design 3 shape tokens.
    static M3ShapeTokens baseline() {
        return new M3ShapeTokensImpl(4.0, 8.0, 12.0, 16.0, 28.0, 999.0);
    }

    /// Returns provisional expressive shape tokens.
    static M3ShapeTokens expressive() {
        return new M3ShapeTokensImpl(6.0, 10.0, 16.0, 24.0, 32.0, 999.0);
    }

    /// Converts shape tokens into inline JavaFX CSS declarations.
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        M3TokenCss.append(builder, "-m3-shape-corner-extra-small", M3TokenCss.pixels(extraSmall()));
        M3TokenCss.append(builder, "-m3-shape-corner-small", M3TokenCss.pixels(small()));
        M3TokenCss.append(builder, "-m3-shape-corner-medium", M3TokenCss.pixels(medium()));
        M3TokenCss.append(builder, "-m3-shape-corner-large", M3TokenCss.pixels(large()));
        M3TokenCss.append(builder, "-m3-shape-corner-extra-large", M3TokenCss.pixels(extraLarge()));
        M3TokenCss.append(builder, "-m3-shape-corner-full", M3TokenCss.pixels(full()));
        return builder.toString().trim();
    }
}

/// Default immutable implementation of {@link M3ShapeTokens}.
///
/// @param extraSmall the extra-small corner radius
/// @param small the small corner radius
/// @param medium the medium corner radius
/// @param large the large corner radius
/// @param extraLarge the extra-large corner radius
/// @param full the full corner radius used for pills
@NotNullByDefault
record M3ShapeTokensImpl(
        double extraSmall,
        double small,
        double medium,
        double large,
        double extraLarge,
        double full
) implements M3ShapeTokens {
    /// Creates shape tokens.
    M3ShapeTokensImpl {
        validate(extraSmall, "extraSmall");
        validate(small, "small");
        validate(medium, "medium");
        validate(large, "large");
        validate(extraLarge, "extraLarge");
        validate(full, "full");
    }

    /// Validates a radius token.
    private static void validate(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
