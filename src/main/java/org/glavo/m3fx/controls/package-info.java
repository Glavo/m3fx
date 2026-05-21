// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Provides JavaFX controls that implement Material Design 3 component behavior, layout metrics,
/// visual states, and interaction feedback.
///
/// The controls in this package are regular JavaFX scene graph nodes. They can be used from Java code,
/// FXML, and CSS, and they follow the JavaFX property pattern so applications can bind, observe, and style
/// them in the same way as standard JavaFX controls. The package intentionally exposes M3-specific concepts
/// such as variants, surface elevation, navigation selection, supporting text, validation, and token-backed
/// sizing properties instead of requiring applications to copy Material CSS snippets into every control.
///
/// A typical application installs a theme through [org.glavo.m3fx.theme.M3ThemeManager] and then creates
/// controls such as [M3Button], [M3TextInputLayout], [M3NavigationDrawer], [M3SnackbarHost],
/// [M3DatePicker], or [M3ProgressIndicator]. Controls resolve color, shape, typography, elevation,
/// density, and motion through the active M3FX theme while still allowing per-control CSS overrides.
///
/// The implementation follows the public Material Design 3 component guidance where it maps cleanly to
/// JavaFX. See [Material Design](https://m3.material.io/) and the
/// [Material Design component catalog](https://m3.material.io/components) for the design language that
/// this package targets.
@NotNullByDefault
package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
