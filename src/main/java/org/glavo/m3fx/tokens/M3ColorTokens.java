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

/// Exposes the color roles of an immutable MonetFX [ColorScheme] as M3FX tokens.
///
/// The supplied color scheme is retained, not copied or regenerated. Role lookup therefore returns exactly the
/// JavaFX [Color] values defined by that scheme. The token object itself is immutable and may be shared.
///
/// See [Material Design color](https://m3.material.io/styles/color/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public sealed interface M3ColorTokens permits M3ColorTokensImpl {
    /// Returns the MonetFX color scheme used by this token set.
    ///
    /// @return the immutable MonetFX color scheme retained by this token set; never `null`
    ColorScheme colorScheme();

    /// Creates color tokens backed by a MonetFX color scheme.
    ///
    /// @param colorScheme the MonetFX color scheme backing the created token set
    /// @return a color token set backed by the supplied color scheme
    /// @throws NullPointerException if `colorScheme` is `null`
    static M3ColorTokens fromColorScheme(ColorScheme colorScheme) {
        return new M3ColorTokensImpl(colorScheme);
    }

    /// Returns the color for a MonetFX color role.
    ///
    /// @param role the MonetFX color role to resolve
    /// @return the JavaFX color for the supplied role
    /// @throws NullPointerException if `role` is `null`
    default Color get(ColorRole role) {
        return colorScheme().getColor(role);
    }

    /// Returns all MonetFX color roles supported by [get].
    ///
    /// The returned list is the shared immutable role list defined by MonetFX. It is ordered by
    /// the declaration order of [ColorRole], contains no `null` elements, and is not a live view of this token object.
    ///
    /// @return the immutable, ordered list of supported MonetFX color roles
    default @Unmodifiable List<ColorRole> roles() {
        return ColorRole.ALL;
    }

}
