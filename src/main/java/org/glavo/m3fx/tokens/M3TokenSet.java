// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TokenSetImpl;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

/// Aggregates the complete immutable token state used by an M3FX theme.
///
/// A token set contains color roles, typography, shape, elevation, motion, state-layer, and component-specific
/// values. Applications usually obtain one through
/// [M3Theme][org.glavo.m3fx.theme.M3Theme], but custom integrations can create one explicitly when they need to
/// replace complete token groups.
///
/// Token groups are retained as immutable values and are not copied. A token set is therefore safe to share after
/// construction. Its [profile] and [density] describe how defaults were selected; replacing other groups does not
/// implicitly change either value.
///
/// See [Material Design styles](https://m3.material.io/styles).
@NotNullByDefault
public sealed interface M3TokenSet permits M3TokenSetImpl {
    /// Returns the profile that produced this token set.
    ///
    /// @return the Material token profile
    M3Profile profile();

    /// Returns the density used to derive layout-sensitive component tokens.
    ///
    /// @return the component density
    M3Density density();

    /// Returns the color tokens.
    ///
    /// @return the color token group
    M3ColorTokens colorTokens();

    /// Returns the typography tokens.
    ///
    /// @return the typography token group
    M3TypographyTokens typographyTokens();

    /// Returns the shape tokens.
    ///
    /// @return the shape token group
    M3ShapeTokens shapeTokens();

    /// Returns the elevation tokens.
    ///
    /// @return the elevation token group
    M3ElevationTokens elevationTokens();

    /// Returns the motion tokens.
    ///
    /// @return the motion token group
    M3MotionTokens motionTokens();

    /// Returns the state layer tokens.
    ///
    /// @return the interaction-state opacity token group
    M3StateLayerTokens stateLayerTokens();

    /// Returns the component tokens.
    ///
    /// @return the component-specific metric token group
    M3ComponentTokens componentTokens();

    /// Creates a builder initialized with the profile defaults and supplied color scheme and density.
    ///
    /// @param profile the Material token profile
    /// @param colorScheme the MonetFX color scheme
    /// @param density the density used for component metrics
    /// @return a mutable token-set builder
    /// @throws NullPointerException if `profile`, `colorScheme`, or `density` is `null`
    static M3TokenSetBuilder builder(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        return new M3TokenSetBuilder(profile, colorScheme, density);
    }

    /// Creates a builder initialized with every token group from an existing token set.
    ///
    /// @param tokenSet the token set to copy
    /// @return a mutable token-set builder
    /// @throws NullPointerException if `tokenSet` is `null`
    static M3TokenSetBuilder builder(M3TokenSet tokenSet) {
        return new M3TokenSetBuilder(tokenSet);
    }

}
