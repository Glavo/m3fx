// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    /// Verifies that disabled motion applies CSS-resolved hover opacity without starting a transition.
    @Test
    void disabledMotionAppliesCssResolvedHoverOpacityImmediately() {
        runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-button");
            M3MotionSettings.setAnimationsEnabled(owner, false);
            M3StateLayer stateLayer = new M3StateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);

            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            stateLayer.animateOverlayOpacityFromCss();

            assertTrue(overlay.getOpacity() > 0.0);
            assertFalse(stateLayer.isOverlayOpacityAnimationRunning());
        });
    }

    /// Verifies that disabled motion suppresses transient ripple animation.
    @Test
    void disabledMotionSuppressesRippleAnimation() {
        runOnFxThread(() -> {
            Pane owner = new Pane();
            M3MotionSettings.setAnimationsEnabled(owner, false);
            M3StateLayer stateLayer = new M3StateLayer();
            owner.getChildren().add(stateLayer);
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);

            stateLayer.playRipple(20.0, 20.0);

            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            assertEquals(0.0, ripple.getOpacity(), 0.0001);
            assertFalse(stateLayer.isRippleAnimationRunning());
        });
    }

    /// Verifies that plain focused state does not show a persistent state layer.
    @Test
    void stateLayerUsesFocusVisibleInsteadOfPlainFocus() {
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
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
            owner.applyCss();
            assertEquals(0.0, overlay.getOpacity(), 0.0001);

            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            owner.applyCss();
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
        });
    }

    /// Verifies that ripples remain visible until explicitly released.
    @Test
    void rippleHoldsUntilReleaseThenFades() throws InterruptedException {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                Pane root = new Pane();
                M3StateLayer stateLayer = new M3StateLayer();
                root.getChildren().add(stateLayer);
                Scene scene = new Scene(root, 100.0, 40.0);

                root.applyCss();
                stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
                stateLayer.playRipple(20.0, 20.0);

                PauseTransition expansionPause = new PauseTransition(Duration.millis(430.0));
                expansionPause.setOnFinished(event -> verifyRippleRelease(stateLayer, failure, latch));
                expansionPause.play();
            } catch (Throwable e) {
                failure.set(e);
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
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

    /// Verifies that an early release still lets the ripple expand before it fades.
    @Test
    void rippleReleasedBeforeExpansionCompletesStillExpandsBeforeFade() throws InterruptedException {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                Pane root = new Pane();
                M3StateLayer stateLayer = new M3StateLayer();
                root.getChildren().add(stateLayer);
                Scene scene = new Scene(root, 100.0, 40.0);

                root.applyCss();
                stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
                stateLayer.playRipple(12.0, 20.0);

                Region ripple = lookupRegion(stateLayer, ".m3-ripple");
                double startScale = Math.max(ripple.getScaleX(), ripple.getScaleY());
                stateLayer.releaseRipple();

                assertTrue(ripple.getOpacity() > 0.1);
                assertTrue(stateLayer.isRippleAnimationRunning());

                PauseTransition expansionPause = new PauseTransition(Duration.millis(160.0));
                expansionPause.setOnFinished(event -> verifyEarlyReleaseExpansion(
                        ripple,
                        startScale,
                        failure,
                        latch
                ));
                expansionPause.play();
            } catch (Throwable e) {
                failure.set(e);
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
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

    /// Verifies the visible part of a ripple that was released immediately after press.
    private static void verifyEarlyReleaseExpansion(
            Region ripple,
            double startScale,
            AtomicReference<Throwable> failure,
            CountDownLatch latch
    ) {
        try {
            assertTrue(Math.max(ripple.getScaleX(), ripple.getScaleY()) > startScale);
            assertTrue(ripple.getOpacity() > 0.1);

            PauseTransition completionPause = new PauseTransition(Duration.millis(560.0));
            completionPause.setOnFinished(event -> {
                try {
                    assertEquals(0.0, ripple.getOpacity(), 0.0001);
                    assertEquals(1.0, ripple.getScaleX(), 0.0001);
                    assertEquals(1.0, ripple.getScaleY(), 0.0001);
                } catch (Throwable e) {
                    failure.set(e);
                } finally {
                    latch.countDown();
                }
            });
            completionPause.play();
        } catch (Throwable e) {
            failure.set(e);
            latch.countDown();
        }
    }

    /// Verifies release behavior after the expansion phase has had time to complete.
    private static void verifyRippleRelease(
            M3StateLayer stateLayer,
            AtomicReference<Throwable> failure,
            CountDownLatch latch
    ) {
        try {
            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            assertTrue(ripple.getOpacity() > 0.1);

            stateLayer.releaseRipple();

            assertTrue(ripple.getOpacity() > 0.1);
            assertTrue(stateLayer.isRippleAnimationRunning());

            PauseTransition releasePause = new PauseTransition(Duration.millis(280.0));
            releasePause.setOnFinished(event -> {
                try {
                    assertEquals(0.0, ripple.getOpacity(), 0.0001);
                } catch (Throwable e) {
                    failure.set(e);
                } finally {
                    latch.countDown();
                }
            });
            releasePause.play();
        } catch (Throwable e) {
            failure.set(e);
            latch.countDown();
        }
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
