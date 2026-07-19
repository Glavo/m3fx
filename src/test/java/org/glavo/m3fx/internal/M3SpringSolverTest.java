// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.glavo.m3fx.animation.M3SpringParameters;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    /// Verifies that every damping regime finds its final visibility-threshold crossing.
    @Test
    void estimatesFiniteSettlingDurationAcrossDampingRegimes() {
        M3SpringParameters[] springs = {
                new M3SpringParameters(0.8, 380.0),
                new M3SpringParameters(1.0, 1600.0),
                new M3SpringParameters(1.4, 380.0)
        };
        double threshold = 5.0e-4;

        for (M3SpringParameters spring : springs) {
            double duration = M3SpringSolver.estimateDurationSeconds(-0.08, 0.0, threshold, spring);

            assertTrue(
                    Double.isFinite(duration) && duration > 0.0 && duration < 1.0,
                    () -> "spring=" + spring + ", duration=" + duration
            );
            for (int sample = 0; sample <= 10; sample++) {
                double elapsed = duration + sample * 0.01;
                double displacement = M3SpringSolver.value(0.92, 1.0, 0.0, elapsed, spring) - 1.0;
                assertTrue(
                        Math.abs(displacement) <= threshold * 1.01,
                        () -> "spring=" + spring + ", duration=" + duration + ", displacement=" + displacement
                );
            }
        }
    }

    /// Verifies settled and invalid states follow the estimator's internal contract.
    @Test
    void durationEstimatorHandlesBoundaryStates() {
        M3SpringParameters spring = new M3SpringParameters(0.8, 380.0);

        assertEquals(0.0, M3SpringSolver.estimateDurationSeconds(0.0, 0.0, 0.01, spring));
        assertTrue(Double.isNaN(
                M3SpringSolver.estimateDurationSeconds(Double.NaN, 0.0, 0.01, spring)
        ));
        assertThrows(
                IllegalArgumentException.class,
                () -> M3SpringSolver.estimateDurationSeconds(1.0, 0.0, 0.0, spring)
        );
    }
}
