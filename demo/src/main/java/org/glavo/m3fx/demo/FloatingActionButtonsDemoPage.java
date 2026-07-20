// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the FloatingActionButtons component showcase page.
@NotNullByDefault
final class FloatingActionButtonsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    FloatingActionButtonsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the floating action button component page.
    Node createContent() {
        return createGallery(
                createShowcaseGroup(
                        "Sizes With Icons",
                        createFab("add", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.SMALL),
                        createFab("edit", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.REGULAR),
                        createFab("share", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.MEDIUM),
                        createFab("navigation", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.LARGE)
                ),
                createShowcaseGroup(
                        "Tonal Colors",
                        createFab("add", M3FloatingActionButtonVariant.SURFACE, M3FloatingActionButtonSize.REGULAR),
                        createFab("edit", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.REGULAR),
                        createFab("share", M3FloatingActionButtonVariant.SECONDARY_CONTAINER, M3FloatingActionButtonSize.REGULAR),
                        createFab("favorite", M3FloatingActionButtonVariant.TERTIARY_CONTAINER, M3FloatingActionButtonSize.REGULAR)
                ),
                createShowcaseGroup(
                        "Solid Colors",
                        createFab("edit", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.REGULAR),
                        createFab("share", M3FloatingActionButtonVariant.SECONDARY, M3FloatingActionButtonSize.REGULAR),
                        createFab("favorite", M3FloatingActionButtonVariant.TERTIARY, M3FloatingActionButtonSize.REGULAR)
                )
        );
    }
}
