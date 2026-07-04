// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.theme;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.theme.M3ThemeImpl;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.glavo.monetfx.Brightness;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Describes a complete M3FX theme.
///
/// A theme is an immutable description of the Material Design 3 values that should be applied to a JavaFX
/// scene: dynamic color roles, component tokens, density, typography, shape, elevation, and motion. Themes do
/// not modify controls by themselves; install one with [M3ThemeManager] to add the base stylesheet, root style
/// classes, and generated JavaFX CSS declarations required by M3FX controls.
///
/// `M3Theme` can be created from a seed color, from an existing MonetFX [ColorScheme], or from an explicit
/// [M3TokenSet]. The seed-color factories follow the Material dynamic color model used by
/// [Material Design](https://m3.material.io/) and the
/// [Material color system](https://m3.material.io/styles/color/overview).
@NotNullByDefault
public sealed interface M3Theme permits M3ThemeImpl {
    /// The default seed color used by M3FX.
    Color DEFAULT_SEED_COLOR = Color.web("#6750a4");

    /// Returns the Material Design 3 token profile.
    M3Profile profile();

    /// Returns the MonetFX color scheme.
    ColorScheme colorScheme();

    /// Returns the brightness mode used by the color scheme.
    default Brightness brightness() {
        return colorScheme().getBrightness();
    }

    /// Returns the density applied to layout-sensitive component tokens.
    M3Density density();

    /// Returns the complete token set for this theme.
    M3TokenSet tokens();

    /// Creates the default light baseline theme.
    static M3Theme defaultTheme() {
        return fromSeed(DEFAULT_SEED_COLOR, M3Profile.BASELINE_2021, Brightness.LIGHT, M3Density.standard());
    }

    /// Creates a baseline light theme from a seed color.
    static M3Theme fromSeed(Color seedColor) {
        return fromSeed(seedColor, M3Profile.BASELINE_2021, Brightness.LIGHT, M3Density.standard());
    }

    /// Creates a baseline theme from a seed color and brightness.
    static M3Theme fromSeed(Color seedColor, Brightness brightness) {
        return fromSeed(seedColor, M3Profile.BASELINE_2021, brightness, M3Density.standard());
    }

    /// Creates a theme from a seed color, profile, and brightness.
    static M3Theme fromSeed(Color seedColor, M3Profile profile, Brightness brightness) {
        return fromSeed(seedColor, profile, brightness, M3Density.standard());
    }

    /// Creates a theme from a seed color, profile, brightness, and density.
    static M3Theme fromSeed(Color seedColor, M3Profile profile, Brightness brightness, M3Density density) {
        Objects.requireNonNull(seedColor, "seedColor");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(brightness, "brightness");
        Objects.requireNonNull(density, "density");

        ColorScheme colorScheme = ColorScheme.newBuilder()
                .setPrimaryColorSeed(seedColor)
                .setBrightness(brightness)
                .setSpecVersion(profile.getColorSpecVersion())
                .setColorStyle(profile.getColorStyle())
                .build();
        return fromColorScheme(profile, colorScheme, density);
    }

    /// Creates a baseline theme from an existing MonetFX color scheme.
    static M3Theme fromColorScheme(ColorScheme colorScheme) {
        return fromColorScheme(M3Profile.BASELINE_2021, colorScheme, M3Density.standard());
    }

    /// Creates a theme from an existing MonetFX color scheme and profile.
    static M3Theme fromColorScheme(M3Profile profile, ColorScheme colorScheme) {
        return fromColorScheme(profile, colorScheme, M3Density.standard());
    }

    /// Creates a theme from an existing MonetFX color scheme.
    static M3Theme fromColorScheme(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorScheme, "colorScheme");
        Objects.requireNonNull(density, "density");

        return fromTokenSet(profile, colorScheme, density, M3TokenSet.create(profile, colorScheme, density));
    }

    /// Creates a theme from an explicit token set.
    static M3Theme fromTokenSet(M3Profile profile, ColorScheme colorScheme, M3Density density, M3TokenSet tokens) {
        return new M3ThemeImpl(profile, colorScheme, density, tokens);
    }

    /// Converts root-level theme tokens into JavaFX inline CSS declarations.
    default String toRootStyleDeclarations() {
        return tokens().toRootStyleDeclarations();
    }

    /// Converts component tokens into JavaFX CSS rules for M3FX controls.
    default String toControlStyleRules() {
        return tokens().toControlStyleRules();
    }
}
