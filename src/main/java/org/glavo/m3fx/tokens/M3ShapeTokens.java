// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ShapeTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 shape system tokens.
///
/// Shape tokens define the corner radius scale used by surfaces, cards, buttons, text fields, sheets, and other
/// controls. Baseline and expressive profiles can supply different radii while component code consumes the same
/// semantic shape roles.
///
/// See [Material Design shape](https://m3.material.io/styles/shape/overview).
@NotNullByDefault
public sealed interface M3ShapeTokens permits M3ShapeTokensImpl {
    /// Returns the extra-small corner radius.
    ///
    /// @return the extra-small corner radius in pixels
    double extraSmall();

    /// Returns the small corner radius.
    ///
    /// @return the small corner radius in pixels
    double small();

    /// Returns the medium corner radius.
    ///
    /// @return the medium corner radius in pixels
    double medium();

    /// Returns the large corner radius.
    ///
    /// @return the large corner radius in pixels
    double large();

    /// Returns the extra-large corner radius.
    ///
    /// @return the extra-large corner radius in pixels
    double extraLarge();

    /// Returns the full corner radius used for pills.
    ///
    /// @return the full corner radius used for pills, in pixels
    double full();

    /// Creates shape tokens.
    ///
    /// @param extraSmall the extra-small corner radius in pixels
    /// @param small the small corner radius in pixels
    /// @param medium the medium corner radius in pixels
    /// @param large the large corner radius in pixels
    /// @param extraLarge the extra-large corner radius in pixels
    /// @param full the full corner radius used for pills, in pixels
    /// @return the created shape token set
    static M3ShapeTokens create(
            double extraSmall,
            double small,
            double medium,
            double large,
            double extraLarge,
            double full
    ) {
        return new M3ShapeTokensImpl(extraSmall, small, medium, large, extraLarge, full);
    }

    /// Returns baseline Material Design 3 shape tokens.
    ///
    /// @return baseline Material Design 3 shape tokens
    static M3ShapeTokens baseline() {
        return create(4.0, 8.0, 12.0, 16.0, 28.0, 999.0);
    }

    /// Returns expressive Material Design 3 shape tokens.
    ///
    /// @return expressive Material Design 3 shape tokens
    static M3ShapeTokens expressive() {
        return create(6.0, 10.0, 16.0, 24.0, 32.0, 999.0);
    }

    /// Converts shape tokens into inline JavaFX CSS declarations.
    ///
    /// @return inline JavaFX CSS declarations for this shape token set
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        M3TokenCss.append(builder, "-m3-shape-corner-extra-small", M3TokenCss.pixels(extraSmall()));
        M3TokenCss.append(builder, "-m3-shape-corner-small", M3TokenCss.pixels(small()));
        M3TokenCss.append(builder, "-m3-shape-corner-medium", M3TokenCss.pixels(medium()));
        M3TokenCss.append(builder, "-m3-shape-corner-large", M3TokenCss.pixels(large()));
        M3TokenCss.append(builder, "-m3-shape-corner-extra-large", M3TokenCss.pixels(extraLarge()));
        M3TokenCss.append(builder, "-m3-shape-corner-full", M3TokenCss.pixels(full()));
        return builder.toString().trim();
    }
}
