// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SearchBar;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/// Displays one selected instance with HMCL-style left tabs and management content.
@NotNullByDefault
final class HMCLInstanceDetailView extends BorderPane {
    /// Cycled max-memory values in megabytes.
    private static final int @org.jetbrains.annotations.Unmodifiable [] MEMORY_OPTIONS =
            {2048, 4096, 6144, 8192, 12288};

    /// Cycled window-resolution labels.
    private static final String @org.jetbrains.annotations.Unmodifiable [] RESOLUTION_OPTIONS =
            {"854x480", "1280x720", "1600x900", "1920x1080", "2560x1440"};

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
        SHADERS,

        /// Schematics.
        SCHEMATICS
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

    /// The schematics tab.
    private final M3ListItem schematicsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.CODE, null);

    /// The update-modpack bottom action.
    private final M3ListItem updateItem = HMCLDemoUi.navItem("", HMCLDemoIcons.REFRESH, null);

    /// The test-game bottom action.
    private final M3ListItem testItem = HMCLDemoUi.navItem("", HMCLDemoIcons.PLAY, null);

    /// The open-folder bottom action.
    private final M3ListItem folderItem = HMCLDemoUi.navItem("", HMCLDemoIcons.FOLDER, null);

    /// The manage bottom action.
    private final M3ListItem manageItem = HMCLDemoUi.navItem("", HMCLDemoIcons.MANAGE, null);

    /// The animated center content host.
    private final M3AnimatedContent contentHost = new M3AnimatedContent();

    /// Local mods search query used by the mods section.
    private String modsQuery = "";

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
        contentHost.setFitToWidth(true);
        contentHost.setContentTransform(HMCLDemoTransitions.sectionFade());
        settingsItem.setOnAction(event -> showSection(Section.SETTINGS));
        installersItem.setOnAction(event -> showSection(Section.INSTALLERS));
        modsItem.setOnAction(event -> showSection(Section.MODS));
        resourcePacksItem.setOnAction(event -> showSection(Section.RESOURCE_PACKS));
        worldsItem.setOnAction(event -> showSection(Section.WORLDS));
        shadersItem.setOnAction(event -> showSection(Section.SHADERS));
        schematicsItem.setOnAction(event -> showSection(Section.SCHEMATICS));
        updateItem.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        testItem.setOnAction(event -> controller.launchSelected());
        folderItem.setOnAction(event -> controller.showMessageKey("snackbar.open_folder"));
        manageItem.setOnAction(event -> showManageDialog());

        VBox sidebar = HMCLDemoUi.sidebar(
                settingsItem,
                installersItem,
                modsItem,
                resourcePacksItem,
                worldsItem,
                shadersItem,
                schematicsItem,
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
                renderSection(false);
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
        renderSection(false);
    }

    /// Updates static labels.
    void refreshLocale() {
        settingsItem.setHeadlineText(strings.get("instance.nav.settings"));
        installersItem.setHeadlineText(strings.get("instance.nav.installers"));
        modsItem.setHeadlineText(strings.get("instance.nav.mods"));
        resourcePacksItem.setHeadlineText(strings.get("instance.nav.resource_packs"));
        worldsItem.setHeadlineText(strings.get("instance.nav.worlds"));
        shadersItem.setHeadlineText(strings.get("instance.nav.shaders"));
        schematicsItem.setHeadlineText(strings.get("instance.nav.schematics"));
        updateItem.setHeadlineText(strings.get("instance.action.update"));
        testItem.setHeadlineText(strings.get("instance.action.test"));
        folderItem.setHeadlineText(strings.get("instance.action.folder"));
        manageItem.setHeadlineText(strings.get("instance.action.manage"));
        renderSection(false);
    }

    /// Selects a left-pane section.
    ///
    /// @param next the section to show
    private void showSection(Section next) {
        boolean changed = section != next;
        section = next;
        settingsItem.setSelected(next == Section.SETTINGS);
        installersItem.setSelected(next == Section.INSTALLERS);
        modsItem.setSelected(next == Section.MODS);
        resourcePacksItem.setSelected(next == Section.RESOURCE_PACKS);
        worldsItem.setSelected(next == Section.WORLDS);
        shadersItem.setSelected(next == Section.SHADERS);
        schematicsItem.setSelected(next == Section.SCHEMATICS);
        renderSection(changed);
    }

    /// Rebuilds the center content for the active section and instance.
    ///
    /// @param animate whether to animate the section replacement
    private void renderSection(boolean animate) {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        Node content;
        if (instance == null || (instanceId != null && !instance.id().equals(instanceId))) {
            content = emptyState();
        } else {
            content = switch (section) {
                case SETTINGS -> settingsContent(instance);
                case INSTALLERS -> installersContent(instance);
                case MODS -> modsContent(instance);
                case RESOURCE_PACKS -> resourcePacksContent(instance);
                case WORLDS -> worldsContent(instance);
                case SHADERS -> shadersContent(instance);
                case SCHEMATICS -> schematicsContent(instance);
            };
        }
        if (content instanceof Region region) {
            HMCLDemoUi.fill(region);
        }
        contentHost.setContent(content);
        if (!animate) {
            contentHost.snapToCurrentState();
        }
    }

    /// Creates the version-settings form.
    ///
    /// @param instance the selected instance
    /// @return the settings content
    private Node settingsContent(HMCLDemoInstance instance) {
        M3TextField nameField = new M3TextField(instance.name());
        M3TextInputLayout name = new M3TextInputLayout(nameField);
        name.setLabelText(strings.get("instance.settings.name"));

        M3Button applyName = new M3Button(strings.get("common.apply"), M3ButtonVariant.TONAL);
        applyName.setOnAction(event -> {
            String value = nameField.getText().strip();
            if (value.isEmpty()) {
                return;
            }
            if (state.renameSelectedInstance(value)) {
                controller.showMessageKey("snackbar.instance_renamed", value);
            }
        });
        HBox nameRow = new HBox(8.0, name, applyName);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(name, Priority.ALWAYS);

        M3Text gameVersion = new M3Text(
                strings.format("instance.settings.game_version", instance.gameVersion()),
                M3TextRole.BODY_MEDIUM);
        M3Text loader = new M3Text(
                strings.format("instance.settings.loader", instance.loader()),
                M3TextRole.BODY_MEDIUM);

        M3SwitchSettingItem isolated = new M3SwitchSettingItem(strings.get("instance.settings.isolated"));
        isolated.setSupportingText(strings.get("instance.settings.isolated.support"));
        isolated.setSelected(instance.isolated());
        isolated.selectedProperty().addListener((observable, oldValue, newValue) ->
                applySettings(instance, newValue, instance.maxMemoryMb(), instance.resolution(),
                        instance.fullscreen(), instance.javaId()));

        M3SwitchSettingItem fullscreen = new M3SwitchSettingItem(strings.get("instance.settings.fullscreen"));
        fullscreen.setSelected(instance.fullscreen());
        fullscreen.selectedProperty().addListener((observable, oldValue, newValue) ->
                applySettings(instance, instance.isolated(), instance.maxMemoryMb(), instance.resolution(),
                        newValue, instance.javaId()));

        M3SettingItem java = new M3SettingItem(strings.get("instance.settings.java"));
        java.setSupportingText(javaLabel(instance.javaId()));
        java.setOnAction(event -> cycleJava(instance));

        M3SettingItem memory = new M3SettingItem(strings.get("instance.settings.memory"));
        memory.setSupportingText(memoryLabel(instance.maxMemoryMb()));
        memory.setOnAction(event -> cycleMemory(instance));

        M3SettingItem resolution = new M3SettingItem(strings.get("instance.settings.resolution"));
        resolution.setSupportingText(instance.resolution().replace("x", " x "));
        resolution.setOnAction(event -> cycleResolution(instance));

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getItems().setAll(isolated, fullscreen, java, memory, resolution);

        VBox column = HMCLDemoUi.contentColumn(nameRow, gameVersion, loader, list);
        return HMCLDemoUi.scroll(column);
    }

    /// Creates the installer slot list.
    ///
    /// @param instance the selected instance
    /// @return the installers content
    private Node installersContent(HMCLDemoInstance instance) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");

        for (HMCLDemoInstaller installer : instance.installers()) {
            list.getItems().add(installerRow(installer));
        }

        var listScroll = HMCLDemoUi.listHost(list);
        VBox body = HMCLDemoUi.fill(new VBox(listScroll));
        body.getStyleClass().add("hmcl-list-surface");
        VBox.setVgrow(listScroll, Priority.ALWAYS);
        VBox column = HMCLDemoUi.contentColumn(body);
        VBox.setVgrow(body, Priority.ALWAYS);
        return column;
    }

    /// Creates one installer management row.
    ///
    /// @param installer the installer slot
    /// @return the list item
    private M3ListItem installerRow(HMCLDemoInstaller installer) {
        String support = installer.isInstalled()
                ? installer.installedVersion()
                : strings.get("instance.installers.not_installed");

        M3Button primary = new M3Button(
                installer.isInstalled()
                        ? strings.get("instance.installers.change")
                        : strings.get("instance.installers.install"),
                M3ButtonVariant.TONAL);
        primary.setOnAction(event -> {
            String version = nextInstallerVersion(installer);
            if (state.setInstallerVersion(installer.id(), version)) {
                controller.showMessageKey("snackbar.installer_updated", installer.name(), version);
            }
        });

        HBox trailing = new HBox(6.0, primary);
        trailing.setAlignment(Pos.CENTER_RIGHT);
        if (installer.isInstalled() && !"game".equals(installer.id())) {
            M3Button remove = new M3Button(strings.get("instance.installers.remove"), M3ButtonVariant.TEXT);
            remove.setOnAction(event -> {
                if (state.setInstallerVersion(installer.id(), null)) {
                    controller.showMessageKey("snackbar.installer_removed", installer.name());
                }
            });
            trailing.getChildren().add(0, remove);
        }

        M3ListItem row = new M3ListItem(installer.name());
        row.getStyleClass().add("hmcl-installer-row");
        row.setSupportingText(support);
        row.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.DOWNLOAD));
        row.setTrailing(trailing);
        return row;
    }

    /// Creates the searchable mod list.
    ///
    /// @param instance the selected instance
    /// @return the mods content
    private Node modsContent(HMCLDemoInstance instance) {
        M3SearchBar searchBar = new M3SearchBar();
        searchBar.setPromptText(strings.get("instance.mods.search"));
        searchBar.setText(modsQuery);

        M3Button refresh = createTextAction(strings.get("common.refresh"), () ->
                controller.showMessageKey("snackbar.refreshed"));
        M3Button add = createTextAction(strings.get("instance.mods.add"), () -> {
            @Nullable HMCLDemoMod mod = state.addDemoMod();
            if (mod != null) {
                controller.showMessageKey("snackbar.mod_added", mod.name());
            }
        });
        M3Button checkUpdates = createTextAction(strings.get("instance.mods.check_updates"), () ->
                controller.showMessageKey("snackbar.refreshed"));

        HBox toolbar = HMCLDemoUi.toolbar(
                searchBar,
                HMCLDemoUi.hgrow(),
                refresh,
                add,
                checkUpdates
        );
        HBox.setHgrow(searchBar, Priority.ALWAYS);

        if (instance.mods().isEmpty()) {
            return listSurface(
                    toolbar,
                    placeholderContent(strings.get("instance.nav.mods"), strings.get("instance.mods.empty")));
        }

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");
        M3Text count = new M3Text("", M3TextRole.LABEL_SMALL);

        Runnable refill = () -> {
            String query = modsQuery.strip().toLowerCase(Locale.ROOT);
            list.getItems().clear();
            int visible = 0;
            for (HMCLDemoMod mod : instance.mods()) {
                if (query.isEmpty()
                        || mod.name().toLowerCase(Locale.ROOT).contains(query)
                        || mod.fileName().toLowerCase(Locale.ROOT).contains(query)
                        || mod.version().toLowerCase(Locale.ROOT).contains(query)) {
                    list.getItems().add(modRow(mod));
                    visible++;
                }
            }
            count.setText(strings.format("instance.mods.count", visible));
        };
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            modsQuery = newValue == null ? "" : newValue;
            refill.run();
        });
        refill.run();

        var listScroll = HMCLDemoUi.listHost(list);
        VBox body = HMCLDemoUi.fill(new VBox(8.0, toolbar, count, listScroll));
        body.getStyleClass().add("hmcl-list-surface");
        VBox.setVgrow(listScroll, Priority.ALWAYS);
        VBox column = HMCLDemoUi.contentColumn(body);
        VBox.setVgrow(body, Priority.ALWAYS);
        return column;
    }

    /// Creates one mod row with enable switch and remove action.
    ///
    /// @param mod the mod
    /// @return the list item
    private M3ListItem modRow(HMCLDemoMod mod) {
        M3Switch enabled = new M3Switch();
        enabled.setSelected(mod.enabled());
        enabled.selectedProperty().addListener((observable, oldValue, newValue) ->
                state.setSelectedModEnabled(mod.id(), newValue));

        M3Button remove = new M3Button(strings.get("instance.mods.remove"), M3ButtonVariant.TEXT);
        remove.setOnAction(event -> {
            if (state.removeMod(mod.id())) {
                controller.showMessageKey("snackbar.mod_removed", mod.name());
            }
        });

        HBox trailing = new HBox(8.0, enabled, remove);
        trailing.setAlignment(Pos.CENTER_RIGHT);

        M3ListItem row = new M3ListItem(mod.name());
        row.getStyleClass().add("hmcl-mod-row");
        row.setSupportingText(mod.fileName() + " · " + mod.version());
        row.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.EXTENSION));
        row.setTrailing(trailing);
        return row;
    }

    /// Creates the resource-pack list.
    ///
    /// @param instance the selected instance
    /// @return the resource-packs content
    private Node resourcePacksContent(HMCLDemoInstance instance) {
        M3Button add = createTextAction(strings.get("instance.resource_packs.add"), () -> {
            @Nullable HMCLDemoPack pack = state.addDemoResourcePack();
            if (pack != null) {
                controller.showMessageKey("snackbar.pack_added", pack.name());
            }
        });
        HBox toolbar = HMCLDemoUi.toolbar(
                new M3Text(strings.get("instance.nav.resource_packs"), M3TextRole.TITLE_SMALL),
                HMCLDemoUi.hgrow(),
                add);

        if (instance.resourcePacks().isEmpty()) {
            return listSurface(
                    toolbar,
                    placeholderContent(
                            strings.get("instance.nav.resource_packs"),
                            strings.get("instance.resource_packs.empty")));
        }

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");
        for (HMCLDemoPack pack : instance.resourcePacks()) {
            M3Switch enabled = new M3Switch();
            enabled.setSelected(pack.enabled());
            enabled.selectedProperty().addListener((observable, oldValue, newValue) ->
                    state.setResourcePackEnabled(pack.id(), newValue));

            M3ListItem row = new M3ListItem(pack.name());
            row.setSupportingText(pack.detail());
            row.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.IMAGE));
            row.setTrailing(enabled);
            list.getItems().add(row);
        }
        return listSurface(toolbar, HMCLDemoUi.listHost(list));
    }

    /// Creates the worlds list.
    ///
    /// @param instance the selected instance
    /// @return the worlds content
    private Node worldsContent(HMCLDemoInstance instance) {
        M3Button add = createTextAction(strings.get("instance.worlds.add"), () -> {
            @Nullable HMCLDemoWorld world = state.addDemoWorld();
            if (world != null) {
                controller.showMessageKey("snackbar.world_added", world.name());
            }
        });
        HBox toolbar = HMCLDemoUi.toolbar(
                new M3Text(strings.get("instance.nav.worlds"), M3TextRole.TITLE_SMALL),
                HMCLDemoUi.hgrow(),
                add);

        if (instance.worlds().isEmpty()) {
            return listSurface(
                    toolbar,
                    placeholderContent(strings.get("instance.nav.worlds"), strings.get("instance.worlds.empty")));
        }

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");
        for (HMCLDemoWorld world : instance.worlds()) {
            M3Button remove = new M3Button(strings.get("instance.worlds.remove"), M3ButtonVariant.TEXT);
            remove.setOnAction(event -> {
                if (state.removeWorld(world.id())) {
                    controller.showMessageKey("snackbar.world_removed", world.name());
                }
            });

            M3ListItem row = new M3ListItem(world.name());
            row.setSupportingText(world.gameMode() + " · " + world.lastPlayed());
            row.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.WORLD));
            row.setTrailing(remove);
            list.getItems().add(row);
        }
        return listSurface(toolbar, HMCLDemoUi.listHost(list));
    }

    /// Creates the shader-pack list.
    ///
    /// @param instance the selected instance
    /// @return the shaders content
    private Node shadersContent(HMCLDemoInstance instance) {
        M3Button add = createTextAction(strings.get("instance.shaders.add"), () -> {
            @Nullable HMCLDemoPack pack = state.addDemoShader();
            if (pack != null) {
                controller.showMessageKey("snackbar.pack_added", pack.name());
            }
        });
        HBox toolbar = HMCLDemoUi.toolbar(
                new M3Text(strings.get("instance.nav.shaders"), M3TextRole.TITLE_SMALL),
                HMCLDemoUi.hgrow(),
                add);

        if (instance.shaderPacks().isEmpty()) {
            return listSurface(
                    toolbar,
                    placeholderContent(strings.get("instance.nav.shaders"), strings.get("instance.shaders.empty")));
        }

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");
        for (HMCLDemoPack pack : instance.shaderPacks()) {
            M3Switch enabled = new M3Switch();
            enabled.setSelected(pack.enabled());
            enabled.selectedProperty().addListener((observable, oldValue, newValue) ->
                    state.setShaderEnabled(pack.id(), newValue));

            M3ListItem row = new M3ListItem(pack.name());
            row.setSupportingText(pack.detail());
            row.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.IMAGE));
            row.setTrailing(enabled);
            list.getItems().add(row);
        }
        return listSurface(toolbar, HMCLDemoUi.listHost(list));
    }

    /// Creates the schematics list.
    ///
    /// @param instance the selected instance
    /// @return the schematics content
    private Node schematicsContent(HMCLDemoInstance instance) {
        M3Button add = createTextAction(strings.get("instance.schematics.add"), () -> {
            @Nullable HMCLDemoPack pack = state.addDemoSchematic();
            if (pack != null) {
                controller.showMessageKey("snackbar.pack_added", pack.name());
            }
        });
        HBox toolbar = HMCLDemoUi.toolbar(
                new M3Text(strings.get("instance.nav.schematics"), M3TextRole.TITLE_SMALL),
                HMCLDemoUi.hgrow(),
                add);

        if (instance.schematics().isEmpty()) {
            return listSurface(
                    toolbar,
                    placeholderContent(
                            strings.get("instance.nav.schematics"),
                            strings.get("instance.schematics.empty")));
        }

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");
        for (HMCLDemoPack pack : instance.schematics()) {
            M3ListItem row = new M3ListItem(pack.name());
            row.setSupportingText(pack.detail());
            row.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.CODE));
            list.getItems().add(row);
        }
        return listSurface(toolbar, HMCLDemoUi.listHost(list));
    }

    /// Shows the manage dialog with rename, copy, and delete actions.
    private void showManageDialog() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        if (instance == null) {
            return;
        }

        M3Button rename = new M3Button(strings.get("instance.manage.rename"), M3ButtonVariant.TEXT);
        M3Button copy = new M3Button(strings.get("instance.manage.copy"), M3ButtonVariant.TEXT);
        M3Button delete = new M3Button(strings.get("instance.manage.delete"), M3ButtonVariant.TEXT);
        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);

        M3Text body = new M3Text(strings.format("instance.manage.body", instance.name()), M3TextRole.BODY_MEDIUM);
        body.setWrapText(true);
        VBox content = new VBox(12.0, body);
        content.setPadding(new Insets(4.0, 0.0, 0.0, 0.0));
        content.setPrefWidth(360.0);

        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("instance.action.manage"));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getActions().setAll(cancel, rename, copy, delete);
        dialog.setOnHidden(event -> {
            if (event.getAction() == rename) {
                showRenameDialog(instance.name());
            } else if (event.getAction() == copy) {
                @Nullable HMCLDemoInstance copied = state.copySelectedInstance();
                if (copied != null) {
                    instanceId = copied.id();
                    controller.showMessageKey("snackbar.instance_copied", copied.name());
                    renderSection(false);
                }
            } else if (event.getAction() == delete) {
                String name = instance.name();
                if (state.deleteSelectedInstance()) {
                    controller.showMessageKey("snackbar.instance_deleted", name);
                    controller.goBack();
                }
            }
        });
        controller.overlay().showDialog(dialog);
    }

    /// Shows a rename dialog for the selected instance.
    ///
    /// @param currentName the current display name
    private void showRenameDialog(String currentName) {
        M3TextField field = new M3TextField(currentName);
        M3TextInputLayout layout = new M3TextInputLayout(field);
        layout.setLabelText(strings.get("instance.settings.name"));

        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button apply = new M3Button(strings.get("common.apply"), M3ButtonVariant.TEXT);
        apply.setDefaultButton(true);

        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("instance.manage.rename"));
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getActions().setAll(cancel, apply);
        dialog.setOnHidden(event -> {
            if (event.getAction() != apply) {
                return;
            }
            String value = field.getText().strip();
            if (value.isEmpty()) {
                return;
            }
            if (state.renameSelectedInstance(value)) {
                controller.showMessageKey("snackbar.instance_renamed", value);
            }
        });
        controller.overlay().showDialog(dialog);
    }

    /// Applies settings fields through the shared state.
    ///
    /// @param baseline the instance snapshot used only for identity checks
    /// @param isolated whether the instance uses an isolated working directory
    /// @param maxMemoryMb configured max memory
    /// @param resolution window resolution label
    /// @param fullscreen whether fullscreen is preferred
    /// @param javaId selected Java runtime id, or `auto`
    private void applySettings(
            HMCLDemoInstance baseline,
            boolean isolated,
            int maxMemoryMb,
            String resolution,
            boolean fullscreen,
            String javaId
    ) {
        @Nullable HMCLDemoInstance current = state.getSelectedInstance();
        if (current == null || !current.id().equals(baseline.id())) {
            return;
        }
        state.updateSelectedInstanceSettings(isolated, maxMemoryMb, resolution, fullscreen, javaId);
    }

    /// Cycles the selected Java runtime id.
    ///
    /// @param instance the selected instance
    private void cycleJava(HMCLDemoInstance instance) {
        List<String> options = new ArrayList<>();
        options.add("auto");
        for (HMCLDemoJavaRuntime runtime : state.getJavaRuntimes()) {
            options.add(runtime.id());
        }
        int index = options.indexOf(instance.javaId());
        String next = options.get((Math.max(index, 0) + 1) % options.size());
        applySettings(instance, instance.isolated(), instance.maxMemoryMb(), instance.resolution(),
                instance.fullscreen(), next);
    }

    /// Cycles the selected max-memory value.
    ///
    /// @param instance the selected instance
    private void cycleMemory(HMCLDemoInstance instance) {
        int index = indexOf(MEMORY_OPTIONS, instance.maxMemoryMb());
        int next = MEMORY_OPTIONS[(index + 1) % MEMORY_OPTIONS.length];
        applySettings(instance, instance.isolated(), next, instance.resolution(),
                instance.fullscreen(), instance.javaId());
    }

    /// Cycles the selected resolution label.
    ///
    /// @param instance the selected instance
    private void cycleResolution(HMCLDemoInstance instance) {
        int index = indexOf(RESOLUTION_OPTIONS, instance.resolution());
        String next = RESOLUTION_OPTIONS[(index + 1) % RESOLUTION_OPTIONS.length];
        applySettings(instance, instance.isolated(), instance.maxMemoryMb(), next,
                instance.fullscreen(), instance.javaId());
    }

    /// Returns a deterministic next installer version label.
    ///
    /// @param installer the installer slot
    /// @return the next version string
    private static String nextInstallerVersion(HMCLDemoInstaller installer) {
        if (!installer.isInstalled()) {
            return "latest";
        }
        String current = installer.installedVersion();
        if ("latest".equals(current)) {
            return "stable";
        }
        if ("stable".equals(current)) {
            return "1.0.0";
        }
        return "latest";
    }

    /// Resolves the Java supporting label for a runtime id.
    ///
    /// @param javaId the runtime id, or `auto`
    /// @return the display label
    private String javaLabel(String javaId) {
        if ("auto".equals(javaId)) {
            return strings.get("instance.settings.java.support");
        }
        for (HMCLDemoJavaRuntime runtime : state.getJavaRuntimes()) {
            if (runtime.id().equals(javaId)) {
                return runtime.name() + " · " + runtime.version();
            }
        }
        return javaId;
    }

    /// Formats a memory supporting label.
    ///
    /// @param maxMemoryMb the memory limit
    /// @return the display label
    private static String memoryLabel(int maxMemoryMb) {
        return maxMemoryMb + " MB";
    }

    /// Returns the index of `value` in `options`, or `0` when absent.
    ///
    /// @param options the option array
    /// @param value the searched value
    /// @return the matching index
    private static int indexOf(int[] options, int value) {
        for (int i = 0; i < options.length; i++) {
            if (options[i] == value) {
                return i;
            }
        }
        return 0;
    }

    /// Returns the index of `value` in `options`, or `0` when absent.
    ///
    /// @param options the option array
    /// @param value the searched value
    /// @return the matching index
    private static int indexOf(String[] options, String value) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    /// Creates a list surface with a fixed toolbar and growing body.
    ///
    /// @param toolbar the toolbar row
    /// @param bodyContent the body node
    /// @return the page content
    private Node listSurface(HBox toolbar, Node bodyContent) {
        VBox body = HMCLDemoUi.fill(new VBox(toolbar, bodyContent));
        body.getStyleClass().add("hmcl-list-surface");
        VBox.setVgrow(bodyContent, Priority.ALWAYS);
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
        StackPane host = HMCLDemoUi.fill(new StackPane(box));
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
