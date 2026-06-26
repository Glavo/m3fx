// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TypographyTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 typography system tokens.
///
/// Typography tokens describe the Material type scale roles used by controls and by
/// [M3Text][org.glavo.m3fx.controls.M3Text]. Each role maps to an immutable [M3TextStyle] containing font
/// family, size, line height, weight, and tracking values that are converted into JavaFX CSS declarations.
///
/// See [Material Design typography](https://m3.material.io/styles/typography/overview).
@NotNullByDefault
public sealed interface M3TypographyTokens permits M3TypographyTokensImpl {
    /// Returns the display large text style.
    M3TextStyle displayLarge();

    /// Returns the display medium text style.
    M3TextStyle displayMedium();

    /// Returns the display small text style.
    M3TextStyle displaySmall();

    /// Returns the headline large text style.
    M3TextStyle headlineLarge();

    /// Returns the headline medium text style.
    M3TextStyle headlineMedium();

    /// Returns the headline small text style.
    M3TextStyle headlineSmall();

    /// Returns the title large text style.
    M3TextStyle titleLarge();

    /// Returns the title medium text style.
    M3TextStyle titleMedium();

    /// Returns the title small text style.
    M3TextStyle titleSmall();

    /// Returns the label large text style.
    M3TextStyle labelLarge();

    /// Returns the label medium text style.
    M3TextStyle labelMedium();

    /// Returns the label small text style.
    M3TextStyle labelSmall();

    /// Returns the body large text style.
    M3TextStyle bodyLarge();

    /// Returns the body medium text style.
    M3TextStyle bodyMedium();

    /// Returns the body small text style.
    M3TextStyle bodySmall();

    /// Creates typography tokens from the original compact role subset.
    static M3TypographyTokens create(
            M3TextStyle displayLarge,
            M3TextStyle headlineMedium,
            M3TextStyle titleLarge,
            M3TextStyle labelLarge,
            M3TextStyle bodyLarge,
            M3TextStyle bodyMedium
    ) {
        return create(
                displayLarge,
                displayLarge,
                displayLarge,
                headlineMedium,
                headlineMedium,
                headlineMedium,
                titleLarge,
                titleLarge,
                titleLarge,
                labelLarge,
                labelLarge,
                labelLarge,
                bodyLarge,
                bodyMedium,
                bodyMedium
        );
    }

    /// Creates typography tokens.
    static M3TypographyTokens create(
            M3TextStyle displayLarge,
            M3TextStyle displayMedium,
            M3TextStyle displaySmall,
            M3TextStyle headlineLarge,
            M3TextStyle headlineMedium,
            M3TextStyle headlineSmall,
            M3TextStyle titleLarge,
            M3TextStyle titleMedium,
            M3TextStyle titleSmall,
            M3TextStyle labelLarge,
            M3TextStyle labelMedium,
            M3TextStyle labelSmall,
            M3TextStyle bodyLarge,
            M3TextStyle bodyMedium,
            M3TextStyle bodySmall
    ) {
        return new M3TypographyTokensImpl(
                displayLarge,
                displayMedium,
                displaySmall,
                headlineLarge,
                headlineMedium,
                headlineSmall,
                titleLarge,
                titleMedium,
                titleSmall,
                labelLarge,
                labelMedium,
                labelSmall,
                bodyLarge,
                bodyMedium,
                bodySmall
        );
    }

    /// Returns the baseline Material Design 3 typography tokens.
    static M3TypographyTokens baseline() {
        return create(
                M3TextStyle.create("System", 57.0, 64.0, 400, -0.25),
                M3TextStyle.create("System", 45.0, 52.0, 400, 0.0),
                M3TextStyle.create("System", 36.0, 44.0, 400, 0.0),
                M3TextStyle.create("System", 32.0, 40.0, 400, 0.0),
                M3TextStyle.create("System", 28.0, 36.0, 400, 0.0),
                M3TextStyle.create("System", 24.0, 32.0, 400, 0.0),
                M3TextStyle.create("System", 22.0, 28.0, 400, 0.0),
                M3TextStyle.create("System", 16.0, 24.0, 500, 0.15),
                M3TextStyle.create("System", 14.0, 20.0, 500, 0.10),
                M3TextStyle.create("System", 14.0, 20.0, 500, 0.10),
                M3TextStyle.create("System", 12.0, 16.0, 500, 0.50),
                M3TextStyle.create("System", 11.0, 16.0, 500, 0.50),
                M3TextStyle.create("System", 16.0, 24.0, 400, 0.50),
                M3TextStyle.create("System", 14.0, 20.0, 400, 0.25),
                M3TextStyle.create("System", 12.0, 16.0, 400, 0.40)
        );
    }

    /// Returns expressive Material Design 3 typography tokens.
    static M3TypographyTokens expressive() {
        return create(
                M3TextStyle.create("System", 64.0, 72.0, 500, -0.25),
                M3TextStyle.create("System", 52.0, 60.0, 500, 0.0),
                M3TextStyle.create("System", 44.0, 52.0, 500, 0.0),
                M3TextStyle.create("System", 36.0, 44.0, 500, 0.0),
                M3TextStyle.create("System", 32.0, 40.0, 500, 0.0),
                M3TextStyle.create("System", 28.0, 36.0, 500, 0.0),
                M3TextStyle.create("System", 24.0, 32.0, 500, 0.0),
                M3TextStyle.create("System", 18.0, 26.0, 600, 0.15),
                M3TextStyle.create("System", 15.0, 22.0, 600, 0.10),
                M3TextStyle.create("System", 14.0, 20.0, 600, 0.10),
                M3TextStyle.create("System", 13.0, 18.0, 600, 0.50),
                M3TextStyle.create("System", 12.0, 16.0, 600, 0.50),
                M3TextStyle.create("System", 17.0, 26.0, 400, 0.50),
                M3TextStyle.create("System", 15.0, 22.0, 400, 0.25),
                M3TextStyle.create("System", 13.0, 18.0, 400, 0.40)
        );
    }

    /// Converts typography tokens into inline JavaFX CSS declarations.
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        append(builder, "display-large", displayLarge());
        append(builder, "display-medium", displayMedium());
        append(builder, "display-small", displaySmall());
        append(builder, "headline-large", headlineLarge());
        append(builder, "headline-medium", headlineMedium());
        append(builder, "headline-small", headlineSmall());
        append(builder, "title-large", titleLarge());
        append(builder, "title-medium", titleMedium());
        append(builder, "title-small", titleSmall());
        append(builder, "label-large", labelLarge());
        append(builder, "label-medium", labelMedium());
        append(builder, "label-small", labelSmall());
        append(builder, "body-large", bodyLarge());
        append(builder, "body-medium", bodyMedium());
        append(builder, "body-small", bodySmall());
        return builder.toString().trim();
    }

    /// Converts typography tokens into JavaFX CSS rules for M3FX text controls.
    default String toControlStyleRules() {
        StringBuilder builder = new StringBuilder();
        appendRule(builder, ".m3-display-large-text", displayLarge());
        appendRule(builder, ".m3-display-medium-text", displayMedium());
        appendRule(builder, ".m3-display-small-text", displaySmall());
        appendRule(builder, ".m3-headline-large-text", headlineLarge());
        appendRule(builder, ".m3-headline-medium-text", headlineMedium());
        appendRule(builder, ".m3-headline-small-text", headlineSmall());
        appendRule(builder, ".m3-title-large-text", titleLarge());
        appendRule(builder, ".m3-title-medium-text", titleMedium());
        appendRule(builder, ".m3-title-small-text", titleSmall());
        appendRule(builder, ".m3-label-large-text", labelLarge());
        appendRule(builder, ".m3-label-medium-text", labelMedium());
        appendRule(builder, ".m3-label-small-text", labelSmall());
        appendRule(builder, ".m3-body-large-text", bodyLarge());
        appendRule(builder, ".m3-body-medium-text", bodyMedium());
        appendRule(builder, ".m3-body-small-text", bodySmall());
        return builder.toString().stripTrailing();
    }

    /// Appends declarations for a typography token.
    private static void append(StringBuilder builder, String name, M3TextStyle style) {
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-font-family", "\"" + style.fontFamily() + "\"");
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-font-size", M3TokenCss.pixels(style.size()));
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-line-height", M3TokenCss.pixels(style.lineHeight()));
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-font-weight", Integer.toString(style.weight()));
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-tracking", M3TokenCss.pixels(style.tracking()));
    }

    /// Appends a control CSS rule for a typography token.
    private static void appendRule(StringBuilder builder, String selector, M3TextStyle style) {
        builder.append(selector).append(" {\n");
        appendDeclaration(builder, "-m3-typography-font-family", "\"" + style.fontFamily() + "\"");
        appendDeclaration(builder, "-m3-typography-font-size", M3TokenCss.pixels(style.size()));
        appendDeclaration(builder, "-m3-typography-line-height", M3TokenCss.pixels(style.lineHeight()));
        appendDeclaration(builder, "-m3-typography-font-weight", Integer.toString(style.weight()));
        appendDeclaration(builder, "-m3-typography-tracking", M3TokenCss.pixels(style.tracking()));
        builder.append("}\n\n");
    }

    /// Appends one declaration inside a control CSS rule.
    private static void appendDeclaration(StringBuilder builder, String name, String value) {
        builder.append("    ").append(name).append(": ").append(value).append(";\n");
    }
}
