// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.controls.M3TabBarVariant;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/// Displays a focused overview, settings sample, and mod list for the selected game instance.
@NotNullByDefault
public final class HMCLInstanceDetailView extends HMCLDemoView {
    /// Creates the selected-instance detail page.
    ///
    /// @param strings the localization source
    /// @param state   the shared demo state
    /// @param actions the application command sink
    public HMCLInstanceDetailView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        initializeView();
    }

    /// Creates the localized instance-detail content.
    ///
    /// @return the instance-detail page tree
    @Override
    protected Node createContent() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        if (instance == null) {
            return page(
                    heading(text("instance.detail.empty.title"), text("instance.detail.empty.subtitle")),
                    commandButton(
                            text("action.back_to_instances"),
                            M3ButtonVariant.FILLED,
                            HMCLDemoActions.command("navigate", HMCLDemoActions.ROUTE_INSTANCES)
                    )
            );
        }

        StackPane contentHost = new StackPane();
        contentHost.setMinWidth(0.0);
        contentHost.setMaxWidth(Double.MAX_VALUE);

        M3Tab overviewTab = new M3Tab(text("instance.tab.overview"));
        M3Tab settingsTab = new M3Tab(text("instance.tab.settings"));
        M3Tab modsTab = new M3Tab(text("instance.tab.mods"));
        overviewTab.setSelected(true);

        overviewTab.setOnAction(event -> contentHost.getChildren().setAll(createOverview(instance)));
        settingsTab.setOnAction(event -> contentHost.getChildren().setAll(createSettings(instance)));
        modsTab.setOnAction(event -> contentHost.getChildren().setAll(createMods(instance)));

        M3TabBar tabs = new M3TabBar();
        tabs.setVariant(M3TabBarVariant.SECONDARY);
        tabs.getTabs().addAll(overviewTab, settingsTab, modsTab);
        contentHost.getChildren().setAll(createOverview(instance));

        return page(
                heading(instance.name(), text("instance.detail.subtitle", instance.gameVersion(), instance.loader())),
                tabs,
                contentHost
        );
    }

    /// Creates the instance overview tab.
    ///
    /// @param instance the represented instance
    /// @return the overview content
    private Node createOverview(HMCLDemoInstance instance) {
        M3AssistChip status = new M3AssistChip(text(
                "instance.status." + instance.status().name().toLowerCase(Locale.ROOT)
        ));
        status.setDisable(true);

        M3Text description = new M3Text(
                HMCLDemoModelText.instanceDescription(strings, instance),
                M3TextRole.BODY_LARGE
        );
        description.setWrapText(true);
        M3Text lastPlayed = new M3Text(
                text("instance.last_played", HMCLDemoModelText.instanceLastPlayed(strings, instance)),
                M3TextRole.BODY_MEDIUM
        );

        M3Button play = new M3Button(text("action.play"), M3ButtonVariant.FILLED);
        play.setGraphic(HMCLDemoIcons.create(HMCLDemoIcons.PLAY));
        play.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_PLAY));
        M3Button folder = new M3Button(text("instance.open_folder"), M3ButtonVariant.OUTLINED);
        folder.setOnAction(event -> actions.dispatch("open-instance-folder", instance.id()));

        M3Card summary = card(
                M3CardVariant.ELEVATED,
                status,
                new M3Text(instance.name(), M3TextRole.HEADLINE_SMALL),
                description,
                lastPlayed,
                new HBox(10.0, play, folder)
        );

        M3SettingItem gameVersion = new M3SettingItem(text("instance.game_version"));
        gameVersion.setSupportingText(instance.gameVersion());
        M3SettingItem loader = new M3SettingItem(text("instance.loader"));
        loader.setSupportingText(instance.loader());
        M3SettingItem installedMods = new M3SettingItem(text("instance.installed_mods"));
        installedMods.setSupportingText(text("instance.installed_mods.count", instance.mods().size()));

        M3ListPane facts = createList(gameVersion, loader, installedMods);
        return new VBox(20.0, summary, sectionTitle(text("instance.overview.details")), facts);
    }

    /// Creates the representative instance settings tab.
    ///
    /// @param instance the represented instance
    /// @return the settings content
    private Node createSettings(HMCLDemoInstance instance) {
        M3SettingItem javaRuntime = new M3SettingItem(text("instance.settings.java"));
        javaRuntime.setSupportingText(text("instance.settings.java.value"));
        javaRuntime.setOnAction(event -> actions.dispatch("choose-java", instance.id()));

        M3SettingItem resolution = new M3SettingItem(text("instance.settings.resolution"));
        resolution.setSupportingText(text("instance.settings.resolution.value"));
        resolution.setOnAction(event -> actions.dispatch("choose-resolution", instance.id()));

        M3SwitchSettingItem isolation = new M3SwitchSettingItem(text("instance.settings.isolation"));
        isolation.setSupportingText(text("instance.settings.isolation.supporting"));
        isolation.setSelected(true);
        isolation.setOnAction(event -> actions.dispatch("toggle-isolation", instance.id()));

        M3SwitchSettingItem showLogs = new M3SwitchSettingItem(text("instance.settings.logs"));
        showLogs.setSupportingText(text("instance.settings.logs.supporting"));
        showLogs.setSelected(false);
        showLogs.setOnAction(event -> actions.dispatch("toggle-game-logs", instance.id()));

        M3ListPane settings = createList(javaRuntime, resolution, isolation, showLogs);

        M3Text memoryTitle = new M3Text(text("instance.settings.memory"), M3TextRole.TITLE_MEDIUM);
        M3Text memoryValue = new M3Text(text("instance.settings.memory.value", 8), M3TextRole.BODY_MEDIUM);
        M3Slider memory = new M3Slider(2.0, 24.0, 8.0);
        memory.valueProperty().addListener((observable, oldValue, newValue) ->
                memoryValue.setText(text("instance.settings.memory.value", Math.round(newValue.doubleValue()))));
        M3Card memoryCard = card(M3CardVariant.FILLED, memoryTitle, memoryValue, memory);

        return new VBox(20.0, sectionTitle(text("instance.settings.gameplay")), settings, memoryCard);
    }

    /// Creates the installed-mod tab.
    ///
    /// @param instance the represented instance
    /// @return the mod-management content
    private Node createMods(HMCLDemoInstance instance) {
        if (instance.mods().isEmpty()) {
            return card(
                    M3CardVariant.FILLED,
                    new M3Text(text("instance.mods.empty.title"), M3TextRole.TITLE_MEDIUM),
                    new M3Text(text("instance.mods.empty.subtitle"), M3TextRole.BODY_MEDIUM),
                    commandButton(
                            text("instance.mods.discover"),
                            M3ButtonVariant.TONAL,
                            HMCLDemoActions.command("navigate", HMCLDemoActions.ROUTE_DISCOVER)
                    )
            );
        }

        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        for (HMCLDemoMod mod : instance.mods()) {
            M3SwitchSettingItem item = new M3SwitchSettingItem(mod.name());
            item.setSupportingText(text("instance.mods.version", mod.version()));
            item.setSelected(mod.enabled());
            item.setOnAction(event -> {
                state.setSelectedModEnabled(mod.id(), item.isSelected());
                actions.dispatch("toggle-mod", mod.id());
            });
            list.getItems().add(item);
        }

        M3Button discover = new M3Button(text("instance.mods.discover"), M3ButtonVariant.TONAL);
        discover.setOnAction(event -> actions.navigate(HMCLDemoActions.ROUTE_DISCOVER));
        HBox actionsRow = new HBox(discover);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(20.0, sectionTitle(text("instance.mods.title")), list, actionsRow);
    }

    /// Creates a non-selecting segmented list.
    ///
    /// @param items the list rows
    /// @return the configured list pane
    private static M3ListPane createList(Node... items) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getItems().addAll(items);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }
}
