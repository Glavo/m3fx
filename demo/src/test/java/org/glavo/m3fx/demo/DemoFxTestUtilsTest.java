// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.application.Platform;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests JavaFX helper behavior used by the demo visual smoke tests.
@NotNullByDefault
final class DemoFxTestUtilsTest {
    /// Starts the JavaFX toolkit before exercising FX-thread wait helpers.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        DemoFxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that one-shot wait timeouts include condition and pulse diagnostics.
    @Test
    void runOnFxThreadWhenTimeoutIncludesWaitDiagnostics() {
        AssertionError failure = assertThrows(AssertionError.class, () -> DemoFxTestUtils.runOnFxThreadWhenWithTimeoutForTesting(
                () -> false,
                () -> "condition stayed false",
                () -> {
                },
                () -> {
                    throw new AssertionError("verification should not run");
                },
                TimeUnit.MILLISECONDS.toNanos(50L)
        ));
        String message = failure.getMessage();
        assertTrue(message.contains("condition stayed false"));
        assertTrue(message.contains("wait diagnostics:"));
        assertTrue(message.contains("pulseCount="));
        assertTrue(message.contains("conditionEvaluations="));
        assertTrue(message.contains("lastCondition=false"));
    }

    /// Verifies that stable wait timeouts report stable-pulse progress.
    @Test
    void runOnFxThreadWhenStableTimeoutIncludesWaitDiagnostics() {
        AssertionError failure = assertThrows(AssertionError.class, () -> DemoFxTestUtils.runOnFxThreadWhenStableWithTimeoutForTesting(
                () -> true,
                1000,
                () -> "condition never stayed stable",
                () -> {
                },
                () -> {
                    throw new AssertionError("verification should not run");
                },
                TimeUnit.MILLISECONDS.toNanos(80L)
        ));
        String message = failure.getMessage();
        assertTrue(message.contains("condition never stayed stable"));
        assertTrue(message.contains("lastCondition=true"));
        assertTrue(message.contains("stablePulses="));
        assertTrue(message.contains("maxStablePulses="));
        assertTrue(message.contains("requiredStablePulses=1000"));
    }

    /// Verifies that one-shot condition waits restore global motion settings after setup changes them.
    @Test
    void runOnFxThreadWhenRestoresGlobalMotionSettings() throws InterruptedException {
        boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
        M3MotionScheme previousScheme = M3MotionSettings.getMotionScheme();
        M3MotionBehavior previousBehavior = M3MotionSettings.getMotionBehavior();
        M3MotionScheme baselineScheme = M3MotionScheme.standard();
        M3MotionBehavior baselineBehavior = M3MotionBehavior.standard();
        M3MotionScheme changedScheme = M3MotionScheme.expressive();
        M3MotionBehavior changedBehavior = M3MotionBehavior.expressive();
        try {
            M3MotionSettings.setAnimationsEnabled(true);
            M3MotionSettings.setMotionScheme(baselineScheme);
            M3MotionSettings.setMotionBehavior(baselineBehavior);

            DemoFxTestUtils.runOnFxThreadWhen(
                    () -> !M3MotionSettings.areAnimationsEnabled()
                            && M3MotionSettings.getMotionScheme() == changedScheme
                            && M3MotionSettings.getMotionBehavior() == changedBehavior,
                    () -> {
                        M3MotionSettings.setAnimationsEnabled(false);
                        M3MotionSettings.setMotionScheme(changedScheme);
                        M3MotionSettings.setMotionBehavior(changedBehavior);
                    },
                    () -> {
                        assertFalse(M3MotionSettings.areAnimationsEnabled());
                        assertSame(changedScheme, M3MotionSettings.getMotionScheme());
                        assertSame(changedBehavior, M3MotionSettings.getMotionBehavior());
                    }
            );

            assertTrue(M3MotionSettings.areAnimationsEnabled());
            assertSame(baselineScheme, M3MotionSettings.getMotionScheme());
            assertSame(baselineBehavior, M3MotionSettings.getMotionBehavior());
        } finally {
            M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);
            M3MotionSettings.setMotionScheme(previousScheme);
            M3MotionSettings.setMotionBehavior(previousBehavior);
        }
    }

    /// Verifies that stable condition waits restore global motion settings after verification changes them.
    @Test
    void runOnFxThreadWhenStableRestoresGlobalMotionSettings() throws InterruptedException {
        boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
        M3MotionScheme previousScheme = M3MotionSettings.getMotionScheme();
        M3MotionBehavior previousBehavior = M3MotionSettings.getMotionBehavior();
        M3MotionScheme baselineScheme = M3MotionScheme.standard();
        M3MotionBehavior baselineBehavior = M3MotionBehavior.standard();
        M3MotionScheme changedScheme = M3MotionScheme.expressive();
        M3MotionBehavior changedBehavior = M3MotionBehavior.expressive();
        try {
            M3MotionSettings.setAnimationsEnabled(false);
            M3MotionSettings.setMotionScheme(baselineScheme);
            M3MotionSettings.setMotionBehavior(baselineBehavior);

            DemoFxTestUtils.runOnFxThreadWhenStable(
                    () -> M3MotionSettings.areAnimationsEnabled()
                            && M3MotionSettings.getMotionScheme() == changedScheme
                            && M3MotionSettings.getMotionBehavior() == changedBehavior,
                    2,
                    () -> {
                        M3MotionSettings.setAnimationsEnabled(true);
                        M3MotionSettings.setMotionScheme(changedScheme);
                        M3MotionSettings.setMotionBehavior(changedBehavior);
                    },
                    () -> {
                        assertTrue(M3MotionSettings.areAnimationsEnabled());
                        assertSame(changedScheme, M3MotionSettings.getMotionScheme());
                        assertSame(changedBehavior, M3MotionSettings.getMotionBehavior());
                        M3MotionSettings.setAnimationsEnabled(true);
                        M3MotionSettings.setMotionScheme(changedScheme);
                        M3MotionSettings.setMotionBehavior(changedBehavior);
                    }
            );

            assertFalse(M3MotionSettings.areAnimationsEnabled());
            assertSame(baselineScheme, M3MotionSettings.getMotionScheme());
            assertSame(baselineBehavior, M3MotionSettings.getMotionBehavior());
        } finally {
            M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);
            M3MotionSettings.setMotionScheme(previousScheme);
            M3MotionSettings.setMotionBehavior(previousBehavior);
        }
    }

    /// Verifies that one-shot condition waits restore global motion settings after verification fails.
    @Test
    void runOnFxThreadWhenRestoresGlobalMotionSettingsAfterVerificationFailure() {
        boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
        M3MotionScheme previousScheme = M3MotionSettings.getMotionScheme();
        M3MotionBehavior previousBehavior = M3MotionSettings.getMotionBehavior();
        M3MotionScheme baselineScheme = M3MotionScheme.standard();
        M3MotionBehavior baselineBehavior = M3MotionBehavior.standard();
        M3MotionScheme changedScheme = M3MotionScheme.expressive();
        M3MotionBehavior changedBehavior = M3MotionBehavior.expressive();
        try {
            M3MotionSettings.setAnimationsEnabled(true);
            M3MotionSettings.setMotionScheme(baselineScheme);
            M3MotionSettings.setMotionBehavior(baselineBehavior);

            assertThrows(AssertionError.class, () -> DemoFxTestUtils.runOnFxThreadWhen(
                    () -> !M3MotionSettings.areAnimationsEnabled()
                            && M3MotionSettings.getMotionScheme() == changedScheme
                            && M3MotionSettings.getMotionBehavior() == changedBehavior,
                    () -> {
                        M3MotionSettings.setAnimationsEnabled(false);
                        M3MotionSettings.setMotionScheme(changedScheme);
                        M3MotionSettings.setMotionBehavior(changedBehavior);
                    },
                    () -> {
                        assertFalse(M3MotionSettings.areAnimationsEnabled());
                        assertSame(changedScheme, M3MotionSettings.getMotionScheme());
                        assertSame(changedBehavior, M3MotionSettings.getMotionBehavior());
                        throw new AssertionError("verification failure");
                    }
            ));

            assertTrue(M3MotionSettings.areAnimationsEnabled());
            assertSame(baselineScheme, M3MotionSettings.getMotionScheme());
            assertSame(baselineBehavior, M3MotionSettings.getMotionBehavior());
        } finally {
            M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);
            M3MotionSettings.setMotionScheme(previousScheme);
            M3MotionSettings.setMotionBehavior(previousBehavior);
        }
    }

    /// Verifies that stable condition waits restore global motion settings after setup fails.
    @Test
    void runOnFxThreadWhenStableRestoresGlobalMotionSettingsAfterSetupFailure() {
        boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
        M3MotionScheme previousScheme = M3MotionSettings.getMotionScheme();
        M3MotionBehavior previousBehavior = M3MotionSettings.getMotionBehavior();
        M3MotionScheme baselineScheme = M3MotionScheme.standard();
        M3MotionBehavior baselineBehavior = M3MotionBehavior.standard();
        M3MotionScheme changedScheme = M3MotionScheme.expressive();
        M3MotionBehavior changedBehavior = M3MotionBehavior.expressive();
        try {
            M3MotionSettings.setAnimationsEnabled(false);
            M3MotionSettings.setMotionScheme(baselineScheme);
            M3MotionSettings.setMotionBehavior(baselineBehavior);

            assertThrows(IllegalStateException.class, () -> DemoFxTestUtils.runOnFxThreadWhenStable(
                    () -> M3MotionSettings.areAnimationsEnabled()
                            && M3MotionSettings.getMotionScheme() == changedScheme
                            && M3MotionSettings.getMotionBehavior() == changedBehavior,
                    2,
                    () -> {
                        M3MotionSettings.setAnimationsEnabled(true);
                        M3MotionSettings.setMotionScheme(changedScheme);
                        M3MotionSettings.setMotionBehavior(changedBehavior);
                        throw new IllegalStateException("setup failure");
                    },
                    () -> {
                        throw new AssertionError("verification should not run");
                    }
            ));

            assertFalse(M3MotionSettings.areAnimationsEnabled());
            assertSame(baselineScheme, M3MotionSettings.getMotionScheme());
            assertSame(baselineBehavior, M3MotionSettings.getMotionBehavior());
        } finally {
            M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);
            M3MotionSettings.setMotionScheme(previousScheme);
            M3MotionSettings.setMotionBehavior(previousBehavior);
        }
    }
}
