// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.glavo.m3fx.internal.animation.M3MotionSchemeImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds an immutable [M3MotionScheme] by replacing semantic motion roles.
///
/// A builder is always initialized from a complete scheme. Each replacement method validates its argument before
/// changing the builder and returns this builder for method chaining. [build] creates an independent immutable
/// snapshot; subsequent changes to the builder do not modify previously built schemes. Builders may be reused but
/// are not thread-safe.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3MotionSchemeBuilder {
    /// The fast effects motion spec.
    private M3MotionSpec fastEffects;

    /// The default effects motion spec.
    private M3MotionSpec defaultEffects;

    /// The slow effects motion spec.
    private M3MotionSpec slowEffects;

    /// The fast spatial motion spec.
    private M3MotionSpec fastSpatial;

    /// The default spatial motion spec.
    private M3MotionSpec defaultSpatial;

    /// The slow spatial motion spec.
    private M3MotionSpec slowSpatial;

    /// Creates a builder initialized from an existing scheme.
    ///
    /// @param scheme the scheme to copy
    M3MotionSchemeBuilder(M3MotionScheme scheme) {
        M3MotionScheme source = Objects.requireNonNull(scheme, "scheme");
        fastEffects = source.fastEffects();
        defaultEffects = source.defaultEffects();
        slowEffects = source.slowEffects();
        fastSpatial = source.fastSpatial();
        defaultSpatial = source.defaultSpatial();
        slowSpatial = source.slowSpatial();
    }

    /// Replaces the fast effects motion spec.
    ///
    /// @param spec the replacement spec
    /// @return this builder
    /// @throws NullPointerException if `spec` is `null`
    public M3MotionSchemeBuilder fastEffects(M3MotionSpec spec) {
        fastEffects = Objects.requireNonNull(spec, "spec");
        return this;
    }

    /// Replaces the default effects motion spec.
    ///
    /// @param spec the replacement spec
    /// @return this builder
    /// @throws NullPointerException if `spec` is `null`
    public M3MotionSchemeBuilder defaultEffects(M3MotionSpec spec) {
        defaultEffects = Objects.requireNonNull(spec, "spec");
        return this;
    }

    /// Replaces the slow effects motion spec.
    ///
    /// @param spec the replacement spec
    /// @return this builder
    /// @throws NullPointerException if `spec` is `null`
    public M3MotionSchemeBuilder slowEffects(M3MotionSpec spec) {
        slowEffects = Objects.requireNonNull(spec, "spec");
        return this;
    }

    /// Replaces the fast spatial motion spec.
    ///
    /// @param spec the replacement spec
    /// @return this builder
    /// @throws NullPointerException if `spec` is `null`
    public M3MotionSchemeBuilder fastSpatial(M3MotionSpec spec) {
        fastSpatial = Objects.requireNonNull(spec, "spec");
        return this;
    }

    /// Replaces the default spatial motion spec.
    ///
    /// @param spec the replacement spec
    /// @return this builder
    /// @throws NullPointerException if `spec` is `null`
    public M3MotionSchemeBuilder defaultSpatial(M3MotionSpec spec) {
        defaultSpatial = Objects.requireNonNull(spec, "spec");
        return this;
    }

    /// Replaces the slow spatial motion spec.
    ///
    /// @param spec the replacement spec
    /// @return this builder
    /// @throws NullPointerException if `spec` is `null`
    public M3MotionSchemeBuilder slowSpatial(M3MotionSpec spec) {
        slowSpatial = Objects.requireNonNull(spec, "spec");
        return this;
    }

    /// Creates an immutable snapshot of the current role assignments.
    ///
    /// @return a new immutable motion scheme; never `null`
    public M3MotionScheme build() {
        return new M3MotionSchemeImpl(
                fastEffects,
                defaultEffects,
                slowEffects,
                fastSpatial,
                defaultSpatial,
                slowSpatial
        );
    }
}
