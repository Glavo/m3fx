// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests runtime M3FX motion settings.
@NotNullByDefault
final class M3MotionSettingsTest {
    /// Verifies the application-wide reduced-motion request.
    @Test
    void globalReducedMotionRequestControlsDefaultState() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            M3MotionSettings.setGlobalReducedMotionRequested(true);
            assertTrue(M3MotionSettings.isGlobalReducedMotionRequested());
            assertTrue(M3MotionSettings.globalReducedMotionRequestedProperty().get());
            assertTrue(M3MotionSettings.shouldReduceMotion(owner));

            M3MotionSettings.setGlobalReducedMotionRequested(false);
            assertFalse(M3MotionSettings.isGlobalReducedMotionRequested());
            assertFalse(M3MotionSettings.shouldReduceMotion(owner));
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

            M3MotionSettings.setGlobalReducedMotionRequested(false);
            assertFalse(M3MotionSettings.shouldReduceMotion(nested));
            assertFalse(M3MotionSettings.isReducedMotionRequested(nested));

            M3MotionSettings.setReducedMotionRequested(root, true);
            assertTrue(M3MotionSettings.shouldReduceMotion(nested));

            M3MotionSettings.setReducedMotionRequested(child, false);
            assertTrue(M3MotionSettings.shouldReduceMotion(nested));
            assertFalse(M3MotionSettings.isReducedMotionRequested(child));

            M3MotionSettings.setReducedMotionRequested(child, false);
            assertTrue(M3MotionSettings.shouldReduceMotion(nested));

            M3MotionSettings.setReducedMotionRequested(root, false);
            assertFalse(M3MotionSettings.shouldReduceMotion(nested));

            M3MotionSettings.setGlobalReducedMotionRequested(true);
            M3MotionSettings.setReducedMotionRequested(child, false);
            assertTrue(M3MotionSettings.shouldReduceMotion(nested));
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

            M3MotionSettings.shouldReduceMotion(nested);
            assertFalse(root.hasProperties());
            assertFalse(child.hasProperties());
            assertFalse(nested.hasProperties());
        });
    }
}
