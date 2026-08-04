// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3TableRowSkin;
import org.glavo.m3fx.skins.M3TableViewSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies Material table state, inherited data behavior, rendering, directionality, and skin lifecycle.
@NotNullByDefault
final class M3TableViewTest {
    /// The Material one-line list height used by default table rows and headers.
    private static final double MATERIAL_ONE_LINE_HEIGHT = 56.0;

    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies constructors, inherited defaults, Material identity, and the default row factory.
    @Test
    void exposesStableDefaults() {
        FxTestUtils.runOnFxThread(() -> {
            M3TableView<ProjectRow> emptyTable = new M3TableView<>();
            assertTrue(emptyTable.getItems().isEmpty());
            assertEquals(AccessibleRole.TABLE_VIEW, emptyTable.getAccessibleRole());
            assertTrue(emptyTable.isFocusTraversable());
            assertTrue(emptyTable.getStyleClass().contains("m3-table-view"));
            assertTrue(emptyTable.getRowFactory().call(emptyTable) instanceof M3TableRow<?>);
            assertEquals(SelectionMode.SINGLE, emptyTable.getSelectionModel().getSelectionMode());
            assertFalse(emptyTable.getSelectionModel().isCellSelectionEnabled());

            List<ProjectRow> values = rows(3);
            M3TableView<ProjectRow> populatedTable = new M3TableView<>(FXCollections.observableArrayList(values));
            assertEquals(values, List.copyOf(populatedTable.getItems()));

            populatedTable.setRowFactory(tableView -> new NamedTableRow());
            assertTrue(populatedTable.getRowFactory().call(populatedTable) instanceof NamedTableRow);
        });
    }

    /// Verifies that inherited columns, sorting, selection, resizing, and editing models remain authoritative.
    @Test
    void preservesTableModels() {
        FxTestUtils.runOnFxThread(() -> {
            M3TableView<ProjectRow> tableView = tableWithColumns(rows(4));
            TableColumn<ProjectRow, String> nameColumn = nameColumn(tableView);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectIndices(0, 2);
            assertEquals(List.of(0, 2), List.copyOf(tableView.getSelectionModel().getSelectedIndices()));

            nameColumn.setSortType(TableColumn.SortType.DESCENDING);
            tableView.getSortOrder().clear();
            tableView.getSortOrder().add(nameColumn);
            tableView.sort();
            assertEquals("Project 4", tableView.getItems().get(0).name());
            assertSame(nameColumn, tableView.getSortOrder().get(0));

            double originalWidth = nameColumn.getWidth();
            assertTrue(tableView.resizeColumn(nameColumn, 24.0));
            assertTrue(nameColumn.getWidth() > originalWidth);

            tableView.setEditable(true);
            tableView.edit(0, nameColumn);
            assertTrue(tableView.isEditable());
        });
    }

    /// Verifies Material rows, header geometry, selected surfaces, and shared scrollbar styling.
    @Test
    void stylesMaterialTableStructure() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3TableView<ProjectRow> tableView = tableWithColumns(rows(24));
            tableView.getSelectionModel().select(1);
            tableView.setPrefSize(520.0, 280.0);
            StackPane root = themedRoot(tableView, 560.0, 320.0);
            layout(root, 560.0, 320.0);

            assertTrue(tableView.getSkin() instanceof M3TableViewSkin<?>);
            assertEquals(MATERIAL_ONE_LINE_HEIGHT, tableView.getFixedCellSize(), 0.001);
            List<M3TableRow<?>> visibleRows = visibleRows(tableView);
            assertFalse(visibleRows.isEmpty());
            assertTrue(visibleRows.stream().allMatch(row -> row.getSkin() instanceof M3TableRowSkin<?>));
            assertTrue(visibleRows.stream()
                    .allMatch(row -> row.lookup(".m3-state-layer-container") != null));
            assertTrue(visibleRows.stream()
                    .allMatch(row -> Math.abs(row.getHeight() - MATERIAL_ONE_LINE_HEIGHT) < 0.001));

            M3TableRow<?> selectedRow = visibleRows.stream()
                    .filter(TableRow::isSelected)
                    .findFirst()
                    .orElseThrow();
            assertFalse(selectedRow.getBackground().getFills().isEmpty());

            List<Node> headers = tableView.lookupAll(".column-header").stream()
                    .filter(node -> node.getStyleClass().contains("table-column"))
                    .toList();
            assertFalse(headers.isEmpty());
            assertTrue(headers.stream()
                    .allMatch(header -> Math.abs(header.getBoundsInParent().getHeight()
                            - MATERIAL_ONE_LINE_HEIGHT) < 0.001),
                    () -> "column header heights: " + headers.stream()
                            .map(header -> header.getStyleClass() + "="
                                    + header.getBoundsInParent().getHeight())
                            .toList());

            List<ScrollBar> scrollBars = tableView.lookupAll(".scroll-bar").stream()
                    .filter(ScrollBar.class::isInstance)
                    .map(ScrollBar.class::cast)
                    .toList();
            assertEquals(2, scrollBars.size());
            assertTrue(scrollBars.stream()
                    .allMatch(scrollBar -> scrollBar.getStyleClass().contains("m3-scroll-bar")));
        }));
    }

    /// Verifies right-to-left layout and replacement of the Material skin and row factory.
    @Test
    void preservesDirectionalityAndStockSkinReplacement() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3TableView<ProjectRow> tableView = tableWithColumns(rows(6));
            tableView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            tableView.setPrefSize(480.0, 260.0);
            StackPane root = themedRoot(tableView, 520.0, 300.0);
            layout(root, 520.0, 300.0);

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, tableView.getEffectiveNodeOrientation());
            assertTrue(visibleRows(tableView).stream()
                    .allMatch(row -> row.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT));

            Object originalSkin = tableView.getSkin();
            TableViewSkin<ProjectRow> replacementSkin = new TableViewSkin<>(tableView);
            tableView.setSkin(replacementSkin);
            layout(root, 520.0, 300.0);
            assertNotSame(originalSkin, tableView.getSkin());
            assertSame(replacementSkin, tableView.getSkin());
            assertEquals(1, tableView.lookupAll(".virtual-flow").size());
        }));
    }

    /// Creates a table with representative sortable name and numeric columns.
    ///
    /// @param values the initial rows
    /// @return the configured table
    private static M3TableView<ProjectRow> tableWithColumns(List<ProjectRow> values) {
        M3TableView<ProjectRow> tableView = new M3TableView<>(FXCollections.observableArrayList(values));
        TableColumn<ProjectRow, String> name = new TableColumn<>("Name");
        name.setId("name");
        name.setPrefWidth(240.0);
        name.setCellValueFactory(features -> new ReadOnlyStringWrapper(features.getValue().name()));
        TableColumn<ProjectRow, Number> tasks = new TableColumn<>("Tasks");
        tasks.setPrefWidth(120.0);
        tasks.setCellValueFactory(features -> new ReadOnlyIntegerWrapper(features.getValue().tasks()));
        tableView.getColumns().add(name);
        tableView.getColumns().add(tasks);
        return tableView;
    }

    /// Returns the name column from a configured test table.
    ///
    /// @param tableView the table containing the named column
    /// @return the name column
    @SuppressWarnings("unchecked")
    private static TableColumn<ProjectRow, String> nameColumn(M3TableView<ProjectRow> tableView) {
        return (TableColumn<ProjectRow, String>) tableView.getColumns().stream()
                .filter(column -> "name".equals(column.getId()))
                .findFirst()
                .orElseThrow();
    }

    /// Creates deterministic test rows.
    ///
    /// @param count the number of rows
    /// @return the generated rows
    private static List<ProjectRow> rows(int count) {
        ArrayList<ProjectRow> rows = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            rows.add(new ProjectRow("Project " + index, index * 3));
        }
        return List.copyOf(rows);
    }

    /// Creates a themed root containing one table.
    ///
    /// @param tableView the table to present
    /// @param width     the root width
    /// @param height    the root height
    /// @return the themed root
    private static StackPane themedRoot(M3TableView<?> tableView, double width, double height) {
        StackPane root = new StackPane(tableView);
        M3ThemeManager.install(root, M3Theme.defaultTheme());
        new javafx.scene.Scene(root, width, height);
        return root;
    }

    /// Applies CSS and lays out one detached test root at a stable size.
    ///
    /// @param root   the root to lay out
    /// @param width  the root width
    /// @param height the root height
    private static void layout(StackPane root, double width, double height) {
        root.applyCss();
        root.resize(width, height);
        root.layout();
        root.applyCss();
        root.layout();
    }

    /// Returns the non-empty Material rows currently materialized by a table.
    ///
    /// @param tableView the table to inspect
    /// @return the visible non-empty Material rows in index order
    private static List<M3TableRow<?>> visibleRows(M3TableView<?> tableView) {
        ArrayList<M3TableRow<?>> rows = new ArrayList<>();
        for (Node node : tableView.lookupAll(".m3-table-row")) {
            if (node instanceof M3TableRow<?> row && !row.isEmpty() && row.isVisible()) {
                rows.add(row);
            }
        }
        rows.sort(java.util.Comparator.comparingInt(TableRow::getIndex));
        return List.copyOf(rows);
    }

    /// One immutable table row used by the tests.
    ///
    /// @param name  the project name
    /// @param tasks the task count
    @NotNullByDefault
    private record ProjectRow(String name, int tasks) {
    }

    /// Identifies an application-supplied row factory during replacement tests.
    @NotNullByDefault
    private static final class NamedTableRow extends TableRow<ProjectRow> {
        /// Creates an empty named row.
        private NamedTableRow() {
        }
    }
}
