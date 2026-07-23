// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.ActionEvent;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import org.glavo.m3fx.internal.M3Accessible;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 settings row with a trailing switch value.
///
/// Activating the row toggles the inherited [#selectedProperty()] before it delivers an action event. Direct changes
/// to that property update the switch presentation without firing an action event. The trailing [M3Switch] mirrors
/// the row value, keeps full switch motion and drag behavior, and accepts pointer input independently of the row body
/// while remaining non-focusable so the row stays the keyboard and accessibility target.
///
/// Use this control for an independent on/off preference when the entire row should be clickable and the switch
/// should also be clickable or draggable. It does not save or restore a preference value; applications own
/// persistence and may bind [#selectedProperty()] to their state.
///
/// See [Material Design switches](https://m3.material.io/components/switch/overview).
@NotNullByDefault
public final class M3SwitchSettingItem extends M3SettingItemBase {
    /// The concrete style class assigned to switch setting rows.
    private static final String DEFAULT_STYLE_CLASS = "m3-switch-setting-item";

    /// The trailing switch owned by this row.
    private final M3Switch switchControl = new M3Switch();

    /// Whether this row is currently forwarding a nested switch action and must not toggle again.
    private boolean forwardingSwitchAction;

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
        installTrailingControl(switchControl, true);
        switchControl.selectedProperty().bindBidirectional(selectedProperty());
        selectedProperty().addListener((observable, oldValue, newValue) ->
                M3Accessible.notifyToggleStateChanged(this));
        // Nested switch activation already changed selected state; only notify row action listeners.
        switchControl.addEventHandler(ActionEvent.ACTION, this::handleSwitchAction);
    }

    /// Returns the trailing switch owned by this row.
    ///
    /// The switch is not focus traversable. Applications may customize icons and geometry, but must not reparent the
    /// node or replace the bidirectional binding of [#selectedProperty()].
    ///
    /// @return the trailing switch
    public M3Switch getSwitch() {
        return switchControl;
    }

    /// Toggles the selected value before dispatching the row action.
    ///
    /// @return `false` when a nested switch action is already being forwarded; otherwise `true`
    @Override
    boolean prepareAction() {
        if (forwardingSwitchAction) {
            return false;
        }
        setSelected(!isSelected());
        return true;
    }

    /// Forwards nested switch activation to row action listeners without toggling twice.
    private void handleSwitchAction(ActionEvent event) {
        if (isDisabled() || forwardingSwitchAction) {
            return;
        }
        forwardingSwitchAction = true;
        try {
            // The switch already toggled selectedProperty through its fire()/drag commit path.
            dispatchActionEvent();
        } finally {
            forwardingSwitchAction = false;
        }
        event.consume();
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
