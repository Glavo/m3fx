// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.tokens.M3ComponentTokenCssCompiler;
import org.glavo.m3fx.internal.tokens.M3TokenCssCompiler;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.tokens.M3ColorTokens;
import org.glavo.m3fx.tokens.M3ComponentTokens;
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
    /// @return JavaFX CSS declarations suitable for a generated root rule
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
    /// @return JavaFX CSS declarations suitable for a generated root rule
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
                + componentColorStyleDeclarations(tokens.componentTokens(), tokens.colorTokens());
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
        Color primary = colorTokens.get(ColorRole.PRIMARY);
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
                + "-m3-chip-leading-icon-color: " + toRgba(primary, 1.0) + "; "
                + "-m3-chip-trailing-icon-color: " + toRgba(onSurfaceVariant, 1.0) + "; "
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

    /// Compiles component color-role mappings as inherited CSS values.
    private static String componentColorStyleDeclarations(
            M3ComponentTokens componentTokens,
            M3ColorTokens colorTokens
    ) {
        M3ComponentTokens.MenuColorTokens menu = componentTokens.menu().colors();
        M3ComponentTokens.NavigationBarColorTokens navigationBar = componentTokens.navigationBar().colors();
        Color selectedContainerColor = colorTokens.get(menu.selectedItemContainerRole());
        return "-m3-menu-container-color: " + colorRoleVariable(menu.containerRole()) + "; "
                + "-m3-menu-item-state-layer-color: " + colorRoleVariable(menu.itemStateLayerRole()) + "; "
                + "-m3-menu-selected-item-container-color: "
                + colorRoleVariable(menu.selectedItemContainerRole()) + "; "
                + "-m3-menu-selected-item-content-color: "
                + colorRoleVariable(menu.selectedItemContentRole()) + "; "
                + "-m3-menu-selected-disabled-container-color: "
                + toRgba(selectedContainerColor, menu.selectedDisabledContainerOpacity()) + "; "
                + "-m3-menu-vibrant-container-color: "
                + colorRoleVariable(menu.vibrantContainerRole()) + "; "
                + "-m3-menu-vibrant-item-content-color: "
                + colorRoleVariable(menu.vibrantItemContentRole()) + "; "
                + "-m3-menu-vibrant-item-state-layer-color: "
                + colorRoleVariable(menu.vibrantItemStateLayerRole()) + "; "
                + "-m3-menu-vibrant-selected-item-container-color: "
                + colorRoleVariable(menu.vibrantSelectedItemContainerRole()) + "; "
                + "-m3-menu-vibrant-selected-item-content-color: "
                + colorRoleVariable(menu.vibrantSelectedItemContentRole()) + "; "
                + "-m3-menu-vibrant-interaction-icon-color: "
                + colorRoleVariable(menu.vibrantInteractionIconRole()) + "; "
                + "-m3-navigation-bar-selected-label-color: "
                + colorRoleVariable(navigationBar.selectedLabelRole()) + "; "
                + "-m3-navigation-bar-state-layer-color: "
                + colorRoleVariable(navigationBar.stateLayerRole()) + ";";
    }

    /// Returns the generated Material CSS variable for a MonetFX color role.
    private static String colorRoleVariable(ColorRole role) {
        return role.getVariableName("-m3-color");
    }
}
