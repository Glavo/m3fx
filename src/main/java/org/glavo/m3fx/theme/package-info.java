// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Defines themes and installation helpers for applying Material Design 3 tokens to JavaFX scenes.
///
/// A theme combines a Material color scheme, a component token profile, density settings, and motion tokens.
/// [M3Theme] creates immutable theme descriptions, while [M3ThemeManager] installs the generated CSS
/// declarations and style classes on a [javafx.scene.Scene] or root [javafx.scene.Parent]. Installing a theme
/// is the normal entry point for applications that want M3FX controls to share one color system and component
/// token set.
///
/// Themes are built on top of MonetFX dynamic color generation and expose both baseline Material Design 3 and
/// Material Design 3 Expressive profiles. See [Material Design](https://m3.material.io/) and
/// [Material color](https://m3.material.io/styles/color/overview) for the design model mirrored by this
/// package.
@NotNullByDefault
package org.glavo.m3fx.theme;

import org.jetbrains.annotations.NotNullByDefault;
