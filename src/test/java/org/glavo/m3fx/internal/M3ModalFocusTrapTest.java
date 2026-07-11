// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies scene-level modal focus trap ordering.
@NotNullByDefault
final class M3ModalFocusTrapTest {
    /// Starts the JavaFX toolkit before modal focus trap tests create windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes real windows created by modal focus trap tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
        });
    }

    /// Verifies that updating an already installed active trap makes it the topmost scene trap.
    @Test
    void updateRefreshesAlreadyInstalledTrapAsTopmost() {
        FxTestUtils.runOnFxThread(() -> {
            Button firstTarget = new Button("First");
            Button secondTarget = new Button("Second");
            VBox firstOwner = new VBox(firstTarget);
            VBox secondOwner = new VBox(secondTarget);
            VBox root = new VBox(firstOwner, secondOwner);
            boolean[] firstActive = {true};
            boolean[] secondActive = {false};
            M3ModalFocusTrap firstTrap = new M3ModalFocusTrap(
                    firstOwner,
                    () -> firstActive[0],
                    () -> List.of(firstTarget),
                    null
            );
            M3ModalFocusTrap secondTrap = new M3ModalFocusTrap(
                    secondOwner,
                    () -> secondActive[0],
                    () -> List.of(secondTarget),
                    null
            );
            firstTrap.install();
            secondTrap.install();

            Scene scene = show(root, 240.0, 120.0);
            secondActive[0] = true;
            secondTrap.update();
            firstTarget.requestFocus();
            fireKey(scene, keyEvent(KeyCode.TAB));

            assertTrue(secondTarget.isFocused(), "the later activated trap should own Tab traversal");

            firstTrap.update();
            fireKey(scene, keyEvent(KeyCode.TAB));

            assertTrue(firstTarget.isFocused(), "updating an active installed trap should refresh it as topmost");
        });
    }

    /// Verifies that Escape is handled only by the topmost active trap in a scene.
    @Test
    void escapeRunsOnlyTopmostTrapAction() {
        FxTestUtils.runOnFxThread(() -> {
            Button firstTarget = new Button("First");
            Button secondTarget = new Button("Second");
            VBox firstOwner = new VBox(firstTarget);
            VBox secondOwner = new VBox(secondTarget);
            VBox root = new VBox(firstOwner, secondOwner);
            boolean[] firstActive = {true};
            boolean[] secondActive = {true};
            int[] firstEscapes = {0};
            int[] secondEscapes = {0};
            M3ModalFocusTrap firstTrap = new M3ModalFocusTrap(
                    firstOwner,
                    () -> firstActive[0],
                    () -> List.of(firstTarget),
                    () -> firstEscapes[0]++
            );
            M3ModalFocusTrap secondTrap = new M3ModalFocusTrap(
                    secondOwner,
                    () -> secondActive[0],
                    () -> List.of(secondTarget),
                    () -> secondEscapes[0]++
            );
            firstTrap.install();
            secondTrap.install();
            Scene scene = show(root, 240.0, 120.0);

            KeyEvent topmostEscape = fireKey(scene, keyEvent(KeyCode.ESCAPE));

            assertTrue(topmostEscape.isConsumed(), "Escape handled by a topmost trap should be consumed");
            assertEquals(0, firstEscapes[0], "covered background trap should not receive Escape");
            assertEquals(1, secondEscapes[0], "topmost trap should receive Escape");

            secondActive[0] = false;
            secondTrap.update();
            KeyEvent exposedEscape = fireKey(scene, keyEvent(KeyCode.ESCAPE));

            assertTrue(exposedEscape.isConsumed(), "Escape handled by the exposed trap should be consumed");
            assertEquals(1, firstEscapes[0], "exposed trap should receive Escape after top trap deactivates");
            assertEquals(1, secondEscapes[0], "inactive trap should not receive additional Escape events");
        });
    }

    /// Verifies that a stale inactive topmost trap does not block the active trap below it.
    @Test
    void staleInactiveTopmostTrapDoesNotBlockActiveTrap() {
        FxTestUtils.runOnFxThread(() -> {
            Button firstStart = new Button("First start");
            Button firstEnd = new Button("First end");
            VBox firstOwner = new VBox(firstStart, firstEnd);
            Button secondStart = new Button("Second start");
            Button secondEnd = new Button("Second end");
            VBox secondOwner = new VBox(secondStart, secondEnd);
            VBox root = new VBox(firstOwner, secondOwner);
            boolean[] secondActive = {true};
            M3ModalFocusTrap firstTrap = new M3ModalFocusTrap(
                    firstOwner,
                    () -> true,
                    () -> List.of(firstStart, firstEnd),
                    null
            );
            M3ModalFocusTrap secondTrap = new M3ModalFocusTrap(
                    secondOwner,
                    () -> secondActive[0],
                    () -> List.of(secondStart, secondEnd),
                    null
            );
            firstTrap.install();
            secondTrap.install();
            Scene scene = show(root, 320.0, 180.0);

            secondActive[0] = false;
            firstStart.requestFocus();
            fireKey(scene, keyEvent(KeyCode.TAB));

            assertTrue(firstEnd.isFocused(), "the active lower trap should handle Tab when the top trap becomes stale");
            assertFalse(secondStart.isFocused());
            assertFalse(secondEnd.isFocused());
        });
    }

    /// Verifies that a trap installed before a stage is shown starts handling traversal after the window is visible.
    @Test
    void trapInstalledBeforeWindowShownActivatesAfterShow() {
        FxTestUtils.runOnFxThread(() -> {
            Button outside = new Button("Outside");
            Button target = new Button("Target");
            VBox owner = new VBox(target);
            VBox root = new VBox(outside, owner);
            M3ModalFocusTrap trap = new M3ModalFocusTrap(
                    owner,
                    () -> true,
                    () -> List.of(target),
                    null
            );
            trap.install();

            Stage stage = new Stage();
            Scene scene = new Scene(root, 240.0, 120.0);
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();
            outside.requestFocus();

            fireKey(scene, keyEvent(KeyCode.TAB));

            assertTrue(target.isFocused(), "trap should activate when a prebuilt owner scene becomes visible");
        });
    }

    /// Verifies that uninstalling a trap removes its scene filter and releases keyboard traversal.
    @Test
    void uninstallReleasesSceneTraversalFilter() {
        FxTestUtils.runOnFxThread(() -> {
            Button outside = new Button("Outside");
            Button target = new Button("Target");
            VBox owner = new VBox(target);
            VBox root = new VBox(outside, owner);
            M3ModalFocusTrap trap = new M3ModalFocusTrap(
                    owner,
                    () -> true,
                    () -> List.of(target),
                    null
            );
            Scene scene = show(root, 240.0, 120.0);
            int initialPropertyCount = scene.getProperties().size();
            trap.install();
            trap.install();
            assertTrue(scene.getProperties().size() > initialPropertyCount);

            trap.uninstall();
            assertEquals(initialPropertyCount, scene.getProperties().size());

            root.getChildren().remove(owner);
            root.getChildren().add(owner);
            outside.requestFocus();

            KeyEvent tab = fireKey(scene, keyEvent(KeyCode.TAB));

            assertFalse(tab.isConsumed(), "uninstalled trap should not consume Tab");
            assertFalse(target.isFocused(), "uninstalled trap should not move focus into its owner");
        });
    }

    /// Verifies that application or platform traversal shortcuts are not converted into modal cycling.
    @Test
    void modifiedTraversalKeysPassThroughActiveTrap() {
        FxTestUtils.runOnFxThread(() -> {
            Button first = new Button("First");
            Button second = new Button("Second");
            VBox owner = new VBox(first, second);
            M3ModalFocusTrap trap = new M3ModalFocusTrap(
                    owner,
                    () -> true,
                    () -> List.of(first, second),
                    null
            );
            trap.install();
            Scene scene = show(owner, 240.0, 120.0);
            first.requestFocus();

            KeyEvent controlTab = fireKey(scene, keyEvent(KeyCode.TAB, false, true, false, false));
            KeyEvent altF6 = fireKey(scene, keyEvent(KeyCode.F6, false, false, true, false));

            assertFalse(controlTab.isConsumed(), "Ctrl+Tab should remain available to application shortcuts");
            assertFalse(altF6.isConsumed(), "Alt+F6 should remain available to platform shortcuts");
            assertTrue(first.isFocused(), "modified traversal shortcuts should not move modal focus");
        });
    }

    /// Verifies that empty active traps still consume traversal keys so focus cannot leave the modal surface.
    @Test
    void emptyTrapConsumesUnmodifiedTraversalKeys() {
        FxTestUtils.runOnFxThread(() -> {
            Button outside = new Button("Outside");
            VBox owner = new VBox();
            VBox root = new VBox(outside, owner);
            M3ModalFocusTrap trap = new M3ModalFocusTrap(
                    owner,
                    () -> true,
                    List::of,
                    null
            );
            trap.install();
            Scene scene = show(root, 240.0, 120.0);
            outside.requestFocus();

            KeyEvent tab = fireKey(scene, keyEvent(KeyCode.TAB));
            KeyEvent shiftF6 = fireKey(scene, keyEvent(KeyCode.F6, true, false, false, false));

            assertTrue(tab.isConsumed(), "empty modal trap should consume Tab");
            assertTrue(shiftF6.isConsumed(), "empty modal trap should consume Shift+F6");
            assertTrue(outside.isFocused(), "empty modal trap should not move focus to an outside node");
        });
    }

    /// Shows the supplied root in a real JavaFX window and performs an initial layout pass.
    private static Scene show(Parent root, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.layout();
        return scene;
    }

    /// Fires one key press through the scene-level event dispatch chain and returns the dispatched event.
    private static KeyEvent fireKey(Scene scene, KeyEvent event) {
        KeyEvent dispatched = event.copyFor(scene, scene);
        Event.fireEvent(scene, dispatched);
        return dispatched;
    }

    /// Creates a key press event using the standard JavaFX test event constructor.
    private static KeyEvent keyEvent(KeyCode code) {
        return keyEvent(code, false, false, false, false);
    }

    /// Creates a key press event with explicit modifier state.
    private static KeyEvent keyEvent(
            KeyCode code,
            boolean shiftDown,
            boolean controlDown,
            boolean altDown,
            boolean metaDown
    ) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shiftDown, controlDown, altDown, metaDown);
    }
}