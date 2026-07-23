// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies tooltip entrance, exit, and interrupted-exit motion in a presenting JavaFX window.
///
/// The tests observe the popup skin node rather than implementation-owned animation objects. This keeps the
/// assertions limited to the public popup lifecycle and the rendered opacity and scale channels.
@NotNullByDefault
@Tier2Test
final class M3TooltipMotionTest {
    /// The test-local effect duration used to make intermediate frames reliably observable.
    private static final Duration OBSERVABLE_DURATION = Duration.millis(600.0);

    /// The minimum distance from either endpoint required for an intermediate value.
    private static final double INTERMEDIATE_MARGIN = 0.02;

    /// Starts the JavaFX toolkit before a real stage or popup is created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes any popup or stage left visible after a failed assertion.
    @AfterEach
    void closeWindows() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                window.hide();
            }
        });
    }

    /// Verifies that showing exposes a rendered intermediate frame and settles at the neutral transform.
    @Test
    void entranceExposesIntermediateFrameAndSettlesAtNeutralTransform() throws InterruptedException {
        AtomicReference<@Nullable TooltipFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        return fixture != null && isEntranceIntermediate(visibilityState(fixture));
                    },
                    1,
                    () -> {
                        TooltipFixture fixture = createFixture(false);
                        fixtureReference.set(fixture);
                        showTooltip(fixture);
                    },
                    () -> {
                        TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        VisibilityState state = visibilityState(fixture);
                        assertTrue(fixture.tooltip().isShowing());
                        assertTrue(isEntranceIntermediate(state), () -> "entrance state: " + state);
                    }
            );

            awaitVisibleEndpoint(fixtureReference);
        } finally {
            disposeFixture(fixtureReference.get());
        }
    }

    /// Verifies that hiding retains the popup throughout its intermediate exit frame and closes only at the end.
    @Test
    void exitKeepsPopupShowingUntilAnimatedTransformSettles() throws InterruptedException {
        AtomicReference<@Nullable TooltipFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        return fixture != null
                                && fixture.tooltip().isShowing()
                                && isExitIntermediate(visibilityState(fixture));
                    },
                    1,
                    () -> {
                        TooltipFixture fixture = createFixture(true);
                        fixtureReference.set(fixture);
                        fixture.tooltip().hide();
                        assertTrue(fixture.tooltip().isShowing(), "hide closed the popup before its first frame");
                    },
                    () -> {
                        TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        VisibilityState state = visibilityState(fixture);
                        assertTrue(fixture.tooltip().isShowing());
                        assertTrue(isExitIntermediate(state), () -> "exit state: " + state);
                        assertEquals(0, fixture.hiddenTransitions().get());
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        return fixture != null && !fixture.tooltip().isShowing();
                    },
                    2,
                    () -> {
                    },
                    () -> {
                        TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertFalse(fixture.tooltip().isShowing());
                        assertEquals(1, fixture.hiddenTransitions().get());
                        assertNeutralTransform(visibilityState(fixture));
                    }
            );
        } finally {
            disposeFixture(fixtureReference.get());
        }
    }

    /// Verifies that showing during exit retargets the existing popup toward visibility without closing it.
    @Test
    void showDuringExitReversesExistingPopupWithoutHiddenTransition() throws InterruptedException {
        AtomicReference<@Nullable TooltipFixture> fixtureReference = new AtomicReference<>();
        AtomicReference<@Nullable VisibilityState> reversalStartReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> visibilityNodeReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        return fixture != null
                                && fixture.tooltip().isShowing()
                                && isReversalStartFrame(visibilityState(fixture));
                    },
                    1,
                    () -> {
                        TooltipFixture fixture = createFixture(true);
                        fixtureReference.set(fixture);
                        visibilityNodeReference.set(visibilityNode(fixture));
                        fixture.tooltip().hide();
                    },
                    () -> {
                        TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        VisibilityState reversalStart = visibilityState(fixture);
                        reversalStartReference.set(reversalStart);
                        assertTrue(fixture.tooltip().isShowing());
                        assertEquals(0, fixture.hiddenTransitions().get());

                        showTooltip(fixture);

                        assertTrue(fixture.tooltip().isShowing());
                        assertSame(visibilityNodeReference.get(), visibilityNode(fixture));
                        assertEquals(0, fixture.hiddenTransitions().get());
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        @Nullable VisibilityState reversalStart = reversalStartReference.get();
                        return fixture != null
                                && reversalStart != null
                                && isReversalIntermediate(visibilityState(fixture), reversalStart);
                    },
                    1,
                    () -> {
                    },
                    () -> {
                        TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        VisibilityState reversalStart = Objects.requireNonNull(
                                reversalStartReference.get(),
                                "reversal start"
                        );
                        VisibilityState state = visibilityState(fixture);
                        assertTrue(fixture.tooltip().isShowing());
                        assertTrue(
                                isReversalIntermediate(state, reversalStart),
                                () -> "reversal start=" + reversalStart + ", current=" + state
                        );
                        assertSame(visibilityNodeReference.get(), visibilityNode(fixture));
                        assertEquals(0, fixture.hiddenTransitions().get());
                    }
            );

            awaitVisibleEndpoint(fixtureReference);
        } finally {
            disposeFixture(fixtureReference.get());
        }
    }

    /// Verifies that programmatic focus restoration does not schedule a keyboard tooltip activation.
    @Test
    void programmaticFocusRestorationDoesNotActivateTooltip() throws InterruptedException {
        AtomicReference<@Nullable TooltipFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        return fixture != null
                                && fixture.owner().isFocused()
                                && !fixture.tooltip().isShowing();
                    },
                    3,
                    () -> {
                        TooltipFixture fixture = createFixture(false);
                        fixture.tooltip().setShowDelay(Duration.ZERO);
                        M3Tooltip.install(fixture.owner(), fixture.tooltip());
                        fixtureReference.set(fixture);
                        fixture.owner().requestFocus();
                    },
                    () -> {
                        TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertTrue(fixture.owner().isFocused());
                        assertFalse(fixture.tooltip().isShowing(),
                                "programmatic owner focus must not open a tooltip");
                    }
            );
        } finally {
            @Nullable TooltipFixture fixture = fixtureReference.get();
            if (fixture != null) {
                M3Tooltip.uninstall(fixture.owner(), fixture.tooltip());
            }
            disposeFixture(fixture);
        }
    }

    /// Verifies that pointer exit hides a transient tooltip even when the owner retains focus.
    @Test
    void pointerExitHidesTooltipWhenOwnerRetainsFocus() throws InterruptedException {
        AtomicReference<@Nullable TooltipFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        return fixture != null && fixture.tooltip().isShowing();
                    },
                    2,
                    () -> {
                        TooltipFixture fixture = createFixture(false);
                        fixture.tooltip().setShowDelay(Duration.ZERO);
                        fixture.tooltip().setHideDelay(Duration.ZERO);
                        M3Tooltip.install(fixture.owner(), fixture.tooltip());
                        fixtureReference.set(fixture);
                        M3MotionSettings.setReducedMotionRequested(fixture.root(), true);
                        fixture.owner().fireEvent(pointerEvent(MouseEvent.MOUSE_ENTERED));
                    },
                    () -> {
                        TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        fixture.owner().requestFocus();
                        fixture.owner().fireEvent(pointerEvent(MouseEvent.MOUSE_EXITED));
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        return fixture != null && !fixture.tooltip().isShowing();
                    },
                    2,
                    () -> {
                    },
                    () -> assertFalse(
                            Objects.requireNonNull(fixtureReference.get(), "fixture").tooltip().isShowing()
                    )
            );
        } finally {
            @Nullable TooltipFixture fixture = fixtureReference.get();
            if (fixture != null) {
                M3Tooltip.uninstall(fixture.owner(), fixture.tooltip());
            }
            disposeFixture(fixture);
        }
    }

    /// Verifies that hiding an installed target cancels a delayed tooltip activation.
    @Test
    void hiddenTargetCancelsDelayedActivation() throws InterruptedException {
        AtomicReference<@Nullable TooltipFixture> fixtureReference = new AtomicReference<>();
        AtomicLong activationStartNanos = new AtomicLong();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable TooltipFixture fixture = fixtureReference.get();
                        return fixture != null
                                && activationStartNanos.get() != 0L
                                && System.nanoTime() - activationStartNanos.get() >= 600_000_000L
                                && !fixture.tooltip().isShowing();
                    },
                    2,
                    () -> {
                        TooltipFixture fixture = createFixture(false);
                        fixture.tooltip().setShowDelay(Duration.millis(300.0));
                        M3Tooltip.install(fixture.owner(), fixture.tooltip());
                        fixtureReference.set(fixture);

                        fixture.owner().fireEvent(pointerEvent(MouseEvent.MOUSE_ENTERED));
                        fixture.owner().setVisible(false);
                        activationStartNanos.set(System.nanoTime());
                    },
                    () -> {
                        TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertFalse(fixture.tooltip().isShowing());
                    }
            );
        } finally {
            @Nullable TooltipFixture fixture = fixtureReference.get();
            if (fixture != null) {
                M3Tooltip.uninstall(fixture.owner(), fixture.tooltip());
            }
            disposeFixture(fixture);
        }
    }

    /// Creates a synthetic pointer transition for tooltip lifecycle tests.
    ///
    /// @param eventType the pointer transition type
    /// @return a pointer event at a stable local coordinate
    private static MouseEvent pointerEvent(EventType<MouseEvent> eventType) {
        return new MouseEvent(
                eventType,
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

    /// Creates a real window and an optionally visible tooltip with an observable test-local motion scheme.
    ///
    /// @param initiallyVisible whether the tooltip should be shown synchronously at its visible endpoint
    /// @return the initialized fixture
    private static TooltipFixture createFixture(boolean initiallyVisible) {
        M3Button owner = new M3Button("Tooltip owner");
        StackPane root = new StackPane(owner);
        Scene scene = new Scene(root, 360.0, 180.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setMotionScheme(root, observableMotionScheme());

        Stage stage = new Stage();
        try {
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();

            Bounds ownerBounds = Objects.requireNonNull(
                    owner.localToScreen(owner.getBoundsInLocal()),
                    "owner screen bounds"
            );
            M3Tooltip tooltip = new M3Tooltip("Animated tooltip");
            AtomicInteger hiddenTransitions = new AtomicInteger();
            tooltip.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    hiddenTransitions.incrementAndGet();
                }
            });
            TooltipFixture fixture = new TooltipFixture(
                    stage,
                    root,
                    owner,
                    tooltip,
                    ownerBounds.getMinX(),
                    ownerBounds.getMaxY() + 8.0,
                    hiddenTransitions
            );

            if (initiallyVisible) {
                M3MotionSettings.setReducedMotionRequested(root, true);
                try {
                    showTooltip(fixture);
                } finally {
                    M3MotionSettings.setReducedMotionRequested(root, false);
                }
                assertTrue(tooltip.isShowing());
                assertNeutralTransform(visibilityState(fixture));
            }
            return fixture;
        } catch (RuntimeException | Error exception) {
            stage.close();
            throw exception;
        }
    }

    /// Shows the fixture tooltip at its stable screen anchor.
    ///
    /// @param fixture the fixture to show
    private static void showTooltip(TooltipFixture fixture) {
        fixture.tooltip().show(fixture.owner(), fixture.anchorX(), fixture.anchorY());
    }

    /// Waits until a tooltip remains fully visible for consecutive JavaFX pulses.
    ///
    /// @param fixtureReference the fixture reference initialized by the test setup
    private static void awaitVisibleEndpoint(
            AtomicReference<@Nullable TooltipFixture> fixtureReference
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    @Nullable TooltipFixture fixture = fixtureReference.get();
                    return fixture != null
                            && fixture.tooltip().isShowing()
                            && isNeutralTransform(visibilityState(fixture));
                },
                3,
                () -> {
                },
                () -> {
                    TooltipFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                    assertTrue(fixture.tooltip().isShowing());
                    assertNeutralTransform(visibilityState(fixture));
                    assertEquals(0, fixture.hiddenTransitions().get());
                }
        );
    }

    /// Returns the popup skin node whose rendered channels carry tooltip visibility motion.
    ///
    /// @param fixture the active tooltip fixture
    /// @return the popup skin node
    private static Node visibilityNode(TooltipFixture fixture) {
        Skin<?> skin = Objects.requireNonNull(fixture.tooltip().getSkin(), "tooltip skin");
        return skin.getNode();
    }

    /// Captures the current opacity and scale channels from the popup skin node.
    ///
    /// @param fixture the active tooltip fixture
    /// @return the current visibility state
    private static VisibilityState visibilityState(TooltipFixture fixture) {
        Node node = visibilityNode(fixture);
        return new VisibilityState(node.getOpacity(), node.getScaleX(), node.getScaleY());
    }

    /// Returns whether a state is strictly between the hidden and visible entrance endpoints.
    ///
    /// @param state the state to inspect
    /// @return `true` when both opacity and scale are observable intermediate values
    private static boolean isEntranceIntermediate(VisibilityState state) {
        return isBetweenHiddenAndVisible(state.opacity(), 0.0)
                && isBetweenHiddenAndVisible(state.scaleX(), 0.8)
                && isBetweenHiddenAndVisible(state.scaleY(), 0.8);
    }

    /// Returns whether a state is strictly between the visible and hidden exit endpoints.
    ///
    /// @param state the state to inspect
    /// @return `true` when both opacity and scale are observable intermediate values
    private static boolean isExitIntermediate(VisibilityState state) {
        return isEntranceIntermediate(state);
    }

    /// Returns whether an exit has reached a middle frame with enough remaining distance to observe reversal.
    ///
    /// @param state the state to inspect
    /// @return `true` when opacity and scale have moved well clear of both endpoints
    private static boolean isReversalStartFrame(VisibilityState state) {
        return state.opacity() > 0.2
                && state.opacity() < 0.75
                && state.scaleX() > 0.84
                && state.scaleX() < 0.95
                && state.scaleY() > 0.84
                && state.scaleY() < 0.95;
    }

    /// Returns whether a reversed exit has advanced toward visibility without already reaching its endpoint.
    ///
    /// @param state the current state
    /// @param reversalStart the state captured immediately before the reversal
    /// @return `true` when opacity and both scale channels have advanced toward one
    private static boolean isReversalIntermediate(VisibilityState state, VisibilityState reversalStart) {
        return state.opacity() > reversalStart.opacity() + INTERMEDIATE_MARGIN
                && state.opacity() < 1.0 - INTERMEDIATE_MARGIN
                && state.scaleX() > reversalStart.scaleX() + INTERMEDIATE_MARGIN
                && state.scaleX() < 1.0 - INTERMEDIATE_MARGIN
                && state.scaleY() > reversalStart.scaleY() + INTERMEDIATE_MARGIN
                && state.scaleY() < 1.0 - INTERMEDIATE_MARGIN;
    }

    /// Returns whether a value lies visibly between a hidden endpoint and the neutral visible value.
    ///
    /// @param value the value to inspect
    /// @param hiddenEndpoint the hidden endpoint below one
    /// @return `true` when the value is separated from both endpoints by the test margin
    private static boolean isBetweenHiddenAndVisible(double value, double hiddenEndpoint) {
        return value > hiddenEndpoint + INTERMEDIATE_MARGIN
                && value < 1.0 - INTERMEDIATE_MARGIN;
    }

    /// Returns whether all visibility channels are at their neutral visible values.
    ///
    /// @param state the state to inspect
    /// @return `true` when opacity and both scale channels are within tolerance of one
    private static boolean isNeutralTransform(VisibilityState state) {
        return Math.abs(state.opacity() - 1.0) <= 0.001
                && Math.abs(state.scaleX() - 1.0) <= 0.001
                && Math.abs(state.scaleY() - 1.0) <= 0.001;
    }

    /// Verifies that all visibility channels are at their neutral visible values.
    ///
    /// @param state the state to verify
    private static void assertNeutralTransform(VisibilityState state) {
        assertEquals(1.0, state.opacity(), 0.001, "opacity");
        assertEquals(1.0, state.scaleX(), 0.001, "scaleX");
        assertEquals(1.0, state.scaleY(), 0.001, "scaleY");
    }

    /// Returns a motion scheme whose fast effects role is long enough for deterministic frame observation.
    ///
    /// @return the test-local motion scheme
    private static M3MotionScheme observableMotionScheme() {
        M3MotionSpec observable = M3MotionSpec.of(OBSERVABLE_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.builder(M3MotionScheme.standard())
                .fastEffects(observable)
                .build();
    }

    /// Closes a fixture window and removes its test-local motion settings.
    ///
    /// @param fixture the fixture to dispose, or `null` when setup did not complete
    private static void disposeFixture(@Nullable TooltipFixture fixture) {
        if (fixture == null) {
            return;
        }
        FxTestUtils.runOnFxThread(() -> {
            fixture.stage().close();
            M3MotionSettings.setReducedMotionRequested(fixture.root(), false);
            FxTestUtils.clearMotionScheme(fixture.root());
        });
    }

    /// Holds one real-window tooltip motion fixture.
    ///
    /// @param stage the presenting stage
    /// @param root the themed scene root
    /// @param owner the tooltip owner node
    /// @param tooltip the tested tooltip
    /// @param anchorX the tooltip screen anchor x coordinate
    /// @param anchorY the tooltip screen anchor y coordinate
    /// @param hiddenTransitions the number of observed transitions to a non-showing popup
    private record TooltipFixture(
            Stage stage,
            StackPane root,
            M3Button owner,
            M3Tooltip tooltip,
            double anchorX,
            double anchorY,
            AtomicInteger hiddenTransitions
    ) {
    }

    /// Captures the popup skin node channels used by visibility motion.
    ///
    /// @param opacity the popup skin opacity
    /// @param scaleX the popup skin horizontal scale
    /// @param scaleY the popup skin vertical scale
    private record VisibilityState(double opacity, double scaleX, double scaleY) {
    }
}
