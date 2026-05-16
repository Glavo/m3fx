// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TypographyTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 typography system tokens.
@NotNullByDefault
public sealed interface M3TypographyTokens permits M3TypographyTokensImpl {
    /// Returns the display large text style.
    M3TextStyle displayLarge();

    /// Returns the headline medium text style.
    M3TextStyle headlineMedium();

    /// Returns the title large text style.
    M3TextStyle titleLarge();

    /// Returns the label large text style.
    M3TextStyle labelLarge();

    /// Returns the body large text style.
    M3TextStyle bodyLarge();

    /// Returns the body medium text style.
    M3TextStyle bodyMedium();

    /// Creates typography tokens.
    static M3TypographyTokens create(
            M3TextStyle displayLarge,
            M3TextStyle headlineMedium,
            M3TextStyle titleLarge,
            M3TextStyle labelLarge,
            M3TextStyle bodyLarge,
            M3TextStyle bodyMedium
    ) {
        return new M3TypographyTokensImpl(
                displayLarge,
                headlineMedium,
                titleLarge,
                labelLarge,
                bodyLarge,
                bodyMedium
        );
    }

    /// Returns the baseline Material Design 3 typography tokens.
    static M3TypographyTokens baseline() {
        return create(
                M3TextStyle.create("System", 57.0, 64.0, 400),
                M3TextStyle.create("System", 28.0, 36.0, 400),
                M3TextStyle.create("System", 22.0, 28.0, 400),
                M3TextStyle.create("System", 14.0, 20.0, 500),
                M3TextStyle.create("System", 16.0, 24.0, 400),
                M3TextStyle.create("System", 14.0, 20.0, 400)
        );
    }

    /// Returns provisional expressive typography tokens.
    static M3TypographyTokens expressive() {
        return create(
                M3TextStyle.create("System", 64.0, 72.0, 500),
                M3TextStyle.create("System", 32.0, 40.0, 500),
                M3TextStyle.create("System", 24.0, 32.0, 500),
                M3TextStyle.create("System", 14.0, 20.0, 600),
                M3TextStyle.create("System", 17.0, 26.0, 400),
                M3TextStyle.create("System", 15.0, 22.0, 400)
        );
    }

    /// Converts typography tokens into inline JavaFX CSS declarations.
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        append(builder, "display-large", displayLarge());
        append(builder, "headline-medium", headlineMedium());
        append(builder, "title-large", titleLarge());
        append(builder, "label-large", labelLarge());
        append(builder, "body-large", bodyLarge());
        append(builder, "body-medium", bodyMedium());
        return builder.toString().trim();
    }

    /// Converts typography tokens into JavaFX CSS rules for M3FX text controls.
    default String toControlStyleRules() {
        StringBuilder builder = new StringBuilder();
        appendRule(builder, ".m3-display-large-text", displayLarge());
        appendRule(builder, ".m3-headline-medium-text", headlineMedium());
        appendRule(builder, ".m3-title-large-text", titleLarge());
        appendRule(builder, ".m3-label-large-text", labelLarge());
        appendRule(builder, ".m3-body-large-text", bodyLarge());
        appendRule(builder, ".m3-body-medium-text", bodyMedium());
        return builder.toString().stripTrailing();
    }

    /// Appends declarations for a typography token.
    private static void append(StringBuilder builder, String name, M3TextStyle style) {
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-font-family", "\"" + style.fontFamily() + "\"");
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-font-size", M3TokenCss.pixels(style.size()));
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-line-height", M3TokenCss.pixels(style.lineHeight()));
        M3TokenCss.append(builder, "-m3-typescale-" + name + "-font-weight", Integer.toString(style.weight()));
    }

    /// Appends a control CSS rule for a typography token.
    private static void appendRule(StringBuilder builder, String selector, M3TextStyle style) {
        builder.append(selector).append(" {\n");
        appendDeclaration(builder, "-m3-typography-font-family", "\"" + style.fontFamily() + "\"");
        appendDeclaration(builder, "-m3-typography-font-size", M3TokenCss.pixels(style.size()));
        appendDeclaration(builder, "-m3-typography-line-height", M3TokenCss.pixels(style.lineHeight()));
        appendDeclaration(builder, "-m3-typography-font-weight", Integer.toString(style.weight()));
        builder.append("}\n\n");
    }

    /// Appends one declaration inside a control CSS rule.
    private static void appendDeclaration(StringBuilder builder, String name, String value) {
        builder.append("    ").append(name).append(": ").append(value).append(";\n");
    }
}
