// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.tokens.M3ColorTokensImpl;
import org.glavo.monetfx.ColorRole;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Wraps MonetFX color scheme output as M3FX color tokens.
///
/// Color tokens expose the Material Design 3 color roles generated from a MonetFX [ColorScheme]. They provide
/// the same dynamic color roles to controls, themes, and application code without coupling the token model
/// to a particular rendering backend.
///
/// See [Material Design color](https://m3.material.io/styles/color/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public sealed interface M3ColorTokens permits M3ColorTokensImpl {
    /// Returns the MonetFX color scheme used by this token set.
    ///
    /// @return the MonetFX color scheme backing this token set
    ColorScheme colorScheme();

    /// Creates color tokens from a MonetFX color scheme.
    ///
    /// @param colorScheme the MonetFX color scheme backing the created token set
    /// @return a color token set backed by the supplied color scheme
    static M3ColorTokens fromColorScheme(ColorScheme colorScheme) {
        return new M3ColorTokensImpl(colorScheme);
    }

    /// Returns the color for a MonetFX color role.
    ///
    /// @param role the MonetFX color role to resolve
    /// @return the JavaFX color for the supplied role
    default Color get(ColorRole role) {
        return colorScheme().getColor(role);
    }

    /// Returns all MonetFX color roles used by this token set.
    ///
    /// @return the immutable list of supported MonetFX color roles
    default @Unmodifiable List<ColorRole> roles() {
        return ColorRole.ALL;
    }

}
