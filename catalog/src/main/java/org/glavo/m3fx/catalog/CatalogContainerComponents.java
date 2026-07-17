// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Supplies container and structural entries for the Catalog registry.
@NotNullByDefault
final class CatalogContainerComponents {
    /// Prevents utility class instantiation.
    private CatalogContainerComponents() {
    }

    /// Creates the container component descriptors.
    ///
    /// @return the immutable descriptor list
    static @Unmodifiable List<CatalogComponent> create() {
        return List.of(
                CatalogComponents.component(
                        "Badges",
                        "Badges show notifications, counts, or compact status information attached to another element.",
                        CatalogIcons.NOTIFICATIONS,
                        "badges",
                        "M3Badge",
                        CatalogComponents.example(
                                "Count badge",
                                "A count badge attached to an actionable icon.",
                                false,
                                CatalogSamples::badge
                        )
                ),
                CatalogComponents.component(
                        "Bottom app bars",
                        "Bottom app bars place navigation and key actions at the lower edge of a window.",
                        CatalogIcons.BOTTOM_APP_BAR,
                        "bottom-app-bar",
                        "M3BottomAppBar",
                        CatalogComponents.example(
                                "Bottom app bar",
                                "Actions with a prominent floating action.",
                                false,
                                CatalogSamples::bottomAppBar
                        )
                ),
                CatalogComponents.component(
                        "Bottom sheets",
                        "Bottom sheets contain supplementary content anchored to the bottom edge.",
                        CatalogIcons.BOTTOM_SHEET,
                        "bottom-sheets",
                        "M3BottomSheet",
                        CatalogComponents.example(
                                "Standard sheet",
                                "A standard sheet with local show and hide actions.",
                                false,
                                CatalogSamples::bottomSheet
                        )
                ),
                CatalogComponents.component(
                        "Cards",
                        "Cards group related information and may optionally act as a single action target.",
                        CatalogIcons.CARD,
                        "cards",
                        "M3Card",
                        CatalogComponents.example(
                                "Card variants",
                                "Filled, outlined, elevated, and actionable cards.",
                                false,
                                CatalogSamples::cards
                        )
                ),
                CatalogComponents.component(
                        "Carousel",
                        "Carousels present a horizontally browsable sequence with selection and snapping.",
                        CatalogIcons.CAROUSEL,
                        "carousel",
                        "M3Carousel",
                        CatalogComponents.example(
                                "Multi-browse carousel",
                                "A multi-item carousel with direct navigation.",
                                false,
                                CatalogSamples::carousel
                        )
                ),
                CatalogComponents.component(
                        "Dialogs",
                        "Dialogs interrupt a workflow to request a decision or present focused information.",
                        CatalogIcons.DIALOG,
                        "dialogs",
                        "M3DialogPane",
                        CatalogComponents.example(
                                "Basic dialog",
                                "An inline preview of dialog content and actions.",
                                false,
                                CatalogSamples::dialog
                        )
                ),
                CatalogComponents.component(
                        "Lists",
                        "Lists present vertically arranged rows of related content and actions.",
                        CatalogIcons.LIST,
                        "lists",
                        "M3ListPane",
                        CatalogComponents.example(
                                "Segmented list",
                                "List rows with leading and trailing content.",
                                true,
                                CatalogSamples::lists
                        )
                )
        );
    }
}
