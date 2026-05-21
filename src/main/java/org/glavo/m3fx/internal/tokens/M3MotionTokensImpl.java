// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.tokens.M3MotionTokens;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3MotionTokens].
///
/// @param short1 the short1 duration token
/// @param short2 the short2 duration token
/// @param short3 the short3 duration token
/// @param short4 the short4 duration token
/// @param medium1 the medium1 duration token
/// @param medium2 the medium2 duration token
/// @param medium3 the medium3 duration token
/// @param medium4 the medium4 duration token
/// @param long1 the long1 duration token
/// @param long2 the long2 duration token
/// @param long3 the long3 duration token
/// @param long4 the long4 duration token
/// @param extraLong1 the extraLong1 duration token
/// @param extraLong2 the extraLong2 duration token
/// @param extraLong3 the extraLong3 duration token
/// @param extraLong4 the extraLong4 duration token
/// @param scheme the semantic motion scheme
/// @param behavior the motion-adjacent interaction timings
@NotNullByDefault
public record M3MotionTokensImpl(
        int short1,
        int short2,
        int short3,
        int short4,
        int medium1,
        int medium2,
        int medium3,
        int medium4,
        int long1,
        int long2,
        int long3,
        int long4,
        int extraLong1,
        int extraLong2,
        int extraLong3,
        int extraLong4,
        M3MotionScheme scheme,
        M3MotionBehavior behavior
) implements M3MotionTokens {
    /// Creates motion tokens.
    public M3MotionTokensImpl {
        validate(short1, "short1");
        validate(short2, "short2");
        validate(short3, "short3");
        validate(short4, "short4");
        validate(medium1, "medium1");
        validate(medium2, "medium2");
        validate(medium3, "medium3");
        validate(medium4, "medium4");
        validate(long1, "long1");
        validate(long2, "long2");
        validate(long3, "long3");
        validate(long4, "long4");
        validate(extraLong1, "extraLong1");
        validate(extraLong2, "extraLong2");
        validate(extraLong3, "extraLong3");
        validate(extraLong4, "extraLong4");
        Objects.requireNonNull(scheme, "scheme");
        Objects.requireNonNull(behavior, "behavior");
    }

    /// Validates a duration token.
    private static void validate(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
