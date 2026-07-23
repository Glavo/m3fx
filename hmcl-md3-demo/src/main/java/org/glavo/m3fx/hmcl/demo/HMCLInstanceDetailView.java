// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3TextField;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

/// Displays a selected instance through HMCL-style contextual navigation and dense management rows.
@NotNullByDefault
public final class HMCLInstanceDetailView extends HMCLDemoView {
    /// Creates the selected-instance detail page.
    ///
    /// @param strings the localization source
    /// @param state the shared demo state
    /// @param actions the application command sink
    public HMCLInstanceDetailView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        initializeView();
    }

    /// Creates the selected-instance page without duplicating the shell-managed title.
    ///
    /// @return the instance-detail page tree
    @Override
    protected Node createContent() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        if (instance == null) {
            return page(commandButton(
                    text("action.back_to_instances"),
                    HMCLDemoActions.command("navigate", HMCLDemoActions.ROUTE_INSTANCES)
            ));
        }

        StackPane content = new StackPane(createGameSettings(instance));
        content.setMinWidth(0.0);
        content.setMaxWidth(Double.MAX_VALUE);
        return contextualPage(createSidebar(instance, content), content);
    }

    /// Creates the fixed-width contextual navigation and its instance actions.
    ///
    /// @param instance the selected instance
    /// @param content the content host to replace
    /// @return the sidebar node
    private Node createSidebar(HMCLDemoInstance instance, StackPane content) {
        M3ListPane sections = createList();
        sections.setSelectionMode(M3SelectionMode.SINGLE);
        sections.setAllowEmptySelection(false);

        M3ListItem settings = addSection(
                sections,
                text("instance.navigation.game_settings"),
                HMCLDemoIcons.SETTINGS,
                () -> createGameSettings(instance),
                content
        );
        addSection(sections, text("instance.navigation.auto_install"), HMCLDemoIcons.DISCOVER,
                () -> createAutoInstall(instance), content);
        addSection(sections, text("instance.mods"), HMCLDemoIcons.INSTANCES,
                () -> createMods(instance), content);
        addSection(sections, text("discover.filter.resources"), HMCLDemoIcons.HOME,
                () -> createContentAction(text("discover.filter.resources"), instance), content);
        addSection(sections, text("instance.navigation.worlds"), HMCLDemoIcons.CHAT,
                () -> createContentAction(text("instance.navigation.worlds"), instance), content);
        addSection(sections, text("discover.filter.shaders"), HMCLDemoIcons.DISCOVER,
                () -> createContentAction(text("discover.filter.shaders"), instance), content);
        settings.setSelected(true);

        M3Button play = new M3Button(text("action.play"), M3ButtonVariant.FILLED);
        play.setMaxWidth(Double.MAX_VALUE);
        play.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_PLAY));

        M3Button openFolder = new M3Button(text("instance.open_folder"), M3ButtonVariant.TEXT);
        openFolder.setMaxWidth(Double.MAX_VALUE);
        openFolder.setOnAction(event -> actions.dispatch("open-instance-folder", instance.id()));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox sidebar = new VBox(8.0, sections, spacer, play, openFolder);
        sidebar.setPadding(new javafx.geometry.Insets(12.0, 8.0, 10.0, 8.0));
        sidebar.setPrefWidth(200.0);
        sidebar.setMinWidth(200.0);
        sidebar.setMaxWidth(200.0);
        return sidebar;
    }

    /// Adds one selection-aware sidebar section that replaces the central content when activated.
    ///
    /// @param list the section list
    /// @param label the localized section label
    /// @param iconPath the section icon path
    /// @param factory the section content factory
    /// @param content the central content host
    /// @return the inserted sidebar item
    private static M3ListItem addSection(
            M3ListPane list,
            String label,
            String iconPath,
            Supplier<Node> factory,
            StackPane content
    ) {
        M3ListItem item = compactItem(label);
        item.setLeading(HMCLDemoIcons.create(iconPath));
        item.setOnAction(event -> content.getChildren().setAll(factory.get()));
        list.getItems().add(item);
        return item;
    }

    /// Creates the default game-settings groups.
    ///
    /// @param instance the selected instance
    /// @return the settings content
    private Node createGameSettings(HMCLDemoInstance instance) {
        M3SettingItem version = actionItem(
                text("instance.game_version"), instance.gameVersion(), "choose-game-version", instance);
        M3SettingItem loader = actionItem(text("instance.loader"), instance.loader(), "choose-loader", instance);
        M3SettingItem javaRuntime = actionItem(
                text("instance.settings.java"), text("instance.settings.java.value"), "choose-java", instance);
        M3SettingItem resolution = actionItem(
                text("instance.settings.resolution"), text("instance.settings.resolution.value"), "choose-resolution", instance);
        M3SettingItem memory = actionItem(
                text("instance.settings.memory"), text("instance.settings.memory.value", 4), "choose-memory", instance);

        M3SwitchSettingItem isolation = compactSwitch(text("instance.settings.isolation"));
        isolation.setSelected(true);
        isolation.setOnAction(event -> actions.dispatch("toggle-isolation", instance.id()));

        M3SwitchSettingItem logs = compactSwitch(text("instance.settings.logs"));
        logs.setSelected(false);
        logs.setOnAction(event -> actions.dispatch("toggle-game-logs", instance.id()));

        return new VBox(
                18.0,
                sectionTitle(text("instance.settings.gameplay")),
                createList(version, loader, javaRuntime, resolution, memory),
                createList(isolation, logs)
        );
    }

    /// Creates the automatic-install management rows.
    ///
    /// @param instance the selected instance
    /// @return the automatic-install content
    private Node createAutoInstall(HMCLDemoInstance instance) {
        M3SettingItem install = actionItem(
                text("common.install"),
                text("instance.detail.subtitle", instance.gameVersion(), instance.loader()),
                HMCLDemoActions.ACTION_INSTALL,
                instance
        );
        M3SettingItem refresh = actionItem(
                text("common.refresh"),
                HMCLDemoModelText.instanceLastPlayed(strings, instance),
                HMCLDemoActions.ACTION_REFRESH,
                instance
        );
        return new VBox(18.0, sectionTitle(text("common.install")), createList(install, refresh));
    }

    /// Creates a searchable dense installed-mod list.
    ///
    /// @param instance the selected instance
    /// @return the mod-management content
    private Node createMods(HMCLDemoInstance instance) {
        M3TextField search = new M3TextField();
        search.setPromptText(text("common.search"));
        search.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(search, Priority.ALWAYS);

        M3IconButton refresh = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.REFRESH));
        refresh.setAccessibleText(text("common.refresh"));
        refresh.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_REFRESH, instance.id()));

        HBox toolbar = new HBox(8.0, search, refresh);
        toolbar.getStyleClass().add("hmcl-list-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        M3ListPane list = createList();
        populateMods(list, instance, "");
        search.textProperty().addListener((observable, oldText, newText) -> populateMods(list, instance, newText));
        return new VBox(8.0, toolbar, list);
    }

    /// Populates the mod list with entries matching a case-insensitive query.
    ///
    /// @param target the list that owns the mod rows
    /// @param instance the selected instance
    /// @param query the query to apply
    private void populateMods(M3ListPane target, HMCLDemoInstance instance, String query) {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        target.getItems().clear();
        for (HMCLDemoMod mod : instance.mods()) {
            if (normalized.isEmpty()
                    || mod.name().toLowerCase(Locale.ROOT).contains(normalized)
                    || mod.version().toLowerCase(Locale.ROOT).contains(normalized)) {
                target.getItems().add(createModRow(mod));
            }
        }
        if (target.getItems().isEmpty()) {
            target.getItems().add(compactItem(text("instance.mods.empty.title")));
        }
    }

    /// Creates one dense installed-mod row.
    ///
    /// @param mod the represented mod
    /// @return the configured mod row
    private M3SwitchSettingItem createModRow(HMCLDemoMod mod) {
        M3SwitchSettingItem item = compactSwitch(mod.name());
        item.getStyleClass().add("hmcl-mod-row");
        item.setLeading(HMCLDemoAssets.imageView("img/mcmod.png", 32.0, 32.0));
        item.setSupportingText(mod.id() + "-" + mod.version() + ".jar");
        item.setTwoLineHeight(52.0);
        item.setTrailingSupportingText(text("instance.mods.version", mod.version()));
        item.setSelected(mod.enabled());
        item.setOnAction(event -> {
            state.setSelectedModEnabled(mod.id(), item.isSelected());
            actions.dispatch("toggle-mod", mod.id());
        });
        return item;
    }

    /// Creates one generic compact content-management section.
    ///
    /// @param label the localized content category label
    /// @param instance the selected instance
    /// @return the compact content list
    private Node createContentAction(String label, HMCLDemoInstance instance) {
        return new VBox(
                18.0,
                sectionTitle(label),
                createList(actionItem(label, text("common.manage"), "manage-instance-content", instance))
        );
    }

    /// Creates an action row with a trailing value and an instance-targeted command.
    ///
    /// @param label the row label
    /// @param value the trailing value
    /// @param action the dispatched action token
    /// @param instance the selected instance
    /// @return the configured action row
    private M3SettingItem actionItem(String label, String value, String action, HMCLDemoInstance instance) {
        M3SettingItem item = new M3SettingItem(label);
        item.setOneLineHeight(48.0);
        item.setTrailingSupportingText(value);
        item.setOnAction(event -> actions.dispatch(action, instance.id()));
        return item;
    }

    /// Creates one compact switch setting row.
    ///
    /// @param label the row label
    /// @return the configured switch row
    private static M3SwitchSettingItem compactSwitch(String label) {
        M3SwitchSettingItem item = new M3SwitchSettingItem(label);
        item.setOneLineHeight(48.0);
        return item;
    }

    /// Creates a compact navigation row.
    ///
    /// @param headline the visible row title
    /// @return the configured row
    private static M3ListItem compactItem(String headline) {
        M3ListItem item = new M3ListItem(headline);
        item.setOneLineHeight(48.0);
        return item;
    }

    /// Creates a non-selecting segmented line-form list.
    ///
    /// @param items the list rows
    /// @return the configured list pane
    private static M3ListPane createList(Node... items) {
        M3ListPane list = new M3ListPane();
        list.getStyleClass().add("hmcl-dense-list");
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getItems().addAll(items);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }
}
