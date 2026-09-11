// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies popup observation lifecycle and owner changes made by popup listeners.
@NotNullByDefault
final class M3PopupContextSynchronizerTest {
    /// Starts JavaFX before constructing popup context.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Keeps observation stopped when a popup stylesheet callback stops the initial synchronization.
    @Test
    void stoppingDuringStartDoesNotInstallMotionObservation() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            Pane owner = new Pane();
            new Scene(owner);
            Pane popup = new Pane();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popup);
            AtomicBoolean stop = new AtomicBoolean(true);
            popup.getStylesheets().addListener((ListChangeListener<String>) change -> {
                if (stop.getAndSet(false)) {
                    synchronizer.stop();
                }
            });

            try {
                synchronizer.start();
                assertFalse(stop.get());
                M3MotionSettings.setReducedMotionRequested(owner, true);
                assertFalse(M3MotionSettings.isReducedMotionRequested(popup));

                synchronizer.start();
                assertTrue(M3MotionSettings.isReducedMotionRequested(popup));
                M3MotionSettings.setReducedMotionRequested(owner, false);
                assertFalse(M3MotionSettings.isReducedMotionRequested(popup));
            } finally {
                synchronizer.start();
                synchronizer.stop();
                M3MotionSettings.setReducedMotionRequested(owner, false);
            }
        }));
    }

    /// Retains the latest orientation when a popup listener changes the owner during synchronization.
    @Test
    void orientationChangedDuringSynchronizationIsEventuallyCopied() {
        Pane owner = new Pane();
        Pane popup = new Pane();
        M3PopupContextSynchronizer synchronizer = FxTestUtils.callOnFxThread(() ->
                new M3PopupContextSynchronizer(owner, popup));
        try {
            FxTestUtils.runOnFxThread(() -> {
                new Scene(owner);
                synchronizer.start();
                popup.nodeOrientationProperty().addListener((observable, previous, orientation) -> {
                    if (orientation == NodeOrientation.RIGHT_TO_LEFT) {
                        owner.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                    }
                });
                owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            });
            FxTestUtils.runOnFxThread(() ->
                    assertEquals(owner.getEffectiveNodeOrientation(), popup.getNodeOrientation()));
        } finally {
            FxTestUtils.runOnFxThread(synchronizer::stop);
        }
    }

    /// Keeps a queued stylesheet update when a popup callback also requests immediate synchronization.
    @Test
    void reentrantSyncDoesNotCancelPendingStylesheetUpdate() {
        Pane owner = new Pane();
        Pane popup = new Pane();
        M3PopupContextSynchronizer synchronizer = FxTestUtils.callOnFxThread(() ->
                new M3PopupContextSynchronizer(owner, popup));
        String stylesheet = M3Stylesheets.controlStylesheet("button.css");
        try {
            FxTestUtils.runOnFxThread(() -> {
                Scene scene = new Scene(owner);
                AtomicBoolean changeSource = new AtomicBoolean(true);
                popup.getStylesheets().addListener((ListChangeListener<String>) change -> {
                    if (changeSource.getAndSet(false)) {
                        scene.getStylesheets().add(stylesheet);
                        synchronizer.sync();
                    }
                });
                synchronizer.start();
            });
            FxTestUtils.runOnFxThread(() -> assertTrue(popup.getStylesheets().contains(stylesheet)));
        } finally {
            FxTestUtils.runOnFxThread(synchronizer::stop);
        }
    }

    /// Stops subsequent copying steps when an orientation callback stops the initial pass.
    @Test
    void stoppingDuringOrientationSkipsMotionAndStylesheetCopy() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            new Scene(owner);
            M3MotionSettings.setReducedMotionRequested(owner, true);
            Pane popup = new Pane();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popup);
            popup.nodeOrientationProperty().addListener(observable -> synchronizer.stop());

            try {
                synchronizer.start();
                assertFalse(M3MotionSettings.isReducedMotionRequested(popup));
                assertTrue(popup.getStylesheets().isEmpty());
            } finally {
                synchronizer.stop();
                M3MotionSettings.setReducedMotionRequested(owner, false);
            }
        });
    }

    /// Preserves a callback's replacement observation when the superseded startup fails.
    @Test
    void failedStartupDoesNotStopReplacementObservation() {
        Pane owner = new Pane();
        Pane popup = new Pane();
        AtomicReference<@Nullable M3PopupContextSynchronizer> reference = new AtomicReference<>();
        AtomicBoolean restart = new AtomicBoolean(true);
        IllegalStateException failure = new IllegalStateException("superseded lookup failed");
        M3PopupContextSynchronizer synchronizer = FxTestUtils.callOnFxThread(() -> {
            Scene scene = new Scene(owner);
            return new M3PopupContextSynchronizer(owner, popup, () -> {
                if (restart.getAndSet(false)) {
                    M3PopupContextSynchronizer current = Objects.requireNonNull(reference.get());
                    current.stop();
                    current.start();
                    throw failure;
                }
                return scene.getStylesheets();
            }, () -> null);
        });
        reference.set(synchronizer);
        try {
            FxTestUtils.runOnFxThread(() ->
                    assertSame(failure, assertThrows(IllegalStateException.class, synchronizer::start)));
            FxTestUtils.runOnFxThread(() -> {
                assertTrue(popup.getStylesheets().contains(M3Stylesheets.fallbackStylesheet()));
                owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, popup.getNodeOrientation());
            });
        } finally {
            FxTestUtils.runOnFxThread(synchronizer::stop);
        }
    }

    /// Copies owner motion changes raised from the orientation-only synchronization path.
    @Test
    void motionChangedDuringOrientationSynchronizationIsEventuallyCopied() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            Pane popup = new Pane();
            M3PopupContextSynchronizer synchronizer = FxTestUtils.callOnFxThread(() ->
                    new M3PopupContextSynchronizer(owner, popup));
            try {
                FxTestUtils.runOnFxThread(() -> {
                    M3MotionSettings.setGlobalReducedMotionRequested(false);
                    new Scene(owner);
                    synchronizer.start();
                    popup.nodeOrientationProperty().addListener(observable ->
                            M3MotionSettings.setReducedMotionRequested(owner, true));
                    owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                });
                FxTestUtils.runOnFxThread(() -> assertTrue(M3MotionSettings.isReducedMotionRequested(popup)));
            } finally {
                FxTestUtils.runOnFxThread(() -> {
                    synchronizer.stop();
                    M3MotionSettings.setReducedMotionRequested(owner, false);
                });
            }
        });
    }

    /// Preserves theme changes raised while copied popup metadata is notifying its listeners.
    @Test
    void themeChangedDuringSynchronizationIsEventuallyCopied() {
        Pane owner = new Pane();
        Pane popup = new Pane();
        M3Theme theme = M3Theme.defaultTheme();
        M3PopupContextSynchronizer synchronizer = FxTestUtils.callOnFxThread(() ->
                new M3PopupContextSynchronizer(owner, popup));
        try {
            FxTestUtils.runOnFxThread(() -> {
                new Scene(owner);
                M3ThemeManager.install(owner, theme);
                AtomicBoolean clear = new AtomicBoolean(true);
                popup.getProperties().addListener((MapChangeListener<Object, Object>) change -> {
                    if (M3ThemeMetadata.isThemePropertyKey(change.getKey()) && clear.getAndSet(false)) {
                        M3ThemeManager.uninstall(owner);
                    }
                });
                synchronizer.start();
            });
            FxTestUtils.runOnFxThread(() -> assertNull(M3ThemeMetadata.getTheme(popup)));
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                synchronizer.stop();
                M3ThemeManager.uninstall(owner);
            });
        }
    }
}
