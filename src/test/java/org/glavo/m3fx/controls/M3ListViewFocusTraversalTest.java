// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies scene traversal and logical cell focus for virtualized Material lists.
@Tier2Test
@NotNullByDefault
final class M3ListViewFocusTraversalTest {
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
                    stage.close();
                }
            }
        });
    }

    /// Verifies forward and reverse Tab traversal treat a virtualized list as one composite focus stop.
    @Test
    void tabTraversalEntersFocusedRowAndExitsCompositeInBothDirections() {
        FxTestUtils.runOnFxThread(() -> {
            Button before = new Button("Before");
            Button after = new Button("After");
            M3ListView<String> listView = listView("First", "Second", "Third");
            listView.setListStyle(M3ListStyle.SEGMENTED);
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.selectIndex(1);

            VBox root = new VBox(before, listView, after);
            show(root);

            before.requestFocus();
            before.fireEvent(tabKeyEvent(false));

            assertTrue(listView.isFocused());
            assertEquals(1, listView.getFocusedIndex());
            assertLogicalCellFocus(listView, 1);

            listView.focusIndex(2);
            listView.selectIndex(1);
            listView.fireEvent(tabKeyEvent(false));
            assertTrue(after.isFocused());

            after.fireEvent(tabKeyEvent(true));
            assertTrue(listView.isFocused());
            assertEquals(2, listView.getFocusedIndex());
            assertEquals(1, listView.getSelectedIndex());
            assertLogicalCellFocus(listView, 2);

            listView.fireEvent(tabKeyEvent(true));
            assertTrue(before.isFocused());
        });
    }

    /// Verifies a non-focusable page viewport does not hide a virtualized list from scene traversal.
    @Test
    void tabTraversalEntersListInsidePageViewport() {
        FxTestUtils.runOnFxThread(() -> {
            Button before = new Button("Before");
            M3ListView<String> listView = listView("First", "Second", "Third");
            listView.setListStyle(M3ListStyle.SEGMENTED);
            Button after = new Button("After");
            VBox page = new VBox(before, listView, after);
            ScrollPane viewport = new ScrollPane(page);
            viewport.setFocusTraversable(false);
            viewport.setFitToWidth(true);
            StackPane root = new StackPane(viewport);
            show(root);

            before.requestFocus();
            before.fireEvent(tabKeyEvent(false));

            assertTrue(listView.isFocused());
            assertEquals(0, listView.getFocusedIndex());
            assertLogicalCellFocus(listView, 0);

            listView.fireEvent(tabKeyEvent(false));
            assertTrue(after.isFocused());
        });
    }

    /// Verifies focus entry skips unreachable rows and keeps vertical list order independent of node orientation.
    @Test
    void focusEntrySkipsUnreachableRowsInLeftToRightAndRightToLeftLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            for (NodeOrientation orientation : List.of(
                    NodeOrientation.LEFT_TO_RIGHT,
                    NodeOrientation.RIGHT_TO_LEFT
            )) {
                M3ListItem hidden = new M3ListItem("Hidden");
                hidden.setVisible(false);
                M3ListItem disabled = new M3ListItem("Disabled");
                disabled.setDisable(true);
                M3ListItem firstReachable = new M3ListItem("First reachable");
                M3ListItem secondReachable = new M3ListItem("Second reachable");
                M3ListView<M3ListItem> listView = listView(hidden, disabled, firstReachable, secondReachable);
                listView.setNodeOrientation(orientation);
                Button outside = new Button("Outside");

                VBox root = new VBox(listView, outside);
                show(root);
                listView.requestFocus();

                assertEquals(2, listView.getFocusedIndex());
                assertSame(firstReachable, listView.getFocusedItem());
                assertEquals(-1, listView.getSelectedIndex());

                listView.clearFocus();
                listView.selectIndex(3);
                outside.requestFocus();
                listView.requestFocus();

                assertEquals(3, listView.getFocusedIndex());
                assertSame(secondReachable, listView.getFocusedItem());
            }
        });
    }

    /// Verifies a focused list rehomes logical focus when rows become unreachable or are removed.
    @Test
    void focusedListRehomesLogicalFocusAfterReachabilityAndItemChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem first = new M3ListItem("First");
            M3ListItem second = new M3ListItem("Second");
            M3ListItem third = new M3ListItem("Third");
            M3ListView<M3ListItem> listView = listView(first, second, third);
            VBox root = new VBox(listView);
            show(root);

            listView.requestFocus();
            assertEquals(0, listView.getFocusedIndex());

            first.setDisable(true);
            assertEquals(1, listView.getFocusedIndex());
            assertSame(second, listView.getFocusedItem());

            second.setVisible(false);
            assertEquals(2, listView.getFocusedIndex());
            assertSame(third, listView.getFocusedItem());

            listView.getItems().remove(third);
            assertEquals(-1, listView.getFocusedIndex());
            assertNull(listView.getFocusedItem());

            M3ListItem replacement = new M3ListItem("Replacement");
            listView.getItems().add(replacement);
            assertEquals(2, listView.getFocusedIndex());
            assertSame(replacement, listView.getFocusedItem());
        });
    }

    /// Verifies virtual cells remain traversal proxies and empty cells cannot claim list focus.
    @Test
    void virtualCellsRemainOutsideDirectTraversalAndEmptyCellsRejectFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = listView("First", "Second");
            VBox root = new VBox(listView);
            show(root);
            listView.requestFocus();

            for (M3ListCell<?> cell : visibleCells(listView)) {
                assertFalse(cell.isFocusTraversable());
                M3ListItem item = Objects.requireNonNull(cell.getListItem(), "visible list item");
                assertFalse(item.isFocusTraversable());
            }

            M3ListCell<String> emptyCell = new M3ListCell<>(listView);
            emptyCell.updateIndex(-1);
            assertTrue(emptyCell.isEmpty());
            assertFalse(emptyCell.isFocusTraversable());
            assertFalse(emptyCell.focusCell());
        });
    }

    /// Creates a virtualized list with stable dimensions suitable for focus tests.
    @SafeVarargs
    private static <T> M3ListView<T> listView(T... items) {
        M3ListView<T> listView = new M3ListView<>();
        listView.getItems().addAll(items);
        listView.setFixedCellSize(56.0);
        listView.setPrefSize(280.0, 168.0);
        return listView;
    }

    /// Shows a focus test scene and performs its initial CSS and layout pass.
    private static void show(Parent root) {
        M3ThemeManager.install(root, M3Theme.defaultTheme());
        Scene scene = new Scene(root, 320.0, 260.0);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        stage.requestFocus();
        root.applyCss();
        root.layout();
    }

    /// Returns attached non-empty cells ordered by virtualized data index.
    private static List<M3ListCell<?>> visibleCells(M3ListView<?> listView) {
        ArrayList<M3ListCell<?>> cells = new ArrayList<>();
        for (Node node : listView.lookupAll("." + M3ListCell.STYLE_CLASS)) {
            if (node instanceof M3ListCell<?> cell && !cell.isEmpty()) {
                cells.add(cell);
            }
        }
        cells.sort(Comparator.comparingInt(M3ListCell::getIndex));
        return List.copyOf(cells);
    }

    /// Verifies the requested row owns logical focus while the list remains the scene focus owner.
    private static void assertLogicalCellFocus(M3ListView<?> listView, int expectedIndex) {
        M3ListCell<?> focusedCell = visibleCells(listView).stream()
                .filter(cell -> cell.getIndex() == expectedIndex)
                .findFirst()
                .orElseThrow(() -> new AssertionError("focused cell is not materialized"));
        M3ListItem focusedItem = Objects.requireNonNull(focusedCell.getListItem(), "focused list item");

        assertTrue(listView.isFocused());
        assertFalse(focusedCell.isFocused());
        assertFalse(focusedItem.isFocused());
        assertTrue(focusedCell.getPseudoClassStates().contains(FOCUS_VISIBLE));
        assertTrue(focusedItem.getPseudoClassStates().contains(FOCUS_VISIBLE));
    }

    /// Creates an unmodified Tab or Shift+Tab key-pressed event.
    private static KeyEvent tabKeyEvent(boolean shiftDown) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                KeyCode.TAB.getName(),
                KeyCode.TAB.getName(),
                KeyCode.TAB,
                shiftDown,
                false,
                false,
                false
        );
    }
}
