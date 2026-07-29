// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Orientation;
import org.glavo.m3fx.controls.M3ButtonGroupVariant;
import org.glavo.m3fx.controls.M3ButtonShape;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3IconButtonWidth;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3MenuColorStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3ToolbarColorStyle;
import org.glavo.m3fx.controls.M3ToolbarVariant;
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
                                "Standard actions",
                                "Independent tonal actions arranged with standard spacing.",
                                true,
                                () -> CatalogActionSamples.buttonGroup(
                                        M3ButtonGroupVariant.STANDARD,
                                        M3ButtonSize.MEDIUM,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Standard single select",
                                "Selectable icon actions with standard group spacing.",
                                true,
                                () -> CatalogActionSamples.buttonGroup(
                                        M3ButtonGroupVariant.STANDARD,
                                        M3ButtonSize.MEDIUM,
                                        true,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Connected labeled actions",
                                "Labeled actions joined into a connected group.",
                                true,
                                () -> CatalogActionSamples.buttonGroup(
                                        M3ButtonGroupVariant.CONNECTED,
                                        M3ButtonSize.MEDIUM,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Connected single select",
                                "A connected group in which one icon action remains selected.",
                                true,
                                () -> CatalogActionSamples.buttonGroup(
                                        M3ButtonGroupVariant.CONNECTED,
                                        M3ButtonSize.MEDIUM,
                                        true,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Connected multi select",
                                "Connected icon actions with independent selection.",
                                true,
                                () -> CatalogActionSamples.buttonGroup(
                                        M3ButtonGroupVariant.CONNECTED,
                                        M3ButtonSize.MEDIUM,
                                        true,
                                        true
                                )
                        ),
                        CatalogComponents.example(
                                "Small group",
                                "A compact standard group using the small size role.",
                                true,
                                () -> CatalogActionSamples.buttonGroup(
                                        M3ButtonGroupVariant.STANDARD,
                                        M3ButtonSize.SMALL,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Large group",
                                "A prominent standard group using the large size role.",
                                true,
                                () -> CatalogActionSamples.buttonGroup(
                                        M3ButtonGroupVariant.STANDARD,
                                        M3ButtonSize.LARGE,
                                        false,
                                        false
                                )
                        )
                ),
                CatalogComponents.component(
                        "Buttons",
                        "Buttons initiate actions with emphasis conveyed by their container and color treatment.",
                        CatalogIcons.TOUCH_APP,
                        "buttons",
                        "M3Button",
                        CatalogComponents.example(
                                "Variant overview",
                                "Filled, tonal, outlined, text, and elevated actions.",
                                false,
                                CatalogSamples::buttons
                        ),
                        CatalogComponents.example(
                                "Filled button",
                                "High-emphasis filled button in enabled and disabled states.",
                                false,
                                () -> CatalogActionSamples.buttonVariant(M3ButtonVariant.FILLED)
                        ),
                        CatalogComponents.example(
                                "Filled tonal button",
                                "Medium-emphasis tonal button in enabled and disabled states.",
                                false,
                                () -> CatalogActionSamples.buttonVariant(M3ButtonVariant.TONAL)
                        ),
                        CatalogComponents.example(
                                "Outlined button",
                                "Outlined button in enabled and disabled states.",
                                false,
                                () -> CatalogActionSamples.buttonVariant(M3ButtonVariant.OUTLINED)
                        ),
                        CatalogComponents.example(
                                "Text button",
                                "Low-emphasis text button in enabled and disabled states.",
                                false,
                                () -> CatalogActionSamples.buttonVariant(M3ButtonVariant.TEXT)
                        ),
                        CatalogComponents.example(
                                "Elevated button",
                                "Elevated button in enabled and disabled states.",
                                false,
                                () -> CatalogActionSamples.buttonVariant(M3ButtonVariant.ELEVATED)
                        ),
                        CatalogComponents.example(
                                "Extra-small buttons",
                                "Buttons using the 32-pixel expressive size.",
                                true,
                                () -> CatalogActionSamples.buttonSize(M3ButtonSize.EXTRA_SMALL)
                        ),
                        CatalogComponents.example(
                                "Small buttons",
                                "Buttons using the baseline 40-pixel size.",
                                false,
                                () -> CatalogActionSamples.buttonSize(M3ButtonSize.SMALL)
                        ),
                        CatalogComponents.example(
                                "Medium buttons",
                                "Buttons using the 56-pixel expressive size.",
                                true,
                                () -> CatalogActionSamples.buttonSize(M3ButtonSize.MEDIUM)
                        ),
                        CatalogComponents.example(
                                "Large buttons",
                                "Buttons using the 96-pixel expressive size.",
                                true,
                                () -> CatalogActionSamples.buttonSize(M3ButtonSize.LARGE)
                        ),
                        CatalogComponents.example(
                                "Extra-large buttons",
                                "Buttons using the 136-pixel expressive size.",
                                true,
                                () -> CatalogActionSamples.buttonSize(M3ButtonSize.EXTRA_LARGE)
                        ),
                        CatalogComponents.example(
                                "Round buttons",
                                "Medium buttons with fully round resting containers.",
                                true,
                                () -> CatalogActionSamples.buttonShape(M3ButtonShape.ROUND)
                        ),
                        CatalogComponents.example(
                                "Square buttons",
                                "Medium buttons with expressive rounded-square containers.",
                                true,
                                () -> CatalogActionSamples.buttonShape(M3ButtonShape.SQUARE)
                        ),
                        CatalogComponents.example(
                                "Medium button with icon",
                                "A medium tonal button with a leading action icon.",
                                true,
                                () -> CatalogActionSamples.buttonWithIcon(
                                        M3ButtonSize.MEDIUM,
                                        M3ButtonShape.ROUND
                                )
                        ),
                        CatalogComponents.example(
                                "Large square button with icon",
                                "A large expressive button combining a square container and leading icon.",
                                true,
                                () -> CatalogActionSamples.buttonWithIcon(
                                        M3ButtonSize.LARGE,
                                        M3ButtonShape.SQUARE
                                )
                        ),
                        CatalogComponents.example(
                                "Local colors",
                                "A filled action whose colors override the active theme locally.",
                                false,
                                CatalogActionSamples::buttonLocalColors
                        ),
                        CatalogComponents.example(
                                "Action feedback",
                                "An interactive button that updates local result text.",
                                false,
                                CatalogActionSamples::buttonInteraction
                        )
                ),
                CatalogComponents.component(
                        "Extended FABs",
                        "Extended floating action buttons combine a prominent icon and text label.",
                        CatalogIcons.EXTENDED_FAB,
                        "extended-fab",
                        "M3FloatingActionButton",
                        CatalogComponents.example(
                                "Surface extended FAB",
                                "Baseline surface color treatment with a label and icon.",
                                false,
                                () -> CatalogActionSamples.extendedFabVariant(
                                        M3FloatingActionButtonVariant.SURFACE
                                )
                        ),
                        CatalogComponents.example(
                                "Primary tonal extended FAB",
                                "Extended FAB using the primary-container color role.",
                                true,
                                () -> CatalogActionSamples.extendedFabVariant(
                                        M3FloatingActionButtonVariant.PRIMARY_CONTAINER
                                )
                        ),
                        CatalogComponents.example(
                                "Secondary tonal extended FAB",
                                "Extended FAB using the secondary-container color role.",
                                true,
                                () -> CatalogActionSamples.extendedFabVariant(
                                        M3FloatingActionButtonVariant.SECONDARY_CONTAINER
                                )
                        ),
                        CatalogComponents.example(
                                "Tertiary tonal extended FAB",
                                "Extended FAB using the tertiary-container color role.",
                                true,
                                () -> CatalogActionSamples.extendedFabVariant(
                                        M3FloatingActionButtonVariant.TERTIARY_CONTAINER
                                )
                        ),
                        CatalogComponents.example(
                                "Primary extended FAB",
                                "High-emphasis extended FAB using the primary color role.",
                                true,
                                () -> CatalogActionSamples.extendedFabVariant(
                                        M3FloatingActionButtonVariant.PRIMARY
                                )
                        ),
                        CatalogComponents.example(
                                "Secondary extended FAB",
                                "High-emphasis extended FAB using the secondary color role.",
                                true,
                                () -> CatalogActionSamples.extendedFabVariant(
                                        M3FloatingActionButtonVariant.SECONDARY
                                )
                        ),
                        CatalogComponents.example(
                                "Tertiary extended FAB",
                                "High-emphasis extended FAB using the tertiary color role.",
                                true,
                                () -> CatalogActionSamples.extendedFabVariant(
                                        M3FloatingActionButtonVariant.TERTIARY
                                )
                        ),
                        CatalogComponents.example(
                                "Small extended FAB",
                                "Extended FAB using the smallest FAB metrics.",
                                false,
                                () -> CatalogActionSamples.extendedFabSize(M3FloatingActionButtonSize.SMALL)
                        ),
                        CatalogComponents.example(
                                "Regular extended FAB",
                                "Extended FAB using the default size metrics.",
                                false,
                                () -> CatalogActionSamples.extendedFabSize(M3FloatingActionButtonSize.REGULAR)
                        ),
                        CatalogComponents.example(
                                "Medium extended FAB",
                                "Extended FAB using the medium expressive size.",
                                true,
                                () -> CatalogActionSamples.extendedFabSize(M3FloatingActionButtonSize.MEDIUM)
                        ),
                        CatalogComponents.example(
                                "Large extended FAB",
                                "Extended FAB using the large expressive size.",
                                true,
                                () -> CatalogActionSamples.extendedFabSize(M3FloatingActionButtonSize.LARGE)
                        ),
                        CatalogComponents.example(
                                "Disabled extended FAB",
                                "An unavailable labeled floating action.",
                                true,
                                CatalogActionSamples::disabledExtendedFab
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
                                () -> CatalogActionSamples.fabMenu(
                                        true,
                                        M3FloatingActionButtonVariant.PRIMARY_CONTAINER
                                )
                        ),
                        CatalogComponents.example(
                                "Collapsed FAB menu",
                                "A FAB menu before its related actions are revealed.",
                                true,
                                () -> CatalogActionSamples.fabMenu(
                                        false,
                                        M3FloatingActionButtonVariant.PRIMARY_CONTAINER
                                )
                        ),
                        CatalogComponents.example(
                                "Tertiary FAB menu",
                                "An expanded FAB menu using the tertiary color family.",
                                true,
                                () -> CatalogActionSamples.fabMenu(
                                        true,
                                        M3FloatingActionButtonVariant.TERTIARY_CONTAINER
                                )
                        )
                ),
                CatalogComponents.component(
                        "Floating action buttons",
                        "Floating action buttons emphasize the most important action in a view.",
                        CatalogIcons.FLOATING_ACTION,
                        "floating-action-button",
                        "M3FloatingActionButton",
                        CatalogComponents.example(
                                "Size overview",
                                "Floating actions across the Material size scale.",
                                true,
                                CatalogSamples::floatingActionButtons
                        ),
                        CatalogComponents.example(
                                "Small FAB",
                                "A compact 40-pixel floating action button.",
                                false,
                                () -> CatalogActionSamples.floatingActionButton(
                                        M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                        M3FloatingActionButtonSize.SMALL,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Regular FAB",
                                "The default 56-pixel floating action button.",
                                false,
                                () -> CatalogActionSamples.floatingActionButton(
                                        M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                        M3FloatingActionButtonSize.REGULAR,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Medium FAB",
                                "The 80-pixel expressive floating action button.",
                                true,
                                () -> CatalogActionSamples.floatingActionButton(
                                        M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                        M3FloatingActionButtonSize.MEDIUM,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Large FAB",
                                "The prominent 96-pixel floating action button.",
                                true,
                                () -> CatalogActionSamples.floatingActionButton(
                                        M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                        M3FloatingActionButtonSize.LARGE,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Surface FAB",
                                "Baseline surface-container treatment.",
                                false,
                                () -> CatalogActionSamples.floatingActionButton(
                                        M3FloatingActionButtonVariant.SURFACE,
                                        M3FloatingActionButtonSize.REGULAR,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Primary FAB",
                                "High-emphasis primary color treatment.",
                                true,
                                () -> CatalogActionSamples.floatingActionButton(
                                        M3FloatingActionButtonVariant.PRIMARY,
                                        M3FloatingActionButtonSize.REGULAR,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Secondary FAB",
                                "High-emphasis secondary color treatment.",
                                true,
                                () -> CatalogActionSamples.floatingActionButton(
                                        M3FloatingActionButtonVariant.SECONDARY,
                                        M3FloatingActionButtonSize.REGULAR,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Tertiary FAB",
                                "High-emphasis tertiary color treatment.",
                                true,
                                () -> CatalogActionSamples.floatingActionButton(
                                        M3FloatingActionButtonVariant.TERTIARY,
                                        M3FloatingActionButtonSize.REGULAR,
                                        false
                                )
                        )
                ),
                CatalogComponents.component(
                        "Floating toolbars",
                        "Floating toolbars collect contextual actions in a compact movable surface.",
                        CatalogIcons.TOOLBAR,
                        "toolbars",
                        "M3Toolbar",
                        CatalogComponents.example(
                                "Floating standard",
                                "A horizontal floating toolbar using surface colors.",
                                true,
                                () -> CatalogActionSamples.toolbar(
                                        M3ToolbarVariant.FLOATING,
                                        M3ToolbarColorStyle.STANDARD,
                                        Orientation.HORIZONTAL,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Floating vibrant",
                                "A horizontal floating toolbar using vibrant colors.",
                                true,
                                () -> CatalogActionSamples.toolbar(
                                        M3ToolbarVariant.FLOATING,
                                        M3ToolbarColorStyle.VIBRANT,
                                        Orientation.HORIZONTAL,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Docked standard",
                                "A full-width docked toolbar using surface colors.",
                                true,
                                () -> CatalogActionSamples.toolbar(
                                        M3ToolbarVariant.DOCKED,
                                        M3ToolbarColorStyle.STANDARD,
                                        Orientation.HORIZONTAL,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Docked vibrant",
                                "A full-width docked toolbar using vibrant colors.",
                                true,
                                () -> CatalogActionSamples.toolbar(
                                        M3ToolbarVariant.DOCKED,
                                        M3ToolbarColorStyle.VIBRANT,
                                        Orientation.HORIZONTAL,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Floating toolbar with FAB",
                                "A contextual toolbar paired with a related floating action.",
                                true,
                                () -> CatalogActionSamples.toolbar(
                                        M3ToolbarVariant.FLOATING,
                                        M3ToolbarColorStyle.STANDARD,
                                        Orientation.HORIZONTAL,
                                        true
                                )
                        ),
                        CatalogComponents.example(
                                "Vertical floating toolbar",
                                "A vibrant floating toolbar arranged vertically.",
                                true,
                                () -> CatalogActionSamples.toolbar(
                                        M3ToolbarVariant.FLOATING,
                                        M3ToolbarColorStyle.VIBRANT,
                                        Orientation.VERTICAL,
                                        false
                                )
                        )
                ),
                CatalogComponents.component(
                        "Icon buttons",
                        "Icon buttons expose familiar compact actions and optional toggle selection.",
                        CatalogIcons.FAVORITE,
                        "icon-buttons",
                        "M3IconButton",
                        CatalogComponents.example(
                                "Overview",
                                "Standard icon actions and selectable icon actions.",
                                true,
                                CatalogSamples::iconButtons
                        ),
                        CatalogComponents.example(
                                "Standard icon button",
                                "A baseline small, round icon-only action.",
                                false,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.SMALL,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Disabled icon button",
                                "An unavailable icon-only action.",
                                false,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.SMALL,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND,
                                        true
                                )
                        ),
                        CatalogComponents.example(
                                "Extra-small icon button",
                                "Icon button using the extra-small expressive size.",
                                true,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.EXTRA_SMALL,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Small icon button",
                                "Icon button using the baseline small size.",
                                false,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.SMALL,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Medium icon button",
                                "Icon button using the medium expressive size.",
                                true,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Large icon button",
                                "Icon button using the large expressive size.",
                                true,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.LARGE,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Extra-large icon button",
                                "Icon button using the extra-large expressive size.",
                                true,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.EXTRA_LARGE,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Narrow icon button",
                                "A medium icon button using the narrow width role.",
                                true,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.NARROW,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Default-width icon button",
                                "A medium icon button using the default width role.",
                                true,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Wide icon button",
                                "A medium icon button using the wide width role.",
                                true,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.WIDE,
                                        M3ButtonShape.ROUND,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Square icon button",
                                "A medium icon button with a rounded-square container.",
                                true,
                                () -> CatalogActionSamples.iconButton(
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.SQUARE,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Standard toggle icon button",
                                "A selected toggle icon button with a transparent resting container.",
                                false,
                                () -> CatalogActionSamples.iconToggleButton(
                                        M3IconToggleButtonVariant.STANDARD,
                                        true,
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND
                                )
                        ),
                        CatalogComponents.example(
                                "Filled toggle icon button",
                                "A selected toggle icon button using the filled color treatment.",
                                true,
                                () -> CatalogActionSamples.iconToggleButton(
                                        M3IconToggleButtonVariant.FILLED,
                                        true,
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND
                                )
                        ),
                        CatalogComponents.example(
                                "Tonal toggle icon button",
                                "A selected toggle icon button using the tonal color treatment.",
                                false,
                                () -> CatalogActionSamples.iconToggleButton(
                                        M3IconToggleButtonVariant.TONAL,
                                        true,
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND
                                )
                        ),
                        CatalogComponents.example(
                                "Outlined toggle icon button",
                                "An unselected toggle icon button with a persistent outline.",
                                false,
                                () -> CatalogActionSamples.iconToggleButton(
                                        M3IconToggleButtonVariant.OUTLINED,
                                        false,
                                        M3ButtonSize.MEDIUM,
                                        M3IconButtonWidth.DEFAULT,
                                        M3ButtonShape.ROUND
                                )
                        ),
                        CatalogComponents.example(
                                "Single-select toggle group",
                                "An icon toggle group that preserves one selection.",
                                false,
                                () -> CatalogActionSamples.iconToggleGroup(M3SelectionMode.SINGLE)
                        ),
                        CatalogComponents.example(
                                "Multi-select toggle group",
                                "An icon toggle group with independent selections.",
                                false,
                                () -> CatalogActionSamples.iconToggleGroup(M3SelectionMode.MULTIPLE)
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
                                CatalogActionSamples::menuButton
                        ),
                        CatalogComponents.example(
                                "Standard inline menu",
                                "A non-selectable standard menu rendered inline.",
                                false,
                                () -> CatalogActionSamples.menu(
                                        M3SelectionMode.NONE,
                                        M3MenuColorStyle.STANDARD,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Single-select menu",
                                "A standard menu that preserves one selected item.",
                                false,
                                () -> CatalogActionSamples.menu(
                                        M3SelectionMode.SINGLE,
                                        M3MenuColorStyle.STANDARD,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Multi-select menu",
                                "A standard menu that allows independent selections.",
                                false,
                                () -> CatalogActionSamples.menu(
                                        M3SelectionMode.MULTIPLE,
                                        M3MenuColorStyle.STANDARD,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Vibrant menu",
                                "A single-select menu using the vibrant color mapping.",
                                true,
                                () -> CatalogActionSamples.menu(
                                        M3SelectionMode.SINGLE,
                                        M3MenuColorStyle.VIBRANT,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Submenu",
                                "An inline menu containing a nested destination menu.",
                                false,
                                () -> CatalogActionSamples.menu(
                                        M3SelectionMode.NONE,
                                        M3MenuColorStyle.STANDARD,
                                        true,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Disabled menu item",
                                "A menu that includes an unavailable action.",
                                false,
                                () -> CatalogActionSamples.menu(
                                        M3SelectionMode.NONE,
                                        M3MenuColorStyle.STANDARD,
                                        false,
                                        true
                                )
                        )
                ),
                CatalogComponents.component(
                        "Split buttons",
                        "Split buttons combine a primary action with a menu of related alternatives.",
                        CatalogIcons.SPLIT_BUTTON,
                        "split-button",
                        "M3SplitButton",
                        CatalogComponents.example(
                                "Variant overview",
                                "Primary actions with attached alternative menus.",
                                true,
                                CatalogSamples::splitButtons
                        ),
                        CatalogComponents.example(
                                "Filled split button",
                                "A high-emphasis filled split action.",
                                false,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.FILLED,
                                        M3ButtonSize.SMALL,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Tonal split button",
                                "A medium-emphasis tonal split action.",
                                false,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.TONAL,
                                        M3ButtonSize.SMALL,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Outlined split button",
                                "An outlined split action.",
                                false,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.OUTLINED,
                                        M3ButtonSize.SMALL,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Elevated split button",
                                "An elevated split action separated from its surface.",
                                false,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.ELEVATED,
                                        M3ButtonSize.SMALL,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Extra-small split button",
                                "A split action using the extra-small expressive size.",
                                true,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.TONAL,
                                        M3ButtonSize.EXTRA_SMALL,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Small split button",
                                "A split action using the baseline small size.",
                                false,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.TONAL,
                                        M3ButtonSize.SMALL,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Medium split button",
                                "A split action using the medium expressive size.",
                                true,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.TONAL,
                                        M3ButtonSize.MEDIUM,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Large split button",
                                "A split action using the large expressive size.",
                                true,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.TONAL,
                                        M3ButtonSize.LARGE,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Extra-large split button",
                                "A split action using the extra-large expressive size.",
                                true,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.TONAL,
                                        M3ButtonSize.EXTRA_LARGE,
                                        false,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Disabled split button",
                                "An unavailable split action whose menu is also disabled.",
                                false,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.TONAL,
                                        M3ButtonSize.SMALL,
                                        true,
                                        false
                                )
                        ),
                        CatalogComponents.example(
                                "Nested split-button menu",
                                "A split action with a submenu of additional formats.",
                                true,
                                () -> CatalogActionSamples.splitButton(
                                        M3ButtonVariant.FILLED,
                                        M3ButtonSize.MEDIUM,
                                        false,
                                        true
                                )
                        )
                )
        );
    }
}
