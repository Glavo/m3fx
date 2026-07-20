// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3CheckBox;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Checkboxes component showcase page.
@NotNullByDefault
final class CheckboxesDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    CheckboxesDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the checkbox component page.
    Node createContent() {
        M3CheckBox unchecked = createCheckBox("Unchecked", false, false, false, false, false);
        M3CheckBox checked = createCheckBox("Checked", true, false, false, false, false);
        M3CheckBox indeterminate = createCheckBox("Indeterminate", false, true, true, false, false);
        M3CheckBox threeState = createCheckBox("Three-state cycle", false, false, true, false, false);

        M3CheckBox errorUnchecked = createCheckBox("Error unchecked", false, false, false, true, false);
        M3CheckBox errorChecked = createCheckBox("Error checked", true, false, false, true, false);
        M3CheckBox errorIndeterminate = createCheckBox("Error indeterminate", false, true, false, true, false);

        M3CheckBox disabledUnchecked = createCheckBox("Disabled unchecked", false, false, false, false, true);
        M3CheckBox disabledChecked = createCheckBox("Disabled checked", true, false, false, false, true);
        M3CheckBox disabledIndeterminate =
                createCheckBox("Disabled indeterminate", false, true, false, false, true);

        return createGallery(
                createShowcaseGroup("Interactive States", unchecked, checked, indeterminate, threeState),
                createShowcaseGroup("Error States", errorUnchecked, errorChecked, errorIndeterminate),
                createShowcaseGroup("Disabled States", disabledUnchecked, disabledChecked, disabledIndeterminate)
        );
    }

    /// Creates a checkbox sample.
    private static M3CheckBox createCheckBox(
            String text,
            boolean selected,
            boolean indeterminate,
            boolean allowIndeterminate,
            boolean error,
            boolean disabled
    ) {
        M3CheckBox checkBox = new M3CheckBox(text);
        checkBox.setSelected(selected);
        checkBox.setIndeterminate(indeterminate);
        checkBox.setAllowIndeterminate(allowIndeterminate);
        checkBox.setError(error);
        checkBox.setDisable(disabled);
        return checkBox;
    }
}
