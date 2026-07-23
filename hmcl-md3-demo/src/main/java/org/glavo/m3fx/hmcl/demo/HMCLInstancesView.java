// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Displays the local game-directory context beside a compact selectable instance list.
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

    /// Creates the instance browser without a page heading because the application shell owns the title.
    ///
    /// @return the instances page tree
    @Override
    protected Node createContent() {
        M3TextField search = new M3TextField();
        search.setPromptText(text("instances.search"));
        search.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(search, Priority.ALWAYS);

        M3IconButton refresh = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.REFRESH));
        refresh.setAccessibleText(text("common.refresh"));
        refresh.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_REFRESH));

        HBox toolbar = new HBox(8.0, search, refresh);
        toolbar.getStyleClass().add("hmcl-list-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        M3ListPane instances = createList();
        populateInstances(instances, "");
        search.textProperty().addListener((observable, oldText, newText) -> populateInstances(instances, newText));

        ScrollPane listScroll = new ScrollPane(instances);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        M3ScrollPanes.style(listScroll);

        VBox center = new VBox(0.0, toolbar, listScroll);
        center.getStyleClass().add("hmcl-list-surface");
        center.setMinWidth(0.0);
        center.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(listScroll, Priority.ALWAYS);
        return contextualPage(createSidebar(), center);
    }

    /// Creates the fixed-width game-directory context and its bottom management actions.
    ///
    /// @return the sidebar node
    private Node createSidebar() {
        M3Text label = new M3Text(text("instances.title"), M3TextRole.LABEL_LARGE);
        M3ListPane directories = createList();
        M3ListItem defaultDirectory = compactItem(text("instances.directory.default"));
        defaultDirectory.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.INSTANCES));
        defaultDirectory.setSelected(true);
        M3ListItem officialDirectory = compactItem(text("instances.directory.official"));
        officialDirectory.setLeading(HMCLDemoAssets.imageView("img/grass.png", 24.0, 24.0));
        directories.getItems().addAll(defaultDirectory, officialDirectory);

        M3Button add = new M3Button(text("instances.add"), M3ButtonVariant.FILLED);
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_ADD_INSTANCE));

        M3Button importPack = new M3Button(text("instances.import"), M3ButtonVariant.TEXT);
        importPack.setMaxWidth(Double.MAX_VALUE);
        importPack.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_ADD_INSTANCE));

        M3Button settings = new M3Button(text("nav.settings"), M3ButtonVariant.TEXT);
        settings.setMaxWidth(Double.MAX_VALUE);
        settings.setOnAction(event -> actions.navigate(HMCLDemoActions.ROUTE_SETTINGS));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox sidebar = new VBox(8.0, label, directories, spacer, add, importPack, settings);
        sidebar.setPadding(new javafx.geometry.Insets(12.0, 8.0, 10.0, 8.0));
        sidebar.setPrefWidth(200.0);
        sidebar.setMinWidth(200.0);
        sidebar.setMaxWidth(200.0);
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
            if (matches(instance, normalized)) {
                target.getItems().add(createInstanceRow(instance));
            }
        }
        if (target.getItems().isEmpty()) {
            target.getItems().add(compactItem(text("instances.empty")));
        }
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

    /// Creates one 48-pixel instance row with inline management access.
    ///
    /// @param instance the represented instance
    /// @return the configured list row
    private M3ListItem createInstanceRow(HMCLDemoInstance instance) {
        M3ListItem row = compactItem(instance.name());
        row.getStyleClass().add("hmcl-instance-row");
        row.setLeading(instanceIcon(instance));
        row.setSupportingText(text("instances.card.details", instance.gameVersion(), instance.loader()));

        M3IconButton manage = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.SETTINGS));
        manage.setAccessibleText(text("instances.manage"));
        manage.setOnAction(event -> {
            select(instance);
            actions.navigate(HMCLDemoActions.ROUTE_INSTANCE_DETAIL);
        });
        row.setTrailing(manage);
        row.setOnAction(event -> select(instance));
        return row;
    }

    /// Creates a one-line row with HMCL's compact 48-pixel list metric.
    ///
    /// @param headline the visible row title
    /// @return the configured compact list item
    private static M3ListItem compactItem(String headline) {
        M3ListItem item = new M3ListItem(headline);
        item.setOneLineHeight(48.0);
        item.setTwoLineHeight(48.0);
        return item;
    }

    /// Returns an instance icon selected from its configured loader.
    ///
    /// @param instance the represented instance
    /// @return the configured image view
    private static ImageView instanceIcon(HMCLDemoInstance instance) {
        String loader = instance.loader().toLowerCase(Locale.ROOT);
        String image = loader.contains("neoforge")
                ? "neoforge"
                : loader.contains("forge")
                ? "forge"
                : loader.contains("fabric")
                ? "fabric"
                : loader.contains("quilt")
                ? "quilt"
                : "grass";
        return HMCLDemoAssets.imageView("img/" + image + ".png", 24.0, 24.0);
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
        list.getStyleClass().add("hmcl-dense-list");
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }
}
