// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests state layer animation behavior.
@NotNullByDefault
final class M3StateLayerTest {
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

    /// Verifies that CSS-resolved hover opacity is reached through an animation.
    @Test
    void stateLayerAnimatesCssResolvedHoverOpacity() {
        runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-button");
            M3StateLayer stateLayer = new M3StateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);

            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            assertEquals(0.0, overlay.getOpacity(), 0.0001);

            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            stateLayer.animateOverlayOpacityFromCss();

            assertEquals(0.0, overlay.getOpacity(), 0.0001);
            assertTrue(stateLayer.isOverlayOpacityAnimationRunning());
        });
    }

    /// Returns a region looked up below a node.
    private static Region lookupRegion(Pane node, String selector) {
        javafx.scene.Node child = node.lookup(selector);
        assertInstanceOf(Region.class, child);
        return (Region) child;
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
