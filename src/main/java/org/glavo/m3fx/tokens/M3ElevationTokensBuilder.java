// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ElevationTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds immutable [M3ElevationTokens] by overriding named elevation levels.
///
/// See [Material Design elevation](https://m3.material.io/styles/elevation/overview).
@NotNullByDefault
public final class M3ElevationTokensBuilder {
    /// The current level-zero elevation.
    private double level0;

    /// The current level-one elevation.
    private double level1;

    /// The current level-two elevation.
    private double level2;

    /// The current level-three elevation.
    private double level3;

    /// The current level-four elevation.
    private double level4;

    /// The current level-five elevation.
    private double level5;

    /// Creates a builder initialized from an existing elevation token set.
    ///
    /// @param tokens the elevation tokens to copy
    M3ElevationTokensBuilder(M3ElevationTokens tokens) {
        M3ElevationTokens source = Objects.requireNonNull(tokens, "tokens");
        level0 = source.level0();
        level1 = source.level1();
        level2 = source.level2();
        level3 = source.level3();
        level4 = source.level4();
        level5 = source.level5();
    }

    /// Replaces elevation level zero.
    ///
    /// @param value the replacement elevation
    /// @return this builder
    public M3ElevationTokensBuilder level0(double value) {
        level0 = validElevation(value, "level0");
        return this;
    }

    /// Replaces elevation level one.
    ///
    /// @param value the replacement elevation
    /// @return this builder
    public M3ElevationTokensBuilder level1(double value) {
        level1 = validElevation(value, "level1");
        return this;
    }

    /// Replaces elevation level two.
    ///
    /// @param value the replacement elevation
    /// @return this builder
    public M3ElevationTokensBuilder level2(double value) {
        level2 = validElevation(value, "level2");
        return this;
    }

    /// Replaces elevation level three.
    ///
    /// @param value the replacement elevation
    /// @return this builder
    public M3ElevationTokensBuilder level3(double value) {
        level3 = validElevation(value, "level3");
        return this;
    }

    /// Replaces elevation level four.
    ///
    /// @param value the replacement elevation
    /// @return this builder
    public M3ElevationTokensBuilder level4(double value) {
        level4 = validElevation(value, "level4");
        return this;
    }

    /// Replaces elevation level five.
    ///
    /// @param value the replacement elevation
    /// @return this builder
    public M3ElevationTokensBuilder level5(double value) {
        level5 = validElevation(value, "level5");
        return this;
    }

    /// Creates immutable elevation tokens from the current values.
    ///
    /// @return the built elevation tokens
    public M3ElevationTokens build() {
        return new M3ElevationTokensImpl(level0, level1, level2, level3, level4, level5);
    }

    /// Validates one elevation value eagerly.
    private static double validElevation(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
        return value;
    }
}
