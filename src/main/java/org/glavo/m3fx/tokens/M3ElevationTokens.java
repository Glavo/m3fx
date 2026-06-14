// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ElevationTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 elevation system tokens.
///
/// See [Material Design elevation](https://m3.material.io/styles/elevation/overview).
@NotNullByDefault
public sealed interface M3ElevationTokens permits M3ElevationTokensImpl {
    /// Returns elevation level zero.
    ///
    /// @return elevation level zero
    double level0();

    /// Returns elevation level one.
    ///
    /// @return elevation level one
    double level1();

    /// Returns elevation level two.
    ///
    /// @return elevation level two
    double level2();

    /// Returns elevation level three.
    ///
    /// @return elevation level three
    double level3();

    /// Returns elevation level four.
    ///
    /// @return elevation level four
    double level4();

    /// Returns elevation level five.
    ///
    /// @return elevation level five
    double level5();

    /// Creates elevation tokens.
    ///
    /// @param level0 the elevation value for level zero
    /// @param level1 the elevation value for level one
    /// @param level2 the elevation value for level two
    /// @param level3 the elevation value for level three
    /// @param level4 the elevation value for level four
    /// @param level5 the elevation value for level five
    /// @return elevation tokens containing the supplied levels
    static M3ElevationTokens create(
            double level0,
            double level1,
            double level2,
            double level3,
            double level4,
            double level5
    ) {
        return new M3ElevationTokensImpl(level0, level1, level2, level3, level4, level5);
    }

    /// Returns baseline elevation tokens.
    ///
    /// @return baseline elevation tokens
    static M3ElevationTokens baseline() {
        return create(0.0, 1.0, 3.0, 6.0, 8.0, 12.0);
    }

    /// Converts elevation tokens into inline JavaFX CSS declarations.
    ///
    /// @return inline JavaFX CSS declarations for these elevation tokens
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        M3TokenCss.append(builder, "-m3-elevation-level0", M3TokenCss.pixels(level0()));
        M3TokenCss.append(builder, "-m3-elevation-level1", M3TokenCss.pixels(level1()));
        M3TokenCss.append(builder, "-m3-elevation-level2", M3TokenCss.pixels(level2()));
        M3TokenCss.append(builder, "-m3-elevation-level3", M3TokenCss.pixels(level3()));
        M3TokenCss.append(builder, "-m3-elevation-level4", M3TokenCss.pixels(level4()));
        M3TokenCss.append(builder, "-m3-elevation-level5", M3TokenCss.pixels(level5()));
        return builder.toString().trim();
    }

    /// Converts elevation tokens into JavaFX CSS rules for m3fx controls.
    ///
    /// @return JavaFX CSS rules for m3fx controls using these elevation tokens
    default String toControlStyleRules() {
        StringBuilder builder = new StringBuilder();
        appendShadowRule(builder, ".m3-elevated-button", level3(), level1());
        appendShadowRule(builder, ".m3-elevated-button:hover, .m3-elevated-button:focus-visible", level4(), level2());
        appendShadowRule(builder, ".m3-elevated-button:armed, .m3-elevated-button:pressed", level2(), level1());
        appendShadowRule(builder, ".m3-fab", level4(), level2());
        appendShadowRule(builder, ".m3-fab:hover, .m3-fab:focus-visible", level5(), level3());
        appendShadowRule(builder, ".m3-fab:armed, .m3-fab:pressed", level4(), level2());
        appendShadowRule(builder, ".m3-elevated-card .m3-card-container", level4(), Math.max(level1(), level2() - level1()));
        appendShadowRule(builder, ".m3-elevated-card:hover .m3-card-container, .m3-elevated-card:focus-visible .m3-card-container", level5(), level3());
        appendShadowRule(builder, ".m3-elevated-card:pressed .m3-card-container", level4(), Math.max(level1(), level2() - level1()));
        appendShadowRule(builder, ".m3-surface-elevation-level1", level1(), level1());
        appendShadowRule(builder, ".m3-surface-elevation-level2", level2(), Math.max(level1(), level2() - level1()));
        appendShadowRule(builder, ".m3-surface-elevation-level3", level3(), Math.max(level1(), level3() - level2()));
        appendShadowRule(builder, ".m3-surface-elevation-level4", level4(), Math.max(level1(), level4() - level3()));
        appendShadowRule(builder, ".m3-surface-elevation-level5", level5(), Math.max(level1(), level5() - level4()));
        return builder.toString().stripTrailing();
    }

    /// Appends a dropshadow CSS rule.
    private static void appendShadowRule(StringBuilder builder, String selector, double radius, double offsetY) {
        builder.append(selector)
                .append(" {\n    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), ")
                .append(M3TokenCss.format(radius))
                .append(", 0.18, 0, ")
                .append(M3TokenCss.format(offsetY))
                .append(");\n}\n\n");
    }
}
