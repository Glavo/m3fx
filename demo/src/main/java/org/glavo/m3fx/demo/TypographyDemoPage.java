// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Typography component showcase page.
@NotNullByDefault
final class TypographyDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    TypographyDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the typography component page.
    Node createContent() {
        M3Text bodyLarge = new M3Text(
                "Body Large text follows the active theme typography tokens and wraps within the available page width.",
                M3TextRole.BODY_LARGE
        );
        bodyLarge.setWrapText(true);
        bodyLarge.setMinWidth(0.0);
        bodyLarge.setMaxWidth(Double.MAX_VALUE);

        return createGallery(
                createShowcaseGroup(
                        "Display",
                        new M3Text("Display Large", M3TextRole.DISPLAY_LARGE),
                        new M3Text("Display Medium", M3TextRole.DISPLAY_MEDIUM),
                        new M3Text("Display Small", M3TextRole.DISPLAY_SMALL)
                ),
                createShowcaseGroup(
                        "Headline",
                        new M3Text("Headline Large", M3TextRole.HEADLINE_LARGE),
                        new M3Text("Headline Medium", M3TextRole.HEADLINE_MEDIUM),
                        new M3Text("Headline Small", M3TextRole.HEADLINE_SMALL)
                ),
                createShowcaseGroup(
                        "Title",
                        new M3Text("Title Large", M3TextRole.TITLE_LARGE),
                        new M3Text("Title Medium", M3TextRole.TITLE_MEDIUM),
                        new M3Text("Title Small", M3TextRole.TITLE_SMALL)
                ),
                createFullWidthShowcaseGroup(
                        "Body",
                        bodyLarge,
                        new M3Text("Body Medium text", M3TextRole.BODY_MEDIUM),
                        new M3Text("Body Small text", M3TextRole.BODY_SMALL)
                ),
                createShowcaseGroup(
                        "Label",
                        new M3Text("Label Large", M3TextRole.LABEL_LARGE),
                        new M3Text("Label Medium", M3TextRole.LABEL_MEDIUM),
                        new M3Text("Label Small", M3TextRole.LABEL_SMALL)
                )
        );
    }
}
