// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.glavo.m3fx.controls.M3NavigationRailVariant;
import org.glavo.m3fx.controls.M3TabBarLayout;
import org.glavo.m3fx.controls.M3TabBarVariant;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
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
                CatalogComponents.extensionComponent(
                        "Breadcrumbs",
                        "Breadcrumbs show hierarchy and navigational context for the current location.",
                        CatalogIcons.BREADCRUMBS,
                        "https://opensource.adobe.com/spectrum-web-components/components/breadcrumbs/",
                        "M3Breadcrumbs",
                        breadcrumbExamples()
                ),
                CatalogComponents.component(
                        "Navigation bar",
                        "Navigation bars switch among primary destinations in compact windows.",
                        CatalogIcons.NAVIGATION_BAR,
                        "navigation-bar",
                        "M3NavigationBar",
                        navigationBarExamples()
                ),
                CatalogComponents.component(
                        "Navigation drawer",
                        "Navigation drawers provide a vertical collection of primary destinations.",
                        CatalogIcons.NAVIGATION_DRAWER,
                        "navigation-drawer",
                        "M3NavigationDrawer",
                        navigationDrawerExamples()
                ),
                CatalogComponents.component(
                        "Navigation rail",
                        "Navigation rails provide compact or expanded navigation for larger windows.",
                        CatalogIcons.NAVIGATION_RAIL,
                        "navigation-rail",
                        "M3NavigationRail",
                        navigationRailExamples()
                ),
                CatalogComponents.component(
                        "Tabs",
                        "Tabs switch between related peer views within one content area.",
                        CatalogIcons.TABS,
                        "tabs",
                        "M3TabBar",
                        tabExamples()
                ),
                CatalogComponents.component(
                        "Top app bars",
                        "Top app bars identify a view and expose navigation and trailing actions.",
                        CatalogIcons.TOP_APP_BAR,
                        "app-bars",
                        "M3TopAppBar",
                        topAppBarExamples()
                )
        );
    }

    /// Creates default, overflow, root-context, and compact breadcrumb examples.
    ///
    /// @return the complete breadcrumbs example array
    private static CatalogExample[] breadcrumbExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Default breadcrumbs",
                        "A three-level hierarchy displayed inline in reading order.",
                        false,
                        () -> CatalogNavigationSamples.breadcrumbs(3, false, false)
                ),
                CatalogComponents.example(
                        "Overflow breadcrumbs",
                        "A deep hierarchy whose earlier levels collapse into an accessible menu.",
                        false,
                        () -> CatalogNavigationSamples.breadcrumbs(6, false, false)
                ),
                CatalogComponents.example(
                        "Root context",
                        "An overflowing hierarchy that preserves its root item when space permits.",
                        false,
                        () -> CatalogNavigationSamples.breadcrumbs(6, true, false)
                ),
                CatalogComponents.example(
                        "Compact breadcrumbs",
                        "A compact hierarchy using reduced vertical metrics.",
                        false,
                        () -> CatalogNavigationSamples.breadcrumbs(4, false, true)
                )
        };
    }

    /// Creates compact, medium, and destination-count navigation bar examples.
    ///
    /// @return the complete navigation-bar example array
    private static CatalogExample[] navigationBarExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Compact navigation bar",
                        "Four vertically arranged destinations with a count badge.",
                        false,
                        () -> CatalogNavigationSamples.navigationBar(
                                4,
                                M3NavigationItemLayout.VERTICAL,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Medium navigation bar",
                        "Four horizontally arranged destinations for a wider window.",
                        true,
                        () -> CatalogNavigationSamples.navigationBar(
                                4,
                                M3NavigationItemLayout.HORIZONTAL,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Three destinations",
                        "A compact navigation bar with three primary destinations.",
                        false,
                        () -> CatalogNavigationSamples.navigationBar(
                                3,
                                M3NavigationItemLayout.VERTICAL,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Five destinations",
                        "A compact navigation bar with the maximum common destination count.",
                        false,
                        () -> CatalogNavigationSamples.navigationBar(
                                5,
                                M3NavigationItemLayout.VERTICAL,
                                false
                        )
                )
        };
    }

    /// Creates standard, modal, and grouped drawer examples.
    ///
    /// @return the complete navigation-drawer example array
    private static CatalogExample[] navigationDrawerExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Standard navigation drawer",
                        "A persistent drawer with dividers, selection, and a badge.",
                        false,
                        () -> CatalogNavigationSamples.navigationDrawer(
                                M3NavigationDrawerVariant.STANDARD,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Modal navigation drawer",
                        "A temporary drawer presented above application content and a scrim.",
                        false,
                        () -> CatalogNavigationSamples.navigationDrawer(
                                M3NavigationDrawerVariant.MODAL,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Grouped destinations",
                        "A standard drawer containing an expandable destination group.",
                        false,
                        () -> CatalogNavigationSamples.navigationDrawer(
                                M3NavigationDrawerVariant.STANDARD,
                                true
                        )
                )
        };
    }

    /// Creates collapsed, expanded, modal, narrow, and immersive navigation rail examples.
    ///
    /// @return the complete navigation-rail example array
    private static CatalogExample[] navigationRailExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Collapsed rail with action",
                        "A standard collapsed rail with navigation and a primary header action.",
                        false,
                        () -> CatalogNavigationSamples.navigationRail(
                                M3NavigationRailVariant.STANDARD,
                                false,
                                false,
                                false,
                                false,
                                false,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Narrow centered rail",
                        "A narrow collapsed rail with centered destinations.",
                        true,
                        () -> CatalogNavigationSamples.navigationRail(
                                M3NavigationRailVariant.STANDARD,
                                false,
                                true,
                                true,
                                false,
                                false,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Expanded standard rail",
                        "A persistent expanded rail with horizontal destination items.",
                        true,
                        () -> CatalogNavigationSamples.navigationRail(
                                M3NavigationRailVariant.STANDARD,
                                true,
                                false,
                                false,
                                false,
                                false,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Expanded rail with action",
                        "An expanded persistent rail retaining its primary header action.",
                        true,
                        () -> CatalogNavigationSamples.navigationRail(
                                M3NavigationRailVariant.STANDARD,
                                true,
                                false,
                                false,
                                false,
                                false,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Modal navigation rail",
                        "A temporary expanded rail using modal container and elevation tokens.",
                        true,
                        () -> CatalogNavigationSamples.navigationRail(
                                M3NavigationRailVariant.MODAL,
                                true,
                                false,
                                false,
                                false,
                                false,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Full-width modal indicators",
                        "An expanded modal rail whose active indicator spans the container.",
                        true,
                        () -> CatalogNavigationSamples.navigationRail(
                                M3NavigationRailVariant.MODAL,
                                true,
                                false,
                                false,
                                true,
                                false,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Immersive navigation rail",
                        "A modal rail configured to disappear when collapsed.",
                        true,
                        () -> CatalogNavigationSamples.navigationRail(
                                M3NavigationRailVariant.MODAL,
                                true,
                                false,
                                false,
                                true,
                                true,
                                false
                        )
                )
        };
    }

    /// Creates fixed, scrollable, and hierarchical tab examples.
    ///
    /// @return the complete tab example array
    private static CatalogExample[] tabExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Primary fixed tabs",
                        "Equal-width primary tabs for a compact set of peers.",
                        false,
                        () -> CatalogNavigationSamples.tabBar(
                                M3TabBarVariant.PRIMARY,
                                M3TabBarLayout.FIXED,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Secondary fixed tabs",
                        "Secondary tabs including a disabled destination.",
                        false,
                        () -> CatalogNavigationSamples.tabBar(
                                M3TabBarVariant.SECONDARY,
                                M3TabBarLayout.FIXED,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Primary scrollable tabs",
                        "A longer primary destination set that scrolls horizontally.",
                        false,
                        () -> CatalogNavigationSamples.tabBar(
                                M3TabBarVariant.PRIMARY,
                                M3TabBarLayout.SCROLLABLE,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Secondary scrollable tabs",
                        "A longer secondary destination set that scrolls horizontally.",
                        false,
                        () -> CatalogNavigationSamples.tabBar(
                                M3TabBarVariant.SECONDARY,
                                M3TabBarLayout.SCROLLABLE,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Primary and secondary hierarchy",
                        "Stacked tab bars representing two related hierarchy levels.",
                        false,
                        CatalogNavigationSamples::tabHierarchy
                )
        };
    }

    /// Creates baseline and expressive top app bar examples.
    ///
    /// @return the complete top-app-bar example array
    private static CatalogExample[] topAppBarExamples() {
        return new CatalogExample[]{
                topAppBarExample("Small top app bar", "Baseline compact app bar.", M3TopAppBarVariant.SMALL, false, false),
                topAppBarExample(
                        "Center-aligned top app bar",
                        "A compact app bar with centered title text.",
                        M3TopAppBarVariant.CENTER_ALIGNED,
                        false,
                        false
                ),
                topAppBarExample(
                        "Medium top app bar",
                        "Baseline medium hierarchy treatment.",
                        M3TopAppBarVariant.MEDIUM,
                        false,
                        false
                ),
                topAppBarExample(
                        "Large top app bar",
                        "Baseline large hierarchy treatment.",
                        M3TopAppBarVariant.LARGE,
                        false,
                        false
                ),
                topAppBarExample(
                        "Medium flexible top app bar",
                        "Material Expressive medium flexible arrangement.",
                        M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                        false,
                        false
                ),
                topAppBarExample(
                        "Large flexible top app bar",
                        "Material Expressive large flexible arrangement.",
                        M3TopAppBarVariant.LARGE_FLEXIBLE,
                        false,
                        false
                ),
                topAppBarExample(
                        "Medium flexible with subtitle",
                        "A medium flexible bar with supporting subtitle text.",
                        M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                        true,
                        false
                ),
                topAppBarExample(
                        "Large flexible with subtitle",
                        "A large flexible bar with supporting subtitle text.",
                        M3TopAppBarVariant.LARGE_FLEXIBLE,
                        true,
                        false
                ),
                topAppBarExample(
                        "Collapsed medium flexible",
                        "A medium flexible bar after content scrolls beneath it.",
                        M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                        false,
                        true
                ),
                topAppBarExample(
                        "Collapsed large flexible",
                        "A large flexible bar with subtitle after content scrolls beneath it.",
                        M3TopAppBarVariant.LARGE_FLEXIBLE,
                        true,
                        true
                )
        };
    }

    /// Creates one top-app-bar example descriptor.
    ///
    /// @param name the example name
    /// @param description the example description
    /// @param variant the top app bar variant
    /// @param subtitle whether a subtitle is present
    /// @param scrolledUnder whether the scrolled-under state is active
    /// @return the example descriptor
    private static CatalogExample topAppBarExample(
            String name,
            String description,
            M3TopAppBarVariant variant,
            boolean subtitle,
            boolean scrolledUnder
    ) {
        return CatalogComponents.example(
                name,
                description,
                variant == M3TopAppBarVariant.MEDIUM_FLEXIBLE
                        || variant == M3TopAppBarVariant.LARGE_FLEXIBLE,
                () -> CatalogNavigationSamples.topAppBar(variant, subtitle, scrolledUnder)
        );
    }
}
