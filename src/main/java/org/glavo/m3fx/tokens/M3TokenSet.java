package org.glavo.m3fx.tokens;

import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Aggregates all Material Design 3 tokens used by a theme.
@NotNullByDefault
public record M3TokenSet(
        /// The profile that produced this token set.
        M3Profile profile,

        /// The color tokens.
        M3ColorTokens colorTokens,

        /// The typography tokens.
        M3TypographyTokens typographyTokens,

        /// The shape tokens.
        M3ShapeTokens shapeTokens,

        /// The elevation tokens.
        M3ElevationTokens elevationTokens,

        /// The motion tokens.
        M3MotionTokens motionTokens,

        /// The state layer tokens.
        M3StateLayerTokens stateLayerTokens,

        /// The component tokens.
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
                + stateLayerTokens.toStyleDeclarations();
    }
}
