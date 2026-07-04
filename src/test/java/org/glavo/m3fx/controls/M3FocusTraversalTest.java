// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies shared focus traversal behavior.
@NotNullByDefault
final class M3FocusTraversalTest {
    /// Starts the JavaFX toolkit for focus traversal tests.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes real windows created by focus traversal tests.
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

    /// Verifies directional focus traversal reveals a lower target inside the enclosing scroll pane.
    @Test
    void directionalFocusRevealsTargetInScrollPane() {
        FxTestUtils.runOnFxThread(() -> {
            FocusRow first = new FocusRow();
            FocusRow second = new FocusRow();
            FocusRow third = new FocusRow();
            FocusRow fourth = new FocusRow();
            FocusRow fifth = new FocusRow();
            FocusRow sixth = new FocusRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();
            fifth.requestFocus();

            KeyEvent event = keyEvent(KeyCode.DOWN);
            assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    event,
                    M3FocusTraversal.focusTargets(owner.getChildren()),
                    false,
                    true
            ));

            assertTrue(sixth.isFocused());
            assertVisible(scrollPane, owner, sixth);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies directional traversal does not consume keys when focus cannot move to a detached target.
    @Test
    void directionalFocusRejectsDetachedTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button target = new M3Button("Detached");
            HBox owner = new HBox(target);

            KeyEvent event = keyEvent(KeyCode.RIGHT);
            assertFalse(M3FocusTraversal.handleHorizontalKeyFocus(
                    owner,
                    event,
                    M3FocusTraversal.focusTargets(owner.getChildren())
            ));
            assertFalse(event.isConsumed());
            assertFalse(target.isFocused());
        });
    }

    /// Verifies owner-level navigation events are consumed while a descendant text input owns focus.
    @Test
    void ownerLevelNavigationKeyIsConsumedWhenTextInputOwnsFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField editor = new M3TextField("Edit");
            M3Button next = new M3Button("Next");
            VBox owner = new VBox(editor, next);

            show(owner, 160.0, 80.0);
            owner.applyCss();
            owner.layout();
            editor.requestFocus();

            KeyEvent event = targetedKeyEvent(KeyCode.RIGHT, owner);
            assertFalse(M3FocusTraversal.handleHorizontalKeyFocus(
                    owner,
                    event,
                    M3FocusTraversal.focusTargets(owner.getChildren())
            ));

            assertTrue(editor.isFocused());
            assertFalse(next.isFocused());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies target-level text input navigation events remain available to the text input itself.
    @Test
    void textInputTargetNavigationKeyIsNotConsumedByContainerTraversal() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField editor = new M3TextField("Edit");
            M3Button next = new M3Button("Next");
            VBox owner = new VBox(editor, next);

            show(owner, 160.0, 80.0);
            owner.applyCss();
            owner.layout();
            editor.requestFocus();

            KeyEvent event = targetedKeyEvent(KeyCode.RIGHT, editor);
            assertFalse(M3FocusTraversal.handleHorizontalKeyFocus(
                    owner,
                    event,
                    M3FocusTraversal.focusTargets(owner.getChildren())
            ));

            assertTrue(editor.isFocused());
            assertFalse(next.isFocused());
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies modified navigation keys are left to application shortcuts and platform editing behavior.
    @Test
    void modifiedDirectionalFocusKeysAreIgnored() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            HBox owner = new HBox(first, second);

            show(owner, 180.0, 60.0);
            owner.applyCss();
            owner.layout();
            first.requestFocus();

            KeyEvent event = new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    KeyCode.RIGHT.getName(),
                    KeyCode.RIGHT.getName(),
                    KeyCode.RIGHT,
                    false,
                    true,
                    false,
                    false
            );
            assertFalse(M3FocusTraversal.handleHorizontalKeyFocus(
                    owner,
                    event,
                    M3FocusTraversal.focusTargets(owner.getChildren())
            ));

            assertTrue(first.isFocused());
            assertFalse(second.isFocused());
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies preset-to-picker handoff consumes keys only when the picker accepts focus.
    @Test
    void presetHandoffConsumesOnlyAfterSuccessfulFocusTransfer() {
        FxTestUtils.runOnFxThread(() -> {
            VBox presetList = new VBox();
            presetList.setFocusTraversable(true);
            Pane owner = new Pane(presetList);
            boolean[] focusAccepted = {false};
            M3PresetNavigation.install(presetList, owner, () -> focusAccepted[0]);
            show(owner, 160.0, 80.0);
            presetList.requestFocus();

            KeyEvent rejected = targetedKeyEvent(KeyCode.RIGHT, presetList);
            presetList.fireEvent(rejected);

            assertFalse(rejected.isConsumed());

            focusAccepted[0] = true;
            KeyEvent accepted = targetedKeyEvent(KeyCode.RIGHT, presetList);
            presetList.fireEvent(accepted);

            assertTrue(accepted.isConsumed());
        });
    }

    /// Verifies modified preset handoff keys are left to application shortcuts and platform behavior.
    @Test
    void modifiedPresetHandoffKeysAreIgnored() {
        FxTestUtils.runOnFxThread(() -> {
            VBox presetList = new VBox();
            presetList.setFocusTraversable(true);
            Pane owner = new Pane(presetList);
            boolean[] focusAccepted = {false};
            M3PresetNavigation.install(presetList, owner, () -> {
                focusAccepted[0] = true;
                return true;
            });
            show(owner, 160.0, 80.0);
            presetList.requestFocus();

            KeyEvent event = new KeyEvent(
                    presetList,
                    presetList,
                    KeyEvent.KEY_PRESSED,
                    KeyCode.RIGHT.getName(),
                    KeyCode.RIGHT.getName(),
                    KeyCode.RIGHT,
                    false,
                    true,
                    false,
                    false
            );
            presetList.fireEvent(event);

            assertFalse(focusAccepted[0]);
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies preset-to-picker handoff mirrors the logical handoff key in right-to-left layouts.
    @Test
    void presetHandoffMirrorsRightToLeftDirection() {
        FxTestUtils.runOnFxThread(() -> {
            VBox presetList = new VBox();
            presetList.setFocusTraversable(true);
            Pane owner = new Pane(presetList);
            owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            boolean[] focusAccepted = {true};
            M3PresetNavigation.install(presetList, owner, () -> focusAccepted[0]);
            show(owner, 160.0, 80.0);
            presetList.requestFocus();

            KeyEvent wrongDirection = targetedKeyEvent(KeyCode.RIGHT, presetList);
            presetList.fireEvent(wrongDirection);

            assertFalse(wrongDirection.isConsumed());

            KeyEvent logicalHandoff = targetedKeyEvent(KeyCode.LEFT, presetList);
            presetList.fireEvent(logicalHandoff);

            assertTrue(logicalHandoff.isConsumed());
        });
    }

    /// Verifies anchored horizontal focus traversal follows visual direction in right-to-left layouts.
    @Test
    void horizontalFocusMirrorsAnchoredRightToLeftMovement() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            HBox owner = new HBox(first, second, third);
            owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            show(owner, 240.0, 60.0);
            owner.applyCss();
            owner.layout();
            second.requestFocus();

            KeyEvent right = keyEvent(KeyCode.RIGHT);
            assertTrue(M3FocusTraversal.handleHorizontalKeyFocus(
                    owner,
                    right,
                    M3FocusTraversal.focusTargets(owner.getChildren())
            ));
            assertTrue(first.isFocused());
            assertTrue(right.isConsumed());

            second.requestFocus();
            KeyEvent left = keyEvent(KeyCode.LEFT);
            assertTrue(M3FocusTraversal.handleHorizontalKeyFocus(
                    owner,
                    left,
                    M3FocusTraversal.focusTargets(owner.getChildren())
            ));
            assertTrue(third.isFocused());
            assertTrue(left.isConsumed());
        });
    }

    /// Verifies unanchored horizontal focus traversal chooses edge targets before mirroring anchored motion.
    @Test
    void horizontalFocusKeepsUnanchoredEdgesForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            HBox owner = new HBox(first, second, third);
            owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            owner.setFocusTraversable(true);

            show(owner, 240.0, 60.0);
            owner.applyCss();
            owner.layout();

            KeyEvent right = keyEvent(KeyCode.RIGHT);
            assertTrue(M3FocusTraversal.handleHorizontalKeyFocus(
                    owner,
                    right,
                    M3FocusTraversal.focusTargets(owner.getChildren())
            ));
            assertTrue(first.isFocused());
            assertTrue(right.isConsumed());

            owner.requestFocus();
            KeyEvent left = keyEvent(KeyCode.LEFT);
            assertTrue(M3FocusTraversal.handleHorizontalKeyFocus(
                    owner,
                    left,
                    M3FocusTraversal.focusTargets(owner.getChildren())
            ));
            assertTrue(third.isFocused());
            assertTrue(left.isConsumed());
        });
    }

    /// Verifies page focus traversal uses the enclosing scroll pane viewport as its step size.
    @Test
    void pageDirectionalFocusUsesScrollPaneViewportHeight() {
        FxTestUtils.runOnFxThread(() -> {
            FocusRow first = new FocusRow();
            FocusRow second = new FocusRow();
            FocusRow third = new FocusRow();
            FocusRow fourth = new FocusRow();
            FocusRow fifth = new FocusRow();
            FocusRow sixth = new FocusRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();
            first.requestFocus();

            KeyEvent pageDown = keyEvent(KeyCode.PAGE_DOWN);
            assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    pageDown,
                    M3FocusTraversal.focusTargets(owner.getChildren()),
                    false,
                    true
            ));

            assertTrue(third.isFocused());
            assertVisible(scrollPane, owner, third);
            assertTrue(pageDown.isConsumed());

            scrollPane.setVvalue(1.0);
            KeyEvent pageUp = keyEvent(KeyCode.PAGE_UP);
            assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    pageUp,
                    M3FocusTraversal.focusTargets(owner.getChildren()),
                    false,
                    true
            ));

            assertTrue(first.isFocused());
            assertVisible(scrollPane, owner, first);
            assertTrue(scrollPane.getVvalue() < 1.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTrue(pageUp.isConsumed());
        });
    }

    /// Verifies failed non-wrapping traversal leaves scroll position and event state unchanged.
    @Test
    void failedDirectionalFocusDoesNotRevealOrConsumeEvent() {
        FxTestUtils.runOnFxThread(() -> {
            FocusRow first = new FocusRow();
            FocusRow second = new FocusRow();
            FocusRow third = new FocusRow();
            FocusRow fourth = new FocusRow();
            FocusRow fifth = new FocusRow();
            FocusRow sixth = new FocusRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();
            scrollPane.setVvalue(1.0);
            first.requestFocus();

            KeyEvent event = keyEvent(KeyCode.UP);
            assertFalse(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    event,
                    M3FocusTraversal.focusTargets(owner.getChildren()),
                    false,
                    true,
                    -1,
                    false
            ));

            assertTrue(first.isFocused());
            assertEquals(1.0, scrollPane.getVvalue(), 0.0001, () -> "vvalue=" + scrollPane.getVvalue());
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies traversal skips unreachable targets before requesting focus or revealing a row.
    @Test
    void directionalFocusSkipsUnreachableTargetBeforeReveal() {
        FxTestUtils.runOnFxThread(() -> {
            FocusRow first = new FocusRow();
            FocusRow second = new FocusRow();
            FocusRow third = new FocusRow();
            FocusRow fourth = new FocusRow();
            FocusRow fifth = new FocusRow();
            FocusRow sixth = new FocusRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();
            fifth.setDisable(true);
            fourth.requestFocus();

            KeyEvent event = keyEvent(KeyCode.DOWN);
            assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    event,
                    M3FocusTraversal.focusTargets(owner.getChildren()),
                    false,
                    true
            ));

            assertFalse(fifth.isFocused());
            assertTrue(sixth.isFocused());
            assertVisible(scrollPane, owner, sixth);
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies traversal filters hidden, detached, and duplicate targets before moving focus.
    @Test
    void directionalFocusFiltersUnreachableAndDuplicateTargets() {
        FxTestUtils.runOnFxThread(() -> {
            FocusRow first = new FocusRow();
            FocusRow second = new FocusRow();
            FocusRow hidden = new FocusRow();
            FocusRow detached = new FocusRow();
            FocusRow third = new FocusRow();
            VBox owner = new VBox(first, second, hidden, third);
            hidden.setVisible(false);

            show(owner, 160.0, 120.0);
            owner.applyCss();
            owner.layout();
            second.requestFocus();

            KeyEvent event = keyEvent(KeyCode.DOWN);
            assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    event,
                    List.of(first, second, hidden, second, detached, third),
                    false,
                    true
            ));

            assertFalse(hidden.isFocused());
            assertFalse(detached.isFocused());
            assertTrue(third.isFocused());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies fallback indexes are normalized after stale targets are filtered from a caller-supplied list.
    @Test
    void directionalFocusNormalizesFallbackIndexAfterFiltering() {
        FxTestUtils.runOnFxThread(() -> {
            FocusRow hidden = new FocusRow();
            FocusRow first = new FocusRow();
            FocusRow second = new FocusRow();
            FocusRow third = new FocusRow();
            VBox owner = new VBox(hidden, first, second, third);
            hidden.setVisible(false);

            show(owner, 160.0, 120.0);
            owner.applyCss();
            owner.layout();

            KeyEvent event = keyEvent(KeyCode.DOWN);
            assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    event,
                    List.of(hidden, first, second, third),
                    false,
                    true,
                    2
            ));

            assertTrue(third.isFocused());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies an explicit event target can anchor traversal when the scene focus owner is unavailable.
    @Test
    void directionalFocusUsesContainedEventTargetAsAnchor() {
        FxTestUtils.runOnFxThread(() -> {
            FocusRow first = new FocusRow();
            Pane eventTarget = new Pane();
            FocusContainer anchor = new FocusContainer(eventTarget);
            FocusRow second = new FocusRow();
            VBox owner = new VBox(first, anchor, second);

            show(owner, 160.0, 120.0);
            owner.applyCss();
            owner.layout();

            KeyEvent event = targetedKeyEvent(KeyCode.DOWN, eventTarget);
            assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    event,
                    List.of(first, anchor, second),
                    false,
                    true,
                    -1,
                    true,
                    eventTarget
            ));

            assertTrue(second.isFocused());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies indexed accessibility children take part in subtree focus traversal before parent children repeat.
    @Test
    void reachableTreeTraversalUsesIndexedAccessibilityChildren() {
        FxTestUtils.runOnFxThread(() -> {
            FocusRow first = new FocusRow();
            FocusRow indexedChild = new FocusRow();
            IndexedFocusContainer indexedContainer = new IndexedFocusContainer(indexedChild);
            FocusRow after = new FocusRow();
            VBox owner = new VBox(first, indexedContainer, after);

            show(owner, 160.0, 120.0);
            first.requestFocus();

            KeyEvent event = keyEvent(KeyCode.DOWN);
            assertTrue(M3FocusTraversal.handleDirectionalKeyFocus(
                    owner,
                    event,
                    M3FocusTraversal.focusTargetsInReachableTree(owner),
                    false,
                    true
            ));

            assertTrue(indexedChild.isFocused());
            assertFalse(indexedContainer.isFocused());
            assertFalse(after.isFocused());
            assertTrue(event.isConsumed());
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

    /// Verifies that the target row is inside the current scroll pane viewport.
    private static void assertVisible(ScrollPane scrollPane, VBox content, FocusRow target) {
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollableHeight = content.getBoundsInLocal().getHeight() - viewportHeight;
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        double fraction = (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange;
        double visibleTop = Math.max(0.0, Math.min(1.0, fraction)) * scrollableHeight;
        double visibleBottom = visibleTop + viewportHeight;
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        assertTrue(targetBounds.getMinY() >= visibleTop - 0.5, () -> "targetTop=" + targetBounds.getMinY());
        assertTrue(targetBounds.getMaxY() <= visibleBottom + 0.5, () -> "targetBottom=" + targetBounds.getMaxY());
    }

    /// Creates a pressed key event with an explicit source and target node.
    private static KeyEvent targetedKeyEvent(KeyCode code, Node target) {
        return new KeyEvent(
                target,
                target,
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

    /// Creates a pressed key event for focus traversal behavior checks.
    private static KeyEvent keyEvent(KeyCode code) {
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


    /// Focusable container row that exposes one indexed accessibility child.
    @NotNullByDefault
    private static final class IndexedFocusContainer extends Pane {
        /// The preferred height of one row.
        private static final double ROW_HEIGHT = 20.0;

        /// The child returned from indexed accessibility queries.
        private final Node indexedChild;

        /// Creates an indexed focus container around a child node.
        private IndexedFocusContainer(Node indexedChild) {
            super(indexedChild);
            this.indexedChild = indexedChild;
            setFocusTraversable(true);
        }

        /// Computes the preferred row width.
        @Override
        protected double computePrefWidth(double height) {
            return 100.0;
        }

        /// Computes the preferred row height.
        @Override
        protected double computePrefHeight(double width) {
            return ROW_HEIGHT;
        }

        /// Returns indexed accessibility children for focus traversal tests.
        @Override
        public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
            return switch (attribute) {
                case ITEM_COUNT -> 1;
                case ITEM_AT_INDEX -> parameters.length > 0
                        && parameters[0] instanceof Number number
                        && number.intValue() == 0 ? indexedChild : null;
                default -> super.queryAccessibleAttribute(attribute, parameters);
            };
        }
    }

    /// Focusable container row with a stable preferred size.
    @NotNullByDefault
    private static final class FocusContainer extends VBox {
        /// The preferred height of one row.
        private static final double ROW_HEIGHT = 20.0;

        /// Creates a focusable container row around a child node.
        private FocusContainer(Node child) {
            super(child);
            setFocusTraversable(true);
        }

        /// Computes the preferred row width.
        @Override
        protected double computePrefWidth(double height) {
            return 100.0;
        }

        /// Computes the preferred row height.
        @Override
        protected double computePrefHeight(double width) {
            return ROW_HEIGHT;
        }
    }

    /// Focusable test row with a stable preferred size.
    @NotNullByDefault
    private static final class FocusRow extends Region {
        /// The preferred height of one row.
        private static final double ROW_HEIGHT = 20.0;

        /// Creates a focusable row.
        private FocusRow() {
            setFocusTraversable(true);
        }

        /// Computes the preferred row width.
        @Override
        protected double computePrefWidth(double height) {
            return 100.0;
        }

        /// Computes the preferred row height.
        @Override
        protected double computePrefHeight(double width) {
            return ROW_HEIGHT;
        }
    }
}