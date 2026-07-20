// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SearchViewLayout;
import org.glavo.m3fx.controls.M3SearchViewStyle;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Search component showcase page.
@NotNullByDefault
final class SearchDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SearchDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the search component page.
    Node createContent() {
        M3SearchBar searchBar = new M3SearchBar("Search M3FX");
        searchBar.setPrefWidth(420.0);
        M3IconButton clearSearchBar = createIconButton("close");
        clearSearchBar.setOnAction(event -> searchBar.clear());
        searchBar.getTrailingActions().add(clearSearchBar);

        M3SearchBar populated = new M3SearchBar("Search M3FX");
        populated.setText("Buttons");
        populated.setPrefWidth(420.0);

        M3SearchView containedDocked = new M3SearchView("Search components");
        containedDocked.setPrefWidth(520.0);
        M3IconButton tuneSearchView = createIconButton("tune");
        M3IconButton clearSearchView = createIconButton("close");
        clearSearchView.setOnAction(event -> containedDocked.clear());
        containedDocked.getTrailingActions().addAll(tuneSearchView, clearSearchView);
        containedDocked.getResults().addAll(
                createSearchResult("Buttons", "Filled, tonal, outlined, text, and elevated variants"),
                createSearchResult("Menus", "Menu surfaces, selected rows, and menu buttons"),
                createSearchResult("Navigation", "Bars, rails, drawers, and destination items")
        );

        M3SearchView dividedDocked = new M3SearchView("Search divided results");
        dividedDocked.setViewStyle(M3SearchViewStyle.DIVIDED);
        dividedDocked.setPrefWidth(520.0);
        dividedDocked.getResults().addAll(
                createSearchResult("Color", "Dynamic color and component color roles"),
                createSearchResult("Typography", "Material type roles and font metrics")
        );

        M3SearchView containedFullScreen = new M3SearchView("Search full-screen content");
        containedFullScreen.setViewLayout(M3SearchViewLayout.FULL_SCREEN);
        containedFullScreen.setPrefWidth(520.0);
        containedFullScreen.getResults().addAll(
                createSearchResult("Cards", "Elevated, filled, and outlined cards"),
                createSearchResult("Dialogs", "Basic and full-screen dialogs")
        );

        M3SearchView dividedFullScreen = new M3SearchView("Search full-screen divided content");
        dividedFullScreen.setViewStyle(M3SearchViewStyle.DIVIDED);
        dividedFullScreen.setViewLayout(M3SearchViewLayout.FULL_SCREEN);
        dividedFullScreen.setPrefWidth(520.0);
        dividedFullScreen.getTrailingActions().add(createIconButton("tune"));
        dividedFullScreen.getResults().addAll(
                createSearchResult("Date pickers", "Docked and modal date selection"),
                createSearchResult("Time pickers", "Dial and keyboard time entry")
        );

        return createGallery(
                createShowcaseGroup("Search Bars", searchBar, populated),
                createShowcaseGroup("Contained Docked", containedDocked),
                createShowcaseGroup("Divided Docked", dividedDocked),
                createShowcaseGroup("Contained Full-screen", containedFullScreen),
                createShowcaseGroup("Divided Full-screen", dividedFullScreen)
        );
    }

    /// Creates a sample search result row.
    private static M3ListItem createSearchResult(String title, String supportingText) {
        M3ListItem item = new M3ListItem(title);
        item.setSupportingText(supportingText);
        item.setLeading(createNavigationIcon(overviewIconName(title)));
        return item;
    }
}
