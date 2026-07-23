// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes one dummy Minecraft version available from the download center.
///
/// @param id the stable version identifier
/// @param name the displayed version name
/// @param releaseTime the deterministic release-time label
/// @param channel the release channel
@NotNullByDefault
public record HMCLDemoMinecraftVersion(
        String id,
        String name,
        String releaseTime,
        Channel channel
) {
    /// Identifies the Minecraft release channel shown by a version row.
    @NotNullByDefault
    public enum Channel {
        /// An official release build.
        RELEASE,

        /// A snapshot or preview build.
        SNAPSHOT,

        /// An old beta build.
        OLD_BETA,

        /// An old alpha build.
        OLD_ALPHA
    }
}
