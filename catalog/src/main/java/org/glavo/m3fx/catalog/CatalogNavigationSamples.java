// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3NavigationRailVariant;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.controls.M3TabBarLayout;
import org.glavo.m3fx.controls.M3TabBarVariant;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Creates focused destination-navigation, tab, and app-bar samples.
@NotNullByDefault
final class CatalogNavigationSamples {
    /// Prevents instantiation of this factory class.
    private CatalogNavigationSamples() {
    }

    /// Creates a navigation bar with a requested destination count and item layout.
    ///
    /// @param destinationCount the number of destinations from three through five
    /// @param itemLayout the vertical or horizontal item arrangement
    /// @param badge whether one destination has a count badge
    /// @return the configured navigation bar
    static Node navigationBar(
            int destinationCount,
            M3NavigationItemLayout itemLayout,
            boolean badge
    ) {
        M3NavigationBar bar = new M3NavigationBar();
        bar.setItemLayout(itemLayout);
        for (int index = 0; index < destinationCount; index++) {
            M3NavigationItem item = navigationItem(index);
            if (badge && index == 1) {
                item.setBadge(new M3Badge("3"));
            }
            bar.getItems().add(item);
        }
        bar.selectIndex(Math.min(1, destinationCount - 1));
        return CatalogSamples.configureResponsiveWidth(bar, itemLayout == M3NavigationItemLayout.HORIZONTAL
                ? 720.0
                : 560.0);
    }

    /// Creates a standard, modal, or grouped navigation drawer.
    ///
    /// @param variant the persistent or modal drawer variant
    /// @param grouped whether destinations are nested beneath a group
    /// @return the drawer or modal preview
    static Node navigationDrawer(M3NavigationDrawerVariant variant, boolean grouped) {
        M3NavigationDrawer drawer = new M3NavigationDrawer();
        drawer.setVariant(variant);
        if (grouped) {
            M3NavigationDrawerGroup workspace = new M3NavigationDrawerGroup("Workspace");
            workspace.getHeaderItem().setLeading(CatalogSamples.icon(CatalogIcons.SURFACE));
            M3ListItem dashboard = drawerItem("Dashboard", CatalogIcons.HOME);
            workspace.getItems().addAll(
                    dashboard,
                    drawerItem("Reports", CatalogIcons.LIST)
            );
            workspace.setExpanded(true);
            drawer.getItems().addAll(workspace, drawerItem("Settings", CatalogIcons.SETTINGS));
            drawer.select(dashboard);
        } else {
            M3ListItem inbox = drawerItem("Inbox", CatalogIcons.NOTIFICATIONS);
            M3ListItem starred = drawerItem("Starred", CatalogIcons.FAVORITE);
            starred.setTrailing(new M3Badge("3"));
            drawer.getItems().addAll(
                    inbox,
                    starred,
                    new M3Divider(),
                    drawerItem("Sent", CatalogIcons.ARROW_FORWARD),
                    drawerItem("Archive", CatalogIcons.BOTTOM_SHEET)
            );
            drawer.select(inbox);
        }
        drawer.setPrefHeight(340.0);
        if (variant == M3NavigationDrawerVariant.STANDARD) {
            return drawer;
        }

        StackPane application = new StackPane(new M3Text("Application content", M3TextRole.TITLE_MEDIUM));
        M3Scrim scrim = new M3Scrim();
        scrim.setFocusTraversable(false);
        drawer.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane preview = new StackPane(application, scrim, drawer);
        preview.setPrefSize(560.0, 340.0);
        StackPane.setAlignment(drawer, Pos.TOP_LEFT);
        return CatalogSamples.configureResponsiveWidth(preview, 560.0);
    }

    /// Creates one collapsed or expanded navigation-rail presentation.
    ///
    /// @param variant the standard or modal expanded-rail treatment
    /// @param expanded whether the rail begins expanded
    /// @param narrow whether the collapsed rail uses narrow geometry
    /// @param centered whether destinations are vertically centered
    /// @param fullWidthIndicator whether selected indicators span the expanded rail
    /// @param hideWhenCollapsed whether a collapsed modal rail disappears
    /// @param headerAction whether the rail header contains a floating action
    /// @return the configured navigation rail
    static Node navigationRail(
            M3NavigationRailVariant variant,
            boolean expanded,
            boolean narrow,
            boolean centered,
            boolean fullWidthIndicator,
            boolean hideWhenCollapsed,
            boolean headerAction
    ) {
        M3NavigationRail rail = new M3NavigationRail();
        rail.setVariant(variant);
        rail.setExpanded(expanded);
        rail.setNarrow(narrow);
        rail.setItemsCentered(centered);
        rail.setFullWidthIndicator(fullWidthIndicator);
        rail.setHideWhenCollapsed(hideWhenCollapsed);
        rail.getItems().addAll(
                navigationItem(0),
                navigationItem(1),
                navigationItem(2),
                navigationItem(3)
        );
        rail.getItems().get(1).setBadge(new M3Badge());
        rail.selectIndex(0);
        rail.setPrefHeight(460.0);
        if (headerAction) {
            VBox header = new VBox(
                    16.0,
                    CatalogSamples.iconButton(CatalogIcons.MORE_VERTICAL, "Toggle navigation"),
                    new M3FloatingActionButton(CatalogSamples.icon(CatalogIcons.ADD))
            );
            header.setAlignment(Pos.TOP_CENTER);
            rail.setHeader(header);
        }
        return rail;
    }

    /// Creates a primary or secondary tab bar in fixed or scrollable form.
    ///
    /// @param variant the primary or secondary hierarchy role
    /// @param layout the fixed or scrollable tab arrangement
    /// @param disabled whether the last tab is disabled
    /// @return the configured tab bar
    static Node tabBar(M3TabBarVariant variant, M3TabBarLayout layout, boolean disabled) {
        M3TabBar bar = new M3TabBar();
        bar.setVariant(variant);
        bar.setTabLayout(layout);
        String[] labels = layout == M3TabBarLayout.SCROLLABLE
                ? new String[]{
                        "Overview",
                        "Recent activity",
                        "Shared with me",
                        "Offline files",
                        "Storage management",
                        "Notifications",
                        "Security and privacy"
                }
                : new String[]{"Overview", "Activity", "Files"};
        for (int index = 0; index < labels.length; index++) {
            M3Tab tab = new M3Tab(labels[index]);
            tab.setSelected(index == 0);
            tab.setDisable(disabled && index == labels.length - 1);
            bar.getTabs().add(tab);
        }
        return CatalogSamples.configureResponsiveWidth(bar, layout == M3TabBarLayout.SCROLLABLE ? 720.0 : 540.0);
    }

    /// Creates stacked primary and secondary tab bars.
    ///
    /// @return the tab-hierarchy example
    static Node tabHierarchy() {
        return new VBox(
                tabBar(M3TabBarVariant.PRIMARY, M3TabBarLayout.FIXED, false),
                tabBar(M3TabBarVariant.SECONDARY, M3TabBarLayout.FIXED, false)
        );
    }

    /// Creates one top app bar variant with optional subtitle and scrolled-under state.
    ///
    /// @param variant the top app bar layout variant
    /// @param subtitle whether a supporting subtitle is supplied
    /// @param scrolledUnder whether content is currently scrolled beneath the bar
    /// @return the configured top app bar
    static Node topAppBar(M3TopAppBarVariant variant, boolean subtitle, boolean scrolledUnder) {
        M3TopAppBar bar = new M3TopAppBar(topAppBarTitle(variant));
        bar.setVariant(variant);
        bar.setNavigation(CatalogSamples.iconButton(CatalogIcons.NAVIGATION, "Open navigation"));
        bar.getActions().addAll(
                CatalogSamples.iconButton(CatalogIcons.SEARCH, "Search"),
                CatalogSamples.iconButton(CatalogIcons.MORE_VERTICAL, "More options")
        );
        if (subtitle) {
            bar.setSubtitle("Recently updated");
        }
        bar.setScrolledUnder(scrolledUnder);
        return CatalogSamples.configureResponsiveWidth(bar, 720.0);
    }

    /// Creates one destination for a position in the common navigation sample.
    ///
    /// @param index the destination index
    /// @return the configured navigation item
    private static M3NavigationItem navigationItem(int index) {
        return switch (index) {
            case 0 -> CatalogSamples.navigationItem("Home", CatalogIcons.HOME);
            case 1 -> CatalogSamples.navigationItem("Search", CatalogIcons.SEARCH);
            case 2 -> CatalogSamples.navigationItem("Profile", CatalogIcons.AVATAR);
            case 3 -> CatalogSamples.navigationItem("Settings", CatalogIcons.SETTINGS);
            default -> CatalogSamples.navigationItem("Files", CatalogIcons.LIST);
        };
    }

    /// Creates one drawer destination row.
    ///
    /// @param text the row label
    /// @param iconPath the leading icon path
    /// @return the configured drawer item
    private static M3ListItem drawerItem(String text, String iconPath) {
        M3ListItem item = new M3ListItem(text);
        item.setLeading(CatalogSamples.icon(iconPath));
        return item;
    }

    /// Returns the representative title for one top app bar variant.
    ///
    /// @param variant the top app bar variant
    /// @return the display title
    private static String topAppBarTitle(M3TopAppBarVariant variant) {
        return switch (variant) {
            case SMALL -> "Inbox";
            case CENTER_ALIGNED -> "Calendar";
            case MEDIUM -> "Project";
            case LARGE -> "Workspace";
            case MEDIUM_FLEXIBLE -> "Library";
            case LARGE_FLEXIBLE -> "Discover";
        };
    }
}
