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
/// [#DEFAULT] uses Material fade-through timing: outgoing content fades promptly, while incoming content fades and
/// scales from `0.92` after a short delay. Motion curves and durations continue to resolve from the active Standard
/// or Expressive theme.
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
    /// The default fade-through content transform.
    public static final M3ContentTransform DEFAULT = new M3ContentTransform(
            M3EnterTransition.fade(0.0)
                    .and(M3EnterTransition.scale(0.92))
                    .withDelay(Duration.millis(90.0)),
            M3ExitTransition.fade(0.0),
            new M3SizeTransform(true, null),
            0.0
    );

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
}
