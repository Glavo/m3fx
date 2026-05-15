package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Formats token values for JavaFX CSS output.
@NotNullByDefault
final class M3TokenCss {
    /// Prevents utility class instantiation.
    private M3TokenCss() {
    }

    /// Formats a decimal number with stable locale-independent output.
    static String format(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /// Formats a decimal number as a pixel value.
    static String pixels(double value) {
        return format(value) + "px";
    }

    /// Appends a CSS declaration to a string builder.
    static void append(StringBuilder builder, String name, String value) {
        builder.append(name).append(": ").append(value).append("; ");
    }
}
