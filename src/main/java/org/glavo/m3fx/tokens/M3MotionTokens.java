package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 motion duration tokens in milliseconds.
///
/// @param shortDuration the short duration token
/// @param mediumDuration the medium duration token
/// @param longDuration the long duration token
@NotNullByDefault
public record M3MotionTokens(
        int shortDuration,
        int mediumDuration,
        int longDuration
) {
    /// Creates motion tokens.
    public M3MotionTokens {
        validate(shortDuration, "shortDuration");
        validate(mediumDuration, "mediumDuration");
        validate(longDuration, "longDuration");
    }

    /// Returns baseline motion tokens.
    public static M3MotionTokens baseline() {
        return new M3MotionTokens(100, 250, 500);
    }

    /// Validates a duration token.
    private static void validate(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
