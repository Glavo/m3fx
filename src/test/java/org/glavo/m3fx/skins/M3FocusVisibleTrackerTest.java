// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests fallback focus-visible modality tracking.
@NotNullByDefault
final class M3FocusVisibleTrackerTest {
    /// Starts the JavaFX toolkit for focus-visible tracker tests.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes real stages opened by focus tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : java.util.List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
        });
    }

    /// Verifies that a native focus-visible property drives the Material pseudo-class when it is available.
    @Test
    void nativeFocusVisiblePropertyDrivesPseudoClass() {
        FxTestUtils.runOnFxThread(() -> {
            Pane pane = focusablePane();
            show(new HBox(pane), 96.0, 48.0);
            SimpleBooleanProperty nativeFocusVisible = new SimpleBooleanProperty(false);
            M3FocusVisibleTracker tracker = new M3FocusVisibleTracker(pane, () -> {}, nativeFocusVisible);
            tracker.install();
            try {
                assertFalse(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                nativeFocusVisible.set(true);
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                nativeFocusVisible.set(false);
                assertFalse(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
            } finally {
                tracker.uninstall();
            }
        });
    }

    /// Verifies keyboard fallback modality from an untracked container is shared across focus targets.
    @Test
    void fallbackKeyboardModalityFromContainerAppliesToNextFocusedOwner() {
        FxTestUtils.runOnFxThread(() -> {
            Pane first = focusablePane();
            Pane second = focusablePane();
            HBox root = new HBox(first, second);
            show(root, 160.0, 48.0);
            M3FocusVisibleTracker firstTracker = new M3FocusVisibleTracker(first, () -> {}, null);
            M3FocusVisibleTracker secondTracker = new M3FocusVisibleTracker(second, () -> {}, null);
            firstTracker.install();
            secondTracker.install();
            try {
                first.requestFocus();
                first.fireEvent(primaryMousePressedEvent());
                root.fireEvent(keyPressedEvent(KeyCode.A));

                second.requestFocus();

                assertFalse(first.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
                assertTrue(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
            } finally {
                firstTracker.uninstall();
                secondTracker.uninstall();
            }
        });
    }

    /// Verifies pointer fallback modality from an untracked container clears the next focused owner.
    @Test
    void fallbackPointerModalityFromContainerClearsNextFocusedOwner() {
        FxTestUtils.runOnFxThread(() -> {
            Pane first = focusablePane();
            Pane second = focusablePane();
            HBox root = new HBox(first, second);
            show(root, 160.0, 48.0);
            M3FocusVisibleTracker firstTracker = new M3FocusVisibleTracker(first, () -> {}, null);
            M3FocusVisibleTracker secondTracker = new M3FocusVisibleTracker(second, () -> {}, null);
            firstTracker.install();
            secondTracker.install();
            try {
                first.requestFocus();
                root.fireEvent(keyPressedEvent(KeyCode.A));
                second.requestFocus();
                assertTrue(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                root.fireEvent(primaryMousePressedEvent());
                first.requestFocus();

                assertFalse(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
                assertFalse(first.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
            } finally {
                firstTracker.uninstall();
                secondTracker.uninstall();
            }
        });
    }

    /// Verifies fallback modality stays isolated between scenes.
    @Test
    void fallbackModalityIsSceneLocal() {
        FxTestUtils.runOnFxThread(() -> {
            Pane first = focusablePane();
            HBox firstRoot = new HBox(first);
            show(firstRoot, 96.0, 48.0);
            Pane second = focusablePane();
            HBox secondRoot = new HBox(second);
            show(secondRoot, 96.0, 48.0);
            M3FocusVisibleTracker firstTracker = new M3FocusVisibleTracker(first, () -> {}, null);
            M3FocusVisibleTracker secondTracker = new M3FocusVisibleTracker(second, () -> {}, null);
            firstTracker.install();
            secondTracker.install();
            try {
                first.requestFocus();
                firstRoot.fireEvent(keyPressedEvent(KeyCode.A));

                second.requestFocus();
                assertFalse(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                secondRoot.fireEvent(keyPressedEvent(KeyCode.A));
                assertTrue(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                firstRoot.fireEvent(primaryMousePressedEvent());
                assertTrue(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                secondRoot.fireEvent(primaryMousePressedEvent());
                assertFalse(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
            } finally {
                firstTracker.uninstall();
                secondTracker.uninstall();
            }
        });
    }

    /// Shows and lays out a root in a real stage.
    private static Scene show(HBox root, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.resize(width, height);
        root.layout();
        return scene;
    }

    /// Creates a focusable pane for tracker tests.
    private static Pane focusablePane() {
        Pane pane = new Pane();
        pane.setFocusTraversable(true);
        pane.setPrefSize(64.0, 32.0);
        return pane;
    }

    /// Creates a key press event for fallback modality tests.
    private static KeyEvent keyPressedEvent(KeyCode code) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                code.getName(),
                code.getName(),
                code,
                false,
                false,
                false,
                false
        );
    }

    /// Creates a primary mouse press event for fallback modality tests.
    private static MouseEvent primaryMousePressedEvent() {
        return new MouseEvent(
                MouseEvent.MOUSE_PRESSED,
                1.0,
                1.0,
                1.0,
                1.0,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                null
        );
    }
}