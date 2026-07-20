// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3Toolbar;
import org.glavo.m3fx.controls.M3ToolbarColorStyle;
import org.glavo.m3fx.controls.M3ToolbarVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds the Toolbars component showcase page.
@NotNullByDefault
final class ToolbarsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ToolbarsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the toolbar component page.
    Node createContent() {
        M3Toolbar floatingStandard = createToolbar(
                M3ToolbarVariant.FLOATING,
                M3ToolbarColorStyle.STANDARD,
                Orientation.HORIZONTAL,
                "archive",
                "share",
                "edit",
                "more"
        );
        M3Toolbar floatingVibrant = createToolbar(
                M3ToolbarVariant.FLOATING,
                M3ToolbarColorStyle.VIBRANT,
                Orientation.HORIZONTAL,
                "bold",
                "italic",
                "underline",
                "tune",
                "visibility"
        );
        M3Toolbar dockedStandard = createToolbar(
                M3ToolbarVariant.DOCKED,
                M3ToolbarColorStyle.STANDARD,
                Orientation.HORIZONTAL,
                "home",
                "search",
                "notifications",
                "person"
        );
        dockedStandard.setMaxWidth(Double.MAX_VALUE);

        M3Toolbar dockedVibrant = createToolbar(
                M3ToolbarVariant.DOCKED,
                M3ToolbarColorStyle.VIBRANT,
                Orientation.HORIZONTAL,
                "search",
                "favorite",
                "settings",
                "more"
        );
        dockedVibrant.setMaxWidth(Double.MAX_VALUE);

        M3Toolbar vertical = createToolbar(
                M3ToolbarVariant.FLOATING,
                M3ToolbarColorStyle.VIBRANT,
                Orientation.VERTICAL,
                "search",
                "favorite",
                "settings",
                "more"
        );

        M3Toolbar pairedToolbar = createToolbar(
                M3ToolbarVariant.FLOATING,
                M3ToolbarColorStyle.STANDARD,
                Orientation.HORIZONTAL,
                "archive",
                "share",
                "more"
        );
        HBox toolbarWithFab = new HBox(
                8.0,
                pairedToolbar,
                createFab(
                        "add",
                        M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                        M3FloatingActionButtonSize.REGULAR
                )
        );
        toolbarWithFab.setAlignment(Pos.CENTER_LEFT);
        toolbarWithFab.getStyleClass().add("demo-toolbar-fab-pair");

        return createGallery(
                createShowcaseGroup("Floating Standard", floatingStandard),
                createShowcaseGroup("Floating Vibrant", floatingVibrant),
                createFullWidthShowcaseGroup("Docked Standard", createToolbarPreview(dockedStandard)),
                createFullWidthShowcaseGroup("Docked Vibrant", createToolbarPreview(dockedVibrant)),
                createShowcaseGroup("Floating With FAB", toolbarWithFab),
                createShowcaseGroup("Vertical Floating", vertical)
        );
    }

    /// Creates a toolbar sample.
    private static M3Toolbar createToolbar(
            M3ToolbarVariant variant,
            M3ToolbarColorStyle colorStyle,
            Orientation orientation,
            String... iconNames
    ) {
        Objects.requireNonNull(iconNames, "iconNames");

        M3Toolbar toolbar = new M3Toolbar();
        toolbar.setVariant(variant);
        toolbar.setColorStyle(colorStyle);
        toolbar.setOrientation(orientation);
        for (int index = 0; index < iconNames.length; index++) {
            String iconName = iconNames[index];
            if (index == 1) {
                M3IconToggleButton selected = createIconToggleButton(
                        iconName,
                        M3IconToggleButtonVariant.STANDARD,
                        true
                );
                selected.setAccessibleText(toolbarIconAccessibleText(iconName));
                toolbar.getItems().add(selected);
            } else {
                M3IconButton button = createToolbarIconButton(iconName);
                button.setDisable(index == iconNames.length - 1);
                toolbar.getItems().add(button);
            }
        }
        return toolbar;
    }

    /// Creates a preview surface for a toolbar sample.
    private static StackPane createToolbarPreview(M3Toolbar toolbar) {
        StackPane preview = new StackPane(toolbar);
        preview.getStyleClass().add("demo-toolbar-preview");
        preview.setMinWidth(560.0);
        preview.setPrefWidth(760.0);
        preview.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(toolbar, Pos.CENTER_LEFT);
        return preview;
    }

    /// Creates a sample icon button for toolbar action slots.
    private static M3IconButton createToolbarIconButton(String iconName) {
        Node icon = createIconViewport(DemoIcons.onSurfaceVariant(iconName));
        M3IconButton button = new M3IconButton(icon);
        button.setAccessibleText(toolbarIconAccessibleText(iconName));
        return button;
    }

    /// Returns the accessible action text used by toolbar icon buttons.
    private static String toolbarIconAccessibleText(String iconName) {
        return switch (iconName) {
            case "archive" -> "Archive";
            case "bold" -> "Bold";
            case "edit" -> "Edit";
            case "favorite" -> "Favorite";
            case "home" -> "Home";
            case "italic" -> "Italic";
            case "more" -> "More options";
            case "notifications" -> "Notifications";
            case "person" -> "Account";
            case "search" -> "Search";
            case "settings" -> "Settings";
            case "share" -> "Share";
            case "tune" -> "Tune";
            case "underline" -> "Underline";
            case "visibility" -> "Visibility";
            default -> throw new IllegalArgumentException("Unknown toolbar icon: " + iconName);
        };
    }
}
