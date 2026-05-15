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
///
/// @param profile the Material Design 3 token profile
/// @param colorScheme the MonetFX color scheme
/// @param density the density applied to layout-sensitive component tokens
/// @param tokens the complete token set for this theme
@NotNullByDefault
public record M3Theme(
        M3Profile profile,
        ColorScheme colorScheme,
        M3Density density,
        M3TokenSet tokens
) {
    /// The default seed color used by m3fx.
    public static final Color DEFAULT_SEED_COLOR = Color.web("#6750a4");

    /// Creates a theme.
    public M3Theme {
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

    /// Creates the default light baseline theme.
    public static M3Theme defaultTheme() {
        return fromSeed(DEFAULT_SEED_COLOR, M3Profile.BASELINE_2021, Brightness.LIGHT, M3Density.standard());
    }

    /// Creates a baseline light theme from a seed color.
    public static M3Theme fromSeed(Color seedColor) {
        return fromSeed(seedColor, M3Profile.BASELINE_2021, Brightness.LIGHT, M3Density.standard());
    }

    /// Creates a theme from a seed color, profile, brightness, and density.
    public static M3Theme fromSeed(Color seedColor, M3Profile profile, Brightness brightness, M3Density density) {
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
    public static M3Theme fromColorScheme(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorScheme, "colorScheme");
        Objects.requireNonNull(density, "density");

        return new M3Theme(profile, colorScheme, density, M3TokenSet.create(profile, colorScheme, density));
    }

    /// Converts root-level theme tokens into JavaFX inline CSS declarations.
    public String toRootStyleDeclarations() {
        return tokens.toRootStyleDeclarations();
    }
}
