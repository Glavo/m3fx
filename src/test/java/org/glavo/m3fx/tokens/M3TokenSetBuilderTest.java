// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/// Verifies component derivation, overrides, and snapshot ownership in token-set builders.
@NotNullByDefault
final class M3TokenSetBuilderTest {
    /// Derives each snapshot from the latest shape scale without changing earlier snapshots.
    @Test
    void derivesComponentsFromCurrentShapeAndDensity() {
        ColorScheme colors = ColorScheme.fromSeed(Color.web("#6750a4"));
        for (M3Profile profile : M3Profile.values()) {
            for (double scale : List.of(-4.0, 0.0, 4.0)) {
                M3Density density = M3Density.of(scale);
                M3TokenSetBuilder builder = M3TokenSet.builder(profile, colors, density);
                M3TokenSet first = builder.build();
                M3ShapeTokens shapes = M3ShapeTokens.builder(first.shapeTokens()).full(123.0).build();
                M3TokenSet second = builder.shapeTokens(shapes).build();

                assertSame(profile, second.profile());
                assertSame(density, second.density());
                assertSame(shapes, second.shapeTokens());
                assertEquals(M3ComponentTokens.builder(profile, shapes, density).build(), second.componentTokens());
                assertEquals(density.compact(40.0), second.componentTokens().filledButton().height());
                assertEquals(123.0, second.componentTokens().filledButton().containerShape());
                assertEquals(first.shapeTokens().full(), first.componentTokens().filledButton().containerShape());
            }
        }
    }

    /// Retains explicit and copied component groups until derivation is requested again.
    @Test
    void retainsOverridesUntilComponentDerivationIsRequested() {
        ColorScheme colors = ColorScheme.fromSeed(Color.web("#6750a4"));
        M3TokenSetBuilder builder = M3TokenSet.builder(M3Profile.BASELINE_2021, colors, M3Density.of(-1.0));
        M3TokenSet original = builder.build();
        M3ComponentTokens override = M3ComponentTokens.builder(original.componentTokens())
                .filledButton(new M3ComponentTokens.ButtonTokens(75.0, 4.0, 6.0))
                .build();
        M3TokenSet explicit = builder.componentTokens(override).build();
        M3TokenSetBuilder copy = M3TokenSet.builder(explicit);
        M3ShapeTokens shapes = M3ShapeTokens.builder(explicit.shapeTokens()).full(123.0).build();
        M3TokenSet retained = copy.shapeTokens(shapes).build();

        assertSame(override, explicit.componentTokens());
        assertSame(override, retained.componentTokens());

        M3TokenSet derived = copy.deriveComponentTokens().deriveComponentTokens().build();
        assertSame(shapes, derived.shapeTokens());
        assertEquals(36.0, derived.componentTokens().filledButton().height());
        assertEquals(123.0, derived.componentTokens().filledButton().containerShape());
        assertEquals(75.0, retained.componentTokens().filledButton().height());
        assertEquals(4.0, retained.componentTokens().filledButton().containerShape());
        assertSame(override, builder.build().componentTokens());
    }
}
