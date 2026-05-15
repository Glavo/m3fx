package org.glavo.m3fx.tokens;

import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Aggregates all Material Design 3 tokens used by a theme.
///
/// @param profile the profile that produced this token set
/// @param colorTokens the color tokens
/// @param typographyTokens the typography tokens
/// @param shapeTokens the shape tokens
/// @param elevationTokens the elevation tokens
/// @param motionTokens the motion tokens
/// @param stateLayerTokens the state layer tokens
/// @param componentTokens the component tokens
@NotNullByDefault
public record M3TokenSet(
        M3Profile profile,
        M3ColorTokens colorTokens,
        M3TypographyTokens typographyTokens,
        M3ShapeTokens shapeTokens,
        M3ElevationTokens elevationTokens,
        M3MotionTokens motionTokens,
        M3StateLayerTokens stateLayerTokens,
        M3ComponentTokens componentTokens
) {
    /// Creates a token set.
    public M3TokenSet {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorTokens, "colorTokens");
        Objects.requireNonNull(typographyTokens, "typographyTokens");
        Objects.requireNonNull(shapeTokens, "shapeTokens");
        Objects.requireNonNull(elevationTokens, "elevationTokens");
        Objects.requireNonNull(motionTokens, "motionTokens");
        Objects.requireNonNull(stateLayerTokens, "stateLayerTokens");
        Objects.requireNonNull(componentTokens, "componentTokens");
    }

    /// Creates a complete default token set for a profile and color scheme.
    public static M3TokenSet create(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorScheme, "colorScheme");
        Objects.requireNonNull(density, "density");

        M3ColorTokens colorTokens = new M3ColorTokens(colorScheme);
        M3TypographyTokens typographyTokens = profile == M3Profile.EXPRESSIVE_2025
                ? M3TypographyTokens.expressive()
                : M3TypographyTokens.baseline();
        M3ShapeTokens shapeTokens = profile == M3Profile.EXPRESSIVE_2025
                ? M3ShapeTokens.expressive()
                : M3ShapeTokens.baseline();
        M3ElevationTokens elevationTokens = M3ElevationTokens.baseline();
        M3MotionTokens motionTokens = M3MotionTokens.baseline();
        M3StateLayerTokens stateLayerTokens = M3StateLayerTokens.baseline();
        M3ComponentTokens componentTokens = M3ComponentTokens.create(profile, shapeTokens, density);

        return new M3TokenSet(
                profile,
                colorTokens,
                typographyTokens,
                shapeTokens,
                elevationTokens,
                motionTokens,
                stateLayerTokens,
                componentTokens
        );
    }

    /// Converts root-level tokens into JavaFX inline CSS declarations.
    public String toRootStyleDeclarations() {
        return colorTokens.toStyleDeclarations()
                + " "
                + typographyTokens.toStyleDeclarations()
                + " "
                + shapeTokens.toStyleDeclarations()
                + " "
                + elevationTokens.toStyleDeclarations()
                + " "
                + motionTokens.toStyleDeclarations()
                + " "
                + stateLayerTokens.toStyleDeclarations()
                + " "
                + componentTokens.toStyleDeclarations();
    }
}
