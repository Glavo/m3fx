// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies scene traversal and logical row focus for virtualized Material tree views.
@Tier2Test
@NotNullByDefault
final class M3TreeViewFocusTraversalTest {
    /// The pseudo-class used to expose keyboard-visible logical row focus.
    private static final PseudoClass FOCUS_VISIBLE = PseudoClass.getPseudoClass("focus-visible");

    /// Starts the JavaFX toolkit before creating controls or windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes windows created by focus traversal tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.setScene(null);
                    stage.close();
                }
            }
        });
    }

    /// Verifies forward and reverse Tab traversal treat a checkbox tree as one composite focus stop.
    @Test
    void tabTraversalEntersFocusedCheckboxRowAndExitsCompositeInBothDirections() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button before = new M3Button("Before");
            M3Button after = new M3Button("After");
            M3TreeView<String> treeView = checkboxTree();
            treeView.getSelectionModel().select(2);

            VBox root = new VBox(before, treeView, after);
            Scene scene = show(root);

            before.requestFocus();
            before.fireEvent(tabKeyEvent(false));

            assertSame(treeView, scene.getFocusOwner());
            assertEquals(2, treeView.getFocusModel().getFocusedIndex());
            assertLogicalCellFocus(treeView, 2);
            assertTrue(visibleCells(treeView).stream()
                    .allMatch(cell -> !cell.getSelectionCheckBox().isFocusTraversable()));

            treeView.fireEvent(tabKeyEvent(false));
            assertTrue(after.isFocused());

            after.fireEvent(tabKeyEvent(true));
            assertSame(treeView, scene.getFocusOwner());
            assertEquals(2, treeView.getFocusModel().getFocusedIndex());
            assertLogicalCellFocus(treeView, 2);

            treeView.fireEvent(tabKeyEvent(true));
            assertTrue(before.isFocused());
        });
    }

    /// Verifies Space toggles only the focused checkbox row without clearing other multiple selections.
    @Test
    void spaceTogglesFocusedCheckboxSelection() {
        FxTestUtils.runOnFxThread(() -> {
            M3TreeView<String> treeView = checkboxTree();
            treeView.getSelectionModel().selectIndices(1, 3);
            treeView.getFocusModel().focus(2);
            VBox root = new VBox(treeView);
            show(root);
            treeView.requestFocus();
            assertTrue(treeView.isFocused());

            KeyEvent select = keyEvent(KeyCode.SPACE, false);
            treeView.fireEvent(select);

            assertEquals(List.of(1, 2, 3), List.copyOf(treeView.getSelectionModel().getSelectedIndices()));
            assertEquals(2, treeView.getFocusModel().getFocusedIndex());
            assertTrue(visibleCells(treeView).stream()
                    .filter(cell -> cell.getIndex() == 2)
                    .findFirst()
                    .orElseThrow()
                    .getSelectionCheckBox()
                    .isSelected());

            KeyEvent clear = keyEvent(KeyCode.SPACE, false);
            treeView.fireEvent(clear);

            assertEquals(List.of(1, 3), List.copyOf(treeView.getSelectionModel().getSelectedIndices()));
            assertEquals(2, treeView.getFocusModel().getFocusedIndex());

            treeView.fireEvent(keyEvent(KeyCode.DOWN, false));
            assertEquals(3, treeView.getFocusModel().getFocusedIndex());
            assertEquals(List.of(1, 3), List.copyOf(treeView.getSelectionModel().getSelectedIndices()));

            treeView.fireEvent(keyEvent(KeyCode.UP, false));
            assertEquals(2, treeView.getFocusModel().getFocusedIndex());
            assertEquals(List.of(1, 3), List.copyOf(treeView.getSelectionModel().getSelectedIndices()));

            treeView.fireEvent(keyEvent(KeyCode.LEFT, false));
            assertEquals(0, treeView.getFocusModel().getFocusedIndex());
            assertEquals(List.of(1, 3), List.copyOf(treeView.getSelectionModel().getSelectedIndices()));
        });
    }

    /// Creates a multiple-selection checkbox tree with every representative row materialized.
    ///
    /// @return the configured checkbox tree
    private static M3TreeView<String> checkboxTree() {
        TreeItem<String> rootItem = new TreeItem<>("Workspace");
        rootItem.getChildren().addAll(List.of(
                new TreeItem<>("Applications"),
                new TreeItem<>("Libraries"),
                new TreeItem<>("Documentation")
        ));
        rootItem.setExpanded(true);
        M3TreeView<String> treeView = new M3TreeView<>(rootItem);
        treeView.setSelectionStyle(M3TreeViewSelectionStyle.CHECKBOX);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeView.setPrefSize(280.0, 224.0);
        return treeView;
    }

    /// Shows a focus test scene and performs its initial CSS and layout pass.
    ///
    /// @param root the scene content
    /// @return the shown scene
    private static Scene show(Parent root) {
        Scene scene = new Scene(root, 320.0, 280.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        stage.requestFocus();
        root.applyCss();
        root.layout();
        return scene;
    }

    /// Returns attached non-empty cells ordered by visible row index.
    ///
    /// @param treeView the owning tree view
    /// @return the ordered materialized cells
    private static @Unmodifiable List<M3TreeCell<?>> visibleCells(M3TreeView<?> treeView) {
        ArrayList<M3TreeCell<?>> cells = new ArrayList<>();
        for (Node node : treeView.lookupAll(".m3-tree-cell")) {
            if (node instanceof M3TreeCell<?> cell && !cell.isEmpty()) {
                cells.add(cell);
            }
        }
        cells.sort(Comparator.comparingInt(M3TreeCell::getIndex));
        return List.copyOf(cells);
    }

    /// Verifies logical row focus while the tree remains the scene focus owner.
    ///
    /// @param treeView the focused tree
    /// @param expectedIndex the expected logical row index
    private static void assertLogicalCellFocus(M3TreeView<?> treeView, int expectedIndex) {
        M3TreeCell<?> focusedCell = visibleCells(treeView).stream()
                .filter(cell -> cell.getIndex() == expectedIndex)
                .findFirst()
                .orElseThrow(() -> new AssertionError("focused tree cell is not materialized"));

        assertTrue(treeView.isFocused());
        assertTrue(focusedCell.isFocused());
        assertTrue(focusedCell.getPseudoClassStates().contains(FOCUS_VISIBLE));
        Node focusIndicator = java.util.Objects.requireNonNull(
                focusedCell.lookup(".m3-focus-indicator"),
                "focused tree row indicator"
        );
        assertEquals(1.0, focusIndicator.getOpacity(), 0.001);
        assertEquals(0.0, focusIndicator.getLayoutX(), 0.001);
        assertEquals(0.0, focusIndicator.getLayoutY(), 0.001);
        assertEquals(focusedCell.getWidth(), focusIndicator.getLayoutBounds().getWidth(), 0.001);
        assertEquals(focusedCell.getHeight(), focusIndicator.getLayoutBounds().getHeight(), 0.001);
        assertTrue(focusedCell.getBackground() == null
                        || focusedCell.getBackground().getFills().stream().allMatch(fill ->
                        fill.getFill() instanceof Color color && color.getOpacity() <= 0.001),
                "checkbox focused row should not retain the stock JavaFX focus background");
    }

    /// Creates an unmodified Tab or Shift+Tab key-pressed event.
    ///
    /// @param shiftDown whether reverse traversal is requested
    /// @return the key event
    private static KeyEvent tabKeyEvent(boolean shiftDown) {
        return keyEvent(KeyCode.TAB, shiftDown);
    }

    /// Creates one key-pressed event with optional Shift state.
    ///
    /// @param code the key code
    /// @param shiftDown whether Shift is down
    /// @return the key event
    private static KeyEvent keyEvent(KeyCode code, boolean shiftDown) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                code.getName(),
                code.getName(),
                code,
                shiftDown,
                false,
                false,
                false
        );
    }
}
