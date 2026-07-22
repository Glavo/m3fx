// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3NavigationRailVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the NavigationRail component showcase page.
@NotNullByDefault
final class NavigationRailDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    NavigationRailDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the navigation rail component page.
    Node createContent() {
        M3NavigationRail collapsed = createFourItemNavigationRail();
        collapsed.setPrefHeight(520.0);

        M3NavigationRail narrow = createFourItemNavigationRail();
        narrow.setNarrow(true);
        narrow.setItemsCentered(true);
        narrow.setPrefHeight(520.0);

        M3NavigationRail standard = createFourItemNavigationRail();
        standard.setExpanded(true);
        standard.setPrefHeight(520.0);

        M3NavigationRail modal = createFourItemNavigationRail();
        modal.setVariant(M3NavigationRailVariant.MODAL);
        modal.setFullWidthIndicator(true);
        modal.setExpanded(true);
        modal.setPrefHeight(520.0);

        M3NavigationRail immersive = createFourItemNavigationRail();
        immersive.setVariant(M3NavigationRailVariant.MODAL);
        immersive.setHideWhenCollapsed(true);
        immersive.setExpanded(true);
        immersive.setPrefHeight(520.0);

        return createGallery(
                createShowcaseGroup(
                        "Collapsed",
                        createNavigationRailPreview("Collapsed with action", collapsed, true),
                        createNavigationRailPreview("Narrow, centered destinations", narrow, false)
                ),
                createShowcaseGroup(
                        "Expanded",
                        createNavigationRailPreview("Expanded standard", standard, false),
                        createNavigationRailPreview("Expanded modal, full-width indicator", modal, false),
                        createNavigationRailPreview("Immersive, hide when collapsed", immersive, false)
                )
        );
    }

    /// Creates a navigation rail sample with initial items.
    private static M3NavigationRail createNavigationRail(M3NavigationItem... items) {
        M3NavigationRail navigationRail = new M3NavigationRail();
        navigationRail.getItems().addAll(items);
        return navigationRail;
    }

    /// Creates the four-item navigation rail sample.
    private M3NavigationRail createFourItemNavigationRail() {
        M3NavigationItem firstItem = createNavigationItem("Home", "home");
        M3NavigationItem secondItem = createNavigationItem("Search", "search");
        M3NavigationItem thirdItem = createNavigationItem("Profile", "person");
        M3NavigationItem fourthItem = createNavigationItem("Settings", "settings");
        secondItem.setBadge(new M3Badge());

        M3NavigationRail navigationRail = createNavigationRail(
                firstItem,
                secondItem,
                thirdItem,
                fourthItem
        );
        navigationRail.selectIndex(0);
        return navigationRail;
    }

    /// Creates a labeled interactive collapsed or expanded navigation rail preview.
    ///
    /// @param title          the variant title displayed above the preview
    /// @param navigationRail the navigation rail to preview
    /// @param showAction     whether the header includes a floating action button
    /// @return the labeled interactive preview
    private static VBox createNavigationRailPreview(
            String title,
            M3NavigationRail navigationRail,
            boolean showAction
    ) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-group-title");

        boolean initiallyExpanded = navigationRail.isExpanded();
        M3IconButton menuButton = createIconButton(initiallyExpanded ? "close" : "menu");
        menuButton.setAccessibleText(initiallyExpanded ? "Collapse navigation rail" : "Expand navigation rail");
        menuButton.setOnAction(event -> {
            boolean expanded = !navigationRail.isExpanded();
            navigationRail.setExpanded(expanded);
            menuButton.setGraphic(createIconViewport(DemoIcons.primary(expanded ? "close" : "menu")));
            menuButton.setAccessibleText(expanded ? "Collapse navigation rail" : "Expand navigation rail");
        });

        VBox header = new VBox(16.0, menuButton);
        header.setAlignment(Pos.TOP_CENTER);
        if (showAction) {
            M3FloatingActionButton action = createFab(
                    "add",
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            action.setAccessibleText("Create");
            header.getChildren().add(action);
        }
        navigationRail.setHeader(header);

        Node railPresentation = navigationRail;
        if (navigationRail.isHideWhenCollapsed()) {
            M3IconButton revealButton = createIconButton("menu");
            revealButton.setAccessibleText("Show navigation rail");
            revealButton.setVisible(!navigationRail.isExpanded());
            revealButton.setManaged(!navigationRail.isExpanded());
            revealButton.setOnAction(event -> {
                navigationRail.setExpanded(true);
                revealButton.setVisible(false);
                revealButton.setManaged(false);
                menuButton.setGraphic(createIconViewport(DemoIcons.primary("close")));
                menuButton.setAccessibleText("Hide navigation rail");
            });
            menuButton.setOnAction(event -> {
                navigationRail.setExpanded(false);
                revealButton.setVisible(true);
                revealButton.setManaged(true);
                menuButton.setGraphic(createIconViewport(DemoIcons.primary("menu")));
                menuButton.setAccessibleText("Show navigation rail");
            });

            StackPane immersivePresentation = new StackPane(navigationRail, revealButton);
            immersivePresentation.setAlignment(Pos.TOP_LEFT);
            StackPane.setAlignment(navigationRail, Pos.TOP_LEFT);
            StackPane.setAlignment(revealButton, Pos.TOP_LEFT);
            railPresentation = immersivePresentation;
        }

        StackPane presentationFootprint = createRailPresentationFootprint(railPresentation, navigationRail);
        VBox preview = new VBox(12.0, titleLabel, presentationFootprint);
        preview.setAlignment(Pos.TOP_LEFT);
        reserveExpandedRailFootprint(preview, navigationRail);
        return preview;
    }

    /// Creates a stable-height viewport for one rail presentation.
    ///
    /// A rail may resolve different content heights for its vertical and horizontal destination layouts. The
    /// viewport retains the sample's declared preferred height while allowing the rail to keep its own maximum-size
    /// behavior, so rows below the sample do not move during expansion.
    ///
    /// @param presentation   the rail or composite presentation shown in the preview
    /// @param navigationRail the rail that declares the sample height
    /// @return the stable presentation viewport
    private static StackPane createRailPresentationFootprint(Node presentation, M3NavigationRail navigationRail) {
        StackPane footprint = new StackPane(presentation);
        footprint.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(presentation, Pos.TOP_LEFT);
        footprint.minHeightProperty().bind(navigationRail.prefHeightProperty());
        footprint.prefHeightProperty().bind(navigationRail.prefHeightProperty());
        footprint.maxHeightProperty().bind(navigationRail.prefHeightProperty());
        return footprint;
    }

    /// Reserves the rail's resolved expanded width without stretching its current presentation.
    ///
    /// The surrounding gallery may still wrap complete previews when its available width changes. Keeping each
    /// preview's footprint stable ensures that the rail's own expansion animation cannot reflow a sibling preview.
    ///
    /// @param preview        the preview cell placed in the gallery flow
    /// @param navigationRail the rail whose expanded footprint is reserved
    private static void reserveExpandedRailFootprint(VBox preview, M3NavigationRail navigationRail) {
        DoubleBinding expandedFootprint = Bindings.createDoubleBinding(
                () -> {
                    double minimum = navigationRail.getExpandedMinimumContainerWidth();
                    double maximum = Math.max(minimum, navigationRail.getExpandedMaximumContainerWidth());
                    return Math.max(minimum, Math.min(maximum, navigationRail.getExpandedContainerWidth()));
                },
                navigationRail.expandedMinimumContainerWidthProperty(),
                navigationRail.expandedContainerWidthProperty(),
                navigationRail.expandedMaximumContainerWidthProperty()
        );

        preview.setFillWidth(false);
        preview.minWidthProperty().bind(expandedFootprint);
        preview.prefWidthProperty().bind(expandedFootprint);
        preview.maxWidthProperty().bind(expandedFootprint);
    }
}
