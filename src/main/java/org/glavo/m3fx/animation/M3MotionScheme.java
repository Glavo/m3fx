// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3MotionSchemeImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Groups Material Design 3 motion specifications by semantic role.
///
/// Effects roles are intended for transitions such as opacity and color changes that do not move content through
/// space. Spatial roles are intended for position, size, scale, and shape transitions. Within each family, the
/// fast, default, and slow roles allow a control to select a duration according to the scope and prominence of the
/// change without depending on profile-specific numeric values.
///
/// A scheme is immutable and may be shared. [standard] and [expressive] provide complete spring-based schemes;
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
                spring(1.0, 3800.0, M3Motion.SHORT3, M3MotionEasing.FAST_EFFECTS),
                spring(1.0, 1600.0, M3Motion.SHORT4, M3MotionEasing.DEFAULT_EFFECTS),
                spring(1.0, 800.0, M3Motion.MEDIUM2, M3MotionEasing.SLOW_EFFECTS),
                spring(0.9, 1400.0, M3Motion.MEDIUM3, M3MotionEasing.STANDARD_SPATIAL),
                spring(0.9, 700.0, M3Motion.LONG2, M3MotionEasing.STANDARD_SPATIAL),
                spring(0.9, 300.0, Duration.millis(750.0), M3MotionEasing.STANDARD_SPATIAL)
        );
    }

    /// Returns a complete Material Design 3 Expressive motion scheme.
    ///
    /// @return the expressive M3FX motion scheme
    static M3MotionScheme expressive() {
        return new M3MotionSchemeImpl(
                spring(1.0, 3800.0, M3Motion.SHORT3, M3MotionEasing.FAST_EFFECTS),
                spring(1.0, 1600.0, M3Motion.SHORT4, M3MotionEasing.DEFAULT_EFFECTS),
                spring(1.0, 800.0, M3Motion.MEDIUM2, M3MotionEasing.SLOW_EFFECTS),
                spring(0.6, 800.0, M3Motion.MEDIUM3, M3MotionEasing.EXPRESSIVE_FAST_SPATIAL),
                spring(0.8, 380.0, M3Motion.LONG2, M3MotionEasing.EXPRESSIVE_DEFAULT_SPATIAL),
                spring(0.8, 200.0, Duration.millis(650.0), M3MotionEasing.EXPRESSIVE_SLOW_SPATIAL)
        );
    }

    /// Creates one built-in spring role with its finite cross-platform fallback.
    private static M3MotionSpec spring(
            double dampingRatio,
            double stiffness,
            Duration settlingDuration,
            M3MotionEasing fallbackEasing
    ) {
        return M3MotionSpec.spring(
                new M3SpringParameters(dampingRatio, stiffness),
                settlingDuration,
                fallbackEasing
        );
    }
}
