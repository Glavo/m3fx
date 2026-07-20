// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3AvatarVariant;
import org.glavo.m3fx.controls.M3ListItem;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Avatars component showcase page.
@NotNullByDefault
final class AvatarsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    AvatarsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the avatar component page.
    Node createContent() {
        M3Avatar initials = new M3Avatar("AB");
        M3Avatar single = new M3Avatar("M");
        single.setVariant(M3AvatarVariant.SECONDARY);
        M3Avatar graphic = new M3Avatar(createNavigationIcon("person"));
        graphic.setVariant(M3AvatarVariant.TERTIARY);
        M3Avatar surface = new M3Avatar("S");
        surface.setVariant(M3AvatarVariant.SURFACE);

        M3ListItem account = new M3ListItem("Account");
        account.setSupportingText("Avatar as leading content");
        account.setLeading(new M3Avatar("A"));

        return createGallery(
                createShowcaseGroup("Avatars", initials, single, graphic, surface),
                createShowcaseGroup("List Usage", account)
        );
    }
}
