// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Implements immutable effect-list operations shared by enter and exit transition values.
@NotNullByDefault
final class M3TransitionEffects {
    /// Prevents instantiation.
    private M3TransitionEffects() {
    }

    /// Returns an immutable validated copy whose effect kinds are unique.
    static @Unmodifiable List<M3TransitionEffect> copyOf(List<M3TransitionEffect> effects) {
        Objects.requireNonNull(effects, "effects");
        boolean fade = false;
        boolean scale = false;
        boolean slide = false;
        for (M3TransitionEffect effect : effects) {
            switch (Objects.requireNonNull(effect, "effect").kind()) {
                case FADE -> {
                    if (fade) {
                        throw duplicate(effect.kind());
                    }
                    fade = true;
                }
                case SCALE -> {
                    if (scale) {
                        throw duplicate(effect.kind());
                    }
                    scale = true;
                }
                case SLIDE -> {
                    if (slide) {
                        throw duplicate(effect.kind());
                    }
                    slide = true;
                }
            }
        }
        return List.copyOf(effects);
    }

    /// Combines two immutable effect lists while rejecting duplicate channels.
    static @Unmodifiable List<M3TransitionEffect> combine(
            List<M3TransitionEffect> first,
            List<M3TransitionEffect> second
    ) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.isEmpty()) {
            return copyOf(second);
        }
        if (second.isEmpty()) {
            return copyOf(first);
        }
        ArrayList<M3TransitionEffect> combined = new ArrayList<>(first.size() + second.size());
        combined.addAll(first);
        combined.addAll(second);
        return copyOf(combined);
    }

    /// Copies every effect with one explicit motion specification.
    static @Unmodifiable List<M3TransitionEffect> withMotionSpec(
            List<M3TransitionEffect> effects,
            @Nullable M3MotionSpec motionSpec
    ) {
        if (effects.isEmpty()) {
            return effects;
        }
        ArrayList<M3TransitionEffect> updated = new ArrayList<>(effects.size());
        for (M3TransitionEffect effect : effects) {
            updated.add(effect.withMotionSpec(motionSpec));
        }
        return List.copyOf(updated);
    }

    /// Copies every effect with one delay.
    static @Unmodifiable List<M3TransitionEffect> withDelay(
            List<M3TransitionEffect> effects,
            Duration delay
    ) {
        Objects.requireNonNull(delay, "delay");
        if (effects.isEmpty()) {
            return effects;
        }
        ArrayList<M3TransitionEffect> updated = new ArrayList<>(effects.size());
        for (M3TransitionEffect effect : effects) {
            updated.add(effect.withDelay(delay));
        }
        return List.copyOf(updated);
    }

    /// Creates the exception used for overlapping visual channels.
    private static IllegalArgumentException duplicate(M3TransitionEffectKind kind) {
        return new IllegalArgumentException("transition contains more than one " + kind.name().toLowerCase() + " effect");
    }
}
