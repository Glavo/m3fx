// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

/// Provides JavaFX skin implementations used by M3FX controls.
///
/// The package is not exported by the `org.glavo.m3fx` module. Applications should customize controls through
/// the public APIs in [org.glavo.m3fx.controls], styleable properties, CSS, and theme tokens instead of depending
/// on concrete skin classes. These skins implement Material Design 3 layout, state layers, ripple feedback, focus
/// indication, elevation, and motion using the active M3FX token set.
///
/// Localized skins observe the skinned control's [org.glavo.m3fx.localization.M3Localized#localizerProperty()]
/// directly and release their subscriptions on disposal. Built-in labels do not inherit an ancestor's language;
/// applications share localization by explicitly setting or binding control localizers.
///
/// See [Material Design](https://m3.material.io/) and the
/// [Material Design component catalog](https://m3.material.io/components) for the component behavior mirrored
/// by these skins.
@NotNullByDefault
package org.glavo.m3fx.skins;

import org.jetbrains.annotations.NotNullByDefault;
