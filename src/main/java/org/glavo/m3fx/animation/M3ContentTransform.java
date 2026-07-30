// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Describes the enter, exit, size, and drawing-order behavior of one content replacement.
///
/// Instances are immutable and may be shared by multiple animated containers. The target content enters using
/// [#targetContentEnter()], while the previously current content exits using [#initialContentExit()]. A non-null
/// [#sizeTransform()] animates the container toward the target content's measured size. The target content is drawn
/// above outgoing content when [#targetContentZIndex()] is greater than or equal to zero and below it when negative.
///
/// Shared presets cover Material fade, fade-through, and shared-axis patterns. They retain immutable transition
/// values and may be reused without allocating per content replacement. Motion curves and durations continue to
/// resolve from the active Standard or Expressive theme.
///
/// @param targetContentEnter  the effects used by incoming content
/// @param initialContentExit  the effects used by outgoing content
/// @param sizeTransform       the size behavior, or `null` to change size synchronously without clipping
/// @param targetContentZIndex the finite drawing-order value of target content relative to outgoing content
///
/// See [Compose ContentTransform](https://developer.android.com/reference/kotlin/androidx/compose/animation/ContentTransform)
/// and [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public record M3ContentTransform(
        M3EnterTransition targetContentEnter,
        M3ExitTransition initialContentExit,
        @Nullable M3SizeTransform sizeTransform,
        double targetContentZIndex
) {
    /// The default logical-pixel distance used by horizontal and vertical shared-axis presets.
    private static final double SHARED_AXIS_DISTANCE = 30.0;

    /// The delay before incoming content begins fading during a fade-through pattern.
    private static final Duration FADE_THROUGH_ENTER_DELAY = Duration.millis(90.0);

    /// The shared size policy used by built-in animated replacement patterns.
    private static final M3SizeTransform DEFAULT_SIZE_TRANSFORM = new M3SizeTransform(true, null);

    /// A transform that replaces content synchronously without visual or size animation.
    public static final M3ContentTransform NONE = new M3ContentTransform(
            M3EnterTransition.none(),
            M3ExitTransition.none(),
            null,
            0.0
    );

    /// The Material fade pattern.
    ///
    /// Incoming content fades while scaling from `0.8`; outgoing content only fades. The container animates and
    /// clips its size to the target content.
    public static final M3ContentTransform FADE = new M3ContentTransform(
            M3EnterTransition.fade(0.0)
                    .and(M3EnterTransition.scale(0.8)),
            M3ExitTransition.fade(0.0),
            DEFAULT_SIZE_TRANSFORM,
            0.0
    );

    /// The Material fade-through pattern.
    ///
    /// Outgoing content fades promptly. Incoming content scales from `0.92` over the complete spatial transition
    /// and begins fading after a short delay, minimizing visual overlap between unrelated content states.
    public static final M3ContentTransform FADE_THROUGH = new M3ContentTransform(
            M3EnterTransition.fade(0.0)
                    .withDelay(FADE_THROUGH_ENTER_DELAY)
                    .and(M3EnterTransition.scale(0.92)),
            M3ExitTransition.fade(0.0),
            DEFAULT_SIZE_TRANSFORM,
            0.0
    );

    /// The cached forward horizontal shared-axis transform.
    private static final M3ContentTransform SHARED_AXIS_X_FORWARD =
            createSharedAxis(M3TransitionAxis.X, true);

    /// The cached backward horizontal shared-axis transform.
    private static final M3ContentTransform SHARED_AXIS_X_BACKWARD =
            createSharedAxis(M3TransitionAxis.X, false);

    /// The cached forward vertical shared-axis transform.
    private static final M3ContentTransform SHARED_AXIS_Y_FORWARD =
            createSharedAxis(M3TransitionAxis.Y, true);

    /// The cached backward vertical shared-axis transform.
    private static final M3ContentTransform SHARED_AXIS_Y_BACKWARD =
            createSharedAxis(M3TransitionAxis.Y, false);

    /// The cached forward depth shared-axis transform.
    private static final M3ContentTransform SHARED_AXIS_Z_FORWARD =
            createSharedAxis(M3TransitionAxis.Z, true);

    /// The cached backward depth shared-axis transform.
    private static final M3ContentTransform SHARED_AXIS_Z_BACKWARD =
            createSharedAxis(M3TransitionAxis.Z, false);

    /// The default content transform, equal to [#FADE_THROUGH].
    public static final M3ContentTransform DEFAULT = FADE_THROUGH;

    /// Creates a content transform after validating its required effects and drawing order.
    ///
    /// @throws NullPointerException     if `targetContentEnter` or `initialContentExit` is `null`
    /// @throws IllegalArgumentException if `targetContentZIndex` is not finite
    public M3ContentTransform {
        Objects.requireNonNull(targetContentEnter, "targetContentEnter");
        Objects.requireNonNull(initialContentExit, "initialContentExit");
        if (!Double.isFinite(targetContentZIndex)) {
            throw new IllegalArgumentException("targetContentZIndex must be finite");
        }
    }

    /// Returns the shared Material transition for movement along one axis.
    ///
    /// Forward X motion brings incoming content from logical end while outgoing content moves toward logical start.
    /// Forward Y motion brings incoming content from the bottom while outgoing content moves toward the top.
    /// Forward Z motion brings incoming content from scale `0.8` while outgoing content grows to `1.1`. Backward
    /// motion reverses those directions and scale relationships. Every preset composes fade-through feedback with
    /// its spatial channel and animates container size.
    ///
    /// Repeated calls with the same arguments return the same immutable object.
    ///
    /// @param axis    the shared spatial axis
    /// @param forward whether the relationship moves forward along that axis
    /// @return the cached shared-axis content transform
    /// @throws NullPointerException if `axis` is `null`
    public static M3ContentTransform sharedAxis(M3TransitionAxis axis, boolean forward) {
        return switch (Objects.requireNonNull(axis, "axis")) {
            case X -> forward ? SHARED_AXIS_X_FORWARD : SHARED_AXIS_X_BACKWARD;
            case Y -> forward ? SHARED_AXIS_Y_FORWARD : SHARED_AXIS_Y_BACKWARD;
            case Z -> forward ? SHARED_AXIS_Z_FORWARD : SHARED_AXIS_Z_BACKWARD;
        };
    }

    /// Returns a copy with a replacement size transform.
    ///
    /// Passing `null` makes the container adopt target content size synchronously without clipping. If the supplied
    /// value equals the current size transform, this object is returned.
    ///
    /// @param sizeTransform the replacement size transform, or `null` for synchronous size changes
    /// @return this transform or an immutable copy with the replacement size policy
    public M3ContentTransform withSizeTransform(@Nullable M3SizeTransform sizeTransform) {
        return Objects.equals(this.sizeTransform, sizeTransform)
                ? this
                : new M3ContentTransform(
                        targetContentEnter,
                        initialContentExit,
                        sizeTransform,
                        targetContentZIndex
                );
    }

    /// Returns a copy with a replacement target-content drawing order.
    ///
    /// If the supplied value equals the current drawing order, this object is returned.
    ///
    /// @param targetContentZIndex the finite target-content drawing order
    /// @return this transform or an immutable copy with the replacement drawing order
    /// @throws IllegalArgumentException if `targetContentZIndex` is not finite
    public M3ContentTransform withTargetContentZIndex(double targetContentZIndex) {
        return Double.compare(this.targetContentZIndex, targetContentZIndex) == 0
                ? this
                : new M3ContentTransform(
                        targetContentEnter,
                        initialContentExit,
                        sizeTransform,
                        targetContentZIndex
                );
    }

    /// Creates one cached shared-axis transform.
    ///
    /// @param axis    the spatial axis
    /// @param forward whether the relationship moves forward along the axis
    /// @return the immutable content transform
    private static M3ContentTransform createSharedAxis(M3TransitionAxis axis, boolean forward) {
        M3EnterTransition enter = M3EnterTransition.fade(0.0).withDelay(FADE_THROUGH_ENTER_DELAY);
        M3ExitTransition exit = M3ExitTransition.fade(0.0);
        switch (axis) {
            case X -> {
                M3TransitionEdge enterEdge = forward ? M3TransitionEdge.END : M3TransitionEdge.START;
                M3TransitionEdge exitEdge = forward ? M3TransitionEdge.START : M3TransitionEdge.END;
                enter = enter.and(M3EnterTransition.slideFrom(enterEdge, SHARED_AXIS_DISTANCE));
                exit = exit.and(M3ExitTransition.slideTo(exitEdge, SHARED_AXIS_DISTANCE));
            }
            case Y -> {
                M3TransitionEdge enterEdge = forward ? M3TransitionEdge.BOTTOM : M3TransitionEdge.TOP;
                M3TransitionEdge exitEdge = forward ? M3TransitionEdge.TOP : M3TransitionEdge.BOTTOM;
                enter = enter.and(M3EnterTransition.slideFrom(enterEdge, SHARED_AXIS_DISTANCE));
                exit = exit.and(M3ExitTransition.slideTo(exitEdge, SHARED_AXIS_DISTANCE));
            }
            case Z -> {
                enter = enter.and(M3EnterTransition.scale(forward ? 0.8 : 1.1));
                exit = exit.and(M3ExitTransition.scale(forward ? 1.1 : 0.8));
            }
        }
        return new M3ContentTransform(enter, exit, DEFAULT_SIZE_TRANSFORM, 0.0);
    }
}
