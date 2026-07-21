// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.animation;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3TransitionEdge;
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
        boolean clip = false;
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
                case CLIP -> {
                    if (clip) {
                        throw duplicate(effect.kind());
                    }
                    clip = true;
                }
            }
        }
        return List.copyOf(effects);
    }

    /// Creates a collapsed logical reveal rectangle for one or both supplied axes.
    ///
    /// @param horizontalAnchor the logical horizontal anchor, or `null` to retain full width
    /// @param verticalAnchor   the vertical anchor, or `null` to retain full height
    /// @return a validated clip effect
    /// @throws NullPointerException     if both anchors are `null`
    /// @throws IllegalArgumentException if an anchor is invalid for its axis
    static M3TransitionEffect clip(
            @Nullable M3TransitionEdge horizontalAnchor,
            @Nullable M3TransitionEdge verticalAnchor
    ) {
        if (horizontalAnchor == null && verticalAnchor == null) {
            throw new NullPointerException("at least one clip anchor must be non-null");
        }
        if (horizontalAnchor != null
                && horizontalAnchor != M3TransitionEdge.START
                && horizontalAnchor != M3TransitionEdge.END) {
            throw new IllegalArgumentException("horizontalAnchor must be START or END");
        }
        if (verticalAnchor != null
                && verticalAnchor != M3TransitionEdge.TOP
                && verticalAnchor != M3TransitionEdge.BOTTOM) {
            throw new IllegalArgumentException("verticalAnchor must be TOP or BOTTOM");
        }

        double minX = horizontalAnchor == M3TransitionEdge.END ? 1.0 : 0.0;
        double maxX = horizontalAnchor == M3TransitionEdge.START ? 0.0 : 1.0;
        double minY = verticalAnchor == M3TransitionEdge.BOTTOM ? 1.0 : 0.0;
        double maxY = verticalAnchor == M3TransitionEdge.TOP ? 0.0 : 1.0;
        return new M3TransitionEffect(
                M3TransitionEffectKind.CLIP,
                0.0,
                null,
                new Rectangle2D(minX, minY, maxX - minX, maxY - minY),
                null,
                Duration.ZERO
        );
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
