// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies stretch resource ownership across scroll, transform, and content callbacks.
@NotNullByDefault
final class M3StretchOverscrollEffectReentrancyTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains bounded scrolling when its callback disables the decorating effect.
    @Test
    void scrollCallbackCanDisableEffect() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            M3StretchOverscrollEffect effect = effect(pane);

            double consumed = effect.applyToScroll(Orientation.VERTICAL, 80, event(), delta -> {
                pane.setOverscrollEffect(null);
                return delta / 2;
            });

            assertEquals(40, consumed);
            assertFalse(effect.isInProgress());
            assertTrue(pane.getContent().getTransforms().isEmpty());
        });
    }

    /// Does not apply an old scroll remainder to a later owner of the same effect instance.
    @Test
    void scrollCallbackCanTransferEffect() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane first = pane();
            M3ScrollPane second = pane();
            M3StretchOverscrollEffect effect = effect(first);
            double[] nestedScale = new double[1];

            double consumed = effect.applyToScroll(Orientation.VERTICAL, 80, event(), delta -> {
                first.setOverscrollEffect(null);
                second.setOverscrollEffect(effect);
                pull(effect, Orientation.VERTICAL, 20);
                nestedScale[0] = scale(second).getY();
                return 0;
            });

            assertEquals(0, consumed);
            assertEquals(nestedScale[0], scale(second).getY());
            assertTrue(first.getContent().getTransforms().isEmpty());
            second.setOverscrollEffect(null);
        });
    }

    /// Retires an installation synchronously when adding its transform disables overscroll.
    @Test
    void transformInstallationCanDisableEffect() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            M3StretchOverscrollEffect effect = effect(pane);
            AtomicBoolean changed = new AtomicBoolean();
            pane.getContent().getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (changed.compareAndSet(false, true)) {
                    pane.setOverscrollEffect(null);
                }
            });

            pull(effect, Orientation.VERTICAL, 80);

            assertFalse(effect.isInProgress());
            assertTrue(pane.getContent().getTransforms().isEmpty());
        });
    }

    /// Follows the latest content selected while removing the old content's transform.
    @Test
    void transformRemovalCanReplaceContentAgain() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            M3StretchOverscrollEffect effect = effect(pane);
            Region original = (Region) pane.getContent();
            Region intermediate = content();
            Region last = content();
            pull(effect, Orientation.VERTICAL, 80);
            Scale scale = scale(pane);
            AtomicBoolean changed = new AtomicBoolean();
            original.getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (changed.compareAndSet(false, true)) {
                    pane.setContent(last);
                }
            });

            pane.setContent(intermediate);

            assertSame(last, pane.getContent());
            assertTrue(original.getTransforms().isEmpty());
            assertTrue(intermediate.getTransforms().isEmpty());
            assertSame(scale, scale(pane));
            pane.setOverscrollEffect(null);
            assertTrue(last.getTransforms().isEmpty());
        });
    }

    /// Stops remaining transform writes when a pivot listener detaches the effect.
    @Test
    void pivotChangeCanDisableEffect() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            M3StretchOverscrollEffect effect = effect(pane);
            pull(effect, Orientation.HORIZONTAL, 40);
            Scale scale = scale(pane);
            scale.pivotYProperty().addListener(observable -> pane.setOverscrollEffect(null));

            pull(effect, Orientation.VERTICAL, -80);

            assertEquals(1, scale.getX());
            assertEquals(1, scale.getY());
            assertTrue(pane.getContent().getTransforms().isEmpty());
            assertFalse(effect.isInProgress());
        });
    }

    /// Recomputes both axes when a scale listener changes the resistance during an update.
    @Test
    void scaleChangeCanUpdateResistance() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            M3StretchOverscrollEffect effect = effect(pane);
            pull(effect, Orientation.HORIZONTAL, 40);
            pull(effect, Orientation.VERTICAL, 80);
            Scale scale = scale(pane);
            AtomicBoolean changed = new AtomicBoolean();
            scale.xProperty().addListener(observable -> {
                if (changed.compareAndSet(false, true)) {
                    effect.setResistance(1);
                }
            });

            effect.setMaximumStretch(0.2);

            assertEquals(1 + 0.2 * -Math.expm1(-40 / pane.getViewportBounds().getWidth()), scale.getX(), 1e-12);
            assertEquals(1 + 0.2 * -Math.expm1(-80 / pane.getViewportBounds().getHeight()), scale.getY(), 1e-12);
            pane.setOverscrollEffect(null);
        });
    }

    /// Keeps a new pull started by transform removal while the old release completes.
    @Test
    void releaseCleanupCanStartAnotherPull() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            M3MotionSettings.setReducedMotionRequested(pane, true);
            M3StretchOverscrollEffect effect = effect(pane);
            pull(effect, Orientation.VERTICAL, 80);
            Scale previous = scale(pane);
            AtomicBoolean changed = new AtomicBoolean();
            pane.getContent().getTransforms().addListener((ListChangeListener<Transform>) change -> {
                if (changed.compareAndSet(false, true)) {
                    pull(effect, Orientation.HORIZONTAL, 40);
                }
            });

            effect.release();

            assertNotSame(previous, scale(pane));
            assertTrue(scale(pane).getX() > 1);
            assertEquals(1, scale(pane).getY());
            assertTrue(effect.isInProgress());
            pane.setOverscrollEffect(null);
        });
    }

    /// Counts relaxation already performed before a progress listener transfers the effect.
    @Test
    void relaxationCallbackStillInvokesBoundedScrollOnce() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane first = pane();
            M3ScrollPane second = pane();
            M3StretchOverscrollEffect effect = effect(first);
            pull(effect, Orientation.VERTICAL, 80);
            AtomicBoolean changed = new AtomicBoolean();
            effect.inProgressProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue && changed.compareAndSet(false, true)) {
                    first.setOverscrollEffect(null);
                    second.setOverscrollEffect(effect);
                    pull(effect, Orientation.HORIZONTAL, 20);
                }
            });
            AtomicInteger invocations = new AtomicInteger();

            double consumed = effect.applyToScroll(Orientation.VERTICAL, -100, event(), available -> {
                invocations.incrementAndGet();
                assertEquals(-20, available);
                return -10;
            });

            assertEquals(1, invocations.get());
            assertEquals(-90, consumed);
            assertTrue(scale(second).getX() > 1);
            assertEquals(1, scale(second).getY());
            second.setOverscrollEffect(null);
        });
    }

    /// Does not duplicate the owned transform when replacement content already contains it.
    @Test
    void contentReplacementRetainsAnExistingTransformOnce() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            M3StretchOverscrollEffect effect = effect(pane);
            pull(effect, Orientation.VERTICAL, 80);
            Scale scale = scale(pane);
            Region replacement = content();
            replacement.getTransforms().add(scale);

            pane.setContent(replacement);

            assertSame(scale, scale(pane));
            pane.setOverscrollEffect(null);
            assertTrue(replacement.getTransforms().isEmpty());
        });
    }

    /// Creates laid-out content with room to scroll on both axes.
    private static Region content() {
        Region content = new Region();
        content.setPrefSize(320, 480);
        return content;
    }

    /// Creates a pane with usable viewport geometry without showing a native window.
    private static M3ScrollPane pane() {
        M3ScrollPane pane = new M3ScrollPane(content());
        new Scene(pane, 180, 120);
        pane.applyCss();
        pane.resize(180, 120);
        pane.layout();
        return pane;
    }

    /// Returns the pane's default stretch effect.
    private static M3StretchOverscrollEffect effect(M3ScrollPane pane) {
        return assertInstanceOf(M3StretchOverscrollEffect.class, pane.getOverscrollEffect());
    }

    /// Returns the currently rendered transform.
    private static Scale scale(M3ScrollPane pane) {
        assertEquals(1, pane.getContent().getTransforms().size());
        return assertInstanceOf(Scale.class, pane.getContent().getTransforms().get(0));
    }

    /// Offers a delta that is entirely available to the effect.
    private static void pull(M3StretchOverscrollEffect effect, Orientation axis, double delta) {
        effect.applyToScroll(axis, delta, event(), available -> 0);
    }

    /// Creates a direct scroll event for explicit effect application.
    private static ScrollEvent event() {
        return new ScrollEvent(ScrollEvent.SCROLL, 0, 0, 0, 0,
                false, false, false, false, true, false, 0, 0, 0, 0,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 1, null);
    }
}
