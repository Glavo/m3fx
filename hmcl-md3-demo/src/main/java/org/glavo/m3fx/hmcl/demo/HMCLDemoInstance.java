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
/// @param loader the displayed loader name
/// @param lastPlayed the deterministic localized-neutral last-played label
/// @param description the instance summary
/// @param iconName the logical demo icon name
/// @param status the current instance status
/// @param mods the immutable installed-mod list
@NotNullByDefault
public record HMCLDemoInstance(
        String id,
        String name,
        String gameVersion,
        String loader,
        String lastPlayed,
        String description,
        String iconName,
        InstanceStatus status,
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
        return new HMCLDemoInstance(
                newId, newName, gameVersion, loader, lastPlayed, description, iconName, status, mods);
    }

    /// Returns a copy with a replacement mod list.
    ///
    /// @param newMods the replacement list
    /// @return the updated instance
    public HMCLDemoInstance withMods(@Unmodifiable List<HMCLDemoMod> newMods) {
        return new HMCLDemoInstance(
                id, name, gameVersion, loader, lastPlayed, description, iconName, status, newMods);
    }

    /// Describes the launch readiness of a dummy instance.
    @NotNullByDefault
    public enum InstanceStatus {
        /// The instance is ready to launch.
        READY,

        /// The instance has an available update.
        UPDATE_AVAILABLE,

        /// The instance is currently running.
        RUNNING,

        /// The instance requires repair before launch.
        NEEDS_REPAIR
    }
}
