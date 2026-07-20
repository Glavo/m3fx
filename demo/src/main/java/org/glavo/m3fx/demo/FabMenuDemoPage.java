// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the FabMenu component showcase page.
@NotNullByDefault
final class FabMenuDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    FabMenuDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the floating action button menu component page.
    Node createContent() {
        M3FabMenu expanded = createFabMenu();
        expanded.setExpanded(true);

        M3FabMenu collapsed = createFabMenu();
        M3FabMenu secondary = createFabMenu(
                M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                M3FloatingActionButtonVariant.SECONDARY_CONTAINER
        );
        secondary.setExpanded(true);
        M3FabMenu tertiary = createFabMenu(
                M3FloatingActionButtonVariant.TERTIARY_CONTAINER,
                M3FloatingActionButtonVariant.TERTIARY_CONTAINER
        );
        tertiary.setExpanded(true);

        return createGallery(
                createShowcaseGroup("Expanded", expanded),
                createShowcaseGroup("Collapsed", collapsed),
                createShowcaseGroup("Color Families", secondary, tertiary)
        );
    }
}
