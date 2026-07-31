// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/// Verifies effective node-tree visibility used by render-activity decisions.
@NotNullByDefault
final class M3PresentationActivityTest {
    /// Starts the JavaFX toolkit before tests mutate node properties.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that tree visibility follows only the visible property of the node and its ancestors.
    @Test
    void treeVisibilityFollowsVisibleAncestorChain() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane parent = new Pane(owner);
            new Pane(parent);

            assertTrue(M3PresentationActivity.isTreeVisible(owner));
            assertFalse(M3PresentationActivity.isTreeVisible(null));

            owner.setManaged(false);
            owner.setOpacity(0.0);
            owner.setDisable(true);
            assertTrue(M3PresentationActivity.isTreeVisible(owner));

            parent.setVisible(false);
            assertFalse(M3PresentationActivity.isTreeVisible(owner));

            parent.setVisible(true);
            owner.setVisible(false);
            assertFalse(M3PresentationActivity.isTreeVisible(owner));

            owner.setVisible(true);
            assertTrue(M3PresentationActivity.isTreeVisible(owner));
        });
    }

    /// Verifies the native JavaFX tree-visible property when the runtime module is open to the test process.
    @Test
    void nativeTreeVisiblePropertyFollowsVisibleAncestorChainWhenAccessible() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane parent = new Pane(owner);
            SubScene subScene = new SubScene(new Pane(parent), 100.0, 100.0);
            new Scene(new Pane(subScene));
            @Nullable ObservableBooleanValue property = M3TreeVisibility.treeVisibleProperty(owner);
            assumeTrue(property != null, "JavaFX does not expose Node.treeVisibleProperty() to this test process");

            assertTrue(property.get());
            parent.setVisible(false);
            assertFalse(property.get());
            parent.setVisible(true);
            assertTrue(property.get());

            subScene.setVisible(false);
            assertFalse(property.get());
            subScene.setVisible(true);
            assertTrue(property.get());
        });
    }
}
