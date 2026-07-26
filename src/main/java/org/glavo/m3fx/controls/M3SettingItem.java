// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleRole;
import org.jetbrains.annotations.NotNullByDefault;

/// An actionable M3FX settings row styled with Material tokens.
///
/// Material Design 3 does not define a settings-row component. This extension applies list-item presentation and
/// button-like activation to a settings entry that opens another screen, presents a dialog, or performs an immediate
/// operation. It does not imply navigation, persistence, or a selected value. Install an action handler with
/// [#setOnAction(javafx.event.EventHandler)] to define the operation performed when the row is activated.
///
/// The row has the normal list-item content slots. Its action is delivered for pointer activation, `Space`, `Enter`,
/// and [#fire()] while the row is enabled.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview) and
/// [Material Design buttons](https://m3.material.io/components/buttons/overview).
@NotNullByDefault
public final class M3SettingItem extends M3SettingItemBase {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-action-setting-item";

    /// Creates an empty action setting row.
    public M3SettingItem() {
        this("");
    }

    /// Creates an action setting row with the specified headline text.
    ///
    /// @param headlineText the primary row text
    /// @throws NullPointerException if `headlineText` is `null`
    public M3SettingItem(String headlineText) {
        super(headlineText, AccessibleRole.BUTTON);
        addSettingStyleClass(DEFAULT_STYLE_CLASS);
    }
}
