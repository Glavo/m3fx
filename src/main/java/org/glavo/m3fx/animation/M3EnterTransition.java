// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3EnterTransitionImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Describes the visual effects applied while content enters an animated container.
///
/// An enter transition is an immutable composition of at most one fade, scale, and slide effect. Each effect starts
/// at its configured value and finishes at the content's neutral state: opacity and scale `1.0`, with zero
/// translation. Effects created independently may use different delays and motion specifications before they are
/// combined with [#and(M3EnterTransition)]. Combining two effects that control the same visual channel is rejected
/// rather than relying on ordering.
///
/// The transition is applied to a private holder owned by [M3AnimatedContent] or [M3AnimatedVisibility]. It does not
/// change the content node's opacity, scale, translation, or transform list. A `null` motion specification selects
/// the semantic effects or spatial role from the active M3FX theme when the transition starts.
///
/// See [Compose enter transitions](https://developer.android.com/reference/kotlin/androidx/compose/animation/EnterTransition)
/// and [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3EnterTransition permits M3EnterTransitionImpl {
    /// Returns a transition with no enter effects.
    ///
    /// Content using this transition appears at its neutral visual state. Size animation configured by the enclosing
    /// [M3ContentTransform] may still keep the replacement transition active.
    ///
    /// @return the shared empty transition
    static M3EnterTransition none() {
        return M3EnterTransitionImpl.NONE;
    }

    /// Creates a fade effect that starts at the supplied opacity.
    ///
    /// The effect initially has no explicit motion specification and no delay.
    ///
    /// @param initialOpacity the finite starting opacity in the range `0.0` through `1.0`
    /// @return an immutable fade transition
    /// @throws IllegalArgumentException if `initialOpacity` is outside the supported range or is not finite
    static M3EnterTransition fade(double initialOpacity) {
        return M3EnterTransitionImpl.fade(initialOpacity);
    }

    /// Creates a uniform scale effect that starts at the supplied scale.
    ///
    /// The effect initially has no explicit motion specification and no delay. Scaling is performed around the
    /// center of the private content holder and does not affect its layout bounds.
    ///
    /// @param initialScale the finite, positive starting scale
    /// @return an immutable scale transition
    /// @throws IllegalArgumentException if `initialScale` is not finite and greater than zero
    static M3EnterTransition scale(double initialScale) {
        return M3EnterTransitionImpl.scale(initialScale);
    }

    /// Creates a slide effect that starts the supplied distance beyond one logical edge.
    ///
    /// The effect initially has no explicit motion specification and no delay. It moves toward zero translation as
    /// content enters. Logical horizontal edges are resolved when the transition runs, so changing node orientation
    /// before a new run changes the physical direction without rebuilding this value.
    ///
    /// @param edge     the edge from which content enters
    /// @param distance the finite, non-negative distance in logical pixels
    /// @return an immutable slide transition
    /// @throws NullPointerException     if `edge` is `null`
    /// @throws IllegalArgumentException if `distance` is negative or not finite
    static M3EnterTransition slideFrom(M3TransitionEdge edge, double distance) {
        return M3EnterTransitionImpl.slide(edge, distance);
    }

    /// Combines this transition with another transition.
    ///
    /// The returned value is independent of both operands. Empty operands are ignored. Two operands must not both
    /// define a fade, both define a scale, or both define a slide effect.
    ///
    /// @param other the transition to combine with this transition
    /// @return the combined immutable transition
    /// @throws NullPointerException     if `other` is `null`
    /// @throws IllegalArgumentException if the transitions contain overlapping effects
    default M3EnterTransition and(M3EnterTransition other) {
        return M3EnterTransitionImpl.combine(this, other);
    }

    /// Returns a copy whose effects all use the supplied explicit motion specification.
    ///
    /// Passing `null` restores semantic theme resolution for every effect. Calling this method on an empty
    /// transition returns the same empty transition.
    ///
    /// @param motionSpec the explicit specification, or `null` to resolve the active theme
    /// @return the transition with updated effect timing
    default M3EnterTransition withMotionSpec(@Nullable M3MotionSpec motionSpec) {
        return M3EnterTransitionImpl.withMotionSpec(this, motionSpec);
    }

    /// Returns a copy whose effects all start after the supplied delay.
    ///
    /// The delay is measured from the beginning of each content replacement. It is applied before the effect's own
    /// motion specification. Calling this method on an empty transition returns the same empty transition.
    ///
    /// @param delay the finite, non-negative delay
    /// @return the transition with updated effect delay
    /// @throws NullPointerException     if `delay` is `null`
    /// @throws IllegalArgumentException if `delay` is negative, indefinite, or unknown
    default M3EnterTransition withDelay(Duration delay) {
        return M3EnterTransitionImpl.withDelay(this, delay);
    }
}
