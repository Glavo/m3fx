// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import org.glavo.m3fx.controls.M3CheckBoxSettingItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3RadioButtonSettingItem;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Settings rows showcase page.
@NotNullByDefault
final class SettingsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SettingsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the settings rows demo page.
    ///
    /// @return the settings rows page content
    Node createContent() {
        M3SettingItem account = new M3SettingItem("Account");
        account.setSupportingText("Profile, security, and linked devices");
        account.setLeading(createSurfaceVariantIcon("person"));
        account.setTrailing(createSurfaceVariantIcon("chevron-right"));
        account.setOnAction(event -> context.showSnackbar("Account settings opened"));

        M3SettingItem storage = new M3SettingItem("Storage");
        storage.setSupportingText("Review local files and cloud backups");
        storage.setLeading(createSurfaceVariantIcon("archive"));
        storage.setTrailing(createSurfaceVariantIcon("chevron-right"));
        storage.setOnAction(event -> context.showSnackbar("Storage settings opened"));

        M3ListPane actionRows = createSettingsPane(M3ListStyle.STANDARD, account, storage);

        M3SwitchSettingItem automaticUpdates = new M3SwitchSettingItem("Automatic updates");
        automaticUpdates.setSupportingText("Install feature updates when the device is idle");
        automaticUpdates.setLeading(createSurfaceVariantIcon("archive"));
        automaticUpdates.setSelected(true);
        automaticUpdates.setOnAction(event -> context.showSnackbar(
                automaticUpdates.isSelected() ? "Automatic updates enabled" : "Automatic updates disabled"
        ));

        M3CheckBoxSettingItem mobileData = new M3CheckBoxSettingItem("Use mobile data");
        mobileData.setSupportingText("Allow update downloads away from Wi-Fi");
        mobileData.setLeading(createSurfaceVariantIcon("settings"));
        mobileData.setAllowIndeterminate(true);
        mobileData.setOnAction(event -> context.showSnackbar(
                mobileData.isIndeterminate() ? "Mobile data follows network policy"
                        : mobileData.isSelected() ? "Mobile data enabled" : "Mobile data disabled"
        ));

        M3CheckBoxSettingItem diagnosticReports = new M3CheckBoxSettingItem("Send diagnostic reports");
        diagnosticReports.setSupportingText("Help improve reliability with anonymous reports");
        diagnosticReports.setLeading(createSurfaceVariantIcon("info"));
        diagnosticReports.setDisable(true);
        diagnosticReports.setIndeterminate(true);

        M3ListPane toggleRows = createSettingsPane(
                M3ListStyle.SEGMENTED,
                automaticUpdates,
                mobileData,
                diagnosticReports
        );

        ToggleGroup themeGroup = new ToggleGroup();
        M3RadioButtonSettingItem systemTheme = createThemeSetting("Use system theme", "Match the operating system", themeGroup);
        M3RadioButtonSettingItem lightTheme = createThemeSetting("Light", "Always use the light color scheme", themeGroup);
        M3RadioButtonSettingItem darkTheme = createThemeSetting("Dark", "Always use the dark color scheme", themeGroup);
        systemTheme.setSelected(true);

        M3ListPane choiceRows = createSettingsPane(M3ListStyle.SEGMENTED, systemTheme, lightTheme, darkTheme);

        return createGallery(
                createFullWidthShowcaseGroup("Action Settings", actionRows),
                createFullWidthShowcaseGroup("Toggle Settings", toggleRows),
                createFullWidthShowcaseGroup("Single Choice", choiceRows)
        );
    }

    /// Creates one theme radio setting row.
    ///
    /// @param headlineText   the primary row text
    /// @param supportingText the explanatory row text
    /// @param toggleGroup    the group coordinating the theme choice
    /// @return the configured radio setting row
    private M3RadioButtonSettingItem createThemeSetting(
            String headlineText,
            String supportingText,
            ToggleGroup toggleGroup
    ) {
        M3RadioButtonSettingItem item = new M3RadioButtonSettingItem(headlineText);
        item.setSupportingText(supportingText);
        item.setLeading(createSurfaceVariantIcon("palette"));
        item.setToggleGroup(toggleGroup);
        item.setOnAction(event -> context.showSnackbar(headlineText + " selected"));
        return item;
    }

    /// Creates a settings list pane with list-managed selection disabled.
    ///
    /// Setting rows own their own boolean or radio state, so the enclosing pane must only provide layout and focus
    /// traversal rather than a second selection model.
    ///
    /// @param listStyle the list container style
    /// @param items     the rows to display
    /// @return the configured settings list pane
    private static M3ListPane createSettingsPane(M3ListStyle listStyle, Node... items) {
        M3ListPane listPane = new M3ListPane();
        listPane.getStyleClass().add("demo-list");
        listPane.setListStyle(listStyle);
        listPane.setSelectionMode(M3SelectionMode.NONE);
        listPane.getItems().addAll(items);
        return configureResponsiveWidth(listPane, 620.0);
    }
}