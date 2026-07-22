// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.testing.Tier3Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the observable floating-label motion contract through natural JavaFX pulses in a real window.
///
/// The test deliberately treats the label transform and the skin that produces it as implementation details. It
/// observes only computed typography, rendered scale, opacity, and scene-space bounds after the scene has received
/// its one initial CSS and layout pass.
@NotNullByDefault
@Tier3Test
final class M3TextInputMotionTest {
    /// The native rendered scale of minimized floating-label typography.
    private static final double MINIMIZED_SCALE = 1.0;

    /// The expected computed size of the minimized label font, in pixels.
    private static final double MINIMIZED_FONT_SIZE = 12.0;

    /// The tolerance used when comparing a rendered scale with an endpoint.
    private static final double SCALE_EPSILON = 2.0e-3;

    /// The tolerance used to detect a synchronous presentation discontinuity.
    private static final double RETARGET_EPSILON = 1.0e-6;

    /// The tolerance used when comparing scene-space label bounds.
    private static final double BOUNDS_EPSILON = 2.0e-3;

    /// The smallest scale change retained as visible motion while determining settlement.
    private static final double VISIBLE_SCALE_DELTA = 1.0e-5;

    /// The distance from an endpoint treated as an exact-looking plateau.
    private static final double PLATEAU_EPSILON = 4.0e-4;

    /// The departure from an endpoint considered a visible return after a plateau.
    private static final double VISIBLE_RETURN_EPSILON = 4.0e-3;

    /// The minimum fraction of endpoint travel required before an in-flight transition is reversed.
    private static final double INTERRUPTION_PROGRESS = 0.15;

    /// The minimum duration for which a complete transition is observed.
    private static final long MINIMUM_OBSERVATION_NANOS = TimeUnit.MILLISECONDS.toNanos(700L);

    /// The quiet interval required after the last visible change before a transition is settled.
    private static final long SETTLEMENT_QUIET_NANOS = TimeUnit.MILLISECONDS.toNanos(180L);

    /// The duration for which settled scene-space bounds must remain unchanged.
    private static final long POST_SETTLEMENT_STABILITY_NANOS = TimeUnit.MILLISECONDS.toNanos(1_050L);

    /// The minimum endpoint plateau duration that becomes visibly suspicious before a return.
    private static final long VISIBLE_PLATEAU_NANOS = TimeUnit.MILLISECONDS.toNanos(32L);

    /// The constrained width that leaves no valid horizontal label geometry.
    private static final double NARROW_LAYOUT_WIDTH = 24.0;

    /// The global reduced-motion setting restored after the current test.
    private boolean previousGlobalReducedMotionRequested;

    /// Starts the JavaFX toolkit before creating real windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Enables motion explicitly while preserving the application-wide setting.
    @BeforeEach
    void enableMotion() {
        previousGlobalReducedMotionRequested = M3MotionSettings.isGlobalReducedMotionRequested();
        M3MotionSettings.setGlobalReducedMotionRequested(false);
    }

    /// Closes windows left open by a failed motion assertion and restores the global motion preference.
    @AfterEach
    void closeWindows() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
            M3MotionSettings.setGlobalReducedMotionRequested(previousGlobalReducedMotionRequested);
        });
    }

    /// Verifies forward motion, complete settlement, interruption, and long-term stability for both motion profiles.
    @Test
    void floatingLabelUsesContinuousPulseDrivenMotionWithoutEndpointReentry() throws InterruptedException {
        assertFloatingLabelMotion(M3Profile.BASELINE_2021);
        assertFloatingLabelMotion(M3Profile.EXPRESSIVE_2025);
    }

    /// Verifies that invalid narrow geometry is cleared and subsequently rebuilt by normal layout pulses.
    @Test
    void floatingLabelRecoversAfterNarrowLayoutInvalidatesItsGeometry() throws InterruptedException {
        MotionScene scene = createMotionScene(M3Profile.BASELINE_2021);
        double expandedScale = expandedScale(scene);

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> scene.layout.getWidth() <= NARROW_LAYOUT_WIDTH + BOUNDS_EPSILON
                            && (!scene.label.isVisible()
                            || sceneBounds(scene.label).getWidth() <= BOUNDS_EPSILON),
                    () -> "narrow text-input layout retained stale floating-label geometry",
                    () -> {
                        scene.layout.setMinWidth(NARROW_LAYOUT_WIDTH);
                        scene.layout.setPrefWidth(NARROW_LAYOUT_WIDTH);
                        scene.layout.setMaxWidth(NARROW_LAYOUT_WIDTH);
                    },
                    () -> assertTrue(!scene.label.isVisible()
                                    || sceneBounds(scene.label).getWidth() <= BOUNDS_EPSILON,
                            "invalid geometry remained visible")
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> scene.layout.getWidth() > NARROW_LAYOUT_WIDTH
                            && sceneBounds(scene.label).getWidth() > BOUNDS_EPSILON
                            && isAtScale(renderedScale(scene.label), expandedScale),
                    () -> "floating-label geometry did not recover after width was restored",
                    () -> {
                        scene.layout.setMinWidth(Region.USE_COMPUTED_SIZE);
                        scene.layout.setPrefWidth(Region.USE_COMPUTED_SIZE);
                        scene.layout.setMaxWidth(Double.MAX_VALUE);
                    },
                    () -> {
                        assertEquals(expandedScale, renderedScale(scene.label), SCALE_EPSILON);
                        assertTrue(sceneBounds(scene.label).getWidth() > BOUNDS_EPSILON);
                    }
            );

            MotionRun floating = observeTransition(
                    scene,
                    MINIMIZED_SCALE,
                    () -> scene.field.setText("M3FX"),
                    "restored floating label did not settle"
            );
            assertNoSynchronousJump(floating, "restored floating-label transition");
            assertEquals(MINIMIZED_SCALE, floating.endpoint().scale(), SCALE_EPSILON);
        } finally {
            scene.close();
        }
    }

    /// Verifies both rendered endpoints and round-trip geometry in RTL orientation.
    @Test
    void floatingLabelEndpointsRemainStableInRightToLeftOrientation() throws InterruptedException {
        MotionScene scene = createMotionScene(M3Profile.BASELINE_2021);
        double expandedScale = expandedScale(scene);

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> scene.layout.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT,
                    3,
                    () -> "text-input layout did not settle after switching to RTL orientation",
                    () -> scene.root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT),
                    () -> assertEquals(expandedScale, renderedScale(scene.label), SCALE_EPSILON)
            );
            Bounds restingBounds = FxTestUtils.callOnFxThread(() -> sceneBounds(scene.label));

            MotionRun floating = observeTransition(
                    scene,
                    MINIMIZED_SCALE,
                    () -> scene.field.setText("M3FX"),
                    "RTL floating-label endpoint did not settle"
            );
            assertEquals(MINIMIZED_SCALE, floating.endpoint().scale(), SCALE_EPSILON);
            assertTrue(floating.endpoint().bounds().getWidth() > BOUNDS_EPSILON);

            MotionRun resting = observeTransition(
                    scene,
                    expandedScale,
                    scene.field::clear,
                    "RTL resting floating-label endpoint did not settle"
            );
            assertEquals(expandedScale, resting.endpoint().scale(), SCALE_EPSILON);
            assertTrue(equalBounds(restingBounds, resting.endpoint().bounds()),
                    () -> "RTL resting endpoint changed after a complete round trip: before="
                            + restingBounds + ", after=" + resting.endpoint().bounds());
        } finally {
            scene.close();
        }
    }

    /// Verifies that reduced motion applies dynamically computed endpoints synchronously for both profiles.
    @Test
    void floatingLabelSnapsToComputedEndpointsWhenAnimationsAreDisabled() {
        assertReducedMotionEndpoints(M3Profile.BASELINE_2021);
        assertReducedMotionEndpoints(M3Profile.EXPRESSIVE_2025);
    }

    /// Exercises one profile and verifies the complete observable floating-label motion lifecycle.
    ///
    /// @param profile the Material profile under test
    /// @throws InterruptedException if the test thread is interrupted while awaiting JavaFX pulses
    private static void assertFloatingLabelMotion(M3Profile profile) throws InterruptedException {
        MotionScene scene = createMotionScene(profile);
        double expandedScale = expandedScale(scene);

        try {
            MotionFrame resting = FxTestUtils.callOnFxThread(() -> captureFrame(scene.label));
            assertEquals(MINIMIZED_FONT_SIZE,
                    FxTestUtils.callOnFxThread(() -> scene.label.getFont().getSize()),
                    SCALE_EPSILON,
                    () -> profile + " label did not use native minimized typography");
            assertEquals(expandedScale, resting.scale(), SCALE_EPSILON);
            assertEquals(1.0, resting.opacity(), 0.0);

            MotionRun forward = observeTransition(
                    scene,
                    MINIMIZED_SCALE,
                    () -> scene.field.setText("M3FX"),
                    profile + " forward floating-label motion did not settle"
            );
            assertNoSynchronousJump(forward, profile + " forward transition");
            assertIntermediateFrames(forward.pulseFrames(), expandedScale, MINIMIZED_SCALE, profile);
            assertNoPlateauThenVisibleDeparture(forward.pulseFrames(), MINIMIZED_SCALE, profile);
            assertEquals(MINIMIZED_SCALE, forward.endpoint().scale(), SCALE_EPSILON);

            InterruptedMotion interrupted = observeInterruptedReverse(scene, expandedScale, profile);
            assertNoSynchronousJump(
                    interrupted.reverseStart(),
                    interrupted.reverseStartAfter(),
                    profile + " reverse start"
            );
            assertIntermediateFrames(
                    interrupted.reverseFrames(),
                    MINIMIZED_SCALE,
                    expandedScale,
                    profile
            );
            assertNoSynchronousJump(
                    interrupted.beforeInterruption(),
                    interrupted.afterInterruption(),
                    profile + " interrupted reversal"
            );
            assertNoPlateauThenVisibleDeparture(
                    interrupted.returnRun().pulseFrames(),
                    MINIMIZED_SCALE,
                    profile
            );
            assertEquals(MINIMIZED_SCALE, interrupted.returnRun().endpoint().scale(), SCALE_EPSILON);

            MotionRun reverse = observeTransition(
                    scene,
                    expandedScale,
                    scene.field::clear,
                    profile + " reverse floating-label motion did not settle"
            );
            assertNoSynchronousJump(reverse, profile + " reverse transition");
            assertIntermediateFrames(reverse.pulseFrames(), MINIMIZED_SCALE, expandedScale, profile);
            assertNoPlateauThenVisibleDeparture(reverse.pulseFrames(), expandedScale, profile);
            assertEquals(expandedScale, reverse.endpoint().scale(), SCALE_EPSILON);
            assertStableSceneBounds(scene, expandedScale, profile);
        } finally {
            scene.close();
        }
    }

    /// Exercises one profile with global animations disabled and verifies immediate endpoint application.
    ///
    /// @param profile the Material profile under test
    private static void assertReducedMotionEndpoints(M3Profile profile) {
        MotionScene scene = createMotionScene(profile);
        try {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                scene.field.setStyle("-fx-font-size: 20px;");
                scene.root.applyCss();
                scene.root.layout();
                double expandedScale = expandedScaleNow(scene);
                assertEquals(MINIMIZED_FONT_SIZE, scene.label.getFont().getSize(), SCALE_EPSILON);
                assertEquals(20.0 / MINIMIZED_FONT_SIZE, expandedScale, SCALE_EPSILON);
                assertEquals(expandedScale, renderedScale(scene.label), SCALE_EPSILON);

                scene.field.setText("M3FX");
                assertEquals(MINIMIZED_SCALE, renderedScale(scene.label), SCALE_EPSILON);
                assertEquals(1.0, scene.label.getOpacity(), 0.0);

                scene.field.clear();
                assertEquals(expandedScale, renderedScale(scene.label), SCALE_EPSILON);
                assertEquals(1.0, scene.label.getOpacity(), 0.0);
            });
        } finally {
            scene.close();
        }
    }

    /// Observes a transition from its synchronous retarget through a complete natural-pulse settlement.
    ///
    /// @param scene the real-window scene under test
    /// @param targetScale the expected rendered endpoint scale
    /// @param retarget the state change that starts or reverses the transition
    /// @param failureMessage the timeout diagnostic
    /// @return the synchronous and pulse-driven observations
    /// @throws InterruptedException if the test thread is interrupted while awaiting JavaFX pulses
    private static MotionRun observeTransition(
            MotionScene scene,
            double targetScale,
            Runnable retarget,
            String failureMessage
    ) throws InterruptedException {
        TransitionState state = new TransitionState();

        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    long now = System.nanoTime();
                    MotionFrame frame = captureFrame(scene.label, now);
                    state.pulseFrames.add(frame);
                    if (state.firstPulseNanos < 0L) {
                        state.firstPulseNanos = now;
                        state.lastVisibleChangeNanos = now;
                    }
                    if (state.previousFrame != null
                            && visiblyDifferent(state.previousFrame, frame)) {
                        state.lastVisibleChangeNanos = now;
                    }
                    state.previousFrame = frame;
                    return isAtScale(frame.scale(), targetScale)
                            && now - state.firstPulseNanos >= MINIMUM_OBSERVATION_NANOS
                            && now - state.lastVisibleChangeNanos >= SETTLEMENT_QUIET_NANOS;
                },
                1,
                () -> failureMessage,
                () -> {
                    state.beforeRetarget = captureFrame(scene.label);
                    retarget.run();
                    state.afterRetarget = captureFrame(scene.label);
                },
                () -> assertEquals(targetScale, renderedScale(scene.label), SCALE_EPSILON)
        );

        return new MotionRun(
                Objects.requireNonNull(state.beforeRetarget, "before retarget frame"),
                Objects.requireNonNull(state.afterRetarget, "after retarget frame"),
                List.copyOf(state.pulseFrames)
        );
    }

    /// Starts motion toward the resting endpoint, reverses it in flight, and observes the return settlement.
    ///
    /// @param scene the real-window scene under test
    /// @param expandedScale the dynamically computed resting endpoint
    /// @param profile the Material profile used for diagnostics
    /// @return observations from both sides of the interrupted transition
    /// @throws InterruptedException if the test thread is interrupted while awaiting JavaFX pulses
    private static InterruptedMotion observeInterruptedReverse(
            MotionScene scene,
            double expandedScale,
            M3Profile profile
    ) throws InterruptedException {
        InterruptionState state = new InterruptionState();

        FxTestUtils.runOnFxThreadWhen(
                () -> {
                    MotionFrame frame = captureFrame(scene.label);
                    state.reverseFrames.add(frame);
                    double progress = expandedTravelFraction(frame.scale(), expandedScale);
                    return progress >= INTERRUPTION_PROGRESS
                            && isStrictlyBetween(frame.scale(), MINIMIZED_SCALE, expandedScale);
                },
                () -> profile + " reverse motion did not expose an interruptible intermediate frame",
                () -> {
                    state.reverseStart = captureFrame(scene.label);
                    scene.field.clear();
                    state.reverseStartAfter = captureFrame(scene.label);
                },
                () -> {
                    state.beforeInterruption = captureFrame(scene.label);
                    scene.field.setText("M3FX");
                    state.afterInterruption = captureFrame(scene.label);
                }
        );

        MotionRun returnRun = observeTransition(
                scene,
                MINIMIZED_SCALE,
                () -> {
                },
                profile + " interrupted floating-label motion did not return to the floating endpoint"
        );
        return new InterruptedMotion(
                Objects.requireNonNull(state.reverseStart, "reverse start frame"),
                Objects.requireNonNull(state.reverseStartAfter, "reverse start after frame"),
                List.copyOf(state.reverseFrames),
                Objects.requireNonNull(state.beforeInterruption, "before interruption frame"),
                Objects.requireNonNull(state.afterInterruption, "after interruption frame"),
                returnRun
        );
    }

    /// Verifies that a settled endpoint remains geometrically unchanged for approximately one second.
    ///
    /// @param scene the real-window scene under test
    /// @param targetScale the expected settled scale
    /// @param profile the Material profile used for diagnostics
    /// @throws InterruptedException if the test thread is interrupted while awaiting JavaFX pulses
    private static void assertStableSceneBounds(
            MotionScene scene,
            double targetScale,
            M3Profile profile
    ) throws InterruptedException {
        StabilityState state = new StabilityState();
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    long now = System.nanoTime();
                    MotionFrame current = captureFrame(scene.label, now);
                    if (state.reference == null) {
                        state.reference = current;
                        state.startNanos = now;
                    }
                    assertEquals(targetScale, current.scale(), SCALE_EPSILON,
                            () -> profile + " settled scale changed during the stability window");
                    MotionFrame reference = Objects.requireNonNull(state.reference, "stability reference");
                    assertTrue(equalBounds(reference.bounds(), current.bounds()),
                            () -> profile + " floating-label scene bounds moved after settlement: reference="
                                    + reference.bounds() + ", current=" + current.bounds());
                    return now - state.startNanos >= POST_SETTLEMENT_STABILITY_NANOS;
                },
                1,
                () -> profile + " floating-label stability window did not complete",
                () -> {
                },
                () -> assertNotNull(state.reference, "stability window captured no pulse")
        );
    }

    /// Verifies that a retarget did not synchronously alter rendered presentation.
    ///
    /// @param run the observed transition
    /// @param description the assertion diagnostic prefix
    private static void assertNoSynchronousJump(MotionRun run, String description) {
        assertNoSynchronousJump(run.beforeRetarget(), run.afterRetarget(), description);
    }

    /// Verifies that two frames around one synchronous retarget have identical presentation.
    ///
    /// @param before the frame immediately before retargeting
    /// @param after the frame immediately after retargeting in the same JavaFX callback
    /// @param description the assertion diagnostic prefix
    private static void assertNoSynchronousJump(MotionFrame before, MotionFrame after, String description) {
        assertEquals(before.scale(), after.scale(), RETARGET_EPSILON,
                () -> description + " changed scale synchronously");
        assertTrue(equalBounds(before.bounds(), after.bounds(), RETARGET_EPSILON),
                () -> description + " changed scene bounds synchronously: before="
                        + before.bounds() + ", after=" + after.bounds());
    }

    /// Verifies that a pulse trace contains finite, positive, non-endpoint rendered frames.
    ///
    /// @param frames the natural-pulse observations
    /// @param startScale the transition's starting endpoint
    /// @param targetScale the transition's target endpoint
    /// @param profile the Material profile used for diagnostics
    private static void assertIntermediateFrames(
            @Unmodifiable List<MotionFrame> frames,
            double startScale,
            double targetScale,
            M3Profile profile
    ) {
        assertTrue(frames.stream().allMatch(frame -> Double.isFinite(frame.scale()) && frame.scale() > 0.0),
                () -> profile + " produced a non-finite or non-positive label scale");
        assertTrue(frames.stream().anyMatch(frame -> isStrictlyBetween(frame.scale(), startScale, targetScale)),
                () -> profile + " floating label skipped every intermediate rendered scale: "
                        + frames.stream().map(MotionFrame::scale).toList());
        assertTrue(frames.stream().allMatch(frame -> frame.opacity() == 1.0),
                () -> profile + " changed floating-label opacity during scale motion");
    }

    /// Rejects a clamped endpoint plateau followed by a visible return into the transition range.
    ///
    /// Expressive motion may cross an endpoint slightly. A continuous crossing is accepted; holding at the exact
    /// endpoint for multiple rendered pulses and then moving visibly away is not.
    ///
    /// @param frames the natural-pulse observations
    /// @param targetScale the target endpoint
    /// @param profile the Material profile used for diagnostics
    private static void assertNoPlateauThenVisibleDeparture(
            @Unmodifiable List<MotionFrame> frames,
            double targetScale,
            M3Profile profile
    ) {
        int plateauStart = -1;
        for (int index = 0; index < frames.size(); index++) {
            MotionFrame frame = frames.get(index);
            if (Math.abs(frame.scale() - targetScale) <= PLATEAU_EPSILON) {
                if (plateauStart < 0) {
                    plateauStart = index;
                }
                continue;
            }

            if (plateauStart >= 0) {
                MotionFrame plateauFirst = frames.get(plateauStart);
                MotionFrame plateauLast = frames.get(index - 1);
                long plateauDuration = plateauLast.timestampNanos() - plateauFirst.timestampNanos();
                boolean visiblyDeparted = Math.abs(frame.scale() - targetScale) >= VISIBLE_RETURN_EPSILON;
                assertTrue(plateauDuration < VISIBLE_PLATEAU_NANOS || !visiblyDeparted,
                        () -> profile + " held a clamped endpoint for "
                                + TimeUnit.NANOSECONDS.toMillis(plateauDuration)
                                + " ms before visibly returning to scale " + frame.scale());
                plateauStart = -1;
            }
        }
    }

    /// Returns the resting scale derived from the current computed input and minimized-label fonts.
    ///
    /// @param scene the motion scene whose computed fonts are queried
    /// @return the expanded endpoint scale
    private static double expandedScale(MotionScene scene) {
        return FxTestUtils.callOnFxThread(() -> expandedScaleNow(scene));
    }

    /// Returns the resting scale while already executing on the JavaFX application thread.
    ///
    /// @param scene the motion scene whose computed fonts are queried
    /// @return the expanded endpoint scale
    private static double expandedScaleNow(MotionScene scene) {
        return scene.field.getFont().getSize() / scene.label.getFont().getSize();
    }

    /// Returns the scale represented by the label's rendered local-to-parent transform.
    ///
    /// @param label the rendered floating-label node
    /// @return the uniform rendered scale
    private static double renderedScale(Label label) {
        var transform = label.getLocalToParentTransform();
        return Math.hypot(transform.getMxx(), transform.getMyx());
    }

    /// Captures one rendered label frame using the current monotonic time.
    ///
    /// @param label the rendered floating-label node
    /// @return the captured frame
    private static MotionFrame captureFrame(Label label) {
        return captureFrame(label, System.nanoTime());
    }

    /// Captures one rendered label frame for an AnimationTimer observation.
    ///
    /// @param label the rendered floating-label node
    /// @param timestampNanos the pulse observation time in nanoseconds
    /// @return the captured frame
    private static MotionFrame captureFrame(Label label, long timestampNanos) {
        return new MotionFrame(timestampNanos, renderedScale(label), label.getOpacity(), sceneBounds(label));
    }

    /// Returns whether two observations differ enough to reset the settlement quiet interval.
    ///
    /// @param first the earlier frame
    /// @param second the later frame
    /// @return `true` when scale or scene-space bounds changed visibly
    private static boolean visiblyDifferent(MotionFrame first, MotionFrame second) {
        return Math.abs(first.scale() - second.scale()) > VISIBLE_SCALE_DELTA
                || !equalBounds(first.bounds(), second.bounds());
    }

    /// Returns whether a scale has reached the requested endpoint.
    ///
    /// @param scale the rendered scale
    /// @param targetScale the expected endpoint
    /// @return `true` when the values differ by no more than [#SCALE_EPSILON]
    private static boolean isAtScale(double scale, double targetScale) {
        return Math.abs(scale - targetScale) <= SCALE_EPSILON;
    }

    /// Returns whether a scale lies strictly between two endpoints.
    ///
    /// @param scale the rendered scale
    /// @param firstEndpoint one endpoint
    /// @param secondEndpoint the other endpoint
    /// @return `true` when the scale excludes both endpoint tolerance regions
    private static boolean isStrictlyBetween(double scale, double firstEndpoint, double secondEndpoint) {
        double lower = Math.min(firstEndpoint, secondEndpoint) + SCALE_EPSILON;
        double upper = Math.max(firstEndpoint, secondEndpoint) - SCALE_EPSILON;
        return scale > lower && scale < upper;
    }

    /// Returns the normalized distance traveled between two scale endpoints.
    ///
    /// @param scale the current rendered scale
    /// @param expandedScale the expanded endpoint
    /// @return the signed endpoint-relative travel fraction
    private static double expandedTravelFraction(double scale, double expandedScale) {
        return (scale - MINIMIZED_SCALE) / (expandedScale - MINIMIZED_SCALE);
    }

    /// Returns the label bounds transformed into scene coordinates.
    ///
    /// @param label the rendered floating-label node
    /// @return the scene-space bounds
    private static Bounds sceneBounds(Label label) {
        return label.localToScene(label.getBoundsInLocal());
    }

    /// Returns whether two scene-space bounds agree within the standard stability tolerance.
    ///
    /// @param first one scene-space bounds value
    /// @param second the other scene-space bounds value
    /// @return `true` when all observable components agree
    private static boolean equalBounds(Bounds first, Bounds second) {
        return equalBounds(first, second, BOUNDS_EPSILON);
    }

    /// Returns whether two scene-space bounds agree within a supplied tolerance.
    ///
    /// @param first one scene-space bounds value
    /// @param second the other scene-space bounds value
    /// @param epsilon the maximum permitted component difference
    /// @return `true` when all observable components agree
    private static boolean equalBounds(Bounds first, Bounds second, double epsilon) {
        return Math.abs(first.getMinX() - second.getMinX()) <= epsilon
                && Math.abs(first.getMinY() - second.getMinY()) <= epsilon
                && Math.abs(first.getWidth() - second.getWidth()) <= epsilon
                && Math.abs(first.getHeight() - second.getHeight()) <= epsilon;
    }

    /// Creates a themed, shown outlined field and performs its only explicit CSS and layout pass.
    ///
    /// @param profile the Material profile installed on the scene
    /// @return the initialized real-window scene
    private static MotionScene createMotionScene(M3Profile profile) {
        MotionScene[] result = new MotionScene[1];
        FxTestUtils.runOnFxThread(() -> {
            M3Button initialFocus = new M3Button("Initial focus");
            M3TextField field = new M3TextField();
            field.setVariant(M3TextInputVariant.OUTLINED);
            M3TextInputLayout layout = new M3TextInputLayout(field);
            layout.setLabelText("Project name");
            VBox root = new VBox(16.0, initialFocus, layout);
            Scene fxScene = new Scene(root, 420.0, 180.0);
            Stage stage = new Stage();
            stage.setScene(fxScene);
            M3ThemeManager.install(fxScene, M3Theme.fromSeed(
                    Color.web("#6750A4"),
                    profile,
                    Brightness.LIGHT
            ));
            M3MotionSettings.setReducedMotionRequested(root, false);
            stage.show();
            initialFocus.requestFocus();
            root.applyCss();
            root.layout();

            Label label = assertInstanceOf(
                    Label.class,
                    layout.lookup("." + M3TextInputLayout.LABEL_STYLE_CLASS)
            );
            assertTrue(initialFocus.isFocused());
            assertEquals(MINIMIZED_FONT_SIZE, label.getFont().getSize(), SCALE_EPSILON);
            assertEquals(field.getFont().getSize() / label.getFont().getSize(),
                    renderedScale(label),
                    SCALE_EPSILON);
            assertEquals(1.0, label.getOpacity(), 0.0);
            result[0] = new MotionScene(stage, root, layout, field, label);
        });
        return Objects.requireNonNull(result[0], "motion scene");
    }

    /// Holds mutable observations while one ordinary transition is sampled.
    private static final class TransitionState {
        /// The frame captured immediately before the triggering state change.
        private @Nullable MotionFrame beforeRetarget;

        /// The frame captured immediately after the state change in the same callback.
        private @Nullable MotionFrame afterRetarget;

        /// The natural-pulse frames captured until complete settlement.
        private final List<MotionFrame> pulseFrames = new ArrayList<>();

        /// The preceding pulse frame used to detect visible changes.
        private @Nullable MotionFrame previousFrame;

        /// The first observed pulse time, or `-1` until sampling starts.
        private long firstPulseNanos = -1L;

        /// The most recent pulse time at which presentation changed visibly.
        private long lastVisibleChangeNanos;
    }

    /// Holds mutable observations while a reverse transition is interrupted.
    private static final class InterruptionState {
        /// The frame captured before reverse motion starts.
        private @Nullable MotionFrame reverseStart;

        /// The frame captured synchronously after reverse motion starts.
        private @Nullable MotionFrame reverseStartAfter;

        /// Natural-pulse frames observed before the interruption.
        private final List<MotionFrame> reverseFrames = new ArrayList<>();

        /// The in-flight frame immediately before reversal.
        private @Nullable MotionFrame beforeInterruption;

        /// The frame immediately after reversal in the same callback.
        private @Nullable MotionFrame afterInterruption;
    }

    /// Holds mutable observations for the post-settlement stability window.
    private static final class StabilityState {
        /// The first frame used as the stable geometry reference.
        private @Nullable MotionFrame reference;

        /// The start of the natural-pulse stability window.
        private long startNanos;
    }

    /// Describes one observable rendered label frame.
    ///
    /// @param timestampNanos the monotonic observation time in nanoseconds
    /// @param scale the rendered uniform scale
    /// @param opacity the rendered label opacity
    /// @param bounds the label bounds in scene coordinates
    private record MotionFrame(long timestampNanos, double scale, double opacity, Bounds bounds) {
    }

    /// Describes one state change followed through complete settlement.
    ///
    /// @param beforeRetarget the frame before changing semantic state
    /// @param afterRetarget the frame immediately after changing state in the same callback
    /// @param pulseFrames the natural-pulse observations through settlement
    private record MotionRun(
            MotionFrame beforeRetarget,
            MotionFrame afterRetarget,
            @Unmodifiable List<MotionFrame> pulseFrames
    ) {
        /// Returns the final settled pulse frame.
        ///
        /// @return the final frame
        private MotionFrame endpoint() {
            return pulseFrames.get(pulseFrames.size() - 1);
        }
    }

    /// Describes reverse motion that is retargeted before reaching its endpoint.
    ///
    /// @param reverseStart the frame before reverse motion starts
    /// @param reverseStartAfter the frame immediately after reverse motion starts
    /// @param reverseFrames the natural-pulse frames before interruption
    /// @param beforeInterruption the in-flight frame immediately before retargeting
    /// @param afterInterruption the frame immediately after retargeting in the same callback
    /// @param returnRun the observations through settlement at the original endpoint
    private record InterruptedMotion(
            MotionFrame reverseStart,
            MotionFrame reverseStartAfter,
            @Unmodifiable List<MotionFrame> reverseFrames,
            MotionFrame beforeInterruption,
            MotionFrame afterInterruption,
            MotionRun returnRun
    ) {
    }

    /// Holds the nodes used by one real-window floating-label motion run.
    ///
    /// @param stage the window presenting the field
    /// @param root the scene root
    /// @param layout the Material text-input layout
    /// @param field the wrapped text field
    /// @param label the rendered floating-label node
    private record MotionScene(
            Stage stage,
            VBox root,
            M3TextInputLayout layout,
            M3TextField field,
            Label label
    ) {
        /// Closes the window and clears its local motion override.
        private void close() {
            FxTestUtils.runOnFxThread(() -> {
                M3MotionSettings.setReducedMotionRequested(root, false);
                stage.close();
            });
        }
    }
}
