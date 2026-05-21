// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    /// Verifies that CSS effect changes are animated from the current effect.
    @Test
    void animatesCssResolvedDropShadow() {
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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

    /// Runs a task on the FX application thread and propagates failures.
    private static void runOnFxThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
            return;
        }

        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                failure.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        Throwable exception = failure.get();
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (exception instanceof Error error) {
            throw error;
        }
        if (exception != null) {
            throw new AssertionError(exception);
        }
    }
}
