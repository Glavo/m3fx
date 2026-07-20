// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds the AppBars component showcase page.
@NotNullByDefault
final class AppBarsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    AppBarsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the app bar component page.
    Node createContent() {
        M3TopAppBar small = createTopAppBar("Inbox", M3TopAppBarVariant.SMALL, "menu", "search", "more");
        M3TopAppBar centerAligned = createTopAppBar("Calendar", M3TopAppBarVariant.CENTER_ALIGNED,
                "back", "add", "more");
        M3TopAppBar mediumFlexible = createTopAppBar(
                "Library",
                M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                "menu",
                "search",
                "more"
        );
        M3TopAppBar mediumFlexibleSubtitle = createTopAppBar(
                "Messages",
                M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                "back",
                "search",
                "more"
        );
        mediumFlexibleSubtitle.setSubtitle("4 unread conversations");
        M3TopAppBar largeFlexible = createTopAppBar(
                "Discover",
                M3TopAppBarVariant.LARGE_FLEXIBLE,
                "menu",
                "search",
                "more"
        );
        M3TopAppBar largeFlexibleSubtitle = createTopAppBar(
                "Collections",
                M3TopAppBarVariant.LARGE_FLEXIBLE,
                "back",
                "search",
                "more"
        );
        largeFlexibleSubtitle.setSubtitle("Recently updated");

        M3TopAppBar medium = createTopAppBar("Project", M3TopAppBarVariant.MEDIUM, "menu", "search", "more");
        M3TopAppBar large = createTopAppBar("Workspace", M3TopAppBarVariant.LARGE, "menu", "search", "more");

        M3TopAppBar mediumScrolled = createTopAppBar(
                "Downloads",
                M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                "menu",
                "search",
                "more"
        );
        M3TopAppBar largeScrolled = createTopAppBar(
                "Photography",
                M3TopAppBarVariant.LARGE_FLEXIBLE,
                "back",
                "search",
                "more"
        );
        largeScrolled.setSubtitle("Shared collection");
        mediumScrolled.setScrolledUnder(true);
        largeScrolled.setScrolledUnder(true);

        M3Button toggleScrollState = new M3Button("Toggle scroll state");
        toggleScrollState.setVariant(M3ButtonVariant.TONAL);
        toggleScrollState.setOnAction(event -> {
            boolean scrolledUnder = !mediumScrolled.isScrolledUnder();
            mediumScrolled.setScrolledUnder(scrolledUnder);
            largeScrolled.setScrolledUnder(scrolledUnder);
        });

        return createGallery(
                createAppBarShowcaseGroup(
                        "Current Variants",
                        createLabeledAppBarPreview("Small", createTopAppBarPreview(small)),
                        createLabeledAppBarPreview("Center Aligned", createTopAppBarPreview(centerAligned)),
                        createLabeledAppBarPreview("Medium Flexible", createTopAppBarPreview(mediumFlexible)),
                        createLabeledAppBarPreview(
                                "Medium Flexible with Subtitle",
                                createTopAppBarPreview(mediumFlexibleSubtitle)
                        ),
                        createLabeledAppBarPreview("Large Flexible", createTopAppBarPreview(largeFlexible)),
                        createLabeledAppBarPreview(
                                "Large Flexible with Subtitle",
                                createTopAppBarPreview(largeFlexibleSubtitle)
                        )
                ),
                createAppBarShowcaseGroup(
                        "Baseline Compatibility",
                        createLabeledAppBarPreview("Medium", createTopAppBarPreview(medium)),
                        createLabeledAppBarPreview("Large", createTopAppBarPreview(large))
                ),
                createAppBarShowcaseGroup(
                        "Scroll Transformation",
                        toggleScrollState,
                        createLabeledAppBarPreview("Medium Flexible collapsed", createTopAppBarPreview(mediumScrolled)),
                        createLabeledAppBarPreview("Large Flexible collapsed", createTopAppBarPreview(largeScrolled))
                )
        );
    }

    /// Creates the app bar showcase group whose samples are stacked and expanded to the available width.
    private static VBox createAppBarShowcaseGroup(String title, Node... nodes) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");

        VBox stack = new VBox(16.0);
        stack.getStyleClass().addAll("demo-app-bar-stack", "demo-stacked-flow");
        stack.setFillWidth(true);
        stack.setMaxWidth(Double.MAX_VALUE);
        stack.getChildren().addAll(nodes);

        VBox group = new VBox(10.0, label, stack);
        group.getStyleClass().add("demo-showcase-group");
        group.setMaxWidth(Double.MAX_VALUE);
        return group;
    }

    /// Creates a top app bar sample.
    private static M3TopAppBar createTopAppBar(
            String title,
            M3TopAppBarVariant variant,
            String navigationIcon,
            String... actionIcons
    ) {
        Objects.requireNonNull(actionIcons, "actionIcons");

        M3TopAppBar topAppBar = new M3TopAppBar(title);
        topAppBar.setVariant(variant);
        topAppBar.setNavigation(createLeadingAppBarIconButton(navigationIcon));
        for (String actionIcon : actionIcons) {
            topAppBar.getActions().add(createTrailingAppBarIconButton(actionIcon));
        }
        topAppBar.setMaxWidth(Double.MAX_VALUE);
        return topAppBar;
    }

    /// Creates a preview surface for a top app bar.
    private static VBox createTopAppBarPreview(M3TopAppBar topAppBar) {
        VBox preview = createAppBarPreview();
        preview.getStyleClass().add("demo-top-app-bar-preview");
        preview.getChildren().addAll(topAppBar, createTopAppBarPreviewContent(topAppBar.getTitle()));
        return preview;
    }

    /// Creates a labeled app bar preview for variant comparison.
    private static VBox createLabeledAppBarPreview(String labelText, Node preview) {
        Label label = new Label(labelText);
        label.getStyleClass().add("demo-app-bar-sample-label");

        VBox sample = new VBox(8.0, label, preview);
        sample.getStyleClass().add("demo-app-bar-sample");
        sample.setFillWidth(true);
        sample.setMaxWidth(Double.MAX_VALUE);
        if (preview instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(region, Priority.NEVER);
        }
        return sample;
    }

    /// Creates the lightweight content area shown below a top app bar preview.
    private static VBox createTopAppBarPreviewContent(String title) {
        VBox content = new VBox(10.0);
        content.getStyleClass().add("demo-top-app-bar-preview-content");
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(
                createTopAppBarPreviewRow(title + " updates", "Updated just now"),
                createTopAppBarPreviewRow("Pinned items", "3 active entries")
        );
        return content;
    }

    /// Creates one content row for a top app bar preview.
    private static HBox createTopAppBarPreviewRow(String title, String supportingText) {
        StackPane leading = new StackPane();
        leading.getStyleClass().add("demo-top-app-bar-preview-leading");
        leading.setMinSize(40.0, 40.0);
        leading.setPrefSize(40.0, 40.0);
        leading.setMaxSize(40.0, 40.0);

        Label headline = new Label(title);
        headline.getStyleClass().add("demo-top-app-bar-preview-headline");
        Label supporting = new Label(supportingText);
        supporting.getStyleClass().add("demo-top-app-bar-preview-supporting");
        VBox text = new VBox(2.0, headline, supporting);
        text.getStyleClass().add("demo-top-app-bar-preview-text");
        HBox.setHgrow(text, Priority.ALWAYS);

        Region trailing = new Region();
        trailing.getStyleClass().add("demo-top-app-bar-preview-trailing");
        trailing.setMinSize(56.0, 12.0);
        trailing.setPrefSize(56.0, 12.0);
        trailing.setMaxSize(56.0, 12.0);

        HBox row = new HBox(16.0, leading, text, trailing);
        row.getStyleClass().add("demo-top-app-bar-preview-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    /// Creates a sample icon button for leading app bar slots.
    private static M3IconButton createLeadingAppBarIconButton(String iconName) {
        Node icon = createIconViewport(DemoIcons.onSurface(iconName), "demo-app-bar-icon");
        M3IconButton button = new M3IconButton(icon);
        button.setAccessibleText(appBarIconAccessibleText(iconName));
        return button;
    }
}
