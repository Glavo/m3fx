// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Provides shared JavaFX toolkit helpers for tests.
@NotNullByDefault
public final class FxTestUtils {
    /// The maximum time a test waits for JavaFX toolkit work to complete.
    public static final long FX_TIMEOUT_SECONDS = 10L;

    /// The number of pulses kept under CSS warning capture after pulse-based tests close windows or popups.
    private static final int POST_PULSE_TEST_CSS_DRAIN_PULSES = 8;

    /// Prevents instantiation of this utility class.
    private FxTestUtils() {
    }

    /// Starts the JavaFX toolkit when it is not already running.
    public static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        await(latch);
    }

    /// Runs a task on the FX application thread and propagates failures.
    public static void runOnFxThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
            return;
        }

        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
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
        awaitUnchecked(latch);
        throwIfFailed(failure.get());
    }

    /// Runs a task with global Material animations disabled and restores previous global motion settings.
    public static void runWithAnimationsDisabled(Runnable task) {
        runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setAnimationsEnabled(false);
            task.run();
        });
    }

    /// Runs a task with global Material motion settings restored afterward.
    public static void runWithMotionSettingsPreserved(Runnable task) {
        boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
        M3MotionScheme previousScheme = M3MotionSettings.getMotionScheme();
        M3MotionBehavior previousBehavior = M3MotionSettings.getMotionBehavior();
        try {
            task.run();
        } finally {
            M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);
            M3MotionSettings.setMotionScheme(previousScheme);
            M3MotionSettings.setMotionBehavior(previousBehavior);
        }
    }

    /// Runs a task with global Material motion settings restored afterward.
    private static void runWithMotionSettingsPreservedChecked(CheckedRunnable task) throws InterruptedException {
        boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
        M3MotionScheme previousScheme = M3MotionSettings.getMotionScheme();
        M3MotionBehavior previousBehavior = M3MotionSettings.getMotionBehavior();
        try {
            task.run();
        } finally {
            M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);
            M3MotionSettings.setMotionScheme(previousScheme);
            M3MotionSettings.setMotionBehavior(previousBehavior);
        }
    }

    /// Runs a task on the FX application thread with global Material animations disabled.
    public static void runOnFxThreadWithAnimationsDisabled(Runnable task) {
        runOnFxThread(() -> runWithAnimationsDisabled(task));
    }

    /// Runs a task on the FX application thread with global Material motion settings restored afterward.
    public static void runOnFxThreadWithMotionSettingsPreserved(Runnable task) {
        runOnFxThread(() -> runWithMotionSettingsPreserved(task));
    }

    /// Captures JavaFX CSS warnings emitted while running a task.
    public static @Unmodifiable List<LogRecord> captureCssWarnings(Runnable task) {
        return captureWarnings(task, "javafx.css", "javafx.scene.CssStyleHelper");
    }

    /// Verifies that a task does not emit JavaFX CSS warnings.
    public static void assertNoCssWarnings(Runnable task) {
        List<LogRecord> warnings = captureCssWarnings(task);
        assertTrue(warnings.isEmpty(), () -> formatLogRecords(warnings));
    }

    /// Verifies that a task does not emit matching JavaFX CSS warnings.
    public static void assertNoCssWarningsMatching(Runnable task, Predicate<LogRecord> predicate) {
        List<LogRecord> warnings = captureCssWarnings(task);
        assertTrue(warnings.stream().noneMatch(predicate), () -> formatLogRecords(warnings));
    }

    /// Verifies that a task does not emit M3FX token-related JavaFX CSS warnings.
    public static void assertNoM3CssTokenWarnings(Runnable task) {
        assertNoCssWarningsMatching(task, FxTestUtils::isM3CssTokenWarning);
    }

    /// Returns whether a JavaFX CSS warning indicates unresolved M3FX color tokens.
    public static boolean isM3CssTokenWarning(LogRecord record) {
        String message = record.getMessage();
        return message != null
                && (message.contains("-m3-color-")
                || message.contains("ClassCastException") && message.contains("-fx-background-color"));
    }

    /// Captures warning-level records from the requested loggers while running a task.
    private static @Unmodifiable List<LogRecord> captureWarnings(Runnable task, String... loggerNames) {
        return captureWarningsChecked(() -> task.run(), loggerNames);
    }

    /// Captures warning-level records from the requested loggers while running a task.
    private static @Unmodifiable List<LogRecord> captureWarningsChecked(CheckedRunnable task, String... loggerNames) {
        List<LogRecord> warnings = Collections.synchronizedList(new ArrayList<>());
        CapturingLogHandler handler = new CapturingLogHandler(warnings);
        List<Logger> loggers = new ArrayList<>(loggerNames.length);
        for (String loggerName : loggerNames) {
            Logger logger = Logger.getLogger(loggerName);
            logger.addHandler(handler);
            loggers.add(logger);
        }
        try {
            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        } finally {
            for (Logger logger : loggers) {
                logger.removeHandler(handler);
            }
        }
        synchronized (warnings) {
            return List.copyOf(warnings);
        }
    }

    /// Formats log records for assertion diagnostics.
    private static String formatLogRecords(List<LogRecord> records) {
        return records.stream()
                .map(record -> record.getLevel() + ": " + record.getMessage())
                .collect(Collectors.joining("\n"));
    }

    /// Runs a supplier on the FX application thread and returns its result.
    public static <T> T callOnFxThread(Supplier<T> supplier) {
        if (Platform.isFxApplicationThread()) {
            return supplier.get();
        }

        AtomicReference<@Nullable T> result = new AtomicReference<>();
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(supplier.get());
            } catch (Throwable e) {
                failure.set(e);
            } finally {
                latch.countDown();
            }
        });
        awaitUnchecked(latch);
        throwIfFailed(failure.get());
        return result.get();
    }

    /// Runs setup on the FX application thread and verifies the result after a JavaFX delay.
    public static void runOnFxThreadAfterDelay(
            Duration delay,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        List<LogRecord> warnings = captureWarningsChecked(
                () -> runWithMotionSettingsPreservedChecked(
                        () -> runOnFxThreadAfterDelayWithoutCssCapture(delay, setup, verification)
                ),
                "javafx.css",
                "javafx.scene.CssStyleHelper"
        );
        assertTrue(warnings.stream().noneMatch(FxTestUtils::isM3CssTokenWarning), () -> formatLogRecords(warnings));
    }

    /// Runs setup on the FX application thread and verifies the result after JavaFX pulses.
    public static void runOnFxThreadAfterPulses(
            int pulseCount,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        List<LogRecord> warnings = captureWarningsChecked(
                () -> runWithMotionSettingsPreservedChecked(
                        () -> {
                            runOnFxThreadAfterPulsesWithoutCssCapture(pulseCount, setup, verification);
                            waitForPulses(POST_PULSE_TEST_CSS_DRAIN_PULSES);
                        }
                ),
                "javafx.css",
                "javafx.scene.CssStyleHelper"
        );
        assertTrue(warnings.stream().noneMatch(FxTestUtils::isM3CssTokenWarning), () -> formatLogRecords(warnings));
    }

    /// Runs setup on the FX application thread and verifies the result when a condition becomes true.
    public static void runOnFxThreadWhen(
            BooleanSupplier condition,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        List<LogRecord> warnings = captureWarningsChecked(
                () -> runWithMotionSettingsPreservedChecked(
                        () -> runOnFxThreadWhenWithoutCssCapture(condition, setup, verification)
                ),
                "javafx.css",
                "javafx.scene.CssStyleHelper"
        );
        assertTrue(warnings.stream().noneMatch(FxTestUtils::isM3CssTokenWarning), () -> formatLogRecords(warnings));
    }

    /// Runs setup on the FX application thread and verifies the result after a JavaFX delay.
    private static void runOnFxThreadAfterDelayWithoutCssCapture(
            Duration delay,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                setup.run();
                PauseTransition pause = new PauseTransition(delay);
                pause.setOnFinished(event -> {
                    try {
                        verification.run();
                    } catch (Throwable e) {
                        failure.set(e);
                    } finally {
                        latch.countDown();
                    }
                });
                pause.play();
            } catch (Throwable e) {
                failure.set(e);
                latch.countDown();
            }
        });

        await(latch);
        throwIfFailed(failure.get());
    }

    /// Runs setup on the FX application thread and verifies the result after JavaFX pulses.
    private static void runOnFxThreadAfterPulsesWithoutCssCapture(
            int pulseCount,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        if (pulseCount < 1) {
            throw new IllegalArgumentException("pulseCount must be positive");
        }

        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                setup.run();
                AnimationTimer timer = new AnimationTimer() {
                    /// The number of pulses observed after setup.
                    private int pulses;

                    /// Counts pulses and runs verification after the requested pulse.
                    @Override
                    public void handle(long now) {
                        pulses++;
                        if (pulses < pulseCount) {
                            return;
                        }

                        stop();
                        try {
                            verification.run();
                        } catch (Throwable e) {
                            failure.set(e);
                        } finally {
                            latch.countDown();
                        }
                    }
                };
                timer.start();
            } catch (Throwable e) {
                failure.set(e);
                latch.countDown();
            }
        });

        await(latch);
        throwIfFailed(failure.get());
    }

    /// Runs setup on the FX application thread and verifies the result when a condition becomes true.
    private static void runOnFxThreadWhenWithoutCssCapture(
            BooleanSupplier condition,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                setup.run();
                long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(FX_TIMEOUT_SECONDS);
                AnimationTimer timer = new AnimationTimer() {
                    /// Checks the readiness condition on each JavaFX pulse.
                    @Override
                    public void handle(long now) {
                        try {
                            if (condition.getAsBoolean()) {
                                stop();
                                verification.run();
                                latch.countDown();
                            } else if (now >= deadlineNanos) {
                                stop();
                                failure.set(new AssertionError("Timed out waiting for JavaFX condition"));
                                latch.countDown();
                            }
                        } catch (Throwable e) {
                            stop();
                            failure.set(e);
                            latch.countDown();
                        }
                    }
                };

                if (condition.getAsBoolean()) {
                    verification.run();
                    latch.countDown();
                } else {
                    timer.start();
                }
            } catch (Throwable e) {
                failure.set(e);
                latch.countDown();
            }
        });

        await(latch);
        throwIfFailed(failure.get());
    }

    /// Waits for deferred JavaFX pulse work before continuing on the test thread.
    private static void waitForPulses(int pulseCount) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> runAfterPulses(pulseCount, latch::countDown));
        await(latch);
    }

    /// Runs an action after the requested number of JavaFX pulses on the FX application thread.
    private static void runAfterPulses(int pulseCount, Runnable action) {
        if (pulseCount < 1) {
            throw new IllegalArgumentException("pulseCount must be positive");
        }

        AnimationTimer timer = new AnimationTimer() {
            /// The number of pulses observed before running the action.
            private int pulses;

            /// Counts pulses and runs the action after the requested pulse.
            @Override
            public void handle(long now) {
                pulses++;
                if (pulses < pulseCount) {
                    return;
                }

                stop();
                action.run();
            }
        };
        timer.start();
    }

    /// Waits for a latch using the shared JavaFX test timeout.
    public static void await(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    /// Waits for a latch using the shared JavaFX test timeout and wraps interruptions.
    public static void awaitUnchecked(CountDownLatch latch) {
        try {
            await(latch);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    /// Re-throws a captured failure from the FX application thread.
    public static void throwIfFailed(@Nullable Throwable exception) {
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

    /// Represents a test action that may be interrupted while waiting for JavaFX work.
    @FunctionalInterface
    private interface CheckedRunnable {
        /// Runs this action.
        void run() throws InterruptedException;
    }

    /// Captures warning-level log records into an in-memory list.
    private static final class CapturingLogHandler extends Handler {
        /// The mutable destination for captured records.
        private final List<LogRecord> records;

        /// Creates a handler that appends records to the supplied list.
        private CapturingLogHandler(List<LogRecord> records) {
            this.records = records;
        }

        /// Captures records with warning severity or higher.
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                records.add(record);
            }
        }

        /// Flushes this in-memory handler.
        @Override
        public void flush() {
        }

        /// Closes this in-memory handler.
        @Override
        public void close() {
        }
    }
}
