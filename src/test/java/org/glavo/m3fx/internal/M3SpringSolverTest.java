// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.glavo.m3fx.animation.M3SpringParameters;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the allocation-free spring equations used by interruptible node motion.
@NotNullByDefault
final class M3SpringSolverTest {
    /// Verifies that critically damped effects start continuously and converge without overshoot.
    @Test
    void criticallyDampedSpringConvergesMonotonically() {
        M3SpringParameters spring = new M3SpringParameters(1.0, 1600.0);

        assertEquals(0.0, M3SpringSolver.value(0.0, 1.0, 0.0, 0.0, spring), 1.0e-12);
        assertEquals(0.0, M3SpringSolver.velocity(0.0, 1.0, 0.0, 0.0, spring), 1.0e-12);

        double previous = 0.0;
        for (int step = 1; step <= 20; step++) {
            double value = M3SpringSolver.value(0.0, 1.0, 0.0, step * 0.01, spring);
            assertTrue(value >= previous && value < 1.0);
            previous = value;
        }
        assertTrue(previous > 0.99);
    }

    /// Verifies that an Expressive spatial spring overshoots before settling.
    @Test
    void underdampedSpatialSpringOvershoots() {
        M3SpringParameters spring = new M3SpringParameters(0.6, 800.0);

        assertTrue(M3SpringSolver.value(0.0, 1.0, 0.0, 0.12, spring) > 1.0);
        assertEquals(1.0, M3SpringSolver.value(0.0, 1.0, 0.0, 0.5, spring), 0.001);
    }

    /// Verifies that retargeting can carry an arbitrary incoming velocity into the new spring.
    @Test
    void springPreservesSuppliedInitialVelocity() {
        M3SpringParameters spring = new M3SpringParameters(0.9, 700.0);

        assertEquals(3.25, M3SpringSolver.velocity(0.4, 0.0, 3.25, 0.0, spring), 1.0e-12);
        assertTrue(M3SpringSolver.value(0.4, 0.0, 3.25, 0.002, spring) > 0.4);
    }
}
