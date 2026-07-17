// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ElevationTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Defines the immutable Material Design 3 elevation scale.
///
/// Values are finite, non-negative elevations in JavaFX logical pixels. Level zero represents a surface without
/// added elevation; higher levels are semantic inputs used by controls when selecting surface and shadow
/// treatment. The levels are token roles rather than a requirement that every control render a shadow.
///
/// See [Material Design elevation](https://m3.material.io/styles/elevation/overview).
@NotNullByDefault
public sealed interface M3ElevationTokens permits M3ElevationTokensImpl {
    /// Returns elevation level zero.
    ///
    /// @return elevation level zero in logical pixels
    double level0();

    /// Returns elevation level one.
    ///
    /// @return elevation level one in logical pixels
    double level1();

    /// Returns elevation level two.
    ///
    /// @return elevation level two in logical pixels
    double level2();

    /// Returns elevation level three.
    ///
    /// @return elevation level three in logical pixels
    double level3();

    /// Returns elevation level four.
    ///
    /// @return elevation level four in logical pixels
    double level4();

    /// Returns elevation level five.
    ///
    /// @return elevation level five in logical pixels
    double level5();

    /// Creates a builder initialized with all values from [baseline].
    ///
    /// @return a mutable elevation-token builder
    static M3ElevationTokensBuilder builder() {
        return new M3ElevationTokensBuilder(baseline());
    }

    /// Creates a builder initialized from an existing elevation token set.
    ///
    /// @param tokens the elevation tokens to copy
    /// @return a mutable elevation-token builder
    /// @throws NullPointerException if `tokens` is `null`
    static M3ElevationTokensBuilder builder(M3ElevationTokens tokens) {
        return new M3ElevationTokensBuilder(tokens);
    }

    /// Returns the baseline Material Design 3 elevation scale.
    ///
    /// @return immutable levels `0.0`, `1.0`, `3.0`, `6.0`, `8.0`, and `12.0`
    static M3ElevationTokens baseline() {
        return new M3ElevationTokensImpl(0.0, 1.0, 3.0, 6.0, 8.0, 12.0);
    }

}
