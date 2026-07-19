// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3ElevationTokens;
import org.jetbrains.annotations.NotNullByDefault;

/// Default immutable implementation of [M3ElevationTokens].
///
/// Every level is expressed in JavaFX logical pixels and must be finite and non-negative.
///
/// @param level0 elevation level zero in logical pixels
/// @param level1 elevation level one in logical pixels
/// @param level2 elevation level two in logical pixels
/// @param level3 elevation level three in logical pixels
/// @param level4 elevation level four in logical pixels
/// @param level5 elevation level five in logical pixels
@NotNullByDefault
public record M3ElevationTokensImpl(
        double level0,
        double level1,
        double level2,
        double level3,
        double level4,
        double level5
) implements M3ElevationTokens {
    /// Creates elevation tokens.
    ///
    /// @throws IllegalArgumentException if any level is negative or not finite
    public M3ElevationTokensImpl {
        validate(level0, "level0");
        validate(level1, "level1");
        validate(level2, "level2");
        validate(level3, "level3");
        validate(level4, "level4");
        validate(level5, "level5");
    }

    /// Validates an elevation token.
    private static void validate(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}
