// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3RadioButtonSettingItem;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.tokens.M3Profile;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Locale;

/// Settings primary destination with live appearance binding.
@NotNullByDefault
final class HMCLSettingsView extends BorderPane {
    private static final @Unmodifiable List<Color> SEED_COLORS = List.of(
            Color.web("#5C6BC0"),
            Color.web("#6750A4"),
            Color.web("#006A6A"),
            Color.web("#9C4146"),
            Color.web("#426900")
    );

    private final HMCLDemoController controller;
    private final HMCLDemoStrings strings;
    private final HMCLDemoState state;

    private final M3ListItem globalGameItem = HMCLDemoUi.navItem("", HMCLDemoIcons.INSTANCES, null);
    private final M3ListItem javaItem = HMCLDemoUi.navItem("", HMCLDemoIcons.CODE, null);
    private final M3ListItem generalItem = HMCLDemoUi.navItem("", HMCLDemoIcons.MANAGE, null);
    private final M3ListItem appearanceItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);
    private final M3ListItem downloadItem = HMCLDemoUi.navItem("", HMCLDemoIcons.DOWNLOAD, null);
    private final M3ListItem helpItem = HMCLDemoUi.navItem("", HMCLDemoIcons.HELP, null);
    private final M3ListItem feedbackItem = HMCLDemoUi.navItem("", HMCLDemoIcons.CHAT, null);
    private final M3ListItem aboutItem = HMCLDemoUi.navItem("", HMCLDemoIcons.INFO, null);

    private final M3AnimatedContent centerHost = new M3AnimatedContent();
    private HMCLDemoRoute.SettingsSection section = HMCLDemoRoute.SettingsSection.GLOBAL_GAME;

    /// Creates the settings page.
    ///
    /// @param controller the application controller
    HMCLSettingsView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().addAll("hmcl-settings-page", "hmcl-secondary-page");
        HMCLDemoUi.fill(this);

        globalGameItem.setOnAction(event -> controller.openSettings(HMCLDemoRoute.SettingsSection.GLOBAL_GAME));
        javaItem.setOnAction(event -> controller.openSettings(HMCLDemoRoute.SettingsSection.JAVA));
        generalItem.setOnAction(event -> controller.openSettings(HMCLDemoRoute.SettingsSection.GENERAL));
        appearanceItem.setOnAction(event -> controller.openSettings(HMCLDemoRoute.SettingsSection.APPEARANCE));
        downloadItem.setOnAction(event -> controller.openSettings(HMCLDemoRoute.SettingsSection.DOWNLOAD));
        helpItem.setOnAction(event -> controller.openSettings(HMCLDemoRoute.SettingsSection.HELP));
        feedbackItem.setOnAction(event -> controller.openSettings(HMCLDemoRoute.SettingsSection.FEEDBACK));
        aboutItem.setOnAction(event -> controller.openSettings(HMCLDemoRoute.SettingsSection.ABOUT));

        VBox sidebar = HMCLDemoUi.sidebar(
                globalGameItem,
                javaItem,
                HMCLDemoUi.sectionLabel(""),
                generalItem,
                appearanceItem,
                downloadItem,
                HMCLDemoUi.sectionLabel(""),
                helpItem,
                feedbackItem,
                aboutItem
        );
        HMCLDemoUi.fill(centerHost);
        centerHost.setFitToWidth(true);
        centerHost.setFitToHeight(true);
        setLeft(sidebar);
        setCenter(centerHost);
        refreshLocale();
        render(false);
    }

    /// Shows a settings section.
    ///
    /// @param next the section
    void showSection(HMCLDemoRoute.SettingsSection next) {
        boolean changed = section != next;
        section = next;
        render(changed);
        syncNav();
    }

    /// Refreshes locale-dependent labels.
    void refreshLocale() {
        VBox sidebar = (VBox) getLeft();
        sidebar.getChildren().set(2, HMCLDemoUi.sectionLabel(strings.get("settings.section.launcher")));
        sidebar.getChildren().set(6, HMCLDemoUi.sectionLabel(strings.get("settings.section.help")));
        globalGameItem.setHeadlineText(strings.get("settings.nav.global_game"));
        javaItem.setHeadlineText(strings.get("settings.nav.java"));
        generalItem.setHeadlineText(strings.get("settings.nav.general"));
        appearanceItem.setHeadlineText(strings.get("settings.nav.appearance"));
        downloadItem.setHeadlineText(strings.get("settings.nav.download"));
        helpItem.setHeadlineText(strings.get("settings.nav.help"));
        feedbackItem.setHeadlineText(strings.get("settings.nav.feedback"));
        aboutItem.setHeadlineText(strings.get("settings.nav.about"));
        render(false);
    }

    private void syncNav() {
        globalGameItem.setSelected(section == HMCLDemoRoute.SettingsSection.GLOBAL_GAME);
        javaItem.setSelected(section == HMCLDemoRoute.SettingsSection.JAVA);
        generalItem.setSelected(section == HMCLDemoRoute.SettingsSection.GENERAL);
        appearanceItem.setSelected(section == HMCLDemoRoute.SettingsSection.APPEARANCE);
        downloadItem.setSelected(section == HMCLDemoRoute.SettingsSection.DOWNLOAD);
        helpItem.setSelected(section == HMCLDemoRoute.SettingsSection.HELP);
        feedbackItem.setSelected(section == HMCLDemoRoute.SettingsSection.FEEDBACK);
        aboutItem.setSelected(section == HMCLDemoRoute.SettingsSection.ABOUT);
    }

    private void render(boolean animate) {
        syncNav();
        centerHost.setContentTransform(animate && !state.isAnimationDisabled()
                ? HMCLDemoTransitions.sectionUp()
                : HMCLDemoTransitions.none());
        Node content = switch (section) {
            case GLOBAL_GAME -> globalGameContent();
            case JAVA -> javaContent();
            case GENERAL -> generalContent();
            case APPEARANCE -> appearanceContent();
            case DOWNLOAD -> downloadContent();
            case HELP -> helpContent();
            case FEEDBACK -> feedbackContent();
            case ABOUT -> aboutContent();
        };
        centerHost.setContent(content);
        if (!animate || state.isAnimationDisabled()) {
            centerHost.snapToCurrentState();
        }
    }

    private Node globalGameContent() {
        M3SettingItem memory = HMCLDemoUi.settingItem(strings.get("settings.global.memory"), state.getGlobalMaxMemoryMb() + " MB"
        );
        memory.setSupportingText(strings.get("settings.global.memory.support"));
        memory.setOnAction(event -> {
            int next = switch (state.getGlobalMaxMemoryMb()) {
                case 2048 -> 4096;
                case 4096 -> 6144;
                case 6144 -> 8192;
                default -> 2048;
            };
            state.setGlobalMaxMemoryMb(next);
            render(false);
        });

        M3SettingItem resolution = HMCLDemoUi.settingItem(strings.get("settings.global.resolution"), state.getGlobalResolution()
        );
        resolution.setSupportingText(strings.get("settings.global.resolution.support"));
        resolution.setOnAction(event -> {
            state.setGlobalResolution(switch (state.getGlobalResolution()) {
                case "854x480" -> "1280x720";
                case "1280x720" -> "1920x1080";
                default -> "854x480";
            });
            render(false);
        });

        M3SwitchSettingItem autoMemory = new M3SwitchSettingItem(strings.get("settings.general.auto_memory"));
        autoMemory.setSupportingText(strings.get("settings.general.auto_memory.support"));
        autoMemory.setSelected(state.isAutoAllocateMemory());
        autoMemory.selectedProperty().addListener((observable, oldValue, newValue) ->
                state.setAutoAllocateMemory(Boolean.TRUE.equals(newValue)));

        return scroll(column(
                heading(strings.get("settings.global.section.game")),
                memory,
                resolution,
                autoMemory
        ));
    }

    private Node javaContent() {
        VBox list = new VBox(4.0);
        for (HMCLDemoJavaRuntime runtime : state.getJavaRuntimes()) {
            M3ListItem row = new M3ListItem(runtime.name());
            row.setSupportingText(runtime.version() + " · " + runtime.architecture());
            row.setSelected(runtime.id().equals(state.getSelectedJavaId()));
            row.setMaxWidth(Double.MAX_VALUE);
            row.setOnAction(event -> {
                state.setSelectedJavaId(runtime.id());
                controller.showMessageKey("snackbar.settings_java_selected", runtime.name());
                render(false);
            });
            list.getChildren().add(row);
        }
        M3SettingItem downloadJava = HMCLDemoUi.settingItem(strings.get("settings.java.download"), strings.get("settings.java.download.support")
        );
        downloadJava.setOnAction(event -> controller.showMessageKey("snackbar.settings_download_java"));
        return scroll(column(heading(strings.get("settings.java.section.runtimes")), list, downloadJava));
    }

    private Node generalContent() {
        M3RadioButtonSettingItem english = new M3RadioButtonSettingItem(strings.get("settings.language.en"));
        M3RadioButtonSettingItem chinese = new M3RadioButtonSettingItem(strings.get("settings.language.zh_cn"));
        english.setSelected(state.getLanguage().equals(Locale.ENGLISH));
        chinese.setSelected(state.getLanguage().getLanguage().equals("zh"));
        english.setOnAction(event -> {
            state.setLanguage(Locale.ENGLISH);
            chinese.setSelected(false);
            english.setSelected(true);
        });
        chinese.setOnAction(event -> {
            state.setLanguage(Locale.SIMPLIFIED_CHINESE);
            english.setSelected(false);
            chinese.setSelected(true);
        });

        M3SwitchSettingItem acceptPreview = new M3SwitchSettingItem(strings.get("settings.general.accept_preview"));
        acceptPreview.setSupportingText(strings.get("settings.general.accept_preview.support"));
        acceptPreview.setSelected(state.isAcceptPreviewUpdate());
        acceptPreview.selectedProperty().addListener((observable, oldValue, newValue) ->
                state.setAcceptPreviewUpdate(Boolean.TRUE.equals(newValue)));

        M3SettingItem checkUpdates = HMCLDemoUi.settingItem(strings.get("settings.general.check_updates"), strings.get("settings.general.check_updates.support")
        );
        checkUpdates.setOnAction(event -> controller.showMessageKey("snackbar.settings_check_updates"));

        return scroll(column(
                heading(strings.get("settings.general.section.language")),
                english,
                chinese,
                heading(strings.get("settings.general.section.update")),
                acceptPreview,
                checkUpdates
        ));
    }

    private Node appearanceContent() {
        FlowPane seeds = new FlowPane(12.0, 12.0);
        for (Color color : SEED_COLORS) {
            M3IconButton button = new M3IconButton();
            button.getStyleClass().add("hmcl-seed-button");
            button.setStyle("-fx-background-color: " + toHex(color) + ";");
            button.setOnAction(event -> state.setThemeColor(color));
            seeds.getChildren().add(button);
        }

        M3RadioButtonSettingItem light = new M3RadioButtonSettingItem(strings.get("settings.brightness.light"));
        M3RadioButtonSettingItem dark = new M3RadioButtonSettingItem(strings.get("settings.brightness.dark"));
        M3RadioButtonSettingItem system = new M3RadioButtonSettingItem(strings.get("settings.brightness.system"));
        light.setSelected(state.getBrightness() == HMCLDemoState.Brightness.LIGHT);
        dark.setSelected(state.getBrightness() == HMCLDemoState.Brightness.DARK);
        system.setSelected(state.getBrightness() == HMCLDemoState.Brightness.SYSTEM);
        light.setOnAction(event -> {
            state.setBrightness(HMCLDemoState.Brightness.LIGHT);
            render(false);
        });
        dark.setOnAction(event -> {
            state.setBrightness(HMCLDemoState.Brightness.DARK);
            render(false);
        });
        system.setOnAction(event -> {
            state.setBrightness(HMCLDemoState.Brightness.SYSTEM);
            render(false);
        });

        M3SwitchSettingItem expressive = new M3SwitchSettingItem(strings.get("settings.appearance.expressive"));
        expressive.setSupportingText(strings.get("settings.appearance.expressive.support"));
        expressive.setSelected(state.getProfile() == M3Profile.EXPRESSIVE_2025);
        expressive.selectedProperty().addListener((observable, oldValue, newValue) ->
                state.setProfile(Boolean.TRUE.equals(newValue)
                        ? M3Profile.EXPRESSIVE_2025
                        : M3Profile.BASELINE_2021));

        M3SwitchSettingItem animation = new M3SwitchSettingItem(strings.get("settings.appearance.animation"));
        animation.setSupportingText(strings.get("settings.appearance.animation.support"));
        animation.setSelected(state.isAnimationDisabled());
        animation.selectedProperty().addListener((observable, oldValue, newValue) ->
                state.setAnimationDisabled(Boolean.TRUE.equals(newValue)));

        M3RadioButtonSettingItem meadow = new M3RadioButtonSettingItem(strings.get("settings.wallpaper.meadow"));
        M3RadioButtonSettingItem caves = new M3RadioButtonSettingItem(strings.get("settings.wallpaper.caves"));
        M3RadioButtonSettingItem sunset = new M3RadioButtonSettingItem(strings.get("settings.wallpaper.sunset"));
        meadow.setSelected(state.getWallpaper() == HMCLDemoState.Wallpaper.MEADOW);
        caves.setSelected(state.getWallpaper() == HMCLDemoState.Wallpaper.CAVES);
        sunset.setSelected(state.getWallpaper() == HMCLDemoState.Wallpaper.SUNSET);
        meadow.setOnAction(event -> {
            state.setWallpaper(HMCLDemoState.Wallpaper.MEADOW);
            render(false);
        });
        caves.setOnAction(event -> {
            state.setWallpaper(HMCLDemoState.Wallpaper.CAVES);
            render(false);
        });
        sunset.setOnAction(event -> {
            state.setWallpaper(HMCLDemoState.Wallpaper.SUNSET);
            render(false);
        });

        return scroll(column(
                heading(strings.get("settings.appearance.section.theme")),
                seeds,
                light,
                dark,
                system,
                expressive,
                heading(strings.get("settings.appearance.section.animation")),
                animation,
                heading(strings.get("settings.appearance.section.background")),
                meadow,
                caves,
                sunset
        ));
    }

    private Node downloadContent() {
        M3SettingItem source = HMCLDemoUi.settingItem(strings.get("settings.download.source"), state.getDownloadSource()
        );
        source.setOnAction(event -> {
            state.setDownloadSource(switch (state.getDownloadSource()) {
                case "official" -> "mirror";
                case "mirror" -> "auto";
                default -> "official";
            });
            render(false);
        });

        M3SwitchSettingItem autoThreads = new M3SwitchSettingItem(strings.get("settings.download.auto_threads"));
        autoThreads.setSupportingText(strings.get("settings.download.auto_threads.support"));
        autoThreads.setSelected(state.isAutoDownloadThreads());
        autoThreads.selectedProperty().addListener((observable, oldValue, newValue) ->
                state.setAutoDownloadThreads(Boolean.TRUE.equals(newValue)));

        M3SettingItem clean = HMCLDemoUi.settingItem(strings.get("settings.download.clean_cache"), strings.get("settings.download.clean_cache.support")
        );
        clean.setOnAction(event -> controller.showMessageKey("snackbar.settings_clean_cache"));

        return scroll(column(
                heading(strings.get("settings.download.section.source")),
                source,
                autoThreads,
                clean
        ));
    }

    private Node helpContent() {
        return scroll(column(
                body(strings.get("settings.help.body")),
                action(strings.get("settings.help.install"), "snackbar.settings_open_docs"),
                action(strings.get("settings.help.launch"), "snackbar.settings_open_docs"),
                action(strings.get("settings.help.mods"), "snackbar.settings_open_docs")
        ));
    }

    private Node feedbackContent() {
        return scroll(column(
                body(strings.get("settings.feedback.body")),
                action(strings.get("settings.feedback.github"), "snackbar.settings_feedback_github"),
                action(strings.get("settings.feedback.discord"), "snackbar.settings_feedback_discord")
        ));
    }

    private Node aboutContent() {
        return scroll(column(
                body(strings.get("settings.about.body")),
                HMCLDemoUi.settingItem(strings.get("settings.about.app_name"), strings.get("settings.about.app_name.value")),
                HMCLDemoUi.settingItem(strings.get("settings.about.version"), strings.get("settings.about.version.value")),
                HMCLDemoUi.settingItem(strings.get("settings.about.author"), strings.get("settings.about.author.value")),
                HMCLDemoUi.settingItem(strings.get("settings.about.dep.m3fx"), strings.get("settings.about.dep.m3fx.detail")),
                HMCLDemoUi.settingItem(strings.get("settings.about.legal.hmcl"), strings.get("settings.about.legal.hmcl.detail"))
        ));
    }

    private M3SettingItem action(String title, String snackbarKey) {
        M3SettingItem item = HMCLDemoUi.settingItem(title, "");
        item.setOnAction(event -> controller.showMessageKey(snackbarKey));
        return item;
    }

    private static M3Text heading(String text) {
        M3Text heading = new M3Text(text, M3TextRole.TITLE_SMALL);
        heading.setMaxWidth(Double.MAX_VALUE);
        return heading;
    }

    private static M3Text body(String text) {
        M3Text body = new M3Text(text, M3TextRole.BODY_MEDIUM);
        body.setWrapText(true);
        body.setMaxWidth(Double.MAX_VALUE);
        return body;
    }

    private static VBox column(Node... children) {
        return HMCLDemoUi.pageColumn(children);
    }

    private static Node scroll(VBox column) {
        return HMCLDemoUi.scroll(column);
    }

    private static String toHex(Color color) {
        return String.format(
                "#%02X%02X%02X",
                Math.round(color.getRed() * 255.0),
                Math.round(color.getGreen() * 255.0),
                Math.round(color.getBlue() * 255.0)
        );
    }
}
