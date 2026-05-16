package org.glavo.m3fx.theme;

import javafx.scene.paint.Color;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.glavo.monetfx.Brightness;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Describes a complete m3fx theme.
@NotNullByDefault
public sealed interface M3Theme permits M3ThemeImpl {
    /// The default seed color used by m3fx.
    Color DEFAULT_SEED_COLOR = Color.web("#6750a4");

    /// Returns the Material Design 3 token profile.
    M3Profile profile();

    /// Returns the MonetFX color scheme.
    ColorScheme colorScheme();

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

    /// Creates a theme from an existing MonetFX color scheme.
    static M3Theme fromColorScheme(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorScheme, "colorScheme");
        Objects.requireNonNull(density, "density");

        return new M3ThemeImpl(profile, colorScheme, density, M3TokenSet.create(profile, colorScheme, density));
    }

    /// Converts root-level theme tokens into JavaFX inline CSS declarations.
    default String toRootStyleDeclarations() {
        return tokens().toRootStyleDeclarations();
    }

    /// Converts component tokens into JavaFX CSS rules for m3fx controls.
    default String toControlStyleRules() {
        return tokens().toControlStyleRules();
    }
}

/// Default immutable implementation of {@link M3Theme}.
///
/// @param profile the Material Design 3 token profile
/// @param colorScheme the MonetFX color scheme
/// @param density the density applied to layout-sensitive component tokens
/// @param tokens the complete token set for this theme
@NotNullByDefault
record M3ThemeImpl(
        M3Profile profile,
        ColorScheme colorScheme,
        M3Density density,
        M3TokenSet tokens
) implements M3Theme {
    /// Creates a theme implementation.
    M3ThemeImpl {
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
