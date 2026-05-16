package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 motion duration tokens in milliseconds.
@NotNullByDefault
public sealed interface M3MotionTokens permits M3MotionTokensImpl {
    /// Returns the short duration token.
    int shortDuration();

    /// Returns the medium duration token.
    int mediumDuration();

    /// Returns the long duration token.
    int longDuration();

    /// Returns baseline motion tokens.
    static M3MotionTokens baseline() {
        return new M3MotionTokensImpl(100, 250, 500);
    }

    /// Converts motion tokens into inline JavaFX CSS declarations.
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        M3TokenCss.append(builder, "-m3-motion-duration-short", shortDuration() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-medium", mediumDuration() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-long", longDuration() + "ms");
        return builder.toString().trim();
    }
}

/// Default immutable implementation of {@link M3MotionTokens}.
///
/// @param shortDuration the short duration token
/// @param mediumDuration the medium duration token
/// @param longDuration the long duration token
@NotNullByDefault
record M3MotionTokensImpl(
        int shortDuration,
        int mediumDuration,
        int longDuration
) implements M3MotionTokens {
    /// Creates motion tokens.
    M3MotionTokensImpl {
        validate(shortDuration, "shortDuration");
        validate(mediumDuration, "mediumDuration");
        validate(longDuration, "longDuration");
    }

    /// Validates a duration token.
    private static void validate(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
