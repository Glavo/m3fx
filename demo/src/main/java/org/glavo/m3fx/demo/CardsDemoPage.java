// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3AvatarVariant;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Cards component showcase page.
@NotNullByDefault
final class CardsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    CardsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the card component page.
    Node createContent() {
        M3Card filled = createSampleCard(
                "Filled card",
                "Status summary",
                "Use filled cards for related content on a higher emphasis container.",
                M3CardVariant.FILLED
        );
        M3Card outlined = createSampleCard(
                "Outlined card",
                "Document details",
                "Use outlined cards when the page already has stronger filled surfaces.",
                M3CardVariant.OUTLINED
        );
        M3Card elevated = createSampleCard(
                "Elevated card",
                "Pinned project",
                "Use elevated cards sparingly when separation from the page background matters.",
                M3CardVariant.ELEVATED
        );

        M3Card media = createMediaCard(
                "Media card",
                "Preview",
                "Use media cards for concise previews with clear supporting actions.",
                M3CardVariant.FILLED
        );
        M3Card elevatedMedia = createMediaCard(
                "Release candidate",
                "Passive container",
                "Cards with nested actions keep the container itself passive.",
                M3CardVariant.ELEVATED
        );
        M3Card outlinedMedia = createMediaCard(
                "Archived review",
                "Supporting actions",
                "Each nested action remains an independent keyboard and pointer target.",
                M3CardVariant.OUTLINED
        );

        M3Card dragged = createSampleCard(
                "Dragged card",
                "Reordering state",
                "Dragged cards use the published state layer and elevated container level.",
                M3CardVariant.ELEVATED
        );
        dragged.setDragged(true);

        M3Card disabled = createSampleCard(
                "Disabled card",
                "Unavailable",
                "Disabled cards retain their variant-specific container treatment without interaction feedback.",
                M3CardVariant.OUTLINED
        );
        disabled.setDisable(true);

        M3Card localColors = createSampleCard(
                "Local container",
                "Container override",
                "Content and interaction colors continue to follow the active theme.",
                M3CardVariant.FILLED
        );
        localColors.setContainerColor(Color.web("#FFF3E0"));

        return createGallery(
                createShowcaseGroup("Variants", filled, outlined, elevated),
                createShowcaseGroup("Passive Cards With Actions", media, elevatedMedia, outlinedMedia),
                createShowcaseGroup("States", dragged, disabled),
                createShowcaseGroup("Local Container Paint", localColors)
        );
    }

    /// Creates a compact sample card.
    private M3Card createSampleCard(String title, String overline, String body, M3CardVariant variant) {
        VBox content = createCardTextContent(title, overline, body);
        M3Card card = new M3Card(content, variant);
        card.setOnAction(event -> context.showSnackbar("Theme-aware snackbar"));
        card.getStyleClass().add("demo-card");
        card.setPrefSize(280.0, 168.0);
        return card;
    }

    /// Creates a sample card with media, supporting text, and actions.
    private M3Card createMediaCard(String title, String overline, String body, M3CardVariant variant) {
        VBox content = new VBox(12.0);
        content.getStyleClass().add("demo-card-content");

        Region media = new Region();
        media.getStyleClass().add("demo-card-media");
        media.setMinHeight(104.0);
        media.setPrefHeight(104.0);
        media.setMaxHeight(104.0);
        media.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox(12.0);
        header.getStyleClass().add("demo-card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        M3Avatar avatar = new M3Avatar(title.substring(0, 1));
        avatar.setVariant(M3AvatarVariant.SECONDARY);

        VBox text = createCardTextContent(title, overline, body);
        HBox.setHgrow(text, Priority.ALWAYS);

        header.getChildren().addAll(avatar, text);

        HBox actions = new HBox(8.0);
        actions.getStyleClass().add("demo-card-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        M3Button details = new M3Button("Details", M3ButtonVariant.TEXT);
        details.setOnAction(event -> context.showSnackbar("Open card details"));
        M3Button open = new M3Button("Open", M3ButtonVariant.TONAL);
        open.setOnAction(event -> context.showSnackbar("Open card content"));
        actions.getChildren().addAll(details, open);

        content.getChildren().addAll(media, header, actions);

        M3Card card = new M3Card(content, variant);
        card.getStyleClass().add("demo-card");
        card.setPrefSize(360.0, 300.0);
        return card;
    }

    /// Creates the text stack used by demo cards.
    private static VBox createCardTextContent(String title, String overline, String body) {
        Label overlineLabel = new Label(overline);
        overlineLabel.getStyleClass().add("demo-card-overline");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-card-title");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("demo-card-body");
        bodyLabel.setWrapText(true);

        VBox content = new VBox(4.0, overlineLabel, titleLabel, bodyLabel);
        content.getStyleClass().add("demo-card-text");
        return content;
    }
}
