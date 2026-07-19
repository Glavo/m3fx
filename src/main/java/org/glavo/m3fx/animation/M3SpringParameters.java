// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the physical parameters of a unit-mass spring animation.
///
/// The damping ratio controls oscillation around a target. A value below `1.0` is underdamped and may overshoot,
/// `1.0` is critically damped, and a value above `1.0` is overdamped. Stiffness controls how quickly the spring
/// accelerates toward its target. Both values are independent of the animated property's unit.
///
/// Instances are immutable and may be shared between motion specifications.
///
/// @param dampingRatio the finite, positive damping ratio
/// @param stiffness    the finite, positive spring stiffness
///
/// See [Material Design motion physics](https://m3.material.io/styles/motion/overview/specs).
@NotNullByDefault
public record M3SpringParameters(double dampingRatio, double stiffness) {
    /// Creates validated spring parameters.
    ///
    /// @throws IllegalArgumentException if either value is non-finite or not greater than zero
    public M3SpringParameters {
        if (!Double.isFinite(dampingRatio) || dampingRatio <= 0.0) {
            throw new IllegalArgumentException("dampingRatio must be finite and greater than zero");
        }
        if (!Double.isFinite(stiffness) || stiffness <= 0.0) {
            throw new IllegalArgumentException("stiffness must be finite and greater than zero");
        }
    }
}
