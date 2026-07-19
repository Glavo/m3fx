// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3TextStyle;
import org.glavo.m3fx.tokens.M3TypographyTokens;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3TypographyTokens].
///
/// @param displayLarge   the display large text style
/// @param displayMedium  the display medium text style
/// @param displaySmall   the display small text style
/// @param headlineLarge  the headline large text style
/// @param headlineMedium the headline medium text style
/// @param headlineSmall  the headline small text style
/// @param titleLarge     the title large text style
/// @param titleMedium    the title medium text style
/// @param titleSmall     the title small text style
/// @param labelLarge     the label large text style
/// @param labelMedium    the label medium text style
/// @param labelSmall     the label small text style
/// @param bodyLarge      the body large text style
/// @param bodyMedium     the body medium text style
/// @param bodySmall      the body small text style
@NotNullByDefault
public record M3TypographyTokensImpl(
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
) implements M3TypographyTokens {
    /// Creates a complete typography token set.
    ///
    /// @throws NullPointerException if any text style is `null`
    public M3TypographyTokensImpl {
        Objects.requireNonNull(displayLarge, "displayLarge");
        Objects.requireNonNull(displayMedium, "displayMedium");
        Objects.requireNonNull(displaySmall, "displaySmall");
        Objects.requireNonNull(headlineLarge, "headlineLarge");
        Objects.requireNonNull(headlineMedium, "headlineMedium");
        Objects.requireNonNull(headlineSmall, "headlineSmall");
        Objects.requireNonNull(titleLarge, "titleLarge");
        Objects.requireNonNull(titleMedium, "titleMedium");
        Objects.requireNonNull(titleSmall, "titleSmall");
        Objects.requireNonNull(labelLarge, "labelLarge");
        Objects.requireNonNull(labelMedium, "labelMedium");
        Objects.requireNonNull(labelSmall, "labelSmall");
        Objects.requireNonNull(bodyLarge, "bodyLarge");
        Objects.requireNonNull(bodyMedium, "bodyMedium");
        Objects.requireNonNull(bodySmall, "bodySmall");
    }
}
