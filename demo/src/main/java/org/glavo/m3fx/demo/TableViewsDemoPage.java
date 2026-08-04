// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import org.glavo.m3fx.controls.M3TableView;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

/// Builds the Table Views extension showcase page.
@NotNullByDefault
final class TableViewsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    TableViewsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the table-view extension page.
    ///
    /// @return the complete table-view showcase
    Node createContent() {
        M3TableView<ProjectStatus> sortable = createProjectTable("demo-table-view-sortable");
        sortable.getItems().setAll(projectRows());
        sortable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sortable.getSelectionModel().selectIndices(1, 4);
        TableColumn<ProjectStatus, ?> updatedColumn = sortable.getColumns().get(3);
        updatedColumn.setSortType(TableColumn.SortType.DESCENDING);
        sortable.getSortOrder().add(updatedColumn);
        sortable.sort();

        M3TableView<ProjectStatus> compact = createProjectTable("demo-table-view-compact");
        compact.getItems().setAll(projectRows().subList(0, 4));
        compact.setFixedCellSize(48.0);
        compact.getSelectionModel().select(2);
        compact.setPrefHeight(272.0);
        compact.setMaxHeight(272.0);

        M3TableView<ProjectStatus> empty = createProjectTable("demo-table-view-empty");
        M3Text placeholder = new M3Text("No projects match the current filters", M3TextRole.BODY_MEDIUM);
        placeholder.getStyleClass().add("demo-table-view-placeholder");
        empty.setPlaceholder(placeholder);
        empty.setPrefHeight(224.0);
        empty.setMaxHeight(224.0);

        return createGallery(
                createFullWidthShowcaseGroup("Sortable and Resizable", sortable),
                createFullWidthShowcaseGroup("Compact Rows", compact),
                createFullWidthShowcaseGroup("Empty State", empty)
        );
    }

    /// Creates a responsive project-status table with sortable, resizable columns.
    ///
    /// @param styleClass the demo style class identifying the sample role
    /// @return the configured table
    private static M3TableView<ProjectStatus> createProjectTable(String styleClass) {
        M3TableView<ProjectStatus> tableView = new M3TableView<>();
        tableView.getStyleClass().addAll("demo-table-view", styleClass);

        TableColumn<ProjectStatus, String> project = new TableColumn<>("Project");
        project.setId("project");
        project.setMinWidth(180.0);
        project.setPrefWidth(240.0);
        project.setCellValueFactory(features -> new ReadOnlyStringWrapper(features.getValue().project()));

        TableColumn<ProjectStatus, String> status = new TableColumn<>("Status");
        status.setId("status");
        status.setMinWidth(120.0);
        status.setPrefWidth(160.0);
        status.setCellValueFactory(features -> new ReadOnlyStringWrapper(features.getValue().status()));

        TableColumn<ProjectStatus, Number> issues = new TableColumn<>("Open issues");
        issues.setId("issues");
        issues.setMinWidth(112.0);
        issues.setPrefWidth(128.0);
        issues.setCellValueFactory(features -> new ReadOnlyIntegerWrapper(features.getValue().openIssues()));

        TableColumn<ProjectStatus, String> updated = new TableColumn<>("Last updated");
        updated.setId("updated");
        updated.setMinWidth(132.0);
        updated.setPrefWidth(168.0);
        updated.setCellValueFactory(features -> new ReadOnlyStringWrapper(features.getValue().lastUpdated()));

        tableView.getColumns().addAll(List.of(project, status, issues, updated));
        tableView.setPrefHeight(392.0);
        tableView.setMaxHeight(392.0);
        configureResponsiveWidth(tableView, 760.0);
        tableView.setMaxWidth(840.0);
        return tableView;
    }

    /// Returns deterministic project rows used by the interactive table samples.
    ///
    /// @return the immutable project rows
    private static List<ProjectStatus> projectRows() {
        return List.of(
                new ProjectStatus("M3FX", "Active", 14, "2026-08-04"),
                new ProjectStatus("MonetFX", "Stable", 3, "2026-08-02"),
                new ProjectStatus("HMCL", "Active", 27, "2026-08-03"),
                new ProjectStatus("TUIFX", "Planning", 8, "2026-07-29"),
                new ProjectStatus("Javif", "Active", 19, "2026-08-04"),
                new ProjectStatus("SCSSFX", "Stable", 2, "2026-07-24"),
                new ProjectStatus("Arkivo", "Review", 6, "2026-07-31"),
                new ProjectStatus("Kala IO", "Planning", 11, "2026-07-27")
        );
    }

    /// One immutable row displayed by the table-view samples.
    ///
    /// @param project     the project name
    /// @param status      the current project status
    /// @param openIssues  the number of open issues
    /// @param lastUpdated the ISO date of the latest update
    @NotNullByDefault
    private record ProjectStatus(String project, String status, int openIssues, String lastUpdated) {
    }
}
