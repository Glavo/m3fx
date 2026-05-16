// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3TextStyle;
import org.glavo.m3fx.tokens.M3TypographyTokens;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of {@link M3TypographyTokens}.
///
/// @param displayLarge the display large text style
/// @param headlineMedium the headline medium text style
/// @param titleLarge the title large text style
/// @param labelLarge the label large text style
/// @param bodyLarge the body large text style
/// @param bodyMedium the body medium text style
@NotNullByDefault
public record M3TypographyTokensImpl(
        M3TextStyle displayLarge,
        M3TextStyle headlineMedium,
        M3TextStyle titleLarge,
        M3TextStyle labelLarge,
        M3TextStyle bodyLarge,
        M3TextStyle bodyMedium
) implements M3TypographyTokens {
    /// Creates typography tokens.
    public M3TypographyTokensImpl {
        Objects.requireNonNull(displayLarge, "displayLarge");
        Objects.requireNonNull(headlineMedium, "headlineMedium");
        Objects.requireNonNull(titleLarge, "titleLarge");
        Objects.requireNonNull(labelLarge, "labelLarge");
        Objects.requireNonNull(bodyLarge, "bodyLarge");
        Objects.requireNonNull(bodyMedium, "bodyMedium");
    }
}
