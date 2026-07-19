// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.glavo.m3fx.internal.animation.M3MotionSchemeImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Groups Material Design 3 motion specifications by semantic role.
///
/// Effects roles are intended for transitions such as opacity and color changes that do not move content through
/// space. Spatial roles are intended for position, size, scale, and shape transitions. Within each family, the
/// fast, default, and slow roles allow a control to select a duration according to the scope and prominence of the
/// change without depending on profile-specific numeric values.
///
/// A scheme is immutable and may be shared. [standard] and [expressive] provide complete built-in schemes;
/// [builder][#builder()] creates a mutable copy for replacing individual roles.
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

    /// Creates a builder initialized with all roles from [standard].
    ///
    /// @return a mutable motion scheme builder
    static M3MotionSchemeBuilder builder() {
        return new M3MotionSchemeBuilder(standard());
    }

    /// Creates a builder initialized with all roles from an existing scheme.
    ///
    /// @param scheme the scheme to copy
    /// @return a mutable motion scheme builder
    /// @throws NullPointerException if `scheme` is `null`
    static M3MotionSchemeBuilder builder(M3MotionScheme scheme) {
        return new M3MotionSchemeBuilder(scheme);
    }

    /// Returns a complete Standard Material motion scheme.
    ///
    /// @return the baseline M3FX motion scheme
    static M3MotionScheme standard() {
        return new M3MotionSchemeImpl(
                M3MotionSpec.of(M3Motion.SHORT1, M3MotionEasing.STANDARD),
                M3MotionSpec.of(M3Motion.SHORT4, M3MotionEasing.STANDARD),
                M3MotionSpec.of(M3Motion.MEDIUM2, M3MotionEasing.STANDARD),
                M3MotionSpec.of(M3Motion.SHORT3, M3MotionEasing.STANDARD),
                M3MotionSpec.of(M3Motion.MEDIUM3, M3MotionEasing.STANDARD),
                M3MotionSpec.of(M3Motion.LONG2, M3MotionEasing.STANDARD)
        );
    }

    /// Returns a complete Material Design 3 Expressive motion scheme.
    ///
    /// @return the expressive M3FX motion scheme
    static M3MotionScheme expressive() {
        return new M3MotionSchemeImpl(
                M3MotionSpec.of(M3Motion.SHORT3, M3MotionEasing.EMPHASIZED),
                M3MotionSpec.of(M3Motion.MEDIUM1, M3MotionEasing.EMPHASIZED),
                M3MotionSpec.of(M3Motion.MEDIUM3, M3MotionEasing.EMPHASIZED),
                M3MotionSpec.of(M3Motion.MEDIUM1, M3MotionEasing.EMPHASIZED_DECELERATE),
                M3MotionSpec.of(M3Motion.MEDIUM4, M3MotionEasing.EMPHASIZED),
                M3MotionSpec.of(M3Motion.LONG3, M3MotionEasing.EMPHASIZED)
        );
    }
}
