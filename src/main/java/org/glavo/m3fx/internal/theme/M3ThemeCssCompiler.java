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
        StringBuilder builder = new StringBuilder();
        appendRootStyleDeclarations(builder, tokens);
        return builder.toString();
    }

    /// Appends root-level declarations for a complete token set to an existing CSS buffer.
    ///
    /// @param builder the destination CSS buffer
    /// @param tokens  the token set to compile
    /// @throws NullPointerException if `builder` or `tokens` is `null`
    public static void appendRootStyleDeclarations(StringBuilder builder, M3TokenSet tokens) {
        Objects.requireNonNull(builder, "builder");
        Objects.requireNonNull(tokens, "tokens");
        int start = builder.length();
        M3TokenCssCompiler.appendStyleDeclarations(builder, tokens.colorTokens());
        M3TokenCssCompiler.appendStyleDeclarations(builder, tokens.typographyTokens());
        M3TokenCssCompiler.appendStyleDeclarations(builder, tokens.shapeTokens());
        M3TokenCssCompiler.appendStyleDeclarations(builder, tokens.elevationTokens());
        M3TokenCssCompiler.appendStyleDeclarations(builder, tokens.motionTokens());
        M3TokenCssCompiler.appendStyleDeclarations(builder, tokens.stateLayerTokens());
        appendStateColorStyleDeclarations(builder, tokens.colorTokens(), tokens.stateLayerTokens());
        M3ComponentTokenCssCompiler.appendStyleDeclarations(builder, tokens.componentTokens());
        appendComponentColorStyleDeclarations(builder, tokens.componentTokens(), tokens.colorTokens());
        stripTrailingWhitespace(builder, start);
    }

    /// Compiles component selector rules for a complete token set.
    ///
    /// @param tokens the token set to compile
    /// @return JavaFX CSS rules
    public static String controlStyleRules(M3TokenSet tokens) {
        StringBuilder builder = new StringBuilder();
        appendControlStyleRules(builder, tokens);
        return builder.toString();
    }

    /// Appends component selector rules for a complete token set to an existing CSS buffer.
    ///
    /// @param builder the destination CSS buffer
    /// @param tokens  the token set to compile
    /// @throws NullPointerException if `builder` or `tokens` is `null`
    public static void appendControlStyleRules(StringBuilder builder, M3TokenSet tokens) {
        Objects.requireNonNull(builder, "builder");
        Objects.requireNonNull(tokens, "tokens");
        int start = builder.length();
        M3TokenCssCompiler.appendControlStyleRules(builder, tokens.typographyTokens());
        M3ComponentTokenCssCompiler.appendControlStyleRules(builder, tokens.componentTokens());
        M3TokenCssCompiler.appendControlStyleRules(builder, tokens.stateLayerTokens());
        M3TokenCssCompiler.appendControlStyleRules(builder, tokens.elevationTokens());
        stripTrailingWhitespace(builder, start);
    }

    /// Appends state-dependent colors that combine role colors with state opacities.
    private static void appendStateColorStyleDeclarations(
            StringBuilder builder,
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
        appendRgbaDeclaration(
                builder,
                "-m3-state-disabled-container-color",
                onSurface,
                stateLayerTokens.disabledContainerOpacity()
        );
        appendRgbaDeclaration(builder, "-m3-button-icon-color", onSurfaceVariant, 1.0);
        appendRgbaDeclaration(builder, "-m3-chip-leading-icon-color", primary, 1.0);
        appendRgbaDeclaration(builder, "-m3-chip-trailing-icon-color", onSurfaceVariant, 1.0);
        appendRgbaDeclaration(builder, "-m3-disclosure-icon-color", onSurfaceVariant, 1.0);
        appendRgbaDeclaration(
                builder,
                "-m3-state-disabled-content-color",
                onSurface,
                stateLayerTokens.disabledContentOpacity()
        );
        appendRgbaDeclaration(builder, "-m3-button-disabled-container-color", onSurface, 0.10);
        appendRgbaDeclaration(builder, "-m3-list-item-disabled-state-layer-color", onSurface, 0.10);
        appendRgbaDeclaration(
                builder,
                "-m3-filled-card-disabled-container-color",
                surfaceVariant,
                stateLayerTokens.disabledContentOpacity()
        );
        appendRgbaDeclaration(
                builder,
                "-m3-elevated-card-disabled-container-color",
                surface,
                stateLayerTokens.disabledContentOpacity()
        );
        appendRgbaDeclaration(
                builder,
                "-m3-outlined-card-disabled-outline-color",
                outline,
                stateLayerTokens.disabledContainerOpacity()
        );
        appendRgbaDeclaration(builder, "-m3-text-field-disabled-container-color", onSurface, 0.04);
        appendRgbaDeclaration(builder, "-m3-text-field-hover-container-color", textFieldHoverContainer, 1.0);
        appendRgbaDeclaration(builder, "-m3-text-input-trailing-icon-color", error, 1.0);
    }

    /// Appends one color declaration as a JavaFX CSS rgba paint.
    private static void appendRgbaDeclaration(StringBuilder builder, String name, Color color, double opacity) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        builder.append(name)
                .append(": rgba(")
                .append(red)
                .append(',')
                .append(green)
                .append(',')
                .append(blue)
                .append(',')
                .append(format(opacity))
                .append("); ");
    }

    /// Formats one decimal with stable locale-independent output.
    private static String format(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /// Appends component color-role mappings as inherited CSS values.
    private static void appendComponentColorStyleDeclarations(
            StringBuilder builder,
            M3ComponentTokens componentTokens,
            M3ColorTokens colorTokens
    ) {
        M3ComponentTokens.MenuColorTokens menu = componentTokens.menu().colors();
        M3ComponentTokens.NavigationBarColorTokens navigationBar = componentTokens.navigationBar().colors();
        Color selectedContainerColor = colorTokens.get(menu.selectedItemContainerRole());
        appendDeclaration(builder, "-m3-menu-container-color", colorRoleVariable(menu.containerRole()));
        appendDeclaration(builder, "-m3-menu-item-state-layer-color", colorRoleVariable(menu.itemStateLayerRole()));
        appendDeclaration(
                builder,
                "-m3-menu-selected-item-container-color",
                colorRoleVariable(menu.selectedItemContainerRole())
        );
        appendDeclaration(
                builder,
                "-m3-menu-selected-item-content-color",
                colorRoleVariable(menu.selectedItemContentRole())
        );
        appendRgbaDeclaration(
                builder,
                "-m3-menu-selected-disabled-container-color",
                selectedContainerColor,
                menu.selectedDisabledContainerOpacity()
        );
        appendDeclaration(builder, "-m3-menu-vibrant-container-color", colorRoleVariable(menu.vibrantContainerRole()));
        appendDeclaration(
                builder,
                "-m3-menu-vibrant-item-content-color",
                colorRoleVariable(menu.vibrantItemContentRole())
        );
        appendDeclaration(
                builder,
                "-m3-menu-vibrant-item-state-layer-color",
                colorRoleVariable(menu.vibrantItemStateLayerRole())
        );
        appendDeclaration(
                builder,
                "-m3-menu-vibrant-selected-item-container-color",
                colorRoleVariable(menu.vibrantSelectedItemContainerRole())
        );
        appendDeclaration(
                builder,
                "-m3-menu-vibrant-selected-item-content-color",
                colorRoleVariable(menu.vibrantSelectedItemContentRole())
        );
        appendDeclaration(
                builder,
                "-m3-menu-vibrant-interaction-icon-color",
                colorRoleVariable(menu.vibrantInteractionIconRole())
        );
        appendDeclaration(
                builder,
                "-m3-navigation-bar-selected-label-color",
                colorRoleVariable(navigationBar.selectedLabelRole())
        );
        appendDeclaration(
                builder,
                "-m3-navigation-bar-state-layer-color",
                colorRoleVariable(navigationBar.stateLayerRole())
        );
    }

    /// Appends one CSS declaration to the destination buffer.
    private static void appendDeclaration(StringBuilder builder, String name, String value) {
        builder.append(name).append(": ").append(value).append("; ");
    }

    /// Removes trailing whitespace appended by one compiler section without touching earlier buffer content.
    private static void stripTrailingWhitespace(StringBuilder builder, int start) {
        int end = builder.length();
        while (end > start && Character.isWhitespace(builder.charAt(end - 1))) {
            end--;
        }
        builder.setLength(end);
    }

    /// Returns the generated Material CSS variable for a MonetFX color role.
    private static String colorRoleVariable(ColorRole role) {
        return role.getVariableName("-m3-color");
    }
}
