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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            HBox root = new HBox(pane);
            show(root, 96.0, 48.0);
            SimpleBooleanProperty nativeFocusVisible = new SimpleBooleanProperty(false);
            M3FocusVisibleTracker tracker = new M3FocusVisibleTracker(pane, () -> {}, nativeFocusVisible);
            tracker.install();
            try {
                assertFalse(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                pane.requestFocus();
                assertTrue(pane.isFocused());
                nativeFocusVisible.set(true);
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                nativeFocusVisible.set(false);
                assertFalse(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                root.fireEvent(keyPressedEvent(KeyCode.A));
                assertFalse(
                        pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS),
                        "native focus-visible state must not be overridden by fallback scene modality"
                );
            } finally {
                tracker.uninstall();
            }
        });
    }

    /// Verifies that focus loss clears Material focus feedback even before a native property catches up.
    @Test
    void nativeFocusVisibleStateCannotOutliveOwnerFocus() {
        FxTestUtils.runOnFxThread(() -> {
            Pane first = focusablePane();
            Pane second = focusablePane();
            HBox root = new HBox(first, second);
            show(root, 160.0, 48.0);
            SimpleBooleanProperty nativeFocusVisible = new SimpleBooleanProperty(false);
            M3FocusVisibleTracker tracker = new M3FocusVisibleTracker(first, () -> {}, nativeFocusVisible);
            tracker.install();
            try {
                first.requestFocus();
                nativeFocusVisible.set(true);
                assertTrue(first.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                second.requestFocus();

                assertFalse(first.isFocused());
                assertTrue(nativeFocusVisible.get(), "the test deliberately retains a stale native value");
                assertFalse(first.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
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

    /// Verifies fallback tracking transfers cleanly when its owner moves between scenes.
    @Test
    void fallbackTrackerTransfersBetweenScenes() {
        FxTestUtils.runOnFxThread(() -> {
            Pane pane = focusablePane();
            HBox firstRoot = new HBox(pane);
            Scene firstScene = show(firstRoot, 96.0, 48.0);
            HBox secondRoot = new HBox();
            Scene secondScene = show(secondRoot, 96.0, 48.0);
            int firstInitialPropertyCount = firstScene.getProperties().size();
            int secondInitialPropertyCount = secondScene.getProperties().size();
            M3FocusVisibleTracker tracker = new M3FocusVisibleTracker(pane, () -> {}, null);

            tracker.install();
            try {
                assertTrue(firstScene.getProperties().size() > firstInitialPropertyCount);
                assertEquals(secondInitialPropertyCount, secondScene.getProperties().size());

                firstRoot.getChildren().clear();
                secondRoot.getChildren().add(pane);

                assertEquals(firstInitialPropertyCount, firstScene.getProperties().size());
                assertTrue(secondScene.getProperties().size() > secondInitialPropertyCount);

                pane.requestFocus();
                secondRoot.fireEvent(keyPressedEvent(KeyCode.A));
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                firstRoot.fireEvent(primaryMousePressedEvent());
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                secondRoot.fireEvent(primaryMousePressedEvent());
                assertFalse(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
            } finally {
                tracker.uninstall();
            }

            assertEquals(firstInitialPropertyCount, firstScene.getProperties().size());
            assertEquals(secondInitialPropertyCount, secondScene.getProperties().size());
        });
    }
    /// Verifies duplicate fallback trackers on one owner remain independent across partial uninstall.
    @Test
    void duplicateFallbackTrackersShareOwnerRegistration() {
        FxTestUtils.runOnFxThread(() -> {
            Pane pane = focusablePane();
            HBox root = new HBox(pane);
            show(root, 96.0, 48.0);
            int[] invalidations = new int[2];
            M3FocusVisibleTracker firstTracker =
                    new M3FocusVisibleTracker(pane, () -> invalidations[0]++, null);
            M3FocusVisibleTracker secondTracker =
                    new M3FocusVisibleTracker(pane, () -> invalidations[1]++, null);
            firstTracker.install();
            secondTracker.install();
            try {
                pane.requestFocus();
                int firstBeforeKey = invalidations[0];
                int secondBeforeKey = invalidations[1];

                root.fireEvent(keyPressedEvent(KeyCode.A));

                assertEquals(firstBeforeKey + 1, invalidations[0]);
                assertEquals(secondBeforeKey + 1, invalidations[1]);
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                firstTracker.uninstall();
                int firstAfterUninstall = invalidations[0];
                int secondBeforePointer = invalidations[1];

                root.fireEvent(primaryMousePressedEvent());

                assertEquals(firstAfterUninstall, invalidations[0]);
                assertEquals(secondBeforePointer + 1, invalidations[1]);
                assertFalse(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                int secondBeforeSecondKey = invalidations[1];
                root.fireEvent(keyPressedEvent(KeyCode.B));

                assertEquals(firstAfterUninstall, invalidations[0]);
                assertEquals(secondBeforeSecondKey + 1, invalidations[1]);
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
            } finally {
                firstTracker.uninstall();
                secondTracker.uninstall();
            }

            int[] afterUninstall = invalidations.clone();
            root.fireEvent(primaryMousePressedEvent());
            assertEquals(afterUninstall[0], invalidations[0]);
            assertEquals(afterUninstall[1], invalidations[1]);
        });
    }

    /// Verifies that the scene-owned fallback tracker is released after its last owner uninstalls.
    @Test
    void fallbackTrackerReleasesSceneStateAfterLastUninstall() {
        FxTestUtils.runOnFxThread(() -> {
            Pane first = focusablePane();
            Pane second = focusablePane();
            HBox root = new HBox(first, second);
            Scene scene = show(root, 160.0, 48.0);
            int initialPropertyCount = scene.getProperties().size();
            M3FocusVisibleTracker firstTracker = new M3FocusVisibleTracker(first, () -> {}, null);
            M3FocusVisibleTracker secondTracker = new M3FocusVisibleTracker(second, () -> {}, null);

            firstTracker.install();
            secondTracker.install();
            assertTrue(scene.getProperties().size() > initialPropertyCount);

            firstTracker.uninstall();
            assertTrue(scene.getProperties().size() > initialPropertyCount);

            secondTracker.uninstall();
            assertEquals(initialPropertyCount, scene.getProperties().size());
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
