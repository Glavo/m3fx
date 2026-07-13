// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
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
            assertNull(M3MotionSettings.getAnimationsEnabled(nested));

            M3MotionSettings.setAnimationsEnabled(root, false);
            assertFalse(M3MotionSettings.areAnimationsEnabled(nested));

            M3MotionSettings.setAnimationsEnabled(child, true);
            assertFalse(M3MotionSettings.areAnimationsEnabled(nested));
            assertNull(M3MotionSettings.getAnimationsEnabled(child));

            M3MotionSettings.clearAnimationsEnabled(child);
            assertFalse(M3MotionSettings.areAnimationsEnabled(nested));

            M3MotionSettings.clearAnimationsEnabled(root);
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

            M3MotionSettings.setMotionScheme(M3MotionScheme.expressive());
            long afterGlobalScheme = M3MotionSettings.revisionProperty().get();
            assertTrue(afterGlobalScheme > afterGlobalAnimations);

            M3MotionSettings.setMotionBehavior(M3MotionBehavior.expressive());
            long afterGlobalBehavior = M3MotionSettings.revisionProperty().get();
            assertTrue(afterGlobalBehavior > afterGlobalScheme);

            M3MotionSettings.setAnimationsEnabled(node, false);
            long afterNodeAnimations = M3MotionSettings.revisionProperty().get();
            assertTrue(afterNodeAnimations > afterGlobalBehavior);

            M3MotionSettings.setAnimationsEnabled(node, false);

            assertEquals(afterNodeAnimations, M3MotionSettings.revisionProperty().get());
        });
    }

    /// Verifies the global motion scheme switch.
    @Test
    void globalMotionSchemeSwitchControlsDefaultScheme() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setMotionScheme(M3MotionScheme.expressive());

            assertEquals(M3MotionEasing.EMPHASIZED, M3MotionSettings.getMotionScheme().defaultEffects().easing());

            M3MotionSettings.setMotionScheme(M3MotionScheme.standard());

            assertEquals(M3MotionEasing.STANDARD, M3MotionSettings.getMotionScheme().defaultEffects().easing());
        });
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
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
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
        });
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
            M3MotionSettings.getMotionScheme(root);
            M3MotionSettings.getMotionScheme(child);
            M3MotionSettings.getMotionScheme(nested);
            M3MotionSettings.getMotionBehavior(root);
            M3MotionSettings.getMotionBehavior(child);
            M3MotionSettings.getMotionBehavior(nested);

            assertFalse(root.hasProperties());
            assertFalse(child.hasProperties());
            assertFalse(nested.hasProperties());
        });
    }
}
