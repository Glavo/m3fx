// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.scene.Node;
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
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Locale;

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

    /// Default memory steps cycled by the global memory row.
    private static final int @Unmodifiable [] MEMORY_STEPS_MB = {2048, 4096, 8192, 12288};

    /// Default resolution labels cycled by the global resolution row.
    private static final String @Unmodifiable [] RESOLUTION_STEPS = {
            "854x480", "1280x720", "1920x1080", "2560x1440"
    };

    /// Isolation policy ids cycled by the isolation row.
    private static final String @Unmodifiable [] ISOLATION_STEPS = {"never", "always", "modded"};

    /// Launcher visibility policy ids.
    private static final String @Unmodifiable [] VISIBILITY_STEPS = {"hide", "keep", "close"};

    /// Process priority ids.
    private static final String @Unmodifiable [] PRIORITY_STEPS = {"high", "normal", "low"};

    /// Proxy host presets cycled by the host row.
    private static final String @Unmodifiable [] PROXY_HOST_STEPS = {
            "127.0.0.1", "localhost", "proxy.example.com"
    };

    /// Proxy port presets cycled by the port row.
    private static final int @Unmodifiable [] PROXY_PORT_STEPS = {7890, 1080, 8080, 3128};

    /// Concurrent thread counts cycled by the download-threads row.
    private static final int @Unmodifiable [] THREAD_STEPS = {16, 32, 64, 128};

    /// Background opacity percentages.
    private static final int @Unmodifiable [] OPACITY_STEPS = {100, 80, 60, 40};

    /// Font size steps in points.
    private static final int @Unmodifiable [] FONT_SIZE_STEPS = {12, 13, 14, 16};

    /// Theme seed colors cycled by the theme-color row.
    private static final Color @Unmodifiable [] THEME_COLORS = {
            Color.web("#5C6BC0"),
            Color.web("#00897B"),
            Color.web("#FB8C00"),
            Color.web("#8E24AA")
    };

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
    private final M3ListItem globalGameItem = HMCLDemoUi.navItem("", HMCLDemoIcons.SETTINGS, null);
    private final M3ListItem javaItem = HMCLDemoUi.navItem("", HMCLDemoIcons.CODE, null);
    private final M3ListItem generalItem = HMCLDemoUi.navItem("", HMCLDemoIcons.SETTINGS, null);
    private final M3ListItem appearanceItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);
    private final M3ListItem downloadItem = HMCLDemoUi.navItem("", HMCLDemoIcons.DOWNLOAD, null);
    private final M3ListItem helpItem = HMCLDemoUi.navItem("", HMCLDemoIcons.HELP, null);
    private final M3ListItem feedbackItem = HMCLDemoUi.navItem("", HMCLDemoIcons.CHAT, null);
    private final M3ListItem aboutItem = HMCLDemoUi.navItem("", HMCLDemoIcons.INFO, null);

    /// The animated center content host.
    private final M3AnimatedContent contentHost = new M3AnimatedContent();

    /// The active section.
    private Section section = Section.GENERAL;

    /// Process priority for launched game processes.
    private String processPriority = "normal";

    /// Whether the game log window is shown while running.
    private boolean showLogs;

    /// Whether debug-level logging is enabled.
    private boolean debugLog;

    /// Whether integrity checks are skipped before launch.
    private boolean skipIntegrityCheck;

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
    /// @param strings the localization source
    /// @param state the shared state
    /// @param controller the application controller
    HMCLSettingsView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoController controller) {
        this.strings = strings;
        this.state = state;
        this.controller = controller;

        getStyleClass().add("hmcl-secondary-page");
        HMCLDemoUi.fill(this);
        HMCLDemoUi.fill(contentHost);
        contentHost.setFitToWidth(true);
        contentHost.setFitToHeight(true);
        contentHost.setContentTransform(HMCLDemoTransitions.sectionFade());

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
        setLeft(sidebar);
        setCenter(contentHost);

        state.globalMaxMemoryMbProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GLOBAL_GAME));
        state.globalResolutionProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GLOBAL_GAME));
        state.launcherVisibilityProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GLOBAL_GAME));
        state.defaultIsolationProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GLOBAL_GAME));
        state.autoAllocateMemoryProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GLOBAL_GAME));
        state.selectedJavaIdProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.JAVA));
        state.updateChannelProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GENERAL));
        state.acceptPreviewUpdateProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GENERAL));
        state.disableAutoShowUpdateDialogProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GENERAL));
        state.disableAprilFoolsProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GENERAL));
        state.languageProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.GENERAL));
        state.brightnessProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.APPEARANCE));
        state.themeColorProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.APPEARANCE));
        state.wallpaperProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.APPEARANCE));
        state.animationDisabledProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.APPEARANCE));
        state.titleBarTransparentProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.APPEARANCE));
        state.windowTransparentProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.APPEARANCE));
        state.backgroundOpacityProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.APPEARANCE));
        state.versionListSourceProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.fileDownloadSourceProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.defaultAddonSourceProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.downloadSourceProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.downloadThreadsProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.autoDownloadThreadsProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.cacheDirectoryTypeProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.proxyTypeProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.proxyHostProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.proxyPortProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));
        state.proxyAuthenticationProperty().addListener((observable, oldValue, newValue) -> refreshIf(Section.DOWNLOAD));

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

    /// Rebuilds the active section when it matches `target`.
    ///
    /// @param target the section that owns the changed property
    private void refreshIf(Section target) {
        if (section == target) {
            renderSection(false);
        }
    }

    /// Selects a settings section and updates navigation selection.
    ///
    /// @param next the section
    private void showSection(Section next) {
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
        renderSection(changed);
    }

    /// Rebuilds the center content for the active section.
    ///
    /// @param animate whether to animate the section replacement
    private void renderSection(boolean animate) {
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
    }

    /// Creates the global game settings form.
    ///
    /// @return the content node
    private Node globalGameContent() {
        M3SettingItem isolation = cycleSetting(
                "settings.global.isolation",
                isolationLabel(state.getDefaultIsolation()),
                () -> {
                    state.setDefaultIsolation(cycleString(ISOLATION_STEPS, state.getDefaultIsolation()));
                    saved();
                }
        );

        M3SwitchSettingItem autoMemory = switchSetting(
                "settings.general.auto_memory",
                "settings.general.auto_memory.support",
                state.isAutoAllocateMemory(),
                selected -> {
                    state.setAutoAllocateMemory(selected);
                    saved();
                }
        );

        M3SettingItem memory = cycleSetting(
                "settings.global.memory",
                state.getGlobalMaxMemoryMb() + " MB · " + strings.get("settings.global.memory.support"),
                () -> {
                    state.setGlobalMaxMemoryMb(cycleInt(MEMORY_STEPS_MB, state.getGlobalMaxMemoryMb()));
                    saved();
                }
        );

        M3SettingItem resolution = cycleSetting(
                "settings.global.resolution",
                state.getGlobalResolution() + " · " + strings.get("settings.global.resolution.support"),
                () -> {
                    state.setGlobalResolution(cycleString(RESOLUTION_STEPS, state.getGlobalResolution()));
                    saved();
                }
        );

        M3SettingItem visibility = cycleSetting(
                "settings.global.launcher_visibility",
                visibilityLabel(state.getLauncherVisibility()),
                () -> {
                    state.setLauncherVisibility(cycleString(VISIBILITY_STEPS, state.getLauncherVisibility()));
                    saved();
                }
        );

        M3SettingItem priority = cycleSetting(
                "settings.global.process_priority",
                priorityLabel(processPriority),
                () -> {
                    processPriority = cycleString(PRIORITY_STEPS, processPriority);
                    saved();
                    renderSection(false);
                }
        );

        M3SwitchSettingItem showLogsItem = switchSetting(
                "settings.global.show_logs",
                "settings.global.show_logs.support",
                showLogs,
                selected -> {
                    showLogs = selected;
                    saved();
                }
        );

        M3SwitchSettingItem debugLogItem = switchSetting(
                "settings.global.debug_log",
                "settings.global.debug_log.support",
                debugLog,
                selected -> {
                    debugLog = selected;
                    saved();
                }
        );

        M3SwitchSettingItem skipIntegrity = switchSetting(
                "settings.global.skip_integrity",
                "settings.global.skip_integrity.support",
                skipIntegrityCheck,
                selected -> {
                    skipIntegrityCheck = selected;
                    saved();
                }
        );

        M3SettingItem jvmArgs = actionSetting(
                "settings.global.jvm_args",
                "settings.global.jvm_args.support",
                "snackbar.settings_jvm_args"
        );
        M3SettingItem wrapper = actionSetting(
                "settings.global.wrapper",
                "settings.global.wrapper.support",
                "snackbar.settings_wrapper"
        );
        M3SettingItem preLaunch = actionSetting(
                "settings.global.pre_launch",
                "settings.global.pre_launch.support",
                "snackbar.settings_pre_launch"
        );
        M3SettingItem postExit = actionSetting(
                "settings.global.post_exit",
                "settings.global.post_exit.support",
                "snackbar.settings_post_exit"
        );

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().addAll(
                sectionBlock(strings.get("settings.global.section.basic"), isolation, autoMemory),
                sectionBlock(strings.get("settings.global.section.game"), memory, resolution, visibility, priority),
                sectionBlock(strings.get("settings.global.section.launcher"), showLogsItem, debugLogItem, skipIntegrity),
                sectionBlock(strings.get("settings.global.section.advanced"), jvmArgs, wrapper, preLaunch, postExit)
        );
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(root));
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
        M3SettingItem channel = cycleSetting(
                "settings.general.update_channel",
                channelLabel(state.getUpdateChannel()),
                () -> {
                    state.setUpdateChannel("stable".equals(state.getUpdateChannel()) ? "dev" : "stable");
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

        M3SettingItem language = cycleSetting(
                "settings.general.language",
                languageLabel(state.getLanguage()) + " · " + strings.get("settings.general.language.support"),
                () -> {
                    Locale next = state.getLanguage().equals(HMCLDemoStrings.SIMPLIFIED_CHINESE)
                            ? HMCLDemoStrings.ENGLISH
                            : HMCLDemoStrings.SIMPLIFIED_CHINESE;
                    state.setLanguage(next);
                    saved();
                }
        );

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
        M3SettingItem theme = cycleSetting(
                "settings.appearance.theme",
                colorLabel(state.getThemeColor()),
                () -> {
                    state.setThemeColor(nextThemeColor(state.getThemeColor()));
                    saved();
                }
        );

        M3SettingItem colorStyle = actionSetting(
                "settings.appearance.color_style",
                "settings.appearance.color_style.support",
                "snackbar.settings_color_style"
        );

        M3SettingItem brightness = cycleSetting(
                "settings.appearance.brightness",
                brightnessLabel(state.getBrightness()),
                () -> {
                    HMCLDemoState.Brightness next = switch (state.getBrightness()) {
                        case LIGHT -> HMCLDemoState.Brightness.DARK;
                        case DARK -> HMCLDemoState.Brightness.SYSTEM;
                        case SYSTEM -> HMCLDemoState.Brightness.LIGHT;
                    };
                    state.setBrightness(next);
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

        M3SettingItem opacity = cycleSetting(
                "settings.appearance.background_opacity",
                state.getBackgroundOpacity() + "%",
                () -> {
                    state.setBackgroundOpacity(cycleInt(OPACITY_STEPS, state.getBackgroundOpacity()));
                    saved();
                }
        );

        M3SettingItem wallpaper = cycleSetting(
                "settings.appearance.wallpaper",
                wallpaperLabel(state.getWallpaper()),
                () -> {
                    HMCLDemoState.Wallpaper next = switch (state.getWallpaper()) {
                        case MEADOW -> HMCLDemoState.Wallpaper.CAVES;
                        case CAVES -> HMCLDemoState.Wallpaper.SUNSET;
                        case SUNSET -> HMCLDemoState.Wallpaper.MEADOW;
                    };
                    state.setWallpaper(next);
                    saved();
                }
        );

        M3SettingItem loadPolicy = cycleSetting(
                "settings.appearance.background_load",
                backgroundLoadLabel(backgroundLoadPolicy),
                () -> {
                    backgroundLoadPolicy = "eager".equals(backgroundLoadPolicy) ? "lazy" : "eager";
                    saved();
                    renderSection(false);
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

        M3SettingItem fontFamilyItem = cycleSetting(
                "settings.appearance.font_family",
                fontFamilyLabel(fontFamily),
                () -> {
                    fontFamily = switch (fontFamily) {
                        case "sans" -> "serif";
                        case "serif" -> "system";
                        default -> "sans";
                    };
                    saved();
                    renderSection(false);
                }
        );

        M3SettingItem fontSizeItem = cycleSetting(
                "settings.appearance.font_size",
                fontSize + " pt",
                () -> {
                    fontSize = cycleInt(FONT_SIZE_STEPS, fontSize);
                    saved();
                    renderSection(false);
                }
        );

        M3SettingItem antialias = cycleSetting(
                "settings.appearance.font_antialias",
                fontAntialiasLabel(fontAntialias),
                () -> {
                    fontAntialias = switch (fontAntialias) {
                        case "lcd" -> "gray";
                        case "gray" -> "default";
                        default -> "lcd";
                    };
                    saved();
                    renderSection(false);
                }
        );

        VBox root = new VBox(16.0);
        root.setMinHeight(0.0);
        root.getChildren().addAll(
                sectionBlock(strings.get("settings.appearance.section.theme"), theme, colorStyle),
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
        M3SettingItem versionList = cycleSetting(
                "settings.download.version_list_source",
                sourceModeLabel(state.getVersionListSource()),
                () -> {
                    state.setVersionListSource(cycleSourceMode(state.getVersionListSource()));
                    saved();
                }
        );

        M3SettingItem fileSource = cycleSetting(
                "settings.download.file_source",
                sourceModeLabel(state.getFileDownloadSource()),
                () -> {
                    state.setFileDownloadSource(cycleSourceMode(state.getFileDownloadSource()));
                    saved();
                }
        );

        M3SettingItem addonSource = cycleSetting(
                "settings.download.addon_source",
                addonSourceLabel(state.getDefaultAddonSource()),
                () -> {
                    state.setDefaultAddonSource(
                            "modrinth".equals(state.getDefaultAddonSource()) ? "curseforge" : "modrinth");
                    saved();
                }
        );

        M3SettingItem cacheType = cycleSetting(
                "settings.download.cache_directory",
                cacheDirectoryLabel(state.getCacheDirectoryType()),
                () -> {
                    state.setCacheDirectoryType(
                            "default".equals(state.getCacheDirectoryType()) ? "custom" : "default");
                    saved();
                }
        );

        M3SettingItem cleanCache = actionSetting(
                "settings.download.clean_cache",
                "settings.download.clean_cache.support",
                "snackbar.settings_clean_cache"
        );

        M3SwitchSettingItem autoThreads = switchSetting(
                "settings.download.auto_threads",
                "settings.download.auto_threads.support",
                state.isAutoDownloadThreads(),
                selected -> {
                    state.setAutoDownloadThreads(selected);
                    saved();
                }
        );

        M3SettingItem threads = cycleSetting(
                "settings.download.threads",
                String.valueOf(state.getDownloadThreads()),
                () -> {
                    state.setDownloadThreads(cycleInt(THREAD_STEPS, state.getDownloadThreads()));
                    saved();
                }
        );
        threads.setDisable(state.isAutoDownloadThreads());

        M3SettingItem proxyType = cycleSetting(
                "settings.download.proxy_type",
                proxyTypeLabel(state.getProxyType()),
                () -> {
                    state.setProxyType(cycleProxyType(state.getProxyType()));
                    saved();
                }
        );

        M3SettingItem proxyHost = cycleSetting(
                "settings.download.proxy_host",
                state.getProxyHost(),
                () -> {
                    state.setProxyHost(cycleString(PROXY_HOST_STEPS, state.getProxyHost()));
                    saved();
                }
        );

        M3SettingItem proxyPort = cycleSetting(
                "settings.download.proxy_port",
                String.valueOf(state.getProxyPort()),
                () -> {
                    state.setProxyPort(cycleInt(PROXY_PORT_STEPS, state.getProxyPort()));
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

    /// Builds a labeled section with a header and a segmented list pane of setting rows.
    ///
    /// @param title the section header text
    /// @param items the setting rows or a prebuilt list pane
    /// @return the section block
    private VBox sectionBlock(String title, Node... items) {
        M3ListSectionHeader header = new M3ListSectionHeader(title);
        VBox block = new VBox(8.0);
        block.setMinHeight(0.0);
        if (items.length == 1 && items[0] instanceof M3ListPane listPane) {
            block.getChildren().addAll(header, listPane);
        } else {
            block.getChildren().addAll(header, settingListPane(items));
        }
        return block;
    }

    /// Wraps setting rows in a segmented list pane.
    ///
    /// @param items the rows
    /// @return the list pane
    private M3ListPane settingListPane(Node... items) {
        M3ListPane list = newList();
        list.getItems().setAll(items);
        return list;
    }

    /// Creates an empty segmented list pane.
    ///
    /// @return the list
    private static M3ListPane newList() {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getStyleClass().add("hmcl-dense-list");
        list.setMinHeight(0.0);
        return list;
    }

    /// Creates a cycling action setting row.
    ///
    /// @param headlineKey the headline message key
    /// @param supporting the supporting text
    /// @param action the activation handler
    /// @return the setting row
    private M3SettingItem cycleSetting(String headlineKey, String supporting, Runnable action) {
        M3SettingItem item = new M3SettingItem(strings.get(headlineKey));
        item.setSupportingText(supporting);
        item.setOnAction(event -> action.run());
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

    /// Returns the localized isolation label.
    ///
    /// @param isolation the isolation policy id
    /// @return the label
    private String isolationLabel(String isolation) {
        return strings.get(switch (isolation) {
            case "never" -> "settings.isolation.never";
            case "always" -> "settings.isolation.always";
            default -> "settings.isolation.modded";
        });
    }

    /// Returns the localized launcher-visibility label.
    ///
    /// @param visibility the visibility policy id
    /// @return the label
    private String visibilityLabel(String visibility) {
        return strings.get(switch (visibility) {
            case "keep" -> "settings.visibility.keep";
            case "close" -> "settings.visibility.close";
            default -> "settings.visibility.hide";
        });
    }

    /// Returns the localized process-priority label.
    ///
    /// @param priority the priority id
    /// @return the label
    private String priorityLabel(String priority) {
        return strings.get(switch (priority) {
            case "high" -> "settings.priority.high";
            case "low" -> "settings.priority.low";
            default -> "settings.priority.normal";
        });
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

    /// Cycles auto → official → mirror → auto.
    ///
    /// @param current the current source id
    /// @return the next source id
    private static String cycleSourceMode(String current) {
        return switch (current) {
            case "official" -> "mirror";
            case "mirror" -> "auto";
            default -> "official";
        };
    }

    /// Cycles system → none → http → socks → system.
    ///
    /// @param current the current proxy type
    /// @return the next proxy type
    private static String cycleProxyType(String current) {
        return switch (current) {
            case "none" -> "http";
            case "http" -> "socks";
            case "socks" -> "system";
            default -> "none";
        };
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

    /// Cycles through the demo theme seeds.
    ///
    /// @param current the current color
    /// @return the next color
    private static Color nextThemeColor(Color current) {
        String label = colorLabel(current);
        for (int index = 0; index < THEME_COLORS.length; index++) {
            if (label.equals(colorLabel(THEME_COLORS[index]))) {
                return THEME_COLORS[(index + 1) % THEME_COLORS.length];
            }
        }
        return THEME_COLORS[0];
    }

    /// Returns the next integer step after `current`, wrapping to the first value.
    ///
    /// @param steps the cycle values
    /// @param current the current value
    /// @return the next value
    private static int cycleInt(int[] steps, int current) {
        for (int index = 0; index < steps.length; index++) {
            if (steps[index] == current) {
                return steps[(index + 1) % steps.length];
            }
        }
        return steps[0];
    }

    /// Returns the next string step after `current`, wrapping to the first value.
    ///
    /// @param steps the cycle values
    /// @param current the current value
    /// @return the next value
    private static String cycleString(String[] steps, String current) {
        for (int index = 0; index < steps.length; index++) {
            if (steps[index].equals(current)) {
                return steps[(index + 1) % steps.length];
            }
        }
        return steps[0];
    }
}
