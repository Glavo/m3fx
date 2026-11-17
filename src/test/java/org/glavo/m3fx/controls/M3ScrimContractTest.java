// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies scrim actions, modal input handling, and visual styling.
@NotNullByDefault
final class M3ScrimContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that scrims fire action events for programmatic and mouse activation.
    @Test
    void scrimFiresActionEvents() {
        M3Scrim scrim = new M3Scrim();
        AtomicInteger actions = new AtomicInteger();
        scrim.setOnAction(event -> actions.incrementAndGet());

        scrim.fire();
        scrim.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 10.0, 10.0, false));
        scrim.setDisable(true);
        scrim.fire();

        assertEquals(2, actions.get());
    }

    /// Verifies configurable scrim opacity and pointer dismissal behavior.
    @Test
    void scrimSupportsVisibilityOptions() {
        M3Scrim scrim = new M3Scrim();
        AtomicInteger actions = new AtomicInteger();
        scrim.setOnAction(event -> actions.incrementAndGet());

        scrim.setVisibleOpacity(0.48);
        assertEquals(0.48, scrim.getVisibleOpacity(), 0.0001);
        assertEquals(0.48, scrim.getOpacity(), 0.0001);

        scrim.hide();
        assertEquals(0.0, scrim.getOpacity(), 0.0001);
        scrim.show();
        assertEquals(0.48, scrim.getOpacity(), 0.0001);

        scrim.setDismissOnClick(false);
        scrim.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 10.0, 10.0, false));
        assertEquals(0, actions.get());

        scrim.setDismissOnClick(true);
        scrim.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 10.0, 10.0, false));
        assertEquals(1, actions.get());

        assertThrows(IllegalArgumentException.class, () -> scrim.setVisibleOpacity(-0.1));
        assertThrows(IllegalArgumentException.class, () -> scrim.setVisibleOpacity(1.1));
    }

    /// Verifies that scrims expose visibility actions and keyboard dismissal.
    @Test
    void scrimSupportsAccessibleVisibilityAndKeyboardActions() {
        M3Scrim scrim = new M3Scrim();
        AtomicInteger actions = new AtomicInteger();
        scrim.setOnAction(event -> actions.incrementAndGet());

        assertEquals(true, scrim.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        scrim.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertFalse(scrim.isShown());
        assertEquals(false, scrim.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        scrim.executeAccessibleAction(AccessibleAction.EXPAND);
        assertTrue(scrim.isShown());
        assertEquals(scrim, scrim.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        KeyEvent enter = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER);
        KeyEvent space = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE);
        KeyEvent escape = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE);
        scrim.fireEvent(enter);
        scrim.fireEvent(space);
        scrim.fireEvent(escape);

        assertEquals(3, actions.get());
    }

    /// Verifies that scrims expose animated shown state changes.
    @Tier2Test
    @Test
    void scrimSupportsAnimatedShownState() throws InterruptedException {
        M3Scrim scrim = new M3Scrim();

        applyCss(scrim);
        assertTrue(scrim.isShown());
        assertTrue(scrim.isVisible());
        assertTrue(scrim.isManaged());

        scrim.hide();

        assertFalse(scrim.isShown());
        assertTrue(scrim.isVisible());
        assertTrue(scrim.isManaged());

        FxTestUtils.runOnFxThreadWhen(() -> !scrim.isVisible()
                && !scrim.isManaged()
                && Math.abs(scrim.getOpacity()) < 0.0001, () -> {
        }, () -> {
            assertFalse(scrim.isVisible());
            assertFalse(scrim.isManaged());
            assertEquals(0.0, scrim.getOpacity(), 0.0001);
        });

        scrim.show();

        assertTrue(scrim.isShown());
        assertTrue(scrim.isVisible());
        assertTrue(scrim.isManaged());

        FxTestUtils.runOnFxThreadWhen(() -> Math.abs(scrim.getOpacity() - 0.32) < 0.0001, () -> {
        }, () -> assertEquals(0.32, scrim.getOpacity(), 0.0001));
    }

    /// Verifies that scrim opacity applies through the active theme.
    @Test
    void scrimAppliesTokenOpacity() {
        M3Scrim scrim = new M3Scrim();

        applyCss(scrim);

        assertEquals(0.32, scrim.getOpacity(), 0.0001);
    }
}
