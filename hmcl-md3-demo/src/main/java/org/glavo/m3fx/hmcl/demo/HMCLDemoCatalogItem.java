// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// One download-catalog row used by mods, modpacks, resource packs, shaders, and worlds.
///
/// @param id stable identifier
/// @param title display title
/// @param author author label
/// @param summary short description
/// @param downloads download count label
/// @param kind content kind
@NotNullByDefault
public record HMCLDemoCatalogItem(
        String id,
        String title,
        String author,
        String summary,
        String downloads,
        Kind kind
) {
    /// Catalog content kinds.
    @NotNullByDefault
    public enum Kind {
        /// Modpack.
        MODPACK,
        /// Mod.
        MOD,
        /// Resource pack.
        RESOURCE_PACK,
        /// Shader pack.
        SHADER,
        /// World.
        WORLD
    }
}
