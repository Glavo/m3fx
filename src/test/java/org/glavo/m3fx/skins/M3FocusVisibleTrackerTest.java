// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3FocusVisibleTracker;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests focus-visible modality tracking across native, fallback, and custom traversal paths.
@NotNullByDefault
final class M3FocusVisibleTrackerTest {
    /// Stages owned by the current test, including stages hidden before cleanup.
    private final List<Stage> stages = new ArrayList<>();

    /// Starts the JavaFX toolkit for focus-visible tracker tests.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Closes real stages opened by focus tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Stage stage : stages) {
                stage.close();
            }
            stages.clear();
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

    /// Verifies that installing a tracker cannot revive an orphaned pseudo-class on an unfocused owner.
    @Test
    void installClearsOrphanedInitialFocusVisibleState() {
        FxTestUtils.runOnFxThread(() -> {
            Pane pane = focusablePane();
            pane.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            M3FocusVisibleTracker tracker = new M3FocusVisibleTracker(pane, () -> {}, null);

            tracker.install();
            try {
                assertFalse(pane.isFocused());
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

    /// Verifies that custom traversal preserves focus-visible semantics when native traversal is bypassed.
    @Test
    void customKeyboardTraversalMarksFocusedTargetAsFocusVisible() {
        FxTestUtils.runOnFxThread(() -> {
            Pane first = focusablePane();
            Pane second = focusablePane();
            HBox root = new HBox(first, second);
            show(root, 160.0, 48.0);
            SimpleBooleanProperty firstNativeFocusVisible = new SimpleBooleanProperty(false);
            SimpleBooleanProperty secondNativeFocusVisible = new SimpleBooleanProperty(false);
            M3FocusVisibleTracker firstTracker =
                    new M3FocusVisibleTracker(first, () -> {}, firstNativeFocusVisible);
            M3FocusVisibleTracker secondTracker =
                    new M3FocusVisibleTracker(second, () -> {}, secondNativeFocusVisible);
            firstTracker.install();
            secondTracker.install();
            try {
                first.requestFocus();
                first.fireEvent(primaryMousePressedEvent());
                KeyEvent right = keyPressedEvent(KeyCode.RIGHT);

                assertTrue(M3FocusTraversal.handleHorizontalKeyFocus(root, right, List.of(first, second)));
                assertTrue(right.isConsumed());
                assertTrue(second.isFocused());
                assertTrue(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                second.fireEvent(primaryMousePressedEvent());
                assertFalse(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                KeyEvent tab = keyPressedEvent(KeyCode.TAB);
                assertTrue(M3FocusTraversal.handleCyclicTabKeyFocus(root, tab, List.of(second)));
                assertTrue(tab.isConsumed());
                assertTrue(second.isFocused());
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
            show(firstRoot, 96.0, 48.0);
            HBox secondRoot = new HBox();
            int[] invalidations = {0};
            M3FocusVisibleTracker tracker = new M3FocusVisibleTracker(pane, () -> invalidations[0]++, null);

            tracker.install();
            try {
                pane.requestFocus();
                firstRoot.fireEvent(keyPressedEvent(KeyCode.A));
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                show(secondRoot, 96.0, 48.0);
                firstRoot.getChildren().clear();
                secondRoot.getChildren().add(pane);

                pane.requestFocus();
                assertFalse(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
                secondRoot.fireEvent(keyPressedEvent(KeyCode.A));
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                firstRoot.fireEvent(primaryMousePressedEvent());
                assertTrue(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                secondRoot.fireEvent(primaryMousePressedEvent());
                assertFalse(pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
            } finally {
                tracker.uninstall();
            }

            int afterUninstall = invalidations[0];
            firstRoot.fireEvent(keyPressedEvent(KeyCode.A));
            secondRoot.fireEvent(keyPressedEvent(KeyCode.A));
            assertEquals(afterUninstall, invalidations[0], "neither scene may notify an uninstalled tracker");
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
                assertTrue(
                        pane.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS),
                        "uninstalling one tracker must retain another tracker's contribution"
                );
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

    /// Verifies partial uninstall preserves other owners and final uninstall resets scene modality.
    @Test
    void fallbackTrackerReleasesSceneStateAfterLastUninstall() {
        FxTestUtils.runOnFxThread(() -> {
            Pane first = focusablePane();
            Pane second = focusablePane();
            HBox root = new HBox(first, second);
            show(root, 160.0, 48.0);
            int[] invalidations = new int[2];
            M3FocusVisibleTracker firstTracker = new M3FocusVisibleTracker(first, () -> invalidations[0]++, null);
            M3FocusVisibleTracker secondTracker = new M3FocusVisibleTracker(second, () -> invalidations[1]++, null);
            try {
                firstTracker.install();
                secondTracker.install();
                first.requestFocus();
                root.fireEvent(keyPressedEvent(KeyCode.A));
                assertTrue(first.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));

                firstTracker.uninstall();
                int firstAfterUninstall = invalidations[0];
                second.requestFocus();
                assertTrue(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
                root.fireEvent(primaryMousePressedEvent());
                assertFalse(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
                root.fireEvent(keyPressedEvent(KeyCode.B));
                assertTrue(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
                assertEquals(firstAfterUninstall, invalidations[0]);

                secondTracker.uninstall();
                int secondAfterUninstall = invalidations[1];
                root.fireEvent(keyPressedEvent(KeyCode.C));
                assertEquals(secondAfterUninstall, invalidations[1]);

                secondTracker.install();
                assertFalse(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS),
                        "a fresh registration must not inherit modality from a disposed scene observer");
                root.fireEvent(keyPressedEvent(KeyCode.D));
                assertTrue(second.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
            } finally {
                firstTracker.uninstall();
                secondTracker.uninstall();
            }
        });
    }

    /// Shows and lays out a root in a real stage.
    private Scene show(HBox root, double width, double height) {
        Stage stage = new Stage();
        stages.add(stage);
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
