// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3MotionTokens;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
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
    /// The property key used to retain a test-local motion theme override.
    private static final Object MOTION_THEME_STATE_KEY = new Object();

    /// The maximum time a test waits for JavaFX toolkit work to complete.
    public static final long FX_TIMEOUT_SECONDS = 10L;

    /// The default timeout in nanoseconds for pulse-driven JavaFX condition waits.
    private static final long FX_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(FX_TIMEOUT_SECONDS);

    /// The number of FX event-loop turns kept under CSS warning capture after tests close windows or popups.
    private static final int POST_TEST_CSS_DRAIN_TURNS = 8;

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
        Platform.setImplicitExit(false);
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

    /// Clears a control's current skin and installs a newly created replacement.
    ///
    /// Clearing the current skin first guarantees that replacement occurs when the old and new skins have the same
    /// runtime class. The factory is invoked after the old skin has been disposed.
    ///
    /// @param control     the control whose skin is replaced
    /// @param skinFactory the factory that creates a skin for `control`
    /// @param <C>         the control type
    /// @throws NullPointerException if `control`, `skinFactory`, or the created skin is `null`
    /// @throws IllegalArgumentException if the created skin belongs to a different control
    public static <C extends Control> void replaceSkin(
            C control,
            Function<? super C, ? extends Skin<?>> skinFactory
    ) {
        Objects.requireNonNull(control, "control");
        Objects.requireNonNull(skinFactory, "skinFactory");
        // JavaFX 17 ignores direct replacement when both skins have the same runtime class.
        control.setSkin(null);
        Skin<?> skin = Objects.requireNonNull(skinFactory.apply(control), "skinFactory result");
        if (skin.getSkinnable() != control) {
            throw new IllegalArgumentException("skinFactory created a skin for a different control");
        }
        control.setSkin(skin);
    }

    /// Runs a task with reduced motion requested globally and restores the previous setting.
    public static void runWithReducedMotion(Runnable task) {
        runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(true);
            task.run();
        });
    }

    /// Runs a task with global Material motion settings restored afterward.
    public static void runWithMotionSettingsPreserved(Runnable task) {
        boolean previousReducedMotionRequested = M3MotionSettings.isGlobalReducedMotionRequested();
        try {
            task.run();
        } finally {
            M3MotionSettings.setGlobalReducedMotionRequested(previousReducedMotionRequested);
        }
    }

    /// Runs a task with global Material motion settings restored afterward.
    private static void runWithMotionSettingsPreservedChecked(CheckedRunnable task) throws InterruptedException {
        boolean previousReducedMotionRequested = M3MotionSettings.isGlobalReducedMotionRequested();
        try {
            task.run();
        } finally {
            M3MotionSettings.setGlobalReducedMotionRequested(previousReducedMotionRequested);
        }
    }

    /// Installs a test-local theme that replaces only the semantic motion scheme.
    public static void setMotionScheme(Node node, M3MotionScheme scheme) {
        if (!Platform.isFxApplicationThread()) {
            runOnFxThread(() -> setMotionScheme(node, scheme));
            return;
        }
        MotionThemeState state = motionThemeState(node);
        state.scheme = Objects.requireNonNull(scheme, "scheme");
        applyMotionTheme(state);
    }

    /// Clears a test-local motion-scheme override while preserving any behavior override.
    public static void clearMotionScheme(Node node) {
        if (!Platform.isFxApplicationThread()) {
            runOnFxThread(() -> clearMotionScheme(node));
            return;
        }
        @Nullable MotionThemeState state = existingMotionThemeState(node);
        if (state == null) {
            return;
        }
        state.scheme = null;
        applyMotionTheme(state);
    }

    /// Installs a test-local theme that replaces only motion-adjacent interaction timings.
    public static void setMotionBehavior(Node node, M3MotionBehavior behavior) {
        if (!Platform.isFxApplicationThread()) {
            runOnFxThread(() -> setMotionBehavior(node, behavior));
            return;
        }
        MotionThemeState state = motionThemeState(node);
        state.behavior = Objects.requireNonNull(behavior, "behavior");
        applyMotionTheme(state);
    }

    /// Clears a test-local motion-behavior override while preserving any scheme override.
    public static void clearMotionBehavior(Node node) {
        if (!Platform.isFxApplicationThread()) {
            runOnFxThread(() -> clearMotionBehavior(node));
            return;
        }
        @Nullable MotionThemeState state = existingMotionThemeState(node);
        if (state == null) {
            return;
        }
        state.behavior = null;
        applyMotionTheme(state);
    }

    /// Returns the mutable test motion-theme state for a node, creating it from the inherited theme when needed.
    private static MotionThemeState motionThemeState(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable MotionThemeState state = existingMotionThemeState(node);
        if (state != null) {
            return state;
        }
        Parent root = motionThemeRoot(node);
        @Nullable M3Theme directTheme = M3ThemeManager.getTheme(root);
        M3Theme baseTheme = directTheme == null ? inheritedTheme(root) : directTheme;
        @Nullable Scene scene = root.getScene();
        @Nullable Scene managedScene = scene != null && scene.getRoot() == root && directTheme != null ? scene : null;
        state = new MotionThemeState(node, root, managedScene, baseTheme, directTheme != null);
        node.getProperties().put(MOTION_THEME_STATE_KEY, state);
        return state;
    }

    /// Returns the root whose theme controls motion for a test target.
    private static Parent motionThemeRoot(Node node) {
        @Nullable Scene scene = node.getScene();
        if (scene != null && M3ThemeManager.getTheme(scene) != null) {
            return scene.getRoot();
        }

        @Nullable Node current = node;
        while (current != null) {
            if (current instanceof Parent parent && M3ThemeManager.getTheme(parent) != null) {
                return parent;
            }
            current = current.getParent();
        }
        if (node instanceof Parent parent) {
            return parent;
        }
        throw new IllegalArgumentException("motion theme target has no Parent theme root");
    }

    /// Returns an existing test motion-theme state without allocating a node properties map.
    private static @Nullable MotionThemeState existingMotionThemeState(Node node) {
        Objects.requireNonNull(node, "node");
        if (!node.hasProperties()) {
            return null;
        }
        @Nullable Object value = node.getProperties().get(MOTION_THEME_STATE_KEY);
        return value instanceof MotionThemeState state ? state : null;
    }

    /// Resolves the nearest inherited theme for a new local motion override.
    private static M3Theme inheritedTheme(Parent root) {
        @Nullable Parent current = root.getParent();
        while (current != null) {
            @Nullable M3Theme theme = M3ThemeManager.getTheme(current);
            if (theme != null) {
                return theme;
            }
            current = current.getParent();
        }
        return M3Theme.defaultTheme();
    }

    /// Applies or removes one test-local theme from the current override state.
    private static void applyMotionTheme(MotionThemeState state) {
        Parent root = state.root;
        if (state.scheme == null && state.behavior == null) {
            state.target.getProperties().remove(MOTION_THEME_STATE_KEY);
            if (state.restoreDirectTheme) {
                if (state.scene == null) {
                    M3ThemeManager.install(root, state.baseTheme);
                } else {
                    M3ThemeManager.install(state.scene, state.baseTheme);
                }
            } else {
                M3ThemeManager.uninstall(root);
            }
            root.applyCss();
            root.layout();
            return;
        }

        M3TokenSet baseTokens = state.baseTheme.tokens();
        M3MotionTokens baseMotion = baseTokens.motionTokens();
        M3MotionTokens motion = M3MotionTokens.builder(baseMotion)
                .scheme(state.scheme == null ? baseMotion.scheme() : state.scheme)
                .behavior(state.behavior == null ? baseMotion.behavior() : state.behavior)
                .build();
        M3TokenSet tokens = M3TokenSet.builder(baseTokens)
                .motionTokens(motion)
                .build();
        M3Theme theme = M3Theme.fromTokenSet(tokens);
        if (state.scene == null) {
            M3ThemeManager.install(root, theme);
        } else {
            M3ThemeManager.install(state.scene, theme);
        }
        root.applyCss();
        root.layout();
    }

    /// Stores the base theme and nullable motion overrides applied to one test root.
    @NotNullByDefault
    private static final class MotionThemeState {
        /// The target that owns this test override state.
        private final Node target;

        /// The root that receives the temporary motion theme.
        private final Parent root;

        /// The scene installation that owns the root theme, or `null` for a directly themed root.
        private final @Nullable Scene scene;

        /// The theme whose non-motion tokens and default motion values are preserved.
        private final M3Theme baseTheme;

        /// Whether clearing all overrides must restore a theme that was already installed directly.
        private final boolean restoreDirectTheme;

        /// The semantic motion-scheme override, or `null` to preserve the base value.
        private @Nullable M3MotionScheme scheme;

        /// The interaction-timing override, or `null` to preserve the base value.
        private @Nullable M3MotionBehavior behavior;

        /// Creates state for one test-local theme override.
        private MotionThemeState(
                Node target,
                Parent root,
                @Nullable Scene scene,
                M3Theme baseTheme,
                boolean restoreDirectTheme
        ) {
            this.target = target;
            this.root = root;
            this.scene = scene;
            this.baseTheme = baseTheme;
            this.restoreDirectTheme = restoreDirectTheme;
        }
    }

    /// Runs a task on the FX application thread with global Material animations disabled.
    public static void runOnFxThreadWithAnimationsDisabled(Runnable task) {
        runOnFxThread(() -> runWithReducedMotion(task));
    }

    /// Captures JavaFX CSS warnings emitted while running a task.
    public static @Unmodifiable List<LogRecord> captureCssWarnings(Runnable task) {
        return captureWarningsChecked(() -> task.run(), "javafx.css", "javafx.scene.CssStyleHelper");
    }

    /// Verifies that a task does not emit JavaFX CSS warnings.
    public static void assertNoCssWarnings(Runnable task) {
        List<LogRecord> warnings = captureCssWarnings(task);
        assertTrue(warnings.isEmpty(), () -> formatLogRecords(warnings));
    }

    /// Verifies that an interruptible task does not emit JavaFX CSS warnings.
    ///
    /// @param task the interruptible task to execute while warnings are captured
    public static void assertNoCssWarningsInterruptibly(InterruptibleRunnable task) {
        List<LogRecord> warnings = captureWarningsChecked(
                Objects.requireNonNull(task, "task")::run,
                "javafx.css",
                "javafx.scene.CssStyleHelper"
        );
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

    /// Runs a supplier on the FX application thread and returns its non-null result.
    ///
    /// @throws NullPointerException if `supplier` or its result is `null`
    public static <T> T callOnFxThread(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (Platform.isFxApplicationThread()) {
            return Objects.requireNonNull(supplier.get(), "FX task result");
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
        return Objects.requireNonNull(result.get(), "FX task result");
    }

    /// Runs setup on the FX application thread and verifies the result when a condition becomes true.
    public static void runOnFxThreadWhen(
            BooleanSupplier condition,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        runOnFxThreadWhen(condition, () -> "Timed out waiting for JavaFX condition", setup, verification);
    }

    /// Runs setup on the FX application thread and verifies the result when a condition becomes true.
    public static void runOnFxThreadWhen(
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
    ) {
        requirePositiveTimeout(timeoutNanos);
        List<LogRecord> warnings = captureWarningsChecked(
                () -> runWithMotionSettingsPreservedChecked(
                        () -> runOnFxThreadWhenWithoutCssCapture(
                                condition,
                                timeoutMessage,
                                setup,
                                verification,
                                timeoutNanos
                        )
                ),
                "javafx.css",
                "javafx.scene.CssStyleHelper"
        );
        assertTrue(warnings.stream().noneMatch(FxTestUtils::isM3CssTokenWarning), () -> formatLogRecords(warnings));
    }

    /// Runs setup on the FX application thread and verifies the result after a condition stays true for pulses.
    public static void runOnFxThreadWhenStable(
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
    public static void runOnFxThreadWhenStable(
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
    ) {
        requirePositiveTimeout(timeoutNanos);
        List<LogRecord> warnings = captureWarningsChecked(
                () -> runWithMotionSettingsPreservedChecked(
                        () -> {
                            runOnFxThreadWhenStableWithoutCssCapture(
                                    condition,
                                    stablePulseCount,
                                    timeoutMessage,
                                    setup,
                                    verification,
                                    timeoutNanos
                            );
                            waitForFxTurns(POST_TEST_CSS_DRAIN_TURNS);
                        }
                ),
                "javafx.css",
                "javafx.scene.CssStyleHelper"
        );
        assertTrue(warnings.stream().noneMatch(FxTestUtils::isM3CssTokenWarning), () -> formatLogRecords(warnings));
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

    /// Waits for a condition latch and reports the condition-specific timeout message if pulses stop.
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

    /// Waits for deferred JavaFX event-loop work before continuing on the test thread.
    private static void waitForFxTurns(int turnCount) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> runAfterFxTurns(turnCount, latch::countDown));
        await(latch);
    }

    /// Runs an action after the requested number of JavaFX event-loop turns.
    private static void runAfterFxTurns(int turnCount, Runnable action) {
        if (turnCount < 1) {
            throw new IllegalArgumentException("turnCount must be positive");
        }
        if (turnCount == 1) {
            action.run();
        } else {
            Platform.runLater(() -> runAfterFxTurns(turnCount - 1, action));
        }
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
    private interface CheckedRunnable {
        /// Runs this action.
        void run() throws InterruptedException;
    }

    /// Represents a public test-fixture action that may be interrupted while waiting for JavaFX work.
    @FunctionalInterface
    public interface InterruptibleRunnable {
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
