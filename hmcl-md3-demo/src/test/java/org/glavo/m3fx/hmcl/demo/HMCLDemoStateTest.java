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
        assertEquals(6, state.getInstances().size());
        assertEquals(8, state.getContents().size());
        assertEquals(Color.web("#5C6BC0"), state.getThemeColor());

        assertTrue(state.selectInstance("engineering-lab"));
        HMCLDemoInstance copy = Objects.requireNonNull(state.copySelectedInstance());
        assertEquals(7, state.getInstances().size());
        assertEquals(copy, state.getSelectedInstance());
        assertTrue(state.deleteSelectedInstance());
        assertEquals(6, state.getInstances().size());

        assertTrue(state.selectInstance("creative-workshop"));
        assertTrue(state.setSelectedModEnabled("sodium", false));
        HMCLDemoInstance selected = Objects.requireNonNull(state.getSelectedInstance());
        assertFalse(selected.mods().get(1).enabled());

        assertTrue(state.selectAccount("maple"));
        assertTrue(state.removeSelectedAccount());
        assertEquals(2, state.getAccounts().size());
    }

    /// Verifies case-insensitive catalog filtering across metadata fields.
    @Test
    void filtersDiscoverCatalogDeterministically() {
        HMCLDemoState state = new HMCLDemoState();

        state.setSearchQuery("northwind");
        assertEquals(1, state.getFilteredContents().size());
        assertEquals("sodium-skies", state.getFilteredContents().get(0).id());

        state.setSearchQuery("SHADER");
        assertEquals(1, state.getFilteredContents().size());
        assertEquals("aurora-path", state.getFilteredContents().get(0).id());

        state.setSearchQuery("   ");
        assertEquals(state.getContents(), state.getFilteredContents());
    }

    /// Verifies failure, cancellation, retry, completion, and installed-item guards.
    @Test
    void advancesInstallationState() {
        HMCLDemoState state = new HMCLDemoState();
        assertTrue(state.selectContent("builders-compass"));
        HMCLDemoContent content = Objects.requireNonNull(state.getSelectedContent());

        assertEquals(HMCLDemoState.InstallState.AVAILABLE, state.installStateFor(content));
        assertTrue(state.startInstallation(content));
        state.setInstallProgress(0.5);
        assertEquals(0.5, state.getInstallProgress());
        assertTrue(state.failInstallation());
        assertEquals(HMCLDemoState.InstallState.FAILED, state.installStateFor(content));
        assertTrue(state.cancelInstallation());

        assertTrue(state.startInstallation(content));
        state.setInstallProgress(1.0);
        assertEquals(HMCLDemoState.InstallState.INSTALLED, state.installStateFor(content));
        assertFalse(state.startInstallation(content));
    }
}
