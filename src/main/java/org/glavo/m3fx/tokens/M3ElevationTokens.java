// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ElevationTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 elevation system tokens.
///
/// Values are non-negative elevation levels in logical pixels. Level zero represents a surface without added
/// elevation; higher levels are semantic inputs to M3FX surface and shadow rendering.
///
/// See [Material Design elevation](https://m3.material.io/styles/elevation/overview).
@NotNullByDefault
public sealed interface M3ElevationTokens permits M3ElevationTokensImpl {
    /// Returns elevation level zero.
    ///
    /// @return elevation level zero
    double level0();

    /// Returns elevation level one.
    ///
    /// @return elevation level one
    double level1();

    /// Returns elevation level two.
    ///
    /// @return elevation level two
    double level2();

    /// Returns elevation level three.
    ///
    /// @return elevation level three
    double level3();

    /// Returns elevation level four.
    ///
    /// @return elevation level four
    double level4();

    /// Returns elevation level five.
    ///
    /// @return elevation level five
    double level5();

    /// Creates a builder initialized with baseline elevation levels.
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

    /// Returns baseline elevation tokens.
    ///
    /// @return baseline elevation tokens
    static M3ElevationTokens baseline() {
        return new M3ElevationTokensImpl(0.0, 1.0, 3.0, 6.0, 8.0, 12.0);
    }

}
