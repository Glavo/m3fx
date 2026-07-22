// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3ListCell;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3CheckBoxSettingItem;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the List component showcase page.
@NotNullByDefault
final class ListDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ListDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the list component page.
    Node createContent() {
        M3ListItem oneLine = new M3ListItem("One-line item");
        oneLine.setLeading(createSurfaceVariantIcon("inbox"));

        M3ListItem disabled = new M3ListItem("Disabled item");
        disabled.setSupportingText("Unavailable destination");
        disabled.setLeading(createSurfaceVariantIcon("lock"));
        disabled.setDisable(true);

        M3ListItem twoLine = new M3ListItem("Two-line item");
        twoLine.setSupportingText("Supporting text");
        twoLine.setTrailingSupportingText("3 min");
        twoLine.setTrailing(createSurfaceVariantIcon("chevron-right"));

        M3ListItem threeLine = new M3ListItem("Three-line item");
        threeLine.setOverlineText("Overline");
        threeLine.setSupportingText("Supporting text can span a denser row.");
        threeLine.setLeadingAvatar("A");

        M3ListPane standardList = new M3ListPane();
        standardList.getStyleClass().add("demo-list");
        configureResponsiveWidth(standardList, 520.0);
        standardList.setSelectionMode(M3SelectionMode.SINGLE);
        standardList.getItems().addAll(
                new M3ListSectionHeader("Recent"),
                oneLine,
                disabled,
                twoLine,
                threeLine
        );

        M3ListItem thumbnail = new M3ListItem("Thumbnail item");
        thumbnail.setSupportingText("Leading square media and trailing metadata.");
        thumbnail.setLeadingThumbnail(createListThumbnail());
        thumbnail.setTrailingSupportingText("12:40");

        M3ListItem wideThumbnail = new M3ListItem("Wide thumbnail item");
        wideThumbnail.setSupportingText("Media content is clipped to the configured slot size.");
        wideThumbnail.setLeadingMedia(createListThumbnail(), M3ListItemSlotSize.WIDE_THUMBNAIL);
        wideThumbnail.setTrailing(createSurfaceVariantIcon("chevron-right"));

        M3ListItem selected = new M3ListItem("Selected item");
        selected.setSupportingText("Current destination");
        selected.setLeading(createSurfaceVariantIcon("done"));
        selected.setTrailingSupportingText("Now");

        M3ListPane segmentedList = new M3ListPane();
        segmentedList.getStyleClass().add("demo-list");
        configureResponsiveWidth(segmentedList, 520.0);
        segmentedList.setListStyle(M3ListStyle.SEGMENTED);
        segmentedList.setSelectionMode(M3SelectionMode.SINGLE);
        segmentedList.getItems().addAll(thumbnail, wideThumbnail, selected);
        segmentedList.select(selected);

        M3SwitchSettingItem updates = new M3SwitchSettingItem("Automatic updates");
        updates.setSupportingText("Install feature updates when the device is idle");
        updates.setLeading(createSurfaceVariantIcon("archive"));
        updates.setSelected(true);
        updates.setOnAction(event -> context.showSnackbar(
                updates.isSelected() ? "Automatic updates enabled" : "Automatic updates disabled"
        ));

        M3CheckBoxSettingItem mobileData = new M3CheckBoxSettingItem("Use mobile data");
        mobileData.setSupportingText("Allow update downloads away from Wi-Fi");
        mobileData.setLeading(createSurfaceVariantIcon("settings"));
        mobileData.setAllowIndeterminate(true);
        mobileData.setOnAction(event -> context.showSnackbar(
                mobileData.isIndeterminate() ? "Mobile data follows network policy"
                        : mobileData.isSelected() ? "Mobile data enabled" : "Mobile data disabled"
        ));

        M3ListPane settingsList = new M3ListPane();
        settingsList.getStyleClass().add("demo-list");
        configureResponsiveWidth(settingsList, 520.0);
        settingsList.setListStyle(M3ListStyle.SEGMENTED);
        settingsList.setSelectionMode(M3SelectionMode.NONE);
        settingsList.getItems().addAll(updates, mobileData);

        return createGallery(
                createFullWidthShowcaseGroup("Standard", standardList),
                createFullWidthShowcaseGroup("Segmented", segmentedList),
                createFullWidthShowcaseGroup("Settings Rows", settingsList),
                createFullWidthShowcaseGroup("Virtualized Segmented", createVirtualizedListView())
        );
    }

    /// Creates the virtualized list view sample.
    private static M3ListView<String> createVirtualizedListView() {
        M3ListView<String> listView = new M3ListView<>();
        listView.getStyleClass().add("demo-virtualized-list");
        listView.setListStyle(M3ListStyle.SEGMENTED);
        for (int i = 1; i <= 240; i++) {
            listView.getItems().add("Virtualized row " + i);
        }
        listView.setSelectionMode(M3SelectionMode.SINGLE);
        listView.setFixedCellSize(72.0);
        configureResponsiveWidth(listView, 520.0);
        listView.setPrefHeight(360.0);
        listView.setCellFactory(view -> new M3ListCell<>(view) {
            /// Creates the reusable row structure for this virtualized cell.
            @Override
            protected M3ListItem createListItem() {
                M3ListItem item = new M3ListItem();
                item.setSupportingText("Reused VirtualFlow row with generated content");
                item.setLeading(createSurfaceVariantIcon("task"));
                return item;
            }

            /// Updates the reusable row with the current data value.
            @Override
            protected void updateListItem(M3ListItem item, String text) {
                item.setHeadlineText(text);
                item.setTrailingSupportingText(Integer.toString(text.length()));
            }
        });
        listView.selectIndex(2);
        return listView;
    }

    /// Creates a sample thumbnail used by list item media rows.
    private static StackPane createListThumbnail() {
        Node icon = createIconViewport(DemoIcons.onSurface("image"));
        StackPane thumbnail = new StackPane(icon);
        thumbnail.getStyleClass().add("demo-list-thumbnail");
        return thumbnail;
    }
}
