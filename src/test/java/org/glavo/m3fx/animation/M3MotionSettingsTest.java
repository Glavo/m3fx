// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests runtime M3FX motion settings.
@NotNullByDefault
final class M3MotionSettingsTest {
    /// Verifies the global animation switch.
    @Test
    void globalAnimationSwitchControlsDefaultState() {
        boolean previous = M3MotionSettings.areAnimationsEnabled();
        try {
            M3MotionSettings.setAnimationsEnabled(false);
            assertFalse(M3MotionSettings.areAnimationsEnabled());

            M3MotionSettings.setAnimationsEnabled(true);
            assertTrue(M3MotionSettings.areAnimationsEnabled());
        } finally {
            M3MotionSettings.setAnimationsEnabled(previous);
        }
    }

    /// Verifies that node-local settings inherit through parent nodes.
    @Test
    void nodeAnimationSettingsInheritThroughParentChain() {
        boolean previous = M3MotionSettings.areAnimationsEnabled();
        try {
            Pane root = new Pane();
            Pane child = new Pane();
            Pane nested = new Pane();
            root.getChildren().add(child);
            child.getChildren().add(nested);

            M3MotionSettings.setAnimationsEnabled(true);
            assertTrue(M3MotionSettings.areAnimationsEnabled(nested));
            assertNull(M3MotionSettings.getAnimationsEnabled(nested));

            M3MotionSettings.setAnimationsEnabled(root, false);
            assertFalse(M3MotionSettings.areAnimationsEnabled(nested));

            M3MotionSettings.setAnimationsEnabled(child, true);
            assertTrue(M3MotionSettings.areAnimationsEnabled(nested));

            M3MotionSettings.clearAnimationsEnabled(child);
            assertFalse(M3MotionSettings.areAnimationsEnabled(nested));

            M3MotionSettings.clearAnimationsEnabled(root);
            assertTrue(M3MotionSettings.areAnimationsEnabled(nested));
        } finally {
            M3MotionSettings.setAnimationsEnabled(previous);
        }
    }
}
