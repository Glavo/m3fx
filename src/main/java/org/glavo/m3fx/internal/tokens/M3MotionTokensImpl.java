// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3MotionTokens;
import org.jetbrains.annotations.NotNullByDefault;

/// Default immutable implementation of {@link M3MotionTokens}.
///
/// @param shortDuration the short duration token
/// @param mediumDuration the medium duration token
/// @param longDuration the long duration token
@NotNullByDefault
public record M3MotionTokensImpl(
        int shortDuration,
        int mediumDuration,
        int longDuration
) implements M3MotionTokens {
    /// Creates motion tokens.
    public M3MotionTokensImpl {
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
