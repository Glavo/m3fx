// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes one immutable mod entry in a dummy game instance.
///
/// @param id the stable mod identifier
/// @param name the displayed mod name
/// @param fileName the displayed jar file name
/// @param enabled whether the mod is enabled
@NotNullByDefault
public record HMCLDemoMod(String id, String name, String fileName, boolean enabled) {
    /// Returns a copy with the requested enabled state.
    ///
    /// @param value the new enabled state
    /// @return this value when unchanged, otherwise an updated copy
    public HMCLDemoMod withEnabled(boolean value) {
        return enabled == value ? this : new HMCLDemoMod(id, name, fileName, value);
    }
}
