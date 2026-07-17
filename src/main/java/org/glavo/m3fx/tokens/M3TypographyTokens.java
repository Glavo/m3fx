// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TypographyTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 typography system tokens.
///
/// Typography tokens describe the Material type scale roles used by controls and by
/// [M3Text][org.glavo.m3fx.controls.M3Text]. Each role maps to an immutable [M3TextStyle] containing font
/// family, size, line height, weight, and tracking values that are converted into JavaFX CSS declarations.
///
/// See [Material Design typography](https://m3.material.io/styles/typography/overview).
@NotNullByDefault
public sealed interface M3TypographyTokens permits M3TypographyTokensImpl {
    /// Returns the display large text style.
    ///
    /// @return the display large text style
    M3TextStyle displayLarge();

    /// Returns the display medium text style.
    ///
    /// @return the display medium text style
    M3TextStyle displayMedium();

    /// Returns the display small text style.
    ///
    /// @return the display small text style
    M3TextStyle displaySmall();

    /// Returns the headline large text style.
    ///
    /// @return the headline large text style
    M3TextStyle headlineLarge();

    /// Returns the headline medium text style.
    ///
    /// @return the headline medium text style
    M3TextStyle headlineMedium();

    /// Returns the headline small text style.
    ///
    /// @return the headline small text style
    M3TextStyle headlineSmall();

    /// Returns the title large text style.
    ///
    /// @return the title large text style
    M3TextStyle titleLarge();

    /// Returns the title medium text style.
    ///
    /// @return the title medium text style
    M3TextStyle titleMedium();

    /// Returns the title small text style.
    ///
    /// @return the title small text style
    M3TextStyle titleSmall();

    /// Returns the label large text style.
    ///
    /// @return the label large text style
    M3TextStyle labelLarge();

    /// Returns the label medium text style.
    ///
    /// @return the label medium text style
    M3TextStyle labelMedium();

    /// Returns the label small text style.
    ///
    /// @return the label small text style
    M3TextStyle labelSmall();

    /// Returns the body large text style.
    ///
    /// @return the body large text style
    M3TextStyle bodyLarge();

    /// Returns the body medium text style.
    ///
    /// @return the body medium text style
    M3TextStyle bodyMedium();

    /// Returns the body small text style.
    ///
    /// @return the body small text style
    M3TextStyle bodySmall();

    /// Creates a builder initialized with baseline typography tokens.
    ///
    /// @return a mutable typography-token builder
    static M3TypographyTokensBuilder builder() {
        return new M3TypographyTokensBuilder(baseline());
    }

    /// Creates a builder initialized from an existing typography token set.
    ///
    /// @param tokens the typography tokens to copy
    /// @return a mutable typography-token builder
    /// @throws NullPointerException if `tokens` is `null`
    static M3TypographyTokensBuilder builder(M3TypographyTokens tokens) {
        return new M3TypographyTokensBuilder(tokens);
    }

    /// Creates typography tokens.
    private static M3TypographyTokens create(
            M3TextStyle displayLarge,
            M3TextStyle displayMedium,
            M3TextStyle displaySmall,
            M3TextStyle headlineLarge,
            M3TextStyle headlineMedium,
            M3TextStyle headlineSmall,
            M3TextStyle titleLarge,
            M3TextStyle titleMedium,
            M3TextStyle titleSmall,
            M3TextStyle labelLarge,
            M3TextStyle labelMedium,
            M3TextStyle labelSmall,
            M3TextStyle bodyLarge,
            M3TextStyle bodyMedium,
            M3TextStyle bodySmall
    ) {
        return new M3TypographyTokensImpl(
                displayLarge,
                displayMedium,
                displaySmall,
                headlineLarge,
                headlineMedium,
                headlineSmall,
                titleLarge,
                titleMedium,
                titleSmall,
                labelLarge,
                labelMedium,
                labelSmall,
                bodyLarge,
                bodyMedium,
                bodySmall
        );
    }

    /// Returns the baseline Material Design 3 typography tokens.
    ///
    /// @return an immutable baseline type scale using the JavaFX system font family
    static M3TypographyTokens baseline() {
        return create(
                M3TextStyle.of("System", 57.0, 64.0, 400, -0.25),
                M3TextStyle.of("System", 45.0, 52.0, 400, 0.0),
                M3TextStyle.of("System", 36.0, 44.0, 400, 0.0),
                M3TextStyle.of("System", 32.0, 40.0, 400, 0.0),
                M3TextStyle.of("System", 28.0, 36.0, 400, 0.0),
                M3TextStyle.of("System", 24.0, 32.0, 400, 0.0),
                M3TextStyle.of("System", 22.0, 28.0, 400, 0.0),
                M3TextStyle.of("System", 16.0, 24.0, 500, 0.15),
                M3TextStyle.of("System", 14.0, 20.0, 500, 0.10),
                M3TextStyle.of("System", 14.0, 20.0, 500, 0.10),
                M3TextStyle.of("System", 12.0, 16.0, 500, 0.50),
                M3TextStyle.of("System", 11.0, 16.0, 500, 0.50),
                M3TextStyle.of("System", 16.0, 24.0, 400, 0.50),
                M3TextStyle.of("System", 14.0, 20.0, 400, 0.25),
                M3TextStyle.of("System", 12.0, 16.0, 400, 0.40)
        );
    }

    /// Returns expressive Material Design 3 typography tokens.
    ///
    /// @return an immutable expressive type scale using the JavaFX system font family
    static M3TypographyTokens expressive() {
        return create(
                M3TextStyle.of("System", 64.0, 72.0, 500, -0.25),
                M3TextStyle.of("System", 52.0, 60.0, 500, 0.0),
                M3TextStyle.of("System", 44.0, 52.0, 500, 0.0),
                M3TextStyle.of("System", 36.0, 44.0, 500, 0.0),
                M3TextStyle.of("System", 32.0, 40.0, 500, 0.0),
                M3TextStyle.of("System", 28.0, 36.0, 500, 0.0),
                M3TextStyle.of("System", 24.0, 32.0, 500, 0.0),
                M3TextStyle.of("System", 18.0, 26.0, 600, 0.15),
                M3TextStyle.of("System", 15.0, 22.0, 600, 0.10),
                M3TextStyle.of("System", 14.0, 20.0, 600, 0.10),
                M3TextStyle.of("System", 13.0, 18.0, 600, 0.50),
                M3TextStyle.of("System", 12.0, 16.0, 600, 0.50),
                M3TextStyle.of("System", 17.0, 26.0, 400, 0.50),
                M3TextStyle.of("System", 15.0, 22.0, 400, 0.25),
                M3TextStyle.of("System", 13.0, 18.0, 400, 0.40)
        );
    }

}
