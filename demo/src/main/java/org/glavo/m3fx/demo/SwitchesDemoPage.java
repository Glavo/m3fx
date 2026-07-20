// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

import org.glavo.m3fx.controls.M3Switch;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Switches component showcase page.
@NotNullByDefault
final class SwitchesDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SwitchesDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the switch component page.
    Node createContent() {
        M3Switch onSwitch = new M3Switch("On");
        onSwitch.setSelected(true);
        M3Switch offSwitch = new M3Switch("Off");
        M3Switch disabledOffSwitch = new M3Switch("Disabled off");
        M3Switch disabledOnSwitch = new M3Switch("Disabled on");
        disabledOnSwitch.setSelected(true);
        disabledOffSwitch.setDisable(true);
        disabledOnSwitch.setDisable(true);

        M3Switch disabledIconOff = new M3Switch("Disabled icon off");
        disabledIconOff.setUnselectedIcon(createSwitchHandleIcon("close"));
        disabledIconOff.setDisable(true);
        M3Switch disabledIconOn = new M3Switch("Disabled icon on");
        disabledIconOn.setSelectedIcon(createSwitchHandleIcon("check"));
        disabledIconOn.setSelected(true);
        disabledIconOn.setDisable(true);

        M3Switch selectedIconOff = new M3Switch("Selected icon off");
        selectedIconOff.setSelectedIcon(createSwitchHandleIcon("check"));
        M3Switch selectedIconOn = new M3Switch("Selected icon on");
        selectedIconOn.setSelectedIcon(createSwitchHandleIcon("check"));
        selectedIconOn.setSelected(true);

        M3Switch bothIconsOff = new M3Switch("Both icons off");
        bothIconsOff.setSelectedIcon(createSwitchHandleIcon("check"));
        bothIconsOff.setUnselectedIcon(createSwitchHandleIcon("close"));
        M3Switch bothIconsOn = new M3Switch("Both icons on");
        bothIconsOn.setSelectedIcon(createSwitchHandleIcon("check"));
        bothIconsOn.setUnselectedIcon(createSwitchHandleIcon("close"));
        bothIconsOn.setSelected(true);

        return createGallery(
                createShowcaseGroup("Interactive States", onSwitch, offSwitch),
                createShowcaseGroup(
                        "Handle Icons",
                        selectedIconOff,
                        selectedIconOn,
                        bothIconsOff,
                        bothIconsOn
                ),
                createShowcaseGroup(
                        "Disabled States",
                        disabledOffSwitch,
                        disabledOnSwitch,
                        disabledIconOff,
                        disabledIconOn
                )
        );
    }

    /// Creates a 16-pixel vector icon whose paint is supplied by the switch handle state.
    private static SVGPath createSwitchHandleIcon(String iconName) {
        SVGPath icon = DemoIcons.primary(iconName);
        icon.getStyleClass().remove(DemoIcons.PRIMARY_STYLE_CLASS);
        icon.setScaleX(2.0 / 3.0);
        icon.setScaleY(2.0 / 3.0);
        return icon;
    }
}
