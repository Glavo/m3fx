// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the ComponentsOverview component showcase page.
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
        M3ListPane primaryComponents = createListPane(
                createOverviewItem("App bars", "Top and bottom app bars for persistent actions."),
                createOverviewItem("Buttons", "Common actions, icon buttons, split buttons, and FABs."),
                createOverviewItem("Text fields", "Filled, outlined, validation, and supporting text patterns."),
                createOverviewItem("Selection", "Checkbox, radio button, switch, chips, and segmented controls."),
                createOverviewItem("Navigation", "Navigation bar, rail, drawer, and tabs.")
        );
        primaryComponents.getStyleClass().add("demo-overview-list");
        primaryComponents.setMaxWidth(720.0);

        M3ListPane feedbackComponents = createListPane(
                createOverviewItem("Loading & progress", "Linear and circular progress plus loading indicators."),
                createOverviewItem("Date & time pickers", "Date, range, and time selection controls."),
                createOverviewItem("Dialogs & sheets", "Dialogs, bottom sheets, side sheets, scrims, and snackbars."),
                createOverviewItem("Lists & surfaces", "Lists, cards, carousel, badges, menus, and surfaces.")
        );
        feedbackComponents.getStyleClass().add("demo-overview-list");
        feedbackComponents.setMaxWidth(720.0);

        return createGallery(
                createShowcaseGroup("Primary Components", primaryComponents),
                createShowcaseGroup("Feedback And Containers", feedbackComponents)
        );
    }

    /// Creates one overview list item.
    private static M3ListItem createOverviewItem(String title, String supportingText) {
        M3ListItem item = new M3ListItem(title);
        item.setSupportingText(supportingText);
        item.setLeading(createNavigationIcon(overviewIconName(title)));
        return item;
    }

    /// Creates a list pane sample with initial content nodes.
    private static M3ListPane createListPane(Node... items) {
        M3ListPane listPane = new M3ListPane();
        listPane.getItems().addAll(items);
        return listPane;
    }
}
