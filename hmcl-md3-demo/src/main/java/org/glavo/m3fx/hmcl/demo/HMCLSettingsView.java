// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Displays launcher settings with HMCL-style section navigation.
@NotNullByDefault
final class HMCLSettingsView extends BorderPane {
    /// Settings sections.
    private enum Section {
        /// Global game settings.
        GLOBAL_GAME,

        /// Java management.
        JAVA,

        /// General launcher settings.
        GENERAL,

        /// Appearance settings.
        APPEARANCE,

        /// Download settings.
        DOWNLOAD,

        /// Help.
        HELP,

        /// Feedback.
        FEEDBACK,

        /// About.
        ABOUT
    }

    /// The localization source.
    private final HMCLDemoStrings strings;

    /// The shared state.
    private final HMCLDemoState state;

    /// The application controller.
    private final HMCLDemoController controller;

    /// Section labels and items.
    private final M3Text gameSection = HMCLDemoUi.sectionLabel("");
    private final M3Text launcherSection = HMCLDemoUi.sectionLabel("");
    private final M3Text helpSection = HMCLDemoUi.sectionLabel("");
    private final M3ListItem globalGameItem = HMCLDemoUi.navItem("", HMCLDemoIcons.SETTINGS, null);
    private final M3ListItem javaItem = HMCLDemoUi.navItem("", HMCLDemoIcons.CODE, null);
    private final M3ListItem generalItem = HMCLDemoUi.navItem("", HMCLDemoIcons.SETTINGS, null);
    private final M3ListItem appearanceItem = HMCLDemoUi.navItem("", HMCLDemoIcons.IMAGE, null);
    private final M3ListItem downloadItem = HMCLDemoUi.navItem("", HMCLDemoIcons.DOWNLOAD, null);
    private final M3ListItem helpItem = HMCLDemoUi.navItem("", HMCLDemoIcons.HELP, null);
    private final M3ListItem feedbackItem = HMCLDemoUi.navItem("", HMCLDemoIcons.CHAT, null);
    private final M3ListItem aboutItem = HMCLDemoUi.navItem("", HMCLDemoIcons.INFO, null);

    /// The center host.
    private final StackPane contentHost = new StackPane();

    /// The active section.
    private Section section = Section.GENERAL;

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
        globalGameItem.setOnAction(event -> showSection(Section.GLOBAL_GAME));
        javaItem.setOnAction(event -> showSection(Section.JAVA));
        generalItem.setOnAction(event -> showSection(Section.GENERAL));
        appearanceItem.setOnAction(event -> showSection(Section.APPEARANCE));
        downloadItem.setOnAction(event -> showSection(Section.DOWNLOAD));
        helpItem.setOnAction(event -> showSection(Section.HELP));
        feedbackItem.setOnAction(event -> showSection(Section.FEEDBACK));
        aboutItem.setOnAction(event -> showSection(Section.ABOUT));

        VBox sidebar = HMCLDemoUi.sidebar(
                gameSection,
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
        refreshLocale();
        showSection(Section.GENERAL);
    }

    /// Updates static labels.
    void refreshLocale() {
        gameSection.setText(strings.get("settings.section.game"));
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
        renderSection();
    }

    /// Selects a settings section.
    ///
    /// @param next the section
    private void showSection(Section next) {
        section = next;
        globalGameItem.setSelected(next == Section.GLOBAL_GAME);
        javaItem.setSelected(next == Section.JAVA);
        generalItem.setSelected(next == Section.GENERAL);
        appearanceItem.setSelected(next == Section.APPEARANCE);
        downloadItem.setSelected(next == Section.DOWNLOAD);
        helpItem.setSelected(next == Section.HELP);
        feedbackItem.setSelected(next == Section.FEEDBACK);
        aboutItem.setSelected(next == Section.ABOUT);
        renderSection();
    }

    /// Rebuilds the center content.
    private void renderSection() {
        contentHost.getChildren().setAll(switch (section) {
            case GLOBAL_GAME -> simpleList(
                    setting(strings.get("settings.global.memory"), strings.get("settings.global.memory.support")),
                    setting(strings.get("settings.global.resolution"),
                            strings.get("settings.global.resolution.support")),
                    setting(strings.get("settings.global.launcher_visibility"),
                            strings.get("settings.global.launcher_visibility.support"))
            );
            case JAVA -> simpleList(
                    setting(strings.get("settings.java.current"), strings.get("settings.java.current.support")),
                    setting(strings.get("settings.java.add"), strings.get("settings.java.add.support"))
            );
            case GENERAL -> generalContent();
            case APPEARANCE -> appearanceContent();
            case DOWNLOAD -> simpleList(
                    setting(strings.get("settings.download.source"), strings.get("settings.download.source.support")),
                    setting(strings.get("settings.download.threads"),
                            strings.get("settings.download.threads.support"))
            );
            case HELP -> placeholder(strings.get("settings.nav.help"), strings.get("settings.help.body"));
            case FEEDBACK -> placeholder(strings.get("settings.nav.feedback"), strings.get("settings.feedback.body"));
            case ABOUT -> placeholder(strings.get("settings.nav.about"), strings.get("settings.about.body"));
        });
    }

    /// Creates the general settings content.
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
            language.setSupportingText(languageLabel(next));
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem updateChannel = setting(
                strings.get("settings.general.update_channel"),
                strings.get("settings.general.update_channel.support"));
        M3SettingItem fileAssociation = setting(
                strings.get("settings.general.file_association"),
                strings.get("settings.general.file_association.support"));
        return simpleList(language, updateChannel, fileAssociation);
    }

    /// Creates the appearance settings content.
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
            brightness.setSupportingText(brightnessLabel(next));
            controller.showMessageKey("snackbar.settings_saved");
        });

        M3SettingItem theme = new M3SettingItem(strings.get("settings.appearance.theme"));
        theme.setSupportingText(colorLabel(state.getThemeColor()));
        theme.setOnAction(event -> {
            Color next = nextThemeColor(state.getThemeColor());
            state.setThemeColor(next);
            theme.setSupportingText(colorLabel(next));
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
            wallpaper.setSupportingText(wallpaperLabel(next));
            controller.showMessageKey("snackbar.settings_saved");
        });
        return simpleList(brightness, theme, wallpaper);
    }

    /// Creates a simple segmented settings list.
    ///
    /// @param items the rows
    /// @return the scrollable list
    private Node simpleList(M3SettingItem... items) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getItems().setAll(items);
        return HMCLDemoUi.scroll(HMCLDemoUi.contentColumn(list));
    }

    /// Creates a placeholder panel.
    ///
    /// @param title the title
    /// @param body the body
    /// @return the placeholder
    private Node placeholder(String title, String body) {
        M3Text titleText = new M3Text(title, M3TextRole.TITLE_LARGE);
        M3Text bodyText = new M3Text(body, M3TextRole.BODY_MEDIUM);
        bodyText.setWrapText(true);
        VBox box = new VBox(12.0, titleText, bodyText);
        box.setPadding(new Insets(24.0));
        box.setMaxWidth(520.0);
        return HMCLDemoUi.scroll(box);
    }

    /// Creates a settings row with a simulated action.
    ///
    /// @param headline the headline
    /// @param supporting the supporting text
    /// @return the row
    private M3SettingItem setting(String headline, String supporting) {
        M3SettingItem item = new M3SettingItem(headline);
        item.setSupportingText(supporting);
        item.setOnAction(event -> controller.showMessageKey("snackbar.action_simulated"));
        return item;
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

    /// Returns a compact color label.
    ///
    /// @param color the color
    /// @return the hex label
    private static String colorLabel(Color color) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    /// Cycles through a small set of HMCL-like theme seeds.
    ///
    /// @param current the current color
    /// @return the next color
    private static Color nextThemeColor(Color current) {
        Color blue = Color.web("#5C6BC0");
        Color teal = Color.web("#00897B");
        Color orange = Color.web("#FB8C00");
        Color purple = Color.web("#8E24AA");
        if (colorsEqual(current, blue)) {
            return teal;
        }
        if (colorsEqual(current, teal)) {
            return orange;
        }
        if (colorsEqual(current, orange)) {
            return purple;
        }
        return blue;
    }

    /// Compares two colors in sRGB 8-bit space.
    ///
    /// @param left the first color
    /// @param right the second color
    /// @return whether both colors match
    private static boolean colorsEqual(Color left, Color right) {
        return colorLabel(left).equals(colorLabel(right));
    }
}
