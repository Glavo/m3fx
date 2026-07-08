// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies reachability observation for owner nodes and changing ancestor chains.
@NotNullByDefault
final class M3ReachabilityObserverTest {
    /// Starts the JavaFX toolkit before reachability tests create nodes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that owner and ancestor visibility or disabled changes invalidate the observer.
    @Test
    void ownerAndAncestorStateChangesInvalidateObserver() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane parent = new Pane(owner);
            AtomicInteger invalidations = new AtomicInteger();
            M3ReachabilityObserver observer = new M3ReachabilityObserver(owner, invalidations::incrementAndGet);

            observer.install();
            observer.install();
            parent.setVisible(false);
            owner.setDisable(true);

            assertEquals(2, invalidations.get(), "owner and ancestor reachability changes should invalidate once each");
        });
    }

    /// Verifies that moving the owner rebuilds the observed ancestor chain.
    @Test
    void movingOwnerRebuildsObservedAncestorChain() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane oldParent = new Pane(owner);
            Pane newParent = new Pane();
            Pane root = new Pane(oldParent, newParent);
            AtomicInteger invalidations = new AtomicInteger();
            M3ReachabilityObserver observer = new M3ReachabilityObserver(owner, invalidations::incrementAndGet);

            observer.install();
            oldParent.getChildren().remove(owner);
            newParent.getChildren().add(owner);
            int invalidationsAfterMove = invalidations.get();
            assertTrue(invalidationsAfterMove >= 2, "moving the owner should invalidate parent reachability");

            oldParent.setVisible(false);
            assertEquals(invalidationsAfterMove, invalidations.get(), "old ancestors should be detached from observation");

            newParent.setVisible(false);
            assertEquals(invalidationsAfterMove + 1, invalidations.get(), "new ancestors should be observed");

            root.getChildren().clear();
        });
    }

    /// Verifies that uninstalling removes owner and ancestor listeners.
    @Test
    void uninstallRemovesObservedChainListeners() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane parent = new Pane(owner);
            AtomicInteger invalidations = new AtomicInteger();
            M3ReachabilityObserver observer = new M3ReachabilityObserver(owner, invalidations::incrementAndGet);

            observer.install();
            observer.uninstall();
            parent.setVisible(false);
            owner.setDisable(true);

            assertEquals(0, invalidations.get(), "uninstalled observers should not receive reachability changes");
        });
    }
}