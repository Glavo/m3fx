// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonGroupVariant;
import org.glavo.m3fx.controls.M3ButtonShape;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3IconButtonWidth;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SplitButton;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the AllButtons component showcase page.
@NotNullByDefault
final class AllButtonsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    AllButtonsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the all-buttons overview component page.
    Node createContent() {
        M3Button disabledFilled = new M3Button("Disabled", M3ButtonVariant.FILLED);
        disabledFilled.setDisable(true);

        M3IconToggleButtonGroup toggleGroup = createIconToggleGroup(
                M3IconToggleButtonVariant.TONAL,
                "bookmark",
                "schedule",
                "notifications"
        );

        M3FabMenu fabMenu = createFabMenu();
        fabMenu.setExpanded(true);

        M3ButtonGroup actionGroup = createButtonGroup(
                new M3Button("Archive", M3ButtonVariant.TONAL),
                new M3Button("Share", M3ButtonVariant.TONAL),
                new M3Button("Edit", M3ButtonVariant.TONAL)
        );
        actionGroup.setVariant(M3ButtonGroupVariant.STANDARD);

        M3SegmentedButtonGroup segmentedGroup = createDayWeekMonthSegmentedGroup();
        M3SplitButton splitButton = createSplitButton("Create", M3ButtonVariant.TONAL);

        return createGallery(
                createShowcaseGroup(
                        "Common Buttons",
                        new M3Button("Filled", M3ButtonVariant.FILLED),
                        new M3Button("Tonal", M3ButtonVariant.TONAL),
                        new M3Button("Outlined", M3ButtonVariant.OUTLINED),
                        new M3Button("Text", M3ButtonVariant.TEXT),
                        new M3Button("Elevated", M3ButtonVariant.ELEVATED),
                        disabledFilled
                ),
                createShowcaseGroup(
                        "Icon Buttons",
                        createIconButton("search"),
                        createIconButton("favorite", M3ButtonSize.MEDIUM, M3IconButtonWidth.WIDE, M3ButtonShape.ROUND),
                        toggleGroup
                ),
                createShowcaseGroup(
                        "Floating Actions",
                        createFab("add", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.REGULAR),
                        createExtendedFab(
                                "Create",
                                "add",
                                M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        fabMenu
                ),
                createShowcaseGroup(
                        "Grouped Actions",
                        actionGroup,
                        segmentedGroup,
                        splitButton
                )
        );
    }
}
