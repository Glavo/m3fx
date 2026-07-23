// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.scene.Node;
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
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Displays a selected instance through a contextual sidebar and dense line-form content.
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

    /// Creates the selected instance page.
    ///
    /// @return the instance-detail page tree
    @Override
    protected Node createContent() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        if (instance == null) {
            return page(heading(text("instance.detail.empty.title"), text("instance.detail.empty.subtitle")),
                    commandButton(text("action.back_to_instances"), M3ButtonVariant.FILLED,
                            HMCLDemoActions.command("navigate", HMCLDemoActions.ROUTE_INSTANCES)));
        }
        StackPane content = new StackPane(createGameSettings(instance));
        content.setMinWidth(0.0);
        content.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(content, Priority.ALWAYS);
        HBox layout = new HBox(24.0, createSidebar(instance, content), content);
        layout.setMinWidth(0.0);
        layout.setMaxWidth(Double.MAX_VALUE);
        return page(heading(instance.name(), text("instance.detail.subtitle", instance.gameVersion(), instance.loader())), layout);
    }

    /// Creates the approximately 200-pixel contextual navigation and bottom instance actions.
    ///
    /// @param instance the selected instance
    /// @param content the content host to replace
    /// @return the sidebar node
    private Node createSidebar(HMCLDemoInstance instance, StackPane content) {
        M3ListPane sections = createList();
        addSection(sections, text("instance.settings"), () -> createGameSettings(instance), content);
        addSection(sections, text("common.install"), () -> createAutoInstall(instance), content);
        addSection(sections, text("instance.mods"), () -> createMods(instance), content);
        addSection(sections, text("discover.filter.resources"), () -> createContentAction(text("discover.filter.resources"), instance), content);
        addSection(sections, text("common.open"), () -> createContentAction(text("common.open"), instance), content);
        addSection(sections, text("discover.filter.shaders"), () -> createContentAction(text("discover.filter.shaders"), instance), content);
        M3Button testLaunch = new M3Button(text("action.play"), M3ButtonVariant.FILLED);
        testLaunch.setMaxWidth(Double.MAX_VALUE);
        testLaunch.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_PLAY));
        M3Button browse = new M3Button(text("common.open"), M3ButtonVariant.TEXT);
        browse.setMaxWidth(Double.MAX_VALUE);
        browse.setOnAction(event -> actions.dispatch("open-instance-folder", instance.id()));
        M3Button manage = new M3Button(text("common.manage"), M3ButtonVariant.TEXT);
        manage.setMaxWidth(Double.MAX_VALUE);
        manage.setOnAction(event -> actions.dispatch("manage-instance", instance.id()));
        VBox sidebar = new VBox(8.0, sections, testLaunch, browse, manage);
        sidebar.setPrefWidth(200.0);
        sidebar.setMinWidth(180.0);
        sidebar.setMaxWidth(220.0);
        return sidebar;
    }

    /// Adds a sidebar section that replaces the central content when activated.
    ///
    /// @param list the section list
    /// @param label the localized section label
    /// @param factory the section content factory
    /// @param content the central content host
    private static void addSection(M3ListPane list, String label, java.util.function.Supplier<Node> factory,
                                   StackPane content) {
        M3ListItem item = new M3ListItem(label);
        item.setOnAction(event -> content.getChildren().setAll(factory.get()));
        list.getItems().add(item);
    }

    /// Creates the game settings line form.
    ///
    /// @param instance the selected instance
    /// @return the settings content
    private Node createGameSettings(HMCLDemoInstance instance) {
        M3SettingItem version = actionItem(text("instance.game_version"), instance.gameVersion(), "choose-game-version", instance);
        M3SettingItem loader = actionItem(text("instance.loader"), instance.loader(), "choose-loader", instance);
        M3SettingItem javaRuntime = actionItem(text("instance.settings.java"), text("instance.settings.java.value"), "choose-java", instance);
        M3SettingItem resolution = actionItem(text("instance.settings.resolution"), text("instance.settings.resolution.value"), "choose-resolution", instance);
        M3SwitchSettingItem isolation = new M3SwitchSettingItem(text("instance.settings.isolation"));
        isolation.setSupportingText(text("instance.settings.isolation.supporting"));
        isolation.setSelected(true);
        isolation.setOnAction(event -> actions.dispatch("toggle-isolation", instance.id()));
        return createList(version, loader, javaRuntime, resolution, isolation);
    }

    /// Creates the automatic-install line form.
    ///
    /// @param instance the selected instance
    /// @return the automatic-install content
    private Node createAutoInstall(HMCLDemoInstance instance) {
        M3SettingItem install = actionItem(text("common.install"), text("instance.detail.subtitle", instance.gameVersion(), instance.loader()), HMCLDemoActions.ACTION_INSTALL, instance);
        M3SettingItem refresh = actionItem(text("common.refresh"), HMCLDemoModelText.instanceLastPlayed(strings, instance), HMCLDemoActions.ACTION_REFRESH, instance);
        return createList(install, refresh);
    }

    /// Creates the installed-mod line form.
    ///
    /// @param instance the selected instance
    /// @return the mod-management content
    private Node createMods(HMCLDemoInstance instance) {
        M3ListPane list = createList();
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
        if (list.getItems().isEmpty()) list.getItems().add(new M3ListItem(text("instance.mods.empty.title")));
        return list;
    }

    /// Creates one generic content-management line-form row.
    ///
    /// @param label the localized content category label
    /// @param instance the selected instance
    /// @return the compact content list
    private Node createContentAction(String label, HMCLDemoInstance instance) {
        return createList(actionItem(label, text("common.manage"), "manage-instance-content", instance));
    }

    /// Creates an action row with supporting text and an instance-targeted command.
    ///
    /// @param label the row label
    /// @param supporting the supporting text
    /// @param action the dispatched action token
    /// @param instance the selected instance
    /// @return the configured action row
    private M3SettingItem actionItem(String label, String supporting, String action, HMCLDemoInstance instance) {
        M3SettingItem item = new M3SettingItem(label);
        item.setSupportingText(supporting);
        item.setOnAction(event -> actions.dispatch(action, instance.id()));
        return item;
    }

    /// Creates a non-selecting segmented line-form list.
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
