// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests runtime M3FX motion settings.
@NotNullByDefault
final class M3MotionSettingsTest {
    /// Verifies the global animation switch.
    @Test
    void globalAnimationSwitchControlsDefaultState() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setAnimationsEnabled(false);
            assertFalse(M3MotionSettings.areAnimationsEnabled());

            M3MotionSettings.setAnimationsEnabled(true);
            assertTrue(M3MotionSettings.areAnimationsEnabled());
        });
    }

    /// Verifies that node-local settings inherit through parent nodes.
    @Test
    void nodeAnimationSettingsInheritThroughParentChain() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane root = new Pane();
            Pane child = new Pane();
            Pane nested = new Pane();
            root.getChildren().add(child);
            child.getChildren().add(nested);

            M3MotionSettings.setAnimationsEnabled(true);
            assertTrue(M3MotionSettings.areAnimationsEnabled(nested));
            assertFalse(M3MotionSettings.isReducedMotionRequested(nested));

            M3MotionSettings.setReducedMotionRequested(root, true);
            assertFalse(M3MotionSettings.areAnimationsEnabled(nested));

            M3MotionSettings.setReducedMotionRequested(child, false);
            assertFalse(M3MotionSettings.areAnimationsEnabled(nested));
            assertFalse(M3MotionSettings.isReducedMotionRequested(child));

            M3MotionSettings.setReducedMotionRequested(child, false);
            assertFalse(M3MotionSettings.areAnimationsEnabled(nested));

            M3MotionSettings.setReducedMotionRequested(root, false);
            assertTrue(M3MotionSettings.areAnimationsEnabled(nested));
        });
    }

    /// Verifies that the settings revision changes when global or node-local motion settings change.
    @Test
    void settingsRevisionChangesWhenSettingsChange() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
            Pane node = new Pane();
            long revision = M3MotionSettings.revisionProperty().get();

            M3MotionSettings.setAnimationsEnabled(!previousAnimationsEnabled);
            long afterGlobalAnimations = M3MotionSettings.revisionProperty().get();
            assertTrue(afterGlobalAnimations > revision);

            M3MotionSettings.setReducedMotionRequested(node, true);
            long afterNodeAnimations = M3MotionSettings.revisionProperty().get();
            assertTrue(afterNodeAnimations > afterGlobalAnimations);

            M3MotionSettings.setReducedMotionRequested(node, true);

            assertEquals(afterNodeAnimations, M3MotionSettings.revisionProperty().get());
        });
    }

    /// Verifies read-only motion queries do not allocate JavaFX node property maps.
    @Test
    void motionQueriesDoNotAllocateNodePropertyMaps() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane root = new Pane();
            Pane child = new Pane();
            Pane nested = new Pane();
            root.getChildren().add(child);
            child.getChildren().add(nested);

            assertFalse(root.hasProperties());
            assertFalse(child.hasProperties());
            assertFalse(nested.hasProperties());

            M3MotionSettings.areAnimationsEnabled(nested);
            assertFalse(root.hasProperties());
            assertFalse(child.hasProperties());
            assertFalse(nested.hasProperties());
        });
    }
}
