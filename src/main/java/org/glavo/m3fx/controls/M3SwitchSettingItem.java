// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import org.glavo.m3fx.internal.M3Accessible;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// An M3FX settings row with a trailing switch value.
///
/// Material Design 3 does not define a switch settings-row component. This extension combines list-item
/// presentation with an interactive switch and is styled with Material tokens.
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
/// See [Material Design lists](https://m3.material.io/components/lists/overview) and
/// [Material Design switches](https://m3.material.io/components/switch/overview).
@NotNullByDefault
public final class M3SwitchSettingItem extends M3SettingItemBase {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-switch-setting-item";

    /// The trailing switch owned by this row.
    private final M3Switch switchControl = new M3Switch(this::fire);

    /// Prevents a request handler from recursively activating the row.
    private boolean requestingToggle;

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
        switchControl.animationsEnabledProperty().bind(animationsEnabledProperty());
        switchControl.selectedProperty().bind(selectedProperty());
        selectedProperty().addListener((observable, oldValue, newValue) ->
                M3Accessible.notifyToggleStateChanged(this));
    }

    /// Returns the trailing switch. Its selection is bound to the row and its activation requests
    /// a change on the row, including when the user drags the handle.
    public M3Switch getSwitch() {
        return switchControl;
    }

    /// The optional handler for selection requests.
    private @Nullable ObjectProperty<@Nullable EventHandler<M3SwitchEvent>> onToggleRequest;

    /// Returns the handler for proposed changes, or null.
    public @Nullable EventHandler<M3SwitchEvent> getOnToggleRequest() {
        return onToggleRequest == null ? null : onToggleRequest.get();
    }

    /// Sets the request handler, or removes it when null.
    public void setOnToggleRequest(@Nullable EventHandler<M3SwitchEvent> handler) {
        onToggleRequestProperty().set(handler);
    }

    /// Invoked before pointer, keyboard, or accessible activation changes selection, even when
    /// selection is bound. Consume the request to update an application model instead.
    /// A consumed request or a bound selection does not produce a row action.
    public ObjectProperty<@Nullable EventHandler<M3SwitchEvent>> onToggleRequestProperty() {
        if (onToggleRequest == null) {
            onToggleRequest = new SimpleObjectProperty<>(this, "onToggleRequest") {
                /// Registers the handler on the row's event dispatch chain.
                @Override protected void invalidated() {
                    setEventHandler(M3SwitchEvent.TOGGLE_REQUEST, get());
                }
            };
        }
        return onToggleRequest;
    }

    /// Requests the proposed state before accepting the default change.
    @Override
    boolean prepareAction() {
        if (requestingToggle) return false;
        boolean target = !isSelected();
        var request = new M3SwitchEvent(this, target);
        requestingToggle = true;
        try {
            fireEvent(request);
        } finally {
            requestingToggle = false;
        }
        if (request.isConsumed() || isDisabled() || selectedProperty().isBound() || isSelected() == target) return false;
        setSelected(target);
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
