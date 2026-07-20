// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonGroupVariant;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.List;

/// Builds the ButtonGroups component showcase page.
@NotNullByDefault
final class ButtonGroupsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ButtonGroupsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the button group component page.
    Node createContent() {
        M3ButtonGroup standardGroup = createButtonGroup(
                new M3Button("Archive", M3ButtonVariant.TONAL),
                new M3Button("Share", M3ButtonVariant.TONAL),
                new M3Button("Edit", M3ButtonVariant.TONAL)
        );
        standardGroup.setVariant(M3ButtonGroupVariant.STANDARD);
        standardGroup.setSize(M3ButtonSize.MEDIUM);

        M3ButtonGroup standardSingleSelect = createToggleButtonGroup(
                M3ButtonGroupVariant.STANDARD,
                false,
                "format_align_left",
                "format_align_center",
                "format_align_right"
        );
        standardSingleSelect.setSize(M3ButtonSize.MEDIUM);

        M3ButtonGroup connectedSingleSelect = createToggleButtonGroup(
                M3ButtonGroupVariant.CONNECTED,
                false,
                "format_align_left",
                "format_align_center",
                "format_align_right"
        );
        connectedSingleSelect.setSize(M3ButtonSize.MEDIUM);

        M3ButtonGroup connectedMultiSelect = createToggleButtonGroup(
                M3ButtonGroupVariant.CONNECTED,
                true,
                "bold",
                "italic",
                "underline"
        );
        connectedMultiSelect.setSize(M3ButtonSize.MEDIUM);

        M3ButtonGroup small = createButtonGroup(
                new M3Button("Previous", M3ButtonVariant.TONAL),
                new M3Button("Next", M3ButtonVariant.TONAL)
        );
        small.setVariant(M3ButtonGroupVariant.STANDARD);
        small.setSize(M3ButtonSize.SMALL);

        M3ButtonGroup large = createButtonGroup(
                new M3Button("Decline", M3ButtonVariant.OUTLINED),
                new M3Button("Accept", M3ButtonVariant.FILLED)
        );
        large.setVariant(M3ButtonGroupVariant.STANDARD);
        large.setSize(M3ButtonSize.LARGE);

        return createGallery(
                createShowcaseGroup("Standard Actions", standardGroup),
                createShowcaseGroup("Standard Toggle Selection", standardSingleSelect),
                createFullWidthShowcaseGroup("Connected Single Select", connectedSingleSelect),
                createFullWidthShowcaseGroup("Connected Multi Select", connectedMultiSelect),
                createShowcaseGroup("Size Scale", small, large)
        );
    }

    /// Creates a selectable icon-toggle button group for one Material button-group variant.
    ///
    /// @param variant   the standard or connected group behavior
    /// @param multiple  whether independent multi-selection is allowed
    /// @param iconNames the icon names used by the toggle buttons
    /// @return the configured selectable button group
    private static M3ButtonGroup createToggleButtonGroup(
            M3ButtonGroupVariant variant,
            boolean multiple,
            String... iconNames
    ) {
        M3ButtonGroup group = new M3ButtonGroup();
        group.setVariant(variant);
        List<M3IconToggleButton> buttons = new ArrayList<>(iconNames.length);
        for (int index = 0; index < iconNames.length; index++) {
            M3IconToggleButton button = createIconToggleButton(
                    iconNames[index],
                    M3IconToggleButtonVariant.TONAL,
                    index == 0 || multiple && index == iconNames.length - 1
            );
            buttons.add(button);
            group.getItems().add(button);
        }
        if (!multiple) {
            for (M3IconToggleButton button : buttons) {
                button.setOnAction(event -> {
                    for (M3IconToggleButton candidate : buttons) {
                        candidate.setSelected(candidate == button);
                    }
                });
            }
        }
        return group;
    }
}
