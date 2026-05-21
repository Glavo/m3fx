// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Defines runtime motion settings and semantic motion specs for M3FX controls.
///
/// The animation package exposes the duration and easing model used by interaction feedback, state changes,
/// popup transitions, progress indicators, and Material Design 3 Expressive motion variants. Applications can
/// disable animations globally, override motion for a subtree, or provide a different [M3MotionScheme] when a
/// workflow requires reduced motion or a custom interaction feel.
///
/// Motion settings are resolved by walking the JavaFX parent chain, so a container can opt an entire feature
/// area in or out of animation without changing each control individually.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/) for the design language reflected by these APIs.
@NotNullByDefault
package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;
