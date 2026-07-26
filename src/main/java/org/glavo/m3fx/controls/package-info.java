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
/// The package contains both leaf controls, such as [M3Button], [M3Switch], and [M3TextField], and composite
/// controls, such as [M3NavigationDrawer], [M3DatePicker], and [M3Dialog]. Nodes assigned to content slots remain
/// subject to the JavaFX single-parent rule and cannot simultaneously appear in another parent. Individual controls
/// document whether a slot accepts `null` and when an assigned node becomes part of the scene graph.
///
/// As with the standard JavaFX scene graph, controls and their live observable collections are not thread-safe.
/// Applications must construct or mutate controls on the JavaFX Application Thread after they become part of a
/// live scene graph. Unless a declaration is annotated with
/// [org.jetbrains.annotations.Nullable], reference arguments and return values are non-null. Passing `null` where
/// it is not permitted violates the API contract and methods that validate the argument report
/// [NullPointerException].
///
/// Public component dimensions use JavaFX logical pixels. Setters for non-negative styleable dimensions reject
/// negative and non-finite values with [IllegalArgumentException]. Node lists returned by controls are live,
/// mutable views unless documented otherwise. Changes to a live list are observed immediately and preserve list
/// order in the rendered control; individual APIs document whether they reject `null`, duplicates, or nonselectable
/// entries. Read-only observable views remain observable but reject structural modification.
///
/// A typical application uses an [M3OverlayPane] as its stable scene root, assigns its ordinary scaffold with
/// [M3OverlayPane#setContent(javafx.scene.Node)], and installs a theme through
/// [org.glavo.m3fx.theme.M3ThemeManager]. Controls such as [M3Button], [M3TextInputLayout], [M3NavigationDrawer],
/// [M3DatePicker], and [M3ProgressIndicator] resolve color, shape, typography, elevation, density, and motion
/// through the active M3FX theme while still allowing
/// per-control CSS overrides. [M3OverlayPane] owns in-scene [M3Dialog] and [M3Snackbar] presentations without
/// replacing the scene root, while [M3DialogWindow] can present a dialog in an independent native window when no
/// overlay scene exists.
///
/// These controls follow the public Material Design 3 component guidance where it maps cleanly to JavaFX.
/// See [Material Design](https://m3.material.io/) and the
/// [Material Design component catalog](https://m3.material.io/components) for the design language that
/// this package targets.
@NotNullByDefault
package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
