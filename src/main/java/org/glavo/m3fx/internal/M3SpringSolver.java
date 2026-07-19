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

    /// The time tolerance used by the duration estimator's Newton iterations, in seconds.
    private static final double DURATION_CONVERGENCE_SECONDS = 1.0e-3;

    /// The maximum number of iterations used to locate the final visibility-threshold crossing.
    private static final int MAX_DURATION_ITERATIONS = 100;

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

    /// Estimates when the spring last crosses the supplied displacement threshold.
    ///
    /// The estimate follows the damped-spring envelope and final-crossing model used by
    /// [AndroidX animation-core](https://android.googlesource.com/platform/frameworks/support/+/f68402285edc35592203bcd92aaf1af3636464a4/compose/animation/animation-core/src/commonMain/kotlin/androidx/compose/animation/core/SpringEstimation.kt).
    /// It performs no per-call allocation. A non-finite result indicates that the supplied state cannot be
    /// estimated and lets the caller select a deterministic fallback duration.
    ///
    /// @param initialDisplacement displacement from equilibrium at time zero
    /// @param initialVelocity     value velocity at time zero, in units per second
    /// @param visibilityThreshold the finite, positive displacement below which the value is considered settled
    /// @param spring              the physical spring parameters
    /// @return the non-negative estimated duration in seconds, or `NaN` when no finite estimate is available
    /// @throws IllegalArgumentException if `visibilityThreshold` is non-finite or not greater than zero
    static double estimateDurationSeconds(
            double initialDisplacement,
            double initialVelocity,
            double visibilityThreshold,
            M3SpringParameters spring
    ) {
        if (!Double.isFinite(visibilityThreshold) || visibilityThreshold <= 0.0) {
            throw new IllegalArgumentException("visibilityThreshold must be finite and greater than zero");
        }
        if (!Double.isFinite(initialDisplacement) || !Double.isFinite(initialVelocity)) {
            return Double.NaN;
        }
        if (initialDisplacement == 0.0 && initialVelocity == 0.0) {
            return 0.0;
        }

        double dampingRatio = spring.dampingRatio();
        double naturalFrequency = Math.sqrt(spring.stiffness());
        double displacement = Math.abs(initialDisplacement);
        double velocity = initialDisplacement < 0.0 ? -initialVelocity : initialVelocity;
        double duration;

        if (dampingRatio < 1.0 - CRITICAL_DAMPING_TOLERANCE) {
            duration = estimateUnderDampedDuration(
                    naturalFrequency, dampingRatio, displacement, velocity, visibilityThreshold
            );
        } else if (dampingRatio > 1.0 + CRITICAL_DAMPING_TOLERANCE) {
            duration = estimateOverDampedDuration(
                    naturalFrequency, dampingRatio, displacement, velocity, visibilityThreshold
            );
        } else {
            duration = estimateCriticallyDampedDuration(
                    naturalFrequency, displacement, velocity, visibilityThreshold
            );
        }

        return Double.isFinite(duration) ? Math.max(0.0, duration) : Double.NaN;
    }

    /// Estimates an underdamped spring's final crossing from its exponentially decaying envelope.
    private static double estimateUnderDampedDuration(
            double naturalFrequency,
            double dampingRatio,
            double initialDisplacement,
            double initialVelocity,
            double visibilityThreshold
    ) {
        double realRate = -dampingRatio * naturalFrequency;
        double imaginaryRate = naturalFrequency * Math.sqrt(1.0 - dampingRatio * dampingRatio);
        double secondCoefficient = (initialVelocity - realRate * initialDisplacement) / imaginaryRate;
        double envelope = Math.hypot(initialDisplacement, secondCoefficient);
        return Math.log(visibilityThreshold / envelope) / realRate;
    }

    /// Estimates a critically damped spring's final threshold crossing with Newton's method.
    private static double estimateCriticallyDampedDuration(
            double naturalFrequency,
            double initialDisplacement,
            double initialVelocity,
            double visibilityThreshold
    ) {
        double rate = -naturalFrequency;
        double secondCoefficient = initialVelocity - rate * initialDisplacement;

        double firstEstimate = Math.log(Math.abs(visibilityThreshold / initialDisplacement)) / rate;
        double secondGuess = Math.log(Math.abs(visibilityThreshold / secondCoefficient));
        double secondEstimate = secondGuess;
        for (int iteration = 0; iteration < 6; iteration++) {
            secondEstimate = secondGuess - Math.log(Math.abs(secondEstimate / rate));
        }
        secondEstimate /= rate;

        double currentTime = laterFiniteEstimate(firstEstimate, secondEstimate);
        double inflectionTime = -(rate * initialDisplacement + secondCoefficient) / (rate * secondCoefficient);
        double inflectionValue = (initialDisplacement + secondCoefficient * inflectionTime)
                * Math.exp(rate * inflectionTime);
        double signedThreshold;
        if (Double.isNaN(inflectionTime) || inflectionTime <= DURATION_CONVERGENCE_SECONDS) {
            signedThreshold = -visibilityThreshold;
        } else if (-inflectionValue < visibilityThreshold) {
            if (secondCoefficient < 0.0 && initialDisplacement > 0.0) {
                currentTime = 0.0;
            }
            signedThreshold = -visibilityThreshold;
        } else {
            currentTime = -(2.0 / rate) - initialDisplacement / secondCoefficient;
            signedThreshold = visibilityThreshold;
        }

        for (int iteration = 0; iteration < MAX_DURATION_ITERATIONS; iteration++) {
            double exponential = Math.exp(rate * currentTime);
            double value = (initialDisplacement + secondCoefficient * currentTime) * exponential + signedThreshold;
            double derivative = (secondCoefficient * (rate * currentTime + 1.0) + initialDisplacement * rate)
                    * exponential;
            double nextTime = currentTime - value / derivative;
            if (!Double.isFinite(nextTime)) {
                return Double.NaN;
            }
            if (Math.abs(nextTime - currentTime) <= DURATION_CONVERGENCE_SECONDS) {
                return nextTime;
            }
            currentTime = nextTime;
        }
        return currentTime;
    }

    /// Estimates an overdamped spring's final threshold crossing with Newton's method.
    private static double estimateOverDampedDuration(
            double naturalFrequency,
            double dampingRatio,
            double initialDisplacement,
            double initialVelocity,
            double visibilityThreshold
    ) {
        double root = Math.sqrt(dampingRatio * dampingRatio - 1.0);
        double firstRate = -naturalFrequency * (dampingRatio - root);
        double secondRate = -naturalFrequency * (dampingRatio + root);
        double secondCoefficient =
                (firstRate * initialDisplacement - initialVelocity) / (firstRate - secondRate);
        double firstCoefficient = initialDisplacement - secondCoefficient;

        double firstEstimate = Math.log(Math.abs(visibilityThreshold / firstCoefficient)) / firstRate;
        double secondEstimate = Math.log(Math.abs(visibilityThreshold / secondCoefficient)) / secondRate;
        double currentTime = laterFiniteEstimate(firstEstimate, secondEstimate);

        double inflectionTime = Math.log(
                firstCoefficient * firstRate / (-secondCoefficient * secondRate)
        ) / (secondRate - firstRate);
        double inflectionValue = firstCoefficient * Math.exp(firstRate * inflectionTime)
                + secondCoefficient * Math.exp(secondRate * inflectionTime);
        double signedThreshold;
        if (Double.isNaN(inflectionTime) || inflectionTime <= DURATION_CONVERGENCE_SECONDS) {
            signedThreshold = -visibilityThreshold;
        } else if (-inflectionValue < visibilityThreshold) {
            if (secondCoefficient < 0.0 && firstCoefficient > 0.0) {
                currentTime = 0.0;
            }
            signedThreshold = -visibilityThreshold;
        } else {
            currentTime = Math.log(
                    -(secondCoefficient * secondRate * secondRate)
                            / (firstCoefficient * firstRate * firstRate)
            ) / (firstRate - secondRate);
            signedThreshold = visibilityThreshold;
        }

        for (int iteration = 0; iteration < MAX_DURATION_ITERATIONS; iteration++) {
            double firstExponential = Math.exp(firstRate * currentTime);
            double secondExponential = Math.exp(secondRate * currentTime);
            double value = firstCoefficient * firstExponential
                    + secondCoefficient * secondExponential
                    + signedThreshold;
            if (Math.abs(value) < 1.0e-4) {
                return currentTime;
            }
            double derivative = firstCoefficient * firstRate * firstExponential
                    + secondCoefficient * secondRate * secondExponential;
            double nextTime = currentTime - value / derivative;
            if (!Double.isFinite(nextTime)) {
                return Double.NaN;
            }
            if (Math.abs(nextTime - currentTime) <= DURATION_CONVERGENCE_SECONDS) {
                return nextTime;
            }
            currentTime = nextTime;
        }
        return currentTime;
    }

    /// Returns the later finite estimate, or `NaN` when neither estimate is finite.
    private static double laterFiniteEstimate(double first, double second) {
        if (!Double.isFinite(first)) {
            return Double.isFinite(second) ? second : Double.NaN;
        }
        if (!Double.isFinite(second)) {
            return first;
        }
        return Math.max(first, second);
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
