// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies scene traversal and logical row focus for virtualized Material table views.
@Tier2Test
@NotNullByDefault
final class M3TableViewFocusTraversalTest {
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

    /// Verifies forward and reverse Tab traversal treat a table as one composite focus stop.
    @Test
    void tabTraversalEntersFocusedRowAndExitsCompositeInBothDirections() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button before = new M3Button("Before");
            M3Button after = new M3Button("After");
            M3TableView<String> tableView = table();
            tableView.getSelectionModel().select(2);

            VBox root = new VBox(before, tableView, after);
            Scene scene = show(root);

            before.requestFocus();
            before.fireEvent(tabKeyEvent(false));

            assertSame(tableView, scene.getFocusOwner());
            assertEquals(2, tableView.getFocusModel().getFocusedIndex());
            assertLogicalRowFocus(tableView, 2);
            assertTrue(tableView.lookupAll(".table-cell").stream()
                    .filter(TableCell.class::isInstance)
                    .allMatch(cell -> !cell.isFocusTraversable()));

            tableView.fireEvent(tabKeyEvent(false));
            assertTrue(after.isFocused());

            after.fireEvent(tabKeyEvent(true));
            assertSame(tableView, scene.getFocusOwner());
            assertEquals(2, tableView.getFocusModel().getFocusedIndex());
            assertLogicalRowFocus(tableView, 2);

            tableView.fireEvent(tabKeyEvent(true));
            assertTrue(before.isFocused());
        });
    }

    /// Verifies inherited arrow navigation moves both logical focus and single selection.
    @Test
    void arrowNavigationMovesFocusedSelection() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button before = new M3Button("Before");
            M3TableView<String> tableView = table();
            tableView.getSelectionModel().select(1);
            VBox root = new VBox(before, tableView);
            show(root);
            before.requestFocus();
            before.fireEvent(tabKeyEvent(false));

            tableView.fireEvent(keyEvent(KeyCode.DOWN, false));
            assertEquals(2, tableView.getFocusModel().getFocusedIndex());
            assertEquals(2, tableView.getSelectionModel().getSelectedIndex());
            assertLogicalRowFocus(tableView, 2);

            tableView.fireEvent(keyEvent(KeyCode.UP, false));
            assertEquals(1, tableView.getFocusModel().getFocusedIndex());
            assertEquals(1, tableView.getSelectionModel().getSelectedIndex());
            assertLogicalRowFocus(tableView, 1);
        });
    }

    /// Verifies cell-selection mode renders cell-local selection and focus instead of a row-wide focus indicator.
    @Test
    void cellSelectionUsesCellLocalFeedback() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button before = new M3Button("Before");
            M3TableView<String> tableView = table();
            TableColumn<String, ?> column = tableView.getColumns().get(0);
            tableView.getSelectionModel().setCellSelectionEnabled(true);
            tableView.getSelectionModel().select(1, column);
            tableView.getFocusModel().focus(1, column);
            VBox root = new VBox(before, tableView);
            Scene scene = show(root);

            before.requestFocus();
            before.fireEvent(tabKeyEvent(false));

            assertSame(tableView, scene.getFocusOwner());
            TableCell<?, ?> focusedCell = visibleCell(tableView, 1, column);
            assertTrue(focusedCell.isSelected());
            assertTrue(focusedCell.isFocused());
            assertFalse(focusedCell.getBackground().getFills().isEmpty());
            assertFalse(focusedCell.getBorder().getStrokes().isEmpty());

            M3TableRow<?> focusedRow = visibleRows(tableView).stream()
                    .filter(row -> row.getIndex() == 1)
                    .findFirst()
                    .orElseThrow();
            @Nullable Node focusIndicator = focusedRow.lookup(".m3-focus-indicator");
            assertTrue(focusIndicator == null || Math.abs(focusIndicator.getOpacity()) < 0.001);
        });
    }

    /// Verifies a pointer press on a column header clears keyboard-only row focus feedback before reordering.
    @Test
    void headerPointerPressClearsRowFocusIndicator() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button before = new M3Button("Before");
            M3TableView<String> tableView = table();
            tableView.getSelectionModel().select(1);
            VBox root = new VBox(before, tableView);
            show(root);

            before.requestFocus();
            before.fireEvent(tabKeyEvent(false));
            M3TableRow<?> focusedRow = visibleRows(tableView).stream()
                    .filter(row -> row.getIndex() == 1)
                    .findFirst()
                    .orElseThrow();
            Node focusIndicator = Objects.requireNonNull(
                    focusedRow.lookup(".m3-focus-indicator"),
                    "focused table row indicator"
            );
            assertEquals(1.0, focusIndicator.getOpacity(), 0.001);

            Node columnHeader = tableView.lookupAll(".column-header").stream()
                    .filter(node -> node.getStyleClass().contains("table-column"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("table column header is not materialized"));
            double x = columnHeader.getBoundsInLocal().getWidth() / 2.0;
            double y = columnHeader.getBoundsInLocal().getHeight() / 2.0;
            columnHeader.fireEvent(primaryMouseEvent(columnHeader, MouseEvent.MOUSE_PRESSED, x, y, true));

            assertEquals(0.0, focusIndicator.getOpacity(), 0.001);
            columnHeader.fireEvent(primaryMouseEvent(columnHeader, MouseEvent.MOUSE_RELEASED, x, y, false));
        });
    }

    /// Creates a representative single-column table with every row materialized.
    ///
    /// @return the configured table
    private static M3TableView<String> table() {
        M3TableView<String> tableView = new M3TableView<>();
        tableView.getItems().addAll(List.of("Alpha", "Beta", "Gamma", "Delta"));
        TableColumn<String, String> column = new TableColumn<>("Name");
        column.setPrefWidth(280.0);
        column.setCellValueFactory(features -> new ReadOnlyStringWrapper(features.getValue()));
        tableView.getColumns().add(column);
        tableView.setPrefSize(280.0, 280.0);
        return tableView;
    }

    /// Shows a focus test scene and performs its initial CSS and layout pass.
    ///
    /// @param root the scene content
    /// @return the shown scene
    private static Scene show(Parent root) {
        Scene scene = new Scene(root, 320.0, 360.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        stage.requestFocus();
        root.applyCss();
        root.layout();
        return scene;
    }

    /// Returns attached non-empty rows ordered by visible index.
    ///
    /// @param tableView the owning table view
    /// @return the ordered materialized rows
    private static @Unmodifiable List<M3TableRow<?>> visibleRows(M3TableView<?> tableView) {
        ArrayList<M3TableRow<?>> rows = new ArrayList<>();
        for (Node node : tableView.lookupAll(".m3-table-row")) {
            if (node instanceof M3TableRow<?> row && !row.isEmpty()) {
                rows.add(row);
            }
        }
        rows.sort(Comparator.comparingInt(M3TableRow::getIndex));
        return List.copyOf(rows);
    }

    /// Returns the materialized cell at one row-and-column coordinate.
    ///
    /// @param tableView the owning table view
    /// @param rowIndex  the requested row index
    /// @param column    the requested column
    /// @return the matching visible cell
    private static TableCell<?, ?> visibleCell(
            M3TableView<?> tableView,
            int rowIndex,
            TableColumn<?, ?> column
    ) {
        return tableView.lookupAll(".table-cell").stream()
                .filter(TableCell.class::isInstance)
                .map(TableCell.class::cast)
                .filter(cell -> cell.getIndex() == rowIndex && cell.getTableColumn() == column)
                .findFirst()
                .orElseThrow(() -> new AssertionError("requested table cell is not materialized"));
    }

    /// Verifies logical row focus while the table remains the scene focus owner.
    ///
    /// @param tableView    the focused table
    /// @param expectedIndex the expected logical row index
    private static void assertLogicalRowFocus(M3TableView<?> tableView, int expectedIndex) {
        M3TableRow<?> focusedRow = visibleRows(tableView).stream()
                .filter(row -> row.getIndex() == expectedIndex)
                .findFirst()
                .orElseThrow(() -> new AssertionError("focused table row is not materialized"));

        assertTrue(tableView.isFocused());
        assertTrue(focusedRow.isFocused());
        Node focusIndicator = Objects.requireNonNull(
                focusedRow.lookup(".m3-focus-indicator"),
                "focused table row indicator"
        );
        assertEquals(1.0, focusIndicator.getOpacity(), 0.001);
        assertEquals(0.0, focusIndicator.getLayoutX(), 0.001);
        assertEquals(0.0, focusIndicator.getLayoutY(), 0.001);
        assertEquals(focusedRow.getWidth(), focusIndicator.getLayoutBounds().getWidth(), 0.001);
        assertEquals(focusedRow.getHeight(), focusIndicator.getLayoutBounds().getHeight(), 0.001);
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
    /// @param code      the key code
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

    /// Creates a primary-button mouse event at one local point of a node.
    ///
    /// @param node the event target
    /// @param eventType the mouse-event type
    /// @param x the local x coordinate
    /// @param y the local y coordinate
    /// @param primaryButtonDown whether the primary button is down
    /// @return the mouse event
    private static MouseEvent primaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        Point2D scenePoint = node.localToScene(x, y);
        Point2D screenPoint = node.localToScreen(x, y);
        double screenX = screenPoint == null ? scenePoint.getX() : screenPoint.getX();
        double screenY = screenPoint == null ? scenePoint.getY() : screenPoint.getY();
        return new MouseEvent(
                eventType,
                x,
                y,
                screenX,
                screenY,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                new PickResult(node, scenePoint.getX(), scenePoint.getY())
        );
    }
}
