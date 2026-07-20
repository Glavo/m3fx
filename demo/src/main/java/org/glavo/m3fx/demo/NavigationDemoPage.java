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
        medium.setItemLayout(M3NavigationItemLayout.HORIZONTAL);
        medium.setMaxWidth(720.0);

        return createGallery(
                createFullWidthShowcaseGroup("Compact Window", compact),
                createFullWidthShowcaseGroup("Medium Window", medium)
        );
    }

    /// Creates a navigation bar sample with initial items.
    private static M3NavigationBar createNavigationBar(M3NavigationItem... items) {
        M3NavigationBar navigationBar = new M3NavigationBar();
        navigationBar.getItems().addAll(items);
        return navigationBar;
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
