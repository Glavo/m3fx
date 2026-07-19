// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3MotionScheme].
///
/// @param fastEffects    the fast effects motion spec
/// @param defaultEffects the default effects motion spec
/// @param slowEffects    the slow effects motion spec
/// @param fastSpatial    the fast spatial motion spec
/// @param defaultSpatial the default spatial motion spec
/// @param slowSpatial    the slow spatial motion spec
@NotNullByDefault
public record M3MotionSchemeImpl(
        M3MotionSpec fastEffects,
        M3MotionSpec defaultEffects,
        M3MotionSpec slowEffects,
        M3MotionSpec fastSpatial,
        M3MotionSpec defaultSpatial,
        M3MotionSpec slowSpatial
) implements M3MotionScheme {
    /// Creates a motion scheme.
    ///
    /// @throws NullPointerException if any motion specification is `null`
    public M3MotionSchemeImpl {
        Objects.requireNonNull(fastEffects, "fastEffects");
        Objects.requireNonNull(defaultEffects, "defaultEffects");
        Objects.requireNonNull(slowEffects, "slowEffects");
        Objects.requireNonNull(fastSpatial, "fastSpatial");
        Objects.requireNonNull(defaultSpatial, "defaultSpatial");
        Objects.requireNonNull(slowSpatial, "slowSpatial");
    }
}
