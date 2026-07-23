// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Instance list primary destination.
@NotNullByDefault
final class HMCLInstancesView extends BorderPane {
    private final HMCLDemoController controller;
    private final HMCLDemoStrings strings;
    private final HMCLDemoState state;

    private final VBox directoryList = new VBox(0.0);
    private final M3Button newDirectoryButton = new M3Button();
    private final M3Button newGameButton = new M3Button();
    private final M3Button importButton = new M3Button();
    private final M3Button globalSettingsButton = new M3Button();
    private final M3SearchBar searchBar = new M3SearchBar();
    private final M3ListPane instanceList = new M3ListPane();

    /// Creates the instances page.
    ///
    /// @param controller the application controller
    HMCLInstancesView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().add("hmcl-instances-page");
        HMCLDemoUi.fill(this);

        newDirectoryButton.setVariant(M3ButtonVariant.TEXT);
        newGameButton.setVariant(M3ButtonVariant.FILLED);
        importButton.setVariant(M3ButtonVariant.TONAL);
        globalSettingsButton.setVariant(M3ButtonVariant.TEXT);
        newDirectoryButton.setMaxWidth(Double.MAX_VALUE);
        newGameButton.setMaxWidth(Double.MAX_VALUE);
        importButton.setMaxWidth(Double.MAX_VALUE);
        globalSettingsButton.setMaxWidth(Double.MAX_VALUE);

        newDirectoryButton.setOnAction(event -> showNewDirectoryDialog());
        newGameButton.setOnAction(event -> controller.openDownload(HMCLDemoRoute.DownloadCategory.GAME));
        importButton.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        globalSettingsButton.setOnAction(event ->
                controller.openSettings(HMCLDemoRoute.SettingsSection.GLOBAL_GAME));

        VBox sidebar = HMCLDemoUi.sidebar(
                HMCLDemoUi.sectionLabel(""),
                directoryList,
                newDirectoryButton,
                newGameButton,
                importButton,
                globalSettingsButton
        );
        VBox.setVgrow(directoryList, Priority.ALWAYS);

        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                state.setInstanceSearchQuery(newValue == null ? "" : newValue));
        instanceList.setListStyle(M3ListStyle.STANDARD);
        instanceList.setSelectionMode(M3SelectionMode.SINGLE);
        instanceList.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox center = HMCLDemoUi.fill(new VBox(12.0, searchBar, instanceList));
        center.setPadding(new Insets(16.0, 20.0, 24.0, 20.0));
        VBox.setVgrow(instanceList, Priority.ALWAYS);

        setLeft(sidebar);
        setCenter(center);

        state.getDirectories().addListener((ListChangeListener<HMCLDemoGameDirectory>) change -> rebuildDirectories());
        state.selectedDirectoryProperty().addListener((observable, oldValue, newValue) -> rebuildDirectories());
        state.getFilteredInstances().addListener((ListChangeListener<HMCLDemoInstance>) change -> rebuildInstances());
        state.selectedInstanceProperty().addListener((observable, oldValue, newValue) -> rebuildInstances());

        refreshLocale();
        rebuildDirectories();
        rebuildInstances();
    }

    /// Refreshes locale-dependent labels.
    void refreshLocale() {
        VBox sidebar = (VBox) getLeft();
        sidebar.getChildren().set(0, HMCLDemoUi.sectionLabel(strings.get("instances.section.directories")));
        newDirectoryButton.setText(strings.get("instances.new_directory"));
        newGameButton.setText(strings.get("instances.new_game"));
        importButton.setText(strings.get("instances.import"));
        globalSettingsButton.setText(strings.get("instances.global_settings"));
        searchBar.setPromptText(strings.get("instances.search"));
        rebuildDirectories();
        rebuildInstances();
    }

    private void rebuildDirectories() {
        directoryList.getChildren().clear();
        HMCLDemoGameDirectory selected = state.getSelectedDirectory();
        for (HMCLDemoGameDirectory directory : state.getDirectories()) {
            M3ListItem item = new M3ListItem(directory.name());
            item.setSupportingText(directory.path());
            item.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.FOLDER));
            item.setSelected(directory.equals(selected));
            item.setMaxWidth(Double.MAX_VALUE);
            item.setOnAction(event -> state.selectDirectory(directory.id()));
            directoryList.getChildren().add(item);
        }
    }

    private void rebuildInstances() {
        instanceList.getItems().clear();
        @Nullable HMCLDemoInstance selected = state.getSelectedInstance();
        for (HMCLDemoInstance instance : state.getFilteredInstances()) {
            M3Button manage = new M3Button(strings.get("instances.manage"), M3ButtonVariant.TEXT);
            manage.setOnAction(event ->
                    controller.openInstance(instance.id(), HMCLDemoRoute.InstanceSection.SETTINGS));
            M3Button launch = new M3Button(strings.get("instances.launch"), M3ButtonVariant.TEXT);
            launch.setOnAction(event -> {
                state.selectInstance(instance.id());
                controller.launchSelected();
            });
            HBox trailing = new HBox(4.0, manage, launch);
            trailing.setAlignment(Pos.CENTER_RIGHT);

            M3ListItem row = new M3ListItem(instance.name());
            row.setSupportingText(instance.gameVersion() + " · " + instance.loader());
            row.setLeading(HMCLDemoAssets.imageView(instance.iconPath(), 36.0, 36.0));
            row.setTrailing(trailing);
            row.setSelected(instance.equals(selected));
            row.setMaxWidth(Double.MAX_VALUE);
            row.setOnAction(event -> {
                state.selectInstance(instance.id());
                controller.openInstance(instance.id(), HMCLDemoRoute.InstanceSection.SETTINGS);
            });
            instanceList.getItems().add(row);
        }
    }

    private void showNewDirectoryDialog() {
        M3TextField nameField = new M3TextField(strings.get("instances.directory.default_name"));
        M3TextInputLayout layout = new M3TextInputLayout(nameField);
        layout.setLabelText(strings.get("instances.directory.name"));
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("instances.new_directory"));
        dialog.getDialogPane().setContent(layout);
        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button create = new M3Button(strings.get("common.apply"), M3ButtonVariant.TEXT);
        create.setDefaultButton(true);
        dialog.getDialogPane().getActions().setAll(cancel, create);
        create.setOnAction(event -> {
            HMCLDemoGameDirectory directory = state.addDirectory(
                    nameField.getText().isBlank()
                            ? strings.get("instances.directory.default_name")
                            : nameField.getText().strip()
            );
            controller.showMessageKey("snackbar.directory_added", directory.name());
        });
        controller.overlay().showDialog(dialog);
    }
}
