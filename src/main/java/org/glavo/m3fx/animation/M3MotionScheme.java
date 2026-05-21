// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.glavo.m3fx.internal.animation.M3MotionSchemeImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Groups semantic Material Design 3 motion specs for a theme profile.
///
/// Effects specs are intended for opacity, color, and lightweight state feedback. Spatial specs are intended for
/// movement, scale, size, and other transitions that change where a user perceives content to be. Themes expose a
/// scheme so controls can select motion semantically and still switch between baseline and expressive profiles.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3MotionScheme permits M3MotionSchemeImpl {
    /// Returns the fast effects motion spec.
    ///
    /// @return the fast effects motion spec
    M3MotionSpec fastEffects();

    /// Returns the default effects motion spec.
    ///
    /// @return the default effects motion spec
    M3MotionSpec defaultEffects();

    /// Returns the slow effects motion spec.
    ///
    /// @return the slow effects motion spec
    M3MotionSpec slowEffects();

    /// Returns the fast spatial motion spec.
    ///
    /// @return the fast spatial motion spec
    M3MotionSpec fastSpatial();

    /// Returns the default spatial motion spec.
    ///
    /// @return the default spatial motion spec
    M3MotionSpec defaultSpatial();

    /// Returns the slow spatial motion spec.
    ///
    /// @return the slow spatial motion spec
    M3MotionSpec slowSpatial();

    /// Creates a motion scheme from explicit specs.
    ///
    /// @param fastEffects the fast effects spec
    /// @param defaultEffects the default effects spec
    /// @param slowEffects the slow effects spec
    /// @param fastSpatial the fast spatial spec
    /// @param defaultSpatial the default spatial spec
    /// @param slowSpatial the slow spatial spec
    /// @return an immutable motion scheme
    static M3MotionScheme create(
            M3MotionSpec fastEffects,
            M3MotionSpec defaultEffects,
            M3MotionSpec slowEffects,
            M3MotionSpec fastSpatial,
            M3MotionSpec defaultSpatial,
            M3MotionSpec slowSpatial
    ) {
        return new M3MotionSchemeImpl(
                fastEffects,
                defaultEffects,
                slowEffects,
                fastSpatial,
                defaultSpatial,
                slowSpatial
        );
    }

    /// Returns the standard Material motion scheme for recurring utility interactions.
    ///
    /// @return the baseline M3FX motion scheme
    static M3MotionScheme standard() {
        return create(
                M3MotionSpec.create(M3Motion.SHORT1, M3MotionEasing.STANDARD),
                M3MotionSpec.create(M3Motion.SHORT4, M3MotionEasing.STANDARD),
                M3MotionSpec.create(M3Motion.MEDIUM2, M3MotionEasing.STANDARD),
                M3MotionSpec.create(M3Motion.SHORT3, M3MotionEasing.STANDARD),
                M3MotionSpec.create(M3Motion.MEDIUM3, M3MotionEasing.STANDARD),
                M3MotionSpec.create(M3Motion.LONG2, M3MotionEasing.STANDARD)
        );
    }

    /// Returns the expressive Material motion scheme for prominent and spatial interactions.
    ///
    /// @return the expressive M3FX motion scheme
    static M3MotionScheme expressive() {
        return create(
                M3MotionSpec.create(M3Motion.SHORT3, M3MotionEasing.EMPHASIZED),
                M3MotionSpec.create(M3Motion.MEDIUM1, M3MotionEasing.EMPHASIZED),
                M3MotionSpec.create(M3Motion.MEDIUM3, M3MotionEasing.EMPHASIZED),
                M3MotionSpec.create(M3Motion.MEDIUM1, M3MotionEasing.EMPHASIZED_DECELERATE),
                M3MotionSpec.create(M3Motion.MEDIUM4, M3MotionEasing.EMPHASIZED),
                M3MotionSpec.create(M3Motion.LONG3, M3MotionEasing.EMPHASIZED)
        );
    }
}
