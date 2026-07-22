// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Navigation component showcase page.
@NotNullByDefault
final class NavigationDemoPage extends DemoPageSupport {
    /// The minimum bar width that preserves four 160px horizontal destination slots and 8px side insets.
    private static final double MINIMUM_FOUR_ITEM_HORIZONTAL_WIDTH = 656.0;

    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    NavigationDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the navigation component page.
    Node createContent() {
        M3NavigationBar compact = createFourItemNavigationBar();
        M3NavigationBar medium = createFourItemNavigationBar();
        configureResponsiveWidth(compact, 520.0);
        configureResponsiveWidth(medium, 720.0);
        installAdaptiveItemLayout(medium);

        M3NavigationBar threeDestinations = createNavigationBar(
                createNavigationItem("Home", "home"),
                createNavigationItem("Search", "search"),
                createNavigationItem("Profile", "person")
        );
        threeDestinations.selectIndex(0);
        configureResponsiveWidth(threeDestinations, 520.0);

        M3NavigationBar fiveDestinations = createNavigationBar(
                createNavigationItem("Home", "home"),
                createNavigationItem("Search", "search"),
                createNavigationItem("Updates", "notifications"),
                createNavigationItem("Files", "label"),
                createNavigationItem("Profile", "person")
        );
        fiveDestinations.selectIndex(2);
        configureResponsiveWidth(fiveDestinations, 520.0);

        return createGallery(
                createFullWidthShowcaseGroup("Compact Window", compact),
                createFullWidthShowcaseGroup("Adaptive Window", medium),
                createFullWidthShowcaseGroup("Three And Five Destinations", threeDestinations, fiveDestinations)
        );
    }

    /// Creates a navigation bar sample with initial items.
    private static M3NavigationBar createNavigationBar(M3NavigationItem... items) {
        M3NavigationBar navigationBar = new M3NavigationBar();
        navigationBar.getItems().addAll(items);
        return navigationBar;
    }

    /// Selects the horizontal medium-window arrangement only while all four destination targets fit the bar.
    ///
    /// Below the Material token width, the bar uses the compact vertical arrangement instead of clipping or
    /// compressing destination hit targets.
    ///
    /// @param navigationBar the four-destination navigation bar to adapt
    private static void installAdaptiveItemLayout(M3NavigationBar navigationBar) {
        navigationBar.widthProperty().addListener((observable, oldWidth, newWidth) ->
                updateAdaptiveItemLayout(navigationBar)
        );
        updateAdaptiveItemLayout(navigationBar);
    }

    /// Updates the navigation item arrangement after the bar receives a new allocated width.
    ///
    /// @param navigationBar the four-destination navigation bar to update
    private static void updateAdaptiveItemLayout(M3NavigationBar navigationBar) {
        M3NavigationItemLayout layout = navigationBar.getWidth() >= MINIMUM_FOUR_ITEM_HORIZONTAL_WIDTH
                ? M3NavigationItemLayout.HORIZONTAL
                : M3NavigationItemLayout.VERTICAL;
        if (navigationBar.getItemLayout() != layout) {
            navigationBar.setItemLayout(layout);
        }
    }

    /// Creates the four-item navigation bar sample.
    private M3NavigationBar createFourItemNavigationBar() {
        M3NavigationItem firstItem = createNavigationItem("Home", "home");
        M3NavigationItem secondItem = createNavigationItem("Search", "search");
        M3NavigationItem thirdItem = createNavigationItem("Profile", "person");
        M3NavigationItem fourthItem = createNavigationItem("Settings", "settings");
        secondItem.setBadge(new M3Badge("3"));

        M3NavigationBar navigationBar = createNavigationBar(
                firstItem,
                secondItem,
                thirdItem,
                fourthItem
        );
        navigationBar.selectIndex(0);
        return navigationBar;
    }
}
