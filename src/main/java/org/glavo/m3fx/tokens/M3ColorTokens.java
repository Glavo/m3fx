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
public record M3ColorTokens(
        /// The MonetFX color scheme used by this token set.
        ColorScheme colorScheme
) {
    /// The default CSS prefix used for Monet color roles.
    public static final String DEFAULT_CSS_PREFIX = "-monet";

    /// Creates color tokens.
    public M3ColorTokens {
        Objects.requireNonNull(colorScheme, "colorScheme");
    }

    /// Returns the color for a MonetFX color role.
    public Color get(ColorRole role) {
        return colorScheme.getColor(role);
    }

    /// Returns all MonetFX color roles used by this token set.
    public @Unmodifiable List<ColorRole> roles() {
        return ColorRole.ALL;
    }

    /// Converts the color tokens into a JavaFX stylesheet rule for a style class.
    public String toStyleSheet(String styleClass) {
        return colorScheme.toStyleSheet(styleClass, DEFAULT_CSS_PREFIX, roles());
    }

    /// Converts the color tokens into inline JavaFX CSS declarations.
    public String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        for (ColorRole role : roles()) {
            builder.append(role.getVariableName(DEFAULT_CSS_PREFIX))
                    .append(": ")
                    .append(toRgb(get(role)))
                    .append("; ");
        }
        return builder.toString().trim();
    }

    /// Converts a color into a JavaFX CSS rgb value.
    public static String toRgb(Color color) {
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
