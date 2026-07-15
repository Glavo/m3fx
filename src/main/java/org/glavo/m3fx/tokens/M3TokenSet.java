// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.tokens.M3TokenSetImpl;
import org.glavo.monetfx.ColorRole;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

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

    /// Creates a token set from explicit token groups.
    static M3TokenSet create(
            M3Profile profile,
            M3ColorTokens colorTokens,
            M3TypographyTokens typographyTokens,
            M3ShapeTokens shapeTokens,
            M3ElevationTokens elevationTokens,
            M3MotionTokens motionTokens,
            M3StateLayerTokens stateLayerTokens,
            M3ComponentTokens componentTokens
    ) {
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
        M3MotionTokens motionTokens = profile == M3Profile.EXPRESSIVE_2025
                ? M3MotionTokens.expressive()
                : M3MotionTokens.baseline();
        M3StateLayerTokens stateLayerTokens = M3StateLayerTokens.baseline();
        M3ComponentTokens componentTokens = M3ComponentTokens.create(profile, shapeTokens, density);

        return create(
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
                + stateColorStyleDeclarations(colorTokens(), stateLayerTokens())
                + " "
                + componentTokens().toStyleDeclarations()
                + " "
                + menuColorStyleDeclarations(profile(), colorTokens());
    }

    /// Converts state-dependent on-surface colors into reusable alpha-preserving JavaFX paints.
    private static String stateColorStyleDeclarations(
            M3ColorTokens colorTokens,
            M3StateLayerTokens stateLayerTokens
    ) {
        Color onSurface = colorTokens.get(ColorRole.ON_SURFACE);
        Color onSurfaceVariant = colorTokens.get(ColorRole.ON_SURFACE_VARIANT);
        Color surface = colorTokens.get(ColorRole.SURFACE);
        Color surfaceVariant = colorTokens.get(ColorRole.SURFACE_VARIANT);
        Color surfaceContainerHighest = colorTokens.get(ColorRole.SURFACE_CONTAINER_HIGHEST);
        Color outline = colorTokens.get(ColorRole.OUTLINE);
        Color error = colorTokens.get(ColorRole.ERROR);
        Color textFieldHoverContainer = surfaceContainerHighest.interpolate(
                onSurface,
                stateLayerTokens.hoverOpacity()
        );
        return "-m3-state-disabled-container-color: "
                + toRgba(onSurface, stateLayerTokens.disabledContainerOpacity()) + "; "
                + "-m3-disclosure-icon-color: " + toRgba(onSurfaceVariant, 1.0) + "; "
                + "-m3-state-disabled-content-color: "
                + toRgba(onSurface, stateLayerTokens.disabledContentOpacity()) + "; "
                + "-m3-button-disabled-container-color: " + toRgba(onSurface, 0.10) + "; "
                + "-m3-list-item-disabled-state-layer-color: " + toRgba(onSurface, 0.10) + "; "
                + "-m3-filled-card-disabled-container-color: "
                + toRgba(surfaceVariant, stateLayerTokens.disabledContentOpacity()) + "; "
                + "-m3-elevated-card-disabled-container-color: "
                + toRgba(surface, stateLayerTokens.disabledContentOpacity()) + "; "
                + "-m3-outlined-card-disabled-outline-color: "
                + toRgba(outline, stateLayerTokens.disabledContainerOpacity()) + "; "
                + "-m3-text-field-disabled-container-color: " + toRgba(onSurface, 0.04) + "; "
                + "-m3-text-field-hover-container-color: " + toRgba(textFieldHoverContainer, 1.0) + "; "
                + "-m3-text-input-trailing-icon-color: " + toRgba(error, 1.0) + ";";
    }

    /// Converts a color and component opacity into a JavaFX CSS rgba paint.
    private static String toRgba(Color color, double opacity) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        return "rgba(" + red + "," + green + "," + blue + "," + M3TokenCss.format(opacity) + ")";
    }

    /// Converts menu color mappings into JavaFX inline CSS declarations.
    private static String menuColorStyleDeclarations(M3Profile profile, M3ColorTokens colorTokens) {
        boolean expressive = profile == M3Profile.EXPRESSIVE_2025;
        String standardContainer = expressive ? "-m3-color-surface-container-low" : "-m3-color-surface-container";
        String standardSelectedContainer = expressive ? "-m3-color-tertiary-container" : "-m3-color-secondary-container";
        String standardSelectedContent = expressive ? "-m3-color-on-tertiary-container" : "-m3-color-on-secondary-container";
        Color selectedContainerColor = colorTokens.get(expressive
                ? ColorRole.TERTIARY_CONTAINER
                : ColorRole.SECONDARY_CONTAINER);
        return "-m3-menu-container-color: " + standardContainer + "; "
                + "-m3-menu-item-state-layer-color: -m3-color-on-surface; "
                + "-m3-menu-selected-item-container-color: " + standardSelectedContainer + "; "
                + "-m3-menu-selected-item-content-color: " + standardSelectedContent + "; "
                + "-m3-menu-selected-disabled-container-color: "
                + toRgba(selectedContainerColor, expressive ? 0.38 : 1.0) + "; "
                + "-m3-menu-vibrant-container-color: -m3-color-tertiary-container; "
                + "-m3-menu-vibrant-item-content-color: -m3-color-on-tertiary-container; "
                + "-m3-menu-vibrant-item-state-layer-color: -m3-color-on-tertiary-container; "
                + "-m3-menu-vibrant-selected-item-container-color: -m3-color-tertiary; "
                + "-m3-menu-vibrant-selected-item-content-color: -m3-color-on-tertiary;";
    }

    /// Converts component tokens into JavaFX CSS rules for M3FX controls.
    default String toControlStyleRules() {
        return typographyTokens().toControlStyleRules()
                + "\n\n"
                + componentTokens().toControlStyleRules()
                + "\n\n"
                + stateLayerTokens().toControlStyleRules()
                + "\n\n"
                + elevationTokens().toControlStyleRules();
    }
}
