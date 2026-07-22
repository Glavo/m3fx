// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import org.glavo.m3fx.internal.M3Accessible;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 settings row with a trailing switch value.
///
/// Activating the row toggles the inherited [#selectedProperty()] before it delivers an action event. Direct changes
/// to that property update the switch presentation without firing an action event. The visible switch is part of the
/// row's presentation and is not an independent focus or pointer target.
///
/// Use this control for an independent on/off preference when the entire row should be clickable. It does not save or
/// restore a preference value; applications own persistence and may bind [#selectedProperty()] to their state.
///
/// See [Material Design switches](https://m3.material.io/components/switch/overview).
@NotNullByDefault
public final class M3SwitchSettingItem extends M3SettingItemBase {
    /// The concrete style class assigned to switch setting rows.
    private static final String DEFAULT_STYLE_CLASS = "m3-switch-setting-item";

    /// The trailing switch presentation owned by this row.
    private final M3Switch indicator = new M3Switch();

    /// Creates an unselected switch setting row with an empty headline.
    public M3SwitchSettingItem() {
        this("");
    }

    /// Creates an unselected switch setting row with the specified headline text.
    ///
    /// @param headlineText the primary row text
    /// @throws NullPointerException if `headlineText` is `null`
    public M3SwitchSettingItem(String headlineText) {
        super(headlineText, AccessibleRole.CHECK_BOX);
        addSettingStyleClass(DEFAULT_STYLE_CLASS);
        installTrailingIndicator(indicator);
        indicator.selectedProperty().bindBidirectional(selectedProperty());
        selectedProperty().addListener((observable, oldValue, newValue) ->
                M3Accessible.notifyToggleStateChanged(this));
    }

    /// Toggles the selected value before dispatching the row action.
    ///
    /// @return always `true`, because an enabled switch setting always has a value transition
    @Override
    boolean prepareAction() {
        setSelected(!isSelected());
        return true;
    }

    /// Returns accessibility attributes for the switch state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when unsupported
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (M3Accessible.isToggleStateAttribute(attribute)) {
            return M3Accessible.toggleState(isSelected());
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }
}