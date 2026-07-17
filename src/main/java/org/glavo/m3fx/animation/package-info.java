// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Defines runtime motion settings and semantic motion specs for M3FX controls.
///
/// The animation package exposes the duration and easing model used by interaction feedback, state changes,
/// popup transitions, progress indicators, and Material Design 3 Expressive motion variants. Applications can
/// request reduced motion globally or for a subtree. Motion schemes and behavior timings belong to theme tokens,
/// so Standard and Expressive profiles remain the single source of design motion.
///
/// Reduced-motion settings are resolved by walking the JavaFX parent chain, so a container can request reduced
/// motion for an entire feature area without changing each control individually.
///
/// Motion specs and schemes are immutable and can be shared. Their builders are mutable, reusable, and not
/// thread-safe. Runtime settings that observe or mutate live nodes follow JavaFX scene-graph threading rules.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/) for the design language reflected by these APIs.
@NotNullByDefault
package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;
