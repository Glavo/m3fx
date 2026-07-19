// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Defines Material Design 3 motion primitives, semantic specifications, and reduced-motion settings.
///
/// [M3Motion] exposes the primitive duration and easing scale. [M3MotionSpec] combines those primitives, while
/// [M3MotionScheme] assigns specifications to effects and spatial roles. [M3MotionBehavior] contains related
/// control delays and repeating cycle periods. Applications normally consume these values through the active
/// theme rather than selecting constants directly.
///
/// [M3MotionSettings] can request reduced motion globally or for a node subtree. Effective node settings are
/// inherited through the JavaFX parent chain; a descendant cannot cancel an ancestor request.
///
/// [M3DoubleAnimatable] provides allocation-stable, interruptible animation of a writable double property.
/// [M3AnimatedVisibility] owns enter and exit visuals for one content node. [M3AnimatedContent] retains incoming
/// and outgoing nodes while animating replacement and preferred size, while [M3LayoutTransition] adds
/// transform-based child placement motion to an existing JavaFX layout container. These runtime APIs resolve
/// reduced-motion policy from their owner nodes and do not require specialized animated layout subclasses.
///
/// Motion specs and schemes are immutable and can be shared. Their builders are mutable, reusable, and not
/// thread-safe. Runtime settings that observe or mutate live nodes follow JavaFX scene-graph threading rules.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/) for the design language reflected by these APIs.
@NotNullByDefault
package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;
