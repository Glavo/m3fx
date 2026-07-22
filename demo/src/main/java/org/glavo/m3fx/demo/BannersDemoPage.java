// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Builds the Banners component showcase page.
@NotNullByDefault
final class BannersDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    BannersDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the banner component page.
    Node createContent() {
        M3Banner informational = createBanner(
                "M3FX can install generated token stylesheets for each JavaFX scene while keeping application scene management explicit.",
                createInfoIcon(),
                "Learn",
                "Dismiss"
        );

        M3Banner warning = createBanner(
                "The selected jlink target uses platform-specific BellSoft LibericaJDK Full jmods.",
                createErrorIcon("warning"),
                "Review"
        );

        M3Banner noIcon = createBanner(
                "Banners may omit the leading icon when surrounding context already makes the message clear.",
                null,
                "Manage"
        );

        M3Banner passive = createBanner(
                "Passive banners keep persistent contextual information visible without interrupting the current task.",
                null
        );

        M3Banner narrow = createBanner(
                "A narrow banner wraps longer text while keeping actions reachable.",
                createInfoIcon(),
                "Details",
                "Close"
        );
        configureResponsiveWidth(narrow, 420.0);

        return createGallery(
                createFullWidthShowcaseGroup("With Actions", informational, warning),
                createFullWidthShowcaseGroup("Without Icon", noIcon),
                createFullWidthShowcaseGroup("Passive", passive),
                createFullWidthShowcaseGroup("Responsive", narrow)
        );
    }

    /// Creates a sample banner for the page gallery.
    private M3Banner createBanner(String text, @Nullable Node icon, String... actionTexts) {
        M3Banner banner = new M3Banner(text);
        banner.setIcon(icon);
        configureResponsiveWidth(banner, 760.0);
        banner.getStyleClass().add("demo-banner");
        for (String actionText : actionTexts) {
            M3Button action = new M3Button(actionText, M3ButtonVariant.TEXT);
            action.setOnAction(event -> context.showSnackbar(actionText + " pressed"));
            banner.getActions().add(action);
        }
        return banner;
    }

    /// Creates a fixed viewport for a primary-colored icon slot.
    private static StackPane createInfoIcon() {
        return createIconViewport(DemoIcons.primary("info"));
    }
}
