// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.tokens.M3ColorTokensImpl;
import org.glavo.monetfx.ColorRole;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Wraps MonetFX color scheme output as M3FX color tokens.
///
/// Color tokens expose the Material Design 3 color roles generated from a MonetFX [ColorScheme]. They are
/// converted into both `-monet-*` and `-m3-color-*` JavaFX CSS variables so controls and application styles can
/// address the same dynamic color system.
///
/// See [Material Design color](https://m3.material.io/styles/color/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public sealed interface M3ColorTokens permits M3ColorTokensImpl {
    /// The default CSS prefix used for Monet color roles.
    String DEFAULT_CSS_PREFIX = "-monet";

    /// The m3fx CSS prefix used for Material color roles.
    String M3_CSS_PREFIX = "-m3-color";

    /// Returns the MonetFX color scheme used by this token set.
    ///
    /// @return the MonetFX color scheme backing this token set
    ColorScheme colorScheme();

    /// Creates color tokens from a MonetFX color scheme.
    ///
    /// @param colorScheme the MonetFX color scheme backing the created token set
    /// @return a color token set backed by the supplied color scheme
    static M3ColorTokens create(ColorScheme colorScheme) {
        return new M3ColorTokensImpl(colorScheme);
    }

    /// Returns the color for a MonetFX color role.
    ///
    /// @param role the MonetFX color role to resolve
    /// @return the JavaFX color for the supplied role
    default Color get(ColorRole role) {
        return colorScheme().getColor(role);
    }

    /// Returns all MonetFX color roles used by this token set.
    ///
    /// @return the immutable list of supported MonetFX color roles
    default @Unmodifiable List<ColorRole> roles() {
        return ColorRole.ALL;
    }

    /// Converts the color tokens into a JavaFX stylesheet rule for a style class.
    ///
    /// @param styleClass the style class selector without the leading dot
    /// @return a JavaFX CSS rule containing color token declarations
    default String toStyleSheet(String styleClass) {
        return colorScheme().toStyleSheet(styleClass, DEFAULT_CSS_PREFIX, roles());
    }

    /// Converts the color tokens into inline JavaFX CSS declarations.
    ///
    /// @return inline JavaFX CSS declarations for all supported color roles
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        for (ColorRole role : roles()) {
            String color = toRgb(get(role));
            M3TokenCss.append(builder, role.getVariableName(DEFAULT_CSS_PREFIX), color);
            M3TokenCss.append(builder, role.getVariableName(M3_CSS_PREFIX), color);
        }
        return builder.toString().trim();
    }

    /// Converts a color into a JavaFX CSS rgb value.
    ///
    /// @param color the JavaFX color to convert
    /// @return a JavaFX CSS `rgb(r,g,b)` color value
    static String toRgb(Color color) {
        int red = toChannel(color.getRed());
        int green = toChannel(color.getGreen());
        int blue = toChannel(color.getBlue());
        return "rgb(" + red + "," + green + "," + blue + ")";
    }

    /// Converts a color channel into an integer CSS channel.
    private static int toChannel(double value) {
        return (int) Math.round(value * 255.0);
    }
}
