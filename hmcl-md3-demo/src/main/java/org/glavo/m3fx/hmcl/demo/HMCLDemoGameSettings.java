// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Offline game-launch settings shared by the global preset and per-instance overrides.
///
/// Field names follow HMCL `GameSettings` concepts. Values are session-only demo state.
///
/// @param javaMode `auto`, `version`, `detected`, or `custom`
/// @param javaId detected runtime id when `javaMode` is `detected`, otherwise empty
/// @param javaVersion requested major version when `javaMode` is `version`
/// @param javaPath custom Java binary path when `javaMode` is `custom`
/// @param autoMemory whether max memory is chosen automatically
/// @param maxMemoryMb max heap in mebibytes when manual
/// @param minMemoryMb min heap in mebibytes
/// @param metaspaceMb permanent generation / metaspace size in mebibytes
/// @param windowType `windowed`, `fullscreen`, `borderless`, or `maximized`
/// @param resolution windowed size label such as `854x480`
/// @param launcherVisibility `hide`, `keep`, or `close`
/// @param allowAutoAgent whether launcher may inject agents automatically
/// @param disableAutoGameOptions whether automatic game-option patches are disabled
/// @param showLogs whether the log window is shown while the game runs
/// @param enableDebugLog whether verbose launcher logging is enabled
/// @param skipGameCheck whether game integrity checks are skipped
/// @param quickPlayType `none`, `multiplayer`, `singleplayer`, or `realms`
/// @param quickPlayMultiplayer multiplayer address for quick play
/// @param quickPlaySingleplayer world name for quick play
/// @param quickPlayRealms realms id for quick play
/// @param runningDirectory custom run directory path, or empty for default
/// @param gameArguments extra Minecraft arguments
/// @param environmentVariables extra environment variables text
/// @param processPriority `high`, `normal`, or `low`
/// @param noJvmArgs whether launcher JVM options are disabled
/// @param noOptimizingJvmArgs whether optimizing JVM options are disabled
/// @param skipJvmCheck whether JVM validity checks are skipped
/// @param jvmArgs extra JVM options
/// @param wrapper command wrapper, or empty
/// @param preLaunch pre-launch command, or empty
/// @param postExit post-exit command, or empty
/// @param isolated whether the instance uses an isolated game directory
/// @param useGlobalPreset whether instance settings follow the global preset (instance UI only)
@NotNullByDefault
public record HMCLDemoGameSettings(
        String javaMode,
        String javaId,
        String javaVersion,
        String javaPath,
        boolean autoMemory,
        int maxMemoryMb,
        int minMemoryMb,
        int metaspaceMb,
        String windowType,
        String resolution,
        String launcherVisibility,
        boolean allowAutoAgent,
        boolean disableAutoGameOptions,
        boolean showLogs,
        boolean enableDebugLog,
        boolean skipGameCheck,
        String quickPlayType,
        String quickPlayMultiplayer,
        String quickPlaySingleplayer,
        String quickPlayRealms,
        String runningDirectory,
        String gameArguments,
        String environmentVariables,
        String processPriority,
        boolean noJvmArgs,
        boolean noOptimizingJvmArgs,
        boolean skipJvmCheck,
        String jvmArgs,
        String wrapper,
        String preLaunch,
        String postExit,
        boolean isolated,
        boolean useGlobalPreset
) {
    /// Creates the offline global-preset defaults.
    ///
    /// @return the default settings
    public static HMCLDemoGameSettings globalDefaults() {
        return new HMCLDemoGameSettings(
                "auto",
                "",
                "21",
                "",
                true,
                4096,
                512,
                256,
                "windowed",
                "854x480",
                "hide",
                true,
                false,
                false,
                false,
                false,
                "none",
                "",
                "",
                "",
                "",
                "",
                "",
                "normal",
                false,
                false,
                false,
                "",
                "",
                "",
                "",
                false,
                true
        );
    }

    /// Creates instance defaults derived from the global preset values.
    ///
    /// @param global the global preset
    /// @param isolated whether the instance is isolated
    /// @return the instance settings
    public static HMCLDemoGameSettings instanceDefaults(HMCLDemoGameSettings global, boolean isolated) {
        return global.withIsolated(isolated).withUseGlobalPreset(true);
    }

    /// Returns a copy with the requested isolation flag.
    public HMCLDemoGameSettings withIsolated(boolean value) {
        return value == isolated ? this : copyWith(isolated, useGlobalPreset, javaMode, javaId, javaVersion, javaPath,
                autoMemory, maxMemoryMb, minMemoryMb, metaspaceMb, windowType, resolution, launcherVisibility,
                allowAutoAgent, disableAutoGameOptions, showLogs, enableDebugLog, skipGameCheck, quickPlayType,
                quickPlayMultiplayer, quickPlaySingleplayer, quickPlayRealms, runningDirectory, gameArguments,
                environmentVariables, processPriority, noJvmArgs, noOptimizingJvmArgs, skipJvmCheck, jvmArgs,
                wrapper, preLaunch, postExit);
    }

    /// Returns a copy with the requested global-preset follow flag.
    public HMCLDemoGameSettings withUseGlobalPreset(boolean value) {
        return value == useGlobalPreset ? this : copyWith(isolated, value, javaMode, javaId, javaVersion, javaPath,
                autoMemory, maxMemoryMb, minMemoryMb, metaspaceMb, windowType, resolution, launcherVisibility,
                allowAutoAgent, disableAutoGameOptions, showLogs, enableDebugLog, skipGameCheck, quickPlayType,
                quickPlayMultiplayer, quickPlaySingleplayer, quickPlayRealms, runningDirectory, gameArguments,
                environmentVariables, processPriority, noJvmArgs, noOptimizingJvmArgs, skipJvmCheck, jvmArgs,
                wrapper, preLaunch, postExit);
    }

    /// Returns a copy with Java selection fields replaced.
    public HMCLDemoGameSettings withJava(String mode, String id, String version, String path) {
        return copyWith(isolated, useGlobalPreset, mode, id, version, path, autoMemory, maxMemoryMb, minMemoryMb,
                metaspaceMb, windowType, resolution, launcherVisibility, allowAutoAgent, disableAutoGameOptions,
                showLogs, enableDebugLog, skipGameCheck, quickPlayType, quickPlayMultiplayer, quickPlaySingleplayer,
                quickPlayRealms, runningDirectory, gameArguments, environmentVariables, processPriority, noJvmArgs,
                noOptimizingJvmArgs, skipJvmCheck, jvmArgs, wrapper, preLaunch, postExit);
    }

    /// Returns a copy with memory fields replaced.
    public HMCLDemoGameSettings withMemory(boolean auto, int maxMb, int minMb, int metaMb) {
        return copyWith(isolated, useGlobalPreset, javaMode, javaId, javaVersion, javaPath, auto, maxMb, minMb,
                metaMb, windowType, resolution, launcherVisibility, allowAutoAgent, disableAutoGameOptions, showLogs,
                enableDebugLog, skipGameCheck, quickPlayType, quickPlayMultiplayer, quickPlaySingleplayer,
                quickPlayRealms, runningDirectory, gameArguments, environmentVariables, processPriority, noJvmArgs,
                noOptimizingJvmArgs, skipJvmCheck, jvmArgs, wrapper, preLaunch, postExit);
    }

    /// Returns a copy with window fields replaced.
    public HMCLDemoGameSettings withWindow(String type, String size) {
        return copyWith(isolated, useGlobalPreset, javaMode, javaId, javaVersion, javaPath, autoMemory, maxMemoryMb,
                minMemoryMb, metaspaceMb, type, size, launcherVisibility, allowAutoAgent, disableAutoGameOptions,
                showLogs, enableDebugLog, skipGameCheck, quickPlayType, quickPlayMultiplayer, quickPlaySingleplayer,
                quickPlayRealms, runningDirectory, gameArguments, environmentVariables, processPriority, noJvmArgs,
                noOptimizingJvmArgs, skipJvmCheck, jvmArgs, wrapper, preLaunch, postExit);
    }

    /// Returns a copy with launcher visibility replaced.
    public HMCLDemoGameSettings withLauncherVisibility(String value) {
        return copyWith(isolated, useGlobalPreset, javaMode, javaId, javaVersion, javaPath, autoMemory, maxMemoryMb,
                minMemoryMb, metaspaceMb, windowType, resolution, value, allowAutoAgent, disableAutoGameOptions,
                showLogs, enableDebugLog, skipGameCheck, quickPlayType, quickPlayMultiplayer, quickPlaySingleplayer,
                quickPlayRealms, runningDirectory, gameArguments, environmentVariables, processPriority, noJvmArgs,
                noOptimizingJvmArgs, skipJvmCheck, jvmArgs, wrapper, preLaunch, postExit);
    }

    /// Returns a copy with one boolean launcher flag replaced.
    public HMCLDemoGameSettings withFlag(
            boolean newAllowAutoAgent,
            boolean newDisableAutoGameOptions,
            boolean newShowLogs,
            boolean newEnableDebugLog,
            boolean newSkipGameCheck
    ) {
        return copyWith(isolated, useGlobalPreset, javaMode, javaId, javaVersion, javaPath, autoMemory, maxMemoryMb,
                minMemoryMb, metaspaceMb, windowType, resolution, launcherVisibility, newAllowAutoAgent,
                newDisableAutoGameOptions, newShowLogs, newEnableDebugLog, newSkipGameCheck, quickPlayType,
                quickPlayMultiplayer, quickPlaySingleplayer, quickPlayRealms, runningDirectory, gameArguments,
                environmentVariables, processPriority, noJvmArgs, noOptimizingJvmArgs, skipJvmCheck, jvmArgs,
                wrapper, preLaunch, postExit);
    }

    /// Returns a copy with quick-play fields replaced.
    public HMCLDemoGameSettings withQuickPlay(
            String type,
            String multiplayer,
            String singleplayer,
            String realms
    ) {
        return copyWith(isolated, useGlobalPreset, javaMode, javaId, javaVersion, javaPath, autoMemory, maxMemoryMb,
                minMemoryMb, metaspaceMb, windowType, resolution, launcherVisibility, allowAutoAgent,
                disableAutoGameOptions, showLogs, enableDebugLog, skipGameCheck, type, multiplayer, singleplayer,
                realms, runningDirectory, gameArguments, environmentVariables, processPriority, noJvmArgs,
                noOptimizingJvmArgs, skipJvmCheck, jvmArgs, wrapper, preLaunch, postExit);
    }

    /// Returns a copy with advanced launch fields replaced.
    public HMCLDemoGameSettings withAdvancedLaunch(
            String directory,
            String gameArgs,
            String env,
            String priority
    ) {
        return copyWith(isolated, useGlobalPreset, javaMode, javaId, javaVersion, javaPath, autoMemory, maxMemoryMb,
                minMemoryMb, metaspaceMb, windowType, resolution, launcherVisibility, allowAutoAgent,
                disableAutoGameOptions, showLogs, enableDebugLog, skipGameCheck, quickPlayType, quickPlayMultiplayer,
                quickPlaySingleplayer, quickPlayRealms, directory, gameArgs, env, priority, noJvmArgs,
                noOptimizingJvmArgs, skipJvmCheck, jvmArgs, wrapper, preLaunch, postExit);
    }

    /// Returns a copy with JVM advanced fields replaced.
    public HMCLDemoGameSettings withJvm(
            boolean noArgs,
            boolean noOptimize,
            boolean skipCheck,
            String args,
            String wrap,
            String pre,
            String post
    ) {
        return copyWith(isolated, useGlobalPreset, javaMode, javaId, javaVersion, javaPath, autoMemory, maxMemoryMb,
                minMemoryMb, metaspaceMb, windowType, resolution, launcherVisibility, allowAutoAgent,
                disableAutoGameOptions, showLogs, enableDebugLog, skipGameCheck, quickPlayType, quickPlayMultiplayer,
                quickPlaySingleplayer, quickPlayRealms, runningDirectory, gameArguments, environmentVariables,
                processPriority, noArgs, noOptimize, skipCheck, args, wrap, pre, post);
    }

    /// Shared constructor helper.
    private HMCLDemoGameSettings copyWith(
            boolean newIsolated,
            boolean newUseGlobalPreset,
            String newJavaMode,
            String newJavaId,
            String newJavaVersion,
            String newJavaPath,
            boolean newAutoMemory,
            int newMaxMemoryMb,
            int newMinMemoryMb,
            int newMetaspaceMb,
            String newWindowType,
            String newResolution,
            String newLauncherVisibility,
            boolean newAllowAutoAgent,
            boolean newDisableAutoGameOptions,
            boolean newShowLogs,
            boolean newEnableDebugLog,
            boolean newSkipGameCheck,
            String newQuickPlayType,
            String newQuickPlayMultiplayer,
            String newQuickPlaySingleplayer,
            String newQuickPlayRealms,
            String newRunningDirectory,
            String newGameArguments,
            String newEnvironmentVariables,
            String newProcessPriority,
            boolean newNoJvmArgs,
            boolean newNoOptimizingJvmArgs,
            boolean newSkipJvmCheck,
            String newJvmArgs,
            String newWrapper,
            String newPreLaunch,
            String newPostExit
    ) {
        return new HMCLDemoGameSettings(
                newJavaMode,
                newJavaId,
                newJavaVersion,
                newJavaPath,
                newAutoMemory,
                newMaxMemoryMb,
                newMinMemoryMb,
                newMetaspaceMb,
                newWindowType,
                newResolution,
                newLauncherVisibility,
                newAllowAutoAgent,
                newDisableAutoGameOptions,
                newShowLogs,
                newEnableDebugLog,
                newSkipGameCheck,
                newQuickPlayType,
                newQuickPlayMultiplayer,
                newQuickPlaySingleplayer,
                newQuickPlayRealms,
                newRunningDirectory,
                newGameArguments,
                newEnvironmentVariables,
                newProcessPriority,
                newNoJvmArgs,
                newNoOptimizingJvmArgs,
                newSkipJvmCheck,
                newJvmArgs,
                newWrapper,
                newPreLaunch,
                newPostExit,
                newIsolated,
                newUseGlobalPreset
        );
    }
}
