// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ShapeTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Defines the immutable Material Design 3 corner-radius scale.
///
/// Values are finite, non-negative radii in JavaFX logical pixels. Component tokens select values from this scale
/// when deriving their default shapes. A radius does not dynamically affect an existing component token set;
/// component tokens must be derived again after replacing the shape scale.
///
/// See [Material Design shape](https://m3.material.io/styles/shape/overview).
@NotNullByDefault
public sealed interface M3ShapeTokens permits M3ShapeTokensImpl {
    /// Returns the no-corner radius.
    ///
    /// @return the no-corner radius in pixels
    double none();

    /// Returns the extra-small corner radius.
    ///
    /// @return the extra-small corner radius in pixels
    double extraSmall();

    /// Returns the small corner radius.
    ///
    /// @return the small corner radius in pixels
    double small();

    /// Returns the medium corner radius.
    ///
    /// @return the medium corner radius in pixels
    double medium();

    /// Returns the large corner radius.
    ///
    /// @return the large corner radius in pixels
    double large();

    /// Returns the large-increased corner radius.
    ///
    /// @return the large-increased corner radius in pixels
    double largeIncreased();

    /// Returns the extra-large corner radius.
    ///
    /// @return the extra-large corner radius in pixels
    double extraLarge();

    /// Returns the extra-large-increased corner radius.
    ///
    /// @return the extra-large-increased corner radius in pixels
    double extraLargeIncreased();

    /// Returns the extra-extra-large corner radius.
    ///
    /// @return the extra-extra-large corner radius in pixels
    double extraExtraLarge();

    /// Returns the full corner radius used for pills.
    ///
    /// @return the full corner radius used for pills, in pixels
    double full();

    /// Creates a builder initialized with all values from [baseline].
    ///
    /// @return a mutable shape-token builder
    static M3ShapeTokensBuilder builder() {
        return new M3ShapeTokensBuilder(baseline());
    }

    /// Creates a builder initialized from an existing shape token set.
    ///
    /// @param tokens the shape tokens to copy
    /// @return a mutable shape-token builder
    /// @throws NullPointerException if `tokens` is `null`
    static M3ShapeTokensBuilder builder(M3ShapeTokens tokens) {
        return new M3ShapeTokensBuilder(tokens);
    }

    /// Creates shape tokens.
    ///
    /// @param none                the no-corner radius in pixels
    /// @param extraSmall          the extra-small corner radius in pixels
    /// @param small               the small corner radius in pixels
    /// @param medium              the medium corner radius in pixels
    /// @param large               the large corner radius in pixels
    /// @param largeIncreased      the large-increased corner radius in pixels
    /// @param extraLarge          the extra-large corner radius in pixels
    /// @param extraLargeIncreased the extra-large-increased corner radius in pixels
    /// @param extraExtraLarge     the extra-extra-large corner radius in pixels
    /// @param full                the full corner radius used for pills, in pixels
    /// @return the created shape token set
    private static M3ShapeTokens create(
            double none,
            double extraSmall,
            double small,
            double medium,
            double large,
            double largeIncreased,
            double extraLarge,
            double extraLargeIncreased,
            double extraExtraLarge,
            double full
    ) {
        return new M3ShapeTokensImpl(
                none,
                extraSmall,
                small,
                medium,
                large,
                largeIncreased,
                extraLarge,
                extraLargeIncreased,
                extraExtraLarge,
                full
        );
    }

    /// Returns the baseline Material Design 3 shape scale.
    ///
    /// @return baseline Material Design 3 shape tokens
    static M3ShapeTokens baseline() {
        return create(0.0, 4.0, 8.0, 12.0, 16.0, 20.0, 28.0, 32.0, 48.0, 999.0);
    }

    /// Returns the Material Design 3 Expressive shape scale.
    ///
    /// @return expressive Material Design 3 shape tokens
    static M3ShapeTokens expressive() {
        return create(0.0, 6.0, 10.0, 16.0, 24.0, 28.0, 32.0, 40.0, 48.0, 999.0);
    }

}
