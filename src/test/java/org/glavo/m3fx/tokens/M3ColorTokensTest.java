package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
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
        M3ColorTokens tokens = new M3ColorTokens(colorScheme);

        assertEquals(colorScheme.getColor(ColorRole.PRIMARY), tokens.get(ColorRole.PRIMARY));
        assertTrue(tokens.toStyleDeclarations().contains("-monet-primary"));
        assertTrue(tokens.toStyleDeclarations().contains("-m3-color-primary"));
        assertTrue(tokens.toStyleSheet("m3-root").contains(".m3-root"));
    }
}
