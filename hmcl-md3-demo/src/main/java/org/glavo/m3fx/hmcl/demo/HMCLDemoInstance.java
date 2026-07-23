// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Describes one immutable dummy game instance.
///
/// @param id the stable instance identifier
/// @param name the displayed instance name
/// @param gameVersion the displayed game version
/// @param loader the displayed loader or distribution label
/// @param directoryId the owning game-directory identifier
/// @param iconPath the generated icon asset path
/// @param mods the immutable installed-mod list
@NotNullByDefault
public record HMCLDemoInstance(
        String id,
        String name,
        String gameVersion,
        String loader,
        String directoryId,
        String iconPath,
        @Unmodifiable List<HMCLDemoMod> mods
) {
    /// Copies mutable constructor inputs to retain value semantics.
    public HMCLDemoInstance {
        mods = List.copyOf(mods);
    }

    /// Returns a copy with a different identity and display name.
    ///
    /// @param newId the copied instance identifier
    /// @param newName the copied instance display name
    /// @return the copied instance
    public HMCLDemoInstance copyAs(String newId, String newName) {
        return new HMCLDemoInstance(newId, newName, gameVersion, loader, directoryId, iconPath, mods);
    }

    /// Returns a copy with a replacement mod list.
    ///
    /// @param newMods the replacement list
    /// @return the updated instance
    public HMCLDemoInstance withMods(@Unmodifiable List<HMCLDemoMod> newMods) {
        return new HMCLDemoInstance(id, name, gameVersion, loader, directoryId, iconPath, newMods);
    }
}
