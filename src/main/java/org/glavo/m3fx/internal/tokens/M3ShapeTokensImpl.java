// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3ShapeTokens;
import org.jetbrains.annotations.NotNullByDefault;

/// Default immutable implementation of {@link M3ShapeTokens}.
///
/// @param extraSmall the extra-small corner radius
/// @param small the small corner radius
/// @param medium the medium corner radius
/// @param large the large corner radius
/// @param extraLarge the extra-large corner radius
/// @param full the full corner radius used for pills
@NotNullByDefault
public record M3ShapeTokensImpl(
        double extraSmall,
        double small,
        double medium,
        double large,
        double extraLarge,
        double full
) implements M3ShapeTokens {
    /// Creates shape tokens.
    public M3ShapeTokensImpl {
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
