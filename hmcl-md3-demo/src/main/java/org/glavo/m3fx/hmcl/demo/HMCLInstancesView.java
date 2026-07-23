// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3Text;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Displays game directories and the searchable instance list.
@NotNullByDefault
final class HMCLInstancesView extends BorderPane {
    /// The localization source.
    private final HMCLDemoStrings strings;

    /// The shared state.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// The directories section label.
    private final M3Text directoriesSection = HMCLDemoUi.sectionLabel("");

    /// The left directory list host.
    private final VBox directoryList = new VBox(4.0);

    /// The new-game action.
    private final M3ListItem newGameItem = HMCLDemoUi.navItem("", HMCLDemoIcons.ADD, null);

    /// The import-modpack action.
    private final M3ListItem importItem = HMCLDemoUi.navItem("", HMCLDemoIcons.DOWNLOAD, null);

    /// The global-settings action.
    private final M3ListItem globalSettingsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.SETTINGS, null);

    /// The instance search field.
    private final M3SearchBar searchBar = new M3SearchBar();

    /// The refresh control.
    private final M3IconButton refreshButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.REFRESH));

    /// The add-instance control.
    private final M3IconButton addButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.ADD));

    /// The instance list surface.
    private final M3ListPane instanceList = new M3ListPane();

    /// Creates the instances page.
    ///
    /// @param strings the localization source
    /// @param state the shared state
    /// @param controller the application controller
    HMCLInstancesView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoController controller) {
        this.strings = strings;
        this.state = state;
        this.controller = controller;

        getStyleClass().add("hmcl-secondary-page");
        instanceList.setListStyle(M3ListStyle.SEGMENTED);
        instanceList.setSelectionMode(M3SelectionMode.SINGLE);
        instanceList.getStyleClass().add("hmcl-dense-list");

        newGameItem.setOnAction(event -> {
            state.addDemoInstance();
            controller.showMessageKey("snackbar.instance_added");
        });
        importItem.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        globalSettingsItem.setOnAction(event -> controller.openSettings());
        refreshButton.setOnAction(event -> controller.showMessageKey("snackbar.refreshed"));
        addButton.setOnAction(event -> {
            state.addDemoInstance();
            controller.showMessageKey("snackbar.instance_added");
        });
        searchBar.textProperty().bindBidirectional(state.instanceSearchQueryProperty());

        VBox sidebar = HMCLDemoUi.sidebar(
                directoriesSection,
                directoryList,
                HMCLDemoUi.vgrow(),
                newGameItem,
                importItem,
                globalSettingsItem
        );
        setLeft(sidebar);

        HBox toolbar = HMCLDemoUi.toolbar(searchBar, HMCLDemoUi.hgrow(), refreshButton, addButton);
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        VBox body = new VBox(toolbar, instanceList);
        body.getStyleClass().add("hmcl-list-surface");
        VBox.setVgrow(instanceList, Priority.ALWAYS);
        VBox center = HMCLDemoUi.contentColumn(body);
        VBox.setVgrow(body, Priority.ALWAYS);
        setCenter(center);

        state.selectedDirectoryProperty().addListener((observable, oldValue, newValue) -> {
            rebuildDirectories();
            rebuildInstances();
        });
        state.selectedInstanceProperty().addListener((observable, oldValue, newValue) -> rebuildInstances());
        state.getDirectories().addListener(
                (ListChangeListener<HMCLDemoGameDirectory>) change -> rebuildDirectories());
        state.getFilteredInstances().addListener(
                (ListChangeListener<HMCLDemoInstance>) change -> rebuildInstances());
        state.getInstances().addListener((ListChangeListener<HMCLDemoInstance>) change -> rebuildInstances());

        refreshLocale();
        rebuildDirectories();
        rebuildInstances();
    }

    /// Updates static labels.
    void refreshLocale() {
        directoriesSection.setText(strings.get("instances.section.directories"));
        newGameItem.setHeadlineText(strings.get("instances.new_game"));
        importItem.setHeadlineText(strings.get("instances.import"));
        globalSettingsItem.setHeadlineText(strings.get("instances.global_settings"));
        searchBar.setPromptText(strings.get("instances.search"));
        refreshButton.setAccessibleText(strings.get("common.refresh"));
        addButton.setAccessibleText(strings.get("instances.add"));
        rebuildDirectories();
        rebuildInstances();
    }

    /// Rebuilds the left directory list.
    private void rebuildDirectories() {
        directoryList.getChildren().clear();
        HMCLDemoGameDirectory selected = state.getSelectedDirectory();
        for (HMCLDemoGameDirectory directory : state.getDirectories()) {
            M3ListItem item = HMCLDemoUi.navItem(
                    directory.name(),
                    directory.path(),
                    HMCLDemoIcons.create(HMCLDemoIcons.FOLDER),
                    () -> state.selectDirectory(directory.id())
            );
            item.setSelected(directory.equals(selected));
            directoryList.getChildren().add(item);
        }
    }

    /// Rebuilds the instance rows for the current filters.
    private void rebuildInstances() {
        instanceList.getItems().clear();
        @Nullable HMCLDemoInstance selected = state.getSelectedInstance();
        for (HMCLDemoInstance instance : state.getFilteredInstances()) {
            M3IconButton manage = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.SETTINGS));
            manage.setAccessibleText(strings.get("instances.manage"));
            manage.setOnAction(event -> controller.openInstance(instance.id()));

            M3Button launch = new M3Button(strings.get("instances.launch"), M3ButtonVariant.TONAL);
            launch.setOnAction(event -> {
                state.selectInstance(instance.id());
                controller.launchSelected();
            });

            HBox trailing = new HBox(6.0, manage, launch);
            trailing.setAlignment(Pos.CENTER_RIGHT);

            M3ListItem row = new M3ListItem(instance.name());
            row.getStyleClass().add("hmcl-instance-row");
            row.setSupportingText(instance.gameVersion() + "  " + instance.loader());
            row.setLeading(HMCLDemoUi.instanceIcon(instance, 32.0));
            row.setTrailing(trailing);
            row.setSelected(instance.equals(selected));
            row.setOnAction(event -> state.selectInstance(instance.id()));
            instanceList.getItems().add(row);
        }
    }
}
