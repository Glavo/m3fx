// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    /// Verifies the global motion scheme switch.
    @Test
    void globalMotionSchemeSwitchControlsDefaultScheme() {
        M3MotionScheme previous = M3MotionSettings.getMotionScheme();
        try {
            M3MotionSettings.setMotionScheme(M3MotionScheme.expressive());

            assertEquals(M3MotionEasing.EMPHASIZED, M3MotionSettings.getMotionScheme().defaultEffects().easing());

            M3MotionSettings.setMotionScheme(M3MotionScheme.standard());

            assertEquals(M3MotionEasing.STANDARD, M3MotionSettings.getMotionScheme().defaultEffects().easing());
        } finally {
            M3MotionSettings.setMotionScheme(previous);
        }
    }

    /// Verifies that node-local motion schemes can be set and cleared.
    @Test
    void nodeMotionSchemeStoresLocalOverride() {
        Pane node = new Pane();

        assertNull(M3MotionSettings.getMotionScheme(node));

        M3MotionSettings.setMotionScheme(node, M3MotionScheme.expressive());

        assertEquals(M3MotionEasing.EMPHASIZED, M3MotionSettings.getMotionScheme(node).defaultEffects().easing());

        M3MotionSettings.clearMotionScheme(node);

        assertNull(M3MotionSettings.getMotionScheme(node));
    }

    /// Verifies the global motion behavior switch.
    @Test
    void globalMotionBehaviorSwitchControlsDefaultBehavior() {
        M3MotionBehavior previous = M3MotionSettings.getMotionBehavior();
        try {
            M3MotionSettings.setMotionBehavior(M3MotionBehavior.expressive());

            assertEquals(4000.0, M3MotionSettings.getMotionBehavior().snackbarDisplayDuration().toMillis(), 0.0001);
            assertEquals(150.0, M3MotionSettings.getMotionBehavior().subMenuHoverOpenDelay().toMillis(), 0.0001);
            assertEquals(900.0, M3MotionSettings.getMotionBehavior().typeAheadResetDelay().toMillis(), 0.0001);
            assertEquals(650.0, M3MotionSettings.getMotionBehavior().loadingIndicatorMorphInterval().toMillis(), 0.0001);
            assertEquals(
                    4666.0,
                    M3MotionSettings.getMotionBehavior().loadingIndicatorGlobalRotationDuration().toMillis(),
                    0.0001
            );

            M3MotionSettings.setMotionBehavior(M3MotionBehavior.standard());

            assertEquals(4000.0, M3MotionSettings.getMotionBehavior().snackbarDisplayDuration().toMillis(), 0.0001);
            assertEquals(200.0, M3MotionSettings.getMotionBehavior().subMenuHoverOpenDelay().toMillis(), 0.0001);
            assertEquals(1000.0, M3MotionSettings.getMotionBehavior().typeAheadResetDelay().toMillis(), 0.0001);
            assertEquals(650.0, M3MotionSettings.getMotionBehavior().loadingIndicatorMorphInterval().toMillis(), 0.0001);
            assertEquals(
                    4666.0,
                    M3MotionSettings.getMotionBehavior().loadingIndicatorGlobalRotationDuration().toMillis(),
                    0.0001
            );
        } finally {
            M3MotionSettings.setMotionBehavior(previous);
        }
    }

    /// Verifies that node-local motion behavior can be set and cleared.
    @Test
    void nodeMotionBehaviorStoresLocalOverride() {
        Pane node = new Pane();

        assertNull(M3MotionSettings.getMotionBehavior(node));

        M3MotionSettings.setMotionBehavior(node, M3MotionBehavior.expressive());

        assertEquals(4000.0, M3MotionSettings.getMotionBehavior(node).snackbarDisplayDuration().toMillis(), 0.0001);
        assertEquals(150.0, M3MotionSettings.getMotionBehavior(node).subMenuHoverOpenDelay().toMillis(), 0.0001);
        assertEquals(900.0, M3MotionSettings.getMotionBehavior(node).typeAheadResetDelay().toMillis(), 0.0001);
        assertEquals(650.0, M3MotionSettings.getMotionBehavior(node).loadingIndicatorMorphInterval().toMillis(), 0.0001);

        M3MotionSettings.clearMotionBehavior(node);

        assertNull(M3MotionSettings.getMotionBehavior(node));
    }
}
