// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3Scrim;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the NavigationDrawer component showcase page.
@NotNullByDefault
final class NavigationDrawerDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    NavigationDrawerDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the navigation drawer component page.
    Node createContent() {
        M3NavigationDrawer standard = createFourItemNavigationDrawer();
        standard.getItems().addAll(
                new M3Divider(),
                createDrawerItem("Drafts", "edit"),
                createDrawerItem("Spam", "warning"),
                createDrawerItem("Trash", "delete"),
                createDrawerItem("Settings", "settings")
        );
        standard.setPrefHeight(280.0);
        M3NavigationDrawer modal = createFourItemNavigationDrawer();
        modal.setVariant(M3NavigationDrawerVariant.MODAL);
        M3NavigationDrawer grouped = createSectionNavigationDrawer();
        grouped.setPrefHeight(360.0);

        return createGallery(
                createShowcaseGroup("Standard", standard),
                createFullWidthShowcaseGroup("Modal", createModalNavigationDrawerPreview(modal)),
                createShowcaseGroup("Grouped destinations", grouped)
        );
    }

    /// Creates a modal navigation drawer preview above application content and a scrim.
    ///
    /// @param drawer the modal drawer
    /// @return the modal drawer preview
    private static StackPane createModalNavigationDrawerPreview(M3NavigationDrawer drawer) {
        Label contentLabel = new Label("Application content");
        contentLabel.getStyleClass().add("demo-modal-drawer-content-label");
        StackPane applicationContent = new StackPane(contentLabel);
        applicationContent.getStyleClass().add("demo-modal-drawer-content");

        M3Scrim scrim = new M3Scrim();
        scrim.setFocusTraversable(false);

        drawer.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane preview = new StackPane(applicationContent, scrim, drawer);
        preview.getStyleClass().add("demo-modal-drawer-preview");
        StackPane.setAlignment(drawer, Pos.TOP_LEFT);
        return preview;
    }

    /// Creates a navigation drawer sample with initial items.
    private static M3NavigationDrawer createNavigationDrawer(Node... items) {
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer();
        navigationDrawer.getItems().addAll(items);
        return navigationDrawer;
    }

    /// Creates the four-item navigation drawer sample.
    private static M3NavigationDrawer createFourItemNavigationDrawer() {
        M3ListItem firstItem = createDrawerItem("Inbox", "inbox");
        M3ListItem secondItem = createDrawerItem("Starred", "star");
        M3ListItem thirdItem = createDrawerItem("Sent", "send");
        M3ListItem fourthItem = createDrawerItem("Archive", "archive");
        secondItem.setTrailing(new M3Badge("3"));

        M3NavigationDrawer navigationDrawer = createNavigationDrawer(
                firstItem,
                secondItem,
                new M3Divider(),
                thirdItem,
                fourthItem
        );
        navigationDrawer.selectIndex(0);
        return navigationDrawer;
    }

    /// Creates the sectioned navigation drawer sample.
    private static M3NavigationDrawer createSectionNavigationDrawer() {
        M3NavigationDrawerGroup workspace = new M3NavigationDrawerGroup("Workspace");
        workspace.getHeaderItem().setLeading(createNavigationIcon("dashboard"));
        M3ListItem dashboard = createDrawerItem("Dashboard", "home");
        workspace.getItems().addAll(
                dashboard,
                createDrawerItem("Reports", "reports")
        );
        workspace.setExpanded(true);

        M3NavigationDrawer navigationDrawer = createNavigationDrawer(
                workspace,
                createDrawerItem("Settings", "settings")
        );
        navigationDrawer.select(dashboard);
        return navigationDrawer;
    }

    /// Creates a sample drawer item.
    private static M3ListItem createDrawerItem(String text, String iconName) {
        M3ListItem item = new M3ListItem(text);
        item.setLeading(createNavigationIcon(iconName));
        return item;
    }
}
