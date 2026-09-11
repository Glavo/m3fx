// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Orientation;
import javafx.scene.input.ScrollEvent;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies ownership when overscroll lifecycle callbacks change the configured effect.
@NotNullByDefault
final class M3OverscrollEffectLifecycleTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains the replacement selected by an attachment callback and detaches both retired effects.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void attachmentCanReplaceEffect(boolean propertyCreated) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane(propertyCreated);
            RecordingEffect first = new RecordingEffect();
            RecordingEffect second = new RecordingEffect();
            RecordingEffect last = new RecordingEffect();
            pane.setOverscrollEffect(first);
            second.attached = () -> pane.setOverscrollEffect(last);

            pane.setOverscrollEffect(second);

            assertSame(last, pane.getOverscrollEffect());
            assertSame(pane, last.getScrollPane());
            assertRetired(first);
            assertRetired(second);
            assertEquals(1, last.attachCount);
            assertEquals(0, last.detachCount);
        });
    }

    /// Honors disablement requested while the first explicit effect is attaching.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void attachmentCanDisableEffect(boolean propertyCreated) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane(propertyCreated);
            RecordingEffect effect = new RecordingEffect();
            effect.attached = () -> pane.setOverscrollEffect(null);

            pane.setOverscrollEffect(effect);

            assertNull(pane.getOverscrollEffect());
            assertRetired(effect);
        });
    }

    /// Finishes detachment before reattaching an effect selected by its own cleanup callback.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void detachmentCanRestoreEffect(boolean propertyCreated) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane(propertyCreated);
            RecordingEffect first = new RecordingEffect();
            RecordingEffect second = new RecordingEffect();
            pane.setOverscrollEffect(first);
            first.detached = () -> pane.setOverscrollEffect(first);

            pane.setOverscrollEffect(second);

            assertSame(first, pane.getOverscrollEffect());
            assertSame(pane, first.getScrollPane());
            assertEquals(2, first.attachCount);
            assertEquals(1, first.detachCount);
            assertRetired(second);
        });
    }

    /// Exposes the requested effect when its callback first materializes the property.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void attachmentObservesConfiguredEffect(boolean propertyCreated) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane(propertyCreated);
            RecordingEffect effect = new RecordingEffect();
            effect.attached = () -> {
                assertSame(effect, pane.getOverscrollEffect());
                assertSame(effect, pane.overscrollEffectProperty().get());
            };

            pane.setOverscrollEffect(effect);

            assertSame(effect, pane.getOverscrollEffect());
            assertSame(effect, pane.overscrollEffectProperty().get());
            assertSame(pane, effect.getScrollPane());
        });
    }

    /// Keeps the previous owner after attachment fails and permits a later successful replacement.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void attachmentFailureAllowsRecovery(boolean propertyCreated) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane(propertyCreated);
            RecordingEffect previous = new RecordingEffect();
            RecordingEffect failed = new RecordingEffect();
            RecordingEffect replacement = new RecordingEffect();
            IllegalStateException failure = new IllegalStateException("Attachment failed");
            pane.setOverscrollEffect(previous);
            failed.attached = () -> {
                throw failure;
            };

            assertSame(failure, assertThrows(IllegalStateException.class,
                    () -> pane.setOverscrollEffect(failed)));
            assertSame(pane, previous.getScrollPane());
            assertThrows(IllegalStateException.class, failed::getScrollPane);
            assertEquals(0, previous.detachCount);

            pane.setOverscrollEffect(replacement);

            assertSame(replacement, pane.getOverscrollEffect());
            assertSame(pane, replacement.getScrollPane());
            assertRetired(previous);
        });
    }

    /// Clears a failed detachment's ownership and permits subsequent replacement and reuse.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void detachmentFailureAllowsRecovery(boolean propertyCreated) {
        FxTestUtils.runOnFxThread(() -> {
            M3ScrollPane pane = pane(propertyCreated);
            RecordingEffect previous = new RecordingEffect();
            RecordingEffect replacement = new RecordingEffect();
            IllegalStateException failure = new IllegalStateException("Detachment failed");
            pane.setOverscrollEffect(previous);
            previous.detached = () -> {
                throw failure;
            };

            assertSame(failure, assertThrows(IllegalStateException.class,
                    () -> pane.setOverscrollEffect(replacement)));
            assertSame(pane, replacement.getScrollPane());
            assertRetired(previous);

            pane.setOverscrollEffect(previous);

            assertSame(previous, pane.getOverscrollEffect());
            assertSame(pane, previous.getScrollPane());
            assertEquals(2, previous.attachCount);
            assertRetired(replacement);
        });
    }

    /// Creates an explicitly disabled pane with optional property materialization.
    private static M3ScrollPane pane(boolean propertyCreated) {
        M3ScrollPane pane = new M3ScrollPane();
        pane.setOverscrollEffect(null);
        if (propertyCreated) {
            pane.overscrollEffectProperty();
        }
        return pane;
    }

    /// Checks that a retired effect received cleanup exactly once and no longer has an owner.
    private static void assertRetired(RecordingEffect effect) {
        assertEquals(1, effect.attachCount);
        assertEquals(1, effect.detachCount);
        assertThrows(IllegalStateException.class, effect::getScrollPane);
    }

    /// Records lifecycle transitions and invokes each configured callback at most once.
    @NotNullByDefault
    private static final class RecordingEffect extends M3OverscrollEffect {
        /// The next attachment callback, or `null` when absent or already invoked.
        private @Nullable Runnable attached;
        /// The next detachment callback, or `null` when absent or already invoked.
        private @Nullable Runnable detached;
        /// The number of completed attachment entries.
        private int attachCount;
        /// The number of completed detachment entries.
        private int detachCount;

        /// Creates an effect without rendering resources.
        private RecordingEffect() {
        }

        @Override
        protected void onAttached(M3ScrollPane scrollPane) {
            attachCount++;
            @Nullable Runnable callback = attached;
            attached = null;
            if (callback != null) {
                callback.run();
            }
        }

        @Override
        protected void onDetached(M3ScrollPane scrollPane) {
            detachCount++;
            @Nullable Runnable callback = detached;
            detached = null;
            if (callback != null) {
                callback.run();
            }
        }

        @Override
        protected double onApplyToScroll(Orientation orientation, double delta,
                                         ScrollEvent event, DoubleUnaryOperator performScroll) {
            return performScroll.applyAsDouble(delta);
        }

        @Override
        protected void onRelease() {
        }

        /// Returns `false` because this effect does not render a response.
        @Override
        public boolean isInProgress() {
            return false;
        }
    }
}
