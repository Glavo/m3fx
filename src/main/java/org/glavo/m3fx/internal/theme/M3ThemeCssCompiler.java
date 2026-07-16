// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.tokens.M3ComponentTokenCssCompiler;
import org.glavo.m3fx.internal.tokens.M3TokenCssCompiler;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.tokens.M3ColorTokens;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.glavo.monetfx.ColorRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;
import java.util.Objects;

/// Compiles structured Material tokens into the JavaFX CSS consumed by M3FX.
///
/// This class belongs to the rendering backend. Public theme and token APIs intentionally expose structured values
/// rather than generated stylesheet text.
@NotNullByDefault
public final class M3ThemeCssCompiler {
    /// Prevents utility class instantiation.
    private M3ThemeCssCompiler() {
    }

    /// Compiles the root declarations for a theme.
    ///
    /// @param theme the theme to compile
    /// @return JavaFX inline CSS declarations
    public static String rootStyleDeclarations(M3Theme theme) {
        return rootStyleDeclarations(Objects.requireNonNull(theme, "theme").tokens());
    }

    /// Compiles control rules for a theme.
    ///
    /// @param theme the theme to compile
    /// @return JavaFX CSS rules
    public static String controlStyleRules(M3Theme theme) {
        return controlStyleRules(Objects.requireNonNull(theme, "theme").tokens());
    }

    /// Compiles root-level declarations for a complete token set.
    ///
    /// @param tokens the token set to compile
    /// @return JavaFX inline CSS declarations
    public static String rootStyleDeclarations(M3TokenSet tokens) {
        return M3TokenCssCompiler.styleDeclarations(tokens.colorTokens())
                + " "
                + M3TokenCssCompiler.styleDeclarations(tokens.typographyTokens())
                + " "
                + M3TokenCssCompiler.styleDeclarations(tokens.shapeTokens())
                + " "
                + M3TokenCssCompiler.styleDeclarations(tokens.elevationTokens())
                + " "
                + M3TokenCssCompiler.styleDeclarations(tokens.motionTokens())
                + " "
                + M3TokenCssCompiler.styleDeclarations(tokens.stateLayerTokens())
                + " "
                + stateColorStyleDeclarations(tokens.colorTokens(), tokens.stateLayerTokens())
                + " "
                + M3ComponentTokenCssCompiler.styleDeclarations(tokens.componentTokens())
                + " "
                + menuColorStyleDeclarations(tokens.profile(), tokens.colorTokens());
    }

    /// Compiles component selector rules for a complete token set.
    ///
    /// @param tokens the token set to compile
    /// @return JavaFX CSS rules
    public static String controlStyleRules(M3TokenSet tokens) {
        return M3TokenCssCompiler.controlStyleRules(tokens.typographyTokens())
                + "\n\n"
                + M3ComponentTokenCssCompiler.controlStyleRules(tokens.componentTokens())
                + "\n\n"
                + M3TokenCssCompiler.controlStyleRules(tokens.stateLayerTokens())
                + "\n\n"
                + M3TokenCssCompiler.controlStyleRules(tokens.elevationTokens());
    }

    /// Compiles state-dependent colors that combine role colors with state opacities.
    private static String stateColorStyleDeclarations(
            M3ColorTokens colorTokens,
            M3StateLayerTokens stateLayerTokens
    ) {
        Color onSurface = colorTokens.get(ColorRole.ON_SURFACE);
        Color onSurfaceVariant = colorTokens.get(ColorRole.ON_SURFACE_VARIANT);
        Color surface = colorTokens.get(ColorRole.SURFACE);
        @SuppressWarnings("deprecation")
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
                + "-m3-button-icon-color: " + toRgba(onSurfaceVariant, 1.0) + "; "
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

    /// Converts one color and opacity to a JavaFX CSS rgba paint.
    private static String toRgba(Color color, double opacity) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        return "rgba(" + red + "," + green + "," + blue + "," + format(opacity) + ")";
    }

    /// Formats one decimal with stable locale-independent output.
    private static String format(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /// Compiles menu color mappings selected by the active Material profile.
    private static String menuColorStyleDeclarations(M3Profile profile, M3ColorTokens colorTokens) {
        boolean expressive = profile == M3Profile.EXPRESSIVE_2025;
        String standardContainer = expressive ? "-m3-color-surface-container-low" : "-m3-color-surface-container";
        String standardSelectedContainer = expressive ? "-m3-color-tertiary-container" : "-m3-color-secondary-container";
        String standardSelectedContent = expressive
                ? "-m3-color-on-tertiary-container"
                : "-m3-color-on-secondary-container";
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
}
