// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Instance management secondary route with animated sections.
@NotNullByDefault
final class HMCLInstanceDetailView extends BorderPane {
    private static final int @org.jetbrains.annotations.Unmodifiable [] MEMORY_OPTIONS =
            {2048, 4096, 6144, 8192, 12288};
    private static final String @org.jetbrains.annotations.Unmodifiable [] RESOLUTION_OPTIONS =
            {"854x480", "1280x720", "1600x900", "1920x1080", "2560x1440"};

    private final HMCLDemoController controller;
    private final HMCLDemoStrings strings;
    private final HMCLDemoState state;

    private final M3ListItem settingsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.SETTINGS, null);
    private final M3ListItem installersItem = HMCLDemoUi.navItem("", HMCLDemoIcons.DOWNLOAD, null);
    private final M3ListItem modsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.EXTENSION, null);
    private final M3ListItem resourcePacksItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);
    private final M3ListItem worldsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.WORLD, null);
    private final M3ListItem shadersItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);
    private final M3ListItem schematicsItem = HMCLDemoUi.navItem("", HMCLDemoIcons.FOLDER, null);

    private final M3Button updateButton = new M3Button();
    private final M3Button testButton = new M3Button();
    private final M3Button folderButton = new M3Button();
    private final M3Button manageButton = new M3Button();

    private final M3AnimatedContent centerHost = new M3AnimatedContent();
    private @Nullable String instanceId;
    private HMCLDemoRoute.InstanceSection section = HMCLDemoRoute.InstanceSection.SETTINGS;

    /// Creates the instance detail page.
    ///
    /// @param controller the application controller
    HMCLInstanceDetailView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().addAll("hmcl-instance-detail-page", "hmcl-secondary-page");
        HMCLDemoUi.fill(this);

        settingsItem.setOnAction(event -> showSection(HMCLDemoRoute.InstanceSection.SETTINGS));
        installersItem.setOnAction(event -> showSection(HMCLDemoRoute.InstanceSection.INSTALLERS));
        modsItem.setOnAction(event -> showSection(HMCLDemoRoute.InstanceSection.MODS));
        resourcePacksItem.setOnAction(event -> showSection(HMCLDemoRoute.InstanceSection.RESOURCE_PACKS));
        worldsItem.setOnAction(event -> showSection(HMCLDemoRoute.InstanceSection.WORLDS));
        shadersItem.setOnAction(event -> showSection(HMCLDemoRoute.InstanceSection.SHADERS));
        schematicsItem.setOnAction(event -> showSection(HMCLDemoRoute.InstanceSection.SCHEMATICS));

        updateButton.setVariant(M3ButtonVariant.TEXT);
        testButton.setVariant(M3ButtonVariant.TEXT);
        folderButton.setVariant(M3ButtonVariant.TEXT);
        manageButton.setVariant(M3ButtonVariant.TONAL);
        updateButton.setMaxWidth(Double.MAX_VALUE);
        testButton.setMaxWidth(Double.MAX_VALUE);
        folderButton.setMaxWidth(Double.MAX_VALUE);
        manageButton.setMaxWidth(Double.MAX_VALUE);
        updateButton.setOnAction(event -> runUpdateTask());
        testButton.setOnAction(event -> controller.launchSelected());
        folderButton.setOnAction(event -> controller.showMessageKey("snackbar.open_folder"));
        manageButton.setOnAction(event -> showManageDialog());

        VBox sidebar = HMCLDemoUi.sidebar(
                settingsItem,
                installersItem,
                modsItem,
                resourcePacksItem,
                worldsItem,
                shadersItem,
                schematicsItem,
                updateButton,
                testButton,
                folderButton,
                manageButton
        );
        HMCLDemoUi.fill(centerHost);
        centerHost.setFitToWidth(true);
        centerHost.setFitToHeight(true);
        setLeft(sidebar);
        setCenter(centerHost);

        state.selectedInstanceProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.id().equals(instanceId)) {
                renderSection(false);
            }
        });
        refreshLocale();
        renderSection(false);
    }

    /// Shows management UI for the given instance and section.
    ///
    /// @param id the instance id
    /// @param next the section to show
    void showInstance(String id, HMCLDemoRoute.InstanceSection next) {
        boolean sectionChanged = instanceId != null && instanceId.equals(id) && section != next;
        instanceId = id;
        section = next;
        renderSection(sectionChanged);
        syncNavSelection();
    }

    /// Refreshes locale-dependent labels.
    void refreshLocale() {
        settingsItem.setHeadlineText(strings.get("instance.nav.settings"));
        installersItem.setHeadlineText(strings.get("instance.nav.installers"));
        modsItem.setHeadlineText(strings.get("instance.nav.mods"));
        resourcePacksItem.setHeadlineText(strings.get("instance.nav.resource_packs"));
        worldsItem.setHeadlineText(strings.get("instance.nav.worlds"));
        shadersItem.setHeadlineText(strings.get("instance.nav.shaders"));
        schematicsItem.setHeadlineText(strings.get("instance.nav.schematics"));
        updateButton.setText(strings.get("instance.action.update"));
        testButton.setText(strings.get("instance.action.test"));
        folderButton.setText(strings.get("instance.action.folder"));
        manageButton.setText(strings.get("instance.action.manage"));
        renderSection(false);
    }

    private void showSection(HMCLDemoRoute.InstanceSection next) {
        if (instanceId == null) {
            return;
        }
        controller.openInstance(instanceId, next);
    }

    private void syncNavSelection() {
        settingsItem.setSelected(section == HMCLDemoRoute.InstanceSection.SETTINGS);
        installersItem.setSelected(section == HMCLDemoRoute.InstanceSection.INSTALLERS);
        modsItem.setSelected(section == HMCLDemoRoute.InstanceSection.MODS);
        resourcePacksItem.setSelected(section == HMCLDemoRoute.InstanceSection.RESOURCE_PACKS);
        worldsItem.setSelected(section == HMCLDemoRoute.InstanceSection.WORLDS);
        shadersItem.setSelected(section == HMCLDemoRoute.InstanceSection.SHADERS);
        schematicsItem.setSelected(section == HMCLDemoRoute.InstanceSection.SCHEMATICS);
    }

    private void renderSection(boolean animate) {
        syncNavSelection();
        @Nullable HMCLDemoInstance instance = currentInstance();
        centerHost.setContentTransform(animate && !state.isAnimationDisabled()
                ? HMCLDemoTransitions.sectionUp()
                : HMCLDemoTransitions.none());
        if (instance == null) {
            centerHost.setContent(padded(HMCLDemoUi.emptyState(strings.get("instance.empty"))));
            return;
        }
        Node content = switch (section) {
            case SETTINGS -> settingsContent(instance);
            case INSTALLERS -> installersContent(instance);
            case MODS -> modsContent(instance);
            case RESOURCE_PACKS -> packsContent(instance.resourcePacks(), true);
            case SHADERS -> packsContent(instance.shaderPacks(), false);
            case WORLDS -> worldsContent(instance);
            case SCHEMATICS -> schematicsContent(instance);
        };
        centerHost.setContent(content);
        if (!animate || state.isAnimationDisabled()) {
            centerHost.snapToCurrentState();
        }
    }

    private Node settingsContent(HMCLDemoInstance instance) {
        M3SettingItem name = HMCLDemoUi.settingItem(strings.get("instance.settings.name"), instance.name());
        name.setOnAction(event -> showRenameDialog(instance));

        M3SettingItem gameVersion = HMCLDemoUi.settingItem(
                strings.get("instance.settings.game_version_label"),
                instance.gameVersion()
        );
        M3SettingItem loader = HMCLDemoUi.settingItem(
                strings.get("instance.settings.loader_label"),
                instance.loader()
        );

        VBox identity = new VBox(
                8.0,
                heading(strings.get("settings.game.section.basic")),
                name,
                gameVersion,
                loader
        );
        identity.setPadding(new Insets(0.0, 0.0, 8.0, 0.0));

        ScrollPane form = HMCLGameSettingsForm.create(
                controller,
                () -> state.getInstanceGameSettings(instance.id()),
                value -> state.setInstanceGameSettings(instance.id(), value),
                true
        );
        VBox host = new VBox(identity, form);
        VBox.setVgrow(form, Priority.ALWAYS);
        HMCLDemoUi.fill(host);
        return host;
    }

    private Node installersContent(HMCLDemoInstance instance) {
        VBox list = new VBox(8.0);
        for (HMCLDemoInstaller installer : instance.installers()) {
            M3ListItem row = new M3ListItem(installer.name());
            row.setSupportingText(installer.installedVersion() == null
                    ? strings.get("instance.installers.not_installed")
                    : installer.installedVersion());
            row.setMaxWidth(Double.MAX_VALUE);
            if (!"game".equals(installer.id())) {
                M3Button action = new M3Button(
                        installer.installedVersion() == null
                                ? strings.get("instance.installers.install")
                                : strings.get("instance.installers.remove"),
                        M3ButtonVariant.TEXT
                );
                action.setOnAction(event -> {
                    if (installer.installedVersion() == null) {
                        state.setInstallerVersion(installer.id(), "latest");
                        controller.showMessageKey("snackbar.installer_updated", installer.name(), "latest");
                    } else {
                        state.setInstallerVersion(installer.id(), null);
                        controller.showMessageKey("snackbar.installer_removed", installer.name());
                    }
                    renderSection(false);
                });
                row.setTrailing(action);
            }
            list.getChildren().add(row);
        }
        return HMCLDemoUi.scroll(HMCLDemoUi.pageColumn(
                new M3Text(strings.get("instance.installers.body"), M3TextRole.BODY_MEDIUM),
                list
        ));
    }

    private Node modsContent(HMCLDemoInstance instance) {
        M3SearchBar search = new M3SearchBar(strings.get("instance.mods.search"));
        VBox list = new VBox(4.0);
        Runnable rebuild = () -> {
            list.getChildren().clear();
            String query = search.getText().strip().toLowerCase();
            for (HMCLDemoMod mod : instance.mods()) {
                if (!query.isEmpty()
                        && !mod.name().toLowerCase().contains(query)
                        && !mod.fileName().toLowerCase().contains(query)) {
                    continue;
                }
                M3Switch enabled = new M3Switch();
                enabled.setSelected(mod.enabled());
                enabled.selectedProperty().addListener((observable, oldValue, newValue) ->
                        state.setSelectedModEnabled(mod.id(), Boolean.TRUE.equals(newValue)));
                M3Button remove = new M3Button(strings.get("instance.mods.remove"), M3ButtonVariant.TEXT);
                remove.setOnAction(event -> {
                    state.removeMod(mod.id());
                    controller.showMessageKey("snackbar.mod_removed", mod.name());
                    renderSection(false);
                });
                HBox trailing = new HBox(4.0, remove, enabled);
                trailing.setAlignment(Pos.CENTER_RIGHT);
                M3ListItem row = new M3ListItem(mod.name());
                row.setSupportingText(mod.version());
                row.setTrailing(trailing);
                row.setMaxWidth(Double.MAX_VALUE);
                list.getChildren().add(row);
            }
            if (list.getChildren().isEmpty()) {
                list.getChildren().add(HMCLDemoUi.emptyState(strings.get("instance.mods.empty")));
            }
        };
        search.textProperty().addListener((observable, oldValue, newValue) -> rebuild.run());
        M3Button add = new M3Button(strings.get("instance.mods.add"), M3ButtonVariant.TONAL);
        add.setOnAction(event -> {
            @Nullable HMCLDemoMod mod = state.addDemoMod();
            if (mod != null) {
                controller.showMessageKey("snackbar.mod_added", mod.name());
                renderSection(false);
            }
        });
        rebuild.run();
        return HMCLDemoUi.scroll(HMCLDemoUi.pageColumn(
                HMCLDemoUi.toolbar(search, add),
                new M3Text(strings.format("instance.mods.count", instance.mods().size()), M3TextRole.BODY_SMALL),
                list
        ));
    }

    private Node packsContent(List<HMCLDemoPack> packs, boolean resourcePacks) {
        VBox list = new VBox(4.0);
        if (packs.isEmpty()) {
            list.getChildren().add(HMCLDemoUi.emptyState(resourcePacks
                    ? strings.get("instance.resource_packs.empty")
                    : strings.get("instance.shaders.empty")));
        } else {
            for (HMCLDemoPack pack : packs) {
                M3Switch enabled = new M3Switch();
                enabled.setSelected(pack.enabled());
                enabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (resourcePacks) {
                        state.setResourcePackEnabled(pack.id(), Boolean.TRUE.equals(newValue));
                    } else {
                        state.setShaderEnabled(pack.id(), Boolean.TRUE.equals(newValue));
                    }
                });
                M3ListItem row = new M3ListItem(pack.name());
                row.setSupportingText(pack.detail());
                row.setTrailing(enabled);
                row.setMaxWidth(Double.MAX_VALUE);
                list.getChildren().add(row);
            }
        }
        M3Button add = new M3Button(
                resourcePacks ? strings.get("instance.resource_packs.add") : strings.get("instance.shaders.add"),
                M3ButtonVariant.TONAL
        );
        add.setOnAction(event -> {
            @Nullable HMCLDemoPack pack = resourcePacks ? state.addDemoResourcePack() : state.addDemoShader();
            if (pack != null) {
                controller.showMessageKey("snackbar.pack_added", pack.name());
                renderSection(false);
            }
        });
        return HMCLDemoUi.scroll(HMCLDemoUi.pageColumn(add, list));
    }

    private Node worldsContent(HMCLDemoInstance instance) {
        VBox list = new VBox(4.0);
        if (instance.worlds().isEmpty()) {
            list.getChildren().add(HMCLDemoUi.emptyState(strings.get("instance.worlds.empty")));
        } else {
            for (HMCLDemoWorld world : instance.worlds()) {
                M3Button remove = new M3Button(strings.get("instance.worlds.remove"), M3ButtonVariant.TEXT);
                remove.setOnAction(event -> {
                    state.removeWorld(world.id());
                    controller.showMessageKey("snackbar.world_removed", world.name());
                    renderSection(false);
                });
                M3ListItem row = new M3ListItem(world.name());
                row.setSupportingText(world.gameMode() + " · " + world.lastPlayed());
                row.setTrailing(remove);
                row.setMaxWidth(Double.MAX_VALUE);
                list.getChildren().add(row);
            }
        }
        M3Button add = new M3Button(strings.get("instance.worlds.add"), M3ButtonVariant.TONAL);
        add.setOnAction(event -> {
            @Nullable HMCLDemoWorld world = state.addDemoWorld();
            if (world != null) {
                controller.showMessageKey("snackbar.world_added", world.name());
                renderSection(false);
            }
        });
        return HMCLDemoUi.scroll(HMCLDemoUi.pageColumn(add, list));
    }

    private Node schematicsContent(HMCLDemoInstance instance) {
        VBox list = new VBox(4.0);
        if (instance.schematics().isEmpty()) {
            list.getChildren().add(HMCLDemoUi.emptyState(strings.get("instance.schematics.empty")));
        } else {
            for (HMCLDemoPack pack : instance.schematics()) {
                M3ListItem row = new M3ListItem(pack.name());
                row.setSupportingText(pack.detail());
                row.setMaxWidth(Double.MAX_VALUE);
                list.getChildren().add(row);
            }
        }
        M3Button add = new M3Button(strings.get("instance.schematics.add"), M3ButtonVariant.TONAL);
        add.setOnAction(event -> {
            @Nullable HMCLDemoPack pack = state.addDemoSchematic();
            if (pack != null) {
                controller.showMessageKey("snackbar.pack_added", pack.name());
                renderSection(false);
            }
        });
        return HMCLDemoUi.scroll(HMCLDemoUi.pageColumn(add, list));
    }

    private void cycleMemory(HMCLDemoInstance instance) {
        int next = MEMORY_OPTIONS[0];
        for (int index = 0; index < MEMORY_OPTIONS.length; index++) {
            if (MEMORY_OPTIONS[index] == instance.maxMemoryMb()) {
                next = MEMORY_OPTIONS[(index + 1) % MEMORY_OPTIONS.length];
                break;
            }
        }
        state.updateSelectedInstanceSettings(
                instance.isolated(), next, instance.resolution(), instance.fullscreen(), instance.javaId());
        renderSection(false);
    }

    private void cycleResolution(HMCLDemoInstance instance) {
        String next = RESOLUTION_OPTIONS[0];
        for (int index = 0; index < RESOLUTION_OPTIONS.length; index++) {
            if (RESOLUTION_OPTIONS[index].equals(instance.resolution())) {
                next = RESOLUTION_OPTIONS[(index + 1) % RESOLUTION_OPTIONS.length];
                break;
            }
        }
        state.updateSelectedInstanceSettings(
                instance.isolated(), instance.maxMemoryMb(), next, instance.fullscreen(), instance.javaId());
        renderSection(false);
    }

    private void showRenameDialog(HMCLDemoInstance instance) {
        M3TextField field = new M3TextField(instance.name());
        M3TextInputLayout layout = new M3TextInputLayout(field);
        layout.setLabelText(strings.get("instance.settings.name"));
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("instance.manage.rename"));
        dialog.getDialogPane().setContent(layout);
        M3Button cancel = new M3Button(strings.get("common.cancel"), M3ButtonVariant.TEXT);
        cancel.setCancelButton(true);
        M3Button apply = new M3Button(strings.get("common.apply"), M3ButtonVariant.TEXT);
        apply.setDefaultButton(true);
        dialog.getDialogPane().getActions().setAll(cancel, apply);
        apply.setOnAction(event -> {
            if (state.renameSelectedInstance(field.getText())) {
                controller.showMessageKey("snackbar.instance_renamed", field.getText().strip());
                controller.refreshChrome();
                renderSection(false);
            }
        });
        controller.overlay().showDialog(dialog);
    }

    private void showManageDialog() {
        @Nullable HMCLDemoInstance instance = currentInstance();
        if (instance == null) {
            return;
        }
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(strings.get("instance.action.manage"));
        dialog.getDialogPane().setContentText(strings.format("instance.manage.body", instance.name()));
        M3Button rename = new M3Button(strings.get("instance.manage.rename"), M3ButtonVariant.TEXT);
        M3Button copy = new M3Button(strings.get("instance.manage.copy"), M3ButtonVariant.TEXT);
        M3Button delete = new M3Button(strings.get("instance.manage.delete"), M3ButtonVariant.TEXT);
        M3Button close = new M3Button(strings.get("common.close"), M3ButtonVariant.TEXT);
        close.setCancelButton(true);
        dialog.getDialogPane().getActions().setAll(close, rename, copy, delete);
        rename.setOnAction(event -> showRenameDialog(instance));
        copy.setOnAction(event -> {
            @Nullable HMCLDemoInstance copied = state.copySelectedInstance();
            if (copied != null) {
                controller.showMessageKey("snackbar.instance_copied", copied.name());
                controller.openInstance(copied.id(), HMCLDemoRoute.InstanceSection.SETTINGS);
            }
        });
        delete.setOnAction(event -> {
            String name = instance.name();
            if (state.deleteSelectedInstance()) {
                controller.showMessageKey("snackbar.instance_deleted", name);
                controller.openInstances();
            }
        });
        controller.overlay().showDialog(dialog);
    }

    private void runUpdateTask() {
        @Nullable HMCLDemoInstance instance = currentInstance();
        if (instance == null) {
            return;
        }
        controller.runTask(
                strings.get("instance.action.update"),
                List.of(
                        strings.get("wizard.step.client"),
                        strings.get("wizard.step.libraries"),
                        strings.get("wizard.step.assets"),
                        strings.get("wizard.step.finalize")
                ),
                () -> controller.showMessageKey("snackbar.installed", instance.name()),
                () -> controller.showMessageKey("snackbar.install_cancelled")
        );
    }

    private @Nullable HMCLDemoInstance currentInstance() {
        if (instanceId == null) {
            return null;
        }
        for (HMCLDemoInstance instance : state.getInstances()) {
            if (instance.id().equals(instanceId)) {
                return instance;
            }
        }
        return state.getSelectedInstance();
    }

    private static M3Text heading(String text) {
        M3Text heading = new M3Text(text, M3TextRole.TITLE_SMALL);
        heading.setMaxWidth(Double.MAX_VALUE);
        return heading;
    }

    private static Node padded(Node node) {
        VBox box = new VBox(node);
        box.setPadding(new Insets(24.0));
        return box;
    }
}
