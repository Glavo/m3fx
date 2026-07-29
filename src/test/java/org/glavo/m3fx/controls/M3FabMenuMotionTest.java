// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that floating action button menu motion follows runtime motion settings.
@NotNullByDefault
final class M3FabMenuMotionTest {
    /// Starts the JavaFX toolkit before tests create controls and windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that enabling reduced motion settles an active expansion immediately.
    @Tier2Test
    @Test
    void reducedMotionSettlesRunningExpansion() {
        FxTestUtils.runOnFxThread(() -> {
            M3FloatingActionButton action = new M3FloatingActionButton("Create");
            M3FabMenu menu = new M3FabMenu();
            menu.getItems().add(action);
            Pane root = new Pane(menu);
            Scene scene = new Scene(root, 240.0, 240.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                M3MotionSettings.setReducedMotionRequested(root, false);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menu.resizeRelocate(24.0, 24.0, 160.0, 180.0);
                menu.layout();

                menu.show();

                assertTrue(action.getOpacity() < 1.0, "expansion should start from an intermediate state");

                M3MotionSettings.setReducedMotionRequested(root, true);

                assertTrue(menu.isExpanded());
                assertEquals(1.0, action.getOpacity(), 0.0001);
                assertEquals(1.0, action.getScaleX(), 0.0001);
                assertEquals(1.0, action.getScaleY(), 0.0001);
                assertEquals(0.0, action.getTranslateY(), 0.0001);
            } finally {
                M3MotionSettings.setReducedMotionRequested(root, false);
                stage.close();
            }
        });
    }
}
