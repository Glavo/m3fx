// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3RadioButtonSettingItem;
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

import java.util.Locale;

/// Displays representative launcher, appearance, and download settings backed by the dummy state.
@NotNullByDefault
public final class HMCLSettingsView extends HMCLDemoView {
    /// Creates the settings page.
    ///
    /// @param strings the localization source
    /// @param state   the shared demo state
    /// @param actions the application command sink
    public HMCLSettingsView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        initializeView();
    }

    /// Creates the localized settings content.
    ///
    /// @return the settings page tree
    @Override
    protected Node createContent() {
        StackPane contentHost = new StackPane();
        contentHost.setMinWidth(0.0);
        contentHost.setMaxWidth(Double.MAX_VALUE);

        M3Tab general = new M3Tab(text("settings.tab.general"));
        M3Tab appearance = new M3Tab(text("settings.tab.appearance"));
        M3Tab downloads = new M3Tab(text("settings.tab.downloads"));
        general.setSelected(true);

        general.setOnAction(event -> contentHost.getChildren().setAll(createGeneralSettings()));
        appearance.setOnAction(event -> contentHost.getChildren().setAll(createAppearanceSettings()));
        downloads.setOnAction(event -> contentHost.getChildren().setAll(createDownloadSettings()));

        M3TabBar tabs = new M3TabBar();
        tabs.setVariant(M3TabBarVariant.SECONDARY);
        tabs.getTabs().addAll(general, appearance, downloads);
        contentHost.getChildren().setAll(createGeneralSettings());

        return page(
                heading(text("settings.title"), text("settings.subtitle")),
                tabs,
                contentHost
        );
    }

    /// Creates the general launcher settings.
    ///
    /// @return the general settings content
    private Node createGeneralSettings() {
        M3SwitchSettingItem previewUpdates = new M3SwitchSettingItem(text("settings.updates.preview"));
        previewUpdates.setSupportingText(text("settings.updates.preview.supporting"));
        previewUpdates.setSelected(false);
        previewUpdates.setOnAction(event -> actions.dispatch("toggle-preview-updates"));

        M3SwitchSettingItem updateNotices = new M3SwitchSettingItem(text("settings.updates.notices"));
        updateNotices.setSupportingText(text("settings.updates.notices.supporting"));
        updateNotices.setSelected(true);
        updateNotices.setOnAction(event -> actions.dispatch("toggle-update-notices"));

        M3SettingItem logs = new M3SettingItem(text("settings.logs"));
        logs.setSupportingText(text("settings.logs.supporting"));
        logs.setOnAction(event -> actions.dispatch("export-logs"));

        M3ListPane launcher = createList(previewUpdates, updateNotices, logs);

        ToggleGroup languages = new ToggleGroup();
        M3RadioButtonSettingItem english = radio(
                text("settings.language.english"),
                text("settings.language.english.supporting"),
                languages
        );
        M3RadioButtonSettingItem chinese = radio(
                text("settings.language.chinese"),
                text("settings.language.chinese.supporting"),
                languages
        );
        english.setSelected(!"zh".equalsIgnoreCase(state.getLanguage().getLanguage()));
        chinese.setSelected("zh".equalsIgnoreCase(state.getLanguage().getLanguage()));
        english.setOnAction(event -> state.setLanguage(HMCLDemoStrings.ENGLISH));
        chinese.setOnAction(event -> state.setLanguage(HMCLDemoStrings.SIMPLIFIED_CHINESE));

        return new VBox(
                20.0,
                sectionTitle(text("settings.section.launcher")),
                launcher,
                sectionTitle(text("settings.section.language")),
                createList(english, chinese)
        );
    }

    /// Creates the theme and wallpaper settings.
    ///
    /// @return the appearance settings content
    private Node createAppearanceSettings() {
        ToggleGroup brightness = new ToggleGroup();
        M3RadioButtonSettingItem system = brightnessItem(
                "settings.appearance.system",
                HMCLDemoState.Brightness.SYSTEM,
                brightness
        );
        M3RadioButtonSettingItem light = brightnessItem(
                "settings.appearance.light",
                HMCLDemoState.Brightness.LIGHT,
                brightness
        );
        M3RadioButtonSettingItem dark = brightnessItem(
                "settings.appearance.dark",
                HMCLDemoState.Brightness.DARK,
                brightness
        );

        M3Button purple = colorButton("settings.color.purple", Color.web("#6750A4"));
        M3Button green = colorButton("settings.color.green", Color.web("#386A20"));
        M3Button orange = colorButton("settings.color.orange", Color.web("#8B5000"));
        FlowPane colors = flow(purple, green, orange);
        M3Card colorCard = card(
                M3CardVariant.FILLED,
                new M3Text(text("settings.appearance.color"), M3TextRole.TITLE_MEDIUM),
                wrapped(text("settings.appearance.color.supporting")),
                colors
        );

        ToggleGroup wallpapers = new ToggleGroup();
        M3RadioButtonSettingItem meadow = wallpaperItem(
                "settings.wallpaper.meadow",
                HMCLDemoState.Wallpaper.MEADOW,
                wallpapers
        );
        M3RadioButtonSettingItem caves = wallpaperItem(
                "settings.wallpaper.caves",
                HMCLDemoState.Wallpaper.CAVES,
                wallpapers
        );
        M3RadioButtonSettingItem sunset = wallpaperItem(
                "settings.wallpaper.sunset",
                HMCLDemoState.Wallpaper.SUNSET,
                wallpapers
        );

        return new VBox(
                20.0,
                sectionTitle(text("settings.appearance.brightness")),
                createList(system, light, dark),
                colorCard,
                sectionTitle(text("settings.appearance.wallpaper")),
                createList(meadow, caves, sunset)
        );
    }

    /// Creates the representative download settings.
    ///
    /// @return the download settings content
    private Node createDownloadSettings() {
        M3SettingItem source = new M3SettingItem(text("settings.download.source"));
        source.setSupportingText(text("settings.download.source.value"));
        source.setOnAction(event -> actions.dispatch("choose-download-source"));

        M3SettingItem cache = new M3SettingItem(text("settings.download.cache"));
        cache.setSupportingText(text("settings.download.cache.value"));
        cache.setOnAction(event -> actions.dispatch("clear-download-cache"));

        M3SwitchSettingItem proxy = new M3SwitchSettingItem(text("settings.download.proxy"));
        proxy.setSupportingText(text("settings.download.proxy.supporting"));
        proxy.setSelected(false);
        proxy.setOnAction(event -> actions.dispatch("toggle-proxy"));

        M3SwitchSettingItem verify = new M3SwitchSettingItem(text("settings.download.verify"));
        verify.setSupportingText(text("settings.download.verify.supporting"));
        verify.setSelected(true);
        verify.setOnAction(event -> actions.dispatch("toggle-download-verification"));

        M3Text threadValue = new M3Text(text("settings.download.threads.value", 8), M3TextRole.BODY_MEDIUM);
        M3Slider threads = new M3Slider(1.0, 16.0, 8.0);
        threads.valueProperty().addListener((observable, oldValue, newValue) ->
                threadValue.setText(text("settings.download.threads.value", Math.round(newValue.doubleValue()))));
        M3Card threadCard = card(
                M3CardVariant.FILLED,
                new M3Text(text("settings.download.threads"), M3TextRole.TITLE_MEDIUM),
                threadValue,
                threads
        );

        return new VBox(
                20.0,
                sectionTitle(text("settings.section.downloads")),
                createList(source, cache, proxy, verify),
                threadCard
        );
    }

    /// Creates a brightness choice bound to the shared state.
    ///
    /// @param key         the localized title key
    /// @param value       the represented brightness value
    /// @param toggleGroup the owning toggle group
    /// @return the configured setting row
    private M3RadioButtonSettingItem brightnessItem(
            String key,
            HMCLDemoState.Brightness value,
            ToggleGroup toggleGroup
    ) {
        M3RadioButtonSettingItem item = radio(
                text(key),
                text(key + ".supporting"),
                toggleGroup
        );
        item.setSelected(state.getBrightness() == value);
        item.setOnAction(event -> {
            state.setBrightness(value);
            actions.dispatch("brightness", value.name().toLowerCase(Locale.ROOT));
        });
        return item;
    }

    /// Creates a wallpaper choice bound to the shared state.
    ///
    /// @param key         the localized title key
    /// @param value       the represented wallpaper
    /// @param toggleGroup the owning toggle group
    /// @return the configured setting row
    private M3RadioButtonSettingItem wallpaperItem(
            String key,
            HMCLDemoState.Wallpaper value,
            ToggleGroup toggleGroup
    ) {
        M3RadioButtonSettingItem item = radio(
                text(key),
                text(key + ".supporting"),
                toggleGroup
        );
        item.setSelected(state.getWallpaper() == value);
        item.setOnAction(event -> {
            state.setWallpaper(value);
            actions.dispatch("wallpaper", value.name().toLowerCase(Locale.ROOT));
        });
        return item;
    }

    /// Creates a theme-color action button.
    ///
    /// @param key   the localized color-name key
    /// @param color the represented seed color
    /// @return the configured button
    private M3Button colorButton(String key, Color color) {
        M3Button button = new M3Button(text(key), M3ButtonVariant.TONAL);
        button.setOnAction(event -> {
            state.setThemeColor(color);
            actions.dispatch("theme-color", color.toString());
        });
        return button;
    }

    /// Creates one radio setting row.
    ///
    /// @param headline   the headline text
    /// @param supporting the supporting text
    /// @param group      the owning toggle group
    /// @return the configured row
    private static M3RadioButtonSettingItem radio(
            String headline,
            String supporting,
            ToggleGroup group
    ) {
        M3RadioButtonSettingItem item = new M3RadioButtonSettingItem(headline);
        item.setSupportingText(supporting);
        item.setToggleGroup(group);
        return item;
    }

    /// Creates a non-selecting segmented list.
    ///
    /// @param items the list rows
    /// @return the configured list
    private static M3ListPane createList(Node... items) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getItems().addAll(items);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }

    /// Creates wrapping supporting text.
    ///
    /// @param value the localized value
    /// @return the configured text node
    private static M3Text wrapped(String value) {
        M3Text text = new M3Text(value, M3TextRole.BODY_MEDIUM);
        text.setWrapText(true);
        return text;
    }
}
