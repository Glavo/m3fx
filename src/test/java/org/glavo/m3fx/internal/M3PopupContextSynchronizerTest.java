// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Scrim;
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

    /// Applies popup context to actual descendants while preserving overrides and clearing it on stop.
    @Test
    void ordinaryPopupRootPropagatesMotionToContent() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            M3Scrim owner = new M3Scrim();
            new Scene(new Pane(owner));
            M3Scrim inherited = new M3Scrim();
            M3Scrim styled = new M3Scrim();
            styled.setStyle("-m3-animations-enabled: true;");
            M3Scrim bound = new M3Scrim();
            bound.animationsEnabledProperty().bind(new SimpleBooleanProperty(true));
            Pane popup = new Pane(new Pane(inherited, styled, bound));
            popup.setStyle("-fx-opacity: 0.75;");
            new Scene(popup);
            M3Scrim unrelated = new M3Scrim();
            new Scene(new Pane(unrelated));
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popup);
            try {
                owner.setAnimationsEnabled(false);
                synchronizer.start();
                popup.applyCss();
                assertFalse(inherited.isAnimationsEnabled());
                assertTrue(styled.isAnimationsEnabled());
                assertTrue(bound.isAnimationsEnabled());
                assertTrue(bound.animationsEnabledProperty().isBound());
                assertTrue(unrelated.isAnimationsEnabled());
                assertFalse(M3MotionSettings.shouldReduceMotion(popup));
                assertTrue(M3MotionSettings.shouldReduceMotion(inherited));
                assertEquals("-fx-opacity: 0.75;", popup.getStyle());

                owner.setAnimationsEnabled(true);
                popup.applyCss();
                assertTrue(inherited.isAnimationsEnabled());
                owner.setAnimationsEnabled(false);
                popup.applyCss();
                assertFalse(inherited.isAnimationsEnabled());

                synchronizer.stop();
                popup.applyCss();
                assertTrue(inherited.isAnimationsEnabled());
                owner.setAnimationsEnabled(true);
                owner.setAnimationsEnabled(false);
                popup.applyCss();
                assertTrue(inherited.isAnimationsEnabled());
                assertTrue(styled.isAnimationsEnabled());
                assertTrue(bound.isAnimationsEnabled());
                assertTrue(unrelated.isAnimationsEnabled());
            } finally {
                synchronizer.stop();
                bound.animationsEnabledProperty().unbind();
            }
        }));
    }

    /// Keeps observation stopped when a popup stylesheet callback stops the initial synchronization.
    @Test
    void stoppingDuringStartDoesNotInstallMotionObservation() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            M3Scrim owner = new M3Scrim();
            new Scene(owner);
            M3Scrim popup = new M3Scrim();
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
                owner.setAnimationsEnabled(false);
                assertTrue(popup.isAnimationsEnabled());

                synchronizer.start();
                assertFalse(popup.isAnimationsEnabled());
                owner.setAnimationsEnabled(true);
                assertTrue(popup.isAnimationsEnabled());
            } finally {
                synchronizer.start();
                synchronizer.stop();
                owner.setAnimationsEnabled(true);
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
            M3Scrim owner = new M3Scrim();
            new Scene(owner);
            owner.setAnimationsEnabled(false);
            M3Scrim popup = new M3Scrim();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popup);
            popup.nodeOrientationProperty().addListener(observable -> synchronizer.stop());

            try {
                synchronizer.start();
                assertTrue(popup.isAnimationsEnabled());
                assertTrue(popup.getStylesheets().isEmpty());
            } finally {
                synchronizer.stop();
                owner.setAnimationsEnabled(true);
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
            M3Scrim owner = new M3Scrim();
            M3Scrim popup = new M3Scrim();
            M3PopupContextSynchronizer synchronizer = FxTestUtils.callOnFxThread(() ->
                    new M3PopupContextSynchronizer(owner, popup));
            try {
                FxTestUtils.runOnFxThread(() -> {
                    M3MotionSettings.setGlobalReducedMotionRequested(false);
                    new Scene(owner);
                    synchronizer.start();
                    popup.nodeOrientationProperty().addListener(observable ->
                            owner.setAnimationsEnabled(false));
                    owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                });
                FxTestUtils.runOnFxThread(() -> assertFalse(popup.isAnimationsEnabled()));
            } finally {
                FxTestUtils.runOnFxThread(() -> {
                    synchronizer.stop();
                    owner.setAnimationsEnabled(true);
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
