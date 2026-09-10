// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3IconSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies font-icon sizing without taking ownership of application layout constraints.
@NotNullByDefault
final class M3IconTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves explicit constraints across size-role and glyph-size changes.
    @Test
    void sizeChangesPreserveExplicitLayoutConstraints() {
        FxTestUtils.runOnFxThread(() -> {
            M3Icon icon = new M3Icon("A");
            icon.setMinSize(11.0, 12.0);
            icon.setPrefSize(81.0, 62.0);
            icon.setSize(M3IconSize.LARGE);
            assertConstraints(icon, 11.0, 12.0, 81.0, 62.0);
            icon.setIconSize(30.0);
            assertConstraints(icon, 11.0, 12.0, 81.0, 62.0);

            icon.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            icon.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            assertConstraints(icon, 45.0, 45.0, 45.0, 45.0);
        });
    }

    /// Leaves bound dimensions under their source's control when the glyph size changes.
    @Test
    void sizeChangesPreserveBoundLayoutConstraints() {
        FxTestUtils.runOnFxThread(() -> {
            M3Icon icon = new M3Icon("A");
            SimpleDoubleProperty size = new SimpleDoubleProperty(70.0);
            icon.minWidthProperty().bind(size);
            icon.minHeightProperty().bind(size);
            icon.prefWidthProperty().bind(size);
            icon.prefHeightProperty().bind(size);
            icon.setSize(M3IconSize.LARGE);
            icon.setIconSize(30.0);
            assertEquals(30.0, icon.getIconFont().getSize());
            assertConstraints(icon, 70.0, 70.0, 70.0, 70.0);
            size.set(80.0);
            assertConstraints(icon, 80.0, 80.0, 80.0, 80.0);
        });
    }

    /// Computes padded line boxes consistently before and after skin installation.
    @Test
    void computedSizeTracksGlyphSizeAndInsetsWithoutSkin() {
        FxTestUtils.runOnFxThread(() -> {
            M3Icon icon = new M3Icon("A");
            assertConstraints(icon, 36.0, 36.0, 36.0, 36.0);
            icon.setPadding(new Insets(2.0, 3.0, 4.0, 5.0));
            assertConstraints(icon, 44.0, 42.0, 44.0, 42.0);
            icon.setSize(M3IconSize.SMALL);
            assertConstraints(icon, 35.0, 33.0, 35.0, 33.0);
            icon.setSkin(new M3IconSkin(icon));
            assertConstraints(icon, 35.0, 33.0, 35.0, 33.0);
            icon.setIconSize(30.0);
            assertConstraints(icon, 53.0, 51.0, 53.0, 51.0);
        });
    }

    /// Checks the effective minimum and preferred dimensions used by parent layout.
    private static void assertConstraints(M3Icon icon, double minWidth, double minHeight,
                                          double prefWidth, double prefHeight) {
        assertEquals(minWidth, icon.minWidth(-1.0));
        assertEquals(minHeight, icon.minHeight(-1.0));
        assertEquals(prefWidth, icon.prefWidth(-1.0));
        assertEquals(prefHeight, icon.prefHeight(-1.0));
    }
}
