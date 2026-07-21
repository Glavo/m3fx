// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3ExitTransitionImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Describes the visual effects applied while content exits an animated container.
///
/// An exit transition is an immutable composition of at most one fade, scale, and slide effect. Each effect starts
/// from the currently rendered value and finishes at its configured target. Effects created independently may use
/// different delays and motion specifications before they are combined with [#and(M3ExitTransition)]. Combining two
/// effects that control the same visual channel is rejected rather than relying on ordering.
///
/// The transition is applied to a private holder owned by [M3AnimatedContent] or [M3AnimatedVisibility]. It does not
/// change the content node's visual properties. A `null` motion specification selects the semantic effects or
/// spatial role from the active M3FX theme when the transition starts.
///
/// See [Compose exit transitions](https://developer.android.com/reference/kotlin/androidx/compose/animation/ExitTransition)
/// and [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public sealed interface M3ExitTransition permits M3ExitTransitionImpl {
    /// Returns a transition with no exit effects.
    ///
    /// Outgoing content remains at its neutral visual state until other enter or size effects finish, then it is
    /// detached. If the complete content transform has no active effect, detachment is synchronous.
    ///
    /// @return the shared empty transition
    static M3ExitTransition none() {
        return M3ExitTransitionImpl.NONE;
    }

    /// Creates a fade effect that finishes at the supplied opacity.
    ///
    /// The effect initially has no explicit motion specification and no delay.
    ///
    /// @param targetOpacity the finite target opacity in the range `0.0` through `1.0`
    /// @return an immutable fade transition
    /// @throws IllegalArgumentException if `targetOpacity` is outside the supported range or is not finite
    static M3ExitTransition fade(double targetOpacity) {
        return M3ExitTransitionImpl.fade(targetOpacity);
    }

    /// Creates a uniform scale effect that finishes at the supplied scale.
    ///
    /// The effect initially has no explicit motion specification and no delay. Scaling is performed around the
    /// center of the private content holder and does not affect its layout bounds.
    ///
    /// @param targetScale the finite, positive target scale
    /// @return an immutable scale transition
    /// @throws IllegalArgumentException if `targetScale` is not finite and greater than zero
    static M3ExitTransition scale(double targetScale) {
        return M3ExitTransitionImpl.scale(targetScale);
    }

    /// Creates a slide effect that moves content the supplied distance toward one logical edge.
    ///
    /// The effect initially has no explicit motion specification and no delay. Logical horizontal edges are
    /// resolved when the transition runs.
    ///
    /// @param edge     the edge toward which content exits
    /// @param distance the finite, non-negative distance in logical pixels
    /// @return an immutable slide transition
    /// @throws NullPointerException     if `edge` is `null`
    /// @throws IllegalArgumentException if `distance` is negative or not finite
    static M3ExitTransition slideTo(M3TransitionEdge edge, double distance) {
        return M3ExitTransitionImpl.slide(edge, distance);
    }

    /// Combines this transition with another transition.
    ///
    /// @param other the transition to combine with this transition
    /// @return the combined immutable transition
    /// @throws NullPointerException     if `other` is `null`
    /// @throws IllegalArgumentException if the transitions contain overlapping effects
    default M3ExitTransition and(M3ExitTransition other) {
        return M3ExitTransitionImpl.combine(this, other);
    }

    /// Returns a copy whose effects all use the supplied explicit motion specification.
    ///
    /// @param motionSpec the explicit specification, or `null` to resolve the active theme
    /// @return the transition with updated effect timing
    default M3ExitTransition withMotionSpec(@Nullable M3MotionSpec motionSpec) {
        return M3ExitTransitionImpl.withMotionSpec(this, motionSpec);
    }

    /// Returns a copy whose effects all start after the supplied delay.
    ///
    /// @param delay the finite, non-negative delay
    /// @return the transition with updated effect delay
    /// @throws NullPointerException     if `delay` is `null`
    /// @throws IllegalArgumentException if `delay` is negative, indefinite, or unknown
    default M3ExitTransition withDelay(Duration delay) {
        return M3ExitTransitionImpl.withDelay(this, delay);
    }
}
