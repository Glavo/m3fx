// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

    /// Reports repeated state changes even when the callback does not read the observed properties.
    @Test
    void repeatedStateChangesRemainObservable() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane parent = new Pane(owner);
            AtomicInteger invalidations = new AtomicInteger();
            M3ReachabilityObserver observer = new M3ReachabilityObserver(owner, invalidations::incrementAndGet);
            observer.install();
            for (int index = 0; index < 4; index++) {
                parent.setVisible(index % 2 != 0);
                owner.setDisable(index % 2 == 0);
            }
            assertEquals(8, invalidations.get());
            observer.uninstall();
        });
    }

    /// Observes visibility across a SubScene boundary when native tree visibility is available.
    @Test
    void nativeVisibilityChangesInvalidateSubSceneOwner() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            SubScene subScene = new SubScene(new Pane(owner), 100.0, 100.0);
            new Scene(new Pane(subScene));
            assumeTrue(M3TreeVisibility.treeVisibleProperty(owner) != null,
                    "JavaFX does not expose native tree visibility to this test process");
            AtomicInteger invalidations = new AtomicInteger();
            M3ReachabilityObserver observer = new M3ReachabilityObserver(owner, invalidations::incrementAndGet);
            observer.install();
            subScene.setVisible(false);
            assertEquals(1, invalidations.get());
            subScene.setVisible(true);
            assertEquals(2, invalidations.get());
            observer.uninstall();
            subScene.setVisible(false);
            assertEquals(2, invalidations.get());
            observer.install();
            subScene.setVisible(true);
            assertEquals(3, invalidations.get());
            observer.uninstall();
        });
    }

    /// Uses native tree visibility for reachability across a SubScene boundary when available.
    @Test
    void nativeReachabilityFollowsSubSceneVisibility() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            SubScene subScene = new SubScene(new Pane(owner), 100.0, 100.0);
            new Scene(new Pane(subScene));
            assumeTrue(M3TreeVisibility.treeVisibleProperty(owner) != null,
                    "JavaFX does not expose native tree visibility to this test process");
            assertTrue(M3Accessible.canReach(owner));
            owner.setVisible(false);
            assertTrue(M3Accessible.canReveal(owner));
            subScene.setVisible(false);
            assertFalse(M3Accessible.canReveal(owner));
            owner.setVisible(true);
            assertFalse(M3Accessible.canReach(owner));
            subScene.setVisible(true);
            assertTrue(M3Accessible.canReach(owner));
            subScene.setDisable(true);
            assertFalse(M3Accessible.canReach(owner));
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
