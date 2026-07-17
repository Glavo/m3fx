// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Supplies action-oriented entries for the Catalog registry.
@NotNullByDefault
final class CatalogActionComponents {
    /// Prevents utility class instantiation.
    private CatalogActionComponents() {
    }

    /// Creates the action component descriptors.
    ///
    /// @return the immutable descriptor list
    static @Unmodifiable List<CatalogComponent> create() {
        return List.of(
                CatalogComponents.component(
                        "Button groups",
                        "Button groups arrange related actions with standard or connected geometry.",
                        CatalogIcons.BUTTON_GROUP,
                        "button-groups",
                        "M3ButtonGroup",
                        CatalogComponents.example(
                                "Standard and connected",
                                "Related actions in both group variants.",
                                true,
                                CatalogSamples::buttonGroups
                        )
                ),
                CatalogComponents.component(
                        "Buttons",
                        "Buttons initiate actions with emphasis conveyed by their container and color treatment.",
                        CatalogIcons.TOUCH_APP,
                        "buttons",
                        "M3Button",
                        CatalogComponents.example(
                                "Button variants",
                                "Filled, tonal, outlined, text, and elevated actions.",
                                false,
                                CatalogSamples::buttons
                        )
                ),
                CatalogComponents.component(
                        "Extended FABs",
                        "Extended floating action buttons combine a prominent icon and text label.",
                        CatalogIcons.EXTENDED_FAB,
                        "extended-fab",
                        "M3FloatingActionButton",
                        CatalogComponents.example(
                                "Extended FABs",
                                "Prominent primary actions with labels.",
                                true,
                                CatalogSamples::extendedFabs
                        )
                ),
                CatalogComponents.component(
                        "FAB menu",
                        "FAB menus reveal a small set of related floating actions from one primary control.",
                        CatalogIcons.FAB_MENU,
                        "fab-menu",
                        "M3FabMenu",
                        CatalogComponents.example(
                                "Expanded FAB menu",
                                "A primary FAB with revealed actions.",
                                true,
                                CatalogSamples::fabMenu
                        )
                ),
                CatalogComponents.component(
                        "Floating action buttons",
                        "Floating action buttons emphasize the most important action in a view.",
                        CatalogIcons.FLOATING_ACTION,
                        "floating-action-button",
                        "M3FloatingActionButton",
                        CatalogComponents.example(
                                "FAB sizes",
                                "Floating actions across the Material size scale.",
                                true,
                                CatalogSamples::floatingActionButtons
                        )
                ),
                CatalogComponents.component(
                        "Floating toolbars",
                        "Floating toolbars collect contextual actions in a compact movable surface.",
                        CatalogIcons.TOOLBAR,
                        "toolbars",
                        "M3Toolbar",
                        CatalogComponents.example(
                                "Floating toolbar",
                                "A centered contextual action toolbar.",
                                true,
                                CatalogSamples::floatingToolbar
                        )
                ),
                CatalogComponents.component(
                        "Icon buttons",
                        "Icon buttons expose familiar compact actions and optional toggle selection.",
                        CatalogIcons.FAVORITE,
                        "icon-buttons",
                        "M3IconButton",
                        CatalogComponents.example(
                                "Icon and toggle buttons",
                                "Standard icon actions and selectable icon actions.",
                                true,
                                CatalogSamples::iconButtons
                        )
                ),
                CatalogComponents.component(
                        "Menus",
                        "Menus expose a temporary list of choices from a button or contextual target.",
                        CatalogIcons.MORE_VERTICAL,
                        "menus",
                        "M3MenuButton",
                        CatalogComponents.example(
                                "Menu button",
                                "A button that owns and presents a Material menu.",
                                false,
                                CatalogSamples::menus
                        )
                ),
                CatalogComponents.component(
                        "Split buttons",
                        "Split buttons combine a primary action with a menu of related alternatives.",
                        CatalogIcons.SPLIT_BUTTON,
                        "split-button",
                        "M3SplitButton",
                        CatalogComponents.example(
                                "Filled and outlined",
                                "Primary actions with attached alternative menus.",
                                true,
                                CatalogSamples::splitButtons
                        )
                )
        );
    }
}
