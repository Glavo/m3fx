// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the components overview and its navigation destinations.
@NotNullByDefault
final class ComponentsOverviewDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ComponentsOverviewDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the component overview page.
    Node createContent() {
        M3ListPane materialComponents = createOverviewList();
        M3ListPane additionalDemos = createOverviewList();
        for (DemoPage page : context.demoPages()) {
            if (page.sidebarSection().equals(DemoPageCatalog.COMPONENTS_OVERVIEW_GROUP)) {
                continue;
            }

            M3ListPane destinationList = page.sidebarSection().equals(DemoPageCatalog.ADDITIONAL_DEMOS_GROUP)
                    ? additionalDemos
                    : materialComponents;
            destinationList.getItems().add(createOverviewItem(page));
        }

        return createGallery(
                createFullWidthShowcaseGroup("Material Components", materialComponents),
                createFullWidthShowcaseGroup("Additional Demos", additionalDemos)
        );
    }

    /// Creates a segmented list used by one overview section.
    private static M3ListPane createOverviewList() {
        M3ListPane list = new M3ListPane();
        list.getStyleClass().add("demo-overview-list");
        list.setListStyle(M3ListStyle.SEGMENTED);
        list.setMinWidth(0.0);
        list.setMaxWidth(Double.MAX_VALUE);
        return list;
    }

    /// Creates one overview destination backed by a registered page.
    private M3ListItem createOverviewItem(DemoPage page) {
        M3ListItem item = new M3ListItem(page.navigationTitle());
        item.getStyleClass().add("demo-overview-destination");
        item.setSupportingText(page.subtitle());
        item.setLeading(createNavigationIcon(overviewIconName(page.navigationTitle())));

        Node indicator = createSurfaceVariantIcon("chevron-right");
        indicator.getStyleClass().add("demo-overview-destination-indicator");
        item.setTrailing(indicator);
        item.setTrailingSlotSize(M3ListItemSlotSize.ICON);
        item.setUserData(page);
        item.setOnAction(event -> context.navigateTo(page));
        return item;
    }
}
