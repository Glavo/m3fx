// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Supplies application-navigation entries for the Catalog registry.
@NotNullByDefault
final class CatalogNavigationComponents {
    /// Prevents utility class instantiation.
    private CatalogNavigationComponents() {
    }

    /// Creates the navigation component descriptors.
    ///
    /// @return the immutable descriptor list
    static @Unmodifiable List<CatalogComponent> create() {
        return List.of(
                CatalogComponents.component(
                        "Navigation bar",
                        "Navigation bars switch among primary destinations in compact windows.",
                        CatalogIcons.NAVIGATION_BAR,
                        "navigation-bar",
                        "M3NavigationBar",
                        CatalogComponents.example(
                                "Navigation bar",
                                "Three primary destinations with selection.",
                                false,
                                CatalogSamples::navigationBar
                        )
                ),
                CatalogComponents.component(
                        "Navigation drawer",
                        "Navigation drawers provide a vertical collection of primary destinations.",
                        CatalogIcons.NAVIGATION_DRAWER,
                        "navigation-drawer",
                        "M3NavigationDrawer",
                        CatalogComponents.example(
                                "Navigation drawer",
                                "A standard drawer with selected destination.",
                                false,
                                CatalogSamples::navigationDrawer
                        )
                ),
                CatalogComponents.component(
                        "Navigation rail",
                        "Navigation rails provide compact or expanded navigation for larger windows.",
                        CatalogIcons.NAVIGATION_RAIL,
                        "navigation-rail",
                        "M3NavigationRail",
                        CatalogComponents.example(
                                "Collapsed and expanded",
                                "Navigation rail presentations and selection.",
                                true,
                                CatalogSamples::navigationRail
                        )
                ),
                CatalogComponents.component(
                        "Tabs",
                        "Tabs switch between related peer views within one content area.",
                        CatalogIcons.TABS,
                        "tabs",
                        "M3TabBar",
                        CatalogComponents.example(
                                "Primary tabs",
                                "A selectable primary tab bar.",
                                false,
                                CatalogSamples::tabs
                        )
                ),
                CatalogComponents.component(
                        "Top app bars",
                        "Top app bars identify a view and expose navigation and trailing actions.",
                        CatalogIcons.TOP_APP_BAR,
                        "app-bars",
                        "M3TopAppBar",
                        CatalogComponents.example(
                                "Small top app bar",
                                "Navigation, title, and trailing actions.",
                                false,
                                CatalogSamples::topAppBar
                        )
                )
        );
    }
}
