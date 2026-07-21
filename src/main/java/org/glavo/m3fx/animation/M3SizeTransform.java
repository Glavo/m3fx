// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Describes how an animated-content container changes size between retained nodes.
///
/// A non-null size transform makes [M3AnimatedContent] animate its minimum and preferred dimensions toward the
/// target content size. A `null` [#motionSpec()] resolves the default spatial role from the active theme. When
/// [#clip()] is `true`, drawing and picking are clipped to the container's animated bounds for the complete
/// transition. Supplying `null` as [M3ContentTransform#sizeTransform()] disables size animation and clipping.
///
/// @param clip       whether content is clipped to the animated container bounds
/// @param motionSpec the explicit size specification, or `null` to resolve the active theme
///
/// See [Compose SizeTransform](https://developer.android.com/reference/kotlin/androidx/compose/animation/SizeTransform).
@NotNullByDefault
public record M3SizeTransform(boolean clip, @Nullable M3MotionSpec motionSpec) {
}
