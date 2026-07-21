// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3ExitTransitionImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Describes the visual effects applied while content exits an animated container.
///
/// An exit transition is an immutable composition of at most one fade, scale, slide, and shrink effect. Each effect
/// starts from the currently rendered value and finishes at its configured target. Effects created independently may
/// use different delays and motion specifications before they are combined with [#and(M3ExitTransition)]. Combining
/// two effects that control the same visual channel is rejected rather than relying on ordering.
///
/// The transition is applied to a private holder owned by [M3AnimatedContent] or [M3AnimatedVisibility]. It does not
/// change the content node's visual properties, clip, or transform list. A `null` motion specification selects the
/// semantic effects or spatial role from the active M3FX theme when the transition starts.
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

    /// Creates a two-dimensional shrink effect toward the logical bottom-end corner.
    ///
    /// The private content holder shrinks from its current reveal rectangle to an empty rectangle anchored to
    /// [M3TransitionEdge#END] and [M3TransitionEdge#BOTTOM]. The reveal clips drawing only; it does not change the
    /// holder's measured or laid-out size.
    ///
    /// @return an immutable two-dimensional shrink transition
    static M3ExitTransition shrinkOut() {
        return shrinkOut(M3TransitionEdge.END, M3TransitionEdge.BOTTOM);
    }

    /// Creates a two-dimensional shrink effect toward the supplied logical corner.
    ///
    /// [M3TransitionEdge#START] and [M3TransitionEdge#END] follow the effective node orientation when the transition
    /// runs. The vertical anchor is independent of node orientation. The reveal clips a private holder and does not
    /// change content layout bounds or mutate the content node.
    ///
    /// @param horizontalAnchor the logical horizontal anchor, either `START` or `END`
    /// @param verticalAnchor   the vertical anchor, either `TOP` or `BOTTOM`
    /// @return an immutable two-dimensional shrink transition
    /// @throws NullPointerException     if either anchor is `null`
    /// @throws IllegalArgumentException if an anchor is not valid for its axis
    static M3ExitTransition shrinkOut(
            M3TransitionEdge horizontalAnchor,
            M3TransitionEdge verticalAnchor
    ) {
        return M3ExitTransitionImpl.shrink(
                Objects.requireNonNull(horizontalAnchor, "horizontalAnchor"),
                Objects.requireNonNull(verticalAnchor, "verticalAnchor")
        );
    }

    /// Creates a horizontal shrink effect toward the logical end edge.
    ///
    /// The holder shrinks to zero revealed width while retaining its full revealed height. Its measured and laid-out
    /// dimensions remain unchanged.
    ///
    /// @return an immutable horizontal shrink transition
    static M3ExitTransition shrinkHorizontally() {
        return shrinkHorizontally(M3TransitionEdge.END);
    }

    /// Creates a horizontal shrink effect toward the supplied logical edge.
    ///
    /// @param horizontalAnchor the logical anchor, either `START` or `END`
    /// @return an immutable horizontal shrink transition
    /// @throws NullPointerException     if `horizontalAnchor` is `null`
    /// @throws IllegalArgumentException if `horizontalAnchor` is not a horizontal edge
    static M3ExitTransition shrinkHorizontally(M3TransitionEdge horizontalAnchor) {
        return M3ExitTransitionImpl.shrink(
                Objects.requireNonNull(horizontalAnchor, "horizontalAnchor"),
                null
        );
    }

    /// Creates a vertical shrink effect toward the bottom edge.
    ///
    /// The holder shrinks to zero revealed height while retaining its full revealed width. Its measured and laid-out
    /// dimensions remain unchanged.
    ///
    /// @return an immutable vertical shrink transition
    static M3ExitTransition shrinkVertically() {
        return shrinkVertically(M3TransitionEdge.BOTTOM);
    }

    /// Creates a vertical shrink effect toward the supplied edge.
    ///
    /// @param verticalAnchor the anchor, either `TOP` or `BOTTOM`
    /// @return an immutable vertical shrink transition
    /// @throws NullPointerException     if `verticalAnchor` is `null`
    /// @throws IllegalArgumentException if `verticalAnchor` is not a vertical edge
    static M3ExitTransition shrinkVertically(M3TransitionEdge verticalAnchor) {
        return M3ExitTransitionImpl.shrink(
                null,
                Objects.requireNonNull(verticalAnchor, "verticalAnchor")
        );
    }

    /// Combines this transition with another transition.
    ///
    /// Empty operands are ignored. Two operands must not both define a fade, both define a scale, both define a
    /// slide, or both define a shrink effect.
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
