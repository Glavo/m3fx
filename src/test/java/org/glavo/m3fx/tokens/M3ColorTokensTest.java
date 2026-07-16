// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.tokens.M3TokenCssCompiler;
import org.glavo.monetfx.ColorRole;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for MonetFX-backed color tokens.
@NotNullByDefault
final class M3ColorTokensTest {
    /// Verifies that color roles are exposed as JavaFX CSS declarations.
    @Test
    void convertsColorRolesToCssDeclarations() {
        ColorScheme colorScheme = ColorScheme.fromSeed(Color.web("#6750a4"));
        M3ColorTokens tokens = M3ColorTokens.fromColorScheme(colorScheme);

        assertEquals(colorScheme.getColor(ColorRole.PRIMARY), tokens.get(ColorRole.PRIMARY));
        assertTrue(M3TokenCssCompiler.styleDeclarations(tokens).contains("-monet-primary"));
        assertTrue(M3TokenCssCompiler.styleDeclarations(tokens).contains("-m3-color-primary"));
    }
}
