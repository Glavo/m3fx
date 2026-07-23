// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Displays the local game-directory context and a dense selectable instance list.
@NotNullByDefault
public final class HMCLInstancesView extends HMCLDemoView {
    /// Creates the instance-management page.
    ///
    /// @param strings the localization source
    /// @param state the shared demo state
    /// @param actions the application command sink
    public HMCLInstancesView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        state.getInstances().addListener((javafx.collections.ListChangeListener<HMCLDemoInstance>) change -> refreshView());
        state.selectedInstanceProperty().addListener((observable, oldInstance, newInstance) -> refreshView());
        initializeView();
    }

    /// Creates the instances page with a game-directory sidebar and compact list rows.
    ///
    /// @return the instances page tree
    @Override
    protected Node createContent() {
        M3TextField search = new M3TextField();
        search.setPromptText(text("instances.search"));
        search.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(search, Priority.ALWAYS);
        M3IconButton refresh = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES));
        refresh.setAccessibleText(text("common.refresh"));
        refresh.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_REFRESH));
        HBox toolbar = new HBox(8.0, refresh, search);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        M3ListPane instances = createList();
        populateInstances(instances, "");
        search.textProperty().addListener((observable, oldText, newText) -> populateInstances(instances, newText));
        VBox center = new VBox(12.0, toolbar, instances);
        center.setMinWidth(0.0);
        center.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(center, Priority.ALWAYS);
        HBox layout = new HBox(24.0, createSidebar(), center);
        layout.setMinWidth(0.0);
        layout.setMaxWidth(Double.MAX_VALUE);
        return page(heading(text("instances.title"), text("instances.subtitle")), layout);
    }

    /// Creates the fixed-width game-directory context sidebar.
    ///
    /// @return the sidebar node
    private Node createSidebar() {
        M3ListPane directories = createList();
        for (HMCLDemoInstance instance : state.getInstances()) {
            M3ListItem item = new M3ListItem(instance.name());
            item.setSupportingText(text("instances.card.version", instance.gameVersion()));
            item.setSelected(instance.equals(state.getSelectedInstance()));
            item.setOnAction(event -> select(instance));
            directories.getItems().add(item);
        }
        M3Button add = new M3Button(text("instances.add"), M3ButtonVariant.FILLED);
        add.setGraphic(HMCLDemoIcons.create(HMCLDemoIcons.ADD));
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_ADD_INSTANCE));
        M3Button importPack = new M3Button(text("instances.import"), M3ButtonVariant.TEXT);
        importPack.setMaxWidth(Double.MAX_VALUE);
        importPack.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_ADD_INSTANCE));
        M3Button settings = new M3Button(text("nav.settings"), M3ButtonVariant.TEXT);
        settings.setMaxWidth(Double.MAX_VALUE);
        settings.setOnAction(event -> actions.navigate(HMCLDemoActions.ROUTE_SETTINGS));
        VBox sidebar = new VBox(8.0, directories, add, importPack, settings);
        sidebar.setPrefWidth(200.0);
        sidebar.setMinWidth(180.0);
        sidebar.setMaxWidth(220.0);
        return sidebar;
    }

    /// Replaces list rows with instances that match a case-insensitive query.
    ///
    /// @param target the list that owns the rows
    /// @param query the query to apply
    private void populateInstances(M3ListPane target, String query) {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        target.getItems().clear();
        for (HMCLDemoInstance instance : state.getInstances()) {
            if (matches(instance, normalized)) target.getItems().add(createInstanceRow(instance));
        }
        if (target.getItems().isEmpty()) target.getItems().add(new M3ListItem(text("instances.empty")));
    }

    /// Returns whether an instance matches a normalized query.
    ///
    /// @param instance the instance to inspect
    /// @param query the normalized query
    /// @return whether the instance should be visible
    private static boolean matches(HMCLDemoInstance instance, String query) {
        return query.isEmpty() || instance.name().toLowerCase(Locale.ROOT).contains(query)
                || instance.gameVersion().toLowerCase(Locale.ROOT).contains(query)
                || instance.loader().toLowerCase(Locale.ROOT).contains(query);
    }

    /// Creates one dense instance row with inline update and management actions.
    ///
    /// @param instance the represented instance
    /// @return the configured list row
    private M3ListItem createInstanceRow(HMCLDemoInstance instance) {
        M3ListItem row = new M3ListItem(instance.name());
        M3Text icon = new M3Text("◆", M3TextRole.TITLE_MEDIUM);
        icon.setPrefSize(32.0, 32.0);
        icon.setMinSize(32.0, 32.0);
        icon.setMaxSize(32.0, 32.0);
        row.setLeading(icon);
        row.setSupportingText(text("instances.card.details", instance.gameVersion(), instance.loader()));
        M3Button update = new M3Button(text("common.refresh"), M3ButtonVariant.TEXT);
        update.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_REFRESH, instance.id()));
        M3Button manage = new M3Button(text("common.manage"), M3ButtonVariant.TONAL);
        manage.setOnAction(event -> { select(instance); actions.navigate(HMCLDemoActions.ROUTE_INSTANCE_DETAIL); });
        HBox actionsRow = new HBox(6.0, update, manage);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        row.setTrailing(actionsRow);
        row.setOnAction(event -> select(instance));
        return row;
    }

    /// Selects an instance and reports the selection to the demo command sink.
    ///
    /// @param instance the instance to select
    private void select(HMCLDemoInstance instance) {
        state.selectInstance(instance.id());
        actions.dispatch(HMCLDemoActions.ACTION_SELECT_INSTANCE, instance.id());
    }

    /// Creates a non-selecting segmented list for compact contextual rows.
    ///
    /// @return the configured list pane
    private static M3ListPane createList() {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }
}
