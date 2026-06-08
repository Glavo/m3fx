// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests CSS-resolved effect transitions.
@NotNullByDefault
final class M3CssEffectTransitionTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that CSS effect changes are animated from the current effect.
    @Test
    void animatesCssResolvedDropShadow() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            Scene scene = new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            owner.applyCss();
            assertNull(target.getEffect());

            transition.install();
            target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3);");
            transition.animateEffectFromCss();

            DropShadow animated = assertInstanceOf(DropShadow.class, target.getEffect());
            assertEquals(0.0, animated.getRadius(), 0.0001);
            assertTrue(transition.isRunning());

            transition.uninstall();
        });
    }

    /// Verifies that disabled motion applies CSS effect changes without starting a transition.
    @Test
    void disabledMotionAppliesCssResolvedDropShadowImmediately() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            M3MotionSettings.setAnimationsEnabled(owner, false);
            Scene scene = new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            owner.applyCss();
            transition.install();
            target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3);");
            transition.animateEffectFromCss();

            DropShadow shadow = assertInstanceOf(DropShadow.class, target.getEffect());
            assertEquals(8.0, shadow.getRadius(), 0.0001);
            assertFalse(transition.isRunning());

            transition.uninstall();
        });
    }

    /// Verifies that a running CSS effect transition settles when animations are disabled at runtime.
    @Test
    void runningCssResolvedDropShadowSettlesWhenAnimationsAreDisabledAtRuntime() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            Scene scene = new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            owner.applyCss();
            transition.install();
            M3MotionSettings.setAnimationsEnabled(owner, true);
            try {
                target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3);");
                transition.animateEffectFromCss();

                DropShadow animated = assertInstanceOf(DropShadow.class, target.getEffect());
                assertEquals(0.0, animated.getRadius(), 0.0001);
                assertTrue(transition.isRunning());

                M3MotionSettings.setAnimationsEnabled(owner, false);

                DropShadow settled = assertInstanceOf(DropShadow.class, target.getEffect());
                assertEquals(8.0, settled.getRadius(), 0.0001);
                assertEquals(3.0, settled.getOffsetY(), 0.0001);
                assertFalse(transition.isRunning());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(owner);
                transition.uninstall();
            }
        });
    }
}
