// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Provides the common interaction model for Material Design 3 setting rows.
///
/// A setting row is one focusable and actionable control. Its trailing control is a visual value indicator rather
/// than an independently interactive child. Pointer and keyboard activation therefore target the row, while
/// concrete setting rows define the value transition that occurs before their action event is fired.
///
/// This type is package-private because applications use one of its concrete subclasses.
@NotNullByDefault
abstract sealed class M3SettingItemBase extends M3ListItemBase
        permits M3SettingItem, M3SwitchSettingItem, M3CheckBoxSettingItem, M3RadioButtonSettingItem {
    /// The common style class assigned to all setting rows.
    private static final String DEFAULT_STYLE_CLASS = "m3-setting-item";

    /// Creates an empty setting row with the specified accessibility role.
    ///
    /// @param accessibleRole the accessibility role of the row
    /// @throws NullPointerException if `accessibleRole` is `null`
    M3SettingItemBase(AccessibleRole accessibleRole) {
        this("", accessibleRole);
    }

    /// Creates a setting row with headline text and the specified accessibility role.
    ///
    /// @param headlineText   the primary row text
    /// @param accessibleRole the accessibility role of the row
    /// @throws NullPointerException if an argument is `null`
    M3SettingItemBase(String headlineText, AccessibleRole accessibleRole) {
        super(headlineText);
        M3ControlStyles.add(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(Objects.requireNonNull(accessibleRole, "accessibleRole"));
    }

    /// Adds a concrete setting-row style class.
    ///
    /// @param styleClass the style class to add
    /// @throws NullPointerException if `styleClass` is `null`
    final void addSettingStyleClass(String styleClass) {
        M3ControlStyles.add(this, styleClass);
    }

    /// Installs a non-interactive trailing value indicator.
    ///
    /// The supplied control remains owned by this row until replaced through the inherited trailing-content API.
    /// It mirrors this row's disabled state, cannot receive focus, and does not receive pointer events. Its value
    /// properties are bound by the concrete setting-row implementation.
    ///
    /// @param indicator the value indicator to display at the logical trailing edge
    /// @throws NullPointerException if `indicator` is `null`
    final void installTrailingIndicator(Control indicator) {
        Objects.requireNonNull(indicator, "indicator");
        indicator.setAccessibleRole(AccessibleRole.NODE);
        indicator.setFocusTraversable(false);
        indicator.setMouseTransparent(true);
        indicator.disableProperty().bind(disabledProperty());
        setTrailing(indicator);
    }
}