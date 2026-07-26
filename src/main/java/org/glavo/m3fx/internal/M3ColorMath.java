// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorChannel;
import org.glavo.m3fx.controls.M3ColorSpace;
import org.glavo.m3fx.controls.M3HsbColor;
import org.glavo.m3fx.controls.M3HslColor;
import org.glavo.m3fx.controls.M3RgbColor;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides color-space conversion, hexadecimal formatting, and validation for M3FX color controls.
@NotNullByDefault
public final class M3ColorMath {
    /// The largest unsigned 16-bit channel sample.
    private static final int MAXIMUM_CANONICAL_CHANNEL = 0xFFFF;

    /// Hexadecimal digits used by allocation-conscious color formatting.
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    /// Prevents utility class instantiation.
    private M3ColorMath() {
    }

    /// Validates a normalized channel value.
    ///
    /// @param value the value to validate
    /// @param name  the channel name used in the exception message
    /// @throws IllegalArgumentException if `value` is not finite or is outside `0.0..1.0`
    public static void requireUnit(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be finite and in the range 0.0 through 1.0: " + value);
        }
    }

    /// Validates a hue in degrees.
    ///
    /// @param value the hue to validate
    /// @param name  the channel name used in the exception message
    /// @throws IllegalArgumentException if `value` is not finite or is outside `0.0..360.0`
    public static void requireHue(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 360.0) {
            throw new IllegalArgumentException(name + " must be finite and in the range 0.0 through 360.0: " + value);
        }
    }

    /// Wraps a finite angle to `0.0..360.0`, excluding `360.0`.
    ///
    /// @param value the angle in degrees
    /// @return the wrapped angle
    /// @throws IllegalArgumentException if `value` is not finite
    public static double wrapHue(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("hue must be finite: " + value);
        }
        double wrapped = value % 360.0;
        if (wrapped < 0.0) {
            wrapped += 360.0;
        }
        return wrapped == -0.0 ? 0.0 : wrapped;
    }

    /// Converts any supported color to RGB.
    ///
    /// @param color the source color
    /// @return the equivalent RGB value
    /// @throws NullPointerException if `color` is `null`
    public static M3RgbColor toRgb(M3Color color) {
        Objects.requireNonNull(color, "color");
        if (color instanceof M3RgbColor rgb) {
            return rgb;
        }
        if (color instanceof M3HsbColor hsb) {
            return hsbToRgb(hsb);
        }

        M3HslColor hsl = (M3HslColor) color;
        double lightness = hsl.lightness();
        double saturation = hsl.saturation();
        if (saturation == 0.0) {
            return new M3RgbColor(lightness, lightness, lightness, hsl.alpha());
        }

        double hue = hsl.hue() / 360.0;
        double second = lightness < 0.5
                ? lightness * (1.0 + saturation)
                : lightness + saturation - lightness * saturation;
        double first = 2.0 * lightness - second;
        return new M3RgbColor(
                hueToRgb(first, second, hue + 1.0 / 3.0),
                hueToRgb(first, second, hue),
                hueToRgb(first, second, hue - 1.0 / 3.0),
                hsl.alpha()
        );
    }

    /// Converts any supported color to HSL.
    ///
    /// Direct HSB conversion preserves the source hue even when the rendered color is achromatic.
    ///
    /// @param color the source color
    /// @return the equivalent HSL value
    /// @throws NullPointerException if `color` is `null`
    public static M3HslColor toHsl(M3Color color) {
        Objects.requireNonNull(color, "color");
        if (color instanceof M3HslColor hsl) {
            return hsl;
        }
        if (color instanceof M3HsbColor hsb) {
            double lightness = hsb.brightness() * (1.0 - hsb.saturation() / 2.0);
            double saturation = lightness == 0.0 || lightness == 1.0
                    ? 0.0
                    : (hsb.brightness() - lightness) / Math.min(lightness, 1.0 - lightness);
            return new M3HslColor(hsb.hue(), saturation, lightness, hsb.alpha());
        }

        M3RgbColor rgb = (M3RgbColor) color;
        double maximum = Math.max(rgb.red(), Math.max(rgb.green(), rgb.blue()));
        double minimum = Math.min(rgb.red(), Math.min(rgb.green(), rgb.blue()));
        double delta = maximum - minimum;
        double lightness = (maximum + minimum) / 2.0;
        double saturation = delta == 0.0
                ? 0.0
                : delta / (1.0 - Math.abs(2.0 * lightness - 1.0));
        return new M3HslColor(rgbHue(rgb, maximum, delta), saturation, lightness, rgb.alpha());
    }

    /// Converts any supported color to HSB.
    ///
    /// Direct HSL conversion preserves the source hue even when the rendered color is achromatic.
    ///
    /// @param color the source color
    /// @return the equivalent HSB value
    /// @throws NullPointerException if `color` is `null`
    public static M3HsbColor toHsb(M3Color color) {
        Objects.requireNonNull(color, "color");
        if (color instanceof M3HsbColor hsb) {
            return hsb;
        }
        if (color instanceof M3HslColor hsl) {
            double brightness = hsl.lightness()
                    + hsl.saturation() * Math.min(hsl.lightness(), 1.0 - hsl.lightness());
            double saturation = brightness == 0.0
                    ? 0.0
                    : 2.0 * (1.0 - hsl.lightness() / brightness);
            return new M3HsbColor(hsl.hue(), saturation, brightness, hsl.alpha());
        }

        M3RgbColor rgb = (M3RgbColor) color;
        double maximum = Math.max(rgb.red(), Math.max(rgb.green(), rgb.blue()));
        double minimum = Math.min(rgb.red(), Math.min(rgb.green(), rgb.blue()));
        double delta = maximum - minimum;
        double saturation = maximum == 0.0 ? 0.0 : delta / maximum;
        return new M3HsbColor(rgbHue(rgb, maximum, delta), saturation, maximum, rgb.alpha());
    }

    /// Returns a color converted to the supplied space with one channel replaced.
    ///
    /// @param color      the source color
    /// @param colorSpace the editing color space
    /// @param channel    the channel to replace
    /// @param value      the replacement value
    /// @return the updated color in `colorSpace`
    /// @throws NullPointerException if an object argument is `null`
    /// @throws IllegalArgumentException if the channel does not belong to `colorSpace` or the value is invalid
    public static M3Color withChannel(
            M3Color color,
            M3ColorSpace colorSpace,
            M3ColorChannel channel,
            double value
    ) {
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(colorSpace, "colorSpace");
        Objects.requireNonNull(channel, "channel");
        if (!colorSpace.supports(channel)) {
            throw new IllegalArgumentException(colorSpace + " does not contain channel " + channel);
        }
        return color.toColorSpace(colorSpace).withChannel(channel, value);
    }

    /// Resolves the color space used to edit a channel of a value.
    ///
    /// RGB channels select RGB, lightness selects HSL, and brightness selects HSB. Hue and saturation preserve an
    /// existing HSL or HSB value and otherwise select HSB. Alpha preserves the source color space.
    ///
    /// @param color   the current color
    /// @param channel the channel to edit
    /// @return the compatible editing color space
    /// @throws NullPointerException if an argument is `null`
    public static M3ColorSpace editingSpace(M3Color color, M3ColorChannel channel) {
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(channel, "channel");
        return switch (channel) {
            case RED, GREEN, BLUE -> M3ColorSpace.RGB;
            case LIGHTNESS -> M3ColorSpace.HSL;
            case BRIGHTNESS -> M3ColorSpace.HSB;
            case HUE, SATURATION -> color.getColorSpace() == M3ColorSpace.HSL
                    ? M3ColorSpace.HSL
                    : M3ColorSpace.HSB;
            case ALPHA -> color.getColorSpace();
        };
    }

    /// Returns the canonical 16-bit RGBA key for a color.
    ///
    /// Each rendered channel is rounded to an unsigned 16-bit sample. Equal keys therefore form a transitive
    /// relation suitable for collection uniqueness and deterministic selection.
    ///
    /// @param color the color to canonicalize
    /// @return the packed 64-bit RGBA key
    /// @throws NullPointerException if `color` is `null`
    public static long canonicalRgbaKey(M3Color color) {
        M3RgbColor rgb = toRgb(Objects.requireNonNull(color, "color"));
        return ((long) toCanonicalChannel(rgb.red()) << 48)
                | ((long) toCanonicalChannel(rgb.green()) << 32)
                | ((long) toCanonicalChannel(rgb.blue()) << 16)
                | toCanonicalChannel(rgb.alpha());
    }

    /// Converts a color directly to a non-premultiplied ARGB pixel.
    ///
    /// @param color the color to convert
    /// @return the packed ARGB pixel
    /// @throws NullPointerException if `color` is `null`
    public static int toArgb(M3Color color) {
        Objects.requireNonNull(color, "color");
        if (color instanceof M3RgbColor rgb) {
            return packArgb(rgb.red(), rgb.green(), rgb.blue(), rgb.alpha());
        } else if (color instanceof M3HslColor hsl) {
            return hslToArgb(hsl.hue(), hsl.saturation(), hsl.lightness(), hsl.alpha());
        } else {
            M3HsbColor hsb = (M3HsbColor) color;
            return hsbToArgb(hsb.hue(), hsb.saturation(), hsb.brightness(), hsb.alpha());
        }
    }

    /// Converts ordered channel values directly to a non-premultiplied ARGB pixel.
    ///
    /// The first three values correspond to [M3ColorSpace#getChannels()] in order. This internal rendering path
    /// assumes that all components are finite and within their declared ranges.
    ///
    /// @param colorSpace the source color space
    /// @param first      the first ordered color channel
    /// @param second     the second ordered color channel
    /// @param third      the third ordered color channel
    /// @param alpha      the normalized alpha channel
    /// @return the packed ARGB pixel
    /// @throws NullPointerException if `colorSpace` is `null`
    public static int toArgb(
            M3ColorSpace colorSpace,
            double first,
            double second,
            double third,
            double alpha
    ) {
        return switch (colorSpace) {
            case RGB -> packArgb(first, second, third, alpha);
            case HSL -> hslToArgb(first, second, third, alpha);
            case HSB -> hsbToArgb(first, second, third, alpha);
        };
    }

    /// Formats a color as `#RRGGBB` or `#RRGGBBAA`.
    ///
    /// Alpha is included when `includeAlpha` is true.
    ///
    /// @param color        the color to format
    /// @param includeAlpha whether to include an alpha byte
    /// @return the uppercase hexadecimal color
    /// @throws NullPointerException if `color` is `null`
    public static String formatHex(M3Color color, boolean includeAlpha) {
        M3RgbColor rgb = toRgb(Objects.requireNonNull(color, "color"));
        int length = includeAlpha ? 9 : 7;
        char[] result = new char[length];
        result[0] = '#';
        writeByte(result, 1, toByte(rgb.red()));
        writeByte(result, 3, toByte(rgb.green()));
        writeByte(result, 5, toByte(rgb.blue()));
        if (includeAlpha) {
            writeByte(result, 7, toByte(rgb.alpha()));
        }
        return new String(result);
    }

    /// Parses CSS-style three-, four-, six-, or eight-digit hexadecimal RGB text.
    ///
    /// A leading `#` is optional. Four- and eight-digit forms place alpha after the blue channel. Surrounding
    /// whitespace is ignored.
    ///
    /// @param text the text to parse
    /// @return the parsed RGB color, or `null` when the text is not a supported hexadecimal form
    /// @throws NullPointerException if `text` is `null`
    public static @Nullable M3RgbColor parseHex(String text) {
        Objects.requireNonNull(text, "text");
        String value = text.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        int length = value.length();
        if (length != 3 && length != 4 && length != 6 && length != 8) {
            return null;
        }

        try {
            if (length == 3 || length == 4) {
                int red = expandedNibble(value.charAt(0));
                int green = expandedNibble(value.charAt(1));
                int blue = expandedNibble(value.charAt(2));
                int alpha = length == 4 ? expandedNibble(value.charAt(3)) : 255;
                return fromBytes(red, green, blue, alpha);
            }
            int red = parsedByte(value, 0);
            int green = parsedByte(value, 2);
            int blue = parsedByte(value, 4);
            int alpha = length == 8 ? parsedByte(value, 6) : 255;
            return fromBytes(red, green, blue, alpha);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /// Returns a stable accessible description for a color.
    ///
    /// @param color the color to describe
    /// @return an uppercase hexadecimal RGBA description
    /// @throws NullPointerException if `color` is `null`
    public static String describe(M3Color color) {
        Objects.requireNonNull(color, "color");
        return formatHex(color, color.getAlpha() < 1.0);
    }

    /// Returns the HSL/HSB hue represented by an RGB value.
    private static double rgbHue(M3RgbColor rgb, double maximum, double delta) {
        if (delta == 0.0) {
            return 0.0;
        }
        double hue;
        if (maximum == rgb.red()) {
            hue = 60.0 * (((rgb.green() - rgb.blue()) / delta) % 6.0);
        } else if (maximum == rgb.green()) {
            hue = 60.0 * (((rgb.blue() - rgb.red()) / delta) + 2.0);
        } else {
            hue = 60.0 * (((rgb.red() - rgb.green()) / delta) + 4.0);
        }
        return hue < 0.0 ? hue + 360.0 : hue;
    }

    /// Converts HSB channels directly to double-precision RGB channels.
    private static M3RgbColor hsbToRgb(M3HsbColor hsb) {
        double hue = wrapHue(hsb.hue());
        double chroma = hsb.brightness() * hsb.saturation();
        double secondary = chroma * (1.0 - Math.abs((hue / 60.0) % 2.0 - 1.0));
        double red;
        double green;
        double blue;
        if (hue < 60.0) {
            red = chroma;
            green = secondary;
            blue = 0.0;
        } else if (hue < 120.0) {
            red = secondary;
            green = chroma;
            blue = 0.0;
        } else if (hue < 180.0) {
            red = 0.0;
            green = chroma;
            blue = secondary;
        } else if (hue < 240.0) {
            red = 0.0;
            green = secondary;
            blue = chroma;
        } else if (hue < 300.0) {
            red = secondary;
            green = 0.0;
            blue = chroma;
        } else {
            red = chroma;
            green = 0.0;
            blue = secondary;
        }

        double match = hsb.brightness() - chroma;
        return new M3RgbColor(red + match, green + match, blue + match, hsb.alpha());
    }

    /// Converts primitive HSL components directly to one ARGB pixel.
    private static int hslToArgb(double hue, double saturation, double lightness, double alpha) {
        if (saturation == 0.0) {
            return packArgb(lightness, lightness, lightness, alpha);
        }

        double normalizedHue = hue / 360.0;
        double second = lightness < 0.5
                ? lightness * (1.0 + saturation)
                : lightness + saturation - lightness * saturation;
        double first = 2.0 * lightness - second;
        return packArgb(
                hueToRgb(first, second, normalizedHue + 1.0 / 3.0),
                hueToRgb(first, second, normalizedHue),
                hueToRgb(first, second, normalizedHue - 1.0 / 3.0),
                alpha
        );
    }

    /// Converts primitive HSB components directly to one ARGB pixel.
    private static int hsbToArgb(double hue, double saturation, double brightness, double alpha) {
        double wrappedHue = hue == 360.0 ? 0.0 : hue;
        double chroma = brightness * saturation;
        double secondary = chroma * (1.0 - Math.abs((wrappedHue / 60.0) % 2.0 - 1.0));
        double red;
        double green;
        double blue;
        if (wrappedHue < 60.0) {
            red = chroma;
            green = secondary;
            blue = 0.0;
        } else if (wrappedHue < 120.0) {
            red = secondary;
            green = chroma;
            blue = 0.0;
        } else if (wrappedHue < 180.0) {
            red = 0.0;
            green = chroma;
            blue = secondary;
        } else if (wrappedHue < 240.0) {
            red = 0.0;
            green = secondary;
            blue = chroma;
        } else if (wrappedHue < 300.0) {
            red = secondary;
            green = 0.0;
            blue = chroma;
        } else {
            red = chroma;
            green = 0.0;
            blue = secondary;
        }
        double match = brightness - chroma;
        return packArgb(red + match, green + match, blue + match, alpha);
    }

    /// Evaluates one HSL hue sector.
    private static double hueToRgb(double first, double second, double hue) {
        double wrapped = hue;
        if (wrapped < 0.0) {
            wrapped += 1.0;
        } else if (wrapped > 1.0) {
            wrapped -= 1.0;
        }
        if (wrapped < 1.0 / 6.0) {
            return first + (second - first) * 6.0 * wrapped;
        }
        if (wrapped < 1.0 / 2.0) {
            return second;
        }
        if (wrapped < 2.0 / 3.0) {
            return first + (second - first) * (2.0 / 3.0 - wrapped) * 6.0;
        }
        return first;
    }

    /// Converts a normalized component to the canonical unsigned 16-bit sample.
    private static int toCanonicalChannel(double value) {
        return (int) Math.round(value * MAXIMUM_CANONICAL_CHANNEL);
    }

    /// Packs normalized RGBA components into a non-premultiplied ARGB pixel.
    private static int packArgb(double red, double green, double blue, double alpha) {
        return toByte(alpha) << 24
                | toByte(red) << 16
                | toByte(green) << 8
                | toByte(blue);
    }

    /// Converts a normalized component to an unsigned byte with nearest-value rounding.
    private static int toByte(double value) {
        return (int) Math.round(Math.max(0.0, Math.min(1.0, value)) * 255.0);
    }

    /// Writes an unsigned byte as two uppercase hexadecimal digits.
    private static void writeByte(char[] target, int offset, int value) {
        target[offset] = HEX_DIGITS[(value >>> 4) & 0xF];
        target[offset + 1] = HEX_DIGITS[value & 0xF];
    }

    /// Parses two hexadecimal digits at `offset`.
    private static int parsedByte(String value, int offset) {
        int high = Character.digit(value.charAt(offset), 16);
        int low = Character.digit(value.charAt(offset + 1), 16);
        if (high < 0 || low < 0) {
            throw new IllegalArgumentException("invalid hexadecimal byte");
        }
        return (high << 4) | low;
    }

    /// Expands one hexadecimal nibble to a repeated-byte value.
    private static int expandedNibble(char value) {
        int nibble = Character.digit(value, 16);
        if (nibble < 0) {
            throw new IllegalArgumentException("invalid hexadecimal nibble");
        }
        return (nibble << 4) | nibble;
    }

    /// Creates an RGB color from unsigned byte channels.
    private static M3RgbColor fromBytes(int red, int green, int blue, int alpha) {
        return new M3RgbColor(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);
    }
}
