// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3MotionTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 motion duration tokens in milliseconds.
@NotNullByDefault
public sealed interface M3MotionTokens permits M3MotionTokensImpl {
    /// Returns the short duration token.
    int shortDuration();

    /// Returns the medium duration token.
    int mediumDuration();

    /// Returns the long duration token.
    int longDuration();

    /// Returns baseline motion tokens.
    static M3MotionTokens baseline() {
        return new M3MotionTokensImpl(100, 250, 500);
    }

    /// Converts motion tokens into inline JavaFX CSS declarations.
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        M3TokenCss.append(builder, "-m3-motion-duration-short", shortDuration() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-medium", mediumDuration() + "ms");
        M3TokenCss.append(builder, "-m3-motion-duration-long", longDuration() + "ms");
        return builder.toString().trim();
    }
}
