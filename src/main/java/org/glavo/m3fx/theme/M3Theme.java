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

/// Describes a complete, immutable M3FX theme.
///
/// A theme combines a [M3TokenSet] with the profile, brightness, density, and color scheme derivable from that
/// token set. Creating a theme has no effect on a scene graph. Use [M3ThemeManager] to install it on a
/// [Scene][javafx.scene.Scene] or on a local [Parent][javafx.scene.Parent] subtree.
///
/// A theme can be created from a seed color, from an existing MonetFX [ColorScheme], or from an explicit
/// [M3TokenSet]. Seed-color factories derive a new color scheme. Color-scheme and token-set factories retain the
/// supplied immutable value instead of copying it. Theme instances are immutable and may be shared between
/// scenes.
///
/// The seed-color factories follow the Material dynamic color model used by
/// [Material Design](https://m3.material.io/) and the
/// [Material color system](https://m3.material.io/styles/color/overview).
@NotNullByDefault
public sealed interface M3Theme permits M3ThemeImpl {
    /// The default M3FX seed color, `#6750A4`.
    Color DEFAULT_SEED_COLOR = Color.web("#6750a4");

    /// Returns the Material Design 3 token profile.
    ///
    /// The value identifies the preset from which defaults were selected. Explicit token-group replacements may
    /// differ from that preset.
    ///
    /// @return the retained profile identity; never `null`
    default M3Profile profile() {
        return tokens().profile();
    }

    /// Returns the MonetFX color scheme.
    ///
    /// @return the immutable color scheme used by this theme; never `null`
    default ColorScheme colorScheme() {
        return tokens().colorTokens().colorScheme();
    }

    /// Returns the brightness mode used by the color scheme.
    ///
    /// @return the light or dark brightness mode; never `null`
    default Brightness brightness() {
        return colorScheme().getBrightness();
    }

    /// Returns the density applied to layout-sensitive component tokens.
    ///
    /// @return the density used when deriving this theme's component metrics; never `null`
    default M3Density density() {
        return tokens().density();
    }

    /// Returns the complete token set for this theme.
    ///
    /// @return the immutable token set; never `null`
    M3TokenSet tokens();

    /// Creates the default light Standard theme.
    ///
    /// @return a new `BASELINE_2021` theme using [DEFAULT_SEED_COLOR], light brightness, and standard density
    static M3Theme defaultTheme() {
        return fromSeed(DEFAULT_SEED_COLOR, M3Profile.BASELINE_2021, Brightness.LIGHT, M3Density.standard());
    }

    /// Creates a baseline light theme from a seed color.
    ///
    /// @param seedColor the primary source color used to derive the dynamic color scheme
    /// @return a new light baseline theme using standard density
    /// @throws NullPointerException if `seedColor` is `null`
    static M3Theme fromSeed(Color seedColor) {
        return fromSeed(seedColor, M3Profile.BASELINE_2021, Brightness.LIGHT, M3Density.standard());
    }

    /// Creates a baseline theme from a seed color and brightness.
    ///
    /// @param seedColor the primary source color used to derive the dynamic color scheme
    /// @param brightness the requested light or dark color scheme
    /// @return a new baseline theme using standard density
    /// @throws NullPointerException if `seedColor` or `brightness` is `null`
    static M3Theme fromSeed(Color seedColor, Brightness brightness) {
        return fromSeed(seedColor, M3Profile.BASELINE_2021, brightness, M3Density.standard());
    }

    /// Creates a theme from a seed color, profile, and brightness.
    ///
    /// @param seedColor the primary source color used to derive the dynamic color scheme
    /// @param profile the token profile and MonetFX color specification to use
    /// @param brightness the requested light or dark color scheme
    /// @return a new theme using standard density
    /// @throws NullPointerException if `seedColor`, `profile`, or `brightness` is `null`
    static M3Theme fromSeed(Color seedColor, M3Profile profile, Brightness brightness) {
        return fromSeed(seedColor, profile, brightness, M3Density.standard());
    }

    /// Creates a theme from a seed color, profile, brightness, and density.
    ///
    /// The supplied [M3Profile] controls both the component token family and the MonetFX color specification used
    /// to derive the color scheme. The resulting theme is immutable; subsequent changes to application state do
    /// not alter it.
    ///
    /// @param seedColor the primary source color used to derive the dynamic color scheme
    /// @param profile the token profile and MonetFX color specification to use
    /// @param brightness the requested light or dark color scheme
    /// @param density the density used for layout-sensitive component metrics
    /// @return a new theme containing a fully derived token set
    /// @throws NullPointerException if `seedColor`, `profile`, `brightness`, or `density` is `null`
    static M3Theme fromSeed(Color seedColor, M3Profile profile, Brightness brightness, M3Density density) {
        Objects.requireNonNull(seedColor, "seedColor");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(brightness, "brightness");
        Objects.requireNonNull(density, "density");

        ColorScheme colorScheme = ColorScheme.newBuilder()
                .setPrimaryColorSeed(seedColor)
                .setBrightness(brightness)
                .setSpecVersion(profile.colorSpecVersion())
                .setColorStyle(profile.colorStyle())
                .build();
        return fromColorScheme(profile, colorScheme, density);
    }

    /// Creates a baseline theme from an existing MonetFX color scheme.
    ///
    /// @param colorScheme the immutable MonetFX color scheme to use without re-deriving its colors
    /// @return a new baseline theme using standard density
    /// @throws NullPointerException if `colorScheme` is `null`
    static M3Theme fromColorScheme(ColorScheme colorScheme) {
        return fromColorScheme(M3Profile.BASELINE_2021, colorScheme, M3Density.standard());
    }

    /// Creates a theme from an existing MonetFX color scheme and profile.
    ///
    /// The profile supplies default token families, including component geometry and semantic color-role mappings.
    /// The color scheme is retained as supplied and is not regenerated using the profile's MonetFX settings.
    ///
    /// @param profile the component token profile to use
    /// @param colorScheme the immutable MonetFX color scheme to use without re-deriving its colors
    /// @return a new theme using standard density
    /// @throws NullPointerException if `profile` or `colorScheme` is `null`
    static M3Theme fromColorScheme(M3Profile profile, ColorScheme colorScheme) {
        return fromColorScheme(profile, colorScheme, M3Density.standard());
    }

    /// Creates a theme from an existing MonetFX color scheme, profile, and density.
    ///
    /// The color scheme is retained as the source of color roles. It is not regenerated to match `profile`;
    /// callers may intentionally combine a color scheme with a different component-token preset. Token groups may
    /// be replaced afterward through [M3TokenSet.builder(M3TokenSet)][M3TokenSet#builder(M3TokenSet)].
    ///
    /// @param profile the component token profile to use
    /// @param colorScheme the immutable MonetFX color scheme to use
    /// @param density the density used for layout-sensitive component metrics
    /// @return a new theme containing tokens derived from the supplied values
    /// @throws NullPointerException if `profile`, `colorScheme`, or `density` is `null`
    static M3Theme fromColorScheme(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorScheme, "colorScheme");
        Objects.requireNonNull(density, "density");

        return fromTokenSet(M3TokenSet.builder(profile, colorScheme, density).build());
    }

    /// Creates a theme from an explicit token set.
    ///
    /// The token set becomes the complete source of profile, color, density, and component values for the theme.
    ///
    /// @param tokens the complete immutable token set exposed by the theme
    /// @return a new immutable theme backed by `tokens`
    /// @throws NullPointerException if `tokens` is `null`
    static M3Theme fromTokenSet(M3TokenSet tokens) {
        return new M3ThemeImpl(tokens);
    }

}
