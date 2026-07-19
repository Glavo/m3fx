// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.glavo.m3fx.animation.M3SpringParameters;
import org.jetbrains.annotations.NotNullByDefault;

/// Evaluates unit-mass damped springs without allocating per animation pulse.
@NotNullByDefault
final class M3SpringSolver {
    /// The tolerance used when selecting the critically damped spring equation.
    private static final double CRITICAL_DAMPING_TOLERANCE = 1.0e-6;

    /// Prevents instantiation.
    private M3SpringSolver() {
    }

    /// Returns the animated value at the specified elapsed time.
    ///
    /// @param start           the value at time zero
    /// @param target          the spring equilibrium
    /// @param initialVelocity the value velocity at time zero, in units per second
    /// @param elapsedSeconds  the non-negative elapsed time in seconds
    /// @param spring          the physical spring parameters
    /// @return the spring value at `elapsedSeconds`
    static double value(
            double start,
            double target,
            double initialVelocity,
            double elapsedSeconds,
            M3SpringParameters spring
    ) {
        return target + displacement(start - target, initialVelocity, elapsedSeconds, spring);
    }

    /// Returns the animated value's velocity at the specified elapsed time.
    ///
    /// @param start           the value at time zero
    /// @param target          the spring equilibrium
    /// @param initialVelocity the value velocity at time zero, in units per second
    /// @param elapsedSeconds  the non-negative elapsed time in seconds
    /// @param spring          the physical spring parameters
    /// @return the value velocity at `elapsedSeconds`, in units per second
    static double velocity(
            double start,
            double target,
            double initialVelocity,
            double elapsedSeconds,
            M3SpringParameters spring
    ) {
        double dampingRatio = spring.dampingRatio();
        double naturalFrequency = Math.sqrt(spring.stiffness());
        double initialDisplacement = start - target;

        if (dampingRatio < 1.0 - CRITICAL_DAMPING_TOLERANCE) {
            double dampedFrequency = naturalFrequency * Math.sqrt(1.0 - dampingRatio * dampingRatio);
            double decay = Math.exp(-dampingRatio * naturalFrequency * elapsedSeconds);
            double cosine = Math.cos(dampedFrequency * elapsedSeconds);
            double sine = Math.sin(dampedFrequency * elapsedSeconds);
            double secondCoefficient =
                    (initialVelocity + dampingRatio * naturalFrequency * initialDisplacement) / dampedFrequency;
            double oscillation = initialDisplacement * cosine + secondCoefficient * sine;
            double oscillationVelocity =
                    -initialDisplacement * dampedFrequency * sine + secondCoefficient * dampedFrequency * cosine;
            return decay * (oscillationVelocity - dampingRatio * naturalFrequency * oscillation);
        }
        if (dampingRatio > 1.0 + CRITICAL_DAMPING_TOLERANCE) {
            double root = Math.sqrt(dampingRatio * dampingRatio - 1.0);
            double firstRate = -naturalFrequency * (dampingRatio - root);
            double secondRate = -naturalFrequency * (dampingRatio + root);
            double firstCoefficient =
                    (initialVelocity - secondRate * initialDisplacement) / (firstRate - secondRate);
            double secondCoefficient = initialDisplacement - firstCoefficient;
            return firstRate * firstCoefficient * Math.exp(firstRate * elapsedSeconds)
                    + secondRate * secondCoefficient * Math.exp(secondRate * elapsedSeconds);
        }

        double decay = Math.exp(-naturalFrequency * elapsedSeconds);
        double linearCoefficient = initialVelocity + naturalFrequency * initialDisplacement;
        return decay * (initialVelocity - naturalFrequency * linearCoefficient * elapsedSeconds);
    }

    /// Returns displacement from equilibrium at the specified elapsed time.
    private static double displacement(
            double initialDisplacement,
            double initialVelocity,
            double elapsedSeconds,
            M3SpringParameters spring
    ) {
        double dampingRatio = spring.dampingRatio();
        double naturalFrequency = Math.sqrt(spring.stiffness());

        if (dampingRatio < 1.0 - CRITICAL_DAMPING_TOLERANCE) {
            double dampedFrequency = naturalFrequency * Math.sqrt(1.0 - dampingRatio * dampingRatio);
            double secondCoefficient =
                    (initialVelocity + dampingRatio * naturalFrequency * initialDisplacement) / dampedFrequency;
            return Math.exp(-dampingRatio * naturalFrequency * elapsedSeconds)
                    * (initialDisplacement * Math.cos(dampedFrequency * elapsedSeconds)
                    + secondCoefficient * Math.sin(dampedFrequency * elapsedSeconds));
        }
        if (dampingRatio > 1.0 + CRITICAL_DAMPING_TOLERANCE) {
            double root = Math.sqrt(dampingRatio * dampingRatio - 1.0);
            double firstRate = -naturalFrequency * (dampingRatio - root);
            double secondRate = -naturalFrequency * (dampingRatio + root);
            double firstCoefficient =
                    (initialVelocity - secondRate * initialDisplacement) / (firstRate - secondRate);
            double secondCoefficient = initialDisplacement - firstCoefficient;
            return firstCoefficient * Math.exp(firstRate * elapsedSeconds)
                    + secondCoefficient * Math.exp(secondRate * elapsedSeconds);
        }

        double linearCoefficient = initialVelocity + naturalFrequency * initialDisplacement;
        return (initialDisplacement + linearCoefficient * elapsedSeconds)
                * Math.exp(-naturalFrequency * elapsedSeconds);
    }
}
