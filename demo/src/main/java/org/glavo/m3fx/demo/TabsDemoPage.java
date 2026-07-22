// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.layout.VBox;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.controls.M3TabBarLayout;
import org.glavo.m3fx.controls.M3TabBarVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Tabs component showcase page.
@NotNullByDefault
final class TabsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    TabsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the tab component page.
    Node createContent() {
        M3TabBar primary = createTabBar("Overview", "Activity", "Files");
        M3TabBar secondary = createTabBar("Overview", "Details", "Reviews");
        secondary.setVariant(M3TabBarVariant.SECONDARY);
        secondary.getTabs().get(2).setDisable(true);

        M3TabBar scrollablePrimary = createTabBar(
                new M3Tab("Overview"),
                new M3Tab("Recent activity"),
                new M3Tab("Shared with me"),
                new M3Tab("Offline files"),
                new M3Tab("Storage management"),
                new M3Tab("Notifications"),
                new M3Tab("Security and privacy"),
                new M3Tab("Connected applications")
        );
        scrollablePrimary.setTabLayout(M3TabBarLayout.SCROLLABLE);
        configureResponsiveWidth(scrollablePrimary, 720.0);

        M3TabBar scrollableSecondary = createTabBar(
                new M3Tab("Highlights"),
                new M3Tab("Technical specifications"),
                new M3Tab("Customer reviews"),
                new M3Tab("Compatibility"),
                new M3Tab("Accessories"),
                new M3Tab("Support resources"),
                new M3Tab("Release history")
        );
        scrollableSecondary.setVariant(M3TabBarVariant.SECONDARY);
        scrollableSecondary.setTabLayout(M3TabBarLayout.SCROLLABLE);
        configureResponsiveWidth(scrollableSecondary, 720.0);

        M3TabBar hierarchyPrimary = createTabBar("Flights", "Trips", "Explore");
        M3TabBar hierarchySecondary = createTabBar("Upcoming", "Previous", "Saved");
        hierarchySecondary.setVariant(M3TabBarVariant.SECONDARY);
        VBox hierarchy = new VBox(hierarchyPrimary, hierarchySecondary);
        hierarchy.getStyleClass().add("demo-tab-hierarchy");
        hierarchy.setMinWidth(0.0);
        hierarchy.setMaxWidth(Double.MAX_VALUE);

        return createGallery(
                createShowcaseGroup("Primary Fixed", primary),
                createShowcaseGroup("Secondary Fixed", secondary),
                createFullWidthShowcaseGroup("Primary Scrollable", scrollablePrimary),
                createFullWidthShowcaseGroup("Secondary Scrollable", scrollableSecondary),
                createShowcaseGroup("Primary And Secondary Hierarchy", hierarchy)
        );
    }

    /// Creates a tab bar sample with initial tabs.
    private static M3TabBar createTabBar(M3Tab... tabs) {
        M3TabBar tabBar = new M3TabBar();
        tabBar.getTabs().addAll(tabs);
        return tabBar;
    }

    /// Creates a tab bar sample.
    private static M3TabBar createTabBar(String first, String second, String third) {
        M3Tab firstTab = new M3Tab(first);
        firstTab.setSelected(true);
        return createTabBar(firstTab, new M3Tab(second), new M3Tab(third));
    }
}
