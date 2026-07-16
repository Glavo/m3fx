// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TokenSetImpl;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

/// Aggregates all Material Design 3 tokens used by a theme.
///
/// A token set is the structured source of truth that M3FX turns into JavaFX CSS declarations. It contains the
/// color roles, type scale, shape scale, elevation shadows, motion values, state layer opacities, and
/// component-specific metrics used by controls. Applications usually obtain a token set through
/// [M3Theme][org.glavo.m3fx.theme.M3Theme], but custom integrations can create one explicitly when they need to
/// bridge another design-token pipeline.
///
/// The model mirrors the token-based approach documented by [Material Design](https://m3.material.io/) and is
/// intentionally independent from any single stylesheet so themes can switch between baseline and expressive
/// profiles.
@NotNullByDefault
public sealed interface M3TokenSet permits M3TokenSetImpl {
    /// Returns the profile that produced this token set.
    M3Profile profile();

    /// Returns the density used to derive layout-sensitive component tokens.
    M3Density density();

    /// Returns the color tokens.
    M3ColorTokens colorTokens();

    /// Returns the typography tokens.
    M3TypographyTokens typographyTokens();

    /// Returns the shape tokens.
    M3ShapeTokens shapeTokens();

    /// Returns the elevation tokens.
    M3ElevationTokens elevationTokens();

    /// Returns the motion tokens.
    M3MotionTokens motionTokens();

    /// Returns the state layer tokens.
    M3StateLayerTokens stateLayerTokens();

    /// Returns the component tokens.
    M3ComponentTokens componentTokens();

    /// Creates a builder initialized with the profile defaults and supplied color scheme and density.
    ///
    /// @param profile the Material token profile
    /// @param colorScheme the MonetFX color scheme
    /// @param density the density used for component metrics
    /// @return a mutable token-set builder
    static M3TokenSetBuilder builder(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        return new M3TokenSetBuilder(profile, colorScheme, density);
    }

    /// Creates a builder initialized with every token group from an existing token set.
    ///
    /// @param tokenSet the token set to copy
    /// @return a mutable token-set builder
    static M3TokenSetBuilder builder(M3TokenSet tokenSet) {
        return new M3TokenSetBuilder(tokenSet);
    }

    /// Creates a complete default token set for a profile and color scheme.
    static M3TokenSet create(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        return builder(profile, colorScheme, density).build();
    }

}
