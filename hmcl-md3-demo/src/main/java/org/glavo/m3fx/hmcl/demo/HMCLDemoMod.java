// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// One in-memory mod entry.
///
/// @param id stable identifier
/// @param name display name
/// @param fileName jar file name
/// @param version version label
/// @param enabled whether enabled
@NotNullByDefault
public record HMCLDemoMod(String id, String name, String fileName, String version, boolean enabled) {
    /// Returns a copy with the requested enabled state.
    public HMCLDemoMod withEnabled(boolean value) {
        return enabled == value ? this : new HMCLDemoMod(id, name, fileName, version, value);
    }
}
