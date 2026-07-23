// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

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

        /// Help placeholder.
        HELP,

        /// Feedback placeholder.
        FEEDBACK,

        /// About placeholder.
        ABOUT
    }

    /// Default memory steps cycled by the global memory row.
    private static final int[] MEMORY_STEPS_MB = {2048, 4096, 8192, 12288};

    /// Default resolution labels cycled by the global resolution row.
    private static final String[] RESOLUTION_STEPS = {"854x480", "1280x720", "1920x1080", "2560x1440"};

    /// Default launcher-visibility labels cycled by the visibility row.
    private static final String[] VISIBILITY_KEYS = {
            "settings.visibility.hide",
            "settings.visibility.keep",
            "settings.visibility.close"
    };

    /// Download source labels cycled by the download-source row.
    private static final String[] DOWNLOAD_SOURCES = {"official", "bmclapi", "mirror"};

    /// Concurrent thread counts cycled by the download-threads row.
    private static final int[] THREAD_STEPS = {16, 32, 64, 128};

    /// Theme seed colors cycled by the theme-color row.
    private static final Color[] THEME_COLORS = {
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

    /// Index into [#RESOLUTION_STEPS].
    private int resolutionIndex = 0;

    /// Index into [#VISIBILITY_KEYS].
    private int visibilityIndex = 0;

    /// Whether new instances default to an isolated working directory.
    private boolean isolationDefault = true;

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

        state.globalMaxMemoryMbProperty().addListener((observable, oldValue, newValue) -> {
            if (section == Section.GLOBAL_GAME) {
                renderSection(false);
            }
        });
        state.updateChannelProperty().addListener((observable, oldValue, newValue) -> {
            if (section == Section.GENERAL) {
                renderSection(false);
            }
        });
        state.downloadSourceProperty().addListener((observable, oldValue, newValue) -> {
            if (section == Section.DOWNLOAD) {
                renderSection(false);
            }
        });
        state.downloadThreadsProperty().addListener((observable, oldValue, newValue) -> {
            if (section == Section.DOWNLOAD) {
                renderSection(false);
            }
        });
        state.brightnessProperty().addListener((observable, oldValue, newValue) -> {
            if (section == Section.APPEARANCE) {
                renderSection(false);
            }
        });
        state.themeColorProperty().addListener((observable, oldValue, newValue) -> {
            if (section == Section.APPEARANCE) {
                renderSection(false);
            }
        });
        state.wallpaperProperty().addListener((observable, oldValue, newValue) -> {
            if (section == Section.APPEARANCE) {
                renderSection(false);
            }
        });

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
            case HELP -> placeholder(strings.get("settings.nav.help"), strings.get("settings.help.body"));
            case FEEDBACK -> placeholder(strings.get("settings.nav.feedback"), strings.get("settings.feedback.body"));
            case ABOUT -> placeholder(strings.get("settings.nav.about"), strings.get("settings.about.body"));
        };
        if (content instanceof Region region) {
            HMCLDemoUi.fill(region);
        }
        contentHost.setContent(content);
        if (!animate) {
            contentHost.snapToCurrentState();
        }
    }

    /// Creates the global game settings list.
    ///
    /// @return the content node
    private Node globalGameContent() {
        M3SettingItem memory = new M3SettingItem(strings.get("settings.global.memory"));
        memory.setSupportingText(state.getGlobalMaxMemoryMb() + " MB · " + strings.get("settings.global.memory.support"));
        memory.setOnAction(event -> {
            state.setGlobalMaxMemoryMb(nextInt(MEMORY_STEPS_MB, state.getGlobalMaxMemoryMb()));
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem resolution = new M3SettingItem(strings.get("settings.global.resolution"));
        resolution.setSupportingText(
                RESOLUTION_STEPS[resolutionIndex] + " · " + strings.get("settings.global.resolution.support"));
        resolution.setOnAction(event -> {
            resolutionIndex = (resolutionIndex + 1) % RESOLUTION_STEPS.length;
            resolution.setSupportingText(
                    RESOLUTION_STEPS[resolutionIndex] + " · " + strings.get("settings.global.resolution.support"));
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem visibility = new M3SettingItem(strings.get("settings.global.launcher_visibility"));
        visibility.setSupportingText(strings.get(VISIBILITY_KEYS[visibilityIndex]));
        visibility.setOnAction(event -> {
            visibilityIndex = (visibilityIndex + 1) % VISIBILITY_KEYS.length;
            visibility.setSupportingText(strings.get(VISIBILITY_KEYS[visibilityIndex]));
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem isolation = new M3SettingItem(strings.get("settings.global.isolation"));
        isolation.setSupportingText(isolationDefault
                ? strings.get("settings.global.isolation.on")
                : strings.get("settings.global.isolation.off"));
        isolation.setOnAction(event -> {
            isolationDefault = !isolationDefault;
            isolation.setSupportingText(isolationDefault
                    ? strings.get("settings.global.isolation.on")
                    : strings.get("settings.global.isolation.off"));
            controller.showMessageKey("snackbar.settings_saved");
        });

        return settingList(memory, resolution, visibility, isolation);
    }

    /// Creates the Java management list from discovered runtimes.
    ///
    /// @return the content node
    private Node javaContent() {
        M3ListPane list = newList();
        for (HMCLDemoJavaRuntime runtime : state.getJavaRuntimes()) {
            M3SettingItem item = new M3SettingItem(runtime.name());
            item.setSupportingText(runtime.version() + " · " + runtime.architecture() + " · " + runtime.path());
            item.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
            list.getItems().add(item);
        }
        M3SettingItem addJava = new M3SettingItem(strings.get("settings.java.add"));
        addJava.setSupportingText(strings.get("settings.java.add.support"));
        addJava.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        list.getItems().add(addJava);
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(list));
    }

    /// Creates the general launcher settings list.
    ///
    /// @return the content node
    private Node generalContent() {
        M3SettingItem language = new M3SettingItem(strings.get("settings.general.language"));
        language.setSupportingText(languageLabel(state.getLanguage()));
        language.setOnAction(event -> {
            Locale next = state.getLanguage().equals(HMCLDemoStrings.SIMPLIFIED_CHINESE)
                    ? HMCLDemoStrings.ENGLISH
                    : HMCLDemoStrings.SIMPLIFIED_CHINESE;
            state.setLanguage(next);
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem updateChannel = new M3SettingItem(strings.get("settings.general.update_channel"));
        updateChannel.setSupportingText(channelLabel(state.getUpdateChannel()));
        updateChannel.setOnAction(event -> {
            String next = "stable".equals(state.getUpdateChannel()) ? "dev" : "stable";
            state.setUpdateChannel(next);
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem fileAssociation = new M3SettingItem(strings.get("settings.general.file_association"));
        fileAssociation.setSupportingText(strings.get("settings.general.file_association.support"));
        fileAssociation.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));

        M3SwitchSettingItem autoMemory = new M3SwitchSettingItem(strings.get("settings.general.auto_memory"));
        autoMemory.setSupportingText(strings.get("settings.general.auto_memory.support"));
        autoMemory.setSelected(state.isAutoAllocateMemory());
        autoMemory.setOnAction(event -> {
            state.setAutoAllocateMemory(autoMemory.isSelected());
            controller.showMessageKey("snackbar.settings_saved");
        });

        return settingList(language, updateChannel, fileAssociation, autoMemory);
    }

    /// Creates the appearance settings list.
    ///
    /// @return the content node
    private Node appearanceContent() {
        M3SettingItem brightness = new M3SettingItem(strings.get("settings.appearance.brightness"));
        brightness.setSupportingText(brightnessLabel(state.getBrightness()));
        brightness.setOnAction(event -> {
            HMCLDemoState.Brightness next = switch (state.getBrightness()) {
                case LIGHT -> HMCLDemoState.Brightness.DARK;
                case DARK -> HMCLDemoState.Brightness.SYSTEM;
                case SYSTEM -> HMCLDemoState.Brightness.LIGHT;
            };
            state.setBrightness(next);
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem theme = new M3SettingItem(strings.get("settings.appearance.theme"));
        theme.setSupportingText(colorLabel(state.getThemeColor()));
        theme.setOnAction(event -> {
            state.setThemeColor(nextThemeColor(state.getThemeColor()));
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem wallpaper = new M3SettingItem(strings.get("settings.appearance.wallpaper"));
        wallpaper.setSupportingText(wallpaperLabel(state.getWallpaper()));
        wallpaper.setOnAction(event -> {
            HMCLDemoState.Wallpaper next = switch (state.getWallpaper()) {
                case MEADOW -> HMCLDemoState.Wallpaper.CAVES;
                case CAVES -> HMCLDemoState.Wallpaper.SUNSET;
                case SUNSET -> HMCLDemoState.Wallpaper.MEADOW;
            };
            state.setWallpaper(next);
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SwitchSettingItem animation = new M3SwitchSettingItem(strings.get("settings.appearance.animation"));
        animation.setSupportingText(strings.get("settings.appearance.animation.support"));
        animation.setSelected(state.isAnimationDisabled());
        animation.setOnAction(event -> {
            state.setAnimationDisabled(animation.isSelected());
            controller.showMessageKey("snackbar.settings_saved");
        });

        return settingList(brightness, theme, wallpaper, animation);
    }

    /// Creates the download settings list.
    ///
    /// @return the content node
    private Node downloadContent() {
        M3SettingItem source = new M3SettingItem(strings.get("settings.download.source"));
        source.setSupportingText(downloadSourceLabel(state.getDownloadSource()));
        source.setOnAction(event -> {
            state.setDownloadSource(nextString(DOWNLOAD_SOURCES, state.getDownloadSource()));
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem threads = new M3SettingItem(strings.get("settings.download.threads"));
        threads.setSupportingText(String.valueOf(state.getDownloadThreads()));
        threads.setOnAction(event -> {
            state.setDownloadThreads(nextInt(THREAD_STEPS, state.getDownloadThreads()));
            controller.showMessageKey("snackbar.settings_saved");
        });

        return settingList(source, threads);
    }

    /// Wraps setting rows in a segmented scrollable list.
    ///
    /// @param items the rows
    /// @return the scroll host
    private Node settingList(Node... items) {
        M3ListPane list = newList();
        list.getItems().setAll(items);
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(list));
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

    /// Creates a longer placeholder panel for help-style sections.
    ///
    /// @param title the title
    /// @param body the body
    /// @return the placeholder
    private Node placeholder(String title, String body) {
        M3Text titleText = new M3Text(title, M3TextRole.TITLE_LARGE);
        M3Text bodyText = new M3Text(body, M3TextRole.BODY_MEDIUM);
        bodyText.setWrapText(true);
        M3Text footnote = new M3Text(strings.get("settings.help.footnote"), M3TextRole.BODY_SMALL);
        footnote.setWrapText(true);
        VBox box = HMCLDemoUi.contentColumn(titleText, bodyText, footnote);
        box.setMaxWidth(640.0);
        return HMCLDemoUi.scroll(box);
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

    /// Returns the localized download-source label.
    ///
    /// @param source the source id
    /// @return the label
    private String downloadSourceLabel(String source) {
        return switch (source) {
            case "bmclapi" -> strings.get("settings.download.source.bmclapi");
            case "mirror" -> strings.get("settings.download.source.mirror");
            default -> strings.get("settings.download.source.official");
        };
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
    private static int nextInt(int[] steps, int current) {
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
    private static String nextString(String[] steps, String current) {
        for (int index = 0; index < steps.length; index++) {
            if (steps[index].equals(current)) {
                return steps[(index + 1) % steps.length];
            }
        }
        return steps[0];
    }
}
