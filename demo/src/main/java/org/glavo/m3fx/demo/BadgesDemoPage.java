// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3BadgedBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Badges component showcase page.
@NotNullByDefault
final class BadgesDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    BadgesDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the badge component page.
    Node createContent() {
        M3Button button = new M3Button("Inbox", M3ButtonVariant.TONAL);
        M3BadgedBox buttonWithBadge = new M3BadgedBox(button, new M3Badge("9"));

        return createGallery(
                createShowcaseGroup("Badges", new M3Badge(), new M3Badge("7"), new M3Badge("1234")),
                createShowcaseGroup("Attached", buttonWithBadge)
        );
    }
}
