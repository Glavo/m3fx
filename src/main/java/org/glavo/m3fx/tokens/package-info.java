// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Contains immutable Material Design 3 token models used by M3FX themes and controls.
///
/// Token objects describe the design values that controls consume: color roles, typography styles, shape
/// radii, elevation, density adjustments, state layer opacities, component metrics, and motion profiles.
/// Applications normally create tokens indirectly through [org.glavo.m3fx.theme.M3Theme], but the public
/// token interfaces are available for advanced integrations, diagnostics, and custom theme construction.
///
/// The token model is designed to keep component code independent from any single CSS file. Theme instances
/// convert tokens into JavaFX CSS custom properties and component rules that can be installed through
/// [org.glavo.m3fx.theme.M3ThemeManager].
///
/// See [Material Design](https://m3.material.io/),
/// [Material Design color](https://m3.material.io/styles/color/overview), and
/// [Material Design motion](https://m3.material.io/styles/motion/overview) for the upstream design system
/// represented by these tokens.
@NotNullByDefault
package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;
