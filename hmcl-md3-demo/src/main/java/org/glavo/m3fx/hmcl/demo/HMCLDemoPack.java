// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// One in-memory resource pack, shader pack, or schematic entry.
///
/// @param id stable identifier
/// @param name display name
/// @param detail secondary label
/// @param enabled whether enabled when applicable
@NotNullByDefault
public record HMCLDemoPack(String id, String name, String detail, boolean enabled) {
    /// Returns a copy with the requested enabled state.
    public HMCLDemoPack withEnabled(boolean value) {
        return enabled == value ? this : new HMCLDemoPack(id, name, detail, value);
    }
}
