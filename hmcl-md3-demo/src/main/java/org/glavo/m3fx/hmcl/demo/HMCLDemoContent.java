// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Describes one immutable item in the deterministic Discover catalog.
///
/// @param id the stable catalog identifier
/// @param title the displayed content title
/// @param author the displayed author name
/// @param summary the short catalog description
/// @param kind the content category
/// @param gameVersions the immutable compatible-version list
/// @param iconName the logical demo icon name
/// @param downloadCount the deterministic popularity count
/// @param featured whether the item belongs to the featured collection
@NotNullByDefault
public record HMCLDemoContent(
        String id,
        String title,
        String author,
        String summary,
        ContentKind kind,
        @Unmodifiable List<String> gameVersions,
        String iconName,
        long downloadCount,
        boolean featured
) {
    /// Copies mutable constructor inputs to retain value semantics.
    public HMCLDemoContent {
        gameVersions = List.copyOf(gameVersions);
    }

    /// Describes the catalog category of a dummy content item.
    @NotNullByDefault
    public enum ContentKind {
        /// A gameplay or utility mod.
        MOD,

        /// A curated mod collection.
        MODPACK,

        /// A resource-pack entry.
        RESOURCE_PACK,

        /// A shader-pack entry.
        SHADER_PACK
    }
}
