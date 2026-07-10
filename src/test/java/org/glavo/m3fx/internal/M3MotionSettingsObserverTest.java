// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests scene-aware runtime motion settings observation.
@NotNullByDefault
final class M3MotionSettingsObserverTest {
    /// Starts the JavaFX toolkit before scene attachment tests create scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that observer refreshes caused by background settings changes run on the JavaFX thread.
    @Test
    void dispatchesBackgroundSettingsChangesToFxThread() throws InterruptedException {
        CountDownLatch refreshLatch = new CountDownLatch(1);
        AtomicBoolean captureRefresh = new AtomicBoolean(false);
        AtomicBoolean refreshedOnFxThread = new AtomicBoolean(false);
        M3MotionScheme previousScheme = M3MotionSettings.getMotionScheme();
        M3MotionScheme replacementScheme = previousScheme.defaultEffects().easing() == M3MotionEasing.STANDARD
                ? M3MotionScheme.expressive()
                : M3MotionScheme.standard();

        M3MotionSettingsObserver observer = FxTestUtils.callOnFxThread(() -> {
            Pane owner = new Pane();
            new Scene(owner);
            return new M3MotionSettingsObserver(owner, () -> {
                if (captureRefresh.get()) {
                    refreshedOnFxThread.set(Platform.isFxApplicationThread());
                    refreshLatch.countDown();
                }
            });
        });

        try {
            captureRefresh.set(true);
            Thread settingsThread = new Thread(
                    () -> M3MotionSettings.setMotionScheme(replacementScheme),
                    "m3fx-motion-settings-test"
            );
            settingsThread.start();
            settingsThread.join(TimeUnit.SECONDS.toMillis(FxTestUtils.FX_TIMEOUT_SECONDS));

            assertFalse(settingsThread.isAlive());
            assertTrue(
                    refreshLatch.await(FxTestUtils.FX_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "Motion settings refresh did not reach the JavaFX thread"
            );
            assertTrue(refreshedOnFxThread.get());
        } finally {
            FxTestUtils.runOnFxThread(observer::dispose);
            M3MotionSettings.setMotionScheme(previousScheme);
        }
    }

    /// Verifies that observers only receive settings changes while their owner is attached and not disposed.
    @Test
    void observesSettingsOnlyWhileAttachedAndNotDisposed() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
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
                assertEquals(3, refreshes.get());
                M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);

                assertEquals(3, refreshes.get());

                root.getChildren().add(owner);

                assertEquals(4, refreshes.get());

                observer.dispose();
                M3MotionSettings.setAnimationsEnabled(!previousAnimationsEnabled);

                assertEquals(4, refreshes.get());
            } finally {
                observer.dispose();
            }
        }));
    }

    /// Verifies that one observer refreshes when its scene enters, leaves, shows, or hides a window.
    @Test
    void observesSceneWindowLifecycle() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Scene scene = new Scene(owner);
            AtomicInteger refreshes = new AtomicInteger();
            M3MotionSettingsObserver observer = new M3MotionSettingsObserver(owner, refreshes::incrementAndGet);
            Stage stage = new Stage();

            try {
                int attachedRefreshes = refreshes.get();
                stage.setScene(scene);
                assertTrue(refreshes.get() > attachedRefreshes);

                int windowRefreshes = refreshes.get();
                stage.show();
                assertTrue(refreshes.get() > windowRefreshes);

                int shownRefreshes = refreshes.get();
                stage.hide();
                assertTrue(refreshes.get() > shownRefreshes);

                observer.dispose();
                int disposedRefreshes = refreshes.get();
                stage.show();
                assertEquals(disposedRefreshes, refreshes.get());
            } finally {
                observer.dispose();
                stage.close();
            }
        });
    }
}
