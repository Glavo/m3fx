// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
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

/// Provides shared JavaFX toolkit helpers for demo tests.
@NotNullByDefault
final class DemoFxTestUtils {
    /// The maximum time a demo test waits for JavaFX toolkit work to complete.
    static final long FX_TIMEOUT_SECONDS = 10L;

    /// The default timeout in nanoseconds for pulse-driven JavaFX condition waits.
    private static final long FX_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(FX_TIMEOUT_SECONDS);

    /// The number of pulses kept under CSS warning capture after pulse-based tests close windows or popups.
    private static final int POST_PULSE_TEST_CSS_DRAIN_PULSES = 8;

    /// Prevents instantiation of this utility class.
    private DemoFxTestUtils() {
    }

    /// Starts the JavaFX toolkit when it is not already running.
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        await(latch);
        Platform.setImplicitExit(false);
    }

    /// Runs a task on the FX application thread and propagates failures.
    static void runOnFxThread(Runnable task) {
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
    static void runWithAnimationsDisabled(CheckedRunnable task) throws InterruptedException {
        runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setAnimationsEnabled(false);
            task.run();
        });
    }

    /// Runs a task with global Material motion settings restored afterward.
    static void runWithMotionSettingsPreserved(CheckedRunnable task) throws InterruptedException {
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
    static void runOnFxThreadWithAnimationsDisabled(Runnable task) {
        runOnFxThread(() -> {
            try {
                runWithAnimationsDisabled(task::run);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        });
    }

    /// Captures JavaFX CSS warnings emitted while running a task.
    static @Unmodifiable List<LogRecord> captureCssWarnings(CheckedRunnable task) throws InterruptedException {
        return captureWarnings(task, "javafx.css", "javafx.scene.CssStyleHelper");
    }

    /// Verifies that a task does not emit JavaFX CSS warnings.
    static void assertNoCssWarnings(CheckedRunnable task) throws InterruptedException {
        List<LogRecord> warnings = captureCssWarnings(task);
        assertTrue(warnings.isEmpty(), () -> formatLogRecords(warnings));
    }

    /// Verifies that a task does not emit matching JavaFX CSS warnings.
    static void assertNoCssWarningsMatching(
            CheckedRunnable task,
            Predicate<LogRecord> predicate
    ) throws InterruptedException {
        List<LogRecord> warnings = captureCssWarnings(task);
        assertTrue(warnings.stream().noneMatch(predicate), () -> formatLogRecords(warnings));
    }

    /// Verifies that a task does not emit M3FX token-related JavaFX CSS warnings.
    static void assertNoM3CssTokenWarnings(CheckedRunnable task) throws InterruptedException {
        assertNoCssWarningsMatching(task, DemoFxTestUtils::isM3CssTokenWarning);
    }

    /// Returns whether a JavaFX CSS warning indicates unresolved M3FX color tokens.
    static boolean isM3CssTokenWarning(LogRecord record) {
        String message = record.getMessage();
        return message != null
                && (message.contains("-m3-color-")
                || message.contains("ClassCastException") && message.contains("-fx-background-color"));
    }

    /// Runs setup on the FX application thread and verifies the result when a condition becomes true.
    static void runOnFxThreadWhen(
            BooleanSupplier condition,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        runOnFxThreadWhen(condition, () -> "Timed out waiting for JavaFX condition", setup, verification);
    }

    /// Runs setup on the FX application thread and verifies the result when a condition becomes true.
    static void runOnFxThreadWhen(
            BooleanSupplier condition,
            Supplier<String> timeoutMessage,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        runOnFxThreadWhenWithTimeout(condition, timeoutMessage, setup, verification, FX_TIMEOUT_NANOS);
    }


    /// Runs setup on the FX application thread and verifies the result when a condition becomes true.
    private static void runOnFxThreadWhenWithTimeout(
            BooleanSupplier condition,
            Supplier<String> timeoutMessage,
            Runnable setup,
            Runnable verification,
            long timeoutNanos
    ) throws InterruptedException {
        requirePositiveTimeout(timeoutNanos);
        assertNoCssWarnings(() -> runWithMotionSettingsPreserved(() -> runOnFxThreadWhenWithoutCssCapture(
                condition,
                timeoutMessage,
                setup,
                verification,
                timeoutNanos
        )));
    }

    /// Runs setup on the FX application thread and verifies the result after a condition stays true for pulses.
    static void runOnFxThreadWhenStable(
            BooleanSupplier condition,
            int stablePulseCount,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        runOnFxThreadWhenStable(
                condition,
                stablePulseCount,
                () -> "Timed out waiting for stable JavaFX condition",
                setup,
                verification
        );
    }

    /// Runs setup on the FX application thread and verifies the result after a condition stays true for pulses.
    static void runOnFxThreadWhenStable(
            BooleanSupplier condition,
            int stablePulseCount,
            Supplier<String> timeoutMessage,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        runOnFxThreadWhenStableWithTimeout(condition, stablePulseCount, timeoutMessage, setup, verification, FX_TIMEOUT_NANOS);
    }


    /// Runs setup on the FX application thread and verifies the result after a condition stays true for pulses.
    private static void runOnFxThreadWhenStableWithTimeout(
            BooleanSupplier condition,
            int stablePulseCount,
            Supplier<String> timeoutMessage,
            Runnable setup,
            Runnable verification,
            long timeoutNanos
    ) throws InterruptedException {
        requirePositiveTimeout(timeoutNanos);
        assertNoCssWarnings(() -> runWithMotionSettingsPreserved(() -> {
            runOnFxThreadWhenStableWithoutCssCapture(
                    condition,
                    stablePulseCount,
                    timeoutMessage,
                    setup,
                    verification,
                    timeoutNanos
            );
            waitForPulses(POST_PULSE_TEST_CSS_DRAIN_PULSES);
        }));
    }

    /// Runs setup on the FX application thread and verifies the result when a condition becomes true.
    private static void runOnFxThreadWhenWithoutCssCapture(
            BooleanSupplier condition,
            Supplier<String> timeoutMessage,
            Runnable setup,
            Runnable verification,
            long timeoutNanos
    ) throws InterruptedException {
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        WaitDiagnostics diagnostics = new WaitDiagnostics(1, timeoutNanos);

        Platform.runLater(() -> {
            try {
                setup.run();
                long startNanos = System.nanoTime();
                long deadlineNanos = startNanos + timeoutNanos;
                diagnostics.start(startNanos);
                AnimationTimer timer = new AnimationTimer() {
                    /// Checks the readiness condition on each JavaFX pulse.
                    @Override
                    public void handle(long now) {
                        diagnostics.recordPulse(now);
                        try {
                            if (diagnostics.evaluate(condition)) {
                                stop();
                                verification.run();
                                latch.countDown();
                            } else if (System.nanoTime() >= deadlineNanos) {
                                stop();
                                failure.set(timeoutAssertion(timeoutMessage, diagnostics));
                                latch.countDown();
                            }
                        } catch (Throwable e) {
                            stop();
                            failure.set(e);
                            latch.countDown();
                        }
                    }
                };

                if (diagnostics.evaluate(condition)) {
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

        awaitFxConditionLatch(latch, failure, timeoutMessage, diagnostics);
        throwIfFailed(failure.get());
    }

    /// Runs setup on the FX application thread and verifies after a condition remains true across pulses.
    private static void runOnFxThreadWhenStableWithoutCssCapture(
            BooleanSupplier condition,
            int stablePulseCount,
            Supplier<String> timeoutMessage,
            Runnable setup,
            Runnable verification,
            long timeoutNanos
    ) throws InterruptedException {
        if (stablePulseCount < 1) {
            throw new IllegalArgumentException("stablePulseCount must be positive");
        }

        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        WaitDiagnostics diagnostics = new WaitDiagnostics(stablePulseCount, timeoutNanos);

        Platform.runLater(() -> {
            try {
                setup.run();
                long startNanos = System.nanoTime();
                long deadlineNanos = startNanos + timeoutNanos;
                diagnostics.start(startNanos);
                AnimationTimer timer = new AnimationTimer() {
                    /// The number of consecutive pulses where the condition has been true.
                    private int stablePulses;

                    /// Checks the readiness condition and waits for the requested stable pulse count.
                    @Override
                    public void handle(long now) {
                        diagnostics.recordPulse(now);
                        try {
                            if (diagnostics.evaluate(condition)) {
                                stablePulses++;
                                diagnostics.recordStablePulses(stablePulses);
                                if (stablePulses >= stablePulseCount) {
                                    stop();
                                    verification.run();
                                    latch.countDown();
                                    return;
                                }
                            } else {
                                stablePulses = 0;
                                diagnostics.recordStablePulses(stablePulses);
                            }
                            if (System.nanoTime() >= deadlineNanos) {
                                stop();
                                failure.set(timeoutAssertion(timeoutMessage, diagnostics));
                                latch.countDown();
                            }
                        } catch (Throwable e) {
                            stop();
                            failure.set(e);
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

        awaitFxConditionLatch(latch, failure, timeoutMessage, diagnostics);
        throwIfFailed(failure.get());
    }

    /// Creates a timeout assertion from the most recent FX-thread diagnostic message.
    private static AssertionError timeoutAssertion(Supplier<String> timeoutMessage, WaitDiagnostics diagnostics) {
        String message;
        try {
            message = timeoutMessage.get();
        } catch (RuntimeException e) {
            AssertionError failure = new AssertionError(withWaitDiagnostics(
                    "Timed out waiting for JavaFX condition",
                    diagnostics
            ));
            failure.addSuppressed(e);
            return failure;
        }
        if (message.isBlank()) {
            return new AssertionError(withWaitDiagnostics("Timed out waiting for JavaFX condition", diagnostics));
        }
        return new AssertionError(withWaitDiagnostics(message, diagnostics));
    }

    /// Appends pulse and condition diagnostics to a timeout message.
    private static String withWaitDiagnostics(String message, WaitDiagnostics diagnostics) {
        return message + "\n" + diagnostics.describe();
    }

    /// Waits for a condition latch and reports the condition-specific timeout message if pulse delivery stops.
    private static void awaitFxConditionLatch(
            CountDownLatch latch,
            AtomicReference<@Nullable Throwable> failure,
            Supplier<String> timeoutMessage,
            WaitDiagnostics diagnostics
    ) throws InterruptedException {
        if (!latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS) && failure.get() == null) {
            failure.set(timeoutAssertion(timeoutMessage, diagnostics));
        }
    }

    /// Verifies that a pulse-driven wait timeout is positive.
    private static void requirePositiveTimeout(long timeoutNanos) {
        if (timeoutNanos < 1L) {
            throw new IllegalArgumentException("timeoutNanos must be positive");
        }
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

    /// Waits for a latch using the shared JavaFX demo test timeout.
    static void await(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    /// Waits for a latch using the shared JavaFX demo test timeout and wraps interruptions.
    static void awaitUnchecked(CountDownLatch latch) {
        try {
            await(latch);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    /// Captures warning-level records from the requested loggers while running a task.
    private static @Unmodifiable List<LogRecord> captureWarnings(
            CheckedRunnable task,
            String... loggerNames
    ) throws InterruptedException {
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

    /// Re-throws a captured failure from the FX application thread.
    static void throwIfFailed(@Nullable Throwable exception) {
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

    /// Records pulse and condition progress for timeout diagnostics.
    private static final class WaitDiagnostics {
        /// The number of stable true pulses required before verification runs.
        private final int requiredStablePulses;

        /// The configured timeout for this wait.
        private final long timeoutNanos;

        /// Whether the wait has recorded its start time.
        private boolean started;

        /// The `System.nanoTime` value when the wait started.
        private long startNanos;

        /// The `System.nanoTime` value when the condition was last evaluated.
        private long lastConditionCheckNanos;

        /// The last JavaFX pulse timestamp reported by `AnimationTimer`.
        private long lastPulseNanos;

        /// The number of JavaFX pulses observed by the wait timer.
        private int pulseCount;

        /// The number of times the wait condition was evaluated.
        private int conditionEvaluations;

        /// The number of condition evaluations that returned true.
        private int trueConditionEvaluations;

        /// The current consecutive true-pulse count.
        private int stablePulses;

        /// The highest consecutive true-pulse count observed before timeout.
        private int maxStablePulses;

        /// Whether at least one condition result has been recorded.
        private boolean hasLastCondition;

        /// The most recent condition result.
        private boolean lastCondition;

        /// Creates wait diagnostics for the requested stable pulse count.
        private WaitDiagnostics(int requiredStablePulses, long timeoutNanos) {
            this.requiredStablePulses = requiredStablePulses;
            this.timeoutNanos = timeoutNanos;
        }


        /// Records the start time of the wait.
        private void start(long startNanos) {
            this.started = true;
            this.startNanos = startNanos;
            this.lastConditionCheckNanos = startNanos;
        }

        /// Records that an `AnimationTimer` pulse has reached the wait.
        private void recordPulse(long pulseNanos) {
            pulseCount++;
            lastPulseNanos = pulseNanos;
        }

        /// Evaluates and records the wait condition.
        private boolean evaluate(BooleanSupplier condition) {
            boolean result = condition.getAsBoolean();
            conditionEvaluations++;
            if (result) {
                trueConditionEvaluations++;
            }
            hasLastCondition = true;
            lastCondition = result;
            lastConditionCheckNanos = System.nanoTime();
            return result;
        }

        /// Records the current stable true-pulse count.
        private void recordStablePulses(int stablePulses) {
            this.stablePulses = stablePulses;
            maxStablePulses = Math.max(maxStablePulses, stablePulses);
        }

        /// Returns a compact diagnostic string for timeout failures.
        private String describe() {
            long now = System.nanoTime();
            long elapsedMillis = started ? TimeUnit.NANOSECONDS.toMillis(now - startNanos) : -1L;
            long lastConditionAgeMillis = started
                    ? TimeUnit.NANOSECONDS.toMillis(now - lastConditionCheckNanos)
                    : -1L;
            int falseConditionEvaluations = conditionEvaluations - trueConditionEvaluations;
            return "wait diagnostics: timeoutMillis=" + TimeUnit.NANOSECONDS.toMillis(timeoutNanos)
                    + ", elapsedMillis=" + elapsedMillis
                    + ", pulseCount=" + pulseCount
                    + ", conditionEvaluations=" + conditionEvaluations
                    + ", trueConditionEvaluations=" + trueConditionEvaluations
                    + ", falseConditionEvaluations=" + falseConditionEvaluations
                    + ", lastCondition=" + (hasLastCondition ? Boolean.toString(lastCondition) : "unavailable")
                    + ", stablePulses=" + stablePulses
                    + ", maxStablePulses=" + maxStablePulses
                    + ", requiredStablePulses=" + requiredStablePulses
                    + ", lastConditionAgeMillis=" + lastConditionAgeMillis
                    + ", lastPulseNanos=" + (pulseCount == 0 ? "unavailable" : Long.toString(lastPulseNanos));
        }
    }

    /// Represents a test action that may be interrupted while waiting for JavaFX work.
    @FunctionalInterface
    interface CheckedRunnable {
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
