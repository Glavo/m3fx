// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the ExtendedFabs component showcase page.
@NotNullByDefault
final class ExtendedFabsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ExtendedFabsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the extended floating action button component page.
    Node createContent() {
        return createGallery(
                createShowcaseGroup(
                        "Expressive Sizes",
                        createExtendedFab(
                                "Create",
                                "add",
                                M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Compose",
                                "edit",
                                M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                                M3FloatingActionButtonSize.MEDIUM
                        ),
                        createExtendedFab(
                                "Favorite",
                                "favorite",
                                M3FloatingActionButtonVariant.TERTIARY_CONTAINER,
                                M3FloatingActionButtonSize.LARGE
                        )
                ),
                createShowcaseGroup(
                        "Tonal Colors",
                        createExtendedFab(
                                "Primary",
                                "add",
                                M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Secondary",
                                "upload",
                                M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Tertiary",
                                "favorite",
                                M3FloatingActionButtonVariant.TERTIARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        )
                ),
                createShowcaseGroup(
                        "Solid Colors",
                        createExtendedFab(
                                "Primary",
                                "add",
                                M3FloatingActionButtonVariant.PRIMARY,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Secondary",
                                "upload",
                                M3FloatingActionButtonVariant.SECONDARY,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Tertiary",
                                "favorite",
                                M3FloatingActionButtonVariant.TERTIARY,
                                M3FloatingActionButtonSize.REGULAR
                        )
                ),
                createShowcaseGroup(
                        "Baseline Surface",
                        createExtendedFab(
                                "Surface",
                                "edit",
                                M3FloatingActionButtonVariant.SURFACE,
                                M3FloatingActionButtonSize.REGULAR
                        )
                )
        );
    }
}
