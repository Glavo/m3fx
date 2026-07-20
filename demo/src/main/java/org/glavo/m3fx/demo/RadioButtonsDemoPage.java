// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import org.glavo.m3fx.controls.M3RadioButton;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the RadioButtons component showcase page.
@NotNullByDefault
final class RadioButtonsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    RadioButtonsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the radio button component page.
    Node createContent() {
        ToggleGroup radioGroup = new ToggleGroup();
        M3RadioButton radioOne = new M3RadioButton("Radio A");
        radioOne.setSelected(true);
        M3RadioButton radioTwo = new M3RadioButton("Radio B");
        M3RadioButton disabledUnchecked = new M3RadioButton("Disabled unchecked");
        M3RadioButton disabledSelected = new M3RadioButton("Disabled selected");
        disabledSelected.setSelected(true);
        radioOne.setToggleGroup(radioGroup);
        radioTwo.setToggleGroup(radioGroup);
        disabledUnchecked.setDisable(true);
        disabledSelected.setDisable(true);

        return createGallery(
                createShowcaseGroup("Selection Group", radioOne, radioTwo),
                createShowcaseGroup("Disabled States", disabledUnchecked, disabledSelected)
        );
    }
}
