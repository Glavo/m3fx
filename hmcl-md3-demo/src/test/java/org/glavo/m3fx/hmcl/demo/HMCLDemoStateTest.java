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
        assertEquals(Color.web("#5C6BC0"), state.getThemeColor());

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
}
