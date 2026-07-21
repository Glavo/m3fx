// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3EnterTransition;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3TransitionEdge;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/// Default immutable implementation of [M3EnterTransition].
///
/// @param effects the immutable, channel-unique effect list
@NotNullByDefault
public record M3EnterTransitionImpl(
        @Unmodifiable List<M3TransitionEffect> effects
) implements M3EnterTransition {
    /// The shared empty enter transition.
    public static final M3EnterTransitionImpl NONE = new M3EnterTransitionImpl(List.of());

    /// Creates an immutable transition from a validated copy of the supplied effects.
    ///
    /// @throws NullPointerException     if the list or an effect is `null`
    /// @throws IllegalArgumentException if more than one effect controls the same visual channel
    public M3EnterTransitionImpl {
        effects = M3TransitionEffects.copyOf(effects);
    }

    /// Creates one fade effect.
    public static M3EnterTransition fade(double initialOpacity) {
        return new M3EnterTransitionImpl(List.of(new M3TransitionEffect(
                M3TransitionEffectKind.FADE,
                initialOpacity,
                null,
                null,
                Duration.ZERO
        )));
    }

    /// Creates one scale effect.
    public static M3EnterTransition scale(double initialScale) {
        return new M3EnterTransitionImpl(List.of(new M3TransitionEffect(
                M3TransitionEffectKind.SCALE,
                initialScale,
                null,
                null,
                Duration.ZERO
        )));
    }

    /// Creates one logical slide effect.
    public static M3EnterTransition slide(M3TransitionEdge edge, double distance) {
        return new M3EnterTransitionImpl(List.of(new M3TransitionEffect(
                M3TransitionEffectKind.SLIDE,
                distance,
                Objects.requireNonNull(edge, "edge"),
                null,
                Duration.ZERO
        )));
    }

    /// Combines two enter transitions.
    public static M3EnterTransition combine(M3EnterTransition first, M3EnterTransition second) {
        M3EnterTransitionImpl firstImpl = implementation(first);
        M3EnterTransitionImpl secondImpl = implementation(second);
        if (firstImpl.effects.isEmpty()) {
            return secondImpl;
        }
        if (secondImpl.effects.isEmpty()) {
            return firstImpl;
        }
        return new M3EnterTransitionImpl(M3TransitionEffects.combine(firstImpl.effects, secondImpl.effects));
    }

    /// Copies an enter transition with one explicit motion specification.
    public static M3EnterTransition withMotionSpec(
            M3EnterTransition transition,
            @Nullable M3MotionSpec motionSpec
    ) {
        M3EnterTransitionImpl implementation = implementation(transition);
        if (implementation.effects.isEmpty()) {
            return implementation;
        }
        return new M3EnterTransitionImpl(
                M3TransitionEffects.withMotionSpec(implementation.effects, motionSpec)
        );
    }

    /// Copies an enter transition with one delay.
    public static M3EnterTransition withDelay(M3EnterTransition transition, Duration delay) {
        M3EnterTransitionImpl implementation = implementation(transition);
        if (implementation.effects.isEmpty()) {
            Objects.requireNonNull(delay, "delay");
            return implementation;
        }
        return new M3EnterTransitionImpl(M3TransitionEffects.withDelay(implementation.effects, delay));
    }

    /// Returns the only permitted implementation after checking the public argument.
    private static M3EnterTransitionImpl implementation(M3EnterTransition transition) {
        return (M3EnterTransitionImpl) Objects.requireNonNull(transition, "transition");
    }
}
