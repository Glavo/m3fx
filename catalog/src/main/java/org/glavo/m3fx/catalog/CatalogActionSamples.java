// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonGroupVariant;
import org.glavo.m3fx.controls.M3ButtonShape;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconButtonWidth;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuColorStyle;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3MenuSectionHeader;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3SubMenuItem;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3Toolbar;
import org.glavo.m3fx.controls.M3ToolbarColorStyle;
import org.glavo.m3fx.controls.M3ToolbarVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Creates focused action-control samples used by independent Catalog example routes.
///
/// Each method exposes one size, shape, color, state, or interaction dimension so that the component page does not
/// collapse the complete M3FX action-control matrix into a single gallery.
@NotNullByDefault
final class CatalogActionSamples {
    /// Prevents instantiation of this factory class.
    private CatalogActionSamples() {
    }

    /// Creates a standard or connected group of labeled or toggle actions.
    ///
    /// @param variant the group geometry
    /// @param size the shared Material button size
    /// @param toggle whether the group contains selectable icon toggles
    /// @param multiple whether toggle items may be selected independently
    /// @return the configured button group
    static Node buttonGroup(
            M3ButtonGroupVariant variant,
            M3ButtonSize size,
            boolean toggle,
            boolean multiple
    ) {
        M3ButtonGroup group = new M3ButtonGroup();
        group.setVariant(variant);
        group.setSize(size);
        if (!toggle) {
            group.getItems().addAll(
                    new M3Button("Archive", M3ButtonVariant.TONAL),
                    new M3Button("Share", M3ButtonVariant.TONAL),
                    new M3Button("Edit", M3ButtonVariant.TONAL)
            );
            return group;
        }

        M3IconToggleButton leading = toggleButton(CatalogIcons.EDIT, false);
        M3IconToggleButton middle = toggleButton(CatalogIcons.FAVORITE, true);
        M3IconToggleButton trailing = toggleButton(CatalogIcons.SETTINGS, multiple);
        group.getItems().addAll(leading, middle, trailing);
        if (!multiple) {
            leading.setOnAction(event -> selectOnly(leading, leading, middle, trailing));
            middle.setOnAction(event -> selectOnly(middle, leading, middle, trailing));
            trailing.setOnAction(event -> selectOnly(trailing, leading, middle, trailing));
        }
        return group;
    }

    /// Creates enabled and disabled buttons for one emphasis variant.
    ///
    /// @param variant the button emphasis variant
    /// @return the variant state comparison
    static Node buttonVariant(M3ButtonVariant variant) {
        M3Button enabled = new M3Button(buttonVariantLabel(variant), variant);
        M3Button disabled = new M3Button("Disabled", variant);
        disabled.setDisable(true);
        return CatalogSamples.row(enabled, disabled);
    }

    /// Creates a button in one Material Expressive size.
    ///
    /// @param size the button size
    /// @return buttons using the requested size
    static Node buttonSize(M3ButtonSize size) {
        M3Button filled = sizedButton(sizeLabel(size), M3ButtonVariant.FILLED, size, M3ButtonShape.ROUND);
        M3Button tonal = sizedButton("Tonal", M3ButtonVariant.TONAL, size, M3ButtonShape.ROUND);
        return CatalogSamples.row(filled, tonal);
    }

    /// Creates round and square comparisons focused on one resting shape.
    ///
    /// @param shape the requested resting button shape
    /// @return buttons using the requested shape
    static Node buttonShape(M3ButtonShape shape) {
        M3Button filled = sizedButton(shapeLabel(shape), M3ButtonVariant.FILLED, M3ButtonSize.MEDIUM, shape);
        M3Button outlined = sizedButton("Outlined", M3ButtonVariant.OUTLINED, M3ButtonSize.MEDIUM, shape);
        return CatalogSamples.row(filled, outlined);
    }

    /// Creates a labeled button with a leading icon at one expressive size.
    ///
    /// @param size the button size
    /// @param shape the button shape
    /// @return the icon-and-label button
    static Node buttonWithIcon(M3ButtonSize size, M3ButtonShape shape) {
        M3Button button = sizedButton("Create", M3ButtonVariant.TONAL, size, shape);
        button.setGraphic(CatalogSamples.icon(CatalogIcons.ADD));
        return button;
    }

    /// Creates a filled button whose local colors override the active theme tokens.
    ///
    /// @return the locally colored button
    static Node buttonLocalColors() {
        M3Button button = new M3Button("Local colors", M3ButtonVariant.FILLED);
        button.setContainerColor(Color.web("#006A6A"));
        button.setContentColor(Color.WHITE);
        return button;
    }

    /// Creates a button that reports activation within the example.
    ///
    /// @return the interactive button and result text
    static Node buttonInteraction() {
        M3Text result = new M3Text("No action yet", M3TextRole.BODY_MEDIUM);
        M3Button button = new M3Button("Run action", M3ButtonVariant.FILLED);
        button.setOnAction(event -> result.setText("Action completed"));
        return CatalogSamples.column(button, result);
    }

    /// Creates an extended floating action button using one color role.
    ///
    /// @param variant the floating action button color role
    /// @return the configured extended FAB
    static Node extendedFabVariant(M3FloatingActionButtonVariant variant) {
        M3FloatingActionButton button = new M3FloatingActionButton(
                fabVariantLabel(variant),
                CatalogSamples.icon(CatalogIcons.EDIT)
        );
        button.setVariant(variant);
        return button;
    }

    /// Creates an extended floating action button using one expressive size.
    ///
    /// @param size the floating action button size
    /// @return the configured extended FAB
    static Node extendedFabSize(M3FloatingActionButtonSize size) {
        M3FloatingActionButton button = new M3FloatingActionButton(
                fabSizeLabel(size),
                CatalogSamples.icon(CatalogIcons.ADD)
        );
        button.setVariant(M3FloatingActionButtonVariant.PRIMARY_CONTAINER);
        button.setSize(size);
        return button;
    }

    /// Creates a disabled extended floating action button.
    ///
    /// @return the disabled extended FAB
    static Node disabledExtendedFab() {
        M3FloatingActionButton button = new M3FloatingActionButton(
                "Unavailable",
                CatalogSamples.icon(CatalogIcons.ADD)
        );
        button.setDisable(true);
        return button;
    }

    /// Creates a collapsed or expanded floating action button menu.
    ///
    /// @param expanded whether the menu begins expanded
    /// @param variant the color family used by the menu actions
    /// @return the configured FAB menu
    static Node fabMenu(boolean expanded, M3FloatingActionButtonVariant variant) {
        M3FloatingActionButton toggle = new M3FloatingActionButton(CatalogSamples.icon(CatalogIcons.ADD));
        toggle.setVariant(variant);
        M3FabMenu menu = new M3FabMenu(toggle);
        M3FloatingActionButton create = new M3FloatingActionButton(
                "New document",
                CatalogSamples.icon(CatalogIcons.EDIT)
        );
        create.setVariant(variant);
        M3FloatingActionButton favorite = new M3FloatingActionButton(
                "Favorite",
                CatalogSamples.icon(CatalogIcons.FAVORITE)
        );
        favorite.setVariant(variant);
        menu.getItems().addAll(create, favorite);
        menu.setExpanded(expanded);
        return menu;
    }

    /// Creates an icon-only floating action button in one size and color role.
    ///
    /// @param variant the floating action button color role
    /// @param size the floating action button size
    /// @param disabled whether the action is disabled
    /// @return the configured FAB
    static Node floatingActionButton(
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size,
            boolean disabled
    ) {
        M3FloatingActionButton button = new M3FloatingActionButton(CatalogSamples.icon(CatalogIcons.ADD));
        button.setVariant(variant);
        button.setSize(size);
        button.setDisable(disabled);
        return button;
    }

    /// Creates a floating or docked toolbar with the requested color and orientation.
    ///
    /// @param variant whether the toolbar floats or docks
    /// @param colorStyle the standard or vibrant color mapping
    /// @param orientation the toolbar orientation
    /// @param includeFab whether a related FAB is shown beside the toolbar
    /// @return the configured toolbar sample
    static Node toolbar(
            M3ToolbarVariant variant,
            M3ToolbarColorStyle colorStyle,
            Orientation orientation,
            boolean includeFab
    ) {
        M3Toolbar toolbar = new M3Toolbar();
        toolbar.setVariant(variant);
        toolbar.setColorStyle(colorStyle);
        toolbar.setOrientation(orientation);
        toolbar.getItems().addAll(
                CatalogSamples.iconButton(CatalogIcons.EDIT, "Edit"),
                CatalogSamples.iconButton(CatalogIcons.FAVORITE, "Favorite"),
                CatalogSamples.iconButton(CatalogIcons.SETTINGS, "Settings")
        );
        if (variant == M3ToolbarVariant.DOCKED) {
            CatalogSamples.configureResponsiveWidth(toolbar, 560.0);
        }
        if (!includeFab) {
            return toolbar;
        }
        return CatalogSamples.row(
                toolbar,
                floatingActionButton(
                        M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                        M3FloatingActionButtonSize.REGULAR,
                        false
                )
        );
    }

    /// Creates a regular icon button in one size, width, shape, and enabled state.
    ///
    /// @param size the shared button size
    /// @param width the expressive icon-button width
    /// @param shape the resting container shape
    /// @param disabled whether the action is disabled
    /// @return the configured icon button
    static Node iconButton(
            M3ButtonSize size,
            M3IconButtonWidth width,
            M3ButtonShape shape,
            boolean disabled
    ) {
        M3IconButton button = CatalogSamples.iconButton(CatalogIcons.FAVORITE, "Favorite");
        button.setSize(size);
        button.setWidthRole(width);
        button.setButtonShape(shape);
        button.setDisable(disabled);
        return button;
    }

    /// Creates a toggle icon button in one variant and selection state.
    ///
    /// @param variant the toggle color variant
    /// @param selected whether the button begins selected
    /// @param size the shared button size
    /// @param width the expressive icon-button width
    /// @param shape the resting container shape
    /// @return the configured toggle icon button
    static Node iconToggleButton(
            M3IconToggleButtonVariant variant,
            boolean selected,
            M3ButtonSize size,
            M3IconButtonWidth width,
            M3ButtonShape shape
    ) {
        M3IconToggleButton button = new M3IconToggleButton(CatalogSamples.icon(CatalogIcons.FAVORITE));
        button.setVariant(variant);
        button.setSelected(selected);
        button.setSize(size);
        button.setWidthRole(width);
        button.setButtonShape(shape);
        return button;
    }

    /// Creates a single- or multiple-selection icon toggle group.
    ///
    /// @param selectionMode the group selection mode
    /// @return the configured icon toggle group
    static Node iconToggleGroup(M3SelectionMode selectionMode) {
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
        group.setSelectionMode(selectionMode);
        group.getItems().addAll(
                toggleButton(CatalogIcons.EDIT, false),
                toggleButton(CatalogIcons.FAVORITE, false),
                toggleButton(CatalogIcons.SETTINGS, false)
        );
        group.selectIndex(0);
        if (selectionMode == M3SelectionMode.MULTIPLE) {
            group.selectIndex(2);
        }
        return group;
    }

    /// Creates a menu-owning action button.
    ///
    /// @return the configured menu button
    static Node menuButton() {
        M3MenuButton button = new M3MenuButton(
                "Open menu",
                new M3MenuSectionHeader("Document"),
                menuItem("Duplicate", CatalogIcons.EDIT, "Ctrl+D"),
                new M3SubMenuItem(
                        "Move to",
                        menuItem("Archive", CatalogIcons.BOTTOM_SHEET, ""),
                        menuItem("Favorites", CatalogIcons.FAVORITE, "")
                ),
                new M3Divider(),
                menuItem("Delete", CatalogIcons.CLOSE, "")
        );
        button.setVariant(M3ButtonVariant.OUTLINED);
        return button;
    }

    /// Creates an inline menu with the requested selection and color behavior.
    ///
    /// @param selectionMode the menu selection mode
    /// @param colorStyle the standard or vibrant color mapping
    /// @param includeSubmenu whether the menu includes a nested submenu
    /// @param includeDisabled whether the menu includes a disabled item
    /// @return the configured inline menu
    static Node menu(
            M3SelectionMode selectionMode,
            M3MenuColorStyle colorStyle,
            boolean includeSubmenu,
            boolean includeDisabled
    ) {
        M3Menu menu = new M3Menu();
        menu.setSelectionMode(selectionMode);
        menu.setColorStyle(colorStyle);
        menu.getItems().add(new M3MenuSectionHeader("Actions"));
        menu.getItems().add(menuItem("Create", CatalogIcons.ADD, "Ctrl+N"));
        if (includeSubmenu) {
            menu.getItems().add(new M3SubMenuItem(
                    "Move to",
                    menuItem("Archive", CatalogIcons.BOTTOM_SHEET, ""),
                    menuItem("Favorites", CatalogIcons.FAVORITE, "")
            ));
        } else {
            menu.getItems().add(menuItem("Edit", CatalogIcons.EDIT, ""));
        }
        M3MenuItem disabled = menuItem("Unavailable", CatalogIcons.CLOSE, "");
        disabled.setDisable(includeDisabled);
        menu.getItems().add(disabled);
        if (selectionMode != M3SelectionMode.NONE) {
            menu.selectIndex(1);
            if (selectionMode == M3SelectionMode.MULTIPLE) {
                menu.selectIndex(menu.getItems().size() - 1);
            }
        }
        return menu;
    }

    /// Creates a split button using one button variant and size.
    ///
    /// @param variant the button color and elevation variant
    /// @param size the shared button size
    /// @param disabled whether the split button is disabled
    /// @param nestedMenu whether the attached menu contains a submenu
    /// @return the configured split button
    static Node splitButton(
            M3ButtonVariant variant,
            M3ButtonSize size,
            boolean disabled,
            boolean nestedMenu
    ) {
        M3SplitButton button = new M3SplitButton("Export");
        button.setVariant(variant);
        button.setSize(size);
        button.setGraphic(CatalogSamples.icon(CatalogIcons.ARROW_FORWARD));
        button.getItems().addAll(
                new M3MenuItem("Export as PDF"),
                new M3MenuItem("Export as image")
        );
        if (nestedMenu) {
            button.getItems().add(new M3SubMenuItem(
                    "More formats",
                    new M3MenuItem("Export as JSON"),
                    new M3MenuItem("Export as Markdown")
            ));
        }
        button.setDisable(disabled);
        return button;
    }

    /// Creates a labeled button with explicit size and shape roles.
    ///
    /// @param text the button label
    /// @param variant the button emphasis variant
    /// @param size the Material button size
    /// @param shape the resting button shape
    /// @return the configured button
    private static M3Button sizedButton(
            String text,
            M3ButtonVariant variant,
            M3ButtonSize size,
            M3ButtonShape shape
    ) {
        M3Button button = new M3Button(text, variant);
        button.setSize(size);
        button.setButtonShape(shape);
        return button;
    }

    /// Creates a tonal icon toggle used inside groups.
    ///
    /// @param path the icon path
    /// @param selected whether the toggle begins selected
    /// @return the configured toggle
    private static M3IconToggleButton toggleButton(String path, boolean selected) {
        M3IconToggleButton button = new M3IconToggleButton(CatalogSamples.icon(path));
        button.setVariant(M3IconToggleButtonVariant.TONAL);
        button.setSelected(selected);
        return button;
    }

    /// Selects one toggle and clears its peers.
    ///
    /// @param selected the toggle that remains selected
    /// @param buttons all toggles in the logical group
    private static void selectOnly(M3IconToggleButton selected, M3IconToggleButton... buttons) {
        for (M3IconToggleButton button : buttons) {
            button.setSelected(button == selected);
        }
    }

    /// Creates one menu item with a leading icon and optional shortcut label.
    ///
    /// @param text the menu item label
    /// @param iconPath the leading icon path
    /// @param shortcutText the shortcut label, or an empty string
    /// @return the configured menu item
    private static M3MenuItem menuItem(String text, String iconPath, String shortcutText) {
        M3MenuItem item = new M3MenuItem(text, CatalogSamples.icon(iconPath));
        if (!shortcutText.isBlank()) {
            item.setTrailing(new Label(shortcutText));
        }
        return item;
    }

    /// Returns a human-readable label for one button variant.
    ///
    /// @param variant the button variant
    /// @return the display label
    private static String buttonVariantLabel(M3ButtonVariant variant) {
        return switch (variant) {
            case FILLED -> "Filled";
            case TONAL -> "Tonal";
            case OUTLINED -> "Outlined";
            case TEXT -> "Text";
            case ELEVATED -> "Elevated";
        };
    }

    /// Returns a compact label for one button size.
    ///
    /// @param size the button size
    /// @return the display label
    private static String sizeLabel(M3ButtonSize size) {
        return switch (size) {
            case EXTRA_SMALL -> "Extra small";
            case SMALL -> "Small";
            case MEDIUM -> "Medium";
            case LARGE -> "Large";
            case EXTRA_LARGE -> "Extra large";
        };
    }

    /// Returns a display label for one button shape.
    ///
    /// @param shape the button shape
    /// @return the display label
    private static String shapeLabel(M3ButtonShape shape) {
        return switch (shape) {
            case ROUND -> "Round";
            case SQUARE -> "Square";
        };
    }

    /// Returns a display label for one FAB variant.
    ///
    /// @param variant the FAB color role
    /// @return the display label
    private static String fabVariantLabel(M3FloatingActionButtonVariant variant) {
        return switch (variant) {
            case SURFACE -> "Surface";
            case PRIMARY_CONTAINER -> "Primary tonal";
            case SECONDARY_CONTAINER -> "Secondary tonal";
            case TERTIARY_CONTAINER -> "Tertiary tonal";
            case PRIMARY -> "Primary";
            case SECONDARY -> "Secondary";
            case TERTIARY -> "Tertiary";
        };
    }

    /// Returns a display label for one FAB size.
    ///
    /// @param size the FAB size
    /// @return the display label
    private static String fabSizeLabel(M3FloatingActionButtonSize size) {
        return switch (size) {
            case SMALL -> "Small";
            case REGULAR -> "Regular";
            case MEDIUM -> "Medium";
            case LARGE -> "Large";
        };
    }
}
