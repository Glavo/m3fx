// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.glavo.m3fx.internal.M3Accessible;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 settings row with a trailing radio-button value.
///
/// An ungrouped row toggles its inherited [#selectedProperty()] with each activation. When it belongs to a
/// [ToggleGroup], activating an unselected row selects it and clears the group's previous selection. Activating the
/// selected member of a group does nothing and does not fire an action event, preserving the normal JavaFX radio
/// group contract.
///
/// The visible radio button is part of the row's presentation and is not an independently focusable or clickable
/// child. The row itself implements [Toggle], so it can share a [ToggleGroup] with standard JavaFX toggles and
/// [M3RadioButton] controls.
///
/// See [Material Design radio buttons](https://m3.material.io/components/radio-button/overview).
@NotNullByDefault
public final class M3RadioButtonSettingItem extends M3SettingItemBase implements Toggle {
    /// The concrete style class assigned to radio-button setting rows.
    private static final String DEFAULT_STYLE_CLASS = "m3-radio-button-setting-item";

    /// The trailing radio-button presentation owned by this row.
    private final M3RadioButton indicator = new M3RadioButton();

    /// Creates an unselected radio-button setting row with an empty headline and no toggle group.
    public M3RadioButtonSettingItem() {
        this("");
    }

    /// Creates an unselected radio-button setting row with the specified headline and no toggle group.
    ///
    /// @param headlineText the primary row text
    /// @throws NullPointerException if `headlineText` is `null`
    public M3RadioButtonSettingItem(String headlineText) {
        super(headlineText, AccessibleRole.RADIO_BUTTON);
        addSettingStyleClass(DEFAULT_STYLE_CLASS);
        installTrailingIndicator(indicator);
        indicator.selectedProperty().bindBidirectional(selectedProperty());
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            M3Accessible.notifyToggleStateChanged(this);
            updateToggleGroupSelection(newValue);
        });
    }

    /// The toggle group that coordinates this row's selected value.
    ///
    /// @defaultValue `null`
    private @Nullable ObjectProperty<@Nullable ToggleGroup> toggleGroup;

    /// Returns the toggle group that coordinates this row.
    ///
    /// @return the toggle group, or `null` when this row is independent
    @Override
    public @Nullable ToggleGroup getToggleGroup() {
        return toggleGroup == null ? null : toggleGroup.get();
    }

    /// Sets the toggle group that coordinates this row.
    ///
    /// Changing the group synchronizes membership in the old and new groups. Passing `null` removes the row from its
    /// current group without changing [#selectedProperty()].
    ///
    /// @param toggleGroup the new toggle group, or `null`
    @Override
    public void setToggleGroup(@Nullable ToggleGroup toggleGroup) {
        toggleGroupProperty().set(toggleGroup);
    }

    /// Returns the observable, bindable toggle-group property.
    ///
    /// The property is `null` by default. Its value remains synchronized with [ToggleGroup#getToggles()], including
    /// group membership changes initiated through that list.
    ///
    /// @return the toggle-group property
    @Override
    public ObjectProperty<@Nullable ToggleGroup> toggleGroupProperty() {
        if (toggleGroup == null) {
            toggleGroup = new ObjectPropertyBase<>() {
                /// The group most recently synchronized by this property.
                private @Nullable ToggleGroup oldGroup;

                /// Whether membership synchronization is already in progress.
                private boolean updatingGroup;

                /// Synchronizes group membership after the property changes.
                @Override
                protected void invalidated() {
                    if (updatingGroup) {
                        return;
                    }

                    @Nullable ToggleGroup newGroup = get();
                    if (newGroup == oldGroup) {
                        return;
                    }

                    updatingGroup = true;
                    try {
                        if (oldGroup != null) {
                            oldGroup.getToggles().remove(M3RadioButtonSettingItem.this);
                        }
                        if (newGroup != null && !newGroup.getToggles().contains(M3RadioButtonSettingItem.this)) {
                            newGroup.getToggles().add(M3RadioButtonSettingItem.this);
                        }
                    } finally {
                        updatingGroup = false;
                        oldGroup = newGroup;
                    }
                }

                /// Returns the owning setting row.
                @Override
                public Object getBean() {
                    return M3RadioButtonSettingItem.this;
                }

                /// Returns the JavaFX property name.
                @Override
                public String getName() {
                    return "toggleGroup";
                }
            };
        }
        return toggleGroup;
    }

    /// Synchronizes the JavaFX toggle group after this row's selected value changes.
    ///
    /// @param selected whether this row is now selected
    private void updateToggleGroupSelection(boolean selected) {
        @Nullable ToggleGroup group = getToggleGroup();
        if (group == null) {
            return;
        }
        if (selected) {
            group.selectToggle(this);
        } else if (group.getSelectedToggle() == this) {
            group.selectToggle(null);
        }
    }

    /// Selects this row or toggles its independent selected value before dispatching the row action.
    ///
    /// @return `false` when an already selected group member was activated; otherwise `true`
    @Override
    boolean prepareAction() {
        if (getToggleGroup() != null && isSelected()) {
            return false;
        }
        setSelected(!isSelected());
        return true;
    }

    /// Returns accessibility attributes for the radio-button state.
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