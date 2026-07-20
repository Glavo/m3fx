// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CarouselLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Carousel component showcase page.
@NotNullByDefault
final class CarouselDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    CarouselDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the carousel component page.
    Node createContent() {
        M3Carousel multiBrowse = createCarousel(
                createCarouselCard("Morning focus", "Deep work", "schedule", 280.0, 140.0),
                createCarouselCard("Design review", "Components", "edit", 280.0, 140.0),
                createCarouselCard("Release notes", "Packaging", "reports", 280.0, 140.0),
                createCarouselCard("Accessibility", "Keyboard", "visibility", 280.0, 140.0),
                createCarouselCard("Motion study", "Expressive", "spark", 280.0, 140.0),
                createCarouselCard("Color system", "Palettes", "image", 280.0, 140.0)
        );
        multiBrowse.setCarouselLayout(M3CarouselLayout.MULTI_BROWSE);
        multiBrowse.setMaxWidth(Double.MAX_VALUE);
        multiBrowse.selectFirst();

        M3Button showAll = new M3Button("Show all", M3ButtonVariant.TEXT);
        showAll.setOnAction(event -> context.showSnackbar("Open the complete carousel collection"));

        VBox multiBrowseSample = new VBox(4.0, multiBrowse, showAll);
        multiBrowseSample.setFillWidth(true);
        multiBrowseSample.setMaxWidth(Double.MAX_VALUE);

        M3Carousel hero = createCarousel(
                createCarouselCard("City guide", "Featured", "navigation", 320.0, 168.0),
                createCarouselCard("Architecture", "Collection", "dashboard", 320.0, 168.0),
                createCarouselCard("Landscape", "Weekend", "image", 320.0, 168.0),
                createCarouselCard("Portraits", "Stories", "person", 320.0, 168.0)
        );
        hero.setCarouselLayout(M3CarouselLayout.HERO);
        hero.setMaxWidth(Double.MAX_VALUE);
        hero.selectFirst();

        M3Carousel centerAlignedHero = createCarousel(
                createCarouselCard("Previous", "Preview", "back", 320.0, 168.0),
                createCarouselCard("Research", "Background", "search", 320.0, 168.0),
                createCarouselCard("Featured story", "Centered", "star", 320.0, 168.0),
                createCarouselCard("Gallery", "Related", "image", 320.0, 168.0),
                createCarouselCard("Next", "Preview", "chevron-right", 320.0, 168.0)
        );
        centerAlignedHero.setCarouselLayout(M3CarouselLayout.CENTER_ALIGNED_HERO);
        centerAlignedHero.setMaxWidth(Double.MAX_VALUE);
        centerAlignedHero.selectIndex(2);

        M3Carousel uncontained = createCarousel(
                createCarouselCard("Inbox", "24 unread", "inbox", 176.0, 112.0),
                createCarouselCard("Tasks", "6 due", "task", 176.0, 112.0),
                createCarouselCard("Files", "Recent", "folder", 176.0, 112.0),
                createCarouselCard("People", "Updates", "group", 176.0, 112.0),
                createCarouselCard("Calendar", "3 events", "calendar", 176.0, 112.0),
                createCarouselCard("Messages", "8 new", "email", 176.0, 112.0)
        );
        uncontained.setCarouselLayout(M3CarouselLayout.UNCONTAINED);
        uncontained.setMaxWidth(Double.MAX_VALUE);
        uncontained.selectFirst();

        M3Carousel multiAspectRatio = createCarousel(
                createCarouselCard("Morning focus", "Deep work", "schedule", 232.0, 140.0),
                createCarouselCard("Design review", "Components", "edit", 280.0, 140.0),
                createCarouselCard("Release notes", "Packaging", "reports", 196.0, 140.0),
                createCarouselCard("Mood board", "Inspiration", "image", 252.0, 140.0),
                createCarouselCard("Accessibility", "Keyboard", "visibility", 180.0, 140.0),
                createCarouselCard("Motion study", "Expressive", "spark", 264.0, 140.0)
        );
        multiAspectRatio.setCarouselLayout(M3CarouselLayout.UNCONTAINED_MULTI_ASPECT_RATIO);
        multiAspectRatio.setMaxWidth(Double.MAX_VALUE);
        multiAspectRatio.selectFirst();

        M3Carousel fullScreen = createCarousel(
                createCarouselCard("Workspace", "Edge-to-edge", "work", 320.0, 420.0),
                createCarouselCard("Timeline", "Project activity", "schedule", 320.0, 420.0),
                createCarouselCard("Insights", "Reporting", "reports", 320.0, 420.0)
        );
        fullScreen.setCarouselLayout(M3CarouselLayout.FULL_SCREEN);
        fullScreen.setPrefSize(320.0, 420.0);
        fullScreen.setMaxSize(320.0, 420.0);
        fullScreen.selectFirst();

        return createGallery(
                createFullWidthShowcaseGroup("Multi-browse", multiBrowseSample),
                createFullWidthShowcaseGroup("Hero", hero),
                createFullWidthShowcaseGroup("Center-aligned hero", centerAlignedHero),
                createFullWidthShowcaseGroup("Uncontained", uncontained),
                createFullWidthShowcaseGroup("Uncontained multi-aspect ratio", multiAspectRatio),
                createFullWidthShowcaseGroup("Full-screen", fullScreen)
        );
    }

    /// Creates a carousel sample with initial items.
    private static M3Carousel createCarousel(Node... items) {
        M3Carousel carousel = new M3Carousel();
        carousel.getItems().addAll(items);
        return carousel;
    }

    /// Creates a sample carousel card.
    private M3Card createCarouselCard(
            String title,
            String body,
            String iconName,
            double width,
            double height
    ) {
        VBox content = new VBox(4.0);
        content.getStyleClass().add("demo-carousel-card-content");
        content.setAlignment(Pos.CENTER);

        StackPane icon = createIconViewport(DemoIcons.secondary(iconName), 48.0);
        icon.getStyleClass().add("demo-carousel-card-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-card-title");
        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("demo-card-body");

        VBox copy = new VBox(2.0, titleLabel, bodyLabel);
        copy.getStyleClass().add("demo-carousel-card-copy");
        copy.setAlignment(Pos.CENTER);
        copy.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        content.getChildren().addAll(icon, spacer, copy);
        M3Card card = new M3Card(content, M3CardVariant.FILLED);
        card.getStyleClass().add("demo-carousel-card");
        card.setOnAction(event -> context.showSnackbar("Theme-aware snackbar"));
        card.setPrefSize(width, height);
        return card;
    }
}
