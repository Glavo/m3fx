// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3RadioButtonSettingItem;
import org.glavo.m3fx.controls.M3SelectSettingItem;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/// Displays launcher settings with HMCL-style section navigation and interactive offline controls.
@NotNullByDefault
final class HMCLSettingsView extends BorderPane {
    /// Settings sections shown in the left pane.
    private enum Section {
        /// Global game defaults shared by instances.
        GLOBAL_GAME,

        /// Discovered Java runtimes.
        JAVA,

        /// General launcher preferences.
        GENERAL,

        /// Theme and wallpaper preferences.
        APPEARANCE,

        /// Download source and concurrency.
        DOWNLOAD,

        /// Help topics.
        HELP,

        /// Feedback channels.
        FEEDBACK,

        /// About and legal notices.
        ABOUT
    }

    /// Proxy host presets shown by the host select row.
    private static final List<String> PROXY_HOST_OPTIONS = List.of(
            "127.0.0.1", "localhost", "proxy.example.com"
    );

    /// Proxy port presets shown by the port select row.
    private static final List<Integer> PROXY_PORT_OPTIONS = List.of(7890, 1080, 8080, 3128);

    /// Concurrent thread counts shown by the download-threads select row.
    private static final List<Integer> THREAD_OPTIONS = List.of(16, 32, 64, 128);

    /// Background opacity percentages.
    private static final List<Integer> OPACITY_OPTIONS = List.of(100, 80, 60, 40);

    /// Font size steps in points.
    private static final List<Integer> FONT_SIZE_OPTIONS = List.of(12, 13, 14, 16);

    /// Theme seed colors shown by the theme-color select row.
    private static final List<Color> THEME_COLORS = List.of(
            Color.web("#5C6BC0"),
            Color.web("#00897B"),
            Color.web("#FB8C00"),
            Color.web("#8E24AA")
    );

    /// Update channel ids.
    private static final List<String> CHANNEL_OPTIONS = List.of("stable", "dev");

    /// Language options for the general settings row.
    private static final List<Locale> LANGUAGE_OPTIONS =
            List.of(HMCLDemoStrings.ENGLISH, HMCLDemoStrings.SIMPLIFIED_CHINESE);

    /// Brightness modes.
    private static final List<HMCLDemoState.Brightness> BRIGHTNESS_OPTIONS =
            List.of(
                    HMCLDemoState.Brightness.SYSTEM,
                    HMCLDemoState.Brightness.LIGHT,
                    HMCLDemoState.Brightness.DARK
            );

    /// Wallpaper presets.
    private static final List<HMCLDemoState.Wallpaper> WALLPAPER_OPTIONS =
            List.of(
                    HMCLDemoState.Wallpaper.MEADOW,
                    HMCLDemoState.Wallpaper.CAVES,
                    HMCLDemoState.Wallpaper.SUNSET
            );

    /// Background image load policies.
    private static final List<String> BACKGROUND_LOAD_OPTIONS = List.of("eager", "lazy");

    /// Font family ids.
    private static final List<String> FONT_FAMILY_OPTIONS = List.of("system", "sans", "serif");

    /// Font antialias modes.
    private static final List<String> FONT_ANTIALIAS_OPTIONS = List.of("default", "lcd", "gray");

    /// Auto / official / mirror source modes.
    private static final List<String> SOURCE_MODE_OPTIONS = List.of("auto", "official", "mirror");

    /// Addon catalog sources.
    private static final List<String> ADDON_SOURCE_OPTIONS = List.of("modrinth", "curseforge");

    /// Cache directory modes.
    private static final List<String> CACHE_TYPE_OPTIONS = List.of("default", "custom");

    /// Proxy type ids.
    private static final List<String> PROXY_TYPE_OPTIONS = List.of("system", "none", "http", "socks");

    /// The localization source.
    private final HMCLDemoStrings strings;

    /// The shared state.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// The launcher section label.
    private final M3Text launcherSection = HMCLDemoUi.sectionLabel("");

    /// The help section label.
    private final M3Text helpSection = HMCLDemoUi.sectionLabel("");

    /// Navigation rows.
    private final M3ListItem globalGameItem = HMCLDemoUi.navItem(HMCLDemoIcons.SETTINGS);
    private final M3ListItem javaItem = HMCLDemoUi.navItem(HMCLDemoIcons.CODE);
    private final M3ListItem generalItem = HMCLDemoUi.navItem(HMCLDemoIcons.SETTINGS);
    private final M3ListItem appearanceItem = HMCLDemoUi.navItem(HMCLDemoIcons.IMAGE);
    private final M3ListItem downloadItem = HMCLDemoUi.navItem(HMCLDemoIcons.DOWNLOAD);
    private final M3ListItem helpItem = HMCLDemoUi.navItem(HMCLDemoIcons.HELP);
    private final M3ListItem feedbackItem = HMCLDemoUi.navItem(HMCLDemoIcons.CHAT);
    private final M3ListItem aboutItem = HMCLDemoUi.navItem(HMCLDemoIcons.INFO);

    /// The animated center content host.
    private final M3AnimatedContent contentHost = new M3AnimatedContent();

    /// The active section.
    private Section section = Section.GENERAL;

    /// Background image load policy: `eager` or `lazy`.
    private String backgroundLoadPolicy = "eager";

    /// UI font family id: `system`, `sans`, or `serif`.
    private String fontFamily = "system";

    /// UI font size in points.
    private int fontSize = 13;

    /// Font antialias mode: `default`, `lcd`, or `gray`.
    private String fontAntialias = "default";

    /// Creates the settings page.
    ///
    /// @param controller the application controller
    HMCLSettingsView(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();
        this.state = controller.state();

        getStyleClass().addAll("hmcl-settings-page", "hmcl-secondary-page");
        HMCLDemoUi.fill(this);
        HMCLDemoUi.fill(contentHost);
        contentHost.setFitToWidth(true);
        contentHost.setFitToHeight(true);
        contentHost.setContentTransform(HMCLDemoTransitions.sectionUp());

        globalGameItem.setOnAction(event -> showSection(Section.GLOBAL_GAME));
        javaItem.setOnAction(event -> showSection(Section.JAVA));
        generalItem.setOnAction(event -> showSection(Section.GENERAL));
        appearanceItem.setOnAction(event -> showSection(Section.APPEARANCE));
        downloadItem.setOnAction(event -> showSection(Section.DOWNLOAD));
        helpItem.setOnAction(event -> showSection(Section.HELP));
        feedbackItem.setOnAction(event -> showSection(Section.FEEDBACK));
        aboutItem.setOnAction(event -> showSection(Section.ABOUT));

        VBox sidebar = HMCLDemoUi.sidebar(
                globalGameItem,
                javaItem,
                launcherSection,
                generalItem,
                appearanceItem,
                downloadItem,
                helpSection,
                helpItem,
                feedbackItem,
                aboutItem
        );
        setLeft(HMCLDemoUi.sidebarHost(sidebar));
        setCenter(contentHost);

        refreshLocale();
        showSection(Section.GENERAL);
    }

    /// Updates static labels and rebuilds the active section.
    void refreshLocale() {
        launcherSection.setText(strings.get("settings.section.launcher"));
        helpSection.setText(strings.get("settings.section.help"));
        globalGameItem.setHeadlineText(strings.get("settings.nav.global_game"));
        javaItem.setHeadlineText(strings.get("settings.nav.java"));
        generalItem.setHeadlineText(strings.get("settings.nav.general"));
        appearanceItem.setHeadlineText(strings.get("settings.nav.appearance"));
        downloadItem.setHeadlineText(strings.get("settings.nav.download"));
        helpItem.setHeadlineText(strings.get("settings.nav.help"));
        feedbackItem.setHeadlineText(strings.get("settings.nav.feedback"));
        aboutItem.setHeadlineText(strings.get("settings.nav.about"));
        renderSection(false);
    }

    /// Selects a settings section from the shell route model.
    ///
    /// @param next the route section
    void showSection(HMCLDemoRoute.SettingsSection next) {
        showSection(mapSection(next), true);
    }

    /// Selects a settings section and updates navigation selection.
    ///
    /// @param next the section
    private void showSection(Section next) {
        showSection(next, false);
    }

    /// Selects a settings section and optionally refreshes a retained route on re-entry.
    ///
    /// @param next the section
    /// @param refreshCurrent whether to rebuild an unchanged section from current application state
    private void showSection(Section next, boolean refreshCurrent) {
        boolean changed = section != next;
        section = next;
        globalGameItem.setSelected(next == Section.GLOBAL_GAME);
        javaItem.setSelected(next == Section.JAVA);
        generalItem.setSelected(next == Section.GENERAL);
        appearanceItem.setSelected(next == Section.APPEARANCE);
        downloadItem.setSelected(next == Section.DOWNLOAD);
        helpItem.setSelected(next == Section.HELP);
        feedbackItem.setSelected(next == Section.FEEDBACK);
        aboutItem.setSelected(next == Section.ABOUT);
        if (changed || refreshCurrent || contentHost.getContent() == null) {
            renderSection(changed);
        }
    }

    /// Maps a shell route section onto the local settings section model.
    ///
    /// @param next the route section
    /// @return the local section
    private static Section mapSection(HMCLDemoRoute.SettingsSection next) {
        return switch (next) {
            case GLOBAL_GAME -> Section.GLOBAL_GAME;
            case JAVA -> Section.JAVA;
            case GENERAL -> Section.GENERAL;
            case APPEARANCE -> Section.APPEARANCE;
            case DOWNLOAD -> Section.DOWNLOAD;
            case HELP -> Section.HELP;
            case FEEDBACK -> Section.FEEDBACK;
            case ABOUT -> Section.ABOUT;
        };
    }

    /// Rebuilds the center content for the active section.
    ///
    /// When the same section is refreshed after locale or route state changes, the previous scroll offsets are
    /// restored.
    ///
    /// @param animate whether to animate the section replacement
    private void renderSection(boolean animate) {
        double previousV = 0.0;
        double previousH = 0.0;
        boolean restoreScroll = false;
        @Nullable Node previousContent = contentHost.getContent();
        if (!animate && previousContent instanceof ScrollPane previousScroll) {
            previousV = previousScroll.getVvalue();
            previousH = previousScroll.getHvalue();
            restoreScroll = true;
        }

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
        if (content instanceof Region region) {
            HMCLDemoUi.fill(region);
        }
        contentHost.setContent(content);
        if (!animate) {
            contentHost.snapToCurrentState();
        }
        if (restoreScroll && content instanceof ScrollPane scrollPane) {
            scrollPane.setVvalue(previousV);
            scrollPane.setHvalue(previousH);
        }
    }

    /// Creates the global game settings form aligned with HMCL `GameSettingsPage` (preset mode).
    ///
    /// @return the content node
    private Node globalGameContent() {
        return HMCLGameSettingsForm.create(
                controller,
                state::getGlobalGameSettings,
                state::setGlobalGameSettings,
                false
        );
    }

    /// Creates the Java management form.
    ///
    /// @return the content node
    private Node javaContent() {
        ToggleGroup group = new ToggleGroup();
        String selectedId = state.getSelectedJavaId();

        M3RadioButtonSettingItem auto = new M3RadioButtonSettingItem(strings.get("settings.java.auto"));
        auto.setSupportingText(strings.get("settings.java.auto.support"));
        auto.setToggleGroup(group);
        auto.setSelected("auto".equals(selectedId));
        auto.setOnAction(event -> {
            state.setSelectedJavaId("auto");
            saved();
        });

        M3ListPane runtimes = newList();
        runtimes.getItems().add(auto);
        for (HMCLDemoJavaRuntime runtime : state.getJavaRuntimes()) {
            M3RadioButtonSettingItem item = new M3RadioButtonSettingItem(runtime.name());
            item.setSupportingText(runtime.version() + " · " + runtime.architecture() + " · " + runtime.path());
            item.setToggleGroup(group);
            item.setSelected(runtime.id().equals(selectedId));
            item.setOnAction(event -> {
                state.setSelectedJavaId(runtime.id());
                controller.showMessageKey("snackbar.settings_java_selected", runtime.name());
            });
            runtimes.getItems().add(item);
        }

        M3SettingItem addJava = actionSetting(
                "settings.java.add",
                "settings.java.add.support",
                "snackbar.settings_add_java"
        );
        M3SettingItem downloadJava = actionSetting(
                "settings.java.download",
                "settings.java.download.support",
                "snackbar.settings_download_java"
        );
        M3SettingItem disabledJava = actionSetting(
                "settings.java.disabled",
                "settings.java.disabled.support",
                "snackbar.settings_disabled_java"
        );
        M3SettingItem reveal = actionSetting(
                "settings.java.reveal",
                "settings.java.reveal.support",
                "snackbar.settings_reveal_java"
        );

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().addAll(
                sectionBlock(strings.get("settings.java.section.runtimes"), runtimes),
                sectionBlock(strings.get("settings.java.section.manage"), addJava, downloadJava, disabledJava, reveal)
        );
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
    }

    /// Creates the general launcher settings form.
    ///
    /// @return the content node
    private Node generalContent() {
        M3SelectSettingItem<String> channel = selectSetting(
                "settings.general.update_channel",
                CHANNEL_OPTIONS,
                state.getUpdateChannel(),
                this::channelLabel,
                value -> {
                    state.setUpdateChannel(value);
                    saved();
                }
        );

        M3SwitchSettingItem acceptPreview = switchSetting(
                "settings.general.accept_preview",
                "settings.general.accept_preview.support",
                state.isAcceptPreviewUpdate(),
                selected -> {
                    state.setAcceptPreviewUpdate(selected);
                    saved();
                }
        );

        M3SwitchSettingItem disableUpdateDialog = switchSetting(
                "settings.general.disable_update_dialog",
                "settings.general.disable_update_dialog.support",
                state.isDisableAutoShowUpdateDialog(),
                selected -> {
                    state.setDisableAutoShowUpdateDialog(selected);
                    saved();
                }
        );

        M3SettingItem checkUpdates = actionSetting(
                "settings.general.check_updates",
                "settings.general.check_updates.support",
                "snackbar.settings_check_updates"
        );

        M3SelectSettingItem<Locale> language = selectSetting(
                "settings.general.language",
                LANGUAGE_OPTIONS,
                state.getLanguage(),
                this::languageLabel,
                value -> {
                    state.setLanguage(value);
                    saved();
                }
        );
        language.setSupportingText(strings.get("settings.general.language.support"));

        M3SwitchSettingItem aprilFools = switchSetting(
                "settings.general.disable_april_fools",
                "settings.general.disable_april_fools.support",
                state.isDisableAprilFools(),
                selected -> {
                    state.setDisableAprilFools(selected);
                    saved();
                }
        );

        M3SettingItem revealLogs = actionSetting(
                "settings.general.reveal_logs",
                "settings.general.reveal_logs.support",
                "snackbar.settings_reveal_logs"
        );
        M3SettingItem exportLogs = actionSetting(
                "settings.general.export_logs",
                "settings.general.export_logs.support",
                "snackbar.settings_export_logs"
        );

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().addAll(
                sectionBlock(strings.get("settings.general.section.update"),
                        channel, acceptPreview, disableUpdateDialog, checkUpdates),
                sectionBlock(strings.get("settings.general.section.language"), language),
                sectionBlock(strings.get("settings.general.section.misc"), aprilFools, revealLogs, exportLogs)
        );
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
    }

    /// Creates the appearance settings form.
    ///
    /// @return the content node
    private Node appearanceContent() {
        Color currentTheme = nearestThemeColor(state.getThemeColor());
        M3SelectSettingItem<Color> theme = selectSetting(
                "settings.appearance.theme",
                THEME_COLORS,
                currentTheme,
                HMCLSettingsView::colorLabel,
                value -> {
                    state.setThemeColor(value);
                    saved();
                }
        );

        M3SettingItem colorStyle = actionSetting(
                "settings.appearance.color_style",
                "settings.appearance.color_style.support",
                "snackbar.settings_color_style"
        );

        M3SelectSettingItem<HMCLDemoState.Brightness> brightness = selectSetting(
                "settings.appearance.brightness",
                BRIGHTNESS_OPTIONS,
                state.getBrightness(),
                this::brightnessLabel,
                value -> {
                    state.setBrightness(value);
                    saved();
                }
        );

        M3SwitchSettingItem titleBar = switchSetting(
                "settings.appearance.title_bar_transparent",
                "settings.appearance.title_bar_transparent.support",
                state.isTitleBarTransparent(),
                selected -> {
                    state.setTitleBarTransparent(selected);
                    saved();
                }
        );

        M3SwitchSettingItem windowTransparent = switchSetting(
                "settings.appearance.window_transparent",
                "settings.appearance.window_transparent.support",
                state.isWindowTransparent(),
                selected -> {
                    state.setWindowTransparent(selected);
                    saved();
                }
        );

        M3SelectSettingItem<Integer> opacity = selectSetting(
                "settings.appearance.background_opacity",
                OPACITY_OPTIONS,
                nearestOption(OPACITY_OPTIONS, state.getBackgroundOpacity()),
                value -> value + "%",
                value -> {
                    state.setBackgroundOpacity(value);
                    saved();
                }
        );

        M3SelectSettingItem<HMCLDemoState.Wallpaper> wallpaper = selectSetting(
                "settings.appearance.wallpaper",
                WALLPAPER_OPTIONS,
                state.getWallpaper(),
                this::wallpaperLabel,
                value -> {
                    state.setWallpaper(value);
                    saved();
                }
        );

        M3SelectSettingItem<String> loadPolicy = selectSetting(
                "settings.appearance.background_load",
                BACKGROUND_LOAD_OPTIONS,
                backgroundLoadPolicy,
                this::backgroundLoadLabel,
                value -> {
                    backgroundLoadPolicy = value;
                    saved();
                }
        );

        M3SwitchSettingItem animation = switchSetting(
                "settings.appearance.animation",
                "settings.appearance.animation.support",
                state.isAnimationDisabled(),
                selected -> {
                    state.setAnimationDisabled(selected);
                    saved();
                }
        );

        M3SwitchSettingItem expressive = switchSetting(
                "settings.appearance.expressive",
                "settings.appearance.expressive.support",
                state.getProfile() == org.glavo.m3fx.tokens.M3Profile.EXPRESSIVE_2025,
                selected -> {
                    state.setProfile(selected
                            ? org.glavo.m3fx.tokens.M3Profile.EXPRESSIVE_2025
                            : org.glavo.m3fx.tokens.M3Profile.BASELINE_2021);
                    saved();
                }
        );

        M3SelectSettingItem<String> fontFamilyItem = selectSetting(
                "settings.appearance.font_family",
                FONT_FAMILY_OPTIONS,
                fontFamily,
                this::fontFamilyLabel,
                value -> {
                    fontFamily = value;
                    saved();
                }
        );

        M3SelectSettingItem<Integer> fontSizeItem = selectSetting(
                "settings.appearance.font_size",
                FONT_SIZE_OPTIONS,
                nearestOption(FONT_SIZE_OPTIONS, fontSize),
                value -> value + " pt",
                value -> {
                    fontSize = value;
                    saved();
                }
        );

        M3SelectSettingItem<String> antialias = selectSetting(
                "settings.appearance.font_antialias",
                FONT_ANTIALIAS_OPTIONS,
                fontAntialias,
                this::fontAntialiasLabel,
                value -> {
                    fontAntialias = value;
                    saved();
                }
        );

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().addAll(
                sectionBlock(strings.get("settings.appearance.section.theme"), theme, colorStyle, expressive),
                sectionBlock(strings.get("settings.appearance.section.appearance"),
                        brightness, titleBar, windowTransparent, opacity),
                sectionBlock(strings.get("settings.appearance.section.background"), wallpaper, loadPolicy),
                sectionBlock(strings.get("settings.appearance.section.animation"), animation),
                sectionBlock(strings.get("settings.appearance.section.fonts"),
                        fontFamilyItem, fontSizeItem, antialias)
        );
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
    }

    /// Creates the download settings form.
    ///
    /// @return the content node
    private Node downloadContent() {
        M3SelectSettingItem<String> versionList = selectSetting(
                "settings.download.version_list_source",
                SOURCE_MODE_OPTIONS,
                state.getVersionListSource(),
                this::sourceModeLabel,
                value -> {
                    state.setVersionListSource(value);
                    saved();
                }
        );

        M3SelectSettingItem<String> fileSource = selectSetting(
                "settings.download.file_source",
                SOURCE_MODE_OPTIONS,
                state.getFileDownloadSource(),
                this::sourceModeLabel,
                value -> {
                    state.setFileDownloadSource(value);
                    saved();
                }
        );

        M3SelectSettingItem<String> addonSource = selectSetting(
                "settings.download.addon_source",
                ADDON_SOURCE_OPTIONS,
                state.getDefaultAddonSource(),
                this::addonSourceLabel,
                value -> {
                    state.setDefaultAddonSource(value);
                    saved();
                }
        );

        M3SelectSettingItem<String> cacheType = selectSetting(
                "settings.download.cache_directory",
                CACHE_TYPE_OPTIONS,
                state.getCacheDirectoryType(),
                this::cacheDirectoryLabel,
                value -> {
                    state.setCacheDirectoryType(value);
                    saved();
                }
        );

        M3SettingItem cleanCache = actionSetting(
                "settings.download.clean_cache",
                "settings.download.clean_cache.support",
                "snackbar.settings_clean_cache"
        );

        M3SelectSettingItem<Integer> threads = selectSetting(
                "settings.download.threads",
                THREAD_OPTIONS,
                nearestOption(THREAD_OPTIONS, state.getDownloadThreads()),
                String::valueOf,
                value -> {
                    state.setDownloadThreads(value);
                    saved();
                }
        );
        threads.setDisable(state.isAutoDownloadThreads());

        M3SwitchSettingItem autoThreads = switchSetting(
                "settings.download.auto_threads",
                "settings.download.auto_threads.support",
                state.isAutoDownloadThreads(),
                selected -> {
                    state.setAutoDownloadThreads(selected);
                    threads.setDisable(selected);
                    saved();
                }
        );

        M3SelectSettingItem<String> proxyType = selectSetting(
                "settings.download.proxy_type",
                PROXY_TYPE_OPTIONS,
                state.getProxyType(),
                this::proxyTypeLabel,
                value -> {
                    state.setProxyType(value);
                    saved();
                }
        );

        M3SelectSettingItem<String> proxyHost = selectSetting(
                "settings.download.proxy_host",
                PROXY_HOST_OPTIONS,
                nearestOption(PROXY_HOST_OPTIONS, state.getProxyHost()),
                value -> value,
                value -> {
                    state.setProxyHost(value);
                    saved();
                }
        );

        M3SelectSettingItem<Integer> proxyPort = selectSetting(
                "settings.download.proxy_port",
                PROXY_PORT_OPTIONS,
                nearestOption(PROXY_PORT_OPTIONS, state.getProxyPort()),
                String::valueOf,
                value -> {
                    state.setProxyPort(value);
                    saved();
                }
        );

        M3SwitchSettingItem proxyAuth = switchSetting(
                "settings.download.proxy_auth",
                "settings.download.proxy_auth.support",
                state.isProxyAuthentication(),
                selected -> {
                    state.setProxyAuthentication(selected);
                    saved();
                }
        );

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().addAll(
                sectionBlock(strings.get("settings.download.section.source"),
                        versionList, fileSource, addonSource),
                sectionBlock(strings.get("settings.download.section.download"),
                        cacheType, cleanCache, autoThreads, threads),
                sectionBlock(strings.get("settings.download.section.proxy"),
                        proxyType, proxyHost, proxyPort, proxyAuth)
        );
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
    }

    /// Creates the help topics form.
    ///
    /// @return the content node
    private Node helpContent() {
        M3SettingItem install = helpTopic("settings.help.install");
        M3SettingItem launch = helpTopic("settings.help.launch");
        M3SettingItem mods = helpTopic("settings.help.mods");
        M3SettingItem multiplayer = helpTopic("settings.help.multiplayer");
        M3SettingItem accounts = helpTopic("settings.help.accounts");
        M3SettingItem faq = helpTopic("settings.help.faq");

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().add(
                sectionBlock(strings.get("settings.help.section.topics"),
                        install, launch, mods, multiplayer, accounts, faq)
        );
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
    }

    /// Creates the feedback channels form.
    ///
    /// @return the content node
    private Node feedbackContent() {
        M3SettingItem qq = actionSetting(
                "settings.feedback.qq",
                "settings.feedback.qq.support",
                "snackbar.settings_feedback_qq"
        );
        M3SettingItem discord = actionSetting(
                "settings.feedback.discord",
                "settings.feedback.discord.support",
                "snackbar.settings_feedback_discord"
        );
        M3SettingItem github = actionSetting(
                "settings.feedback.github",
                "settings.feedback.github.support",
                "snackbar.settings_feedback_github"
        );
        M3SettingItem email = actionSetting(
                "settings.feedback.email",
                "settings.feedback.email.support",
                "snackbar.settings_feedback_email"
        );

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().addAll(
                sectionBlock(strings.get("settings.feedback.section.chat"), qq, discord),
                sectionBlock(strings.get("settings.feedback.section.feedback"), github, email)
        );
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
    }

    /// Creates the about form.
    ///
    /// @return the content node
    private Node aboutContent() {
        M3SettingItem appName = infoSetting(
                "settings.about.app_name",
                strings.get("settings.about.app_name.value")
        );
        M3SettingItem version = infoSetting(
                "settings.about.version",
                strings.get("settings.about.version.value")
        );
        M3SettingItem author = infoSetting(
                "settings.about.author",
                strings.get("settings.about.author.value")
        );

        M3SettingItem thanks1 = infoSetting("settings.about.thanks.huaiyu", strings.get("settings.about.thanks.huaiyu.role"));
        M3SettingItem thanks2 = infoSetting("settings.about.thanks.zkitefly", strings.get("settings.about.thanks.zkitefly.role"));
        M3SettingItem thanks3 = infoSetting("settings.about.thanks.burningtnt", strings.get("settings.about.thanks.burningtnt.role"));
        M3SettingItem thanks4 = infoSetting("settings.about.thanks.glavo", strings.get("settings.about.thanks.glavo.role"));
        M3SettingItem thanks5 = infoSetting("settings.about.thanks.community", strings.get("settings.about.thanks.community.role"));

        M3SettingItem depM3fx = infoSetting("settings.about.dep.m3fx", strings.get("settings.about.dep.m3fx.detail"));
        M3SettingItem depMonet = infoSetting("settings.about.dep.monetfx", strings.get("settings.about.dep.monetfx.detail"));
        M3SettingItem depJfx = infoSetting("settings.about.dep.javafx", strings.get("settings.about.dep.javafx.detail"));
        M3SettingItem depJdk = infoSetting("settings.about.dep.openjdk", strings.get("settings.about.dep.openjdk.detail"));

        M3SettingItem legalM3fx = infoSetting("settings.about.legal.m3fx", strings.get("settings.about.legal.m3fx.detail"));
        M3SettingItem legalHmcl = infoSetting("settings.about.legal.hmcl", strings.get("settings.about.legal.hmcl.detail"));
        M3SettingItem legalMc = infoSetting("settings.about.legal.minecraft", strings.get("settings.about.legal.minecraft.detail"));

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().addAll(
                sectionBlock(strings.get("settings.about.section.about"), appName, version, author),
                sectionBlock(strings.get("settings.about.section.thanks"),
                        thanks1, thanks2, thanks3, thanks4, thanks5),
                sectionBlock(strings.get("settings.about.section.dependencies"),
                        depM3fx, depMonet, depJfx, depJdk),
                sectionBlock(strings.get("settings.about.section.legal"),
                        legalM3fx, legalHmcl, legalMc)
        );
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
    }

    /// Builds a labeled section with a header and a continuous settings group.
    ///
    /// @param title the section header text
    /// @param items the setting rows or a prebuilt list pane
    /// @return the section block
    private VBox sectionBlock(String title, Node... items) {
        M3ListSectionHeader header = new M3ListSectionHeader(title);
        header.getStyleClass().add("hmcl-settings-section-header");
        header.setPadding(new javafx.geometry.Insets(8.0, 16.0, 4.0, 16.0));
        VBox block = new VBox(4.0);
        block.setMinHeight(0.0);
        if (items.length == 1 && items[0] instanceof M3ListPane listPane) {
            listPane.getStyleClass().add("hmcl-settings-group");
            block.getChildren().addAll(header, listPane);
        } else {
            block.getChildren().addAll(header, settingListPane(items));
        }
        return block;
    }

    /// Wraps setting rows in a continuous list group surface.
    ///
    /// @param items the rows
    /// @return the list pane
    private M3ListPane settingListPane(Node... items) {
        M3ListPane list = newList();
        list.getItems().setAll(items);
        return list;
    }

    /// Creates an empty continuous settings list pane.
    ///
    /// @return the list
    private static M3ListPane newList() {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.STANDARD);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().addAll("hmcl-settings-list", "hmcl-settings-group");
        list.setMinHeight(0.0);
        return list;
    }

    /// Creates a dropdown select setting row for a fixed option list.
    ///
    /// @param <T>         the option type
    /// @param headlineKey the headline message key
    /// @param choices     the selectable values
    /// @param value       the current value
    /// @param converter   formats trailing and menu labels
    /// @param onChange    receives the newly selected value
    /// @return the select setting row
    private <T> M3SelectSettingItem<T> selectSetting(
            String headlineKey,
            List<T> choices,
            T value,
            Function<T, String> converter,
            Consumer<T> onChange
    ) {
        M3SelectSettingItem<T> item = new M3SelectSettingItem<>(strings.get(headlineKey));
        item.getItems().setAll(choices);
        item.setConverter(converter);
        item.setValue(value);
        item.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !java.util.Objects.equals(oldValue, newValue)) {
                onChange.accept(newValue);
            }
        });
        return item;
    }

    /// Creates a switch setting row bound to a boolean value.
    ///
    /// @param headlineKey the headline message key
    /// @param supportKey the supporting-text message key
    /// @param selected the initial selected state
    /// @param onToggle the toggle handler
    /// @return the switch row
    private M3SwitchSettingItem switchSetting(
            String headlineKey,
            String supportKey,
            boolean selected,
            java.util.function.Consumer<Boolean> onToggle
    ) {
        M3SwitchSettingItem item = new M3SwitchSettingItem(strings.get(headlineKey));
        item.setSupportingText(strings.get(supportKey));
        item.setSelected(selected);
        item.setOnAction(event -> onToggle.accept(item.isSelected()));
        return item;
    }

    /// Creates an action setting row that shows a snackbar.
    ///
    /// @param headlineKey the headline message key
    /// @param supportKey the supporting-text message key
    /// @param snackbarKey the snackbar message key
    /// @return the setting row
    private M3SettingItem actionSetting(String headlineKey, String supportKey, String snackbarKey) {
        M3SettingItem item = new M3SettingItem(strings.get(headlineKey));
        item.setSupportingText(strings.get(supportKey));
        item.setOnAction(event -> controller.showMessageKey(snackbarKey));
        return item;
    }

    /// Creates a non-mutating informational setting row.
    ///
    /// @param headlineKey the headline message key
    /// @param supporting the supporting text
    /// @return the setting row
    private M3SettingItem infoSetting(String headlineKey, String supporting) {
        M3SettingItem item = new M3SettingItem(strings.get(headlineKey));
        item.setSupportingText(supporting);
        item.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        return item;
    }

    /// Creates a help-topic setting row.
    ///
    /// @param headlineKey the headline message key
    /// @return the setting row
    private M3SettingItem helpTopic(String headlineKey) {
        M3SettingItem item = new M3SettingItem(strings.get(headlineKey));
        item.setSupportingText(strings.get(headlineKey + ".support"));
        item.setOnAction(event -> controller.showMessageKey("snackbar.settings_open_docs"));
        return item;
    }

    /// Shows the generic settings-saved snackbar.
    private void saved() {
        controller.showMessageKey("snackbar.settings_saved");
    }

    /// Returns the localized language label.
    ///
    /// @param locale the locale
    /// @return the label
    private String languageLabel(Locale locale) {
        return locale.equals(HMCLDemoStrings.SIMPLIFIED_CHINESE)
                ? strings.get("settings.language.zh_cn")
                : strings.get("settings.language.en");
    }

    /// Returns the localized update-channel label.
    ///
    /// @param channel the channel id
    /// @return the label
    private String channelLabel(String channel) {
        return "dev".equals(channel)
                ? strings.get("settings.channel.dev")
                : strings.get("settings.channel.stable");
    }

    /// Returns the localized brightness label.
    ///
    /// @param brightness the brightness mode
    /// @return the label
    private String brightnessLabel(HMCLDemoState.Brightness brightness) {
        return strings.get(switch (brightness) {
            case SYSTEM -> "settings.brightness.system";
            case LIGHT -> "settings.brightness.light";
            case DARK -> "settings.brightness.dark";
        });
    }

    /// Returns the localized wallpaper label.
    ///
    /// @param wallpaper the wallpaper
    /// @return the label
    private String wallpaperLabel(HMCLDemoState.Wallpaper wallpaper) {
        return strings.get(switch (wallpaper) {
            case MEADOW -> "settings.wallpaper.meadow";
            case CAVES -> "settings.wallpaper.caves";
            case SUNSET -> "settings.wallpaper.sunset";
        });
    }

    /// Returns the localized background-load-policy label.
    ///
    /// @param policy the policy id
    /// @return the label
    private String backgroundLoadLabel(String policy) {
        return "lazy".equals(policy)
                ? strings.get("settings.background_load.lazy")
                : strings.get("settings.background_load.eager");
    }

    /// Returns the localized font-family label.
    ///
    /// @param family the family id
    /// @return the label
    private String fontFamilyLabel(String family) {
        return strings.get(switch (family) {
            case "sans" -> "settings.font.sans";
            case "serif" -> "settings.font.serif";
            default -> "settings.font.system";
        });
    }

    /// Returns the localized font-antialias label.
    ///
    /// @param mode the antialias mode id
    /// @return the label
    private String fontAntialiasLabel(String mode) {
        return strings.get(switch (mode) {
            case "lcd" -> "settings.font_antialias.lcd";
            case "gray" -> "settings.font_antialias.gray";
            default -> "settings.font_antialias.default";
        });
    }

    /// Returns the localized auto/official/mirror source label.
    ///
    /// @param source the source id
    /// @return the label
    private String sourceModeLabel(String source) {
        return strings.get(switch (source) {
            case "official" -> "settings.download.source.official";
            case "mirror" -> "settings.download.source.mirror";
            default -> "settings.download.source.auto";
        });
    }

    /// Returns the localized addon-source label.
    ///
    /// @param source the source id
    /// @return the label
    private String addonSourceLabel(String source) {
        return "curseforge".equals(source)
                ? strings.get("settings.download.addon.curseforge")
                : strings.get("settings.download.addon.modrinth");
    }

    /// Returns the localized cache-directory label.
    ///
    /// @param type the cache type id
    /// @return the label
    private String cacheDirectoryLabel(String type) {
        return "custom".equals(type)
                ? strings.get("settings.download.cache.custom")
                : strings.get("settings.download.cache.default");
    }

    /// Returns the localized proxy-type label.
    ///
    /// @param type the proxy type id
    /// @return the label
    private String proxyTypeLabel(String type) {
        return strings.get(switch (type) {
            case "none" -> "settings.proxy.none";
            case "http" -> "settings.proxy.http";
            case "socks" -> "settings.proxy.socks";
            default -> "settings.proxy.system";
        });
    }

    /// Returns a compact sRGB hex label.
    ///
    /// @param color the color
    /// @return the hex label
    private static String colorLabel(Color color) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    /// Maps an arbitrary theme color onto the nearest selectable seed.
    ///
    /// @param current the current theme color
    /// @return a color from [#THEME_COLORS]
    private static Color nearestThemeColor(Color current) {
        String label = colorLabel(current);
        for (Color candidate : THEME_COLORS) {
            if (label.equals(colorLabel(candidate))) {
                return candidate;
            }
        }
        return THEME_COLORS.get(0);
    }

    /// Returns `current` when present in `options`, otherwise the first option.
    ///
    /// @param <T>     the option type
    /// @param options the selectable options
    /// @param current the current value
    /// @return a value present in `options`
    private static <T> T nearestOption(List<T> options, T current) {
        if (options.contains(current)) {
            return current;
        }
        return options.get(0);
    }
}
