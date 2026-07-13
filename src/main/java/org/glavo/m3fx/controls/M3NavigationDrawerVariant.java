// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the presentation variant of a Material Design 3 navigation drawer.
///
/// The standard variant is a persistent surface integrated into the application layout. The modal variant uses
/// the modal container color and elevation and is intended to be placed above application content with an
/// [M3Scrim].
///
/// See [Material Design navigation drawer](https://m3.material.io/components/navigation-drawer/specs).
@NotNullByDefault
public enum M3NavigationDrawerVariant {
    /// A persistent drawer using the surface color and level-zero elevation.
    STANDARD,

    /// A temporary drawer using the surface-container-low color and level-one elevation.
    MODAL
}
