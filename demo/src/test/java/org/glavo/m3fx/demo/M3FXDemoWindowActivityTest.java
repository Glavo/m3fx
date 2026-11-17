// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that demo-owned animations follow the native window presentation lifecycle.
@NotNullByDefault
@Tier2Test
final class M3FXDemoWindowActivityTest {
    /// Starts the JavaFX toolkit before the test opens a native window.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies page animation suspension and restoration across window, CSS, and global motion changes.
    @Test
    void pageAnimationsFollowWindowAndMotionSettings() {
        FxTestUtils.runOnFxThread(() -> {
            boolean previousGlobalRequest = M3MotionSettings.isGlobalReducedMotionRequested();
            Stage stage = new Stage();
            M3FXDemoApp application = new M3FXDemoApp();
            PauseTransition animation = new PauseTransition(Duration.minutes(1.0));
            try {
                M3MotionSettings.setGlobalReducedMotionRequested(false);
                application.start(stage);
                Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                M3AnimatedContent host = assertInstanceOf(
                        M3AnimatedContent.class, scene.lookup(".demo-page-host"));
                application.registerPageAnimation(animation);
                assertEquals(Animation.Status.RUNNING, animation.getStatus());

                stage.setIconified(true);
                assertEquals(Animation.Status.PAUSED, animation.getStatus());
                stage.setIconified(false);
                assertEquals(Animation.Status.RUNNING, animation.getStatus());

                scene.getRoot().pseudoClassStateChanged(PseudoClass.getPseudoClass("reduced-motion"), true);
                scene.getRoot().applyCss();
                assertFalse(host.isAnimationsEnabled());
                assertEquals(Animation.Status.PAUSED, animation.getStatus());

                M3MotionSettings.setGlobalReducedMotionRequested(true);
                scene.getRoot().pseudoClassStateChanged(PseudoClass.getPseudoClass("reduced-motion"), false);
                scene.getRoot().applyCss();
                assertTrue(host.isAnimationsEnabled());
                assertEquals(Animation.Status.PAUSED, animation.getStatus());

                M3MotionSettings.setGlobalReducedMotionRequested(false);
                assertEquals(Animation.Status.RUNNING, animation.getStatus());

                for (int cycle = 0; cycle < 2; cycle++) {
                    stage.setIconified(true);
                    M3MotionSettings.setGlobalReducedMotionRequested(true);
                    host.setAnimationsEnabled(false);
                    host.setAnimationsEnabled(true);
                    M3MotionSettings.setGlobalReducedMotionRequested(false);
                    assertEquals(Animation.Status.PAUSED, animation.getStatus());

                    stage.setIconified(false);
                    assertEquals(Animation.Status.RUNNING, animation.getStatus());
                    M3MotionSettings.setGlobalReducedMotionRequested(true);
                    host.setAnimationsEnabled(false);
                    host.setAnimationsEnabled(true);
                    assertEquals(Animation.Status.PAUSED, animation.getStatus());
                    M3MotionSettings.setGlobalReducedMotionRequested(false);
                    assertEquals(Animation.Status.RUNNING, animation.getStatus());

                    host.setAnimationsEnabled(false);
                    assertEquals(Animation.Status.PAUSED, animation.getStatus());
                    host.setAnimationsEnabled(true);
                    assertEquals(Animation.Status.RUNNING, animation.getStatus());
                }
            } finally {
                M3MotionSettings.setGlobalReducedMotionRequested(previousGlobalRequest);
                animation.stop();
                stage.close();
            }
        });
    }
}
