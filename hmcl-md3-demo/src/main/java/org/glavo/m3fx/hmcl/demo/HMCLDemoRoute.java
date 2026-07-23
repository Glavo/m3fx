// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies one page hosted by the HMCL Material 3 demo shell.
@NotNullByDefault
sealed interface HMCLDemoRoute {
    /// Primary home destination with launch controls.
    record Home() implements HMCLDemoRoute {
    }

    /// Account list secondary route.
    record Accounts() implements HMCLDemoRoute {
    }

    /// Primary instance-list destination.
    record Instances() implements HMCLDemoRoute {
    }

    /// Instance management secondary route.
    ///
    /// @param instanceId the selected instance identifier
    /// @param section the active management section
    record Instance(String instanceId, InstanceSection section) implements HMCLDemoRoute {
        /// Creates an instance route for the settings section.
        ///
        /// @param instanceId the selected instance identifier
        Instance(String instanceId) {
            this(instanceId, InstanceSection.SETTINGS);
        }
    }

    /// Primary download-center destination.
    ///
    /// @param category the active download category
    record Download(DownloadCategory category) implements HMCLDemoRoute {
        /// Creates a download route for official game versions.
        Download() {
            this(DownloadCategory.GAME);
        }
    }

    /// Primary settings destination.
    ///
    /// @param section the active settings section
    record Settings(SettingsSection section) implements HMCLDemoRoute {
        /// Creates a settings route for global game settings.
        Settings() {
            this(SettingsSection.GLOBAL_GAME);
        }
    }

    /// Multiplayer secondary route.
    record Multiplayer() implements HMCLDemoRoute {
    }

    /// Returns whether this route is one of the four primary destinations.
    ///
    /// @return `true` for home, instances, download, and settings
    default boolean isPrimary() {
        return this instanceof Home
                || this instanceof Instances
                || this instanceof Download
                || this instanceof Settings;
    }

    /// Returns the primary destination that owns this route for navigation selection.
    ///
    /// @return the primary route used by the bar and rail
    default HMCLDemoRoute primaryDestination() {
        if (this instanceof Instance || this instanceof Accounts || this instanceof Multiplayer) {
            if (this instanceof Instance) {
                return new Instances();
            }
            return new Home();
        }
        if (this instanceof Download download) {
            return new Download(download.category());
        }
        if (this instanceof Settings settings) {
            return new Settings(settings.section());
        }
        if (this instanceof Instances) {
            return new Instances();
        }
        return new Home();
    }

    /// Sections shown by the instance management page.
    enum InstanceSection {
        /// Version settings.
        SETTINGS,

        /// Loader and installer slots.
        INSTALLERS,

        /// Mods.
        MODS,

        /// Resource packs.
        RESOURCE_PACKS,

        /// Worlds.
        WORLDS,

        /// Shader packs.
        SHADERS,

        /// Schematics.
        SCHEMATICS
    }

    /// Categories shown by the download center.
    enum DownloadCategory {
        /// Official Minecraft versions.
        GAME,

        /// Modpacks.
        MODPACK,

        /// Mods.
        MOD,

        /// Resource packs.
        RESOURCE_PACK,

        /// Shader packs.
        SHADER,

        /// Worlds.
        WORLD
    }

    /// Sections shown by the settings page.
    enum SettingsSection {
        /// Global game settings.
        GLOBAL_GAME,

        /// Java management.
        JAVA,

        /// General launcher settings.
        GENERAL,

        /// Appearance and theme.
        APPEARANCE,

        /// Download and proxy settings.
        DOWNLOAD,

        /// Help topics.
        HELP,

        /// Feedback links.
        FEEDBACK,

        /// About information.
        ABOUT
    }
}
