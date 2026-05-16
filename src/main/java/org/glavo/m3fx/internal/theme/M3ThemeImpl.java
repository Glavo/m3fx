// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of {@link M3Theme}.
///
/// @param profile the Material Design 3 token profile
/// @param colorScheme the MonetFX color scheme
/// @param density the density applied to layout-sensitive component tokens
/// @param tokens the complete token set for this theme
@NotNullByDefault
public record M3ThemeImpl(
        M3Profile profile,
        ColorScheme colorScheme,
        M3Density density,
        M3TokenSet tokens
) implements M3Theme {
    /// Creates a theme implementation.
    public M3ThemeImpl {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorScheme, "colorScheme");
        Objects.requireNonNull(density, "density");
        Objects.requireNonNull(tokens, "tokens");
        if (tokens.profile() != profile) {
            throw new IllegalArgumentException("Token profile must match theme profile");
        }
        if (!tokens.colorTokens().colorScheme().equals(colorScheme)) {
            throw new IllegalArgumentException("Token color scheme must match theme color scheme");
        }
    }
}
