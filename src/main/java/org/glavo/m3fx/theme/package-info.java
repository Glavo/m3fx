// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Defines immutable themes and scene-graph installation services for M3FX.
///
/// [M3Theme] combines a Material color scheme, token profile, density, and complete token set.
/// [M3ThemeManager] installs that immutable description on a [javafx.scene.Scene] or a local
/// [javafx.scene.Parent] subtree. Scene installations follow root replacement; parent installations define a
/// nested theme scope.
///
/// Theme descriptions are immutable and can be shared between scenes. Installing or removing a theme mutates
/// JavaFX scene-graph state and must be performed on the JavaFX Application Thread once the scene graph is live.
///
/// Themes are built on top of MonetFX dynamic color generation and expose both baseline Material Design 3 and
/// Material Design 3 Expressive profiles. See [Material Design](https://m3.material.io/) and
/// [Material color](https://m3.material.io/styles/color/overview) for the design model mirrored by this
/// package.
@NotNullByDefault
package org.glavo.m3fx.theme;

import org.jetbrains.annotations.NotNullByDefault;
