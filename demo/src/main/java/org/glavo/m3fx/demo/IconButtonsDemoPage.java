// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3ButtonShape;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconButtonWidth;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the IconButtons component showcase page.
@NotNullByDefault
final class IconButtonsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    IconButtonsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the icon button component page.
    Node createContent() {
        M3IconButton disabledIcon = createIconButton("info");
        disabledIcon.setDisable(true);

        return createGallery(
                createShowcaseGroup(
                        "Icon Buttons",
                        createIconButton("info"),
                        createIconButton("add"),
                        disabledIcon
                ),
                createShowcaseGroup(
                        "Icon Button Sizes",
                        createIconButton("star", M3ButtonSize.EXTRA_SMALL, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("star", M3ButtonSize.SMALL, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("star", M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("star", M3ButtonSize.LARGE, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("star", M3ButtonSize.EXTRA_LARGE, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND)
                ),
                createShowcaseGroup(
                        "Icon Button Widths",
                        createIconButton("search", M3ButtonSize.MEDIUM, M3IconButtonWidth.NARROW, M3ButtonShape.ROUND),
                        createIconButton("search", M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("search", M3ButtonSize.MEDIUM, M3IconButtonWidth.WIDE, M3ButtonShape.ROUND)
                ),
                createShowcaseGroup(
                        "Icon Button Shapes",
                        createIconButton("favorite", M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("favorite", M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.SQUARE),
                        createIconButton("favorite", M3ButtonSize.LARGE, M3IconButtonWidth.DEFAULT, M3ButtonShape.SQUARE)
                ),
                createShowcaseGroup(
                        "Toggle Icon Buttons",
                        createIconToggleGroup(
                                M3IconToggleButtonVariant.STANDARD,
                                "star",
                                "favorite",
                                "tune",
                                "visibility"
                        ),
                        createIconToggleGroup(
                                M3IconToggleButtonVariant.TONAL,
                                "bookmark",
                                "schedule",
                                "notifications"
                        ),
                        createFormattingToggleGroup(),
                        createIconToggleButton("delete", M3IconToggleButtonVariant.TONAL, false)
                ),
                createShowcaseGroup(
                        "Toggle Size And Shape",
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.EXTRA_SMALL, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.SMALL, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.LARGE, M3IconButtonWidth.WIDE, M3ButtonShape.SQUARE),
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.EXTRA_LARGE, M3IconButtonWidth.WIDE, M3ButtonShape.SQUARE)
                )
        );
    }

    /// Creates a selected filled favorite toggle icon button with Material Expressive sizing roles.
    private static M3IconToggleButton createExpressiveFavoriteToggleButton(
            M3ButtonSize size,
            M3IconButtonWidth widthRole,
            M3ButtonShape shape
    ) {
        Node icon = createIconViewport(DemoIcons.onSurfaceVariant("favorite"), defaultIconButtonGlyphSize(size));
        M3IconToggleButton button = new M3IconToggleButton(icon);
        button.setVariant(M3IconToggleButtonVariant.FILLED);
        button.setSelected(true);
        button.setSize(size);
        button.setWidthRole(widthRole);
        button.setButtonShape(shape);
        return button;
    }

    /// Creates the formatting multi-selection toggle icon button group.
    private static M3IconToggleButtonGroup createFormattingToggleGroup() {
        M3IconToggleButton firstButton = createIconToggleButton("bold", M3IconToggleButtonVariant.OUTLINED, false);
        M3IconToggleButton secondButton = createIconToggleButton("italic", M3IconToggleButtonVariant.OUTLINED, false);
        M3IconToggleButton thirdButton = createIconToggleButton("underline", M3IconToggleButtonVariant.OUTLINED, false);
        M3IconToggleButtonGroup group = createIconToggleButtonGroup(
                firstButton,
                secondButton,
                thirdButton
        );
        group.setSelectionMode(M3SelectionMode.MULTIPLE);
        group.selectIndex(0);
        group.selectIndex(2);
        return group;
    }
}
