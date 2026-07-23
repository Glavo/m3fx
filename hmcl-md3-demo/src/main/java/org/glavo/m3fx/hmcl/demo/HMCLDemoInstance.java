// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// One immutable in-memory game instance with managed content.
///
/// @param id stable identifier
/// @param name display name
/// @param gameVersion Minecraft version
/// @param loader loader summary label
/// @param directoryId owning game directory
/// @param iconPath icon asset path
/// @param isolated whether the instance uses an isolated working directory
/// @param maxMemoryMb configured max memory
/// @param resolution window resolution label
/// @param fullscreen whether fullscreen is preferred
/// @param javaId selected Java runtime id, or `auto`
/// @param installers loader/component slots
/// @param mods mods
/// @param resourcePacks resource packs
/// @param shaderPacks shader packs
/// @param worlds worlds
/// @param schematics schematics
@NotNullByDefault
public record HMCLDemoInstance(
        String id,
        String name,
        String gameVersion,
        String loader,
        String directoryId,
        String iconPath,
        boolean isolated,
        int maxMemoryMb,
        String resolution,
        boolean fullscreen,
        String javaId,
        @Unmodifiable List<HMCLDemoInstaller> installers,
        @Unmodifiable List<HMCLDemoMod> mods,
        @Unmodifiable List<HMCLDemoPack> resourcePacks,
        @Unmodifiable List<HMCLDemoPack> shaderPacks,
        @Unmodifiable List<HMCLDemoWorld> worlds,
        @Unmodifiable List<HMCLDemoPack> schematics
) {
    /// Copies mutable constructor inputs.
    public HMCLDemoInstance {
        installers = List.copyOf(installers);
        mods = List.copyOf(mods);
        resourcePacks = List.copyOf(resourcePacks);
        shaderPacks = List.copyOf(shaderPacks);
        worlds = List.copyOf(worlds);
        schematics = List.copyOf(schematics);
    }

    /// Returns a renamed copy with a new id.
    public HMCLDemoInstance copyAs(String newId, String newName) {
        return new HMCLDemoInstance(
                newId, newName, gameVersion, loader, directoryId, iconPath, isolated, maxMemoryMb, resolution,
                fullscreen, javaId, installers, mods, resourcePacks, shaderPacks, worlds, schematics);
    }

    /// Returns a copy with a new display name.
    public HMCLDemoInstance withName(String newName) {
        return new HMCLDemoInstance(
                id, newName, gameVersion, loader, directoryId, iconPath, isolated, maxMemoryMb, resolution,
                fullscreen, javaId, installers, mods, resourcePacks, shaderPacks, worlds, schematics);
    }

    /// Returns a copy with updated settings fields.
    public HMCLDemoInstance withSettings(
            boolean newIsolated,
            int newMaxMemoryMb,
            String newResolution,
            boolean newFullscreen,
            String newJavaId
    ) {
        return new HMCLDemoInstance(
                id, name, gameVersion, loader, directoryId, iconPath, newIsolated, newMaxMemoryMb, newResolution,
                newFullscreen, newJavaId, installers, mods, resourcePacks, shaderPacks, worlds, schematics);
    }

    /// Returns a copy with replacement mods.
    public HMCLDemoInstance withMods(@Unmodifiable List<HMCLDemoMod> newMods) {
        return new HMCLDemoInstance(
                id, name, gameVersion, loader, directoryId, iconPath, isolated, maxMemoryMb, resolution,
                fullscreen, javaId, installers, newMods, resourcePacks, shaderPacks, worlds, schematics);
    }

    /// Returns a copy with replacement resource packs.
    public HMCLDemoInstance withResourcePacks(@Unmodifiable List<HMCLDemoPack> packs) {
        return new HMCLDemoInstance(
                id, name, gameVersion, loader, directoryId, iconPath, isolated, maxMemoryMb, resolution,
                fullscreen, javaId, installers, mods, packs, shaderPacks, worlds, schematics);
    }

    /// Returns a copy with replacement shader packs.
    public HMCLDemoInstance withShaderPacks(@Unmodifiable List<HMCLDemoPack> packs) {
        return new HMCLDemoInstance(
                id, name, gameVersion, loader, directoryId, iconPath, isolated, maxMemoryMb, resolution,
                fullscreen, javaId, installers, mods, resourcePacks, packs, worlds, schematics);
    }

    /// Returns a copy with replacement worlds.
    public HMCLDemoInstance withWorlds(@Unmodifiable List<HMCLDemoWorld> newWorlds) {
        return new HMCLDemoInstance(
                id, name, gameVersion, loader, directoryId, iconPath, isolated, maxMemoryMb, resolution,
                fullscreen, javaId, installers, mods, resourcePacks, shaderPacks, newWorlds, schematics);
    }

    /// Returns a copy with replacement schematics.
    public HMCLDemoInstance withSchematics(@Unmodifiable List<HMCLDemoPack> packs) {
        return new HMCLDemoInstance(
                id, name, gameVersion, loader, directoryId, iconPath, isolated, maxMemoryMb, resolution,
                fullscreen, javaId, installers, mods, resourcePacks, shaderPacks, worlds, packs);
    }

    /// Returns a copy with replacement installers.
    public HMCLDemoInstance withInstallers(@Unmodifiable List<HMCLDemoInstaller> newInstallers) {
        return new HMCLDemoInstance(
                id, name, gameVersion, loader, directoryId, iconPath, isolated, maxMemoryMb, resolution,
                fullscreen, javaId, newInstallers, mods, resourcePacks, shaderPacks, worlds, schematics);
    }
}
