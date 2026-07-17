// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.tokens.M3MotionTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Holds Material Design 3 motion duration tokens in milliseconds.
///
/// Motion tokens provide the duration ladder, semantic [M3MotionScheme], and [M3MotionBehavior] used by M3FX
/// controls for state feedback, popup transitions, smooth scrolling, and progress animation. Baseline and
/// expressive profiles can provide different durations and curves while sharing the same public API.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3MotionTokens permits M3MotionTokensImpl {
    /// Returns the short1 duration token.
    ///
    /// @return the short1 duration token in milliseconds
    int short1();

    /// Returns the short2 duration token.
    ///
    /// @return the short2 duration token in milliseconds
    int short2();

    /// Returns the short3 duration token.
    ///
    /// @return the short3 duration token in milliseconds
    int short3();

    /// Returns the short4 duration token.
    ///
    /// @return the short4 duration token in milliseconds
    int short4();

    /// Returns the medium1 duration token.
    ///
    /// @return the medium1 duration token in milliseconds
    int medium1();

    /// Returns the medium2 duration token.
    ///
    /// @return the medium2 duration token in milliseconds
    int medium2();

    /// Returns the medium3 duration token.
    ///
    /// @return the medium3 duration token in milliseconds
    int medium3();

    /// Returns the medium4 duration token.
    ///
    /// @return the medium4 duration token in milliseconds
    int medium4();

    /// Returns the long1 duration token.
    ///
    /// @return the long1 duration token in milliseconds
    int long1();

    /// Returns the long2 duration token.
    ///
    /// @return the long2 duration token in milliseconds
    int long2();

    /// Returns the long3 duration token.
    ///
    /// @return the long3 duration token in milliseconds
    int long3();

    /// Returns the long4 duration token.
    ///
    /// @return the long4 duration token in milliseconds
    int long4();

    /// Returns the extraLong1 duration token.
    ///
    /// @return the extraLong1 duration token in milliseconds
    int extraLong1();

    /// Returns the extraLong2 duration token.
    ///
    /// @return the extraLong2 duration token in milliseconds
    int extraLong2();

    /// Returns the extraLong3 duration token.
    ///
    /// @return the extraLong3 duration token in milliseconds
    int extraLong3();

    /// Returns the extraLong4 duration token.
    ///
    /// @return the extraLong4 duration token in milliseconds
    int extraLong4();

    /// Returns the semantic motion scheme used by this profile.
    ///
    /// @return the semantic motion scheme used by this profile
    M3MotionScheme scheme();

    /// Returns the motion-adjacent interaction timings used by this profile.
    ///
    /// @return the motion-adjacent interaction timings used by this profile
    M3MotionBehavior behavior();

    /// Returns the fast effects motion spec.
    ///
    /// @return the fast effects motion spec
    default M3MotionSpec fastEffects() {
        return scheme().fastEffects();
    }

    /// Returns the default effects motion spec.
    ///
    /// @return the default effects motion spec
    default M3MotionSpec defaultEffects() {
        return scheme().defaultEffects();
    }

    /// Returns the slow effects motion spec.
    ///
    /// @return the slow effects motion spec
    default M3MotionSpec slowEffects() {
        return scheme().slowEffects();
    }

    /// Returns the fast spatial motion spec.
    ///
    /// @return the fast spatial motion spec
    default M3MotionSpec fastSpatial() {
        return scheme().fastSpatial();
    }

    /// Returns the default spatial motion spec.
    ///
    /// @return the default spatial motion spec
    default M3MotionSpec defaultSpatial() {
        return scheme().defaultSpatial();
    }

    /// Returns the slow spatial motion spec.
    ///
    /// @return the slow spatial motion spec
    default M3MotionSpec slowSpatial() {
        return scheme().slowSpatial();
    }

    /// Creates a builder initialized with baseline motion tokens.
    ///
    /// @return a mutable motion-token builder
    static M3MotionTokensBuilder builder() {
        return new M3MotionTokensBuilder(baseline());
    }

    /// Creates a builder initialized from an existing motion token set.
    ///
    /// @param tokens the motion tokens to copy
    /// @return a mutable motion-token builder
    /// @throws NullPointerException if `tokens` is `null`
    static M3MotionTokensBuilder builder(M3MotionTokens tokens) {
        return new M3MotionTokensBuilder(tokens);
    }

    /// Creates the standard duration ladder with the supplied semantic motion values.
    ///
    /// @param scheme the semantic motion scheme
    /// @param behavior the motion-adjacent interaction timings
    /// @return immutable motion tokens
    private static M3MotionTokens defaultTokens(
            M3MotionScheme scheme,
            M3MotionBehavior behavior
    ) {
        Objects.requireNonNull(scheme, "scheme");
        Objects.requireNonNull(behavior, "behavior");
        return new M3MotionTokensImpl(
                50,
                100,
                150,
                200,
                250,
                300,
                350,
                400,
                450,
                500,
                550,
                600,
                700,
                800,
                900,
                1000,
                scheme,
                behavior
        );
    }

    /// Returns baseline Material Design 3 motion tokens.
    ///
    /// @return baseline Material Design 3 motion tokens
    static M3MotionTokens baseline() {
        return defaultTokens(M3MotionScheme.standard(), M3MotionBehavior.standard());
    }

    /// Returns expressive Material Design 3 motion tokens.
    ///
    /// @return expressive Material Design 3 motion tokens
    static M3MotionTokens expressive() {
        return defaultTokens(
                M3MotionScheme.expressive(),
                M3MotionBehavior.expressive()
        );
    }

}
