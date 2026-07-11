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
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    /// Verifies a failing initial refresh cannot leave owner or scene dispatcher registrations behind.
    @Test
    void rollsBackRegistrationWhenInitialRefreshFails() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            Scene scene = new Scene(owner);
            int ownerPropertyCount = owner.getProperties().size();
            int scenePropertyCount = scene.getProperties().size();
            AtomicInteger refreshes = new AtomicInteger();

            assertThrows(IllegalStateException.class, () -> new M3MotionSettingsObserver(owner, () -> {
                refreshes.incrementAndGet();
                throw new IllegalStateException("refresh failed");
            }));

            assertEquals(1, refreshes.get());
            assertEquals(ownerPropertyCount, owner.getProperties().size());
            assertEquals(scenePropertyCount, scene.getProperties().size());

            M3MotionSettings.setAnimationsEnabled(!M3MotionSettings.areAnimationsEnabled());
            assertEquals(1, refreshes.get());
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


    /// Verifies multiple subscriptions on one owner share lifecycle state until the last subscription is disposed.
    @Test
    void sharesOwnerLifecycleAcrossSubscriptions() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            boolean animationsEnabled = M3MotionSettings.areAnimationsEnabled();
            Pane owner = new Pane();
            Scene scene = new Scene(owner);
            int initialOwnerPropertyCount = owner.getProperties().size();
            int initialScenePropertyCount = scene.getProperties().size();
            AtomicInteger firstRefreshes = new AtomicInteger();
            AtomicInteger secondRefreshes = new AtomicInteger();
            M3MotionSettingsObserver first =
                    new M3MotionSettingsObserver(owner, firstRefreshes::incrementAndGet);
            int registeredOwnerPropertyCount = owner.getProperties().size();
            int registeredScenePropertyCount = scene.getProperties().size();
            M3MotionSettingsObserver second =
                    new M3MotionSettingsObserver(owner, secondRefreshes::incrementAndGet);

            try {
                assertEquals(1, firstRefreshes.get());
                assertEquals(1, secondRefreshes.get());
                assertTrue(registeredOwnerPropertyCount > initialOwnerPropertyCount);
                assertTrue(registeredScenePropertyCount > initialScenePropertyCount);
                assertEquals(registeredOwnerPropertyCount, owner.getProperties().size());
                assertEquals(registeredScenePropertyCount, scene.getProperties().size());

                M3MotionSettings.setAnimationsEnabled(!animationsEnabled);
                assertEquals(2, firstRefreshes.get());
                assertEquals(2, secondRefreshes.get());

                first.dispose();
                M3MotionSettings.setAnimationsEnabled(animationsEnabled);

                assertEquals(2, firstRefreshes.get());
                assertEquals(3, secondRefreshes.get());
                assertEquals(registeredOwnerPropertyCount, owner.getProperties().size());
                assertEquals(registeredScenePropertyCount, scene.getProperties().size());
            } finally {
                first.dispose();
                second.dispose();
            }

            assertEquals(initialOwnerPropertyCount, owner.getProperties().size());
            assertEquals(initialScenePropertyCount, scene.getProperties().size());
        }));
    }

    /// Verifies one owner callback may dispose itself and a sibling without destabilizing callback dispatch.
    @Test
    void supportsDisposalDuringOwnerDispatch() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            boolean animationsEnabled = M3MotionSettings.areAnimationsEnabled();
            Pane owner = new Pane();
            new Scene(owner);
            AtomicBoolean disposeDuringRefresh = new AtomicBoolean();
            AtomicInteger firstRefreshes = new AtomicInteger();
            AtomicInteger secondRefreshes = new AtomicInteger();
            AtomicInteger thirdRefreshes = new AtomicInteger();
            AtomicReference<@Nullable M3MotionSettingsObserver> firstReference = new AtomicReference<>();
            AtomicReference<@Nullable M3MotionSettingsObserver> secondReference = new AtomicReference<>();

            M3MotionSettingsObserver first = new M3MotionSettingsObserver(owner, () -> {
                firstRefreshes.incrementAndGet();
                if (!disposeDuringRefresh.getAndSet(false)) {
                    return;
                }

                @Nullable M3MotionSettingsObserver secondObserver = secondReference.getAndSet(null);
                if (secondObserver != null) {
                    secondObserver.dispose();
                }
                @Nullable M3MotionSettingsObserver firstObserver = firstReference.getAndSet(null);
                if (firstObserver != null) {
                    firstObserver.dispose();
                }
            });
            firstReference.set(first);
            M3MotionSettingsObserver second =
                    new M3MotionSettingsObserver(owner, secondRefreshes::incrementAndGet);
            secondReference.set(second);
            M3MotionSettingsObserver third =
                    new M3MotionSettingsObserver(owner, thirdRefreshes::incrementAndGet);

            try {
                disposeDuringRefresh.set(true);
                M3MotionSettings.setAnimationsEnabled(!animationsEnabled);

                assertEquals(2, firstRefreshes.get());
                assertEquals(1, secondRefreshes.get());
                assertEquals(2, thirdRefreshes.get());

                M3MotionSettings.setAnimationsEnabled(animationsEnabled);

                assertEquals(2, firstRefreshes.get());
                assertEquals(1, secondRefreshes.get());
                assertEquals(3, thirdRefreshes.get());
            } finally {
                @Nullable M3MotionSettingsObserver firstObserver = firstReference.getAndSet(null);
                if (firstObserver != null) {
                    firstObserver.dispose();
                }
                @Nullable M3MotionSettingsObserver secondObserver = secondReference.getAndSet(null);
                if (secondObserver != null) {
                    secondObserver.dispose();
                }
                third.dispose();
            }
        }));
    }

    /// Verifies one owner callback may dispose a different owner before its scene callback is dispatched.
    @Test
    void supportsOwnerRemovalDuringSceneDispatch() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            boolean animationsEnabled = M3MotionSettings.areAnimationsEnabled();
            Pane firstOwner = new Pane();
            Pane secondOwner = new Pane();
            new Scene(new Pane(firstOwner, secondOwner));
            AtomicBoolean disposeDuringRefresh = new AtomicBoolean();
            AtomicInteger firstRefreshes = new AtomicInteger();
            AtomicInteger secondRefreshes = new AtomicInteger();
            AtomicReference<@Nullable M3MotionSettingsObserver> secondReference = new AtomicReference<>();

            M3MotionSettingsObserver first = new M3MotionSettingsObserver(firstOwner, () -> {
                firstRefreshes.incrementAndGet();
                if (disposeDuringRefresh.getAndSet(false)) {
                    @Nullable M3MotionSettingsObserver observer = secondReference.getAndSet(null);
                    if (observer != null) {
                        observer.dispose();
                    }
                }
            });
            M3MotionSettingsObserver second =
                    new M3MotionSettingsObserver(secondOwner, secondRefreshes::incrementAndGet);
            secondReference.set(second);

            try {
                disposeDuringRefresh.set(true);
                M3MotionSettings.setAnimationsEnabled(!animationsEnabled);

                assertEquals(2, firstRefreshes.get());
                assertEquals(1, secondRefreshes.get());

                M3MotionSettings.setAnimationsEnabled(animationsEnabled);

                assertEquals(3, firstRefreshes.get());
                assertEquals(1, secondRefreshes.get());
            } finally {
                first.dispose();
                @Nullable M3MotionSettingsObserver secondObserver = secondReference.getAndSet(null);
                if (secondObserver != null) {
                    secondObserver.dispose();
                }
            }
        }));
    }

    /// Verifies an owner may transfer scenes from inside a refresh callback without skipping its sibling callbacks.
    @Test
    void supportsSceneTransferDuringOwnerDispatch() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            boolean animationsEnabled = M3MotionSettings.areAnimationsEnabled();
            Pane owner = new Pane();
            Pane firstRoot = new Pane(owner);
            Pane secondRoot = new Pane();
            Scene firstScene = new Scene(firstRoot);
            Scene secondScene = new Scene(secondRoot);
            int firstInitialPropertyCount = firstScene.getProperties().size();
            int secondInitialPropertyCount = secondScene.getProperties().size();
            AtomicBoolean transferDuringRefresh = new AtomicBoolean();
            AtomicInteger firstRefreshes = new AtomicInteger();
            AtomicInteger secondRefreshes = new AtomicInteger();

            M3MotionSettingsObserver first = new M3MotionSettingsObserver(owner, () -> {
                firstRefreshes.incrementAndGet();
                if (transferDuringRefresh.getAndSet(false)) {
                    firstRoot.getChildren().clear();
                    secondRoot.getChildren().add(owner);
                }
            });
            M3MotionSettingsObserver second =
                    new M3MotionSettingsObserver(owner, secondRefreshes::incrementAndGet);

            try {
                transferDuringRefresh.set(true);
                M3MotionSettings.setAnimationsEnabled(!animationsEnabled);

                assertSame(secondScene, owner.getScene());
                assertEquals(3, firstRefreshes.get());
                assertEquals(2, secondRefreshes.get());
                assertEquals(firstInitialPropertyCount, firstScene.getProperties().size());
                assertTrue(secondScene.getProperties().size() > secondInitialPropertyCount);

                M3MotionSettings.setAnimationsEnabled(animationsEnabled);

                assertEquals(4, firstRefreshes.get());
                assertEquals(3, secondRefreshes.get());
            } finally {
                first.dispose();
                second.dispose();
            }

            assertEquals(firstInitialPropertyCount, firstScene.getProperties().size());
            assertEquals(secondInitialPropertyCount, secondScene.getProperties().size());
        }));
    }}
