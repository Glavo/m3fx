// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies scroll dispatch when effect callbacks replace effects or deliver nested input.
@NotNullByDefault
final class M3ScrollPaneReentrancyTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Resolves the current effect and input policy independently for each nonempty axis.
    @ParameterizedTest
    @CsvSource({"disable,false", "disable,true", "replace,false", "replace,true", "mode,false"})
    void horizontalCallbackCanChangeVerticalEffect(String change, boolean direct) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            RecordingEffect replacement = new RecordingEffect();
            RecordingEffect original = new RecordingEffect();
            original.afterScroll = () -> {
                switch (change) {
                    case "disable" -> pane.setOverscrollEffect(null);
                    case "replace" -> pane.setOverscrollEffect(replacement);
                    case "mode" -> pane.setOverscrollInputMode(M3OverscrollInputMode.DIRECT);
                    default -> throw new AssertionError(change);
                }
            };
            pane.setOverscrollEffect(original);

            pane.fireEvent(event(pane, -20, -30, direct));

            assertEquals(List.of(Orientation.HORIZONTAL), original.axes);
            assertEquals(change.equals("replace") ? List.of(Orientation.VERTICAL) : List.of(), replacement.axes);
            assertTrue(pane.getHvalue() > 0.5);
            assertTrue(pane.getVvalue() > 0.5);
            pane.setOverscrollEffect(null);
        });
    }

    /// Does not release a retired effect or its unused replacement after a one-axis wheel event.
    @Test
    void replacementDuringWheelIsNotReleasedAsTheOldEffect() {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            RecordingEffect replacement = new RecordingEffect();
            RecordingEffect original = new RecordingEffect();
            original.afterScroll = () -> pane.setOverscrollEffect(replacement);
            pane.setOverscrollEffect(original);

            pane.fireEvent(event(pane, 0, -30, false));

            assertEquals(0, original.releases);
            assertEquals(0, replacement.releases);
            assertSame(replacement, pane.getOverscrollEffect());
            pane.setOverscrollEffect(null);
        });
    }

    /// Restores the outer callback's validation and direct-input mode after a nested scroll event.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void valueListenerCanDeliverNestedScroll(boolean nestedDirect) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            RecordingEffect effect = new RecordingEffect();
            pane.setOverscrollEffect(effect);
            AtomicBoolean nested = new AtomicBoolean();
            pane.hvalueProperty().addListener(observable -> {
                if (nested.compareAndSet(false, true)) {
                    pane.fireEvent(event(pane, 0, -10, nestedDirect));
                }
            });
            double horizontalSpan = ((Region) pane.getContent()).getWidth() - pane.getViewportBounds().getWidth();
            double verticalSpan = ((Region) pane.getContent()).getHeight() - pane.getViewportBounds().getHeight();

            pane.fireEvent(event(pane, -20, -30, true));

            assertEquals(List.of(Orientation.HORIZONTAL, Orientation.VERTICAL, Orientation.VERTICAL), effect.axes);
            assertEquals(0.5 + 20 / horizontalSpan, pane.getHvalue(), 1e-12);
            assertEquals(0.5 + 40 / verticalSpan, pane.getVvalue(), 1e-12);
            pane.setOverscrollEffect(null);
        });
    }

    /// Restores direct-input mode when an effect dispatches nested input before invoking its callback.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void effectCanDeliverNestedScrollBeforeDelegating(boolean nestedDirect) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane();
            RecordingEffect effect = new RecordingEffect();
            pane.setOverscrollEffect(effect);
            AtomicBoolean nested = new AtomicBoolean();
            effect.beforeScroll = () -> {
                if (nested.compareAndSet(false, true)) {
                    pane.fireEvent(event(pane, 0, -10, nestedDirect));
                }
            };
            double horizontalSpan = ((Region) pane.getContent()).getWidth() - pane.getViewportBounds().getWidth();
            double verticalSpan = ((Region) pane.getContent()).getHeight() - pane.getViewportBounds().getHeight();

            pane.fireEvent(event(pane, -20, -30, true));

            assertEquals(List.of(Orientation.HORIZONTAL, Orientation.VERTICAL, Orientation.VERTICAL), effect.axes);
            assertEquals(0.5 + 20 / horizontalSpan, pane.getHvalue(), 1e-12);
            assertEquals(0.5 + 40 / verticalSpan, pane.getVvalue(), 1e-12);
            pane.setOverscrollEffect(null);
        });
    }

    /// Creates a laid-out pane with synchronous motion and both scroll positions centered.
    private static M3ScrollPane pane() {
        Region content = new Region();
        content.setPrefSize(320, 480);
        M3ScrollPane pane = new M3ScrollPane(content);
        pane.setOverscrollInputMode(M3OverscrollInputMode.ALL);
        pane.setAnimationsEnabled(false);
        new Scene(pane, 180, 120);
        pane.applyCss();
        pane.resize(180, 120);
        pane.layout();
        pane.setHvalue(0.5);
        pane.setVvalue(0.5);
        return pane;
    }

    /// Creates a scroll event with explicit pixel deltas and direct-input classification.
    private static ScrollEvent event(M3ScrollPane pane, double horizontal, double vertical, boolean direct) {
        return new ScrollEvent(pane, pane, ScrollEvent.SCROLL, 0, 0, 0, 0,
                false, false, false, false, direct, false, horizontal, vertical, horizontal, vertical,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, direct ? 1 : 0, null);
    }

    /// Records decorated axes and releases while delegating all movement to bounded scrolling.
    @NotNullByDefault
    private static final class RecordingEffect extends M3OverscrollEffect {
        /// The axes offered to this effect, in invocation order.
        private final List<Orientation> axes = new ArrayList<>();

        /// Runs before each bounded scroll operation.
        private Runnable beforeScroll = () -> {};

        /// Runs after each bounded scroll operation.
        private Runnable afterScroll = () -> {};

        /// The number of release requests received while attached.
        private int releases;

        /// Creates a recording effect with no renderer.
        private RecordingEffect() {
        }

        /// Records one axis and invokes test callbacks around bounded scrolling.
        @Override
        protected double onApplyToScroll(Orientation orientation, double delta,
                                         ScrollEvent event, DoubleUnaryOperator performScroll) {
            axes.add(orientation);
            beforeScroll.run();
            double consumed = performScroll.applyAsDouble(delta);
            afterScroll.run();
            return consumed;
        }

        /// Records a release without creating visual state.
        @Override
        protected void onRelease() {
            releases++;
        }

        /// Returns `false` because this effect does not render.
        @Override
        public boolean isInProgress() {
            return false;
        }
    }
}
