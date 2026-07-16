// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.internal.tokens.M3MotionTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds immutable [M3MotionTokens] from named duration, scheme, and behavior values.
@NotNullByDefault
public final class M3MotionTokensBuilder {
    /// The current short1 duration in milliseconds.
    private int short1;

    /// The current short2 duration in milliseconds.
    private int short2;

    /// The current short3 duration in milliseconds.
    private int short3;

    /// The current short4 duration in milliseconds.
    private int short4;

    /// The current medium1 duration in milliseconds.
    private int medium1;

    /// The current medium2 duration in milliseconds.
    private int medium2;

    /// The current medium3 duration in milliseconds.
    private int medium3;

    /// The current medium4 duration in milliseconds.
    private int medium4;

    /// The current long1 duration in milliseconds.
    private int long1;

    /// The current long2 duration in milliseconds.
    private int long2;

    /// The current long3 duration in milliseconds.
    private int long3;

    /// The current long4 duration in milliseconds.
    private int long4;

    /// The current extraLong1 duration in milliseconds.
    private int extraLong1;

    /// The current extraLong2 duration in milliseconds.
    private int extraLong2;

    /// The current extraLong3 duration in milliseconds.
    private int extraLong3;

    /// The current extraLong4 duration in milliseconds.
    private int extraLong4;

    /// The current semantic motion scheme.
    private M3MotionScheme scheme;

    /// The current motion-adjacent behavior timings.
    private M3MotionBehavior behavior;

    /// Creates a builder initialized from an existing motion token set.
    ///
    /// @param tokens the motion tokens to copy
    M3MotionTokensBuilder(M3MotionTokens tokens) {
        M3MotionTokens source = Objects.requireNonNull(tokens, "tokens");
        short1 = source.short1();
        short2 = source.short2();
        short3 = source.short3();
        short4 = source.short4();
        medium1 = source.medium1();
        medium2 = source.medium2();
        medium3 = source.medium3();
        medium4 = source.medium4();
        long1 = source.long1();
        long2 = source.long2();
        long3 = source.long3();
        long4 = source.long4();
        extraLong1 = source.extraLong1();
        extraLong2 = source.extraLong2();
        extraLong3 = source.extraLong3();
        extraLong4 = source.extraLong4();
        scheme = source.scheme();
        behavior = source.behavior();
    }

    /// Replaces the short1 duration.
    ///
    /// @param short1 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder short1(int short1) {
        this.short1 = nonNegativeDuration(short1, "short1");
        return this;
    }

    /// Replaces the short2 duration.
    ///
    /// @param short2 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder short2(int short2) {
        this.short2 = nonNegativeDuration(short2, "short2");
        return this;
    }

    /// Replaces the short3 duration.
    ///
    /// @param short3 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder short3(int short3) {
        this.short3 = nonNegativeDuration(short3, "short3");
        return this;
    }

    /// Replaces the short4 duration.
    ///
    /// @param short4 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder short4(int short4) {
        this.short4 = nonNegativeDuration(short4, "short4");
        return this;
    }

    /// Replaces the medium1 duration.
    ///
    /// @param medium1 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder medium1(int medium1) {
        this.medium1 = nonNegativeDuration(medium1, "medium1");
        return this;
    }

    /// Replaces the medium2 duration.
    ///
    /// @param medium2 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder medium2(int medium2) {
        this.medium2 = nonNegativeDuration(medium2, "medium2");
        return this;
    }

    /// Replaces the medium3 duration.
    ///
    /// @param medium3 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder medium3(int medium3) {
        this.medium3 = nonNegativeDuration(medium3, "medium3");
        return this;
    }

    /// Replaces the medium4 duration.
    ///
    /// @param medium4 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder medium4(int medium4) {
        this.medium4 = nonNegativeDuration(medium4, "medium4");
        return this;
    }

    /// Replaces the long1 duration.
    ///
    /// @param long1 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder long1(int long1) {
        this.long1 = nonNegativeDuration(long1, "long1");
        return this;
    }

    /// Replaces the long2 duration.
    ///
    /// @param long2 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder long2(int long2) {
        this.long2 = nonNegativeDuration(long2, "long2");
        return this;
    }

    /// Replaces the long3 duration.
    ///
    /// @param long3 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder long3(int long3) {
        this.long3 = nonNegativeDuration(long3, "long3");
        return this;
    }

    /// Replaces the long4 duration.
    ///
    /// @param long4 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder long4(int long4) {
        this.long4 = nonNegativeDuration(long4, "long4");
        return this;
    }

    /// Replaces the extraLong1 duration.
    ///
    /// @param extraLong1 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder extraLong1(int extraLong1) {
        this.extraLong1 = nonNegativeDuration(extraLong1, "extraLong1");
        return this;
    }

    /// Replaces the extraLong2 duration.
    ///
    /// @param extraLong2 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder extraLong2(int extraLong2) {
        this.extraLong2 = nonNegativeDuration(extraLong2, "extraLong2");
        return this;
    }

    /// Replaces the extraLong3 duration.
    ///
    /// @param extraLong3 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder extraLong3(int extraLong3) {
        this.extraLong3 = nonNegativeDuration(extraLong3, "extraLong3");
        return this;
    }

    /// Replaces the extraLong4 duration.
    ///
    /// @param extraLong4 the duration in milliseconds
    /// @return this builder
    public M3MotionTokensBuilder extraLong4(int extraLong4) {
        this.extraLong4 = nonNegativeDuration(extraLong4, "extraLong4");
        return this;
    }

    /// Replaces the semantic motion scheme.
    ///
    /// @param scheme the replacement scheme
    /// @return this builder
    public M3MotionTokensBuilder scheme(M3MotionScheme scheme) {
        this.scheme = Objects.requireNonNull(scheme, "scheme");
        return this;
    }

    /// Replaces the motion-adjacent behavior timings.
    ///
    /// @param behavior the replacement behavior timings
    /// @return this builder
    public M3MotionTokensBuilder behavior(M3MotionBehavior behavior) {
        this.behavior = Objects.requireNonNull(behavior, "behavior");
        return this;
    }

    /// Creates an immutable motion token set from the current builder state.
    ///
    /// @return the built motion tokens
    public M3MotionTokens build() {
        return new M3MotionTokensImpl(
                short1,
                short2,
                short3,
                short4,
                medium1,
                medium2,
                medium3,
                medium4,
                long1,
                long2,
                long3,
                long4,
                extraLong1,
                extraLong2,
                extraLong3,
                extraLong4,
                scheme,
                behavior
        );
    }

    /// Rejects negative duration values.
    private static int nonNegativeDuration(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
        return value;
    }
}

