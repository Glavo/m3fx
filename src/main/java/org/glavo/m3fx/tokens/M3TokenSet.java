package org.glavo.m3fx.tokens;

import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Aggregates all Material Design 3 tokens used by a theme.
@NotNullByDefault
public sealed interface M3TokenSet permits M3TokenSetImpl {
    /// Returns the profile that produced this token set.
    M3Profile profile();

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

    /// Creates a complete default token set for a profile and color scheme.
    static M3TokenSet create(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorScheme, "colorScheme");
        Objects.requireNonNull(density, "density");

        M3ColorTokens colorTokens = M3ColorTokens.create(colorScheme);
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

        return new M3TokenSetImpl(
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
    default String toRootStyleDeclarations() {
        return colorTokens().toStyleDeclarations()
                + " "
                + typographyTokens().toStyleDeclarations()
                + " "
                + shapeTokens().toStyleDeclarations()
                + " "
                + elevationTokens().toStyleDeclarations()
                + " "
                + motionTokens().toStyleDeclarations()
                + " "
                + stateLayerTokens().toStyleDeclarations()
                + " "
                + componentTokens().toStyleDeclarations();
    }

    /// Converts component tokens into JavaFX CSS rules for m3fx controls.
    default String toControlStyleRules() {
        return componentTokens().toControlStyleRules()
                + "\n\n"
                + stateLayerTokens().toControlStyleRules()
                + "\n\n"
                + elevationTokens().toControlStyleRules();
    }
}

/// Default immutable implementation of {@link M3TokenSet}.
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
record M3TokenSetImpl(
        M3Profile profile,
        M3ColorTokens colorTokens,
        M3TypographyTokens typographyTokens,
        M3ShapeTokens shapeTokens,
        M3ElevationTokens elevationTokens,
        M3MotionTokens motionTokens,
        M3StateLayerTokens stateLayerTokens,
        M3ComponentTokens componentTokens
) implements M3TokenSet {
    /// Creates a token set implementation.
    M3TokenSetImpl {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorTokens, "colorTokens");
        Objects.requireNonNull(typographyTokens, "typographyTokens");
        Objects.requireNonNull(shapeTokens, "shapeTokens");
        Objects.requireNonNull(elevationTokens, "elevationTokens");
        Objects.requireNonNull(motionTokens, "motionTokens");
        Objects.requireNonNull(stateLayerTokens, "stateLayerTokens");
        Objects.requireNonNull(componentTokens, "componentTokens");
    }
}
