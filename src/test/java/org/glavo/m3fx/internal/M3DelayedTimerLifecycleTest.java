// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/// Tests delayed interaction lifetime across scene and window presentation changes.
@NotNullByDefault
final class M3DelayedTimerLifecycleTest {
    /// A delay short enough to complete during a deterministic pulse-driven test.
    private static final Duration SHORT_DELAY = Duration.millis(60.0);

    /// A delay long enough that lifecycle cancellation, rather than expiration, determines the outcome.
    private static final Duration LONG_DELAY = Duration.seconds(30.0);

    /// Starts the JavaFX toolkit before delayed interaction tests create scenes and windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that a scene without an associated window still runs and releases delayed interactions.
    @Test
    void delaysCompleteInSceneWithoutWindow() throws InterruptedException {
        AtomicReference<@Nullable LifecycleFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable LifecycleFixture fixture = fixtureReference.get();
                        return fixture != null && fixture.delaysReleased();
                    },
                    () -> {
                        LifecycleFixture fixture = new LifecycleFixture(SHORT_DELAY);
                        fixtureReference.set(fixture);
                        fixture.startDelays();
                        fixture.assertDelaysPending();
                    },
                    () -> Objects.requireNonNull(fixtureReference.get(), "fixture").assertDelaysReleased()
            );
        } finally {
            disposeFixture(fixtureReference);
        }
    }

    /// Verifies that removing owners from their scene cancels pending work and releases lifecycle observers.
    @Test
    void sceneDetachmentCancelsDelays() {
        FxTestUtils.runOnFxThread(() -> {
            LifecycleFixture fixture = new LifecycleFixture(LONG_DELAY);
            try {
                fixture.startDelays();
                fixture.assertDelaysPending();

                fixture.root.getChildren().clear();

                fixture.assertDelaysReleased();
            } finally {
                fixture.dispose();
            }
        });
    }

    /// Verifies that delayed work is not started while the scene is associated with a hidden window.
    @Test
    @Tier2Test
    void hiddenAssociatedWindowRejectsDelays() {
        FxTestUtils.runOnFxThread(() -> {
            LifecycleFixture fixture = new LifecycleFixture(LONG_DELAY);
            Stage stage = new Stage();
            try {
                stage.setScene(fixture.scene);
                assertFalse(stage.isShowing());

                fixture.startDelays();

                fixture.assertDelaysReleased();
            } finally {
                fixture.dispose();
                stage.close();
            }
        });
    }

    /// Verifies that hiding a presenting window cancels pending work and releases lifecycle observers.
    @Test
    @Tier2Test
    void hidingWindowCancelsDelays() {
        FxTestUtils.runOnFxThread(() -> {
            LifecycleFixture fixture = new LifecycleFixture(LONG_DELAY);
            Stage stage = new Stage();
            try {
                stage.setScene(fixture.scene);
                stage.show();
                fixture.startDelays();
                fixture.assertDelaysPending();

                stage.hide();

                fixture.assertDelaysReleased();
            } finally {
                fixture.dispose();
                stage.close();
            }
        });
    }

    /// Disposes a fixture retained by an asynchronous test, when one was created.
    ///
    /// @param fixtureReference the reference containing the fixture to dispose
    private static void disposeFixture(AtomicReference<@Nullable LifecycleFixture> fixtureReference) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable LifecycleFixture fixture = fixtureReference.getAndSet(null);
            if (fixture != null) {
                fixture.dispose();
            }
        });
    }

    /// Creates a pointer-entry event for installed tooltip activation.
    ///
    /// @return a pointer-entry event with no pressed mouse buttons
    private static MouseEvent mouseEnteredEvent() {
        return new MouseEvent(
                MouseEvent.MOUSE_ENTERED,
                1.0,
                1.0,
                1.0,
                1.0,
                MouseButton.NONE,
                0,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                null
        );
    }

    /// Holds one type-ahead timer and one tooltip timer in the same presentation context.
    @NotNullByDefault
    private static final class LifecycleFixture {
        /// The owner used by the type-ahead state.
        private final Pane typeAheadOwner = new Pane();

        /// The owner used by the installed tooltip.
        private final Pane tooltipOwner = new Pane();

        /// The scene root containing both delayed-interaction owners.
        private final Pane root = new Pane(typeAheadOwner, tooltipOwner);

        /// The scene whose optional window controls both delayed interactions.
        private final Scene scene = new Scene(root);

        /// The type-ahead state under test.
        private final M3TypeAheadState typeAheadState = new M3TypeAheadState(typeAheadOwner);

        /// The installed tooltip under test.
        private final M3Tooltip tooltip = new M3Tooltip("Details");

        /// The type-ahead owner property count before the current delays start.
        private int typeAheadOwnerPropertyCount;

        /// The tooltip owner property count before the current delays start.
        private int tooltipOwnerPropertyCount;

        /// The scene property count before the current delays start.
        private int scenePropertyCount;

        /// Creates a fixture whose two delayed interactions use the supplied duration.
        ///
        /// @param delay the type-ahead reset and tooltip show delay
        private LifecycleFixture(Duration delay) {
            M3MotionBehavior behavior = M3MotionBehavior.builder()
                    .typeAheadResetDelay(delay)
                    .build();
            FxTestUtils.setMotionBehavior(root, behavior);
            tooltip.setShowDelay(delay);
            tooltip.setShowDuration(Duration.INDEFINITE);
            M3Tooltip.install(tooltipOwner, tooltip);
        }

        /// Starts both delayed interactions and records their persistent property-map baselines.
        private void startDelays() {
            typeAheadOwnerPropertyCount = typeAheadOwner.getProperties().size();
            tooltipOwnerPropertyCount = tooltipOwner.getProperties().size();
            scenePropertyCount = scene.getProperties().size();

            typeAheadState.append("a");
            tooltipOwner.fireEvent(mouseEnteredEvent());
        }

        /// Returns whether both timers and their shared lifecycle observation have been released.
        ///
        /// @return `true` when no delayed prefix or observer registration remains
        private boolean delaysReleased() {
            return typeAheadState.getPrefix().isEmpty()
                    && typeAheadOwner.getProperties().size() == typeAheadOwnerPropertyCount
                    && tooltipOwner.getProperties().size() == tooltipOwnerPropertyCount
                    && scene.getProperties().size() == scenePropertyCount;
        }

        /// Asserts that both interactions retain pending timers and shared scene observation.
        private void assertDelaysPending() {
            assertEquals("a", typeAheadState.getPrefix());
            assertEquals(typeAheadOwnerPropertyCount + 1, typeAheadOwner.getProperties().size());
            assertEquals(tooltipOwnerPropertyCount + 1, tooltipOwner.getProperties().size());
            assertEquals(scenePropertyCount + 1, scene.getProperties().size());
            assertFalse(tooltip.isShowing());
        }

        /// Asserts that both interactions and their lifecycle observation have been released.
        private void assertDelaysReleased() {
            assertEquals("", typeAheadState.getPrefix());
            assertEquals(typeAheadOwnerPropertyCount, typeAheadOwner.getProperties().size());
            assertEquals(tooltipOwnerPropertyCount, tooltipOwner.getProperties().size());
            assertEquals(scenePropertyCount, scene.getProperties().size());
            assertFalse(tooltip.isShowing());
        }

        /// Releases installed handlers, timers, and the test-local motion override.
        private void dispose() {
            typeAheadState.clear();
            M3Tooltip.uninstall(tooltipOwner, tooltip);
            FxTestUtils.clearMotionBehavior(root);
        }
    }
}
