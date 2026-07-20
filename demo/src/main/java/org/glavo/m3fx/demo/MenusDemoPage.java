// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.control.Label;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuColorStyle;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3MenuSectionHeader;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SubMenuItem;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Menus component showcase page.
@NotNullByDefault
final class MenusDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    MenusDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the menu component page.
    Node createContent() {
        M3Menu inlineMenu = new M3Menu(
                new M3MenuSectionHeader("File"),
                createMenuItem("New", "add", "Ctrl+N"),
                createMenuItem("Open", "folder", "Ctrl+O"),
                new M3SubMenuItem(
                        "Open Recent",
                        createMenuItem("Project Alpha", "work", ""),
                        createMenuItem("Project Beta", "bookmark", "")
                ),
                createMenuItem("Save", "save", "Ctrl+S"),
                new M3Divider(),
                new M3MenuSectionHeader("Recent"),
                createMenuItem("Project Alpha", "work", ""),
                createMenuItem("Project Beta", "bookmark", "")
        );

        M3MenuButton menuButton = new M3MenuButton(
                "Open menu",
                new M3MenuSectionHeader("Document"),
                createMenuItem("Duplicate", "bookmark", "Ctrl+D"),
                new M3SubMenuItem(
                        "Move to",
                        createMenuItem("Archive", "archive", ""),
                        createMenuItem("Inbox", "inbox", "")
                ),
                createMenuItem("Rename", "edit", ""),
                new M3Divider(),
                new M3MenuSectionHeader("Danger"),
                createMenuItem("Delete", "delete", "")
        );
        menuButton.setVariant(M3ButtonVariant.OUTLINED);
        menuButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);

        M3MenuItem selected = createMenuItem("Selected item", "check", "");
        M3MenuItem disabledStandard = createMenuItem("Unavailable", "delete", "");
        disabledStandard.setDisable(true);
        M3Menu selectedMenu = new M3Menu(
                selected,
                createMenuItem("Regular item", "label", ""),
                disabledStandard
        );
        selectedMenu.setSelectionMode(M3SelectionMode.SINGLE);
        selectedMenu.setAllowEmptySelection(false);
        selectedMenu.selectIndex(0);

        M3Menu multiSelectMenu = new M3Menu(
                new M3MenuSectionHeader("Visibility"),
                createMenuItem("Icons", "visibility", ""),
                createMenuItem("Labels", "label", ""),
                createMenuItem("Badges", "bookmark", "")
        );
        multiSelectMenu.setSelectionMode(M3SelectionMode.MULTIPLE);
        multiSelectMenu.selectIndex(1);
        multiSelectMenu.selectIndex(3);

        M3MenuItem vibrantSelected = createMenuItem("Pinned", "bookmark", "");
        M3MenuItem disabledVibrant = createMenuItem("Unavailable", "delete", "");
        disabledVibrant.setDisable(true);
        M3Menu vibrantMenu = new M3Menu(
                new M3MenuSectionHeader("Vibrant"),
                vibrantSelected,
                createMenuItem("Shared", "share", ""),
                createMenuItem("Archived", "archive", ""),
                disabledVibrant
        );
        vibrantMenu.setColorStyle(M3MenuColorStyle.VIBRANT);
        vibrantMenu.setSelectionMode(M3SelectionMode.SINGLE);
        vibrantMenu.setAllowEmptySelection(false);
        vibrantMenu.select(vibrantSelected);

        return createGallery(
                createShowcaseGroup("Menu Button", menuButton),
                createShowcaseGroup("Inline Menus", inlineMenu, selectedMenu, multiSelectMenu, vibrantMenu)
        );
    }

    /// Creates a sample menu item.
    private static M3MenuItem createMenuItem(String text, String iconText, String shortcutText) {
        M3MenuItem item = new M3MenuItem(text);
        item.setLeading(createNavigationIcon(iconText));
        if (!shortcutText.isBlank()) {
            Label shortcut = new Label(shortcutText);
            shortcut.getStyleClass().add("demo-menu-shortcut");
            item.setTrailing(shortcut);
        }
        return item;
    }
}
