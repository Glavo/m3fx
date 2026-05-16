package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import org.glavo.monetfx.ColorRole;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/// Wraps MonetFX color scheme output as m3fx color tokens.
@NotNullByDefault
public sealed interface M3ColorTokens permits M3ColorTokensImpl {
    /// The default CSS prefix used for Monet color roles.
    String DEFAULT_CSS_PREFIX = "-monet";

    /// The m3fx CSS prefix used for Material color roles.
    String M3_CSS_PREFIX = "-m3-color";

    /// Returns the MonetFX color scheme used by this token set.
    ColorScheme colorScheme();

    /// Creates color tokens from a MonetFX color scheme.
    static M3ColorTokens create(ColorScheme colorScheme) {
        return new M3ColorTokensImpl(colorScheme);
    }

    /// Returns the color for a MonetFX color role.
    default Color get(ColorRole role) {
        return colorScheme().getColor(role);
    }

    /// Returns all MonetFX color roles used by this token set.
    default @Unmodifiable List<ColorRole> roles() {
        return ColorRole.ALL;
    }

    /// Converts the color tokens into a JavaFX stylesheet rule for a style class.
    default String toStyleSheet(String styleClass) {
        return colorScheme().toStyleSheet(styleClass, DEFAULT_CSS_PREFIX, roles());
    }

    /// Converts the color tokens into inline JavaFX CSS declarations.
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

/// Default immutable implementation of {@link M3ColorTokens}.
///
/// @param colorScheme the MonetFX color scheme used by this token set
@NotNullByDefault
record M3ColorTokensImpl(ColorScheme colorScheme) implements M3ColorTokens {
    /// Creates color tokens.
    M3ColorTokensImpl {
        Objects.requireNonNull(colorScheme, "colorScheme");
    }
}
