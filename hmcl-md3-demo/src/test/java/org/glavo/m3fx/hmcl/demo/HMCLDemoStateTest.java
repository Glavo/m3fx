// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies deterministic fixture data and interactive demo-state transitions.
@NotNullByDefault
final class HMCLDemoStateTest {
    /// Verifies fixture sizes, default appearance, selection, copying, deletion, and mod mutation.
    @Test
    void exposesAndMutatesDeterministicFixtures() {
        HMCLDemoState state = new HMCLDemoState(new HMCLDemoStrings(Locale.ENGLISH));

        assertEquals(3, state.getAccounts().size());
        assertEquals(2, state.getDirectories().size());
        assertEquals(5, state.getInstances().size());
        assertEquals(12, state.getMinecraftVersions().size());
        assertEquals(3, state.getJavaRuntimes().size());
        assertEquals(Color.web("#5C6BC0"), state.getThemeColor());
        assertTrue(state.getCatalog(HMCLDemoCatalogItem.Kind.MOD).size() >= 4);
        assertTrue(state.getCatalog(HMCLDemoCatalogItem.Kind.MODPACK).size() >= 4);
        assertTrue(state.getCatalog(HMCLDemoCatalogItem.Kind.RESOURCE_PACK).size() >= 4);
        assertTrue(state.getCatalog(HMCLDemoCatalogItem.Kind.SHADER).size() >= 4);
        assertTrue(state.getCatalog(HMCLDemoCatalogItem.Kind.WORLD).size() >= 4);

        assertTrue(state.selectInstance("forge-1-12-2"));
        HMCLDemoInstance copy = Objects.requireNonNull(state.copySelectedInstance());
        assertEquals(6, state.getInstances().size());
        assertEquals(copy, state.getSelectedInstance());
        assertTrue(state.deleteSelectedInstance());
        assertEquals(5, state.getInstances().size());

        assertTrue(state.selectInstance("fabulously-optimized"));
        assertTrue(state.setSelectedModEnabled("sodium", false));
        HMCLDemoInstance selected = Objects.requireNonNull(state.getSelectedInstance());
        assertFalse(selected.mods().get(0).enabled());
        assertFalse(selected.installers().isEmpty());
        assertFalse(selected.resourcePacks().isEmpty());
        assertFalse(selected.shaderPacks().isEmpty());
        assertFalse(selected.worlds().isEmpty());

        assertTrue(state.selectAccount("alex"));
        assertTrue(state.removeSelectedAccount());
        assertEquals(2, state.getAccounts().size());
    }

    /// Verifies directory and search filtering for the instance list.
    @Test
    void filtersInstancesByDirectoryAndQuery() {
        HMCLDemoState state = new HMCLDemoState();

        assertEquals(4, state.getFilteredInstances().size());
        assertTrue(state.selectDirectory("minecraft2"));
        assertEquals(1, state.getFilteredInstances().size());
        assertEquals("lab-minecraft2", state.getFilteredInstances().get(0).id());

        assertTrue(state.selectDirectory("minecraft"));
        state.setInstanceSearchQuery("fabric");
        assertEquals(1, state.getFilteredInstances().size());
        assertEquals("fabulously-optimized", state.getFilteredInstances().get(0).id());
    }

    /// Verifies install wizard state writes a selected instance with the chosen loader.
    @Test
    void installsInstanceFromWizardDraft() {
        HMCLDemoState state = new HMCLDemoState(new HMCLDemoStrings(Locale.ENGLISH));

        HMCLDemoInstance installed = state.installInstance("Demo Fabric", "1.21.11", "fabric", "0.16.14");
        assertEquals("Demo Fabric", installed.name());
        assertEquals("1.21.11", installed.gameVersion());
        assertTrue(installed.loader().contains("Fabric"));
        assertEquals(installed, state.getSelectedInstance());
        assertEquals(6, state.getInstances().size());
    }

    /// Verifies Minecraft version channel and search filters.
    @Test
    void filtersMinecraftVersionsDeterministically() {
        HMCLDemoState state = new HMCLDemoState();

        state.setShowSnapshotVersions(false);
        state.setShowOldVersions(false);
        assertTrue(state.getFilteredMinecraftVersions().stream()
                .allMatch(version -> version.channel() == HMCLDemoMinecraftVersion.Channel.RELEASE));

        state.setShowReleaseVersions(false);
        state.setShowSnapshotVersions(true);
        state.setVersionSearchQuery("26.3");
        assertEquals(1, state.getFilteredMinecraftVersions().size());
        assertEquals("26.3-snapshot-4", state.getFilteredMinecraftVersions().get(0).id());
    }

    /// Verifies installation progress completion and cancellation.
    @Test
    void advancesInstallationProgress() {
        HMCLDemoState state = new HMCLDemoState();
        state.beginInstallation("1.21.11 + Fabric");
        assertEquals("1.21.11 + Fabric", state.getInstallingTitle());
        state.setInstallProgress(0.5);
        assertEquals(0.5, state.getInstallProgress());
        assertTrue(state.cancelInstallation());
        assertEquals(null, state.getInstallingTitle());

        state.beginInstallation("26.1 + Vanilla");
        state.setInstallProgress(1.0);
        assertEquals(null, state.getInstallingTitle());
    }

    /// Verifies multiplayer phase helpers and launcher settings properties.
    @Test
    void multiplayerAndSettingsMutations() {
        HMCLDemoState state = new HMCLDemoState();

        assertEquals(HMCLDemoState.MultiplayerPhase.WAITING, state.getMultiplayerPhase());
        String room = state.startHost();
        assertEquals(HMCLDemoState.MultiplayerPhase.HOSTING, state.getMultiplayerPhase());
        assertEquals(room, state.getMultiplayerRoomCode());
        state.startJoin("JOIN-42");
        assertEquals(HMCLDemoState.MultiplayerPhase.JOINING, state.getMultiplayerPhase());
        assertEquals("JOIN-42", state.getMultiplayerRoomCode());
        state.resetMultiplayer();
        assertEquals(HMCLDemoState.MultiplayerPhase.WAITING, state.getMultiplayerPhase());
        assertEquals("", state.getMultiplayerRoomCode());

        state.setUpdateChannel("dev");
        state.setAutoAllocateMemory(false);
        state.setDownloadThreads(32);
        state.setDownloadSource("bmclapi");
        state.setAnimationDisabled(true);
        state.setGlobalMaxMemoryMb(8192);
        assertEquals("dev", state.getUpdateChannel());
        assertFalse(state.isAutoAllocateMemory());
        assertEquals(32, state.getDownloadThreads());
        assertEquals("bmclapi", state.getDownloadSource());
        assertTrue(state.isAnimationDisabled());
        assertEquals(8192, state.getGlobalMaxMemoryMb());
    }

    /// Verifies selected-instance content mutations used by management pages.
    @Test
    void mutatesSelectedInstanceContent() {
        HMCLDemoState state = new HMCLDemoState();
        assertTrue(state.selectInstance("fabulously-optimized"));

        assertTrue(state.renameSelectedInstance("FO Renamed"));
        assertEquals("FO Renamed", Objects.requireNonNull(state.getSelectedInstance()).name());
        assertTrue(state.updateSelectedInstanceSettings(true, 8192, "2560x1440", true, "java-21"));
        HMCLDemoInstance settings = Objects.requireNonNull(state.getSelectedInstance());
        assertTrue(settings.isolated());
        assertEquals(8192, settings.maxMemoryMb());
        assertEquals("2560x1440", settings.resolution());
        assertTrue(settings.fullscreen());
        assertEquals("java-21", settings.javaId());

        int modCount = settings.mods().size();
        assertNotNull(state.addDemoMod());
        assertEquals(modCount + 1, Objects.requireNonNull(state.getSelectedInstance()).mods().size());
        assertTrue(state.removeMod("sodium"));

        assertTrue(state.setResourcePackEnabled("faithful", false));
        assertFalse(Objects.requireNonNull(state.getSelectedInstance()).resourcePacks().get(0).enabled());
        assertNotNull(state.addDemoResourcePack());

        assertTrue(state.setShaderEnabled("complementary", false));
        assertNotNull(state.addDemoShader());

        int worldCount = Objects.requireNonNull(state.getSelectedInstance()).worlds().size();
        assertNotNull(state.addDemoWorld());
        assertEquals(worldCount + 1, Objects.requireNonNull(state.getSelectedInstance()).worlds().size());
        assertTrue(state.removeWorld("fo-survival"));

        assertNotNull(state.addDemoSchematic());
        assertTrue(state.setInstallerVersion("optifine", "I8"));
        assertEquals("I8", Objects.requireNonNull(state.getSelectedInstance())
                .installers().stream()
                .filter(installer -> installer.id().equals("optifine"))
                .findFirst()
                .orElseThrow()
                .installedVersion());
    }
}
