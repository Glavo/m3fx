// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Defines Material Design 3 motion primitives, semantic specifications, and reduced-motion settings.
///
/// [M3Motion] exposes the primitive duration and easing scale. [M3MotionSpec] combines those primitives, while
/// [M3MotionScheme] assigns specifications to effects and spatial roles. [M3MotionBehavior] contains related
/// control delays and repeating cycle periods. Applications normally consume these values through the active
/// theme rather than selecting constants directly.
///
/// [M3Animated] components expose an `animationsEnabled` property. Its CSS counterpart,
/// `-m3-animations-enabled`, inherits from ancestor styles and allows nearer declarations to override them.
/// [M3MotionSettings] provides an independent application-wide reduced-motion request that takes precedence
/// over component settings. Ordinary JavaFX nodes used as animation owners follow only the global request.
///
/// [M3DoubleAnimatable] provides interruptible animation of a writable double property, while [M3StateTransition]
/// coordinates primitive doubles and immutable values through one typed, seekable transition. [M3VectorConverter]
/// defines the component mapping for immutable JavaFX values, and [M3VectorConverters] provides mappings for common
/// color and geometry types. [M3AnimatedVisibility] exposes the enter, visible, exit, and hidden lifecycle of one
/// content node. [M3AnimatedContent] animates content replacement and preferred size. Its immutable
/// [M3EnterTransition] and [M3ExitTransition] values compose independent fade, scale, logical-edge slide, and
/// RTL-aware expand or shrink reveal effects, while [M3ContentTransform] coordinates those effects with size and
/// drawing-order behavior. Cached fade, fade-through, and [shared-axis][M3TransitionAxis] presets cover standard
/// Material content-replacement patterns. [M3LayoutTransition] animates direct-child placement in an existing
/// JavaFX layout container. These runtime APIs resolve reduced-motion policy from their owner nodes and do not
/// require specialized animated layout subclasses.
///
/// Motion specs and schemes are immutable and can be shared. Their builders are mutable, reusable, and not
/// thread-safe. Runtime settings that observe or mutate live nodes follow JavaFX scene-graph threading rules.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/) for the design language reflected by these APIs.
@NotNullByDefault
package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;
