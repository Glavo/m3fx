// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Displays one selected instance with HMCL-style left tabs and management content.
@NotNullByDefault
final class HMCLInstanceDetailView extends BorderPane {
    /// Left-pane management sections.
    private enum Section {
        /// Version settings.
        SETTINGS,

        /// Auto installers.
        INSTALLERS,

        /// Mods.
        MODS,

        /// Resource packs.
        RESOURCE_PACKS,

        /// Worlds.
        WORLDS,

        /// Shader packs.
        SHADERS
    }

    /// The localization source.
    private final HMCLDemoStrings strings;

    /// The shared state.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// The settings tab.
    private final M3ListItem settingsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.SETTINGS, null);

    /// The installers tab.
    private final M3ListItem installersItem = HMCLDemoUi.navItem("", HMCLDemoIcons.DOWNLOAD, null);

    /// The mods tab.
    private final M3ListItem modsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.EXTENSION, null);

    /// The resource-packs tab.
    private final M3ListItem resourcePacksItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);

    /// The worlds tab.
    private final M3ListItem worldsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.WORLD, null);

    /// The shaders tab.
    private final M3ListItem shadersItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);

    /// The update-modpack bottom action.
    private final M3ListItem updateItem = HMCLDemoUi.navItem("", HMCLDemoIcons.REFRESH, null);

    /// The test-game bottom action.
    private final M3ListItem testItem = HMCLDemoUi.navItem("", HMCLDemoIcons.PLAY, null);

    /// The open-folder bottom action.
    private final M3ListItem folderItem = HMCLDemoUi.navItem("", HMCLDemoIcons.FOLDER, null);

    /// The manage bottom action.
    private final M3ListItem manageItem = HMCLDemoUi.navItem("", HMCLDemoIcons.MANAGE, null);

    /// The center content host.
    private final StackPane contentHost = new StackPane();

    /// The currently displayed section.
    private Section section = Section.SETTINGS;

    /// The currently bound instance identifier.
    private @Nullable String instanceId;

    /// Creates the instance detail page.
    ///
    /// @param strings the localization source
    /// @param state the shared state
    /// @param controller the application controller
    HMCLInstanceDetailView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoController controller) {
        this.strings = strings;
        this.state = state;
        this.controller = controller;

        getStyleClass().add("hmcl-secondary-page");
        HMCLDemoUi.fill(this);
        HMCLDemoUi.fill(contentHost);
        settingsItem.setOnAction(event -> showSection(Section.SETTINGS));
        installersItem.setOnAction(event -> showSection(Section.INSTALLERS));
        modsItem.setOnAction(event -> showSection(Section.MODS));
        resourcePacksItem.setOnAction(event -> showSection(Section.RESOURCE_PACKS));
        worldsItem.setOnAction(event -> showSection(Section.WORLDS));
        shadersItem.setOnAction(event -> showSection(Section.SHADERS));
        updateItem.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        testItem.setOnAction(event -> controller.launchSelected());
        folderItem.setOnAction(event -> controller.showMessageKey("snackbar.open_folder"));
        manageItem.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));

        VBox sidebar = HMCLDemoUi.sidebar(
                settingsItem,
                installersItem,
                modsItem,
                resourcePacksItem,
                worldsItem,
                shadersItem,
                HMCLDemoUi.vgrow(),
                updateItem,
                testItem,
                folderItem,
                manageItem
        );
        setLeft(sidebar);
        setCenter(contentHost);

        state.selectedInstanceProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.id().equals(instanceId)) {
                renderSection();
            }
        });
        refreshLocale();
        showSection(Section.SETTINGS);
    }

    /// Binds the page to the requested instance and refreshes content.
    ///
    /// @param id the instance identifier
    void showInstance(String id) {
        instanceId = id;
        state.selectInstance(id);
        renderSection();
    }

    /// Updates static labels.
    void refreshLocale() {
        settingsItem.setHeadlineText(strings.get("instance.nav.settings"));
        installersItem.setHeadlineText(strings.get("instance.nav.installers"));
        modsItem.setHeadlineText(strings.get("instance.nav.mods"));
        resourcePacksItem.setHeadlineText(strings.get("instance.nav.resource_packs"));
        worldsItem.setHeadlineText(strings.get("instance.nav.worlds"));
        shadersItem.setHeadlineText(strings.get("instance.nav.shaders"));
        updateItem.setHeadlineText(strings.get("instance.action.update"));
        testItem.setHeadlineText(strings.get("instance.action.test"));
        folderItem.setHeadlineText(strings.get("instance.action.folder"));
        manageItem.setHeadlineText(strings.get("instance.action.manage"));
        renderSection();
    }

    /// Selects a left-pane section.
    ///
    /// @param next the section to show
    private void showSection(Section next) {
        section = next;
        settingsItem.setSelected(next == Section.SETTINGS);
        installersItem.setSelected(next == Section.INSTALLERS);
        modsItem.setSelected(next == Section.MODS);
        resourcePacksItem.setSelected(next == Section.RESOURCE_PACKS);
        worldsItem.setSelected(next == Section.WORLDS);
        shadersItem.setSelected(next == Section.SHADERS);
        renderSection();
    }

    /// Rebuilds the center content for the active section and instance.
    private void renderSection() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        if (instance == null) {
            contentHost.getChildren().setAll(emptyState());
            return;
        }
        Node content = switch (section) {
            case SETTINGS -> settingsContent(instance);
            case INSTALLERS -> placeholderContent(
                    strings.get("instance.nav.installers"),
                    strings.get("instance.installers.body"));
            case MODS -> modsContent(instance);
            case RESOURCE_PACKS -> placeholderContent(
                    strings.get("instance.nav.resource_packs"),
                    strings.get("instance.resource_packs.body"));
            case WORLDS -> placeholderContent(
                    strings.get("instance.nav.worlds"),
                    strings.get("instance.worlds.body"));
            case SHADERS -> placeholderContent(
                    strings.get("instance.nav.shaders"),
                    strings.get("instance.shaders.body"));
        };
        contentHost.getChildren().setAll(content);
    }

    /// Creates the version-settings form.
    ///
    /// @param instance the selected instance
    /// @return the settings content
    private Node settingsContent(HMCLDemoInstance instance) {
        M3TextInputLayout name = new M3TextInputLayout(new M3TextField(instance.name()));
        name.setLabelText(strings.get("instance.settings.name"));

        M3Text gameVersion = new M3Text(
                strings.format("instance.settings.game_version", instance.gameVersion()),
                M3TextRole.BODY_MEDIUM);
        M3Text loader = new M3Text(
                strings.format("instance.settings.loader", instance.loader()),
                M3TextRole.BODY_MEDIUM);

        M3SwitchSettingItem autoJoin = new M3SwitchSettingItem(strings.get("instance.settings.auto_join"));
        autoJoin.setSupportingText(strings.get("instance.settings.auto_join.support"));
        M3SwitchSettingItem fullscreen = new M3SwitchSettingItem(strings.get("instance.settings.fullscreen"));
        M3SettingItem java = new M3SettingItem(strings.get("instance.settings.java"));
        java.setSupportingText(strings.get("instance.settings.java.support"));
        java.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        M3SettingItem memory = new M3SettingItem(strings.get("instance.settings.memory"));
        memory.setSupportingText(strings.get("instance.settings.memory.support"));
        memory.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        M3SettingItem resolution = new M3SettingItem(strings.get("instance.settings.resolution"));
        resolution.setSupportingText(strings.get("instance.settings.resolution.support"));
        resolution.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        M3SettingItem launcherVisibility = new M3SettingItem(strings.get("instance.settings.launcher_visibility"));
        launcherVisibility.setSupportingText(strings.get("instance.settings.launcher_visibility.support"));
        launcherVisibility.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getItems().setAll(autoJoin, fullscreen, java, memory, resolution, launcherVisibility);

        VBox column = HMCLDemoUi.contentColumn(
                name,
                gameVersion,
                loader,
                list
        );
        return HMCLDemoUi.scroll(column);
    }

    /// Creates the mod list for the selected instance.
    ///
    /// @param instance the selected instance
    /// @return the mods content
    private Node modsContent(HMCLDemoInstance instance) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");

        if (instance.mods().isEmpty()) {
            return placeholderContent(strings.get("instance.nav.mods"), strings.get("instance.mods.empty"));
        }

        for (HMCLDemoMod mod : instance.mods()) {
            M3Switch enabled = new M3Switch();
            enabled.setSelected(mod.enabled());
            enabled.selectedProperty().addListener((observable, oldValue, newValue) ->
                    state.setSelectedModEnabled(mod.id(), newValue));

            M3ListItem row = new M3ListItem(mod.name());
            row.getStyleClass().add("hmcl-mod-row");
            row.setSupportingText(mod.fileName());
            row.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.EXTENSION));
            row.setTrailing(enabled);
            list.getItems().add(row);
        }

        HBox toolbar = HMCLDemoUi.toolbar(
                new M3Text(strings.format("instance.mods.count", instance.mods().size()), M3TextRole.TITLE_SMALL),
                HMCLDemoUi.hgrow(),
                createTextAction(strings.get("instance.mods.add"), () ->
                        controller.showMessageKey("snackbar.action_simulated")),
                createTextAction(strings.get("instance.mods.check_updates"), () ->
                        controller.showMessageKey("snackbar.refreshed"))
        );
        var listScroll = HMCLDemoUi.listHost(list);
        VBox body = HMCLDemoUi.fill(new VBox(toolbar, listScroll));
        body.getStyleClass().add("hmcl-list-surface");
        VBox.setVgrow(listScroll, Priority.ALWAYS);
        VBox column = HMCLDemoUi.contentColumn(body);
        VBox.setVgrow(body, Priority.ALWAYS);
        return column;
    }

    /// Creates a centered empty or placeholder panel.
    ///
    /// @param title the title
    /// @param body the supporting body
    /// @return the placeholder node
    private Node placeholderContent(String title, String body) {
        M3Text titleText = new M3Text(title, M3TextRole.TITLE_LARGE);
        M3Text bodyText = new M3Text(body, M3TextRole.BODY_MEDIUM);
        bodyText.setWrapText(true);
        VBox box = new VBox(12.0, titleText, bodyText);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(24.0));
        box.setMaxWidth(520.0);
        StackPane host = new StackPane(box);
        host.getStyleClass().add("hmcl-page-body");
        StackPane.setAlignment(box, Pos.TOP_LEFT);
        return host;
    }

    /// Creates a simple empty-state message when no instance is selected.
    ///
    /// @return the empty-state node
    private Node emptyState() {
        return placeholderContent(strings.get("instance.title"), strings.get("instance.empty"));
    }

    /// Creates a compact text action button.
    ///
    /// @param text the button text
    /// @param action the activation handler
    /// @return the button
    private M3Button createTextAction(String text, Runnable action) {
        M3Button button = new M3Button(text, M3ButtonVariant.TEXT);
        button.setOnAction(event -> action.run());
        return button;
    }
}
