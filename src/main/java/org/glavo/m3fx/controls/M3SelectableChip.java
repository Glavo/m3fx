// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import org.glavo.m3fx.internal.M3Accessible;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Base class for Material Design 3 chips with persistent selected state.
///
/// Filter and input chips use this class to expose a JavaFX selected property, the `selected` pseudo-class,
/// toggle-button accessibility semantics, and toggle-on-fire behavior. Command-only assist and suggestion chips
/// do not expose this state.
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public abstract sealed class M3SelectableChip extends M3Chip permits M3FilterChip, M3InputChip {
    /// The pseudo-class representing selected state.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// Creates a selectable chip with fixed semantic styling.
    ///
    /// @param text              the text displayed by the chip
    /// @param graphic           the optional graphic displayed with the text
    /// @param variantStyleClass the style class identifying the concrete chip kind
    M3SelectableChip(String text, @Nullable Node graphic, String variantStyleClass) {
        super(text, graphic, variantStyleClass);
        setAccessibleRole(AccessibleRole.TOGGLE_BUTTON);
    }

    /// The persistent selected state.
    ///
    /// Direct property changes update presentation and accessibility without firing an action event.
    ///
    /// @defaultValue `false`
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates visual and accessibility state when selection changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
            // JavaFX 14 has no aggregate TOGGLE_STATE attribute; the helper is a no-op there.
            M3Accessible.notifyToggleStateChanged(M3SelectableChip.this);
        }
    };

    /// Returns whether this chip is selected.
    ///
    /// @return `true` when this chip is selected
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this chip is selected.
    ///
    /// @param selected whether this chip should be selected
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the `selected` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `selected` property
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// Returns accessibility attributes for the selected state.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        // JavaFX 14 has no TOGGLE_STATE enum constant, so test the optional runtime value first.
        if (M3Accessible.isToggleStateAttribute(attribute)) {
            return M3Accessible.toggleState(isSelected());
        }
        if (attribute == AccessibleAttribute.SELECTED) {
            return isSelected();
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }

    /// Toggles this chip and fires its action event.
    ///
    /// Selection changes before synchronous event delivery. This method is a no-op while the chip is disabled.
    @Override
    public void fire() {
        if (!isDisabled()) {
            setSelected(!isSelected());
            super.fire();
        }
    }
}
