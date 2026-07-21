// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.glavo.m3fx.internal.animation.M3EnterTransitionImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Describes the visual effects applied while content enters an animated container.
///
/// An enter transition is an immutable composition of at most one fade, scale, slide, and expand effect. Each effect
/// starts at its configured value and finishes at the content's neutral state: opacity and scale `1.0`, zero
/// translation, and a fully revealed holder. Effects created independently may use different delays and motion
/// specifications before they are combined with [#and(M3EnterTransition)]. Combining two effects that control the
/// same visual channel is rejected rather than relying on ordering.
///
/// The transition is applied to a private holder owned by [M3AnimatedContent] or [M3AnimatedVisibility]. It does not
/// change the content node's opacity, scale, translation, clip, or transform list. A `null` motion specification
/// selects the semantic effects or spatial role from the active M3FX theme when the transition starts.
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

    /// Creates a two-dimensional expand effect from the logical bottom-end corner.
    ///
    /// The private content holder starts with an empty reveal rectangle anchored to [M3TransitionEdge#END] and
    /// [M3TransitionEdge#BOTTOM], then reveals its complete layout bounds. The reveal clips drawing only; it does not
    /// change the holder's measured or laid-out size.
    ///
    /// @return an immutable two-dimensional expand transition
    static M3EnterTransition expandIn() {
        return expandIn(M3TransitionEdge.END, M3TransitionEdge.BOTTOM);
    }

    /// Creates a two-dimensional expand effect from the supplied logical corner.
    ///
    /// [M3TransitionEdge#START] and [M3TransitionEdge#END] follow the effective node orientation when the transition
    /// runs. The vertical anchor is independent of node orientation. The reveal clips a private holder and does not
    /// change content layout bounds or mutate the content node.
    ///
    /// @param horizontalAnchor the logical horizontal anchor, either `START` or `END`
    /// @param verticalAnchor   the vertical anchor, either `TOP` or `BOTTOM`
    /// @return an immutable two-dimensional expand transition
    /// @throws NullPointerException     if either anchor is `null`
    /// @throws IllegalArgumentException if an anchor is not valid for its axis
    static M3EnterTransition expandIn(
            M3TransitionEdge horizontalAnchor,
            M3TransitionEdge verticalAnchor
    ) {
        return M3EnterTransitionImpl.expand(
                Objects.requireNonNull(horizontalAnchor, "horizontalAnchor"),
                Objects.requireNonNull(verticalAnchor, "verticalAnchor")
        );
    }

    /// Creates a horizontal expand effect from the logical end edge.
    ///
    /// The holder starts at zero revealed width and full revealed height. Its measured and laid-out dimensions remain
    /// unchanged while the reveal width grows.
    ///
    /// @return an immutable horizontal expand transition
    static M3EnterTransition expandHorizontally() {
        return expandHorizontally(M3TransitionEdge.END);
    }

    /// Creates a horizontal expand effect from the supplied logical edge.
    ///
    /// @param horizontalAnchor the logical anchor, either `START` or `END`
    /// @return an immutable horizontal expand transition
    /// @throws NullPointerException     if `horizontalAnchor` is `null`
    /// @throws IllegalArgumentException if `horizontalAnchor` is not a horizontal edge
    static M3EnterTransition expandHorizontally(M3TransitionEdge horizontalAnchor) {
        return M3EnterTransitionImpl.expand(
                Objects.requireNonNull(horizontalAnchor, "horizontalAnchor"),
                null
        );
    }

    /// Creates a vertical expand effect from the bottom edge.
    ///
    /// The holder starts at full revealed width and zero revealed height. Its measured and laid-out dimensions remain
    /// unchanged while the reveal height grows.
    ///
    /// @return an immutable vertical expand transition
    static M3EnterTransition expandVertically() {
        return expandVertically(M3TransitionEdge.BOTTOM);
    }

    /// Creates a vertical expand effect from the supplied edge.
    ///
    /// @param verticalAnchor the anchor, either `TOP` or `BOTTOM`
    /// @return an immutable vertical expand transition
    /// @throws NullPointerException     if `verticalAnchor` is `null`
    /// @throws IllegalArgumentException if `verticalAnchor` is not a vertical edge
    static M3EnterTransition expandVertically(M3TransitionEdge verticalAnchor) {
        return M3EnterTransitionImpl.expand(
                null,
                Objects.requireNonNull(verticalAnchor, "verticalAnchor")
        );
    }

    /// Combines this transition with another transition.
    ///
    /// The returned value is independent of both operands. Empty operands are ignored. Two operands must not both
    /// define a fade, both define a scale, both define a slide, or both define an expand effect.
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
