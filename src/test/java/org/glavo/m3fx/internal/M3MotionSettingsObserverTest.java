// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests scene-aware runtime motion settings observation.
@NotNullByDefault
final class M3MotionSettingsObserverTest {
    /// Starts the JavaFX toolkit before scene attachment tests create scenes.
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

    /// Verifies that observers only receive settings changes while their owner is attached and not disposed.
    @Test
    void observesSettingsOnlyWhileAttachedAndNotDisposed() {
        boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
        Pane owner = new Pane();
        AtomicInteger refreshes = new AtomicInteger();
        M3MotionSettingsObserver observer = new M3MotionSettingsObserver(owner, refreshes::incrementAndGet);

        try {
            assertEquals(0, refreshes.get());

            Pane root = new Pane(owner);
            Scene scene = new Scene(root);

            assertSame(scene, owner.getScene());
            assertEquals(1, refreshes.get());

            M3MotionSettings.setAnimationsEnabled(!previousAnimationsEnabled);

            assertEquals(2, refreshes.get());

            root.getChildren().clear();
            M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);

            assertEquals(2, refreshes.get());

            root.getChildren().add(owner);

            assertEquals(3, refreshes.get());

            observer.dispose();
            M3MotionSettings.setAnimationsEnabled(!previousAnimationsEnabled);

            assertEquals(3, refreshes.get());
        } finally {
            observer.dispose();
            M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);
        }
    }
}
