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
                        "Dividers",
                        "Dividers group related content with a subtle horizontal or vertical boundary.",
                        CatalogIcons.DIVIDER,
                        "divider",
                        "M3Divider",
                        CatalogComponents.example(
                                "Divider variants",
                                "Full-width, inset, middle-inset, and vertical dividers.",
                                false,
                                CatalogSamples::dividers
                        )
                ),
                CatalogComponents.component(
                        "Lists",
                        "Lists present vertically arranged rows of related content and actions.",
                        CatalogIcons.LIST,
                        "lists",
                        "M3ListPane",
                        CatalogComponents.example(
                                "Standard list",
                                "A continuous list with supporting and trailing content.",
                                false,
                                CatalogSamples::standardList
                        ),
                        CatalogComponents.example(
                                "Segmented list",
                                "Contained list rows separated by the Material segmented gap.",
                                true,
                                CatalogSamples::segmentedList
                        )
                ),
                CatalogComponents.component(
                        "Side sheets",
                        "Side sheets present supplementary content from a side edge without replacing the page.",
                        CatalogIcons.SIDE_SHEET,
                        "side-sheets",
                        "M3SideSheet",
                        CatalogComponents.example(
                                "Standard side sheet",
                                "A persistent supplementary surface with actions.",
                                false,
                                CatalogSamples::standardSideSheet
                        ),
                        CatalogComponents.example(
                                "Modal side sheet",
                                "A dismissible modal sheet coordinated with a scrim.",
                                false,
                                CatalogSamples::modalSideSheet
                        )
                )
        );
    }
}
