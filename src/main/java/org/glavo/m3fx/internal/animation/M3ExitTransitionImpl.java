// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3ExitTransition;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3TransitionEdge;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/// Default immutable implementation of [M3ExitTransition].
///
/// @param effects the immutable, channel-unique effect list
@NotNullByDefault
public record M3ExitTransitionImpl(
        @Unmodifiable List<M3TransitionEffect> effects
) implements M3ExitTransition {
    /// The shared empty exit transition.
    public static final M3ExitTransitionImpl NONE = new M3ExitTransitionImpl(List.of());

    /// Creates an immutable transition from a validated copy of the supplied effects.
    ///
    /// @throws NullPointerException     if the list or an effect is `null`
    /// @throws IllegalArgumentException if more than one effect controls the same visual channel
    public M3ExitTransitionImpl {
        effects = M3TransitionEffects.copyOf(effects);
    }

    /// Creates one fade effect.
    public static M3ExitTransition fade(double targetOpacity) {
        return new M3ExitTransitionImpl(List.of(new M3TransitionEffect(
                M3TransitionEffectKind.FADE,
                targetOpacity,
                null,
                null,
                Duration.ZERO
        )));
    }

    /// Creates one scale effect.
    public static M3ExitTransition scale(double targetScale) {
        return new M3ExitTransitionImpl(List.of(new M3TransitionEffect(
                M3TransitionEffectKind.SCALE,
                targetScale,
                null,
                null,
                Duration.ZERO
        )));
    }

    /// Creates one logical slide effect.
    public static M3ExitTransition slide(M3TransitionEdge edge, double distance) {
        return new M3ExitTransitionImpl(List.of(new M3TransitionEffect(
                M3TransitionEffectKind.SLIDE,
                distance,
                Objects.requireNonNull(edge, "edge"),
                null,
                Duration.ZERO
        )));
    }

    /// Combines two exit transitions.
    public static M3ExitTransition combine(M3ExitTransition first, M3ExitTransition second) {
        M3ExitTransitionImpl firstImpl = implementation(first);
        M3ExitTransitionImpl secondImpl = implementation(second);
        if (firstImpl.effects.isEmpty()) {
            return secondImpl;
        }
        if (secondImpl.effects.isEmpty()) {
            return firstImpl;
        }
        return new M3ExitTransitionImpl(M3TransitionEffects.combine(firstImpl.effects, secondImpl.effects));
    }

    /// Copies an exit transition with one explicit motion specification.
    public static M3ExitTransition withMotionSpec(
            M3ExitTransition transition,
            @Nullable M3MotionSpec motionSpec
    ) {
        M3ExitTransitionImpl implementation = implementation(transition);
        if (implementation.effects.isEmpty()) {
            return implementation;
        }
        return new M3ExitTransitionImpl(
                M3TransitionEffects.withMotionSpec(implementation.effects, motionSpec)
        );
    }

    /// Copies an exit transition with one delay.
    public static M3ExitTransition withDelay(M3ExitTransition transition, Duration delay) {
        M3ExitTransitionImpl implementation = implementation(transition);
        if (implementation.effects.isEmpty()) {
            Objects.requireNonNull(delay, "delay");
            return implementation;
        }
        return new M3ExitTransitionImpl(M3TransitionEffects.withDelay(implementation.effects, delay));
    }

    /// Returns the only permitted implementation after checking the public argument.
    private static M3ExitTransitionImpl implementation(M3ExitTransition transition) {
        return (M3ExitTransitionImpl) Objects.requireNonNull(transition, "transition");
    }
}
